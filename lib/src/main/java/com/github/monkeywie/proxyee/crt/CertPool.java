package com.github.monkeywie.proxyee.crt;

import com.github.monkeywie.proxyee.server.HttpProxyServerConfig;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class CertPool {

    private static Map<Integer, Map<String, X509Certificate>> certCache = new WeakHashMap<>();

    public static X509Certificate getCert(Integer port, String host, HttpProxyServerConfig serverConfig)
            throws Exception {
        X509Certificate cert = null;
        if (host != null) {
            Map<String, X509Certificate> portCertCache = certCache.get(port);
            if (portCertCache == null) {
                portCertCache = new HashMap<>();
                certCache.put(port, portCertCache);
            }
            String key = host.trim().toLowerCase();
            if (portCertCache.containsKey(key)) {
                return portCertCache.get(key);
            } else {
                cert = CertUtil.genCert(serverConfig.getIssuer(), serverConfig.getCaPriKey(),
                        serverConfig.getCaNotBefore(), serverConfig.getCaNotAfter(),
                        serverConfig.getServerPubKey(), key);
                portCertCache.put(key, cert);
            }
        }
//        trustManager().checkClientTrusted(new X509Certificate[]{cert} ,"ECDHE_RSA");
        return cert;
    }

    public static void clear() {
        certCache.clear();
    }


    public static X509TrustManager trustManager() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        X509Certificate certificate = CertUtil.loadCert(Thread.currentThread().getContextClassLoader().getResourceAsStream("ca_test.crt"));
        keyStore.load(null, null);
        keyStore.setCertificateEntry("test", certificate);

        TrustManagerFactory trustManagerFactory = null;
        trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
            throw new IllegalStateException("Unexpected default trust managers:"
                    + Arrays.toString(trustManagers));
        }
        X509TrustManager trustManager = (X509TrustManager) trustManagers[0];
        return trustManager;
    }
}
