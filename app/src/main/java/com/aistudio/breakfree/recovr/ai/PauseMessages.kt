package com.aistudio.breakfree.recovr.ai

import com.aistudio.breakfree.recovr.model.AppLanguage

/**
 * The three notifications that arrive during a ten-minute pause.
 *
 * Templates are the guaranteed floor: they work offline, without a key and without a
 * model, and they are also what a short model answer is padded with. The model's job is
 * only to say the same thing in the user's own words — never to invent progress.
 *
 * Tone is strictly logical: time, mechanics, data. No emotional manipulation, no
 * guilt-tripping, no motivational quotes.
 */
object PauseMessages {

    fun templates(
        promise: String,
        cleanDays: Long,
        bestDays: Long,
        urgesSurvived: Int,
        language: AppLanguage
    ): List<String> = when (language) {
        AppLanguage.ARABIC -> listOf(
            "مرّ $cleanDays يوماً على تعافيك. هذه الرغبة ارتفاع كيميائي مؤقت — تبلغ ذروتها ثم تمر خلال 90 ثانية تقريباً.",
            "الانتكاس الآن يعيد عدادك إلى صفر. لقد تجاوزت $urgesSurvived رغبة من قبل.",
            "اخترت الانتظار. بقيت دقيقة واحدة لاتخاذ قرار واعٍ، وليس نزوة."
        )

        AppLanguage.FRENCH -> listOf(
            "$cleanDays jours sans rechute. Cette envie est un pic chimique temporaire — elle monte et retombe en 90 secondes environ.",
            "Rechuter maintenant remet votre compteur à zéro. $urgesSurvived envies déjà surmontées.",
            "Vous avez choisi d'attendre. 1 minute restante pour une décision consciente, pas impulsive."
        )

        AppLanguage.ENGLISH -> listOf(
            "$cleanDays days clean. This urge is a temporary chemical spike — it peaks and fades within about 90 seconds.",
            "Relapsing now resets your streak to zero. $urgesSurvived urges already survived.",
            "You chose to wait. 1 minute left to make a conscious choice, not an impulsive one."
        )
    }

    /**
     * Models sometimes answer with bullets or numbering despite being told not to.
     * Strips that noise and drops anything too long to be a notification.
     */
    fun parse(raw: String): List<String> = raw
        .lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { line ->
            line
                .replace(Regex("^[-*•]\\s*"), "")
                .replace(Regex("^\\d+[.)]\\s*"), "")
                .trim()
                .removeSurrounding("\"")
                .removeSurrounding("«", "»")
                .trim()
        }
        .filter { it.length in 10..160 }
        .take(3)
        .toList()
}
