package com.mcloud.router.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mcloud.core.persistence.BaseEntityCrudServiceImpl;
import com.mcloud.router.entity.AliyunRouterDTO;
import com.mcloud.router.repository.AliyunRouterRepository;

@Service
@Transactional
public class AliyunRouterService extends BaseEntityCrudServiceImpl<AliyunRouterDTO, AliyunRouterRepository> {

	@Autowired
	private AliyunRouterRepository repository;

	@Override
	protected AliyunRouterRepository getRepository() {
		return repository;
	}

}
