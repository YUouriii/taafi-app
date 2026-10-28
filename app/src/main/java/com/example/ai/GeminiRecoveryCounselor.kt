package com.example.ai

import android.util.Log
import com.example.model.AppLanguage
import com.example.model.DailyCheckIn
import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeout

class GeminiRecoveryCounselor(
    private val getRecentCheckIns: () -> List<DailyCheckIn>
) {
    private var chatSession: Chat? = null

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash"
        )
    }

    suspend fun sendChatMessage(
        userMessage: String,
        cleanDays: Long,
        language: AppLanguage
    ): String {
        return try {
            withTimeout(45_000L) {
                val isAr = language == AppLanguage.ARABIC
                val recentCheckIns = getRecentCheckIns()

                val checkInContext = if (recentCheckIns.isNotEmpty()) {
                    val last3 = recentCheckIns.take(3)
                    val summary = last3.joinToString("\n") {
                        "- Date: ${it.dateString}, Mood (1-5): ${it.moodScore}, Urge Intensity (0-10): ${it.urgeIntensity}, Emotions: ${it.primaryEmotions.joinToString()}, Triggers: ${it.triggersEncountered.joinToString()}, Reflection: ${it.reflectionNotes}"
                    }
                    "\n[User's Recent Daily Mood & Urge Log Data]:\n$summary\n"
                } else {
                    "\n[User's Daily Log]: No check-in logs submitted yet.\n"
                }

                val systemPrompt = """
You are the BreakFree / تعافي Senior AI Resilience Counselor. You specialize in Cognitive Behavioral Therapy (CBT), Acceptance and Commitment Therapy (ACT), and neuroscience-backed Urge Surfing for overcoming compulsive pornography and sex addiction.

Client Profile:
- Clean Streak: $cleanDays days
- Preferred Language: ${if (isAr) "Arabic (العربية)" else "English"}
$checkInContext

Counseling Guidelines:
1. Deep Empathy & Neuroscience: Frame urges as natural dopamine readjustment waves, removing shame and reinforcing prefrontal cortex neuroplasticity.
2. In-Depth Mood Tracking Integration: Actively analyze the user's daily mood score and urge trends. If urge was high or mood was low recently (e.g. loneliness, boredom, late night), point out the trigger correlation gently and offer customized coping mechanisms.
3. High Actionability: Give clear steps (e.g., 4-7-8 breathing, sensory grounding, changing the room, cold splash).
4. Language: Respond purely in ${if (isAr) "warm, eloquent, and supportive Arabic" else "motivating, empowering English"}.
                """.trimIndent()

                if (chatSession == null) {
                    chatSession = model.startChat(
                        history = listOf(
                            content("user") { text(systemPrompt + "\n\nHello, I am ready to begin counseling.") },
                            content("model") {
                                text(
                                    if (isAr)
                                        "أهلاً بك يا بطل في مرشد التعافي الذكي. اطلعت على سجل مشاعرك ومستوى الرغبات الأخير، وأنا معك خطوة بخطوة لدعم مسيرتك. ما الذي تشعر به الآن؟"
                                    else
                                        "Welcome warrior to your BreakFree counselor. I have reviewed your emotional trends and urge levels. How are you feeling right now?"
                                )
                            }
                        )
                    )
                }

                val response = chatSession!!.sendMessage(userMessage)
                val text = response.text
                if (!text.isNullOrBlank()) {
                    text
                } else {
                    OpenCodeCounselorService.generateCounselorResponse(userMessage, cleanDays, language)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w("GeminiCounselor", "Firebase AI Logic fallback: ${e.message}")
            OpenCodeCounselorService.generateCounselorResponse(userMessage, cleanDays, language)
        }
    }
}
