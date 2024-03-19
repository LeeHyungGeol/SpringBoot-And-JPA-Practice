# 프로젝트 환경설정

## Index
- [프로젝트 생성](#프로젝트-생성)
  - [validation 모듈 추가](#validation-모듈-추가)
  - [Junit4,5 설정](#junit45-설정)
  - [Lombok 설정](#lombok-설정)
- [라이브러리 살펴보기](#라이브러리-살펴보기)
  - [gradle 의존관계 보기](#gradle-의존관계-보기)
  - [스프링 부트 라이브러리 살펴보기](#스프링-부트-라이브러리-살펴보기)
  - [테스트 라이브러리 살펴보기](#테스트-라이브러리-살펴보기)
  - [핵심 라이브러리 정리](#핵심-라이브러리-정리)
- [View 환경 설정](#view-환경-설정)
  - [thymeleaf 템플릿엔진 동작 확인(hello.html, index.html)](#thymeleaf-템플릿엔진-동작-확인hellohtml-indexhtml)
  - [spring-boot-devtools](#spring-boot-devtools)
  - [예제 결과](#예제-결과)
- [H2 데이터베이스 설치](#h2-데이터베이스-설치)
- [JPA 와 DB 설정, 동작확인](#jpa-와-db-설정-동작확인)
  - [Member entity 로 정상 작동하는지 테스트 해보기](#member-entity-로-정상-작동하는지-테스트-해보기)
  - [tdd 와 같은 템플릿 단축키 만드는 방법](#tdd-와-같은-템플릿-단축키-만드는-방법)
  - [OpenJDK 64-Bit Server VM warning 해결하는 방법](#openjdk-64-bit-server-vm-warning-해결하는-방법)
  - [쿼리 파라미터 로그 남기기](#쿼리-파라미터-로그-남기기)
    - [외부 라이브러리 사용하여 쿼리 파라미터 로그 남기기](#외부-라이브러리-사용하여-쿼리-파라미터-로그-남기기)

## 프로젝트 생성
스프링 부트 스타터(https://start.spring.io/)
- Project: **Gradle - Groovy** Project
- 사용 기능: web, thymeleaf, jpa, h2, lombok, validation
- groupId: jpabook artifactId: jpashop

스프링 부트 3.0을 선택하게 되면 다음 부분을 꼭 확인해주세요.
1. **Java 17 이상**을 사용해야 합니다.
2. **javax 패키지 이름을 jakarta로 변경**해야 합니다. 오라클과 자바 라이센스 문제로 모든 `javax` 패키지를 `jakarta` 로 변경하기로 했습니다.
3. **H2 데이터베이스를 2.1.214 버전 이상 사용해주세요.**

### validation 모듈 추가

- validation 모듈이 추가되었습니다. (최신 스프링 부트에서는 직접 추가해야 합니다.)

```groovy
implementation 'org.springframework.boot:spring-boot-starter-validation'
```


### Junit4,5 설정
강의 영상이 JUnit4를 기준으로 하기 때문에 `build.gradle` 에 있는 다음 부분을 꼭 직접 추가해주세요.
해당 부분을 입력하지 않으면 JUnit5로 동작합니다. JUnit5를 잘 알고 선호하시면 입력하지 않아도 됩니다.

```groovy
//JUnit4 추가 
testImplementation("org.junit.vintage:junit-vintage-engine") {
    exclude group: "org.hamcrest", module: "hamcrest-core"
}
```

### Lombok 설정

웬만하면 롬복은 그냥 쓰는걸 추천. 코드량을 많이 줄여주기도 하고 편하기 때문이다.

롬복 적용
1. Preferences plugin lombok 검색 실행 (재시작)
2. Preferences Annotation Processors 검색 Enable annotation processing 체크 (재시작)
3. 임의의 테스트 클래스를 만들고 @Getter, @Setter 확인

```java

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Hello {
    private String data;
}

@SpringBootApplication
public class JpabookApplication {

    public static void main(String[] args) {
        Hello hello = new Hello();
        hello.setData("data");
        System.out.println("hello.getData() = " + hello.getData());

        SpringApplication.run(JpabookApplication.class, args);
    }
}
```

```
hello.getData() = data
```

## 라이브러리 살펴보기

### gradle 의존관계 보기

`./gradlew dependencies —configuration compileClasspath`

### 스프링 부트 라이브러리 살펴보기

- spring-boot-starter-web
    - spring-boot-starter-tomcat: 톰캣 (웹서버)
    - spring-webmvc: 스프링 웹 MVC
- spring-boot-starter-thymeleaf: 타임리프 템플릿 엔진(View)
- spring-boot-starter-data-jpa
    - spring-boot-starter-aop
    - spring-boot-starter-jdbc
        - HikariCP 커넥션 풀 (부트 2.0 기본)
    - hibernate + JPA: 하이버네이트 + JPA
    - spring-data-jpa: 스프링 데이터 JPA
- spring-boot-starter(공통): 스프링 부트 + 스프링 코어 + 로깅
    - spring-boot
        - spring-core
    - spring-boot-starter-logging
        - logback, slf4j

### 테스트 라이브러리 살펴보기
**테스트 라이브러리**
- spring-boot-starter-test
    - junit: 테스트 프레임워크
    - mockito: 목 라이브러리
    - assertj: 테스트 코드를 좀 더 편하게 작성하게 도와주는 라이브러리
    - spring-test: 스프링 통합 테스트 지원

### 핵심 라이브러리 정리
**핵심 라이브러리**
- 스프링 MVC
- 스프링 ORM
- JPA, 하이버네이트
- 스프링 데이터 JPA

**기타 라이브러리**
- H2 데이터베이스 클라이언트
- 커넥션 풀: 부트 기본은 HikariCP
- WEB(thymeleaf)
- 로깅 SLF4J & LogBack
- 테스트



- 스프링 데이터 JPA -> 스프링 코어
- 스프링 Web -> 스프링 코어

과거에는 모든 라이브러리를 직접 다 찾아서 끌어와야 했는데, 이제는 필요한 라이브러리를 선택하면 해당 라이브러리를 사용하는데 필요한 의존관계를 다 긁어 옵니다. 이런 것을 의존성 전이라 합니다.

> 참고: 스프링 데이터 JPA는 스프링과 JPA를 먼저 이해하고 사용해야 하는 응용기술이다.

## View 환경 설정

> thymeleaf 의 장점은 markup 을 깨지 않고 그대로 사용해서, 웹 브라우저에서도 바로 열 수 있다. 3.x 버젼부터 단점들을 많이 개선해서 많이 편해졌고, 무엇보다 spring 과 integration(통합)되어서 좋다.

> 단점은 메뉴얼을 좀 보면서 활용법을 익혀야 한다. 생각보다 될 것 같은 것들이 안될 때가 있다.

> 사실, 뷰 템플릿 엔진, 서버 사이드 랜더링 보다는 reactor, vue 를 사용하는 것이 더 일반적이긴 하다.

**thymeleaf 템플릿 엔진**
- thymeleaf 공식 사이트: https://www.thymeleaf.org/
- 스프링 공식 튜토리얼: https://spring.io/guides/gs/serving-web-content/
- 스프링부트 메뉴얼: https://docs.spring.io/spring-boot/docs/2.1.6.RELEASE/reference/html/boot-features-developing-web-applications.html#boot-features-spring-mvc-template-engines

### thymeleaf 템플릿엔진 동작 확인(hello.html, index.html)


스프링 부트 thymeleaf viewName 매핑 `resources:templates/` +{ViewName}+ `.html`

```java
@Controller
public class HelloController {
    @GetMapping("hello")
    public String hello(Model model) {
        model.addAttribute("data", "hello");
        return "hello";
    }
}
```

- model 은 SpringUI 의 model 에 data 를 실어서 view 로 보낸다.
- return 은 화면 이름이다. `.html` 이 자동으로 붙는다. `hello.html` 이 되는 것이다.
    - `src/main/resources/templates` 에 `hello.html` 파일을 생성한다.

```html
 <!DOCTYPE HTML>
 <html xmlns:th="http://www.thymeleaf.org">
 <head>
     <title>Hello</title>
     <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
 </head>
<body>
<p th:text="'안녕하세요. ' + ${data}" >안녕하세요. 손님</p>
</body>
</html>
```

정적인 파일 (랜더링 안하고 순수하게 파일 뿌리기)

```html
 <!DOCTYPE HTML>
 <html xmlns:th="http://www.thymeleaf.org">
 <head>
     <title>Hello</title>
     <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
 </head>
<body>
Hello
 <a href="/hello">hello</a>
</body>
</html>
```

- html 파일을 생성 후에 항상 resource 폴더에 있기 때문에, 항상 서버를 다시 띄워야 한다.

### spring-boot-devtools

html 파일을 조금 수정했는데도 서버를 항상 재시작해야 한다면 매우 불편하다. 이럴 때 `spring-boot-devtools` 를 사용하면 편해진다.
- html` 파일을 컴파일만 해주면 서버 재시작 없이 View 파일 변경이 가능하다.
- intelliJ 컴파일 방법: `menu > Build > Recompile`

**적용 전**
<img width="1628" alt="스크린샷 2024-03-19 오후 2 35 19" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/a5ac8a3a-0c5a-48ee-8234-7cc3a47ec4d7">
**적용 후**

main 에서 restartedMain 으로 바뀐다.
![spring-boot-devtools 적용후](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/b29e0f9b-ae07-420f-8855-46ed55f82d63)

### 예제 결과

<img width="295" alt="스크린샷 2024-03-19 오후 2 30 11" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/9bbfc1ef-d97a-448f-aad8-d5aff389c776">

<img width="313" alt="스크린샷 2024-03-19 오후 2 30 22" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/9643269c-aa6d-4a95-9529-f583cd4e7e6f">

## H2 데이터베이스 설치

개발이나 테스트 용도로 가볍고 편리한 DB, 웹 화면 제공
[https://www.h2database.com]()

**다운로드 및 설치**
- 스프링 부트 2.x를 사용하면 **1.4.200 버전**을 다운로드 받으면 된다.
- 스프링 부트 3.x를 사용하면 **2.1.214 버전 이상** 사용해야 한다. 데이터베이스 파일 생성 방법

> 파일 생성이 권한도 있어야 되서, 아까의 그 세션 키를 물고 있어야 db 생성이 된다.

1. `jdbc:h2:~/jpashop` (최소 한번)
2. `~/jpashop.mv.db` 파일 생성 확인
3. 이후 부터는 `jdbc:h2:tcp://localhost/~/jpashop` 이렇게 접속

**주의**: H2 데이터베이스의 MVCC 옵션은 H2 1.4.198 버전부터 제거되었습니다. **최신 버전에서는 MVCC 옵션 을 사용하면 오류가 발생**합니다. 영상에서 나오는 MVCC 옵션은 제거해주세요.

참고
- `jdbc:h2:~/jpashop` : 파일모드로 접속
- `jdbc:h2:tcp://localhost/~/jpashop` : jdbc url 로 네트워크 모드로 접근

![jpabook mv db](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/cba6072c-dfda-4ce9-a758-9a21e1c31a8a)

## JPA 와 DB 설정, 동작확인

> **application properties 를 .properties 로 생성할 수도 있고, .yaml 파일로도 생성할 수 있다.**

`main/resources/application.yml`
```yaml
spring:
  datasource:
    url: jdbc:h2:tcp://localhost/~/jpabook
    username: sa
    password:
    driver-class-name: org.h2.Driver
  
  jpa:
    hibernate:
      ddl-auto: create
    properties:
      hibernate:
#        show_sql: true
        format_sql: true

logging.level:
  org.hibernate.SQL: debug
# org.hibernate.type: trace #스프링 부트 2.x, hibernate5
# org.hibernate.orm.jdbc.bind: trace #스프링 부트 3.x, hibernate6
```


- `spring.jpa.hibernate.ddl-auto: create` : 이 옵션은 애플리케이션 실행 시점에 테이블을 drop 하고, 다시 생성한다.

> - `show_sql` : 옵션은 `System.out` 에 하이버네이트 실행 SQL을 남긴다.
> - `org.hibernate.SQL` : 옵션은 logger를 통해 하이버네이트 실행 SQL을 남긴다.

따라서, **모든 로그 출력은 가급적 logger를 통해 남겨야 한다.** `show_sql` 옵션은 안쓰는게 맞다.

> - **주의!** `application.yml` 같은 `yml` 파일은 띄어쓰기(스페이스) 2칸으로 계층을 만듭니다. 따라서 띄어쓰기 2칸을 필수로 적어주어야 합니다.
> - 예를 들어서 아래의 `datasource` 는 `spring:` 하위에 있고 앞에 띄어쓰기 2칸이 있으므로
    `spring.datasource` 가 됩니다. 다음 코드에 주석으로 띄어쓰기를 적어두었습니다.

### Member entity 로 정상 작동하는지 테스트 해보기

**Member Entity**
```java
@Entity
@Getter @Setter
public class Member {
    @Id @GeneratedValue
    private Long id;
    private String username;
}
```

**Member Repository**
```java
@Repository
public class MemberRepository {

    @PersistenceContext
    private EntityManager em;

    public long save(Member member) {
        em.persist(member);
        return member.getId();
    }
}
```

- **Repository** 란 entity 를 db 에서 찾아주는 것, DAO 와 비슷한 것이다.
- `@Repository` 란 component scan 의 대상이 되는 컴포넌트 중 하나이다.
- `@PersistenceContext`: 해당 어노테이션이 있으면 Spring Boot 가 EntityManager 를 주입해준다.
    - spring-boot-starter-data-jpa 를 추가해서 라이브러리들이 다 딸려왔기 때문, 여기서 EntityManager 를 생성하는 팩토리 메서드 같은 코드가 다 포함되어 있다.
- `public long save(Member member)` 의 return 값이 long 인 이유는 **command 와 query 를 분리하라 원칙**에 의해서
    - save() 는 저장하기 때문에 side-effect 를 일으키는 commmand 성이기 때문에 return 값을 거의 안만든다. id 는 조회용으로 사용할 수도 있기 때문에, id 값 정도만 return 한다.


**Member Repository Test Code**
```java
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class MemberRepositoryTest {
  @Autowired MemberRepository memberRepository;

  @Test
  @Transactional
  @Rollback(value = false)
  public void testMember() throws Exception {
    // given
    Member member = new Member();
    member.setUsername("memberA");

    // when
    long savedId = memberRepository.save(member);
    Member findMember = memberRepository.find(savedId);

    // then
    assertThat(findMember.getId()).isEqualTo(member.getId());
    assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
    assertThat(findMember).isEqualTo(member);
    System.out.println("(findMember == member) = " + (findMember == member));
  }
}
```

- **`@Transactional` 어노테이션을 사용해야 하는 이유: EntityManager 를 통한 모든 데이터 변경은 Transaction 안에서 수행되어야 한다.**
    - `import org.springframework.transaction.annotation.Transactional;`
    - `import jakarta.transaction.Transactional;`
    - Spring Framework 를 사용하기 때문에 이미 spring 에 종속적으로 설계를 하기 때문에 spring 것을 사용하는 것을 권장한다.
- **Transactional 어노테이션은 테스트가 끝난 다음에 데이터를 롤백해버리기 때문에 db 에 데이터가 남아있지 않다.**
    - 테스트가 아닌 곳에 사용되면 정상적으로 동작하지만 테스트에 있으면 테스트가 끝난 다음에 롤백을 해버린다.
    - 롤백하기 싫다면, `@Rollback(value = false)` 를 추가해주면 된다.
- **findMember == member 이다. 같은 transaction 안의 같은 영속성 컨텍스트 안의 1차 캐시 안에서 id 값이 같으면 같은 entity 로 인식하기 때문이다.**

`import static org.assertj.core.api.Assertions.assertThat;`
> JUnit 5 자체에는 Assertions 클래스가 있지만, 이 클래스에서는 assertEquals, assertTrue, assertAll 같은 메서드를 제공합니다. assertThat 메서드를 사용하고 싶다면 AssertJ 라이브러리를 사용해야 합니다.
> Spring Boot의 spring-boot-starter-test 의존성에는 AssertJ가 포함되어 있기 때문에 별도로 AssertJ를 추가할 필요 없이 assertThat을 사용할 수 있습니다. AssertJ는 테스트 코드를 더 읽기 쉽고 직관적으로 만들어주는 매우 표현력이 풍부한 매칭 언어를 제공합니다.

> 참고: 스프링 부트를 통해 복잡한 설정이 다 자동화 되었다. `persistence.xml` 도 없고,  `LocalContainerEntityManagerFactoryBean` 도 없다.
> 스프링 부트를 통한 추가 설정은 스프링 부트 메뉴얼을 참고하자.

### tdd 와 같은 템플릿 단축키 만드는 방법

<img width="985" alt="스크린샷 2024-03-19 오후 8 57 36" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/078c6ff9-da12-43c3-85e3-02e46868ea7b">

`Settings... > Editors > Live Templates > Java > Live Template`

**Template Text**
```java 
@Test
@DisplayName("")
public void $METHOD_NAME$() throws Exception{
    // given
    
    // when
    
    // then
}
```

<img width="632" alt="스크린샷 2024-03-19 오후 8 59 24" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/0452b79b-b3be-43ad-9df5-9cedf2c36c24">

**Edit Variables**
- `$METHOD_NAME$` 에 대한 변수 설정

<img width="984" alt="스크린샷 2024-03-19 오후 9 00 03" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/fce29c70-77c6-4e17-b6fe-6dd439707523">

**Define**
- template 을 어디에서 사용할지 정의한다.

### OpenJDK 64-Bit Server VM warning 해결하는 방법

![OpenJDK 64-bit Server VM warning](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/1b456035-7dbf-4591-9e4e-586609e17f8a)

- 이 부분은 IntelliJ 내부에서 자바를 사용할 때 발생하는 단순 내부 경고여서 무시하셔도 괜찮긴 하지만, 빨간 줄로 떠서 거슬리니 해결해보자.
- 검색해보니, jdk 설치 후 나타나는 경고 메시지이다.

**첫번쨰 해결방법**
![async stack traces](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/6b374d5e-4f0c-4ccc-9a70-7cf7db0d93a3)
- 위의 이미지와 같이 `Build, Execution, Deployment > Debugger > Async Stack Traces` 에서 체크박스를 언체크해주면 된다.

**두번째 해결방법**

```groovy
tasks.named('test') {
	useJUnitPlatform()
	jvmArgs '-Xshare:off' // JVM 아규먼트 설정
//	maxParallelForks = Runtime.runtime.availableProcessors() // 병렬 실행 설정
}

//test {
//	useJUnitPlatform()
//	// JVM 옵션 설정
//	jvmArgs '-Xshare:off'
//}
```

- gradle 로 프로젝트를 생성했다면, 위와 같이 `jvmArgs '-Xshare:off'` 를 설정해주면 된다.
- 참고:
    - https://github.com/mockito/mockito/issues/3111
    - https://stackoverflow.com/questions/77512409/adding-xshareoff-jvm-arg-break-jacoco-maven-plugin-setup

### 쿼리 파라미터 로그 남기기
로그에 다음을 추가하기: SQL 실행 파라미터를 로그로 남긴다.

```yaml
logging.level:
  org.hibernate.SQL: debug
  org.hibernate.orm.jdbc.bind: trace #스프링 부트 3.x, hibernate6
```

**주의! 스프링 부트 3.x를 사용한다면 영상 내용과 다르기 때문에 다음 내용을 참고하자.**
- 스프링 부트 2.x, hibernate5 `org.hibernate.type: trace`
- 스프링 부트 3.x, hibernate6 `org.hibernate.orm.jdbc.bind: trace`

```
    insert 
    into
        member
        (username, id) 
    values
        (?, ?)
2024-03-20T00:19:12.776+09:00 TRACE 87801 --- [jpabook] [    Test worker] org.hibernate.orm.jdbc.bind  : binding parameter (1:VARCHAR) <- [memberA]
2024-03-20T00:19:12.776+09:00 TRACE 87801 --- [jpabook] [    Test worker] org.hibernate.orm.jdbc.bind  : binding parameter (2:BIGINT) <- [1]
```

#### 외부 라이브러리 사용하여 쿼리 파라미터 로그 남기기
https://github.com/gavlyukovskiy/spring-boot-data-source-decorator

**database connection 에서 발생한 sql statement 를 wrapping 해서 log 로 출력해주는 역할을 한다.**

- 스프링 부트를 사용하면 이 라이브러리만 추가하면 된다.
- 스프링 부트 3.0 이상을 사용하면 라이브러리 버전을 1.9.0 이상을 사용해야 한다.


```
implementation 'com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.5.6' 
implementation 'com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.9.0'
```

```
2024-03-20T01:04:13.375+09:00  INFO 88128 --- [jpabook] [    Test worker] p6spy  : #1710864253375 | took 0ms | statement | connection 4| url jdbc:h2:tcp://localhost/~/jpabook
insert into member (username,id) values (?,?)
insert into member (username,id) values ('memberA',1);
```

참고: 쿼리 파라미터를 로그로 남기는 외부 라이브러리는 시스템 자원을 사용하므로, 개발 단계에서는 편하게 사용해도 된다. **하지만 운영시스템에 적용하려면 꼭 성능테스트를 하고 사용하는 것이 좋다.** 



