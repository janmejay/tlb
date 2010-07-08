package com.github.tlb.service.http;

import java.util.Map;

/**
 * @understands http protocol method
 */
public interface HttpAction {

    String get(String url);

    String post(String url, Map<String,String> data);

    String put(String url, String data);

    String post(String url, String data);
}
