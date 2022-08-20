package fr.uparis.forgetmenot

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import fr.uparis.forgetmenot.databinding.ActivityWateringBinding
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

class WateringActivity : AppCompatActivity() {

    private lateinit var binding : ActivityWateringBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWateringBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar
        setSupportActionBar(binding.myToolbar)

        // Affichage du logo de retour en arrière
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // TODO: Afficher les plantes à arroser puis proposer de les arroser ou de reporter l'arrosage

        //Date et format de la date
        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val cal = Calendar.getInstance()

        //Accès à la base de donneé
        val db = PlantDB.getDatabase(this)
        val adapter = RecAdapter()

        //Les liste  vides pour filtrer les plants que nous voulions
        var plants = emptyArray<Plant>()
        var plant = emptyArray<Plant>()

        //Date d'aujourd'hui en string
        val today = LocalDate.now().toString()

        //Recupere la liste des plants de la base de donnée
        val thread = Thread {
            plant = db.plantDao().loadAll()
        }
        thread.start()
        thread.join()

        for(i in plant.indices){
            if(plant[i].nextWatering == today) {
                plants = arrayOf(plant[i])
            }
        }
        //Affichage de la liste des plantes a arroser
        adapter.setListePlants(plants)
        binding.wateringRecycler?.hasFixedSize()
        binding.wateringRecycler?.layoutManager = LinearLayoutManager(this)
        binding.wateringRecycler?.adapter = adapter

        if(plants.isEmpty()){
            binding.arroser?.isEnabled=false
            binding.reporter?.isEnabled=false
            binding.test.text = "Il n'y a pas de plante à arroser aujourd'hui"
        }
        else{
            binding.test.visibility= View.INVISIBLE
        }
        //Action pour le bouton arroser
        binding.arroser?.setOnClickListener {
            //parcour des plantes
            for(i in plants.indices){
                //derniere date d'arrosage devient aujourd'hui
                plants[i].lastWatering=today

                //Si la plante a une frenquence d'hiver
                if(plants[i].winterFreq!=null){
                    //Si la date d'aujourd'hui appartient au mois entre avril à octobre
                   if(LocalDate.now().monthValue in 4..10){
                        cal.add(Calendar.DATE,plants[i].summerFreq)
                   }
                    else {
                        cal.add(Calendar.DATE,plants[i].winterFreq!!)
                   }
                }
                else{
                    cal.add(Calendar.DATE,plants[i].summerFreq)
                }
                plants[i].nextWatering = sdf.format(cal.time).toString()
            }
            Toast.makeText(this,"Vous avez arrosé vos plantes",Toast.LENGTH_LONG).show()
            return@setOnClickListener
        }

        binding.reporter?.setOnClickListener {
            for(i in plants.indices){
                cal.add(Calendar.DATE,1)
                plants[i].nextWatering = sdf.format(cal.time).toString()
            }
            Toast.makeText(this,"Vous avez reporter l'arrosage",Toast.LENGTH_LONG).show()
            return@setOnClickListener
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
