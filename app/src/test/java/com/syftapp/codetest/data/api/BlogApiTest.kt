package com.syftapp.codetest.data.api

import com.syftapp.codetest.data.model.domain.Comment
import com.syftapp.codetest.data.model.domain.Post
import com.syftapp.codetest.data.model.domain.User
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Single
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class BlogApiTest {

    private val blogService = mockk<BlogService>()
    private val sut = BlogApi(blogService)

    @Test
    fun `get users contains correct domain models`() {
        every { blogService.getUsers() } returns StubBlogService().getUsers()

        val apiUser = rxValue(blogService.getUsers())[0]
        val users = rxValue(sut.getUsers())

        assertThat(users)
            .hasSize(2)
            .contains(
                User(
                    id = apiUser.id,
                    name = apiUser.name,
                    username = apiUser.username,
                    email = apiUser.email
                )
            )
    }

    @Test
    fun `get posts contains correct domain models`() {
        every { blogService.getPosts(1, 5) } returns StubBlogService().getPosts(1, 5)
        val apiPost = rxValue(blogService.getPosts(1))[0]
        val posts = rxValue(sut.getPosts(1))

        assertThat(posts)
            .hasSize(5)
            .contains(
                Post(
                    id = apiPost.id,
                    userId = apiPost.userId,
                    title = apiPost.title,
                    body = apiPost.body
                )
            )
    }

    @Test
    fun `get comments contains correct domain models`() {
        every { blogService.getComments() } returns StubBlogService().getComments()

        val apiComment = rxValue(blogService.getComments())[0]
        val comments = rxValue(sut.getComments())

        assertThat(comments)
            .hasSize(3)
            .contains(
                Comment(
                    id = apiComment.id,
                    postId = apiComment.postId,
                    name = apiComment.name,
                    email = apiComment.email,
                    body = apiComment.body
                )
            )
    }

    private fun <T> rxValue(apiItem: Single<T>): T = apiItem.test().values()[0]
}