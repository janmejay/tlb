package com.thoughtworks.cruise.tlb.domain;

import com.thoughtworks.cruise.tlb.TlbFileResource;

/**
 * @understands a file resource which can have one or more tests
 */
public class TestFileResource {
    private TlbFileResource resource;

    public TestFileResource(TlbFileResource resource) {
        this.resource = resource;
    }

    public String getName() {
        return resource.getName();
    }

    public TlbFileResource getWrappedResource() {
        return resource;
    }
}
