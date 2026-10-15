package lab.calculator;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class CalculatorClient {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private JTextField firstNumberField;
    private JTextField secondNumberField;
    private JTextField resultField;

    public CalculatorClient() {

        try {
            InetAddress address = InetAddress.getByName("localhost");
            socket = new Socket(address, 1900);

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

            createGUI();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createGUI() {

        JFrame frame = new JFrame("Calculator");
        Font font = new Font("Arial", Font.PLAIN, 35);

        JLabel firstNumberLabel = new JLabel("First number:");
        JLabel secondNumberLabel = new JLabel("Second number:");
        JLabel resultLabel = new JLabel("Result:");

        firstNumberLabel.setFont(font);
        secondNumberLabel.setFont(font);
        resultLabel.setFont(font);

        firstNumberField = new JTextField(10);
        secondNumberField = new JTextField(10);
        resultField = new JTextField(10);

        firstNumberField.setFont(font);
        secondNumberField.setFont(font);
        resultField.setFont(font);

        resultField.setEditable(false);

        JButton addButton = new JButton("+");
        JButton subtractButton = new JButton("-");
        JButton multiplyButton = new JButton("*");
        JButton divideButton = new JButton("/");

        addButton.setFont(font);
        subtractButton.setFont(font);
        multiplyButton.setFont(font);
        divideButton.setFont(font);

        JPanel numbersPanel = new JPanel(new GridLayout(3, 2));

        numbersPanel.add(firstNumberLabel);
        numbersPanel.add(firstNumberField);

        numbersPanel.add(secondNumberLabel);
        numbersPanel.add(secondNumberField);

        numbersPanel.add(resultLabel);
        numbersPanel.add(resultField);

        JPanel buttonsPanel = new JPanel();

        buttonsPanel.add(addButton);
        buttonsPanel.add(subtractButton);
        buttonsPanel.add(multiplyButton);
        buttonsPanel.add(divideButton);

        addButton.addActionListener(e -> calculate("+"));
        subtractButton.addActionListener(e -> calculate("-"));
        multiplyButton.addActionListener(e -> calculate("*"));
        divideButton.addActionListener(e -> calculate("/"));

        frame.add(numbersPanel, BorderLayout.CENTER);
        frame.add(buttonsPanel, BorderLayout.SOUTH);

        frame.setSize(700, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private void calculate(String operation) {

        try {
            double number1 = Double.parseDouble(firstNumberField.getText());
            double number2 = Double.parseDouble(secondNumberField.getText());

            out.println(number1 + " " + operation + " " + number2);
            out.flush();

            String result = in.readLine();

            resultField.setText(result);

        } catch (Exception e) {
            resultField.setText("Error");
        }
    }

    public static void main(String[] args) {
        new CalculatorClient();
    }
}