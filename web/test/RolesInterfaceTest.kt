import com.codeborne.selenide.Condition
import io.qameta.allure.Description
import org.junit.jupiter.api.Test

class RolesInterfaceTest: BasicTest() {
    @Test
    @Description("Проверка схлопывания сайдбара")
    fun testCollapsingSidebar() {
        webTest {
            login("admin", "12345678")
            profilePage.apply {
                sidebar.shouldNotHave(Condition.cssClass("active"))
                sidebarCollapse.click()
                sidebar.shouldHave(Condition.cssClass("active"))
                sidebarCollapse.click()
            }
            logout()
        }
    }

    @Test
    @Description("Проверка доступных страниц для продавца")
    fun checkSellerAvailablePages() {
        webTest {
            login("seller", "password")
            sideBar.apply {
                profile.shouldBe(Condition.exist)
                districts.shouldBe(Condition.exist)
                cars.shouldNotBe(Condition.exist)
                reports.shouldBe(Condition.exist)
                supply.shouldBe(Condition.exist)
                store.shouldBe(Condition.exist)
                keys.shouldNotBe(Condition.exist)
                users.shouldNotBe(Condition.exist)
            }
            logout()
        }
    }

    @Test
    @Description("Проверка доступных страниц для поставщика")
    fun checkSupplierAvailablePages() {
        webTest {
            login("supplier", "password")
            sideBar.apply {
                profile.shouldBe(Condition.exist)
                districts.shouldNotBe(Condition.exist)
                cars.shouldNotBe(Condition.exist)
                reports.shouldNotBe(Condition.exist)
                supply.shouldBe(Condition.exist)
                store.shouldNotBe(Condition.exist)
                keys.shouldNotBe(Condition.exist)
                users.shouldNotBe(Condition.exist)
            }
            logout()
        }
    }

    @Test
    @Description("Проверка доступных страниц для капитана")
    fun checkRegulatorAvailablePages() {
        webTest {
            login("regulator", "password")
            sideBar.apply {
                profile.shouldBe(Condition.exist)
                districts.shouldNotBe(Condition.exist)
                cars.shouldBe(Condition.exist)
                reports.shouldBe(Condition.exist)
                supply.shouldNotBe(Condition.exist)
                store.shouldNotBe(Condition.exist)
                keys.shouldBe(Condition.exist)
                users.shouldNotBe(Condition.exist)
            }
            logout()
        }
    }

    @Test
    @Description("Проверка доступных страниц для курьера")
    fun checkCourierAvailablePages() {
        webTest {
            login("courier", "password")
            sideBar.apply {
                profile.shouldBe(Condition.exist)
                districts.shouldNotBe(Condition.exist)
                cars.shouldNotBe(Condition.exist)
                reports.shouldNotBe(Condition.exist)
                supply.shouldNotBe(Condition.exist)
                store.shouldNotBe(Condition.exist)
                keys.shouldNotBe(Condition.exist)
                users.shouldNotBe(Condition.exist)
            }
            logout()
        }
    }

    @Test
    @Description("Проверка доступных страниц для клиента")
    fun checkClientAvailablePages() {
        webTest {
            login("courier", "password")
            sideBar.apply {
                profile.shouldBe(Condition.exist)
                districts.shouldNotBe(Condition.exist)
                cars.shouldNotBe(Condition.exist)
                reports.shouldNotBe(Condition.exist)
                supply.shouldNotBe(Condition.exist)
                store.shouldNotBe(Condition.exist)
                keys.shouldNotBe(Condition.exist)
                users.shouldNotBe(Condition.exist)
            }
            logout()
        }
    }
}