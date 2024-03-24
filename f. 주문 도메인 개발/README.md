# 6. 주문 도메인 개발

## Index

## 주문, 주문상품 엔티티 개발

Item.class
```java
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
@Getter
public abstract class Item {
    @Id @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    private String name;
    private int price;
    private int stockQuantity;

    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();

    // 비즈니스 로직

    /**
     * stock 증가
     */
    public void addStock(int stockQuantity) {
        this.stockQuantity += stockQuantity;
    }

    public void removeStock(int stockQuantity) {
        int restStock = this.stockQuantity - stockQuantity;
        if (restStock < 0) {
           throw new NotEnoughStockException("need more stock");
        }
        this.stockQuantity = restStock;
    }
}
```

OrderItem.class
```java
@Entity
@Getter @Setter
public class OrderItem {
  @Id @GeneratedValue
  @Column(name = "order_item_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "item_id")
  private Item item;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id")
  private Order order;

  private int orderPrice; // 주문 가격
  private int count; // 주문 수량

  // 생성 메서드
  public static OrderItem createOrderItem(Item item, int count, int orderPrice) {
    OrderItem orderItem = new OrderItem();
    orderItem.setItem(item);
    orderItem.setCount(count);
    orderItem.setOrderPrice(orderPrice);

    item.removeStock(count);
    return orderItem;
  }

  // 비즈니스 로직
  /**
   * 재고 수량 원복
   */
  public void cancel() {
    getItem().addStock(count);
  }

  public int getTotalPrice() {
    return getOrderPrice() * getCount();
  }
}

```

Order.class
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

    // 생성 메서드
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) {
        Order order = new Order();
        order.setMember(member);
        order.setDelivery(delivery);
        Arrays.stream(orderItems).forEach(order::addOrderItem);
        order.setStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());
        return order;
    }

    // 비즈니스 로직
    /**
     * 주문 취소
     */
    public void cancel() {
        if (delivery.getStatus() == DeliveryStatus.COMP) {
            throw new IllegalStateException("이미 배송완료된 상품은 취소가 불가능합니다.");
        }
        this.setStatus(OrderStatus.CANCEL);
        for (OrderItem oi: orderItems) {
            oi.cancel();
        }
//        orderItems.forEach(OrderItem::cancel);
    }

    // 조회 로직
    /**
     * 전체 주문 가격 조회
     */
    public int getTotalPrice() {
        return orderItems.stream().mapToInt(OrderItem::getOrderPrice).sum();
    }
}
```

- **생성 메서드**(`createOrder()`): 주문 엔티티를 생성할 때 사용한다. 주문 회원, 배송정보, 주문상품의 정보를 받아서 실제 주문 엔티티를 생성한다.
  - ***주문생성에 대한 복잡한 비즈니스로직을 한 곳에서 응집해서 완결해버린다.***
- **주문 취소**(`cancel()`): 주문 취소시 사용한다. 주문 상태를 취소로 변경하고 주문상품에 주문 취소를 알린다. 만 약 이미 배송을 완료한 상품이면 주문을 취소하지 못하도록 예외를 발생시킨다.
- **전체 주문 가격 조회(`getTotalPrice()`):** 주문 시 사용한 전체 주문 가격을 조회한다. 전체 주문 가격을 알려면 각각의 주문상품 가격 을 알아야 한다. 로직을 보면 연관된 주문상품들의 가격을 조회해서 더한 값을 반환한다. (실무에서는 주로 주문에 전체 주문 가격 필드를 두고 역정규화 한다.)

### ⭐Entity 필드 직접 접근 or Getter 이용?

OrderItem.class
```java
public void cancel() {
    getItem().addStock(count);
}

public int getTotalPrice() {
    return getOrderPrice() * getCount();
}
```

- count 는 필드 직접 접근
- 나머지는 getter 사용
- 저기서 count 대신에 getCount()를 호출한 것은 사실 아무 의미가 없다.

> 객체 외부에서는 당연히 필드에 직접 접근하면 안되겠지만, 객체 내부에서는 필드에 직접 접근해도 아무 문제가 없습니다. 번거롭게 getXxx를 호출하는 것 보다는 필드를 직접 호출하는 것이 코드도 더 깔끔하고요. 
그래서 저도 필드에 직접 접근하는 방법을 주로 사용합니다. 저기서 count 대신에 getCount()를 호출한 것은 사실 아무 의미가 없다.
>
> 그런데! 사실은 객체 내부에서 필드에 직접 접근하는가, 아니면 getter 를 통해서 접근하는가가 **JPA 프록시(proxy)를** 많이 다루게 되면 **중요해집니다.** 
> **일반적으로 이런 상황을 겪을일은 거의 없지만, 조회한 엔티티가 프록시 객체인 경우 필드에 직접 접근하면 원본 객체를 가져오지 못하고, 프록시 객체의 필드에 직접 접근해버리게 됩니다.**
> 이게 일반적인 상황에는 문제가 없는데, equals(), hashCode() 를 JPA 프록시 객체로 구현할 때 문제가 될 수 있습니다.
프록시 객체의 equals를 호출했는데 거기서 필드에 직접 접근하면, 프록시 객체는 필드에 값이 없으므로 항상 null이 반환됩니다. 
> 
> 그래서 JPA 엔티티에서 equals(), hashCode() 를 구현할 때는 getter 를 내부에서 사용해야 합니다.

- JPA 책 15.3.3 프록시 동등성 비교

### ⭐️왜 주문은 OrderRepository, OrderItemRepository 가 따로 없고, Order 엔티티를 통해서 관리했나? feat. Aggregate Root(어그리게이트 루트)

- Order가 OrderItem을 관리하도록 설계했습니다.
- OrderItem을 저장하거나 관리하려면 별도의 리포지토리가 아니라, 항상 Order를 통해서 관리하는 하도록 설계를 제약했습니다.
- 개념상 Order, OrderItem을 하나로 묶고(Aggregate), Order를 통해서만 OrderItem에 접근하게 강제했습니다.

> 이렇게 설계를 하면 외부에서는 Order, OrderItem 중에 Order만 알면 되기 때문에, 도메인을 좀 더 덜 복잡하게 설계할 수 있습니다. 이렇게 그룹을 대표하는 엔티티를 도메인 주도 설계(DDD)에서는 aggregate root(에그리게잇 루트) 엔티티라 합니다. 
> 이제 OrderItem의 생명주기는 모두 Order에 달려 있습니다. 심지어 OrderItem은 리포지토리도 없습니다. 
> 모두 Order를 통해서 관리되는 것이지요. 물론 이런 생명주기는 Cascade 기능을 통해서 관리됩니다.

### 💡JPA는 동시성 문제를 해결하기 위해 낙관적 락과 비관적 락 2가지 방식을 제공
- 자바 ORM표준 JPA 프로그래밍 책 16.1 트랜잭션과 락 부분

## 주문 리포지토리 개발 (OrderRepository)

```java
@Repository
@RequiredArgsConstructor
public class OrderRepository {
    private final EntityManager em;

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

//    public List<Order> findAll(OrderSearch orderSearch) {}
}
```

## 주문 서비스 개발 (OrderService)

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

    /**
     * 주문
     */
    @Transactional
    public Long order(Long memberId, Long itemId, int count) {
        // 엔티티 조회
        Member member = memberRepository.find(memberId);
        Item item = itemRepository.find(itemId);

        // 배송정보 생성
        Delivery delivery = new Delivery();
        delivery.setAddress(member.getAddress());

        // 주문상품 생성
        OrderItem orderItem = OrderItem.createOrderItem(item, count, item.getPrice());

        // 주문 생성
        Order order = Order.createOrder(member, delivery, orderItem);

        // 주문 저장
        orderRepository.save(order);
        return order.getId();
    }

    /**
     * 주문 취소
     */
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findOne(orderId);
        order.cancel();
    }

    /**
     * 검색
     */
//    public List<Order> findOrders(OrderSearch orderSearch) {
//        return orderRepository.findAll(orderSearch);
//    }
}
```

### ⭐️정적 팩토리 패턴으로 생성매서드 같은 것을 사용할 떄 기본 생성자을 protected 로 막아둬서 무분별한 사용을 방지하자!!!

```java
import lombok.NoArgsConstructor;
@NoArgsConstructor(access = AccessLevel.PROTECTED)
```

- 이렇게 설정해놓으면 다른 곳에서(service 등) 기본 생성자로 무분별한 객체 생성을 방지할 수 있다.
- 항상 이렇게 제한적으로 코드 짠다면 항상 좋은 설계와 유지 보수를 할 수 있다.

### ⭐️Transactional Script 와 JPA 의 Dirty Checking (변경감지)

db sql 을 직접 다루 MyBatis, JDBC 템플릿 같은 경우는 데이터를 변경 할 때마다 직접 update, delete 같은 sql 문들을 직접 날려야 한다. 
그래서, 서비스 계층에서 비즈니스 로직을 작성할 수 밖에 없다. 이를 트랜잭셔널 스크립트라고 한다.
하지만, JPA 를 활용하면 엔티티의 데이터가 변경되면 JPA 가 알아서 dirty checking (변경감지)를 해서 db 에 sql 쿼리문들을 다 날려준다.
이것이 JPA 의 엄청난 장점이다.

### ⭐️도메인 모델 패턴과 트랜잭션 스크립트 패턴

주문 서비스의 주문과 주문 취소 메서드를 보면 비즈니스 로직 대부분이 엔티티에 있다. 
서비스 계층은 단순히 엔티티에 필요한 요청을 위임하는 역할을 한다. 이처럼 엔티티가 비즈니스 로직을 가지고 객체 지향의 특성을 적극 활용하는 것을 **도메인 모델 패턴**(http://martinfowler.com/eaaCatalog/domainModel.html)이라 한다. 
반대로 엔티티에는 비즈니스 로직이 거의 없고 서비스 계층에서 대부분의 비즈니스 로직을 처리하는 것을 **트랜잭션 스크립트 패턴**(http://martinfowler.com/eaaCatalog/transactionScript.html)이라 한다.

## 주문 기능 테스트

```java
@SpringBootTest
@Transactional
public class OrderServiceTest {

    @Autowired EntityManager em;
    @Autowired OrderService orderService;
    @Autowired OrderRepository orderRepository;

    @Test
    public void 상품주문() throws Exception {
        // given
        Member member = createMember("member");
        Book book = createBook("JPA", 10000, 10);

        int orderCount = 2;

        // when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        // then
        Order findOrder = orderRepository.findOne(orderId);

        assertEquals(OrderStatus.ORDER, findOrder.getStatus(), "상품 주문시 상태는 ORDER");
        assertEquals(1, findOrder.getOrderItems().size(), "주문한 상품 종류 수가 정확해야 한다.");
        assertEquals(10000*orderCount, findOrder.getTotalPrice(), "주문 가격은 가격*수량 이다.");
        assertEquals(8, book.getStockQuantity(), "주문 수량 만큼 재고가 줄어야 한다.");
    }

    @Test
    public void 주문취소() throws Exception {
        // given
        Member member = createMember("member");
        Book book = createBook("시골 JPA", 10000, 10);
        int orderCount = 2;

        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);
        // when
        orderService.cancelOrder(orderId);
        // then
        Order getOrder = orderRepository.findOne(orderId);
        assertEquals(OrderStatus.CANCEL, getOrder.getStatus(), "주문 취소시 상태는 CANCEL 이다.");
        assertEquals(10, book.getStockQuantity(), "주문이 취소된 상품은 그만큼 재고가 증가해야 한다.");
    }
    
    @Test
    public void 상품주문_재고수량초과() throws Exception {
        // given
        Member member = createMember("member");
        Book book = createBook("JPA", 10000, 10);
        int orderCount = 11;

        // when
        NotEnoughStockException notEnoughStockException = assertThrows(
            NotEnoughStockException.class,
            () -> orderService.order(member.getId(), book.getId(), orderCount));

        // then
        assertEquals(notEnoughStockException.getMessage(), "재고가 부족합니다.");
    }

    private Book createBook(String name, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        return book;
    }

    private Member createMember(String name) {
        Member member = new Member();
        member.setName(name);
        member.setAddress(new Address("서울시", "능동로", "17길 12"));
        em.persist(member);
        return member;
    }
}
```