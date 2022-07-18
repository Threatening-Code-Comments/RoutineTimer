package de.threateningcodecomments.accessibility

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import de.threateningcodecomments.data.TileEvent
import de.threateningcodecomments.routinetimer.StartFragment

object TestButtonRun {
    private var hasListener = false

    fun run(sf: StartFragment) {
        fun log(tileEvent: TileEvent?, snapshot: DataSnapshot) {
            MyLog.d("TileEvent: $tileEvent. \n shapshot: $snapshot")
        }

        sf.run {

        }
    }
}
