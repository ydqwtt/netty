package com.yunsign.util;

import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Consts;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 * HttpUtil 工具类
 */
public class HttpUtil {

	private static final Log LOGGER = LogFactory.getLog(HttpUtil.class);
	
	private static PoolingHttpClientConnectionManager connManager;
	private static RequestConfig requestConfig;
	
	static{
		try {
			SSLContext sslcontext = createIgnoreVerifySSL();
			Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
			        .<ConnectionSocketFactory> create().register("http", PlainConnectionSocketFactory.INSTANCE)
			        .register("https", new SSLConnectionSocketFactory(sslcontext)).build();
			connManager = new PoolingHttpClientConnectionManager(
			        socketFactoryRegistry);
			// 连接池超时时间使用connect超时时间
			requestConfig = RequestConfig.custom()
					.setConnectionRequestTimeout(30000)
					.setConnectTimeout(30000)
					.setSocketTimeout(30000).build();
		} catch (Exception e) {
			LOGGER.error(" [XPAY-SDK] init connectionManager or requestConfig error !!! ",e);
			e.printStackTrace();
		}
	}

	public static String doPostJsonRequest(String reqeustString, String url,
			int connectTimeout, int socketTimeOut) throws Exception {

		CloseableHttpResponse response = null;
		try {
			
			changeRequestConfig(connectTimeout,socketTimeOut);
			CloseableHttpClient httpclient = HttpClients.createDefault();
			HttpPost httpPost = new HttpPost(url);
			httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
			httpPost.setConfig(requestConfig);
			httpPost.setEntity(new StringEntity(reqeustString, ContentType.APPLICATION_JSON));
			response = httpclient.execute(httpPost);
			// get http status code
			int resStatu = response.getStatusLine().getStatusCode();
			String responseString = null;
			if (resStatu == HttpStatus.SC_OK) {
				responseString = EntityUtils.toString(response.getEntity());
			} else {
				throw new Exception(url + ",the statusCode is " + resStatu);
			}
			return responseString;
		} catch (ConnectTimeoutException e) {
			e.printStackTrace();
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (response != null) {
				try {
					EntityUtils.consume(response.getEntity());
					response.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return url;
	}
	
	/**
     * 绕过验证
     * 
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    private static SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sc = SSLContext.getInstance("TLSv1.2");//SSLv3 TLSv1.2 TLS
        // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
        X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                     String paramString) throws CertificateException {}

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                     String paramString) throws CertificateException {}

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        sc.init(null, new TrustManager[] { trustManager }, null);
        return sc;
    }

	public static String doPostJsonRequestByHttps(String reqeustString, String url,
			int connectTimeout, int socketTimeOut) {
		long startTime = System.currentTimeMillis();
		CloseableHttpResponse response = null;
        String responseString = null;
		try {
			
			changeRequestConfig(connectTimeout,socketTimeOut);
			CloseableHttpClient httpsClient = HttpClients.custom().setConnectionManager(connManager).build();
			HttpPost httpPost = new HttpPost(url);			
			httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
			httpPost.addHeader("Authorization","YTViNWYwNzFmZjc3YTplYTUxMmViNzcwNGQ1ZmI1YTZhOTM3Y2FmYTcwZTc3MQ==");
			httpPost.setConfig(requestConfig);
			httpPost.setEntity(new StringEntity(reqeustString, ContentType.APPLICATION_JSON));
			response = httpsClient.execute(httpPost);
			// get http status code
			int resStatu = response.getStatusLine().getStatusCode();
			responseString = null;
			if (resStatu == HttpStatus.SC_OK) {
				responseString = EntityUtils.toString(response.getEntity());
			} else {
				throw new Exception(url + ",the statusCode is " + resStatu);
			}
			LOGGER.info(String.format(" [XPAY-SDK] call sdk-gateway response data : [ %s ] , time consuming : [ %s ] ms !! ",responseString
					,(System.currentTimeMillis()- startTime)));
			return responseString;
		}catch (ConnectTimeoutException e) {
			e.printStackTrace();
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			if (response != null) {
				try {
					EntityUtils.consume(response.getEntity());
					response.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return responseString;
    }
	/**
	 *	POST方式调用
	 * @param requestMap
	 * @param url
	 * @param connectionTimeout
	 * @param socketTimeOut
	 * @return
	 * @throws Exception
	 */
	public static String postRequest(Map<String, Object> requestMap, String url, int connectionTimeout,
									 int socketTimeOut) throws Exception {
		CloseableHttpResponse response = null;
		List<NameValuePair> pairList = new ArrayList<NameValuePair>(requestMap.size());
		for (Map.Entry<String, Object> entry : requestMap.entrySet()) {
			NameValuePair pair = new BasicNameValuePair(entry.getKey(), entry.getValue().toString());
			pairList.add(pair);
		}
		HttpPost httpPost = new HttpPost(url);
		// 连接池超时时间使用connect超时时间
		RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(connectionTimeout)
				.setConnectTimeout(connectionTimeout).setSocketTimeout(socketTimeOut).build();
		httpPost.setConfig(requestConfig);
		httpPost.setEntity(new UrlEncodedFormEntity(pairList, Charset.forName("UTF-8")));
		try {
			response = HttpConnectionManager.getHttpClient(requestConfig).execute(httpPost);
			int status = response.getStatusLine().getStatusCode();
			String responseString = null;
			if (status == HttpStatus.SC_OK) {
				responseString = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
				System.out.println(responseString);
			} else {
				System.out.println("request Url:" + url + ", the connection is error,statusCode=" + status);
				throw new Exception(url + ",the statusCode is " + status);
			}
			return responseString;
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (response != null) {
				try {
					EntityUtils.consume(response.getEntity());
					response.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Http request ：Post
	 * 参数拼接方式 http://www.baidu.com?a=1&b=2
	 * @param url
	 * @return result map
	 */
	public static String httpPostForm(String jsonStr, String url, int connectionTimeout, int socketTimeOut)
			throws Exception {

		CloseableHttpResponse response = null;
		try {
			HttpPost httpPost = new HttpPost(url);
			httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
			// 连接池超时时间使用connect超时时间
			RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(connectionTimeout)
					.setConnectTimeout(connectionTimeout).setSocketTimeout(socketTimeOut).build();
			httpPost.setConfig(requestConfig);
//			String paraStr = encode(jsonStr);
			String parameter = buildParameter(jsonStr);
			StringEntity stringEntity = new StringEntity(parameter);
			stringEntity.setContentEncoding(Consts.UTF_8.name());
			stringEntity.setContentType("application/x-www-form-urlencoded");
			httpPost.setEntity(stringEntity);
			response = HttpConnectionManager.getHttpClient(requestConfig).execute(httpPost);
			System.out.println(response);
			// get http status code
			int resStatu = response.getStatusLine().getStatusCode();
			String responseString = null;
			if (resStatu == HttpStatus.SC_OK) {
				responseString = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
//				System.out.println(responseString);
			} else {
				System.out.println("rquestUrl:" + url + ", the connection is error,statusCode=" + resStatu);
				throw new Exception(url + ",the statusCode is " + resStatu);
			}
			return responseString;
		} catch (ConnectTimeoutException e) {
			e.printStackTrace();
			throw e;
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (response != null) {
				try {
					EntityUtils.consume(response.getEntity());
					response.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static String buildParameter(String paraStr) {
		return "parameters=" + paraStr;
	}
	/**
	 * 修改默认超时时间
	 * @param connectionTime
	 * @param soTimeout
	 */
	private static void changeRequestConfig(int connectionTime,int soTimeout){
		if(connectionTime != requestConfig.getConnectionRequestTimeout()  
				|| soTimeout != requestConfig.getSocketTimeout()){
			requestConfig = RequestConfig.custom()
					.setConnectionRequestTimeout(connectionTime)
					.setConnectTimeout(connectionTime)
					.setSocketTimeout(soTimeout).build();
		}
	}
}
