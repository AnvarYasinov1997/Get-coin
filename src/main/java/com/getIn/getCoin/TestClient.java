package com.getIn.getCoin;

import java.io.*;
import java.net.Socket;

public class TestClient {
    public static void main(String[] args) throws IOException {
        System.out.println("> Server started");
        Socket socket = new Socket("localhost", 8080);
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        bufferedWriter.write("Hello" + "\n");
        bufferedWriter.flush();
        System.out.println(bufferedReader.readLine());
        socket.close();
    }
}
