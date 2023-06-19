package ru.netology.data;

import com.github.javafaker.Faker;
import lombok.Value;
import org.apache.commons.lang3.RandomStringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public class DataHelper {

    static ArrayList<Integer> month = new ArrayList<>();

    private static Faker faker = new Faker(new Locale("en"));
    private static Faker fakerCyrillic = new Faker(new Locale("ru"));
    private static LocalDate date = LocalDate.now();


    private DataHelper() {

    }

    @Value
    public static class CardInfo {
        private final String number;
        private final String month;
        private final String year;
        private final String holder;
        private final String cvc;
    }

    public static CardInfo getValidApprovedCard() {
        return new CardInfo(getNumberByStatus("approved"), getValidRandomMonth(), getValidRandomYear(),
                generateValidRandomCardsHolder(), generateRandomCVV());
    }

    public static CardInfo getValidDeclinedCard() {
        return new CardInfo(getNumberByStatus("declined"), getValidRandomMonth(), getValidRandomYear(),
                generateValidRandomCardsHolder(), generateRandomCVV());
    }

    public static String getNumberByStatus(String status) {
        if (status.equalsIgnoreCase("APPROVED")) {
            return "4444 4444 4444 4441";
        } else if (status.equalsIgnoreCase("DECLINED")) {
            return "4444 4444 4444 4442";
        }
        return null;
    }

    public static String getNumberWithoutSpacebarByStatus(String status) {
        if (status.equalsIgnoreCase("APPROVED")) {
            return "4444444444444441";
        } else if (status.equalsIgnoreCase("DECLINED")) {
            return "4444444444444442";
        }
        return null;
    }

    public static void cleanListNow() {
        month.clear();
    }

    public static String generateRandomCardNumberElevenDigits() {
        return faker.numerify("#### #### ###");
    }

    public static String generateRandomCardNumberTwelveDigits() {
        return faker.numerify("#### #### ####");
    }

    public static String generateRandomCardNumberThirteenDigits() {
        return faker.numerify("#### #### #### #");
    }

    public static String generateRandomCardNumberFifteenDigits() {
        return faker.numerify("#### #### #### ##");
    }

    public static String generateRandomCardNumberSixteenDigits() {
        return faker.numerify("#### #### #### ####");
    }

    public static String generateRandomCardNumberSixteenDigitsWithoutSpase() {
        return faker.numerify("################");
    }

    public static String generateRandomCardNumberSeventeenDigits() {
        return faker.numerify("#### #### #### #### #");
    }

    public static String generateRandomCardNumberEighteenDigits() {
        return faker.numerify("#### #### #### #### ##");
    }

    public static String generateRandomCardNumberNineteenDigits() {
        return faker.numerify("#### #### #### #### ###");
    }

    public static String generateRandomCardNumberTwentyDigits() {
        return faker.numerify("#### #### #### #### ####");
    }

    public static String getValidRandomMonth() { //генерация валидного месяца
        //int getMonth = date.getMonthValue();
        //return LocalDate.now().plusMonths(ThreadLocalRandom.current().nextInt(getMonth, 240 - getMonth)).format(DateTimeFormatter.ofPattern("LL"));
       int random = ThreadLocalRandom.current().nextInt(1, 12);
       month.add(random);
       String rMonth;
       if (random < 10) {
       rMonth = "0" + random;
       } else {
       rMonth = Integer.toString(random);
       }
       return rMonth;
    }

    public static int getInvalidRandomMonth() { //генерация невалидного месяца больше 12 и менее 100
        return ThreadLocalRandom.current().nextInt(13, 99);
    }

    public static int getMonthOneToNine() { //генерация невалидного месяца больше 12 и менее 100
        return ThreadLocalRandom.current().nextInt(1, 9);
    }

    public static int getMonthNull() { //генерация невалидного месяца больше 12 и менее 100
        return 0;
    }

    public static String getMonthEmpty() { //генерация невалидного месяца больше 12 и менее 100
        return "";
    }

    public static String getMonthDoubleNull() { //генерация невалидного месяца больше 12 и менее 100
        return "00";
    }

    public static String previousYear() { // Для проверки сценария с просроченной картой
        return Integer.toString((date.getYear() - 1) % 100);
    }

    public static String getValidRandomYear() { //Генерация валидного года
       //return LocalDate.now().plusMonths(ThreadLocalRandom.current().nextInt(0, 4)).format(DateTimeFormatter.ofPattern("yy"));
        int addRandom = ThreadLocalRandom.current().nextInt(0, 3);
        int getMonth = date.getMonthValue();
        int getYear = date.getYear();
        String arrayMonth = month.toString().replaceAll("(^\\[|\\]$)", "");
        int generateMonth = Integer.parseInt(arrayMonth);
        int currencyYear;
        int randomYear = 0;
        if (getMonth > generateMonth) {
        currencyYear = getYear + 1;}
        else {
        currencyYear = getYear;
        }
        randomYear = (currencyYear + addRandom) % 100;
       return Integer.toString(randomYear);
    }

    public static String getInvalidYear() { //Генерация года больше текущего на 20 лет
        return Integer.toString((date.getYear() + 21) % 100);
    }

    public static String getYearNull() { // Вывод текущего года
        return "0";
    }

    public static String getYearDoubleNull() { // Вывод текущего года
        return "00";
    }
    public static String getYearEmpty() { //генерация невалидного месяца больше 12 и менее 100
        return "";
    }
    public static String generateValidRandomCardsHolder() { // Владелец валидный вариант
        return faker.name().fullName().toUpperCase();
    }

    public static String generateEmptyHolder() { // Владелец валидный вариант
        return "";
    }

    public static String generateHolderWithDash() { // с использованием дефиса
        return faker.letterify("??????-?????").toUpperCase();
    }

    public static String generateHolderWithSpaceBarBefore() { // с использованием пробела до
        return " " + faker.name().fullName().toUpperCase();
    }

    public static String generateHolderWithSpaceBarAfter() { // с использованием пробела д после
        return faker.name().fullName().toUpperCase() + " ";
    }

    public static String generateHolderWithUnembossedName() { // при неименной карте
        return "Unembossed name";
    }

    public static String generateHolderWithSpecialSymbols() { // Ввод спец. символов
        return "SPECIAL?#^)(!\"'></*%$.@№!&-+~`:";
    }

    public static String generateHolderWithUpperAndLowerCaseLatin() { // Ввод спец. символов
        return faker.name().fullName();
    }

    public static String generateRandomCardsHolderNameLUCyrillic() { // Для сценария с проверкой ввода владельца на кириллице верхнего и нижнего регистра
        return fakerCyrillic.name().fullName();
    }

    public static String generateRandomCardsHolderWithDigits() { // Для сценария с проверкой ввода владельца на кириллице верхнего и нижнего регистра
        return RandomStringUtils.randomAlphanumeric(12).toUpperCase();
    }

    public static String generateRandomCardsHolderWithOneLetter() { // Генерация владельца из случайной буквы верхнего регистра
        return faker.letterify("?").toUpperCase();
    }

    public static String generateRandomCVV() { // Генерация валидного CVV
        return faker.number().digits(3);
    }

    public static String generateCVVEmpty() { // Генерация невалидного CVV из 0
        return "";
    }

    public static String generateCVV0() { // Генерация невалидного CVV из 0
        return "0";
    }

    public static String generateCVV00() { // Генерация невалидного CVV из 00
        return "00";
    }
}