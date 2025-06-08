# 베이스 이미지 설정
FROM openjdk:21-jdk-slim

ENV TZ=Asia/Seoul
RUN ln -sf /usr/share/zoneinfo/Asia/Seoul /etc/localtime
RUN apt-get update && apt-get install -y curl

# 작업 디렉토리를 설정
WORKDIR /app

# 파일 복사
COPY build/libs/intelliview-0.0.1-SNAPSHOT.jar app.jar

# 커맨드 실행
CMD ["java", "-jar", "app.jar"]
