package local.uniclog.netcore;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class TcpClient {
    private final Socket socket;

    public TcpClient() throws IOException {
        socket = new Socket("localhost", 27001);
    }

    public static void main(String[] args) {
        try {
            TcpClient client = new TcpClient();
            client.start();
            System.out.println("tcp client init...");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void start() {
        try {
            Scanner scanner = new Scanner(System.in);

            OutputStream out = socket.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(out, StandardCharsets.UTF_8);
            PrintWriter printWriter = new PrintWriter(osw, true);
            printWriter.println("hello chat");

            // receive
            Thread handler = new Thread(new ServerHandler());
            handler.start();
            // sender
            while (true) {
                String msg = scanner.nextLine();
                printWriter.println(msg);
                System.out.println("send: " + msg);
                if (msg.equals("exit")) {
                    System.exit(0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class ServerHandler implements Runnable {
        public void run() {
            try {
                InputStream in = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8);
                BufferedReader bufferedReader = new BufferedReader(isr);
                String message = null;
                while ((message = bufferedReader.readLine()) != null) {
                    System.out.println("receive: " + message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
