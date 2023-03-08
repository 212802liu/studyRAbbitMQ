package com.itheima.test;


import com.itheima.rabbitmq.config.RabbitMQConfig;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
//@RunWith(SpringRunner.class)
public class ProducerTest {

    //1.注入RabbitTemplate
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void testSend(){

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME,"boot.haha","boot mq hello~~~");
        /*try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
    }
    /**
     * TTL:过期时间
     *  1. 队列统一过期
     *
     *  2. 消息单独过期
     * 
     *
     * keypoint:如果设置了消息的过期时间，也设置了队列的过期时间，它以时间短的为准。
     * keypoint:队列过期后，会将队列所有消息全部移除。
     * keypoint:消息过期后，只有消息在队列顶端，才会判断其是否过期(移除掉)
     *
     */
    @Test
    public void testTTL() {
        // 消息后处理对象，设置一些消息的参数信息
        MessagePostProcessor messagePostProcessor = new MessagePostProcessor() {

            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                //1.设置message的信息
                message.getMessageProperties().setExpiration("5000");//消息的过期时间
                //2.返回该消息
                return message;
            }
        };

        for (int i = 0; i < 10; i++) {
            if (i == 5) {
                //消息单独过期
                rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "dlx.ttl", "message ttl....", messagePostProcessor);
            } else {
                //不过期的消息
                rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "dlx.test", "message ttl....");

            }

        }
    }
}
