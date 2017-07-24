package com.mcloud.router.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mcloud.core.persistence.BaseEntityCrudServiceImpl;
import com.mcloud.router.entity.AliyunRouterTableDTO;
import com.mcloud.router.repository.AliyunRouterTableRepository;

@Service
@Transactional
public class AliyunRouterTableService
		extends BaseEntityCrudServiceImpl<AliyunRouterTableDTO, AliyunRouterTableRepository> {

	@Autowired
	private AliyunRouterTableRepository repository;

	@Override
	protected AliyunRouterTableRepository getRepository() {
		return repository;
	}

}
