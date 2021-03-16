package com.holeaf.mobile.ui.orders

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.holeaf.mobile.NewEvent
import com.holeaf.mobile.R
import com.holeaf.mobile.data.login.model.LoggedUserInfo
import com.holeaf.mobile.data.orders.OrderDetailsAdapter
import com.holeaf.mobile.ui.LocationViewModel
import com.holeaf.mobile.ui.MainActivity
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class OrderDetailsFragment : Fragment(), KoinComponent {

    val viewModel: OrderViewModel by viewModel()

    val loggedUserInfo: LoggedUserInfo by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.order_details, container, false)
    }

    val locationVM: LocationViewModel by sharedViewModel()

    var map: GoogleMap? = null

    lateinit var button: Button
    lateinit var orderDetailsInfo: RecyclerView
    lateinit var summaryText: TextView

    lateinit var orderDetailsAdapter: OrderDetailsAdapter

    var targetMarker: Marker? = null

    private val callback = OnMapReadyCallback { googleMap ->

        googleMap.setBuildingsEnabled(false)
        googleMap.setMinZoomPreference(6.0f)
        googleMap.setMaxZoomPreference(18.0f)


        val det = viewModel.orderDetails.value

        var lat = det?.lat ?: 59.87
        var long = det?.long ?: 30.309

        googleMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(lat.toDouble(), long.toDouble()),
                14.0f
            )
        )

        val location_lat = locationVM.location.value?.latitude
        val location_long = locationVM.location.value?.longitude

        if (location_lat != null && location_long != null) {
            googleMap.addMarker(
                MarkerOptions().position(LatLng(location_lat, location_long)).draggable(false)
                    .title("Ваше местоположение")
            )
        }

        if (det?.lat != null && det?.long != null) {
            targetMarker = googleMap.addMarker(
                MarkerOptions()
                    .position(
                        LatLng(
                            lat.toDouble(),
                            long.toDouble()
                        )
                    ).title("Место размещения")
                    .draggable(true)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.place))
            )
        }


        map = googleMap
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.i("Shop", "View created")
        super.onViewCreated(view, savedInstanceState)
        orderDetailsInfo = view.findViewById(R.id.order_details_info)
        val orderDetailTitle = view.findViewById<TextView>(R.id.order_details_page_label)
        button = view.findViewById(R.id.order_details_button)
        summaryText = view.findViewById(R.id.order_details_summary_cost)
        //todo: button and reaction
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            .apply {
                isSmoothScrollbarEnabled = true
            }
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        orderDetailsAdapter = OrderDetailsAdapter()
        orderDetailsInfo.layoutManager = layoutManager
        orderDetailsInfo.adapter = orderDetailsAdapter

        val id = this.arguments?.getInt("id", 0) ?: 0
        viewModel.getDetails(id)
        viewModel.orderDetails.observe(viewLifecycleOwner) {
            Log.i("OrderDetails", "Items is $it")
            orderDetailTitle.text = "Детали заказа №${it.orderId}"
            orderDetailsAdapter.items = it.orderItemList.toMutableList()
            orderDetailsAdapter.notifyDataSetChanged()
            var summary = 0
            val order = it
            viewModel.orderDetails.value?.orderItemList?.forEach { ci ->
                summary += ci.amount * ci.price
            }
            summaryText.text = "$summary р."

            val mapFragment =
                childFragmentManager.findFragmentById(R.id.order_details_place_map) as SupportMapFragment?
            mapFragment?.getMapAsync(callback)

            //изменить надпись и действие на кнопке
            if (loggedUserInfo.role?.mapClient == true) {
                //если это клиент
                if (it.status == 0) {
                    //и заказ ещё не взят
                    button.visibility = View.VISIBLE
                    button.text = "Отменить заказ"
                    button.setOnClickListener {
                        viewModel.setStatus(order.orderId, 3)
                        (requireActivity() as MainActivity).navigateToOrders()
                    }
                } else {
                    //или спрятать кнопку
                    button.visibility = GONE
                }
            } else if (loggedUserInfo.role?.mapCourier == true) {
                //если это курьер
                if (it.status == 0) {
                    button.visibility = View.VISIBLE
                    button.text = "Взять заказ"
                    button.setOnClickListener {
                        viewModel.setStatus(order.orderId, 1)
                        (requireActivity() as MainActivity).eventsService?.sendMessage(NewEvent("confirmorder:${order.orderId}"))
                        (requireActivity() as MainActivity).navigateToMap()
                    }
                } else if (it.status == 1) {
                    button.visibility = View.VISIBLE
                    button.text = "Завершить заказ"
                    button.setOnClickListener {
                        viewModel.setStatus(order.orderId, 2)
                        (requireActivity() as MainActivity).eventsService?.sendMessage(NewEvent("closeorder:${order.orderId}"))
                        (requireActivity() as MainActivity).navigateToOrders()
                    }
                } else {
                    button.visibility = GONE
                }
            } else {
                button.visibility = GONE
            }
        }


    }
}