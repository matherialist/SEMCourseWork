import com.codeborne.selenide.Condition
import io.qameta.allure.Description
import org.junit.jupiter.api.Test

class CarsTest:BasicTest() {
    @Test
    @Description("Проверка управления машинами")
    fun testCars() {

        webTest {
            login("admin", "12345678")
            carsPage.apply {
                navLink.click()
                header.shouldHave(Condition.exactText("Управление машинами"))
                chooseCarTitle.shouldBe(Condition.visible)
                addNewCarButton.click()
                newCarLink.click()
                labelCarTelemetry.shouldBe(Condition.visible)
                carTelemetry.clear()
                carTelemetry.sendKeys("[")
                saveTelemetryButton.click()
                alert.shouldBe(Condition.visible)
                carTelemetry.clear()
                carTelemetry.sendKeys("[]")
                saveTelemetryButton.click()
                deleteCarLink.click()
            }
            logout()
        }
    }
}
