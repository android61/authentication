package ru.netology.nmedia.activity

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.net.toFile
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentSignUpBinding
import ru.netology.nmedia.error.ApiException
import ru.netology.nmedia.viewmodel.AuthViewModel


class SignUpFragment : DialogFragment() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSignUpBinding.inflate(inflater, container, false)

        binding.avatar.visibility = View.INVISIBLE

        val selectAvatarContract =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    ImagePicker.RESULT_ERROR -> Snackbar.make(
                        binding.root,
                        ImagePicker.getError(it.data),
                        Snackbar.LENGTH_LONG
                    ).show()
                    Activity.RESULT_OK -> {
                        val uri: Uri? = it.data?.data
                        authViewModel.changePhoto(uri, uri?.toFile())
                    }
                }
            }

        binding.uploadAvatar.setOnClickListener {
            ImagePicker.with(this)
                .galleryOnly()
                .crop()
                .compress(2048)
                .provider(ImageProvider.GALLERY)
                .galleryMimeTypes(
                    arrayOf(
                        "image/png",
                        "image/jpeg",
                    )
                )
                .createIntent(selectAvatarContract::launch)
        }

        authViewModel.photo.observe(viewLifecycleOwner) {
            binding.avatar.setImageURI(it.uri)
            binding.avatar.isVisible = it.uri != null
        }


        authViewModel.error.observe(viewLifecycleOwner) {
            when (it) {
                is ApiException -> Toast.makeText(
                    context,
                    R.string.error_loading,
                    Toast.LENGTH_LONG
                ).show()

                else -> Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
            }
        }


        authViewModel.state.observe(viewLifecycleOwner) {
            if (it != null) {
                dismiss()
            }
        }

        binding.registerButton.setOnClickListener {
            if (binding.username.text.isBlank() || binding.login.text.isBlank() ||
                binding.password.text.isBlank() || binding.repeatPassword.text.isBlank()
            ) {
                Toast.makeText(context, R.string.error_blank_fields, Toast.LENGTH_LONG).show()
            } else if (binding.password.text.toString() != binding.repeatPassword.text.toString()) {
                Toast.makeText(context, R.string.error_passwords, Toast.LENGTH_LONG).show()
            } else {
                when (authViewModel.photo.value) {
                    null -> authViewModel.registerUser(
                        binding.login.text.toString(),
                        binding.password.text.toString(),
                        binding.username.text.toString()
                    )
                    else -> authViewModel.photo.value?.file?.let { file ->
                        authViewModel.registerWithPhoto(
                            binding.login.text.toString(),
                            binding.password.text.toString(),
                            binding.username.text.toString(),
                            file
                        )
                    }
                }
            }
        }
        return binding.root
    }
}