package com.holeaf.mobile.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.holeaf.mobile.R
import com.holeaf.mobile.ui.LocationViewModel
import com.holeaf.mobile.ui.MainActivity
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class CourierWaitingFragment : Fragment() {

    val locationVM: LocationViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (locationVM.courierWaiting.value == null || locationVM.courierWaiting.value == 1) {
            (requireActivity() as MainActivity).navigateToOrders()
        }
        return inflater.inflate(R.layout.courier_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        locationVM.courierWaiting.observe(viewLifecycleOwner) {
            if (it==null) {
                (requireActivity() as MainActivity).navigateToOrders()
            } else if (it==1) {
                (requireActivity() as MainActivity).navigateToCourierAssigned()
            }
        }
        super.onViewCreated(view, savedInstanceState)
    }
}