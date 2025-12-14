package com.news.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 热搜实体类
 * 对应数据库 hot_search 表
 */
@Data
@TableName("hot_search")
public class HotSearch {

    /** 热搜ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 热搜标题 */
    private String title;

    /** 热度值 */
    private String hot;

    /** 链接地址 */
    private String url;

    /** 来源平台 */
    private String source;

    /** 排名 */
    private Integer rankNum;

    /** 抓取时间 */
    private LocalDateTime fetchTime;
}
