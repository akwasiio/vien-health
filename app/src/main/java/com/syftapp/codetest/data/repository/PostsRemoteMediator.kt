package com.syftapp.codetest.data.repository

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.rxjava2.RxRemoteMediator
import com.syftapp.codetest.data.api.BlogApi
import com.syftapp.codetest.data.dao.PostDao
import com.syftapp.codetest.data.database.AppDatabase
import com.syftapp.codetest.data.model.domain.Post
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

@OptIn(ExperimentalPagingApi::class)
class PostsRemoteMediator(
    private val blogApi: BlogApi,
    private val appDatabase: AppDatabase,
    private val postDao: PostDao
) : RxRemoteMediator<Int, Post>() {

    @OptIn(ExperimentalPagingApi::class)
    override fun loadSingle(
        loadType: LoadType,
        state: PagingState<Int, Post>
    ): Single<MediatorResult> {
        var numberOfPagesLoaded = 1

        when (loadType) {
            LoadType.REFRESH -> {
            }

            LoadType.PREPEND -> {
                return Single.just(MediatorResult.Success(true))
            }

            LoadType.APPEND -> {
                val lastPost =
                    state.lastItemOrNull() ?: return Single.just(MediatorResult.Success(true))

                numberOfPagesLoaded += 1
            }
        }

        return blogApi.getPosts().subscribeOn(Schedulers.io()).map { posts ->
            appDatabase.runInTransaction {
                if (loadType == LoadType.REFRESH) {
                    // clear posts
                    Log.e("is Refresh", "About to clear posts table")
                    postDao.deletePosts()
                }

                postDao.insertAll(posts)
            }

            return@map MediatorResult.Success(posts.isEmpty())
        }
    }
}