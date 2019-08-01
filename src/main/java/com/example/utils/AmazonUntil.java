package com.example.utils;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class AmazonUntil implements AmazonConstant {

    private static final Logger logger = LoggerFactory.getLogger(AmazonUntil.class);

    /***
     * post请求通道
     * @param secretKey
     * @param parameters
     * @return
     * @throws Exception
     */
    public static JSONArray doPost(String url, HashMap<String, String> parameters, String secretKey) throws Exception {
        String str = DateTime.now(DateTimeZone.UTC).toString(TIME_FORMAT_STR);
        /**++++++++++++++++++++++++++++++获取当天前00:00:00分-23:59:59的信息 查找过去一天的时间段的数据信息++++++++++++++++++++++++++++++*/
        // long zero = LocalDateTime.of(LocalDate.now(), LocalTime.of(8,0)).toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
        //String after =new DateTime(zero-1000*3600*24, DateTimeZone.UTC).toString(TIME_FORMAT_STR);
        //String before =new DateTime(zero, DateTimeZone.UTC).toString(TIME_FORMAT_STR);
        /**++++++++++++++++++++++++++++++++++++以上获取过去整点一天的订单数据信息++++++++++++++++++++++++++++++++++++++++++*/

        String after =new DateTime(System.currentTimeMillis()-1000*3600*24*3, DateTimeZone.UTC).toString(TIME_FORMAT_STR);
        //最后更新时间在5分钟之前的数据，如果该值不设定,默认是提交2分钟以前
        String before =new DateTime(System.currentTimeMillis()-5*60*1000, DateTimeZone.UTC).toString(TIME_FORMAT_STR);

        parameters.put("Timestamp", urlEncode(str));
        parameters.put("Action", AmazonUntil.urlEncode(ACTION));
        parameters.put("SignatureMethod", AmazonUntil.urlEncode(ALGORITHM));
        parameters.put("Version", AmazonUntil.urlEncode(VERSION));
        parameters.put("SignatureVersion", AmazonUntil.urlEncode(SIGNATURE_VERSION));
        parameters.put("OrderStatus.Status.1", SHIPPED);
        parameters.put("LastUpdatedAfter", AmazonUntil.urlEncode(after));
        parameters.put("LastUpdatedBefore", AmazonUntil.urlEncode(before));
        String formattedParameters = calculateStringToSignV2(parameters, POST_URL);
        logger.info("签名内容：{}", formattedParameters);
        String signature = sign(formattedParameters, secretKey);
        logger.info("签名signature: {}", signature);
        parameters.put("Signature", urlEncode(signature));
        parameters.put("Timestamp", str);
        String paramStr = sortParams(new StringBuilder(), parameters);
        logger.info("排序后参数：{}", paramStr);
        return doPost(url, paramStr);

    }


    /**
     * signV2签名内容
     *
     * @param parameters
     * @param serviceUrl
     * @return
     * @throws SignatureException
     * @throws URISyntaxException
     */
    private static String calculateStringToSignV2(
            Map<String, String> parameters, String serviceUrl)
            throws  URISyntaxException {
        URI endpoint = new URI(serviceUrl.toLowerCase());
        StringBuilder data = new StringBuilder();
        data.append("POST\n");
        data.append(endpoint.getHost());
        data.append("\n/Orders/2013-09-01");
        data.append("\n");
        return sortParams(data, parameters);

    }

    /***
     * signV2签名方式
     * @param data
     * @param secretKey
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws IllegalStateException
     * @throws UnsupportedEncodingException
     */
    private static String sign(String data, String secretKey)
            throws NoSuchAlgorithmException, InvalidKeyException,
            IllegalStateException, UnsupportedEncodingException {
        Mac mac = Mac.getInstance(ALGORITHM);
        mac.init(new SecretKeySpec(secretKey.getBytes(CHARACTER_ENCODING),
                ALGORITHM));
        byte[] signature = mac.doFinal(data.getBytes(CHARACTER_ENCODING));
        String signatureBase64 = new String(Base64.encodeBase64(signature),
                CHARACTER_ENCODING);
        return new String(signatureBase64);
    }

    /**
     * url非法字符转换
     *
     * @param rawValue
     * @return
     */
    public static String urlEncode(String rawValue) {
        String value = (rawValue == null) ? "" : rawValue;
        String encoded = null;

        try {
            encoded = URLEncoder.encode(value, CHARACTER_ENCODING)
                    .replace("+", "%20")
                    .replace("*", "%2A")
                    .replace("%7E", "~");
        } catch (UnsupportedEncodingException e) {
            System.err.println("Unknown encoding: " + CHARACTER_ENCODING);
        }

        return encoded;
    }

    /***
     * post请求
     * @param url
     * @param params
     * @return
     * @throws Exception
     */
    public static JSONArray doPost(String url, String params) throws Exception {

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        httpPost.setHeader("Accept", "Accept: text/plain, */*");
        httpPost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3724.8 Safari/537.36");
        httpPost.addHeader("x-amazon-user-agent", "AmazonJavascriptScratchpad/1.0 (Language=Javascript)");
        httpPost.addHeader("X-Requested-With", "XMLHttpRequest");
        String charSet = "UTF-8";
        StringEntity entity = new StringEntity(params, charSet);
        httpPost.setEntity(entity);
        CloseableHttpResponse response = null;

        try {

            response = httpclient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();
            InputStream is = responseEntity.getContent();
            return parseXML(is);
        } catch (Exception e) {
            throw e;
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    throw e;
                }
            }
            try {
                httpclient.close();
            } catch (IOException e) {
                throw e;
            }
        }
    }

    /**
     * 对传递参数转换
     *
     * @param data
     * @param parameters
     * @return
     */
    private static String sortParams(StringBuilder data, Map<String, String> parameters) {
        Map<String, String> sorted = new TreeMap<String, String>();
        sorted.putAll(parameters);

        Iterator<Map.Entry<String, String>> pairs =
                sorted.entrySet().iterator();
        while (pairs.hasNext()) {
            Map.Entry<String, String> pair = pairs.next();
            if (pair.getValue() != null) {
                data.append(pair.getKey() + "=" + pair.getValue());
            } else {
                data.append(pair.getKey() + "=");
            }
            if (pairs.hasNext()) {
                data.append("&");
            }
        }
        return data.toString();
    }

    private static JSONArray parseXML(InputStream is) {

        JSON xml = new XMLSerializer().readFromStream(is);
        JSONObject jsonObject = JSONObject.fromObject(xml);
        JSONArray orders = new JSONArray();
        if (null != jsonObject.get("ListOrdersResult")) {
            JSONObject listOrdersResult = (JSONObject) jsonObject.get("ListOrdersResult");
            if (listOrdersResult != null) {
                /***获取到所有的订单内容 list*/
                JSONObject orderLists =null;
                try {
                    orderLists=(JSONObject) listOrdersResult.get("Orders");
                }catch (Exception e){

                }
                if (orderLists != null) {
                    if (orderLists.size() == 1) {
                        orders.add(orderLists.get("Order"));
                    } else {
                        /***获取一个订单批次的所有订单内容*/
                        orders = (JSONArray) orderLists.get("Order");

                    }
                }
            }
        }
        return orders;
    }

}
