package com.gotkcups.tools;

import java.io.*;
import java.security.*;
import java.security.cert.*;
import javax.net.ssl.*;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

public class InstallTrustedCerts {

    private static String password = "changeit";
    private static final Logger logger = Logger.getLogger(
            InstallTrustedCerts.class);

    public static void main(String[] args) throws Exception {
        Security.insertProviderAt(new BouncyCastleProvider(),1);
        //args = new String[]{"49138d2352f2a25efe0f8e5cf58577d3:ce90c3e31f002b84eb0a7c84428db576@gotkcups.myshopify.com", "443"};
        args = new String[]{"teves.us", "22"};
        InstallTrustedCerts.initializeCertificate(args[0],
                Integer.parseInt(args[1]));
    }

    public static SSLContext createSSLClientContext(String host, int port) throws Exception {
        // Create/initialize the SSLContext with key material
        char[] passphrase = password.toCharArray();
        // First initialize the key and trust material.
        char SEP = File.separatorChar;
        File securityDir = new File(System.getProperty("java.home") + SEP
                + "lib" + SEP + "security");
        //File jssecacerts = new File(securityDir, host + "_" + port); // HD #3489
        File jssecacerts = new File(securityDir, "cacerts"); // HD #3489

        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(jssecacerts), passphrase);
        SSLContext sslContext = SSLContext.getInstance("TLS");

        // TrustManager's decide whether to allow connections.
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);
        sslContext.init(null, tmf.getTrustManagers(), null);
        return sslContext;
    }

    public static SSLContext createSSLServerContext() throws Exception {
        // Create/initialize the SSLContext with key material
        char[] passphrase = "avajsop".toCharArray();
        // First initialize the key and trust material.
        char SEP = File.separatorChar;
        File jssecacerts = new File("obssl");
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(jssecacerts), passphrase);
        SSLContext sslContext = SSLContext.getInstance("TLS");
        // TrustManager's decide whether to allow connections.
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);
        sslContext.init(kmf.getKeyManagers(), null, null);
        return sslContext;
    }

    public static void initializeCertificate(String host,
            int port) {
        char[] passphrase = password.toCharArray();
        char SEP = File.separatorChar;
        File securityDir = new File(System.getProperty("java.home") + SEP
                + "lib" + SEP + "security");
        //File jssecacerts = new File(securityDir, host + "_" + port); // HD #34899
        File jssecacerts = new File(securityDir, "cacerts"); // HD #34899
        logger.debug(String.format("Loading JSSECACERTS file %s",
                jssecacerts.getAbsolutePath()));
        SSLSocketFactory factory = null;
        InputStream in = null;
        SavingTrustManager tm = null;
        KeyStore ks = null;
        SSLSocket socket = null;
        try {
            File certs = jssecacerts;
            if (jssecacerts.exists() == false) {
                certs = new File(securityDir, "cacerts");
            }
            in = new FileInputStream(certs);
            ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(in, passphrase);
            in.close();
            SSLContext context = SSLContext.getInstance("TLS");
            TrustManagerFactory tmf =
                    TrustManagerFactory.getInstance(TrustManagerFactory.
                    getDefaultAlgorithm());
            tmf.init(ks);
            X509TrustManager defaultTrustManager = (X509TrustManager) tmf.
                    getTrustManagers()[0];
            tm = new SavingTrustManager(defaultTrustManager);
            context.init(null, new TrustManager[]{tm}, null);
            factory = context.getSocketFactory();
            logger.debug(String.format("Opening connection to host %s port %d",
                    host, port));
            socket = (SSLSocket) factory.createSocket(host, port);
            //socket.setEnabledCipherSuites(socket.getSupportedCipherSuites()); // HD #3489
            socket.setSoTimeout(15000);
            logger.debug("Starting SSL handshake...");
            
            // JE , #11790
            // TODO
            // From what I read, Java 7 and later will throw an error here.
            // The certificates still get installed. Need to look to see if
            // there is a cleaner way.
            socket.startHandshake();
            logger.debug("No errors, server certificate is already trusted");
            //InstallTrustedCerts.displayCertificates(tm.chain); // HD #34899
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("Store Certificate", ex);

            try {
                X509Certificate[] chain = tm.chain;
                if (chain == null) {
                    throw new Exception(
                            "Unable to obtain server certificate chain");
                }
                logger.debug(String.format("Detected %d certificate(s)",
                        chain.length));
                MessageDigest sha1 = MessageDigest.getInstance("SHA1");
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                int index = 0;
                for (X509Certificate cert : chain) {
                    sha1.update(cert.getEncoded());
                    md5.update(cert.getEncoded());
                    logger.debug(String.format("Subject %s", cert.getSubjectDN()));
                    logger.debug(String.format("Issuer %s", cert.getIssuerDN()));
                    logger.debug(String.format("SHA1 %s",
                            Hex.toHexString(sha1.digest())));
                    logger.debug(String.format("MD5 %s",
                            Hex.toHexString(md5.digest())));

                    //ks.setCertificateEntry("host".concat("" + ++index), cert);
                    ks.setCertificateEntry(host, cert);
                    InstallTrustedCerts.storeCertificate(jssecacerts,
                            passphrase, ks);
                }
            } catch (Exception exx) {
                logger.error("Store Certificate", exx);
            }
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ex1) {
                }
            }
        }

    }

    private static class SavingTrustManager implements X509TrustManager {

        private final X509TrustManager tm;
        private X509Certificate[] chain;

        SavingTrustManager(X509TrustManager tm) {
            this.tm = tm;
        }

        // JE , #11790
        // Add this return statement as part of Java 1.8.
        public X509Certificate[] getAcceptedIssuers() {
            //throw new UnsupportedOperationException();
            return new X509Certificate[0]; 
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType) throws
                CertificateException {
            throw new UnsupportedOperationException();
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws
                CertificateException {
            this.chain = chain;
            tm.checkServerTrusted(chain, authType);
        }
    }

    private static void displayCertificates(X509Certificate[] chain) throws
            NoSuchAlgorithmException, CertificateEncodingException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        for (X509Certificate cert : chain) {
            sha1.update(cert.getEncoded());
            md5.update(cert.getEncoded());
            logger.debug(String.format(">>>> %s", cert.getVersion()));
            logger.debug(String.format("Subject %s", cert.getSubjectDN()));
            logger.debug(String.format("Issuer %s", cert.getIssuerDN()));
            logger.debug(String.format("SHA1 %s", Hex.toHexString(sha1.digest())));
            logger.debug(String.format("MD5 %s", Hex.toHexString(md5.digest())));
        }

    }

    private static void storeCertificate(File jssecacerts, char[] passphrase,
            KeyStore ks) {
        logger.debug("storing");
        OutputStream out = null;
        try {
            out = new FileOutputStream(jssecacerts);
            ks.store(out, passphrase);
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("Store Certificate", ex);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex1) {
                }
            }
        }

    }
}
