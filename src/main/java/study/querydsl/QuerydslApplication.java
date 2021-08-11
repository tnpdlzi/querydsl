package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.persistence.EntityManager;

@SpringBootApplication
public class QuerydslApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuerydslApplication.class, args);
	}

	// 그냥 스프링 빈으로 등록해버리기.
	// 싱글톤이다. 동시성 문제가 없을까? 없다. EntityManager도 마찬가지.
	// Entity Manager에 의존하게 되는데, 스프링은 트랜잭션에 의존하게 된다.
	@Bean
	JPAQueryFactory jpaQueryFactory(EntityManager em) {
		return new JPAQueryFactory(em);
	}
}
