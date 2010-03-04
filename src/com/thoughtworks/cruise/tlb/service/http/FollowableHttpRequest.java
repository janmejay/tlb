package com.thoughtworks.cruise.tlb.service.http;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;

import java.io.IOException;

/**
 * @understands
*/
public abstract class FollowableHttpRequest {
    private DefaultHttpAction defaultHttpAction;

    FollowableHttpRequest(DefaultHttpAction defaultHttpAction) {
        this.defaultHttpAction = defaultHttpAction;
    }

    public abstract HttpMethodBase createMethod(String url);

    public String executeRequest(String url) {
        HttpMethodBase method = createMethod(url);
        try {
            int result = defaultHttpAction.executeMethod(method);
            if (result >= 300 && result < 400) {
                executeRequest(method.getResponseHeader("Location").getValue());
            }
            if (result < 200 || result >= 300) {
                throw new RuntimeException(String.format("Something went horribly wrong. Bu Hao. The response[status: %s] looks like: %s", result, method.getResponseBodyAsString()));
            }
            return method.getResponseBodyAsString();
        } catch (IOException e) {
            throw new RuntimeException("Oops! Something went wrong", e);
        }
    }


}
