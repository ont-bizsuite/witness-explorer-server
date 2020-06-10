package io.ont.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.ontio.common.ErrorCode;
import com.github.ontio.common.Helper;
import com.github.ontio.crypto.Digest;
import com.github.ontio.io.BinaryWriter;
import com.github.ontio.network.exception.RpcException;
import io.ont.exception.SourcingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author zhouq
 * @date 2018/02/27
 */
@Component
@Slf4j
public class HelperUtil {

    private static HttpClient httpClient;
    private static final String JSON_RPC_VERSION = "2.0";

    private static ConfigParam configParam;
    @Autowired
    private ConfigParam autoConfigParam;
    private static URL url;

    @PostConstruct
    public void init() throws MalformedURLException {
        configParam = autoConfigParam;
        url = new URL(configParam.MIDDLEWARE_URL);
    }

    static {
        //HttpClient4.5版本后的参数设置
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        //客户端和服务器建立连接的timeout
        requestConfigBuilder.setConnectTimeout(30000);
        //从连接池获取连接的timeout
        requestConfigBuilder.setConnectionRequestTimeout(30000);
        //连接建立后，request没有回应的timeout。
        requestConfigBuilder.setSocketTimeout(60000);

        HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        clientBuilder.setDefaultRequestConfig(requestConfigBuilder.build());
        //连接建立后，request没有回应的timeout
        clientBuilder.setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(60000).build());
        clientBuilder.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy());

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(200);
        cm.setDefaultMaxPerRoute(30);
        httpClient = clientBuilder.setConnectionManager(cm).build();
    }

    /**
     * check the param whether is null or ''
     *
     * @param params
     * @return boolean
     */
    public static Boolean isEmptyOrNull(Object... params) {
        if (params != null) {
            for (Object val : params) {
                if ("".equals(val) || val == null) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }


    /**
     * merge byte[] head and byte[] tail ->byte[head+tail] rs
     *
     * @param head
     * @param tail
     * @return byte[]
     */
    public static byte[] byteMerrage(byte[] head, byte[] tail) {
        byte[] temp = new byte[head.length + tail.length];
        System.arraycopy(head, 0, temp, 0, head.length);
        System.arraycopy(tail, 0, temp, head.length, tail.length);
        return temp;
    }


    /**
     * judge whether the string is in json format.
     *
     * @param str
     * @return
     */
    public static Boolean isJSONStr(String str) {
        try {
            JSONObject obj = JSONObject.parseObject(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 日期格式字符串转换成时间戳
     *
     * @param dateStr 字符串日期
     * @param format  如：yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static long Date2TimeStamp(String dateStr, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.parse(dateStr).getTime() / 1000L;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * get current method name
     *
     * @return
     */
    public static String currentMethod() {
        return new Exception("").getStackTrace()[1].getMethodName();
    }


    //length用户要求产生字符串的长度
    public static String getRandomString(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    public static String httpClientPost(String url, String reqBodyStr, Map<String, Object> headerMap) throws Exception {

        String responseStr = "";

        StringEntity stringEntity = new StringEntity(reqBodyStr, Charset.forName("UTF-8"));
        stringEntity.setContentType(ContentType.APPLICATION_JSON.getMimeType());

        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(stringEntity);
        //设置请求头
        for (Map.Entry<String, Object> entry :
                headerMap.entrySet()) {
            httpPost.setHeader(entry.getKey(), entry.getValue().toString());
        }

        CloseableHttpResponse response = null;
        try {
            response = (CloseableHttpResponse) httpClient.execute(httpPost);
            HttpEntity httpEntity = response.getEntity();
            responseStr = EntityUtils.toString(httpEntity);
        } catch (IOException e) {
            log.error("{} error...", HelperUtil.currentMethod(), e);
            throw new SourcingException("callback", ErrorInfo.COMM_NET_FAIL.descCN(), ErrorInfo.COMM_NET_FAIL.descEN(), ErrorInfo.COMM_NET_FAIL.code());
        }
        if (response.getStatusLine().getStatusCode() == 200) {
            log.info("send requestbody:{} to {},response 200:{}", reqBodyStr, url, responseStr);
            return responseStr;
        } else {
            log.error("send requestbody:{} to {},response {}:{}", reqBodyStr, url, response.getStatusLine().getStatusCode(), responseStr);
            throw new SourcingException("callback", ErrorInfo.COMM_NET_FAIL.descCN(), ErrorInfo.COMM_NET_FAIL.descEN(), ErrorInfo.COMM_NET_FAIL.code());
        }
    }


    public static String httpClientGet(String uri, Map<String, Object> paramMap, Map<String, Object> headerMap) throws Exception {

        String responseStr = "";

        CloseableHttpResponse response = null;
        URIBuilder uriBuilder = null;
        try {
            //拼完整的请求url
            uriBuilder = new URIBuilder(uri);
            List<NameValuePair> params = new ArrayList<>();
            for (Map.Entry<String, Object> entry :
                    paramMap.entrySet()) {
                params.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
            }
            uriBuilder.setParameters(params);

            HttpGet httpGet = new HttpGet(uriBuilder.build());
            //设置请求头
            for (Map.Entry<String, Object> entry :
                    headerMap.entrySet()) {
                httpGet.setHeader(entry.getKey(), entry.getValue().toString());
            }
            response = (CloseableHttpResponse) httpClient.execute(httpGet);
            HttpEntity httpEntity = response.getEntity();
            responseStr = EntityUtils.toString(httpEntity);
        } catch (Exception e) {
            log.error("{} error...", HelperUtil.currentMethod(), e);
            throw new SourcingException("callback", ErrorInfo.COMM_NET_FAIL.descCN(), ErrorInfo.COMM_NET_FAIL.descEN(), ErrorInfo.COMM_NET_FAIL.code());
        }
        if (response.getStatusLine().getStatusCode() == 200) {
            log.info("send to {},response 200:{}", uriBuilder.toString(), responseStr);
            return responseStr;
        } else {
            log.error("send to {},response {}:{}", uriBuilder.toString(), response.getStatusLine().getStatusCode(), responseStr);
            throw new SourcingException("callback", ErrorInfo.COMM_NET_FAIL.descCN(), ErrorInfo.COMM_NET_FAIL.descEN(), ErrorInfo.COMM_NET_FAIL.code());
        }
    }

    public static Object rpcCall(String method, Object params) throws RpcException, IOException {
        Map request = new HashMap();
        request.put("jsonrpc", JSON_RPC_VERSION);
        request.put("method", method);
        request.put("params", params);
        request.put("id", "1");

        Map response = (Map) send(request);
        if (response == null) {
            throw new RpcException(0, ErrorCode.ConnectUrlErr(url + " response is null. maybe is connect error"));
        } else if ((int) response.get("error") == 0) {
            return response.get("result");
        } else if ((int) response.get("error") == 41003){
            return null;
        } else {
            throw new SourcingException("rpcCall",ErrorInfo.PARAM_ERROR.descCN(),ErrorInfo.PARAM_ERROR.descEN(),ErrorInfo.PARAM_ERROR.code());
        }
    }

    public static Object send(Object request) throws IOException {
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("addonID", configParam.ADDON_ID);
            connection.setRequestProperty("tenantID", configParam.TENANT_ID);
            connection.setDoOutput(true);
            try (OutputStreamWriter w = new OutputStreamWriter(connection.getOutputStream())) {
                w.write(JSON.toJSONString(request));
            }
            try (InputStreamReader r = new InputStreamReader(connection.getInputStream())) {
                StringBuffer temp = new StringBuffer();
                int c = 0;
                while ((c = r.read()) != -1) {
                    temp.append((char) c);
                }
                return JSON.parseObject(temp.toString(), Map.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * hash
     */
    public static String sha256(byte[] bytes) {
        byte[] sha256Bytes = Digest.sha256(bytes);
        return Helper.toHexString(sha256Bytes);
    }


    /**
     * 拼接JSON格式参数
     */
    public static String getParams(String action, String contractHash, String method, List argsList, String payer) {
        Map map = new HashMap();
        Map parms = new HashMap();
        Map invokeConfig = new HashMap();
        List functions = new ArrayList();
        Map function = new HashMap();

        function.put("operation", method);
        function.put("args", argsList);

        functions.add(function);

        invokeConfig.put("contractHash", contractHash);
        invokeConfig.put("functions", functions);
        invokeConfig.put("payer", payer);
        invokeConfig.put("gasLimit", Constant.GAS_LIMIT);
        invokeConfig.put("gasPrice", Constant.GAS_PRICE);

        parms.put("invokeConfig", invokeConfig);

        map.put("action", action);
        map.put("params", parms);
        return JSON.toJSONString(map);
    }

    public static void writeBigInt(BinaryWriter writer, int value) throws IOException {
        String str = String.valueOf(value);
        byte[] bytes = Helper.BigIntToNeoBytes(new BigInteger((str)));
        writer.writeVarBytes(bytes);
    }

    public static void pushU32(OutputStream out, int data) throws IOException {
        byte[] ret = new byte[4];
        ret[0] = (byte) (data & 0xFF);
        ret[1] = (byte) ((data >> 8) & 0xFF);
        ret[2] = (byte) ((data >> 16) & 0xFF);
        ret[3] = (byte) ((data >> 24) & 0xFF);
        out.write(ret, 0, ret.length);
    }

    public static void pushU64(OutputStream out, long data) throws IOException {
        byte[] ret = new byte[8];
        ret[0] = (byte) (data & 0xFF);
        ret[1] = (byte) ((data >> 8) & 0xFF);
        ret[2] = (byte) ((data >> 16) & 0xFF);
        ret[3] = (byte) ((data >> 24) & 0xFF);
        ret[4] = (byte) ((data >> 32) & 0xFF);
        ret[5] = (byte) ((data >> 40) & 0xFF);
        ret[6] = (byte) ((data >> 48) & 0xFF);
        ret[7] = (byte) ((data >> 56) & 0xFF);
        out.write(ret, 0, ret.length);
    }

}
