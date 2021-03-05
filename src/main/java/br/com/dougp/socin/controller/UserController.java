package br.com.dougp.socin.controller;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.dougp.socin.domain.entity.UserEntity;
import br.com.dougp.socin.dto.UserDTO;
import br.com.dougp.socin.exception.NotFoundException;
import br.com.dougp.socin.service.APIGitHubService;
import br.com.dougp.socin.service.UserService;

@RequestMapping("/users")
@RestController
public class UserController {

	@Autowired
	UserService userService;

	@Autowired
	APIGitHubService apiGitHubService;

	@Autowired
	ModelMapper modelMapper;

	@GetMapping
	public ResponseEntity<List<UserDTO>> getAllUsers() {
		List<UserEntity> entities = userService.getAllUsers();
		List<UserDTO> dtos = new ArrayList<>(entities.size());
		for (UserEntity entity : entities) {
			UserDTO dto = modelMapper.map(entity, UserDTO.class);
			dto.setPassword("");
			dtos.add(dto);
		}
		return new ResponseEntity<>(dtos, new HttpHeaders(), HttpStatus.OK);
	}

	@GetMapping("/gitHubUsers/{fromIdx}")
	public ResponseEntity<UserDTO[]> getGitHubUsers(@PathVariable("fromIdx") int fromIdx) {
		UserDTO[] users = apiGitHubService.getUsers(fromIdx);
		if (fromIdx >= users.length && apiGitHubService.isDone()) {
			users = null;
		}
		return new ResponseEntity<>(users, new HttpHeaders(), HttpStatus.OK);
	}

	@GetMapping("/login/{login}")
	public ResponseEntity<UserDTO> getUserByLogin(@PathVariable("login") String login) throws NotFoundException {
		UserEntity entity = userService.getUserByLogin(login);
		UserDTO dto = modelMapper.map(entity, UserDTO.class);
		dto.setPassword("");
		return new ResponseEntity<>(dto, new HttpHeaders(), HttpStatus.OK);
	}

	@GetMapping("/{id}")
	public ResponseEntity<UserDTO> getUserById(@PathVariable("id") Long id) throws NotFoundException {
		UserEntity entity = userService.getUserById(id);
		UserDTO dto = modelMapper.map(entity, UserDTO.class);
		dto.setPassword("");
		return new ResponseEntity<>(dto, new HttpHeaders(), HttpStatus.OK);
	}

	@PostMapping
	public ResponseEntity<UserDTO> createOrUpdateUser(@RequestBody UserDTO userDTO) throws NotFoundException {
		UserEntity entity = userService.createOrUpdateUser(userDTO);
		modelMapper.map(entity, userDTO);
		userDTO.setPassword("");
		return new ResponseEntity<>(userDTO, new HttpHeaders(), HttpStatus.OK);
	}

	@DeleteMapping("/{id}")
	public HttpStatus deleteUserById(@PathVariable("id") Long id) throws NotFoundException {
		userService.deleteUserById(id);
		return HttpStatus.FORBIDDEN;
	}

}