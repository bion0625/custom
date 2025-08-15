package com.project.base.config;

import org.h2.tools.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class H2WebConsoleConfig {

    // http://localhost:8082 로 접속 → JDBC URL은 애플리케이션과 동일한 H2 URL 입력
    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server h2WebServer() throws java.sql.SQLException {
        // 로컬만 허용: "-webAllowOthers" 제거. 외부 접속 허용하려면 추가.
        return Server.createWebServer("-webPort", "8082", "-webDaemon");
    }

    // (선택) TCP 서버 열기: 외부 툴(DataGrip/DBeaver)에서 TCP로 접속하고 싶을 때
    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server h2TcpServer() throws java.sql.SQLException {
        return Server.createTcpServer("-tcpPort", "9092", "-tcpDaemon", "-tcpAllowOthers");
    }
}
