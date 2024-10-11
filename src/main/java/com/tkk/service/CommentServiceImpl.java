package com.tkk.service;

import com.tkk.modal.Comment;
import com.tkk.modal.Issue;
import com.tkk.modal.User;
import com.tkk.repository.CommentRepository;
import com.tkk.repository.IssueRepository;
import com.tkk.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Comment createComment(Long issueId, Long userId, String content) throws Exception {
        Optional<Issue> issueOptional = issueRepository.findById(issueId);
        Optional<User> userOptional = userRepository.findById(userId);
        if (issueOptional.isEmpty()) {
            throw new Exception("Issue is not found with id " + issueId);
        }
        if (userOptional.isEmpty()) {
            throw new Exception("User is not found with id " + userId);
        }
        Issue issue = issueOptional.get();
        User user = userOptional.get();

        Comment comment = new Comment();
        comment.setIssue(issue);
        comment.setUser(user);
        comment.setCreatedDateTime(LocalDateTime.now());
        comment.setContent(content);

        Comment savedComment = commentRepository.save(comment);

        issue.getComments().add(savedComment);

        return savedComment;
    }

    @Override
    public void deleteComment(Long commentId, Long userId) throws Exception {
        Optional<Comment> commentOptional = commentRepository.findById(commentId);
        Optional<User> userOptional = userRepository.findById(userId);

        if (commentOptional.isEmpty()) {
            throw new Exception("Comment is not found with id " + commentId);
        }
        if (userOptional.isEmpty()) {
            throw new Exception("User is not found with id " + userId);
        }
        Comment comment = commentOptional.get();
        User user = userOptional.get();

        if (comment.getUser().equals(user)) {
            commentRepository.delete(comment);
        } else {
            throw new Exception("User does not have permission to delete this comment!");
        }
    }

    @Override
    public List<Comment> findByIssueId(Long issueId) throws Exception {
        return commentRepository.findByIssueId(issueId);
    }
}
