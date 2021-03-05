package br.com.dougp.socin.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
					String name = readUser(login).path("name").asText();
					String htmlUrl = readUser(login).path("html_url").asText();
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

	private JsonNode readUsers(int page) throws IOException {
		String params = "q=" + URLEncoder.encode("followers:>65000 sort:followers-desc", Charset.defaultCharset()) + "&per_page=100&page=" + page;
		String url = "https://api.github.com/search/users?" + params;
		return new ObjectMapper().readTree(read(url)).get("items");
	}

	private JsonNode readUser(String login) throws IOException {
		String url = "https://api.github.com/users/" + login;
		return new ObjectMapper().readTree(read(url));
	}

	private String read(String url) throws IOException {
		StringBuilder buffer = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
			String line;
			while ((line = br.readLine()) != null) {
				buffer.append(line);
			}
		}
		return buffer.toString();
	}

}