package com.thoughtworks.cruise.tlb.service.http;

import com.thoughtworks.cruise.tlb.TlbConstants;
import static com.thoughtworks.cruise.tlb.TlbConstants.PASSWORD;
import static com.thoughtworks.cruise.tlb.TlbConstants.USERNAME;
import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;

import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.util.Map;

import sun.net.www.http.*;

/**
 * @understands talking http protocol using http client
 */
public class DefaultHttpAction implements HttpAction {
    private final HttpClient client;
    private URI url;
    private boolean ssl;

    public DefaultHttpAction(HttpClient client, URI url) {
        this.client = client;
        this.url = url;
        ssl = url.getScheme().equals("https");
    }

    public DefaultHttpAction(SystemEnvironment environment) {
        this(createHttpClient(environment), createUri(environment));
    }

    private static URI createUri(SystemEnvironment environment) {
        try {
            return new URI(environment.getProperty(TlbConstants.CRUISE_SERVER_URL), true);
        } catch (URIException e) {
            throw new RuntimeException(e);
        }
    }

    private static HttpClient createHttpClient(SystemEnvironment environment) {
        HttpClientParams params = new HttpClientParams();

        if (environment.getProperty(USERNAME) != null) {
            params.setAuthenticationPreemptive(true);
            HttpClient client = new HttpClient(params);
            client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(environment.getProperty(USERNAME), environment.getProperty(PASSWORD)));
            return client;
        } else {
            return new HttpClient(params);
        }
    }

    /**
     * its important that this be done before every http call,
     * as it can be disturbed by tests running under the load balanced environment.
     *
     * Ouch! static state again.
     */
    private void reRegisterProtocol() {
        if (ssl) Protocol.registerProtocol("https", new Protocol("https", (ProtocolSocketFactory) new PermissiveSSLProtocolSocketFactory(), url.getPort()));
    }

    public synchronized int executeMethod(HttpMethodBase method) {
        try {
            reRegisterProtocol();
            return client.executeMethod(method);
        } catch (IOException e) {
            throw new RuntimeException("Oops! Something went wrong", e);
        }
    }

    class FollowableGetRequest extends FollowableHttpRequest {
        protected FollowableGetRequest(DefaultHttpAction action) {
            super(action);
        }

        public HttpMethodBase createMethod(String url) {
            return new GetMethod(url);
        }
    }

    class FollowablePutRequest extends FollowableHttpRequest {
        private String data;

        protected FollowablePutRequest(DefaultHttpAction action, String data) {
            super(action);
            this.data = data;
        }

        public HttpMethodBase createMethod(String url) {
            PutMethod method = new PutMethod(url);
            try {
                method.setRequestEntity(new StringRequestEntity(data, "text/plain", "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            return method;
        }
    }

    class FollowablePostRequest extends FollowableHttpRequest {
        private Map<String, String> data;

        protected FollowablePostRequest(DefaultHttpAction action, Map<String, String> data) {
            super(action);
            this.data = data;
        }

        public HttpMethodBase createMethod(String url) {
            PostMethod method = new PostMethod(url);
            for (Map.Entry<String, String> param : data.entrySet()) {
                method.addParameter(param.getKey(), param.getValue());
            }
            return method;
        }
    }

    public String get(String url) {
        FollowableGetRequest request = new FollowableGetRequest(this);
        return request.executeRequest(url);
    }

    public String post(String url, Map<String,String> data) {
        FollowablePostRequest request = new FollowablePostRequest(this, data);
        return request.executeRequest(url);
    }

    public String put(String url, String data) {
        FollowablePutRequest request = new FollowablePutRequest(this, data);
        return request.executeRequest(url);
    }
}
