package com.cyberark;


import com.cyberark.controllers.ControllerFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

/**
 * Application main entry class
 */
public class ConjurEnchanter {
  private static final Logger logger = LogManager.getLogger(ConjurEnchanter.class);

  public static void main(String[] args) {
    logger.info("Starting Enchanter...");
    logger.info("Checking if the application is set to trust all certificates");
    String ignoreCertSystemSetting = System.getProperty("ignore.certs");
    logger.info("System settings: 'ignore.certs'='{}'", ignoreCertSystemSetting);

    if (ignoreCertSystemSetting == null || Boolean.parseBoolean(ignoreCertSystemSetting)) {
      setupToIgnoreCertificates();
    }

    logger.info("Invoke authentication controller");
    ControllerFactory.getInstance().getAuthnController().login();
  }

  private static void setupToIgnoreCertificates() {
    logger.trace("setupToIgnoreCertificates:enter");
    TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager(){
      public X509Certificate[] getAcceptedIssuers(){return null;}
      public void checkClientTrusted(X509Certificate[] certs, String authType){}
      public void checkServerTrusted(X509Certificate[] certs, String authType){}
    }};

    // Install the all-trusting trust manager
    try {
      SSLContext sc = SSLContext.getInstance("TLS");
      sc.init(null, trustAllCerts, new SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    } catch (Exception e) {
      // ignore
    }
    logger.warn("Enchanter is configured to trust all certificates");
    logger.trace("setupToIgnoreCertificates:exit");
  }
}
