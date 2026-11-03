package com.aistudio.breakfree.recovr.ai

import com.aistudio.breakfree.recovr.model.AppLanguage

/**
 * Hard-coded safety net that runs *before* any model call.
 *
 * A recovery app will eventually receive a message about self-harm. That must never be
 * answered by a canned urge-surfing paragraph, and it must never be sent to a model
 * without an explicit, unambiguous safety response first.
 */
object CrisisSupport {

    private val englishMarkers = listOf(
        "suicide", "suicidal", "kill myself", "end my life", "ending my life",
        "want to die", "better off dead", "self harm", "self-harm", "hurt myself",
        "cut myself", "overdose"
    )

    private val arabicMarkers = listOf(
        "انتحار", "أنتحر", "اقتل نفسي", "أقتل نفسي", "انهي حياتي", "أنهي حياتي",
        "أريد أن أموت", "اريد ان اموت", "إيذاء نفسي", "اذي نفسي", "أجرح نفسي"
    )

    private val frenchMarkers = listOf(
        "suicide", "suicidaire", "me tuer", "mettre fin à ma vie", "mettre fin a ma vie",
        "envie de mourir", "mieux mort", "auto-mutilation", "automutilation",
        "me faire du mal", "me couper", "surdose"
    )

    fun isCrisisMessage(text: String): Boolean {
        val lower = text.lowercase()
        return englishMarkers.any { lower.contains(it) } ||
            arabicMarkers.any { lower.contains(it) } ||
            frenchMarkers.any { lower.contains(it) }
    }

    /** The only reply the app gives to a crisis message. */
    fun response(language: AppLanguage): String = when (language) {
        AppLanguage.ARABIC -> """
**ما تشعر به الآن ثقيل جداً، وأنت تستحق دعماً بشرياً حقيقياً في هذه اللحظة.**

أنا تطبيق، ولست جهة طوارئ. لا أستطيع أن أبقيك آمناً بنفسي.

**افعل هذا الآن:**
1. اتصل برقم الطوارئ في بلدك (112 أو 911 أو 999) أو اذهب إلى أقرب قسم طوارئ.
2. اتصل بشخص تثق به الآن وأخبره بما تشعر به، ولو بكلمة واحدة: «أحتاجك».
3. ابتعد عن أي شيء قد تؤذي به نفسك، وابقَ في مكان فيه أشخاص آخرون.

**خطوط دعم متاحة على مدار الساعة:**
• السعودية: 937 (وزارة الصحة) — الدعم النفسي
• الإمارات: 800 4673 (ESTIJABA)
• مصر: 0800 888 0700 (خط نجدة الصحة النفسية)
• International: findahelpline.com

لست وحدك، وهذه اللحظة ستمر. تواصل مع إنسان الآن.
        """.trimIndent()
        AppLanguage.FRENCH -> """
**Ce que vous portez en ce moment est lourd, et vous méritez un vrai soutien humain.**

Je suis une application, pas un service d'urgence. Je ne peux pas vous garder en sécurité seul.

**Veuillez faire ceci maintenant :**
1. Appelez le numéro d'urgence local (112, 911, 999) ou rendez-vous au service d'urgence le plus proche.
2. Appelez ou envoyez un message à une personne de confiance et dites une phrase : « J'ai besoin de toi maintenant. »
3. Éloignez-vous de tout ce avec quoi vous pourriez vous faire du mal, et restez là où il y a d'autres personnes.

**Lignes d'écoute gratuites, 24 h/24 :**
• France : 3114 (numéro national de prévention du suicide)
• Belgique : 0800 32 123 (Centre de prévention du suicide)
• Suisse : 143 (La Main Tendue)
• Partout : findahelpline.com

Vous n'êtes pas seul(e), et ce moment passera. Veuillez contacter une personne maintenant.
        """.trimIndent()
        else -> """
**What you are carrying right now is heavy, and you deserve real human support.**

I am an app, not an emergency service. I cannot keep you safe on my own.

**Please do this now:**
1. Call your local emergency number (911, 112, 999) or go to the nearest emergency department.
2. Call or message someone you trust and say one sentence: "I need you right now."
3. Move away from anything you could hurt yourself with, and stay where other people are.

**Free, 24/7 crisis lines:**
• US: call or text 988 (Suicide & Crisis Lifeline)
• UK & Ireland: 116 123 (Samaritans)
• Canada: 988
• Anywhere: findahelpline.com

You are not alone, and this moment will pass. Please reach out to a person now.
        """.trimIndent()
    }
}
