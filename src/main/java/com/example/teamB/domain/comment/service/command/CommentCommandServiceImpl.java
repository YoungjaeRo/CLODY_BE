package com.example.teamB.domain.comment.service.command;

import com.example.teamB.domain.comment.converter.CommentConverter;
import com.example.teamB.domain.comment.dto.CommentCreateRequestDto;
import com.example.teamB.domain.comment.dto.CommentResponseDTO;
import com.example.teamB.domain.comment.dto.CommentUpdateRequestDto;
import com.example.teamB.domain.comment.entity.Comment;
import com.example.teamB.domain.comment.exception.CommentErrorCode;
import com.example.teamB.domain.comment.exception.CommentException;
import com.example.teamB.domain.comment.repository.CommentRepository;
import com.example.teamB.domain.member.entity.Member;
import com.example.teamB.domain.post.entity.Post;
import com.example.teamB.domain.post.exception.PostErrorCode;
import com.example.teamB.domain.post.exception.PostException;
import com.example.teamB.domain.post.respository.PostRepository;
import com.example.teamB.global.apiPayload.code.GeneralErrorCode;
import com.example.teamB.global.apiPayload.exception.CustomException;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentCommandServiceImpl implements CommentCommandService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final CommentConverter commentConverter;

    // 댓글 생성
    public CommentResponseDTO.CommentPreviewDTO createComment(CommentCreateRequestDto request, Member member, Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

        Comment parent = null;
        if (request.getParentId() != 0) {
            parent = commentRepository.findById(request.getParentId()).orElseThrow(() -> new CommentException(CommentErrorCode.COMMENT_NOT_FOUND));
        }

        Comment comment = new Comment(request.getContent(), member, post, parent);
        // 생성 후 저장
        Comment savedComment = commentRepository.save(comment);

        return commentConverter.toCommentPreviewDTO(savedComment, null);
    }

    // 댓글 수정
    public CommentResponseDTO.CommentPreviewDTO updateComment(Long commentId, CommentUpdateRequestDto request, Long memberId) {
        Comment comment = commentRepository.findByIdAndMemberId(commentId, memberId)
                .orElseThrow(() -> new CommentException(CommentErrorCode.COMMENT_NOT_FOUND));

        comment.updateContent(request.getContent());

        return commentConverter.toCommentPreviewDTO(comment, null);
    }

    // 댓글 삭제
    public void deleteComment(Long commentId, Long memberId) {
        Comment comment = commentRepository.findByIdAndMemberId(commentId, memberId)
                .orElseThrow(() -> new CommentException(CommentErrorCode.COMMENT_NOT_FOUND));

        commentRepository.delete(comment);
    }

    // 댓글 신고
    @Override
    public void reportComment(Long commentId, Long memberId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new CommentException(CommentErrorCode.COMMENT_NOT_FOUND));

        // 작성자가 본인의 댓글을 신고할 수 없도록 처리함
        if(comment.getMember().getId().equals(memberId)) {
            throw new CommentException(CommentErrorCode.REPORT_FORBIDDEN);
        }

        //신고 수 증가
        comment.increaseReportCount();
    }
}
