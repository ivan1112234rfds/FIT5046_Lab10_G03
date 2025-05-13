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
    
    // Google 登录客户端
    private var googleSignInClient: GoogleSignInClient? = null
    
    // 用户集合引用
    private val usersCollection = db.collection("users")
    
    // 初始化 Google 登录
    fun initGoogleSignIn(context: Context, webClientId: String) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        
        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }
    
    // 获取 Google 登录意图
    fun getGoogleSignInIntent() = googleSignInClient?.signInIntent
    
    // 处理 Google 登录结果
    suspend fun handleGoogleSignInResult(
        data: android.content.Intent?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            
            // 使用 Google 账号凭证进行 Firebase 身份验证
            firebaseAuthWithGoogle(account, onSuccess, onError)
        } catch (e: Exception) {
            Log.e(TAG, "Google 登录失败", e)
            onError(e.message ?: "Google 登录失败")
        }
    }
    
    // 使用 Google 凭证进行 Firebase 身份验证
    private suspend fun firebaseAuthWithGoogle(
        account: GoogleSignInAccount,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            
            // 检查是否是新用户
            val isNewUser = authResult.additionalUserInfo?.isNewUser ?: false
            val uid = authResult.user?.uid ?: ""
            
            if (isNewUser) {
                // 创建新用户文档
                val user = User(
                    uid = uid,
                    username = account.displayName ?: "",
                    nickname = account.displayName ?: "",
                    email = account.email ?: ""
                )
                
                usersCollection.document(uid).set(user).await()
            }
            
            Log.d(TAG, "Google 登录成功: $uid")
            onSuccess()
        } catch (e: Exception) {
            Log.e(TAG, "Firebase 使用 Google 凭证登录失败", e)
            onError(e.message ?: "Google 账号验证失败")
        }
    }
    
    // 用户注册
    suspend fun registerUser(
        email: String, 
        password: String, 
        user: User,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            // 创建用户身份验证
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            
            // 获取用户 UID
            val uid = authResult.user?.uid ?: return
            
            // 创建用户文档
            val userWithId = user.copy(uid = uid, email = email)
            
            // 将用户信息存储到 Firestore
            usersCollection.document(uid).set(userWithId).await()
            
            // 发送验证邮件
            auth.currentUser?.sendEmailVerification()
            
            Log.d(TAG, "用户注册成功: $uid")
            onSuccess()
        } catch (e: Exception) {
            Log.e(TAG, "注册失败", e)
            onError(e.message ?: "注册失败")
        }
    }
    
    // 用户登录
    suspend fun loginUser(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            auth.signInWithEmailAndPassword(email, password).await()
            Log.d(TAG, "登录成功: ${auth.currentUser?.uid}")
            onSuccess()
        } catch (e: Exception) {
            Log.e(TAG, "登录失败", e)
            onError(e.message ?: "登录失败")
        }
    }
    
    // 获取当前登录用户
    fun getCurrentUser() = auth.currentUser
    
    // 获取用户数据
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
                onError("未找到用户数据")
            }
        } catch (e: Exception) {
            Log.e(TAG, "获取用户数据失败", e)
            onError(e.message ?: "获取用户数据失败")
        }
    }
    
    // 退出登录
    fun signOut() {
        auth.signOut()
    }
    
    // 发送重置密码邮件
    suspend fun sendPasswordResetEmail(
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            auth.sendPasswordResetEmail(email).await()
            Log.d(TAG, "密码重置邮件已发送: $email")
            onSuccess()
        } catch (e: Exception) {
            Log.e(TAG, "发送密码重置邮件失败", e)
            onError(e.message ?: "发送密码重置邮件失败")
        }
    }
    
    // 退出 Google 登录
    fun signOutGoogle(context: Context) {
        googleSignInClient?.signOut()
        signOut()
    }
    
    companion object {
        private const val TAG = "FirebaseHelper"
        const val RC_SIGN_IN = 9001 // Google 登录请求代码
    }
} 