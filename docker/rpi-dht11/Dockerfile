ARG basePath
ARG baseImage

FROM $baseImage
MAINTAINER MJG <mehdi.jaqir.pro@gmail.com>

COPY $basePath/src/main/resources/script/dht11.py /home/dht11.py

EXPOSE 9090

CMD ["python", "/home/dht11.py", "--ws"]
