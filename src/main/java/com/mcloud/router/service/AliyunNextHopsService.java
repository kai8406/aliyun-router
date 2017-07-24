package com.mcloud.router.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mcloud.core.persistence.BaseEntityCrudServiceImpl;
import com.mcloud.router.entity.AliyunNextHopsDTO;
import com.mcloud.router.repository.AliyunNextHopsRepository;

@Service
@Transactional
public class AliyunNextHopsService extends BaseEntityCrudServiceImpl<AliyunNextHopsDTO, AliyunNextHopsRepository> {

	@Autowired
	private AliyunNextHopsRepository repository;

	@Override
	protected AliyunNextHopsRepository getRepository() {
		return repository;
	}

}
