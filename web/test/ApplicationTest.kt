import com.codeborne.selenide.Configuration
import com.codeborne.selenide.Selenide
import com.codeborne.selenide.logevents.SelenideLogger
import com.holeaf.module
import com.holeaf.webtest.*
import io.ktor.server.engine.*
import io.ktor.server.jetty.*
import io.qameta.allure.selenide.AllureSelenide
import org.junit.jupiter.api.BeforeEach
import org.openqa.selenium.By

val prefix = "http://localhost:8080"

class LoginPage {
    val logoImage = Selenide.element("img")
    val loginTextField = Selenide.element("input[name=username]")
    val passwordTextField = Selenide.element("input[name=password]")
    val loginButton = Selenide.element("button")
    val messageText = Selenide.element(".alert-danger")
    val titleText = Selenide.element("h1")

    val rows = Selenide.elements("tr")
}

open class InnerPage {
    val header = Selenide.element(".header-text")
    val sidebarCollapse = Selenide.element("#sidebarCollapse")
    val sidebar = Selenide.element("#sidebar")
}

class SideBar {
    val profile = Selenide.element("a[href='/profile']")
    val districts = Selenide.element("a[href='/districts']")
    val cars = Selenide.element("a[href='/cars']")
    val keys = Selenide.element("a[href='/keys']")
    val reports = Selenide.element("a[href='/reports']")
    val supply = Selenide.element("a[href='/supply']")
    val store = Selenide.element("a[href='/store']")
    val users = Selenide.element("a[href='/users']")
}

class ProfilePage : InnerPage() {
    val logoutButton = Selenide.element(".btn-danger")
    val username = Selenide.element(By.xpath("//table//tr[1]/td[2]"))
    val email = Selenide.element(By.xpath("//table//tr[2]/td[2]"))
    val role = Selenide.element(By.xpath("//table//tr[3]/td[2]"))
}

class DistrictPage : InnerPage() {
    val navLink = Selenide.element("a[href='/districts']")
    val addNewDistrictButton = Selenide.element(".internal-button")
    val districtLink = Selenide.element("a[href='/districts/3']")
    val deleteDistrictButton = Selenide.element("a[href='/districts/3/delete']")
    val labelDistrictName = Selenide.element("label[for='title']")
    val districtName = Selenide.element("#title")
    val labelLatitudeTop = Selenide.element("label[for='latitude_top']")
    val latitudeTop = Selenide.element("#latitude_top")
    val labelLongitudeTop = Selenide.element("label[for='longitude_top']")
    val longitudeTop = Selenide.element("#longitude_top")
    val labelLatitudeBot = Selenide.element("label[for='latitude_bot']")
    val latitudeBot = Selenide.element("#latitude_bot")
    val labelLongitudeBot = Selenide.element("label[for='longitude_bot']")
    val longitudeBot = Selenide.element("#longitude_bot")
    val saveButton = Selenide.element("#save_changes")
    val courierInput = Selenide.element("select[name='courier']")
    val addCourierButton = Selenide.element("#add_courier")
    val courierAdded = Selenide.element(By.xpath("//table//tr[1]/td[1]"))
    val deleteCourierButton = Selenide.element(By.xpath("//table//tr[1]/td[2]"))
}

class CarsPage : InnerPage() {
    val navLink = Selenide.element("a[href='/cars']")
    val chooseCarTitle = Selenide.element(".internal-header")
    val addNewCarButton = Selenide.element(".internal-button")
    val newCarLink = Selenide.element("a[href='/cars/3']")
    val labelCarTelemetry = Selenide.element("label[for='car-telemetry']")
    val carTelemetry = Selenide.element("#car-telemetry")
    val alert = Selenide.element(".alert-danger")
    val saveTelemetryButton = Selenide.element(".internal-button")
    val deleteCarLink = Selenide.element("a[href='/cars/3/delete']")
}

class KeysPage : InnerPage() {
    val navLink = Selenide.element("a[href='/keys']")
    val inputKeysTitle = Selenide.element(".internal-header")
    val inputKeys = Selenide.element(".form-control")
    val requestButton = Selenide.element(".internal-button")
    val requestError = Selenide.element(".internal-header-error")
    val answerForm = Selenide.element("#answer")
}

class ReportPage : InnerPage() {
    val navLink = Selenide.element("#reports")
    val reportTitle = Selenide.element(".internal-header")
    val fromLabel = Selenide.element("label[for='from']")
    val toLabel = Selenide.element("label[for='to']")
    val datePickerFrom = Selenide.element("input[name=from]")
    val datePickerTo = Selenide.element("input[name=to]")
    val productReportButton = Selenide.element("#productReport")
    val dailyReportButton = Selenide.element("#dailyReport")
    val reportHeader = Selenide.element(".report-header")
    val reportRows = Selenide.elements("tr")
}

class SupplyPage : InnerPage() {
    val navLink = Selenide.element("a[href='/supply']")
    val supplyTitle = Selenide.element(".internal-header")
    val addNewSupply = Selenide.element(".internal-button")
    val supplyLink = Selenide.element("a[href='/supply/3']")
    val supplyTable = Selenide.element(".table")
    val labelGoodsInput = Selenide.element("label[for='amount']")
    val goodsInput = Selenide.element("input[name='amount']")
    val labelGoodsAmount = Selenide.element("label[for='amount']")
    val goodsAmount = Selenide.element("input[name='amount']")
    val labelGoodsPrice = Selenide.element("label[for='price']")
    val goodsPrice = Selenide.element("input[name='price']")
    val addGoodsButton = Selenide.element("input[value='Добавить товар']")
    val deleteGoodsButton = Selenide.element("a[href='/supply/3/1/delete']")
    val createSupplyButton = Selenide.element("input[name='create']")
    val deleteSupplyLink = Selenide.element("a[href='/supply/3/delete']")

}

class StorePage : InnerPage() {
    val navLink = Selenide.element("a[href='/store']")
    val goodsStoreTitle = Selenide.element(".internal-header")
    val addNewGoods = Selenide.element(".internal-button")
    val goodsLink = Selenide.element("a[href='/store/6']")
    val goodsInfoTitle = Selenide.element(".internal-header")
    val labelGoodsTitle = Selenide.element("label[for='title']")
    val goodsTitle = Selenide.element("input[name='title']")
    val labelGoodsDescription = Selenide.element("label[for='description']")
    val goodsDescription = Selenide.element("textarea[name='description']")
    val labelGoodsPrice = Selenide.element("label[for='price']")
    val goodsPrice = Selenide.element("input[name='price']")
    val labelGoodsAmount = Selenide.element("label[for='amount']")
    val goodsAmount = Selenide.element("input[name='amount']")
    val labelGoodsPhoto = Selenide.element("label[for='photo']")
    val goodsPhoto = Selenide.element("input[name='photo']")
    val saveGoodsButton = Selenide.element(".internal-button")
    val deleteGoodsLink = Selenide.element("a[href='/store/6/delete']")
}

class UsersPage : InnerPage() {
    val navLink = Selenide.element("a[href='/users']")
    val userLink = Selenide.element("a[href='/users/52']")
    val labelUserName = Selenide.element("label[for='title']")
    val userName = Selenide.element("input[name='username']")
    val labelUserEmail = Selenide.element("label[for='email']")
    val userEmail = Selenide.element("input[name='email']")
    val labelUserRole = Selenide.element("label[for='role']")
    val userRole = Selenide.element("select[name='role']")
    val saveUserButton = Selenide.element(".internal-button")
}

open class BasicTest {
    val sideBar = SideBar()
    val loginPage = LoginPage()
    val profilePage = ProfilePage()
    val keysPage = KeysPage()
    val reportPage = ReportPage()
    val supplyPage = SupplyPage()
    val storePage = StorePage()
    val usersPage = UsersPage()
    val carsPage = CarsPage()

    @BeforeEach
    fun setUpAllure() {
        SelenideLogger.addListener("allure", AllureSelenide())
    }

    @BeforeEach
    fun setUp() {
        Configuration.headless = true
        Configuration.browserSize = "1920x1080"
        Configuration.startMaximized = false
        Configuration.timeout = 10000
    }

    fun login(user: String, password: String) {
        loginPage.apply {
            loginTextField.sendKeys(user)
            passwordTextField.sendKeys(password)
            loginButton.click()
        }
    }

    fun logout() {
        Selenide.open("$prefix/profile")
        profilePage.apply {
            logoutButton.click()
        }
    }

    fun webTest(func: () -> Unit) {
        val env = applicationEngineEnvironment {
            module {
                module(testing = true)
            }
            connector {
                host = "127.0.0.1"
                port = 8080
            }
        }
        val engine = embeddedServer(Jetty, env).start(false)

        Selenide.open("$prefix/")
        func()
        engine.stop(300, 300)
        Thread.sleep(300)
    }
}
