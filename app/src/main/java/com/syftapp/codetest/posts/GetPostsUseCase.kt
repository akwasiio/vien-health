package com.syftapp.codetest.posts

import androidx.paging.PagingData
import com.syftapp.codetest.data.model.domain.Post
import com.syftapp.codetest.data.repository.BlogRepository
import io.reactivex.Flowable
import io.reactivex.Single
import org.koin.core.KoinComponent

class GetPostsUseCase(private val repository: BlogRepository) : KoinComponent {

    fun execute(): Flowable<PagingData<Post>> {
        // users must be available for the blog posts
        return repository.getUsers()
            .ignoreElement()
            .andThen(repository.getPosts())
    }

}