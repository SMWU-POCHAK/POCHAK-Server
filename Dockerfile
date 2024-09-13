FROM openjdk:17-jdk
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 3000
ENV SPRING_PROFILES_ACTIVE=DEV

CMD java -jar -Duser.timezone=Asia/Seoul app.jar
