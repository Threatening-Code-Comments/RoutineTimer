package de.threateningcodecomments.routinetimer

import android.content.Intent
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
import de.threateningcodecomments.services_etc.CountdownService

class MainActivity : AppCompatActivity(), IconDialog.Callback, UIContainer {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        activityBuffer = this
        currentFragment = this
        ResourceClass.anim.initAnimations(this)
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

    fun startCountdownService(routineUid: String) {
        val intent = Intent(this, CountdownService::class.java)
        intent.putExtra(CountdownService.ROUTINE_UID_KEY, routineUid)
        startService(intent)
    }

    fun stopCountdownService() {
        val intent = Intent(this, CountdownService::class.java)
        stopService(intent)
    }

    override fun updateUI() {}
    override fun updateCurrentTile() {}

    companion object {
        lateinit var activityBuffer: MainActivity
        lateinit var currentFragment: UIContainer
        var countdownServiceRunning = false
    }
}