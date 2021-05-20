package com.syftapp.codetest.posts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.paging.rxjava2.RxPagingSource
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.syftapp.codetest.data.model.domain.Post
import com.syftapp.codetest.databinding.ViewPostListItemBinding

class PostsAdapter(
    private val presenter: PostsPresenter
) : PagingDataAdapter<Post, PostViewHolder>(POSTS_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ViewPostListItemBinding.inflate(layoutInflater, parent, false)

        return PostViewHolder(binding, presenter)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }

    companion object {
        private val POSTS_COMPARATOR = object : DiffUtil.ItemCallback<Post>() {
            override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
                return oldItem.title == newItem.title && oldItem.body == newItem.body
            }

        }
    }
}

class PostViewHolder(
    private val itemBinding: ViewPostListItemBinding,
    private val presenter: PostsPresenter
) : RecyclerView.ViewHolder(itemBinding.root) {

    fun bind(item: Post) {
        itemBinding.postTitle.text = item.title
        itemBinding.bodyPreview.text = item.body
        itemBinding.root.setOnClickListener { presenter.showDetails(item) }
    }

}