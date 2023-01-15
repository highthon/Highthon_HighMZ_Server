package io.munzil.munzil.domain.question.presentation.dto.response;

import io.munzil.munzil.domain.user.presentation.dto.response.AuthorResponse;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class QueryFeedQuestionDetailsResponse {

    private AuthorResponse author;

    private Long feedId;

    private String content;

    private List<String> imageUrl;

    private Boolean isLike;

    private LocalDateTime createdAt;

    private Long viewCount;

    private Integer likeCount;

}
