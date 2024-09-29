# wifikb client program
# by headshot2017

from __future__ import print_function
import socket
import struct
import random

from pynput.keyboard import Key, Listener

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
    asciiCode = 0
    try:
        asciiCode = ord(key.char)
    except:
        pass
    if keyCode > 0:
        asciiCode = keyCode
        keyCode = -1

    data = struct.pack("<hH", keyCode, asciiCode)
    udp.sendto(data, server)

def main():
    global server, udp
    udp.settimeout(1)
    udp.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)

    print("Waiting for server response...\nIf nothing happens restart the program or check your DS")
    data = ""
    sv = ()
    while 1:
        try:
            udp.sendto("\xff\xff\xff\xff", server) # find the DS server
            data, sv = udp.recvfrom(4096)
            break
        except: pass

    if data.startswith("wifikb"):
        server = sv
        print("Found DS server at {0}".format(server))
        print(data)
    else:
        print("Server not found (unexpected response from {0}: '{1}')".format(sv, data))
        return

    listener = Listener(on_press=onPress)
    listener.start()

    while 1:
        try: data, sv = udp.recvfrom(4096)
        except: continue

        if server != sv: continue

        print(data)


main()
