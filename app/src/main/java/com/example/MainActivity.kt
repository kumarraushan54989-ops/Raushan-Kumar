package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.QuickKartRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.navigation.QuickKartMainScaffold
import com.example.ui.theme.QuickKartTheme
import com.example.ui.viewmodel.QuickKartViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      QuickKartTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
          AppNavigation()
        }
      }
    }
  }
}

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
  val listener = FirebaseAuth.AuthStateListener { auth ->
    trySend(auth.currentUser)
  }
  addAuthStateListener(listener)
  awaitClose { removeAuthStateListener(listener) }
}

@Composable
fun AppNavigation(auth: FirebaseAuth = Firebase.auth) {
  val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
  val user = currentUser

  if (user == null) {
    AuthScreen(onAuthSuccess = {})
  } else {
    val uid = user.uid
    val displayName = user.displayName ?: "QuickKart Shopper"
    val email = user.email ?: ""

    key(uid) {
      val viewModel: QuickKartViewModel = viewModel(
        key = uid,
        factory = viewModelFactory {
          initializer {
            val app = checkNotNull(this[APPLICATION_KEY]) {
              "APPLICATION_KEY missing from CreationExtras"
            }
            val databaseId = app.getString(R.string.firestore_database_id)
            val db = FirebaseFirestore.getInstance(databaseId)
            QuickKartViewModel(
              repository = QuickKartRepository(db, auth),
              currentUserId = uid,
              userDisplayName = displayName,
              userEmail = email
            )
          }
        }
      )

      Surface(modifier = Modifier.fillMaxSize()) {
        QuickKartMainScaffold(
          viewModel = viewModel,
          userEmail = email,
          userDisplayName = displayName
        )
      }
    }
  }
}
