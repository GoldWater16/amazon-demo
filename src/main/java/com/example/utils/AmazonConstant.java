package com.example.utils;

public interface AmazonConstant {

    /***post请求地址*/
    String POST_URL = "https://mws.amazonservices.com/Orders/2013-09-01";

    /***post请求后缀*/
    String SUFFIX_POST_URL = "/Orders/2013-09-01";
    /***订单api版本*/
    String VERSION = "2013-09-01";
    /****请求的Amazon方法*/
    String ACTION = "ListOrders";
    /***签名方式*/
    String ALGORITHM = "HmacSHA256";

    /****时间转换格式*/
    String TIME_FORMAT_STR = "yyyy'-'MM'-'dd'T'HH':'mm':'ss'Z'";

    /****编码格式*/
    String CHARACTER_ENCODING = "UTF-8";

    String SIGNATURE_VERSION ="2";
    /***订单中的所有商品均已发货*/
    String SHIPPED ="Shipped";
}
