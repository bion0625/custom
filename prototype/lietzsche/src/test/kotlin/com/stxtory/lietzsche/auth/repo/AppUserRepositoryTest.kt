package com.stxtory.lietzsche.auth.repo

import com.stxtory.lietzsche.domain.AppUser
import com.stxtory.lietzsche.repository.AppUserRepository
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.test.context.ActiveProfiles

@DataR2dbcTest
@ActiveProfiles("test")
class AppUserRepositoryTest(@Autowired val repo: AppUserRepository) {

    @Test
    fun `findByUsername works`() = runTest {
        repo.deleteAll()

        val user = AppUser(username = "repoUser", password = "encPw", roles = "ROLE_USER")
        repo.save(user)

        val loaded = repo.findByUsername("repoUser")
        assertThat(loaded).isNotNull
        assertThat(loaded!!.username).isEqualTo("repoUser")
    }
}