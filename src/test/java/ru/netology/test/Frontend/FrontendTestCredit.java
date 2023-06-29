package ru.netology.test.Frontend;

import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.*;
import ru.netology.data.DBHelper;
import ru.netology.data.DataHelper;
import ru.netology.page.CardPage;
import ru.netology.page.FormPage;

import java.util.List;

import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.netology.data.DBHelper.cleanDatabase;
import static ru.netology.data.DataHelper.cleanListNow;

public class FrontendTestCredit {

    private static String fieldEmpty = "Поле обязательно для заполнения";

    private static String fieldInvalid = "Неверный формат";

    private static String сardExpired = "Истёк срок действия карты";

    private static String invalidPeriod = "Неверно указан срок действия карты";

    private static String invalidHolderName = "Наименования владельца должно быть указано латиницей верхнего регистра";

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
    @DisplayName("Payment of the tour on credit with valid filling in all fields of the card form")
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
    @DisplayName("Refusal of a loan for the purchase of a tour with valid filling in the fields of the declined card form")
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
        assertEquals(0, credits.size());
        assertEquals(0, orders.size());

        assertTrue(credits.get(0).getStatus().equalsIgnoreCase("declined"));
        assertEquals(credits.get(0).getBank_id(), orders.get(0).getPayment_id());
        assertEquals(credits.get(0).getId(), orders.get(0).getCredit_id());
    }

    @Severity(SeverityLevel.BLOCKER)
    @Test
    @DisplayName("Filling in the Card number field with the generated number having the minimum valid length")
    public void shouldUnsuccessfulWith12DigitsInNumber() {
        cardInfo = DataHelper.getValidApprovedCard();
        var number = DataHelper.generateRandomCardNumberTwelveDigits();
        var matchesNumber = number;

        form = card.clickCreditButton();
        form.insertingValueInForm(number, cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.matchesByInsertValue(matchesNumber, cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.assertBuyOperationWithErrorNotification();
    }

    @Severity(SeverityLevel.BLOCKER)
    @Test
    @DisplayName("Filling in the Card number field with the generated number having the maximum valid length")
    public void shouldUnsuccessfulWith19DigitsInNumber() {
        cardInfo = DataHelper.getValidApprovedCard();
        var number = DataHelper.generateRandomCardNumberNineteenDigits();
        var matchesNumber = number;

        form = card.clickCreditButton();
        form.insertingValueInForm(number, cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.matchesByInsertValue(matchesNumber, cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.assertBuyOperationWithErrorNotification();
    }

    @Severity(SeverityLevel.BLOCKER)
    @Test
    @DisplayName("Filling in the Card number field with a generated 16-digit number")
    public void shouldUnsuccessfulWith16DigitsInNumber() {
        cardInfo = DataHelper.getValidApprovedCard();
        var number = DataHelper.generateRandomCardNumberSixteenDigits();
        var matchesNumber = number;

        form = card.clickCreditButton();
        form.insertingValueInForm(number, cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.matchesByInsertValue(matchesNumber, cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.assertBuyOperationWithErrorNotification();
    }

    @Severity(SeverityLevel.BLOCKER)
    @Test
    @DisplayName("Filling in the Card Number field with a number exceeding the valid length of the card")
    public void shouldTest20DigitsNumberIsInvalid() {
        cardInfo = DataHelper.getValidApprovedCard();
        var number = DataHelper.generateRandomCardNumberTwentyDigits();
        var matchesNumber = number;

        form = card.clickCreditButton();
        form.insertingValueInForm(number, cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.matchesByInsertValue(matchesNumber, cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.assertNumberField(fieldInvalid);
    }

    @Severity(SeverityLevel.BLOCKER)
    @Test
    @DisplayName("Filling in the Card Number field below the acceptable valid card length with a number")
    public void shouldTest11DigitsNumberIsInvalid() {
        cardInfo = DataHelper.getValidApprovedCard();
        var number = DataHelper.generateRandomCardNumberElevenDigits();
        var matchesNumber = number;

        form = card.clickCreditButton();
        form.insertingValueInForm(number, cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.matchesByInsertValue(matchesNumber, cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.assertNumberField(fieldInvalid);
    }

    @Severity(SeverityLevel.BLOCKER)
    @Test
    @DisplayName("The Card number field is not filled in")
    public void shouldVisibleNotificationWithEmptyNumber() {
        cardInfo = DataHelper.getValidApprovedCard();

        form = card.clickCreditButton();
        form.insertingValueInForm("", cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.matchesByInsertValue("", cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.assertNumberField(fieldEmpty);
    }

    @Severity(SeverityLevel.BLOCKER)
    @Test
    @DisplayName("Filling out the form with valid data with an approved card specified without spaces")
    public void shouldSuccessfulWithoutSpacebarInNumber() {
        cardInfo = DataHelper.getValidApprovedCard();
        var number = DataHelper.getNumberWithoutSpacebarByStatus("approved");
        var matchesNumber = cardInfo.getNumber();

        form = card.clickCreditButton();
        form.insertingValueInForm(number, cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.matchesByInsertValue(matchesNumber, cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.assertBuyOperationIsSuccessful();
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("Filling in the Month field with an invalid month number")
    public void shouldInvalidMonth() {
        cardInfo = DataHelper.getValidApprovedCard();
        var month = DataHelper.getInvalidRandomMonth();
        var matchesMonth = month;

        form = card.clickCreditButton();
        form.insertingValueInForm(cardInfo.getNumber(), month, cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), matchesMonth, cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.assertMonthField(invalidPeriod);
    }

    @Severity(SeverityLevel.MINOR)
    @Test
    @DisplayName("Filling in the Month field with zero")
    public void shouldGetMonthZero() {
        cardInfo = DataHelper.getValidApprovedCard();
        var month = DataHelper.getMonthZero();
        var matchesMonth = month;

        form = card.clickCreditButton();
        form.insertingValueInForm(cardInfo.getNumber(), month, cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), matchesMonth, cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.assertMonthField(fieldInvalid);
    }

    @Severity(SeverityLevel.MINOR)
    @Test
    @DisplayName("Filling in the Month field with double zero")
    public void shouldGetMonthDoubleZero() {
        cardInfo = DataHelper.getValidApprovedCard();
        var month = DataHelper.getMonthDoubleZero();
        var matchesMonth = month;

        form = card.clickCreditButton();
        form.insertingValueInForm(cardInfo.getNumber(), month, cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), matchesMonth, cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.assertMonthField(fieldInvalid);
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("Filling in the Month field without zero before the number from 1-9")
    public void shouldGetMonthOneToNineWithoutZeroBefore() {
        cardInfo = DataHelper.getValidApprovedCard();
        var month = DataHelper.getMonthOneToNine();
        var year = DataHelper.nextYear();
        var matchesMonth = month;
        var matchesYear = year;

        form = card.clickCreditButton();
        form.insertingValueInForm(cardInfo.getNumber(), month, year, cardInfo.getHolder(), cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), matchesMonth, matchesYear, cardInfo.getHolder(), cardInfo.getCvc());
        form.assertBuyOperationIsSuccessful();
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("The Month field is not filled in")
    public void shouldWithoutMonth() {
        cardInfo = DataHelper.getValidApprovedCard();
        var month = DataHelper.getMonthEmpty();
        var matchesMonth = month;

        form = card.clickCreditButton();
        form.insertingValueInForm(cardInfo.getNumber(), month, cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), matchesMonth, cardInfo.getYear(), cardInfo.getHolder(), cardInfo.getCvc());
        form.assertMonthField(fieldEmpty);
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("Filling in the Year field with a number greater than the current year for exactly 20 years")
    public void shouldGetYearThanCurrentYearOn20() {
        cardInfo = DataHelper.getValidApprovedCard();
        var year = DataHelper.getValidYearMoreThanCurrent20();
        var matchesYear = year;
        var month = DataHelper.getCurrentMonth();
        var matchesMonth = month;

        form = card.clickCreditButton();
        form.insertingValueInForm(cardInfo.getNumber(), month, year, cardInfo.getHolder(), cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), matchesMonth, matchesYear, cardInfo.getHolder(), cardInfo.getCvc());
        form.assertBuyOperationIsSuccessful();
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("Filling in the Year field with a number exceeding the current year by 20 years")
    public void shouldGetYearMoreThanCurrentYearOn20() {
        cardInfo = DataHelper.getValidApprovedCard();
        var year = DataHelper.getInvalidYear();
        var matchesYear = year;

        form = card.clickCreditButton();
        form.insertingValueInForm(cardInfo.getNumber(), cardInfo.getMonth(), year, cardInfo.getHolder(), cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), cardInfo.getMonth(), matchesYear, cardInfo.getHolder(), cardInfo.getCvc());
        form.assertYearField(invalidPeriod);
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("Filling in the Year field with the date at which the card expired")
    public void shouldGetDateTimeIsOut() {
        cardInfo = DataHelper.getValidApprovedCard();
        var year = DataHelper.previousYear();
        var matchesYear = year;

        form = card.clickCreditButton();
        form.insertingValueInForm(cardInfo.getNumber(), cardInfo.getMonth(), year, cardInfo.getHolder(), cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), cardInfo.getMonth(), matchesYear, cardInfo.getHolder(), cardInfo.getCvc());
        form.assertYearField(сardExpired);
    }

    @Severity(SeverityLevel.MINOR)
    @Test
    @DisplayName("Filling in the Year field with zero")
    public void shouldGetYearZero() {
        cardInfo = DataHelper.getValidApprovedCard();
        var year = DataHelper.getYearZero();
        var matchesYear = year;

        form = card.clickCreditButton();
        form.insertingValueInForm(cardInfo.getNumber(), cardInfo.getMonth(), year, cardInfo.getHolder(), cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), cardInfo.getMonth(), matchesYear, cardInfo.getHolder(), cardInfo.getCvc());
        form.assertYearField(fieldInvalid);
    }

    @Severity(SeverityLevel.MINOR)
    @Test
    @DisplayName("Filling in the Year field with double zero")
    public void shouldGetYearDoubleZero() {
        cardInfo = DataHelper.getValidApprovedCard();
        var month = DataHelper.getCurrentMonth();
        var matchesMonth = month;
        var year = DataHelper.getYearDoubleZero();
        var matchesYear = year;

        form = card.clickCreditButton();
        form.insertingValueInForm(cardInfo.getNumber(), month, year, cardInfo.getHolder(), cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), matchesMonth, matchesYear, cardInfo.getHolder(), cardInfo.getCvc());
        form.assertYearField(сardExpired);
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("The Year field is not filled in")
    public void shouldVisibleNotificationWithEmptyYear() {
        cardInfo = DataHelper.getValidApprovedCard();
        var year = DataHelper.getYearEmpty();
        var matchesYear = year;

        form = card.clickCreditButton();
        form.insertingValueInForm(cardInfo.getNumber(), cardInfo.getMonth(), year, cardInfo.getHolder(), cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), cardInfo.getMonth(), matchesYear, cardInfo.getHolder(), cardInfo.getCvc());
        form.assertYearField(fieldEmpty);
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("Filling in the Owner field with the name using dash") // с использованием дефиса между именами
    public void shouldGenerateHolderWithDash() {
        cardInfo = DataHelper.getValidApprovedCard();
        var holder = DataHelper.generateHolderWithDash();
        var matchesHolder = holder;

        form = card.clickCreditButton();
        form.insertingValueInForm(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), holder, cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), matchesHolder, cardInfo.getCvc());
        form.assertBuyOperationIsSuccessful();
    }

    @Severity(SeverityLevel.MINOR)
    @Test
    @DisplayName("Filling in the Owner field with the name using a space before the name")
    public void shouldGenerateHolderWithSpaceBefore() {
        cardInfo = DataHelper.getValidApprovedCard();
        var holder = DataHelper.generateHolderWithSpaceBarBefore();
        var matchesHolder = holder;

        form = card.clickCreditButton();
        form.insertingValueInForm(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), holder, cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), matchesHolder, cardInfo.getCvc());
        form.assertBuyOperationIsSuccessful();
    }

    @Severity(SeverityLevel.MINOR)
    @Test
    @DisplayName("Filling in the Owner field with the name using a space after the name")
    public void shouldGenerateHolderWithSpaceAfter() {
        cardInfo = DataHelper.getValidApprovedCard();
        var holder = DataHelper.generateHolderWithSpaceBarAfter();
        var matchesHolder = holder;

        form = card.clickCreditButton();
        form.insertingValueInForm(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), holder, cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), matchesHolder, cardInfo.getCvc());
        form.assertBuyOperationIsSuccessful();
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("Filling in the Owner field with the name with two spaces between the names")
    public void shouldHolderWithDoubleSpaceBetweenNames() {
        cardInfo = DataHelper.getValidApprovedCard();
        var holder = DataHelper.generateHolderWithDoubleSpace();
        var matchesHolder = holder;

        form = card.clickCreditButton();
        form.insertingValueInForm(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), holder, cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), matchesHolder, cardInfo.getCvc());
        form.assertHolderField(fieldInvalid);
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("Filling in the Owner field with an unnamed card")
    public void shouldHolderWithUnembossedName() {
        cardInfo = DataHelper.getValidApprovedCard();
        var holder = DataHelper.generateHolderWithUnembossedName();
        var matchesHolder = holder;

        form = card.clickCreditButton();
        form.insertingValueInForm(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), holder, cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), matchesHolder, cardInfo.getCvc());
        form.assertBuyOperationIsSuccessful();
    }

    @Severity(SeverityLevel.MINOR)
    @Test
    @DisplayName("Filling in the Owner field when specifying the name using special symbols")
    public void shouldHolderWithSpecialSymbols() {
        cardInfo = DataHelper.getValidApprovedCard();
        var holder = DataHelper.generateHolderWithSpecialSymbols();
        var matchesHolder = holder;

        form = card.clickCreditButton();
        form.insertingValueInForm(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), holder, cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), matchesHolder, cardInfo.getCvc());
        form.assertHolderField(invalidHolderName);
    }

    @Severity(SeverityLevel.MINOR)
    @Test
    @DisplayName("Filling in the Owner field with the name in Latin upper and lower case")
    public void shouldHolderWithUpperAndLowerCaseLatin() {
        cardInfo = DataHelper.getValidApprovedCard();
        var holder = DataHelper.generateHolderWithUpperAndLowerCaseLatin();
        var matchesHolder = holder;

        form = card.clickCreditButton();
        form.insertingValueInForm(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), holder, cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), matchesHolder, cardInfo.getCvc());
        form.assertHolderField(invalidHolderName);
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("Filling in the Owner field with the name in Cyrillic upper and lower case")
    public void shouldHolderWithCyrillicUpperAndLowerCase() {
        cardInfo = DataHelper.getValidApprovedCard();
        var holder = DataHelper.generateRandomCardsHolderNameLUCyrillic();
        var matchesHolder = holder;

        form = card.clickCreditButton();
        form.insertingValueInForm(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), holder, cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), matchesHolder, cardInfo.getCvc());
        form.assertHolderField(invalidHolderName);
    }

    @Severity(SeverityLevel.MINOR)
    @Test
    @DisplayName("Filling in the Owner field with the name using numbers")
    public void shouldHolderWithDigits() {
        cardInfo = DataHelper.getValidApprovedCard();
        var holder = DataHelper.generateRandomCardsHolderWithDigits();
        var matchesHolder = holder;

        form = card.clickCreditButton();
        form.insertingValueInForm(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), holder, cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), matchesHolder, cardInfo.getCvc());
        form.assertHolderField(invalidHolderName);
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("The Owner field is not filled in")
    public void shouldVisibleNotificationWithEmptyHolder() {
        cardInfo = DataHelper.getValidApprovedCard();
        var holder = DataHelper.generateEmptyHolder();
        var matchesHolder = holder;

        form = card.clickCreditButton();
        form.insertingValueInForm(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), holder, cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), matchesHolder, cardInfo.getCvc());
        form.assertHolderField(fieldEmpty);;
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("Filling in the Owner field with the name of a single letter")
    public void shouldHolderWithOneLatter() {
        cardInfo = DataHelper.getValidApprovedCard();
        var holder = DataHelper.generateRandomCardsHolderWithOneLetter();
        var matchesHolder = holder;

        form = card.clickCreditButton();
        form.insertingValueInForm(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), holder, cardInfo.getCvc());
        form.matchesByInsertValue(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), matchesHolder, cardInfo.getCvc());
        form.assertBuyOperationIsSuccessful();
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("Filling in the CVC/CVV field with one digit")
    public void shouldGetCVCOneDigit() {
        cardInfo = DataHelper.getValidApprovedCard();
        var cvc = DataHelper.generateRandomCVVOneDigit();
        var matchesCVC = cvc;

        form = card.clickCreditButton();
        form.insertingValueInForm(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cvc);
        form.matchesByInsertValue(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), matchesCVC);
        form.assertCvcField(fieldInvalid);
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("Filling in the CVC/CVV field with two digits")
    public void shouldGetCVCTwoDigits() {
        cardInfo = DataHelper.getValidApprovedCard();
        var cvc = DataHelper.generateRandomCVVTwoDigits();
        var matchesCVC = cvc;

        form = card.clickCreditButton();
        form.insertingValueInForm(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cvc);
        form.matchesByInsertValue(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), matchesCVC);
        form.assertCvcField(fieldInvalid);
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("Filling in the CVC/CVV field with zero")
    public void shouldGetCVC0() {
        cardInfo = DataHelper.getValidApprovedCard();
        var cvc = DataHelper.generateCVV0();
        var matchesCVC = cvc;

        form = card.clickCreditButton();
        form.insertingValueInForm(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cvc);
        form.matchesByInsertValue(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), matchesCVC);
        form.assertCvcField(fieldInvalid);
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("Filling in the CVC/CVV field with double zero")
    public void shouldGetCVC00() {
        cardInfo = DataHelper.getValidApprovedCard();
        var cvc = DataHelper.generateCVV00();
        var matchesCVC = cvc;

        form = card.clickCreditButton();
        form.insertingValueInForm(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cvc);
        form.matchesByInsertValue(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), matchesCVC);
        form.assertCvcField(fieldInvalid);
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("The CVC/CVV field is not filled in")
    public void shouldVisibleNotificationWithEmptyCVC() {
        cardInfo = DataHelper.getValidApprovedCard();
        var cvc = DataHelper.generateCVVEmpty();
        var matchesCVC = cvc;

        form = card.clickCreditButton();
        form.insertingValueInForm(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), cvc);
        form.matchesByInsertValue(cardInfo.getNumber(), cardInfo.getMonth(), cardInfo.getYear(), cardInfo.getHolder(), matchesCVC);
        form.assertCvcField(fieldEmpty);;
    }
}
