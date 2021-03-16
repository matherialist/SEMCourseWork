package com.holeaf.mobile

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import io.qameta.allure.android.runners.AllureAndroidJUnit4
import io.qameta.allure.kotlin.Description
import org.hamcrest.CoreMatchers
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AllureAndroidJUnit4::class)
class ChatActivityTest :  LoggedInTest() {

    @Test
    @Description("Проверка наличия элементов интерфейса")
    fun testDesign() {
        loginToApp()

        val chats = onView(withId(R.id.chats))
        val nav_chat = onView(withId(R.id.nav_chat))

        nav_chat.perform(click())
        chats.check(matches(isDisplayed()))
        logoutFromApp()
    }
    @Test
    @Description("Проверка отправки сообщения")
    fun testMessage() {
        loginToApp()

//        val chat_list_item_container = onView(withId(R.id.chat_list_item_container))
        val nav_chat = onView(withId(R.id.nav_chat))

        nav_chat.perform(click())


        Thread.sleep(2000)
        val chat_list_item_container = onView(
                CoreMatchers.allOf(
                        getElementFromMatchAtPosition(CoreMatchers.allOf(withId(R.id.chat_list_item_container)), 0),
                        isDisplayed()))


        chat_list_item_container.perform(click())
        Thread.sleep(2000)
        val newMessage = onView(withId(R.id.newMessage))

        newMessage.perform(ViewActions.typeText("test"))

        val sendMessage = onView(withId(R.id.sendMessage))
        sendMessage.perform(click())
        Thread.sleep(2000)
        val message =  onView(
                CoreMatchers.allOf(
                        getElementFromMatchAtPosition(CoreMatchers.allOf(withId(R.id.messages)), 0),
                        isDisplayed()))
        message.check(matches(isDisplayed()))
        logoutFromApp()
    }

}