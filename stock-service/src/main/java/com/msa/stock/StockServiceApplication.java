package com.msa.stock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Stock Service 애플리케이션 진입점. */
@SpringBootApplication
public class StockServiceApplication {

    /**
     * 애플리케이션을 시작한다.
     *
     * @param args 커맨드라인 인수
     */
    public static void main(String[] args) {
        SpringApplication.run(StockServiceApplication.class, args);
    }
}
