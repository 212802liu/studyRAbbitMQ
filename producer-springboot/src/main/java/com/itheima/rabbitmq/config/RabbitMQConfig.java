package com.itheima.rabbitmq.config;

import org.assertj.core.internal.bytebuddy.implementation.bytecode.Throw;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "boot_topic_exchange";
    public static final String DLX_EXCHANGE_NAME = "dlx_boot_topic_exchange";
    public static final String QUEUE_NAME = "boot_queue";
    public static final String DLX_QUEUE_NAME = "dlx_boot_queue";

    //1.交换机
    @Bean("bootExchange")
    public Exchange bootExchange(){
        return ExchangeBuilder.topicExchange(EXCHANGE_NAME).durable(true).build();
    }

    //1.1 死信交换机
    @Bean("dlxBootExchange")
    public Exchange bootDLXExchange(){
        return ExchangeBuilder.topicExchange(DLX_EXCHANGE_NAME).durable(true).build();
    }

    //2.Queue 队列,绑定死信交换机,"dlx.test" 对应死信交换机的"dlx.#"
    @Bean("bootQueue")
    public Queue bootQueue(){
        //ttl 设置队列中所以消息过期时间，ms  .maxLength
        return QueueBuilder.durable(QUEUE_NAME).ttl(100000).deadLetterExchange(DLX_EXCHANGE_NAME).deadLetterRoutingKey("dlx.test").build();
    }
    //2.1Queue 死信队列
    @Bean("dlxBootQueue")
    public Queue bootQueue1(){
        //ttl 设置队列中所以消息过期时间，ms
        return QueueBuilder.durable(DLX_QUEUE_NAME).build();
    }

    //3. 队列和交互机绑定关系 Binding
    /*
        1. 知道哪个队列
        2. 知道哪个交换机
        3. routing key
     */
    @Bean
    public Binding bindQueueExchange(@Qualifier("bootQueue") Queue queue, @Qualifier("bootExchange") Exchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with("#.*").noargs();
    }
    /**
     * @param queue:
     * @param exchange:
     * @return Binding
     * @description 死信交换机绑定死信队列
     */
    @Bean
    public Binding bindQueueDeadLetterExchange(@Qualifier("dlxBootQueue") Queue queue, @Qualifier("dlxBootExchange") Exchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with("dlx.#").noargs();
    }


        /**
         * @param connectionFactory:
         * @return RabbitTemplate
         * @description RabbitMQ 为我们提供了两种方式用来控制消息的投递可靠性模式。
         * confirm 确认模式
         * return  退回模式
         * rabbitmq 整个消息投递的路径为：
         * producer--->rabbitmq broker--->exchange--->queue--->consumer
         * 消息从 producer 到 exchange 则会返回一个 confirmCallback 。
         * 消息从 exchange-->queue 投递失败则会返回一个 returnCallback 。
         * 我们将利用这两个 callback 控制消息的可靠性投递
         */
    @Bean
    public RabbitTemplate createRabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.setConnectionFactory(connectionFactory);

        //设置消息投递失败的策略，有两种策略：自动删除或返回到客户端。
        //我们既然要做可靠性，当然是设置为返回到客户端(true是返回客户端，false是自动删除)
        rabbitTemplate.setMandatory(true);

        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                System.out.println();
                System.out.println("相关数据：" + correlationData);
                if (ack) {
                    System.out.println("投递成功,确认情况：" + ack);
                } else {
                    System.out.println("投递失败,确认情况：" + ack);
                    System.out.println("原因：" + cause);
                }
            }
        });

        /*rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                System.out.println();
                System.out.println("ReturnCallback:     " + "消息：" + message);
                System.out.println("ReturnCallback:     " + "回应码：" + replyCode);
                System.out.println("ReturnCallback:     " + "回应信息：" + replyText);
                System.out.println("ReturnCallback:     " + "交换机：" + exchange);
                System.out.println("ReturnCallback:     " + "路由键：" + routingKey);
                System.out.println();
            }
        });*/
        rabbitTemplate.setReturnsCallback(new RabbitTemplate.ReturnsCallback(){

            @Override
            public void returnedMessage(ReturnedMessage returnedMessage) {
                System.out.println("ReturnCallback:     " + "交换机：" +returnedMessage.getExchange());
                System.out.println("ReturnCallback:     " + "消息：" + returnedMessage.getMessage());
                System.out.println("ReturnCallback:     " + "回应码：" +returnedMessage.getReplyCode());
                System.out.println("ReturnCallback:     " + "回应信息：" +returnedMessage.getReplyText());
                System.out.println("ReturnCallback:     " + "路由键：" +returnedMessage.getRoutingKey());
            }
        });


        return rabbitTemplate;
    }




}
