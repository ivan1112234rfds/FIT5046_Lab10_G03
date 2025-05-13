package com.example.activitymanager.firebase

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.activitymanager.model.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class FirebaseHelper {
    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = Firebase.firestore
    
    // Google sign-in client
    private var googleSignInClient: GoogleSignInClient? = null
    
    // Users collection reference
    private val usersCollection = db.collection("users")
    
    // Initialize Google sign-in
    fun initGoogleSignIn(context: Context, webClientId: String) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        
        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }
    
    // Get Google sign-in intent
    fun getGoogleSignInIntent() = googleSignInClient?.signInIntent
    
    // Handle Google sign-in result
    suspend fun handleGoogleSignInResult(
        data: android.content.Intent?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            
            // Use Google account credentials for Firebase authentication
            firebaseAuthWithGoogle(account, onSuccess, onError)
        } catch (e: Exception) {
            Log.e(TAG, "Google sign-in failed", e)
            onError(e.message ?: "Google sign-in failed")
        }
    }
    
    // Use Google credentials for Firebase authentication
    private suspend fun firebaseAuthWithGoogle(
        account: GoogleSignInAccount,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            
            // Check if the user is new
            val isNewUser = authResult.additionalUserInfo?.isNewUser ?: false
            val uid = authResult.user?.uid ?: ""
            
            if (isNewUser) {
                // Create new user document
                val user = User(
                    uid = uid,
                    username = account.displayName ?: "",
                    nickname = account.displayName ?: "",
                    email = account.email ?: ""
                )
                
                usersCollection.document(uid).set(user).await()
            }
            
            Log.d(TAG, "Google sign-in successful: $uid")
            onSuccess()
        } catch (e: Exception) {
            Log.e(TAG, "Firebase authentication with Google credentials failed", e)
            onError(e.message ?: "Google account verification failed")
        }
    }
    
    // User registration
    suspend fun registerUser(
        email: String, 
        password: String, 
        user: User,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            // Create user authentication
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            
            // Get user UID
            val uid = authResult.user?.uid ?: return
            
            // Create user document
            val userWithId = user.copy(uid = uid, email = email)
            
            // Store user information in Firestore
            usersCollection.document(uid).set(userWithId).await()
            
            // Comment out the automatic email verification
            // auth.currentUser?.sendEmailVerification()
            
            Log.d(TAG, "User registration successful: $uid")
            onSuccess()
        } catch (e: Exception) {
            Log.e(TAG, "Registration failed", e)
            onError(e.message ?: "Registration failed")
        }
    }
    
    // User login
    suspend fun loginUser(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            auth.signInWithEmailAndPassword(email, password).await()
            Log.d(TAG, "Login successful: ${auth.currentUser?.uid}")
            onSuccess()
        } catch (e: Exception) {
            Log.e(TAG, "Login failed", e)
            onError(e.message ?: "Login failed")
        }
    }
    
    // Get current logged-in user
    fun getCurrentUser() = auth.currentUser
    
    // Get user data
    suspend fun getUserData(
        uid: String = auth.currentUser?.uid ?: "",
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val snapshot = usersCollection.document(uid).get().await()
            val user = snapshot.toObject(User::class.java)
            
            if (user != null) {
                onSuccess(user)
            } else {
                onError("User data not found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get user data", e)
            onError(e.message ?: "Failed to get user data")
        }
    }
    
    // Sign out
    fun signOut() {
        auth.signOut()
    }
    
    // Send password reset email
    suspend fun sendPasswordResetEmail(
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            // Use default settings to send password reset email, without custom domain
            auth.sendPasswordResetEmail(email).await()
            
            Log.d(TAG, "Password reset email sent: $email")
            onSuccess()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send password reset email", e)
            onError(e.message ?: "Failed to send password reset email")
        }
    }
    
    // Sign out from Google
    fun signOutGoogle(context: Context) {
        googleSignInClient?.signOut()
        signOut()
    }
    
    // Add a separate method for sending email verification
    suspend fun sendEmailVerification(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                currentUser.sendEmailVerification().await()
                Log.d(TAG, "Verification email sent: ${currentUser.email}")
                onSuccess()
            } else {
                onError("No logged-in user")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send verification email", e)
            onError(e.message ?: "Failed to send verification email")
        }
    }
    
    companion object {
        private const val TAG = "FirebaseHelper"
        const val RC_SIGN_IN = 9001 // Google sign-in request code
    }
} 