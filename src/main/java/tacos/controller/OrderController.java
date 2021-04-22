package tacos.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import tacos.component.OrderProps;
import tacos.domain.Order;
import tacos.domain.User;
import tacos.repository.OrderRepository;

import javax.validation.Valid;

@Slf4j
@Controller
@RequestMapping("/orders")
@SessionAttributes("order")
public class OrderController {

    private OrderProps props;
    private OrderRepository orderRepository;

    public OrderController(OrderRepository orderRepository, OrderProps props){
        this.orderRepository = orderRepository;
        this.props = props;
    }

    @GetMapping("/current")
    public String orderForm(@AuthenticationPrincipal User user, @ModelAttribute Order order) {

        if(order.getDeliveryName() == null){
            order.setDeliveryName(user.getFullName());
        }
        if(order.getDeliveryStreet() == null){
            order.setDeliveryStreet(user.getStreet());
        }
        if(order.getDeliveryCity() == null){
            order.setDeliveryCity(user.getCity());
        }
        if(order.getDeliveryState() == null){
            order.setDeliveryState(user.getState());
        }
        if(order.getDeliveryZip() == null){
            order.setDeliveryZip(user.getZip());
        }
        return "orderForm";
    }

    @PostMapping
    public String processOrder(@Valid Order order, Errors errors, SessionStatus sessionStatus){

        if(errors.hasErrors()){
            return "orderForm";
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        order.setUser(user);

        orderRepository.save(order);
        sessionStatus.setComplete();

        return "orderComplete";
    }

    @GetMapping
    public String ordersForUser(@AuthenticationPrincipal User user, Model model){ // 가장 최근 주문부터 오래된 주문의 순서로 정렬
        Pageable pageable = PageRequest.of(0, props.getPageSize());
        model.addAttribute("orders", orderRepository.findByUserOrderByPlacedAtDesc(user, pageable));

        return "orderList";
    }
}
