import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class TicTacToeClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 12345;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private JButton[][] buttons = new JButton[3][3];
    private JLabel statusLabel;
    private JButton resetButton;
    private char playerSymbol;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TicTacToeClient::new);
    }

    public TicTacToeClient() {
        try {
            socket = new Socket(SERVER_ADDRESS, PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String response = in.readLine();
            playerSymbol = response.equals("PLAYER_ONE") ? 'X' : 'O';

            createGUI();
            new Thread(this::listenForUpdates).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createGUI() {
        JFrame frame = new JFrame("Tic-Tac-Toe");
        frame.setSize(400, 450);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel boardPanel = new JPanel(new GridLayout(3, 3));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                JButton button = new JButton("");
                buttons[row][col] = button;
                button.setFont(new Font("Arial", Font.BOLD, 40));
                button.addActionListener(e -> makeMove(row, col));
                boardPanel.add(button);
            }
        }

        statusLabel = new JLabel("Your Symbol: " + playerSymbol, SwingConstants.CENTER);
        resetButton = new JButton("Reset Game");
        resetButton.addActionListener(e -> out.println("RESET"));

        frame.add(statusLabel, BorderLayout.NORTH);
        frame.add(boardPanel, BorderLayout.CENTER);
        frame.add(resetButton, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private void makeMove(int row, int col) {
        out.println(row + "," + col);
    }

    private void listenForUpdates() {
        try {
            String input;
            while ((input = in.readLine()) != null) {
                if (input.startsWith("GAME_OVER")) {
                    statusLabel.setText(input.split(",")[1]);
                    continue;
                }
                String[] boardData = input.split(",");
                int index = 0;
                for (int row = 0; row < 3; row++) {
                    for (int col = 0; col < 3; col++) {
                        buttons[row][col].setText(boardData[index++].equals(" ") ? "" : boardData[index - 1]);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
