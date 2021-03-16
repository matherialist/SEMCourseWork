import com.codeborne.selenide.Condition
import io.qameta.allure.Description
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ReportsTest: BasicTest() {
    @Test
    @Description("Проверка отчетов")
    fun testReports() {
        webTest {
            login("admin", "12345678")
            reportPage.apply {
                navLink.click()
                header.shouldHave(Condition.exactText("Просмотр отчётов"))
                reportTitle.shouldBe(Condition.visible)
                fromLabel.shouldBe(Condition.visible)
                toLabel.shouldBe(Condition.visible)
                datePickerFrom.sendKeys("01.12.2020")
                datePickerTo.click()
                datePickerTo.sendKeys("27.12.2020")
                productReportButton.click()
                reportHeader.shouldHave(Condition.exactText("Отчёт по продажам товаров с 01.12.2020 по 27.12.2020"))
                assertEquals(2, reportRows.size)
                dailyReportButton.click()
                reportHeader.shouldHave(Condition.exactText("Ежедневный отчёт о продажах с 01.12.2020 по 27.12.2020"))
                assertEquals(2, reportRows.size)
            }
            logout()
        }
    }
}