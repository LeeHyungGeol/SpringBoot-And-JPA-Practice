# 7. 웹 계층 개발

## Index

## 변경 감지와 병합

**준영속 엔티티?**
- **영속성 컨텍스트가 더는 관리하지 않는 엔티티를 말한다.**
(여기서는 `itemService.saveItem(book)` 에서 수정을 시도하는 `Book` 객체다. `Book` 객체는 이미 DB에 한번 저장되어서 식별자가 존재한다. 
이렇게 임의로 만들어낸 엔티티도 기존 식별자를 가지고 있으면 준영속 엔티티로 볼 수 있다.)
- ***핵심은 식별자를 기준으로 영속상태가 되어서 DB에 저장된 적이 있는가로 보시면 됩니다.***
  - 그래서 식별자를 기준으로 이미 한번 영속상태가 되어버린 엔티티가 있는데, 더이상 영속성 컨텍스트가 관리하지 않으면 모두 준영속 상태입니다. 
  - 그게 em.detach()를 해서 직접적으로 준영속 상태가 될 수도 있고, 
  - 지금처럼 수정을 위해 html form에 데이터를 노출한 이후에 다시 new로 재조립된 엔티티일 수 도 있습니다. 
  - 심지어 다른 원격지 서버에 해당 엔티티를 네트워크로 전송할 수 도 있겠지요. 이 경우 원격지 서버에 도착한 엔티티는 영속성 컨텍스트에서 관리할 수 없기 때문에 준영속 상태라고 합니다. 
    - 그런데 생각해보면 원격지 서버에서 엔티티를 복구할 때도 내부에서는 new 라는 명령어를 사용하겠지요? 이때도 준영속 상태라고 합니다.

**강의 내 book 엔티티는 아래와 같은 흐름**
수정하려는 엔티티의 키가 10이라고 했을 때,

1. DB에는 아직 수정 전인 엔티티가 들어있음. 이건 영속.

2. Book book = new Book(); 한 후 BookForm의 정보들로 set해줌. 이건 그냥 함수 내에서 new로 만들었을 뿐이니 JPA가 관리하고 있지 않음. 하지만 이 book의 키값인 10은 디비에 저장되어있음. 그래서 이건 준영속.
   - (즉, 10이란 키값을 갖는 엔티티에 대해 영속 엔티티와 준영속 엔티티가 동시에 존재하는 상황.)

3. 여기서 준영속 엔티티 book의 key값으로 검색하여 영속 엔티티 findItem을 가져오고 값을 덮어씌움.

4. 모든 작업 이후에도 book은 여전히 준영속 엔티티이므로 더이상 사용하지 않는 것이 좋음.

5. 결국 더티체킹 메서드를 직접 만들든, em.merge()를 사용하든 내부적으로는 전부 더티체킹을 사용하여 update하는 것임.(null 업데이트 문제는 제쳐두고)

**준영속 엔티티를 수정하는 2가지 방법** 
1. 변경 감지 기능 사용 
2. 병합(`merge`) 사용

### 변경 감지 기능 사용

```java
@Transactional
public void updateItem(Long itemId, Book param) {
    Item findItem = itemRepository.find(itemId);
    findItem.setName(param.getName());
    findItem.setPrice(param.getPrice());
    findItem.setStockQuantity(param.getStockQuantity());
}
```
영속성 컨텍스트에서 엔티티를 다시 조회한 후에 데이터를 수정하는 방법
- 트랜잭션 안에서 엔티티를 다시 조회, 변경할 값 선택 트랜잭션 커밋 시점에 변경 감지(Dirty Checking)이 동작해서 데이터베이스에 UPDATE SQL 실행

### 병합 사용
병합은 준영속 상태의 엔티티를 영속 상태로 변경할 때 사용하는 기능이다. 
```java
@Transactional
void update(Item itemParam) { //itemParam: 파리미터로 넘어온 준영속 상태의 엔티티
    Item mergeItem = em.merge(itemParam);
    // itemParam 은 그대로 준영속, mergeItem 이 영속상태이다.
}
```

### 병합의 작동 원리

![병합(em merge()) 동작 방식](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/8a5147ab-4d6e-4094-afe2-73a57abb74b5)

1. `merge()` 를 실행한다.
2. 파라미터로 넘어온 준영속 엔티티의 식별자 값으로 1차 캐시에서 엔티티를 조회한다. 
   - 2-1. 만약 1차 캐시에 엔티티가 없으면 데이터베이스에서 엔티티를 조회하고, 1차 캐시에 저장한다.
3. 조회한 영속 엔티티(`mergeMember`)에 `member` 엔티티의 값을 채워 넣는다.(병합한다.) 
   - (member 엔티티의 모든 값을 mergeMember에 밀어 넣는다. 이때 mergeMember 의 “회원1” 이라는 이름이 “회원명변경”으로 바뀐다.)
4. 영속 상태인 mergeMember를 반환한다. (`Item mergeItem = em.merge(itemParam);`)
5. 트랜잭션 커밋 시점에 변경 감지 기능이 동작해서 데이터베이스에 UPDATE SQL이 실행



### 병합 사용시 주의사항 - 그냥 병합은 쓰지 말자!!!

- 변경 감지 기능을 사용하면 원하는 속성만 선택해서 변경할 수 있지만, 
- ***병합을 사용하면 모든 속성이 변경된다. 병합시 값이 없으면 `null` 로 업데이트 할 위험도 있다.(병합은 모든 필드를 교체한다.)***
- 실무에서는 보통 업데이트 기능이 매우 제한적이다. 
- **그런데 병합은 모든 필드를 변경해버리고, 데이터가 없으면 `null` 로 업데이트 해버린다.** 
- 병합을 사용하면서 이 문제를 해결하려면, 변경 폼 화면에서 모든 데이터를 항상 유지해야 한다. 
- **실무에서는 보통 변경가능한 데이터만 노출하기 때문에, 병합을 사용하는 것이 오히려 번거롭다.**

### 가장 좋은 해결 방법
**엔티티를 변경할 때는 항상 변경 감지를 사용하세요**
- 컨트롤러에서 어설프게 엔티티를 생성하지 마세요. 
- 트랜잭션이 있는 서비스 계층에 식별자( `id` )와 변경할 데이터를 명확하게 전달하세요.(파라미터 or dto) 
- 트랜잭션이 있는 서비스 계층에서 영속 상태의 엔티티를 조회하고, 엔티티의 데이터를 직접 변경하세요. 
- 트랜잭션 커밋 시점에 변경 감지가 실행됩니다.

```java
@Controller
@RequiredArgsConstructor
public class ItemController { 
    private final ItemService itemService;
    /**
     * 상품 수정, 권장 코드 
     */
    @PostMapping(value = "/items/{itemId}/edit")
    public String updateItem(@PathVariable Long itemId, @ModelAttribute("form") BookForm form) {
        itemService.updateItem(itemId, form.getName(), form.getPrice(), form.getStockQuantity());
        return "redirect:/items";
    } 
}
```

```java
@Service
@RequiredArgsConstructor 
public class ItemService {
    private final ItemRepository itemRepository;
    /**
     * 영속성 컨텍스트가 자동 변경 
     */
    @Transactional
    public void updateItem(Long id, String name, int price, int stockQuantity) {
        Item item = itemRepository.findOne(id);
        item.setName(name);
        item.setPrice(price);
        item.setStockQuantity(stockQuantity);
    } 
}
```


### 💡데이터베이스 커서와 페이징의 차이

- createQeury().getResultStream() 이었는데 결국 Proxy 를 Stream 에 담은 상태로 불변성이 보장되어서 하이버네이트가 내부에 RealEntity 값을 심어야 되는데 이작업을 할 수 없으니 애러가 나는것이 맞는건가요? 맞다면, Stream은 어느시점에 사용하는것이 좋은가요..?

> 커서(Cursor) 내용은 아시겠지만 혹시 모르는 분들을 위해서 추가로 설명해둘께요.
List로 바로 받는 일반적인 쿼리의 경우 데이터베이스가 결과물을 애플리케이션에 한번에 모두 전달해줍니다. 그래서 데이터가 100만건이면 모든 데이터가 메모리에 올라오면서 Out Of Memory 이 발생하겠지요. 그래서 우리가 페이징 등으로 조금씩 나누어서 조회합니다.
그런데 정말 100만건을 한번에 조회하고 싶으면 커서라는 기능을 사용하면 됩니다. 이 기능으로 조회하면 데이터베이스는 결과를 내부에 저장해두고 애플리케이션에서 달라고 할 때 마다 조금씩 전송해줍니다. 이렇게 되려면 중요한게 애플리케이션과 DB간에 커넥션이 계속 연결되어있어야 하고, 추가로 DB도 해당 데이터를 어딘가에 보관해두어야 합니다.
이렇게 하면 애플리케이션은 조금씩 데이터를 받기 때문에 Out Of Memory 이 발생하지 않고, 사용한 데이터는 GC 처리하면 됩니다.
이 기능은 실시간 애플리케이션에서는 보통 사용하지 않고, 대량의 데이터를 한번에 처리할 때 사용합니다.
getResultStream()을 사용하면 바로 이 커서 기능이 동작합니다.
이 기능을 통해서 스트림을 받아서 스트림을 하나씩 호출하면 내부에서 Jdbc 드라이버의 next()를 호출해서 데이터를 가저옵니다. 바로 커서 기능이 동작하는 것이지요.
그런데 여기서! The object is already closed 라는 뜻은 뭔가 이 커서 데이터가 끊어저 버렸다는 뜻입니다.
이 커서가 끊어지는 것은 데이터베이스마다 다르지만 일반적으로 커넥션이 종료되거나, 또는 트랜잭션이 커밋되어 버리는 경우입니다. 트랜잭션 커밋의 경우 데이터베이스 마다 다릅니다. 트랜잭션이 커밋되어도 커서를 유지하는 옵션을 제공하기도 합니다.
바로 서비스 계층에서 트랜잭션이 커밋되어서 커서가 끊어저버려서 그렇습니다. 아마 서비스 계층에서 스프림을 돌리면 되고, 트랜잭션이 끝난 컨트롤러에서 스트림을 동작해보면 해당 오류가 발생할꺼에요.
그런데 놀랍게도 H2 데이터베이스는 사실 커서를 지원하지 않습니다. 그냥 커서를 지원하는 것 처럼 보이도록 시뮬레이션 한다고 이해하시면 됩니다.
커서는 데이터베이스 리소스를 오랜시간 많이 잡아먹기 때문에 대량의 데이터를 효과적으로 처리해야 하는 경우에만 사용하는 것을 권장합니다.


### ⭐️폼 객체 vs 엔티티 직접 사용 => 당연히 DTO 사용

요구사항이 정말 단순할 때는 폼 객체( `MemberForm` ) 없이 엔티티( `Member` )를 직접 등록과 수정 화면에서 사용해도 된다. 하지만 화면 요구사항이 복잡해지기 시작하면, 엔티티에 화면을 처리하기 위한 기능이 점점 증 가한다. 결과적으로 엔티티는 점점 화면에 종속적으로 변하고, 이렇게 화면 기능 때문에 지저분해진 엔티티는 결 국 유지보수하기 어려워진다.
실무에서 **엔티티는 핵심 비즈니스 로직만 가지고 있고, 화면을 위한 로직은 없어야 한다**. 화면이나 API에 맞는 폼 객체나 DTO를 사용하자. 그래서 화면이나 API 요구사항을 이것들로 처리하고, 엔티티는 최대한 순수하게 유지 하자.

특히, API 개발을 할 때는 무조건 DTO 를 사용하자!!! 엔티티에 중요한 필드가 추가된다고 할 때, 
1. 중요한 내용을 담는 필드가 외부에 노출이 될 수도 있고,
2. API 의 스펙이 변하게 된다. 그럼 해당 API 를 사용하는 다른 개발자들은 변한 API 의 스펙에 맞게 코드를 수정해야 하는 일이 발생할 수도 있다.