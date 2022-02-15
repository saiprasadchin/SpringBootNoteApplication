package com.fundoo.fundoo;

import com.fundoo.fundoo.model.User;
import com.fundoo.fundoo.repository.IUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.*;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.Rollback;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
public class IUserRepositoryTests {

    @Autowired
    private IUserRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void testCreateUser() {
        User user = new User();
        user.setEmail("saiprasadchindam@gmail.com");
        user.setFirstName("saiprasad");
        user.setLastName("chindam");
        user.setPassword("1234567");

        User saveUser = repository.save(user);
        User existUser = entityManager.find(User.class, saveUser.getId());

        assertThat(existUser.getEmail()).isEqualTo(user.getEmail());
    }
}
