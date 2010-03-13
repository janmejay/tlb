package com.thoughtworks.cruise.tlb.service.http.request;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;

import java.io.IOException;
import java.util.logging.Logger;

import com.thoughtworks.cruise.tlb.utils.RetryAfter;
import com.thoughtworks.cruise.tlb.service.http.DefaultHttpAction;

/**
 * @understands error ressilient http request handling
 */
public abstract class FollowableHttpRequest {
    private DefaultHttpAction defaultHttpAction;
    private RetryAfter retryer;
    private static final Logger logger = Logger.getLogger(FollowableHttpRequest.class.getName());

    public FollowableHttpRequest(DefaultHttpAction defaultHttpAction) {
        this(defaultHttpAction, new RetryAfter(RetryAfter.seq(10, 8*6)));
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
        String baseMessage = String.format("http request to %s with %s", url, method.getClass().getSimpleName());
        logger.info("attempting " + baseMessage);
        try {
            int result = retryer.tryFn(new RetryAfter.Fn<Integer>() {
                public Integer fn() throws Exception {
                    return defaultHttpAction.executeMethod(method);
                }
            });
            logger.info(baseMessage + " returned " + result);
            if (result >= 300 && result < 400) {
                return executeRequest(method.getResponseHeader("Location").getValue());
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
