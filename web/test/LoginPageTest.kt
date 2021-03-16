package com.holeaf.webtest

import BasicTest
import com.codeborne.selenide.Condition.*
import com.codeborne.selenide.Selenide.screenshot
import io.qameta.allure.Allure.step
import io.qameta.allure.Description
import org.junit.jupiter.api.Test


class LoginPageTest : BasicTest() {
    @Test
    @Description("Проверка страницы входа")
    fun checkLoginPage() {
        webTest {
            loginPage.apply {
                step {
                    logoImage.shouldBe(visible)
                    loginButton.shouldBe(visible)
                    loginTextField.shouldBe(visible)
                    passwordTextField.shouldBe(visible)
                    titleText.shouldBe(visible)
                    titleText.shouldHave(text("holeaf"))
                }
                step {
                    messageText.shouldNotBe(visible)

                    loginButton.click()
                    messageText.shouldBe(visible)
                    messageText.shouldHave(exactText("Вы ввели неверное имя или пароль."))
                    screenshot("invalid_authorization")
                }
                step {
                    loginTextField.sendKeys("admin")
                    passwordTextField.sendKeys("12345678")
                    loginButton.click()
                    messageText.shouldNotBe(exist)
                }
            }

            profilePage.apply {
                logoutButton.click()
                logoutButton.shouldNotBe(exist)
            }
        }
    }

//    @Test
//    @Description("Проверка отсутствия доступа у клиента")
//    fun loginClientNotAllowed() {
//        webTest {
//            loginPage.apply {
//                loginTextField.sendKeys("client")
//                passwordTextField.sendKeys("password")
//                loginButton.click()
//                messageText.shouldBe(visible)
//                messageText.shouldHave(exactText("Вы ввели неверное имя или пароль."))
//            }
//        }
//    }

    @Test
    @Description("Проверка отсутствия доступа у лейтенанта")
    fun loginManagerNotAllowed() {
        webTest {
            loginPage.apply {
                loginTextField.sendKeys("manager")
                passwordTextField.sendKeys("password")
                loginButton.click()
                messageText.shouldBe(visible)
                messageText.shouldHave(exactText("Вы ввели неверное имя или пароль."))
            }
        }
    }

//    @Test
//    @Description("Проверка отсутствия доступа у курьера")
//    fun loginCourierNotAllowed() {
//        webTest {
//            loginPage.apply {
//                loginTextField.sendKeys("courier")
//                passwordTextField.sendKeys("password")
//                loginButton.click()
//                messageText.shouldBe(visible)
//                messageText.shouldHave(exactText("Вы ввели неверное имя или пароль."))
//            }
//        }
//    }
}
