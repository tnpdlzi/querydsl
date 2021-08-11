package study.querydsl.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Profile("local") // local일 때만 돈다. test에서는 안 돈다. application.yml에 정의되어있다.
@Component
@RequiredArgsConstructor
public class InitMember {

    // spring을 띄우면 local로 실행이 되고, 이 프로파일이 먹히면서 얘가 동작하게 된다. 그러면서 PostConstruct 실행되고, 값들이 들어가고 시작됨.

    private final InitMemberService initMemberService;

    @PostConstruct // 시작되기 전에 실행. 여기에 Transactional이 들어가지 않기 때문에 분리해준다.
    public void init() {
        initMemberService.init();
    }

    @Component
    static class InitMemberService {
        @PersistenceContext private EntityManager em;

        @Transactional
        public void init() {
            Team teamA = new Team("teamA");
            Team teamB = new Team("teamB");
            em.persist(teamA);
            em.persist(teamB);

            for (int i = 0; i < 100; i++) {
                Team selectedTeam = i % 2 == 0 ? teamA : teamB;
                em.persist(new Member("member" + i, i, selectedTeam));
            }
        }
    }


}
