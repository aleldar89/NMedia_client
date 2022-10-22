package ru.netology.nmedia.activity

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth


class MyDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle(R.string.are_you_sure)
                .setPositiveButton(R.string.logout) { _, _ ->
                    AppAuth.getInstance().clearAuth()
                    findNavController().navigate(R.id.feedFragment)
                }
                .setNegativeButton(R.string.cancel) { _, _ ->
                    findNavController().navigateUp()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}