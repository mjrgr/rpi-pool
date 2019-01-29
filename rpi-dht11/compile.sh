#!/usr/bin/env bash
#docker run --rm -it -v $PWD:/app golang bash
go get -u -d github.com/pakohan/dht
go generate github.com/pakohan/dht
env GOOS=linux GOARCH=amd64 go build -v -o main main.go

watch ./main