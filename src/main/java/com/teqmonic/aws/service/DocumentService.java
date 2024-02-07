package com.teqmonic.aws.service;

import java.net.URL;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.teqmonic.aws.dao.S3ItemDao;
import com.teqmonic.aws.model.Client;
import com.teqmonic.aws.model.Report;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentService {
	
	private static final String METADATA_TIMESTAMP = "timestamp";
	private static final String CLIENT_ID = "clientid";
	private static final String CLIENT_NAME = "clientname";

	private final S3ItemDao s3ItemDao;
	
	public List<Report> getReportList(String reportType) {
		List<String> keys = s3ItemDao.listItems(reportType);
		
		return keys.stream().map(key -> key.substring(key.indexOf("/")+1, key.length()))
				.map(reportName -> getReportInfo(reportType, reportName))
				.collect(Collectors.toList());
		
	}
	
	public void addReport(Report report, MultipartFile file) {
		String itemKey = generateItemKey(report.getReportType(), file.getOriginalFilename());
		Map<String, String> metadata = new HashMap<>();
		// set current timestamp in the metadata
		metadata.put(METADATA_TIMESTAMP, DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
		if (!ObjectUtils.isEmpty(report.getClient())) {
			metadata.put(CLIENT_ID, report.getClient().getClientId());
			metadata.put(CLIENT_NAME, report.getClient().getName());
		}
		s3ItemDao.uploadItem(itemKey, metadata, file);
	}
	
	public Optional<Report> getReportOrEmpty(String reportType, String report) {
		String itemKey = generateItemKey(reportType, report);
		if(s3ItemDao.listItems(reportType).contains(itemKey)) {
			return Optional.of(getReportInfo(reportType, report));
		}
		
		return Optional.empty();
	}
	
	public byte[] downloadReport(String reportType, String report) {
		String itemKey = generateItemKey(reportType, report);
		return s3ItemDao.downloadItemData(itemKey);
	}
	
	public URL getPresignedUrl(String reportType, String report) {
		String itemKey = generateItemKey(reportType, report);
		return s3ItemDao.generatePresignedUrl(itemKey);
	}
	
	public void deleteReport(String reportType, String report) {
		String itemKey = generateItemKey(reportType, report);
		s3ItemDao.deleteItem(itemKey);
	}
	
	private Report getReportInfo(String reportType, String report) {
		String itemKey = generateItemKey(reportType, report);
		Map<String, String> metadataMap = s3ItemDao.retrieveMetadata(itemKey);
		
	    Report.ReportBuilder builder = Report.builder().reportType(reportType).reportName(report);
	    
	    if (metadataMap.containsKey(METADATA_TIMESTAMP)) {
            builder.timestamp(Instant.parse(metadataMap.get(METADATA_TIMESTAMP)));
        }
	    
	    if (metadataMap.containsKey(CLIENT_ID)) {
			builder.client(
					Client.builder().clientId(metadataMap.get(CLIENT_ID)).name(metadataMap.get(CLIENT_NAME)).build());
	    }
		
		return builder.build();	
	}

	private String generateItemKey(String reportType, String report) {
		return String.format("%s/%s", reportType, report);
	}

}
