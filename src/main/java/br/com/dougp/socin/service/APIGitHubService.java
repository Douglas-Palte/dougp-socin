package br.com.dougp.socin.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.dougp.socin.dto.UserDTO;
import lombok.Data;

@Service
@Data
public class APIGitHubService {
	private static final Logger logger = LoggerFactory.getLogger(APIGitHubService.class);

	private boolean done;
	private UserDTO[] users;

	public UserDTO[] getUsers(int from) {
		if (users == null) {
			users = new UserDTO[500];
			new Thread(() -> process()).start();
		}
		int to = from;
		while (to < users.length && users[to] != null) {
			to++;
		}
		return Arrays.copyOfRange(users, from, to);
	}

	private void process() {
		try {
			int idx = 0;
			int page = 1;
			JsonNode items = readUsers(page++);
			do {
				for (Iterator<JsonNode> iterator = items.iterator(); iterator.hasNext() && idx < users.length; idx++) {
					JsonNode next = iterator.next();
					String login = next.path("login").asText();
					JsonNode userNode = readUser(login);
					String name = userNode.path("name").asText();
					String htmlUrl = userNode.path("html_url").asText();
					users[idx] = new UserDTO(login, name, "", htmlUrl);
				}
				items = readUsers(page++);
			} while (!items.isEmpty() && idx < users.length);
		} catch (Exception e) {
			logger.error(APIGitHubService.class.getName() + ".run: ", e);
		} finally {
			done = true;
		}
	}

	private JsonNode readUsers(int page) throws IOException, InterruptedException {
		// followers:>3000 sort:followers-desc
		String params = "q=followers%3A%3E3000%20sort%3Afollowers-desc&page=" + page;
		String url = "https://api.github.com/search/users?" + params;
		return new ObjectMapper().readTree(read(url, 10)).get("items");
	}

	private JsonNode readUser(String login) throws IOException, InterruptedException {
		String url = "https://api.github.com/users/" + login;
		return new ObjectMapper().readTree(read(url, 10));
	}

	private String read(String url, int attempt) throws IOException, InterruptedException {
		Thread.sleep((long) 1 * 60 * 1000); // 60 requests an hour (GitHub.com rate limiting)
		StringBuilder buffer = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
			String line;
			while ((line = br.readLine()) != null) {
				buffer.append(line);
			}
		} catch (IOException e) {
			if (e.toString().indexOf("Server returned HTTP response code: 403 for URL:") != -1 && --attempt > 0) {
				return read(url, attempt);
			} else {
				throw e;
			}
		}
		return buffer.toString();
	}

}