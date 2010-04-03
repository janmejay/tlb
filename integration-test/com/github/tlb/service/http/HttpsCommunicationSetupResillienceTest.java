package com.github.tlb.service.http;

import org.junit.Test;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.SSLProtocolSocketFactory;

/**
 * @understands testing if tlb-cruise ssl communication is resillient to underlying test hooking up protocol handlers
 */
public class HttpsCommunicationSetupResillienceTest {
    @Test
    public void shouldNotGetMessedUpWhenProtocolRegisteredInTest() throws Exception{
        Protocol.registerProtocol("https",  new Protocol("https", (ProtocolSocketFactory) new SSLProtocolSocketFactory(), 443));
        //there is no assert here as this is testing the tlb balancing, and real test is wether tlb can successfully balance such a test
    }
}
