package com.holeaf.mobile

import android.util.Log
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
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
class RoleAccessTest : LoggedInTest() {
    @Test
    @Description("Проверка доступа курьера")
    fun CourierAcc() {

        val username = onView(withId(R.id.username))
        val password = onView(withId(R.id.password))
        val login = onView(withId(R.id.login))
        username.perform(ViewActions.typeText("courier"))
        password.perform(ViewActions.typeText("password"))
            .perform(ViewActions.closeSoftKeyboard())

        Intents.init()
        login.perform(click())
        Espresso.onIdle()
        Log.i("AS", "Waiting for main activity")
        Intents.intended(IntentMatchers.hasComponent(MainActivity::class.java.name))
        Intents.release()

        Thread.sleep(1000)
        val nav_orders = onView(withId(R.id.nav_orders))
        val nav_map = onView(withId(R.id.nav_map))
        val nav_profile = onView(withId(R.id.nav_profile))
        val nav_shop = onView(withId(R.id.nav_shop))
        val nav_chat = onView(withId(R.id.nav_chat))

        nav_orders.check(matches(isDisplayed()))
        nav_map.check(matches(isDisplayed()))
        nav_profile.check(matches(isDisplayed()))
        nav_shop.check(doesNotExist())
        nav_chat.check(matches(isDisplayed()))

        logoutFromApp()
        Thread.sleep(1000)
    }

    @Test
    @Description("Проверка доступа клиента")
    fun ClientAcc() {

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

        Thread.sleep(1000)
        val nav_orders = onView(withId(R.id.nav_orders))
        val nav_map = onView(withId(R.id.nav_map))
        val nav_profile = onView(withId(R.id.nav_profile))
        val nav_shop = onView(withId(R.id.nav_shop))
        val nav_chat = onView(withId(R.id.nav_chat))

        nav_orders.check(matches(isDisplayed()))
        nav_map.check(matches(isDisplayed()))
        nav_profile.check(matches(isDisplayed()))
        nav_shop.check(matches(isDisplayed()))
        nav_chat.check(matches(isDisplayed()))

        logoutFromApp()
        Thread.sleep(2000)
    }

    @Test
    @Description("Проверка доступа продавца")
    fun SellerAcc() {

        val username = onView(withId(R.id.username))
        val password = onView(withId(R.id.password))
        val login = onView(withId(R.id.login))
        username.perform(ViewActions.typeText("seller"))
        password.perform(ViewActions.typeText("password"))
            .perform(ViewActions.closeSoftKeyboard())

        Intents.init()
        login.perform(click())
        Espresso.onIdle()
        Log.i("AS", "Waiting for main activity")
        Intents.intended(IntentMatchers.hasComponent(MainActivity::class.java.name))
        Intents.release()

        Thread.sleep(1000)
        val nav_orders = onView(withId(R.id.nav_orders))
        val nav_map = onView(withId(R.id.nav_map))
        val nav_profile = onView(withId(R.id.nav_profile))
        val nav_shop = onView(withId(R.id.nav_shop))
        val nav_chat = onView(withId(R.id.nav_chat))

        nav_orders.check(doesNotExist())
        nav_map.check(matches(isDisplayed()))
        nav_profile.check(matches(isDisplayed()))
        nav_shop.check(doesNotExist())
        nav_chat.check(matches(isDisplayed()))

        logoutFromApp()
        Thread.sleep(2000)
    }

    @Test
    @Description("Проверка доступа лейтенанта")
    fun ManagerAcc() {

        val username = onView(withId(R.id.username))
        val password = onView(withId(R.id.password))
        val login = onView(withId(R.id.login))
        username.perform(ViewActions.typeText("manager"))
        password.perform(ViewActions.typeText("password"))
            .perform(ViewActions.closeSoftKeyboard())

        Intents.init()
        login.perform(click())
        Espresso.onIdle()
        Log.i("AS", "Waiting for main activity")
        Intents.intended(IntentMatchers.hasComponent(MainActivity::class.java.name))
        Intents.release()

        Thread.sleep(1000)
        val nav_orders = onView(withId(R.id.nav_orders))
        val nav_map = onView(withId(R.id.nav_map))
        val nav_profile = onView(withId(R.id.nav_profile))
        val nav_shop = onView(withId(R.id.nav_shop))
        val nav_chat = onView(withId(R.id.nav_chat))

        nav_orders.check(doesNotExist())
        nav_map.check(matches(isDisplayed()))
        nav_profile.check(matches(isDisplayed()))
        nav_shop.check(doesNotExist())
        nav_chat.check(matches(isDisplayed()))

        logoutFromApp()
        Thread.sleep(2000)
    }

    @Test
    @Description("Проверка доступа поставщика")
    fun SupplierAcc() {

        val username = onView(withId(R.id.username))
        val password = onView(withId(R.id.password))
        val login = onView(withId(R.id.login))
        username.perform(ViewActions.typeText("supplier"))
        password.perform(ViewActions.typeText("password"))
            .perform(ViewActions.closeSoftKeyboard())

        Intents.init()
        login.perform(click())
        Espresso.onIdle()
        Log.i("AS", "Waiting for main activity")
        Intents.intended(IntentMatchers.hasComponent(MainActivity::class.java.name))
        Intents.release()

        Thread.sleep(1000)
        val nav_orders = onView(withId(R.id.nav_orders))
        val nav_map = onView(withId(R.id.nav_map))
        val nav_profile = onView(withId(R.id.nav_profile))
        val nav_shop = onView(withId(R.id.nav_shop))
        val nav_chat = onView(withId(R.id.nav_chat))

        nav_orders.check(doesNotExist())
        nav_map.check(doesNotExist())
        nav_profile.check(matches(isDisplayed()))
        nav_shop.check(doesNotExist())
        nav_chat.check(matches(isDisplayed()))

        logoutFromApp()
        Thread.sleep(2000)
    }

    @Test
    @Description("Проверка доступа  администратора")
    fun AdminAcc() {

        val username = onView(withId(R.id.username))
        val password = onView(withId(R.id.password))
        val login = onView(withId(R.id.login))
        username.perform(ViewActions.typeText("admin"))
        password.perform(ViewActions.typeText("password"))
            .perform(ViewActions.closeSoftKeyboard())

        Intents.init()
        login.perform(click())
        Espresso.onIdle()
        Log.i("AS", "Waiting for main activity")
        Intents.intended(IntentMatchers.hasComponent(MainActivity::class.java.name))
        Intents.release()

        Thread.sleep(1000)
        val nav_orders = onView(withId(R.id.nav_orders))
        val nav_map = onView(withId(R.id.nav_map))
        val nav_profile = onView(withId(R.id.nav_profile))
        val nav_shop = onView(withId(R.id.nav_shop))
        val nav_chat = onView(withId(R.id.nav_chat))

        nav_orders.check(matches(isDisplayed()))
        nav_map.check(matches(isDisplayed()))
        nav_profile.check(matches(isDisplayed()))
        nav_shop.check(matches(isDisplayed()))
        nav_chat.check(matches(isDisplayed()))

        logoutFromApp()
        Thread.sleep(2000)
    }
}