package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static study.querydsl.entity.QMember.*;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {
    @Autowired
    EntityManager em;
    JPAQueryFactory queryFactory;


    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(em);

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    public void startJPQL() {
        //member1을 찾아라.
        String qlString = "select m from Member m where m.username = :username";
        Member findMember = em.createQuery(qlString, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQuerydsl() {
        // 시작은 JPAQUeryFactory
        // 만들 때 entity manager도 같이 넘겨줘야한다.
        // 얘를 필드로 넘길 수 있따. beforeEach 참고.
//        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        // 동시성 문제가 발생하지 않는다. 멀티 스레드에서 들어와도 현재 나의 트랜잭션에 따라 바인딩 되도록 바인딩 해 준다. 필드로 빼서 사용하는 걸 권장.

        // 어떤 q 멤버인지 이름을 지어준다. 구분하는 이름이다. 크게 중요하진 않음. 나중에 안 쓴다. QMember.member를 쓰게 된다.
//        QMember m = new QMember("m");

        // 이 방법도 있다. QMember에 보면 생성해 놓은 게 있다. 그냥 이거 쓰면 된다.
//        QMember member = QMember.member;
//        근데 얘를 지우는 방법도 있다.
        // QMember.member를 static import 하는 것. 그럼 그냥 member로 쓸 수 있다.

        // alias가 member1로 되어있는데 이걸 변경하는 방법이 있다.
        // 이렇게 바꾸면 그냥 다 m1으로 바뀐다.
//        QMember m1 = new QMember("m1");
        // 같은 테이블을 조인해야 할 경우에 이런식으로 선언해서 바꿔주면 된다.

        // jpql이랑 같이 짜면 된다
        // 파라미터 바인딩이 따로 필요 없다. 자동으로 pstm으로 자동으로 바인딩 한다.
        // JPQL은 문자로 작성해서 오타가 나면 실제 오류 발생이 실행한 시점에서야 알게 되는데 (runtime)
        // querydsl은 컴파일 시점에 오류를 잡아낸다.
        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1")) // 파라미터 바인딩 처리
                .fetchOne();

        // querydsl은 결국 jpql의 빌더 역할을 한다. 결국 jpql로 돌아가는 것.

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }


    @Test
    public void search() {
        // chain을 and나 or로 걸 수 있다.
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.between(10, 30)))
                .fetchOne();

        // 대표적인것은 eq = equal, ne = not equeal, eq().not도 가능
        // isNotNull도 가능
        // in도 가능. (10, 20) 10살이거나 20살이거나.
        // notIn도 가능, between도.

        //goe 가능 greater or equal
        // gt = greater
        // loe = little or equal
        // lt = little

        // like = like 검색
        // contains = like '%member$' 검색
        // startsWith = like 'member%' 검색

        // 등등 있음. 검색해서 써보자.
        // . 찍어보면 나온다.

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void searchAndParam() {
        Member findMember = queryFactory
                .selectFrom(member)
                .where(
                        // 이 방식 선호. 후에 이걸 통해 동적쿼리에서 깔끔한 쿼리가 완성된다.
                        member.username.eq("member1"), // and의 경우 .and 안 넣고 ,를 통해 처리할 수 있다. 여러개 넘기면 걔는 그냥 and다. where의 파라미터로 넘기면 and로 된다.
                        member.age.eq(10)
                )
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void resultFetch() {
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();

        Member fetchOne = queryFactory
                .selectFrom(QMember.member)
                .fetchOne();

        Member fetchFirst = queryFactory
                .selectFrom(QMember.member)
//                .limit(1).fetchOne() 이거랑 똑같다.
                .fetchFirst();

        QueryResults<Member> results = queryFactory
                .selectFrom(member)
                .fetchResults();

        // paging 할 때 기능
        // count 쿼리가 복잡해지는 경우엔 따로 나눠서 쿼리를 두 개 써야지 이걸 쓰면 안 된다.
        results.getTotal();
        List<Member> content = results.getResults();

        // count만 가져오기.
        long total = queryFactory
                .selectFrom(member)
                .fetchCount();
    }

    /**
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 올림차순(asc)
     * 단, 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
     */
    @Test
    public void sort() {
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);
        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }

    @Test
    public void paging1() {
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();
        assertThat(result.size()).isEqualTo(2);
    }


    @Test
    public void paging2() {
        // 실무에서는 카운트 쿼리를 완전히 따로 분리해야 할 수 있다. 그런 경우 이걸 못 쓴다.
        QueryResults<Member> queryResults = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();

        assertThat(queryResults.getTotal()).isEqualTo(4);
        assertThat(queryResults.getLimit()).isEqualTo(2);
        assertThat(queryResults.getOffset()).isEqualTo(1);
        assertThat(queryResults.getResults().size()).isEqualTo(2);
    }


}
