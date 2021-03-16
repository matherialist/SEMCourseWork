import com.codeborne.selenide.Condition
import io.qameta.allure.Description
import org.junit.jupiter.api.Test

class UsersTest: BasicTest() {
    @Test
    @Description("Проверка управления пользователями")
    fun testUsers() {
        webTest {
            login("admin", "password")
            usersPage.apply {
                navLink.click()
                header.shouldHave(Condition.exactText("Управление пользователями"))
                userLink.click()
                labelUserName.shouldBe(Condition.visible)
                userName.shouldBe(Condition.visible)
                labelUserEmail.shouldBe(Condition.visible)
                userEmail.shouldHave(Condition.value("courier2@test.com"))
                userRole.shouldHave(Condition.value("3"))
                userEmail.clear()
                userEmail.sendKeys("courier3@test.com")
                labelUserRole.shouldBe(Condition.visible)
                userRole.selectOptionByValue("0")
                saveUserButton.click()
                userLink.click()
                userEmail.shouldHave(Condition.value("courier3@test.com"))
                userRole.shouldHave(Condition.value("0"))
                userEmail.clear()
                userEmail.sendKeys("courier2@test.com")
                userRole.selectOptionByValue("3")
                saveUserButton.click()
            }
            logout()
        }
    }
}