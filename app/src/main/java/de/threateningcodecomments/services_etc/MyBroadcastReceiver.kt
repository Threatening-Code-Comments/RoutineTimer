package de.threateningcodecomments.services_etc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MyBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == CANCEL_ACTION) {
            cancelCountdown(intent)
        }
    }

    private fun cancelCountdown(intent: Intent) {
        /*val fragment = MainActivity.currentFragment

        if (fragment is RunContinuousRoutineFragment) {
            fragment.cancelCountdown()
        }*/
        val routineUid = intent.getStringExtra(ROUTINE_UID_KEY)

        if (routineUid != null) {
            CountdownService.Timers.stopNotificationCountdown(routineUid)
        }
    }

    companion object {
        const val CANCEL_ACTION = "de.threateningcodecomments.routinetimer.CANCEL_ACTION"
        const val ROUTINE_UID_KEY = "de.threateningcodecomments.routinetimer.ROUTINE_UID_KEY"
    }
}