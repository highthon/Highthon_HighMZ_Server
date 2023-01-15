package io.munzil.munzil.domain.feed.service;

import io.munzil.munzil.domain.feed.domain.FeedImage;
import io.munzil.munzil.domain.feed.domain.repository.FeedImageRepository;
import io.munzil.munzil.domain.feed.domain.repository.FeedRepository;
import io.munzil.munzil.domain.feed.domain.repository.vo.FeedConditionVO;
import io.munzil.munzil.domain.feed.domain.repository.vo.FeedDetailsVO;
import io.munzil.munzil.domain.feed.presentation.dto.response.QueryFeedDetailsResponse;
import io.munzil.munzil.domain.feed.presentation.dto.response.QueryFeedPagesResponse;
import io.munzil.munzil.domain.question.domain.repository.vo.QuestionVO;
import io.munzil.munzil.domain.question.presentation.dto.response.QuestionResponse;
import io.munzil.munzil.domain.user.domain.User;
import io.munzil.munzil.domain.user.domain.repository.vo.AuthorVO;
import io.munzil.munzil.domain.user.facade.UserFacade;
import io.munzil.munzil.domain.user.presentation.dto.response.AuthorResponse;
import io.munzil.munzil.global.enums.SortPageType;
import io.munzil.munzil.global.utils.code.PagingSupportUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class QueryFeedPagesService {

    private final FeedRepository feedRepository;
    private final FeedImageRepository feedImageRepository;
    private final UserFacade userFacade;

    @Transactional(readOnly = true)
    public QueryFeedPagesResponse execute(Long cursorId, Integer pageId, SortPageType sortType) {
        User user = userFacade.getCurrentUser();

        Slice<FeedDetailsVO> feedList = getFeedList(
                FeedConditionVO.builder()
                        .cursorId(PagingSupportUtil.applyCursorId(cursorId))
                        .pageId(pageId)
                        .userId(user.getId())
                        .orders(sortType.getCode())
                        .build(),
                sortType);

        List<QueryFeedDetailsResponse> queryFeedDetailsResponseList = new ArrayList<>();

        for (FeedDetailsVO feedDetailsVO : Objects.requireNonNull(feedList)) {
            List<String> imageUrl = feedImageRepository.findByFeedId(feedDetailsVO.getFeedId())
                    .stream()
                    .map(FeedImage::getImageUrl)
                    .collect(Collectors.toList());

            queryFeedDetailsResponseList.add(
                    buildFeedDetailsResponse(feedDetailsVO, imageUrl)
            );
        }

        return new QueryFeedPagesResponse(queryFeedDetailsResponseList, feedList.hasNext(), queryFeedDetailsResponseList.size(), pageId);
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

    private QueryFeedDetailsResponse buildFeedDetailsResponse(FeedDetailsVO feedDetailsVO, List<String> imageUrl) {
        return QueryFeedDetailsResponse.builder()
                .question(buildQuestionResponse(feedDetailsVO.getQuestionVO()))
                .author(buildAuthorResponse(feedDetailsVO.getAuthorVO()))
                .feedId(feedDetailsVO.getFeedId())
                .imageUrl(imageUrl)
                .createdAt(feedDetailsVO.getCreatedAt())
                .content(feedDetailsVO.getContent())
                .build();
    }

    private Slice<FeedDetailsVO> getFeedList(FeedConditionVO feedConditionVO, SortPageType sortPageType) {
        switch (sortPageType) {
            case LATEST:
                return feedRepository.queryFeedPagesByCursor(feedConditionVO, PagingSupportUtil.applyPageSize());
            case POPULAR:
                return feedRepository.queryFeedPagesByOffset(feedConditionVO, PagingSupportUtil.applyPageSize());
            default:
                return null;
        }
    }
}
