package ru.netology.data;

import com.google.gson.Gson;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public class APIHelper {
    private static final Gson gson = new Gson();
    private static final RequestSpecification spec = new RequestSpecBuilder().setBaseUri("http://localhost").setPort(8080)
            .setAccept(ContentType.JSON).setContentType(ContentType.JSON).log(LogDetail.ALL).build();

    public static void postRequest(DataHelper.CardInfo cardInfo, String path, String Status) {
        var body = gson.toJson(cardInfo);
        given().spec(spec).body(body)
                .when().post(path)
                .then().statusCode(Integer.parseInt(Status));
    }
}
