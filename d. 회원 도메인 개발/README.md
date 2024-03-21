# 4. 회원 도메인 개발

## Index


## MemberRepository 개발

**기술 설명**
- `@Repository` : 스프링 빈으로 등록, JPA 예외를 스프링 기반 예외로 예외 변환 
- `@PersistenceContext` : 엔티티 메니저(`EntityManager`) 주입 
- `@PersistenceUnit` : 엔티티 메니터 팩토리(`EntityManagerFactory`) 주입

### ⭐️스프링 컨테이너와 EntityManager

- **`@PersistenceContext` 어노테이션이 있으면 스프링 컨테이너가 EntityManager 를 주입해준다.**
  - 스프링은 생성자가 하나이면 자동으로 `@Autowired` 가 적용된다.
  - 따라서 EntityManager가 생성자를 통해서 주입되고, 최종적으로 다음 필드에 담겨진다. 
  - `private final EntityManager em;`
- 결과적으로 주입된 EntityManager는 싱글톤이 맞다. 
- ***여기에서 싱글톤이니 동시성 문제가 될 수 있다!!!***
- 스프링 컨테이너는 여기에 실제 EntityManager를 주입하는 것이 아니라, **사실은 실제 EntityManager를 연결해주는 가짜 EntityManager를 주입해준다.**
- 그리고 이 EntityManager를 호출하면, 현재 데이터베이스 트랜잭션과 관련된 실제 EntityManager를 호출해준다.
- 덕분에 개발자는 동시성 이슈에 대한 고민없이, 쉽게 개발할 수 있다.