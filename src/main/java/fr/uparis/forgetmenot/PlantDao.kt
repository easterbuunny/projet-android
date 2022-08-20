package fr.uparis.forgetmenot

import androidx.room.*

@Dao
interface PlantDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertPlants(vararg plant: Plant) : List<Long>

    @Delete
    fun deletePlants(vararg plant: Plant) : Int

    @Update
    fun updatePlants(vararg plants: Plant): Int

    @Query("SELECT * FROM Plant")
    fun loadAll(): Array<Plant>

    @Query("SELECT * FROM Plant WHERE name like :nom || '%' OR latinName like :nom || '%'")
    fun loadPartialName(nom : String): Array<Plant>

    @Query("SELECT * FROM Plant WHERE id = :id")
    fun loadPlant(id : Int): Plant

}
