package com.aistudio.breakfree.recovr.monitoring

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.aistudio.breakfree.recovr.R
import com.aistudio.breakfree.recovr.localization.Translations
import com.aistudio.breakfree.recovr.model.AppLanguage
import com.aistudio.breakfree.recovr.model.GracePeriodState
import kotlin.math.roundToInt

/**
 * The reminder that is drawn *over* the browser window.
 *
 * Deliberately has **no buttons**. An earlier version offered "I stepped back", which
 * looked like a healthy action but was really an escape hatch: one tap cleared the
 * reminder while the page stayed open, which is exactly the moment the reminder exists
 * for. The card now disappears only when the underlying state resolves — the user
 * navigates away, closes the tab, leaves the browser, or the window expires.
 *
 * This is a plain-View card rather than Compose: it is hosted by an
 * [android.accessibilityservice.AccessibilityService] window, which has no lifecycle
 * owner, so a `ComposeView` here would need a hand-rolled lifecycle and can crash when
 * the service dies mid-composition. Plain views also mean the card cannot re-inflate
 * while the user is mid-page.
 *
 * Design mirrors [com.aistudio.breakfree.recovr.ui.components.GracePeriodOverlay]:
 * same "Quiet Night" palette, same order of information. The matched keyword is
 * deliberately never shown here — an overlay sits on top of a browser that may be
 * screen-recorded or looked at over a shoulder, so it states that the page matched the
 * filter without repeating what was matched.
 */
// Built in code, never inflated from XML, so the (Context, AttributeSet) constructor
// lint expects does not exist by design.
@SuppressLint("ClickableViewAccessibility", "ViewConstructor")
class GraceReminderOverlayView(context: Context) : LinearLayout(context) {

    private val titleView: TextView
    private val countdownView: TextView
    private val bodyView: TextView
    private val staysView: TextView
    private val progressBar: CountdownBar

    private var renderedSeconds = -1
    private var renderedLanguage: AppLanguage? = null

    init {
        orientation = VERTICAL
        // Transparent gutter: touches that land here fall through to the browser, so the
        // page stays usable while the reminder is on screen.
        setPadding(dp(14f), dp(10f), dp(14f), dp(8f))

        val card = LinearLayout(context).apply {
            orientation = VERTICAL
            setPadding(dp(16f), dp(14f), dp(16f), dp(16f))
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(WARM_TINT, CARD)
            ).apply {
                cornerRadius = dpf(24f)
                setStroke(dp(1.5f).coerceAtLeast(1), BORDER)
            }
            elevation = dpf(10f)
            // Consume taps on the card's own empty space so a stray tap does not reach the
            // page underneath. The card has no actions of its own; it is not dismissible.
            setOnTouchListener { _, _ -> true }
        }

        val header = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val icon = ImageView(context).apply {
            setImageResource(R.drawable.ic_stat_shield)
            setColorFilter(SOFT_CORAL, PorterDuff.Mode.SRC_IN)
            importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        header.addView(icon, LinearLayout.LayoutParams(dp(20f), dp(20f)))

        titleView = TextView(context).apply {
            setTextColor(TEXT_PRIMARY)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            letterSpacing = 0f
            maxLines = 2
            includeFontPadding = false
        }
        header.addView(
            titleView,
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = dp(9f)
                marginEnd = dp(9f)
            }
        )

        countdownView = TextView(context).apply {
            setTextColor(ON_CORAL)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            // Monospace digits: the pill must not change width every second.
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            letterSpacing = 0f
            includeFontPadding = false
            gravity = Gravity.CENTER
            minWidth = dp(66f)
            background = GradientDrawable().apply {
                cornerRadius = dpf(50f)
                setColor(SOFT_CORAL)
            }
            setPadding(dp(11f), dp(5f), dp(11f), dp(5f))
        }
        header.addView(
            countdownView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        card.addView(
            header,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        progressBar = CountdownBar(context)
        card.addView(
            progressBar,
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(6f)).apply {
                topMargin = dp(14f)
            }
        )

        bodyView = TextView(context).apply {
            setTextColor(TEXT_SECONDARY)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f)
            letterSpacing = 0f
            includeFontPadding = false
            setLineSpacing(dp(3f).toFloat(), 1f)
        }
        card.addView(
            bodyView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(12f)
            }
        )

        // The rule of the card, spelled out, because there is no button to explain it.
        staysView = TextView(context).apply {
            setTextColor(TEXT_MUTED)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f)
            letterSpacing = 0f
            includeFontPadding = false
            setLineSpacing(dp(2f).toFloat(), 1f)
        }
        card.addView(
            staysView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(12f)
            }
        )

        addView(
            card,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
    }

    /**
     * Binds the current countdown. Safe to call on every state emission: the card only
     * re-measures when a whole second has passed, and only re-applies copy when the
     * language changed (a language switch is rare but must not leave stale text).
     */
    fun render(state: GracePeriodState, language: AppLanguage) {
        val languageChanged = language != renderedLanguage
        if (languageChanged) {
            layoutDirection =
                if (language.isRtl) LAYOUT_DIRECTION_RTL else LAYOUT_DIRECTION_LTR
            titleView.text = Translations.t("overlay_title", language)
            bodyView.text = Translations.t("overlay_body", language)
            staysView.text = Translations.t("overlay_stays", language)
            renderedLanguage = language
            renderedSeconds = -1
        }

        if (state.remainingSeconds != renderedSeconds) {
            countdownView.text = state.formattedTime
            progressBar.progress = state.progress
            renderedSeconds = state.remainingSeconds
        }
    }

    private fun dp(value: Float): Int = dpf(value).roundToInt()

    private fun dpf(value: Float): Float = value * resources.displayMetrics.density

    /** Six-dp progress bar that fills from the start edge (right side in RTL). */
    private class CountdownBar(context: Context) : View(context) {

        private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = TRACK }
        private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = SOFT_CORAL }
        private val rect = RectF()

        var progress: Float = 1f
            set(value) {
                field = value.coerceIn(0f, 1f)
                invalidate()
            }

        override fun onDraw(canvas: Canvas) {
            val h = height.toFloat()
            val radius = h / 2f

            rect.set(0f, 0f, width.toFloat(), h)
            canvas.drawRoundRect(rect, radius, radius, trackPaint)

            val filled = width * progress
            if (filled <= 0f) return

            if (layoutDirection == LAYOUT_DIRECTION_RTL) {
                rect.set(width - filled, 0f, width.toFloat(), h)
            } else {
                rect.set(0f, 0f, filled, h)
            }
            canvas.drawRoundRect(rect, radius, radius, fillPaint)
        }
    }
}

// Mirrors ui/theme/Color.kt so the overlay matches the in-app card exactly.
private val CARD = 0xFF111A16.toInt()           // BlackCard
private val WARM_TINT = 0xFF2A1E22.toInt()      // BlackCard with a 16% SoftCoral wash
private val BORDER = 0x8CFB7185.toInt()         // SoftCoral at 55%
private val TRACK = 0xFF223129.toInt()          // BlackBorder
private val TEXT_PRIMARY = 0xFFEDF6F1.toInt()   // WhiteGlowing
private val TEXT_SECONDARY = 0xFFA8BDB3.toInt() // WhiteDim
private val TEXT_MUTED = 0xFF7E9389.toInt()     // TextMuted
private val SOFT_CORAL = 0xFFFB7185.toInt()     // SoftCoral
private val ON_CORAL = 0xFF050806.toInt()       // PureBlack
