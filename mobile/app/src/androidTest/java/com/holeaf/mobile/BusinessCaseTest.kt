package com.holeaf.mobile

import android.util.Log
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.holeaf.mobile.ui.MainActivity
import io.qameta.allure.android.runners.AllureAndroidJUnit4
import io.qameta.allure.kotlin.Description
import org.hamcrest.Matchers.allOf
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AllureAndroidJUnit4::class)
class BusinessCaseTest : LoggedInTest() {
    @Test
    @Description("Проверка основного бизнес-цикла")
    fun testBusinessCycle() {

        val username = onView(withId(R.id.username))
        val password = onView(withId(R.id.password))
        val login = onView(withId(R.id.login))
        username.perform(ViewActions.typeText("client"))
        password.perform(ViewActions.typeText("password"))
            .perform(ViewActions.closeSoftKeyboard())

        Intents.init()
        login.perform(click())
        Espresso.onIdle()
        Log.i("AS", "Waiting for main activity")
        Intents.intended(IntentMatchers.hasComponent(MainActivity::class.java.name))
        Intents.release()
        Thread.sleep(2000)

        val nav_shop = onView(withId(R.id.nav_shop))
        nav_shop.perform(click())
        Thread.sleep(2000)
        val item_plus = onView(
            allOf(
                getElementFromMatchAtPosition(allOf(withId(R.id.item_plus)), 0),
                isDisplayed()
            )
        )
        item_plus.perform(click())

        val go_to_cart = onView(withId(R.id.gotocart))
        go_to_cart.perform(click())
        Thread.sleep(2000)

        val makeorder = onView(withId(R.id.makeorder))
        makeorder.perform(click())
        logoutFromApp()

        Thread.sleep(2000)

        username.perform(ViewActions.typeText("courier"))
        password.perform(ViewActions.typeText("password"))
            .perform(ViewActions.closeSoftKeyboard())

        Intents.init()
        login.perform(click())
        Espresso.onIdle()
        Log.i("AS", "Waiting for main activity")
        Intents.intended(IntentMatchers.hasComponent(MainActivity::class.java.name))
        Intents.release()
        Thread.sleep(2000)

        val nav_orders = onView(withId(R.id.nav_orders))
        nav_orders.perform(click())
        Thread.sleep(2000)

        val order_list_item_elements = onView(
            allOf(
                getElementFromMatchAtPosition(allOf(withId(R.id.order_list_item_elements)), 0),
                isDisplayed()
            )
        )
        order_list_item_elements.perform(click())
        Thread.sleep(2000)

        val order_details_button = onView(withId(R.id.order_details_button))
        order_details_button.perform(click())

        nav_orders.perform(click())
        Thread.sleep(3000)
        val order_list_item_elements2 = onView(
            allOf(
                getElementFromMatchAtPosition(allOf(withId(R.id.order_list_item_elements)), 0),
                isDisplayed()
            )
        )
        order_list_item_elements2.perform(click())
        Thread.sleep(2000)
        val order_details_button2 = onView(withId(R.id.order_details_button))
        order_details_button2.perform(click())

        logoutFromApp()
    }


}