package de.threateningcodecomments.routinetimer

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.maltaisn.icondialog.IconDialog
import com.maltaisn.icondialog.IconDialogSettings
import com.maltaisn.icondialog.data.Icon
import com.maltaisn.icondialog.pack.IconPack
import de.threateningcodecomments.accessibility.ResourceClass
import de.threateningcodecomments.accessibility.Tile
import de.threateningcodecomments.accessibility.UIContainer
import de.threateningcodecomments.routinetimer.EditSequentialRoutineFragment.Companion.ICON_DIALOG_TAG
import de.threateningcodecomments.services_etc.CountingService

class MainActivity : AppCompatActivity(), IconDialog.Callback, UIContainer {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        instance = this
        currentFragment = this
        ResourceClass.Anim.initAnimations(this)
    }

    override fun onStart() {
        super.onStart()

        isNightMode
    }

    override val iconDialogIconPack: IconPack
        get() = ResourceClass.getIconPack()

    override fun onIconDialogIconsSelected(dialog: IconDialog, icons: List<Icon>) {
        val iconID = icons[0].id
        tmpTile.iconID = iconID
        EditSequentialRoutineFragment.fragment.updateUI()
    }

    private lateinit var tmpTile: Tile
    fun openIconDialog(tile: Tile) {
        val iconDialog = supportFragmentManager.findFragmentByTag(ICON_DIALOG_TAG) as IconDialog?
                ?: IconDialog.newInstance(IconDialogSettings())
        iconDialog.show(supportFragmentManager, ICON_DIALOG_TAG)
        tmpTile = tile
    }

    fun startCountingService(routineUid: String) {
        val intent = Intent(this, CountingService::class.java)
        intent.putExtra(CountingService.ROUTINE_UID_KEY, routineUid)

        startService(intent)
    }

    override fun updateUI() {}
    override fun updateCurrentTile() {}

    companion object {
        lateinit var instance: MainActivity
        lateinit var currentFragment: UIContainer
        var countdownServiceRunning = false

        val sharedPreferences: SharedPreferences
            get() = instance.getPreferences(Context.MODE_PRIVATE)

        val isNightMode: Boolean
            get() {
                val isNightMode = ResourceClass.isNightMode(instance)
                ResourceClass.updateNightMode(instance)
                return isNightMode
            }

    }
}