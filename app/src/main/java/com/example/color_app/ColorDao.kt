package com.example.color_app

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ColorDao {
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insert(color: ColorEntity)

    @Query("SELECT * FROM color_table WHERE hexColor = :hexColor LIMIT 1")
    suspend fun getColorByHex(hexColor: String): ColorEntity?

//    @Insert(onConflict = OnConflictStrategy.IGNORE)
//    suspend fun insert(color: ColorEntity)

    @Query("SELECT * FROM color_table")
    suspend fun getAllColors(): List<ColorEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(colors: List<ColorEntity>)

    @Query("DELETE FROM color_table")
    fun clearTable()
}
