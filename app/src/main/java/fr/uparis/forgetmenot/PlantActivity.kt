package fr.uparis.forgetmenot

import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import fr.uparis.forgetmenot.databinding.ActivityPlantBinding
import java.time.LocalDate

class PlantActivity : AppCompatActivity() {

    private lateinit var binding : ActivityPlantBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlantBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar
        setSupportActionBar(binding.myToolbar)

        // Affichage du logo de retour en arrière
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)


        // Récuperation de l'id de la plante dans le bundle
        val bundle = intent.extras
        val id: Int
        if (bundle != null) {
            id = bundle.getInt("id")
        } else {
            return
        }

        // Lecture de la base de données
        val db = PlantDB.getDatabase(this)
        var plant : Plant? = null

        var threadDb = Thread {
            plant = db.plantDao().loadPlant(id)
        }
        threadDb.start()
        threadDb.join()

        // Affichage des attributs de la plante
        val lastWatering = LocalDate.parse(plant?.lastWatering)
        val nextWatering = LocalDate.parse(plant?.nextWatering)
        binding.name.text = "Name : " + plant?.name
        binding.latinName.text = "Latin name : " + plant?.latinName
        binding.imageView?.setImageURI(plant?.imageLink?.toUri())
        binding.summerFreq.text = "Summer Frequence : " + plant?.summerFreq.toString()
        binding.winterFreq.text = "Winter Frequence : " + plant?.winterFreq.toString()
        binding.lastWatering.text = "Last Watering : ${lastWatering.dayOfMonth} ${lastWatering.month} ${lastWatering.year}"
        binding.nextWatering.text = "Next Watering : ${nextWatering.dayOfMonth} ${nextWatering.month} ${nextWatering.year}"

        // Bouton de suppression
        binding.delete.setOnClickListener {
            threadDb = Thread {
                db.plantDao().deletePlants(plant!!)
            }
            threadDb.start()
            finish()
        }

        // Bouton de modification
        binding.edit.setOnClickListener {
            val intent = Intent(this, PlantEditActivity::class.java)
            val b = Bundle()
            b.putInt("id", id)
            intent.putExtras(b)
            intent.type = ACTION_GET_CONTENT
            this.finish()
            this.startActivity(intent)
        }
    }

    // Comportement du menu de la toolbar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Retour en arrière fait quitter l'activité
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}