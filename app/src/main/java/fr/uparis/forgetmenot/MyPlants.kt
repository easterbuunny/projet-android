package fr.uparis.forgetmenot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import fr.uparis.forgetmenot.databinding.ActivityMyPlantsBinding


class MyPlants : AppCompatActivity() {
    lateinit var binding : ActivityMyPlantsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyPlantsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar
        setSupportActionBar(binding.myToolbar)

        // Affichage du logo de retour en arrière
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Affichage du titre de l'activité dans la toolbar
        supportActionBar?.title = "Recherche d'un Plante"

        // Lecture de la base de données et création du RecyclerView
        val db = PlantDB.getDatabase(this)
        val adapter = RecAdapter()
        var plants = emptyArray<Plant>()

        val threadDb = Thread {
                plants = db.plantDao().loadAll()
        }
        threadDb.start()
        threadDb.join()
        adapter.setListePlants(plants)

        binding.myRecyclerview.hasFixedSize()
        binding.myRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.myRecyclerview.adapter = adapter
    }


    // Création de la Toolbar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater : MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_research, menu)

        val searchItem  = menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView

        // Recherche d'une plante
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {return true}
            // Mise à jour de la liste des plantes affichées en fonction de la barre de recherche
            override fun onQueryTextChange(s: String): Boolean {
                val db = PlantDB.getDatabase(applicationContext)
                val adapter = RecAdapter()
                var plants = emptyArray<Plant>()

                val threadDb = Thread {
                    plants = db.plantDao().loadPartialName(s)
                }
                threadDb.start()
                threadDb.join()
                adapter.setListePlants(plants)
                binding.myRecyclerview.swapAdapter(adapter, false)
                return true
            }
        })
        return true
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