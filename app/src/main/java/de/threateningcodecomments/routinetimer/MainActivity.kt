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
import de.threateningcodecomments.accessibility.RC
import de.threateningcodecomments.accessibility.Tile
import de.threateningcodecomments.accessibility.UIContainer
import de.threateningcodecomments.routinetimer.EditSequentialRoutineFragment.Companion.ICON_DIALOG_TAG
import de.threateningcodecomments.services_etc.CountingService

class MainActivity : AppCompatActivity(), IconDialog.Callback, UIContainer {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        instance = this
        /*Thread {
            RC.Anim.initAnimations(this)
            RC.updateContext(applicationContext)
        }.start()*/
        RC.updateContext(applicationContext)
        RC.Anim.initAnimations(this)
    }

    override fun onResume() {
        super.onResume()
        RC.updateContext(applicationContext)
        (application as App).currentActivity = this
    }

    override val iconDialogIconPack: IconPack
        get() = RC.getIconPack()

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
        private var instance: MainActivity = MainActivity()

        @JvmStatic
        val currentFragment: UIContainer
            get() = instance.supportFragmentManager.fragments.first()?.childFragmentManager?.fragments?.get(0) as
                    UIContainer

        @JvmStatic
        var countdownServiceRunning = false

        @JvmStatic
        val sharedPreferences: SharedPreferences
            get() = instance.getPreferences(Context.MODE_PRIVATE)
    }
}