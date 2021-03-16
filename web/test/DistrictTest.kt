import com.codeborne.selenide.Condition
import io.qameta.allure.Description
import org.junit.jupiter.api.Test

class DistrictTest:BasicTest() {
    @Test
    @Description("Проверка управления районами")
    fun testDistrict() {
        val districtPage = DistrictPage()

        webTest {
            login("admin", "12345678")
            districtPage.apply {
                navLink.click()
                header.shouldHave(Condition.exactText("Управление районами"))
                //add
                addNewDistrictButton.shouldBe(Condition.visible)
                addNewDistrictButton.click()
                districtLink.click()
                saveButton.shouldBe(Condition.exist)
                //changes
                labelDistrictName.shouldBe(Condition.visible)
                labelLatitudeTop.shouldBe(Condition.visible)
                labelLongitudeTop.shouldBe(Condition.visible)
                labelLatitudeBot.shouldBe(Condition.visible)
                labelLongitudeBot.shouldBe(Condition.visible)
                districtName.clear()
                districtName.sendKeys("test")
                latitudeTop.clear()
                latitudeTop.sendKeys("1")
                longitudeTop.clear()
                longitudeTop.sendKeys("2")
                latitudeBot.clear()
                latitudeBot.sendKeys("3")
                longitudeBot.clear()
                longitudeBot.sendKeys("4")
                saveButton.click()
                districtLink.click()
                districtName.shouldHave(Condition.value("test"))
                latitudeTop.shouldHave(Condition.value("1"))
                longitudeTop.shouldHave(Condition.value("2"))
                latitudeBot.shouldHave(Condition.value("3"))
                longitudeBot.shouldHave(Condition.value("4"))
                courierInput.shouldBe(Condition.visible)
                addCourierButton.click()
                courierAdded.shouldBe(Condition.visible)
                deleteCourierButton.click()
                saveButton.click()
                //delete
                deleteDistrictButton.shouldBe(Condition.visible)
                deleteDistrictButton.click()
            }
            logout()
        }
    }
}