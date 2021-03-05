package br.com.dougp.socin;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.openfeign.EnableFeignClients;

import br.com.dougp.socin.domain.entity.UserEntity;
import br.com.dougp.socin.service.UserService;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@EnableFeignClients
class UserTests {

	@Autowired
	private UserService userService;

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void servico() {
		List<UserEntity> users = userService.getAllUsers();
		assertThat(users).hasSizeGreaterThan(0);
	}

	@Test
	void endPoint() {
		String body = this.restTemplate.getForObject("/users", String.class);
		assertThat(body).contains("admin");
	}

}
