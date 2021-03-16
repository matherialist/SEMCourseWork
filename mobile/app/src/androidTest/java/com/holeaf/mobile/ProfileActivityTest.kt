package com.holeaf.mobile

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import io.qameta.allure.android.runners.AllureAndroidJUnit4
import io.qameta.allure.kotlin.Description
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AllureAndroidJUnit4::class)
class ProfileActivityTest : LoggedInTest() {
    @Test
    @Description("Проверка наличия элементов интерфейса")
    fun testDesign() {

        loginToApp()

        val nav_profile = onView(withId(R.id.nav_profile))
        nav_profile.perform(click())

        val usernameProfile = onView(withId(R.id.username))
        val email = onView(withId(R.id.email))
        val user_avatar = onView(withId(R.id.user_avatar))
        val exit = onView(withId(R.id.exit))

        usernameProfile.check(matches(isDisplayed()))
        email.check(matches(isDisplayed()))
        user_avatar.check(matches(isDisplayed()))
        exit.check(matches(isDisplayed()))
        exit.check(matches(isClickable()))

        logoutFromApp()
    }

}