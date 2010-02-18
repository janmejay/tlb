package com.thoughtworks.cruise.tlb.service.http;

import com.thoughtworks.cruise.tlb.TlbConstants;
import static com.thoughtworks.cruise.tlb.TlbConstants.PASSWORD;
import static com.thoughtworks.cruise.tlb.TlbConstants.USERNAME;
import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

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

    abstract class FollowableHttpRequest {
        private HttpClient client;

        protected FollowableHttpRequest(HttpClient client) {
            this.client = client;
        }

        public abstract HttpMethodBase createMethod(String url);

        public String executeRequest(String url) {
            HttpMethodBase method = createMethod(url);
            try {
                int result = client.executeMethod(method);
                if (result >= 300 && result < 400) {
                    executeRequest(method.getResponseHeader("Location").getValue());
                }
                if (result != 200) {
                    throw new RuntimeException("Something went horribly wrong. Bu Hao. The response looks like: " + method.getResponseBodyAsString());
                }
                return method.getResponseBodyAsString();
            } catch (IOException e) {
                throw new RuntimeException("Oops! Something went wrong", e);
            }
        }
    }

    class FollowableGetRequest extends FollowableHttpRequest {
        protected FollowableGetRequest(HttpClient client) {
            super(client);
        }

        public HttpMethodBase createMethod(String url) {
            return new GetMethod(url);
        }
    }

    class FollowablePutRequest extends FollowableHttpRequest {
        private String data;

        protected FollowablePutRequest(HttpClient client, String data) {
            super(client);
            this.data = data;
        }

        public HttpMethodBase createMethod(String url) {
            PutMethod method = new PutMethod(url);
            try {
                method.setRequestEntity(new StringRequestEntity(data, "text/plain", "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                new RuntimeException(e);
            }
            return method;
        }
    }

    class FollowablePostRequest extends FollowableHttpRequest {
        private Map<String, String> data;

        protected FollowablePostRequest(HttpClient client, Map<String, String> data) {
            super(client);
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
        FollowableGetRequest request = new FollowableGetRequest(client);
        return request.executeRequest(url);
    }

    public String post(String url, Map<String,String> data) {
        FollowablePostRequest request = new FollowablePostRequest(client, data);
        return request.executeRequest(url);
    }

    public String put(String url, String data) {
        FollowablePutRequest request = new FollowablePutRequest(client, data);
        return request.executeRequest(url);
    }
}
