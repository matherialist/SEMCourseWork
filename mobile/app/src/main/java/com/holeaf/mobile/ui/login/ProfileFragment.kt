package com.holeaf.mobile.ui.login

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.holeaf.mobile.MainApplication
import com.holeaf.mobile.R
import com.holeaf.mobile.Role
import com.holeaf.mobile.data.login.model.LoggedUserInfo
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.cart.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*


fun decodePhoto(photo: String, role: Role?, context: Context): Bitmap {
    return if (!photo.isNullOrBlank()) {
        val bitmap = Base64.decode(photo, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bitmap, 0, bitmap.size)
    } else {
        when {
            role?.mapCourier == true -> BitmapFactory.decodeResource(
                context.resources,
                R.drawable.courier_avatar
            )
            role?.mapClient == true -> BitmapFactory.decodeResource(
                context.resources,
                R.drawable.client_avatar
            )
            else -> BitmapFactory.decodeResource(
                context.resources,
                R.drawable.placeholder
            )
        }
    }

}

class ProfileFragment : Fragment() {

    val profileViewModel: ProfileViewModel by viewModel()
    val loggedUserInfo: LoggedUserInfo by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileViewModel.profileResult.observe(viewLifecycleOwner,
            Observer { profileFormState ->
                if (profileFormState == null) {
                    return@Observer
                }
                if (profileFormState.success != null) {
                    val usernameTextView = view.findViewById<TextView>(R.id.username)
                    val emailTextView = view.findViewById<TextView>(R.id.email)
                    val roleTextView = view.findViewById<TextView>(R.id.role)
                    val avatar = view.findViewById<CircleImageView>(R.id.user_avatar)
                    usernameTextView.setText(profileFormState.success.username)
                    emailTextView.setText(profileFormState.success.email)
                    roleTextView.setText(profileFormState.success.rolename)
                    val photo = decodePhoto(
                        profileFormState.success.photo,
                        loggedUserInfo.role,
                        requireContext()
                    )
                    avatar.setImageBitmap(photo)
                } else if (profileFormState.error != null) {
                    val appContext = context?.applicationContext
                    appContext?.let {
                        Toast.makeText(
                            appContext,
                            getString(R.string.error_retrieving_profile),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            })

        val exitButton = view.findViewById<Button>(R.id.exit)

        exitButton?.setOnClickListener {
            MainApplication.logout()
            requireActivity().finish()
        }
        profileViewModel.load()

    }
}
