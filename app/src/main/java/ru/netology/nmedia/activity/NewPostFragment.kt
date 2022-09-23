package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.viewmodel.PostViewModel

class NewPostFragment : Fragment() {

    companion object {
        var Bundle.textArg: String? by StringArg
    }

    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewPostBinding.inflate(
            inflater,
            container,
            false
        )

        arguments?.textArg
            ?.let(binding.edit::setText)

        binding.ok.setOnClickListener {
            if (binding.edit.text.isNullOrBlank()) {
                Toast.makeText(
                    context,
                    context?.getString(R.string.error_empty_content),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            viewModel.changeContent(binding.edit.text.toString())
            viewModel.save()
            AndroidUtils.hideKeyboard(requireView())
        }

        viewModel.postCreated.observe(viewLifecycleOwner) {
            viewModel.loadPosts()
            findNavController().navigateUp()
        }

//        viewModel.exceptionMessage.observe(viewLifecycleOwner) {
//            if (it == null) {
//                return@observe
//            }
//
//            Toast.makeText(
//                context,
//                viewModel.exceptionMessage.toString(),
//                Toast.LENGTH_SHORT
//            ).show()
//        }

        return binding.root
    }
}