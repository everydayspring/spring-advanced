package org.example.expert.domain.comment.service;

import org.example.expert.domain.comment.repository.CommentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CommentAdminServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentAdminService commentAdminService;

    @Test
    void 댓글삭제_정상동작() {
        // when
        commentAdminService.deleteComment(anyLong());

        // then
        verify(commentRepository, times(1)).deleteById(anyLong());
    }

}
