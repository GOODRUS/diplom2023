[![Build status](https://ci.appveyor.com/api/projects/status/3hq93ksbf7crcdbr/branch/main?svg=true)](https://ci.appveyor.com/project/GOODRUS/diplom2023-vdmw0/branch/main)
# Дипломный проект по профессии «Тестировщик» #
## Настройка SUT, запуск авто-тестов, генерация репортов ##
**Подключение SUT к MySQL**
1. Запустить Docker Desktop
2. Открыть проект в IntelliJ IDEA
3. В терминале в корне проекта запустить контейнеры: <br/>
`docker-compose up`
4. Открыть второй терминал
5. Запустить приложение:<br/>
`java "-Dspring.datasource.url=jdbc:mysql://localhost:3306/app" -jar artifacts/aqa-shop.jar`
6. Открыть третий терминал
7. Запустить тесты:<br/>`./gradlew clean test "-Ddb.url=jdbc:mysql://localhost:3306/app"`
8. Генерация отчёта Allure<br/>
`./gradlew allureServe`
9. Закрыть отчёт:<br/>
**CTRL + C -> y -> Enter**
10. Перейти во второй терминал
11. Остановить приложение:<br/>
    **CTRL + C**
12. Остановить контейнеры:<br/>
    `docker-compose down`

**Подключение SUT к PostgreSQL**

1. Запустить Docker Desktop
2. Открыть проект в IntelliJ IDEA
3. В терминале в корне проекта запустить контейнеры: <br/>
   `docker-compose up`
4. Открыть второй терминал
5. Запустить приложение:<br/>
   `java "-Dspring.datasource.url=jdbc:postgresql://localhost:5432/app" -jar artifacts/aqa-shop.jar`
6. Открыть третий терминал
7. Запустить тесты:<br/>`./gradlew clean test "-Ddb.url=jdbc:postgresql://localhost:5432/app"`
8. Генерация отчёта Allure<br/>
   `./gradlew allureServe`
9. Закрыть отчёт:<br/>
   **CTRL + C -> y -> Enter**
10. Перейти во второй терминал
11. Остановить приложение:<br/>
    **CTRL + C**
12. Остановить контейнеры:<br/>
    `docker-compose down`
