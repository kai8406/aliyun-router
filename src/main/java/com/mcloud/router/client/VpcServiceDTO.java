package com.mcloud.router.client;

import com.mcloud.core.constant.PlatformEnum;

import lombok.Data;

/**
 * VPC聚合对象.
 * 
 * @author liukai
 *
 */
@Data
public class VpcServiceDTO {

	/**
	 * UUID主键.
	 */
	private String id;

	/**
	 * 用户名,唯一.
	 */
	private String username;

	/**
	 * 平台ID. {@link PlatformEnum}
	 */
	private String platformId;

	/**
	 * task对象,不持久化.
	 */
	private String taskId = "";

	/**
	 * 平台资源的唯一标识符.
	 */
	private String uuid = "";

	/**
	 * 服务/资源状态.
	 */
	private String status = "";

	/**
	 * 区域.
	 */
	private String regionId;

	/**
	 * CIDR.
	 */
	private String cidrBlock;

	/**
	 * 说明.
	 */
	private String description = "";

	/**
	 * VPC名称.
	 */
	private String vpcName = "";

}
