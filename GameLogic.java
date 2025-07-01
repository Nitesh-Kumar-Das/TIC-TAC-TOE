public class GameLogic {
    private char[][] board;
    private char currentPlayer;
    private boolean gameOver;
    private int playerXScore;
    private int playerOScore;
    private int draws;
    private int[][] winningLine;

    public GameLogic() {
        board = new char[3][3];
        currentPlayer = 'X';
        gameOver = false;
        playerXScore = 0;
        playerOScore = 0;
        draws = 0;
        resetBoard();
    }

    public void resetGame() {
        resetBoard();
        currentPlayer = 'X';
        gameOver = false;
        winningLine = null;
    }

    private void resetBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = ' ';
            }
        }
    }

    public boolean makeMove(int row, int col) {
        if (!isValidMove(row, col)) {
            return false;
        }

        board[row][col] = currentPlayer;
        
        char winner = checkWinner();
        if (winner != ' ') {
            gameOver = true;
            updateScore(winner);
        } else {
            switchPlayer();
        }

        return true;
    }

    public boolean isValidMove(int row, int col) {
        return row >= 0 && row < 3 && col >= 0 && col < 3 && 
               board[row][col] == ' ' && !gameOver;
    }

    private void switchPlayer() {
        currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
    }

    public char checkWinner() {
        for (int i = 0; i < 3; i++) {
            if (board[i][0] != ' ' && board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                winningLine = new int[][]{{i, 0}, {i, 1}, {i, 2}};
                return board[i][0];
            }
        }

        for (int j = 0; j < 3; j++) {
            if (board[0][j] != ' ' && board[0][j] == board[1][j] && board[1][j] == board[2][j]) {
                winningLine = new int[][]{{0, j}, {1, j}, {2, j}};
                return board[0][j];
            }
        }

        if (board[0][0] != ' ' && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            winningLine = new int[][]{{0, 0}, {1, 1}, {2, 2}};
            return board[0][0];
        }

        if (board[0][2] != ' ' && board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            winningLine = new int[][]{{0, 2}, {1, 1}, {2, 0}};
            return board[0][2];
        }

        if (isBoardFull()) {
            return 'D';
        }

        return ' ';
    }

    private boolean isBoardFull() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == ' ') {
                    return false;
                }
            }
        }
        return true;
    }

    private void updateScore(char winner) {
        if (winner == 'X') {
            playerXScore++;
        } else if (winner == 'O') {
            playerOScore++;
        } else if (winner == 'D') {
            draws++;
        }
    }

    public char simulateMove(int row, int col, char player) {
        if (!isValidMove(row, col)) {
            return ' ';
        }

        char[][] tempBoard = new char[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                tempBoard[i][j] = board[i][j];
            }
        }

        tempBoard[row][col] = player;

        return checkWinnerOnBoard(tempBoard);
    }

    private char checkWinnerOnBoard(char[][] gameBoard) {
        for (int i = 0; i < 3; i++) {
            if (gameBoard[i][0] != ' ' && gameBoard[i][0] == gameBoard[i][1] && gameBoard[i][1] == gameBoard[i][2]) {
                return gameBoard[i][0];
            }
        }

        for (int j = 0; j < 3; j++) {
            if (gameBoard[0][j] != ' ' && gameBoard[0][j] == gameBoard[1][j] && gameBoard[1][j] == gameBoard[2][j]) {
                return gameBoard[0][j];
            }
        }

        if (gameBoard[0][0] != ' ' && gameBoard[0][0] == gameBoard[1][1] && gameBoard[1][1] == gameBoard[2][2]) {
            return gameBoard[0][0];
        }

        if (gameBoard[0][2] != ' ' && gameBoard[0][2] == gameBoard[1][1] && gameBoard[1][1] == gameBoard[2][0]) {
            return gameBoard[0][2];
        }

        boolean full = true;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (gameBoard[i][j] == ' ') {
                    full = false;
                    break;
                }
            }
            if (!full) break;
        }

        return full ? 'D' : ' ';
    }

    public int getEmptySpaces() {
        int count = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == ' ') {
                    count++;
                }
            }
        }
        return count;
    }

    public char[][] getBoard() {
        return board;
    }

    public char getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public int getPlayerXScore() {
        return playerXScore;
    }

    public int getPlayerOScore() {
        return playerOScore;
    }

    public int getDraws() {
        return draws;
    }

    public int[][] getWinningLine() {
        return winningLine;
    }

    public void resetScores() {
        playerXScore = 0;
        playerOScore = 0;
        draws = 0;
    }

    public void printBoard() {
        System.out.println("Current Board:");
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                System.out.print("[" + (board[i][j] == ' ' ? " " : board[i][j]) + "]");
            }
            System.out.println();
        }
        System.out.println("Current Player: " + currentPlayer);
        System.out.println("Game Over: " + gameOver);
        System.out.println();
    }
}
