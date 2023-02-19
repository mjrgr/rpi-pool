ARG basePath

FROM azul/zulu-openjdk-alpine:17 as builder
MAINTAINER MJG <mehdi.jaqir.pro@gmail.com>

COPY --chown=gradle:gradle $basePath /tmp/app
WORKDIR /tmp/app
RUN ./gradlew clean build -x test
WORKDIR /tmp/app/build/distributions
RUN unzip rpi-pool.zip && rm -rf rpi-pool.zip

#------------------------------------------------------------------------------------------
FROM azul/zulu-openjdk-alpine:17-jre

EXPOSE 9000

COPY --from=builder /tmp/app/build/distributions/rpi-pool /app/
COPY --from=builder /tmp/app/docker/rpi-ws/lib/libwiringPi.so /usr/lib/libwiringPi.so
COPY --from=builder /tmp/app/docker/rpi-ws/lib/libwiringPiDev.so /usr/lib/libwiringPiDev.so

WORKDIR /app
CMD ["-Djava.security.egd=file:/dev/./urandom"]
ENTRYPOINT bin/rpi-pool
