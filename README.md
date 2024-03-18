# 실전! 스프링 부트와 JPA 활용1 - 웹 애플리케이션 개발

## Index

# 프로젝트 환경설정

## 프로젝트 생성
스프링 부트 스타터(https://start.spring.io/)
Project: **Gradle - Groovy** Project
사용 기능: web, thymeleaf, jpa, h2, lombok, validation
groupId: jpabook artifactId: jpashop

스프링 부트 3.0을 선택하게 되면 다음 부분을 꼭 확인해주세요. **1. Java 17 이상**을 사용해야 합니다.
**2. javax 패키지 이름을 jakarta로 변경**해야 합니다.
오라클과 자바 라이센스 문제로 모든 `javax` 패키지를 `jakarta` 로 변경하기로 했습니다. **3. H2 데이터베이스를 2.1.214 버전 이상 사용해주세요.**

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