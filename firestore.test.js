const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_user_123";
const BOB_UID = "bob_user_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function pastTimestamp() {
  return new Date(Date.now() - 5000);
}

function validCartPayload(uid) {
  return {
    userId: uid,
    productId: "prod_phone_1",
    title: "NovaPro X5 5G",
    brand: "NovaTech",
    category: "Mobiles",
    price: 24999,
    mrp: 31999,
    quantity: 1,
    imageKey: "mobiles",
    createdAt: pastTimestamp(),
    updatedAt: pastTimestamp(),
  };
}

function validOrderPayload(uid, status = "PLACED") {
  return {
    userId: uid,
    orderNumber: "QK-10042",
    customerName: "Alice Sharma",
    customerPhone: "9876543210",
    deliveryAddress: "42 MG Road, Indiranagar",
    deliveryCity: "Bengaluru",
    deliveryPincode: "560038",
    deliveryLat: 12.9784,
    deliveryLng: 77.6408,
    hubLat: 12.9352,
    hubLng: 77.6245,
    paymentMethod: "UPI",
    deliverySpeed: "EXPRESS",
    couponCode: "QUICK10",
    subtotal: 24999,
    discount: 2500,
    deliveryFee: 0,
    totalAmount: 22499,
    status,
    itemsSummary: "NovaPro X5 5G x1",
    itemTitles: ["NovaPro X5 5G"],
    itemCount: 1,
    createdAt: pastTimestamp(),
    updatedAt: pastTimestamp(),
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write cart or orders", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("cart").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("orders").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("cart").doc("item_1").set(validCartPayload(ALICE_UID))
  );
});

test("Owner can add, update, and list own cart items", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const cartRef = aliceDb.collection("users").doc(ALICE_UID).collection("cart").doc("prod_phone_1");
  await assertSucceeds(cartRef.set(validCartPayload(ALICE_UID)));
  await assertSucceeds(cartRef.update({ quantity: 2, updatedAt: pastTimestamp() }));
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("cart").get());
});

test("Cross-user access to cart, addresses, and orders is denied", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("cart").doc("prod_phone_1").set(validCartPayload(ALICE_UID))
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("orders").doc("order_1").set(validOrderPayload(ALICE_UID))
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("cart").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("orders").doc("order_1").get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("orders").doc("order_2").set(validOrderPayload(ALICE_UID))
  );
});

test("Shadow update with ghost field and invalid type fails", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const badCart = { ...validCartPayload(ALICE_UID), isAdmin: true };
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("cart").doc("prod_bad").set(badCart)
  );
  const badOrder = { ...validOrderPayload(ALICE_UID, "DELIVERED") };
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("orders").doc("order_skip").set(badOrder)
  );
});

test("Order terminal state locks further updates and prevents deletion", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const orderRef = aliceDb.collection("users").doc(ALICE_UID).collection("orders").doc("order_100");
  await assertSucceeds(orderRef.set(validOrderPayload(ALICE_UID)));
  await assertSucceeds(orderRef.update({ status: "DELIVERED", updatedAt: pastTimestamp() }));
  // Terminal state reached: further update or delete must fail
  await assertFails(orderRef.update({ status: "CANCELLED", updatedAt: pastTimestamp() }));
  await assertFails(orderRef.delete());
});

test("Order batch with line item enforces parent order existence", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  // Orphaned line item without parent order must fail
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("orders")
      .doc("missing_order")
      .collection("items")
      .doc("line_1")
      .set({
        userId: ALICE_UID,
        orderId: "missing_order",
        productId: "prod_phone_1",
        title: "NovaPro X5 5G",
        brand: "NovaTech",
        price: 24999,
        mrp: 31999,
        quantity: 1,
        imageKey: "mobiles",
        createdAt: pastTimestamp(),
      })
  );

  const batch = aliceDb.batch();
  const orderRef = aliceDb.collection("users").doc(ALICE_UID).collection("orders").doc("order_batch_1");
  const itemRef = orderRef.collection("items").doc("line_1");
  batch.set(orderRef, validOrderPayload(ALICE_UID));
  batch.set(itemRef, {
    userId: ALICE_UID,
    orderId: "order_batch_1",
    productId: "prod_phone_1",
    title: "NovaPro X5 5G",
    brand: "NovaTech",
    price: 24999,
    mrp: 31999,
    quantity: 1,
    imageKey: "mobiles",
    createdAt: pastTimestamp(),
  });
  await assertSucceeds(batch.commit());
});

function validProductPayload(uid) {
  return {
    userId: uid,
    title: "NovaPro X5 5G",
    brand: "NovaTech",
    categoryName: "Mobiles",
    price: 24999,
    mrp: 31999,
    rating: 4.6,
    reviewCount: 4820,
    highlights: ["8 GB RAM", "120Hz AMOLED"],
    description: "Flagship 5G smartphone",
    badge: "Bestseller",
    imageKey: "mobiles",
    deliveryDays: 1,
    inStock: true,
    sellerListed: false,
    createdAt: pastTimestamp(),
    updatedAt: pastTimestamp(),
  };
}

test("Owner can create, update, and list products; unauth and cross-user denied", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("products").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("products").doc("prod_1").set(validProductPayload(ALICE_UID))
  );

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const prodRef = aliceDb.collection("users").doc(ALICE_UID).collection("products").doc("prod_1");
  await assertSucceeds(prodRef.set(validProductPayload(ALICE_UID)));
  await assertSucceeds(prodRef.update({ price: 22999, updatedAt: pastTimestamp() }));
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("products").get());

  // Cross-user isolation
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("products").get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("products").doc("prod_2").set(validProductPayload(ALICE_UID))
  );

  // Valid product with photoBase64 succeeds, and updating photoBase64 succeeds
  const photoProdRef = aliceDb.collection("users").doc(ALICE_UID).collection("products").doc("prod_with_photo");
  await assertSucceeds(
    photoProdRef.set({
      ...validProductPayload(ALICE_UID),
      photoBase64: "dGVzdF9waG90b19iYXNlNjQ=",
    })
  );
  await assertSucceeds(
    photoProdRef.update({
      photoBase64: "dXBkYXRlZF9waG90b19iYXNlNjQ=",
      updatedAt: pastTimestamp(),
    })
  );

  // Oversized photoBase64 (> 250000 chars) must fail
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("products").doc("prod_big_photo").set({
      ...validProductPayload(ALICE_UID),
      photoBase64: "a".repeat(250001),
    })
  );

  // Invalid price > mrp and ghost field must fail
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("products").doc("prod_bad_price").set({
      ...validProductPayload(ALICE_UID),
      price: 50000,
      mrp: 31999,
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("products").doc("prod_ghost").set({
      ...validProductPayload(ALICE_UID),
      isAdminFeatured: true,
    })
  );
});

