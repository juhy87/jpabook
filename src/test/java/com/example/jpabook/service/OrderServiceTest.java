package com.example.jpabook.service;

import com.example.jpabook.domain.Address;
import com.example.jpabook.domain.Member;
import com.example.jpabook.domain.Order;
import com.example.jpabook.domain.item.Book;
import com.example.jpabook.domain.item.Item;
import com.example.jpabook.enums.OrderStatus;
import com.example.jpabook.exception.NotEnoughSotckException;
import com.example.jpabook.repository.OrderRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class OrderServiceTest {

    @Autowired
    EntityManager em;

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    /**
     * 상품 주문
     */
    @Test
    public void 주문(){
        //given
        Member member = createMember();

        int bookPrice =100;
        Book book = createBook(bookPrice, "가가", 100);


        //when
        int orderCount = 2;
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);
        //then
        Order order = orderRepository.findOne(orderId);
        Assert.assertEquals("상품주문시 상태", OrderStatus.ORDER, order.getOrderStatus());
        Assert.assertEquals("상품주문시 수량", 1, order.getOrderItems().size());
        Assert.assertEquals("주문 가격 = 가격*수량", bookPrice * orderCount, order.getTotalPrice());
        Assert.assertEquals("재고", 98, book.getStockQuantity());

    }


    /**
     * 상품주문_재고수량 초과
     */
    @Test(expected = NotEnoughSotckException.class)
    public void 재고수량초과(){
        //given
        Member member = createMember();

        Item item = createBook(100, "가가", 100);
        //when
        int orderCount =1100;
        orderService.order(member.getId(), item.getId(), orderCount);
        
        
        //then
        Assert.fail("재고 수량 부족이 발생해야됨");
    }

    private Book createBook(int bookPrice, String bookName, int stockQuantity) {
        Book book = new Book();
        book.setName(bookName);
        book.setPrice(bookPrice);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        return book;
    }

    private Member createMember() {
        Member member = new Member();
        member.setUsername("홍길");
        member.setAddress(new Address("seoul", "KKK", "123-123"));
        em.persist(member);
        return member;
    }
}