package com.example.localization

import com.example.model.AppLanguage

object Translations {
    fun t(key: String, language: AppLanguage): String {
        val entry = strings[key] ?: return key
        return when (language) {
            AppLanguage.ENGLISH -> entry.first
            AppLanguage.ARABIC -> entry.second
        }
    }

    private val strings = mapOf(
        // General & Navigation
        "app_name" to Pair("BreakFree", "تعافي"),
        "tab_home" to Pair("Home", "الرئيسية"),
        "tab_guard" to Pair("Guard", "الحماية"),
        "tab_ai_chat" to Pair("AI Chat", "المرشد"),
        "tab_community" to Pair("Community", "المجتمع"),
        "tab_dashboard" to Pair("Home", "الرئيسية"),
        "tab_ai_companion" to Pair("AI Chat", "المرشد"),
        "tab_monitoring" to Pair("Guard", "الحماية"),
        
        // Dashboard
        "clean_time_title" to Pair("Current Clean Time", "وقت التعافي المستمر"),
        "days" to Pair("Days", "أيام"),
        "hours" to Pair("Hours", "ساعات"),
        "minutes" to Pair("Mins", "دقائق"),
        "seconds" to Pair("Secs", "ثوانٍ"),
        "dopamine_score" to Pair("Dopamine Recovery Score", "مؤشر تعافي الدوبامين"),
        "dopamine_score_desc" to Pair("Neuroplastic healing of dopamine receptors and baseline sensitivity.", "الشفاء العصبي لمستقبلات الدوبامين واستعادة الحساسية الطبيعية."),
        "current_phase" to Pair("Current Phase", "المرحلة الحالية"),
        "next_milestone" to Pair("Next Milestone", "الهدف القادم"),
        "longest_streak" to Pair("Personal Best", "أطول سلسلة"),
        "grace_saves" to Pair("Grace Period Saves", "تراجعات الوعي الناجحة"),
        "sos_sessions" to Pair("SOS Completed", "جلسات SOS المكتملة"),
        "badges_title" to Pair("Milestones & Badges", "الأوسمة والإنجازات"),
        "badges_unlocked" to Pair("Unlocked", "تم تحقيقها"),
        "emergency_sos_btn" to Pair("EMERGENCY SOS", "طوارئ SOS"),
        "sos_subtext" to Pair("Breathe & conquer the urge right now", "تنفس واكسر حدة الرغبة فوراً"),
        "simulate_grace_btn" to Pair("Simulate 120s Grace Period", "تجربة فترة السماح (120 ثانية)"),
        "reset_streak" to Pair("Reset Streak / Relapse", "تصفير العداد / تسجيل انتكاسة"),
        "adjust_start_date" to Pair("Adjust Start Timestamp", "تعديل تاريخ البداية"),

        // Grace Period
        "grace_banner_title" to Pair("Mindfulness Check: Step Back!", "وقفة وعي: تراجع الآن لحماية تعافيك!"),
        "grace_banner_desc" to Pair("Explicit content detected. You have 120 seconds to close the app/page without resetting your streak.", "تم رصد محتوى صريح. لديك 120 ثانية لإغلاق الصفحة أو التطبيق دون أن يتأثر عداد تعافيك."),
        "grace_time_remaining" to Pair("Grace Period Remaining", "الوقت المتبقي في فترة السماح"),
        "grace_step_back_btn" to Pair("I Stepped Back (Protect Streak)", "تراجعت وأغلقت المحتوى (حفظ التعافي)"),
        "grace_detected_target" to Pair("Detected target", "المحتوى المرصود"),
        "grace_victory_title" to Pair("Streak Preserved! Great Victory!", "تم الحفاظ على تعافيك! نصر عظيم!"),
        "grace_victory_msg" to Pair("You took control and stepped back during the grace period. This strengthens your neural discipline!", "لقد انتصرت على الرغبة الملحة وتراجعت بوعي. هذا يعزز مسارات ضبط النفس في دماغك!"),
        "grace_relapse_title" to Pair("Exposure Exceeded 2 Minutes", "تجاوزت فترة السماح دقيقتين"),
        "grace_relapse_msg" to Pair("Continuous exposure lasted past 120 seconds. Streak has been reset, but remember: your prior healing isn't lost. Take a deep breath and rise again.", "استمر التعرض لأكثر من 120 ثانية. تم إعادة ضبط العداد، لكن تذكر: لم تفقد قوتك السابقة. خذ نفساً عميقاً وابدأ من جديد."),
        "close" to Pair("Close", "إغلاق"),
        "continue_journey" to Pair("Continue Journey", "مواصلة المسيرة"),

        // SOS Breathing
        "sos_modal_title" to Pair("Emergency Urge De-escalation", "نزع فتيل الرغبة العاجل"),
        "sos_breathing_tab" to Pair("4-7-8 Breathing", "تنفس 4-7-8"),
        "sos_grounding_tab" to Pair("Grounding 5-4-3-2-1", "تقنية التثبيت 5-4-3-2-1"),
        "sos_rules_tab" to Pair("Urge Surfing Rules", "قواعد ركوب الرغبة"),
        "breathe_inhale" to Pair("Inhale Deeply...", "شهيق عميق..."),
        "breathe_hold" to Pair("Hold & Focus...", "احبس النفس وركز..."),
        "breathe_exhale" to Pair("Slowly Exhale Tension...", "زفير بطيء وتفريغ التوتر..."),
        "cycle" to Pair("Cycle", "الدورة"),
        "i_am_calm_btn" to Pair("I Feel Grounded & Calm Now", "أشعر بالهدوء والسيطرة الآن"),
        "grounding_step5" to Pair("👀 Look around: Name 5 blue or green things you see.", "👀 انظر حولك: حدد 5 أشياء باللون الأزرق أو الأخضر."),
        "grounding_step4" to Pair("✋ Touch 4 physical surfaces nearby (desk, cloth, screen, wall).", "✋ المس 4 أسطح مادية بجانبك (المكتب، القماش، الحائط، الهاتف)."),
        "grounding_step3" to Pair("👂 Listen carefully: Notice 3 distinct sounds around you.", "👂 أصغِ جيداً: لاحظ 3 أصوات مختلفة في محيطك."),
        "grounding_step2" to Pair("👃 Inhale: Identify 2 scents or take 2 fresh breaths.", "👃 استنشق: ميّز رائحتين أو خذ نفسين عميقين من هواء نقي."),
        "grounding_step1" to Pair("👅 Sip cold water: Focus completely on the cold physical sensation.", "👅 اشرب ماء بارداً: ركّز تماماً على برودته وتأثيره الحسي."),
        "rule_15min_title" to Pair("The 15-Minute Neuro Wave", "موجة الـ 15 دقيقة العصبية"),
        "rule_15min_desc" to Pair("Urges feel overwhelming but they behave like ocean waves. They naturally peak at 10-15 minutes, then steadily drop as dopamine hunger resets.", "الرغبة الملحة تشبه موجة البحر؛ تبلغ ذروتها خلال 10-15 دقيقة ثم تتلاشى تدريجياً مع استقرار الدوبامين."),
        "rule_cold_water" to Pair("Splash Cold Water On Face", "اغسل وجهك بماء مثلج"),
        "rule_cold_desc" to Pair("Triggers the mammalian dive reflex, immediately slowing heart rate and resetting sympathetic adrenaline spikes.", "يفعل رد الفعل الغطسي الفطري، مما يخفض نبضات القلب فوراً ويهدئ الجهاز العصبي."),

        // AI Urge Companion
        "ai_title" to Pair("OpenCode Resilience Companion", "مرشد الصمود الذكي"),
        "ai_subtitle" to Pair("CBT-based urge surfing & cognitive reframing", "تقنيات العلاج السلوكي المعرفي وركوب الرغبات"),
        "ai_input_placeholder" to Pair("Describe what you are feeling right now...", "اكتب ما تشعر به الآن أو المحفز الذي واجهته..."),
        "ai_send" to Pair("Send", "إرسال"),
        "ai_chip_intense_urge" to Pair("🚨 Intense urge right now!", "🚨 رغبة ملحة جداً الآن!"),
        "ai_chip_halt" to Pair("🔍 Check my HALT triggers", "🔍 فحص محفزات HALT"),
        "ai_chip_night_lonely" to Pair("🌙 Late night & feeling lonely", "🌙 وقت متأخر وشعور بالوحدة"),
        "ai_chip_why_started" to Pair("✨ Remind me why I started", "✨ ذكرني بأسبابي للتعافي"),

        // Community
        "community_title" to Pair("Anonymous Brotherhood", "مجتمع التعافي المجهول"),
        "community_subtitle" to Pair("Zero judgment. Pure shared strength & real stories.", "دون أحكام مسبقة. تكاتف صادق وتجارب حقيقية."),
        "fist_bump" to Pair("Fist Bump 👊", "تحية قوة 👊"),
        "share_story_btn" to Pair("Post Anonymously", "مشاركة مجهولة"),
        "post_dialog_title" to Pair("Share Anonymous Strength", "نشر رسالة دعم مجهولة"),
        "post_alias_label" to Pair("Anonymous Alias", "الاسم المستعار المجهول"),
        "post_content_label" to Pair("Your insight, milestone, or victory...", "اكتب فائدة، إنجاز، أو نصيحة للأخوة..."),
        "post_category_label" to Pair("Category", "التصنيف"),
        "publish_btn" to Pair("Publish Anonymously", "نشر مجهول"),

        // Shield & Monitoring
        "shield_title" to Pair("Smart Shield & Protection", "درع الحماية والمراقبة الذكية"),
        "strict_guard_mode" to Pair("Strict Guard Mode", "نمط الحماية الصارم"),
        "strict_guard_desc" to Pair("Proactively monitor browser URLs & adult queries with immediate feedback", "فحص فوري لعناوين المتصفحات وعبارات البحث مع تنبيهات لحظية"),
        "grace_buffer_toggle" to Pair("120s Warning Buffer", "فترة السماح (120 ثانية)"),
        "grace_buffer_desc" to Pair("Grants 2 minutes to step back safely before streak resets", "تمنحك دقيقتين للتراجع بوعي وإغلاق المحتوى لحماية تعافيك"),
        "keyword_filter_toggle" to Pair("Explicit Domain Blocker", "حاجب الروابط والكلمات الصريحة"),
        "keyword_filter_desc" to Pair("Built-in adult domain dictionary with real-time browser matching", "قاموس مدمج للمواقع الصريحة مع فحص مباشر لعناوين المواقع"),
        "badges_view_all" to Pair("View All", "عرض الكل"),
        "badges_hide" to Pair("Collapse", "طي"),
        "service_status" to Pair("Content Monitoring Service", "خدمة رصد المحتوى الصريح"),
        "service_active" to Pair("Active & Guarding", "نشطة وتعمل بالحماية"),
        "service_inactive" to Pair("Inactive - Tap to Enable", "غير مفعلة - اضغط للتمكين"),
        "grace_rule_card_title" to Pair("The 2-Minute Grace Period Protocol", "بروتوكول فترة السماح (دقيقتان)"),
        "grace_rule_card_desc" to Pair("Unlike harsh blockers that immediately shame and wipe progress on accidental or impulsive clicks, BreakFree grants you a 120-second conscious window. Step back within 2 minutes and your streak remains 100% intact.", "على عكس التطبيقات التقليدية التي تحبط المتعافي بالتصفير الفوري، تمنحك بريك فري نافذة وعي مدتها 120 ثانية. إذا تراجعت وأغلقت المحتوى، يبقى عدادك محفوظاً تقديراً لقوة إرادتك."),
        "test_scanner_title" to Pair("Explicit Domain Scanner", "فاحص الروابط والمحفزات"),
        "test_scanner_desc" to Pair("Monitors browser URLs, search queries, and explicit keywords across Chrome, Firefox, and system browsers.", "يفحص عناوين المتصفحات وعبارات البحث الصريحة في كروم وفايرفوكس والمتصفحات الأخرى."),
        "custom_keywords" to Pair("Active Triggers Count", "عدد الكلمات والمواقع المحجوبة"),
        "language_toggle" to Pair("Language / اللغة", "اللغة / Language"),

        // Daily Mood & Urge Tracking
        "daily_checkin_title" to Pair("Daily Mood & Urge Log", "تسجيل المشاعر والرغبات اليومي"),
        "daily_checkin_subtitle" to Pair("Log your emotional state to train your AI counselor", "سجل حالتك النفسية لتدريب مرشدك الذكي وتقديم نصائح مخصصة"),
        "log_today_btn" to Pair("Log Today's Reflection", "تسجيل مراجعة اليوم"),
        "edit_today_log" to Pair("Update Today's Log", "تعديل مراجعة اليوم"),
        "mood_question" to Pair("How did you feel emotionally today?", "كيف كانت حالتك المزاجية والنفسية اليوم؟"),
        "urge_question" to Pair("Peak urge intensity experienced today (0-10):", "أقصى شدة للرغبة واجهتها اليوم (0 إلى 10):"),
        "emotions_label" to Pair("Primary Emotions Felt:", "المشاعر السائدة اليوم:"),
        "triggers_label" to Pair("Triggers Encountered:", "المحفزات التي واجهتها:"),
        "reflection_label" to Pair("Nightly Journal & Insights:", "تأملاتك وملاحظاتك لليوم:"),
        "reflection_placeholder" to Pair("What challenged you? What worked well today?", "ما الذي شكّل تحدياً لك؟ وما الذي ساعدك على الصمود؟"),
        "streak_safe_toggle" to Pair("Streak Protected & Clean Today", "تمت حماية السلسلة بنجاح اليوم"),
        "save_checkin_btn" to Pair("Save to Cloud & Update AI", "حفظ وتحديث سياق المرشد الذكي"),
        "checkin_saved_success" to Pair("Daily reflection saved! Your AI counselor has updated its insights.", "تم حفظ تسجيلك اليومي بنجاح وتحديث سياق المرشد الذكي."),
        "recent_logs_title" to Pair("Recent Emotional Trends", "سجل المشاعر والرغبات السابقة"),
        "no_logs_yet" to Pair("No logs recorded yet. Complete your first evening reflection!", "لم تسجل أي مراجعة بعد. ابدأ تسجيل أول مراجعة مسائية اليوم!"),
        
        // Google Sign-In & Sync
        "sign_in_google" to Pair("Sign in with Google", "تسجيل الدخول عبر Google"),
        "sign_in_title" to Pair("Cloud Sync & Recovery Identity", "المزامنة السحابية وهوية التعافي"),
        "sign_in_desc" to Pair("Sign in with Google to protect your streak history and daily mood logs across devices.", "سجل الدخول باستخدام Google لحفظ تاريخ تعافيك وسجلات مشاعرك بأمان على السحابة."),
        "sign_out" to Pair("Sign Out", "تسجيل الخروج"),
        "signed_in_as" to Pair("Signed in as", "مسجل الدخول باسم"),
        "cloud_synced" to Pair("Cloud Synced", "متزامن سحابياً")
    )
}
