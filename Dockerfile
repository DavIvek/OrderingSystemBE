# Build
FROM maven:3.8.5-openjdk-17 as builder

RUN mkdir -p /build

WORKDIR /build

COPY pom.xml /build

# Download all required dependencies into one layer
RUN mvn -B dependency:resolve dependency:resolve-plugins

# Copy source code
COPY src /build/src

# Create executable file
RUN mvn package

# Run
FROM openjdk:17-oracle

ENV APP_HOME /ordering-system

RUN mkdir $APP_HOME

# Create folder to save configuration files
RUN mkdir $APP_HOME/config

# Create folder with application logs
RUN mkdir $APP_HOME/log

VOLUME $APP_HOME/log
VOLUME $APP_HOME/config

WORKDIR $APP_HOME

COPY --from=builder /build/target/*.jar app.jar

ENTRYPOINT [ "sh", "-c", "java -Djava.security.egd=file:/dev/./urandom -jar app.jar" ]