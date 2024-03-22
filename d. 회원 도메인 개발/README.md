# 4. 회원 도메인 개발

## Index


## 회원 리포지토리(MemberRepository) 개발

```java
@Repository
public class MemberRepository {
    @PersistenceContext
    private EntityManager em;

    public void save(Member member) {
        em.persist(member);
    }

    public Member find(Long id) {
        return em.find(Member.class, id);
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
            .getResultList();
    }

    public List<Member> findByName(String name) {
        return em.createQuery("select m from Member m where m.name = :name", Member.class)
            .setParameter("name", name)
            .getResultList();
    }
}
```

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

## 회원 서비스(MemberService) 개발

**기술 설명** 
`@Service`
`@Transactional` : 트랜잭션, 영속성 컨텍스트
`readOnly=true` : 데이터의 변경이 없는 읽기 전용 메서드에 사용, 영속성 컨텍스트를 플러시 하지 않으므로 약간의 성능 향상(읽기 전용에는 다 적용)
데이터베이스 드라이버가 지원하면 DB 에서 성능 향상 
`@Autowired`: 생성자 Injection 많이 사용, 생성자가 하나면 생략 가능

### 스프링 필드 주입 대신에 생성자 주입을 사용하자.

**생성자 주입** 
```java
public class MemberService {
    private final MemberRepository memberRepository;
    
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
...
}
```

- 생성자 주입 방식을 권장 
- 변경 불가능한 안전한 객체 생성 가능 
- 생성자가 하나면, `@Autowired` 를 생략할 수 있다.
- `final` 키워드를 추가하면 컴파일 시점에 `memberRepository` 를 설정하지 않는 오류를 체크할 수 있다.(보통 기본 생성자를 추가할 때 발견) 

**lombok**
```java
@RequiredArgsConstructor
public class MemberService {
     private final MemberRepository memberRepository;
... 
}
```

참고: 스프링 데이터 JPA를 사용하면 `EntityManager` 도 주입 가능

```java
@Repository
@RequiredArgsConstructor
public class MemberRepository { 
    private final EntityManager em;
    ... 
}
```  


### ⭐️DAO 와 Repository

매우 디테일하게 들어가면 DAO는 DB 데이터에 접근하는 모든 기능을 이야기하는 것이고, repository는 엔티티를 관리하는 저장소입니다. 그런데 실무에서 사용할 때 크게 구분하지 않기 때문에 둘다 비슷하게 생각하셔도 됩니다.
DAO는 영속성컨텍스트랑 관계 없이 DB에서 데이터만 가지고오는 역할만이고 repository는 영속성컨텍스트랑 관계가 있는거라고 이해
JPA를 사용하면 그냥 repository라고 하고, JPA를 사용하지 않을 때는 DAO라고 많이 합니다.


### ⭐️validateDuplicateMember() 에서 DB Column Unique 제약조건

validateDuplicateMember를 할 때에 동시에 회원가입이 이루어질 때 방지로 name에 유니크를 걸어 주신다고 했는데 이해가 잘 되지를 않습니다.

-> 서버가 한대만 있고, 자바(JVM)로 웹 애플리케이션을 단 하나만 구동하는 상황이면 자바 만으로 동시성 제어를 할 수 있습니다. 
그런데 실무에서는 보통 서버 두 대 이상을 사용하기 때문에, 동시성 제어를 JVM안에서 해결하는게 어렵습니다. 
관계형 데이터베이스는 이런 동시성 제어를 고려해서 개발되었기 때문에, 결국 관계형 데이터베이스에 동시성 제어를 위임해야 합니다. 
그 중에 관계형 데이터베이스가 제공하는 유니크 제약조건을 사용하면, 같은 이름을 절대로 동시에 저장할 수 없습니다. 
그래서 name에 유니크 제약조건을 실무에서는 걸어주어야 한다고 이야기 했습니다. 그런데 이런 유니크 제약조건은 정말 최악의 경우(진짜 초 단위로 같은 데이터가 저장되었을 때)가 발생했을 때 동작하는 것이고, 
대부분은 validation에서 막힙니다.


