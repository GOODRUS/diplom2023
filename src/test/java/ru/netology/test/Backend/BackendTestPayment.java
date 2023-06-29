package ru.netology.test.Backend;

import com.codeborne.selenide.logevents.SelenideLogger;

import io.qameta.allure.selenide.AllureSelenide;
import io.qameta.allure.*;

import org.junit.jupiter.api.*;
import ru.netology.data.DataHelper;
import ru.netology.data.DBHelper;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static ru.netology.data.APIHelper.postRequest;
import static ru.netology.data.DBHelper.cleanDatabase;
import static ru.netology.data.DataHelper.*;



public class BackendTestPayment {
    private static DataHelper.CardInfo cardInfo;
    private static final String paymentUrl = "api/v1/pay";
    private static final String status200 = "200";
    private static final String status400 = "400";
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
        String path = paymentUrl;
        String status = status200;
        postRequest(cardInfo, path, status);

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
    @DisplayName("Sending a POST request with a declined card when paying for a tour")
    public void shouldSadPathPayment() {
        cardInfo = DataHelper.getValidDeclinedCard();
        String path = paymentUrl;
        String status = status400;
        postRequest(cardInfo, path, status);

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
    @DisplayName("Sending a POST request with an empty body when paying for a tour")
    public void shouldStatus400WithEmptyBodyPayment() {
        cardInfo = new DataHelper.CardInfo(null, null, null, null, null);
        String path = paymentUrl;
        String status = status400;
        postRequest(cardInfo, path, status);

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
        cardInfo = new DataHelper.CardInfo(null, getValidRandomMonth(), getValidRandomYear(), generateValidRandomCardsHolder(), generateRandomCVV());
        String path = paymentUrl;
        String status = status400;
        postRequest(cardInfo, path, status);

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
        String path = paymentUrl;
        String status = status400;
        postRequest(cardInfo, path, status);

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
        String path = paymentUrl;
        String status = status400;
        postRequest(cardInfo, path, status);

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
        String path = paymentUrl;
        String status = status400;
        postRequest(cardInfo, path, status);

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
        String path = paymentUrl;
        String status = status400;
        postRequest(cardInfo, path, status);

        payments = DBHelper.getPayments();
        credits = DBHelper.getCreditsRequest();
        orders = DBHelper.getOrders();

        assertEquals(0, payments.size());
        assertEquals(0, credits.size());
        assertEquals(0, orders.size());
    }
}


