package com.mcloud.router.client;

import lombok.Data;

@Data
public class RouterNextHopsDTO {

	private String routerNextHopsId;

	/**
	 * RouteEntry指向的下一跳实例的Id,非ECMP类路由.<br>
	 * 
	 * 必须指定NextHopId而且必须与被删除路由的NextHopId匹配.
	 */
	private String nextHopId = "";

	/**
	 * 下一跳的类型,可选值为Instance|Tunnel|HaVip|RouterInterface,默认值为Instance，即ECS实例.<br>
	 * 
	 * {@link NextHopTypeEnum}
	 * 
	 */
	private String nextHopType;

	private String routerEntityId;

}
