package de.threateningcodecomments.routinetimer

import accessibility.ResourceClass
import accessibility.Tile
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.maltaisn.icondialog.IconDialog
import com.maltaisn.icondialog.IconDialogSettings
import com.maltaisn.icondialog.data.Icon
import com.maltaisn.icondialog.pack.IconPack
import de.threateningcodecomments.routinetimer.EditSequentialRoutineFragment.Companion.ICON_DIALOG_TAG

class MainActivity : AppCompatActivity(), IconDialog.Callback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        activityBuffer = this
        ResourceClass.initAnimations(this)
    }

    override val iconDialogIconPack: IconPack?
        get() = ResourceClass.getIconPack()

    override fun onIconDialogIconsSelected(dialog: IconDialog, icons: List<Icon>) {
        val iconID = icons[0].id
        tmpTile.iconID = iconID
        EditSequentialRoutineFragment.fragment.updateUI()
    }

    private lateinit var tmpTile: Tile
    public fun openIconDialog(tile: Tile) {
        val iconDialog = supportFragmentManager.findFragmentByTag(ICON_DIALOG_TAG) as IconDialog?
                ?: IconDialog.newInstance(IconDialogSettings())
        iconDialog.show(supportFragmentManager, ICON_DIALOG_TAG)
        tmpTile = tile
    }

    companion object {
        lateinit var activityBuffer: MainActivity
    }
}