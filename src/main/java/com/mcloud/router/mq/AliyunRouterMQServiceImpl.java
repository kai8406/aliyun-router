package com.mcloud.router.mq;

import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mcloud.core.constant.PlatformEnum;
import com.mcloud.core.constant.mq.MQConstant;
import com.mcloud.core.mapper.JsonMapper;
import com.mcloud.core.util.EncodeUtils;
import com.mcloud.router.business.AliyunRouterBusiness;
import com.mcloud.router.business.AliyunRouterEntityBusiness;
import com.mcloud.router.business.AliyunRouterTableBusiness;
import com.mcloud.router.client.RouterEntityServiceDTO;
import com.mcloud.router.client.RouterServiceDTO;
import com.mcloud.router.client.RouterTableServiceDTO;

@Component
public class AliyunRouterMQServiceImpl implements AliyunRouterMQService {

	private static JsonMapper binder = JsonMapper.nonEmptyMapper();

	@Autowired
	private AliyunRouterBusiness routerBusiness;

	@Autowired
	private AliyunRouterEntityBusiness routerEntityBusiness;

	@Autowired
	private AliyunRouterTableBusiness routerTableBusiness;

	@Override
	public void aliyunRouterAgg(Message message) {

		String receivedRoutingKey = message.getMessageProperties().getReceivedRoutingKey();

		String receiveString = EncodeUtils.EncodeMessage(message.getBody());

		RouterServiceDTO routerServiceDTO = binder.fromJson(receiveString, RouterServiceDTO.class);

		if (!PlatformEnum.aliyun.name().equalsIgnoreCase(routerServiceDTO.getPlatformId())) {
			return;
		}

		if (MQConstant.ROUTINGKEY_AGG_ROUTER_SAVE.equalsIgnoreCase(receivedRoutingKey)) {

			routerBusiness.saveRouter(routerServiceDTO);

		} else if (MQConstant.ROUTINGKEY_AGG_ROUTER_UPDATE.equalsIgnoreCase(receivedRoutingKey)) {

			routerBusiness.updateRouter(routerServiceDTO);

		} else if (MQConstant.ROUTINGKEY_AGG_ROUTER_REMOVE.equalsIgnoreCase(receivedRoutingKey)) {

			routerBusiness.removeRouter(routerServiceDTO);
		}
	}

	@Override
	public void aliyunRouterEntityAgg(Message message) {

		String receivedRoutingKey = message.getMessageProperties().getReceivedRoutingKey();

		String receiveString = EncodeUtils.EncodeMessage(message.getBody());

		RouterEntityServiceDTO routerEntityServiceDTO = binder.fromJson(receiveString, RouterEntityServiceDTO.class);

		if (!PlatformEnum.aliyun.name().equalsIgnoreCase(routerEntityServiceDTO.getPlatformId())) {
			return;
		}

		if (MQConstant.ROUTINGKEY_AGG_ROUTERENTITY_SAVE.equalsIgnoreCase(receivedRoutingKey)) {

			routerEntityBusiness.saveRouterEntity(routerEntityServiceDTO);

		} else if (MQConstant.ROUTINGKEY_AGG_ROUTERENTITY_REMOVE.equalsIgnoreCase(receivedRoutingKey)) {

			routerEntityBusiness.removeRouterEntity(routerEntityServiceDTO);
		}

	}

	@Override
	public void aliyunRouterTableAgg(Message message) {

		String receivedRoutingKey = message.getMessageProperties().getReceivedRoutingKey();

		String receiveString = EncodeUtils.EncodeMessage(message.getBody());

		RouterTableServiceDTO routerTableServiceDTO = binder.fromJson(receiveString, RouterTableServiceDTO.class);

		if (!PlatformEnum.aliyun.name().equalsIgnoreCase(routerTableServiceDTO.getPlatformId())) {
			return;
		}

		if (MQConstant.ROUTINGKEY_AGG_ROUTERTABLE_SAVE.equalsIgnoreCase(receivedRoutingKey)) {

			routerTableBusiness.saveRouterTable(routerTableServiceDTO);

		} else if (MQConstant.ROUTINGKEY_AGG_ROUTERTABLE_REMOVE.equalsIgnoreCase(receivedRoutingKey)) {

			routerTableBusiness.removeRouterTable(routerTableServiceDTO);
		}
	}

}
