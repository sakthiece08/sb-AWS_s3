package com.teqmonic.aws.model;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Report {

	@JsonProperty("report_type")
	private String reportType;

	@JsonProperty("report_name")
	private String reportName;

	@JsonProperty("created_date_time")
	private Instant timestamp;

	@JsonProperty("client_details")
	private Client client;

}
