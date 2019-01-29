ARG basePath
ARG baseImage

FROM gradle:jdk8 as builder
MAINTAINER MJG <mehdi.jaqir.pro@gmail.com>

COPY --chown=gradle:gradle $basePath /tmp/app
WORKDIR /tmp/app
RUN gradle clean build -x test
WORKDIR /tmp/app/build/distributions
RUN unzip rpi-projects.zip && rm -rf rpi-projects.zip

#------------------------------------------------------------------------------------------
FROM $baseImage

EXPOSE 9000

COPY --from=builder /tmp/app/build/distributions/rpi-projects /app/
COPY --from=builder /tmp/app/docker/rpi-ws/lib/libwiringPi.so /usr/lib/libwiringPi.so
COPY --from=builder /tmp/app/docker/rpi-ws/lib/libwiringPiDev.so /usr/lib/libwiringPiDev.so

WORKDIR /app
CMD ["-Djava.security.egd=file:/dev/./urandom"]
ENTRYPOINT bin/rpi-pool
