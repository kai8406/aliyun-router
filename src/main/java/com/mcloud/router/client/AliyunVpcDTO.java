package com.mcloud.router.client;

import lombok.Data;

@Data
public class AliyunVpcDTO {

	private String id;

	/**
	 * 用户ID.
	 */
	private String username;

	private String platformId;

	/**
	 * task对象,不持久化.
	 */
	private String taskId;

	/**
	 * agg对象Id,不持久化.
	 */
	private String aggId;

	/**
	 * 平台资源的唯一标识符
	 */
	private String uuid = "";

	/**
	 * 资源状态.
	 */
	private String status = "";

	/**
	 * 区域
	 */
	private String regionId;

	/**
	 * CIDR
	 */
	private String cidrBlock;

	/**
	 * 说明
	 */
	private String description = "";

	/**
	 * VPC名称
	 */

	private String vpcName = "";

}
