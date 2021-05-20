package com.syftapp.codetest.posts

import androidx.paging.PagingData
import com.syftapp.codetest.data.model.domain.Post

sealed class PostScreenState {
    class DataAvailable(val posts: PagingData<Post>) : PostScreenState()
    class PostSelected(val post: Post) : PostScreenState()
}