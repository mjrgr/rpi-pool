FROM golang AS builder
MAINTAINER MJG <mehdi.jaqir.pro@gmail.com>

COPY . /tmp/app
WORKDIR /tmp/app

RUN go mod download
RUN go mod verify
RUN CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -a -ldflags '-extldflags "-static"' -o app .

#------------------------------------------------------------------------------------------
FROM scratch

EXPOSE 9090
COPY --from=builder /tmp/app/app /tmp/app/app
CMD ["/tmp/app/app", "-port", "9090"]
#ENTRYPOINT /go/app
