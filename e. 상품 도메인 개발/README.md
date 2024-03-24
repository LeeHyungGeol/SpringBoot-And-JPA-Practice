# 5. 상품 도메인 개발

## Index

## 상품 엔티티 개발(비즈니스 로직 추가)

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

### ⭐️객체지향적인 코드, 캡슐화
setter 메소드로 재고 수량을 조절하는게 아니라 addStock 메서드, removeStock 메서드를 이용해 재고 수량을 조절하는 것이 객체지향적이다. 그래면 **응집력**도 있다.

- 참고: https://velog.io/@kshired/%EA%B0%9D%EC%B2%B4-%EC%A7%80%ED%96%A5-%ED%94%84%EB%A1%9C%EA%B7%B8%EB%9E%98%EB%B0%8D-%EC%9E%85%EB%AC%B8-%EC%BA%A1%EC%8A%90%ED%99%94


### ⭐️객체를 생성하는 방법과 Setter
객체를 생성할 때는 여러가지 방법이 있다.

1. 생성자(단순히 new 를 사용해서 생성하는 방법)
2. 💡정적 팩토리 메서드
3. 💡Builder 패턴
4. 별도의 생성 클래스를 사용하는 방법 등등

(일반적으로 갈수록 복잡도가 높아집니다.)

> 엔티티에 따라 이 방법중 상황에 따라서 하나를 선택하고, 파라미터에 객체 생성에 필요한 데이터를 다 넘기는 방법을 사용한다. 
그리고 정적 팩토리 메서드나, Builder 패턴을 사용할 때는 생성자를 private 처리한다. 
객체 생성이 간단할 때는 단순히 생성자를 사용하고, 만약 객체 생성이 복잡하고, 의미를 가지는 것이 좋다면 나머지 방법 중 하나를 선택한다.
> 
> 하지만 거의 사용하지 않는 정말 단순한 엔티티라면 자바가 기본으로 제공하는 new 를 사용하는게 더 좋을 것이다. 이렇게 한 프로젝트 안에서도 객체를 생성하는데 드는 복잡도에 따라서 다양한 선택을 해야한다. 
> 이럴때 **가장 추천하는 방법은 가장 단순한 방법을 사용하고, 필요하면 리펙토링 한다 입니다.**

- 그러면 setter가 없는데, 엔티티를 어떻게 수정할까?

> 이것은 setter를 만들기 보다는 의미있는 변경 메서드 이름을 사용합니다. 예를 들어서 고객의 등급이 오른다면 member.levelUp() 같은 메서드가 있을 것이다. 이 메서드는 내부의 필드 값을 변경할 것이다.
외부에 어떤 것을 공개할지 객체 생성을 목적으로 하는 것이기 때문에 외부에 한가지 방식만 제공하면 된다.
추가로 setter를 어느정도는 허용하셔도 된다.(단 관리를 잘 하여야 한다.) 연관관계를 양방향으로 적용하시려면 필연적으로 들어가야 할 때도 있다.

## 상품 리포지토리 개발

```java
@Repository
@RequiredArgsConstructor
public class ItemRepository {
    private final EntityManager em;

    public void save(Item item) {
        if (item.getId() == null) {
            em.persist(item);
        } else {
            em.merge(item);
        }
    }

    public Item find(Long itemId) {
        return em.find(Item.class, itemId);
    }

    public List<Item> findAll() {
        return em.createQuery("select i from Item i", Item.class)
            .getResultList();
    }
}
```

## 상품 서비스 개발

```java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    @Transactional
    public void save(Item item) {
        itemRepository.save(item);
    }

    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    public Item findOne(Long itemId) {
        return itemRepository.find(itemId);
    }
}
```