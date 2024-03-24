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
> 이제 OrderItem의 생명주기는 모두 Order에 달려 있습니다. 심지어 OrderItem은 리포지토리도 없습니다. 모두 Order를 통해서 관리되는 것이지요. 물론 이런 생명주기는 Cascade 기능을 통해서 관리됩니다.

### 💡JPA는 동시성 문제를 해결하기 위해 낙관적 락과 비관적 락 2가지 방식을 제공
- 자바 ORM표준 JPA 프로그래밍 책 16.1 트랜잭션과 락 부분
