FROM adoptopenjdk/openjdk8 AS build
RUN sed -i 's/archive.ubuntu.com/mirrors.aliyun.com/' /etc/apt/sources.list

RUN apt-get update && apt-get install -y curl && apt-get install -y procps && apt-get install -y vim
RUN apt-get install -y  maven
WORKDIR /tmp
COPY . /tmp/autojob/
WORKDIR /tmp/autojob
RUN mvn install -DskipTests

FROM adoptopenjdk/openjdk8
RUN sed -i 's/archive.ubuntu.com/mirrors.aliyun.com/' /etc/apt/sources.list

RUN apt-get update && apt-get install -y curl && apt-get install -y procps && apt-get install -y vim


ENV AUTOJOB_DB_TYPE=${AUTOJOB_DB_TYPE} \
    AUTOJOB_DB_URL=${AUTOJOB_DB_URL} \
    AUTOJOB_DB_USERNAME=${AUTOJOB_DB_USERNAME} \
    AUTOJOB_DB_PASSWORD=${AUTOJOB_DB_PASSWORD} \
    AUTOJOB_PASSWORD_ENCRYPT=${AUTOJOB_PASSWORD_ENCRYPT} \
    AUTOJOB_RSA_PUBLIC_KEY=${AUTOJOB_RSA_PUBLIC_KEY} \
    AUTOJOB_RSA_PRIVATE_KEY=${AUTOJOB_RSA_PRIVATE_KEY}

COPY --from=build /tmp/autojob/target/autojob.jar autojob.jar

EXPOSE 8080


CMD ["nohup", "java", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-XX:MaxRAMFraction=2", "-Dserver.port=8080","-jar","autojob.jar"," >/dev/null","&"]


