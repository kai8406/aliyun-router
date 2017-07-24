package com.mcloud.router.client;

import com.mcloud.core.constant.PlatformEnum;

import lombok.Data;

/**
 * RouterTable聚合服务持久化对象.
 * 
 * @author liukai
 *
 */
@Data
public class RouterTableServiceDTO {

	/**
	 * UUID主键.
	 */
	private String routerTableId;

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
	 * 平台资源的唯一标识符.
	 */
	private String routerTableUuid = "";

	/**
	 * 路由表类型, System | Custom 二者选其一,默认为System.
	 */
	private String routerTableType;

	/**
	 * Router聚合服务的主键.
	 */
	private String routerId = "";

	/**
	 * Router聚合服务的UUID.
	 */
	private String routerUuid = "";

	/**
	 * VPC聚合服务主键.
	 */
	private String vpcId = "";

	/**
	 * VPC聚合服务的UUID.
	 */
	private String vpcUuid = "";

}
