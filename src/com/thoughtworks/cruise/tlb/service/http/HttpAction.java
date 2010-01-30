package com.thoughtworks.cruise.tlb.service.http;

/**
 * @understands http protocol method
 */
public interface HttpAction {

    String get(String url);

    String post(String url);
}
