package study.querydsl;

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

import static org.assertj.core.api.Assertions.*;

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
        QMember m = new QMember("m");

        // jpql이랑 같이 짜면 된다
        // 파라미터 바인딩이 따로 필요 없다. 자동으로 pstm으로 자동으로 바인딩 한다.
        // JPQL은 문자로 작성해서 오타가 나면 실제 오류 발생이 실행한 시점에서야 알게 되는데 (runtime)
        // querydsl은 컴파일 시점에 오류를 잡아낸다.
        Member findMember = queryFactory
                .select(m)
                .from(m)
                .where(m.username.eq("member1")) // 파라미터 바인딩 처리
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }
}
