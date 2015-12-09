/**
 *
 */
package mrtech.smarthome.util;

import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


/**
 * @author CJ
 * @version 1.0
 * @date 2015年4月9日 下午10:22:26
 */
public class NetUtil {


    public static SSLSocket createSocket(String ip, int port) throws Exception {
        SSLSocket s = null;
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        ByteArrayInputStream bain = new ByteArrayInputStream(Base64.decode(Constants.PRIVATE_CODE, Base64.DEFAULT));
        X509Certificate cert = (X509Certificate) cf.generateCertificate(bain);

        // 取得SSL的SSLContext实例
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        SmartHomeX509TrustManager x509TrustManager = new SmartHomeX509TrustManager(new X509Certificate[]{cert}, cert);
        sslContext.init(null, new TrustManager[]
                {x509TrustManager}, null);
        SSLSocketFactory factory = sslContext.getSocketFactory();
        s = (SSLSocket) factory.createSocket(ip, port);
        return s;
    }

}

class SmartHomeX509TrustManager implements X509TrustManager {
    private final Set<TrustAnchor> anchors;
    private X509Certificate cert;

    public SmartHomeX509TrustManager(X509Certificate[] trusted, X509Certificate cert) {
        anchors = new HashSet<TrustAnchor>();
        this.cert = cert;
        if (anchors != null) {
            for (X509Certificate certTemp : trusted) {
                anchors.add(new TrustAnchor(certTemp, null));
            }
        }
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {

    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        CertPathValidator certPathValidator = null;
        PKIXParameters parameters = null;
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        CertPath path = certFactory.generateCertPath(Arrays.asList(chain));
        try {
            certPathValidator = CertPathValidator.getInstance("PKIX");
            parameters = new PKIXParameters(anchors);
            parameters.setRevocationEnabled(false);
            certPathValidator.validate(path, parameters);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e1) {
            e1.printStackTrace();
        } catch (CertPathValidatorException e) {
            e.printStackTrace();
        }

    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[]
                {cert};
    }
}
