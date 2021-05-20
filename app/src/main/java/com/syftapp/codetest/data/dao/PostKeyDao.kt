package com.syftapp.codetest.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.syftapp.codetest.data.model.domain.PostKey
import io.reactivex.Single

@Dao
interface PostKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(postKey: List<PostKey>)

    @Query("DELETE FROM post_keys")
    fun clearKeys()

    @Query("SELECT * FROM post_keys WHERE id = :postKeyId")
    fun postKeysById(postKeyId: Long): PostKey

}