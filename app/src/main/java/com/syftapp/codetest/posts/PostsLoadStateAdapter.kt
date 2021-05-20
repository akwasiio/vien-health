package com.syftapp.codetest.posts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.syftapp.codetest.databinding.PostsLoadStateFooterViewBinding

class PostsLoadStateAdapter: LoadStateAdapter<PostsLoadStateViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): PostsLoadStateViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = PostsLoadStateFooterViewBinding.inflate(layoutInflater, parent, false)

        return PostsLoadStateViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostsLoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }
}

class PostsLoadStateViewHolder(private val binding: PostsLoadStateFooterViewBinding): RecyclerView.ViewHolder(binding.root) {
    fun bind(loadState: LoadState) {
        binding.progressBar.visibility = if(loadState is LoadState.Loading) View.VISIBLE else View.GONE

    }
}