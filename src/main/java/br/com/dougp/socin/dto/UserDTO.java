package br.com.dougp.socin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
@JsonInclude(value = Include.NON_NULL)
public class UserDTO {

	private Long id;

	@NonNull
	private String login;

	@NonNull
	private String name;

	@NonNull
	private String password;

	@NonNull
	private String htmlUrl;

	public String toString() {
		return name + " (" + login + ")";
	}
}
