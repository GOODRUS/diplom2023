package ru.netology.test;

import com.codeborne.selenide.logevents.SelenideLogger;
import com.google.gson.Gson;

import io.qameta.allure.selenide.AllureSelenide;
import io.qameta.allure.*;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import org.junit.jupiter.api.*;
import ru.netology.data.DataHelper;
import ru.netology.data.DBHelper;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;
import static ru.netology.data.DBHelper.cleanDatabase;
import static ru.netology.data.DataHelper.*;



public class BackendTest {
    private static DataHelper.CardInfo cardInfo;
    private static final Gson gson = new Gson();
    private static final RequestSpecification spec = new RequestSpecBuilder().setBaseUri("http://localhost").setPort(8080)
            .setAccept(ContentType.JSON).setContentType(ContentType.JSON).log(LogDetail.ALL).build();
    private static final String paymentUrl = "api/v1/pay";
    private static final String creditUrl = "api/v1/credit";
    private static List<DBHelper.PaymentEntity> payments;
    private static List<DBHelper.CreditRequestEntity> credits;
    private static List<DBHelper.OrderEntity> orders;

    @BeforeAll
    static void setUpAll() {
        SelenideLogger.addListener("allure", new AllureSelenide());
    }

    @AfterEach
    public void teardown() {
        cleanDatabase();
        cleanListNow();
    }

    @AfterAll
    static void tearDownAll() {
        SelenideLogger.removeListener("allure");
        cleanDatabase();
    }

    @Severity(SeverityLevel.BLOCKER)
    @Test
    @DisplayName("Sending a POST request with an approved card when paying for a tour")
    public void shouldHappyPathPayment() {
        cardInfo = DataHelper.getValidApprovedCard();
        var body = gson.toJson(cardInfo);
        given().spec(spec).body(body)
                .when().post(paymentUrl)
                .then().statusCode(200);

        payments = DBHelper.getPayments();
        credits = DBHelper.getCreditsRequest();
        orders = DBHelper.getOrders();
        assertEquals(1, payments.size());
        assertEquals(0, credits.size());
        assertEquals(1, orders.size());

        assertTrue(payments.get(0).getStatus().equalsIgnoreCase("approved"));
        assertEquals(payments.get(0).getTransaction_id(), orders.get(0).getPayment_id());
        assertNull(orders.get(0).getCredit_id());
    }

    @Severity(SeverityLevel.BLOCKER)
    @Test
    @DisplayName("Sending a POST request with an approved card when paying for a tour credit")
    public void shouldHappyPathCredit() {
        cardInfo = DataHelper.getValidApprovedCard();
        var body = gson.toJson(cardInfo);
        given().spec(spec).body(body)
                .when().post(creditUrl)
                .then().statusCode(200);

        payments = DBHelper.getPayments();
        credits = DBHelper.getCreditsRequest();
        orders = DBHelper.getOrders();

        assertEquals(0, payments.size());
        assertEquals(1, credits.size());
        assertEquals(1, orders.size());

        assertTrue(credits.get(0).getStatus().equalsIgnoreCase("approved"));
        assertEquals(credits.get(0).getBank_id(), orders.get(0).getPayment_id());
        assertNull(orders.get(0).getCredit_id());
    }

    @Severity(SeverityLevel.BLOCKER)
    @Test
    @DisplayName("Sending a POST request with a declined card when paying for a tour")
    public void shouldSadPathPayment() {
        cardInfo = DataHelper.getValidDeclinedCard();
        var body = gson.toJson(cardInfo);
        given().spec(spec).body(body)
                .when().post(paymentUrl)
                .then().statusCode(400);

        payments = DBHelper.getPayments();
        credits = DBHelper.getCreditsRequest();
        orders = DBHelper.getOrders();

        assertEquals(1, payments.size());
        assertEquals(0, credits.size());
        assertEquals(1, orders.size());

        assertTrue(payments.get(0).getStatus().equalsIgnoreCase("declined"));
        assertEquals(payments.get(0).getTransaction_id(), orders.get(0).getPayment_id());
        assertNull(orders.get(0).getCredit_id());
    }

    @Severity(SeverityLevel.BLOCKER)
    @Test
    @DisplayName("Sending a POST request with a declined card when paying on credit")
    public void shouldSadPathCredit() {
        cardInfo = DataHelper.getValidDeclinedCard();
        var body = gson.toJson(cardInfo);
        given().spec(spec).body(body)
                .when().post(creditUrl)
                .then().statusCode(400);

        payments = DBHelper.getPayments();
        credits = DBHelper.getCreditsRequest();
        orders = DBHelper.getOrders();

        assertEquals(0, payments.size());
        assertEquals(1, credits.size());
        assertEquals(1, orders.size());

        assertTrue(credits.get(0).getStatus().equalsIgnoreCase("declined"));
        assertEquals(credits.get(0).getBank_id(), orders.get(0).getPayment_id());
        assertNull(orders.get(0).getCredit_id());
    }

    @Severity(SeverityLevel.BLOCKER)
    @Test
    @DisplayName("Sending a POST request with an empty body when paying for a tour")
    public void shouldStatus400WithEmptyBodyPayment() {
        given().spec(spec)
                .when().post(paymentUrl)
                .then().statusCode(400);

        payments = DBHelper.getPayments();
        credits = DBHelper.getCreditsRequest();
        orders = DBHelper.getOrders();

        assertEquals(0, payments.size());
        assertEquals(0, credits.size());
        assertEquals(0, orders.size());
    }

    @Severity(SeverityLevel.BLOCKER)
    @Test
    @DisplayName("Sending a POST request with an empty body when paying on credit")
    public void shouldStatus400WithEmptyBodyCredit() {
        given().spec(spec)
                .when().post(creditUrl)
                .then().statusCode(400);

        payments = DBHelper.getPayments();
        credits = DBHelper.getCreditsRequest();
        orders = DBHelper.getOrders();

        assertEquals(0, payments.size());
        assertEquals(0, credits.size());
        assertEquals(0, orders.size());
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("Sending a POST request with an empty number in the body when paying")
    public void shouldStatus400WithEmptyNumberPayment() {
        cardInfo = new DataHelper.CardInfo(null, getValidRandomMonth(), getValidRandomYear(),
                generateValidRandomCardsHolder(), generateRandomCVV());
        var body = gson.toJson(cardInfo);
        given().spec(spec).body(body)
                .when().post(paymentUrl)
                .then().statusCode(400);

        payments = DBHelper.getPayments();
        credits = DBHelper.getCreditsRequest();
        orders = DBHelper.getOrders();

        assertEquals(0, payments.size());
        assertEquals(0, credits.size());
        assertEquals(0, orders.size());
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("Sending a POST request with an empty number in the body when credit")
    public void shouldStatus400WithEmptyNumberCredit() {
        cardInfo = new DataHelper.CardInfo(null, getValidRandomMonth(), getValidRandomYear(),
                generateValidRandomCardsHolder(), generateRandomCVV());
        var body = gson.toJson(cardInfo);
        given().spec(spec).body(body)
                .when().post(creditUrl)
                .then().statusCode(400);

        payments = DBHelper.getPayments();
        credits = DBHelper.getCreditsRequest();
        orders = DBHelper.getOrders();

        assertEquals(0, payments.size());
        assertEquals(0, credits.size());
        assertEquals(0, orders.size());
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("Sending a POST request with an empty month attribute in the body when paying")
    public void shouldStatus400WithEmptyMonthPayment() {
        getValidApprovedCard();
        cardInfo = new DataHelper.CardInfo(getNumberByStatus("approved"), null, getValidRandomYear(), generateValidRandomCardsHolder(), generateRandomCVV());

        var body = gson.toJson(cardInfo);
        given().spec(spec).body(body)
                .when().post(paymentUrl)
                .then().statusCode(400);

        payments = DBHelper.getPayments();
        credits = DBHelper.getCreditsRequest();
        orders = DBHelper.getOrders();

        assertEquals(0, payments.size());
        assertEquals(0, credits.size());
        assertEquals(0, orders.size());
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("Sending a POST request with an empty month attribute to the body when credit")
    public void shouldStatus400WithEmptyMonthCredit() {
        getValidApprovedCard();
        cardInfo = new DataHelper.CardInfo(getNumberByStatus("approved"), null, getValidRandomYear(), generateValidRandomCardsHolder(), generateRandomCVV());

        var body = gson.toJson(cardInfo);
        given().spec(spec).body(body)
                .when().post(creditUrl)
                .then().statusCode(400);

        payments = DBHelper.getPayments();
        credits = DBHelper.getCreditsRequest();
        orders = DBHelper.getOrders();

        assertEquals(0, payments.size());
        assertEquals(0, credits.size());
        assertEquals(0, orders.size());
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("Sending a POST request with an empty year attribute in the body when paying")
    public void shouldStatus400WithEmptyYearPayment() {

        cardInfo = new DataHelper.CardInfo(getNumberByStatus("approved"), getValidRandomMonth(), null, generateValidRandomCardsHolder(), generateRandomCVV());

        var body = gson.toJson(cardInfo);
        given().spec(spec).body(body)
                .when().post(paymentUrl)
                .then().statusCode(400);

        payments = DBHelper.getPayments();
        credits = DBHelper.getCreditsRequest();
        orders = DBHelper.getOrders();

        assertEquals(0, payments.size());
        assertEquals(0, credits.size());
        assertEquals(0, orders.size());
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("Sending a POST request with an empty year attribute in the body when credit")
    public void shouldStatus400WithEmptyYearCredit() {

        cardInfo = new DataHelper.CardInfo(getNumberByStatus("approved"), getValidRandomMonth(), null, generateValidRandomCardsHolder(), generateRandomCVV());

        var body = gson.toJson(cardInfo);
        given().spec(spec).body(body)
                .when().post(creditUrl)
                .then().statusCode(400);

        payments = DBHelper.getPayments();
        credits = DBHelper.getCreditsRequest();
        orders = DBHelper.getOrders();

        assertEquals(0, payments.size());
        assertEquals(0, credits.size());
        assertEquals(0, orders.size());
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("Sending a POST request with an empty holder attribute in the body when paying")
    public void shouldStatus400WithEmptyHolderPayment() {

        cardInfo = new DataHelper.CardInfo(getNumberByStatus("approved"), getValidRandomMonth(), getValidRandomYear(), null, generateRandomCVV());

        var body = gson.toJson(cardInfo);
        given().spec(spec).body(body)
                .when().post(paymentUrl)
                .then().statusCode(400);

        payments = DBHelper.getPayments();
        credits = DBHelper.getCreditsRequest();
        orders = DBHelper.getOrders();

        assertEquals(0, payments.size());
        assertEquals(0, credits.size());
        assertEquals(0, orders.size());
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("Sending a POST request with an empty holder attribute in the body when credit")
    public void shouldStatus400WithEmptyHolderCredit() {

        cardInfo = new DataHelper.CardInfo(getNumberByStatus("approved"), getValidRandomMonth(), getValidRandomYear(), null, generateRandomCVV());

        var body = gson.toJson(cardInfo);
        given().spec(spec).body(body)
                .when().post(creditUrl)
                .then().statusCode(400);

        payments = DBHelper.getPayments();
        credits = DBHelper.getCreditsRequest();
        orders = DBHelper.getOrders();

        assertEquals(0, payments.size());
        assertEquals(0, credits.size());
        assertEquals(0, orders.size());
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("Sending a POST request with an empty CVC/CVV attribute in the body when paying")
    public void shouldStatus400WithEmptyCVVPayment() {

        cardInfo = new DataHelper.CardInfo(getNumberByStatus("approved"), getValidRandomMonth(), getValidRandomYear(), generateValidRandomCardsHolder(), null);

        var body = gson.toJson(cardInfo);
        given().spec(spec).body(body)
                .when().post(paymentUrl)
                .then().statusCode(400);

        payments = DBHelper.getPayments();
        credits = DBHelper.getCreditsRequest();
        orders = DBHelper.getOrders();

        assertEquals(0, payments.size());
        assertEquals(0, credits.size());
        assertEquals(0, orders.size());
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("Sending a POST request with an empty CVC/CVV attribute in the body when credit")
    public void shouldStatus400WithEmptyCVVCredit() {

        cardInfo = new DataHelper.CardInfo(getNumberByStatus("approved"), getValidRandomMonth(), getValidRandomYear(), generateValidRandomCardsHolder(), null);

        var body = gson.toJson(cardInfo);
        given().spec(spec).body(body)
                .when().post(creditUrl)
                .then().statusCode(400);

        payments = DBHelper.getPayments();
        credits = DBHelper.getCreditsRequest();
        orders = DBHelper.getOrders();

        assertEquals(0, payments.size());
        assertEquals(0, credits.size());
        assertEquals(0, orders.size());
    }

}


