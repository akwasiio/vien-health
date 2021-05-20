package com.syftapp.codetest.data.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.syftapp.codetest.data.model.domain.Post
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface PostDao {

    @Query("SELECT * FROM post WHERE id = :postId")
    fun get(postId: Int): Maybe<Post>

    @Query("SELECT * FROM post")
    fun getAll(): Single<List<Post>>

    @Query("SELECT * FROM post")
    fun getPostsPaged(): PagingSource<Int, Post>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(posts: List<Post>)

    @Query("DELETE FROM post")
    fun deletePosts()
}