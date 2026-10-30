package com.aistudio.breakfree.recovr.localization

import com.aistudio.breakfree.recovr.model.AppLanguage

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
        "grace_detected_target" to Pair("Detected target", "المحتوى المرصود"),
        "grace_victory_title" to Pair("Streak Preserved! Great Victory!", "تم الحفاظ على تعافيك! نصر عظيم!"),
        "grace_victory_msg" to Pair("You took control and stepped back during the grace period. This strengthens your neural discipline!", "لقد انتصرت على الرغبة الملحة وتراجعت بوعي. هذا يعزز مسارات ضبط النفس في دماغك!"),
        "grace_relapse_title" to Pair("Exposure Exceeded 2 Minutes", "تجاوزت فترة السماح دقيقتين"),
        "grace_relapse_msg" to Pair("Continuous exposure lasted past 120 seconds. Streak has been reset, but remember: your prior healing isn't lost. Take a deep breath and rise again.", "استمر التعرض لأكثر من 120 ثانية. تم إعادة ضبط العداد، لكن تذكر: لم تفقد قوتك السابقة. خذ نفساً عميقاً وابدأ من جديد."),
        "close" to Pair("Close", "إغلاق"),
        "continue_journey" to Pair("Continue Journey", "مواصلة المسيرة"),

        // System overlay (drawn over the monitored app via SYSTEM_ALERT_WINDOW)
        "overlay_title" to Pair("Step back from this page", "تراجع عن هذه الصفحة"),
        "overlay_body" to Pair(
            "This page matched your filter. Close this page or leave this app to keep your streak. Nothing is recorded unless you confirm it later.",
            "هذه الصفحة تطابق فلترك. أغلق هذه الصفحة أو اخرج من التطبيق للحفاظ على تعافيك. لن يُسجَّل أي شيء قبل تأكيدك لاحقاً."
        ),
        // There is no dismiss button on purpose: this line is the only explanation of
        // how the reminder goes away.
        "overlay_stays" to Pair(
            "This reminder stays until you close the page or leave the app.",
            "يبقى هذا التنبيه حتى تغلق الصفحة أو تخرج من التطبيق."
        ),

        // Sticky notification shown when the overlay permission is not granted.
        "grace_notification_channel" to Pair("BreakFree Mindfulness Shield", "درع الوعي من تعافي"),
        "grace_notification_channel_desc" to Pair("Alerts during the step-back window countdown", "تنبيهات أثناء العد التنازلي لنافذة التراجع"),
        "grace_notification_title" to Pair("Mindfulness check — step back", "وقفة وعي — تراجع الآن"),
        "grace_notification_text" to Pair(
            "Close this page or leave the app to protect your streak.",
            "أغلق هذه الصفحة أو اخرج من التطبيق لحماية تعافيك."
        ),
        "grace_notification_detected" to Pair("Something explicit was detected.", "تم رصد محتوى صريح."),
        "grace_notification_remaining" to Pair(
            "You have {time} left of your {total}s window.",
            "بقي لديك {time} من نافذة {total} ثانية."
        ),
        "grace_notification_stays" to Pair(
            "This notification stays until you close the page or leave the app.",
            "يبقى هذا الإشعار حتى تغلق الصفحة أو تخرج من التطبيق."
        ),
        "grace_notification_not_recorded" to Pair("Nothing has been recorded yet.", "لم يُسجَّل أي شيء بعد."),

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
        "grounding_step5" to Pair("Look around: Name 5 calm items you see in your environment.", "انظر حولك: حدد 5 أشياء تراها في محيطك واشعر بوجودك هنا الآن."),
        "grounding_step4" to Pair("Touch 4 physical surfaces nearby (desk, cloth, screen, wall).", "المس 4 أسطح مادية بجانبك (المكتب، القماش، الحائط، الهاتف)."),
        "grounding_step3" to Pair("Listen carefully: Notice 3 distinct sounds around you.", "أصغِ جيداً: لاحظ 3 أصوات مختلفة في محيطك الحالي."),
        "grounding_step2" to Pair("Inhale deeply: Identify 2 scents or take 2 fresh breaths.", "استنشق بعمق: ميّز رائحتين أو خذ نفسين عميقين من هواء نقي."),
        "grounding_step1" to Pair("Sip cold water: Focus completely on the cold physical sensation.", "اشرب ماء بارداً: ركّز تماماً على برودته وتأثيره الحسي المهدئ."),
        "rule_15min_title" to Pair("The 15-Minute Neuro Wave", "موجة الـ 15 دقيقة العصبية"),
        "rule_15min_desc" to Pair("Urges feel overwhelming but they behave like ocean waves. They naturally peak at 10-15 minutes, then steadily drop as dopamine hunger resets.", "الرغبة الملحة تشبه موجة البحر؛ تبلغ ذروتها خلال 10-15 دقيقة ثم تتلاشى تدريجياً مع استقرار الدوبامين."),
        "rule_cold_water" to Pair("Splash Cold Water On Face", "اغسل وجهك بماء مثلج"),
        "rule_cold_desc" to Pair("Triggers the mammalian dive reflex, immediately slowing heart rate and resetting sympathetic adrenaline spikes.", "يفعل رد الفعل الغطسي الفطري، مما يخفض نبضات القلب فوراً ويهدئ الجهاز العصبي."),

        // AI Urge Companion
        "ai_title" to Pair("Your Resilience Companion", "مرشد الصمود"),
        "ai_subtitle" to Pair("CBT-based urge surfing & cognitive reframing", "تقنيات العلاج السلوكي المعرفي وركوب الرغبات"),
        "ai_input_placeholder" to Pair("Describe what you are feeling right now...", "اكتب ما تشعر به الآن أو المحفز الذي واجهته..."),
        "ai_send" to Pair("Send", "إرسال"),
        "ai_chip_intense_urge" to Pair("Intense urge right now!", "رغبة ملحة جداً الآن!"),
        "ai_chip_halt" to Pair("Check my HALT triggers", "فحص محفزات HALT"),
        "ai_chip_night_lonely" to Pair("Late night & feeling lonely", "وقت متأخر وشعور بالوحدة"),
        "ai_chip_why_started" to Pair("Remind me why I started", "ذكرني بأسبابي للتعافي"),
        "ai_sign_in_required_title" to Pair("Sign In with Google to Unlock AI", "سجّل الدخول عبر Google لتفعيل المساعد الذكي"),
        "ai_sign_in_required_desc" to Pair(
            "Sign in with your Google account to enable real-time Gemini AI counseling tailored to your recovery journey.",
            "سجّل الدخول بحساب Google الخاص بك لتفعيل الإرشاد الذكي المدعوم بنموذج Gemini والمخصص لرحلة تعافيك."
        ),
        "ai_sign_in_button" to Pair("Sign in with Google", "تسجيل الدخول باستخدام Google"),
        "ai_api_key_btn" to Pair("Gemini Key", "مفتاح Gemini"),
        "ai_api_key_dialog_title" to Pair("Configure Gemini AI Key", "إعداد مفتاح Gemini AI"),
        "ai_api_key_dialog_desc" to Pair(
            "BreakFree uses Google's cheapest & fastest free models (Gemini 1.5 Flash / 2.0 Flash). You can enter your personal free API key from Google AI Studio to ensure 100% reliable, quota-free responses.",
            "يستخدم تعافي نماذج Gemini المجانية والأسرع (Gemini 1.5 Flash / 2.0 Flash). يمكنك إدخال مفتاحك المجاني من Google AI Studio لضمان عمل الخدمة بدون انقطاع."
        ),
        "ai_api_key_get_free" to Pair("Get Free Key (Google AI Studio)", "احصل على مفتاح مجاني (Google AI Studio)"),
        "ai_api_key_save" to Pair("Save Key", "حفظ المفتاح"),
        "ai_api_key_clear" to Pair("Clear", "مسح"),
        "ai_api_key_active" to Pair("Gemini 1.5 Flash (Free Tier)", "نموذج Gemini 1.5 Flash المجاني"),

        // Community
        "community_title" to Pair("Anonymous Brotherhood", "مجتمع التعافي المجهول"),
        "community_subtitle" to Pair("Zero judgment. Pure shared strength & real stories.", "دون أحكام مسبقة. تكاتف صادق وتجارب حقيقية."),
        "fist_bump" to Pair("Fist Bump", "تحية قوة"),
        "share_story_btn" to Pair("Post Anonymously", "مشاركة مجهولة"),
        "post_dialog_title" to Pair("Share Anonymous Strength", "نشر رسالة دعم مجهولة"),
        "post_alias_label" to Pair("Anonymous Alias", "الاسم المستعار المجهول"),
        "post_content_label" to Pair("Your insight, milestone, or victory...", "اكتب فائدة، إنجاز، أو نصيحة للأخوة..."),
        "post_category_label" to Pair("Category", "التصنيف"),
        "publish_btn" to Pair("Publish Anonymously", "نشر مجهول"),
        "community_empty_title" to Pair("No stories yet", "لا توجد قصص بعد"),
        "community_empty_desc" to Pair(
            "This space is yours. Be the first to share a win, an urge you surfed, or a milestone — anonymously.",
            "هذه المساحة لك. كن أول من يشارك انتصاراً أو رغبة تجاوزتها أو إنجازاً — دون الكشف عن هويتك."
        ),

        // Shield & Monitoring
        "shield_title" to Pair("Smart Shield & Protection", "درع الحماية والمراقبة الذكية"),
        "strict_guard_mode" to Pair("Strict Guard Mode", "نمط الحماية الصارم"),
        "strict_guard_desc" to Pair("Watch the address bar and visible text of browsers, and of the social apps you allow, on this device only", "فحص شريط العنوان والنص الظاهر في المتصفحات، وفي تطبيقات التواصل التي تسمح بها، على جهازك فقط"),
        "guard_apps_consent_title" to Pair("Protect social apps too", "احمِ تطبيقات التواصل أيضاً"),
        "guard_apps_consent_body" to Pair(
            "Extend your Shield to YouTube, X, Instagram, TikTok, Facebook and Reddit. It reads their visible text on this device only, so explicit titles, captions and posts get the same step-back reminder.",
            "وسّع درعك ليشمل يوتيوب وX وإنستغرام وتيك توك وفيسبوك وريديت. يقرأ نصها الظاهر على جهازك فقط، لتحصل العناوين والمنشورات الصريحة على نفس نافذة التراجع."
        ),
        "guard_apps_consent_note" to Pair(
            "Search boxes and suggestion lists are ignored, images and videos are not analysed, and nothing is stored or uploaded.",
            "تُتجاهل خانات البحث وقوائم الاقتراحات، ولا تُحلَّل الصور أو الفيديوهات، ولا يُخزَّن أي شيء أو يُرسَل."
        ),
        "guard_apps_consent_btn" to Pair("Enable for these apps", "تفعيل لهذه التطبيقات"),
        "guard_apps_consent_later" to Pair("Not now", "ليس الآن"),

        // DNS shield (system Private DNS)
        "dns_shield_section_title" to Pair("DNS Shield", "درع DNS"),
        "dns_shield_section_desc" to Pair(
            "Block adult domains before the page loads, in every app",
            "احجب المواقع الإباحية قبل تحميل الصفحة، في كل التطبيقات"
        ),
        "dns_shield_title" to Pair("DNS Shield", "درع DNS"),
        "dns_shield_body" to Pair(
            "Blocks adult domains system-wide through Cloudflare for Families, before any app can load them. It works even when the content monitor is off.",
            "يحجب المواقع الإباحية على مستوى النظام عبر Cloudflare for Families، قبل أن يتمكن أي تطبيق من تحميلها. يعمل حتى عندما يكون درع المراقبة متوقفاً."
        ),
        "dns_shield_privacy" to Pair(
            "DNS lookups go to Cloudflare, not to BreakFree. Page content is never sent.",
            "تذهب استعلامات DNS إلى Cloudflare، وليس إلى تعافي. لا يُرسَل أي محتوى من الصفحات."
        ),
        "dns_shield_status_active" to Pair("Active", "مفعّل"),
        "dns_shield_status_other" to Pair("Another resolver", "مزوّد آخر"),
        "dns_shield_status_off" to Pair("Off", "متوقف"),
        "dns_shield_status_unsupported" to Pair("Unsupported", "غير مدعوم"),
        "dns_shield_status_locked" to Pair("Locked", "مقفل"),
        "dns_shield_active_unlocked" to Pair(
            "Active. You can change it in Android Settings at any time.",
            "مفعّل. يمكنك تغييره من إعدادات أندرويد في أي وقت."
        ),
        "dns_shield_mechanism_private" to Pair(
            "System Private DNS · enforced for every app",
            "DNS الخاص بالنظام · مطبّق على كل التطبيقات"
        ),
        "dns_shield_mechanism_filter" to Pair(
            "On-device filter · DNS queries only",
            "فلتر داخل الجهاز · استعلامات DNS فقط"
        ),
        "dns_shield_turn_on" to Pair("Turn on", "تفعيل"),
        "dns_filter_active" to Pair(
            "Active. Android shows the VPN key while the filter runs, and you can switch it off there.",
            "مفعّل. يُظهر أندرويد مفتاح VPN أثناء عمل الفلتر، ويمكنك إيقافه من هناك."
        ),
        "dns_filter_hint" to Pair(
            "Android will ask you to allow a VPN. BreakFree uses it for DNS queries only — no page, video or message traffic passes through it.",
            "سيطلب منك أندرويد السماح بـ VPN. يستخدمه تعافي لاستعلامات DNS فقط — لا يمر عبره أي محتوى صفحات أو فيديو أو رسائل."
        ),
        "dns_vpn_notification_channel" to Pair("DNS Shield", "درع DNS"),
        "dns_vpn_notification_channel_desc" to Pair(
            "Shown while the on-device DNS filter is protecting you",
            "يظهر أثناء عمل فلتر DNS داخل الجهاز"
        ),
        "dns_vpn_notification_title" to Pair("DNS Shield is on", "درع DNS مفعّل"),
        "dns_vpn_notification_text" to Pair(
            "Adult domains are blocked before they load. DNS queries only.",
            "تُحجب المواقع الإباحية قبل تحميلها. استعلامات DNS فقط."
        ),
        "dns_vpn_notification_stop" to Pair("Turn off", "إيقاف"),

        // First-run Shield setup
        "setup_title" to Pair("Set up your Shield", "جهّز درعك"),
        "setup_intro" to Pair(
            "BreakFree can protect you in three ways. You can change any of them later in the Guard tab.",
            "يمكن لتعافي أن يحميك بثلاث طرق. يمكنك تغيير أي منها لاحقاً من تبويب الحماية."
        ),
        "setup_dns_title" to Pair("DNS Shield", "درع DNS"),
        "setup_dns_desc" to Pair(
            "Blocks adult domains in every app before the page loads. Lookups go to Cloudflare for Families, never to BreakFree.",
            "يحجب المواقع الإباحية في كل التطبيقات قبل تحميل الصفحة. تذهب الاستعلامات إلى Cloudflare for Families، وليس إلى تعافي."
        ),
        "setup_monitor_title" to Pair("Content monitor", "رصد المحتوى"),
        "setup_monitor_desc" to Pair(
            "Warns you when explicit text appears in a browser or a supported app. Runs on-device; nothing is uploaded.",
            "ينبّهك عند ظهور نص صريح في متصفح أو تطبيق مدعوم. يعمل داخل الجهاز، ولا يُرفع أي شيء."
        ),
        "setup_admin_title" to Pair("Device admin", "مسؤول الجهاز"),
        "setup_admin_desc" to Pair(
            "Optional. Lets the 90-day uninstall lock work. BreakFree declares no device policies, so it cannot lock or wipe the phone.",
            "اختياري. يتيح عمل قفل الإزالة لمدة 90 يوماً. لا يطلب تعافي أي صلاحيات إدارة، فلا يستطيع قفل الجهاز أو مسحه."
        ),
        "setup_enable" to Pair("Enable", "تفعيل"),
        "setup_on" to Pair("On", "مفعّل"),
        "setup_set_up" to Pair("Set up", "إعداد"),
        "setup_activate" to Pair("Activate", "تنشيط"),
        "setup_start" to Pair("Start", "ابدأ"),
        "setup_note" to Pair(
            "The 90-day uninstall lock is never enabled here — it stays a separate, explicit choice.",
            "قفل الإزالة لمدة 90 يوماً لا يُفعّل من هنا — يبقى خياراً منفصلاً وصريحاً."
        ),
        "dns_shield_other_host" to Pair(
            "Private DNS is currently set to {host}.",
            "DNS الخاص مضبوط حالياً على {host}."
        ),
        "dns_shield_locked_until" to Pair(
            "Locked until {date}. You can change it when the commitment ends.",
            "مقفل حتى {date}. يمكنك تغييره عند انتهاء الالتزام."
        ),
        "dns_shield_do_hint" to Pair(
            "Device Owner: one tap applies Cloudflare for Families and locks it for the commitment.",
            "مالك الجهاز: نقرة واحدة تطبّق Cloudflare for Families وتقفله طوال الالتزام."
        ),
        "dns_shield_manual_hint" to Pair(
            "Open Private DNS settings, choose “Private DNS provider hostname”, and enter: family.cloudflare-dns.com",
            "افتح إعدادات DNS الخاص، اختر «اسم مضيف مزوّد DNS الخاص»، وأدخل: family.cloudflare-dns.com"
        ),
        "dns_shield_enable" to Pair("Enable DNS Shield", "تفعيل درع DNS"),
        "dns_shield_enable_lock" to Pair("Enable & lock", "تفعيل وقفل"),
        "dns_shield_open_settings" to Pair("Open Private DNS settings", "فتح إعدادات DNS الخاص"),
        "dns_shield_copy_host" to Pair("Copy hostname", "نسخ اسم المضيف"),
        "dns_shield_copied" to Pair("Hostname copied", "تم نسخ اسم المضيف"),
        "dns_shield_check" to Pair("Check again", "تحقق مرة أخرى"),
        "dns_shield_disable" to Pair("Turn off", "إيقاف"),
        "dns_shield_busy" to Pair("Applying…", "جارٍ التطبيق…"),
        "dns_shield_unsupported" to Pair(
            "The DNS Shield needs Android 9 or newer.",
            "يحتاج درع DNS إلى أندرويد 9 أو أحدث."
        ),
        "dns_shield_apply_unsupported" to Pair(
            "This Android version cannot set Private DNS automatically. Use the manual steps below.",
            "لا يمكن لهذا الإصدار ضبط DNS الخاص تلقائياً. استخدم الخطوات اليدوية أدناه."
        ),
        "dns_shield_error_host" to Pair(
            "Cloudflare for Families could not be reached. Check your connection and try again.",
            "تعذّر الوصول إلى Cloudflare for Families. تحقق من اتصالك وحاول مرة أخرى."
        ),
        "dns_shield_error_generic" to Pair(
            "Android could not apply the DNS Shield. Try the manual steps.",
            "تعذّر على أندرويد تطبيق درع DNS. جرّب الخطوات اليدوية."
        ),
        "dns_shield_error_not_owner" to Pair(
            "Automatic setup needs Device Owner. Use the manual steps.",
            "الإعداد التلقائي يحتاج مالك الجهاز. استخدم الخطوات اليدوية."
        ),
        "dns_shield_consent_title" to Pair(
            "Before enabling the DNS Shield",
            "قبل تفعيل درع DNS"
        ),
        "dns_shield_consent_body" to Pair(
            "The DNS Shield blocks adult domains before they load, in every app.\n\nTo do that, your DNS lookups are sent to Cloudflare for Families (family.cloudflare-dns.com). Cloudflare sees the domain names you look up; BreakFree never sees or stores them, and no page content is sent.\n\nAndroid may also ask you to allow a VPN. BreakFree uses it for DNS queries only — no page, video or message traffic passes through it.\n\nA VPN you install yourself, or a browser's own encrypted DNS, can bypass this. It is a strong speed bump, not a content filter.",
            "يحجب درع DNS المواقع الإباحية قبل تحميلها، في كل التطبيقات.\n\nولتحقيق ذلك، تُرسَل استعلامات DNS إلى Cloudflare for Families ‏(family.cloudflare-dns.com). ترى Cloudflare أسماء النطاقات التي تبحث عنها؛ أما تعافي فلا يراها ولا يخزّنها، ولا يُرسَل أي محتوى من الصفحات.\n\nقد يطلب منك أندرويد أيضاً السماح بـ VPN. يستخدمه تعافي لاستعلامات DNS فقط — لا يمر عبره أي محتوى صفحات أو فيديو أو رسائل.\n\nقد يتجاوزه VPN تثبّته بنفسك، أو DNS مشفّر خاص بالمتصفح. إنه حاجز قوي أمام الاندفاع، وليس فلتراً للمحتوى."
        ),
        "dns_shield_consent_accept" to Pair("I agree, enable", "أوافق، فعّل"),
        "dns_shield_consent_cancel" to Pair("Cancel", "إلغاء"),
        "grace_buffer_toggle" to Pair("120s Step-Back Window", "نافذة التراجع (120 ثانية)"),
        "grace_buffer_desc" to Pair("Two minutes to close the page. Nothing is recorded unless you confirm it later", "دقيقتان لإغلاق الصفحة. لا يُسجَّل أي شيء إلا إذا أكّدت ذلك لاحقاً"),
        "keyword_filter_toggle" to Pair("Text & Keyword Filter", "فلتر النصوص والكلمات"),
        "keyword_filter_desc" to Pair("Also match explicit keywords in page text, not just known domains", "مطابقة الكلمات الصريحة في نص الصفحة أيضاً، وليس المواقع المعروفة فقط"),
        "badges_view_all" to Pair("View All", "عرض الكل"),
        "badges_hide" to Pair("Collapse", "طي"),
        "service_status" to Pair("Content Monitoring Service", "خدمة رصد المحتوى الصريح"),
        "service_active" to Pair("Active & Guarding", "نشطة وتعمل بالحماية"),
        "service_inactive" to Pair("Inactive - Tap to Enable", "غير مفعلة - اضغط للتمكين"),
        "grace_rule_card_title" to Pair("The 2-Minute Grace Period Protocol", "بروتوكول فترة السماح (دقيقتان)"),
        "grace_rule_card_desc" to Pair("Unlike harsh blockers that immediately shame and wipe progress on accidental or impulsive clicks, BreakFree grants you a 120-second conscious window. Step back within 2 minutes and your streak remains 100% intact.", "على عكس التطبيقات التقليدية التي تحبط المتعافي بالتصفير الفوري، تمنحك بريك فري نافذة وعي مدتها 120 ثانية. إذا تراجعت وأغلقت المحتوى، يبقى عدادك محفوظاً تقديراً لقوة إرادتك."),
        "test_scanner_title" to Pair("Explicit Domain Scanner", "فاحص الروابط والمحفزات"),
        "test_scanner_desc" to Pair("Monitors browser URLs, search queries and explicit keywords across Chrome, Firefox, system browsers and the supported social apps.", "يفحص عناوين المتصفحات وعبارات البحث الصريحة في كروم وفايرفوكس والمتصفحات الأخرى وتطبيقات التواصل المدعومة."),
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
        "cloud_synced" to Pair("Cloud Synced", "متزامن سحابياً"),

        // Settings
        "tab_settings" to Pair("Settings", "الإعدادات"),
        "settings_title" to Pair("Settings", "الإعدادات"),
        "settings_subtitle" to Pair("Make BreakFree fit you", "اجعل تعافي يناسبك"),
        "settings_account" to Pair("Account & Sync", "الحساب والمزامنة"),
        "settings_appearance" to Pair("Appearance", "المظهر"),
        "settings_appearance_desc" to Pair("Language and interface direction", "اللغة واتجاه الواجهة"),
        "settings_theme" to Pair("Theme", "المظهر"),
        "settings_theme_desc" to Pair("Dark, light, or follow the system", "داكن، فاتح، أو حسب النظام"),
        "theme_dark" to Pair("Dark", "داكن"),
        "theme_light" to Pair("Light", "فاتح"),
        "update_available_title" to Pair("Update available", "تحديث متاح"),
        "update_available_body" to Pair(
            "Version {version} is available. Download and install it now?",
            "الإصدار {version} متاح. هل ترغب في تنزيله وتثبيته الآن؟"
        ),
        "update_download_install" to Pair("Download & install", "تنزيل وتثبيت"),
        "update_later" to Pair("Later", "لاحقاً"),
        "settings_updates" to Pair("Updates", "التحديثات"),
        "settings_current_version" to Pair("Current version", "الإصدار الحالي"),
        "settings_check_updates" to Pair("Check for updates", "البحث عن تحديثات"),
        "settings_checking_updates" to Pair("Checking…", "جارٍ البحث…"),
        "settings_up_to_date" to Pair("You're up to date", "أنت على آخر إصدار"),
        "settings_auto_check_updates" to Pair("Check automatically", "بحث تلقائي"),
        "settings_language_desc" to Pair("Choose the language of the app", "اختر لغة التطبيق"),
        "settings_protection" to Pair("Protection", "الحماية"),
        "settings_protection_desc" to Pair("Shield status, filters and step-back window", "حالة الدرع والفلاتر ونافذة التراجع"),
        "settings_protection_active" to Pair("Shield is on", "الدرع مفعّل"),
        "settings_protection_inactive" to Pair("Shield is off", "الدرع متوقف"),
        "settings_notifications" to Pair("Notifications", "الإشعارات"),
        "settings_notifications_desc" to Pair("Step-back alerts and daily reminders", "تنبيهات التراجع والتذكيرات اليومية"),
        "settings_notifications_ok" to Pair("Allowed", "مسموحة"),
        "settings_notifications_off" to Pair("Blocked in Android settings", "محظورة من إعدادات أندرويد"),
        "settings_open_system" to Pair("Open", "فتح"),
        "settings_overlay" to Pair("Show reminders over other apps", "إظهار التنبيهات فوق التطبيقات الأخرى"),
        "settings_overlay_ok" to Pair("Allowed — appears above the app", "مسموح — يظهر فوق التطبيق"),
        "settings_overlay_off" to Pair("Off — reminders arrive as notifications", "متوقف — تصل التنبيهات كإشعارات"),
        "overlay_permission_title" to Pair("Show the reminder over the app", "إظهار التنبيه فوق التطبيق"),
        "overlay_permission_body" to Pair(
            "Android needs one more permission for the step-back reminder to appear on top of the app. Without it, the reminder still arrives as a notification.\n\nBreakFree only draws this one card, only while the countdown is running, and it disappears as soon as you leave the page.",
            "يحتاج أندرويد إلى إذن إضافي ليظهر تنبيه التراجع فوق التطبيق. بدون هذا الإذن سيصل التنبيه كإشعار عادي.\n\nلا يرسم تعافي سوى هذه البطاقة، وفقط أثناء العد التنازلي، وتختفي بمجرد مغادرتك الصفحة."
        ),
        "overlay_permission_continue" to Pair("Continue", "متابعة"),

        // 90-day uninstall commitment (device administrator, no policies)
        "settings_uninstall_lock" to Pair("Strict Uninstall Lock (90 days)", "قفل الحذف الصارم (90 يوماً)"),
        "settings_lock_off" to Pair("Off — tap to activate 90-day lock", "متوقف — اضغط لتفعيل قفل 90 يوماً"),
        "settings_lock_until" to Pair("Locked until %date% · %days% days left", "مقفل حتى %date% · يتبقى %days% يوماً"),
        "settings_lock_done" to Pair("Commitment complete — lock released", "اكتمل الالتزام — تم رفع القفل"),
        "uninstall_lock_title" to Pair("Strict 90-Day Recovery Lock", "قفل التعافي الصارم لـ 90 يوماً"),
        "uninstall_lock_body" to Pair(
            "This makes BreakFree an active Device Administrator. Android will then refuse to uninstall it: the only way out is to deactivate it in system Settings, which turns a one-tap uninstall into a deliberate trip through menus.\n\nBreakFree asks for no control over your phone — it cannot lock the screen, wipe data or set passwords. The administrator releases itself automatically when the 90 days end.\n\nAre you ready to commit for 90 days?",
            "يفعّل هذا الخيار تطبيق تعافي كمدير نشط للجهاز. عندها سيرفض أندرويد إزالته: الطريق الوحيد للخروج هو إلغاء التنشيط من إعدادات النظام، وهذا يحوّل الحذف بضغطة واحدة إلى رحلة مقصودة عبر القوائم.\n\nلا يطلب تعافي أي تحكم في هاتفك — لا يقفل الشاشة ولا يمحو البيانات ولا يضع كلمات مرور. وتُلغى صلاحية الإدارة تلقائياً عند انتهاء التسعين يوماً.\n\nهل أنت مستعد للالتزام التام لـ 90 يوماً؟"
        ),
        "uninstall_lock_confirm" to Pair("I commit for 90 days", "أنا ملتزم لـ 90 يوماً"),
        "uninstall_lock_later" to Pair("Not now", "ليس الآن"),
        "uninstall_lock_active_title" to Pair("90-Day Commitment Active", "قفل الالتزام لـ 90 يوماً مفعّل"),
        "uninstall_lock_active_body" to Pair(
            "BreakFree is an active Device Administrator until %date% (%days% days remaining).\n\nAndroid will refuse to uninstall it until then, so removing it means deliberately deactivating it in system Settings. Stay strong!",
            "تطبيق تعافي مدير نشط للجهاز حتى %date% (متبقي %days% يوماً).\n\nسيرفض أندرويد إزالته حتى ذلك الحين، ولن تتم الإزالة إلا بإلغاء التنشيط عمداً من إعدادات النظام. استمر واصمد!"
        ),
        "uninstall_lock_stay_strong" to Pair("I will stay strong", "سأبقى ملتزماً واصمد"),

        // Maximum protection: the two ways Android allows an app to become Device Owner
        "do_setup_open" to Pair("Maximum protection (Device Owner)", "أقصى حماية (مالك الجهاز)"),
        "do_setup_title" to Pair("Maximum protection", "أقصى درجات الحماية"),
        "do_setup_subtitle" to Pair(
            "Make Android itself disable the uninstall button",
            "اجعل أندرويد نفسه يعطّل زر حذف التطبيق"
        ),
        "do_setup_intro" to Pair(
            "This is optional and stronger than the 90-day administrator lock. It needs one extra setup step, because Android only lets an app block its own uninstall when it is the phone's Device Owner.\n\nAfter setup the 90 days start when BreakFree first opens, and the lock releases itself when they end.",
            "هذا الخيار اختياري وأقوى من قفل المدير لمدة 90 يوماً. يحتاج إلى خطوة إعداد إضافية واحدة، لأن أندرويد لا يسمح لتطبيق بتعطيل حذفه إلا إذا كان مالك الجهاز.\n\nبعد الإعداد تبدأ الـ 90 يوماً عند أول فتح لتطبيق تعافي، ويُرفع القفل تلقائياً عند انتهائها."
        ),
        "do_owner_active_title" to Pair("Device Owner is active", "وضع مالك الجهاز مفعّل"),
        "do_owner_active_body" to Pair(
            "Android's uninstall button is disabled. The lock releases on %date%.",
            "زر الحذف في أندرويد معطّل. يُرفع القفل في %date%."
        ),
        "do_pc_title" to Pair("Option 1 — From a computer", "الخيار 1 — من الكمبيوتر"),
        "do_pc_step1" to Pair(
            "1. On this phone, enable Developer options and USB debugging.",
            "1. فعّل خيارات المطور وتصحيح USB على هذا الهاتف."
        ),
        "do_pc_step2" to Pair(
            "2. Remove every Google account first (Settings → Passwords & accounts). Android refuses Device Owner while an account exists.",
            "2. احذف كل حسابات جوجل أولاً (الإعدادات ← كلمات المرور والحسابات). يرفض أندرويد وضع مالك الجهاز ما دام هناك حساب."
        ),
        "do_pc_step3" to Pair(
            "3. Connect the phone to the computer and run:",
            "3. وصّل الهاتف بالكمبيوتر ونفّذ:"
        ),
        "do_pc_note" to Pair(
            "The output must say Success. Reopen BreakFree afterwards: the 90-day clock starts and the uninstall button is disabled.",
            "يجب أن تكون النتيجة Success. أعد فتح تعافي بعدها: تبدأ الـ 90 يوماً ويُعطّل زر الحذف."
        ),
        "do_copy" to Pair("Copy command", "نسخ الأمر"),
        "do_copied" to Pair("Copied", "تم النسخ"),
        "do_qr_title" to Pair("Option 2 — QR setup, no computer", "الخيار 2 — إعداد عبر رمز QR بدون كمبيوتر"),
        "do_qr_warning" to Pair(
            "This option factory-resets the phone: everything on it is erased. Back up anything you need first.",
            "هذا الخيار يعيد ضبط الهاتف من المصنع: تُمحى كل بياناته. انسخ ما تحتاجه أولاً."
        ),
        "do_qr_step1" to Pair(
            "1. Screenshot the code below, or open it on another screen.",
            "1. خذ لقطة شاشة للرمز أدناه، أو اعرضه على شاشة أخرى."
        ),
        "do_qr_step2" to Pair("2. Factory reset the phone.", "2. أعد ضبط الهاتف من المصنع."),
        "do_qr_step3" to Pair(
            "3. On the Welcome screen, tap the same spot 6 times to open the QR scanner.",
            "3. في شاشة الترحيب اضغط في المكان نفسه 6 مرات لفتح ماسح الرمز."
        ),
        "do_qr_step4" to Pair(
            "4. Connect to Wi-Fi and scan the code. Android installs BreakFree and makes it Device Owner.",
            "4. اتصل بشبكة واي فاي وامسح الرمز. سيثبّت أندرويد تعافي ويجعله مالك الجهاز."
        ),
        "do_qr_no_account" to Pair(
            "Do not sign in to a Google account during setup — Android refuses Device Owner when an account exists.",
            "لا تسجّل الدخول إلى حساب جوجل أثناء الإعداد — يرفض أندرويد وضع مالك الجهاز عند وجود حساب."
        ),
        "do_qr_link_label" to Pair(
            "Public HTTPS link to this exact APK",
            "رابط HTTPS عام لهذا الملف نفسه"
        ),
        "do_qr_generate" to Pair("Generate QR", "إنشاء الرمز"),
        "do_qr_invalid" to Pair(
            "Enter a public https:// link to the APK.",
            "أدخل رابط https:// عاماً لملف APK."
        ),
        "do_qr_failed" to Pair(
            "Could not read this APK's checksum.",
            "تعذّرت قراءة بصمة هذا الملف."
        ),
        "do_qr_checksum" to Pair(
            "APK SHA-256 (must match the file at the link):\n%hash%",
            "بصمة APK SHA-256 (يجب أن تطابق الملف على الرابط):\n%hash%"
        ),
        "do_qr_note" to Pair(
            "The QR is built from that link and the SHA-256 of the BreakFree installed on this phone. Host that exact file, or provisioning will fail.",
            "يُبنى الرمز من الرابط وبصمة SHA-256 لنسخة تعافي المثبتة على هذا الهاتف. استضف هذا الملف نفسه وإلا سيفشل الإعداد."
        ),
        "do_close" to Pair("Close", "إغلاق"),
        "settings_data" to Pair("Recovery data", "بيانات التعافي"),
        "settings_data_desc" to Pair("Start date, streak reset and local history", "تاريخ البداية وتصفير العداد والسجل المحلي"),
        "settings_about" to Pair("About", "حول التطبيق"),
        "settings_about_desc" to Pair("Version, privacy and support", "الإصدار والخصوصية والدعم"),
        "settings_privacy" to Pair("Privacy", "الخصوصية"),
        "settings_privacy_desc" to Pair(
            "Mood logs and streak history stay on this device. Chat text is only sent to Gemini after you agree, and the Shield never uploads the pages it reads.",
            "سجلات المشاعر وتاريخ التعافي تبقى على هذا الجهاز. لا تُرسل رسائل المحادثة إلى Gemini إلا بعد موافقتك، والدرع لا يرفع الصفحات التي يقرأها أبداً."
        ),
        "settings_crisis" to Pair("Need urgent help?", "تحتاج مساعدة عاجلة؟"),
        "settings_crisis_desc" to Pair(
            "If you are in immediate danger, contact your local emergency number. BreakFree is a support tool, not a crisis service.",
            "إذا كنت في خطر مباشر، اتصل برقم الطوارئ في بلدك. تعافي أداة دعم وليس خدمة طوارئ."
        ),
        "settings_version" to Pair("Version", "الإصدار"),
        "settings_footer" to Pair("Made for the quiet battles.", "صُنع للمعارك الهادئة."),
        "settings_guard_shortcut" to Pair("Open the Shield", "افتح درع الحماية"),

        // Home additions
        "home_greeting" to Pair("Welcome back", "أهلاً بعودتك"),
        "home_greeting_anon" to Pair("Your journey, quietly protected", "تعافيك محفوظ بهدوء"),
        "quick_actions" to Pair("Quick actions", "إجراءات سريعة"),
        "check_in_short" to Pair("Check in", "تسجيل"),
        "sync_off" to Pair("Not synced", "غير متزامن"),
        "stat_today" to Pair("Today", "اليوم"),
        "quick_stats" to Pair("At a glance", "نظرة سريعة"),
        "badges_locked" to Pair("Locked", "مقفل"),
        "badges_unlocked_label" to Pair("Unlocked", "مُحقق"),
        "badges_progress" to Pair("unlocked", "مُحقق"),

        // Guard tab
        "guard_subtitle" to Pair("Protection that runs on your device", "حماية تعمل على جهازك"),
        "protection_controls" to Pair("Protection controls", "إعدادات الحماية"),
        "shield_running" to Pair("The Shield is running", "الدرع يعمل الآن"),
        "shield_stopped" to Pair("The Shield is not running", "الدرع متوقف"),
        "shield_open_settings" to Pair("Open Accessibility Settings", "إعدادات إمكانية الوصول"),
        "shield_manage" to Pair("Manage access", "إدارة الوصول"),
        "scanner_title" to Pair("Detection rules", "قواعد الرصد"),
        "scanner_rules" to Pair("rules loaded", "قاعدة محمّلة"),
        "simulate_grace_desc" to Pair(
            "Preview the 120-second step-back window (debug builds only)",
            "معاينة نافذة الـ 120 ثانية للتراجع الواعي (إصدار تجريبي فقط)"
        ),
        "buffer_label" to Pair("120s buffer", "نافذة 120 ثانية"),
        "guard_overlay_hint_title" to Pair("Reminders are notifications only", "التنبيهات تظهر كإشعارات فقط"),
        "guard_overlay_hint_body" to Pair(
            "Allow “display over other apps” so the step-back reminder appears above the app instead of only in the notification shade.",
            "اسمح بـ«العرض فوق التطبيقات الأخرى» ليظهر تنبيه التراجع فوق التطبيق بدلاً من شريط الإشعارات فقط."
        ),
        "guard_overlay_hint_btn" to Pair("Allow overlay", "السماح بالعرض فوق التطبيقات"),

        // AI tab
        "ai_thinking" to Pair("Composing guidance…", "المرشد يجهّز نصيحة…"),
        "ai_offline_note" to Pair("Offline guidance — not personalised", "إرشاد عام — ليس رداً مخصصاً"),
        "breathing_sos_chip" to Pair("4-7-8 Breathing SOS", "تنفس 4-7-8 العاجل"),

        // Community tab
        "community_brotherhood" to Pair("Anonymous support", "دعم مجهول"),
        "community_clean" to Pair("clean", "نقية"),
        "community_post_btn" to Pair("Share", "شارك")
    )
}
