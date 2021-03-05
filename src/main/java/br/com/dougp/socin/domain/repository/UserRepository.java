package br.com.dougp.socin.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.dougp.socin.domain.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

	@Query("SELECT user FROM UserEntity user WHERE LOWER(user.login) = LOWER(:userLogin)")
	UserEntity findByLogin(@Param("userLogin") String userLogin);

	List<UserEntity> findAllByOrderByLogin();

}
