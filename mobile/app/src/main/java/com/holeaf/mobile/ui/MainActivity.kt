package com.holeaf.mobile.ui

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.holeaf.mobile.EventsService
import com.holeaf.mobile.MainApplication
import com.holeaf.mobile.MainApplication.Companion.observeEvents
import com.holeaf.mobile.R
import com.holeaf.mobile.data.login.model.LoggedUserInfo
import com.holeaf.mobile.data.maps.model.PlaceInfo
import com.holeaf.mobile.ui.chat.ChatListFragment
import com.holeaf.mobile.ui.login.ProfileFragment
import com.holeaf.mobile.ui.orders.OrderDetailsFragment
import com.holeaf.mobile.ui.orders.OrdersFragment
import com.holeaf.mobile.ui.shop.CartFragment
import com.holeaf.mobile.ui.orders.CourierAssignmentFragment
import com.holeaf.mobile.ui.orders.CourierWaitingFragment
import com.holeaf.mobile.ui.shop.ShopFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.fragment.android.replace
import org.koin.androidx.fragment.android.setupKoinFragmentFactory
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.KoinExperimentalAPI


class MainActivity : AppCompatActivity() {
    var fusedLocationClient: FusedLocationProviderClient? = null
    val locationVM: LocationViewModel by viewModel()
    private lateinit var locationCallback: LocationCallback
    var requestingLocationUpdates = true

    var eventsService: EventsService? = null
    val loggedUserInfo: LoggedUserInfo by inject()

    @KoinExperimentalAPI
    override fun onCreate(savedInstanceState: Bundle?) {
        setupKoinFragmentFactory()
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        setContentView(R.layout.activity_main)
        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNav.setOnNavigationItemSelectedListener(navListener)

        //check role
        val bottomNavigationView =
            findViewById<View>(R.id.bottom_navigation) as BottomNavigationView
        if (loggedUserInfo.role?.mapClient != true) {
            bottomNavigationView.menu.removeItem(R.id.nav_shop)
        }
        if (loggedUserInfo.role?.mapClient != true && loggedUserInfo.role?.mapCourier != true) {
            bottomNavigationView.menu.removeItem(R.id.nav_orders)
        }

        if (loggedUserInfo.role?.mapCourier == true || loggedUserInfo.role?.mapManager == true || loggedUserInfo.role?.mapClient == true || loggedUserInfo.role?.mapSeller == true) {
            askPermission(Manifest.permission.ACCESS_FINE_LOCATION) {
                if (!it.isAccepted) {
                    Toast.makeText(
                        this,
                        "Для полной функциональности необходимо предоставить разрешение на доступ к местоположению",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    if (savedInstanceState == null) {
                        supportFragmentManager.beginTransaction().replace<MapsFragment>(
                            R.id.fragment_container,
                        ).commit()
                    }
                }
            }
        } else {
            if (loggedUserInfo.role?.supplies == true && loggedUserInfo.role?.mapSeller == false) {
                bottomNavigationView.menu.removeItem(R.id.nav_map)
                supportFragmentManager.beginTransaction()
                    .replace<ChatListFragment>(R.id.fragment_container)
                    .commit()
            } else {
                supportFragmentManager.beginTransaction().replace<MapsFragment>(
                    R.id.fragment_container,
                ).commit()
            }
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    //отправка актуального местоположения на сервер
                    //сохранение местоположения на сервер
                    //только для курьеров (остальные только получают соседей)

                    GlobalScope.launch(Dispatchers.IO) {
                        if (loggedUserInfo.role?.mapCourier == true || loggedUserInfo.role?.mapManager == true || loggedUserInfo.role?.mapClient == true || loggedUserInfo.role?.mapSeller == true) {
                            try {
                                val nearest = MainApplication.getServiceAuth()
                                    .savePlace(
                                        PlaceInfo(
                                            0,
                                            0,
                                            location.latitude.toFloat(),
                                            location.longitude.toFloat()
                                        )
                                    )
                                if (loggedUserInfo.role?.mapManager == true
                                    || loggedUserInfo.role?.mapSeller == true
                                    || loggedUserInfo.role?.mapClient == true
                                ) {
                                    locationVM.positions.postValue(nearest.map {
                                        GeoPosition(it.user, it.latitude, it.longitude)
                                    }.toMutableList())
                                }
                            } catch (e: java.lang.Exception) {
                            }
                        }
                    }
                    locationVM.location.postValue(location)
                }
            }
        }

        //connect to events stream
        eventsService = observeEvents {
            Log.i("EVENT", "Take new event")
            Log.i("EVENT", "Data is $it")
            when (it.content) {
                "courier_assigned" -> locationVM.courierWaiting.postValue(1)
                "order_available" -> notifyCourier()
            }
        }
        requestingLocationUpdates =
            (loggedUserInfo.role?.mapCourier == true || loggedUserInfo.role?.mapManager == true || loggedUserInfo.role?.mapSeller == true || loggedUserInfo.role?.mapClient == true)
    }

    val CHANNEL_ID = "Orders"

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Orders"
            val descriptionText = "Информация о заказах"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun notifyCourier() {
        var newMessageNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle("Доступен новый заказ")
            .setContentText("Поступил новый заказ в вашем районе")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true).build()
        with(NotificationManagerCompat.from(this)) {
            Log.i("NC", "Notify courier")
            notify(1, newMessageNotification)
        }
    }

    override fun onResume() {
        super.onResume()
        if (requestingLocationUpdates) startLocationUpdates()
        //unlock test (if paused for login transition)
        try {
            MainApplication.globalLocker.decrement()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
//        stopLocationUpdates()
    }


    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val mLocationRequest = LocationRequest()
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        mLocationRequest.setInterval(10 * 1000)       //10 sec
        mLocationRequest.setFastestInterval(2 * 1000) // 2sec
        fusedLocationClient = FusedLocationProviderClient(this)
        fusedLocationClient?.requestLocationUpdates(
            mLocationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    @KoinExperimentalAPI
    private val navListener: BottomNavigationView.OnNavigationItemSelectedListener =
        object : BottomNavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.nav_map -> supportFragmentManager.beginTransaction()
                        .replace<MapsFragment>(R.id.fragment_container).commit()
                    R.id.nav_orders -> supportFragmentManager.beginTransaction()
                        .replace<OrdersFragment>(R.id.fragment_container).commit()
                    R.id.nav_shop -> supportFragmentManager.beginTransaction()
                        .replace<ShopFragment>(R.id.fragment_container).commit()
                    R.id.nav_profile -> supportFragmentManager.beginTransaction()
                        .replace<ProfileFragment>(R.id.fragment_container).commit()
                    R.id.nav_chat -> supportFragmentManager.beginTransaction()
                        .replace<ChatListFragment>(R.id.fragment_container).commit()
                }
                return true
            }
        }

    @KoinExperimentalAPI
    fun navigateToCart() {
        supportFragmentManager.beginTransaction().replace<CartFragment>(R.id.fragment_container)
            .addToBackStack("cart")
            .commit()
    }

    @KoinExperimentalAPI
    fun navigateToMap() {
        supportFragmentManager.beginTransaction().replace<MapsFragment>(R.id.fragment_container)
            .addToBackStack("map")
            .commit()
        val bottomNavigationView =
            findViewById<View>(R.id.bottom_navigation) as BottomNavigationView
        bottomNavigationView.selectedItemId = R.id.nav_map
    }

    @KoinExperimentalAPI
    fun navigateToOrders() {
        val bottomNavigationView: BottomNavigationView
        bottomNavigationView = findViewById<View>(R.id.bottom_navigation) as BottomNavigationView
        bottomNavigationView.selectedItemId = R.id.nav_orders
    }

    @KoinExperimentalAPI
    fun navigateToShop() {
        val bottomNavigationView: BottomNavigationView
        bottomNavigationView = findViewById<View>(R.id.bottom_navigation) as BottomNavigationView
        bottomNavigationView.selectedItemId = R.id.nav_shop
    }

    @KoinExperimentalAPI
    fun navigateToCourierWaiting() {
        supportFragmentManager.beginTransaction()
            .replace<CourierWaitingFragment>(R.id.fragment_container).commit()
    }

    @KoinExperimentalAPI
    fun navigateToCourierAssigned() {
        supportFragmentManager.beginTransaction()
            .replace<CourierAssignmentFragment>(R.id.fragment_container).commit()
    }

    fun navigateToOrderDetails(id: Int) {
        val args = Bundle()
        args.putInt("id", id)
        supportFragmentManager.beginTransaction()
            .replace<OrderDetailsFragment>(R.id.fragment_container, args)
            .addToBackStack("order_details").commit()
    }
}