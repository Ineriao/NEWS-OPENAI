package com.news.vo;

import lombok.Data;
import java.util.List;

/**
 * 分页结果 VO
 */
@Data
public class PageVO<T> {

    /** 当前页数据 */
    private List<T> list;

    /** 总记录数 */
    private Long total;

    /** 总页数 */
    private Long pages;

    /** 当前页码 */
    private Long current;

    /** 每页条数 */
    private Long size;

    /**
     * 构造分页结果
     */
    public static <T> PageVO<T> of(List<T> list, Long total, Long pages, Long current, Long size) {
        PageVO<T> pageVO = new PageVO<>();
        pageVO.setList(list);
        pageVO.setTotal(total);
        pageVO.setPages(pages);
        pageVO.setCurrent(current);
        pageVO.setSize(size);
        return pageVO;
    }
}
