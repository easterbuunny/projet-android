package fr.uparis.forgetmenot

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import fr.uparis.forgetmenot.databinding.ItemLayoutBinding
import java.time.LocalDate

class RecAdapter : RecyclerView.Adapter<RecAdapter.VH>() {

    private lateinit var plants : ArrayList<Plant>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: VH, position: Int) {
        if (plants[position].name != null) {
            if (plants[position].latinName != null) {
                holder.binding.plantName.text = plants[position].name + " - " + plants[position].latinName
            } else {
                holder.binding.plantName.text = plants[position].name
            }
        } else {
            holder.binding.plantName.text = plants[position].latinName
        }

        val lastWatering = LocalDate.parse(plants[position].lastWatering)
        holder.binding.lastDate.text = "${lastWatering.dayOfMonth}/${lastWatering.monthValue}/${lastWatering.year}"

        val nextWatering = LocalDate.parse(plants[position].nextWatering)
        holder.binding.nextDate.text = "${nextWatering.dayOfMonth}/${nextWatering.monthValue}/${nextWatering.year}"



        val uri = Uri.parse(plants[position].imageLink)
        holder.binding.plantPhoto.setImageURI(uri)

        holder.binding.item.setOnClickListener {
            val context = holder.binding.item.context
            val intent = Intent(context, PlantActivity::class.java)
            val b = Bundle()
            b.putInt("id", plants[position].id!!.toInt())
            intent.putExtras(b)
            (context as Activity).finish()
            context.startActivity(intent)
        }


    }

    override fun getItemCount(): Int {
        return plants.size
    }

    fun setListePlants(list: Array<Plant>) {
        plants = ArrayList()
        plants.addAll(list)
    }

    class VH(val binding: ItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)
}