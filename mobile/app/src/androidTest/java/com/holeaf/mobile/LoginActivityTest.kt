package com.holeaf.mobile

import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import com.holeaf.mobile.ui.MainActivity
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
class LoginActivityTest : KoinTest {

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
        Thread.sleep(1000)
        val username = onView(withId(R.id.username))
        val password = onView(withId(R.id.password))
        val login = onView(withId(R.id.login))              //button!!!
        val loading = onView(withId(R.id.loading))
        val imageView = onView(withId(R.id.imageView))
        val registration = onView(withId(R.id.registration))
        val password_recovery = onView(withId(R.id.password_recovery))

        username.check(matches(isDisplayed()))
        password.check(matches(isDisplayed()))
        login.check(matches(isDisplayed()))
        loading.check(matches(not(isDisplayed())))
        imageView.check(matches(isDisplayed()))
        registration.check(matches(isDisplayed()))
        password_recovery.check(matches(isDisplayed()))
    }

//    @Test
//    @Description("Проверка входа с неправильным паролем")
//
//    fun testInvalidLogin() {
//       val vm: LoginViewModel by viewModel()
//
//        val username = onView(withId(R.id.username))
//        val password = onView(withId(R.id.password))
//        val login = onView(withId(R.id.login))
//        username.perform(typeText("unknownuser"))
//        password.perform(typeText("12345678"))
//            .perform(closeSoftKeyboard())
//
//        Intents.init()
//        login.perform(click())
//        val loading = onView(withId(R.id.loading))
//        loading.check(matches(not(isDisplayed())))
//        Thread.sleep(1000)
//        intended(hasComponent(MainActivity::class.java.name), times(0))
//        Intents.release()
//        assert(vm.loginResult.value?.success == null)
//        assert(vm.loginResult.value?.error != 0)
//    }

    @Test
    @Description("Проверка входа с правильным паролем")
    fun testSuccessfulLogin() {
        val username = onView(withId(R.id.username))
        val password = onView(withId(R.id.password))
        val login = onView(withId(R.id.login))
        username.perform(typeText("client"))
        password.perform(typeText("password"))
            .perform(closeSoftKeyboard())

        Intents.init()
        login.perform(click())
        Thread.sleep(1000)
        intended(hasComponent(MainActivity::class.java.name))
        Intents.release()
        val nav_profile = Espresso.onView(ViewMatchers.withId(R.id.nav_profile))
        nav_profile.perform(ViewActions.click())
        val exit = Espresso.onView(ViewMatchers.withId(R.id.exit))
        exit.perform(ViewActions.click())
    }
}