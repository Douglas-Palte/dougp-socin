package br.com.dougp.socin;

import static org.junit.Assert.assertNotEquals;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.openfeign.EnableFeignClients;

import br.com.dougp.socin.dto.UserDTO;
import br.com.dougp.socin.service.APIGitHubService;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@EnableFeignClients
class GitHubApiTests {

	@Autowired
	private APIGitHubService apiGitHubService;

	@Test
	void users() throws IOException, InterruptedException {
		UserDTO[] users = apiGitHubService.getUsers();
		int idx = 0;
		do {
			while (users[idx] != null) {
				System.out.println(String.format("%03d: %s", idx + 1, users[idx]));
				idx++;
			}
		} while (!apiGitHubService.isDone() || (idx < users.length && users[idx] != null));
		assertNotEquals(idx, 0);
	}

}
