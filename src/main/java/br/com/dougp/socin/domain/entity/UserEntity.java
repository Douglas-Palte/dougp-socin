package br.com.dougp.socin.domain.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity()
@Table(name = "user")
public class UserEntity {

	@Id
	@GeneratedValue
	private Long id;

	@Column(unique = true, nullable = false)
	private String login;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String password;

}
