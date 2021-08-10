package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.dto.UserDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import java.util.List;

import static com.querydsl.jpa.JPAExpressions.*;
import static org.assertj.core.api.Assertions.*;
import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.*;

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

    @Test
    public void aggregation() {
        // querydsl Tuple로 꺼내오게 됨.
        // 타입이 여러개로 들어올 때 Tuple로 들어온다.
        // 근데 보통 DTO로 뽑아오는 방법을 많이 쓴다.
        List<Tuple> result = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);
        // 위에 쓴거 그대로 넣으면 값 조회 가능.
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    /**
     * 팀의 이름과 각 팀의 평균 연령을 구해라.
     */
    @Test
    public void group() {
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
//                .having(item.price.gt(1000)) 이런식으로 having도 가능
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15); // (10 + 20) / 2

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35); // (30 + 40) / 2

    }

    /**
     * 팀 A에 소속된 모든 회원 찾기
     */
    @Test
    public void join() {
        // jpql 쿼리 빌더 역할.
        // select member from member inner join member.team as team where team.name = "teamA"
        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team) // leftJoin, rightJoin도 가능.
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2");
    }

    /**
     * 세타 조인
     * 회원의 이름이 팀 이름과 같은 회원 조회
     * --> 연관관계가 없는 조인
     */
    @Test
    public void theta_join() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        // 모든 회원과 모든 팀을 가져와서 다 조인. 그 후 where에서 필터링. 맞 조인을 다 해버리는 걸 세타 조인이라고 한다. 디비가 보통 성능최적화는 함.
        // 외부 조인 불가능. 근데 on을 사용하면 외부 조인 가능.
        List<Member> result = queryFactory
                .select(member)
                .from(member, team) // 그냥 프롬절에 두 개 나열
                .where(member.username.eq(team.name))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");

    }

    // join on 절.
    // 1. 조인대상 필터링
    // 2. 연관관계 없는 엔티티 외부 조인
    // 2번에 많이 쓰임.

    /**
     * 예) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
     * JPQL: select m, t from Member m left join m.team t on t.name = 'teamA'
     */
    @Test
    public void join_on_filtering() {
        // on 절은 leftJoin을 쓰는 경우에 쓰자. 만약 Inner join인 경우 그냥 WHere절을 쓰자.
        // select가 여러 타입이라 튜플.
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("teamA")) // 그냥 JOIN 하면 팀이 없는 애들은 아예 나타나지 않음.
                // inner join하면 그냥 WHere 절에서 JOIn 하는 거랑 같다.
//                .join(member.team, team).where(team.name.eq("teamA"))
                // 즉, inner join이면 On절 안 쓰고 where로 걸러도 같다. 이런 경우에는 WHERE로 걸러내는 것이 좋다.
                .fetch();

        // left join이기 때문에 member 데이타는 다 가져온다. 그런데 teamA로 on 을 이용한 join을 했으니 teamA가 아닌 애들은 null로 나타난다.
        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    // 얘는 실무에서 쓸 일이 있다. 필요할 때가 있다.
    // 연관이 아예 없어도 join 할 수 있다.
    /**
     * 2. 연관관계가 없는 엔티티 외부 조인
     * 예) 회원의 이름과 팀의 이름이 같은 대상 외부 조인
     * JPQL: select m, t from Member m left join Team t on m.username = t.name
     * SQL : select m.*, t.* from Member m left join team t on m.username = t.name
     */
    @Test
    public void join_on_no_relation() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
//                .leftJoin(member.team, team) 기존의 조인. 이걸 빼버리면 id로 매칭하는 게 아니기 때문에 이름으로 딱 조인이 된다.
                .leftJoin(team).on(member.username.eq(team.name)) // 막 조인
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    public void fetchJoinNo() {
        em.flush();
        em.clear();

        // lazy기 때문에 member만 조회되고 team은 안 나온다.
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        // 이 엔티티가 로딩된 엔티티인지 아직 초기화가 안 된 에티티인지 가르쳐준다.
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 미적용").isFalse();

    }

    @Test
    public void fetchJoinUse() {
        em.flush();
        em.clear();

        // lazy기 때문에 member만 조회되고 team은 안 나온다.
        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin() // 여기에 페치 조인만 들어가고 나머지는 똑같으면 된다.
                // 그러면 연관된 애들을 한번에 끌고온다.
                .where(member.username.eq("member1"))
                .fetchOne();

        // 이 엔티티가 로딩된 엔티티인지 아직 초기화가 안 된 에티티인지 가르쳐준다.
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 미적용").isTrue();
    }

    // 서브쿼리
    // JPAExpressions 사용
    /**
     * 나이가 가장 많은 회원 조회
     */
    @Test
    public void subQuery() {

        // subquery이기 때문에 alias가 member로 겹치면 안 됨.
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(40);
    }

    /**
     * 나이가 평균 이상인 회원 조회
     */
    @Test
    public void subQueryGoe() {

        // subquery이기 때문에 alias가 member로 겹치면 안 됨.
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(30, 40);
    }

    /**
     * 나이가 10살 이상인 회원 조회
     */
    @Test
    public void subQueryIn() {

        // subquery이기 때문에 alias가 member로 겹치면 안 됨.
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        //                        JPAExpressions. // static import 했다
                        select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(10))
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(20, 30, 40);
    }

    @Test
    public void selectSubQuery() {
        QMember memberSub = new QMember("memberSub");

        List<Tuple> result = queryFactory
                .select(member.username,
//                        JPAExpressions. // static import 했다
                        select(memberSub.age.avg())
                                .from(memberSub))
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    // from절의 서브쿼리가 안됨.
    // from 절의 서브쿼리 한계
    // JPA, JPQL서브쿼리의 한계. 쿼리dsl도 마찬가. 인라인뷰(from절의 서브쿼리)는 지원하지 않음.

    // from 절의 서브쿼리 해결방안
    // 1. 서브쿼리를 join으로 변경한다. 일반적으로 바꾸는게 더 효율이 좋다.
    // 2. 애플리케이션에서 쿼리를 2번 분리해서 실행한다.
    // 3. native SQL을 사용한다.


    // case를 꼭 써야할까?
    // 가급적이면 db에서 case를 쓰지 말자. raw 데이터를 필터링, 그룹핑 하는 정도로 쓰자.
    @Test
    public void basicCase() {
        List<String> result = queryFactory
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void complexCase() {
        List<String> result = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0, 20)).then("0~20살")
                        .when(member.age.between(21, 30)).then("21~30살")
                        .otherwise("기타"))
                .from(member)
                .fetch();
        for (String s : result) {
            System.out.println("s = " + s);
        }
    }


    @Test
    public void constant() {
        List<Tuple> result = queryFactory
                .select(member.username, Expressions.constant("A")) // 상수 사용
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    public void concat() {

        // {username}_{age}
        List<String> result = queryFactory
//                .select(member.username.concat("_").concat(member.age)) // 이거 안됨. 타입이 다르기 때문.
                .select(member.username.concat("_").concat(member.age.stringValue())) // 이럼 됨. 타입 캐스팅이 일어난다. .stringValue는 많이 쓰게 된다. Enum type같은 경우도.
                .from(member)
                .where(member.username.eq("member1"))
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }

        // 문자가 아닌 건 .stringValue를 사용하자. 특히 ENUM에서 사용.
    }


    // 프로젝션 대상이 하나면 타입을 명확하게 지정활 수 있음
    // 둘 이상이면 튜플이나 DTO로 조회.
    @Test
    public void simpleProjection() {
        // 프로젝션 대상이 username String 하나일 때. Member로 해서 Member 타입이어도 마찬가지.
        List<String> fetch = queryFactory
                .select(member.username)
                .from(member)
                .fetch();

        for (String s : fetch) {
            System.out.println("s = " + s);
        }
    }

    // tuple로 가져오기.
    // 근데 튜플은 queryDsl의 것이다. 리포지토리에서 쓰는 건 괜찮지만 그걸 넘어 서비스나 컨트롤러에서 사용하는 것은 좋지 않다.
    // querydsl을 쓴다는 걸 외부에 노출하게 되기 때문.
    // 하부 기술을 바꿀 때 앞단을 바꿀 필요가 없게 하기 위해.
    // 바깥 계층에 보낼 때는 DTO로 바꿔서 보내자.
    @Test
    public void tupleProjection() {
        List<Tuple> result = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            // 이런 식으로 가져옴.
            String username = tuple.get(member.username);
            Integer age = tuple.get(member.age);
            System.out.println("username = " + username);
            System.out.println("age = " + age);
        }
    }

    // DTO를 이용한 조회.
    // 이게 실무에서 진짜 많이 사용하게되는 방법.

    // 우선 일반적인 jpa
    // new 명령어 사용해야함. 패키지 이름을 다 적어줘야해서 지저분. 생성자 방식만 지원.
    @Test
    public void findDtoByJPQL() {
        List<MemberDto> resultList = em.createQuery("select new study.querydsl.dto.MemberDto(m.username, m.age) from Member m", MemberDto.class)
                .getResultList();

        for (MemberDto memberDto : resultList) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    // QueryDsl 방법
    // 프로퍼티 접근
    // 필드 직접 접근
    // 생성자 사용

    // 프로퍼티를 이용한 접근 방법
    @Test
    public void findDtoBySetter() {
        List<MemberDto> result = queryFactory
                .select(Projections.bean(MemberDto.class, member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    // 필드를 이용한 접근 방법
    // getter, setter가 없어도 된다. 바로 필드에 값을 꽂는다.
    @Test
    public void findDtoByField() {
        List<MemberDto> result = queryFactory
                .select(Projections.fields(MemberDto.class, member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    // 생성자 접근 방법
    @Test
    public void findDtoByConstructor() {
        List<MemberDto> result = queryFactory
                .select(Projections.constructor(MemberDto.class, member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void findUserDto() {
        QMember memberSub = new QMember("memberSub");

        List<UserDto> result = queryFactory
                .select(Projections.fields(UserDto.class,
                        member.username.as("name"), // 이걸 달아줘야 UserDto의 name과 맞아 들어가게 된다.
                        // 얘도 ExpressionUtils써도 된다.
//                        ExpressionUtils.as(member.username, "name"),

                        // age는 서브쿼리릂 쓰고싶다? 회원 나이의 맥스 값만으로 찍고 싶다!
                        // 이름이 없을 때 ExpressionUtils로 두 번째 파라미터로 alias를 정할 수 있다.
                        ExpressionUtils.as(JPAExpressions
                                .select(memberSub.age.max())
                                    .from(memberSub), "age")
                ))
                .from(member)
                .fetch();

        // name이 아닌 username이라 매칭이 안 되기 때문에 null로 나타남.
       for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }
    }

    // 생성자 접근 방법
    @Test
    public void findUserDtoByConstructor() {
        List<UserDto> result = queryFactory
                .select(Projections.constructor(UserDto.class,
                        member.username, // 생성자는 이름 상관없이 타입만 맞춰주면 된다.
                        member.age)).distinct() // distinct는 그냥 .distinct해주면 된다.
                .from(member)
                .fetch();

        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }
    }

    // 이 방법이 가장 깔끔하다.
    // constructor와의 차이는 constructor는 다른 잘못된 값이 더 들어가도 실행은 되고 컴파일 시점에 에러가 나는 게 아닌 런타임에 에러가 난다.
    // 하지만 이 방식은 컴파일 시점에 에러가 발생한다.
    // 실제 생성자가 호출되는 것도 보장을 해 준다. 확인도 가능.
    // 하지만 단점이 존재
    // QueryProjection도 넣어줘야하고 Q파일도 생성됨
    // DTO는 QueryDSL과 완전히 독립적인 상태. 라이브러리 의존성이 없었지만 DTO가 QueryDSL 의존성이 생기게 됨.
    // DTO는 여러 layer에 걸쳐있는데 (리포지토리, 서비스, 컨트롤러 등) 그 DTO안에 QueryDSL에 의존적으로 설계되게 됨.
    // 애플리케이션 전반에서 사용한다면 사용한다고 판단하고 사용하자.
    @Test
    public void findDtoByQueryProjection() {
        List<MemberDto> result = queryFactory
                .select(new QMemberDto(member.username, member.age)) // 타입이 맞기 때문에 타입이 안 맞으면 컴파일 시점에 오류를 내 준다. command + P 도 가능.
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }

    }

}