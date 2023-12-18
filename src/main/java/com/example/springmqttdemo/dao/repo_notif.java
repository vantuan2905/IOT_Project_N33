package com.example.springmqttdemo.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jakarta.transaction.Transactional;

public interface repo_notif extends JpaRepository<notification, String>{
	@Query(value="update notification set seen=0 where ten=?1",nativeQuery = true)
	@Transactional
	@Modifying
	public void setSeen(String x);
}
