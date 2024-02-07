package com.teqmonic.aws.controller;

import java.net.URL;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.teqmonic.aws.model.Report;
import com.teqmonic.aws.service.DocumentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/api/v1")
@RestController
@RequiredArgsConstructor
public class DocumentController {

	private final DocumentService documentService;

	@GetMapping("/report_type/{reportType}")
	public List<Report> getReportList(@PathVariable(required = true) String reportType) {
		log.info("In DocumentController getReportList(), reportType: {}", reportType);
		List<Report> reports = documentService.getReportList(reportType);
		return reports;
	}
	
	
    /**
     * Items in S3 buckets are immutable. 
     * Any change on either file content, metadata or tags effectively create a new item and replace the existing one.
     * 
     * @param reportType
     * @param report
     * @param file
     */
	@PostMapping("/report_type/{reportType}")
	public void uploadReport(@PathVariable(required = true) String reportType, @RequestPart("report") Report report,
			@RequestParam("file") MultipartFile file) {
		log.info("In DocumentController addReport(), reportType: {}", report.getReportType());
		if (!reportType.equals(report.getReportType())) {
            throw new IllegalArgumentException("Upload report with inconsistent report type");
         }
		documentService.addReport(report, file);
	}
	
	@GetMapping("/report_type/{reportType}/report/{reportName}")
	public Report getReportInfo(@PathVariable(required = true) String reportType, @PathVariable(required = true) String reportName) {
		Optional<Report> report = documentService.getReportOrEmpty(reportType, reportName);
		return report.orElseThrow();
	}
	
	@GetMapping(value = "/download/report_type/{reportType}/report/{reportName}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public byte[] downloadReport(@PathVariable(required = true) String reportType, @PathVariable(required = true) String reportName) {
		Optional<Report> report = documentService.getReportOrEmpty(reportType, reportName);
		if(report.isEmpty()) {
			throw new NoSuchElementException();
		}
		return documentService.downloadReport(reportType, reportName);
	}
	
	@GetMapping("/url/report_type/{reportType}/report/{reportName}")
	public URL getPresignedUrl(@PathVariable(required = true) String reportType, @PathVariable(required = true) String reportName) {
		Optional<Report> report = documentService.getReportOrEmpty(reportType, reportName);
		if(report.isEmpty()) {
			throw new NoSuchElementException();
		}
		return documentService.getPresignedUrl(reportType, reportName);
	}
	
	@DeleteMapping("/report_type/{reportType}/report/{reportName}")
	public void deleteReport(@PathVariable(required = true) String reportType, @PathVariable(required = true) String reportName) {
		Optional<Report> report = documentService.getReportOrEmpty(reportType, reportName);
		if(report.isEmpty()) {
			throw new NoSuchElementException();
		}
		documentService.deleteReport(reportType, reportName);
	}
}
