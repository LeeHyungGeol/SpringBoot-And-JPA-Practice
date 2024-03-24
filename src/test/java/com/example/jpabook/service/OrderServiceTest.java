package com.example.jpabook.service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.jpabook.domain.Address;
import com.example.jpabook.domain.Member;
import com.example.jpabook.domain.Order;
import com.example.jpabook.domain.OrderStatus;
import com.example.jpabook.domain.item.Book;
import com.example.jpabook.exception.NotEnoughStockException;
import com.example.jpabook.repository.OrderRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

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
