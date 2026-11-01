package com.aistudio.breakfree.recovr.data

import com.aistudio.breakfree.recovr.model.CommunityCategory
import com.aistudio.breakfree.recovr.model.CommunityPost
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Public community feed backed by Firebase Realtime Database.
 *
 * Realtime Database is free on the Spark (no-billing) plan, which is why the community
 * moved here from Firestore: Firestore's data API requires billing to be enabled, and
 * the app must keep working for users without a payment method.
 *
 * The feed is global and public: a post either reaches every signed-in user or does not
 * exist. Nothing is cached on the device except each user's own "I bumped this" flags.
 */
class RealtimeCommunityRepository(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
) {

    private val postsRef = database.getReference("community_posts")

    /** Newest 50 posts, re-emitted whenever any of them changes. */
    fun observeCommunityPosts(): Flow<List<CommunityPost>> = callbackFlow {
        val query = postsRef.orderByChild("timestampMillis").limitToLast(MAX_POSTS)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val posts = snapshot.children.mapNotNull { child ->
                    try {
                        CommunityPost(
                            id = child.key ?: return@mapNotNull null,
                            authorAlias = child.child("authorAlias").getValue(String::class.java) ?: "",
                            authorAvatarBg = child.child("authorAvatarBg").getValue(Long::class.java)
                                ?: 0xFF10B981L,
                            daysClean = (child.child("daysClean").getValue(Long::class.java) ?: 0L).toInt(),
                            category = try {
                                CommunityCategory.valueOf(
                                    child.child("category").getValue(String::class.java)
                                        ?: "DAILY_MOTIVATION"
                                )
                            } catch (e: Exception) {
                                CommunityCategory.DAILY_MOTIVATION
                            },
                            contentEn = child.child("contentEn").getValue(String::class.java) ?: "",
                            contentAr = child.child("contentAr").getValue(String::class.java) ?: "",
                            timestampMillis = child.child("timestampMillis").getValue(Long::class.java) ?: 0L,
                            fistBumps = (child.child("fistBumps").getValue(Long::class.java) ?: 0L).toInt(),
                            userCheered = false
                        )
                    } catch (e: Exception) {
                        null
                    }
                }.sortedByDescending { it.timestampMillis }
                trySend(posts)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    /** Publishes a post. Throws on failure so the caller can report the reason. */
    suspend fun saveCommunityPost(post: CommunityPost): String {
        val payload = mapOf(
            "authorAlias" to post.authorAlias,
            "authorAvatarBg" to post.authorAvatarBg,
            "daysClean" to post.daysClean,
            "category" to post.category.name,
            "contentEn" to post.contentEn,
            "contentAr" to post.contentAr,
            "timestampMillis" to post.timestampMillis,
            "fistBumps" to post.fistBumps
        )
        postsRef.child(post.id).setValue(payload).await()
        return post.id
    }

    /** Adjusts the public fist-bump counter. Throws on failure. */
    suspend fun toggleFistBump(postId: String, currentlyCheered: Boolean) {
        val delta = if (currentlyCheered) -1L else 1L
        postsRef.child(postId).child("fistBumps")
            .setValue(ServerValue.increment(delta)).await()
    }

    companion object {
        private const val MAX_POSTS = 50
    }
}
