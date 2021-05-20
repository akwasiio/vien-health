package com.syftapp.codetest.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.rxjava2.flowable
import com.syftapp.codetest.data.api.BlogApi
import com.syftapp.codetest.data.dao.CommentDao
import com.syftapp.codetest.data.dao.PostDao
import com.syftapp.codetest.data.dao.PostKeyDao
import com.syftapp.codetest.data.dao.UserDao
import com.syftapp.codetest.data.database.AppDatabase
import com.syftapp.codetest.data.model.domain.Comment
import com.syftapp.codetest.data.model.domain.Post
import com.syftapp.codetest.data.model.domain.User
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.koin.core.KoinComponent

class BlogRepository(
    private val postDao: PostDao,
    private val commentDao: CommentDao,
    private val userDao: UserDao,
    private val postKeyDao: PostKeyDao,
    private val blogApi: BlogApi,
    private val appDatabase: AppDatabase
) : KoinComponent, BlogProvider {

    override fun getUsers(): Single<List<User>> {
        return fetchData(
            local = { userDao.getAll() },
            remote = { blogApi.getUsers() },
            insert = { value -> userDao.insertAll(*value.toTypedArray()) }
        )
    }

    override fun getComments(): Single<List<Comment>> {
        return fetchData(
            local = { commentDao.getAll() },
            remote = { blogApi.getComments() },
            insert = { value -> commentDao.insertAll(*value.toTypedArray()) }
        )
    }

    @OptIn(ExperimentalPagingApi::class)
    override fun getPosts(): Flowable<PagingData<Post>> {
        return Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false, prefetchDistance = 5),
            remoteMediator = PostsRemoteMediator(blogApi, appDatabase, postDao, postKeyDao)
        ) {
            postDao.getPostsPaged()
        }.flowable.subscribeOn(Schedulers.io())
    }

    fun getPost(postId: Int): Maybe<Post> {
        return postDao.get(postId)
    }

    private fun <T> fetchData(
        local: () -> Single<List<T>>,
        remote: () -> Single<List<T>>,
        insert: (insertValue: List<T>) -> Completable
    ): Single<List<T>> {

        return local.invoke()
            .flatMap {
                if (it.isNotEmpty()) {
                    Single.just(it)
                } else {
                    remote.invoke()
                        .map { value ->
                            insert.invoke(value).subscribe();
                            value
                        }
                }
            }
    }
}
