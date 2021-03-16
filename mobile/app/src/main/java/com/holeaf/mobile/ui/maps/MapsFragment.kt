package com.holeaf.mobile.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.holeaf.mobile.R
import com.holeaf.mobile.data.login.model.LoggedUserInfo
import com.holeaf.mobile.data.orders.models.UserOrderData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.lang.Math.*
import kotlin.random.Random

data class CarLocation(var route: Int, var fragment: Int, var rate: Double, var speed: Double)

class MapsFragment : Fragment(), KoinComponent {

    val CARS_COUNT = 5

    val markers: MutableList<Pair<GeoPosition, Marker>> = mutableListOf()

    val locationVM: LocationViewModel by sharedViewModel()

    val loggedUserInfo: LoggedUserInfo by inject()

    var cars = mutableListOf<CarLocation>()

    var carMarkers = mutableListOf<Marker>()

    val pause = 120L

    var map: GoogleMap? = null

    var locationMarker: Marker? = null

    private fun getCarLocation(
        route: Int,
        fragmentNumber: Int,
        rate: Double
    ): Pair<LatLng, Pair<Double, Int>> {
//        Log.i("Locating", "Route $route, fragment: $fragmentNumber, rate:$rate")
        val cp = vm.telemetry.value?.get(route)
        val startLat = cp?.get(fragmentNumber)?.latitude ?: 0.0
        val finishLat = cp?.get((fragmentNumber + 1) % cp.size)?.latitude ?: 0.0
        val startLong = cp?.get(fragmentNumber)?.longitude ?: 0.0
        val finishLong = cp?.get((fragmentNumber + 1) % cp.size)?.longitude ?: 0.0
//        return LatLng(startLat, startLong) to (0.0 to R.drawable.car)
        var angle = atan2(finishLat - startLat, finishLong - startLong)
//        Log.i("Angle", "$angle")
        var icon = R.drawable.car
        if (abs(angle) <= PI / 2) {
            angle = (-angle) / PI * 180
            icon = R.drawable.car_right
        } else {
            if (angle < 0) angle = 2 * PI + angle
            angle = (PI - angle) / PI * 180
        }
//        Log.i("Changed angle: ", "$angle")

        val lat = startLat + (finishLat - startLat) * rate
        val long = startLong + (finishLong - startLong) * rate
//        Log.i("LocationStart", "Lat:$startLat Long:$startLong")
//        Log.i("LocationEnd", "Lat:$finishLat Long:$finishLong")
//        Log.i("Location", "Lat:$lat Long:$long")
        return LatLng(lat, long) to (angle to icon)
    }

    private fun getFragmentLength(route: Int, fragmentNumber: Int): Double {
        val cp = vm.telemetry.value?.get(route)
        if (cp != null) {
            val startLat = cp[fragmentNumber].latitude
            val finishLat =
                cp[(fragmentNumber + 1) % cp.size].latitude
            val startLong = cp[fragmentNumber].longitude
            val finishLong =
                cp[(fragmentNumber + 1) % cp.size].longitude
            val deltaLong = finishLong - startLong
            val deltaLat = finishLat - startLat
            return sqrt(deltaLong * deltaLong + deltaLat * deltaLat)
        } else {
            return 0.0
        }
    }

    private fun animateMarker() {
        GlobalScope.launch(Dispatchers.Main) {
            while (true) {
                var car = 0
                while (car < cars.size) {
                    val shift =
                        cars[car].speed / getFragmentLength(cars[car].route, cars[car].fragment)
                    cars[car].rate += shift
                    if (cars[car].rate >= 1) {
                        cars[car].fragment =
                            (cars[car].fragment + 1) % (vm.telemetry.value?.get(cars[car].route)?.size
                                ?: 1)
                        cars[car].rate -= 1
                    }
                    val newPosition =
                        getCarLocation(cars[car].route, cars[car].fragment, cars[car].rate)
                    carMarkers[car].position = newPosition.first
                    carMarkers[car].rotation = newPosition.second.first.toFloat()
                    carMarkers[car].setIcon(BitmapDescriptorFactory.fromResource(newPosition.second.second))
                    car++
                }
                delay(pause)
            }
        }
    }

    private val callback = OnMapReadyCallback { googleMap ->
        googleMap.setBuildingsEnabled(false)
        googleMap.setMinZoomPreference(6.0f)
        googleMap.setMaxZoomPreference(18.0f)

        var i = 0

        val all_checkpoints = mutableListOf<LatLng>()
        vm.telemetry.value?.keys?.forEach { key ->
            vm.telemetry.value?.get(
                key
            )?.let {
                all_checkpoints.addAll(
                    it
                )
            }
        }

        locationMarkers.forEach {

            googleMap?.addMarker(
                MarkerOptions().position(
                    LatLng(
                        it.lat.toDouble(),
                        it.long.toDouble()
                    )
                ).anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.outlined))
                    .draggable(false)
            )
        }

//        val center_lat = all_checkpoints.map { it.latitude }.average()
//        val center_long = all_checkpoints.map { it.longitude }.average()
        var located = false
        locationVM.location.observe(viewLifecycleOwner) {

            if (!located) {
                if (it != null) {
                    located = true
                    Log.i("Map", "Move to location: ${it.latitude}:${it.longitude}")
                    googleMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(it.latitude, it.longitude),
                            14.0f
                        )
                    )
                    locationMarker = googleMap.addMarker(
                        MarkerOptions().position(LatLng(it.latitude, it.longitude)).draggable(false)
                            .title("Ваше местоположение")
                    )
                }
            } else {
                if (it != null) {
                    locationMarker?.position = LatLng(it.latitude, it.longitude)
                }
            }
        }


        vm.telemetry.observe(viewLifecycleOwner) {
            if (loggedUserInfo.role?.mapCourier == true) {
                while (i < CARS_COUNT) {
                    var collision = true
                    var routeNumber = 0
                    var fragmentNumber = 0
                    var rate = 0.0
                    var speed = 0.0
                    while (collision) {
                        collision = false
                        var j = 0
                        val routeId = Random.nextInt(0, vm.telemetry.value?.keys?.size ?: 0)
                        routeNumber = vm.telemetry.value?.keys?.toList()?.get(routeId) ?: 0
                        fragmentNumber =
                            Random.nextInt(0, vm.telemetry.value?.get(routeNumber)?.size ?: 0)
                        rate = Random.nextDouble()
                        speed = Random.nextDouble(0.00003, 0.00012)
                        while (j < i) {
                            if (routeNumber == cars[j].route && fragmentNumber == cars[j].fragment && abs(
                                    rate - cars[j].rate
                                ) < 0.1
                            ) collision =
                                true
                            j++
                        }
                    }
                    cars.add(CarLocation(routeNumber, fragmentNumber, rate, speed))

                    val car = getCarLocation(routeNumber, fragmentNumber, rate)
                    val marker = googleMap.addMarker(
                        MarkerOptions().position(car.first)
                            .rotation(car.second.first.toFloat())
                            .title("Машина ${i + 1}")
                            .anchor(0.5f, 0.5f)
                            .icon(BitmapDescriptorFactory.fromResource(car.second.second))
                            .draggable(false)
                    )
                    carMarkers.add(marker)
                    i++
                }
            }
        }
        map = googleMap
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    val vm: MapsViewModel by viewModel()
    var locationMarkers: List<UserOrderData> = mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?

        //place cars
        vm.update()
        vm.currentOrders.observe(viewLifecycleOwner) {
            locationMarkers = it
            mapFragment?.getMapAsync(callback)
            GlobalScope.launch(Dispatchers.Main) {
                delay(500)
                if (loggedUserInfo.role?.mapCourier == true) {
                    animateMarker()
                }
            }
        }

        locationVM.positions.observe(viewLifecycleOwner) { positions ->
            //search for marker
            positions.forEach { pos ->
                val foundMarker = markers.firstOrNull { it.first.id == pos.id }
                if (foundMarker == null) {
                    //new marker appeared
                    val marker = map?.addMarker(
                        MarkerOptions().position(
                            LatLng(
                                pos.latitude.toDouble(),
                                pos.longitude.toDouble()
                            )
                        ).anchor(0.5f, 0.5f)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.cannabis_leaf))
                            .draggable(false)
                    )
                    if (marker != null) {
                        markers.add(pos to marker)
                    }
                } else {
                    foundMarker.second.position =
                        LatLng(pos.latitude.toDouble(), pos.longitude.toDouble())
                }
            }
            val toDelete = mutableListOf<Pair<GeoPosition, Marker>>()
            markers.forEach { mark ->
                val foundPos = positions.firstOrNull { it.id == mark.first.id }
                if (foundPos == null) {
                    mark.second.remove()
                    toDelete.add(mark)
                }
            }
            toDelete.forEach {
                markers.remove(it)
            }
        }
    }
}