package com.ajit.springAI.VideoGenaration.repository;

import com.ajit.springAI.VideoGenaration.model.ProjectRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRecordRepository extends JpaRepository<ProjectRecord, String> {
}
