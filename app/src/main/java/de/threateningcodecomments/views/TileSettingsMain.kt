package de.threateningcodecomments.views

import android.animation.LayoutTransition
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import de.threateningcodecomments.accessibility.RC
import de.threateningcodecomments.routinetimer.R

class TileSettingsMain @kotlin.jvm.JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private lateinit var infoIcon: ImageView
    private lateinit var infoName: MaterialTextView
    private lateinit var infoSummary: MaterialTextView
    private var editLayout: ViewGroup? = null
    private var editLayoutIsVisibleDefault: Boolean = false
    private lateinit var infoLayout: ConstraintLayout

    var name: String
        set(value) {
            infoName.text = value
        }
        get() = infoName.text.toString()

    var summary: String
        set(value) {
            infoSummary.text = value
        }
        get() = infoSummary.text.toString()

    var icon: Drawable
        set(value) =
            infoIcon.setImageDrawable(value)
        get() = infoIcon.drawable

    init {
        if (isInEditMode)
            RC.updateContext(context)

        if (id == View.NO_ID)
        //give id to parent layout
            id = View.generateViewId()

        //inflate layout
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.TileSettingsMain, defStyleAttr, 0)

        val srcId = attributes.getResourceId(R.styleable.TileSettingsMain_src, R.drawable.ic_defaultdrawable)
        val name = attributes.getString(R.styleable.TileSettingsMain_name)
                ?: RC.Resources.getString(R.string.str_TileSettingsMain_defaultName)
        val hasSummary = attributes.getBoolean(R.styleable.TileSettingsMain_hasSummary, false)
        editLayoutIsVisibleDefault = attributes.getBoolean(R.styleable.TileSettingsMain_editLayoutIsVisible, false)

        inflateLayoutUsing(context, srcId, name, hasSummary)

        attributes.recycle()

        //set bottom border
        setBackgroundResource(R.drawable.border_bottom)

        //set padding for animation
        setPadding(0, 0, 0, dpToPx(3))

        layoutTransition = LayoutTransition().apply {
            enableTransitionType(LayoutTransition.CHANGING)
        }
    }

    private var uiRunnable = {}

    fun doOnUIUpdate(run: () -> Unit) {
        uiRunnable = run
    }

    fun updateUI() =
            uiRunnable.invoke()

    private fun inflateLayoutUsing(context: Context, srcId: Int, name: String, hasSummary: Boolean) {
        LayoutInflater.from(context).inflate(R.layout.cvl_tile_settings_main, this, true) as ConstraintLayout

        infoLayout = getChildAt(0) as ConstraintLayout

        //set solid color for animation
        if (!isInEditMode)
            infoLayout.setBackgroundColor(RC.Resources.Colors.onSurfaceColor)

        val contrastColor = RC.Resources.Colors.contrastColor

        infoIcon = findViewById(R.id.iv_cvl_TileSettingsMain_infoIcon)
        infoName = findViewById(R.id.tv_cvl_TileSettingsMain_infoName)
        infoSummary = findViewById(R.id.tv_cvl_TileSettingsMain_infoSummary)

        infoIcon.setImageDrawable(RC.Resources.getDrawable(srcId))
        infoIcon.setColorFilter(contrastColor)

        infoName.text = name
        infoName.setTextColor(contrastColor)

        infoSummary.isVisible = hasSummary
        infoSummary.setTextColor(contrastColor)

        infoIcon.apply {
            setImageDrawable(RC.Resources.getDrawable(srcId))

            setColorFilter(contrastColor)
        }

        infoName.apply {
            text = name

            setTextColor(contrastColor)
        }

        infoSummary.apply {
            isVisible = hasSummary

            setTextColor(contrastColor)
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        if (childCount > 1)
            registerEditLayout()
    }

    private fun registerEditLayout() {
        for (child in this)
            if (child != infoLayout)
                editLayout = child as ViewGroup


        if (editLayout == null)
            throw IllegalStateException("editLayout must not be null!")

        editLayout!!.apply {
            //elevation for animation
            translationZ = -2f

            if (id == NO_ID)
                id = generateViewId()

            for (view in this)
                if (view.id == NO_ID)
                    view.id = generateViewId()
        }

        ConstraintSet().apply {
            clone(this@TileSettingsMain)

            constrainHeight(editLayout!!.id, ConstraintSet.WRAP_CONTENT)
            constrainWidth(editLayout!!.id, ConstraintSet.MATCH_CONSTRAINT)

            connect(editLayout!!.id, ConstraintSet.TOP,
                    this@TileSettingsMain.infoLayout.id, ConstraintSet.BOTTOM)
            connect(editLayout!!.id, ConstraintSet.START,
                    this@TileSettingsMain.infoLayout.id, ConstraintSet.START)
            connect(editLayout!!.id, ConstraintSet.END,
                    this@TileSettingsMain.infoLayout.id, ConstraintSet.END)

            applyTo(this@TileSettingsMain)
        }

        editLayout!!.isVisible =
                if (!isInEditMode)
                    editLayoutIsVisibleDefault
                else
                    true

        setOnClickListener {
            toggleEditLayout()
        }

        initVisibility()
    }

    /**
     * Views that shouldn't be tried to have improved visibility. (Auto text color etc)
     *
     * Example:
     *
     *      [weekdayPicker, myCustomButton, ...]
     */
    var visibilityViewsToIgnore: Set<View>? = null
        set(value) {
            val newValue = mutableSetOf<View>()

            if (value != null)
                for (view in value)
                    newValue.add(view)

            field =
                    if (value != null)
                        newValue
                    else
                        value
            initVisibility()
        }

    /**
     * Types of views that shouldn't be tried to have improved visibility. (Auto text color etc)
     *
     * Example:
     *
     *      [WeekdayPicker, CustomButton, ...]
     */
    var visibilityTypesToIgnore: Set<String>? = null
        set(value) {
            field = value
            initVisibility()
        }

    /**
     * Tries to set the best visibility for all views in the editLayout. This includes auto text color, auto image
     * filters, etc.
     *
     * To filter out views or types, pass them to [visibilityViewsToIgnore] or [visibilityTypesToIgnore]. The method
     * automatically gets called once layout is loaded and on any update on views to filter out.
     */
    private fun initVisibility() {
        editLayout ?: return

        val cc = RC.Resources.Colors.contrastColor

        val viewsToSkip = mutableSetOf<View>()

        if (visibilityViewsToIgnore != null)
            viewsToSkip.addAll(visibilityViewsToIgnore!!)

        //fixed
        val predefined = arrayListOf<String>(WeekdayPicker::class.java.name, MaterialButton::class.java.name)

        if (visibilityTypesToIgnore != null)
            predefined.addAll(visibilityTypesToIgnore!!)

        val typesToSkip: Set<String> = predefined.toSet()

        setVisibilityForChildren(editLayout!!, viewsToSkip, typesToSkip, cc)
    }

    private fun setVisibilityForChildren(layout: ViewGroup, viewsToSkip: Set<Any?>, typesToSkip: Set<String>, cc: Int) {
        if (layout in viewsToSkip || layout::class.java.name in typesToSkip)
            return

        childLoop@
        for (child in layout) {
            setViewVisibility(child, viewsToSkip, typesToSkip, cc)

            if (child is ViewGroup)
                if (child.childCount != 0)
                    setVisibilityForChildren(child, viewsToSkip, typesToSkip, cc)
        }
    }

    private fun setViewVisibility(child: View, viewsToSkip: Set<Any?>, typesToSkip: Set<String>, cc: Int) {
        if (child in viewsToSkip || child::class.java.name in typesToSkip)
            return

        if (child is MaterialTextView || child is TextView || child is AutoCompleteTextView)
            (child as TextView).setTextColor(cc)


        if (child is ImageView || child is ShapeableImageView)
            (child as ImageView).setColorFilter(cc)
    }

    private var animDuration: Long = 250L

    private fun toggleEditLayout() {
        val visible = editLayout!!.isVisible

        animDuration = 250L

        editLayout!!.apply {
            setBackgroundColor(RC.Resources.Colors.onSurfaceColor)

            //set up position for first transition
            translationY =
                    if (visible)
                        0f
                    else
                        -editLayout!!.height.toFloat()

            //animate sliding
            animate()
                    .translationY(
                            if (visible)
                                -editLayout!!.height.toFloat()
                            else
                                0f
                    )
                    .setDuration(animDuration)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .withEndAction {
                        if (visible)
                            isVisible = false
                    }

            //toggle visibility
            if (!visible) {
                isVisible = true
            }
        }
    }

    private fun dpToPx(dp: Int) = RC.Conversions.Size.dpToPx(dp).toInt()

}

