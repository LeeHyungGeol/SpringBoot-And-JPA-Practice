# 2. 도메인 분석 설계

## 요구사항 분석

<img width="645" alt="스크린샷 2024-03-20 오전 2 03 50" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/c9b25e3f-5fdf-49a8-9eba-d5886bd8322d">

**기능 목록**
- 회원 기능 
  - 회원 등록 
  - 회원 조회 
- 상품 기능 
  - 상품 등록 
  - 상품 수정 
  - 상품 조회 
- 주문 기능 
  - 상품 주문 
  - 주문 내역 조회 
  - 주문 취소 
- 기타 요구사항 
  - 상품은 재고 관리가 필요하다. 
  - 상품의 종류는 도서, 음반, 영화가 있다. 상품을 카테고리로 구분할 수 있다. 
  - 상품 주문시 배송 정보를 입력할 수 있다.

## 도메인 모델과 테이블 설계

### 테이블간의 관계
<img width="650" alt="스크린샷 2024-03-20 오후 12 37 40" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/bb3c448f-5e7f-4394-b505-14a4df39fe85">

- **회원, 주문, 상품의 관계:** 회원은 여러 상품을 주문할 수 있다. 그리고 한 번 주문할 때 여러 상품을 선택할 수 있으므로 주문과 상품은 다대다 관계다. 하지만 이런 다대다 관계는 관계형 데이터베이스는 물론이고 엔티티에서도 거의 사용하 지 않는다. 따라서 그림처럼 주문상품이라는 엔티티를 추가해서 다대다 관계를 일대다, 다대일 관계로 풀어냈다.
- **상품 분류:** 상품은 도서, 음반, 영화로 구분되는데 상품이라는 공통 속성을 사용하므로 상속 구조로 표현했다.

### 회원 엔티티 분석
<img width="665" alt="스크린샷 2024-03-20 오후 12 38 06" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/47ba1af9-0ab3-4127-b2ce-8e2c62cb50a3">

- pk 값은 전부 long 타입으로 잡았다.
- address 는 임베디드 타입으로 내장 값 타입이다.
- 한번 주문시 여러개의 상품을 가질 수 있고, 상품도 여러개의 주문에 포함될 수 있다. 그래서, 주문과 상품은 다대다 (N:M) 관계이다. 
  - 다대다 관계는 일대다(1:N), 다대일(N:1) 로 풀어내야 하고, 상품을 몇개 담았는지 count 정보도 필요하고, 주문시점의 금액(orderPrice)도 알아야 하기 때문에 OrderProduct 테이블을 만들었다.
- delivery 에도 address 임베디드 타입을 재활용했다.
- 카테고리(Category): 상품과 다대다 관계를 맺는다. `parent` , `child` 로 부모, 자식 카테고리를 연결한다. **카테고리는 계층구조인 것이다.**


- jpa 로 표현할 수 있는 모든 관계를 표현했다. 
- 사실, 실무에서 사용하면 안되는 몇가지들도 있다. 
  - @ManyToMany (N:M) 도 그렇고,
  - Member 와 ORDERS 의 관계 같은 양방향 연관관계보다 단방향 연관관계가 더 좋다.
- Member(회원) 가 Order(주문) 를 하니까, Member 에 List orders 를 넣으면 되겠다고 생각하지만
  - **사실은, 시스템은 member 와 order 를 동급으로 보고, order 를 생성하는데 member 정보를 갖고 온다고 생각하는게 맞다.**

### 회원 테이블 분석
<img width="649" alt="스크린샷 2024-03-20 오후 12 38 25" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/54aa7e06-98e7-433c-aff1-b1b5e4eee9d5">

-  테이블명이 `ORDER` 가 아니라 `ORDERS` 인 것은 데이터베이스가 `order by` 때문에 예약어로 잡고 있는 경우가 많다. 그래서 관례상 `ORDERS` 를 많이 사용한다.
- **참고: 실제 코드에서는 DB에 소문자 + _(언더스코어) 스타일을 사용하겠다.**
데이터베이스 테이블명, 컬럼명에 대한 관례는 회사마다 다르다. 보통은 대문자 + _(언더스코어)나 소문자 + _(언 더스코어) 방식 중에 하나를 지정해서 일관성 있게 사용한다. 강의에서 설명할 때는 객체와 차이를 나타내기 위해 데이터베이스 테이블, 컬럼명은 대문자를 사용했지만, **실제 코드에서는 소문자 + _(언더스코어) 스타일을 사용하 겠다.**


### 연관관계 매핑 분석

- **member 와 order:** **N:1의 양방향** 관계다. 따라서 연관관계의 주인을 정해야 하는데, 외래 키가 있는 주문을 연관 관계의 주인으로 정하는 것이 좋다. 그러므로 `Order.member` 를 `ORDERS.MEMBER_ID` 외래 키와 매핑한다.
- **orderItem 과 order:** **N:1 양방향** 관계다. 외래 키가 주문상품에 있으므로 주문상품이 연관관계의 주인이다. 그러므로 `OrderItem.order` 를 `ORDER_ITEM.ORDER_ID` 외래 키와 매핑한다.
- **orderItem 과 item:** **N:1 단방향** 관계다. `OrderItem.item` 을 `ORDER_ITEM.ITEM_ID` 외래 키와 매핑한다.
  - item 을 볼때, 나를 주문한 orderItem 을 다 찾아보는 것과 같은 동작이 필요 없기 때문이다.
- **order 와 delivery:** **1:1 양방향** 관계다. `Order.delivery` 를 `ORDERS.DELIVERY_ID` 외래 키와 매핑한다.
- **category 와 item:** `@ManyToMany` 를 사용해서 매핑한다.(실무에서 @ManyToMany는 사용하지 말자. 여기서는 다대 다 관계를 예제로 보여주기 위해 추가했을 뿐이다)

### 외래 키가 있는 곳을 연관관계의 주인으로 정해라.
**연관관계의 주인은 단순히 외래 키를 누가 관리하냐의 문제이지 비즈니스상 우위에 있다고 주인으로 정하면 안된다.** 
- 예를 들어서 team와 member가 있으면, 일대다(1:N) 관계에서 항상 다(N)쪽에 외래 키가 있으므로 외래 키가 있는 member를 연관관계의 주인으로 정하면 된다. 
- team를 연관관계의 주인으로 정하면 team가 관리하지 않는 **member 테이블의 외래 키 값이 업데이트 되므로 관리와 유지보수가 어렵고, 추가적으로 별도의 업데이트 쿼리가 발생하는 성능 문제도 있다.**

## 엔티티 클래스 개발
- 예제에서는 설명을 쉽게하기 위해 엔티티 클래스에 Getter, Setter를 모두 열고, 최대한 단순하게 설계 
- 실무에서는 가급적 Getter는 열어두고, Setter는 꼭 필요한 경우에만 사용하는 것을 추천 

> 참고: 이론적으로 Getter, Setter 모두 제공하지 않고, 꼭 필요한 별도의 메서드를 제공하는게 가장 이상적이다.
> 하지만 실무에서 엔티티의 데이터는 조회할 일이 너무 많으므로, Getter의 경우 모두 열어두는 것이 편리하다.
> Getter는 아무리 호출해도 호출 하는 것 만으로 어떤 일이 발생하지는 않는다. 하지만 Setter는 문제가 다르다.
> Setter를 호출하면 데이터가 변한다. Setter를 막 열어두면 가까운 미래에 엔티티가 도대체 왜 변경되는지 추적 하기 점점 힘들어진다.
> 그래서 엔티티를 변경할 때는 Setter 대신에 변경 지점이 명확하도록 변경을 위한 비즈니스 메서드를 별도로 제공해야 한다.

- `@JoinColumn(name = "member_id")` 은 mapping 을 무엇으로 할 것이냐인데, FK(Foreign Key)인 것이다. 여기서는 FK 이름이 `"member_id"` 가 되는 것이다.

### *연관관계의 주인이 필요한 이유!!!!!(제일 중요)*

```java
@Entity
@Getter @Setter
public class Member {
    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();
}

@Entity
@Table(name = "orders")
@Getter @Setter
public class Order {

    @Id
    @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;
}
```

- Member 와 order 의 관계를 변경하고 싶으면, 이때, JPA 는 FK(member_id) 를 변경해야 한다. 
- 하지만, Member 에 있는 orders 를 변경해야 하는지, order 에 있는 members 를 변경해야 하는지 혼동이 온다.
- 이 FK 를 update 하는 변경점을 둘 중에 하나로만 하도록 JPA 에서 약속을 했다.
- **객체는 변경 포인트가 2군데지만, 테이블은 FK 하나만 변경하면 된다.**
- **둘 중에 하나를 주인이라고 잡고, 하나를 변경했을 때 FK 값을 변경해줄거야 라고 지정한다.** 
- 그것을 **연관관계의 주인**이라고 한다.
- 주인은 그대로 두면 되고, 반대편에 있는 Member.orders 를 **연관관계의 거울**이라고 표시하는 **mappedBy**로 `@OneToMany(mappedBy = "member")` 이라고 표시한다.
  - `mappedBy = "member"` 에서의 member 는 `Order.member` 에서의 member 필드이다.

### OneToOne 일대일 관계에서의 연관관계의 주인 정리

![OneToOne order-delivery](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/c0e8b6eb-fc80-48ba-be71-6468eec01ba2)

![OneToOne order-delivery-entity](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/621697ef-0085-4c3f-8773-a040e2f2b6ee)

```java
@Entity
@Table(name = "orders")
@Getter @Setter
public class Order {
    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;
}

@Entity
@Getter @Setter
public class Delivery {
  @Id @GeneratedValue
  @Column(name = "delivery_id")
  private Long id;

  @OneToOne(mappedBy = "delivery")
  private Order order;
}
```

- 일대일 관계에서는 FK 를 A 에 둬도 되고, B 에 둬도 된다.
- 어디에 두냐에 따라 장단점이 있다.
- 주로 data access 하는 곳에 FK 를 두자.
  - EX) delivery, order 에서 delivery 를 직접 조회하는 것 보다 항상 order 를 보면서 delivery 를 본다면 FK 를 order 에 두는 것이 맞다.
  - FK 를 delivery 에 줘도 되고, order 에 둬도 되긴 하다.
  - order 에 FK 를 두기로 결정했기 때문에, FK 가 있는 order 가 연관관계의 주인이 되고,
    - ***연관관계의 주인***이 되는 `Order.delivery` 에 `@JoinColumn(name = "delivery_id")` 을 설정한다. 
    - ***연관관계의 거울***이 되는`Delivery.order` 에 `@OneToOne(mappedBy = "delivery")` 를 설정한다.