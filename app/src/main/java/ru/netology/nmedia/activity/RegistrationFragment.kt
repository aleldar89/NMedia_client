package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.coroutineScope
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentRegistrationBinding
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
                Toast.makeText(
                    context,
                    context?.getString(R.string.error_empty_registration_form),
                    Toast.LENGTH_SHORT
                ).show()
            return@setOnClickListener
            }

            val auth = viewModel.updateUser(login, pass)

            if (auth != null) {
                viewModel.saveToken(auth.token, auth.id)
            } else {
                Toast.makeText(
                    context,
                    context?.getString(R.string.error_registration),
                    Toast.LENGTH_SHORT
                ).show()
            }

            findNavController().navigateUp()
        }

        return binding.root
    }
}