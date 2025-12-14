# 热搜 API 接入文档

## 1. 概述

本项目使用 [DailyHotApi](https://github.com/imsyy/DailyHotApi) 作为热搜数据源，支持微博、抖音、知乎、百度等多平台热搜数据的获取和存储。

## 2. DailyHotApi 服务部署

### 2.1 克隆项目
```bash
git clone https://github.com/imsyy/DailyHotApi.git
cd DailyHotApi
```

### 2.2 安装依赖
```bash
npm install
```

### 2.3 构建项目
```bash
npm run build
```

### 2.4 启动服务
```bash
npm run start
```

服务默认运行在 `http://localhost:6688`

## 3. API 接口说明

### 3.1 DailyHotApi 接口

| 平台 | 接口地址 | 说明 |
|------|---------|------|
| 微博 | GET /weibo | 微博热搜榜 |
| 抖音 | GET /douyin | 抖音热搜榜 |
| 知乎 | GET /zhihu | 知乎热榜 |
| 百度 | GET /baidu | 百度热搜 |
| 全部 | GET /all | 所有支持的平台 |

**响应格式**:
```json
{
  "code": 200,
  "name": "weibo",
  "title": "微博",
  "type": "热搜榜",
  "data": [
    {
      "id": "xxx",
      "title": "热搜标题",
      "url": "https://...",
      "hot": "热度值"
    }
  ]
}
```

### 3.2 后端热搜接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/public/hot/weibo | GET | 获取微博热搜 |
| /api/public/hot/douyin | GET | 获取抖音热搜 |
| /api/public/hot/zhihu | GET | 获取知乎热搜 |
| /api/public/hot/baidu | GET | 获取百度热搜 |
| /api/public/hot/all | GET | 获取所有平台热搜 |
| /api/public/hot/refresh | POST | 刷新并保存热搜（每平台前10条） |
| /api/public/hot/db/{source} | GET | 从数据库获取最新热搜 |
| /api/public/hot/history/{source}?page=1&size=10 | GET | 获取历史热搜记录（分页） |

## 4. 数据库表结构

```sql
CREATE TABLE IF NOT EXISTS `hot_search` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '热搜ID',
    `title` VARCHAR(255) NOT NULL COMMENT '热搜标题',
    `hot` VARCHAR(50) COMMENT '热度值',
    `url` VARCHAR(500) COMMENT '链接地址',
    `source` VARCHAR(20) NOT NULL COMMENT '来源平台',
    `rank_num` INT NOT NULL COMMENT '排名',
    `fetch_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '抓取时间',
    INDEX `idx_source` (`source`),
    INDEX `idx_fetch_time` (`fetch_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='热搜表';
```

## 5. 相关文件

- `HotSearchService.java` - 热搜服务接口
- `HotSearchServiceImpl.java` - 热搜服务实现
- `HotSearchController.java` - 热搜控制器
- `HotSearch.java` - 热搜实体类
- `HotSearchMapper.java` - 热搜 Mapper
- `HotSearchVO.java` - 热搜视图对象

## 6. 使用示例

### 6.1 获取微博热搜
```bash
curl http://localhost:8080/api/public/hot/weibo
```

### 6.2 刷新并保存所有热搜
```bash
curl -X POST http://localhost:8080/api/public/hot/refresh
```

### 6.3 从数据库获取热搜
```bash
curl http://localhost:8080/api/public/hot/db/微博
```

### 6.4 获取历史热搜记录
```bash
curl "http://localhost:8080/api/public/hot/history/微博?page=1&size=10"
```

---

## 7. 后端接入详细步骤

### 7.1 创建实体类

**entity/HotSearch.java**
```java
@Data
@TableName("hot_search")
public class HotSearch {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String hot;
    private String url;
    private String source;
    private Integer rankNum;
    private LocalDateTime fetchTime;
}
```

### 7.2 创建 VO 类

**vo/HotSearchVO.java**
```java
@Data
public class HotSearchVO {
    private Integer rank;
    private String title;
    private String hot;
    private String url;
    private String source;
}
```

### 7.3 创建 Mapper

**dao/HotSearchMapper.java**
```java
@Mapper
public interface HotSearchMapper extends BaseMapper<HotSearch> {
}
```

### 7.4 Service 核心实现

```java
@Service
public class HotSearchServiceImpl implements HotSearchService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String API_BASE = "http://localhost:6688";

    /**
     * 通用热搜获取方法（适配 DailyHotApi 响应格式）
     */
    private List<HotSearchVO> fetchHotList(String endpoint, String source) {
        List<HotSearchVO> result = new ArrayList<>();
        try {
            String response = restTemplate.getForObject(API_BASE + endpoint, String.class);
            JsonNode root = objectMapper.readTree(response);

            if (root.get("code").asInt() == 200) {
                JsonNode dataArray = root.get("data");
                int rank = 1;
                for (JsonNode item : dataArray) {
                    if (rank > 20) break;
                    HotSearchVO vo = new HotSearchVO();
                    vo.setRank(rank++);
                    vo.setTitle(item.get("title").asText());
                    vo.setHot(item.has("hot") ? item.get("hot").asText() : "");
                    vo.setUrl(item.get("url").asText());
                    vo.setSource(source);
                    result.add(vo);
                }
            }
        } catch (Exception e) {
            log.error("获取{}热搜失败: {}", source, e.getMessage());
        }
        return result;
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
     * 获取历史热搜记录（分页）
     */
    public List<HotSearchVO> getHistory(String source, int page, int size) {
        LambdaQueryWrapper<HotSearch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HotSearch::getSource, source)
               .orderByDesc(HotSearch::getFetchTime)
               .orderByAsc(HotSearch::getRankNum)
               .last("LIMIT " + size + " OFFSET " + (page - 1) * size);
        
        return hotSearchMapper.selectList(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
}
```

---

## 8. 技术要点

### 8.1 RestTemplate 调用外部 API
```java
RestTemplate restTemplate = new RestTemplate();
String response = restTemplate.getForObject(url, String.class);
```

### 8.2 Jackson 解析 JSON
```java
ObjectMapper objectMapper = new ObjectMapper();
JsonNode root = objectMapper.readTree(response);
JsonNode dataArray = root.get("data");
for (JsonNode item : dataArray) {
    String title = item.get("title").asText();
}
```

### 8.3 数据存储策略

**当前策略**：只保存前10条，保留历史记录
```java
// 只取前10条保存
List<HotSearchVO> top10 = voList.stream().limit(10).collect(Collectors.toList());

// 插入新数据（保留历史记录，不删除旧数据）
for (HotSearchVO vo : top10) {
    hotSearchMapper.insert(entity);
}
```

**优点**：
- 支持历史趋势分析
- 可追溯热搜变化
- 数据量可控（每次只存10条）

### 8.4 事务管理
使用 @Transactional 保证原子性：
```java
@Transactional
public void refreshAndSaveAll() {
    saveHotList(getWeiboHot(), "微博");
    saveHotList(getDouyinHot(), "抖音");
    saveHotList(getZhihuHot(), "知乎");
    saveHotList(getBaiduHot(), "百度");
}
```

---

## 9. 故障排查

| 问题 | 原因 | 解决方案 |
|------|------|----------|
| 热搜数据为空 | DailyHotApi 未启动 | curl http://localhost:6688/weibo |
| 表不存在 | 未建表 | 执行第4节 SQL |
| 连接超时 | 端口被占用 | netstat -ano \| findstr :6688 |
