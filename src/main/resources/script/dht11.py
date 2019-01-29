#!/usr/bin/python
# commands to insall dht lib for python
# sudo apt-get update
# sudo apt-get install build-essential python-dev
# git clone https://github.com/adafruit/Adafruit_Python_DHT.git
# cd Adafruit_Python_DHT
# sudo python setup.py install
import argparse
from BaseHTTPServer import BaseHTTPRequestHandler, HTTPServer
from random import randint


class Handler(BaseHTTPRequestHandler):
    def _set_headers(self, code=200):
        self.send_response(code)
        self.send_header('Content-type', 'application/json')
        self.end_headers()

    def do_HEAD(self):
        self._set_headers(200)

    def do_GET(self):
        try:
            values = readDHT11values(self.server.mock)
            self._set_headers(200)
            self.wfile.write(values)
        except ValueError as ex:
            self._set_headers(500)
            self.wfile.write('Error: %s' % ex)
        return


class WsHttpServer:
    def __init__(self, port, mock):
        self.server = HTTPServer(('', port), Handler)
        self.server.mock = mock
        self.server.port = port

    def start(self):
        print 'Start web server on port ', self.server.port
        self.server.serve_forever()

    def stop(self):
        self.server.socket.close()
        print 'Stopped web server'


def runWS(port, mock):
    server = WsHttpServer(port, mock)
    try:
        server.start()
    except KeyboardInterrupt:
        server.stop()


def readDHT11values(mock=True):
    if mock:
        humidity, temperature = randint(10, 55), randint(0, 95)
    else:
        try:
            import Adafruit_DHT
            humidity, temperature = Adafruit_DHT.read_retry(11, 2, 3, 1)
        except:
            raise ValueError("Unable to reach DTH11 sensor")

    if humidity is not None and temperature is not None:
        return "%d|%d" % (temperature, humidity)
    else:
        raise ValueError('Failed to get DHT11 values. Try again!')


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="DHT11 reader script")
    parser.add_argument('--no-ws', dest='ws', action='store_false', help="expose stdout (default)")
    parser.add_argument('--ws', dest='ws', action='store_true', help="expose webservice")
    parser.add_argument('--mock', dest='mock', action='store_true', help="enable sensor mock (default)")
    parser.add_argument('--no-mock', dest='mock', action='store_false', help="disable sensor mock")
    parser.add_argument('--port', default=9090, type=int, help="webservice listening port (default 9090)")
    parser.set_defaults(mock=True, ws=False)

    args = parser.parse_args()

    if args.ws:
        runWS(args.port, args.mock)
    else:
        print "%s" % readDHT11values(args.mock)
