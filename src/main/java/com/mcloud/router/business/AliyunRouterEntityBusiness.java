package com.mcloud.router.business;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aliyuncs.IAcsClient;
import com.aliyuncs.ecs.model.v20140526.CreateRouteEntryRequest;
import com.aliyuncs.ecs.model.v20140526.CreateRouteEntryRequest.NextHopList;
import com.aliyuncs.ecs.model.v20140526.CreateRouteEntryResponse;
import com.aliyuncs.ecs.model.v20140526.DeleteRouteEntryRequest;
import com.aliyuncs.ecs.model.v20140526.DeleteRouteEntryResponse;
import com.aliyuncs.exceptions.ClientException;
import com.mcloud.core.constant.ActiveEnum;
import com.mcloud.core.constant.AggTypeEnum;
import com.mcloud.core.constant.mq.MQConstant;
import com.mcloud.core.constant.result.ResultDTO;
import com.mcloud.core.constant.result.ResultEnum;
import com.mcloud.core.constant.task.TaskDTO;
import com.mcloud.core.constant.task.TaskStatusEnum;
import com.mcloud.router.client.AccesskeyDTO;
import com.mcloud.router.client.RouterEntityServiceDTO;
import com.mcloud.router.constant.AliyunNextHopTypeEnum;
import com.mcloud.router.constant.AliyunRouterEntityStatusEnum;
import com.mcloud.router.constant.AliyunRouterEntityTypeEnum;
import com.mcloud.router.entity.AliyunNextHopsDTO;
import com.mcloud.router.entity.AliyunRouterEntityDTO;
import com.mcloud.router.service.AliyunRouterEntityService;

@Component
public class AliyunRouterEntityBusiness extends AbstractAliyunCommon {

	@Autowired
	protected AliyunRouterEntityService service;

	public void removeRouterEntity(RouterEntityServiceDTO routerEntityServiceDTO) {

		// Step.1 创建Task对象.
		TaskDTO taskDTO = taskClient.getTask(routerEntityServiceDTO.getTaskId());

		// Step.2 根据username获得阿里云accesskeyId和accesskeySecret
		AccesskeyDTO accesskeyDTO = accountClient
				.getAccesskey(routerEntityServiceDTO.getUsername(), routerEntityServiceDTO.getPlatformId()).getData();

		// Step.3 获得AliyunRouterEntityDTO对象.
		Map<String, Object> map = new HashMap<>();
		map.put("EQ_routerEntityId", routerEntityServiceDTO.getRouterEntityId());
		AliyunRouterEntityDTO aliyunRouterEntityDTO = service.find(map);

		// AliyunRouterEntityTypeEnum为System时,aliyn后台会自动删除,只需要更新本地数据库状态.
		// 当为Custom的时候再调用aliyun sdk.
		if (AliyunRouterEntityTypeEnum.Custom.name().equalsIgnoreCase(routerEntityServiceDTO.getRouterEntityType())) {

			// Step.4 调用阿里云SDK执行操作,根据aliyun vpcId 获得aliyun routerId.
			IAcsClient client = getServiceInstance(routerEntityServiceDTO.getRegionId(), accesskeyDTO);

			DeleteRouteEntryRequest request = new DeleteRouteEntryRequest();
			request.setDestinationCidrBlock(aliyunRouterEntityDTO.getDestinationCidrBlock());
			request.setRouteTableId(aliyunRouterEntityDTO.getRouterTableUuid());

			request.setNextHopId(aliyunRouterEntityDTO.getNextHopId());

			List<DeleteRouteEntryRequest.NextHopList> nextHopLists = new ArrayList<>();

			aliyunRouterEntityDTO.getNextHops().stream().forEach(n -> {
				DeleteRouteEntryRequest.NextHopList nextHopList = new DeleteRouteEntryRequest.NextHopList();
				nextHopList.setNextHopId(n.getNextHopId());
				nextHopList.setNextHopType(n.getNextHopType());
				nextHopLists.add(nextHopList);
			});
			request.setNextHopLists(nextHopLists);

			try {

				DeleteRouteEntryResponse response = client.getAcsResponse(request);

				taskDTO.setRequestId(response.getRequestId());

			} catch (ClientException e) {

				// 修改Task对象执行状态.
				taskDTO.setStatus(TaskStatusEnum.执行失败.name());
				taskDTO.setResponseCode(e.getErrCode());
				taskDTO.setResponseData(e.getErrMsg());
				taskDTO = taskClient.updateTask(taskDTO.getId(), taskDTO);

				ResultDTO resultDTO = new ResultDTO(routerEntityServiceDTO.getRouterEntityId(),
						AggTypeEnum.routerEntity.name(), ResultEnum.ERROR.name(), taskDTO.getId(),
						routerEntityServiceDTO.getUsername(), "");

				// 将执行的结果进行广播.
				rabbitTemplate.convertAndSend(MQConstant.MQ_EXCHANGE_NAME, MQConstant.ROUTINGKEY_RESULT_REMOVE,
						binder.toJson(resultDTO));
				return;
			}

		}

		// Step.5 修改AliyunRouterDTO.
		aliyunRouterEntityDTO.setActive(ActiveEnum.N.name());
		service.saveAndFlush(aliyunRouterEntityDTO);

		// Step.6 修改TaskDTO.
		taskDTO.setStatus(TaskStatusEnum.执行成功.name());
		taskDTO = taskClient.updateTask(taskDTO.getId(), taskDTO);

		ResultDTO resultDTO = new ResultDTO(routerEntityServiceDTO.getRouterEntityId(), AggTypeEnum.routerEntity.name(),
				ResultEnum.SUCCESS.name(), taskDTO.getId(), routerEntityServiceDTO.getUsername(), "");

		// Step.7 将执行的结果进行广播.
		rabbitTemplate.convertAndSend(MQConstant.MQ_EXCHANGE_NAME, MQConstant.ROUTINGKEY_RESULT_REMOVE,
				binder.toJson(resultDTO));
	}

	public void saveRouterEntity(RouterEntityServiceDTO routerEntityServiceDTO) {

		// Step.1 获得Task对象.
		TaskDTO taskDTO = taskClient.getTask(routerEntityServiceDTO.getTaskId());

		// Step.2 根据username获得阿里云accesskeyId和accesskeySecret
		AccesskeyDTO accesskeyDTO = accountClient
				.getAccesskey(routerEntityServiceDTO.getUsername(), routerEntityServiceDTO.getPlatformId()).getData();

		// Step.3 持久化AliyunRouterDTO.
		AliyunRouterEntityDTO aliyunRouterEntityDTO = new AliyunRouterEntityDTO();
		aliyunRouterEntityDTO.setDestinationCidrBlock(routerEntityServiceDTO.getDestinationCidrBlock());
		aliyunRouterEntityDTO.setRouterEntityType(routerEntityServiceDTO.getRouterEntityType());
		aliyunRouterEntityDTO.setRouterTableUuid(routerEntityServiceDTO.getRouterTableUuid());
		aliyunRouterEntityDTO.setRouterEntityId(routerEntityServiceDTO.getRouterEntityId());
		aliyunRouterEntityDTO.setRouterTableId(routerEntityServiceDTO.getRouterTableId());
		aliyunRouterEntityDTO.setNextHopType(routerEntityServiceDTO.getNextHopType());
		aliyunRouterEntityDTO.setRouterUuid(routerEntityServiceDTO.getRouterUuid());
		aliyunRouterEntityDTO.setPlatformId(routerEntityServiceDTO.getPlatformId());
		aliyunRouterEntityDTO.setNextHopId(routerEntityServiceDTO.getNextHopId());
		aliyunRouterEntityDTO.setRouterId(routerEntityServiceDTO.getRouterId());
		aliyunRouterEntityDTO.setRegionId(routerEntityServiceDTO.getRegionId());
		aliyunRouterEntityDTO.setUsername(routerEntityServiceDTO.getUsername());
		aliyunRouterEntityDTO.setVpcUuid(routerEntityServiceDTO.getVpcUuid());
		aliyunRouterEntityDTO.setVpcId(routerEntityServiceDTO.getVpcId());

		List<AliyunNextHopsDTO> aliyunNextHopsDTOs = new ArrayList<>();
		routerEntityServiceDTO.getNextHops().stream().forEach(n -> {
			AliyunNextHopsDTO aliyunNextHopsDTO = new AliyunNextHopsDTO();
			aliyunNextHopsDTO.setNextHopType(n.getNextHopType());
			aliyunNextHopsDTO.setNextHopId(n.getNextHopId());
			aliyunNextHopsDTOs.add(aliyunNextHopsDTO);
		});
		aliyunRouterEntityDTO.setNextHops(aliyunNextHopsDTOs);
		aliyunRouterEntityDTO.setCreateTime(new Date());

		service.saveAndFlush(aliyunRouterEntityDTO);

		// 如果是云网段的路由项,无须执行创建动作,阿里云默认会创建,此处只需将数据持久化.
		if (!AliyunNextHopTypeEnum.Service.name().equalsIgnoreCase(routerEntityServiceDTO.getNextHopType())) {

			// Step.4 调用阿里云SDK执行操作,根据aliyun vpcId 获得aliyun routerId.
			IAcsClient client = getServiceInstance(routerEntityServiceDTO.getRegionId(), accesskeyDTO);

			CreateRouteEntryRequest request = new CreateRouteEntryRequest();
			request.setDestinationCidrBlock(routerEntityServiceDTO.getDestinationCidrBlock());
			request.setRouteTableId(routerEntityServiceDTO.getRouterTableUuid());
			request.setNextHopType(routerEntityServiceDTO.getNextHopType());
			request.setNextHopId(routerEntityServiceDTO.getNextHopId());
			List<NextHopList> nextHopLists = new ArrayList<>();
			routerEntityServiceDTO.getNextHops().stream().forEach(routerEntity -> {
				NextHopList nextHopList = new NextHopList();
				nextHopList.setNextHopType(routerEntity.getNextHopType());
				nextHopList.setNextHopId(routerEntity.getNextHopId());
				nextHopLists.add(nextHopList);
			});
			request.setNextHopLists(nextHopLists);

			try {

				CreateRouteEntryResponse response = client.getAcsResponse(request);

				taskDTO.setRequestId(response.getRequestId());

			} catch (ClientException e) {

				// 修改Task对象执行状态.
				taskDTO.setStatus(TaskStatusEnum.执行失败.name());
				taskDTO.setResponseCode(e.getErrCode());
				taskDTO.setResponseData(e.getErrMsg());
				taskDTO = taskClient.updateTask(taskDTO.getId(), taskDTO);

				ResultDTO resultDTO = new ResultDTO(routerEntityServiceDTO.getRouterEntityId(),
						AggTypeEnum.routerEntity.name(), ResultEnum.ERROR.name(), taskDTO.getId(),
						routerEntityServiceDTO.getUsername(), "");

				// 将执行的结果进行广播.
				rabbitTemplate.convertAndSend(MQConstant.MQ_EXCHANGE_NAME, MQConstant.ROUTINGKEY_RESULT_SAVE,
						binder.toJson(resultDTO));
				return;
			}

		}
		// Step.5 修改AliyunRouterDTO.
		aliyunRouterEntityDTO.setStatus(AliyunRouterEntityStatusEnum.Available.name());
		service.saveAndFlush(aliyunRouterEntityDTO);

		// Step.6 修改TaskDTO.
		taskDTO.setStatus(TaskStatusEnum.执行成功.name());
		taskDTO = taskClient.updateTask(taskDTO.getId(), taskDTO);

		ResultDTO resultDTO = new ResultDTO(routerEntityServiceDTO.getRouterEntityId(), AggTypeEnum.routerEntity.name(),
				ResultEnum.SUCCESS.name(), taskDTO.getId(), routerEntityServiceDTO.getUsername(), "");

		// Step.7 将执行的结果进行广播.
		rabbitTemplate.convertAndSend(MQConstant.MQ_EXCHANGE_NAME, MQConstant.ROUTINGKEY_RESULT_SAVE,
				binder.toJson(resultDTO));
	}

}
