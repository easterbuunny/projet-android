package fr.uparis.forgetmenot

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Plant (
        var name : String?,                      // Nom
        var latinName : String?,                 // Nom Latin, au moins un des noms doit être non-vide.

        var imageLink : String,                  // Lien vers une image de la fleur

        var summerFreq : Int,                    // Fréquence d'arrosage d'été au mois
        var winterFreq : Int?,                   // Fréquence d'arrosage d'hiver au mois, au moins une des fréquences doit être non vide.

        var lastWatering : String,               // Dernier Arrosage
        var nextWatering : String                // Prochain Arrosage
){
        @PrimaryKey(autoGenerate = true)
        var id : Long? = null
    // Clé primaire, identifiant de la fleur.
}