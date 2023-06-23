from http.server import BaseHTTPRequestHandler, HTTPServer
import cv2

hostName = "localhost"
serverPort = 8080

class MyServer(BaseHTTPRequestHandler):
    def do_GET(self):
        cam_port = 0
        cam = cv2.VideoCapture(cam_port)

        result, image = cam.read()
        if result:
            cv2.imwrite("collection-log.png", image)

        # If captured image is corrupted, moving to else part
        else:
            print("No image detected. Please! try again")
        

if __name__ == "__main__":        
    webServer = HTTPServer((hostName, serverPort), MyServer)
    print("Server started http://%s:%s" % (hostName, serverPort))

    try:
        webServer.serve_forever()
    except KeyboardInterrupt:
        pass

    webServer.server_close()
    print("Server stopped.")