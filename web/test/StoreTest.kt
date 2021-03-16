import com.codeborne.selenide.Condition
import io.qameta.allure.Description
import org.junit.jupiter.api.Test

class StoreTest: BasicTest() {
    @Test
    @Description("Проверка управления товаром")
    fun testStore() {
        webTest {
            login("admin", "password")
            storePage.apply {
                navLink.click()
                header.shouldHave(Condition.exactText("Управление ассортиментом товаров"))
                goodsStoreTitle.shouldBe(Condition.visible)
                addNewGoods.click()
                goodsLink.click()
                goodsInfoTitle.shouldBe(Condition.visible)
                labelGoodsTitle.shouldBe(Condition.visible)
                goodsTitle.clear()
                goodsTitle.sendKeys("New Goods")
                goodsTitle.shouldHave(Condition.value("New Goods"))
                labelGoodsDescription.shouldBe(Condition.visible)
                goodsDescription.clear()
                goodsDescription.sendKeys("12345678 Fresh New Goods")
                goodsDescription.shouldHave(Condition.value("12345678 Fresh New Goods"))
                labelGoodsPrice.shouldBe(Condition.visible)
                goodsPrice.clear()
                goodsPrice.sendKeys("1000")
                goodsPrice.shouldHave(Condition.value("1000"))
                labelGoodsAmount.shouldBe(Condition.visible)
                goodsAmount.clear()
                goodsAmount.sendKeys("10")
                goodsAmount.shouldHave(Condition.value("10"))
                labelGoodsPhoto.shouldBe(Condition.visible)
                goodsPhoto.shouldBe(Condition.visible)
                saveGoodsButton.click()
                deleteGoodsLink.click()
            }
            logout()
        }
    }
}