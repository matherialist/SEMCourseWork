package com.holeaf.mobile.ui.shop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.holeaf.mobile.CartItemData
import com.holeaf.mobile.R
import com.holeaf.mobile.data.chat.ShopAdapter
import com.holeaf.mobile.ui.LocationViewModel
import com.holeaf.mobile.ui.MainActivity
import kotlinx.android.synthetic.main.shop_list.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.KoinExperimentalAPI

class ShopFragment : Fragment() {

    val viewModel: ShopViewModel by viewModel()
    private val locationViewModel: LocationViewModel by sharedViewModel()

    @KoinExperimentalAPI
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (locationViewModel.courierWaiting.value == 0) {
            (requireActivity() as MainActivity).navigateToCourierWaiting()
        }
        return inflater.inflate(
            R.layout.shop_list, container,
            false
        )
    }

    private lateinit var gotocartButton: Button
    private lateinit var shopList: RecyclerView

    private lateinit var shopAdapter: ShopAdapter

    @KoinExperimentalAPI
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locationViewModel.courierWaiting.observe(viewLifecycleOwner) {
            if (it == 0) {
                //перешли в режим ожидания курьера, нужно показать заглушку
                (requireActivity() as MainActivity).navigateToCourierWaiting()
            }
        }

        shopList = view.findViewById(R.id.shop_items)
        gotocartButton = view.findViewById(R.id.gotocart)
//        var newMessage = view.findViewById<EditText>(R.id.newMessage)

        viewModel.shopData.observe(viewLifecycleOwner) {
            shopAdapter.data = it
            shopAdapter.notifyDataSetChanged()
        }

        viewModel.cart.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                gotocartButton.visibility = View.GONE
                shopList.setPadding(0, 0, 0, 0)
            } else {
                gotocartButton.visibility = View.VISIBLE
                val scale = resources.displayMetrics.density
                val dpAsPixels = (72 * scale + 0.5f)
                shopList.setPadding(0, 0, 0, dpAsPixels.toInt())
            }
        }

        viewModel.update(locationViewModel)
        shopAdapter = ShopAdapter { id, delta ->
            val already = viewModel.cart.value?.firstOrNull { it.id == id }
            if (already != null) {
                val newValue = already.amount + delta
                when {
                    newValue > 0 -> {
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
                    }
                    newValue == 0 -> {
                        viewModel.cart.value =
                            viewModel.cart.value?.filter { it.id != id }?.toMutableList()
                        GlobalScope.launch(Dispatchers.IO) {
                            viewModel.shopRepository.removeFromCart(id)
                        }
                        0
                    }
                    else -> already.amount
                }
            } else {
                if (delta > 0) {
                    val list = viewModel.cart.value
                    list?.let {
                        val cartCopy = mutableListOf<CartItem>()
                        cartCopy.addAll(list)
                        cartCopy.add(CartItem(id, delta))
                        viewModel.cart.postValue(cartCopy)
                    }
                    GlobalScope.launch(Dispatchers.IO) {
                        viewModel.shopRepository.addToCart(CartItemData(id, delta))
                    }
                    delta
                } else {
                    0
                }
            }
        }


        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            .apply {
                isSmoothScrollbarEnabled = true
            }
        shopList.layoutManager = layoutManager
        shopList.adapter = shopAdapter

        gotocart.setOnClickListener {
            (requireActivity() as MainActivity).navigateToCart()
        }
    }
}