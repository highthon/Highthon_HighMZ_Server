package io.munzil.munzil.domain.feed.service;

import io.munzil.munzil.domain.feed.domain.Feed;
import io.munzil.munzil.domain.feed.domain.FeedImage;
import io.munzil.munzil.domain.feed.domain.repository.FeedImageRepository;
import io.munzil.munzil.domain.feed.domain.repository.FeedRepository;
import io.munzil.munzil.domain.feed.domain.repository.vo.FeedDetailsVO;
import io.munzil.munzil.domain.feed.exception.FeedNotFoundException;
import io.munzil.munzil.domain.feed.presentation.dto.response.QueryFeedDetailsResponse;
import io.munzil.munzil.domain.question.domain.repository.vo.QuestionVO;
import io.munzil.munzil.domain.question.presentation.dto.response.QuestionResponse;
import io.munzil.munzil.domain.user.domain.User;
import io.munzil.munzil.domain.user.domain.repository.vo.AuthorVO;
import io.munzil.munzil.domain.user.facade.UserFacade;
import io.munzil.munzil.domain.user.presentation.dto.response.AuthorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class QueryFeedDetailsService {

    private final FeedImageRepository feedImageRepository;
    private final FeedRepository feedRepository;
    private final UserFacade userFacade;

    @Transactional(readOnly = true)
    public QueryFeedDetailsResponse execute(Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> FeedNotFoundException.EXCEPTION);

        User user = userFacade.getCurrentUser();

        List<String> imageUrl = feedImageRepository.findByFeed(feed)
                .stream()
                .map(FeedImage::getImageUrl)
                .collect(Collectors.toList());

        FeedDetailsVO feedDetailsVO = feedRepository.queryFeedDetails(feedId, user.getId());

        return QueryFeedDetailsResponse.builder()
                .question(buildQuestionResponse(feedDetailsVO.getQuestionVO()))
                .author(buildAuthorResponse(feedDetailsVO.getAuthorVO()))
                .feedId(feedDetailsVO.getFeedId())
                .content(feedDetailsVO.getContent())
                .imageUrl(imageUrl)
                .createdAt(feedDetailsVO.getCreatedAt())
                .build();
    }

    private QuestionResponse buildQuestionResponse(QuestionVO questionVO) {
        return QuestionResponse.builder()
                .questionId(questionVO.getQuestionId())
                .questionName(questionVO.getQuestionName())
                .build();
    }

    private AuthorResponse buildAuthorResponse(AuthorVO authorVO) {
        return AuthorResponse.builder()
                .userId(authorVO.getUserId())
                .nickname(authorVO.getNickname())
                .profileImageUrl(authorVO.getProfileImageUrl())
                .build();
    }
}
