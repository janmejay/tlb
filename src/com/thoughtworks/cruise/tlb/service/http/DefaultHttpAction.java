package com.thoughtworks.cruise.tlb.service.http;

import com.thoughtworks.cruise.tlb.TlbConstants;
import static com.thoughtworks.cruise.tlb.TlbConstants.PASSWORD;
import static com.thoughtworks.cruise.tlb.TlbConstants.USERNAME;
import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;

import java.io.IOException;

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
        try {
            URI url = new URI(environment.getProperty(TlbConstants.CRUISE_SERVER_URL), true);
            Protocol.registerProtocol("https", new Protocol("https", (ProtocolSocketFactory) new PermissiveSSLProtocolSocketFactory(), url.getPort()));
        } catch (URIException e) {
            throw new RuntimeException(e);
        }
    }

    public String get(String url) {
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

    public String post(String url) {
        throw new RuntimeException("Not yet implemented");
    }
}
