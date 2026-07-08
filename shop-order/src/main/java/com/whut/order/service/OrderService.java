package com.whut.order.service;

import com.whut.common.entity.Order;
import com.whut.common.entity.User;
import com.whut.order.mapper.OrderMapper;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class OrderService {

    private final OrderMapper orderMapper;
    private final RestTemplate restTemplate;
    private final DiscoveryClient discoveryClient;

    public OrderService(OrderMapper orderMapper,
                        RestTemplate restTemplate,
                        DiscoveryClient discoveryClient) {
        this.orderMapper = orderMapper;
        this.restTemplate = restTemplate;
        this.discoveryClient = discoveryClient;
    }

    public Order getOrderById(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            return null;
        }

        List<ServiceInstance> instances = discoveryClient.getInstances("service-user");
        if (instances.isEmpty()) {
            throw new RuntimeException("service-user not found in Nacos");
        }

        String url = instances.get(0).getUri() + "/user/" + order.getUserId();
        User user = restTemplate.getForObject(url, User.class);
        order.setUser(user);

        return order;
    }
}
