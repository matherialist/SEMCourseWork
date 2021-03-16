package com.holeaf.mobile

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import io.qameta.allure.android.runners.AllureAndroidJUnit4
import io.qameta.allure.kotlin.Description
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AllureAndroidJUnit4::class)
class CartTest : LoggedInTest() {
    @Test
    @Description("Проверка наличия элементов интерфейса")
    fun testDesign() {

        loginToApp()

        val nav_shop = onView(withId(com.holeaf.mobile.R.id.nav_shop))
        nav_shop.perform(click())

        val item_plus = onView(
            getElementFromMatchAtPosition(allOf(withId(com.holeaf.mobile.R.id.item_plus)), 0),
        )
        item_plus.perform(click())
        val go_to_cart = onView(withId(com.holeaf.mobile.R.id.gotocart))
        go_to_cart.perform(click())

        val incart_page_label = onView(withId(com.holeaf.mobile.R.id.incart_page_label))
        val incart_label = onView(withId(com.holeaf.mobile.R.id.incart_label))
        val cart_info = onView(withId(com.holeaf.mobile.R.id.cart_info))
        val cart_summary = onView(withId(com.holeaf.mobile.R.id.cart_summary))
        val cart_summary_cost = onView(withId(com.holeaf.mobile.R.id.cart_summary_cost))
        val select_delivery_place = onView(withId(com.holeaf.mobile.R.id.select_delivery_place))
        val place_map = onView(withId(com.holeaf.mobile.R.id.place_map))
        val makeorder = onView(withId(com.holeaf.mobile.R.id.makeorder))

        incart_page_label.check(matches(isDisplayed()))
        incart_label.check(matches(isDisplayed()))
        cart_info.check(matches(isDisplayed()))
        cart_summary.check(matches(isDisplayed()))
        cart_summary_cost.check(matches(isDisplayed()))
        select_delivery_place.check(matches(isDisplayed()))
        place_map.check(matches(isDisplayed()))
        makeorder.check(matches(isDisplayed()))
        makeorder.check(matches(isClickable()))

        Thread.sleep(1000)
        val short_item_minus = onView(
            allOf(
                getElementFromMatchAtPosition(
                    allOf(withId(com.holeaf.mobile.R.id.short_item_minus)),
                    0
                ),
                isDisplayed()
            )
        )
        short_item_minus.perform(click())

        logoutFromApp()
    }


}