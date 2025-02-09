FROM openjdk:17-jdk
WORKDIR /app
COPY . /app

RUN if [ ! -f "/newrelic/newrelic-java.zip" ]; then ./gradlew downloadNewrelic; fi
RUN if [ ! -d "/newrelic" ]; then ./gradlew unzipNewrelic; fi

COPY build/libs/*.jar app.jar

ARG SPRING_PROFILES_ACTIVE
ARG JASYPT_KEY

ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-DEV}
ENV JASYPT_KEY=${JASYPT_KEY:-pw}

EXPOSE 3000

CMD java -javaagent:/newrelic/newrelic.jar -Duser.timezone=Asia/Seoul -jar app.jar
