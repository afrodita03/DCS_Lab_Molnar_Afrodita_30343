package lab.calculator;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class CalculatorServer {

    public static void main(String[] args) throws IOException {

        ServerSocket ss = null;
        Socket s = null;

        try {
            ss = new ServerSocket(1900);
            System.out.println("Server is waiting for connections...");
            s = ss.accept();
            System.out.println("Client connected.");

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(s.getInputStream()));

            PrintWriter out = new PrintWriter(
                    new BufferedWriter(new OutputStreamWriter(
                            s.getOutputStream())),true);

            while (true) {

                String line = in.readLine();

                if (line == null || line.equals("END")) {
                    break;
                }

                System.out.println("Server received: " + line);

                String[] data = line.split(" ");

                double number1 = Double.parseDouble(data[0]);
                String operation = data[1];
                double number2 = Double.parseDouble(data[2]);

                double result = 0;

                switch (operation) {
                    case "+":
                        result = number1 + number2;
                        break;

                    case "-":
                        result = number1 - number2;
                        break;

                    case "*":
                        result = number1 * number2;
                        break;

                    case "/":
                        result = number1 / number2;
                        break;
                }

                out.println(result);
                out.flush();

                System.out.println("Server sent result: " + result);
            }

            System.out.println("Server application finished.");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ss != null)
                ss.close();

            if (s != null)
                s.close();
        }
    }
}