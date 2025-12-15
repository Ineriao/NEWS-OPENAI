package com.news.controller;

import com.news.common.Result;
import com.news.dto.CommentDTO;
import com.news.entity.Comment;
import com.news.service.CommentService;
import com.news.vo.CommentVO;
import com.news.vo.PageVO;
import com.news.vo.UserCommentVO;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论控制器
 */
@RestController
@RequestMapping("/api")
public class CommentController {

    @Autowired
    private CommentService commentService;

    // ==================== 公开接口 ====================

    /**
     * 获取新闻的评论列表
     * GET /api/public/comments/{newsId}
     */
    @GetMapping("/public/comments/{newsId}")
    public Result<List<CommentVO>> getComments(@PathVariable Long newsId) {
        List<CommentVO> comments = commentService.getCommentsByNewsId(newsId);
        return Result.success(comments);
    }

    /**
     * 获取新闻的评论数
     * GET /api/public/comments/{newsId}/count
     */
    @GetMapping("/public/comments/{newsId}/count")
    public Result<Long> getCommentCount(@PathVariable Long newsId) {
        Long count = commentService.countByNewsId(newsId);
        return Result.success(count);
    }

    // ==================== 需要登录的接口 ====================

    /**
     * 发表评论
     * POST /api/comments
     */
    @PostMapping("/comments")
    public Result<Comment> createComment(HttpServletRequest request,
                                         @RequestBody CommentDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized();
        }

        try {
            Comment comment = commentService.createComment(dto, userId);
            return Result.success("评论成功", comment);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除评论
     * DELETE /api/comments/{id}
     */
    @DeleteMapping("/comments/{id}")
    public Result<Void> deleteComment(HttpServletRequest request,
                                      @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        Integer role = (Integer) request.getAttribute("role");

        if (userId == null) {
            return Result.unauthorized();
        }

        try {
            commentService.deleteComment(id, userId, role);
            return Result.success("删除成功", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户的评论历史（分页）
     * GET /api/user/comments
     */
    @GetMapping("/user/comments")
    public Result<PageVO<UserCommentVO>> getUserComments(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized();
        }

        PageVO<UserCommentVO> result = commentService.getUserComments(userId, pageNum, pageSize);
        return Result.success(result);
    }
}
