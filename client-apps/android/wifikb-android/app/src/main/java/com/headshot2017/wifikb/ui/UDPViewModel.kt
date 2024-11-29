package com.headshot2017.wifikb.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder

class UDPViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(UDPUiState())
    val uiState: StateFlow<UDPUiState> = _uiState.asStateFlow()

    private lateinit var socket: DatagramSocket
    private lateinit var addr: SocketAddress
    private var connected: Boolean = false
    private var stop: Boolean = false
    private var udpJob: Job? = null

    private fun clearLog() {
        _uiState.update { currentState ->
            currentState.copy(
                textLog = ""
            )
        }
    }

    private fun logText(text: String) {
        _uiState.update { currentState ->
            currentState.copy(
                textLog = currentState.textLog + "\n" + text
            )
        }
    }

    private suspend fun connectReverse() {
        logText("Finding DS console...\nIf nothing happens, go back and try again or check your DS console")
        withContext(Dispatchers.IO) {
            socket = DatagramSocket(null)
            socket.soTimeout = 1000
            socket.bind(InetSocketAddress(9091))

            while (!stop) {
                val byteArray = ByteArray(4)
                val packet = DatagramPacket(byteArray, byteArray.size)

                try {
                    socket.receive(packet)
                } catch (e: Exception) {
                    logText("$e")
                    continue
                }

                val buffer = ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN)
                val msg = buffer.get().toInt()

                if (msg == -1) {
                    logText("Found DS console at ${packet.address.hostAddress}")
                    addr = packet.socketAddress
                    connected = true

                    val sendByteArray = ByteArray(4)
                    val sendBuf = ByteBuffer.wrap(sendByteArray).order(ByteOrder.LITTLE_ENDIAN)
                    sendBuf.putInt(-1)

                    socket.send(DatagramPacket(sendByteArray, sendByteArray.size, addr))
                    break
                }
            }

            udpJob = viewModelScope.launch {
                readData()
            }
        }
    }

    private suspend fun connectNonReverse(ip: String) {
        val isBroadcast = (ip == "255.255.255.255")
        withContext(Dispatchers.IO) {
            socket = DatagramSocket(null)
            socket.soTimeout = 1000
            if (isBroadcast) {
                socket.broadcast = true
                logText("Finding DS console...\nIf nothing happens, go back and try again or check your DS console")
            }
            else
                logText("Connecting to $ip...")

            val sendByteArray = ByteArray(4)
            val sendBuf = ByteBuffer.wrap(sendByteArray).order(ByteOrder.LITTLE_ENDIAN)
            sendBuf.putInt(-1)

            while (!stop) {
                socket.send(
                    DatagramPacket(
                        sendByteArray,
                        sendByteArray.size,
                        InetSocketAddress(ip, 9091)
                    )
                )

                val byteArray = ByteArray(4096)
                val packet = DatagramPacket(byteArray, byteArray.size)

                try {
                    socket.receive(packet)
                } catch (e: Exception) {
                    logText("$e")
                    continue
                }

                val msg = byteArray.toString(Charsets.UTF_8).substring(0, packet.length)

                if (msg.startsWith("wifikb")) {
                    logText("Found DS console at ${packet.address.hostAddress}\n$msg")
                    addr = packet.socketAddress
                    connected = true
                    break
                }
            }

            udpJob = viewModelScope.launch {
                readData()
            }
        }
    }

    private suspend fun readData() {
        while (!stop) {
            val byteArray = ByteArray(4096)
            val packet = DatagramPacket(byteArray, byteArray.size)
            var received = false

            withContext(Dispatchers.IO) {
                try {
                    socket.receive(packet)
                    received = true
                } catch (_: Exception) {}
            }

            if (!received) continue

            logText(byteArray.toString(Charsets.UTF_8).substring(0, packet.length))
        }

        withContext(Dispatchers.IO) {
            socket.close()
        }
    }

    fun connect(ip: String, reverse: Boolean) {
        if (connected) return

        clearLog()
        stop = false

        if (reverse) {
            viewModelScope.launch {
                connectReverse()
            }
        }
        else {
            viewModelScope.launch {
                connectNonReverse(ip)
            }
        }
    }

    fun disconnect() {
        connected = false
        stop = true
    }

    fun send(data: String) {
        viewModelScope.launch {
            for (char in data) {
                val sendByteArray = ByteArray(4)
                val sendBuf = ByteBuffer.wrap(sendByteArray).order(ByteOrder.LITTLE_ENDIAN)
                sendBuf.putInt(char.code)

                withContext(Dispatchers.IO) {
                    socket.send(DatagramPacket(sendByteArray, sendByteArray.size, addr))
                }
            }

            val sendByteArray = ByteArray(4)
            val sendBuf = ByteBuffer.wrap(sendByteArray).order(ByteOrder.LITTLE_ENDIAN)
            sendBuf.putInt("\n".first().code) // i could just putInt(10)...

            withContext(Dispatchers.IO) {
                socket.send(DatagramPacket(sendByteArray, sendByteArray.size, addr))
            }
        }
    }
}