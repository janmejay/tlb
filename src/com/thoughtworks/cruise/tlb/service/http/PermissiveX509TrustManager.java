package com.thoughtworks.cruise.tlb.service.http;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class PermissiveX509TrustManager implements X509TrustManager {

    private X509TrustManager standardTrustManager = null;

    public PermissiveX509TrustManager(KeyStore keystore) throws NoSuchAlgorithmException, KeyStoreException {
        super();
        TrustManagerFactory factory = TrustManagerFactory.getInstance("SunX509");
        factory.init(keystore);

        TrustManager[] trustmanagers = factory.getTrustManagers();

        if (trustmanagers.length == 0)
            throw new NoSuchAlgorithmException("SunX509 trust manager not supported");

        this.standardTrustManager = (X509TrustManager) trustmanagers[0];
    }

    public void checkClientTrusted( X509Certificate[] certificates, String string) throws CertificateException {
        this.standardTrustManager.checkClientTrusted(certificates, string);
    }

    public void checkServerTrusted( X509Certificate[] certificates, String string) throws CertificateException {
        if ((certificates != null) && (certificates.length == 1)) {
            X509Certificate certificate = certificates[0];

            try {
                certificate.checkValidity();
            } catch (CertificateException e) {
                e.printStackTrace();
            }
        } else {
            this.standardTrustManager.checkServerTrusted(certificates, string);
        }
    }

    public X509Certificate[] getAcceptedIssuers() {
        return this.standardTrustManager.getAcceptedIssuers();
    }
}
