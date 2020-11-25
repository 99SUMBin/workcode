package com.fusen.workcode.utils;

import org.apache.http.*;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.*;



/*
<dependency>
	<groupId>org.apache.httpcomponents</groupId>
	<artifactId>httpclient</artifactId>
	<version>4.5.2</version>
</dependency>
<dependency>
	<groupId>org.apache.httpcomponents</groupId>
	<artifactId>httpmime</artifactId>
	<version>4.5.2</version>
</dependency>
*/

public class HttpClientUtils {

    //static final int timeOut = 10 * 1000;

    private static CloseableHttpClient httpClient = null;

    private final static Object syncLock = new Object();

    public static Logger log = LoggerFactory.getLogger(HttpClientUtils.class);

    private static void config(HttpRequestBase httpRequestBase) {
        // 设置Header等
        // httpRequestBase.setHeader("User-Agent", "Mozilla/5.0");
        // httpRequestBase
        // .setHeader("Accept",
        // "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        // httpRequestBase.setHeader("Accept-Language",
        // "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");// "en-US,en;q=0.5");
        // httpRequestBase.setHeader("Accept-Charset",
        // "ISO-8859-1,utf-8,gbk,gb2312;q=0.7,*;q=0.7");

        // 配置请求的超时设置
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(1000)
                .setConnectTimeout(10000)
                .setSocketTimeout(0)
                .build();
        httpRequestBase.setConfig(requestConfig);
    }

    /**
     * 获取HttpClient对象
     *
     * @return
     * @author SHANHY
     * @create 2015年12月18日
     */
    public static CloseableHttpClient getHttpClient(String url) {
        String hostname = url.split("/")[2];
        int port = 80;
        if (hostname.contains(":")) {
            String[] arr = hostname.split(":");
            hostname = arr[0];
            port = Integer.parseInt(arr[1]);
        }
        if (httpClient == null) {
            synchronized (syncLock) {
                if (httpClient == null) {
                    httpClient = createHttpClient(200, 100, 100, hostname, port);
                }
            }
        }
        return httpClient;
    }

    /**
     * 创建HttpClient对象
     *
     * @return
     * @author SHANHY
     * @create 2015年12月18日
     */
    private static CloseableHttpClient createHttpClient(int maxTotal,
                                                        int maxPerRoute, int maxRoute, String hostname, int port) {
        ConnectionSocketFactory plainsf = PlainConnectionSocketFactory
                .getSocketFactory();
        LayeredConnectionSocketFactory sslsf = SSLConnectionSocketFactory
                .getSocketFactory();
        Registry<ConnectionSocketFactory> registry = RegistryBuilder
                .<ConnectionSocketFactory>create().register("http", plainsf)
                .register("https", sslsf).build();
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(
                registry);
        // 将最大连接数增加
        cm.setMaxTotal(maxTotal);
        // 将每个路由基础的连接增加
        cm.setDefaultMaxPerRoute(maxPerRoute);
        HttpHost httpHost = new HttpHost(hostname, port);
        // 将目标主机的最大连接数增加
        cm.setMaxPerRoute(new HttpRoute(httpHost), maxRoute);

        // 请求重试处理
        HttpRequestRetryHandler httpRequestRetryHandler = new HttpRequestRetryHandler() {
            public boolean retryRequest(IOException exception,
                                        int executionCount, HttpContext context) {
                if (executionCount >= 5) {// 如果已经重试了5次，就放弃
                    return false;
                }
                if (exception instanceof NoHttpResponseException) {// 如果服务器丢掉了连接，那么就重试
                    return true;
                }
                if (exception instanceof SSLHandshakeException) {// 不要重试SSL握手异常
                    return false;
                }
                if (exception instanceof InterruptedIOException) {// 超时
                    return false;
                }
                if (exception instanceof UnknownHostException) {// 目标服务器不可达
                    return false;
                }
                if (exception instanceof ConnectTimeoutException) {// 连接被拒绝
                    return false;
                }
                if (exception instanceof SSLException) {// SSL握手异常
                    return false;
                }

                HttpClientContext clientContext = HttpClientContext
                        .adapt(context);
                HttpRequest request = clientContext.getRequest();
                // 如果请求是幂等的，就再次尝试
                if (!(request instanceof HttpEntityEnclosingRequest)) {
                    return true;
                }
                return false;
            }
        };

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .setRetryHandler(httpRequestRetryHandler).build();

        return httpClient;
    }

    private static void setPostParams(HttpPost httpost,
                                      Map<String, Object> params) {
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        Set<String> keySet = params.keySet();
        for (String key : keySet) {
            nvps.add(new BasicNameValuePair(key, params.get(key).toString()));
        }
        try {
            httpost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * POST请求URL获取内容
     *
     * @param url
     * @return
     * @throws IOException
     * @author SHANHY
     * @create 2015年12月18日
     */
    public static String post(String url, Map<String, Object> params) throws Exception {
        HttpPost httppost = new HttpPost(url);
        config(httppost);
        setPostParams(httppost, params);
        CloseableHttpResponse response = null;
        try {
            response = getHttpClient(url).execute(httppost,
                    HttpClientContext.create());
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, "utf-8");
            EntityUtils.consume(entity);
            return result;
            //将返回的字符串结果再转为json对象,就可以取其中的内容
        } catch (Exception e) {
//          e.printStackTrace();
            throw new Exception("HttpClientUtils.post() exception. -->" + e.getMessage());
        } finally {
            try {
                if (response != null)
                    response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发post请求
     * @param url
     * @param data
     * @return
     * @throws Exception
     */
    public static String postStringEntity(String url, String data) throws Exception {
        //加时间戳设置请求头
        /*String time = DateUtils.format_s(new Date());
        HttpPost httppost = new HttpPost(url+"&timestamp="+getURLEncoderString(time));
        httppost.setHeader("Content-Type", "application/json");
        httppost.addHeader("Authorization", "Basic YWRtaW46");*/
        HttpPost httppost = new HttpPost(url);
        config(httppost);
        httppost.setEntity(new StringEntity(data, "utf-8"));
        CloseableHttpResponse response = null;
        try {
            response = getHttpClient(url).execute(httppost,
                    HttpClientContext.create());
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, "utf-8");
            EntityUtils.consume(entity);
            return result;
        } catch (Exception e) {
//          e.printStackTrace();
            throw new Exception("HttpClientUtils.postStringEntity() exception. -->" + e.getMessage());
        } finally {
            try {
                if (response != null)
                    response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发送post请求并加请求头
     */
    public static String postStringEntityAndHeader(String url, String data, Map<String, Object> map) throws Exception {
        HttpPost httppost = new HttpPost(url);
        httppost.setHeader("Content-Type", "application/json");
        Set<String> keySet = map.keySet();
        for (String key : keySet) {
            httppost.setHeader(key, map.get(key).toString());
        }
        config(httppost);
        httppost.setEntity(new StringEntity(data, "utf-8"));
        CloseableHttpResponse response = null;
        try {
            response = getHttpClient(url).execute(httppost,
                    HttpClientContext.create());
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, "utf-8");
            EntityUtils.consume(entity);
            return result;
        } catch (Exception e) {
//          e.printStackTrace();
            throw new Exception("HttpClientUtils.postStringEntity() exception. -->" + e.getMessage());
        } finally {
            try {
                if (response != null)
                    response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String postStreamForString(String url, byte[] bytes, ContentType contentType, Map<String, Object> params) throws Exception {
        HttpPost httppost = new HttpPost(url);
        config(httppost);
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create().setMode(HttpMultipartMode.RFC6532);
        multipartEntityBuilder.addBinaryBody(params.get("filename") == null ? "filename" : params.get("filename").toString(), bytes, contentType, params.get("filenameWithSuffix") == null ? "filename" : params.get("filenameWithSuffix").toString());
        if (null != params) {
            Iterator<Map.Entry<String, Object>> it = params.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Object> e = it.next();
                multipartEntityBuilder.addTextBody(e.getKey(), e.getValue().toString());
            }
        }
        HttpEntity httpEntity = multipartEntityBuilder.build();
        httppost.setEntity(httpEntity);
        //setPostParams(httppost, params);
        CloseableHttpResponse response = null;
        try {
            response = getHttpClient(url).execute(httppost,
                    HttpClientContext.create());
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, "utf-8");
            EntityUtils.consume(entity);
            return result;
        } catch (Exception e) {
//          e.printStackTrace();
            throw new Exception("HttpClientUtils.postStreamForString() exception. -->" + e.getMessage());
        } finally {
            try {
                if (response != null)
                    response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发送get请求将响应转换为字节数组
     * @param url
     * @return
     * @throws Exception
     */
    public static byte[] getForByteArray(String url) throws Exception {
        HttpGet httpget = new HttpGet(url);
        config(httpget);
        CloseableHttpResponse response = null;
        InputStream inputStream = null;
        try {
            response = getHttpClient(url).execute(httpget,
                    HttpClientContext.create());
            inputStream = response.getEntity().getContent();
            ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
            byte[] buff = new byte[100];
            int rc = 0;
            while ((rc = inputStream.read(buff, 0, 100)) != -1) {
                swapStream.write(buff, 0, rc);
            }
            return swapStream.toByteArray();
        } catch (IOException e) {
            throw new Exception("HttpClientUtils.getForInputStream() exception. -->" + e.getMessage());
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
                if (response != null)
                    response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Map<String, Object> getForByteArrayAndContentType(String url) throws Exception {
        HttpGet httpget = new HttpGet(url);
        config(httpget);
        CloseableHttpResponse response = null;
        InputStream inputStream = null;
        try {
            response = getHttpClient(url).execute(httpget,
                    HttpClientContext.create());
            Header contentType = response.getEntity().getContentType();
            inputStream = response.getEntity().getContent();
            ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
            byte[] buff = new byte[100];
            int rc = 0;
            while ((rc = inputStream.read(buff, 0, 100)) != -1) {
                swapStream.write(buff, 0, rc);
            }
            Map<String, Object> map = new HashMap<>();
            map.put("content-type", contentType.getValue());
            map.put("byte-array", swapStream.toByteArray());
            return map;
        } catch (IOException e) {
            throw new Exception("HttpClientUtils.getForInputStream() exception. -->" + e.getMessage());
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
                if (response != null)
                    response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * GET请求
     * @param url
     * @return
     * @author SHANHY
     * @create 2015年12月18日
     */
    public static String get(String url) throws Exception {
        HttpGet httpget = new HttpGet(url);
        config(httpget);
        CloseableHttpResponse response = null;
        try {
            response = getHttpClient(url).execute(httpget,
                    HttpClientContext.create());
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, "utf-8");
            EntityUtils.consume(entity);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception("HttpClientUtils.get() exception. -->" + e.getMessage());
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //return null;
    }


    /**
     * get请求,设置请求头参数
     * @param url  请求路径(包含参数)
     * @param list 请求头参数集合
     * @return
     * @throws Exception
     */
    public static String getRequestByHeaderList(String url, List<Header> list) throws Exception {
        HttpGet httpget = new HttpGet(url);
        config(httpget);
        CloseableHttpResponse response = null;
        try {
            if (list != null && list.size() > 0) {
                for (Header header : list) {
                    httpget.setHeader(header);
                }
            }
            response = getHttpClient(url).execute(httpget,
                    HttpClientContext.create());
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, "utf-8");
            EntityUtils.consume(entity);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception("HttpClientUtils.get() exception. -->" + e.getMessage());
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 将url编码后再进行请求
     * @param str 需要编码的部分(例如时间格式)
     * @return
     */
    private static String getURLEncoderString(String str) {
        String result = "";
        if (null == str) {
            return "";
        }
        try {
            result = java.net.URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 根据request获取请求ip地址
     */
    public static String getIPAddress(HttpServletRequest request) {
        String ip = null;

        //X-Forwarded-For：Squid 服务代理
        String ipAddresses = request.getHeader("X-Forwarded-For");

        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            //Proxy-Client-IP：apache 服务代理
            ipAddresses = request.getHeader("Proxy-Client-IP");
        }

        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            //WL-Proxy-Client-IP：weblogic 服务代理
            ipAddresses = request.getHeader("WL-Proxy-Client-IP");
        }

        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            //HTTP_CLIENT_IP：有些代理服务器
            ipAddresses = request.getHeader("HTTP_CLIENT_IP");
        }

        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            //X-Real-IP：nginx服务代理
            ipAddresses = request.getHeader("X-Real-IP");
        }

        //有些网络通过多层代理，那么获取到的ip就会有多个，一般都是通过逗号（,）分割开来，并且第一个ip为客户端的真实IP
        if (ipAddresses != null && ipAddresses.length() != 0) {
            ip = ipAddresses.split(",")[0];
        }

        //还是不能获取到，最后再通过request.getRemoteAddr();获取
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * post请求解析json数据转化为一个Map
     */
    public static Map<String, Object> getRequestJsonData(HttpServletRequest request) throws Exception {
        Map<String, Object> map;
        // 获取输入流
        BufferedReader streamReader = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));

        // 写入数据到Stringbuilder
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = streamReader.readLine()) != null) {
            sb.append(line);
        }
        String json = sb.toString();
        json = json.replaceAll("\t", "");
        log.info("getRequestJsonData-->{}", json);
        map = JsonUtils.jsonToMap(json);
        if (null == map) {
            throw new Exception("解析request请求的json数据异常！-->" + json);
        }
        return map;
    }

    /**
     * 上传文件
     * @param serviceUrl
     * @param file
     * @return
     */
    public static String uploadFile(String serviceUrl,File file) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        String result = null;
        try {
            //创建HttpClient 请求
            httpClient = HttpClients.createDefault();
            //创建 HttpPost
            HttpPost httpPost = new HttpPost(serviceUrl);

            //创建上传文件的表单(设置中文名)
//            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create().setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
//            entityBuilder.setCharset(Charset.forName("UTF-8"));
            //默认就是使用UTF-8
            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create().setMode(HttpMultipartMode.RFC6532);
            entityBuilder.addTextBody("orderId", "1234124");//添加普通参数
            entityBuilder.addPart("fileName", new FileBody(file));//添加上传的文件
            HttpEntity httpEntity = entityBuilder.build();
            httpPost.setEntity(httpEntity);
            //执行post 请求
            response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity, Charset.forName("UTF-8"));
                System.out.println("上传文件success,response:"+result);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("文件上传异常");
        } finally {
            try {
                if (response!=null){
                    response.close();
                }
                if (httpClient!=null){
                    httpClient.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
