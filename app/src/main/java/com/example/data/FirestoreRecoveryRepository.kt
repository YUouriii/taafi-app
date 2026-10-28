package com.example.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.model.DailyCheckIn
import com.example.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

enum class OperationType(val value: String) {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    LIST("list"),
    GET("get"),
    WRITE("write"),
}

fun handleFirestoreError(exception: Exception, operationType: OperationType, path: String?): String {
    val auth = Firebase.auth
    val currentUser = auth.currentUser

    val providerInfoList = currentUser?.providerData?.map { provider ->
        JSONObject().apply {
            put("providerId", provider.providerId)
            put("email", provider.email)
        }
    } ?: emptyList()

    val authInfoJson = JSONObject().apply {
        put("userId", currentUser?.uid)
        put("email", currentUser?.email)
        put("emailVerified", currentUser?.isEmailVerified)
        put("tenantId", currentUser?.tenantId)
        put("providerInfo", JSONArray(providerInfoList))
    }

    val errorInfoJson = JSONObject().apply {
        put("error", exception.message ?: exception.toString())
        put("operationType", operationType.value)
        put("path", path)
        put("authInfo", authInfoJson)
    }

    val jsonString = errorInfoJson.toString()
    Log.e("FirestoreError", "Firestore Error: $jsonString")
    return jsonString
}

class FirestoreRecoveryRepository(
    private val db: FirebaseFirestore
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // Observe all check-ins for the authenticated user
    fun observeCheckIns(): Flow<List<DailyCheckIn>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/checkins"
        val query = db.collection("users").document(uid).collection("checkins")
            .orderBy("createdAt", Query.Direction.DESCENDING)

        emitAll(
            query.snapshots()
                .map { snapshot -> snapshot.toObjects(DailyCheckIn::class.java) }
                .catch { error ->
                    if (error is Exception) {
                        handleFirestoreError(error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    // Save or update today's check-in
    suspend fun saveCheckIn(checkIn: DailyCheckIn) {
        val uid = requireUserId()
        val docId = if (checkIn.id.isNotBlank()) checkIn.id else checkIn.dateString
        val docRef = db.collection("users").document(uid).collection("checkins").document(docId)

        val payload = hashMapOf<String, Any>(
            "userId" to uid,
            "dateString" to checkIn.dateString,
            "moodScore" to checkIn.moodScore,
            "urgeIntensity" to checkIn.urgeIntensity,
            "primaryEmotions" to checkIn.primaryEmotions,
            "triggersEncountered" to checkIn.triggersEncountered,
            "reflectionNotes" to checkIn.reflectionNotes,
            "streakProtected" to checkIn.streakProtected,
            "createdAt" to (checkIn.createdAt ?: FieldValue.serverTimestamp()),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            docRef.set(payload).await()
            Log.d("FirestoreRepo", "Check-in saved successfully for date: ${checkIn.dateString}")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    // Sync user streak profile to Firestore
    suspend fun syncUserProfile(profile: UserProfile) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)

        val payload = hashMapOf<String, Any>(
            "userId" to uid,
            "displayName" to profile.displayName,
            "cleanSinceTimestamp" to profile.cleanSinceTimestamp,
            "longestStreakSeconds" to profile.longestStreakSeconds,
            "gracePeriodSavesCount" to profile.gracePeriodSavesCount,
            "sosCompletedCount" to profile.sosCompletedCount,
            "relapsesCount" to profile.relapsesCount,
            "createdAt" to (profile.createdAt ?: FieldValue.serverTimestamp()),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            docRef.set(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    // Load user streak profile from Firestore
    suspend fun getUserProfile(): UserProfile? {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        return try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                snapshot.toObject(UserProfile::class.java)
            } else {
                null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            null
        }
    }
}
