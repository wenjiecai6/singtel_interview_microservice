package com.singtel.interviewtest.repository;

import com.singtel.interviewtest.entity.RequestTrace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestTraceRepository extends JpaRepository<RequestTrace, Long> {
}
