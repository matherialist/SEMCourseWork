package com.holeaf.mobile

import android.util.Log
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.holeaf.mobile.ui.MainActivity
import io.qameta.allure.android.runners.AllureAndroidJUnit4
import io.qameta.allure.kotlin.Description
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AllureAndroidJUnit4::class)
class CourierOrderActivityTest : LoggedInTest() {
    @Test
    @Description("Проверка наличия элементов интерфейса")
    fun testDesign() {

        val username = onView(withId(com.holeaf.mobile.R.id.username))
        val password = onView(withId(com.holeaf.mobile.R.id.password))
        val login = onView(withId(com.holeaf.mobile.R.id.login))
        username.perform(ViewActions.typeText("courier"))
        password.perform(ViewActions.typeText("password"))
            .perform(ViewActions.closeSoftKeyboard())

        Intents.init()
        login.perform(click())
        Espresso.onIdle()
        Log.i("AS", "Waiting for main activity")
        Intents.intended(IntentMatchers.hasComponent(MainActivity::class.java.name))
        Intents.release()

        val nav_orders = onView(withId(com.holeaf.mobile.R.id.nav_orders))
        nav_orders.perform(click())

        val order_list_items = onView(withId(com.holeaf.mobile.R.id.order_list_items))

        order_list_items.check(matches(isDisplayed()))

        logoutFromApp()
    }

}