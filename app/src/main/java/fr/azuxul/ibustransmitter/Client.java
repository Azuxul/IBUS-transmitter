package fr.azuxul.ibustransmitter;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Client {

    private final int port;
    private InetAddress ip;
    private boolean close = false;
    private DatagramSocket datagramSocket;

    public Client(String ip, int port) {
        try {
            this.ip = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.port = port;
    }

    public void stop() {
        if(datagramSocket != null) {
            datagramSocket.close();
            datagramSocket.disconnect();
        }
        close = true;
    }

    public boolean isClose() {
        return close;
    }

    public void start() {

        try {
            datagramSocket = new DatagramSocket(port);
            datagramSocket.setBroadcast(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendData(final byte[] data) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DatagramPacket datagramPacket = new DatagramPacket(data, data.length, ip, port);

                    if(datagramSocket != null) {
                        datagramSocket.send(datagramPacket);
                    } else {
                        start();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }
}
