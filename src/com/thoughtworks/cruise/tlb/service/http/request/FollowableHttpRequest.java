package com.thoughtworks.cruise.tlb.service.http.request;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;

import java.io.IOException;

import com.thoughtworks.cruise.tlb.utils.RetryAfter;
import com.thoughtworks.cruise.tlb.service.http.DefaultHttpAction;

/**
 * @understands
*/
public abstract class FollowableHttpRequest {
    private DefaultHttpAction defaultHttpAction;
    private RetryAfter retryer;

    public FollowableHttpRequest(DefaultHttpAction defaultHttpAction) {
        this(defaultHttpAction, new RetryAfter(30*1000, 60*1000, 2*60*1000, 5*60*1000));
    }

    FollowableHttpRequest(DefaultHttpAction defaultHttpAction, RetryAfter retryer) {
        this.defaultHttpAction = defaultHttpAction;
        this.retryer = retryer;
    }

    public RetryAfter getRetryer() {
        return retryer;
    }

    public abstract HttpMethodBase createMethod(String url);

    public String executeRequest(String url) {
        final HttpMethodBase method = createMethod(url);
        try {
            int result = retryer.tryFn(new RetryAfter.Fn<Integer>() {
                public Integer fn() throws Exception {
                    return defaultHttpAction.executeMethod(method);
                }
            });
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
