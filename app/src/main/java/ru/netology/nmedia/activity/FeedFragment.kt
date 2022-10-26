package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostViewHolder.Companion.textArg
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.util.parseException

@AndroidEntryPoint
class FeedFragment : Fragment() {

    private val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        val adapter = PostsAdapter(object : OnInteractionListener {
            override fun onEdit(post: Post) {
                viewModel.edit(post)
            }

            override fun onLike(post: Post) {
                if (post.likedByMe)
                    viewModel.dislikeById(post)
                else
                    viewModel.likeById(post)
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }

                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }

            override fun onImage(post: Post) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_imageFragment,
                    Bundle().apply {
                        textArg = post.attachment?.url
                    }
                )
            }

            override fun onUnauthorized(post: Post) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_authentificationFragment
                )
            }

        })

        binding.list.adapter = adapter

        viewModel.data.observe(viewLifecycleOwner) { state ->
            val newPosts = adapter.currentList.size < state.posts.size
            adapter.submitList(state.posts) {
                if (newPosts) {
                    binding.list.smoothScrollToPosition(0)
                }
            }
            binding.emptyText.isVisible = state.empty
        }

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            binding.errorGroup.isVisible = state.error
//            binding.swipeRefresh.isVisible = state.refreshing
        }

        viewModel.error.observe(viewLifecycleOwner) {
            val errorMessage = parseException(it)
            Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_LONG)
                .setAction(R.string.retry_loading) {
                    viewModel.loadPosts()
                }
                .show()
        }

        binding.retryButton.setOnClickListener {
            viewModel.loadPosts()
        }

        viewModel.newerCount.observe(viewLifecycleOwner) { state ->
            binding.newPosts.isVisible = state != 0
        }

        binding.newPosts.setOnClickListener {
            viewModel.refreshPosts()
            binding.newPosts.isVisible = false
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.swipeRefresh()
            binding.swipeRefresh.isRefreshing = false
        }

        binding.fab.setOnClickListener {
            if (viewModel.isAuthorized)
                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
            else
                findNavController().navigate(R.id.action_feedFragment_to_authentificationFragment)
        }

        return binding.root
    }

}
