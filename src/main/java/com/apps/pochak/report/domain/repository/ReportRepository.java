package com.apps.pochak.report.domain.repository;

import com.apps.pochak.report.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
}
