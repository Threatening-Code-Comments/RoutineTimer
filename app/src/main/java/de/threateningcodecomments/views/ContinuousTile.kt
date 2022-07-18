package de.threateningcodecomments.views

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.AutoCompleteTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.core.view.isVisible
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import de.threateningcodecomments.accessibility.RC
import de.threateningcodecomments.data.Tile
import de.threateningcodecomments.routinetimer.R
import android.content.res.ColorStateList
import de.threateningcodecomments.accessibility.MyLog
import de.threateningcodecomments.data.TileEvent
import de.threateningcodecomments.routinetimer.MainActivity


class ContinuousTile @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    init {
        if (isInEditMode)
            RC.updateContext(context)
    }

    private lateinit var cardView: MaterialCardView
    private var cardBackground = RC.Resources.Colors.contrastColor
        set(value) {
            (context as MainActivity).runOnUiThread {
                cardView.setCardBackgroundColor(value)
            }
            field = value
        }

    private lateinit var nameView: MaterialTextView
    private var name = ""
        set(value) {
            (context as MainActivity).runOnUiThread {
                nameView.text = value
            }
            field = value
        }

    private lateinit var iconView: ShapeableImageView
    private var icon = RC.Resources.errorDrawable
        set(value) {
            (context as MainActivity).runOnUiThread {
                iconView.setImageDrawable(value)
            }
            field = value
        }

    private lateinit var info1IconView: ShapeableImageView
    private var info1Icon = RC.Resources.errorDrawable
        set(value) {
            (context as MainActivity).runOnUiThread {
                info1IconView.setImageDrawable(value)
            }
            field = value
        }

    private lateinit var info1View: MaterialTextView
    private var info1 = ""
        set(value) {
            (context as MainActivity).runOnUiThread {
                info1View.text = value
            }
            field = value
        }

    private lateinit var info2IconView: ShapeableImageView
    private var info2Icon: Drawable? = RC.Resources.errorDrawable
        set(value) {

            (context as MainActivity).runOnUiThread {
                info2IconView.setImageDrawable(value)
            }
            field = value
        }

    private lateinit var info2View: MaterialTextView
    private var info2: String? = ""
        set(value) {
            (context as MainActivity).runOnUiThread {
                info2View.text = value
            }
            field = value
        }

    private lateinit var dataInput: AutoCompleteTextView
    private var data = ""
        set(value) {
            if (dataInput.text.toString() != value)
                (context as MainActivity).runOnUiThread {
                    dataInput.setText(value)
                }

            field = value
        }

    private var onClickRunnable: ClickRunnable = { }
    fun doOnClick(block: ClickRunnable) {
        onClickRunnable = block
    }

    override fun performClick(): Boolean {
        onClickRunnable(tile)
        return super.performClick()
    }

    fun doAfterActiveChange(block: () -> Unit) {
        activeRunnable = block
    }

    private var activeRunnable = {}

    fun deactivate(isInstant: Boolean = false) {
        val oldColor = cardBackground
        val newColor = Color.GRAY

        val contrastColor =
            RC.Conversions.Colors.calculateContrast(Color.GRAY)

        animateColor(oldColor, newColor, isInstant)
    }

    fun activate(isInstant: Boolean = false) {
        val oldColor = cardBackground
        val newColor = tile.backgroundColor

        val contrastColor =
            RC.Conversions.Colors.calculateContrast(tile.backgroundColor)

        animateColor(oldColor, newColor, isInstant)
    }

    private fun animateColor(oldColor: Int, newColor: Int, isInstant: Boolean) {
        val previousContrastColor = RC.Conversions.Colors.calculateContrast(oldColor)
        val contrastColor = RC.Conversions.Colors.calculateContrast(newColor)

        //if is instant
        if (isInstant) {
            nameView.setTextColor(contrastColor)
            iconView.setColorFilter(contrastColor)

            info1View.setTextColor(contrastColor)
            info1IconView.setColorFilter(contrastColor)
            info2View.setTextColor(contrastColor)
            info2IconView.setColorFilter(contrastColor)

            cardBackground = newColor

            return
        }

        //if isn't instant

        val contrastAnimation =
            ValueAnimator.ofObject(ArgbEvaluator(), previousContrastColor, contrastColor)
        contrastAnimation.duration = 300L

        contrastAnimation.addUpdateListener { animator ->
            val c = animator.animatedValue as Int

            nameView.setTextColor(c)
            iconView.setColorFilter(c)

            info1View.setTextColor(c)
            info1IconView.setColorFilter(c)
            info2View.setTextColor(c)
            info2IconView.setColorFilter(c)
        }

        val cardAnimation =
            ValueAnimator.ofObject(ArgbEvaluator(), oldColor, newColor)
        cardAnimation.duration = 300L

        cardAnimation.addUpdateListener { animator ->
            val c = animator.animatedValue as Int

            cardBackground = c
        }
        cardAnimation.doOnEnd {
            activeRunnable()
            activeRunnable = {}
        }

        Handler(Looper.getMainLooper()).post {
            cardAnimation.start()
            contrastAnimation.start()
        }
    }

    init {
        inflateLayoutUsing(context)

        /*this.setOnClickListener {
            onClickRunnable(tile)
        }*/

        this.isClickable = true
    }

    private fun inflateLayoutUsing(context: Context) {
        val view = LayoutInflater.from(context).inflate(R.layout.view_continuous_tile_left, this, true)

        cardView = findViewById<MaterialCardView>(R.id.cv_View_ConTile_MainCard)
        nameView = findViewById(R.id.tv_View_ConTile_Name)
        iconView = findViewById(R.id.iv_View_ConTile_icon)

        info1IconView = findViewById(R.id.iv_View_ConTile_info1Icon)
        info1View = findViewById(R.id.tv_View_ConTile_info1)

        info2IconView = findViewById(R.id.iv_View_ConTile_info2Icon)
        info2View = findViewById(R.id.tv_View_ConTile_info2)

        dataInput = findViewById(R.id.et_View_ConTile_dataInput)
    }

    private lateinit var tile: Tile

    private var visibilityWasHandled = false
    fun updateUI(tile: Tile, info1: String? = null, info2: String? = null) {
        this.tile = tile

        visibility = View.VISIBLE

        if(tile == Tile.DEFAULT_TILE){
            visibility = View.INVISIBLE
            return
        }

        name = tile.name.toString()
        icon = RC.getIconDrawable(tile)!!
        cardBackground = tile.backgroundColor

        val isData = tile.mode == Tile.MODE_DATA

        dataInput.isVisible = isData
        (dataInput.parent as View).isVisible = isData
        info1IconView.isVisible = !isData
        info1View.isVisible = !isData
        info2IconView.isVisible = !isData
        info2View.isVisible = !isData

        val pausedEvent = RC.tileEvents.getEventsOfTile(tile).sortedBy { it.start }.lastOrNull() { it.isPaused }

        updateTileTextAndIcon(tile, info1, info2, pausedEvent)

        //handle visibility
        if (visibilityWasHandled)
            return

        val contrastColor = tile.contrastColor

        val states = arrayOf(
            intArrayOf(android.R.attr.state_enabled),
            intArrayOf(-android.R.attr.state_enabled),
            intArrayOf(-android.R.attr.state_checked),
            intArrayOf(android.R.attr.state_pressed),
            intArrayOf(-android.R.attr.focusable),
            intArrayOf(android.R.attr.state_focused),
            intArrayOf(-android.R.attr.state_focused)
        )

        val colors = intArrayOf(
            contrastColor,
            contrastColor,
            contrastColor,
            contrastColor,
            contrastColor,
            contrastColor,
            contrastColor
        )

        val myList = ColorStateList(states, colors)

        nameView.setTextColor(contrastColor)
        iconView.setColorFilter(contrastColor)
        info1IconView.setColorFilter(contrastColor)
        info2IconView.setColorFilter(contrastColor)
        info1View.setTextColor(contrastColor)
        info2View.setTextColor(contrastColor)
        dataInput.setHintTextColor(myList)
        dataInput.setTextColor(myList)
        val tilParent = dataInput.parent.parent as TextInputLayout
        tilParent.boxStrokeColor = contrastColor
        tilParent.hintTextColor = myList

        visibilityWasHandled = true

        resizeInfoViews(tile)
    }

    private fun updateTileTextAndIcon(
        tile: Tile,
        info1: String?,
        info2: String?,
        pausedEvent: TileEvent?
    ) {
        val tileModeException =
            IllegalStateException("Tile mode is incomprehensible (${tile.mode})")

        val isData = tile.mode == Tile.MODE_DATA

        //handle data tile
        if (isData) {
            tile.data.dataField = dataInput

            (dataInput.parent.parent as TextInputLayout).hint = tile.data.lastEntry

            // TODO("data is to be implemented")

            return
        }


        //handle other tile modes
        val isTap = tile.mode == Tile.MODE_TAP

        val info1IconId =
            when (tile.mode) {
                Tile.MODE_COUNT_UP -> R.drawable.ic_time_elapsed
                Tile.MODE_COUNT_DOWN -> R.drawable.ic_countdown_time
                Tile.MODE_TAP -> R.drawable.ic_mode_tap
                else -> throw tileModeException
            }
        info1Icon = RC.Resources.getDrawable(info1IconId)

        this.info1 =
            info1
                ?: when (tile.mode) {
                    Tile.MODE_TAP -> tile.tapSettings.count.toString()
                    Tile.MODE_COUNT_DOWN -> {
                        RC.Conversions.Time.millisToShortTimeString(
                            tile.countDownSettings.countDownTime - (pausedEvent?.data?.toLong() ?: 0L)
                        )
                    }
                    else -> RC.Conversions.Time.millisToHHMMSSorMMSS((pausedEvent?.data?.toLong() ?: 0L))
                }


        //handle visibility for tap, only info1 is supposed to be visible
        info2IconView.isVisible = !isTap
        info2View.isVisible = !isTap

        val info2IconId =
            when (tile.mode) {
                Tile.MODE_COUNT_UP -> R.drawable.ic_total_time_elapsed
                Tile.MODE_COUNT_DOWN -> R.drawable.ic_mode_tap
                Tile.MODE_TAP -> null
                else -> throw tileModeException
            }
        info2Icon = if (info2IconId != null)
            RC.Resources.getDrawable(info2IconId)
        else
            null

        this.info2 =
            info2 ?: when (tile.mode) {
                Tile.MODE_COUNT_UP -> RC.Conversions.Time.millisToHHMMSSorMMSS(tile.totalCountedTime)
                Tile.MODE_COUNT_DOWN -> //times tile has been executed
                    (tile.totalCountedTime / tile.countDownSettings.countDownTime).toInt().toString()
                Tile.MODE_TAP -> null
                else -> throw tileModeException
            }

        tile.data.dataField = null
    }

    private fun resizeInfoViews(tile: Tile) {
        if (tile.mode == Tile.MODE_TAP) {
            val factor = 1.5F

            info1IconView.animate()
                .setDuration(0)
                .scaleX(factor)
                .scaleY(factor)
                .start()

            /*info1View.animate()
                .setDuration(1000)
                .translationX(0f)
                .scaleX(factor)
                .scaleY(factor)
                .start()*/

            info1View.apply {
                scaleX = factor
                scaleY = factor
            }
        } else {
            info1IconView.animate()
                .setDuration(1000)
                .scaleX(1F)
                .scaleY(1F)
                .start()

            info1View.animate()
                .setDuration(1000)
                .scaleX(1F)
                .scaleY(1F)
                .start()
        }
    }

}

typealias ClickRunnable = (tile: Tile) -> Unit