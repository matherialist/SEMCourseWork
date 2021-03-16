import com.codeborne.selenide.Condition
import io.qameta.allure.Description
import org.junit.jupiter.api.Test

class SupplyTest: BasicTest() {
    @Test
    @Description("Проверка управления поставками")
    fun testSupply() {
        webTest {
            login("admin", "12345678")
            supplyPage.apply {
                navLink.click()
                header.shouldHave(Condition.exactText("Поставки товара"))
                supplyTitle.shouldBe(Condition.visible)
                addNewSupply.click()
                supplyLink.click()
                supplyTable.shouldBe(Condition.visible)
                labelGoodsInput.shouldBe(Condition.visible)
                goodsInput.click()
                labelGoodsAmount.shouldBe(Condition.visible)
                goodsAmount.sendKeys("123")
                labelGoodsPrice.shouldBe(Condition.visible)
                goodsPrice.sendKeys("1000")
                addGoodsButton.click()
                deleteGoodsButton.click()
                createSupplyButton.shouldBe(Condition.visible)
                deleteSupplyLink.click()
            }
            logout()
        }
    }
}