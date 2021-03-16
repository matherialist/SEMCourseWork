package com.holeaf.mobile

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import com.holeaf.mobile.ui.login.LoginActivity
import io.qameta.allure.android.rules.LogcatRule
import io.qameta.allure.android.rules.ScreenshotRule
import io.qameta.allure.android.runners.AllureAndroidJUnit4
import io.qameta.allure.kotlin.Description
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.koin.test.KoinTest

@RunWith(AllureAndroidJUnit4::class)
class PassRecActivityTest : KoinTest {

    @get:Rule
    val ruleChain: RuleChain = RuleChain.outerRule(activityScenarioRule<LoginActivity>())
            .around(LogcatRule("logcat.txt"))
            .around(
                    ScreenshotRule(
                            mode = ScreenshotRule.Mode.SUCCESS,
                            screenshotName = "screenshot-success"
                    )
            ).around(ScreenshotRule(
                    mode = ScreenshotRule.Mode.FAILURE,
                    screenshotName = "screenshot-failure"
            ))



    @Test
    @Description("Проверка наличия элементов интерфейса")
    fun testDesign() {


        val email = onView(withId(R.id.email))
        val recovery = onView(withId(R.id.recovery))
        val imageView2 = onView(withId(R.id.imageView2))
        val editTextTextPersonName = onView(withId(R.id.editTextTextPersonName))              //button!!!
        val loading = onView(withId(R.id.loading))
        val editTextTextPersonName2 = onView(withId(R.id.editTextTextPersonName2))
        val password_recovery = onView(withId(R.id.password_recovery))

        password_recovery.perform(click())
        email.check(matches(isDisplayed()))
        recovery.check(matches(isDisplayed()))
        imageView2.check(matches(isDisplayed()))
        recovery.check(matches(isClickable()))
        editTextTextPersonName.check(matches(isDisplayed()))
        loading.check(matches(not(isDisplayed())))
        editTextTextPersonName2.check(matches(isDisplayed()))
    }

}