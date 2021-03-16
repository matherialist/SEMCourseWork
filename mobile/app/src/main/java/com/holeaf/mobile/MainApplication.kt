package com.holeaf.mobile

import android.util.Base64
import android.util.Log
import androidx.multidex.MultiDexApplication
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.idling.CountingIdlingResource
import com.google.gson.Gson
import com.holeaf.mobile.data.chat.*
import com.holeaf.mobile.data.login.*
import com.holeaf.mobile.data.login.model.LoggedUserInfo
import com.holeaf.mobile.data.maps.MapsService
import com.holeaf.mobile.ui.LocationViewModel
import com.holeaf.mobile.ui.MapsFragment
import com.holeaf.mobile.ui.MapsViewModel
import com.holeaf.mobile.ui.chat.ChatListFragment
import com.holeaf.mobile.ui.chat.ChatListViewModel
import com.holeaf.mobile.ui.chat.ChatViewModel
import com.holeaf.mobile.ui.login.*
import com.holeaf.mobile.ui.orders.*
import com.holeaf.mobile.ui.recovery.RecoveryViewModel
import com.holeaf.mobile.ui.registration.RegistrationViewModel
import com.holeaf.mobile.ui.shop.*
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.messageadapter.gson.GsonMessageAdapter
import com.tinder.scarlet.streamadapter.rxjava2.RxJava2StreamAdapterFactory
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import com.tinder.scarlet.ws.Receive
import com.tinder.scarlet.ws.Send
import io.reactivex.Flowable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.fragment.dsl.fragment
import org.koin.androidx.fragment.koin.fragmentFactory
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.KoinExperimentalAPI
import org.koin.core.context.startKoin
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface ChatService {
    @Receive
    fun observeWebSocketEvent(): Flowable<WebSocket.Event>

    @Send
    fun sendMessage(message: NewMessage)

    @Receive
    fun observeMessages(): Flowable<NewMessage>
}

interface EventsService {
    @Receive
    fun observeWebSocketEvent(): Flowable<WebSocket.Event>

    @Send
    fun sendMessage(message: NewEvent)

    @Receive
    fun observeMessages(): Flowable<NewEvent>
}


class MainApplication : MultiDexApplication() {

    companion object {
        var secureClient: OkHttpClient? = null
        val SERVER_URL = "http://77.222.52.151:10080/"
        val CHAT_URL = "ws://77.222.52.151:10080/chat"
        val EVENTS_URL = "ws://77.222.52.151:10080/events"

        private lateinit var retrofit: Retrofit
        private lateinit var service: HoleafService
        var token: String? = null
        var id: Int? = null

        fun getService(): HoleafService {
            return service
        }

        fun logout() {
            this.token = null
            this.id = null
            secureClient = null
        }

        fun authenticate(token: String, id: Int) {
            this.token = token
            this.id = id
            val interceptor = HttpLoggingInterceptor()
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

            secureClient = OkHttpClient.Builder().addInterceptor { chain ->
                val basic = "token:" + token
                val request = chain.request().newBuilder()
                    .addHeader(
                        "Authorization",
                        "Basic " + Base64.encodeToString(basic.toByteArray(), Base64.NO_WRAP)
                    ).build()
                chain.proceed(request)
            }.addInterceptor(interceptor).build()
        }

        fun getServiceAuth(): HoleafService {
            Log.i("SA", "Get service auth")

            val retrofitAuth = Retrofit.Builder()
                .baseUrl(SERVER_URL)
                .client(secureClient)
                .addConverterFactory(GsonConverterFactory.create(Gson()))
                .build()
            return retrofitAuth.create(HoleafService::class.java)
        }

        fun instantiateChat(id: Int, onMessage: (message: NewMessage) -> Unit): ChatService? {
            Log.i("MainApp", "Reinstantiate chat")
            secureClient?.let { secureClient ->
                val scarletInstance = Scarlet.Builder()
                    .webSocketFactory(secureClient.newWebSocketFactory(CHAT_URL))
                    .addMessageAdapterFactory(GsonMessageAdapter.Factory())
                    .addStreamAdapterFactory(RxJava2StreamAdapterFactory())
                    .build()
                val messagesService = scarletInstance.create<ChatService>()
                messagesService.observeWebSocketEvent()
                    .filter { it is WebSocket.Event.OnConnectionOpened<*> }.subscribe {
                        Log.i("MainApp", "Connected")
                        messagesService.sendMessage(NewMessage(id, "subscribe"))
                    }
                messagesService.observeWebSocketEvent()
                    .filter { it is WebSocket.Event.OnConnectionClosed }.subscribe {
                        Log.i("MainApp", "Closed")
                    }
                messagesService.observeMessages().subscribe(onMessage)
                return messagesService
            }
            return null
        }

        fun observeEvents(onMessage: (message: NewEvent) -> Unit): EventsService? {
            Log.i("MainApp", "Observe events")
            secureClient?.let { secureClient ->
                val scarletInstance = Scarlet.Builder()
                    .webSocketFactory(secureClient.newWebSocketFactory(EVENTS_URL))
                    .addMessageAdapterFactory(GsonMessageAdapter.Factory())
                    .addStreamAdapterFactory(RxJava2StreamAdapterFactory())
                    .build()
                val eventsService = scarletInstance.create<EventsService>()
                eventsService.observeWebSocketEvent()
                    .filter { it is WebSocket.Event.OnConnectionOpened<*> }.subscribe {
                        Log.i("MainApp", "Connected")
                        eventsService.sendMessage(NewEvent("subscribe"))
                    }
                eventsService.observeWebSocketEvent()
                    .filter { it is WebSocket.Event.OnConnectionClosed }.subscribe {
                        Log.i("MainApp", "Closed")
                    }
                eventsService.observeMessages().subscribe(onMessage)
                return eventsService
            }
            return null
        }

        var globalLocker = CountingIdlingResource("global")
    }


    @KoinExperimentalAPI
    override fun onCreate() {
        super.onCreate()
        IdlingRegistry.getInstance().register(globalLocker)

        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

        retrofit = Retrofit.Builder()
            .baseUrl(SERVER_URL)
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .client(client)
            .build()

        service = retrofit.create(HoleafService::class.java)
        authenticate("", 0)     //generate insecure public connection

        val module = module {
            viewModel {
                LoginViewModel(get())
            }
            single {
                LoginRepository(get())
            }
            single {
                LoginDataSource()
            }
            viewModel {
                ChatListViewModel(get())
            }
            single {
                ChatListRepository(get())
            }
            single {
                ChatListDataSource()
            }
            viewModel {
                ChatViewModel(get())
            }
            single {
                ChatRepository(get())
            }
            single {
                ChatDataSource()
            }
            viewModel {
                RegistrationViewModel(get())
            }
            single {
                RegistrationRepository(get())
            }
            single {
                RegistrationDataSource()
            }
            viewModel {
                RecoveryViewModel(get())
            }
            single {
                RecoveryRepository(get())
            }
            single {
                RecoveryDataSource()
            }
            viewModel {
                ProfileViewModel(get())
            }
            single {
                ProfileRepository(get())
            }
            single {
                ProfileDataSource()
            }
            viewModel {
                ShopViewModel(get())
            }
            single {
                ShopRepository(get())
            }
            single {
                ShopDataSource()
            }
            fragment {
                LoginFragment()
            }
            fragment {
                ProfileFragment()
            }
            fragment {
                RecoveryFragment()
            }
            fragment {
                MapsFragment()
            }
            fragment {
                ChatListFragment()
            }
            fragment {
                CartFragment()
            }
            fragment {
                ShopFragment()
            }
            viewModel {
                LocationViewModel()
            }
            fragment {
                OrdersFragment()
            }
            fragment {
                CourierWaitingFragment()
            }
            single {
                LoggedUserInfo()
            }
            viewModel {
                OrderViewModel(get())
            }
            fragment {
                OrderDetailsFragment()
            }
            single {
                MapsService()
            }
            viewModel {
                MapsViewModel()
            }
            fragment {
                CourierAssignmentFragment()
            }
        }

        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            fragmentFactory()
            modules(module)
        }
    }
}