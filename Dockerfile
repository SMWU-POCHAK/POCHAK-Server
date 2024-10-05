FROM openjdk:17-jdk
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 3000
ARG SPRING_PROFILES_ACTIVE
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-DEV}

CMD java -jar -Duser.timezone=Asia/Seoul app.jar
