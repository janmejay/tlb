package com.thoughtworks.cruise.tlb.service.http;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Test;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.hamcrest.core.Is.is;

public class FollowableHttpRequestTest {
    @Test
    public void shouldRetryOnTheNewLocationFor3XX() throws Exception{
        final GetMethod getMethod = mock(GetMethod.class);
        when(getMethod.getResponseBodyAsString()).thenReturn("redirected");
        when(getMethod.getRequestHeader("Location")).thenReturn(new Header("Location", "http://some_cruise:8153/cruise/redirected"));
        final GetMethod redirectedMethod = mock(GetMethod.class);
        when(redirectedMethod.getResponseBodyAsString()).thenReturn("actual body");
        HttpClient client = mock(HttpClient.class);
        DefaultHttpAction action = new DefaultHttpAction(client, new URI("http://some_cruise:8153/cruise", true));
        when(client.executeMethod(getMethod)).thenReturn(302);
        when(client.executeMethod(redirectedMethod)).thenReturn(200);
        FollowableHttpRequest req = new FollowableHttpRequest(action) {
            public HttpMethodBase createMethod(String url) {
                return url.endsWith("redirected") ? getMethod : redirectedMethod;
            }
        };
        assertThat(req.executeRequest("http://some_cruise:8153/cruise"), is("actual body"));
    }
}
