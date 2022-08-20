package fr.uparis.forgetmenot

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.app.NotificationManager

/**
 * A compléter
 */
class PlantService : Service() {

    private val notificationMgr: NotificationManager? = null
    private val myThreads = ThreadGroup("ServiceWorker")

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Thread(
            myThreads, null,
            "BackgroundService"
        )
            .start()
        return START_NOT_STICKY
        /*startForeground(id : Int, notification : Notification)*/

    }

    override fun onDestroy() {
        //arrêter tous les threads dans ThreadGroup
        myThreads.interrupt()
        //supprimer la notification
        notificationMgr!!.cancelAll()
        super.onDestroy()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}
