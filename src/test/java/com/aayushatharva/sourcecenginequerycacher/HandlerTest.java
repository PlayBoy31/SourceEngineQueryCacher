package com.aayushatharva.sourcecenginequerycacher;

import com.aayushatharva.sourcecenginequerycacher.utils.Config;
import com.aayushatharva.sourcecenginequerycacher.utils.Packets;
import io.netty.buffer.ByteBufUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HandlerTest {

    static Main main;
    static byte[] a2sChallenge;

    @BeforeAll
    static void setup() throws InterruptedException {
        main = new Main();
        Main.main(new String[]{"-c", "Cacher.conf"});
        Thread.sleep(2500);
    }

    @AfterAll
    static void shutdown() throws ExecutionException, InterruptedException {
        main.shutdown();
    }

    public static byte[] joinArrays(final byte[] array1, byte[] array2) {
        byte[] joinedArray = Arrays.copyOf(array1, array1.length + array2.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }

    @Test
    @Order(1)
    void A2SINFO() throws IOException {
        DatagramPacket queryPck = new DatagramPacket(ByteBufUtil.getBytes(Packets.A2S_INFO_REQUEST), 0,
                ByteBufUtil.getBytes(Packets.A2S_INFO_REQUEST).length, Config.LocalServer.getAddress(), Config.LocalServer.getPort());
        byte[] responseBytes = new byte[4096];
        DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length);

        DatagramSocket datagramSocket = new DatagramSocket();
        datagramSocket.setSoTimeout(1000);
        datagramSocket.send(queryPck);
        datagramSocket.receive(responsePacket);
        datagramSocket.close();

        Assertions.assertEquals("FFFFFFFF",
                toHexString(Arrays.copyOfRange(responsePacket.getData(), responsePacket.getOffset(), responsePacket.getLength())).substring(0, 8));
    }

    @Test
    @Order(2)
    void A2SPlayerChallenge() throws IOException {
        DatagramPacket queryPck = new DatagramPacket(ByteBufUtil.getBytes(Packets.A2S_PLAYER_CHALLENGE_REQUEST_1), 0,
                ByteBufUtil.getBytes(Packets.A2S_PLAYER_CHALLENGE_REQUEST_1).length, Config.LocalServer.getAddress(), Config.LocalServer.getPort());

        byte[] responseBytes = new byte[4096];
        DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length);

        DatagramSocket datagramSocket = new DatagramSocket();
        datagramSocket.setSoTimeout(1000);
        datagramSocket.send(queryPck);
        datagramSocket.receive(responsePacket);
        datagramSocket.close();

        Assertions.assertEquals("FFFFFFFF41",
                toHexString(Arrays.copyOfRange(responsePacket.getData(), responsePacket.getOffset(), responsePacket.getLength())).substring(0, 10));

        a2sChallenge = Arrays.copyOfRange(Arrays.copyOfRange(responsePacket.getData(), responsePacket.getOffset(), responsePacket.getLength()),
                5, 9);
    }

    @Test
    @Order(3)
    void A2SPlayer() throws IOException {
        byte[] Response = joinArrays(ByteBufUtil.getBytes(Packets.A2S_PLAYER_REQUEST_HEADER), a2sChallenge);
        DatagramPacket queryPck = new DatagramPacket(Response, 0, Response.length,
                Config.LocalServer.getAddress(), Config.LocalServer.getPort());

        byte[] responseBytes = new byte[4096];
        DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length);

        DatagramSocket datagramSocket = new DatagramSocket();
        datagramSocket.setSoTimeout(1000);
        datagramSocket.send(queryPck);
        datagramSocket.receive(responsePacket);
        datagramSocket.close();

        Assertions.assertEquals("FFFFFFFF44",
                toHexString(Arrays.copyOfRange(responsePacket.getData(), responsePacket.getOffset(), responsePacket.getLength())).substring(0, 10));
    }

    /**
     * Convert Byte Array into Hex String
     *
     * @param bytes Byte Array
     * @return Hex String
     */
    private String toHexString(final byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = "0123456789ABCDEF".toCharArray()[v >>> 4];
            hexChars[j * 2 + 1] = "0123456789ABCDEF".toCharArray()[v & 0x0F];
        }
        return new String(hexChars);
    }
}
