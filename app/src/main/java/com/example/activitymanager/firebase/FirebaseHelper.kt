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
import com.example.activitymanager.mapper.Activity as ActivityModel
import com.example.activitymanager.dao.UserDao
import com.example.activitymanager.roomEntity.UserEntity
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

class FirebaseHelper {
    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = Firebase.firestore
    
    // Google sign-in client
    private var googleSignInClient: GoogleSignInClient? = null
    
    // Users collection reference
    private val usersCollection = db.collection("users")

    // Activities collection reference
    private val AcitvitiesCollection = db.collection("activities")

    private val TypeCollection = db.collection("activitytypes")
    
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
        userDao: UserDao,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            auth.signInWithEmailAndPassword(email, password).await()
            val uid = auth.currentUser?.uid
            Log.d(TAG, "Login successful: ${auth.currentUser?.uid}")
            if (uid != null) {
                val user = UserEntity(uid = uid, email = email)
                userDao.insertUser(user)
            }
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

    fun checkAuthState() {
        val user = auth.currentUser
        if (user != null) {
            Log.d(TAG, "当前用户已登录: ${user.uid}, email: ${user.email}")
        } else {
            Log.d(TAG, "当前没有用户登录")
        }
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

    // Post Activity
    suspend fun createActivity(
        activity: ActivityModel,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            AcitvitiesCollection.document().set(activity).await()
            Log.d(TAG, "Activity created successfully")
            onSuccess()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create activity", e)
            onError(e.message ?: "Unknown error occurred")
        }
    }

    // Edit Activity
    suspend fun updateActivityByFieldId(
        activity: ActivityModel,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val querySnapshot = AcitvitiesCollection
                .whereEqualTo("id", activity.id)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                val docRef = querySnapshot.documents[0].reference
                docRef.set(activity).await()
                Log.d(TAG, "Activity updated successfully")
                onSuccess()
            } else {
                onError("Activity with id ${activity.id} not found")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to update activity", e)
            onError(e.message ?: "Unknown error occurred")
        }
    }

    // Get all activity types from data collection in firebase
    suspend fun getActivityTypes(): List<String> {
        return try {
            val snapshot = TypeCollection.orderBy("sort").get().await()
            snapshot.documents.mapNotNull { it.getString("name") }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch activity types", e)
            emptyList()
        }
    }

    // get user information
    suspend fun getUserinfoByUid(uid: String): User? {
        return try {
            val snapshot = usersCollection
                .whereEqualTo("uid", uid)
                .get()
                .await()

            snapshot.documents.firstOrNull()?.toObject(User::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user", e)
            null
        }
    }

    suspend fun registerForActivity(
        activityId: String,
        userId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            Log.d("FirebaseHelper", "Starting registration for activity ID: '$activityId'")
            val db = FirebaseFirestore.getInstance()

            // 列出所有活动以检查数据库连接
            val allActivities = db.collection("activities").get().await()
            Log.d("FirebaseHelper", "Total activities in database: ${allActivities.size()}")
            Log.d("FirebaseHelper", "Available activity IDs: ${allActivities.documents.map { it.id }}")

            // 首先检查活动是否存在
            val activityDoc = db.collection("activities").document(activityId).get().await()

            if (!activityDoc.exists()) {
                Log.e("FirebaseHelper", "Activity does not exist: '$activityId'")
                Log.e("FirebaseHelper", "Document data: ${activityDoc.data}")
                Log.e("FirebaseHelper", "Document ID from snapshot: ${activityDoc.id}")
                onError("Activity not found")
                return
            }

            Log.d("FirebaseHelper", "Activity exists, updating participants")

            // 继续执行注册逻辑...
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Exception during registration", e)
            onError(e.message ?: "Failed to register for activity")
        }
    }

    suspend fun checkIfUserRegistered(
        activityId: String,
        userId: String
    ): Boolean {
        try {
            val db = FirebaseFirestore.getInstance()

            val activityDoc = db.collection("activities").document(activityId).get().await()
            val participants = activityDoc.get("participantsIDs") as? List<String> ?: emptyList()
            if (participants.contains(userId)) {
                return true
            }
            return false
        } catch (e: Exception) {
            println("Error checking registration status: ${e.message}")
            return false
        }
    }


    // get activity list
    suspend fun getActivities(
        uid: String? = null,
        type: String? = null,
        startDate: Date? = null,
        endDate: Date? = null
    ): List<ActivityModel> {
        return try {
            var query = AcitvitiesCollection as com.google.firebase.firestore.Query

            if (uid != null) {
                query = query.whereEqualTo("uid", uid)
            }

            if (type != null) {
                query = query.whereEqualTo("type", type)
            }

            if (startDate != null) {
                query = query.whereGreaterThanOrEqualTo("date", startDate)
            }

            if (endDate != null) {
                query = query.whereLessThanOrEqualTo("date", endDate)
            }

            val snapshot = query.get().await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    val id = doc.getString("id") ?: ""
                    val title = doc.getString("title") ?: ""
                    val description = doc.getString("description") ?: ""
                    val date = doc.getDate("date") ?: Date()
                    val location = doc.getString("location") ?: ""
                    val organizer = doc.getString("organizer") ?: ""
                    val uidField = doc.getString("uid") ?: ""
                    val rating = doc.getDouble("rating") ?: 0.0
                    val duration = doc.getString("duration") ?: ""
                    val typeField = doc.getString("type") ?: ""
                    val participants = (doc.getLong("participants") ?: 0).toInt()
                    val participantsIDs = (doc.get("participantsIDs") as? List<String>) ?: emptyList()
                    val isFavorite = doc.getBoolean("isFavorite") ?: false

                    val coordinatesMap = doc.get("coordinates") as? Map<*, *>
                    val lat = coordinatesMap?.get("latitude") as? Double ?: 0.0
                    val lng = coordinatesMap?.get("longitude") as? Double ?: 0.0
                    val coordinates = com.google.android.gms.maps.model.LatLng(lat, lng)

                    ActivityModel(
                        id = id,
                        title = title,
                        description = description,
                        date = date,
                        location = location,
                        organizer = organizer,
                        uid = uidField,
                        rating = rating,
                        duration = duration,
                        type = typeField,
                        participants = participants,
                        participantsIDs = participantsIDs,
                        isFavorite = isFavorite,
                        coordinates = coordinates
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse activity document", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching activities", e)
            emptyList()
        }
    }

    suspend fun deleteActivity(
        activityId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                onError("User not authenticated")
                return
            }
            withContext(Dispatchers.IO) {
                db.collection("activities")
                    .document(activityId)
                    .delete()
                    .await()
            }
            onSuccess()
        } catch (e: Exception) {
            onError(e.message ?: "Unknown error occurred")
        }
    }
} 