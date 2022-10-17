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
import ru.netology.nmedia.view.createToast
import ru.netology.nmedia.viewmodel.RegistrationViewModel

class RegistrationFragment : Fragment() {

    private val viewModel: RegistrationViewModel by viewModels(ownerProducer = ::requireParentFragment)

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

        binding.enter.setOnClickListener {
            val login = binding.login.text.toString()
            val pass = binding.password.text.toString()

            if (login.isBlank() || pass.isBlank()) {
                it.createToast(R.string.error_empty_registration_form)
                return@setOnClickListener
            } else {
                viewModel.updateUser(login, pass)
            }

            viewModel.responseAuthState.observe(viewLifecycleOwner) { authState ->
                if (authState != null) {
                    viewModel.saveToken(authState.token, authState.id)
                } else {
                    view?.createToast(R.string.error_registration)
                }
            }

            findNavController().navigateUp()
        }

        return binding.root
    }
}