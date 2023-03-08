package com.itheima.consumerspringboot.listener;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RabbimtMQListener {

//    @RabbitListener(queues = "boot_queue")
    public void ListenerQueue(Message message){
        //System.out.println(message);
        System.out.println(new String(message.getBody()));
    }

    /**
     * ack确认
     * @param message 队列中的消息;
     * @param channel 当前的消息队列;
     * @param tag 取出来当前消息在队列中的的索引,
     * 用这个@Header(AmqpHeaders.DELIVERY_TAG)注解可以拿到;
     * @throws IOException
     */

//    @RabbitListener(queues = "boot_queue")
    public void myAckListener(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException, InterruptedException {
        Thread.sleep(1000);
        System.out.println(message);
        try {

            /**
             * 无异常就确认消息
             * basicAck(long deliveryTag, boolean multiple)
             * deliveryTag:取出来当前消息在队列中的的索引;
             * multiple:为true的话就是批量确认,如果当前deliveryTag为5,那么就会确认
             * deliveryTag为5及其以下的消息;一般设置为false
             */
            int i = 1/0;
            System.out.println("消息确认");
            channel.basicAck(tag, false);

        }catch (Exception e){
            /**
             * 有异常就绝收消息
             * basicNack(long deliveryTag, boolean multiple, boolean requeue)
             * requeue:true为将消息重返当前消息队列,还可以重新发送给消费者;
             *         false:将消息丢弃
             */
            System.out.println("消息异常,退回");
            channel.basicNack(tag,false,true);
        }

    }

    @RabbitListener(queues = "boot_queue")
    public void receive_email(Object msg, Message message, Channel channel) throws InterruptedException, IOException {
        System.out.println("QUEUE_INFORM_EMAIL msg"+msg);
        long tag = message.getMessageProperties().getDeliveryTag();
        Thread.sleep(1000);
        System.out.println(message);
        try {

            /**
             * 无异常就确认消息
             * basicAck(long deliveryTag, boolean multiple)
             * deliveryTag:取出来当前消息在队列中的的索引;
             * multiple:为true的话就是批量确认,如果当前deliveryTag为5,那么就会确认
             * deliveryTag为5及其以下的消息;一般设置为false
             */
            int i = 1/0;
            System.out.println("消息确认");
            channel.basicAck(tag, true);

        }catch (Exception e){
            /**
             * 有异常就绝收消息
             * basicNack(long deliveryTag, boolean multiple, boolean requeue)
             * requeue:true为将消息重返当前消息队列,还可以重新发送给消费者;
             *         false:将消息丢弃
             */
            System.out.println("消息异常,退回");
            channel.basicNack(tag,false,false);
        }
    }

}
