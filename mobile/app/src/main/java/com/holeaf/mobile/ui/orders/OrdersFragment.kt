package com.holeaf.mobile.ui.orders

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.holeaf.mobile.R
import com.holeaf.mobile.data.orders.OrderListAdapter
import com.holeaf.mobile.ui.LocationViewModel
import com.holeaf.mobile.ui.MainActivity
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.KoinExperimentalAPI

class OrdersFragment : Fragment() {

    val locationViewModel: LocationViewModel by sharedViewModel()
    val orderVM: OrderViewModel by viewModel()

    @KoinExperimentalAPI
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.order_list, container, false)
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
    }

    private lateinit var orderListAdapter: OrderListAdapter

    lateinit var orderList: RecyclerView

    @KoinExperimentalAPI
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        orderList = view.findViewById(R.id.order_list_items)
        orderVM.update()
        orderListAdapter = OrderListAdapter(requireActivity()) { id ->
            //navigate to order details
            (requireActivity() as MainActivity).navigateToOrderDetails(id)
        }
        orderVM.orders.observe(viewLifecycleOwner) {
            orderListAdapter.data = it.toMutableList()
            orderListAdapter.notifyDataSetChanged()
        }

        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            .apply {
                isSmoothScrollbarEnabled = true
            }
        orderList.layoutManager = layoutManager
        orderList.adapter = orderListAdapter
    }
}
