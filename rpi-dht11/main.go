package main

import (
	"flag"
	"fmt"
	"log"
	"math/rand"
	"net/http"
	"time"
)

var (
	pin  int
	port int
	mock bool
	ws   bool
)

type SensorData struct {
	temperature int
	humidity    int
}

func init() {
	flag.IntVar(&pin, "pin", 2, "pin number")
	flag.IntVar(&port, "port", 9090, "webservice listening port (default 9090)")
	flag.BoolVar(&mock, "mock", false, "mock sensor")
	flag.BoolVar(&ws, "ws", false, "Start web-service")
	rand.Seed(time.Now().Unix())
	flag.Parse()
}

func main() {
	if ws {
		http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
			_, _ = w.Write([]byte(readSensorData().String()))
		})
		if err := http.ListenAndServe(fmt.Sprintf(":%d", port), nil); err != nil {
			log.Fatal(err)
		}
	} else {
		fmt.Println(readSensorData())
	}
}

func readSensorData() SensorData {
	if mock {
		return SensorData{random(-10, 55), random(0, 95)}
	} else {
		temperature, humidity, retried, err :=
			dht.ReadDHTxxWithRetry(dht.DHT11, 4, false, 10)
		if err != nil {
			log.Fatal(err)
			panic(err)
		}
		// Print temperature and humidity
		fmt.Printf("Temperature = %v*C, Humidity = %v%% (retried %d times)\n",
			temperature, humidity, retried)
		return SensorData{int(temperature), int(humidity)}
	}
}

func (sd SensorData) String() string {
	return fmt.Sprintf("%d|%d", sd.temperature, sd.humidity)
}

func random(min, max int) int {
	return rand.Intn(max-min) + min
}
