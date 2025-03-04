import java.io.*;
import java.net.*;
import java.util.*;

public class TicTacToeServer {
    private static final int PORT = 12345;
    private static char[][] board = { {' ', ' ', ' '}, {' ', ' ', ' '}, {' ', ' ', ' '} };
    private static boolean playerOneTurn = true;
    private static List<PrintWriter> clients = new ArrayList<>();
    private static boolean gameOver = false;

    public static void main(String[] args) {
        System.out.println("Tic-Tac-Toe Server is running...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (clients.size() < 2) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private char symbol;
        
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                
                synchronized (clients) {
                    if (clients.size() == 0) {
                        symbol = 'X';
                        out.println("PLAYER_ONE");
                    } else {
                        symbol = 'O';
                        out.println("PLAYER_TWO");
                    }
                    clients.add(out);
                }

                String input;
                while ((input = in.readLine()) != null) {
                    if (input.equals("RESET")) {
                        resetGame();
                        continue;
                    }

                    String[] parts = input.split(",");
                    int row = Integer.parseInt(parts[0]);
                    int col = Integer.parseInt(parts[1]);
                    
                    synchronized (board) {
                        if (gameOver || board[row][col] != ' ' || symbol != (playerOneTurn ? 'X' : 'O')) {
                            out.println("REJECTED");
                        } else {
                            board[row][col] = symbol;
                            playerOneTurn = !playerOneTurn;
                            checkWinner();
                            broadcastBoard();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void broadcastBoard() {
        StringBuilder boardState = new StringBuilder();
        for (char[] row : board) {
            for (char cell : row) {
                boardState.append(cell).append(",");
            }
        }
        for (PrintWriter client : clients) {
            client.println(boardState.toString());
        }
    }

    private static void checkWinner() {
        String winner = null;
        for (int i = 0; i < 3; i++) {
            if (board[i][0] != ' ' && board[i][0] == board[i][1] && board[i][1] == board[i][2]) winner = board[i][0] + " Wins";
            if (board[0][i] != ' ' && board[0][i] == board[1][i] && board[1][i] == board[2][i]) winner = board[0][i] + " Wins";
        }
        if (board[0][0] != ' ' && board[0][0] == board[1][1] && board[1][1] == board[2][2]) winner = board[0][0] + " Wins";
        if (board[0][2] != ' ' && board[0][2] == board[1][1] && board[1][1] == board[2][0]) winner = board[0][2] + " Wins";

        boolean draw = true;
        for (char[] row : board) {
            for (char cell : row) {
                if (cell == ' ') draw = false;
            }
        }
        if (draw) winner = "DRAW";

        if (winner != null) {
            for (PrintWriter client : clients) {
                client.println("GAME_OVER," + winner);
            }
            gameOver = true;
        }
    }

    private static void resetGame() {
        board = new char[][] { {' ', ' ', ' '}, {' ', ' ', ' '}, {' ', ' ', ' '} };
        gameOver = false;
        playerOneTurn = true;
        broadcastBoard();
    }
}
