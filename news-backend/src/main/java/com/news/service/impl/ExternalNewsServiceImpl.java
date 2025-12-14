package com.news.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.news.dao.ExternalNewsMapper;
import com.news.entity.ExternalNews;
import com.news.service.ExternalNewsService;
import com.news.vo.ExternalNewsVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 外部新闻服务实现类
 */
@Service
public class ExternalNewsServiceImpl implements ExternalNewsService {

    private static final Logger log = LoggerFactory.getLogger(ExternalNewsServiceImpl.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ExternalNewsMapper externalNewsMapper;

    private static final String API_BASE = "http://localhost:6688";

    @Override
    public List<ExternalNewsVO> get36krNews() {
        return fetchNewsList("/36kr", "36氪");
    }

    @Override
    public List<ExternalNewsVO> getIthomeNews() {
        return fetchNewsList("/ithome", "IT之家");
    }

    @Override
    public List<ExternalNewsVO> getThepaperNews() {
        return fetchNewsList("/thepaper", "澎湃新闻");
    }

    @Override
    public List<ExternalNewsVO> getToutiaoNews() {
        return fetchNewsList("/toutiao", "今日头条");
    }

    @Override
    @Transactional
    public void refreshAndSaveAll() {
        saveNewsList(get36krNews(), "36氪");
        saveNewsList(getIthomeNews(), "IT之家");
        saveNewsList(getThepaperNews(), "澎湃新闻");
        saveNewsList(getToutiaoNews(), "今日头条");
        log.info("外部新闻数据刷新完成");
    }

    @Override
    public List<ExternalNewsVO> getFromDb(String source, int limit) {
        LambdaQueryWrapper<ExternalNews> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExternalNews::getSource, source)
               .orderByDesc(ExternalNews::getFetchTime)
               .last("LIMIT " + limit);

        List<ExternalNews> list = externalNewsMapper.selectList(wrapper);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<ExternalNewsVO> getLatestNews(int limit) {
        LambdaQueryWrapper<ExternalNews> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ExternalNews::getFetchTime)
               .last("LIMIT " + limit);

        List<ExternalNews> list = externalNewsMapper.selectList(wrapper);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    /**
     * 保存新闻列表到数据库（只保存前10条，保留历史记录）
     */
    private void saveNewsList(List<ExternalNewsVO> voList, String source) {
        if (voList == null || voList.isEmpty()) {
            return;
        }

        List<ExternalNewsVO> top10 = voList.stream().limit(10).collect(Collectors.toList());

        LocalDateTime now = LocalDateTime.now();
        for (ExternalNewsVO vo : top10) {
            ExternalNews entity = new ExternalNews();
            entity.setTitle(vo.getTitle());
            entity.setDescription(vo.getDescription());
            entity.setUrl(vo.getUrl());
            entity.setCoverImage(vo.getCoverImage());
            entity.setSource(source);
            entity.setHot(vo.getHot());
            entity.setFetchTime(now);
            externalNewsMapper.insert(entity);
        }
        log.info("保存{}新闻前10条", source);
    }

    /**
     * 实体转VO
     */
    private ExternalNewsVO convertToVO(ExternalNews entity) {
        ExternalNewsVO vo = new ExternalNewsVO();
        vo.setTitle(entity.getTitle());
        vo.setDescription(entity.getDescription());
        vo.setUrl(entity.getUrl());
        vo.setCoverImage(entity.getCoverImage());
        vo.setSource(entity.getSource());
        vo.setHot(entity.getHot());
        if (entity.getFetchTime() != null) {
            vo.setFetchTime(entity.getFetchTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        return vo;
    }

    /**
     * 通用新闻获取方法
     */
    private List<ExternalNewsVO> fetchNewsList(String endpoint, String source) {
        List<ExternalNewsVO> result = new ArrayList<>();

        try {
            String url = API_BASE + endpoint;
            String response = restTemplate.getForObject(url, String.class);

            JsonNode root = objectMapper.readTree(response);

            if (root.has("code") && root.get("code").asInt() == 200) {
                JsonNode dataArray = root.get("data");

                if (dataArray != null && dataArray.isArray()) {
                    for (JsonNode item : dataArray) {
                        if (result.size() >= 20) break;

                        ExternalNewsVO vo = new ExternalNewsVO();
                        vo.setTitle(getTextValue(item, "title"));
                        vo.setDescription(getTextValue(item, "desc"));
                        vo.setUrl(getTextValue(item, "url"));
                        vo.setCoverImage(getTextValue(item, "cover"));
                        vo.setHot(getTextValue(item, "hot"));
                        vo.setSource(source);

                        result.add(vo);
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取{}新闻失败: {}", source, e.getMessage());
        }

        return result;
    }

    private String getTextValue(JsonNode node, String field) {
        if (node.has(field) && !node.get(field).isNull()) {
            return node.get(field).asText();
        }
        return "";
    }
}
