package com.syftapp.codetest.posts

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.syftapp.codetest.Navigation
import com.syftapp.codetest.R
import com.syftapp.codetest.data.model.domain.Post
import com.syftapp.codetest.databinding.ActivityPostsBinding
import org.koin.android.ext.android.inject
import org.koin.core.KoinComponent

class PostsActivity : AppCompatActivity(), PostsView, KoinComponent {

    private val presenter: PostsPresenter by inject()
    private lateinit var navigation: Navigation

    private lateinit var adapter: PostsAdapter
    private lateinit var binding: ActivityPostsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostsBinding.inflate(layoutInflater)

        setContentView(binding.root)
        navigation = Navigation(this)

        binding.listOfPosts.layoutManager = LinearLayoutManager(this)
        val separator = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        binding.listOfPosts.addItemDecoration(separator)

        initAdapter()
        presenter.bind(this)
    }

    override fun onDestroy() {
        presenter.unbind()
        super.onDestroy()
    }

    override fun render(state: PostScreenState) {
        when (state) {
            is PostScreenState.DataAvailable -> showPosts(state.posts)
            is PostScreenState.PostSelected -> navigation.navigateToPostDetail(state.post.id)
        }
    }

    private fun initAdapter() {
        adapter = PostsAdapter(presenter)
        binding.listOfPosts.adapter = adapter.withLoadStateFooter(PostsLoadStateAdapter())


        adapter.addLoadStateListener { loadStates ->
            binding.loading.visibility =
                if (loadStates.refresh is LoadState.Loading && adapter.itemCount == 0) View.VISIBLE else View.GONE

            if (loadStates.refresh is LoadState.Error) {
                showError(getString(R.string.load_posts_error_message))
            }
        }


    }

    private fun showPosts(posts: PagingData<Post>) {
        // this is a fairly crude implementation, if it was Flowable, it would
        // be better to use DiffUtil and consider notifyRangeChanged, notifyItemInserted, etc
        // to preserve animations on the RecyclerView
        adapter.submitData(lifecycle, posts)
        binding.listOfPosts.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        binding.error.visibility = View.VISIBLE
        binding.error.text = message
    }
}
