package com.blue.http;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;


public class AutoCheckin {

	private final static String LOGIN_URL = "https://zcssr.com/auth/login";
	private static Pattern p = Pattern.compile("msg\":\"(.*?)\"");

	public void login(String username, String pwd) throws Exception {
//		CloseableHttpClient client = HttpClientBuilder.create().build();
		SSLContext sslContext = createIgnoreVerifySSL();
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, new String[] { "TLSv1" }, null,
				SSLConnectionSocketFactory.getDefaultHostnameVerifier());
		CloseableHttpClient client = HttpClients.custom().setSSLSocketFactory(sslsf).build();
		HttpHost proxy = new HttpHost("proxy.webank.com", 8080, "http");

		RequestConfig config = RequestConfig.custom().setProxy(proxy).build();

		HttpPost doLogin = new HttpPost(LOGIN_URL);
        List<NameValuePair> form = new ArrayList<NameValuePair>();
		form.add(new BasicNameValuePair("email", username));
		form.add(new BasicNameValuePair("passwd", pwd));
		form.add(new BasicNameValuePair("code", ""));
        UrlEncodedFormEntity postInfo = new UrlEncodedFormEntity(form, "utf-8");
        doLogin.setEntity(postInfo);
		doLogin.setConfig(config);

		CloseableHttpResponse response = getRealResponse(client, doLogin);
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new Exception("登录失败");
        }


		String s = getContent(client, config, "https://zcssr.com/user/checkin", null);
		Matcher m = p.matcher(s);

		if (m.find()) {
			String msg = m.group(1);
			System.out.println("签到成功！" + StringUtil.utfFormat(msg));
		}
		client.close();
    }

    /**
	 * 302 自动跳转
	 * 
	 * @param client
	 * @param post
	 * @return
	 * @throws Exception
	 */
	private CloseableHttpResponse getRealResponse(CloseableHttpClient client, HttpPost post) throws Exception {
        CloseableHttpResponse response = client.execute(post);
        while (response.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY) {
            response.close();
            String location = response.getFirstHeader("Location").getValue();
            response = client.execute(new HttpPost(location));
        }
        return response;

    }


    /**
	 * Map != null 使用post方式提交，否则使用Get提交
	 * 
	 * @param client
	 * @param config
	 * @param url
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public String getContent(CloseableHttpClient client, RequestConfig config, String url, Map<String, String> param)
			throws Exception {

        HttpPost post = new HttpPost(url);
		post.setConfig(config);
        if (param != null) {
            List<NameValuePair> form = new ArrayList<NameValuePair>();
            param.entrySet().forEach(t -> {
                form.add(new BasicNameValuePair(t.getKey(), t.getValue()));
            });
            UrlEncodedFormEntity postInfo = new UrlEncodedFormEntity(form, "utf-8");
            post.setEntity(postInfo);
        }

        CloseableHttpResponse response = client.execute(post);
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new Exception(url + "查询失败");
        }
        return EntityUtils.toString(response.getEntity(), "utf-8");
    }

	public static SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
		SSLContext sc = SSLContext.getInstance("TLSv1.2");

		// 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
		X509TrustManager trustManager = new X509TrustManager() {
			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
					String paramString) throws CertificateException {
			}

			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
					String paramString) throws CertificateException {
			}

			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[] {};
			}
		};

		sc.init(null, new TrustManager[] { trustManager }, null);
		return sc;
	}

	public static void main(String[] args) throws Exception {
		String[] users = { "blue_ranger@126.com", "weiwei_mi@yeah.net", "xue_ranger@yeah.net" };
		String pwd = System.getProperty("pwd");
		for (String t : users) {
			try {
				System.out.println("auto checkin " + t);
				AutoCheckin c = new AutoCheckin();
				c.login(t, pwd);
				System.out.println("check in success " + t);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
