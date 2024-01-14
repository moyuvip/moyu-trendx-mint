package com.moyu.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpUtil;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * OkHttp工具类
 *
 * @author: moyuvip666
 * @since: 2020/9/25 10:50 上午
 */
@Slf4j
public class OkHttpUtil {

    private static final String HTTP_JSON = "application/json; charset=utf-8";
    private static final String HTTP_FORM = "multipart/form-data; charset=utf-8";

    private static final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build();


    /**
     * get请求
     *
     * @param url
     * @param headers
     * @return
     */
    public static String httpGet(String url, Map<String, String> headers) {
        if (StrUtil.isBlank(url)) {
            log.error("url is null");
            throw new RuntimeException("URL 不能为空");
        }
        Request.Builder builder = new Request.Builder();
        if (!CollUtil.isEmpty(headers)) {
            headers.forEach(builder::header);
        }
        Request request = builder.get().url(url).build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response.code() != 200) {
                log.error("Http GET fail, url: {}, response: {}", url, response);
                throw new RuntimeException("Http GET fail");
            }
            return Objects.requireNonNull(response.body()).string();
        } catch (IOException e) {
            log.error("Http GET error, url: {}", url, e);
            throw new RuntimeException("Http GET error, url: " + url, e);
        }
    }

    /**
     * post
     *
     * @param url
     * @return
     */
    public static String httpGet(String url) {
        if (StrUtil.isBlank(url)) {
            log.error("url is null");
            throw new RuntimeException("URL 不能为空");
        }
        Request request = new Request.Builder().url(url).get().build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            String responseBody = Objects.requireNonNull(response.body()).string();
            if (response.code() != 200) {
                log.error("Http GET fail, url: {}, request: {}, response: {}, body: {}",
                        url, request, response, responseBody);
                throw new RuntimeException("Http POST fail");
            }
            log.info("Http GET success, url: {}, request: {}, response: {}, body: {}",
                    url, request, response, responseBody);
            return responseBody;
        } catch (IOException e) {
            log.error("Http GET error, url: {}", url, e);
            throw new RuntimeException("Http GET error, url: " + url, e);
        }
    }

    /**
     * post json数据
     *
     * @param url
     * @param headers
     * @param content
     * @return
     */
    public static String httpPostJson(String url, Map<String, String> headers, String content) {
        return httpPost(url, headers, content, HTTP_JSON);
    }

    /**
     * post表单
     *
     * @param url
     * @param headers
     * @param contentMap
     * @param files
     * @return
     */
    public static String httpPostForm(String url, Map<String, String> headers,
            Map<String, Object> contentMap, List<File> files) {
        if (StrUtil.isBlank(url)) {
            log.error("url is null");
            throw new RuntimeException("URL 不能为空");
        }
        Request.Builder requestBuilder = new Request.Builder().url(url);
        // 填充头信息
        if (!CollUtil.isEmpty(headers)) {
            headers.forEach(requestBuilder::addHeader);
        }
        MediaType mediaType = MediaType.parse("multipart/form-data");
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        // 填充content
        if (!CollUtil.isEmpty(contentMap)) {
            for (Map.Entry<String, Object> entry : contentMap.entrySet()) {
                if (entry.getValue() == null) {
                    continue;
                }
                builder.addFormDataPart(entry.getKey(), entry.getValue().toString());
            }
        }
        // 填充文件信息
        if (!CollUtil.isEmpty(files)) {
            for (File file : files) {
                builder.addFormDataPart("$filename", file.getName(), RequestBody.create(file, mediaType));
            }
        }
        Request request = requestBuilder.post(builder.build()).build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response.code() != 200) {
                log.error("Http POST form data fail, url: {}, request: {}, response: {}", url, request, response);
                throw new RuntimeException("Http POST fail");
            }
            log.info("Http POST form data success, url: {}, request: {}, response: {}", url, request, response);
            return Objects.requireNonNull(response.body()).string();
        } catch (IOException e) {
            log.error("Http POST form data error, url: {}", url, e);
            throw new RuntimeException("Http POST form data error, url: " + url, e);
        }
    }

    /**
     * post
     *
     * @param url
     * @param content
     * @param headers
     * @return
     */
    public static String httpPut(String url, Map<String, String> headers, String content) {
        if (StrUtil.isBlank(url)) {
            log.error("url is null");
            throw new RuntimeException("URL 不能为空");
        }
        MediaType mediaType = MediaType.parse(HTTP_JSON);
        if (content == null) {
            content = "";
        }
        RequestBody body = RequestBody.create(content, mediaType);
        Request.Builder requestBuilder = new Request.Builder().url(url);
        if (!CollUtil.isEmpty(headers)) {
            headers.forEach(requestBuilder::addHeader);
        }
        Request request = requestBuilder.put(body).build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            String responseBody = Objects.requireNonNull(response.body()).string();
            if (response.code() != 200) {
                log.error("Http POST fail, url: {}, request: {}, response: {}, body: {}",
                        url, request, response, responseBody);
                throw new RuntimeException("Http POST fail");
            }
            return responseBody;
        } catch (IOException e) {
            log.error("Http POST error, url: {}", url, e);
            throw new RuntimeException("Http POST error, url: " + url, e);
        }
    }

    /**
     * post
     *
     * @param url
     * @param content
     * @param headers
     * @return
     */
    public static String httpPost(String url, Map<String, String> headers, String content, String mediaTypeStr) {
        if (StrUtil.isBlank(url)) {
            log.error("url is null");
            throw new RuntimeException("URL 不能为空");
        }
        MediaType mediaType = MediaType.parse(mediaTypeStr);
        if (content == null) {
            content = "";
        }
        RequestBody body = RequestBody.create(content, mediaType);
        Request.Builder requestBuilder = new Request.Builder().url(url);
        if (!CollUtil.isEmpty(headers)) {
            headers.forEach(requestBuilder::addHeader);
        }
        Request request = requestBuilder.post(body).build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            String responseBody = Objects.requireNonNull(response.body()).string();
            if (response.code() != 200) {
                log.error("Http POST fail, url: {}, request: {}, response: {}, body: {}",
                        url, request, response, responseBody);
                throw new RuntimeException("Http POST fail");
            }
            return responseBody;
        } catch (IOException e) {
            log.error("Http POST error, url: {}", url, e);
            throw new RuntimeException("Http POST error, url: " + url, e);
        }
    }

    /**
     * 发送get请求(url带参)
     *
     * @param paramerMap 请求参数
     * @param url 请求url
     * @return
     */
    public static String get(Map<String, Object> paramerMap, String url) {
        String result = " ";
        try {
            log.debug("get请求url:{},请求参数:{}", url, paramerMap.toString());
            result = HttpUtil.get(url, paramerMap);
        } catch (HttpException e) {
            result = null;
            log.error("get请求异常：{}", e.toString());
        }
        return result;
    }
}


