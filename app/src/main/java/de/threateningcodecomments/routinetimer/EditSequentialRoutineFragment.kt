package de.threateningcodecomments.routinetimer

import accessibility.ResourceClass
import accessibility.ResourceClass.initIconPack
import accessibility.Routine
import accessibility.Tile
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.maltaisn.icondialog.IconDialog
import com.maltaisn.icondialog.IconDialogSettings
import com.maltaisn.icondialog.data.Icon
import com.maltaisn.icondialog.pack.IconPack


class EditSequentialRoutineFragment : Fragment(), View.OnClickListener, IconDialog.Callback {
    private lateinit var root: ConstraintLayout
    private lateinit var closeIcon: ShapeableImageView

    private lateinit var tileCardView: MaterialCardView
    private lateinit var tileIconView: ShapeableImageView
    private lateinit var tileNameView: EditText

    private lateinit var routineNameEditText: EditText

    var routines: ArrayList<Routine>? = null
    lateinit var currentRoutine: Routine
    private var position: Int = 0

    val args: EditSequentialRoutineFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sharedElementEnterTransition = ResourceClass.sharedElementTransition

        super.onViewCreated(view, savedInstanceState)

        fragment = this

        initBufferViews()

        initListeners()

        initRoutines()

        updateRoutineNameHint()

        initUI()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.iv_EditRoutine_sequential_close ->
                navigateBack()
            R.id.iv_EditRoutine_sequential_icon -> {
                MainActivity.activityBuffer.openIconDialog(currentRoutine.tiles[position])
                //iconDialog = initIconSelecter()
                //iconDialog.show(requireActivity().supportFragmentManager, ICON_DIALOG_TAG)
            }
            else ->
                Toast.makeText(context, "Wrong onClickListener, wtf did you do?", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateRoutineNameHint() {
        routineNameEditText.setText(currentRoutine.name)
    }

    //region update card
    private fun initUI() {
        currentRoutine.setAccessibility(ResourceClass.isNightMode(MainActivity.activityBuffer.application))
        updateCard()
    }

    fun updateUI() {
        updateCard()
    }

    private fun updateCard() {
        val tile: Tile = currentRoutine.tiles[position]

        val tileName = tile.name
        tileNameView.setText(tileName)
        tileNameView.setTextColor(tile.contrastColor)

        val icon = ResourceClass.getIconDrawable(tile)
        tileIconView.setImageDrawable(icon)
        tileIconView.setColorFilter(tile.contrastColor)

        val color = tile.backgroundColor
        tileCardView.setCardBackgroundColor(color)
    }

    //endregion

    //region update tile in DB
    private fun updateRoutine() {
        ResourceClass.saveRoutine(currentRoutine)
    }


    //region icon
    lateinit var iconDialog: IconDialog

    private fun initIconSelecter(): IconDialog {
        //handle setup of iconSelecter
        // If dialog is already added to fragment manager, get it. If not, create a new instance.
        val iconDialog = requireActivity().supportFragmentManager.findFragmentByTag(ICON_DIALOG_TAG) as IconDialog?
                ?: IconDialog.newInstance(IconDialogSettings())
        val context: Context = requireContext()
        initIconPack(context)
        return iconDialog
    }

    override fun onIconDialogIconsSelected(dialog: IconDialog, icons: List<Icon>) {
        val tmpTile = currentRoutine.tiles[position]
        tmpTile.iconID = icons[0].id
        updateRoutine()
        updateCard()
    }

    override val iconDialogIconPack: IconPack = ResourceClass.getIconPack()

    //endregion

    //region name
    private fun editTileName(s: String?) {
        currentRoutine.tiles[position].name = s
        updateRoutine()
    }
    //endregion

    //endregion

    //region init
    private fun initListeners() {
        closeIcon.setOnClickListener(this)

        tileIconView.setOnClickListener(this)
        tileNameView.imeOptions = EditorInfo.IME_ACTION_DONE
        tileNameView.setRawInputType(InputType.TYPE_CLASS_TEXT)
        tileNameView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(text: Editable?) {
                if (text.toString() != "Tile name")
                    editTileName(text.toString())
            }
        })
        tileNameView.setOnKeyListener { view, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                (SelectRoutineFragment.activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(requireView().windowToken, 0)
                tileNameView.clearFocus()
                true
            } else keyCode == KeyEvent.KEYCODE_ENTER
            // Handle all other keys in the default way
        }
    }

    private fun initRoutines() {
        routines = ResourceClass.getRoutines()
        if (routines == null) {
            Toast.makeText(context, "Oh no, routines are null. Good bye.", Toast.LENGTH_LONG).show()
        }
        val position = args.routinePosition
        currentRoutine = routines!![position]
    }

    private fun initBufferViews() {
        val v = requireView()

        root = v.findViewById(R.id.cl_EditRoutine_sequential_root)
        closeIcon = v.findViewById(R.id.iv_EditRoutine_sequential_close)

        tileCardView = v.findViewById(R.id.cv_EditRoutine_sequential_card)
        tileIconView = v.findViewById(R.id.iv_EditRoutine_sequential_icon)
        tileNameView = v.findViewById(R.id.et_EditRoutine_sequential_name)

        routineNameEditText = v.findViewById(R.id.et_EditRoutine_sequential_routineName)
    }
    //endregion

    //region navigation
    private fun navigateBack() {
        val directions = EditSequentialRoutineFragmentDirections.actionEditSequentialRoutineFragmentToSelectRoutineFragment()
        val extras = FragmentNavigatorExtras(root as View to currentRoutine.uid!!, routineNameEditText to currentRoutine.name!!, tileIconView to currentRoutine.uid + "icon")

        findNavController().navigate(directions, extras)
    }

    //endregion

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_edit_sequential_routine, container, false)
    }

    companion object {
        const val ICON_DIALOG_TAG = "icon-dialog"

        lateinit var fragment: EditSequentialRoutineFragment
    }

}