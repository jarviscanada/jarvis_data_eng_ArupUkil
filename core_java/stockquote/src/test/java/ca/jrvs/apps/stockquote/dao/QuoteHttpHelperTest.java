package ca.jrvs.apps.stockquote.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ca.jrvs.apps.stockquote.model.Quote;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.Call;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class QuoteHttpHelperTest {
  @Mock
  private OkHttpClient client;
  @Mock
  private Call call;

  private QuoteHttpHelper helper;

  private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

  @BeforeEach
  void setUp() {
    helper = new QuoteHttpHelper("mockApiKey", client);
  }

  private static Response responseFor(Request req, int code, String body) {
    ResponseBody rb = ResponseBody.create(body == null ? "" : body, JSON);

    return new Response.Builder()
        .request(req)
        .protocol(Protocol.HTTP_1_1)
        .code(code)
        .message(code >= 200 && code < 300 ? "OK" : "ERROR")
        .body(rb)
        .build();
  }

  @Test
  void testFetchQuoteInfo() throws IOException {
    assertThrows(IllegalArgumentException.class, () -> helper.fetchQuoteInfo("   "));
    verifyNoInteractions(client);

    String json =
        "{\n" +
            "  \"Global Quote\": {\n" +
            "    \"01. symbol\": \"MSFT\",\n" +
            "    \"02. open\": \"332.3800\",\n" +
            "    \"03. high\": \"333.8300\",\n" +
            "    \"04. low\": \"326.3600\",\n" +
            "    \"05. price\": \"327.7300\",\n" +
            "    \"06. volume\": \"21085695\",\n" +
            "    \"07. latest trading day\": \"2023-10-13\",\n" +
            "    \"08. previous close\": \"331.1600\",\n" +
            "    \"09. change\": \"-3.4300\",\n" +
            "    \"10. change percent\": \"-1.0358%\"\n" +
            "  }\n" +
            "}";

    // ArgumentCaptor lets you capture the actual argument that was passed into a mocked method, so you can inspect/assert on it
    ArgumentCaptor<Request> reqCap = ArgumentCaptor.forClass(Request.class);
    when(client.newCall(reqCap.capture())).thenReturn(call);

    Request dummyReq = new Request.Builder().url("http://unit.test").build();
    when(call.execute()).thenReturn(responseFor(dummyReq, 200, json));

    long before = System.currentTimeMillis();
    Quote q = helper.fetchQuoteInfo("msft"); // lower-case input
    long after = System.currentTimeMillis();

    assertEquals("MSFT", q.getTicker());
    assertEquals(332.38, q.getOpen(), 1e-6);
    assertEquals(333.83, q.getHigh(), 1e-6);
    assertEquals(326.36, q.getLow(), 1e-6);
    assertEquals(327.73, q.getPrice(), 1e-6);
    assertEquals(21085695, q.getVolume());
    assertEquals("-1.0358%", q.getChangePercent());

    assertNotNull(q.getTimestamp());
    assertTrue(q.getTimestamp().getTime() >= before && q.getTimestamp().getTime() <= after);

    Request built = reqCap.getValue();
    assertTrue(built.url().toString().contains("symbol=MSFT"));
  }
}
