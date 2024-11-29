# wifikb client program
# by headshot2017

from __future__ import print_function
import socket
import struct
import random

from pynput.keyboard import Key, Listener

try:
    input = raw_input
except NameError:
    pass

# https://github.com/blocksds/libnds/blob/master/include/nds/arm9/keyboard.h#L94
ndsKeyCode = {
    Key.esc:       -23,
    Key.tab:       9,
    Key.backspace: 8,
    Key.caps_lock: -15,
    Key.shift_l:   -14,
    Key.shift_r:   -14,
    Key.space:     32,
    Key.cmd_l:     -5,
    Key.cmd_r:     -5,
    Key.enter:     10,
    Key.ctrl_l:    -16,
    Key.ctrl_r:    -16,
    Key.up:        -17,
    Key.right:     -18,
    Key.down:      -19,
    Key.left:      -20,
    Key.alt_l:     -26,
    Key.alt_r:     -26,
    Key.alt_gr:    -26
}

server = ("255.255.255.255", 9091)
udp = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

def onPress(key):
    global server, udp
    if server == ("255.255.255.255", 9091): return

    keyCode = ndsKeyCode[key] if key in ndsKeyCode else -1
    if keyCode == -1:
        try:
            keyCode = ord(key.char)
        except:
            return

    data = struct.pack("<i", keyCode)
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

    listener = Listener(on_press=onPress)
    listener.start()

    while 1:
        try:
            data, sv = udp.recvfrom(4096)
            data = data.decode("utf8")
        except: continue

        if server != sv: continue

        print(data)


main()
