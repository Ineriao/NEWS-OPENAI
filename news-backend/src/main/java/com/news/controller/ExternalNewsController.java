package com.news.controller;

import com.news.common.Result;
import com.news.service.ExternalNewsService;
import com.news.vo.ExternalNewsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 外部新闻控制器
 */
@RestController
@RequestMapping("/api/public/external-news")
public class ExternalNewsController {

    @Autowired
    private ExternalNewsService externalNewsService;

    /**
     * 获取36氪新闻
     */
    @GetMapping("/36kr")
    public Result<List<ExternalNewsVO>> get36krNews() {
        List<ExternalNewsVO> list = externalNewsService.get36krNews();
        return Result.success(list);
    }

    /**
     * 获取IT之家新闻
     */
    @GetMapping("/ithome")
    public Result<List<ExternalNewsVO>> getIthomeNews() {
        List<ExternalNewsVO> list = externalNewsService.getIthomeNews();
        return Result.success(list);
    }

    /**
     * 获取澎湃新闻
     */
    @GetMapping("/thepaper")
    public Result<List<ExternalNewsVO>> getThepaperNews() {
        List<ExternalNewsVO> list = externalNewsService.getThepaperNews();
        return Result.success(list);
    }

    /**
     * 获取今日头条新闻
     */
    @GetMapping("/toutiao")
    public Result<List<ExternalNewsVO>> getToutiaoNews() {
        List<ExternalNewsVO> list = externalNewsService.getToutiaoNews();
        return Result.success(list);
    }

    /**
     * 获取所有来源的新闻
     */
    @GetMapping("/all")
    public Result<Map<String, List<ExternalNewsVO>>> getAllNews() {
        Map<String, List<ExternalNewsVO>> result = new HashMap<>();
        result.put("36kr", externalNewsService.get36krNews());
        result.put("ithome", externalNewsService.getIthomeNews());
        result.put("thepaper", externalNewsService.getThepaperNews());
        result.put("toutiao", externalNewsService.getToutiaoNews());
        return Result.success(result);
    }

    /**
     * 刷新并保存所有新闻到数据库
     */
    @PostMapping("/refresh")
    public Result<Void> refreshAndSave() {
        externalNewsService.refreshAndSaveAll();
        return Result.success(null);
    }

    /**
     * 从数据库获取指定来源的新闻
     */
    @GetMapping("/db/{source}")
    public Result<List<ExternalNewsVO>> getFromDb(
            @PathVariable String source,
            @RequestParam(defaultValue = "10") int limit) {
        List<ExternalNewsVO> list = externalNewsService.getFromDb(source, limit);
        return Result.success(list);
    }

    /**
     * 获取最新新闻（混合展示，用于首页）
     */
    @GetMapping("/latest")
    public Result<List<ExternalNewsVO>> getLatestNews(
            @RequestParam(defaultValue = "20") int limit) {
        List<ExternalNewsVO> list = externalNewsService.getLatestNews(limit);
        return Result.success(list);
    }
}
