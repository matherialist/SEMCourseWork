package com.holeaf.mobile.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.holeaf.mobile.R
import com.holeaf.mobile.Role
import com.holeaf.mobile.ui.LocationViewModel
import com.holeaf.mobile.ui.login.decodePhoto
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class CourierAssignmentFragment : Fragment() {

    val locationVM: LocationViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.courier_assigned, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val avatar = view.findViewById<ImageView>(R.id.courier_avatar)
        val name = view.findViewById<TextView>(R.id.courier_name)

        locationVM.getCourierForActiveOrder()
        locationVM.courier.observe(viewLifecycleOwner) {
            name.text = it.name
            val roleCourier = Role(
                0,
                "stub",
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                true,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false
            )
            avatar.setImageBitmap(decodePhoto(it.photo, roleCourier, requireContext()))
        }
    }
}