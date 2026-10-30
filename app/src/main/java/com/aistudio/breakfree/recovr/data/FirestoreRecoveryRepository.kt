package com.aistudio.breakfree.recovr.data

import android.content.Context
import android.util.Log
import com.aistudio.breakfree.recovr.R
import com.aistudio.breakfree.recovr.model.DailyCheckIn
import com.aistudio.breakfree.recovr.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
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

    // Save or update today's check-in. Returns the server-assigned createdAt so the
    // caller can cache it locally (needed to keep the security rules' createdAt
    // invariant stable on the next update).
    suspend fun saveCheckIn(checkIn: DailyCheckIn): Timestamp? {
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
            // Preserve the original creation timestamp when we know it; only a brand
            // new document gets a server timestamp.
            "createdAt" to (checkIn.createdAt ?: FieldValue.serverTimestamp()),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            // Merge instead of overwrite so unknown/extra fields are never dropped.
            docRef.set(payload, SetOptions.merge()).await()
            if (checkIn.createdAt != null) return checkIn.createdAt
            // First save of the day: read back the server-assigned timestamp so it can
            // be cached locally and kept stable on future updates.
            val saved = docRef.get().await()
            val createdAt = saved.getTimestamp("createdAt")
            Log.d("FirestoreRepo", "Check-in saved successfully for date: ${checkIn.dateString}")
            return createdAt
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
            // Never rewrite createdAt on an update: the rules require it to be stable.
            "createdAt" to (profile.createdAt ?: FieldValue.serverTimestamp()),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            docRef.set(payload, SetOptions.merge()).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    // Load user streak profile from Firestore.
    //
    // A missing document returns null, but a *failed read* throws: the caller must be
    // able to tell "this account has no cloud profile yet" (safe to create one) apart
    // from "we could not read it" (must not overwrite it).
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
            throw e
        }
    }
}
