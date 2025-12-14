package com.news.controller;

import com.news.common.Result;
import com.news.service.HotSearchService;
import com.news.vo.HotSearchVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 热搜控制器
 */
@RestController
@RequestMapping("/api/public/hot")
public class HotSearchController {

    @Autowired
    private HotSearchService hotSearchService;

    /**
     * 获取微博热搜
     */
    @GetMapping("/weibo")
    public Result<List<HotSearchVO>> getWeiboHot() {
        List<HotSearchVO> list = hotSearchService.getWeiboHot();
        return Result.success(list);
    }

    /**
     * 获取抖音热搜
     */
    @GetMapping("/douyin")
    public Result<List<HotSearchVO>> getDouyinHot() {
        List<HotSearchVO> list = hotSearchService.getDouyinHot();
        return Result.success(list);
    }

    /**
     * 获取知乎热搜
     */
    @GetMapping("/zhihu")
    public Result<List<HotSearchVO>> getZhihuHot() {
        List<HotSearchVO> list = hotSearchService.getZhihuHot();
        return Result.success(list);
    }

    /**
     * 获取百度热搜
     */
    @GetMapping("/baidu")
    public Result<List<HotSearchVO>> getBaiduHot() {
        List<HotSearchVO> list = hotSearchService.getBaiduHot();
        return Result.success(list);
    }

    /**
     * 获取所有平台热搜
     */
    @GetMapping("/all")
    public Result<Map<String, List<HotSearchVO>>> getAllHot() {
        Map<String, List<HotSearchVO>> result = new HashMap<>();
        result.put("weibo", hotSearchService.getWeiboHot());
        result.put("douyin", hotSearchService.getDouyinHot());
        result.put("zhihu", hotSearchService.getZhihuHot());
        result.put("baidu", hotSearchService.getBaiduHot());
        return Result.success(result);
    }

    /**
     * 刷新并保存所有热搜到数据库
     */
    @PostMapping("/refresh")
    public Result<Void> refreshAndSave() {
        hotSearchService.refreshAndSaveAll();
        return Result.success(null);
    }

    /**
     * 从数据库获取指定平台热搜
     */
    @GetMapping("/db/{source}")
    public Result<List<HotSearchVO>> getFromDb(@PathVariable String source) {
        List<HotSearchVO> list = hotSearchService.getFromDb(source);
        return Result.success(list);
    }

    /**
     * 获取历史热搜记录（分页）
     */
    @GetMapping("/history/{source}")
    public Result<List<HotSearchVO>> getHistory(
            @PathVariable String source,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<HotSearchVO> list = hotSearchService.getHistory(source, page, size);
        return Result.success(list);
    }
}
