package com.syftapp.codetest.postdetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.syftapp.codetest.R
import com.syftapp.codetest.data.model.domain.Post
import com.syftapp.codetest.databinding.ActivityPostDetailsBinding
import org.koin.android.ext.android.inject
import org.koin.core.KoinComponent

class PostDetailActivity : AppCompatActivity(), PostDetailView, KoinComponent {

    private val presenter: PostDetailPresenter by inject()
    private lateinit var binding: ActivityPostDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailsBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val postId = intent?.extras?.getInt(ARG_POST_ID, -1)
        if (postId == null || postId == -1) {
            render(PostDetailScreenState.Error(Throwable("no post Id")))
        } else {
            presenter.bind(this, postId)
        }
    }

    override fun onDestroy() {
        presenter.unbind()
        super.onDestroy()
    }

    override fun render(state: PostDetailScreenState) {
        when (state) {
            is PostDetailScreenState.Loading -> showLoading()
            is PostDetailScreenState.Error -> {
                // a fairly crude error, and boot them back to the list. The UX could be nicer here
                Toast.makeText(this, R.string.display_post_error_message, Toast.LENGTH_SHORT).show()
                finish()
            }
            is PostDetailScreenState.DataAvailable -> showPost(state.post)
            is PostDetailScreenState.FinishedLoading -> hideLoading()
        }
    }

    private fun hideLoading() {
        binding.loading.visibility = View.GONE
    }

    private fun showPost(post: Post) {
        with(View.VISIBLE) {
            binding.postTitle.visibility = this
            binding.postBody.visibility = this

            // TODO add comments
        }

        binding.postTitle.text = post.title
        binding.postBody.text = post.body
    }

    private fun showLoading() {
        with(View.GONE) {
            binding.error.visibility = this

            binding.postTitle.visibility = this
            binding.postBody.visibility = this
        }

        binding.loading.visibility = View.VISIBLE
    }

    companion object {

        const val ARG_POST_ID = "POST_ID"

        fun getActivityIntent(context: Context, postId: Int): Intent {
            return Intent(context, PostDetailActivity::class.java).apply {
                putExtra(ARG_POST_ID, postId)
            }
        }
    }
}