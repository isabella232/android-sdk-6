package com.configcat;

import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigCatClientIntegrationTest {

    private static final String APIKEY = "TEST_KEY";
    private ConfigCatClient client;
    private MockWebServer server;

    private static final String TEST_JSON = "{ f: { fakeKey: { v: %s, p: [] ,r: [] } } }";

    @BeforeEach
    public void setUp() throws IOException {
        this.server = new MockWebServer();
        this.server.start();

        this.client = ConfigCatClient.newBuilder()
                .httpClient(new OkHttpClient.Builder().build())
                .mode(PollingModes.lazyLoad(2, true))
                .baseUrl(this.server.url("/").toString())
                .build(APIKEY);
    }

    @AfterEach
    public void tearDown() throws IOException {
        this.client.close();
        this.server.shutdown();
    }

    @Test
    public void getStringValue() {
        String sValue = "ááúúóüüőőööúúűű";
        String result = String.format(TEST_JSON, sValue);
        server.enqueue(new MockResponse().setResponseCode(200).setBody(result));
        String config = this.client.getValue(String.class, "fakeKey", null);
        assertEquals(sValue, config);
    }

    @Test
    public void getStringValueReturnsDefaultOnFail() {
        String def = "def";
        server.enqueue(new MockResponse().setResponseCode(500));
        String config = this.client.getValue(String.class, "fakeKey", def);
        assertEquals(def, config);
    }

    @Test
    public void getStringValueReturnsDefaultOnException() {
        String result = "{ test: test] }";
        String def = "def";
        server.enqueue(new MockResponse().setResponseCode(200).setBody(result));
        String config = this.client.getValue(String.class, "fakeKey", def);
        assertEquals(def, config);
    }

    @Test
    public void getBooleanValue() {
        String result = String.format(TEST_JSON, "true");
        server.enqueue(new MockResponse().setResponseCode(200).setBody(result));
        boolean config = this.client.getValue(Boolean.class, "fakeKey", false);
        assertTrue(config);
    }

    @Test
    public void getBooleanValuePrimitive() {
        String result = String.format(TEST_JSON, "true");
        server.enqueue(new MockResponse().setResponseCode(200).setBody(result));
        boolean config = this.client.getValue(boolean.class, "fakeKey", false);
        assertTrue(config);
    }

    @Test
    public void getBooleanValueReturnsDefaultOnFail() {
        server.enqueue(new MockResponse().setResponseCode(500));
        boolean config = this.client.getValue(boolean.class, "fakeKey", true);
        assertTrue(config);
    }

    @Test
    public void getBooleanValueReturnsDefaultOnException() {
        String result = "{ test: test] }";
        boolean def = true;
        server.enqueue(new MockResponse().setResponseCode(200).setBody(result));
        boolean config = this.client.getValue(Boolean.class, "fakeKey", def);
        assertEquals(def, config);
    }

    @Test
    public void getIntegerValue() {
        int iValue = 342423;
        String result = String.format(TEST_JSON, iValue);
        server.enqueue(new MockResponse().setResponseCode(200).setBody(result));
        int config = this.client.getValue(Integer.class, "fakeKey", 0);
        assertEquals(iValue, config);
    }

    @Test
    public void getIntegerValuePrimitive() {
        int iValue = 342423;
        String result = String.format(TEST_JSON, iValue);
        server.enqueue(new MockResponse().setResponseCode(200).setBody(result));
        int config = this.client.getValue(int.class, "fakeKey", 0);
        assertEquals(iValue, config);
    }

    @Test
    public void getIntegerValueReturnsDefaultOnFail() {
        int def = 342423;
        server.enqueue(new MockResponse().setResponseCode(500));
        int config = this.client.getValue(int.class, "fakeKey", def);
        assertEquals(def, config);
    }

    @Test
    public void getIntegerValueReturnsDefaultOnException() {
        String result = "{ test: test] }";
        int def = 14;
        server.enqueue(new MockResponse().setResponseCode(200).setBody(result));
        int config = this.client.getValue(Integer.class, "fakeKey", def);
        assertEquals(def, config);
    }

    @Test
    public void getDoubleValue() {
        double iValue = 432.234;
        String result = String.format(TEST_JSON, iValue);
        server.enqueue(new MockResponse().setResponseCode(200).setBody(result));
        double config = this.client.getValue(double.class, "fakeKey", 0.0);
        assertEquals(iValue, config);
    }

    @Test
    public void getDoubleValueReturnsDefaultOnFail() {
        double def = 432.234;
        server.enqueue(new MockResponse().setResponseCode(500));
        double config = this.client.getValue(Double.class, "fakeKey", def);
        assertEquals(def, config);
    }

    @Test
    public void getDefaultValueWhenKeyNotExist() {
        String result = String.format(TEST_JSON, "true");
        server.enqueue(new MockResponse().setResponseCode(200).setBody(result));
        boolean config = this.client.getValue(Boolean.class, "nonExistingKey", false);
        assertFalse(config);
    }

    @Test
    public void getDoubleValueReturnsDefaultOnException() {
        String result = "{ test: test] }";
        double def = 14.5;
        server.enqueue(new MockResponse().setResponseCode(200).setBody(result));
        double config = this.client.getValue(Double.class, "fakeKey", def);
        assertEquals(def, config);
    }

    @Test
    public void invalidateCache() {
        server.enqueue(new MockResponse().setResponseCode(200).setBody(String.format(TEST_JSON, "test")));
        server.enqueue(new MockResponse().setResponseCode(200).setBody(String.format(TEST_JSON, "test2")));

        assertEquals("test", this.client.getValue(String.class, "fakeKey", null));
        this.client.forceRefresh();
        assertEquals("test2", this.client.getValue(String.class, "fakeKey", null));
    }

    @Test
    public void invalidateCacheFail() {
        server.enqueue(new MockResponse().setResponseCode(200).setBody(String.format(TEST_JSON, "test")));
        server.enqueue(new MockResponse().setResponseCode(500));

        assertEquals("test", this.client.getValue(String.class, "fakeKey", null));
        this.client.forceRefresh();
        assertEquals("test", this.client.getValue(String.class, "fakeKey", null));
    }

    @Test
    public void getConfigurationJsonStringWithDefaultConfigTimeout() {
        ConfigCatClient cl = ConfigCatClient.newBuilder()
                .httpClient(new OkHttpClient.Builder().readTimeout(2, TimeUnit.SECONDS).build())
                .build(APIKEY);

        // makes a call to a real url which would fail, null expected
        String config = cl.getValue(String.class, "test", null);
        assertNull(config);
    }

    @Test
    public void getConfigurationJsonStringWithDefaultConfig() throws InterruptedException, ExecutionException, TimeoutException {
        ConfigCatClient cl = new ConfigCatClient(APIKEY);
        assertNull(cl.getValueAsync(String.class, "test", null).get(2, TimeUnit.SECONDS));
    }

    @Test
    public void getAllKeys() {
        ConfigCatClient cl = ConfigCatClient.newBuilder()
                .logLevel(LogLevel.INFO)
                .dataGovernance(DataGovernance.EU_ONLY)
                .build("PKDVCLf-Hq-h-kCzMp-L7Q/psuH7BGHoUmdONrzzUOY7A");

        Collection<String> keys = cl.getAllKeys();

        assertEquals(16, keys.size());
        assertTrue(keys.contains("stringDefaultCat"));
    }
}