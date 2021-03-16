package com.holeaf.mobile.ui.shop

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
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
import com.holeaf.mobile.CartItemData
import com.holeaf.mobile.NewEvent
import com.holeaf.mobile.R
import com.holeaf.mobile.data.chat.CartAdapter
import com.holeaf.mobile.data.maps.model.PlaceData
import com.holeaf.mobile.ui.LocationViewModel
import com.holeaf.mobile.ui.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class CartFragment : Fragment() {

    val viewModel: ShopViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.cart, container, false)
    }

    val locationVM: LocationViewModel by sharedViewModel()

    var map: GoogleMap? = null

    lateinit var makeOrder: Button
    lateinit var cartInfo: RecyclerView
    lateinit var summaryText: TextView

    lateinit var cartAdapter: CartAdapter

    var targetMarker: Marker? = null

    private val callback = OnMapReadyCallback { googleMap ->

        googleMap.setBuildingsEnabled(false)
        googleMap.setMinZoomPreference(6.0f);
        googleMap.setMaxZoomPreference(18.0f);

        val location_lat = locationVM.location.value?.latitude ?: 0.0
        val location_long = locationVM.location.value?.longitude ?: 0.0

        googleMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(location_lat, location_long),
                14.0f
            )
        )


        val lat = locationVM.location.value?.latitude
        val long = locationVM.location.value?.longitude

        if (lat != null && long != null) {
            targetMarker = googleMap.addMarker(
                MarkerOptions()
                    .position(
                        LatLng(
                            lat,
                            long
                        )
                    )
                    .title("Место размещения")
                    .draggable(true)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.place))
            )
        }


        googleMap.setOnMapLongClickListener {
            //скрыть местоположение
            if (targetMarker != null) {
                targetMarker?.isVisible = false
                targetMarker?.position = null
                //отправить на сервер очистку местоположения
            }
        }

        googleMap.setOnMapClickListener {
            if (targetMarker == null) {
                targetMarker = googleMap.addMarker(
                    MarkerOptions()
                        .position(it)
                        .title("Место размещения")
                        .draggable(true)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.outlined))
                )
            } else {
                targetMarker?.position = it
                targetMarker?.isVisible = true
            }
        }
        map = googleMap
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cartInfo = view.findViewById(R.id.cart_info)
        makeOrder = view.findViewById(R.id.makeorder)
        summaryText = view.findViewById(R.id.cart_summary_cost)
        //todo: button and reaction
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            .apply {
                isSmoothScrollbarEnabled = true
            }
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        viewModel.update(locationVM)
        cartAdapter = CartAdapter { id, delta ->
            val already = viewModel.cart.value?.firstOrNull { it.id == id }
            if (already != null) {
                val newValue = already.amount + delta
                if (newValue > 0) {
                    val list = viewModel.cart.value
                    list?.let {
                        val cartCopy = mutableListOf<CartItem>()
                        cartCopy.addAll(list)
                        cartCopy[cartCopy.indexOfFirst { it.id == id }] = CartItem(id, newValue)
                        viewModel.cart.postValue(cartCopy)
                    }
                    GlobalScope.launch(Dispatchers.IO) {
                        viewModel.shopRepository.addToCart(CartItemData(id, newValue))
                    }
                    newValue
                } else if (newValue == 0) {
                    viewModel.cart.postValue(viewModel.cart.value?.filter { it.id != id }
                        ?.toMutableList())
                    GlobalScope.launch(Dispatchers.IO) {
                        viewModel.shopRepository.removeFromCart(id)
                    }
                    0
                } else already.amount
            } else {
                val newValue = delta
                if (newValue > 0) {
                    val list = viewModel.cart.value
                    list?.let {
                        val cartCopy = mutableListOf<CartItem>()
                        cartCopy.addAll(list)
                        cartCopy.add(CartItem(id, newValue))
                        viewModel.cart.postValue(cartCopy)
                    }
                    GlobalScope.launch(Dispatchers.IO) {
                        viewModel.shopRepository.addToCart(CartItemData(id, newValue))
                    }
                    newValue
                } else {
                    0
                }
            }
        }
        cartInfo.layoutManager = layoutManager
        cartInfo.adapter = cartAdapter

        viewModel.update(locationVM)
        viewModel.shopData.observe(viewLifecycleOwner) {
            cartAdapter.items = it
            cartAdapter.notifyDataSetChanged()
            var summary = 0
            viewModel.cart.value?.forEach { ci ->
                val product = viewModel.shopData.value?.firstOrNull { it.id == ci.id }
                if (product != null) summary += ci.amount * product.price
            }
            summaryText.text = "$summary р."
        }

        viewModel.cart.observe(viewLifecycleOwner) {
            cartAdapter.data = it
            cartAdapter.notifyDataSetChanged()
            var summary = 0
            viewModel.cart.value?.forEach { ci ->
                val product = viewModel.shopData.value?.firstOrNull { it.id == ci.id }
                if (product != null) summary += ci.amount * product.price
            }
            summaryText.text = "$summary р."
        }

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.place_map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        makeOrder.setOnClickListener {
            val lat = targetMarker?.position?.latitude?.toFloat() ?: 0.0f
            val long = targetMarker?.position?.longitude?.toFloat() ?: 0.0f
            val place = PlaceData(lat = lat, long = long)
            GlobalScope.launch(Dispatchers.IO) {
                val result = viewModel.shopRepository.createOrder(place)
                Log.i("Cart", "Order created $result")
                locationVM.courierWaiting.postValue(0)
                (requireActivity() as MainActivity).eventsService?.sendMessage(NewEvent("makeorder:${lat}:${long}"))
                GlobalScope.launch(Dispatchers.Main) {
                    (requireActivity() as MainActivity).navigateToShop()
                }
            }
        }
    }
}