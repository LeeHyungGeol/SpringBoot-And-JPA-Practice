# 3. 애플리케이션 구현 준비

## 💡숙제(구현되지 않은 기능들)

- 로그인과 권한 관리X
- 파라미터 검증과 예외 처리X 
- 상품은 도서만 사용 
- 카테고리는 사용X 
- 배송 정보는 사용X

## 애플리케이션 아키텍처

![JPA 애플리케이션 아키텍쳐](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/591b837a-86d6-446a-b88e-b8749904ed42)

**계층형 구조 사용**
- controller, web: 웹 계층
- service: 비즈니스 로직, 트랜잭션 처리
- repository: JPA를 직접 사용하는 계층, 엔티티 매니저 사용 
- domain: 엔티티가 모여 있는 계층, 모든 계층에서 사용

**패키지 구조** 
- jpabook.jpashop 
  - domain 
  - exception 
  - repository 
  - service 
  - web