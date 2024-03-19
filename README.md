# 실전! 스프링 부트와 JPA 활용1 - 웹 애플리케이션 개발

## Index

# 프로젝트 환경설정

## 프로젝트 생성
스프링 부트 스타터(https://start.spring.io/)
- Project: **Gradle - Groovy** Project
- 사용 기능: web, thymeleaf, jpa, h2, lombok, validation
- groupId: jpabook artifactId: jpashop

스프링 부트 3.0을 선택하게 되면 다음 부분을 꼭 확인해주세요.
1. **Java 17 이상**을 사용해야 합니다.
2. **javax 패키지 이름을 jakarta로 변경**해야 합니다. 오라클과 자바 라이센스 문제로 모든 `javax` 패키지를 `jakarta` 로 변경하기로 했습니다. 
3. **H2 데이터베이스를 2.1.214 버전 이상 사용해주세요.**

### validation 모듈이 추가

- validation 모듈이 추가되었습니다. (최신 스프링 부트에서는 직접 추가해야 합니다.)

```groovy
implementation 'org.springframework.boot:spring-boot-starter-validation'
```


### Junit4 설정
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

