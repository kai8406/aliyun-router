package com.mcloud.router.mq;

import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import com.mcloud.core.constant.mq.MQConstant;

public interface AliyunRouterMQService {

	@RabbitListener(bindings = @QueueBinding(value = @Queue(value = "cmop.aliyun.router", durable = "false", autoDelete = "true"), key = "cmop.agg.router.*", exchange = @Exchange(value = MQConstant.MQ_EXCHANGE_NAME, type = ExchangeTypes.TOPIC)))
	public void aliyunRouterAgg(Message message);

	@RabbitListener(bindings = @QueueBinding(value = @Queue(value = "cmop.aliyun.routerEntity", durable = "false", autoDelete = "true"), key = "cmop.agg.routerEntity.*", exchange = @Exchange(value = MQConstant.MQ_EXCHANGE_NAME, type = ExchangeTypes.TOPIC)))
	public void aliyunRouterEntityAgg(Message message);

	@RabbitListener(bindings = @QueueBinding(value = @Queue(value = "cmop.aliyun.routerTable", durable = "false", autoDelete = "true"), key = "cmop.agg.routerTable.*", exchange = @Exchange(value = MQConstant.MQ_EXCHANGE_NAME, type = ExchangeTypes.TOPIC)))
	public void aliyunRouterTableAgg(Message message);

}
