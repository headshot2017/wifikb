# wifikb client program (no pynput)
# by headshot2017

from __future__ import print_function
import socket
import struct
import random
import threading

try:
    input = raw_input
except NameError:
    pass

server = ("255.255.255.255", 9091)
udp = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

def typeThread():
    global server, udp
    if server == ("255.255.255.255", 9091): return

    while 1:
        msg = input()
        for c in msg:
            data = struct.pack("<i", ord(c))
            udp.sendto(data, server)
        data = struct.pack("<i", ord("\n"))
        udp.sendto(data, server)

def main():
    global server, udp
    udp.settimeout(1)

    option = 0
    print("wifikb client\nChoose an option:\n1. Find DS automatically\n2. Enter IP address\n3. Reverse connection mode (for emulators)")
    while option == 0:
        try: option = int(input("> "))
        except: pass
        if option < 1 or option > 3: option = 0

    if option == 1:
        udp.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
    elif option == 2:
        server = (input("Enter the IP address of your DS\n> "), 9091)
    elif option == 3:
        udp.bind(("0.0.0.0", 9091))
        print("Waiting for broadcast message from the DS...\nIf nothing happens restart the program or check your DS")
        while 1:
            try:
                data, sv = udp.recvfrom(4)
                if data == b"\xff\xff\xff\xff":
                    server = sv
                    print("Found DS at {0}".format(server))
                    udp.sendto(b"\xff\xff\xff\xff", server)
                    break
            except:
                pass

    if option < 3:
        print("Waiting for server response...\nIf nothing happens restart the program or check your DS")
        data = ""
        sv = ()
        while 1:
            try:
                udp.sendto(b"\xff\xff\xff\xff", server) # find the DS server
                data, sv = udp.recvfrom(4096)
                data = data.decode("utf8")
                break
            except: pass

        if data.startswith("wifikb"):
            server = sv
            print("Found DS at {0}".format(server))
            print(data)
        else:
            print("Server not found (unexpected response from {0}: '{1}')".format(sv, data))
            return

    thd = threading.Thread(target=typeThread)
    thd.start()

    while 1:
        try:
            data, sv = udp.recvfrom(4096)
            data = data.decode("utf8")
        except: continue

        if server != sv: continue

        print(data)


main()
