package fr.uparis.forgetmenot

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import fr.uparis.forgetmenot.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Met en place une alarme qui se déclanche tout les jours à 9h pour arroser les plantes
        val intent = Intent(this, WateringService::class.java)

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val calendar: Calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 9)
            timeInMillis = System.currentTimeMillis()
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            PendingIntent.getService(this, 2, intent, PendingIntent.FLAG_IMMUTABLE)
        )

        binding.add.setOnClickListener {
            startActivity(Intent(this, AddPlantActivity::class.java))
        }

        binding.plants.setOnClickListener {
            startActivity(Intent(this, MyPlants::class.java))
        }

        binding.watering.setOnClickListener {
            startActivity(Intent(this, WateringActivity::class.java))
        }
    }
}