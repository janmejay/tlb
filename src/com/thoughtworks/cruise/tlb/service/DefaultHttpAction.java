package com.thoughtworks.cruise.tlb.service;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.params.HttpClientParams;

import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;

import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;
import static com.thoughtworks.cruise.tlb.TlbConstants.USERNAME;
import static com.thoughtworks.cruise.tlb.TlbConstants.PASSWORD;

/**
 * @understands talking http protocol using http client
 */
public class DefaultHttpAction implements HttpAction {
    private final HttpClient client;

    public DefaultHttpAction(SystemEnvironment environment) {
        HttpClientParams params = new HttpClientParams();

        if (environment.getProperty(USERNAME) != null) {
            params.setAuthenticationPreemptive(true);
            client = new HttpClient(params);
            client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(environment.getProperty(USERNAME), environment.getProperty(PASSWORD)));
        } else {
            client = new HttpClient(params);
        }
    }

    public String get(String url) {
        url = httpUrl(url);
        GetMethod method = new GetMethod(url);
        try {
            int result = client.executeMethod(method);
            if (result >= 300 && result < 400) {
                get(method.getResponseHeader("Location").getValue());
            }
            if (result != 200) {
                throw new RuntimeException("Something went horribly wrong. Bu Hao. The response looks like: " + method.getResponseBodyAsString());
            }
            return method.getResponseBodyAsString();
        } catch (IOException e) {
            throw new RuntimeException("Oops! Something went wrong", e);
        }
    }

    //HACK! Replace this with actual https support.
    private String httpUrl(String url) {
        try {
            URL url1 = new URL(url);
            String baseUrl = url;
            if (url1.getProtocol().equals("https")) {
                baseUrl = String.format("http://%s:%s%s", url1.getHost(), url1.getPort() - 1, url1.getPath());
                if (url1.getQuery() != null && !url1.getQuery().isEmpty()) {
                    baseUrl = baseUrl + "?" + url1.getQuery();
                }
            }
            System.out.println("Using url = " + baseUrl);
            return baseUrl;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public String post(String url) {
        throw new RuntimeException("Not yet implemented");
    }
}
