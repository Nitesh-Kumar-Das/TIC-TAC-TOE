import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AIBot {
    private Random random;
    private static final int MAX_DEPTH = 9;

    // Positional weight constants
    private static final int CENTER_WEIGHT = 4;
    private static final int CORNER_WEIGHT = 3;
    private static final int EDGE_WEIGHT = 1;
    private static final int TWO_IN_ROW_WEIGHT = 5;
    private static final int FORK_WEIGHT = 8;

    // Board position constants
    private static final int[][] CORNERS = {{0, 0}, {0, 2}, {2, 0}, {2, 2}};
    private static final int[][] EDGES = {{0, 1}, {1, 0}, {1, 2}, {2, 1}};
    private static final int[] CENTER = {1, 1};

    // All possible winning lines (row, col triplets)
    private static final int[][][] WIN_LINES = {
        {{0, 0}, {0, 1}, {0, 2}},  // rows
        {{1, 0}, {1, 1}, {1, 2}},
        {{2, 0}, {2, 1}, {2, 2}},
        {{0, 0}, {1, 0}, {2, 0}},  // columns
        {{0, 1}, {1, 1}, {2, 1}},
        {{0, 2}, {1, 2}, {2, 2}},
        {{0, 0}, {1, 1}, {2, 2}},  // diagonals
        {{0, 2}, {1, 1}, {2, 0}}
    };

    public AIBot() {
        random = new Random();
    }

    // ========================================================================
    // PRIMARY MOVE SELECTION
    // ========================================================================

    /**
     * Returns the best move for the AI using full strategic analysis:
     * 1. Opening book for first moves
     * 2. Immediate win/block checks
     * 3. Fork creation/prevention
     * 4. Minimax with alpha-beta pruning and positional heuristics
     */
    public int[] getBestMove(char[][] board, char aiPlayer) {
        if (board == null) {
            return null;
        }

        List<int[]> availableMoves = getAvailableMoves(board);
        if (availableMoves.isEmpty()) {
            return null;
        }

        char humanPlayer = (aiPlayer == 'O') ? 'X' : 'O';
        int moveCount = 9 - availableMoves.size();

        // Phase 1: Opening book for first 2 moves
        if (moveCount <= 1) {
            int[] openingMove = getOpeningBookMove(board, aiPlayer, moveCount);
            if (openingMove != null) {
                return openingMove;
            }
        }

        // Phase 2: Check for immediate win
        int[] winningMove = findImmediateWin(board, aiPlayer);
        if (winningMove != null) {
            return winningMove;
        }

        // Phase 3: Block immediate opponent win
        int[] blockingMove = findImmediateWin(board, humanPlayer);
        if (blockingMove != null) {
            return blockingMove;
        }

        // Phase 4: Create a fork (two simultaneous winning threats)
        int[] forkMove = findForkMove(board, aiPlayer);
        if (forkMove != null) {
            return forkMove;
        }

        // Phase 5: Block opponent fork
        int[] blockFork = findBlockForkMove(board, aiPlayer, humanPlayer);
        if (blockFork != null) {
            return blockFork;
        }

        // Phase 6: Full minimax with strategic move ordering
        return minimaxBestMove(board, aiPlayer, humanPlayer, availableMoves);
    }

    // ========================================================================
    // OPENING BOOK
    // ========================================================================

    /**
     * Returns optimal opening moves based on known Tic Tac Toe theory.
     * - If AI goes first: always take center
     * - If opponent took center: take a random corner
     * - If opponent took a corner: take center
     * - If opponent took an edge: take center
     */
    private int[] getOpeningBookMove(char[][] board, char aiPlayer, int moveCount) {
        // AI's very first move (board is empty)
        if (moveCount == 0) {
            return new int[]{CENTER[0], CENTER[1]};
        }

        // AI's response to opponent's first move
        if (moveCount == 1) {
            // Opponent went first, this is AI's first move
            if (board[1][1] == ' ') {
                // Center is open — always take it
                return new int[]{1, 1};
            } else {
                // Opponent took center — take a random corner (optimal response)
                List<int[]> openCorners = new ArrayList<>();
                for (int[] corner : CORNERS) {
                    if (board[corner[0]][corner[1]] == ' ') {
                        openCorners.add(corner);
                    }
                }
                if (!openCorners.isEmpty()) {
                    int[] chosen = openCorners.get(random.nextInt(openCorners.size()));
                    return new int[]{chosen[0], chosen[1]};
                }
            }
        }

        return null;
    }

    // ========================================================================
    // FORK DETECTION & PREVENTION
    // ========================================================================

    /**
     * Finds a move that creates a fork — a position where the player has
     * two or more ways to win simultaneously, guaranteeing a win.
     */
    private int[] findForkMove(char[][] board, char player) {
        List<int[]> availableMoves = getAvailableMoves(board);
        int[] bestFork = null;
        int bestForkCount = 0;

        for (int[] move : availableMoves) {
            board[move[0]][move[1]] = player;
            int forkCount = countWinningThreats(board, player);
            board[move[0]][move[1]] = ' ';

            // A fork creates 2+ simultaneous winning threats
            if (forkCount >= 2 && forkCount > bestForkCount) {
                bestForkCount = forkCount;
                bestFork = new int[]{move[0], move[1]};
            }
        }

        return bestFork;
    }

    /**
     * Blocks opponent forks using two strategies:
     * 1. If only one fork position exists, directly block it
     * 2. If multiple fork positions exist, force the opponent to defend
     *    by creating a two-in-a-row threat that doesn't lead into a fork
     */
    private int[] findBlockForkMove(char[][] board, char aiPlayer, char humanPlayer) {
        List<int[]> availableMoves = getAvailableMoves(board);
        List<int[]> opponentForkMoves = new ArrayList<>();

        // Find all positions where opponent can create a fork
        for (int[] move : availableMoves) {
            board[move[0]][move[1]] = humanPlayer;
            int forkCount = countWinningThreats(board, humanPlayer);
            board[move[0]][move[1]] = ' ';

            if (forkCount >= 2) {
                opponentForkMoves.add(move);
            }
        }

        if (opponentForkMoves.isEmpty()) {
            return null;
        }

        // If only one fork spot, block it directly
        if (opponentForkMoves.size() == 1) {
            return opponentForkMoves.get(0);
        }

        // Multiple fork positions — force opponent to defend by creating
        // a two-in-a-row threat, but only if our threat doesn't set up
        // the opponent's fork
        for (int[] move : availableMoves) {
            board[move[0]][move[1]] = aiPlayer;
            int aiThreats = countWinningThreats(board, aiPlayer);
            
            if (aiThreats >= 1) {
                // Check that the opponent's forced response doesn't land on a fork
                int[] opponentBlock = findImmediateWin(board, aiPlayer);
                board[move[0]][move[1]] = ' ';
                
                if (opponentBlock != null) {
                    boolean blockCreatesFork = false;
                    // Simulate opponent blocking our threat
                    board[move[0]][move[1]] = aiPlayer;
                    board[opponentBlock[0]][opponentBlock[1]] = humanPlayer;
                    int forksAfterBlock = countWinningThreats(board, humanPlayer);
                    board[opponentBlock[0]][opponentBlock[1]] = ' ';
                    board[move[0]][move[1]] = ' ';
                    
                    if (forksAfterBlock < 2) {
                        return move;
                    }
                }
            } else {
                board[move[0]][move[1]] = ' ';
            }
        }

        // Fallback: directly block the first fork position
        return opponentForkMoves.get(0);
    }

    /**
     * Counts how many winning threats (two-in-a-row with open third)
     * a player currently has on the board.
     */
    private int countWinningThreats(char[][] board, char player) {
        int threats = 0;
        
        for (int[][] line : WIN_LINES) {
            int playerCount = 0;
            int emptyCount = 0;
            
            for (int[] pos : line) {
                if (board[pos[0]][pos[1]] == player) {
                    playerCount++;
                } else if (board[pos[0]][pos[1]] == ' ') {
                    emptyCount++;
                }
            }
            
            // Two of player's marks + one empty = winning threat
            if (playerCount == 2 && emptyCount == 1) {
                threats++;
            }
        }
        
        return threats;
    }

    /**
     * Counts fork opportunities — positions where playing creates 2+ winning threats.
     */
    public int countForkOpportunities(char[][] board, char player) {
        List<int[]> availableMoves = getAvailableMoves(board);
        int forkCount = 0;

        for (int[] move : availableMoves) {
            board[move[0]][move[1]] = player;
            if (countWinningThreats(board, player) >= 2) {
                forkCount++;
            }
            board[move[0]][move[1]] = ' ';
        }

        return forkCount;
    }

    // ========================================================================
    // MINIMAX WITH ALPHA-BETA PRUNING
    // ========================================================================

    /**
     * Runs minimax on all available moves with strategic ordering
     * (center → corners → edges) for better alpha-beta pruning efficiency.
     */
    private int[] minimaxBestMove(char[][] board, char aiPlayer, char humanPlayer, List<int[]> availableMoves) {
        // Order moves strategically: center, corners, edges
        List<int[]> orderedMoves = getOrderedMoves(availableMoves);

        int[] bestMove = null;
        int bestScore = Integer.MIN_VALUE;

        for (int[] move : orderedMoves) {
            board[move[0]][move[1]] = aiPlayer;
            int score = minimax(board, 0, false, aiPlayer, humanPlayer, Integer.MIN_VALUE, Integer.MAX_VALUE);
            board[move[0]][move[1]] = ' ';

            if (score > bestScore) {
                bestScore = score;
                bestMove = new int[]{move[0], move[1]};
            }
        }

        return bestMove != null ? bestMove : availableMoves.get(0);
    }

    /**
     * Minimax algorithm with alpha-beta pruning and positional heuristic
     * evaluation at leaf/depth-limit nodes.
     */
    private int minimax(char[][] board, int depth, boolean isMaximizing, char aiPlayer, char humanPlayer, int alpha, int beta) {
        char winner = checkWinner(board);

        // Terminal states
        if (winner == aiPlayer) {
            return 100 - depth;  // Prefer faster wins
        } else if (winner == humanPlayer) {
            return depth - 100;  // Prefer slower losses
        } else if (winner == 'D') {
            return 0;
        }

        // Depth limit — use positional heuristic
        if (depth >= MAX_DEPTH) {
            return evaluatePosition(board, aiPlayer, humanPlayer);
        }

        List<int[]> moves = getOrderedMoves(getAvailableMoves(board));

        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (int[] move : moves) {
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
            for (int[] move : moves) {
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

    // ========================================================================
    // MOVE ORDERING
    // ========================================================================

    /**
     * Orders moves by strategic priority: center → corners → edges.
     * This improves alpha-beta pruning by evaluating best candidates first.
     */
    private List<int[]> getOrderedMoves(List<int[]> moves) {
        List<int[]> centerMoves = new ArrayList<>();
        List<int[]> cornerMoves = new ArrayList<>();
        List<int[]> edgeMoves = new ArrayList<>();

        for (int[] move : moves) {
            if (move[0] == 1 && move[1] == 1) {
                centerMoves.add(move);
            } else if (isCorner(move[0], move[1])) {
                cornerMoves.add(move);
            } else {
                edgeMoves.add(move);
            }
        }

        List<int[]> ordered = new ArrayList<>();
        ordered.addAll(centerMoves);
        ordered.addAll(cornerMoves);
        ordered.addAll(edgeMoves);
        return ordered;
    }

    private boolean isCorner(int row, int col) {
        return (row == 0 || row == 2) && (col == 0 || col == 2);
    }

    // ========================================================================
    // POSITIONAL HEURISTIC EVALUATION
    // ========================================================================

    /**
     * Evaluates a non-terminal board position using multiple heuristics:
     * - Positional control (center, corners, edges)
     * - Line threats (two-in-a-row with open third)
     * - Fork opportunities
     */
    private int evaluatePosition(char[][] board, char aiPlayer, char humanPlayer) {
        int score = 0;

        // Positional control
        score += evaluatePositionalControl(board, aiPlayer, humanPlayer);

        // Line-based threats
        score += evaluateLineThreats(board, aiPlayer, humanPlayer);

        // Fork opportunities
        int aiForks = countForkOpportunities(board, aiPlayer);
        int humanForks = countForkOpportunities(board, humanPlayer);
        score += (aiForks - humanForks) * FORK_WEIGHT;

        return score;
    }

    /**
     * Scores based on which positions each player controls.
     */
    private int evaluatePositionalControl(char[][] board, char aiPlayer, char humanPlayer) {
        int score = 0;

        // Center control
        if (board[1][1] == aiPlayer) {
            score += CENTER_WEIGHT;
        } else if (board[1][1] == humanPlayer) {
            score -= CENTER_WEIGHT;
        }

        // Corner control
        for (int[] corner : CORNERS) {
            if (board[corner[0]][corner[1]] == aiPlayer) {
                score += CORNER_WEIGHT;
            } else if (board[corner[0]][corner[1]] == humanPlayer) {
                score -= CORNER_WEIGHT;
            }
        }

        // Edge control
        for (int[] edge : EDGES) {
            if (board[edge[0]][edge[1]] == aiPlayer) {
                score += EDGE_WEIGHT;
            } else if (board[edge[0]][edge[1]] == humanPlayer) {
                score -= EDGE_WEIGHT;
            }
        }

        return score;
    }

    /**
     * Evaluates all 8 winning lines for threats and opportunities.
     * Two-in-a-row with an open third square is worth TWO_IN_ROW_WEIGHT.
     * A single piece in a line with two empties is worth a smaller bonus.
     */
    private int evaluateLineThreats(char[][] board, char aiPlayer, char humanPlayer) {
        int score = 0;

        for (int[][] line : WIN_LINES) {
            int aiCount = 0;
            int humanCount = 0;
            int emptyCount = 0;

            for (int[] pos : line) {
                char cell = board[pos[0]][pos[1]];
                if (cell == aiPlayer) {
                    aiCount++;
                } else if (cell == humanPlayer) {
                    humanCount++;
                } else {
                    emptyCount++;
                }
            }

            // Only score lines that aren't contested (blocked by opponent)
            if (humanCount == 0) {
                // AI-only line
                if (aiCount == 2 && emptyCount == 1) {
                    score += TWO_IN_ROW_WEIGHT;
                } else if (aiCount == 1 && emptyCount == 2) {
                    score += 2;  // Potential line
                }
            }

            if (aiCount == 0) {
                // Human-only line
                if (humanCount == 2 && emptyCount == 1) {
                    score -= TWO_IN_ROW_WEIGHT;
                } else if (humanCount == 1 && emptyCount == 2) {
                    score -= 2;  // Potential threat
                }
            }
        }

        return score;
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    private int[] findImmediateWin(char[][] board, char player) {
        List<int[]> availableMoves = getAvailableMoves(board);

        for (int[] move : availableMoves) {
            board[move[0]][move[1]] = player;

            if (checkWinner(board) == player) {
                board[move[0]][move[1]] = ' ';
                return new int[]{move[0], move[1]};
            }

            board[move[0]][move[1]] = ' ';
        }

        return null;
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
        // Check rows
        for (int i = 0; i < 3; i++) {
            if (board[i][0] != ' ' && board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                return board[i][0];
            }
        }

        // Check columns
        for (int j = 0; j < 3; j++) {
            if (board[0][j] != ' ' && board[0][j] == board[1][j] && board[1][j] == board[2][j]) {
                return board[0][j];
            }
        }

        // Check diagonals
        if (board[0][0] != ' ' && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            return board[0][0];
        }

        if (board[0][2] != ' ' && board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            return board[0][2];
        }

        // Check for draw
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

    // ========================================================================
    // DIFFICULTY SYSTEM
    // ========================================================================

    /**
     * Returns a random move from available positions.
     */
    public int[] getRandomMove(char[][] board) {
        List<int[]> availableMoves = getAvailableMoves(board);
        if (availableMoves.isEmpty()) {
            return null;
        }
        return availableMoves.get(random.nextInt(availableMoves.size()));
    }

    /**
     * Returns a "medium" quality move — blocks immediate wins and takes
     * immediate wins, but otherwise plays semi-randomly.
     */
    private int[] getMediumMove(char[][] board, char aiPlayer) {
        char humanPlayer = (aiPlayer == 'O') ? 'X' : 'O';

        // Always take a winning move
        int[] win = findImmediateWin(board, aiPlayer);
        if (win != null) return win;

        // Always block opponent's winning move
        int[] block = findImmediateWin(board, humanPlayer);
        if (block != null) return block;

        // Take center if available
        if (board[1][1] == ' ') {
            return new int[]{1, 1};
        }

        // Otherwise pick a random corner or edge
        List<int[]> available = getAvailableMoves(board);
        return available.get(random.nextInt(available.size()));
    }

    /**
     * Enhanced difficulty system with 4 tiers:
     *   0 = Easy:       70% random, 30% best move, no fork detection
     *   1 = Medium:     40% random, 60% medium-quality move (blocks/wins only)
     *   2 = Hard:       Full minimax + alpha-beta + positional scoring
     *   3 = Impossible: Full minimax + opening book + fork creation (never loses)
     */
    public int[] getMoveWithDifficulty(char[][] board, char aiPlayer, int difficulty) {
        switch (difficulty) {
            case 0: // Easy
                return random.nextDouble() < 0.7 ? getRandomMove(board) : getMediumMove(board, aiPlayer);

            case 1: // Medium
                return random.nextDouble() < 0.4 ? getRandomMove(board) : getMediumMove(board, aiPlayer);

            case 2: // Hard
                return getBestMove(board, aiPlayer);

            case 3: // Impossible
            default:
                return getBestMove(board, aiPlayer);
        }
    }

    // ========================================================================
    // PUBLIC BOARD EVALUATION (for external use)
    // ========================================================================

    /**
     * Public evaluation of the current board state from the AI's perspective.
     * Uses the full heuristic system including positional control, line threats,
     * and fork opportunities.
     */
    public int evaluateBoard(char[][] board, char aiPlayer) {
        char winner = checkWinner(board);
        char humanPlayer = (aiPlayer == 'O') ? 'X' : 'O';

        if (winner == aiPlayer) {
            return 100;
        } else if (winner != ' ' && winner != 'D') {
            return -100;
        } else if (winner == 'D') {
            return 0;
        }

        return evaluatePosition(board, aiPlayer, humanPlayer);
    }
}
