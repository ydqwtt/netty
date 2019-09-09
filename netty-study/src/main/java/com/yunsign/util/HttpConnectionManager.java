package com.yunsign.util;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;


public class HttpConnectionManager {
	
	/**
	 * 闲置连接超时时间, 由bean factory设置，缺省为120秒钟
	 */
	private static final int DEFAULT_IDLE_TIMEOUT = 120000;
	
	/**
	 * 线程等待时间 缺省5s
	 */
	private static final int THREAD_WAIT_TIME = 5000;
	
	/**
	 * HTTP连接管理器，该连接管理器必须是线程安全的.
	 */
	private static PoolingHttpClientConnectionManager connectionManager;

	/**
	 * 后台监控线程
	 */
	private static IdleConnectionMonitorThread idleEvictThread;
	
	
	/**
     * 绕过验证
     * 
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    private static SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sc = SSLContext.getInstance("TLSv1.2");//SSLv3 TLSv1.2 TLSv1
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
	
	/**
     * 功能描述: <br>
     * 初始化连接池管理对象
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    private static PoolingHttpClientConnectionManager getPoolManager() {
    	
    	if (null == connectionManager) {
    		System.out.println("Initail PoolingHttpClientConnectionManager.");
    		
    		 synchronized (PoolingHttpClientConnectionManager.class) {
    			 try {
    				 SSLContext sslcontext = createIgnoreVerifySSL();
                     Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create()
                             .register("http", new PlainConnectionSocketFactory())
//                             .register("https", SSLConnectionSocketFactory.getSocketFactory())
                             .register("https", new SSLConnectionSocketFactory(sslcontext))
                             .build();
                     connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
                     // 设置最大连接数
                     connectionManager.setMaxTotal(512);
                     // 设置单路由最大连接数
                     connectionManager.setDefaultMaxPerRoute(256);
                     // 后台监控线程
                     idleEvictThread = new IdleConnectionMonitorThread(connectionManager);
             		 idleEvictThread.start();

             		 // 在JVM中增加一个关闭的钩子
             		 Runtime.getRuntime().addShutdownHook(new Thread() {
             			 @Override
             			 public void run() {
             				 shutdown();
             			 }
             		 });
                 } catch (Exception e) {
                	 e.printStackTrace();
                 }
             }
    	}
    	
        return connectionManager;
        
    }
    
    /**
     * 创建线程安全的HttpClient
     * @param config 客户端超时设置
     * @return
     */
    public static CloseableHttpClient getHttpClient(RequestConfig config) {
        CloseableHttpClient httpClient = HttpClients.custom()
        		.setDefaultRequestConfig(config)
                .setConnectionManager(getPoolManager())
				.setConnectionManagerShared(true)
                .build();
        return httpClient;
    }
    
    /**
     * 清理操作
     */
    private static void shutdown() {
		idleEvictThread.shutdown();
		connectionManager.shutdown();
		
	}
	
	/**
	 * 守护线程，定时清理过期和空闲时间超时的连接
	 *
	 * @author xunyang.liu 2017-10-10
	 */
	private static class IdleConnectionMonitorThread extends Thread {

		private final PoolingHttpClientConnectionManager connMgr;
		private volatile boolean shutdown;

		public IdleConnectionMonitorThread(PoolingHttpClientConnectionManager connectionManager) {
			this.connMgr = connectionManager;
			this.setDaemon(true);// 守护线程
		}

		@Override
		public void run() {
			try {
				while (!shutdown) {
					synchronized (this) {
						wait(THREAD_WAIT_TIME);
						// 关闭过期连接
						connMgr.closeExpiredConnections();
						// 关闭一定时间的空闲连接
						// that have been idle longer than 120 sec
						connMgr.closeIdleConnections(DEFAULT_IDLE_TIMEOUT,
								TimeUnit.MILLISECONDS);
					}
				}
			} catch (InterruptedException ex) {
				System.out.println("Close connection happens exception." + ex);
			}
		}

		public void shutdown() {
			if (!shutdown) {
				shutdown = true;
				synchronized (this) {
					notifyAll();
				}
			}
		}

	}


}
