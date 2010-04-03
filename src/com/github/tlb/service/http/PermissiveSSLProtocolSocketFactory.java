package com.github.tlb.service.http;

import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.ControllerThreadSocketFactory;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class PermissiveSSLProtocolSocketFactory implements SecureProtocolSocketFactory {

    private SSLContext sslcontext = null;

    private SSLContext getSSLContext() {
        if (this.sslcontext == null) {
            SSLContext context;
            try {
                context = SSLContext.getInstance("SSL");
                context.init(null, new TrustManager[]{new PermissiveX509TrustManager(null)}, null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            this.sslcontext = context;
        }
        return this.sslcontext;
    }

    public Socket createSocket(String host, int port, InetAddress clientHost, int clientPort) throws IOException {
        return getSSLContext().getSocketFactory().createSocket(host, port, clientHost, clientPort);
    }

    public Socket createSocket(final String host, final int port, final InetAddress localAddress, final int localPort, final HttpConnectionParams params) throws IOException {
        if (params == null) {
            throw new IllegalArgumentException("Parameters may not be null");
        }
        int timeout = params.getConnectionTimeout();
        return timeout == 0 ? createSocket(host, port, localAddress, localPort) : 
                ControllerThreadSocketFactory.createSocket(this, host, port, localAddress, localPort, timeout);
    }

    public Socket createSocket(String host, int port) throws IOException {
        return getSSLContext().getSocketFactory().createSocket(host, port);
    }

    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
        return getSSLContext().getSocketFactory().createSocket(socket, host, port, autoClose);
    }

    public boolean equals(Object obj) {
        return ((obj != null) && obj.getClass().equals(PermissiveSSLProtocolSocketFactory.class));
    }

    public int hashCode() {
        return PermissiveSSLProtocolSocketFactory.class.hashCode();
    }

}
