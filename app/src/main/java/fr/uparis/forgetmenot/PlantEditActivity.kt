package fr.uparis.forgetmenot

import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import fr.uparis.forgetmenot.databinding.ActivityPlantEditBinding
import java.time.LocalDate
import java.util.*

class PlantEditActivity : AppCompatActivity() {

    private lateinit var binding : ActivityPlantEditBinding
    lateinit var uri : String

    // Récupère l'URI d'une image puis l'affiche dans l'image view.
    val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        binding.btnAddPhoto.setImageURI(uri)
        this.uri = uri.toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlantEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        val threadDb = Thread {
            plant = db.plantDao().loadPlant(id)
        }
        threadDb.start()
        threadDb.join()


        // Remplissage des fields avec les attributs actuels de la plante
        binding.newNameCommun.setText(plant?.name)
        binding.newNameLatin.setText(plant?.latinName)
        uri = plant?.imageLink.toString()

        // Ne fonctionne pas pour le moment, le but est d'afficher l'image de l'URI dans binding.btnAddPhoto
        // contentResolver.takePersistableUriPermission(trueUri, 0)
        // binding.btnAddPhoto.setImageURI(trueUri)

        binding.freqEte.setText(plant?.summerFreq.toString())
        if (plant?.winterFreq != null) {
            binding.saisoniere.isChecked = true
            binding.ete.visibility = View.VISIBLE
            binding.hiver.visibility = View.VISIBLE
            binding.freqHiver.visibility = View.VISIBLE
            binding.freqHiver.setText(plant?.winterFreq.toString())
        }

        // Selectionne une photo pour l'ajouter à la plante
        binding.btnAddPhoto.setOnClickListener {
            getContent.launch("image/*")
        }

        binding.addPlant.setOnClickListener {
            val name = binding.newNameCommun.text.toString()
            val latinName = binding.newNameLatin.text.toString()
            val freqSummer = binding.freqEte.text.toString()
            var freqHiver = binding.freqHiver.text.toString()

            // Traitement des noms
            if (name == "" && latinName == "") {
                Toast.makeText(this, "Rentrez au moins un nom.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (name != "" && !name.matches(Regex("[A-Za-z].*"))) {
                Toast.makeText(this, "Veuillez rentrer un nom correct.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (latinName != "" && !latinName.matches(Regex("[A-Za-z].*"))) {
                Toast.makeText(this, "Veuillez rentrer un nom latin correct.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Traitement des fréquences
            if (freqSummer == "") {
                if (binding.saisoniere.isChecked) {
                    Toast.makeText(this, "Veuillez rentrer une fréquence d'été.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Veuillez rentrer une fréquence.", Toast.LENGTH_SHORT).show()
                }
                return@setOnClickListener
            }
            if (binding.saisoniere.isChecked) {
                if (freqHiver == "") {
                    Toast.makeText(this, "Veuillez rentrer une fréquence d'hiver.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            } else {
                freqHiver = ""
            }

            // Traitement de l'URI
            if (uri == "") {
                Toast.makeText(this, "Veuillez sélectioner une photo.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Creation de la Plante pour la BDD
            val n : String?
            val ln : String?
            val wf : Int?

            if (name != "") {
                n = name.replaceFirstChar {
                    if (it.isLowerCase()) {
                        it.titlecase(Locale.getDefault())
                    } else {
                        it.toString()
                    }
                }
            } else {
                n = null
            }
            if (latinName != "") {
                ln = latinName.replaceFirstChar {
                    if (it.isLowerCase()) {
                        it.titlecase(Locale.getDefault())
                    } else {
                        it.toString()
                    }
                }
            } else {
                ln = null
            }
            if (freqHiver != "") {
                wf = freqHiver.toInt()
            } else {
                wf = null
            }

            // Modification de la plante
            plant?.name = n
            plant?.latinName = ln
            plant?.imageLink = uri
            plant?.summerFreq = freqSummer.toInt()
            plant?.winterFreq = wf
            plant?.lastWatering = LocalDate.now().toString()
            plant?.nextWatering = LocalDate.now().toString()

            // TODO: changer la valeur de NextWatering, quand l'Alarm sera implémentée

            // Mise à jour de la plante dans la base de données
            Thread {
                PlantDB.getDatabase(this).plantDao().updatePlants(plant!!)
            }.start()

            Toast.makeText(this, "Votre plante a bien été mise à jour.", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.saisoniere.setOnClickListener {
            if (binding.saisoniere.isChecked) {
                binding.ete.visibility = View.VISIBLE
                binding.hiver.visibility = View.VISIBLE
                binding.freqHiver.visibility = View.VISIBLE
            } else {
                binding.ete.visibility = View.INVISIBLE
                binding.hiver.visibility = View.INVISIBLE
                binding.freqHiver.visibility = View.INVISIBLE
            }
        }

    }
}