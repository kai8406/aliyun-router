package com.mcloud.router.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mcloud.core.constant.ActiveEnum;
import com.mcloud.core.constant.PlatformEnum;
import com.mcloud.router.constant.AliyunRouterTableTypeEnum;

import lombok.Data;

@Data
@Entity
@Table(name = "aliyun_router_table")
public class AliyunRouterTableDTO {

	/**
	 * UUID主键.
	 */
	@Id
	@GenericGenerator(name = "system-uuid", strategy = "uuid")
	@GeneratedValue(generator = "system-uuid")
	private String id;

	/**
	 * 数据状态,默认"A" {@link ActiveEnum}.
	 */
	@JsonIgnore
	@Column(name = "active")
	private String active = ActiveEnum.A.name();

	/**
	 * 平台ID. {@link PlatformEnum}
	 */
	@Column(name = "platform_id")
	private String platformId;

	/**
	 * 区域.
	 */
	@Column(name = "region_id")
	private String regionId;

	/**
	 * task对象,不持久化.
	 */
	@Transient
	private String taskId = "";

	/**
	 * 用户名,唯一.
	 */
	@Column(name = "user_name")
	private String username;

	/**
	 * 平台资源的唯一标识符.
	 */
	@Column(name = "uuid")
	private String uuid = "";

	/**
	 * 路由表类型, System | Custom 二者选其一,默认为System.
	 */
	@Column(name = "router_table_type")
	private String routerTableType = AliyunRouterTableTypeEnum.System.name();

	/**
	 * VPC聚合服务主键.
	 */
	@Column(name = "vpc_id")
	private String vpcId = "";

	/**
	 * VPC聚合服务的UUID.
	 */
	@Column(name = "vpc_uuid")
	private String vpcUuid = "";

	/**
	 * Router聚合服务的主键.
	 */
	@Column(name = "router_id")
	private String routerId = "";

	/**
	 * Router聚合服务的UUID.
	 */
	@Column(name = "router_uuid")
	private String routerUuid = "";

	/**
	 * RouterTable聚合服务的主键.
	 */
	@Column(name = "router_table_id")
	private String routerTableId = "";

	/**
	 * 创建时间.
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Column(name = "create_time")
	private Date createTime;

	/**
	 * 修改时间.
	 */
	@JsonIgnore
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Column(name = "modify_time")
	private Date modifyTime;

}
