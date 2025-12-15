package com.news.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.news.common.CacheConstants;
import com.news.dao.HotSearchMapper;
import com.news.entity.HotSearch;
import com.news.service.HotSearchService;
import com.news.vo.HotSearchVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 热搜服务实现类
 * 使用本地 DailyHotApi 获取各平台热搜
 */
@Service
public class HotSearchServiceImpl implements HotSearchService {

    private static final Logger log = LoggerFactory.getLogger(HotSearchServiceImpl.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private HotSearchMapper hotSearchMapper;

    // DailyHotApi 本地服务地址
    private static final String API_BASE = "http://localhost:6688";

    @Override
    public List<HotSearchVO> getWeiboHot() {
        return fetchHotList("/weibo", "微博");
    }

    @Override
    public List<HotSearchVO> getDouyinHot() {
        return fetchHotList("/douyin", "抖音");
    }

    @Override
    public List<HotSearchVO> getZhihuHot() {
        return fetchHotList("/zhihu", "知乎");
    }

    @Override
    public List<HotSearchVO> getBaiduHot() {
        return fetchHotList("/baidu", "百度");
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.CACHE_HOT_SEARCH, allEntries = true)
    public void refreshAndSaveAll() {
        saveHotList(getWeiboHot(), "微博");
        saveHotList(getDouyinHot(), "抖音");
        saveHotList(getZhihuHot(), "知乎");
        saveHotList(getBaiduHot(), "百度");
        log.info("热搜数据刷新完成");
    }

    @Override
    @Cacheable(value = CacheConstants.CACHE_HOT_SEARCH, key = "#source")
    public List<HotSearchVO> getFromDb(String source) {
        LambdaQueryWrapper<HotSearch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HotSearch::getSource, source)
               .orderByDesc(HotSearch::getFetchTime)
               .orderByAsc(HotSearch::getRankNum)
               .last("LIMIT 10");

        List<HotSearch> list = hotSearchMapper.selectList(wrapper);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<HotSearchVO> getHistory(String source, int page, int size) {
        LambdaQueryWrapper<HotSearch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HotSearch::getSource, source)
               .orderByDesc(HotSearch::getFetchTime)
               .orderByAsc(HotSearch::getRankNum)
               .last("LIMIT " + size + " OFFSET " + (page - 1) * size);

        List<HotSearch> list = hotSearchMapper.selectList(wrapper);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    /**
     * 保存热搜列表到数据库（只保存前10条，保留历史记录）
     */
    private void saveHotList(List<HotSearchVO> voList, String source) {
        if (voList == null || voList.isEmpty()) {
            return;
        }

        // 只取前10条保存
        List<HotSearchVO> top10 = voList.stream().limit(10).collect(Collectors.toList());

        // 插入新数据（保留历史记录，不删除旧数据）
        LocalDateTime now = LocalDateTime.now();
        for (HotSearchVO vo : top10) {
            HotSearch entity = new HotSearch();
            entity.setTitle(vo.getTitle());
            entity.setHot(vo.getHot());
            entity.setUrl(vo.getUrl());
            entity.setSource(source);
            entity.setRankNum(vo.getRank());
            entity.setFetchTime(now);
            hotSearchMapper.insert(entity);
        }
        log.info("保存{}热搜前10条", source);
    }

    /**
     * 实体转VO
     */
    private HotSearchVO convertToVO(HotSearch entity) {
        HotSearchVO vo = new HotSearchVO();
        vo.setRank(entity.getRankNum());
        vo.setTitle(entity.getTitle());
        vo.setHot(entity.getHot());
        vo.setUrl(entity.getUrl());
        vo.setSource(entity.getSource());
        return vo;
    }

    /**
     * 通用热搜获取方法（适配 DailyHotApi 响应格式）
     */
    private List<HotSearchVO> fetchHotList(String endpoint, String source) {
        List<HotSearchVO> result = new ArrayList<>();

        try {
            String url = API_BASE + endpoint;
            String response = restTemplate.getForObject(url, String.class);

            JsonNode root = objectMapper.readTree(response);

            // DailyHotApi 使用 code=200 表示成功
            if (root.has("code") && root.get("code").asInt() == 200) {
                JsonNode dataArray = root.get("data");

                if (dataArray != null && dataArray.isArray()) {
                    int rank = 1;
                    for (JsonNode item : dataArray) {
                        if (rank > 20) break; // 只取前20条

                        HotSearchVO vo = new HotSearchVO();
                        vo.setRank(rank++);
                        vo.setTitle(getTextValue(item, "title"));
                        vo.setHot(getTextValue(item, "hot"));
                        vo.setUrl(getTextValue(item, "url"));
                        vo.setSource(source);

                        result.add(vo);
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取{}热搜失败: {}", source, e.getMessage());
        }

        return result;
    }

    /**
     * 安全获取 JSON 文本值
     */
    private String getTextValue(JsonNode node, String field) {
        if (node.has(field) && !node.get(field).isNull()) {
            return node.get(field).asText();
        }
        return "";
    }
}
