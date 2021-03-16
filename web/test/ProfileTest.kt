import com.codeborne.selenide.Condition
import io.qameta.allure.Description
import org.junit.jupiter.api.Test

class ProfileTest:BasicTest() {
    @Test
    @Description("Проверка страницы профиля")
    fun testProfile() {
        webTest {
            login("admin", "12345678")

            profilePage.apply {
                username.shouldBe(Condition.visible)
                username.shouldHave(Condition.exactText("admin"))
                email.shouldBe(Condition.visible)
                email.shouldHave(Condition.exactText("admin@test.com"))
                header.shouldHave(Condition.exactText("О пользователе"))
                role.shouldBe(Condition.visible)
                role.shouldHave(Condition.exactText("admin"))
            }
            //navigation.profile.shouldHave(cssClass("active"))
            logout()
        }
    }
}