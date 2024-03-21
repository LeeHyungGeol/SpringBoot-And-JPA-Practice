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

### 💡외래 키가 있는 곳을 연관관계의 주인으로 정해라.⭐️
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

### ⭐️*연관관계의 주인이 필요한 이유!!!!!(제일 중요)*

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

### ⭐️OneToOne 일대일 관계에서의 연관관계의 주인 정리

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

### ⭐️Category 계층구조 

- db 계층 카테고리 설계
- https://annahxxl.tistory.com/5
- jpa 계층형 카테고리
- https://velog.io/@tjddnths0223/%EC%8A%A4%ED%94%84%EB%A7%81%EB%B6%80%ED%8A%B8JPAqueryDsl-%EB%8C%80%EB%8C%93%EA%B8%80%EA%B3%84%EC%B8%B5%ED%98%95-%EA%B5%AC%ED%98%84


![Category 계층구조 entity](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/142063e6-941d-401f-a64e-73e1fe9c7329)

![Category 계층구조 table](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/491a8079-60d4-4569-b9e8-e1b2b1bb2704)

```java
@Entity
@Getter @Setter
public class Category {
    @Id @GeneratedValue
    @Column(name = "category_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent")
    private List<Category> childs = new ArrayList<>();
}
```

- Category 구조라는 것은 계층 구조로 쭉 내려가는 것을 말한다.
- 즉, 부모(parent)로도 조회할 수 있어야 하고, 자기 자신의 자식들도 알고 있어야 한다.
- 이름만 Category 로 똑같지 다른 엔티티와 다대일 양방향 연관관계를 mapping 하는 것과 똑같이 하면 된다.

### ⭐값 타입은 변경 불가능하게 설계해야 한다.

```java
@Embeddable
@Getter
public class Address {
  private String city;
  private String street;
  private String zipcode;

  protected Address() {
  }

  public Address(String city, String street, String zipcode) {
    this.city = city;
    this.street = street;
    this.zipcode = zipcode;
  }
}
```

- `@Setter` 를 제거하고, 생성자에서 값을 모두 초기화해서 변경 불가능한 클래스를 만들자. 
- **JPA 스펙상** 엔티티나 임베디드 타입(`@Embeddable`)은 자바 기본 생성자(default constructor)를 `public` 또는 `protected` 로 설정해야 한다. 
- `public` 으로 두는 것 보다는 `protected` 로 설정하는 것이 그나마 더 안전하다.
  - 다른 곳에서 마음대로 호출해서 생성하지 못하도록 하기 위함이다.
  - 값 타입을 누가 따로 상속할 일도 잘 없다.   
- JPA가 이런 제약을 두는 이유는 JPA 구현 라이브러리가 객체를 생성할 때 리플랙션(reflection), 프록시(proxy) 같은 기술을 사용할 수 있도 록 지원해야 하기 때문이다.

## 엔티티 설계시 주의점

### 엔티티에는 가급적 Setter를 사용하지 말자
- Setter가 모두 열려있다. 변경 포인트가 너무 많아서, 유지보수가 어렵다.

### ⭐모든 연관관계는 지연로딩으로 설정!
- 즉시로딩(`EAGER`)은 예측이 어렵고, 어떤 SQL이 실행될지 추적하기 어렵다. 특히 **`em.find()` 로 할 때는 inner join 을 통해 한번에 가져오지만, JPQL 같은 쿼리로 조회할 시에는 N+1 문제가 자주 발생한다!!!**
- 실무에서 모든 연관관계는 지연로딩(`LAZY`)으로 설정해야 한다. 
- 연관된 엔티티를 함께 DB 에서 조회해야 하면, fetch join 또는 엔티티 그래프(`@EntityGraph`) 기능을 사용한다. 
- @XToOne(OneToOne, ManyToOne) 관계는 기본이 즉시로딩이므로 직접 지연로딩으로 설정해야 한다.
- Lazy Loading 이 Transaction 밖에서 예외가 발생하긴 하지만, 대안이 있다.
  - ⁉️트랜잭션을 좀 빨리 가져온다거나, 상황에 따라서는 open session in view(OSIV) 를 사용할 수 있다.⁉️

### ⭐컬렉션은 필드에서 초기화 하자.

컬렉션을 초기화 할 때 2가지 방법이 있다.
- 아래 예시처럼 `private List<Order> orders = new ArrayList<>();` 선언과 함께 초기화할 수 있고,
- `orders = new ArrayList<>();` 처럼 생성자나 다른 메서드를 통해서 초기화 할 수도 있다.

```java
import java.util.ArrayList;

@Entity
@Getter
@Setter
public class Member {

  @OneToMany(mappedBy = "member")
  private List<Order> orders = new ArrayList<>();

  public Member() {
    orders = new ArrayList<>();
  }
}
```

***컬렉션은 필드에서 바로 초기화 하는 것이 안전하다.***
- 초기화에 대해서 고민을 안해도 된다.
- `null` 문제에서 안전하다.
- 선언할 때 `new ArrayList<>();` 해놓는다고 해도 memory 를 별로 잡아먹지도 않는다.
- 하이버네이트는 엔티티를 영속화 할 때, **컬랙션을 감싸서 하이버네이트가 제공하는 내장 컬렉션으로 변경한다.**
  - 영속화할 때, 기존 ArrayList 를 들고 hibernate 가 PersistentBag(ArrayList 일때, type 에 따라 다르다) 로 감싸 버리는 것이다.
  - Hibernate 가 변경된 것을 추적해야 하기 때문에, 추적할 수 있는 본인의 내장 컬렉션으로 감싸는 것이다.
- 만약 `getOrders()` 처럼 임의의 메서드에서 컬력션을 잘못 생성하면 **하이버네이트 내부 메커니즘에 문제가 발생 할 수 있다.** 
  - **따라서 필드 레벨에서 생성하는 것이 가장 안전하고, 코드도 간결하다.**

```java
public static void main(String[] args) {
  Member member = new Member();
  System.out.println(member.getOrders().getClass());
  em.persist(member);
  System.out.println(member.getOrders().getClass());
}
```

```
//출력 결과 
class java.util.ArrayList
class org.hibernate.collection.internal.PersistentBag
```


### 테이블, 컬럼명 생성 전략
스프링 부트에서 하이버네이트 기본 매핑 전략을 변경해서 실제 테이블 필드명은 다름
- https://docs.spring.io/spring-boot/docs/2.1.3.RELEASE/reference/htmlsingle/#howto-configure-hibernate-naming-strategy 
- http://docs.jboss.org/hibernate/orm/5.4/userguide/html_single/

Hibernate_User_Guide.html#naming
하이버네이트 기존 구현: 엔티티의 필드명을 그대로 테이블의 컬럼명으로 사용 ( `SpringPhysicalNamingStrategy` )
스프링 부트 신규 설정 (엔티티(필드) 테이블(컬럼))
1. 카멜 케이스 언더스코어(memberPoint member_point)
2. .(점) _(언더스코어)
3. 대문자 소문자

### **적용 2 단계**
- 논리명: 명시적으로 테이블이나 컬럼명을 지정하지 않으면 어떻게 생성해줄지의 전략
- 물리명: 테이블명이나 컬럼명이 적혀있든 적혀있지 않든 모든 논리명에 적용이 된다.

1. 논리명 생성: 명시적으로 컬럼, 테이블명을 직접 적지 않으면 ImplicitNamingStrategy 사용
   - `spring.jpa.hibernate.naming.implicit-strategy` : 테이블이나, 컬럼명을 명시하지 않을 때 논리명 적용,
2. 물리명 적용:
   - `spring.jpa.hibernate.naming.physical-strategy` : 모든 논리명에 적용됨, 실제 테이블에 적용 (username -> usernm 등으로 회사 룰로 바꿀 수 있음)

**스프링 부트 기본 설정** 
`spring.jpa.hibernate.naming.implicit-strategy: org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy` 
`spring.jpa.hibernate.naming.physical-strategy: org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy`


### ⭐️연관관계 편의 메소드

테이블의 관점에서는 연관관계의 주인인 FK 가 있는 엔티티에 객체를 세팅해주면 되지만, 객체간에 서로 왔다갔다 하기 위해서는 양쪽에 값을 세팅해주는게 좋다.

```java
@Entity
@Table(name = "orders")
@Getter @Setter
public class Order {
  @Id @GeneratedValue
  @Column(name = "order_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private Member member;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
  private List<OrderItem> orderItems = new ArrayList<>();

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "delivery_id")
  private Delivery delivery;

  private LocalDateTime orderDate;

  private OrderStatus status; // 주문상태: [ORDER, CANCEL]

  // 연관관계 편의 메서드
  public void setMember(Member member) {
    this.member = member;
    member.getOrders().add(this);
  }

  public void addOrderItem(OrderItem orderItem) {
    orderItems.add(orderItem);
    orderItem.setOrder(this);
  }

  public void setDelivery(Delivery delivery) {
    this.delivery = delivery;
    delivery.setOrder(this);
  }
}

@Entity
@Getter @Setter
public class Category {
    @Id @GeneratedValue
    @Column(name = "category_id")
    private Long id;
    private String name;

    @ManyToMany
    @JoinTable(name = "category_item",
        joinColumns = @JoinColumn(name = "category_id"),
        inverseJoinColumns = @JoinColumn(name = "item_id"))
    private List<Item> items = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent")
    private List<Category> child = new ArrayList<>();

    // 연관관계 편의 메서드
    public void addChildCategory(Category child) {
        this.child.add(child);
        child.setParent(this);
    }
}
```

- 연관관계가 양방향일 때 세팅해주면 좋다.
- 양쪽 세팅하는 것을 원자적으로 하나의 메서드로 편하게 관리하는 것이다.

### 💡Setter 와 연관관계 편의 메서드

-  Setter 를 가급적 사용하지 않고, DTO 를 사용하는 실무 상황에서는 연관관계 편의메서드를 어디에 두어야 할까?

> **Setter를 최대한 제거하도록 노력하는 것이 좋다. 하지만 엔티티를 꼭 변경해야 한다면 Setter를 사용하거나 별도의 데이터를 변경하는 메서드는 명시적으로 필요합니다. 그래서 결국 연관관계 메서드는 엔티티에 존재하는 것이 맞습니다.** 

- 연관관계 편의 메서드는 어디에 두는게 맞을까?

> **비즈니스 로직을 개발할 때 자주 사용하게 되는 중심이 되는 엔티티가 있는데, 이곳에 연관관계 편의 메서드를 넣어두자.**

- 연관관계 메서드는 어떤 방식으로 만들어야 할까? 

> **setter 보다는 생성자나 빌더를 사용할 수 있다면 그것이 더 나은 방법입니다.**

- setter 는 그럼 어떨 때에 제공해야 할까?

> **어쩔 수 없이 setter 를 제공해야 하는 경우에는 setter 를 제공해야 한다. 데이터를 꼭 변경해야 할 필요가 있는 경우에는 부분적으로 setter 를 열어두어도 괜찮다. 주의할 점은 무분별하게 setter를 열어두는 것이다.**


