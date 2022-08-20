package fr.uparis.forgetmenot

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.time.LocalDate

class WateringService : Service() {

    companion object {
        const val NOTIF_ID = 1023
        const val CHANNEL_ID = "Forget Me Not"
    }

    // Créer un notification channel,
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(NotificationChannel(CHANNEL_ID, "ForgetMeNot", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Watering alarm"
            })
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendNotification() {
        val intent = Intent(this, WateringActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 10, intent, 0)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_flower)
            .setContentTitle("Arrosage des plantes")
            .setContentText("Il est temps d'arroser vos plantes !")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            notify(NOTIF_ID, builder.build())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // TODO: Vérifier qu'il y a bien au moins une plante à arroser aujourd'hui

        val db = PlantDB.getDatabase(this)
        var plants = emptyArray<Plant>()

        val threadDb = Thread {
            plants = db.plantDao().loadAll()
        }
        threadDb.start()
        threadDb.join()

        val today = LocalDate.now().toString()

        for (plant in plants) {
            Log.d("tototest", "${plant.name} : ${plant.nextWatering} -> ${plant.nextWatering == today}")
        }

        createNotificationChannel()
        sendNotification()
        return START_NOT_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {return null}

    // Faire une alarme qui lance le service à 9h
    // Le service propose un lien vers l'activité d'arrosage si il y a un arrosage à se faire puis lance une alarme pour relancer le service, et le service s'éteint.
    // l'activité d'arrosage propose une liste des plantes à arroser
}