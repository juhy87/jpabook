package com.example.jpabook.service;

import com.example.jpabook.domain.Delivery;
import com.example.jpabook.domain.Member;
import com.example.jpabook.domain.Order;
import com.example.jpabook.domain.OrderItem;
import com.example.jpabook.domain.item.Item;
import com.example.jpabook.repository.ItemRepository;
import com.example.jpabook.repository.MemberBasicJPARepository;
import com.example.jpabook.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberBasicJPARepository memberBasicJPARepository;
    private final ItemRepository itemRepository;

    /**
     * 주문
     * @param memberId
     * @param itemId
     * @param count
     * @return
     */
    @Transactional
    public Long order(Long memberId, Long  itemId, int count){

        //엔티티조회
        Member member = memberBasicJPARepository.findOne(memberId);
        Item item = itemRepository.findOne(itemId);

        //배송지 생성
        Delivery delivery = new Delivery();
        delivery.setAddress(member.getAddress());

        //주문 상품
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);

        //주문 생성
        Order order = Order.createOrder(member, delivery, orderItem);

        //주문 저장
        orderRepository.save(order);

        return order.getId();

    }

    /**
     * 취소
     */
    public void cancelOrder(Long orderId){
        Order order = orderRepository.findOne(orderId);
        order.cancel();

    }

    /**
     * 검색
     */
//    public List<Order> findOrders(OrderSearch orderSearch){
//     return orderRepository.findAll(orderSearch);
//    }

}
