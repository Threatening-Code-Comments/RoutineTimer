package de.threateningcodecomments.services_etc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import de.threateningcodecomments.routinetimer.MainActivity
import de.threateningcodecomments.routinetimer.RunContinuousRoutineFragment

class MyBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == CANCEL_ACTION) {
            Toast.makeText(context, "Hey! cancelling!", Toast.LENGTH_SHORT).show()
            cancelCountdown()
        }
    }

    private fun cancelCountdown() {
        val fragment = MainActivity.currentFragment

        if (fragment is RunContinuousRoutineFragment) {
            fragment.cancelCountdown()
        }
    }

    companion object {
        const val CANCEL_ACTION = "de.threateningcodecomments.routinetimer.CANCEL_ACTION"
    }
}