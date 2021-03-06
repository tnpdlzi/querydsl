package study.querydsl.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.support.PageableExecutionUtils;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;

import javax.persistence.EntityManager;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

// 꼭 원하는 인터페이스 ~~ Impl이라는 이름으로 작성해야한다.
//public class MemberRepositoryImpl extends QuerydslRepositorySupport implements MemberRepositoryCustom {
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    // QuerydslRepositorySupport를 쓰면 이걸 안 쓰고 super를 씀
    private final JPAQueryFactory queryFactory;

    public MemberRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    // 부모가 제공하는 기능이 몇 개 있다.
    // EntityManager, queryDsl을 쓸 수 있다.
    // from도 편하게 쓸 수 있다.
//    public MemberRepositoryImpl() {
//        super(Member.class);
//    }
//
//
//    @Override
//    public List<MemberTeamDto> search(MemberSearchCondition condition) {
//        EntityManager entityManager = getEntityManager(); // 이런 식으로 받아 쓸 수 있다.
//
//        // querydsl 3 version에서 사용한 것.
//        return from(member)
//                .leftJoin(member.team, team)
//                .where(
//                        usernameEq(condition.getUsername()),
//                        teamNameEq(condition.getTeamName()),
//                        ageGoe(condition.getAgeGoe()),
//                        ageLoe(condition.getAgeLoe())
//                )
//                .select(new QMemberTeamDto(
//                        member.id.as("memberId"),
//                        member.username,
//                        member.age,
//                        team.id.as("teamId"),
//                        team.name.as("teamName")
//                ))
//                .fetch();
//
//    }

    // 실무에서 안 쓴다.
    // 편리한 변환 가능. sort는 안 됨.
    // from으로 시작 가능하나 select로 시작하는게 더 명시적이다.
    // JPAQueryFactory 시작 불가능. QueryFactory 제공 안 함.
//    public Page<MemberTeamDto> searchPageSimple2(MemberSearchCondition condition, Pageable pageable) {
//        JPQLQuery<MemberTeamDto> jpaQuery = from(member)
//                .leftJoin(member.team, team)
//                .where(
//                        usernameEq(condition.getUsername()),
//                        teamNameEq(condition.getTeamName()),
//                        ageGoe(condition.getAgeGoe()),
//                        ageLoe(condition.getAgeLoe())
//                )
//                .select(new QMemberTeamDto(
//                        member.id.as("memberId"),
//                        member.username,
//                        member.age,
//                        team.id.as("teamId"),
//                        team.name.as("teamName")
//                ));
//        // 유틸리티 클래스
//        // 이러면 얘가 offset, limit을 다 넣어준다.
//        JPQLQuery<MemberTeamDto> query = getQuerydsl().applyPagination(pageable, jpaQuery);
//
//        query.fetch();
//
//        List<MemberTeamDto> content = results.getResults();
//        long total = results.getTotal(); // count
//
//        // pageImpl 은 페이지의 구현체이다.
//        return new PageImpl<>(content, pageable, total);
//    }


    @Override
    public List<MemberTeamDto> search(MemberSearchCondition condition) {
        return queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .fetch();
    }

    @Override
    public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {
        QueryResults<MemberTeamDto> results = queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                // pageable에서 받아서 offset과 limit 설정
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();// 컨텐츠용쿼리, 카운터용 쿼리 두 개 날린다.

        List<MemberTeamDto> content = results.getResults();
        long total = results.getTotal(); // count

        // pageImpl 은 페이지의 구현체이다.
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {
        List<MemberTeamDto> content = queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                // pageable에서 받아서 offset과 limit 설정
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch(); // 이러면 count용 쿼리는 날라가지 않는다.

        // count용 쿼리를 따로 만든다.
        // join이 필요 없을 때 좋다.
        // content 쿼리는 복잡한데 count 쿼리는 간편할 때 주로 사용
        // 카운트 쿼리 최적화 가능
        // 웬만하면 카운트 쿼리는 최적화 하는 게 좋다. 데이터 없을 때 말고!
//        long total = queryFactory
//                .select(member)
//                .from(member)
//                .leftJoin(member.team, team)
//                .where(
//                        usernameEq(condition.getUsername()),
//                        teamNameEq(condition.getTeamName()),
//                        ageGoe(condition.getAgeGoe()),
//                        ageLoe(condition.getAgeLoe())
//                )
//                .fetchCount();

//        return new PageImpl<>(content, pageable, total);


        // count 쿼리는 생략 가능한 경우가 있다.
        // 1. 페이지 시작이면서 컨텐츠 사이즈가 페이지 사이즈보다 작을 때
        // 2. 마지막 페이지 일 때
        JPAQuery<Member> countQuery = queryFactory
                .select(member)
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                );


        // .fetchCount를 해야 카운트 쿼리가 날라간다.
        // 1, 2 번의 경우에는 count 쿼리를 따로 실행하지 않는다. 그래서 최적화가 된다.
//        return PageableExecutionUtils.getPage(content, pageable, () -> countQuery.fetchCount());
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);

    }

    private BooleanExpression usernameEq(String username) {
        return hasText(username) ? member.username.eq(username) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }

}
