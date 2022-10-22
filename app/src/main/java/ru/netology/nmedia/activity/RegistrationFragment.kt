package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentRegistrationBinding
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.parseException
import ru.netology.nmedia.view.createToast
import ru.netology.nmedia.viewmodel.RegistrationViewModel

class RegistrationFragment : Fragment() {

    private val viewModel: RegistrationViewModel by viewModels()

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
            val errorMessage = parseException(error) + " .Registration failed, try later"
            Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_LONG).show()
            findNavController().navigateUp()
        }

        binding.enter.setOnClickListener {
            val name = binding.name.text.toString()
            val login = binding.login.text.toString()
            val pass = binding.password.text.toString()
            val confirmPass = binding.confirmPassword.text.toString()

            if (login.isBlank() || pass.isBlank() || name.isBlank()) {
                it.createToast(R.string.error_empty_registration_form)
                return@setOnClickListener
            } else if (pass != confirmPass) {
                it.createToast(R.string.error_confirm_password)
                return@setOnClickListener
            } else {
                viewModel.updateUser(login, pass, name)
            }

            AndroidUtils.hideKeyboard(requireView())
        }

        return binding.root
    }
}