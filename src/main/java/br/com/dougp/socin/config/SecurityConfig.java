package br.com.dougp.socin.config;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.dougp.socin.domain.entity.UserEntity;
import br.com.dougp.socin.dto.UserDTO;
import br.com.dougp.socin.service.UserService;

/**
 * @author Douglas Palte
 */

@Configuration
@Order(1)
@RestController
@RequestMapping()
class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private Environment env;

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers(env.getProperty("spring.h2.console.path") + "/**").antMatchers("/users/**").antMatchers("/users/gitHubUsers/**");
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

}

@Component
class CustomAuthenticationProvider implements AuthenticationProvider {
	@Autowired
	private UserService userService;
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	public Authentication authenticate(Authentication auth) throws AuthenticationException {
		UserEntity user = userService.getUserByLogin(auth.getPrincipal().toString());
		if (user == null && "admin".equals(auth.getPrincipal())) {
			user = userService.createOrUpdateUser(new UserDTO("admin", "Administrator", "admin", ""));

		}
		if (user == null || !passwordEncoder.matches(auth.getCredentials().toString(), user.getPassword())) {
			throw new BadCredentialsException("user not found or wrong password");
		}
		return new UsernamePasswordAuthenticationToken(user.getLogin(), user.getPassword(), Collections.emptyList());
	}

	@Override
	public boolean supports(Class<?> auth) {
		return auth.equals(UsernamePasswordAuthenticationToken.class);
	}
}