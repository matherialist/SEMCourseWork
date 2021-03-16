package com.holeaf.mobile

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import io.qameta.allure.android.runners.AllureAndroidJUnit4
import io.qameta.allure.kotlin.Description
import org.hamcrest.BaseMatcher
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith



@RunWith(AllureAndroidJUnit4::class)
class ClientOrderActivityTest : LoggedInTest() {
    @Test
    @Description("Проверка наличия элементов интерфейса")
    fun testDesign() {

        loginToApp()
        Thread.sleep(2000)
        val nav_shop = onView(withId(com.holeaf.mobile.R.id.nav_shop))
        nav_shop.perform(click())
        Thread.sleep(2000)
        val shop_items = onView(withId(com.holeaf.mobile.R.id.shop_items))

        shop_items.check(matches(isDisplayed()))



        logoutFromApp()
    }
    @Test
    @Description("Проверка полного цикла создания заказа")
    fun testFull() {
        loginToApp()
        val nav_shop = onView(withId(com.holeaf.mobile.R.id.nav_shop))
        nav_shop.perform(click())
        Thread.sleep(2000)
        fun getElementFromMatchAtPosition(matcher: Matcher<View>, position: Int): Matcher<View?>? {
            return object : BaseMatcher<View?>() {
                var counter = 0
                override fun describeTo(description: org.hamcrest.Description?) {

                }

                override fun matches(item: Any): Boolean {
                    if (matcher.matches(item)) {
                        if (counter == position) {
                            counter++
                            return true
                        }
                        counter++
                    }
                    return false
                }


            }
        }


        val item_plus = onView(
            allOf(
                getElementFromMatchAtPosition(allOf(withId(com.holeaf.mobile.R.id.item_plus)), 0),
                isDisplayed()))
        item_plus.perform(click())
        Thread.sleep(2000)
        val go_to_cart = onView(withId(com.holeaf.mobile.R.id.gotocart))
        go_to_cart.perform(click())
        Thread.sleep(3000)
        val makeorder= onView(withId(com.holeaf.mobile.R.id.makeorder))
        makeorder.perform(click())
        Thread.sleep(2000)
        val progressBar = onView(withId(com.holeaf.mobile.R.id.progressBar))
        val textView= onView(withId(com.holeaf.mobile.R.id.textView))

        progressBar.check(matches(isDisplayed()))
        textView.check(matches(isDisplayed()))
        val nav_orders = onView(withId(com.holeaf.mobile.R.id.nav_orders))
        nav_orders.perform(click())
        Thread.sleep(2000)
        val order_list_item_elements = onView(
            allOf(
                getElementFromMatchAtPosition(allOf(withId(com.holeaf.mobile.R.id.order_list_item_elements)), 0),
                isDisplayed()))
        order_list_item_elements.perform(click())
        Thread.sleep(2000)
        val order_details_button = onView(ViewMatchers.withId(com.holeaf.mobile.R.id.order_details_button))
        order_details_button.perform(click())
        logoutFromApp()
    }
    @Test
    @Description("Проверка cохранения статуса заказа")
    fun testSave() {
        loginToApp()
        Thread.sleep(1000)
        val nav_shop = onView(withId(com.holeaf.mobile.R.id.nav_shop))
        nav_shop.perform(click())
        Thread.sleep(2000)
        fun getElementFromMatchAtPosition(matcher: Matcher<View>, position: Int): Matcher<View?>? {
            return object : BaseMatcher<View?>() {
                var counter = 0
                override fun describeTo(description: org.hamcrest.Description?) {

                }

                override fun matches(item: Any): Boolean {
                    if (matcher.matches(item)) {
                        if (counter == position) {
                            counter++
                            return true
                        }
                        counter++
                    }
                    return false
                }


            }
        }


        val item_plus = onView(
            allOf(
                getElementFromMatchAtPosition(allOf(withId(com.holeaf.mobile.R.id.item_plus)), 0),
                isDisplayed()))
        item_plus.perform(click())
        logoutFromApp()
        loginToApp()
        Thread.sleep(1000)
        nav_shop.perform(click())
        Thread.sleep(2000)
        val go_to_cart = onView(withId(com.holeaf.mobile.R.id.gotocart))
        go_to_cart.perform(click())
        Thread.sleep(2000)
        val short_item_minus = onView(
            allOf(
                getElementFromMatchAtPosition(allOf(withId(com.holeaf.mobile.R.id.short_item_minus)), 0),
                isDisplayed()))
        short_item_minus.perform(click())
        logoutFromApp()
    }
}