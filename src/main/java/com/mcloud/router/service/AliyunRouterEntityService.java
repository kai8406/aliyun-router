package com.mcloud.router.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mcloud.core.persistence.BaseEntityCrudServiceImpl;
import com.mcloud.router.entity.AliyunRouterEntityDTO;
import com.mcloud.router.repository.AliyunRouterEntityRepository;

@Service
@Transactional
public class AliyunRouterEntityService
		extends BaseEntityCrudServiceImpl<AliyunRouterEntityDTO, AliyunRouterEntityRepository> {

	@Autowired
	private AliyunRouterEntityRepository repository;

	@Override
	protected AliyunRouterEntityRepository getRepository() {
		return repository;
	}

}
