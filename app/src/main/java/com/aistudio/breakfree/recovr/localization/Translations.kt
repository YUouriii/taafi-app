package com.aistudio.breakfree.recovr.localization

import com.aistudio.breakfree.recovr.model.AppLanguage

object Translations {
    fun t(key: String, language: AppLanguage): String {
        val entry = strings[key] ?: return key
        return when (language) {
            AppLanguage.ENGLISH -> entry.first
            AppLanguage.ARABIC -> entry.second
            AppLanguage.FRENCH -> entry.third
        }
    }

    private val strings = mapOf(
        // General & Navigation
        "app_name" to Triple("BreakFree", "تعافي", "BreakFree"),
        "tab_home" to Triple("Home", "الرئيسية", "Accueil"),
        "tab_guard" to Triple("Guard", "الحماية", "Protection"),
        "tab_ai_chat" to Triple("AI Chat", "المرشد", "Chat IA"),
        "tab_community" to Triple("Community", "المجتمع", "Communauté"),
        "tab_dashboard" to Triple("Home", "الرئيسية", "Accueil"),
        "tab_ai_companion" to Triple("AI Chat", "المرشد", "Chat IA"),
        "tab_monitoring" to Triple("Guard", "الحماية", "Protection"),

        // Dashboard
        "clean_time_title" to Triple("Current Clean Time", "وقت التعافي المستمر", "Temps de récupération actuel"),
        "days" to Triple("Days", "أيام", "Jours"),
        "hours" to Triple("Hours", "ساعات", "Heures"),
        "minutes" to Triple("Mins", "دقائق", "Min"),
        "seconds" to Triple("Secs", "ثوانٍ", "Sec"),
        "dopamine_score" to Triple("Dopamine Recovery Score", "مؤشر تعافي الدوبامين", "Score de récupération dopaminergique"),
        "dopamine_score_desc" to Triple("Neuroplastic healing of dopamine receptors and baseline sensitivity.", "الشفاء العصبي لمستقبلات الدوبامين واستعادة الحساسية الطبيعية.", "Guérison neuroplastique des récepteurs de la dopamine et restauration de la sensibilité de base."),
        "current_phase" to Triple("Current Phase", "المرحلة الحالية", "Phase actuelle"),
        "next_milestone" to Triple("Next Milestone", "الهدف القادم", "Prochaine étape"),
        "longest_streak" to Triple("Personal Best", "أطول سلسلة", "Meilleur record"),
        "grace_saves" to Triple("Grace Period Saves", "تراجعات الوعي الناجحة", "Sauvetages par période de grâce"),
        "sos_sessions" to Triple("SOS Completed", "جلسات SOS المكتملة", "Sessions SOS terminées"),
        "badges_title" to Triple("Milestones & Badges", "الأوسمة والإنجازات", "Étapes & badges"),
        "badges_unlocked" to Triple("Unlocked", "تم تحقيقها", "Débloqués"),
        "emergency_sos_btn" to Triple("EMERGENCY SOS", "طوارئ SOS", "SOS URGENCE"),
        "sos_subtext" to Triple("Breathe & conquer the urge right now", "تنفس واكسر حدة الرغبة فوراً", "Respirez et surmontez l'envie maintenant"),
        "simulate_grace_btn" to Triple("Simulate 120s Grace Period", "تجربة فترة السماح (120 ثانية)", "Simuler 120 s de grâce"),
        "reset_streak" to Triple("Reset Streak / Relapse", "تصفير العداد / تسجيل انتكاسة", "Réinitialiser / Rechute"),
        "adjust_start_date" to Triple("Adjust Start Timestamp", "تعديل تاريخ البداية", "Ajuster la date de début"),

        // Grace Period
        "grace_banner_title" to Triple("Mindfulness Check: Step Back!", "وقفة وعي: تراجع الآن لحماية تعافيك!", "Pause de pleine conscience : reculez !"),
        "grace_banner_desc" to Triple("Explicit content detected. You have 120 seconds to close the app/page without resetting your streak.", "تم رصد محتوى صريح. لديك 120 ثانية لإغلاق الصفحة أو التطبيق دون أن يتأثر عداد تعافيك.", "Contenu explicite détecté. Vous avez 120 secondes pour fermer la page/l'application sans réinitialiser votre série."),
        "grace_time_remaining" to Triple("Grace Period Remaining", "الوقت المتبقي في فترة السماح", "Temps restant de grâce"),
        "grace_detected_target" to Triple("Detected target", "المحتوى المرصود", "Cible détectée"),
        "grace_victory_title" to Triple("Streak Preserved! Great Victory!", "تم الحفاظ على تعافيك! نصر عظيم!", "Série préservée ! Grande victoire !"),
        "grace_victory_msg" to Triple("You took control and stepped back during the grace period. This strengthens your neural discipline!", "لقد انتصرت على الرغبة الملحة وتراجعت بوعي. هذا يعزز مسارات ضبط النفس في دماغك!", "Vous avez repris le contrôle et reculé pendant la période de grâce. Cela renforce votre discipline neuronale !"),
        "grace_relapse_title" to Triple("Exposure Exceeded 2 Minutes", "تجاوزت فترة السماح دقيقتين", "Exposition dépassant 2 minutes"),
        "grace_relapse_msg" to Triple("Continuous exposure lasted past 120 seconds. Streak has been reset, but remember: your prior healing isn't lost. Take a deep breath and rise again.", "استمر التعرض لأكثر من 120 ثانية. تم إعادة ضبط العداد، لكن تذكر: لم تفقد قوتك السابقة. خذ نفساً عميقاً وابدأ من جديد.", "L'exposition continue a dépassé 120 secondes. La série a été réinitialisée, mais souvenez-vous : votre guérison antérieure n'est pas perdue. Respirez profondément et relevez-vous."),
        "close" to Triple("Close", "إغلاق", "Fermer"),
        "cancel" to Triple("Cancel", "إلغاء", "Annuler"),
        "save" to Triple("Save", "حفظ", "Enregistrer"),
        "clear" to Triple("Clear", "مسح", "Effacer"),
        "continue_journey" to Triple("Continue Journey", "مواصلة المسيرة", "Poursuivre le parcours"),

        // System overlay
        "overlay_title" to Triple("Step back from this page", "تراجع عن هذه الصفحة", "Reculez de cette page"),
        "overlay_body" to Triple(
            "This page matched your filter. Close this page or leave this app to keep your streak. Nothing is recorded unless you confirm it later.",
            "هذه الصفحة تطابق فلترك. أغلق هذه الصفحة أو اخرج من التطبيق للحفاظ على تعافيك. لن يُسجَّل أي شيء قبل تأكيدك لاحقاً.",
            "Cette page correspond à votre filtre. Fermez-la ou quittez l'application pour préserver votre série. Rien n'est enregistré sans votre confirmation."
        ),
        "overlay_stays" to Triple(
            "This reminder stays until you close the page or leave the app.",
            "يبقى هذا التنبيه حتى تغلق الصفحة أو تخرج من التطبيق.",
            "Ce rappel reste jusqu'à ce que vous fermiez la page ou quittiez l'application."
        ),

        // Sticky notification
        "grace_notification_channel" to Triple("BreakFree Mindfulness Shield", "درع الوعي من تعافي", "Bouclier de pleine conscience BreakFree"),
        "grace_notification_channel_desc" to Triple("Alerts during the step-back window countdown", "تنبيهات أثناء العد التنازلي لنافذة التراجع", "Alertes pendant le décompte de la fenêtre de recul"),
        "grace_notification_title" to Triple("Mindfulness check — step back", "وقفة وعي — تراجع الآن", "Pause de pleine conscience — reculez"),
        "grace_notification_text" to Triple(
            "Close this page or leave the app to protect your streak.",
            "أغلق هذه الصفحة أو اخرج من التطبيق لحماية تعافيك.",
            "Fermez cette page ou quittez l'application pour protéger votre série."
        ),
        "grace_notification_detected" to Triple("Something explicit was detected.", "تم رصد محتوى صريح.", "Un contenu explicite a été détecté."),
        "grace_notification_detected_stealth" to Triple(
            "A reminder was triggered.",
            "تم تفعيل تذكير.",
            "Un rappel a été déclenché."
        ),
        "grace_notification_remaining" to Triple(
            "You have {time} left of your {total}s window.",
            "بقي لديك {time} من نافذة {total} ثانية.",
            "Il vous reste {time} sur votre fenêtre de {total} s."
        ),
        "grace_notification_stays" to Triple(
            "This notification stays until you close the page or leave the app.",
            "يبقى هذا الإشعار حتى تغلق الصفحة أو تخرج من التطبيق.",
            "Cette notification reste jusqu'à ce que vous fermiez la page ou quittiez l'application."
        ),
        "grace_notification_not_recorded" to Triple("Nothing has been recorded yet.", "لم يُسجَّل أي شيء بعد.", "Rien n'a encore été enregistré."),

        // SOS Breathing
        "sos_modal_title" to Triple("Emergency Urge De-escalation", "نزع فتيل الرغبة العاجل", "Désescalade d'urgence de l'envie"),
        "sos_breathing_tab" to Triple("4-7-8 Breathing", "تنفس 4-7-8", "Respiration 4-7-8"),
        "sos_grounding_tab" to Triple("Grounding 5-4-3-2-1", "تقنية التثبيت 5-4-3-2-1", "Ancrage 5-4-3-2-1"),
        "sos_rules_tab" to Triple("Urge Surfing Rules", "قواعد ركوب الرغبة", "Règles du surf d'envie"),
        "breathe_inhale" to Triple("Inhale Deeply...", "شهيق عميق...", "Inspirez profondément..."),
        "breathe_hold" to Triple("Hold & Focus...", "احبس النفس وركز...", "Retenez & concentrez-vous..."),
        "breathe_exhale" to Triple("Slowly Exhale Tension...", "زفير بطيء وتفريغ التوتر...", "Expirez lentement la tension..."),
        "cycle" to Triple("Cycle", "الدورة", "Cycle"),
        "i_am_calm_btn" to Triple("I Feel Grounded & Calm Now", "أشعر بالهدوء والسيطرة الآن", "Je me sens ancré(e) et calme"),
        "grounding_step5" to Triple("Look around: Name 5 calm items you see in your environment.", "انظر حولك: حدد 5 أشياء تراها في محيطك واشعر بوجودك هنا الآن.", "Regardez autour de vous : nommez 5 objets calmes que vous voyez."),
        "grounding_step4" to Triple("Touch 4 physical surfaces nearby (desk, cloth, screen, wall).", "المس 4 أسطح مادية بجانبك (المكتب، القماش، الحائط، الهاتف).", "Touchez 4 surfaces physiques à proximité (bureau, tissu, écran, mur)."),
        "grounding_step3" to Triple("Listen carefully: Notice 3 distinct sounds around you.", "أصغِ جيداً: لاحظ 3 أصوات مختلفة في محيطك الحالي.", "Écoutez attentivement : repérez 3 sons distincts autour de vous."),
        "grounding_step2" to Triple("Inhale deeply: Identify 2 scents or take 2 fresh breaths.", "استنشق بعمق: ميّز رائحتين أو خذ نفسين عميقين من هواء نقي.", "Inspirez profondément : identifiez 2 odeurs ou prenez 2 respirations fraîches."),
        "grounding_step1" to Triple("Sip cold water: Focus completely on the cold physical sensation.", "اشرب ماء بارداً: ركّز تماماً على برودته وتأثيره الحسي المهدئ.", "Buvez de l'eau fraîche : concentrez-vous sur la sensation physique du froid."),
        "rule_15min_title" to Triple("The 15-Minute Neuro Wave", "موجة الـ 15 دقيقة العصبية", "La vague neuro de 15 minutes"),
        "rule_15min_desc" to Triple("Urges feel overwhelming but they behave like ocean waves. They naturally peak at 10-15 minutes, then steadily drop as dopamine hunger resets.", "الرغبة الملحة تشبه موجة البحر؛ تبلغ ذروتها خلال 10-15 دقيقة ثم تتلاشى تدريجياً مع استقرار الدوبامين.", "Les envies semblent écrasantes mais se comportent comme des vagues. Elles culminent à 10-15 minutes, puis diminuent à mesure que la faim dopaminergique se réinitialise."),
        "rule_cold_water" to Triple("Splash Cold Water On Face", "اغسل وجهك بماء مثلج", "Éclaboussez votre visage d'eau froide"),
        "rule_cold_desc" to Triple("Triggers the mammalian dive reflex, immediately slowing heart rate and resetting sympathetic adrenaline spikes.", "يفعل رد الفعل الغطسي الفطري، مما يخفض نبضات القلب فوراً ويهدئ الجهاز العصبي.", "Déclenche le réflexe de plongée mammalien, ralentissant immédiatement le rythme cardiaque et réinitialisant les pics d'adrénaline."),

        // AI Urge Companion
        "ai_title" to Triple("Your Resilience Companion", "مرشد الصمود", "Votre compagnon de résilience"),
        "ai_subtitle" to Triple("CBT-based urge surfing & cognitive reframing", "تقنيات العلاج السلوكي المعرفي وركوب الرغبات", "Surf d'envie & recadrage cognitif basés sur la TCC"),
        "ai_input_placeholder" to Triple("Describe what you are feeling right now...", "اكتب ما تشعر به الآن أو المحفز الذي واجهته...", "Décrivez ce que vous ressentez maintenant..."),
        "ai_send" to Triple("Send", "إرسال", "Envoyer"),
        "ai_chip_intense_urge" to Triple("Intense urge right now!", "رغبة ملحة جداً الآن!", "Envie intense maintenant !"),
        "ai_chip_halt" to Triple("Check my HALT triggers", "فحص محفزات HALT", "Vérifier mes déclencheurs HALT"),
        "ai_chip_night_lonely" to Triple("Late night & feeling lonely", "وقت متأخر وشعور بالوحدة", "Tard le soir & solitude"),
        "ai_chip_why_started" to Triple("Remind me why I started", "ذكرني بأسبابي للتعافي", "Rappelez-moi pourquoi j'ai commencé"),
        "ai_sign_in_required_title" to Triple("Sign In with Google to Unlock AI", "سجّل الدخول عبر Google لتفعيل المساعد الذكي", "Connectez-vous avec Google pour débloquer l'IA"),
        "ai_sign_in_required_desc" to Triple(
            "Sign in with your Google account to enable real-time AI counseling tailored to your recovery journey.",
            "سجّل الدخول بحساب Google الخاص بك لتفعيل الإرشاد الذكي المخصص لرحلة تعافيك.",
            "Connectez-vous avec votre compte Google pour activer le conseil IA en temps réel adapté à votre parcours."
        ),
        "ai_sign_in_button" to Triple("Sign in with Google", "تسجيل الدخول باستخدام Google", "Se connecter avec Google"),
        "ai_api_key_btn" to Triple("Gemini API key", "مفتاح Gemini API", "Clé API Gemini"),
        "ai_provider_dialog_title" to Triple("Connect your AI model", "اربط نموذج الذكاء الاصطناعي", "Connectez votre modèle IA"),
        "ai_provider_dialog_desc" to Triple(
            "All you need is one free Google API key — create it in AI Studio (takes under a minute), paste it here, and you're done. BreakFree only uses Gemini's free models, so there are no costs and no billing, ever. The key is stored encrypted on this device and is only sent to Google.",
            "كل ما تحتاجه هو مفتاح Google مجاني واحد — أنشئه في AI Studio (لا يستغرق أكثر من دقيقة)، ألصقه هنا، وانتهى الأمر. يستخدم BreakFree نماذج Gemini المجانية فقط، فلا توجد أي تكلفة أو فوترة أبداً. يُحفظ المفتاح مشفّراً على هذا الجهاز ولا يُرسل إلا إلى Google.",
            "Il vous suffit d'une seule clé Google gratuite — créez-la dans AI Studio (moins d'une minute), collez-la ici, et c'est tout. BreakFree n'utilise que les modèles gratuits de Gemini : aucun coût, aucune facturation, jamais. La clé est stockée chiffrée sur cet appareil et n'est envoyée qu'à Google."
        ),
        "ai_provider_label" to Triple("Provider", "المزوّد", "Fournisseur"),
        "ai_model_label" to Triple("Model", "النموذج", "Modèle"),
        "ai_base_url_label" to Triple("Base URL (optional)", "عنوان الخدمة (اختياري)", "URL de base (optionnel)"),
        "ai_api_key_label" to Triple("API key", "مفتاح API", "Clé API"),
        "ai_api_key_save" to Triple("Save", "حفظ", "Enregistrer"),
        "ai_api_key_clear" to Triple("Clear key", "مسح المفتاح", "Effacer la clé"),
        "ai_get_key" to Triple("Get an API key", "احصل على مفتاح API", "Obtenir une clé API"),
        "ai_test_connection" to Triple("Test connection", "اختبار الاتصال", "Tester la connexion"),
        "ai_load_models" to Triple("Load models", "تحميل النماذج", "Charger les modèles"),
        "ai_loading_models" to Triple("Loading models…", "جارٍ تحميل النماذج…", "Chargement des modèles…"),
        "ai_models_none" to Triple("No models returned — enter one manually.", "لم تُرجع أي نماذج — أدخلها يدوياً.", "Aucun modèle renvoyé — saisissez-en un manuellement."),
        "ai_models_failed" to Triple("Couldn't load models — enter one manually.", "تعذّر تحميل النماذج — أدخلها يدوياً.", "Impossible de charger les modèles — saisissez-en un manuellement."),
        "ai_offline_mode" to Triple("Offline guidance (no model connected)", "إرشاد دون اتصال (لا يوجد نموذج متصل)", "Guidage hors ligne (aucun modèle connecté)"),
        "ai_suggestions" to Triple("Suggestions", "اقتراحات", "Suggestions"),
        "ai_show_suggestions" to Triple("Show", "إظهار", "Afficher"),
        "ai_hide_suggestions" to Triple("Hide", "إخفاء", "Masquer"),
        "ai_provider_saved" to Triple("AI provider saved", "تم حفظ مزوّد الذكاء الاصطناعي", "Fournisseur IA enregistré"),
        "ai_key_required" to Triple("Enter an API key to use this model.", "أدخل مفتاح API لاستخدام هذا النموذج.", "Saisissez une clé API pour utiliser ce modèle."),

        // Community
        "community_title" to Triple("Anonymous Brotherhood", "مجتمع التعافي المجهول", "Fraternité anonyme"),
        "community_subtitle" to Triple("Zero judgment. Pure shared strength & real stories.", "دون أحكام مسبقة. تكاتف صادق وتجارب حقيقية.", "Zéro jugement. Force partagée et vraies histoires."),
        "fist_bump" to Triple("Fist Bump", "تحية قوة", "Coup de poing"),
        "share_story_btn" to Triple("Post Anonymously", "مشاركة مجهولة", "Publier anonymement"),
        "post_dialog_title" to Triple("Share Anonymous Strength", "نشر رسالة دعم مجهولة", "Partager une force anonyme"),
        "post_alias_label" to Triple("Anonymous Alias", "الاسم المستعار المجهول", "Pseudo anonyme"),
        "post_content_label" to Triple("Your insight, milestone, or victory...", "اكتب فائدة، إنجاز، أو نصيحة للأخوة...", "Votre idée, étape ou victoire..."),
        "post_category_label" to Triple("Category", "التصنيف", "Catégorie"),
        "publish_btn" to Triple("Publish Anonymously", "نشر مجهول", "Publier anonymement"),
        "community_public_note" to Triple(
            "This post is public: every BreakFree user can read it. Only the alias above is attached to it.",
            "هذا المنشور عام: كل مستخدمي تعافي يمكنهم قراءته. الاسم المستعار أعلاه هو الهوية الوحيدة المرفقة.",
            "Ce message est public : tous les utilisateurs de BreakFree peuvent le lire. Seul le pseudo ci-dessus y est attaché."
        ),
        "community_signin_required" to Triple(
            "Sign in to take part — the community feed is public.",
            "سجّل الدخول للمشاركة — مجتمع تعافي عام.",
            "Connectez-vous pour participer — le fil communautaire est public."
        ),
        "community_offline" to Triple(
            "You need an internet connection to publish or react.",
            "تحتاج إلى اتصال بالإنترنت للنشر أو التفاعل.",
            "Vous avez besoin d'une connexion Internet pour publier ou réagir."
        ),
        "community_post_failed" to Triple(
            "Could not publish. Check your connection and try again.",
            "تعذّر النشر. تحقّق من الاتصال وحاول مجدداً.",
            "Publication impossible. Vérifiez votre connexion et réessayez."
        ),
        "community_post_published" to Triple("Published to the community.", "تم النشر في المجتمع.", "Publié dans la communauté."),
        "community_load_failed" to Triple(
            "Couldn't load the public feed. Check your connection and try again.",
            "تعذّر تحميل المجتمع. تحقّق من الاتصال وحاول مجدداً.",
            "Impossible de charger le fil public. Vérifiez votre connexion et réessayez."
        ),
        "community_error_timeout" to Triple(
            "(the server did not respond in time)",
            "(لم يستجب الخادم في الوقت المناسب)",
            "(le serveur n'a pas répondu à temps)"
        ),
        "community_error_permission" to Triple(
            "(this account is not allowed to post — sign out and back in)",
            "(هذا الحساب غير مسموح له بالنشر — سجّل الخروج ثم الدخول من جديد)",
            "(ce compte n'est pas autorisé à publier — déconnectez-vous et reconnectez-vous)"
        ),
        "community_error_network" to Triple(
            "(could not reach the server)",
            "(تعذّر الوصول إلى الخادم)",
            "(impossible de joindre le serveur)"
        ),
        "community_signin_title" to Triple("Join the shared feed", "انضم إلى المجتمع المشترك", "Rejoindre le fil partagé"),
        "community_signin_desc" to Triple(
            "Posts are public and shared by every user. Sign in with Google to read and post — nothing else about your recovery is shared.",
            "المنشورات عامة ويشاهدها كل المستخدمين. سجّل الدخول بحساب Google للقراءة والنشر — لا يُشارك أي شيء آخر عن رحلتك.",
            "Les messages sont publics et partagés par tous. Connectez-vous avec Google pour lire et publier — rien d'autre de votre récupération n'est partagé."
        ),
        "community_empty_title" to Triple("No stories yet", "لا توجد قصص بعد", "Pas encore d'histoires"),
        "community_empty_desc" to Triple(
            "This space is yours. Be the first to share a win, an urge you surfed, or a milestone — anonymously.",
            "هذه المساحة لك. كن أول من يشارك انتصاراً أو رغبة تجاوزتها أو إنجازاً — دون الكشف عن هويتك.",
            "Cet espace est le vôtre. Soyez le premier à partager une victoire, une envie surmontée ou une étape — anonymement."
        ),

        // Shield & Monitoring
        "shield_title" to Triple("Smart Shield & Protection", "درع الحماية والمراقبة الذكية", "Bouclier intelligent & protection"),
        "strict_guard_mode" to Triple("Strict Guard Mode", "نمط الحماية الصارم", "Mode protection stricte"),
        "strict_guard_desc" to Triple("Watch the address bar and visible text of browsers, and of the social apps you allow, on this device only", "فحص شريط العنوان والنص الظاهر في المتصفحات، وفي تطبيقات التواصل التي تسمح بها، على جهازك فقط", "Surveille la barre d'adresse et le texte visible des navigateurs et des applications sociales autorisées, sur cet appareil uniquement"),
        "guard_apps_consent_title" to Triple("Protect social apps too", "احمِ تطبيقات التواصل أيضاً", "Protéger aussi les apps sociales"),
        "guard_apps_consent_body" to Triple(
            "Extend your Shield to YouTube, X, Instagram, TikTok, Facebook and Reddit. It reads their visible text on this device only, so explicit titles, captions and posts get the same step-back reminder.",
            "وسّع درعك ليشمل يوتيوب وX وإنستغرام وتيك توك وفيسبوك وريديت. يقرأ نصها الظاهر على جهازك فقط، لتحصل العناوين والمنشورات الصريحة على نفس نافذة التراجع.",
            "Étendez votre bouclier à YouTube, X, Instagram, TikTok, Facebook et Reddit. Il lit leur texte visible sur cet appareil uniquement, pour que les titres, légendes et publications explicites reçoivent le même rappel de recul."
        ),
        "guard_apps_consent_note" to Triple(
            "Search boxes and suggestion lists are ignored, images and videos are not analysed, and nothing is stored or uploaded.",
            "تُتجاهل خانات البحث وقوائم الاقتراحات، ولا تُحلَّل الصور أو الفيديوهات، ولا يُخزَّن أي شيء أو يُرسَل.",
            "Les champs de recherche et listes de suggestions sont ignorés, les images et vidéos ne sont pas analysées, et rien n'est stocké ni envoyé."
        ),
        "guard_apps_consent_btn" to Triple("Enable for these apps", "تفعيل لهذه التطبيقات", "Activer pour ces apps"),
        "guard_apps_consent_later" to Triple("Not now", "ليس الآن", "Pas maintenant"),

        // DNS shield
        "dns_shield_section_title" to Triple("DNS Shield", "درع DNS", "Bouclier DNS"),
        "dns_shield_section_desc" to Triple(
            "Block adult domains before the page loads, in every app",
            "احجب المواقع الإباحية قبل تحميل الصفحة، في كل التطبيقات",
            "Bloque les domaines adultes avant le chargement de la page, dans toutes les apps"
        ),
        "dns_shield_title" to Triple("DNS Shield", "درع DNS", "Bouclier DNS"),
        "dns_shield_body" to Triple(
            "Blocks adult domains system-wide through Cloudflare for Families, before any app can load them. It works even when the content monitor is off.",
            "يحجب المواقع الإباحية على مستوى النظام عبر Cloudflare for Families، قبل أن يتمكن أي تطبيق من تحميلها. يعمل حتى عندما يكون درع المراقبة متوقفاً.",
            "Bloque les domaines adultes à l'échelle du système via Cloudflare for Families, avant que toute app puisse les charger. Fonctionne même si le moniteur de contenu est désactivé."
        ),
        "dns_shield_privacy" to Triple(
            "DNS lookups go to Cloudflare, not to BreakFree. Page content is never sent.",
            "تذهب استعلامات DNS إلى Cloudflare، وليس إلى تعافي. لا يُرسَل أي محتوى من الصفحات.",
            "Les requêtes DNS vont vers Cloudflare, jamais vers BreakFree. Le contenu des pages n'est jamais envoyé."
        ),
        "dns_shield_status_active" to Triple("Active", "مفعّل", "Actif"),
        "dns_shield_status_other" to Triple("Another resolver", "مزوّد آخر", "Un autre résolveur"),
        "dns_shield_status_off" to Triple("Off", "متوقف", "Désactivé"),
        "dns_shield_status_unsupported" to Triple("Unsupported", "غير مدعوم", "Non pris en charge"),
        "dns_shield_status_locked" to Triple("Locked", "مقفل", "Verrouillé"),
        "dns_shield_active_unlocked" to Triple(
            "Active. You can change it in Android Settings at any time.",
            "مفعّل. يمكنك تغييره من إعدادات أندرويد في أي وقت.",
            "Actif. Vous pouvez le modifier dans les réglages Android à tout moment."
        ),
        "dns_shield_mechanism_private" to Triple(
            "System Private DNS · enforced for every app",
            "DNS الخاص بالنظام · مطبّق على كل التطبيقات",
            "DNS privé système · appliqué à toutes les apps"
        ),
        "dns_shield_mechanism_filter" to Triple(
            "On-device filter · DNS queries only",
            "فلتر داخل الجهاز · استعلامات DNS فقط",
            "Filtre sur l'appareil · requêtes DNS uniquement"
        ),
        "dns_shield_turn_on" to Triple("Turn on", "تفعيل", "Activer"),
        "dns_filter_active" to Triple(
            "Active. Android shows the VPN key while the filter runs, and you can switch it off there.",
            "مفعّل. يُظهر أندرويد مفتاح VPN أثناء عمل الفلتر، ويمكنك إيقافه من هناك.",
            "Actif. Android affiche la clé VPN pendant le filtrage, et vous pouvez l'éteindre là-bas."
        ),
        "dns_filter_hint" to Triple(
            "Android will ask you to allow a VPN. BreakFree uses it for DNS queries only — no page, video or message traffic passes through it.",
            "سيطلب منك أندرويد السماح بـ VPN. يستخدمه تعافي لاستعلامات DNS فقط — لا يمر عبره أي محتوى صفحات أو فيديو أو رسائل.",
            "Android vous demandera d'autoriser un VPN. BreakFree l'utilise uniquement pour les requêtes DNS — aucun trafic de page, vidéo ou message ne le traverse."
        ),
        "dns_vpn_notification_channel" to Triple("DNS Shield", "درع DNS", "Bouclier DNS"),
        "dns_vpn_notification_channel_desc" to Triple(
            "Shown while the on-device DNS filter is protecting you",
            "يظهر أثناء عمل فلتر DNS داخل الجهاز",
            "Affiché pendant que le filtre DNS sur l'appareil vous protège"
        ),
        "dns_vpn_notification_title" to Triple("DNS Shield is on", "درع DNS مفعّل", "Le bouclier DNS est actif"),
        "dns_vpn_notification_text" to Triple(
            "Adult domains are blocked before they load. DNS queries only.",
            "تُحجب المواقع الإباحية قبل تحميلها. استعلامات DNS فقط.",
            "Les domaines adultes sont bloqués avant leur chargement. Requêtes DNS uniquement."
        ),
        "dns_vpn_notification_stop" to Triple("Turn off", "إيقاف", "Désactiver"),

        // Guard foreground service
        "guard_service_channel" to Triple("Protection", "الحماية", "Protection"),
        "guard_service_channel_desc" to Triple(
            "Shown while BreakFree's protection is running",
            "يظهر أثناء عمل حماية تعافي",
            "Affiché pendant que la protection de BreakFree fonctionne"
        ),
        "guard_service_title" to Triple("Protection is on", "الحماية مفعّلة", "La protection est active"),
        "guard_service_text" to Triple(
            "Monitoring and the DNS shield stay active in the background.",
            "رصد المحتوى ودرع DNS يبقيان نشطين في الخلفية.",
            "La surveillance et le bouclier DNS restent actifs en arrière-plan."
        ),
        "guard_service_text_dns" to Triple(
            "Monitoring and the DNS shield are active.",
            "رصد المحتوى ودرع DNS نشطان.",
            "La surveillance et le bouclier DNS sont actifs."
        ),

        // Stealth
        "stealth_lock_title" to Triple("Unlock BreakFree", "افتح تعافي", "Déverrouiller BreakFree"),
        "stealth_lock_subtitle" to Triple("Enter your PIN to continue", "أدخل رمز PIN للمتابعة", "Entrez votre code PIN pour continuer"),
        "stealth_lock_wrong" to Triple("Wrong PIN", "رمز PIN خاطئ", "Code PIN incorrect"),
        "stealth_lock_biometric" to Triple("Use biometric", "استخدام البصمة", "Utiliser la biométrie"),
        "settings_stealth" to Triple("Stealth Mode", "وضع التخفي", "Mode furtif"),
        "settings_stealth_desc" to Triple(
            "Lock the dashboard behind a PIN and keep notifications generic.",
            "قفل لوحة التحكم برمز PIN وإبقاء الإشعارات عامة.",
            "Verrouillez le tableau de bord derrière un code PIN et gardez des notifications génériques."
        ),
        "stealth_enable" to Triple("Enable Stealth Mode", "تفعيل وضع التخفي", "Activer le mode furtif"),
        "stealth_disable" to Triple("Disable Stealth Mode", "إيقاف وضع التخفي", "Désactiver le mode furtif"),
        "stealth_set_pin" to Triple("Set a 4-6 digit PIN", "تعيين رمز PIN من 4-6 أرقام", "Définir un code PIN de 4 à 6 chiffres"),
        "stealth_setup_body" to Triple(
            "Opening BreakFree will ask for this PIN.",
            "سيطلب التطبيق هذا الرمز عند فتحه.",
            "L'ouverture de BreakFree demandera ce code PIN."
        ),
        "stealth_pin_required" to Triple(
            "A PIN is required to protect the dashboard.",
            "رمز PIN مطلوب لحماية لوحة التحكم.",
            "Un code PIN est requis pour protéger le tableau de bord."
        ),
        "stealth_notif_channel" to Triple("Notifications", "إشعارات", "Notifications"),
        "stealth_notif_channel_desc" to Triple(
            "General app notifications",
            "إشعارات التطبيق العامة",
            "Notifications générales de l'application"
        ),
        "stealth_grace_title" to Triple("Reminder", "تذكير", "Rappel"),
        "stealth_grace_text" to Triple("Take a short break.", "خذ استراحة قصيرة.", "Faites une courte pause."),
        "stealth_grace_stays" to Triple(
            "This stays until the reminder ends.",
            "يبقى هذا حتى ينتهي التذكير.",
            "Cela reste jusqu'à la fin du rappel."
        ),
        "stealth_grace_not_recorded" to Triple(
            "Nothing has been recorded.",
            "لم يُسجَّل أي شيء.",
            "Rien n'a été enregistré."
        ),
        "stealth_service_title" to Triple("Service is running", "الخدمة تعمل", "Le service fonctionne"),
        "stealth_service_text" to Triple(
            "Running in the background.",
            "يعمل في الخلفية.",
            "Fonctionne en arrière-plan."
        ),
        "stealth_service_text_dns" to Triple(
            "Running in the background.",
            "يعمل في الخلفية.",
            "Fonctionne en arrière-plan."
        ),
        "stealth_vpn_session" to Triple("Private DNS", "DNS خاص", "DNS privé"),
        "stealth_auto_lock" to Triple("Auto-lock after", "القفل التلقائي بعد", "Verrouillage auto après"),
        "stealth_auto_lock_off" to Triple("Off", "معطّل", "Désactivé"),
        "stealth_auto_lock_1m" to Triple("1 min", "دقيقة", "1 min"),
        "stealth_auto_lock_5m" to Triple("5 min", "5 دقائق", "5 min"),
        "stealth_auto_lock_15m" to Triple("15 min", "15 دقيقة", "15 min"),
        "stealth_duress_title" to Triple("Duress PIN", "رمز الإكراه", "Code PIN de contrainte"),
        "stealth_duress_set" to Triple("Set", "مفعّل", "Défini"),
        "stealth_duress_not_set" to Triple("Not set", "غير مفعّل", "Non défini"),
        "stealth_duress_change" to Triple("Change", "تغيير", "Modifier"),
        "stealth_duress_body" to Triple(
            "A second PIN that opens a fake (empty) dashboard when you are coerced. Optionally wipes data if enabled below.",
            "رمز ثانٍ يفتح لوحة تحكم وهمية (فارغة) عند إجبارك. يمكنه مسح البيانات اختيارياً إذا فُعّل أدناه.",
            "Un second code PIN qui ouvre un tableau de bord factice (vide) en cas de contrainte. Peut effacer les données si activé ci-dessous."
        ),
        "stealth_wipe_on_duress" to Triple("Wipe data on duress", "مسح البيانات عند الإكراه", "Effacer les données sous contrainte"),
        "stealth_pin_mismatch" to Triple("PINs do not match.", "الرمزان غير متطابقين.", "Les codes PIN ne correspondent pas."),
        "stealth_duress_failed" to Triple("Failed to set PIN.", "تعذّر تعيين الرمز.", "Échec de la définition du code PIN."),
        "stealth_confirm_pin" to Triple("Confirm PIN", "تأكيد الرمز", "Confirmer le code PIN"),
        "stealth_disable_title" to Triple("Turn off Stealth Mode?", "إيقاف وضع التخفي؟", "Désactiver le mode furtif ?"),
        "stealth_disable_body" to Triple(
            "The real BreakFree icon and name will come back on your home screen. Enter your PIN to confirm.",
            "ستعود أيقونة تعافي واسمه الحقيقيان إلى الشاشة الرئيسية. أدخل رمز PIN للتأكيد.",
            "La vraie icône et le vrai nom de BreakFree reviendront sur votre écran d'accueil. Entrez votre code PIN pour confirmer."
        ),
        "stealth_disable_wrong_pin" to Triple("Wrong PIN", "رمز PIN خاطئ", "Code PIN incorrect"),
        "stealth_lock_biometric_title" to Triple("Unlock", "فتح", "Déverrouiller"),
        "stealth_lock_biometric_subtitle" to Triple(
            "Use your fingerprint or device PIN",
            "استخدم بصمتك أو رمز الجهاز",
            "Utilisez votre empreinte ou le code PIN de l'appareil"
        ),
        "stealth_duress_welcome" to Triple("Welcome", "أهلاً بك", "Bienvenue"),
        "stealth_duress_empty" to Triple(
            "No activity yet. Start your first check-in to begin.",
            "لا يوجد نشاط بعد. ابدأ أول تسجيل يومي لك.",
            "Aucune activité pour l'instant. Commencez votre premier bilan quotidien."
        ),

        // First-run Shield setup
        "setup_title" to Triple("Set up your Shield", "جهّز درعك", "Configurez votre bouclier"),
        "setup_intro" to Triple(
            "BreakFree can protect you in three ways. You can change any of them later in the Guard tab.",
            "يمكن لتعافي أن يحميك بثلاث طرق. يمكنك تغيير أي منها لاحقاً من تبويب الحماية.",
            "BreakFree peut vous protéger de trois façons. Vous pouvez modifier chacune plus tard dans l'onglet Protection."
        ),
        "setup_dns_title" to Triple("DNS Shield", "درع DNS", "Bouclier DNS"),
        "setup_dns_desc" to Triple(
            "Blocks adult domains in every app before the page loads. Lookups go to Cloudflare for Families, never to BreakFree.",
            "يحجب المواقع الإباحية في كل التطبيقات قبل تحميل الصفحة. تذهب الاستعلامات إلى Cloudflare for Families، وليس إلى تعافي.",
            "Bloque les domaines adultes dans toutes les apps avant le chargement de la page. Les requêtes vont vers Cloudflare for Families, jamais vers BreakFree."
        ),
        "setup_monitor_title" to Triple("Content monitor", "رصد المحتوى", "Moniteur de contenu"),
        "setup_monitor_desc" to Triple(
            "Warns you when explicit text appears in a browser or a supported app. Runs on-device; nothing is uploaded.",
            "ينبّهك عند ظهور نص صريح في متصفح أو تطبيق مدعوم. يعمل داخل الجهاز، ولا يُرفع أي شيء.",
            "Vous avertit lorsqu'un texte explicite apparaît dans un navigateur ou une app prise en charge. Fonctionne sur l'appareil ; rien n'est envoyé."
        ),
        "setup_admin_title" to Triple("Device admin", "مسؤول الجهاز", "Administrateur de l'appareil"),
        "setup_admin_desc" to Triple(
            "Optional. Lets the 90-day uninstall lock work. BreakFree declares no device policies, so it cannot lock or wipe the phone.",
            "اختياري. يتيح عمل قفل الإزالة لمدة 90 يوماً. لا يطلب تعافي أي صلاحيات إدارة، فلا يستطيع قفل الجهاز أو مسحه.",
            "Optionnel. Permet au verrou de désinstallation de 90 jours de fonctionner. BreakFree ne déclare aucune politique d'appareil, il ne peut donc ni verrouiller ni effacer le téléphone."
        ),
        "setup_enable" to Triple("Enable", "تفعيل", "Activer"),
        "setup_on" to Triple("On", "مفعّل", "Activé"),
        "setup_set_up" to Triple("Set up", "إعداد", "Configurer"),
        "setup_activate" to Triple("Activate", "تنشيط", "Activer"),
        "setup_start" to Triple("Start", "ابدأ", "Démarrer"),
        "setup_note" to Triple(
            "The 90-day uninstall lock is never enabled here — it stays a separate, explicit choice.",
            "قفل الإزالة لمدة 90 يوماً لا يُفعّل من هنا — يبقى خياراً منفصلاً وصريحاً.",
            "Le verrou de désinstallation de 90 jours n'est jamais activé ici — il reste un choix distinct et explicite."
        ),
        "setup_checklist_title" to Triple("Finish setting up your Shield", "أكمل إعداد درعك", "Terminez la configuration de votre bouclier"),
        "setup_checklist_desc" to Triple(
            "These steps unlock the protection. Each button opens the exact system screen.",
            "هذه الخطوات تفعّل الحماية. كل زر يفتح شاشة النظام المطلوبة.",
            "Ces étapes débloquent la protection. Chaque bouton ouvre l'écran système exact."
        ),
        "setup_identity_hint" to Triple(
            "In Android Settings this app appears as \"BreakFree\" (package com.aistudio.breakfree.recovr) — look for \"BreakFree\" in every system list.",
            "في إعدادات أندرويد يظهر هذا التطبيق باسم «BreakFree» (الحزمة com.aistudio.breakfree.recovr) — ابحث عن «BreakFree» في كل قائمة نظام.",
            "Dans les réglages Android, cette app apparaît sous « BreakFree » (paquet com.aistudio.breakfree.recovr) — cherchez « BreakFree » dans chaque liste système."
        ),
        "setup_step_restricted" to Triple(
            "Allow restricted settings (Android 13+)",
            "السماح بالإعدادات المقيّدة (أندرويد 13+)",
            "Autoriser les réglages restreints (Android 13+)"
        ),
        "setup_step_autostart" to Triple(
            "Enable Autostart (Xiaomi/MIUI and similar)",
            "تفعيل التشغيل التلقائي (Xiaomi/MIUI وما شابه)",
            "Activer le démarrage auto (Xiaomi/MIUI et similaires)"
        ),
        "setup_step_monitor" to Triple("Enable the content monitor", "تفعيل رصد المحتوى", "Activer le moniteur de contenu"),
        "setup_step_dns" to Triple("Enable the DNS shield", "تفعيل درع DNS", "Activer le bouclier DNS"),
        "setup_open_app_info" to Triple("Open app info", "فتح معلومات التطبيق", "Ouvrir les infos de l'app"),
        "setup_open_accessibility" to Triple("Open accessibility", "فتح إمكانية الوصول", "Ouvrir l'accessibilité"),
        "setup_grant_consent" to Triple("Grant consent", "منح الموافقة", "Accorder le consentement"),
        "setup_turn_on" to Triple("Turn on", "تفعيل", "Activer"),
        "dns_shield_other_host" to Triple(
            "Private DNS is currently set to {host}.",
            "DNS الخاص مضبوط حالياً على {host}.",
            "Le DNS privé est actuellement réglé sur {host}."
        ),
        "dns_shield_locked_until" to Triple(
            "Locked until {date}. You can change it when the commitment ends.",
            "مقفل حتى {date}. يمكنك تغييره عند انتهاء الالتزام.",
            "Verrouillé jusqu'au {date}. Vous pourrez le modifier à la fin de l'engagement."
        ),
        "dns_shield_do_hint" to Triple(
            "Device Owner: one tap applies Cloudflare for Families and locks it for the commitment.",
            "مالك الجهاز: نقرة واحدة تطبّق Cloudflare for Families وتقفله طوال الالتزام.",
            "Propriétaire de l'appareil : un tap applique Cloudflare for Families et le verrouille pour l'engagement."
        ),
        "dns_shield_manual_hint" to Triple(
            "Open Private DNS settings, choose “Private DNS provider hostname”, and enter: family.cloudflare-dns.com",
            "افتح إعدادات DNS الخاص، اختر «اسم مضيف مزوّد DNS الخاص»، وأدخل: family.cloudflare-dns.com",
            "Ouvrez les réglages DNS privé, choisissez « Nom d'hôte du fournisseur DNS privé », et saisissez : family.cloudflare-dns.com"
        ),
        "dns_shield_enable" to Triple("Enable DNS Shield", "تفعيل درع DNS", "Activer le bouclier DNS"),
        "dns_shield_enable_lock" to Triple("Enable & lock", "تفعيل وقفل", "Activer & verrouiller"),
        "dns_shield_open_settings" to Triple("Open Private DNS settings", "فتح إعدادات DNS الخاص", "Ouvrir les réglages DNS privé"),
        "dns_shield_copy_host" to Triple("Copy hostname", "نسخ اسم المضيف", "Copier le nom d'hôte"),
        "dns_shield_copied" to Triple("Hostname copied", "تم نسخ اسم المضيف", "Nom d'hôte copié"),
        "dns_shield_check" to Triple("Check again", "تحقق مرة أخرى", "Vérifier à nouveau"),
        "dns_shield_disable" to Triple("Turn off", "إيقاف", "Désactiver"),
        "dns_shield_busy" to Triple("Applying…", "جارٍ التطبيق…", "Application…"),
        "dns_shield_unsupported" to Triple(
            "The DNS Shield needs Android 9 or newer.",
            "يحتاج درع DNS إلى أندرويد 9 أو أحدث.",
            "Le bouclier DNS nécessite Android 9 ou plus récent."
        ),
        "dns_shield_apply_unsupported" to Triple(
            "This Android version cannot set Private DNS automatically. Use the manual steps below.",
            "لا يمكن لهذا الإصدار ضبط DNS الخاص تلقائياً. استخدم الخطوات اليدوية أدناه.",
            "Cette version d'Android ne peut pas régler le DNS privé automatiquement. Utilisez les étapes manuelles ci-dessous."
        ),
        "dns_shield_error_host" to Triple(
            "Cloudflare for Families could not be reached. Check your connection and try again.",
            "تعذّر الوصول إلى Cloudflare for Families. تحقق من اتصالك وحاول مرة أخرى.",
            "Impossible de joindre Cloudflare for Families. Vérifiez votre connexion et réessayez."
        ),
        "dns_shield_error_generic" to Triple(
            "Android could not apply the DNS Shield. Try the manual steps.",
            "تعذّر على أندرويد تطبيق درع DNS. جرّب الخطوات اليدوية.",
            "Android n'a pas pu appliquer le bouclier DNS. Essayez les étapes manuelles."
        ),
        "dns_shield_error_not_owner" to Triple(
            "Automatic setup needs Device Owner. Use the manual steps.",
            "الإعداد التلقائي يحتاج مالك الجهاز. استخدم الخطوات اليدوية.",
            "La configuration automatique nécessite le propriétaire de l'appareil. Utilisez les étapes manuelles."
        ),
        "dns_shield_consent_title" to Triple(
            "Before enabling the DNS Shield",
            "قبل تفعيل درع DNS",
            "Avant d'activer le bouclier DNS"
        ),
        "dns_shield_consent_body" to Triple(
            "The DNS Shield blocks adult domains before they load, in every app.\n\nTo do that, your DNS lookups are sent to Cloudflare for Families (family.cloudflare-dns.com). Cloudflare sees the domain names you look up; BreakFree never sees or stores them, and no page content is sent.\n\nAndroid may also ask you to allow a VPN. BreakFree uses it for DNS queries only — no page, video or message traffic passes through it.\n\nA VPN you install yourself, or a browser's own encrypted DNS, can bypass this. It is a strong speed bump, not a content filter.",
            "يحجب درع DNS المواقع الإباحية قبل تحميلها، في كل التطبيقات.\n\nولتحقيق ذلك، تُرسَل استعلامات DNS إلى Cloudflare for Families ‏(family.cloudflare-dns.com). ترى Cloudflare أسماء النطاقات التي تبحث عنها؛ أما تعافي فلا يراها ولا يخزّنها، ولا يُرسَل أي محتوى من الصفحات.\n\nقد يطلب منك أندرويد أيضاً السماح بـ VPN. يستخدمه تعافي لاستعلامات DNS فقط — لا يمر عبره أي محتوى صفحات أو فيديو أو رسائل.\n\nقد يتجاوزه VPN تثبّته بنفسك، أو DNS مشفّر خاص بالمتصفح. إنه حاجز قوي أمام الاندفاع، وليس فلتراً للمحتوى.",
            "Le bouclier DNS bloque les domaines adultes avant leur chargement, dans toutes les apps.\n\nPour cela, vos requêtes DNS sont envoyées à Cloudflare for Families (family.cloudflare-dns.com). Cloudflare voit les noms de domaines que vous recherchez ; BreakFree ne les voit ni ne les stocke jamais, et aucun contenu de page n'est envoyé.\n\nAndroid peut aussi vous demander d'autoriser un VPN. BreakFree l'utilise uniquement pour les requêtes DNS — aucun trafic de page, vidéo ou message ne le traverse.\n\nUn VPN que vous installez vous-même, ou le DNS chiffré propre à un navigateur, peut contourner cela. C'est un frein puissant, pas un filtre de contenu."
        ),
        "dns_shield_consent_accept" to Triple("I agree, enable", "أوافق، فعّل", "J'accepte, activer"),
        "dns_shield_consent_cancel" to Triple("Cancel", "إلغاء", "Annuler"),
        "grace_buffer_toggle" to Triple("120s Step-Back Window", "نافذة التراجع (120 ثانية)", "Fenêtre de recul de 120 s"),
        "grace_buffer_desc" to Triple("Two minutes to close the page. Nothing is recorded unless you confirm it later", "دقيقتان لإغلاق الصفحة. لا يُسجَّل أي شيء إلا إذا أكّدت ذلك لاحقاً", "Deux minutes pour fermer la page. Rien n'est enregistré sans votre confirmation"),
        "keyword_filter_toggle" to Triple("Text & Keyword Filter", "فلتر النصوص والكلمات", "Filtre texte & mots-clés"),
        "keyword_filter_desc" to Triple("Also match explicit keywords in page text, not just known domains", "مطابقة الكلمات الصريحة في نص الصفحة أيضاً، وليس المواقع المعروفة فقط", "Correspond aussi aux mots-clés explicites dans le texte de la page, pas seulement aux domaines connus"),
        "badges_view_all" to Triple("View All", "عرض الكل", "Tout voir"),
        "badges_hide" to Triple("Collapse", "طي", "Réduire"),
        "service_status" to Triple("Content Monitoring Service", "خدمة رصد المحتوى الصريح", "Service de surveillance du contenu"),
        "service_active" to Triple("Active & Guarding", "نشطة وتعمل بالحماية", "Actif & en protection"),
        "service_inactive" to Triple("Inactive - Tap to Enable", "غير مفعلة - اضغط للتمكين", "Inactif - appuyez pour activer"),
        "grace_rule_card_title" to Triple("The 2-Minute Grace Period Protocol", "بروتوكول فترة السماح (دقيقتان)", "Protocole de période de grâce de 2 minutes"),
        "grace_rule_card_desc" to Triple("Unlike harsh blockers that immediately shame and wipe progress on accidental or impulsive clicks, BreakFree grants you a 120-second conscious window. Step back within 2 minutes and your streak remains 100% intact.", "على عكس التطبيقات التقليدية التي تحبط المتعافي بالتصفير الفوري، تمنحك بريك فري نافذة وعي مدتها 120 ثانية. إذا تراجعت وأغلقت المحتوى، يبقى عدادك محفوظاً تقديراً لقوة إرادتك.", "Contrairement aux bloqueurs brutaux qui humilient et effacent la progression lors de clics accidentels ou impulsifs, BreakFree vous accorde une fenêtre consciente de 120 secondes. Reculez dans les 2 minutes et votre série reste intacte à 100 %."),
        "test_scanner_title" to Triple("Explicit Domain Scanner", "فاحص الروابط والمحفزات", "Scanner de domaines explicites"),
        "test_scanner_desc" to Triple("Monitors browser URLs, search queries and explicit keywords across Chrome, Firefox, system browsers and the supported social apps.", "يفحص عناوين المتصفحات وعبارات البحث الصريحة في كروم وفايرفوكس والمتصفحات الأخرى وتطبيقات التواصل المدعومة.", "Surveille les URL des navigateurs, les requêtes de recherche et les mots-clés explicites sur Chrome, Firefox, les navigateurs système et les apps sociales prises en charge."),
        "custom_keywords" to Triple("Active Triggers Count", "عدد الكلمات والمواقع المحجوبة", "Nombre de déclencheurs actifs"),
        "language_toggle" to Triple("Language / اللغة", "اللغة / Language", "Langue / Language"),

        // Daily Mood & Urge Tracking
        "daily_checkin_title" to Triple("Daily Mood & Urge Log", "تسجيل المشاعر والرغبات اليومي", "Journal quotidien de l'humeur et des envies"),
        "daily_checkin_subtitle" to Triple("Log your emotional state to train your AI counselor", "سجل حالتك النفسية لتدريب مرشدك الذكي وتقديم نصائح مخصصة", "Notez votre état émotionnel pour entraîner votre conseiller IA"),
        "log_today_btn" to Triple("Log Today's Reflection", "تسجيل مراجعة اليوم", "Enregistrer la réflexion du jour"),
        "edit_today_log" to Triple("Update Today's Log", "تعديل مراجعة اليوم", "Mettre à jour le bilan du jour"),
        "mood_question" to Triple("How did you feel emotionally today?", "كيف كانت حالتك المزاجية والنفسية اليوم؟", "Comment vous êtes-vous senti émotionnellement aujourd'hui ?"),
        "urge_question" to Triple("Peak urge intensity experienced today (0-10):", "أقصى شدة للرغبة واجهتها اليوم (0 إلى 10):", "Intensité maximale de l'envie aujourd'hui (0-10) :"),
        "emotions_label" to Triple("Primary Emotions Felt:", "المشاعر السائدة اليوم:", "Émotions principales ressenties :"),
        "triggers_label" to Triple("Triggers Encountered:", "المحفزات التي واجهتها:", "Déclencheurs rencontrés :"),
        "reflection_label" to Triple("Nightly Journal & Insights:", "تأملاتك وملاحظاتك لليوم:", "Journal du soir & aperçus :"),
        "reflection_placeholder" to Triple("What challenged you? What worked well today?", "ما الذي شكّل تحدياً لك؟ وما الذي ساعدك على الصمود؟", "Qu'est-ce qui vous a mis au défi ? Qu'est-ce qui a bien fonctionné aujourd'hui ?"),
        "streak_safe_toggle" to Triple("Streak Protected & Clean Today", "تمت حماية السلسلة بنجاح اليوم", "Série protégée & propre aujourd'hui"),
        "save_checkin_btn" to Triple("Save to Cloud & Update AI", "حفظ وتحديث سياق المرشد الذكي", "Enregistrer dans le cloud & mettre à jour l'IA"),
        "checkin_saved_success" to Triple("Daily reflection saved! Your AI counselor has updated its insights.", "تم حفظ تسجيلك اليومي بنجاح وتحديث سياق المرشد الذكي.", "Réflexion quotidienne enregistrée ! Votre conseiller IA a mis à jour ses aperçus."),
        "recent_logs_title" to Triple("Recent Emotional Trends", "سجل المشاعر والرغبات السابقة", "Tendances émotionnelles récentes"),
        "no_logs_yet" to Triple("No logs recorded yet. Complete your first evening reflection!", "لم تسجل أي مراجعة بعد. ابدأ تسجيل أول مراجعة مسائية اليوم!", "Aucun journal pour l'instant. Complétez votre première réflexion du soir !"),

        // Google Sign-In & Sync
        "sign_in_google" to Triple("Sign in with Google", "تسجيل الدخول عبر Google", "Se connecter avec Google"),
        "sign_in_title" to Triple("Cloud Sync & Recovery Identity", "المزامنة السحابية وهوية التعافي", "Synchro cloud & identité de récupération"),
        "sign_in_desc" to Triple("Sign in with Google to protect your streak history and daily mood logs across devices.", "سجل الدخول باستخدام Google لحفظ تاريخ تعافيك وسجلات مشاعرك بأمان على السحابة.", "Connectez-vous avec Google pour protéger l'historique de votre série et vos journaux d'humeur sur tous vos appareils."),
        "sign_out" to Triple("Sign Out", "تسجيل الخروج", "Se déconnecter"),
        "signed_in_as" to Triple("Signed in as", "مسجل الدخول باسم", "Connecté en tant que"),
        "cloud_synced" to Triple("Cloud Synced", "متزامن سحابياً", "Synchronisé dans le cloud"),

        // Settings
        "tab_settings" to Triple("Settings", "الإعدادات", "Paramètres"),
        "settings_title" to Triple("Settings", "الإعدادات", "Paramètres"),
        "settings_subtitle" to Triple("Make BreakFree fit you", "اجعل تعافي يناسبك", "Adaptez BreakFree à vous"),
        "settings_account" to Triple("Account & Sync", "الحساب والمزامنة", "Compte & synchro"),
        "settings_appearance" to Triple("Appearance", "المظهر", "Apparence"),
        "settings_appearance_desc" to Triple("Language and interface direction", "اللغة واتجاه الواجهة", "Langue et direction de l'interface"),
        "settings_theme" to Triple("Theme", "المظهر", "Thème"),
        "settings_theme_desc" to Triple("Dark, light, or follow the system", "داكن، فاتح، أو حسب النظام", "Sombre, clair ou selon le système"),
        "theme_dark" to Triple("Dark", "داكن", "Sombre"),
        "theme_light" to Triple("Light", "فاتح", "Clair"),
        "theme_system" to Triple("System", "نظام", "Système"),
        "update_available_title" to Triple("Update available", "تحديث متاح", "Mise à jour disponible"),
        "update_available_body" to Triple(
            "Version {version} is available. Download and install it now?",
            "الإصدار {version} متاح. هل ترغب في تنزيله وتثبيته الآن؟",
            "La version {version} est disponible. Télécharger et installer maintenant ?"
        ),
        "update_download_install" to Triple("Download & install", "تنزيل وتثبيت", "Télécharger et installer"),
        "update_later" to Triple("Later", "لاحقاً", "Plus tard"),
        "settings_updates" to Triple("Updates", "التحديثات", "Mises à jour"),
        "settings_current_version" to Triple("Current version", "الإصدار الحالي", "Version actuelle"),
        "settings_check_updates" to Triple("Check for updates", "البحث عن تحديثات", "Rechercher des mises à jour"),
        "settings_checking_updates" to Triple("Checking…", "جارٍ البحث…", "Vérification…"),
        "settings_up_to_date" to Triple("You're up to date", "أنت على آخر إصدار", "Vous êtes à jour"),
        "update_error_install_permission" to Triple(
            "Grant \"Install unknown apps\" for BreakFree, then try again.",
            "امنح «تثبيت التطبيقات غير المعروفة» لتعافي، ثم أعد المحاولة.",
            "Accordez « Installer des applications inconnues » à BreakFree, puis réessayez."
        ),
        "update_error_download" to Triple(
            "Download failed. Check your connection and try again.",
            "فشل التنزيل. تحقّق من اتصالك ثم أعد المحاولة.",
            "Échec du téléchargement. Vérifiez votre connexion et réessayez."
        ),
        "update_error_rate_limit" to Triple(
            "GitHub rate limit reached. Try again later.",
            "تم بلوغ حدّ طلبات GitHub. أعد المحاولة لاحقاً.",
            "Limite de requêtes GitHub atteinte. Réessayez plus tard."
        ),
        "update_error_no_release" to Triple(
            "No release found. Publish a release on GitHub first.",
            "لم يُعثر على إصدار منشور. انشر إصداراً على GitHub أولاً.",
            "Aucune version trouvée. Publiez d'abord une version sur GitHub."
        ),
        "update_error_server" to Triple(
            "Server returned HTTP {code}",
            "أعاد الخادم رمز HTTP {code}",
            "Le serveur a renvoyé HTTP {code}"
        ),
        "update_error_parse" to Triple(
            "Could not parse the update response.",
            "تعذّرت قراءة استجابة التحديث.",
            "Impossible d'analyser la réponse de mise à jour."
        ),
        "update_error_network" to Triple(
            "Network error. Check your connection and try again.",
            "خطأ في الشبكة. تحقّق من اتصالك ثم أعد المحاولة.",
            "Erreur réseau. Vérifiez votre connexion et réessayez."
        ),
        "settings_auto_check_updates" to Triple("Check automatically", "بحث تلقائي", "Vérifier automatiquement"),
        "settings_language_desc" to Triple("Choose the language of the app", "اختر لغة التطبيق", "Choisissez la langue de l'application"),
        "settings_protection" to Triple("Protection", "الحماية", "Protection"),
        "settings_protection_desc" to Triple("Shield status, filters and step-back window", "حالة الدرع والفلاتر ونافذة التراجع", "État du bouclier, filtres et fenêtre de recul"),
        "settings_protection_active" to Triple("Shield is on", "الدرع مفعّل", "Le bouclier est actif"),
        "settings_protection_inactive" to Triple("Shield is off", "الدرع متوقف", "Le bouclier est désactivé"),
        "settings_notifications" to Triple("Notifications", "الإشعارات", "Notifications"),
        "settings_notifications_desc" to Triple("Step-back alerts and daily reminders", "تنبيهات التراجع والتذكيرات اليومية", "Alertes de recul et rappels quotidiens"),
        "settings_notifications_ok" to Triple("Allowed", "مسموحة", "Autorisées"),
        "settings_notifications_off" to Triple("Blocked in Android settings", "محظورة من إعدادات أندرويد", "Bloquées dans les réglages Android"),
        "settings_open_system" to Triple("Open", "فتح", "Ouvrir"),
        "settings_overlay" to Triple("Show reminders over other apps", "إظهار التنبيهات فوق التطبيقات الأخرى", "Afficher les rappels par-dessus les autres apps"),
        "settings_overlay_ok" to Triple("Allowed — appears above the app", "مسموح — يظهر فوق التطبيق", "Autorisé — apparaît au-dessus de l'app"),
        "settings_overlay_off" to Triple("Off — reminders arrive as notifications", "متوقف — تصل التنبيهات كإشعارات", "Désactivé — les rappels arrivent en notifications"),
        "overlay_permission_title" to Triple("Show the reminder over the app", "إظهار التنبيه فوق التطبيق", "Afficher le rappel par-dessus l'app"),
        "overlay_permission_body" to Triple(
            "Android needs one more permission for the step-back reminder to appear on top of the app. Without it, the reminder still arrives as a notification.\n\nBreakFree only draws this one card, only while the countdown is running, and it disappears as soon as you leave the page.",
            "يحتاج أندرويد إلى إذن إضافي ليظهر تنبيه التراجع فوق التطبيق. بدون هذا الإذن سيصل التنبيه كإشعار عادي.\n\nلا يرسم تعافي سوى هذه البطاقة، وفقط أثناء العد التنازلي، وتختفي بمجرد مغادرتك الصفحة.",
            "Android a besoin d'une permission supplémentaire pour que le rappel de recul apparaisse au-dessus de l'app. Sans elle, le rappel arrive quand même en notification.\n\nBreakFree ne dessine que cette carte, uniquement pendant le décompte, et elle disparaît dès que vous quittez la page."
        ),
        "overlay_permission_continue" to Triple("Continue", "متابعة", "Continuer"),

        // 90-day uninstall commitment
        "settings_uninstall_lock" to Triple("Strict Uninstall Lock (90 days)", "قفل الحذف الصارم (90 يوماً)", "Verrou de désinstallation strict (90 jours)"),
        "settings_lock_off" to Triple("Off — tap to activate 90-day lock", "متوقف — اضغط لتفعيل قفل 90 يوماً", "Désactivé — appuyez pour activer le verrou de 90 jours"),
        "settings_lock_until" to Triple("Locked until %date% · %days% days left", "مقفل حتى %date% · يتبقى %days% يوماً", "Verrouillé jusqu'au %date% · %days% jours restants"),
        "settings_lock_done" to Triple("Commitment complete — lock released", "اكتمل الالتزام — تم رفع القفل", "Engagement terminé — verrou levé"),
        "uninstall_lock_title" to Triple("Strict 90-Day Recovery Lock", "قفل التعافي الصارم لـ 90 يوماً", "Verrou de récupération strict de 90 jours"),
        "uninstall_lock_body" to Triple(
            "This makes BreakFree an active Device Administrator. Android will then refuse to uninstall it: the only way out is to deactivate it in system Settings, which turns a one-tap uninstall into a deliberate trip through menus.\n\nBreakFree asks for no control over your phone — it cannot lock the screen, wipe data or set passwords. The administrator releases itself automatically when the 90 days end.\n\nAre you ready to commit for 90 days?",
            "يفعّل هذا الخيار تطبيق تعافي كمدير نشط للجهاز. عندها سيرفض أندرويد إزالته: الطريق الوحيد للخروج هو إلغاء التنشيط من إعدادات النظام، وهذا يحوّل الحذف بضغطة واحدة إلى رحلة مقصودة عبر القوائم.\n\nلا يطلب تعافي أي تحكم في هاتفك — لا يقفل الشاشة ولا يمحو البيانات ولا يضع كلمات مرور. وتُلغى صلاحية الإدارة تلقائياً عند انتهاء التسعين يوماً.\n\nهل أنت مستعد للالتزام التام لـ 90 يوماً؟",
            "Cela fait de BreakFree un administrateur actif de l'appareil. Android refusera alors de le désinstaller : la seule sortie est de le désactiver dans les réglages système, ce qui transforme une désinstallation en un clic en un parcours délibéré dans les menus.\n\nBreakFree ne demande aucun contrôle sur votre téléphone — il ne peut ni verrouiller l'écran, ni effacer les données, ni définir des mots de passe. L'administrateur se libère automatiquement à la fin des 90 jours.\n\nÊtes-vous prêt à vous engager pour 90 jours ?"
        ),
        "uninstall_lock_confirm" to Triple("I commit for 90 days", "أنا ملتزم لـ 90 يوماً", "Je m'engage pour 90 jours"),
        "uninstall_lock_later" to Triple("Not now", "ليس الآن", "Pas maintenant"),
        "uninstall_lock_active_title" to Triple("90-Day Commitment Active", "قفل الالتزام لـ 90 يوماً مفعّل", "Engagement de 90 jours actif"),
        "uninstall_lock_active_body" to Triple(
            "The strict lock is active. Protection cannot be turned off until it ends.",
            "قفل صارم مفعّل. لا يمكن تعطيل الحماية حتى انتهائه.",
            "Le verrou strict est actif. La protection ne peut pas être désactivée avant la fin."
        ),

        // Daily Mood
        // (already covered)

        // Settings (cont.)
        "settings_theme" to Triple("Theme", "المظهر", "Thème"),

        // Missing advanced entries (recovered from clone, translated to French)
        "ai_offline_note" to Triple("Offline guidance — not personalised", "إرشاد عام — ليس رداً مخصصاً", "Conseils hors ligne — non personnalisés"),
        "ai_thinking" to Triple("Composing guidance…", "المرشد يجهّز نصيحة…", "Rédaction des conseils…"),
        "badges_locked" to Triple("Locked", "مقفل", "Verrouillé"),
        "badges_progress" to Triple("unlocked", "مُحقق", "débloqué"),
        "badges_unlocked_label" to Triple("Unlocked", "مُحقق", "Débloqué"),
        "breathing_sos_chip" to Triple("4-7-8 Breathing SOS", "تنفس 4-7-8 العاجل", "SOS respiration 4-7-8"),
        "buffer_label" to Triple("120s buffer", "نافذة 120 ثانية", "Tampon de 120 s"),
        "check_in_short" to Triple("Check in", "تسجيل", "Bilan"),
        "community_brotherhood" to Triple("Anonymous support", "دعم مجهول", "Soutien anonyme"),
        "community_clean" to Triple("clean", "نقية", "propre"),
        "community_post_btn" to Triple("Share", "شارك", "Partager"),
        "do_close" to Triple("Close", "إغلاق", "Fermer"),
        "do_copied" to Triple("Copied", "تم النسخ", "Copié"),
        "do_copy" to Triple("Copy command", "نسخ الأمر", "Copier la commande"),
        "do_owner_active_body" to Triple("Android's uninstall button is disabled. The lock releases on %date%.", "زر الحذف في أندرويد معطّل. يُرفع القفل في %date%.", "Le bouton de désinstallation d'Android est désactivé. Le verrou se lève le %date%."),
        "do_owner_active_title" to Triple("Device Owner is active", "وضع مالك الجهاز مفعّل", "Le propriétaire de l'appareil est actif"),
        "do_pc_note" to Triple("The output must say Success. Reopen BreakFree afterwards: the 90-day clock starts and the uninstall button is disabled.", "يجب أن تكون النتيجة Success. أعد فتح تعافي بعدها: تبدأ الـ 90 يوماً ويُعطّل زر الحذف.", "Le résultat doit indiquer Success. Rouvrez BreakFree ensuite : l'horloge de 90 jours démarre et le bouton de désinstallation est désactivé."),
        "do_pc_title" to Triple("Option 1 — From a computer", "الخيار 1 — من الكمبيوتر", "Option 1 — Depuis un ordinateur"),
        "do_qr_checksum" to Triple("APK SHA-256 (must match the file at the link):\n%hash%", "بصمة APK SHA-256 (يجب أن تطابق الملف على الرابط):\n%hash%", "SHA-256 de l'APK (doit correspondre au fichier du lien) :\n%hash%"),
        "do_qr_failed" to Triple("Could not read this APK's checksum.", "تعذّرت قراءة بصمة هذا الملف.", "Impossible de lire la somme de contrôle de cet APK."),
        "do_qr_generate" to Triple("Generate QR", "إنشاء الرمز", "Générer le QR"),
        "do_qr_invalid" to Triple("Enter a public https:// link to the APK.", "أدخل رابط https:// عاماً لملف APK.", "Saisissez un lien https:// public vers l'APK."),
        "do_qr_link_label" to Triple("Public HTTPS link to this exact APK", "رابط HTTPS عام لهذا الملف نفسه", "Lien HTTPS public vers cet APK exact"),
        "do_qr_no_account" to Triple("Do not sign in to a Google account during setup — Android refuses Device Owner when an account exists.", "لا تسجّل الدخول إلى حساب جوجل أثناء الإعداد — يرفض أندرويد وضع مالك الجهاز عند وجود حساب.", "Ne vous connectez pas à un compte Google pendant la configuration — Android refuse le propriétaire de l'appareil si un compte existe."),
        "do_qr_note" to Triple("The QR is built from that link and the SHA-256 of the BreakFree installed on this phone. Host that exact file, or provisioning will fail.", "يُبنى الرمز من الرابط وبصمة SHA-256 لنسخة تعافي المثبتة على هذا الهاتف. استضف هذا الملف نفسه وإلا سيفشل الإعداد.", "Le QR est construit à partir de ce lien et du SHA-256 de BreakFree installé sur ce téléphone. Hébergez ce fichier exact, sinon le provisionnement échouera."),
        "do_qr_title" to Triple("Option 2 — QR setup, no computer", "الخيار 2 — إعداد عبر رمز QR بدون كمبيوتر", "Option 2 — Configuration par QR, sans ordinateur"),
        "do_qr_warning" to Triple("This option factory-resets the phone: everything on it is erased. Back up anything you need first.", "هذا الخيار يعيد ضبط الهاتف من المصنع: تُمحى كل بياناته. انسخ ما تحتاجه أولاً.", "Cette option réinitialise le téléphone aux paramètres d'usine : tout est effacé. Sauvegardez d'abord ce dont vous avez besoin."),
        "do_setup_intro" to Triple(
            "This is optional and stronger than the 90-day administrator lock. It needs one extra setup step, because Android only lets an app block its own uninstall when it is the phone's Device Owner.\n\nAfter setup the 90 days start when BreakFree first opens, and the lock releases itself when they end.",
            "هذا الخيار اختياري وأقوى من قفل المدير لمدة 90 يوماً. يحتاج إلى خطوة إعداد إضافية واحدة، لأن أندرويد لا يسمح لتطبيق بتعطيل حذفه إلا إذا كان مالك الجهاز.\n\nبعد الإعداد تبدأ الـ 90 يوماً عند أول فتح لتطبيق تعافي، ويُرفع القفل تلقائياً عند انتهائها.",
            "C'est optionnel et plus fort que le verrou d'administrateur de 90 jours. Cela nécessite une étape de configuration supplémentaire, car Android ne permet à une app de bloquer sa propre désinstallation que si elle est propriétaire de l'appareil.\n\nAprès configuration, les 90 jours démarrent à la première ouverture de BreakFree et le verrou se lève à la fin."
        ),
        "do_setup_open" to Triple("Maximum protection (Device Owner)", "أقصى حماية (مالك الجهاز)", "Protection maximale (propriétaire de l'appareil)"),
        "do_setup_subtitle" to Triple("Make Android itself disable the uninstall button", "اجعل أندرويد نفسه يعطّل زر حذف التطبيق", "Faites désactiver le bouton de désinstallation par Android lui-même"),
        "do_setup_title" to Triple("Maximum protection", "أقصى درجات الحماية", "Protection maximale"),
        "guard_overlay_hint_body" to Triple(
            "Allow “display over other apps” so the step-back reminder appears above the app instead of only in the notification shade.",
            "اسمح بـ«العرض فوق التطبيقات الأخرى» ليظهر تنبيه التراجع فوق التطبيق بدلاً من شريط الإشعارات فقط.",
            "Autorisez « affichage par-dessus les autres apps » pour que le rappel de recul apparaisse au-dessus de l'app plutôt que seulement dans la barre de notifications."
        ),
        "guard_overlay_hint_btn" to Triple("Allow overlay", "السماح بالعرض فوق التطبيقات", "Autoriser la superposition"),
        "guard_overlay_hint_title" to Triple("Reminders are notifications only", "التنبيهات تظهر كإشعارات فقط", "Les rappels sont des notifications uniquement"),
        "guard_subtitle" to Triple("Protection that runs on your device", "حماية تعمل على جهازك", "Protection qui tourne sur votre appareil"),
        "home_greeting" to Triple("Welcome back", "أهلاً بعودتك", "Bon retour"),
        "home_greeting_anon" to Triple("Your journey, quietly protected", "تعافيك محفوظ بهدوء", "Votre parcours, protégé en silence"),
        "protection_controls" to Triple("Protection controls", "إعدادات الحماية", "Contrôles de protection"),
        "quick_actions" to Triple("Quick actions", "إجراءات سريعة", "Actions rapides"),
        "quick_stats" to Triple("At a glance", "نظرة سريعة", "En un coup d'œil"),
        "scanner_rules" to Triple("rules loaded", "قاعدة محمّلة", "règles chargées"),
        "scanner_title" to Triple("Detection rules", "قواعد الرصد", "Règles de détection"),
        "settings_about" to Triple("About", "حول التطبيق", "À propos"),
        "settings_about_desc" to Triple("Version, privacy and support", "الإصدار والخصوصية والدعم", "Version, confidentialité et support"),
        "settings_crisis" to Triple("Need urgent help?", "تحتاج مساعدة عاجلة؟", "Besoin d'aide urgente ?"),
        "settings_crisis_desc" to Triple("If you are in immediate danger, contact your local emergency number. BreakFree is a support tool, not a crisis service.", "إذا كنت في خطر مباشر، اتصل برقم الطوارئ في بلدك. تعافي أداة دعم وليس خدمة طوارئ.", "En cas de danger immédiat, contactez le numéro d'urgence local. BreakFree est un outil de soutien, pas un service de crise."),
        "settings_data" to Triple("Recovery data", "بيانات التعافي", "Données de récupération"),
        "settings_data_desc" to Triple("Start date, streak reset and local history", "تاريخ البداية وتصفير العداد والسجل المحلي", "Date de début, réinitialisation de série et historique local"),
        "settings_footer" to Triple("Made for the quiet battles.", "صُنع للمعارك الهادئة.", "Fait pour les batailles silencieuses."),
        "settings_guard_shortcut" to Triple("Open the Shield", "افتح درع الحماية", "Ouvrir le bouclier"),
        "settings_privacy" to Triple("Privacy", "الخصوصية", "Confidentialité"),
        "settings_privacy_desc" to Triple("Mood logs and streak history stay on this device. Chat text is only sent to the AI provider you configure, after you agree, and the Shield never uploads the pages it reads.", "سجلات المشاعر وتاريخ التعافي تبقى على هذا الجهاز. لا تُرسل رسائل المحادثة إلا إلى مزوّد الذكاء الاصطناعي الذي تحدده، وبعد موافقتك، والدرع لا يرفع الصفحات التي يقرأها أبداً.", "Les journaux d'humeur et l'historique de série restent sur cet appareil. Le texte du chat n'est envoyé qu'au fournisseur IA que vous configurez, après votre accord, et le bouclier n'envoie jamais les pages qu'il lit."),
        "settings_version" to Triple("Version", "الإصدار", "Version"),
        "shield_manage" to Triple("Manage access", "إدارة الوصول", "Gérer l'accès"),
        "shield_open_settings" to Triple("Open Accessibility Settings", "إعدادات إمكانية الوصول", "Ouvrir les réglages d'accessibilité"),
        "shield_running" to Triple("The Shield is running", "الدرع يعمل الآن", "Le bouclier fonctionne"),
        "shield_stopped" to Triple("The Shield is not running", "الدرع متوقف", "Le bouclier ne fonctionne pas"),
        "simulate_grace_desc" to Triple("Preview the 120-second step-back window (debug builds only)", "معاينة نافذة الـ 120 ثانية للتراجع الواعي (إصدار تجريبي فقط)", "Aperçu de la fenêtre de recul de 120 s (versions debug uniquement)"),
        "stat_today" to Triple("Today", "اليوم", "Aujourd'hui"),
        "sync_off" to Triple("Not synced", "غير متزامن", "Non synchronisé"),
        "tamper_gate_body_nopin" to Triple("Your 90-day commitment is active ({days} days left). This switch is what protects you from relapse.", "قفل 90 يوماً مفعّل ({days} يوماً متبقية). هذا الخيار ما يحميك من الانتكاس.", "Votre engagement de 90 jours est actif ({days} jours restants). Ce commutateur est ce qui vous protège de la rechute."),
        "tamper_gate_body_pin" to Triple("Your 90-day commitment is active ({days} days left). Enter your Stealth PIN to turn this off.", "قفل 90 يوماً مفعّل ({days} يوماً متبقية). أدخل رمز PIN للتعطيل.", "Votre engagement de 90 jours est actif ({days} jours restants). Saisissez votre code PIN furtif pour désactiver."),
        "tamper_gate_pin_label" to Triple("Stealth PIN", "رمز PIN", "Code PIN furtif"),
        "tamper_gate_confirm_pin" to Triple("Unlock", "فتح", "Déverrouiller"),
        "tamper_gate_title" to Triple("Protection is locked", "الحماية مقفلة", "La protection est verrouillée"),
        "tamper_gate_turn_off" to Triple("I accept - turn off", "أفهم ذلك - تعطيله", "J'accepte - désactiver"),
        "uninstall_lock_stay_strong" to Triple("I will stay strong", "سأبقى ملتزماً وأصمد", "Je resterai fort"),

        // The Pause: ten minutes between the urge and the relapse.
        "pause_gate_title" to Triple("The Pause", "المهلة", "La Pause"),
        "pause_gate_subtitle" to Triple("Ten minutes between the urge and the relapse.", "عشر دقائق بين الرغبة والانتكاس.", "Dix minutes entre l'envie et la rechute."),
        "pause_gate_time_left" to Triple("minutes left", "دقيقة متبقية", "minutes restantes"),
        "pause_gate_done_short" to Triple("time is up", "انتهى الوقت", "temps écoulé"),
        "pause_gate_note" to Triple("No decision needed for the next few minutes. Focus on the data.", "لا حاجة لاتخاذ قرار في الدقائق القادمة. ركّز على البيانات.", "Aucune décision à prendre dans les prochaines minutes. Concentrez-vous sur les données."),
        "pause_gate_done" to Triple("The ten minutes are over. Whatever you choose, be honest with yourself.", "انتهت الدقائق العشر. مهما اخترت، كن صادقاً مع نفسك.", "Les dix minutes sont écoulées. Quoi que vous choisissiez, soyez honnête avec vous-même."),
        "pause_gate_preparing" to Triple("Personalising your reminders…", "جارٍ تخصيص تذكيراتك…", "Personnalisation de vos rappels…"),
        "pause_gate_days" to Triple("days clean", "يوماً نظيفاً", "jours sans rechute"),
        "pause_gate_best" to Triple("best streak", "أفضل فترة", "record"),
        "pause_gate_survived" to Triple("urges surfed", "رغبات تجاوزتها", "envies surmontées"),
        "pause_gate_promise_label" to Triple("My promise", "وعدي", "Ma promesse"),
        "pause_gate_promise_hint" to Triple("One sentence you want to read in this moment.", "جملة واحدة تريد قراءتها في هذه اللحظة.", "Une phrase que vous voulez lire à ce moment-là."),
        "pause_gate_promise_save" to Triple("Save", "حفظ", "Enregistrer"),
        "pause_gate_edit" to Triple("Edit", "تعديل", "Modifier"),
        "pause_gate_im_ok" to Triple("I'm okay now", "أنا بخير الآن", "Je vais bien maintenant"),
        "pause_gate_still_want" to Triple("I still want to", "ما زلت أريد", "J'en ai toujours envie"),
        "pause_gate_footer" to Triple("No one is watching. This is only for you.", "لا أحد يراقب. هذا لك وحدك.", "Personne ne regarde. C'est seulement pour vous."),
        "pause_gate_survived_toast" to Triple("Logged.", "تم التسجيل.", "Enregistré."),
        "pause_promise_saved" to Triple("Promise saved", "تم حفظ الوعد", "Promesse enregistrée"),
        "pause_notif_title" to Triple("BreakFree", "BreakFree", "BreakFree"),
        "pause_notif_channel" to Triple("Pause reminders", "تذكيرات المهلة", "Rappels de la pause"),
        "pause_notif_channel_desc" to Triple("Gentle reminders during a ten-minute pause.", "تذكيرات لطيفة خلال مهلة العشر دقائق.", "Rappels doux pendant une pause de dix minutes."),
        "sos_pause_btn" to Triple("Give me 10 minutes", "أعطني 10 دقائق", "Donnez-moi 10 minutes"),
        "urges_surfed" to Triple("Urges surfed", "رغبات تجاوزتها", "Envies surmontées"),
        "rewire_title" to Triple("Rewire 30", "إعادة برمجة 30", "Rewire 30"),
        "rewire_prev" to Triple("Prev", "السابق", "Préc."),
        "rewire_next" to Triple("Next", "التالي", "Suiv."),
        "rewire_mark_done" to Triple("Mark done", "تم", "Terminé"),
        "rewire_done" to Triple("Done", "مكتمل", "Fait"),
        "rewire_complete" to Triple("All 30 lessons complete.", "اكتملت جميع الدروس الـ 30.", "Les 30 leçons sont terminées."),
        "weekly_insight_title" to Triple("Weekly insight", "رؤى أسبوعية", "Aperçu hebdomadaire"),
        "weekly_insight_dismiss" to Triple("Dismiss", "إغلاق", "Fermer"),
        "weekly_insight_summary" to Triple("This week: {days} clean days, {urges} urges survived, {checkins} check-ins logged.", "هذا الأسبوع: {days} يوماً نظيفاً، {urges} رغبة متجاوزة، {checkins} تسجيل.", "Cette semaine : {days} jours propres, {urges} envies surmontées, {checkins} bilans."),
        "weekly_insight_good" to Triple("Consistent week. Keep the routine.", "أسبوع ثابت. حافظ على الروتين.", "Semaine régulière. Gardez la routine."),
        "weekly_insight_keep" to Triple("Every day counts. Keep showing up.", "كل يوم مهم. استمر في الحضور.", "Chaque jour compte. Continuez à être présent."),
    )
}
