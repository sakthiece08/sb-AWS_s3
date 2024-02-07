package com.teqmonic.aws.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Client {
	
	@JsonProperty("client_id")
	private String clientId;
	
	@JsonProperty("client_name")
	private String name;

}
