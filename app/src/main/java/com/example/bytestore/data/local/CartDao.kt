package com.example.bytestore.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import androidx.room.Delete
@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items ORDER BY id DESC")
    fun observeCart(): LiveData<List<CartEntity>>

    @Query("SELECT COALESCE(SUM(quantity),0) FROM cart_items")
    fun observeCount(): LiveData<Int>

    @Query("SELECT COALESCE(SUM(unitPriceCents * quantity),0) FROM cart_items")
    fun observeSubtotalCents(): LiveData<Long>

    @Query("SELECT * FROM cart_items WHERE productId = :productId LIMIT 1")
    suspend fun findByProduct(productId: Long): CartEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CartEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(item: CartEntity): Long

    @Update suspend fun update(item: CartEntity)
    @Delete suspend fun delete(item: CartEntity)

    @Query("DELETE FROM cart_items") suspend fun clear()
    @Query("SELECT * FROM cart_items") suspend fun getAll(): List<CartEntity>
}
