import BaseHTTPServer, SimpleHTTPServer
import ssl
import socket
import os

context = ssl.create_default_context(ssl.Purpose.CLIENT_AUTH)
context.load_cert_chain(os.environ['HEIMDALI_SSL_CERT_LOCATION'], keyfile=os.environ['HEIMDALI_SSL_KEY_LOCATION'], password=os.environ['HEIMDALI_SSL_KEY_PASSWORD'])

httpd = BaseHTTPServer.HTTPServer(('0.0.0.0', int(os.environ['HEIMDALI_UI_PORT'])), SimpleHTTPServer.SimpleHTTPRequestHandler)
httpd.socket = context.wrap_socket(httpd.socket, server_side=True)
httpd.serve_forever()
