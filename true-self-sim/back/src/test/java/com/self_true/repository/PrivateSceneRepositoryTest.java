package com.self_true.repository;

import com.self_true.model.entity.PrivateScene;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PrivateSceneRepositoryTest {
    @Autowired
    PrivateSceneRepository privateSceneRepository;

    @Test
    void saveAndFind() {
        PrivateScene scene = PrivateScene.builder()
                .privateSceneId("s1")
                .speaker("sp")
                .backgroundImage("bg")
                .text("txt")
                .isStart(true)
                .isEnd(false)
                .memberId(1L)
                .build();
        privateSceneRepository.save(scene);
        assertThat(privateSceneRepository.findByMemberIdAndPrivateSceneIdAndDeletedAtIsNull(1L, "s1")).isPresent();
    }
}
