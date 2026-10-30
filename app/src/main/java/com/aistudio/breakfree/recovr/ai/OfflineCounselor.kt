package com.aistudio.breakfree.recovr.ai

import com.aistudio.breakfree.recovr.model.AppLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object OfflineCounselor {

    /**
     * Pre-written, general-purpose guidance used when the model is unreachable.
     * It is explicitly labelled as such: presenting it as a personalised answer would
     * be dishonest.
     */
    suspend fun generateCounselorResponse(
        userInput: String,
        cleanDays: Long,
        language: AppLanguage
    ): String = withContext(Dispatchers.Default) {
        val lower = userInput.lowercase()
        val isAr = language == AppLanguage.ARABIC
        val body = if (isAr) {
            when {
                lower.contains("ملحة") || lower.contains("شديدة") || lower.contains("urge") || lower.contains("طوارئ") -> {
                    """
**قِف مكانك وخذ نفساً عميقاً الآن.**

أنت لست رغبتك؛ الرغبة مجرد إشارة كيميائية مؤقتة يطلقها عقلك بحثاً عن دوبامين سهل.
لديك الآن **$cleanDays يوماً** من النقاء العظيم، وكل ثانية تقاوم فيها تعيد بناء خلاياك العصبية!

**خطوات الإنقاذ الفورية (خلال 60 ثانية):**
1. **قاعدة الـ 15 دقيقة:** أقسم عقلك أنك لن تتخذ أي قرار سلبي لمدة 15 دقيقة فقط. الرغبة ستنحدر مثل الموجة.
2. **صدمة الماء البارد:** اذهب إلى المغسلة واغسل وجهك ورقبتك بماء مثلج لتفعيل رد الفعل العصبي المهدئ.
3. **غيّر المكان والوضعية فوراً:** اترك الهاتف في غرفة أخرى واخرج للمشي أو إلى مكان به ناس.

أنت أقوى بكثير من نبضة دوبامين زائفة. اضغط على زر **طوارئ SOS** للبدء في تمرين التنفس فوراً!
                    """.trimIndent()
                }

                lower.contains("halt") || lower.contains("فحص") || lower.contains("محفز") -> {
                    """
**تحليل محفزات الـ HALT العصبية:**

في كثير من الأحيان، لا يكون دافعك هو الرغبة في المشاهدة بحد ذاتها، بل هو محاولة لتسكين شعور مزعج آخر. قيّم نفسك الآن:

• **H (Hungry / جائع؟):** هل مضى وقت طويل دون طعام صحي؟ انخفاض سكر الدم يضعف الإرادة.
• **A (Angry / غاضب أو محبط؟):** هل مررت بموقف أثار غضبك أو توترك في العمل أو الدراسة؟
• **L (Lonely / وحيد أو معزول؟):** هل تشعر بالفراغ أو قلة التواصل مع الآخرين؟ تواصل مع صديق أو اكتب في مجتمعنا المجهول.
• **T (Tired / مرهق أو قلة نوم؟):** هل جسدك مجهد؟ النوم هو أعظم علاج لتجديد القشرة المخية الجبهية.

عالج السبب الحقيقي الخفي، وستجد أن الرغبة الزائفة قد تبخرت!
                    """.trimIndent()
                }

                lower.contains("ليل") || lower.contains("متأخر") || lower.contains("وحيد") || lower.contains("night") || lower.contains("lonely") -> {
                    """
**التعامل مع وحشة الليل والوحدة:**

ساعات الليل المتأخرة هي البيئة الأكثر خطورة على المتعافي، لأن الإرهاق يقلل كفاءة الفص الجبهي في الدماغ بنسبة تصل إلى 70%.

**بروتوكول السلامة الليلية:**
1. **أبعد الأجهزة عن السرير:** لا تضع الشاحن بجانب وسادتك أبداً. اجعل غرفة النوم مكاناً مقدساً للنوم فقط.
2. **استمع لشيء مهدئ:** مقطع صوتي تأملي، قراءة هادئة، أو بودكاست نافع يشتت عقلك بلطف.
3. **ذكّر نفسك:** ما تشعر به الآن ليس رغبة جنسية حقيقية، بل هو فراغ وجوع للمشاعر والراحة. المشاهدة لن تملأ فراغك بل ستزيده قهراً.

أنت في أمان الليلة. تنفس بعمق ودع جسدك يستريح.
                    """.trimIndent()
                }

                lower.contains("بدأت") || lower.contains("ذكرني") || lower.contains("أسبابي") || lower.contains("why") -> {
                    """
**تذكير بقدسية قرارك وقوة حريتك:**

تذكر اللحظة التي قررت فيها تحميل هذا التطبيق والبدء في كسر القيود:
• تذكر الإحساس المظلم بالخزي والندم بعد كل انتكاسة سابقة.
• تذكر كيف كان ضباب الدماغ يسلب منك التركيز، ونظراتك لا تستطيع مواجهة الناس بثقة.
• انظر إلى تقدمك اليوم: **$cleanDays يوماً** من النقاء والكرامة!

التعافي يعيد لك:
صفاء الذهن والذاكرة الحادة.
استعادة المتعة الحقيقية بالأشياء البسيطة في الحياة.
احترامك لذاتك وشعورك بالرجولة والقوة الداخلية.

أنت لا تحرم نفسك من شيء نافع، بل تحرر روحك من سجن وهمي!
                    """.trimIndent()
                }

                else -> {
                    """
**أنا معك في كل خطوة من رحلة تعافيك.**

أشعر بما تمر به. الأفكار والرغبات تلح أحياناً كضيف ثقيل، لكن تذكر: أنت لست مضطراً لفتح الباب لها.

• **ركوب الرغبة (Urge Surfing):** راقب الشعور في جسدك كأنك عالم يدرس ظاهرة طبيعية. أين يتركز التوتر؟ في الصدر؟ في المعدة؟ راقبه دون أن تحكم عليه، ودعه يمر.
• **الخروج من العقل إلى الحواس:** المس شيئاً بارداً، اشرب كوب ماء، أو تحرك سريعاً لمدة دقيقتين.

أنت تبني مسارات عصبية جديدة كل دقيقة تختار فيها الصمود. أخبرني بما تشعر به بمزيد من التفصيل!
                    """.trimIndent()
                }
            }
        } else {
            when {
                lower.contains("intense") || lower.contains("urge") || lower.contains("right now") || lower.contains("emergency") -> {
                    """
**STOP and take a deep conscious breath right now.**

You are NOT your urge. An urge is simply an automatic neurochemical echo—a hungry dopamine receptor seeking a cheap spike.
You currently hold **$cleanDays days** of clean healing. Every single minute you endure physically rewires your prefrontal cortex!

**Immediate 60-Second De-escalation Protocol:**
1. **The 15-Minute Rule:** Make an ironclad deal with yourself: do not take any impulsive action for the next 15 minutes. Urges are waves—they peak and inevitably decline.
2. **Cold Water Shock:** Splash cold water on your face and wrists. It triggers the mammalian dive reflex, instantly lowering heart rate and soothing adrenaline.
3. **Change the Physical Room:** Leave the room, leave your phone behind, and walk into an open or public space.

You have already conquered worse than this. Tap the **EMERGENCY SOS** button above to start guided breathing immediately!
                    """.trimIndent()
                }

                lower.contains("halt") || lower.contains("trigger") || lower.contains("check") -> {
                    """
**HALT Trigger Analysis:**

Most intense cravings are not about physical desire—they are desperate biological attempts to self-medicate uncomfortable emotional states. Ask yourself honestly:

• **H (Hungry):** Low blood glucose impairs executive willpower. Grab a healthy snack or glass of water.
• **A (Angry / Anxious):** Did someone frustrate you today? Are you avoiding a hard work task?
• **L (Lonely):** Isolation amplifies cravings tenfold. Text a close friend or post in our anonymous community tab.
• **T (Tired):** Sleep deprivation weakens prefrontal restraint by up to 70%. If it's night, close your eyes and sleep.

Fix the underlying deficit, and the craving will lose its grip on you!
                    """.trimIndent()
                }

                lower.contains("night") || lower.contains("lonely") || lower.contains("bed") || lower.contains("late") -> {
                    """
**Late-Night & Solitude Protocol:**

Late nights in an isolated room are the danger zone for every warrior in recovery. When your brain is exhausted, willpower reserves are depleted.

**Emergency Actions Tonight:**
1. **Ban Devices from Bed:** Plug your phone across the room or outside your sleeping quarters right now.
2. **Shift Focus to Audio:** Listen to a calming recovery podcast, white noise, or audiobook that guides your thoughts away from looping triggers.
3. **Remember the Illusion:** A relapse will not cure loneliness; it will compound it with guilt and numbness tomorrow morning.

You are safe. Breathe out, close your eyes, and allow restorative sleep to heal your brain receptors.
                    """.trimIndent()
                }

                lower.contains("why") || lower.contains("start") || lower.contains("remind") -> {
                    """
**Remembering Your Sacred Why:**

Think back to why you swore you would break free:
• Remember the hollow numbness and crushing regret immediately following past relapses.
• Remember the brain fog, reduced motivation, and avoidance of eye contact with loved ones.
• Look at where you stand today: **$cleanDays clean days** of genuine dignity and strength!

True freedom brings back:
Razor-sharp focus and effortless social presence.
Authentic sensitivity to everyday joy, nature, and laughter.
Pride in knowing you are the master of your own mind.

You are not depriving yourself; you are liberating your potential!
                    """.trimIndent()
                }

                else -> {
                    """
**I am right here with you.**

What you are experiencing right now is a normal symptom of neuroplastic healing. As your brain adapts to baseline dopamine levels, it throws tantrums—these are cravings.

• **Urge Surfing:** Don't fight the urge with tension. Observe it like an outside researcher. Notice where in your body you feel the sensation. Breathe into it and watch it soften.
• **Physical Interruption:** Do 10 pushups, take 5 slow breaths, or grab an ice-cold glass of water.

Every single urge you surf without giving in permanently strengthens your self-mastery. What triggered this thought? Tell me more!
                    """.trimIndent()
                }
            }
        }

        notice(language) + "\n\n" + body
    }

    /**
     * Says, in the user's language, that this text was not generated for them.
     * Silently passing pre-written text off as a personalised answer would undermine
     * the trust the whole app depends on.
     */
    private fun notice(language: AppLanguage): String = if (language == AppLanguage.ARABIC) {
        "تعذّر الوصول إلى المساعد الذكي الآن، فهذه نصائح مكتوبة مسبقاً لكل المستخدمين. " +
            "إن كنت في خطر فوري، فاتصل بخدمات الطوارئ في بلدك."
    } else {
        "The AI counselor could not be reached, so this is pre-written advice shown to " +
            "every user. If you are in immediate danger, contact your local emergency services."
    }
}
