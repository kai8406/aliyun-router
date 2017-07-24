package com.mcloud.router.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.mcloud.router.constant.AliyunNextHopTypeEnum;

import lombok.Data;

@Data
@Entity
@Table(name = "aliyun_next_hops")
public class AliyunNextHopsDTO {

	@Id
	@GenericGenerator(name = "system-uuid", strategy = "uuid")
	@GeneratedValue(generator = "system-uuid")
	private String id;

	/**
	 * RouteEntry指向的下一跳实例的Id,非ECMP类路由必须指定NextHopId而且必须与被删除路由的NextHopId匹配.
	 */
	@Column(name = "next_hop_id")
	private String nextHopId;

	/**
	 * 下一跳的类型,可选值为Instance|Tunnel|HaVip|RouterInterface,默认值为Instance，即ECS实例.<b>
	 * 
	 * {@link NextHopTypeEnum}
	 * 
	 */
	@Column(name = "next_hop_type")
	private String nextHopType = AliyunNextHopTypeEnum.Instance.name();

	@Column(name = "router_entity_id")
	private String routerEntityId;

}
