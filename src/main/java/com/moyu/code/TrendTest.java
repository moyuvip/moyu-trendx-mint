package com.moyu.code;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.moyu.bean.PyTrendX;
import com.moyu.service.PyTrendXService;
import com.moyu.util.OkHttpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author : moyuvip666
 * @Since: 2024/1/10 12:38
 */
@Slf4j
@SpringBootTest
public class TrendTest {

    public static final String PRO_URL = "https://app.trendx.tech/v1/dao/public/projects/stars?sort=heat1&order=2&page=%d&rows=%d";
    public static final String NEWS_URL = "https://app.trendx.tech/v1/dao/public/projects/%s/articles?tags=&page=%d&rows=%d";
    public static final String LIKE_URL = "https://app.trendx.tech/v1/dao/users/projects/articles/attitudes";
    public static final String VOTE_URL = "https://app.trendx.tech/v1/dao/users/votes";
    public static final String VOTE_PARAM = "{\"projectId\":%s,\"attitudes\":1,\"author\":\"%s\",\"timestamp\":%s,\"signature\":\"%s\"}";
    public static String PRI_KEY = "";
    public static String COOKIE = "";
    public static String X_AUTHORIZATION = "";
    public static String AUTHOR = "";
    private static final String ATTR = "1";

    @Autowired
    PyTrendXService pyTrendXService;

    @Test
    public void proHandler() {
        HashMap<String, String> headerMap = getAuthHeader();
        int index = 0;
        int size = 100;
        int[] total = {11582};
        while (index * size <= total[0]) {
            ++index;
            doProHandler(index, size, total, headerMap);
        }
    }


    public void doProHandler(Integer idx, Integer sz, int[] totalArr, HashMap<String, String> headerMap) {
        log.info("=======================正在爬取第:{}页数据========================", idx);
        String proUrl = String.format(PRO_URL, idx, sz);
        String resJsonStr = null;
        try {
            resJsonStr = OkHttpUtil.httpGet(proUrl, headerMap);
        } catch (Exception e) {
            return;
        }
        JSONObject resJsonObj = JSONObject.parseObject(resJsonStr);
        JSONArray rows = resJsonObj.getJSONObject("data").getJSONArray("rows");
        Integer ttl = resJsonObj.getJSONObject("data").getInteger("total");
        totalArr[0] = ttl;
        if (ttl == 0 || rows.size() == 0) {
            return;
        }
        log.info("=======================开始组装数据========================");
        List<PyTrendX> trends = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            JSONObject data = rows.getJSONObject(i);
            int index = 0;
            int size = 100;
            int[] total = {12};
            while (index * size <= total[0]) {
                ++index;
                newsHandler(data, index, size, total, headerMap, trends);
            }
        }
        log.info("=======================组装数据完成========================");
        log.info("=======================开始插入========================");
        if (CollUtil.isNotEmpty(trends)) {
            List<String> uids = trends.stream().map(PyTrendX::getUid).collect(Collectors.toList());
            List<PyTrendX> xes = pyTrendXService.lambdaQuery().in(PyTrendX::getUid, uids).list();

            if (CollUtil.isNotEmpty(xes)) {
                List<String> dbUids = xes.stream().map(PyTrendX::getUid).collect(Collectors.toList());
                List<String> dbNids = xes.stream().map(PyTrendX::getNewsId).collect(Collectors.toList());
                trends = trends.stream().filter(v -> !dbUids.contains(v.getUid()) || (dbUids.contains(v.getUid()) && !dbNids.contains(v.getNewsId()))).collect(Collectors.toList());
            }

            if (CollUtil.isNotEmpty(trends)) {
                pyTrendXService.saveBatch(trends, trends.size());
                log.info("=======================插入完成,共:{}条========================", trends.size());
            }

        } else {
            log.info("=======================插入完成,共0条========================");
        }
    }

    public void newsHandler(JSONObject data, Integer index, Integer size, int[] totalArr,
                            HashMap<String, String> headerMap, List<PyTrendX> trends) {
        String newsJsonStr = null;
        try {
            String newsUrl = String.format(NEWS_URL, data.getString("uid"), index, size);
            newsJsonStr = OkHttpUtil.httpGet(newsUrl, headerMap);
        } catch (Exception e) {
            return;
        }
        long randomLong = RandomUtil.randomLong(0, 1000);
        try {
            Thread.sleep(randomLong);
        } catch (InterruptedException e) {
            return;
        }
        JSONObject newsJsonObj = JSONObject.parseObject(newsJsonStr);
        Integer total = newsJsonObj.getJSONObject("data").getInteger("total");
        totalArr[0] = total;
        JSONArray newsRows = newsJsonObj.getJSONObject("data").getJSONArray("rows");
        if (total == 0 || newsRows.size() == 0) {
            return;
        }

        for (int j = 0; j < newsRows.size(); j++) {
            JSONObject newsObj = newsRows.getJSONObject(j);
            PyTrendX build = PyTrendX.builder().uid(data.getString("uid")).fullName(data.getString("fullName"))
                    .newsId(newsObj.getString("id")).isLike(newsObj.getInteger("attitude")).build();
            trends.add(build);
        }
    }

    @Test
    public void likeHandler() {
        List<PyTrendX> list = pyTrendXService.lambdaQuery().isNull(PyTrendX::getIsLike).list();
        log.info("========================当前点赞条数：{}条=========================", list.size());
        for (int i = 0; i < list.size(); i++) {
            try {
                doLikeHandler(list.get(i));
                long randomLong = RandomUtil.randomLong(0, 1000);
                Thread.sleep(randomLong);
            } catch (Exception e) {
                continue;
            }


        }

    }

    public void doLikeHandler(PyTrendX pyTrendX) {
        Map<String, Object> param = new HashMap<>();
        param.put("articleId", Integer.parseInt(pyTrendX.getNewsId()));
        param.put("type", "likes");
        String paramJson = JSONUtil.toJsonStr(param);
        try {
            String res = OkHttpUtil.httpPut(LIKE_URL, getAuthHeader(), paramJson);
            if (Objects.equals(JSONObject.parseObject(res).getString("code"), "0")) {
                log.info("========================当前项目：{},点赞+1=========================", pyTrendX.getFullName());
                pyTrendX.setIsLike(1);
                pyTrendXService.updateById(pyTrendX);
            }
        } catch (Exception e) {

        }
    }


    public static HashMap<String, String> getAuthHeader() {

        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("Authority", "app.trendx.tech");
        headerMap.put("Accept", "application/json, text/plain, */*");
        headerMap.put("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,ee;q=0.7");
        headerMap.put("Content-Type", "application/json");
        headerMap.put("Cookie", COOKIE);
        headerMap.put("Origin", "https://app.trendx.tech");
        headerMap.put("Referer",
                "https://app.trendx.tech/projects/132c991c9072f9bd7377ab061c40afdf61266a45b900ec4a5de402ba241861a3");
        headerMap.put("Sec-Ch-Ua-Mobile", "?0");
        headerMap.put("Sec-Ch-Ua-Platform", "Windows");
        headerMap.put("Sec-Fetch-Dest", "empty");
        headerMap.put("Sec-Fetch-Mode", "cors");
        headerMap.put("Sec-Fetch-Site", "same-origin");
        headerMap.put("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        headerMap.put("X-Authorization", X_AUTHORIZATION);
        headerMap.put("X-Chain-Id", "56");

        return headerMap;
    }
}
