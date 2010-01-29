package com.thoughtworks.cruise.tlb.service;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.params.HttpClientParams;

import java.io.IOException;

import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;
import com.thoughtworks.cruise.tlb.TlbConstants;
import static com.thoughtworks.cruise.tlb.TlbConstants.USERNAME;
import static com.thoughtworks.cruise.tlb.TlbConstants.PASSWORD;

/**
 * @understands talking http protocol using http client
 */
public class DefaultHttpAction implements HttpAction {
    private final HttpClient client;

    public DefaultHttpAction(SystemEnvironment environment) {
        HttpClientParams params = new HttpClientParams();
        params.setAuthenticationPreemptive(true);
        client = new HttpClient(params);
        if (environment.getProperty(USERNAME) != null) {
            client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(environment.getProperty(USERNAME), environment.getProperty(PASSWORD)));
        }
    }

    public String get(String url) {
        GetMethod method = new GetMethod(url);
        try {
            int result = client.executeMethod(method);
            if (result != 200) {
                throw new RuntimeException("Something went horribly wrong. Bu Hao. The response looks like: " + method.getResponseBodyAsString());
            }
            return method.getResponseBodyAsString();
        } catch (IOException e) {
            throw new RuntimeException("Oops! Something went wrong", e);
        }
    }
    public String post(String url) {
        throw new RuntimeException("Not yet implemented");
    }
}
