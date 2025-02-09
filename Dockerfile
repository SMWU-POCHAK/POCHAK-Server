FROM openjdk:17-jdk
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 3000

ARG SPRING_PROFILES_ACTIVE
ARG JASYPT_KEY

ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-DEV}
ENV JASYPT_KEY=${JASYPT_KEY:-pw}

CMD java -javaagent:/newrelic/newrelic.jar -Duser.timezone=Asia/Seoul -jar app.jar