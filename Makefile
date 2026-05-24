.PHONY: dev up down build logs ps kafka-ui

# 인프라만 실행 (로컬 Spring Boot 개발 시)
dev:
	docker-compose -f docker-compose.dev.yml up -d

dev-down:
	docker-compose -f docker-compose.dev.yml down

# 전체 실행
up:
	docker-compose up -d

# 전체 종료 (볼륨 삭제)
down:
	docker-compose down -v

# 이미지 빌드
build:
	docker-compose build

# 특정 서비스 재빌드
rebuild:
	docker-compose up -d --build $(svc)

logs:
	docker-compose logs -f $(svc)

ps:
	docker-compose ps

kafka-ui:
	@echo "Kafka UI: http://localhost:8090"

.env:
	cp .env.example .env
	@echo ".env 파일이 생성되었습니다. 내용을 확인하세요."
