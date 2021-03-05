package br.com.dougp.socin.service;

import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.dougp.socin.domain.entity.UserEntity;
import br.com.dougp.socin.domain.repository.UserRepository;
import br.com.dougp.socin.dto.UserDTO;
import br.com.dougp.socin.exception.NotFoundException;

@Service
public class UserService {

	@Autowired
	UserRepository repository;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private PasswordEncoder passwordEncoder;

	public List<UserEntity> getAllUsers() {
		return repository.findAllByOrderByLogin();
	}

	public UserEntity getUserByLogin(String login) {
		return repository.findByLogin(login);
	}

	public UserEntity getUserById(Long id) {
		Optional<UserEntity> user = repository.findById(id);
		return user.isPresent() ? user.get() : null;
	}

	public UserEntity createOrUpdateUser(UserDTO userDTO) {
		UserEntity userEntity = repository.findByLogin(userDTO.getLogin());
		if (userEntity != null) {
			modelMapper.map(userDTO, userEntity);
		} else {
			userEntity = modelMapper.map(userDTO, UserEntity.class);
		}
		userEntity.setPassword(passwordEncoder.encode(userDTO.getPassword()));
		userEntity = repository.save(userEntity);

		return userEntity;
	}

	public void deleteUserById(Long id) throws NotFoundException {
		Optional<UserEntity> user = repository.findById(id);
		if (user.isPresent()) {
			repository.deleteById(id);
		} else {
			throw new NotFoundException("No user record exist for given id");
		}
	}

	public long size() {
		return repository.count();
	}

}