package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentRegistrationBinding
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.parseException
import ru.netology.nmedia.view.createToast
import ru.netology.nmedia.viewmodel.RegistrationViewModel

class RegistrationFragment : Fragment() {

    companion object {
        private const val MAX_IMAGE_SIZE = 500
    }

    private val viewModel: RegistrationViewModel by viewModels()

    private val imageContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        when (result.resultCode) {
            ImagePicker.RESULT_ERROR -> {
                view?.createToast(R.string.image_error)
            }
            else -> {
                val data = result.data?.data ?: run {
                    view?.createToast(R.string.image_error)
                    return@registerForActivityResult
                }
                viewModel.changePhoto(data, data.toFile())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentRegistrationBinding.inflate(
            inflater,
            container,
            false
        )

        viewModel.responseAuthState.observe(viewLifecycleOwner) { authState ->
            if (authState != null) {
                viewModel.saveToken(authState.token, authState.id)
                findNavController().navigateUp()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            val errorMessage = "${parseException(error)}. Registration failed, try later"
            Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_LONG).show()
            findNavController().navigateUp()
        }

        viewModel.media.observe(viewLifecycleOwner) {
            if (it == null) {
                binding.avatar.isGone = true
                return@observe
            }

            binding.avatar.isVisible = true
            binding.avatar.setImageURI(it.uri)
        }

        binding.enter.setOnClickListener {
            val name = binding.name.text.toString()
            val login = binding.login.text.toString()
            val pass = binding.password.text.toString()
            val confirmPass = binding.confirmPassword.text.toString()
            val file = viewModel.media.value?.file

            if (login.isBlank() || pass.isBlank() || name.isBlank()) {
                it.createToast(R.string.error_empty_registration_form)
                return@setOnClickListener
            } else if (pass != confirmPass) {
                it.createToast(R.string.error_confirm_password)
                return@setOnClickListener
            } else {
                if (file == null)
                    viewModel.registerUser(login, pass, name)
                else
                    viewModel.registerWithPhoto(login, pass, name, file)
            }

            AndroidUtils.hideKeyboard(requireView())
        }

        binding.takePhoto.setOnClickListener {
            ImagePicker.Builder(this)
                .crop()
                .cameraOnly()
                .maxResultSize(MAX_IMAGE_SIZE, MAX_IMAGE_SIZE)
                .createIntent(imageContract::launch)
        }

        return binding.root
    }
}