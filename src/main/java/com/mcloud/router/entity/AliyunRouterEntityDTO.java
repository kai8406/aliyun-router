package com.mcloud.router.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import com.mcloud.router.constant.AliyunNextHopTypeEnum;
import com.mcloud.router.constant.AliyunRouterEntityStatusEnum;
import com.mcloud.router.constant.AliyunRouterEntityTypeEnum;

import lombok.Data;

@Data
@Entity
@Table(name = "aliyun_router_entity")
public class AliyunRouterEntityDTO {

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
	 * 目标网段地址.
	 */
	@Column(name = "destination_cidr_block")
	private String destinationCidrBlock;

	/**
	 * RouteEntry指向的下一跳实例的Id,非ECMP类路由.<br>
	 * 
	 * 必须指定NextHopId而且必须与被删除路由的NextHopId匹配.
	 */
	@Column(name = "next_hop_id")
	private String nextHopId = "";

	@Transient
	private List<AliyunNextHopsDTO> nextHops = new ArrayList<>();

	/**
	 * 下一跳的类型,可选值为Instance|Tunnel|HaVip|RouterInterface,默认值为Instance，即ECS实例.<br>
	 * 
	 * {@link NextHopTypeEnum}
	 * 
	 */
	@Column(name = "next_hop_type")
	private String nextHopType = AliyunNextHopTypeEnum.Instance.name();

	/**
	 * 路由类型, System | Custom 二者选其一,默认为Custom.<b>
	 * 
	 * {@link RouterEntityTypeEnum}
	 */
	@Column(name = "router_entity_type")
	private String routerEntityType = AliyunRouterEntityTypeEnum.Custom.name();

	/**
	 * Router Entity状态 Pending | Available | Modifying <b>
	 * 
	 * {@link RouterEntityStatusEnum}
	 */
	@Column(name = "status")
	private String status = AliyunRouterEntityStatusEnum.Pending.name();

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
	 * RouterTable的主键.
	 */
	@Column(name = "router_table_id")
	private String routerTableId;

	/**
	 * RouterTable的UUID.
	 */
	@Column(name = "router_table_uuid")
	private String routerTableUuid;

	/**
	 * RouterEntity的主键.
	 */
	@Column(name = "router_entity_id")
	private String routerEntityId;

	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Column(name = "create_time")
	private Date createTime;

	/**
	 * 修改时间
	 */
	@JsonIgnore
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Column(name = "modify_time")
	private Date modifyTime;

}
