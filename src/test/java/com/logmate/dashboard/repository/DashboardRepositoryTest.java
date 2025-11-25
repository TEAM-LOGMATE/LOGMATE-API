package com.logmate.dashboard.repository;

import com.logmate.dashboard.model.Dashboard;
import com.logmate.folder.model.Folder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class DashboardRepositoryTest {

    @Autowired
    DashboardRepository dashboardRepository;

    @Autowired
    TestEntityManager em;

    @Test
    @DisplayName("findByFolderId - 해당 폴더에 속한 대시보드만 조회")
    void findByFolderId_returnsDashboardsOfFolder() {
        // given
        Folder folder1 = new Folder();
        folder1.setName("folder-1");
        em.persist(folder1);

        Folder folder2 = new Folder();
        folder2.setName("folder-2");
        em.persist(folder2);

        Dashboard d1 = Dashboard.builder()
                .name("dash-1")
                .logPath("/var/log/app1.log")
                .folder(folder1)
                .build();
        em.persist(d1);

        Dashboard d2 = Dashboard.builder()
                .name("dash-2")
                .logPath("/var/log/app2.log")
                .folder(folder1)
                .build();
        em.persist(d2);

        Dashboard dOther = Dashboard.builder()
                .name("dash-other")
                .logPath("/var/log/other.log")
                .folder(folder2)
                .build();
        em.persist(dOther);

        em.flush();
        em.clear();

        // when
        List<Dashboard> result = dashboardRepository.findByFolderId(folder1.getId());

        // then
        assertThat(result)
                .hasSize(2)
                .extracting(Dashboard::getName)
                .containsExactlyInAnyOrder("dash-1", "dash-2");
    }

    @Test
    @DisplayName("findIdsByFolderId - 해당 폴더의 대시보드 ID만 조회")
    void findIdsByFolderId_returnsIdsOnly() {
        // given
        Folder folder = new Folder();
        folder.setName("folder-1");
        em.persist(folder);

        Dashboard d1 = Dashboard.builder()
                .name("dash-1")
                .logPath("/var/log/app1.log")
                .folder(folder)
                .build();
        em.persist(d1);

        Dashboard d2 = Dashboard.builder()
                .name("dash-2")
                .logPath("/var/log/app2.log")
                .folder(folder)
                .build();
        em.persist(d2);

        em.flush();
        em.clear();

        // when
        List<Long> ids = dashboardRepository.findIdsByFolderId(folder.getId());

        // then
        assertThat(ids)
                .hasSize(2)
                .containsExactlyInAnyOrder(d1.getId(), d2.getId());
    }
}
