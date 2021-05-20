package com.syftapp.codetest.data.repository

import androidx.paging.PagingData
import com.syftapp.codetest.data.api.BlogApi
import com.syftapp.codetest.data.dao.CommentDao
import com.syftapp.codetest.data.dao.PostDao
import com.syftapp.codetest.data.dao.PostKeyDao
import com.syftapp.codetest.data.dao.UserDao
import com.syftapp.codetest.data.database.AppDatabase
import com.syftapp.codetest.data.model.domain.Post
import com.syftapp.codetest.data.model.domain.User
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import io.reactivex.Single
import org.junit.Before
import org.junit.Test

class BlogRepositoryTest {

    @RelaxedMockK
    lateinit var postDao: PostDao
    @RelaxedMockK
    lateinit var commentDao: CommentDao

    @RelaxedMockK
    lateinit var userDao: UserDao

    @RelaxedMockK
    lateinit var postKeyDao: PostKeyDao

    @RelaxedMockK
    lateinit var appDatabase: AppDatabase

    @MockK
    lateinit var blogApi: BlogApi

    private val sut by lazy {
        BlogRepository(postDao, commentDao, userDao, postKeyDao, blogApi, appDatabase)
    }

    private val anyUser = User(1, "name", "username", "email")
    private val anyPost = Post(1, 1, "title", "body")



    @Before
    fun setup() = MockKAnnotations.init(this)

    @Test
    fun `get users returns cached values if available`() {
        every { userDao.getAll() } returns Single.just(listOf(anyUser))

        val observer = sut.getUsers().test()
        observer.assertValue(listOf(anyUser))
        verify(exactly = 0) { blogApi.getUsers() }
    }

    @Test
    fun `users fetched from api are inserted in to the cache`() {
        every { userDao.getAll() } returns Single.just(listOf())
        every { blogApi.getUsers() } returns Single.just(listOf(anyUser))

        sut.getUsers().test()

        verify {
            blogApi.getUsers()
            userDao.insertAll(*listOf(anyUser).toTypedArray())
        }
    }



    // These tests are failing because the implementation of blogRepository.getPosts()
    // changed to use RxRemoteMediator for pagination.
    @Test
    fun `get posts returns cached values if available`() {
        every { postDao.getAll() } returns Single.just(listOf(anyPost))

        val observer = sut.getPosts().test()
        observer.assertValue(PagingData.from(listOf(anyPost)))
        verify(exactly = 0) { blogApi.getPosts(1) }
    }

    @Test
    fun `posts value fetched from api is inserted to the cache`() {
        every { postDao.getAll() } returns Single.just(listOf())
        every { blogApi.getPosts(1) } returns Single.just(listOf(anyPost))

        sut.getPosts().test()

        verify {
            blogApi.getPosts(1)
            postDao.insertAll(listOf(anyPost))
        }
    }

    @Test
    fun `value from api is returned to caller`() {
        every { userDao.getAll() } returns Single.just(listOf())
        every { postDao.getAll() } returns Single.just(listOf())
        every { blogApi.getPosts(1) } returns Single.just(listOf(anyPost))
        every { blogApi.getUsers() } returns Single.just(listOf(anyUser))

        val postObserver = sut.getPosts().test()
        val userObserver = sut.getUsers().test()

        postObserver.assertValue(PagingData.from(listOf(anyPost)))
        userObserver.assertValue(listOf(anyUser))
    }

    @Test
    fun `api failing returns reactive error on chain`() {
        every { postDao.getAll() } returns Single.just(listOf())
        val error = Throwable()
        every { blogApi.getPosts(1) } throws error

        val observer = sut.getPosts().test()

        observer.assertError(error)
    }
}