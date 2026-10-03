package com.example.data.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.BuildConfig
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

class FirebaseAuthManager(private val context: Context) {

    private val auth: FirebaseAuth? by lazy {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val apiKey = try {
                    val key = BuildConfig.GEMINI_API_KEY
                    if (key.isNullOrBlank() || key == "MY_GEMINI_API_KEY") "" else key
                } catch (_: Throwable) {
                    ""
                }

                // If default app is not initialized via google-services.json, initialize with options
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:488741706128:android:com.aistudio.lifeos")
                    .setProjectId("lifeos-productivity")
                    .setApiKey(if (apiKey.isNotBlank()) apiKey else "AIzaSyDummyKeyForInitializationOnly")
                    .build()
                FirebaseApp.initializeApp(context, options)
            }
            FirebaseAuth.getInstance()
        } catch (e: Throwable) {
            Log.e("FirebaseAuthManager", "Firebase initialization warning: ${e.message}")
            try {
                FirebaseAuth.getInstance()
            } catch (_: Throwable) {
                null
            }
        }
    }

    val currentUser: FirebaseUser?
        get() = try {
            auth?.currentUser
        } catch (_: Throwable) {
            null
        }

    val isSignedIn: Boolean
        get() = currentUser != null

    suspend fun signIn(email: String, pass: String): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        val firebaseAuth = auth ?: return@withContext Result.failure(
            IllegalStateException("Firebase Auth is not initialized. Please ensure google-services.json is present.")
        )
        try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, pass).awaitTask()
            val user = authResult.user ?: return@withContext Result.failure(Exception("Authentication succeeded but user is null"))
            Result.success(user)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, pass: String, displayName: String): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        val firebaseAuth = auth ?: return@withContext Result.failure(
            IllegalStateException("Firebase Auth is not initialized. Please ensure google-services.json is present.")
        )
        try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, pass).awaitTask()
            val user = authResult.user ?: return@withContext Result.failure(Exception("Registration succeeded but user is null"))

            // Update user profile display name
            if (displayName.isNotBlank()) {
                try {
                    val profileUpdates = userProfileChangeRequest {
                        this.displayName = displayName
                    }
                    user.updateProfile(profileUpdates).awaitTask()
                } catch (pe: Throwable) {
                    Log.w("FirebaseAuthManager", "Could not set displayName: ${pe.message}")
                }
            }
            Result.success(user)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(activityContext: Context): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        val firebaseAuth = auth ?: return@withContext Result.failure(
            IllegalStateException("Firebase Auth is not initialized. Please ensure google-services.json is present.")
        )
        try {
            val credentialManager = CredentialManager.create(activityContext)
            val resId = activityContext.resources.getIdentifier("default_web_client_id", "string", activityContext.packageName)
            val webClientId = if (resId != 0) {
                activityContext.getString(resId)
            } else {
                "488741706128-placeholder.apps.googleusercontent.com"
            }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(activityContext, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                val authResult = firebaseAuth.signInWithCredential(authCredential).awaitTask()
                val user = authResult.user ?: return@withContext Result.failure(Exception("Google Sign-In returned null user"))
                Result.success(user)
            } else {
                Result.failure(Exception("Received unexpected credential type from Google"))
            }
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    fun signOut() {
        try {
            auth?.signOut()
        } catch (e: Throwable) {
            Log.e("FirebaseAuthManager", "Error signing out: ${e.message}")
        }
    }
}

// Coroutine Task adapter
suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result ->
        if (continuation.isActive) {
            continuation.resumeWith(Result.success(result))
        }
    }
    addOnFailureListener { exception ->
        if (continuation.isActive) {
            continuation.resumeWith(Result.failure(exception))
        }
    }
    addOnCanceledListener {
        if (continuation.isActive) {
            continuation.cancel()
        }
    }
}
