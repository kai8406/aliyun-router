package com.mcloud.router.business;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aliyuncs.IAcsClient;
import com.aliyuncs.ecs.model.v20140526.DescribeVRoutersRequest;
import com.aliyuncs.ecs.model.v20140526.DescribeVRoutersResponse;
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
import com.mcloud.router.client.RouterTableServiceDTO;
import com.mcloud.router.entity.AliyunRouterTableDTO;
import com.mcloud.router.service.AliyunRouterTableService;

@Component
public class AliyunRouterTableBusiness extends AbstractAliyunCommon {

	@Autowired
	protected AliyunRouterTableService service;

	/**
	 * 根据阿里云的Id获得AliyunRouterTableDTO对象.
	 * 
	 * @param uuid
	 * @return
	 */
	private AliyunRouterTableDTO getAliyunRouterTableDTOByUUID(String uuid) {
		Map<String, Object> map = new HashMap<>();
		map.put("EQ_uuid", uuid);
		return service.find(map);
	}

	public void removeRouterTable(RouterTableServiceDTO routerTableServiceDTO) {

		/**
		 * 阿里云中,只要成功删除Vpc,则router自动删除.因此这里只对DB做状态的更新.
		 */

		// Step.1 创建Task对象.
		TaskDTO taskDTO = taskClient.getTask(routerTableServiceDTO.getTaskId());
		taskDTO.setStatus(TaskStatusEnum.执行成功.name());
		taskDTO = taskClient.updateTask(taskDTO.getId(), taskDTO);

		// Step.2 获得AliyunRouterDTO对象,并更新状态.
		AliyunRouterTableDTO aliyunRouterTableDTO = getAliyunRouterTableDTOByUUID(
				routerTableServiceDTO.getRouterTableUuid());

		aliyunRouterTableDTO.setActive(ActiveEnum.N.name());
		aliyunRouterTableDTO.setModifyTime(new Date());
		service.saveAndFlush(aliyunRouterTableDTO);

		ResultDTO resultDTO = new ResultDTO(routerTableServiceDTO.getRouterTableId(), AggTypeEnum.routerTable.name(),
				ResultEnum.SUCCESS.name(), taskDTO.getId(), aliyunRouterTableDTO.getUsername(),
				aliyunRouterTableDTO.getUuid());

		// Step.3 将执行的结果进行广播.
		rabbitTemplate.convertAndSend(MQConstant.MQ_EXCHANGE_NAME, MQConstant.ROUTINGKEY_RESULT_REMOVE,
				binder.toJson(resultDTO));
	}

	public void saveRouterTable(RouterTableServiceDTO routerTableServiceDTO) {

		/**
		 * 阿里云中创建VPC时会默认创建一个RouteTable,因此暂时可认为一个VPC下只有一个RouteTable,注意,此处没有考虑高速通道.
		 */

		// Step.1 获得Task对象.
		TaskDTO taskDTO = taskClient.getTask(routerTableServiceDTO.getTaskId());

		// Step.2 根据username获得阿里云accesskeyId和accesskeySecret
		AccesskeyDTO accesskeyDTO = accountClient
				.getAccesskey(routerTableServiceDTO.getUsername(), routerTableServiceDTO.getPlatformId()).getData();

		// Step.3 调用阿里云SDK执行操作,根据aliyun vpcId 获得aliyun routerId.
		IAcsClient client = getServiceInstance(routerTableServiceDTO.getRegionId(), accesskeyDTO);

		// 设置参数
		DescribeVRoutersRequest request = new DescribeVRoutersRequest();
		request.setVRouterId(routerTableServiceDTO.getRouterUuid());
		request.setRegionId(routerTableServiceDTO.getRegionId());

		try {

			DescribeVRoutersResponse response = client.getAcsResponse(request);

			response.getVRouters().stream()
					.forEach(routeTable -> routeTable.getRouteTableIds().stream().forEach(routeTableId -> {

						// Step.3 持久化AliyunRouterTableDTO.

						AliyunRouterTableDTO aliyunRouterTableDTO = BeanMapper.map(routerTableServiceDTO,
								AliyunRouterTableDTO.class);
						aliyunRouterTableDTO.setRouterTableId(routerTableServiceDTO.getRouterTableId());
						aliyunRouterTableDTO.setCreateTime(new Date());
						aliyunRouterTableDTO.setUuid(routeTableId);

						service.saveAndFlush(aliyunRouterTableDTO);

						ResultDTO resultDTO = new ResultDTO(routerTableServiceDTO.getRouterTableId(),
								AggTypeEnum.routerTable.name(), ResultEnum.SUCCESS.name(),
								routerTableServiceDTO.getTaskId(), routerTableServiceDTO.getUsername(),
								aliyunRouterTableDTO.getUuid());

						// Step.4 将执行的结果进行广播.
						rabbitTemplate.convertAndSend(MQConstant.MQ_EXCHANGE_NAME, MQConstant.ROUTINGKEY_RESULT_SAVE,
								binder.toJson(resultDTO));
					}));

			// Step.5 修改TaskDTO.
			taskDTO.setStatus(TaskStatusEnum.执行成功.name());
			taskDTO = taskClient.updateTask(taskDTO.getId(), taskDTO);

		} catch (ClientException e) {

			// 修改Task对象执行状态.
			taskDTO.setStatus(TaskStatusEnum.执行失败.name());
			taskDTO.setResponseCode(e.getErrCode());
			taskDTO.setResponseData(e.getErrMsg());
			taskDTO = taskClient.updateTask(taskDTO.getId(), taskDTO);

			ResultDTO resultDTO = new ResultDTO(routerTableServiceDTO.getRouterTableId(),
					AggTypeEnum.routerTable.name(), ResultEnum.ERROR.name(), taskDTO.getId(),
					routerTableServiceDTO.getUsername(), "");

			// 将执行的结果进行广播.
			rabbitTemplate.convertAndSend(MQConstant.MQ_EXCHANGE_NAME, MQConstant.ROUTINGKEY_RESULT_SAVE,
					binder.toJson(resultDTO));
			return;
		}
	}

}
