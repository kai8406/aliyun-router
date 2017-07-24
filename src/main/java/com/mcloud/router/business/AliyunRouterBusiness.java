package com.mcloud.router.business;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aliyuncs.IAcsClient;
import com.aliyuncs.ecs.model.v20140526.DescribeVpcsRequest;
import com.aliyuncs.ecs.model.v20140526.DescribeVpcsResponse;
import com.aliyuncs.ecs.model.v20140526.DescribeVpcsResponse.Vpc;
import com.aliyuncs.ecs.model.v20140526.ModifyVRouterAttributeRequest;
import com.aliyuncs.ecs.model.v20140526.ModifyVRouterAttributeResponse;
import com.aliyuncs.exceptions.ClientException;
import com.mcloud.core.constant.ActiveEnum;
import com.mcloud.core.constant.AggTypeEnum;
import com.mcloud.core.constant.mq.MQConstant;
import com.mcloud.core.constant.result.ResultDTO;
import com.mcloud.core.constant.result.ResultEnum;
import com.mcloud.core.constant.task.TaskDTO;
import com.mcloud.core.constant.task.TaskStatusEnum;
import com.mcloud.core.mapper.BeanMapper;
import com.mcloud.router.client.AccesskeyDTO;
import com.mcloud.router.client.RouterServiceDTO;
import com.mcloud.router.entity.AliyunRouterDTO;
import com.mcloud.router.service.AliyunRouterService;

@Component
public class AliyunRouterBusiness extends AbstractAliyunCommon {

	@Autowired
	protected AliyunRouterService service;

	/**
	 * 根据阿里云的Id获得AliyunRouterDTO对象.
	 * 
	 * @param uuid
	 * @return
	 */
	private AliyunRouterDTO getAliyunRouterDTOByUUID(String uuid) {
		Map<String, Object> map = new HashMap<>();
		map.put("EQ_uuid", uuid);
		return service.find(map);
	}

	public void removeRouter(RouterServiceDTO routerServiceDTO) {

		/**
		 * 阿里云中,只要成功删除Vpc,则router自动删除.因此这里只对DB做状态的更新.
		 */

		// Step.1 创建Task对象.
		TaskDTO taskDTO = taskClient.getTask(routerServiceDTO.getTaskId());
		taskDTO.setStatus(TaskStatusEnum.执行成功.name());
		taskDTO = taskClient.updateTask(taskDTO.getId(), taskDTO);

		// Step.2 获得AliyunRouterDTO对象,并更新状态.
		AliyunRouterDTO aliyunRouterDTO = getAliyunRouterDTOByUUID(routerServiceDTO.getRouterUuid());

		aliyunRouterDTO.setActive(ActiveEnum.N.name());
		aliyunRouterDTO.setModifyTime(new Date());
		service.saveAndFlush(aliyunRouterDTO);

		ResultDTO resultDTO = new ResultDTO(routerServiceDTO.getRouterId(), AggTypeEnum.router.name(),
				ResultEnum.SUCCESS.name(), taskDTO.getId(), aliyunRouterDTO.getUsername(), aliyunRouterDTO.getUuid());

		// Step.3 将执行的结果进行广播.
		rabbitTemplate.convertAndSend(MQConstant.MQ_EXCHANGE_NAME, MQConstant.ROUTINGKEY_RESULT_REMOVE,
				binder.toJson(resultDTO));
	}

	public void saveRouter(RouterServiceDTO routerServiceDTO) {

		// Step.1 获得Task对象.
		TaskDTO taskDTO = taskClient.getTask(routerServiceDTO.getTaskId());

		// Step.2 根据username获得阿里云accesskeyId和accesskeySecret
		AccesskeyDTO accesskeyDTO = accountClient
				.getAccesskey(routerServiceDTO.getUsername(), routerServiceDTO.getPlatformId()).getData();

		// Step.3 持久化AliyunRouterDTO.

		AliyunRouterDTO aliyunRouterDTO = BeanMapper.map(routerServiceDTO, AliyunRouterDTO.class);
		aliyunRouterDTO.setRouterId(routerServiceDTO.getRouterId());
		aliyunRouterDTO.setCreateTime(new Date());
		aliyunRouterDTO = service.saveAndFlush(aliyunRouterDTO);

		// Step.4 调用阿里云SDK执行操作 .
		IAcsClient client = getServiceInstance(routerServiceDTO.getRegionId(), accesskeyDTO);

		DescribeVpcsRequest request = new DescribeVpcsRequest();
		request.setVpcId(routerServiceDTO.getVpcUuid());

		String uuid = null;// aliyun routerId

		try {
			DescribeVpcsResponse response = client.getAcsResponse(request);

			for (Vpc vpc : response.getVpcs()) {
				uuid = vpc.getVRouterId();
			}

		} catch (ClientException e) {
			// 修改Task对象执行状态.
			taskDTO.setStatus(TaskStatusEnum.执行失败.name());
			taskDTO.setResponseCode(e.getErrCode());
			taskDTO.setResponseData(e.getErrMsg());
			taskDTO = taskClient.updateTask(taskDTO.getId(), taskDTO);

			ResultDTO resultDTO = new ResultDTO(routerServiceDTO.getRouterId(), AggTypeEnum.router.name(),
					ResultEnum.ERROR.name(), taskDTO.getId(), routerServiceDTO.getUsername(), "");

			// 将执行的结果进行广播.
			rabbitTemplate.convertAndSend(MQConstant.MQ_EXCHANGE_NAME, MQConstant.ROUTINGKEY_RESULT_SAVE,
					binder.toJson(resultDTO));
			return;
		}

		// Step.5 修改AliyunRouterDTO.
		aliyunRouterDTO.setUuid(uuid);
		service.saveAndFlush(aliyunRouterDTO);

		// Step.6 修改TaskDTO.
		taskDTO.setStatus(TaskStatusEnum.执行成功.name());
		taskDTO = taskClient.updateTask(taskDTO.getId(), taskDTO);

		ResultDTO resultDTO = new ResultDTO(routerServiceDTO.getRouterId(), AggTypeEnum.router.name(),
				ResultEnum.SUCCESS.name(), taskDTO.getId(), routerServiceDTO.getUsername(), aliyunRouterDTO.getUuid());

		// Step.7 将执行的结果进行广播.
		rabbitTemplate.convertAndSend(MQConstant.MQ_EXCHANGE_NAME, MQConstant.ROUTINGKEY_RESULT_SAVE,
				binder.toJson(resultDTO));
	}

	public void updateRouter(RouterServiceDTO routerServiceDTO) {

		// Step.1 获得Task对象.
		TaskDTO taskDTO = taskClient.getTask(routerServiceDTO.getTaskId());

		// Step.2 根据username获得阿里云accesskeyId和accesskeySecret
		AccesskeyDTO accesskeyDTO = accountClient
				.getAccesskey(routerServiceDTO.getUsername(), routerServiceDTO.getPlatformId()).getData();

		// Step.3 查询AliyunVpcDTO.
		AliyunRouterDTO aliyunRouterDTO = getAliyunRouterDTOByUUID(routerServiceDTO.getRouterUuid());

		// Step.4 调用阿里云SDK执行操作.
		ModifyVRouterAttributeRequest modifyVRouterAttributeRequest = new ModifyVRouterAttributeRequest();
		modifyVRouterAttributeRequest.setDescription(routerServiceDTO.getDescription());
		modifyVRouterAttributeRequest.setVRouterName(routerServiceDTO.getRouterName());
		modifyVRouterAttributeRequest.setVRouterId(routerServiceDTO.getRouterUuid());

		IAcsClient client = getServiceInstance(aliyunRouterDTO.getRegionId(), accesskeyDTO);

		ModifyVRouterAttributeResponse response = null;

		try {

			response = client.getAcsResponse(modifyVRouterAttributeRequest);

			taskDTO.setRequestId(response.getRequestId());

		} catch (ClientException e) {

			/// 修改Task对象执行状态.
			taskDTO.setStatus(TaskStatusEnum.执行失败.name());
			taskDTO.setResponseCode(e.getErrCode());
			taskDTO.setResponseData(e.getErrMsg());
			taskDTO = taskClient.updateTask(taskDTO.getId(), taskDTO);

			ResultDTO resultDTO = new ResultDTO(routerServiceDTO.getRouterId(), AggTypeEnum.router.name(),
					ResultEnum.ERROR.name(), taskDTO.getId(), aliyunRouterDTO.getUsername(), aliyunRouterDTO.getUuid());

			// 将执行的结果进行广播.
			rabbitTemplate.convertAndSend(MQConstant.MQ_EXCHANGE_NAME, MQConstant.ROUTINGKEY_RESULT_UPDATE,
					binder.toJson(resultDTO));
			return;
		}

		// // Step.5 更新Task和服务对象.
		aliyunRouterDTO.setDescription(routerServiceDTO.getDescription());
		aliyunRouterDTO.setRouterName(routerServiceDTO.getRouterName());
		aliyunRouterDTO.setModifyTime(new Date());
		aliyunRouterDTO = service.saveAndFlush(aliyunRouterDTO);

		taskDTO.setStatus(TaskStatusEnum.执行成功.name());
		taskDTO = taskClient.updateTask(taskDTO.getId(), taskDTO);

		ResultDTO resultDTO = new ResultDTO(routerServiceDTO.getRouterId(), AggTypeEnum.router.name(),
				ResultEnum.SUCCESS.name(), taskDTO.getId(), aliyunRouterDTO.getUsername(), aliyunRouterDTO.getUuid());

		// Step.6 将执行的结果进行广播.
		rabbitTemplate.convertAndSend(MQConstant.MQ_EXCHANGE_NAME, MQConstant.ROUTINGKEY_RESULT_UPDATE,
				binder.toJson(resultDTO));
	}

}
