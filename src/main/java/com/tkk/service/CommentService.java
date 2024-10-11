package com.tkk.service;

import com.tkk.modal.Comment;

import java.util.List;

public interface CommentService {
    Comment createComment(Long issueId, Long userId, String content) throws Exception;

    void deleteComment(Long commentId, Long userId) throws Exception;

    List<Comment> findByIssueId(Long issueId) throws Exception;
}
