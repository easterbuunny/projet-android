package fr.uparis.forgetmenot


import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import fr.uparis.forgetmenot.databinding.ActivityAddPlantBinding
import java.io.File
import java.time.LocalDate
import java.util.*

class AddPlantActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddPlantBinding
    private var localUri : Uri? = null
    private lateinit var link : String


    // Récupère l'URI d'une image puis l'affiche dans l'image view.
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        //Action si uri est null
        if(uri ==null) {
            return@registerForActivityResult
        }

        //recupere l'image l'image de uri
        val inputStream = contentResolver.openInputStream(uri)

        //Creer un ficher local pour sotcker l'image localement
        val fileNamePrefex = "plante"
        val preferences = getSharedPreferences("numImage", Context.MODE_PRIVATE)
        val numImage = preferences.getInt("numImage",1)
        val fileName = "$fileNamePrefex$numImage"

        //Ouvrir l'acces vers le fichier local
        val file = File(this.filesDir,fileName)
        val outputStream = file.outputStream()
        //Sauvegarder avec un compteur
        preferences.edit().putInt("numImage",numImage+1).commit()
        //Copier l'image uri et le copie dans le fichier local
        inputStream?.copyTo(outputStream)

        //Memoriser ri de fichier local dans propriété localUri
        localUri=file.toUri()
        outputStream.close()
        inputStream?.close()

        //afficher l'image a l'emplacement
        binding.btnAddPhoto.setImageURI(localUri)
        this.link=localUri.toString()
   }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPlantBinding.inflate(layoutInflater)
        setContentView(binding.root)

        link = ""

        // Selectionne une photo pour l'ajouter à la plante
        binding.btnAddPhoto.setOnClickListener {
            getContent.launch("image/*")
        }


        // Récuperation des données rentrées dans les fields
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
                Toast.makeText(this, "Veuillez rentrer un nom latin correct.", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            // Traitement des fréquences
            if (freqSummer == "") {
                if (binding.saisoniere.isChecked) {
                    Toast.makeText(
                        this,
                        "Veuillez rentrer une fréquence d'été.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(this, "Veuillez rentrer une fréquence.", Toast.LENGTH_SHORT)
                        .show()
                }
                return@setOnClickListener
            } else if (freqSummer.toInt() !in 1..31) {
                Toast.makeText(this, "Veuillez rentrer une fréquence correcte.", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            if (binding.saisoniere.isChecked) {
                if (freqHiver == "") {
                    Toast.makeText(
                        this,
                        "Veuillez rentrer une fréquence d'hiver.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                } else if (freqHiver.toInt() !in 1..7) {
                    Toast.makeText(
                        this,
                        "Veuillez rentrer une fréquence correcte.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
            } else {
                freqHiver = ""
            }

            // Traitement de l'URI
            if (link == "") {
                Toast.makeText(this, "Veuillez sélectioner une photo.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Creation de la Plante pour la BDD

            val n: String? = if (name != "") {
                name.replaceFirstChar {
                    if (it.isLowerCase()) {
                        it.titlecase(Locale.getDefault())
                    } else {
                        it.toString()
                    }
                }
            } else {
                null
            }
            val ln: String? = if (latinName != "") {
                latinName.replaceFirstChar {
                    if (it.isLowerCase()) {
                        it.titlecase(Locale.getDefault())
                    } else {
                        it.toString()
                    }
                }
            } else {
                null
            }
            val wf: Int? = if (freqHiver != "") {
                freqHiver.toInt()
            } else {
                null
            }

            // Calcul de la prochaine date d'arrosage
            val actualMonth = LocalDate.now().monthValue
            var freqDays = 30
            freqDays /= if (wf != null) {
                // On considère que l'été c'est de Mars à Aout inclus...
                if (actualMonth in 4..10) {
                    freqSummer.toInt()
                } else {
                    wf
                }
            } else {
                freqSummer.toInt()
            }

            Log.d("tototest", "freqDays : $freqDays")

            // TODO: changer la valeur de NextWatering, quand l'Alarm sera implémentée
            Thread {
                PlantDB.getDatabase(this).plantDao().insertPlants(
                    Plant(
                        n,
                        ln,
                        link,
                        freqSummer.toInt(),
                        wf,
                        LocalDate.now().toString(),
                        LocalDate.now().plusDays(freqDays.toLong()).toString()
                    )
                )
            }.start()

            Toast.makeText(this, "Votre plante a bien été ajoutée.", Toast.LENGTH_SHORT).show()
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