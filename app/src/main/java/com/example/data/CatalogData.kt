package com.example.data

import com.example.model.DeliveryHub
import com.example.model.Product
import com.example.model.ProductCategory
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object CatalogData {

  val fulfillmentHubs = listOf(
    DeliveryHub("Koramangala Express Hub", "Bengaluru", 12.9352, 77.6245),
    DeliveryHub("Andheri West Hub", "Mumbai", 19.1364, 72.8296),
    DeliveryHub("Connaught Place Hub", "New Delhi", 28.6315, 77.2167),
    DeliveryHub("Hitech City Hub", "Hyderabad", 17.4435, 78.3772)
  )

  fun nearestHubFor(lat: Double, lng: Double): DeliveryHub {
    return fulfillmentHubs.minByOrNull { hub ->
      haversineKm(lat, lng, hub.latitude, hub.longitude)
    } ?: fulfillmentHubs.first()
  }

  fun haversineKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val earthRadiusKm = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a =
      sin(dLat / 2) * sin(dLat / 2) +
        cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2) * sin(dLng / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return earthRadiusKm * c
  }

  val promoCoupons = mapOf(
    "QUICK10" to "10% instant off (up to ₹2,000)",
    "SUPER500" to "Flat ₹500 off on orders above ₹2,000",
    "FREEDEL" to "Free Express 60-min Delivery"
  )

  val curatedProducts = listOf(
    Product(
      id = "prod_phone_novapro",
      title = "NovaPro X5 5G (Cosmic Blue, 256 GB)",
      brand = "NovaTech",
      categoryName = ProductCategory.MOBILES.label,
      price = 24999L,
      mrp = 31999L,
      rating = 4.6,
      reviewCount = 4820L,
      highlights = listOf("8 GB RAM | 256 GB ROM", "6.7\" 120Hz AMOLED Display", "50MP OIS Sony Camera", "5000mAh 67W Turbo Charge"),
      description = "Flagship-grade 5G performance with an ultra-bright 120Hz AMOLED panel and OIS night photography.",
      badge = "Bestseller",
      imageKey = "mobiles",
      deliveryDays = 1L
    ),
    Product(
      id = "prod_phone_pixelite",
      title = "AeroOne Ultra 5G (Obsidian Black, 128 GB)",
      brand = "AeroMobile",
      categoryName = ProductCategory.MOBILES.label,
      price = 18499L,
      mrp = 23999L,
      rating = 4.4,
      reviewCount = 2910L,
      highlights = listOf("8 GB RAM | 128 GB ROM", "Snapdragon 7s Gen 3", "64MP Dual AI Camera", "5500mAh All-Day Battery"),
      description = "Smooth everyday flagship killer designed for mobile gaming, sharp portraits, and rapid charging.",
      badge = "Top Deal",
      imageKey = "mobiles",
      deliveryDays = 1L
    ),
    Product(
      id = "prod_elec_anc_headphones",
      title = "SonicPulse Studio ANC Wireless Headphones",
      brand = "SonicPulse",
      categoryName = ProductCategory.ELECTRONICS.label,
      price = 3499L,
      mrp = 6999L,
      rating = 4.7,
      reviewCount = 6340L,
      highlights = listOf("45dB Hybrid Active Noise Cancellation", "60 Hrs Battery Playback", "Dual-Device Multipoint BT 5.3", "Spatial Audio Ready"),
      description = "Studio-tuned 40mm dynamic drivers with plush memory foam earcups and crystal-clear call mics.",
      badge = "50% Off",
      imageKey = "audio",
      deliveryDays = 1L
    ),
    Product(
      id = "prod_elec_smartwatch",
      title = "ChronoFit AMOLED GPS Smartwatch",
      brand = "ChronoFit",
      categoryName = ProductCategory.ELECTRONICS.label,
      price = 2999L,
      mrp = 5999L,
      rating = 4.5,
      reviewCount = 3780L,
      highlights = listOf("1.43\" Always-On AMOLED", "Built-in Dual-Band GPS", "Bluetooth Calling & SpO2", "IP68 Swim-Proof"),
      description = "Precision fitness and route tracking watch with 100+ sports modes and 10-day battery life.",
      badge = "Assured",
      imageKey = "watch",
      deliveryDays = 1L
    ),
    Product(
      id = "prod_elec_laptop",
      title = "ZenBook Air 14 OLED Thin & Light Laptop",
      brand = "Vertex",
      categoryName = ProductCategory.ELECTRONICS.label,
      price = 54990L,
      mrp = 72990L,
      rating = 4.8,
      reviewCount = 1120L,
      highlights = listOf("16 GB LPDDR5 | 512 GB NVMe SSD", "14\" 2.8K OLED Display", "1.18 kg Aluminum Chassis", "Backlit Keyboard + Wi-Fi 6E"),
      description = "Featherweight creator laptop built for coding, design, and marathon battery sessions.",
      badge = "Hot Pick",
      imageKey = "laptop",
      deliveryDays = 2L
    ),
    Product(
      id = "prod_elec_powerbank",
      title = "VoltCore 20000mAh 65W PD Power Bank",
      brand = "VoltCore",
      categoryName = ProductCategory.ELECTRONICS.label,
      price = 1999L,
      mrp = 3499L,
      rating = 4.5,
      reviewCount = 5230L,
      highlights = listOf("65W Laptop & Phone Fast Charge", "Dual USB-C + USB-A Ports", "Digital LED Percentage Display", "Flight Safe Li-Polymer"),
      description = "High-density fast-charging powerhouse capable of topping up laptops, tablets, and phones simultaneously.",
      badge = "Trending",
      imageKey = "electronics",
      deliveryDays = 1L
    ),
    Product(
      id = "prod_fash_sneakers",
      title = "StrideMax Cloudknit Running Sneakers",
      brand = "StrideMax",
      categoryName = ProductCategory.FASHION.label,
      price = 1799L,
      mrp = 3999L,
      rating = 4.4,
      reviewCount = 8190L,
      highlights = listOf("Breathable 3D Knit Upper", "High-Rebound EVA Cushion Sole", "Anti-Skid Rubber Outsole", "Ultra-Light 220g Build"),
      description = "Engineered for daily road runs and all-day urban comfort with bold contrast styling.",
      badge = "55% Off",
      imageKey = "fashion",
      deliveryDays = 1L
    ),
    Product(
      id = "prod_fash_backpack",
      title = "UrbanTrail 28L Waterproof Laptop Backpack",
      brand = "UrbanTrail",
      categoryName = ProductCategory.FASHION.label,
      price = 1299L,
      mrp = 2799L,
      rating = 4.6,
      reviewCount = 4150L,
      highlights = listOf("Fits up to 16\" Laptop", "USB External Charging Port", "Water-Repellent Oxford Fabric", "Ergonomic Airflow Back"),
      description = "Smart commuter backpack with anti-theft rear pocket, luggage strap, and rain-shield zippers.",
      badge = "Assured",
      imageKey = "fashion",
      deliveryDays = 1L
    ),
    Product(
      id = "prod_fash_jacket",
      title = "NordicWear Quilted Bomber Jacket",
      brand = "NordicWear",
      categoryName = ProductCategory.FASHION.label,
      price = 1499L,
      mrp = 3299L,
      rating = 4.3,
      reviewCount = 1940L,
      highlights = listOf("Windproof Matte Shell", "Soft Fleece Inner Lining", "2 Zippered Side Pockets", "Machine Washable"),
      description = "Tailored winter-ready bomber jacket combining lightweight warmth with clean streetwear style.",
      badge = "Flash Deal",
      imageKey = "fashion",
      deliveryDays = 2L
    ),
    Product(
      id = "prod_home_airfryer",
      title = "ChefCraft 4.5L Digital Air Fryer",
      brand = "ChefCraft",
      categoryName = ProductCategory.APPLIANCES.label,
      price = 4299L,
      mrp = 7999L,
      rating = 4.6,
      reviewCount = 3420L,
      highlights = listOf("85% Less Oil Cooking", "8 One-Touch Digital Presets", "Non-Stick Dishwasher Basket", "1500W Rapid Air Circulation"),
      description = "Crisp snacks, roasted veggies, and grilled favorites faster with smart temperature control.",
      badge = "46% Off",
      imageKey = "appliances",
      deliveryDays = 1L
    ),
    Product(
      id = "prod_app_espresso",
      title = "BaristaPro 15-Bar Espresso & Cappuccino Maker",
      brand = "BaristaPro",
      categoryName = ProductCategory.APPLIANCES.label,
      price = 6499L,
      mrp = 10999L,
      rating = 4.5,
      reviewCount = 980L,
      highlights = listOf("15-Bar Italian Pressure Pump", "Swivel Steam Milk Frother", "1.5L Removable Water Tank", "Brushed Steel Body"),
      description = "Cafe-style crema and velvety lattes right on your kitchen counter.",
      badge = "Assured",
      imageKey = "appliances",
      deliveryDays = 2L
    ),
    Product(
      id = "prod_home_lamp",
      title = "LuminaArc Smart RGB + Warm Study Lamp",
      brand = "LuminaArc",
      categoryName = ProductCategory.HOME.label,
      price = 899L,
      mrp = 1999L,
      rating = 4.5,
      reviewCount = 2670L,
      highlights = listOf("Stepless Touch Dimming", "3 Color Temps (3000K-6500K)", "Flicker-Free Eye Care LED", "USB-C Rechargeable"),
      description = "Minimalist desk lamp with adjustable gooseneck and ambient nightlight ring.",
      badge = "Value Pick",
      imageKey = "home",
      deliveryDays = 1L
    ),
    Product(
      id = "prod_home_bedsheet",
      title = "CottonLoom 400TC King Cotton Bedsheet Set",
      brand = "CottonLoom",
      categoryName = ProductCategory.HOME.label,
      price = 1149L,
      mrp = 2499L,
      rating = 4.4,
      reviewCount = 5820L,
      highlights = listOf("100% Combed Egyptian Cotton", "1 King Sheet + 2 Pillow Covers", "Fade-Resistant Reactive Dye", "Breathable Sateen Weave"),
      description = "Hotel-grade softness and breathable weave built for year-round comfort.",
      badge = "Bestseller",
      imageKey = "home",
      deliveryDays = 1L
    ),
    Product(
      id = "prod_beauty_serum",
      title = "GlowLab 10% Vitamin C + Niacinamide Serum",
      brand = "GlowLab",
      categoryName = ProductCategory.BEAUTY.label,
      price = 499L,
      mrp = 899L,
      rating = 4.5,
      reviewCount = 7410L,
      highlights = listOf("Dermatologically Tested", "Fragrance & Paraben Free", "30ml UV-Protected Dropper", "Fast-Absorbing Gel Formula"),
      description = "Daily brightening antioxidant serum for even skin tone and hydration boost.",
      badge = "Top Rated",
      imageKey = "beauty",
      deliveryDays = 1L
    ),
    Product(
      id = "prod_beauty_trimmer",
      title = "BladeEdge Pro Titanium Grooming Kit",
      brand = "BladeEdge",
      categoryName = ProductCategory.BEAUTY.label,
      price = 1249L,
      mrp = 2499L,
      rating = 4.4,
      reviewCount = 4320L,
      highlights = listOf("40 Precision Length Settings", "90 Mins Cordless Runtime", "IPX7 Waterproof Washable", "USB-C Quick Charge"),
      description = "Self-sharpening titanium blades with travel lock and zero-skin-nick guard.",
      badge = "50% Off",
      imageKey = "beauty",
      deliveryDays = 1L
    ),
    Product(
      id = "prod_groc_almonds",
      title = "HarvestGold California Roasted Almonds (1 kg)",
      brand = "HarvestGold",
      categoryName = ProductCategory.GROCERY.label,
      price = 699L,
      mrp = 1150L,
      rating = 4.7,
      reviewCount = 9120L,
      highlights = listOf("100% Premium Nonpareil Grade", "Vacuum-Sealed Freshness Pack", "Zero Added Salt or Oil", "Rich in Protein & Vitamin E"),
      description = "Crunchy, hand-sorted California almonds packed fresh for daily nutrition.",
      badge = "Fresh Stock",
      imageKey = "grocery",
      deliveryDays = 1L
    ),
    Product(
      id = "prod_groc_coffee",
      title = "CoorgRoast 100% Arabica Filter Coffee (500 g)",
      brand = "CoorgRoast",
      categoryName = ProductCategory.GROCERY.label,
      price = 449L,
      mrp = 699L,
      rating = 4.8,
      reviewCount = 3150L,
      highlights = listOf("Single-Estate Chikmagalur Beans", "Medium-Dark Aromatic Roast", "Resealable Aroma Valve Bag", "Zero Chicory Pure Arabica"),
      description = "Rich chocolatey notes and velvety crema crafted for South Indian filter, French press, or pour-over.",
      badge = "Assured",
      imageKey = "grocery",
      deliveryDays = 1L
    )
  )
}
