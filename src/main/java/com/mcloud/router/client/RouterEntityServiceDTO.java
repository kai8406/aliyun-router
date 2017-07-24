package com.mcloud.router.client;

import java.util.ArrayList;
import java.util.List;

import com.mcloud.core.constant.PlatformEnum;

import lombok.Data;

@Data
public class RouterEntityServiceDTO {

	private String routerEntityId;

	/**
	 * 平台ID. {@link PlatformEnum}
	 */
	private String platformId;

	/**
	 * 区域.
	 */
	private String regionId;

	/**
	 * task对象,不持久化.
	 */
	private String taskId = "";
	/**
	 * 用户名,唯一.
	 */
	private String username;

	/**
	 * 目标网段地址.
	 */
	private String destinationCidrBlock;

	/**
	 * RouteEntry指向的下一跳实例的Id,非ECMP类路由.<br>
	 * 
	 * 必须指定NextHopId而且必须与被删除路由的NextHopId匹配.
	 */
	private String nextHopId = "";

	private List<RouterNextHopsDTO> nextHops = new ArrayList<>();

	/**
	 * 下一跳的类型,可选值为Instance|Tunnel|HaVip|RouterInterface,默认值为Instance，即ECS实例.<br>
	 * 
	 * {@link NextHopTypeEnum}
	 * 
	 */
	private String nextHopType;

	/**
	 * 路由类型, System | Custom 二者选其一,默认为Custom.<b>
	 * 
	 * {@link RouterEntityTypeEnum}
	 */
	private String routerEntityType;

	/**
	 * Router Entity状态 Pending | Available | Modifying <b>
	 * 
	 * {@link RouterEntityStatusEnum}
	 */
	private String status;

	/**
	 * VPC聚合服务主键.
	 */
	private String vpcId = "";

	/**
	 * VPC聚合服务的UUID.
	 */
	private String vpcUuid = "";

	/**
	 * Router聚合服务的主键.
	 */
	private String routerId = "";

	/**
	 * Router聚合服务的UUID.
	 */
	private String routerUuid = "";

	/**
	 * RouterTable的主键.
	 */
	private String routerTableId;

	/**
	 * RouterTable的UUID.
	 */
	private String routerTableUuid;

}
