import com.codeborne.selenide.Condition
import io.qameta.allure.Description
import org.junit.jupiter.api.Test

class KeysTest: BasicTest() {
    @Test
    @Description("Проверка управления ключами шифрования")
    fun testKeys() {
        webTest {
            login("admin", "12345678")
            keysPage.apply {
                navLink.click()
                header.shouldHave(Condition.exactText("Управление ключами шифрования"))
                inputKeysTitle.shouldBe(Condition.visible)
                inputKeys.sendKeys("unknownuser")
                requestButton.click()
                requestError.shouldHave(Condition.exactText("Пользователь не обнаружен"))
                inputKeys.sendKeys("admin")
                requestButton.click()
                answerForm.shouldBe(Condition.visible)
            }
            logout()
        }
    }
}