package com.syftapp.codetest.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.rxjava2.RxRemoteMediator
import com.syftapp.codetest.data.api.BlogApi
import com.syftapp.codetest.data.database.AppDatabase
import com.syftapp.codetest.data.model.domain.Post
import com.syftapp.codetest.data.model.domain.PostKey
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.InvalidObjectException

@OptIn(ExperimentalPagingApi::class)
class PostsRemoteMediator(
    private val blogApi: BlogApi,
    private val appDatabase: AppDatabase,
) : RxRemoteMediator<Int, Post>() {

    @OptIn(ExperimentalPagingApi::class)
    override fun loadSingle(
        loadType: LoadType,
        state: PagingState<Int, Post>
    ): Single<MediatorResult> {
        return Single.just(loadType)
            .subscribeOn(Schedulers.io())
            .map {
                when (it) {
                    LoadType.REFRESH -> {
                        val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)

                        remoteKeys?.nextKey?.minus(1) ?: 1
                    }
                    LoadType.PREPEND -> {
                        val remoteKeys = getRemoteKeyForFirstItem(state)
                            ?: throw InvalidObjectException("Result is empty")

                        remoteKeys.previousKey ?: -1
                    }
                    LoadType.APPEND -> {
                        val remoteKeys = getPostKeyForLastItem(state)
                            ?: throw InvalidObjectException("Result is empty")

                        remoteKeys.nextKey ?: -1
                    }
                }
            }.flatMap { page ->
                if(page == -1) {
                    Single.just(MediatorResult.Success(endOfPaginationReached = true))
                } else {
                    blogApi.getPosts(page).subscribeOn(Schedulers.io()).map { posts ->
                        appDatabase.runInTransaction {
                            if (loadType == LoadType.REFRESH) {
                                appDatabase.postKeyDao().clearKeys()
                                // clear posts
                                appDatabase.postDao().deletePosts()
                            }

                            val prevKey = if (page == 1) null else page - 1
                            val nextKey = if (posts.isEmpty()) null else page + 1

                            val keys = posts.map {
                                PostKey(it.id.toLong(), prevKey, nextKey)
                            }


                            appDatabase.postKeyDao().insertAll(keys)
                            appDatabase.postDao().insertAll(posts)
                        }

                        return@map MediatorResult.Success(posts.isEmpty())
                    }
                }
            }
    }

    private fun getPostKeyForLastItem(state: PagingState<Int, Post>): PostKey? {
        return state.pages.lastOrNull() { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { post ->
            appDatabase.postKeyDao().postKeysById(post.id.toLong())
        }
    }

    private fun getRemoteKeyForFirstItem(state: PagingState<Int, Post>): PostKey? {
        // Get the first page that was retrieved, that contained items.
        // From that first page, get the first item
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { post ->
                // Get the remote keys of the first items retrieved
                appDatabase.postKeyDao().postKeysById(post.id.toLong())
            }
    }

    private fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, Post>
    ): PostKey? {
        // The paging library is trying to load data after the anchor position
        // Get the item closest to the anchor position
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { postId ->
                return appDatabase.postKeyDao().postKeysById(postId.toLong())
            }
        }
    }
}