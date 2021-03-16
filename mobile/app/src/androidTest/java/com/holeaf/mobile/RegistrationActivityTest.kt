package com.holeaf.mobile

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import com.holeaf.mobile.ui.login.LoginActivity
import io.qameta.allure.android.rules.LogcatRule
import io.qameta.allure.android.rules.ScreenshotRule
import io.qameta.allure.android.runners.AllureAndroidJUnit4
import io.qameta.allure.kotlin.Description
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.koin.test.KoinTest

@RunWith(AllureAndroidJUnit4::class)
class RegistrationActivityTest : KoinTest {
    @get:Rule
    val ruleChain: RuleChain = RuleChain.outerRule(activityScenarioRule<LoginActivity>())
            .around(LogcatRule("logcat.txt"))
            .around(
                    ScreenshotRule(
                            mode = ScreenshotRule.Mode.SUCCESS,
                            screenshotName = "screenshot-success"
                    )
            ).around(
                    ScreenshotRule(
                            mode = ScreenshotRule.Mode.FAILURE,
                            screenshotName = "screenshot-failure"
                    )
            )




    @Test
    @Description("Проверка наличия элементов интерфейса")
    fun testDesign() {


        val username = onView(withId(R.id.username))
        val email = onView(withId(R.id.email))
        val password = onView(withId(R.id.password))
        val password_confirm = onView(withId(R.id.password_confirm))
        val register = onView(withId(R.id.register))              //button!!!
        val imageView = onView(withId(R.id.imageView2))
        val registration = onView(withId(R.id.registration))

        registration.perform(click())
        Thread.sleep(1000)
        username.check(matches(isDisplayed()))
        email.check(matches(isDisplayed()))
        password.check(matches(isDisplayed()))
        password_confirm.check(matches(isDisplayed()))
        register.check(matches(isDisplayed()))
        imageView.check(matches(isDisplayed()))
    }

}