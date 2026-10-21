package com.eaglebank;

import com.eaglebank.web.UserController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@DisplayName("Test suite of the class: EagleBankApiApplication")
class EagleBankApiApplicationTest {

	@Autowired
	private UserController userController;

	@Test
	void givenSpringContext_whenAutowiring_thenShouldInjectFromContext() {
		assertNotNull(userController);
	}
}
