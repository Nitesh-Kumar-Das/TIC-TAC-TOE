import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AIBot {
    private Random random;
    private static final int MAX_DEPTH = 9;

    public AIBot() {
        random = new Random();
    }

    public int[] getBestMove(char[][] board, char aiPlayer) {
        if (board == null) {
            return null;
        }

        List<int[]> availableMoves = getAvailableMoves(board);
        if (availableMoves.isEmpty()) {
            return null;
        }

        if (availableMoves.size() == 9) {
            return getOpeningMove();
        }

        char humanPlayer = (aiPlayer == 'O') ? 'X' : 'O';
        
        int[] winningMove = findImmediateWin(board, aiPlayer);
        if (winningMove != null) {
            return winningMove;
        }

        int[] blockingMove = findImmediateWin(board, humanPlayer);
        if (blockingMove != null) {
            return blockingMove;
        }
        int[] bestMove = null;
        int bestScore = Integer.MIN_VALUE;

        for (int[] move : availableMoves) {
            int row = move[0];
            int col = move[1];
            
            board[row][col] = aiPlayer;
            
            int score = minimax(board, 0, false, aiPlayer, humanPlayer, Integer.MIN_VALUE, Integer.MAX_VALUE);
            
            board[row][col] = ' ';
            
            if (score > bestScore) {
                bestScore = score;
                bestMove = new int[]{row, col};
            }
        }

        return bestMove != null ? bestMove : availableMoves.get(0);
    }

    private int minimax(char[][] board, int depth, boolean isMaximizing, char aiPlayer, char humanPlayer, int alpha, int beta) {
        char winner = checkWinner(board);
        
        if (winner == aiPlayer) {
            return 10 - depth;
        } else if (winner == humanPlayer) {
            return depth - 10;
        } else if (winner == 'D' || depth >= MAX_DEPTH) {
            return 0;
        }

        List<int[]> availableMoves = getAvailableMoves(board);
        
        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (int[] move : availableMoves) {
                board[move[0]][move[1]] = aiPlayer;
                int eval = minimax(board, depth + 1, false, aiPlayer, humanPlayer, alpha, beta);
                board[move[0]][move[1]] = ' ';
                
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                
                if (beta <= alpha) {
                    break;
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (int[] move : availableMoves) {
                board[move[0]][move[1]] = humanPlayer;
                int eval = minimax(board, depth + 1, true, aiPlayer, humanPlayer, alpha, beta);
                board[move[0]][move[1]] = ' ';
                
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                
                if (beta <= alpha) {
                    break;
                }
            }
            return minEval;
        }
    }

    private int[] findImmediateWin(char[][] board, char player) {
        List<int[]> availableMoves = getAvailableMoves(board);
        
        for (int[] move : availableMoves) {
            int row = move[0];
            int col = move[1];
            
            board[row][col] = player;
            
            if (checkWinner(board) == player) {
                board[row][col] = ' ';
                return new int[]{row, col};
            }
            
            board[row][col] = ' ';
        }
        
        return null;
    }

    private int[] getOpeningMove() {
        if (random.nextBoolean()) {
            return new int[]{1, 1};
        } else {
            int[][] corners = {{0, 0}, {0, 2}, {2, 0}, {2, 2}};
            return corners[random.nextInt(corners.length)];
        }
    }

    private List<int[]> getAvailableMoves(char[][] board) {
        List<int[]> moves = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == ' ') {
                    moves.add(new int[]{i, j});
                }
            }
        }
        return moves;
    }

    private char checkWinner(char[][] board) {
        for (int i = 0; i < 3; i++) {
            if (board[i][0] != ' ' && board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                return board[i][0];
            }
        }

        for (int j = 0; j < 3; j++) {
            if (board[0][j] != ' ' && board[0][j] == board[1][j] && board[1][j] == board[2][j]) {
                return board[0][j];
            }
        }

        if (board[0][0] != ' ' && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            return board[0][0];
        }

        if (board[0][2] != ' ' && board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            return board[0][2];
        }

        boolean boardFull = true;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == ' ') {
                    boardFull = false;
                    break;
                }
            }
            if (!boardFull) break;
        }

        return boardFull ? 'D' : ' ';
    }

    public int[] getRandomMove(char[][] board) {
        List<int[]> availableMoves = getAvailableMoves(board);
        if (availableMoves.isEmpty()) {
            return null;
        }
        return availableMoves.get(random.nextInt(availableMoves.size()));
    }

    public int[] getMoveWithDifficulty(char[][] board, char aiPlayer, int difficulty) {
        switch (difficulty) {
            case 0:
                return random.nextDouble() < 0.8 ? getRandomMove(board) : getBestMove(board, aiPlayer);
            
            case 1:
                return random.nextDouble() < 0.5 ? getRandomMove(board) : getBestMove(board, aiPlayer);
            
            case 2:
            default:
                return getBestMove(board, aiPlayer);
        }
    }

    public int evaluateBoard(char[][] board, char aiPlayer) {
        char winner = checkWinner(board);
        
        if (winner == aiPlayer) {
            return 10;
        } else if (winner != ' ' && winner != 'D') {
            return -10;
        } else if (winner == 'D') {
            return 0;
        }

        int score = 0;
        
        if (board[1][1] == aiPlayer) {
            score += 3;
        } else if (board[1][1] != ' ') {
            score -= 3;
        }

        int[][] corners = {{0, 0}, {0, 2}, {2, 0}, {2, 2}};
        for (int[] corner : corners) {
            if (board[corner[0]][corner[1]] == aiPlayer) {
                score += 2;
            } else if (board[corner[0]][corner[1]] != ' ') {
                score -= 2;
            }
        }

        return score;
    }
}
