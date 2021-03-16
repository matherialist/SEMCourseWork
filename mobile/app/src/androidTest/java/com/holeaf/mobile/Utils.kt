package com.holeaf.mobile

import android.util.Log
import android.view.View
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.activityScenarioRule
import com.holeaf.mobile.ui.MainActivity
import com.holeaf.mobile.ui.login.LoginActivity
import io.qameta.allure.android.rules.LogcatRule
import io.qameta.allure.android.rules.ScreenshotRule
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.Rule
import org.junit.rules.RuleChain
import org.koin.test.KoinTest


open class LoggedInTest : KoinTest {
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

    fun loginToApp() {
        val username = Espresso.onView(ViewMatchers.withId(R.id.username))
        val password = Espresso.onView(ViewMatchers.withId(R.id.password))
        val login = Espresso.onView(ViewMatchers.withId(R.id.login))
        username.perform(ViewActions.typeText("admin"))
        password.perform(ViewActions.typeText("password"))
            .perform(ViewActions.closeSoftKeyboard())

        Intents.init()
        login.perform(ViewActions.click())
        Espresso.onIdle()
        Log.i("AS", "Waiting for main activity")
        Intents.intended(IntentMatchers.hasComponent(MainActivity::class.java.name))
        Intents.release()
    }

    fun logoutFromApp() {
        val nav_profile = Espresso.onView(ViewMatchers.withId(R.id.nav_profile))
        nav_profile.perform(ViewActions.click())
        val exit = Espresso.onView(ViewMatchers.withId(R.id.exit))
        exit.perform(ViewActions.click())
    }


    fun getElementFromMatchAtPosition(matcher: Matcher<View>, position: Int): Matcher<View?>? {
        return object : BaseMatcher<View?>() {
            var counter = 0
            override fun describeTo(description: Description?) {

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
}

