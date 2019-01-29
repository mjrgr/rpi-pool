ARG baseImage

FROM golang AS builder
MAINTAINER MJG <mehdi.jaqir.pro@gmail.com>

COPY . /tmp/app
WORKDIR /tmp/app

RUN go get -d -v -u github.com/pakohan/dht
RUN go generate github.com/pakohan/dht
RUN CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -a -ldflags '-extldflags "-static"' -o app .

#------------------------------------------------------------------------------------------
FROM scratch

EXPOSE 9090
COPY --from=builder /tmp/app/app /tmp/app/app
CMD ["/tmp/app/app", "-port", "9090"]
#ENTRYPOINT /go/app
