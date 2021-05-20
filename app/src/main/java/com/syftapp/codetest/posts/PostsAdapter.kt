package com.syftapp.codetest.posts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.syftapp.codetest.data.model.domain.Post
import com.syftapp.codetest.databinding.ViewPostListItemBinding

class PostsAdapter(
    private val data: List<Post>,
    private val presenter: PostsPresenter
) : RecyclerView.Adapter<PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ViewPostListItemBinding.inflate(layoutInflater, parent, false)

        return PostViewHolder(binding, presenter)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(data[position])
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