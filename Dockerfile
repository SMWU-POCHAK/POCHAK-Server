FROM openjdk:17-jdk
WORKDIR /app
COPY build/libs/*.jar app.jar
COPY newrelic/ /app/newrelic/
EXPOSE 5000

ARG SPRING_PROFILES_ACTIVE
ARG JASYPT_KEY
ARG NEW_RELIC_LICENSE_KEY

ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-PROD}
ENV JASYPT_KEY=${JASYPT_KEY:-pw}
ENV NEW_RELIC_LICENSE_KEY=${NEW_RELIC_LICENSE_KEY}


CMD java -javaagent:/app/newrelic/newrelic.jar -Duser.timezone=Asia/Seoul -jar app.jar
