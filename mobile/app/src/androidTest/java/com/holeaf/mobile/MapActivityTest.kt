package com.holeaf.mobile

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import io.qameta.allure.android.runners.AllureAndroidJUnit4
import io.qameta.allure.kotlin.Description
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AllureAndroidJUnit4::class)
class MapActivityTest : LoggedInTest() {




    @Test
    @Description("Проверка наличия элементов интерфейса")
    fun testDesign() {
        loginToApp()
        val map = onView(withId(R.id.map))

        map.check(matches(isDisplayed()))
        logoutFromApp()
    }

}