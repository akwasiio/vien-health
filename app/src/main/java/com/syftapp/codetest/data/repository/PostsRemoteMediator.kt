package com.syftapp.codetest.data.repository

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.rxjava2.RxRemoteMediator
import com.syftapp.codetest.data.api.BlogApi
import com.syftapp.codetest.data.dao.PostDao
import com.syftapp.codetest.data.dao.PostKeyDao
import com.syftapp.codetest.data.database.AppDatabase
import com.syftapp.codetest.data.model.domain.Post
import com.syftapp.codetest.data.model.domain.PostKey
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

@OptIn(ExperimentalPagingApi::class)
class PostsRemoteMediator(
    private val blogApi: BlogApi,
    private val appDatabase: AppDatabase,
    private val postDao: PostDao,
    private val postKeyDao: PostKeyDao,
) : RxRemoteMediator<Int, Post>() {

    @OptIn(ExperimentalPagingApi::class)
    override fun loadSingle(
        loadType: LoadType,
        state: PagingState<Int, Post>
    ): Single<MediatorResult> {
        val page: Int = when (loadType) {
            LoadType.REFRESH -> {
                val postKey = getRemoteKeyClosestToCurrentPosition(state)
                Log.e("LoadType", "LoadType is refresh")

                postKey?.nextKey?.minus(1) ?: -1
            }

            LoadType.PREPEND -> {
                val postKey = getRemoteKeyForFirstItem(state)
                Log.e("LoadType", "LoadType is PREPEND")

                val prevKey = postKey?.previousKey ?: return Single.just(
                    MediatorResult.Success(endOfPaginationReached = postKey != null)
                )

                prevKey
            }

            LoadType.APPEND -> {
                val postKey = getPostKeyForLastItem(state)
                Log.e("LoadType", "LoadType is APPEND")

                val nextKey = postKey?.nextKey
                    ?: return Single.just(MediatorResult.Success(endOfPaginationReached = postKey != null))

                nextKey
            }
        }

        return blogApi.getPosts(page).subscribeOn(Schedulers.io()).map { posts ->
            appDatabase.runInTransaction {
                if (loadType == LoadType.REFRESH) {
                    postKeyDao.clearKeys()
                    // clear posts
                    Log.e("is Refresh", "About to clear posts table")
                    postDao.deletePosts()
                }

                val prevKey = if (page == -1) null else page - 1
                val nextKey = if (posts.isEmpty()) null else page + 1

                val keys = posts.map {
                    PostKey(it.id.toLong(), prevKey, nextKey)
                }


                postKeyDao.insertAll(keys)
                postDao.insertAll(posts)
            }

            return@map MediatorResult.Success(posts.isEmpty())
        }
    }

    private fun getPostKeyForLastItem(state: PagingState<Int, Post>): PostKey? {
        return state.pages.lastOrNull() { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { post ->
            postKeyDao.postKeysById(post.id.toLong())
        }
    }

    private fun getRemoteKeyForFirstItem(state: PagingState<Int, Post>): PostKey? {
        // Get the first page that was retrieved, that contained items.
        // From that first page, get the first item
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { post ->
                // Get the remote keys of the first items retrieved
                postKeyDao.postKeysById(post.id.toLong())
            }
    }

    private fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, Post>
    ): PostKey? {
        // The paging library is trying to load data after the anchor position
        // Get the item closest to the anchor position
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { postId ->
                postKeyDao.postKeysById(postId.toLong())
            }
        }
    }
}