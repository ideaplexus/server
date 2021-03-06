package controllers.api;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import dto.api.ProjectApiDto;
import org.openqa.selenium.Cookie;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.mvc.Call;
import play.test.TestBrowser;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Provides some useful methods to do get/post requests to the server.
 */
public class ApiHelper {

    private static final String BASE_URL = "http://localhost:19001";

    @Inject
    private WSClient wsClient;

    public <T> T doGet(Call urlRouteLink,
                       Class<T> expectedReturnType) {
        try {
            return doGetCareFree(urlRouteLink, expectedReturnType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * We need this variation of get, because the json mapper cannot figure
     * out the type of the elements, if a list of objects are returned as json.
     *
     * This is due to the nature of Java Generics and Type erasure.
     * Therefore to get the class of the elements we need to get a TypeReference class
     * which implements the specific type.
     *
     * @param typeReference e.g. TypeReference<List<MyClass>>(){}
     */
    public <T> List<T> doGetWithListResponse(Call urlRouteLink, TypeReference<List<T>> typeReference) {
        try {

            CompletionStage<JsonNode> response = callUrl(urlRouteLink);
            JsonNode rawResponse = response.toCompletableFuture().get();

            Object mappedJson = Json.mapper().readValue(rawResponse.toString(), typeReference);
            return (List<T>) mappedJson;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T doGet(Call urlRouteLink, Class<T> expectedReturnType, TestBrowser browser) {
        try {
            return doGetCareFree(urlRouteLink, expectedReturnType, browser);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void doPost(Call urlRouteLink, Object bodyWhichIsSentAsJson) {
        try {
            doPostCareFree(urlRouteLink, bodyWhichIsSentAsJson);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void doPost(Call urlRouteLink, Object bodyWhichIsSentAsJson, TestBrowser testBrowser) {
        try {
            doPostCareFree(urlRouteLink, bodyWhichIsSentAsJson, testBrowser);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T doPost(Call urlRouteLink, JsonNode data, Class<T> expectedReturnType) {
        try {
            return doPostCareFree(urlRouteLink, data, expectedReturnType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T doPostCareFree(Call urlRouteLink,
                                 JsonNode jsonData,
                                 Class<T> expectedReturnType) throws ExecutionException, InterruptedException {

        CompletionStage<JsonNode> response = wsClient.url(BASE_URL + urlRouteLink.url())
            .setContentType("application/json")
            .setHeader("Accept", "application/json")
            .post(jsonData)
            .thenApply(this::throwExceptionIfFaultyResponse)
            .thenApply(WSResponse::asJson);

        JsonNode rawResponse = response.toCompletableFuture().get();
        return Json.fromJson(rawResponse, expectedReturnType);

    }

    private void doPostCareFree(Call urlRouteLink,
                                Object bodyWhichIsSentAsJson) throws InterruptedException, ExecutionException {
        wsClient.url(BASE_URL + urlRouteLink.url())
            .post(Json.toJson(bodyWhichIsSentAsJson))
            .thenApply(this::throwExceptionIfFaultyResponse)
            .toCompletableFuture().get();
    }

    private void doPostCareFree(Call urlRouteLink,
                                Object bodyWhichIsSentAsJson,
                                TestBrowser testBrowser) throws InterruptedException, ExecutionException {
        WSRequest cookie = createRequestWithSameCookieAsBrowser(urlRouteLink, testBrowser);

        cookie
            .post(Json.toJson(bodyWhichIsSentAsJson))
            .thenApply(this::throwExceptionIfFaultyResponse)
            .toCompletableFuture().get();
    }

    private <T> T doGetCareFree(Call urlRouteLink,
                                Class<T> expectedReturnType) throws InterruptedException, ExecutionException {

        CompletionStage<JsonNode> response = callUrl(urlRouteLink);

        JsonNode rawResponse = response.toCompletableFuture().get();
        return Json.fromJson(rawResponse, expectedReturnType);
    }

    private <T> T doGetCareFree(Call urlRouteLink,
                                Class<T> expectedReturnType,
                                TestBrowser testBrowser) throws InterruptedException, ExecutionException {

        WSRequest requestWithCookie = createRequestWithSameCookieAsBrowser(urlRouteLink, testBrowser);

        CompletionStage<JsonNode> response = requestWithCookie
            .get()
            .thenApply(this::throwExceptionIfFaultyResponse)
            .thenApply(WSResponse::asJson);

        JsonNode rawResponse = response.toCompletableFuture().get();
        return Json.fromJson(rawResponse, expectedReturnType);
    }

    private WSRequest createRequestWithSameCookieAsBrowser(Call urlRouteLink, TestBrowser testBrowser) {
        /**
         * Use the same cookie as in the browser
         */
        Set<Cookie> allCookies = testBrowser.getCookies();
        String cookieAsString = allCookies
            .stream()
            .map(Cookie::toString)
            .collect(Collectors.joining("&"));

        return wsClient.url(BASE_URL + urlRouteLink.url())
            .setHeader("Cookie", cookieAsString);
    }

    private CompletionStage<JsonNode> callUrl(Call urlRouteLink) {
        return wsClient
            .url(BASE_URL + urlRouteLink.url())
            .get()
            .thenApply(this::throwExceptionIfFaultyResponse)
            .thenApply(WSResponse::asJson);
    }

    /**
     * Why do we need this?
     * Because, we try to parse the message as json, that is even done,
     * if the HTTP response is not ok ( e.g. if you get a 404 -> it tries to parse
     * the message as JSON -> lucky for us - we get a complicated Unexpected
     * character exception :( -> so try to catch that and and throw
     * a proper exception.
     */
    private WSResponse throwExceptionIfFaultyResponse(WSResponse o) {
        if (o.getStatus() == 404) {
            throw new RuntimeException("Response with error: " + o.getStatusText());
        } else if (o.getStatus() == 500) {
            throw new RuntimeException("Response with error: " + o.getStatusText());
        } else if (o.getStatus() == 403) {
            throw new RuntimeException("Response with error: " + o.getStatusText());
        }
        return o;
    }
}
