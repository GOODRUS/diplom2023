package ru.netology.test;

import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.*;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.*;
import ru.netology.data.DataHelper;
import ru.netology.data.DBHelper;

import ru.netology.page.CardPage;
import ru.netology.page.FormPage;

import java.util.List;

import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.*;
import static ru.netology.data.DBHelper.cleanDatabase;
import static ru.netology.data.DataHelper.cleanListNow;


public class FrontendTest {
    private static DataHelper.CardInfo cardInfo;
    private static CardPage card;
    private static FormPage form;
    private static List<DBHelper.PaymentEntity> payments;
    private static List<DBHelper.CreditRequestEntity> credits;
    private static List<DBHelper.OrderEntity> orders;


    @BeforeAll
    static void setupAllure() {
        SelenideLogger.addListener("allure", new AllureSelenide());
        cleanDatabase();
    }

    @AfterEach
    public void clean() {
        cleanListNow();
    }

    @AfterAll
    static void teardown() {
        SelenideLogger.removeListener("allure");
        cleanDatabase();
    }

    @BeforeEach
    public void setupMethod() {
        open("http://localhost:8080/");
        card = new CardPage();
        cleanDatabase();
    }

    @Severity(SeverityLevel.BLOCKER)
    @Test
    @DisplayName("Should Happy Path Payment")
    void shouldHappyPathPayment() {
        cardInfo = DataHelper.getValidApprovedCard();

        form = card.clickPayButton();
        form.insertingValueInForm(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.assertBuyOperationIsSuccessful();

        payments = DBHelper.getPayments();
        credits = DBHelper.getCreditsRequest();
        orders = DBHelper.getOrders();
        assertEquals(1, payments.size());
        assertEquals(0, credits.size());
        assertEquals(1, orders.size());

        assertEquals(card.getAmount() * 100, payments.get(0).getAmount());
        assertTrue(payments.get(0).getStatus().equalsIgnoreCase("approved"));
        assertEquals(payments.get(0).getTransaction_id(), orders.get(0).getPayment_id());
        assertNull(orders.get(0).getCredit_id());
    }

    @Severity(SeverityLevel.BLOCKER)
    @Test
    @DisplayName("Should Happy Path Credit")
    public void shouldHappyPathCredit() {
        cardInfo = DataHelper.getValidApprovedCard();

        form = card.clickCreditButton();
        form.insertingValueInForm(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.assertBuyOperationIsSuccessful();

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
    @DisplayName("Should Sad Path")
    public void shouldSadPathPayment() {
        cardInfo = DataHelper.getValidDeclinedCard();

        form = card.clickPayButton();
        form.insertingValueInForm(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.assertBuyOperationWithErrorNotification();

        payments = DBHelper.getPayments();
        credits = DBHelper.getCreditsRequest();
        orders = DBHelper.getOrders();
        assertEquals(1, payments.size());
        assertEquals(0, credits.size());
        assertEquals(1, orders.size());

        assertEquals(card.getAmount() * 100, payments.get(0).getAmount());
        assertTrue(payments.get(0).getStatus().equalsIgnoreCase("declined"));
        assertEquals(payments.get(0).getTransaction_id(), orders.get(0).getPayment_id());
        assertNull(orders.get(0).getCredit_id());
    }

    @Severity(SeverityLevel.BLOCKER)
    @Test
    @DisplayName("Should Sad Path Credit")
    public void shouldSadPathCredit() {

        cardInfo = DataHelper.getValidDeclinedCard();

        form = card.clickCreditButton();
        form.insertingValueInForm(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.assertBuyOperationWithErrorNotification();

        payments = DBHelper.getPayments();
        credits = DBHelper.getCreditsRequest();
        orders = DBHelper.getOrders();
        assertEquals(0, payments.size());
        assertEquals(1, credits.size());
        assertEquals(1, orders.size());

        assertTrue(credits.get(0).getStatus().equalsIgnoreCase("declined"));
        assertEquals(credits.get(0).getBank_id(), orders.get(0).getPayment_id());
        assertEquals(credits.get(0).getId(), orders.get(0).getCredit_id());
    }

    @Severity(SeverityLevel.BLOCKER)
    @Test
    @DisplayName("shouldUnsuccessfulWith12DigitsInNumber")
    public void shouldUnsuccessfulWith12DigitsInNumber() {
        cardInfo = DataHelper.getValidApprovedCard();
        var number = DataHelper.generateRandomCardNumberTwelveDigits();
        var matchesNumber = number;

        form = card.clickPayButton();
        form.insertingValueInForm(number, cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.matchesByInsertValue(matchesNumber, cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.assertBuyOperationWithErrorNotification();
    }

    @Severity(SeverityLevel.BLOCKER)
    @Test
    @DisplayName("shouldUnsuccessfulWith19DigitsInNumber")
    public void shouldUnsuccessfulWith19DigitsInNumber() {
        cardInfo = DataHelper.getValidApprovedCard();
        var number = DataHelper.generateRandomCardNumberNineteenDigits();
        var matchesNumber = number;

        form = card.clickPayButton();
        form.insertingValueInForm(number, cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.matchesByInsertValue(matchesNumber, cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.assertBuyOperationWithErrorNotification();
    }

    @Severity(SeverityLevel.BLOCKER)
    @Test
    @DisplayName("shouldUnsuccessfulWith16DigitsInNumber")
    public void shouldUnsuccessfulWith16DigitsInNumber() {
        cardInfo = DataHelper.getValidApprovedCard();
        var number = DataHelper.generateRandomCardNumberSixteenDigits();
        var matchesNumber = number;

        form = card.clickPayButton();
        form.insertingValueInForm(number, cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.matchesByInsertValue(matchesNumber, cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.assertBuyOperationWithErrorNotification();
        form.assertBuyOperationIsSuccessful();
    }

    @Severity(SeverityLevel.BLOCKER)
    @Test
    @DisplayName("shouldUnsuccessfulWith20DigitsInNumber")
    public void shouldVisibleNotificationWith20DigitsInNumber() {
        cardInfo = DataHelper.getValidApprovedCard();
        var number = DataHelper.generateRandomCardNumberTwentyDigits();
        var matchesNumber = number;

        form = card.clickPayButton();
        form.insertingValueInForm(number, cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.matchesByInsertValue(matchesNumber, cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.assertNumberFieldIsInvalidValue();
    }

    @Severity(SeverityLevel.BLOCKER)
    @Test
    @DisplayName("shouldUnsuccessfulWith11DigitsInNumber")
    public void shouldVisibleNotificationWith11DigitsInNumber() {
        cardInfo = DataHelper.getValidApprovedCard();
        var number = DataHelper.generateRandomCardNumberElevenDigits();
        var matchesNumber = number;

        form = card.clickPayButton();
        form.insertingValueInForm(number, cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.matchesByInsertValue(matchesNumber, cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.assertNumberFieldIsInvalidValue();
    }

    @Severity(SeverityLevel.BLOCKER)
    @Test
    @DisplayName("shouldVisibleNotificationWithEmptyNumber") // поле номер карты не заполняется
    public void shouldVisibleNotificationWithEmptyNumber() {
        cardInfo = DataHelper.getValidApprovedCard();
        form = card.clickPayButton();
        form.insertingValueInForm("", cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.matchesByInsertValue("", cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.assertNumberFieldIsEmptyValue();
    }

    @Severity(SeverityLevel.BLOCKER)
    @Test
    @DisplayName("shouldSuccessfulWithoutSpacebarInNumber") // тест на ввод номера без пробелов
    public void shouldSuccessfulWithoutSpacebarInNumber() {
        cardInfo = DataHelper.getValidApprovedCard();
        var number = DataHelper.getNumberWithoutSpacebarByStatus("approved");
        var matchesNumber = cardInfo.getNumber();

        form = card.clickPayButton();
        form.insertingValueInForm(number, cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.matchesByInsertValue(matchesNumber, cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.assertBuyOperationIsSuccessful();
    }

    @Severity(SeverityLevel.BLOCKER)
    @Test
    @DisplayName("shouldNumberFieldEmpty") // тест без ввода номера карты
    public void shouldNumberFieldEmpty() {
        cardInfo = DataHelper.getValidApprovedCard();
        var number = DataHelper.getNumberWithoutSpacebarByStatus("approved");
        var matchesNumber = cardInfo.getNumber();

        form = card.clickPayButton();
        form.insertingValueInForm(number, cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.matchesByInsertValue(matchesNumber, cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.assertBuyOperationIsSuccessful();
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("shouldInvalidMonth") // Невалидный месяц от 13 до 99
    public void shouldInvalidMonth() {
        cardInfo = DataHelper.getValidApprovedCard();
        var month = DataHelper.getInvalidRandomMonth();

        form = card.clickPayButton();
        form.insertingValueInForm(cardInfo.getNumber(), Integer.toString(month), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), Integer.toString(month), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.assertMonthFieldIsInvalidValue();
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("shouldGetMonthOneToNine") // Месяц без нуля от 1 до 9
    public void shouldGetMonthOneToNineWithoutNullBefore() {
        cardInfo = DataHelper.getValidApprovedCard();
        var month = DataHelper.getMonthOneToNine();

        form = card.clickPayButton();
        form.insertingValueInForm(cardInfo.getNumber(), Integer.toString(month), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), Integer.toString(month), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.assertBuyOperationIsSuccessful();
    }

    @Severity(SeverityLevel.MINOR)
    @Test
    @DisplayName("shouldGetMonthNull") // Месяц 0
    public void shouldGetMonthNull() {
        cardInfo = DataHelper.getValidApprovedCard();
        var month = DataHelper.getMonthNull();

        form = card.clickPayButton();
        form.insertingValueInForm(cardInfo.getNumber(), Integer.toString(month), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), Integer.toString(month), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.assertMonthFieldIsInvalidValueAnother();
    }

    @Severity(SeverityLevel.MINOR)
    @Test
    @DisplayName("shouldGetMonthDoubleNull") // Месяц 00
    public void shouldGetMonthDoubleNull() {
        cardInfo = DataHelper.getValidApprovedCard();
        var month = DataHelper.getMonthDoubleNull();

        form = card.clickPayButton();
        form.insertingValueInForm(cardInfo.getNumber(), month, cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), month, cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.assertMonthFieldIsInvalidValueAnother();
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("shouldWithoutMonth") // без месяца
    public void shouldWithoutMonth() {
        cardInfo = DataHelper.getValidApprovedCard();
        var month = DataHelper.getMonthEmpty();

        form = card.clickPayButton();
        form.insertingValueInForm(cardInfo.getNumber(), month, cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), month, cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.assertMonthFieldIsEmptyValue();
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("shouldGetYearMoreThanCurrentYearOn20") // больше чем текущий год на 20 лет
    public void shouldGetYearMoreThanCurrentYearOn20() {
        cardInfo = DataHelper.getValidApprovedCard();
        var year = DataHelper.getInvalidYear();

        form = card.clickPayButton();
        form.insertingValueInForm(cardInfo.getNumber(), cardInfo.getMonth(), year, cardInfo.getHolder(), cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), cardInfo.getMonth(), year, cardInfo.getHolder(), cardInfo.getCvc());
        form.assertYearFieldIsInvalidValueWrongDate();
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("shouldGetDateTimeIsOut") // дата при которой срок вышел
    public void shouldGetDateTimeIsOut() {
        cardInfo = DataHelper.getValidApprovedCard();
        var year = DataHelper.previousYear();

        form = card.clickPayButton();
        form.insertingValueInForm(cardInfo.getNumber(), cardInfo.getMonth(), year, cardInfo.getHolder(), cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), cardInfo.getMonth(), year, cardInfo.getHolder(), cardInfo.getCvc());
        form.assertYearFieldIsInvalidValue();
    }

    @Severity(SeverityLevel.MINOR)
    @Test
    @DisplayName("shouldGetYearNull") // Год 0
    public void shouldGetYearNull() {
        cardInfo = DataHelper.getValidApprovedCard();
        var year = DataHelper.getYearNull();

        form = card.clickPayButton();
        form.insertingValueInForm(cardInfo.getNumber(), cardInfo.getMonth(), year, cardInfo.getHolder(), cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), cardInfo.getMonth(), year, cardInfo.getHolder(), cardInfo.getCvc());
        form.assertYearFieldIsEmptyValue();
    }

    @Severity(SeverityLevel.MINOR)
    @Test
    @DisplayName("shouldGetYearDoubleNull") // Год 00
    public void shouldGetYearDoubleNull() {
        cardInfo = DataHelper.getValidApprovedCard();
        var year = DataHelper.getYearDoubleNull();

        form = card.clickPayButton();
        form.insertingValueInForm(cardInfo.getNumber(), cardInfo.getMonth(), year, cardInfo.getHolder(), cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), cardInfo.getMonth(), year, cardInfo.getHolder(), cardInfo.getCvc());
        form.assertYearFieldIsInvalidValue();
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("shouldWithoutYear") // без года
    public void shouldVisibleNotificationWithEmptyYear() {
        cardInfo = DataHelper.getValidApprovedCard();
        var year = DataHelper.getYearEmpty();

        form = card.clickPayButton();
        form.insertingValueInForm(cardInfo.getNumber(), cardInfo.getMonth(), year, cardInfo.getHolder(), cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), cardInfo.getMonth(), year, cardInfo.getHolder(), cardInfo.getCvc());
        form.assertYearFieldIsEmptyValue();
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("shouldGenerateHolderWithDash") // с использованием дефиса между именами
    public void shouldGenerateHolderWithDash() {
        cardInfo = DataHelper.getValidApprovedCard();
        var holder = DataHelper.generateHolderWithDash();

        form = card.clickPayButton();
        form.insertingValueInForm(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), holder, cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), holder, cardInfo.getCvc());
        form.assertBuyOperationIsSuccessful();
    }

    @Severity(SeverityLevel.MINOR)
    @Test
    @DisplayName("shouldGenerateHolderWithSpaceBarBefore") // с использованием пробела до
    public void shouldGenerateHolderWithSpaceBarBefore() {
        cardInfo = DataHelper.getValidApprovedCard();
        var holder = DataHelper.generateHolderWithSpaceBarBefore();

        form = card.clickPayButton();
        form.insertingValueInForm(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), holder, cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), holder, cardInfo.getCvc());
        form.assertBuyOperationIsSuccessful();
    }

    @Severity(SeverityLevel.MINOR)
    @Test
    @DisplayName("shouldGenerateHolderWithSpaceBarAfter") // с использованием пробела после
    public void shouldGenerateHolderWithSpaceBarAfter() {
        cardInfo = DataHelper.getValidApprovedCard();
        var holder = DataHelper.generateHolderWithSpaceBarAfter();

        form = card.clickPayButton();
        form.insertingValueInForm(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), holder, cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), holder, cardInfo.getCvc());
        form.assertBuyOperationIsSuccessful();
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("shouldGenerateHolderWithUnembossedName") // при неименной карте
    public void shouldHolderWithUnembossedName() {
        cardInfo = DataHelper.getValidApprovedCard();
        var holder = DataHelper.generateHolderWithUnembossedName();

        form = card.clickPayButton();
        form.insertingValueInForm(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), holder, cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), holder, cardInfo.getCvc());
        form.assertBuyOperationIsSuccessful();
    }

    @Severity(SeverityLevel.MINOR)
    @Test
    @DisplayName("shouldHolderWithSpecialSymbols") // С использованием спец. символов
    public void shouldHolderWithSpecialSymbols() {
        cardInfo = DataHelper.getValidApprovedCard();
        var holder = DataHelper.generateHolderWithSpecialSymbols();

        form = card.clickPayButton();
        form.insertingValueInForm(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), holder, cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), holder, cardInfo.getCvc());
        form.assertHolderFieldIsInvalidValue();
    }

    @Severity(SeverityLevel.MINOR)
    @Test
    @DisplayName("shouldHolderWithUpperAndLowerCaseLatin") // С использованием латиницы верхнего и нижнего регистра
    public void shouldHolderWithUpperAndLowerCaseLatin() {
        cardInfo = DataHelper.getValidApprovedCard();
        var holder = DataHelper.generateHolderWithUpperAndLowerCaseLatin();

        form = card.clickPayButton();
        form.insertingValueInForm(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), holder, cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), holder, cardInfo.getCvc());
        form.assertHolderFieldIsInvalidValue();
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("shouldHolderWithCyrillicUpperAndLowerCase") // С использованием кириллицы верхнего и нижнего регистра
    public void shouldHolderWithCyrillicUpperAndLowerCase() {
        cardInfo = DataHelper.getValidApprovedCard();
        var holder = DataHelper.generateRandomCardsHolderNameLUCyrillic();

        form = card.clickPayButton();
        form.insertingValueInForm(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), holder, cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), holder, cardInfo.getCvc());
        form.assertHolderFieldIsInvalidValue();
    }

    @Severity(SeverityLevel.MINOR)
    @Test
    @DisplayName("shouldHolderWithDigits") // С использованием цифр
    public void shouldHolderWithDigits() {
        cardInfo = DataHelper.getValidApprovedCard();
        var holder = DataHelper.generateRandomCardsHolderWithDigits();

        form = card.clickPayButton();
        form.insertingValueInForm(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), holder, cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), holder, cardInfo.getCvc());
        form.assertHolderFieldIsInvalidValue();
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("shouldVisibleNotificationWithEmptyHolder") // без указания владельца
    public void shouldVisibleNotificationWithEmptyHolder() {
        cardInfo = DataHelper.getValidApprovedCard();
        var holder = DataHelper.generateEmptyHolder();

        form = card.clickPayButton();
        form.insertingValueInForm(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), holder, cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), holder, cardInfo.getCvc());
        form.assertHolderFieldIsEmptyValue();
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("shouldHolderWithOneLatter") // Генерация имени владельца из одной буквы верхнего регистра
    public void shouldHolderWithOneLatter() {
        cardInfo = DataHelper.getValidApprovedCard();
        var holder = DataHelper.generateRandomCardsHolderWithOneLetter();

        form = card.clickPayButton();
        form.insertingValueInForm(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), holder, cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), holder, cardInfo.getCvc());
        form.assertBuyOperationIsSuccessful();
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("shouldVisibleNotificationWithEmptyCVC")
    public void shouldVisibleNotificationWithEmptyCVC() { // Без ввода в поле CVC кода, поле владелец считается пустым
        cardInfo = DataHelper.getValidApprovedCard();
        var cvc = DataHelper.generateCVVEmpty();

        form = card.clickPayButton();
        form.insertingValueInForm(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cvc);
        form.matchesByInsertValue(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cvc);
        form.assertCvcFieldIsEmptyValue();
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("shouldGetEmptyCVC0")
    public void shouldGetEmptyCVC0() { // Ввод в поле CVV 0
        cardInfo = DataHelper.getValidApprovedCard();
        var cvc = DataHelper.generateCVV0();

        form = card.clickPayButton();
        form.insertingValueInForm(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cvc);
        form.matchesByInsertValue(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cvc);
        form.assertCvcFieldIsInvalidValue();
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("shouldGetEmptyCVC00")
    public void shouldGetEmptyCVC00() { // Ввод в поле CVV 00
        cardInfo = DataHelper.getValidApprovedCard();
        var cvc = DataHelper.generateCVV00();

        form = card.clickPayButton();
        form.insertingValueInForm(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cvc);
        form.matchesByInsertValue(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cvc);
        form.assertCvcFieldIsInvalidValue();
    }
}