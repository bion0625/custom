package com.project.base.auth.repo;

import com.project.base.domain.AppUser;
import com.project.base.repository.AppUserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

@DataR2dbcTest
@ActiveProfiles("test")
class AppUserRepositoryTest {

    @Autowired
    private AppUserRepository repo;

    @Test
    void findByUsername_works() {
        AppUser user = new AppUser();
        user.setUsername("repoUser");
        user.setPassword("encPw");
        user.setRoles("ROLE_USER");

        StepVerifier.create(repo.deleteAll()
                        .then(repo.save(user))
                        .then(repo.findByUsername("repoUser")))
                .assertNext(loaded -> {
                    Assertions.assertThat(loaded).isNotNull();
                    Assertions.assertThat(loaded.getUsername()).isEqualTo("repoUser");
                })
                .verifyComplete();
    }
}
