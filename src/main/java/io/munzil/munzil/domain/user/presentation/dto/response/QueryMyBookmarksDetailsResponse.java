package io.munzil.munzil.domain.user.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class QueryMyBookmarksDetailsResponse {

    private Long feedId;

    private String content;

    private List<String> imageUrl;

    private Boolean isLike;

    private LocalDateTime createdAt;

}
