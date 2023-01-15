package io.munzil.munzil.domain.feed.domain.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.munzil.munzil.domain.feed.domain.repository.vo.FeedConditionVO;
import io.munzil.munzil.domain.feed.domain.repository.vo.FeedDetailsVO;
import io.munzil.munzil.domain.feed.domain.repository.vo.QFeedDetailsVO;
import io.munzil.munzil.domain.question.domain.repository.vo.QQuestionVO;
import io.munzil.munzil.domain.user.domain.repository.vo.QAuthorVO;
import io.munzil.munzil.global.utils.code.PagingSupportUtil;
import io.munzil.munzil.global.utils.code.QueryDslSupportUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.ArrayList;
import java.util.List;

import static io.munzil.munzil.domain.feed.domain.QFeed.feed;
import static io.munzil.munzil.domain.like.domain.QFeedLike.feedLike;
import static io.munzil.munzil.domain.question.domain.QFeedQuestion.feedQuestion;
import static org.springframework.util.StringUtils.hasText;

@RequiredArgsConstructor
public class FeedRepositoryImpl implements FeedRepositoryCustom {

    private final JPAQueryFactory query;

    @Override
    public FeedDetailsVO queryFeedDetails(Long feedId, Long userId) {
        return selectFromFeed(userId)
                .where(eqFeedId(feedId))
                .fetchOne();
    }

    @Override
    public Slice<FeedDetailsVO> queryFeedPagesByOffset(FeedConditionVO feedConditionVO, Pageable pageable) {

        List<OrderSpecifier> ORDERS = getAllOrderSpecifiers(feedConditionVO);

        JPAQuery<FeedDetailsVO> jpaQuery = selectFromFeed(feedConditionVO.getUserId())
                .distinct()
                .where(
                        eqFeedQuestionQuestionId(feedConditionVO.getQuestionId()),
                        eqFeedUserId(feedConditionVO.getFindUserId())
                )
                .orderBy(ORDERS.toArray(OrderSpecifier[]::new));

        return PagingSupportUtil.fetchSliceByOffset(jpaQuery, PageRequest.of(feedConditionVO.getPageId(), pageable.getPageSize()));

    }

    @Override
    public Slice<FeedDetailsVO> queryFeedPagesByCursor(FeedConditionVO feedConditionVO, Pageable pageable) {

        List<OrderSpecifier> ORDERS = getAllOrderSpecifiers(feedConditionVO);

        JPAQuery<FeedDetailsVO> jpaQuery = selectFromFeed(feedConditionVO.getUserId())
                .distinct()
                .where(
                        eqPage(feedConditionVO.getCursorId()),
                        eqFeedQuestionQuestionId(feedConditionVO.getQuestionId()),
                        eqFeedUserId(feedConditionVO.getFindUserId())
                )
                .orderBy(ORDERS.toArray(OrderSpecifier[]::new));

        return PagingSupportUtil.fetchSliceByCursor(jpaQuery, pageable);
    }

    private BooleanExpression eqPage(Long cursorId) {
        return cursorId != null ? feed.id.lt(cursorId) : null;
    }

    private BooleanExpression eqFeedQuestionFeedId(NumberPath<Long> id) {
        return id != null ? feedQuestion.feed.id.eq(id) : null;
    }

    private BooleanExpression eqFeedQuestionQuestionId(Long id) {
        return (id != null && id != 0) ? feedQuestion.question.id.eq((id)) : null;
    }

    private BooleanExpression eqFeedUserId(Long id) {
        return id != null ? feed.user.id.eq(id) : null;
    }

    private BooleanExpression eqFeedId(Long id) {
        return id != null ? feed.id.eq(id) : null;
    }

    private JPAQuery<FeedDetailsVO> selectFromFeed(Long userId) {
        return query
                .select(new QFeedDetailsVO(
                        new QQuestionVO(
                                feedQuestion.question.id,
                                feedQuestion.question.questionName
                        ),
                        new QAuthorVO(
                                feed.user.id,
                                feed.user.nickname,
                                feed.user.profileImageUrl
                        ),
                        feed.content,
                        feed.id,
                        feed.feedLikes.size(),
                        feedLike.isNotNull(),
                        feed.createdAt
                ))
                .from(feed)
                .leftJoin(feedQuestion)
                .on(eqFeedQuestionFeedId(feed.id));
    }

    private List<OrderSpecifier> getAllOrderSpecifiers(FeedConditionVO feedConditionVO) {

        List<OrderSpecifier> ORDERS = new ArrayList<>();

        if (hasText(feedConditionVO.getOrders())) {
            switch (feedConditionVO.getOrders()) {
                case "POPULAR":
                    OrderSpecifier<?> orderPopular = QueryDslSupportUtil.getSortedColumn(Order.DESC, feed.feedLikes.size());
                    ORDERS.add(orderPopular);
                    break;
                default:
                    break;
            }
        }
        OrderSpecifier<?> orderId = QueryDslSupportUtil.getSortedColumn(Order.DESC, feed, "id");
        ORDERS.add(orderId);
        return ORDERS;
    }
}
