import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Tic Tac Toe Ultimate — Modern flat UI with warm-neutral palette.
 *
 * SWING RENDERING COMPROMISES:
 *
 * 1. No CSS-level box-shadows: Swing has no native drop-shadow primitive.
 *    Flat design sidesteps this — no shadows are used anywhere.
 *
 * 2. No smooth CSS transitions: All color/size animations are Swing Timer-based
 *    (~16ms discrete steps). Visually smooth at 60 FPS but not GPU-accelerated.
 *
 * 3. Game cells use flat rectangles (not rounded corners) because they tile in a
 *    GridLayout — rounded corners would expose the background through corner gaps.
 *    Only control buttons use the rounded-rectangle subclass.
 *
 * 4. Font fallback: Segoe UI is Windows-specific. On macOS/Linux, the JVM's
 *    logical "SansSerif" font family is used, which maps to the platform default
 *    (SF Pro on macOS, DejaVu Sans on Linux).
 */
public class TicTacToeUI extends JFrame {

    // ========================================================================
    // PALETTE — exactly 5 colors, no others
    // ========================================================================
    private static final Color BG_PRIMARY  = new Color(0xF7, 0xF3, 0xEE);  // #F7F3EE
    private static final Color SURFACE     = new Color(0xE6, 0xD2, 0xB5);  // #E6D2B5
    private static final Color ACCENT      = new Color(0xC4, 0x9A, 0x6C);  // #C49A6C
    private static final Color ACCENT_DARK = new Color(0x8A, 0x5A, 0x44);  // #8A5A44
    private static final Color TEXT_PRIMARY = new Color(0x2F, 0x2F, 0x2F); // #2F2F2F

    // Derived tint for winning cells (lighter blend of ACCENT toward BG_PRIMARY)
    private static final Color WIN_CELL_BG = new Color(0xE2, 0xCC, 0xAD);

    // ========================================================================
    // TYPOGRAPHY
    // ========================================================================
    private static final String FONT_FAMILY = resolveFontFamily();
    private static final Font TITLE_FONT    = new Font(FONT_FAMILY, Font.BOLD, 24);
    private static final Font STATUS_FONT   = new Font(FONT_FAMILY, Font.BOLD, 20);
    private static final Font SCORE_FONT    = new Font(FONT_FAMILY, Font.PLAIN, 15);
    private static final Font BUTTON_FONT   = new Font(FONT_FAMILY, Font.PLAIN, 14);

    private static String resolveFontFamily() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (String name : ge.getAvailableFontFamilyNames()) {
            if (name.equals("Segoe UI")) return "Segoe UI";
        }
        return Font.SANS_SERIF;
    }

    // ========================================================================
    // GAME STATE
    // ========================================================================
    private GameLogic gameLogic;
    private AIBot aiBot;
    private GameCell[][] cells;
    private JLabel statusLabel;
    private JLabel scoreLabel;
    private BoardPanel boardPanel;
    private FlatButton newGameButton;
    private FlatButton modeButton;
    private FlatButton resetScoreButton;
    private boolean isPlayerVsAI;
    private int[][] winLine;  // cached for overlay drawing

    // ========================================================================
    // FLAT BUTTON — custom JButton subclass (requirement #1)
    // ========================================================================

    /**
     * Flat rounded-rectangle button with no default Swing chrome.
     * setContentAreaFilled(false), setBorderPainted(false), setFocusPainted(false).
     * paintComponent draws a filled RoundRectangle2D with 10px corner radius.
     *
     * States (requirement #2):
     *   Default  → #E6D2B5 fill, #2F2F2F text
     *   Hover    → #C49A6C fill, #2F2F2F text
     *   Pressed  → #8A5A44 fill, #F7F3EE text
     *   Active   → same as Pressed (for toggle buttons like mode selector)
     */
    private static class FlatButton extends JButton {
        private boolean isHovered;
        private boolean isActive;
        private static final int CORNER_RADIUS = 10;

        FlatButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(BUTTON_FONT);

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                @Override public void mouseExited(MouseEvent e)  { isHovered = false; repaint(); }
            });
        }

        void setActive(boolean active) {
            this.isActive = active;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            Color bg, fg;
            if (isActive || getModel().isPressed()) {
                bg = ACCENT_DARK;
                fg = BG_PRIMARY;
            } else if (isHovered) {
                bg = ACCENT;
                fg = TEXT_PRIMARY;
            } else {
                bg = SURFACE;
                fg = TEXT_PRIMARY;
            }

            g2.setColor(bg);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(),
                    CORNER_RADIUS, CORNER_RADIUS));

            g2.setColor(fg);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(getText(), x, y);

            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            return new Dimension(fm.stringWidth(getText()) + 40, fm.getHeight() + 22);
        }
    }

    // ========================================================================
    // GAME CELL — flat board square with custom X/O rendering
    // ========================================================================

    /**
     * A flat game cell with no Swing button chrome.
     *
     * Rendering (requirements #3, #4):
     *   Empty default → #F7F3EE fill
     *   Empty hover   → #E6D2B5 tint
     *   X mark        → drawn in #2F2F2F (two crossing lines, 4px stroke)
     *   O mark        → drawn in #8A5A44 (circle ring, 4px stroke)
     *   Winning cell  → lighter tint of #C49A6C background
     */
    private class GameCell extends JButton {
        private char mark = ' ';
        private boolean isWinCell;
        private boolean isHovered;
        private double markScale = 1.0;  // paint-only scale for bounce animation
        @SuppressWarnings("unused")
        private final int row, col;

        GameCell(int row, int col) {
            this.row = row;
            this.col = col;

            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(120, 120));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (mark == ' ' && !gameLogic.isGameOver()) {
                        isHovered = true;
                        repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });

            addActionListener(e -> handleCellClick(row, col));
        }

        void setMark(char m) {
            this.mark = m;
            isHovered = false;
            repaint();
        }

        void setWinCell(boolean win) {
            this.isWinCell = win;
            repaint();
        }

        void setMarkScale(double scale) {
            this.markScale = scale;
            repaint();
        }

        void reset() {
            mark = ' ';
            isWinCell = false;
            isHovered = false;
            markScale = 1.0;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            // Cell background
            if (isWinCell) {
                g2.setColor(WIN_CELL_BG);
            } else if (isHovered && mark == ' ') {
                g2.setColor(SURFACE);
            } else {
                g2.setColor(BG_PRIMARY);
            }
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Mark rendering — scaled from center (paint-only, no layout change)
            int w = getWidth();
            int h = getHeight();

            if (mark != ' ') {
                // Apply scale transform around cell center
                g2.translate(w / 2.0, h / 2.0);
                g2.scale(markScale, markScale);
                g2.translate(-w / 2.0, -h / 2.0);

                int margin = (int) (Math.min(w, h) * 0.27);

                if (mark == 'X') {
                    g2.setColor(TEXT_PRIMARY);
                    g2.setStroke(new BasicStroke(4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(margin, margin, w - margin, h - margin);
                    g2.drawLine(w - margin, margin, margin, h - margin);
                } else {
                    g2.setColor(ACCENT_DARK);
                    g2.setStroke(new BasicStroke(4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawOval(margin, margin, w - margin * 2, h - margin * 2);
                }
            }

            g2.dispose();
        }
    }

    // ========================================================================
    // BOARD PANEL — grid lines + winning line overlay (requirements #3, #5)
    // ========================================================================

    /**
     * Custom panel for the 3x3 cell grid.
     *
     * Grid lines: The panel background is #2F2F2F. GridLayout gaps (2px) expose
     * this background between cells, creating clean 2px grid lines without any
     * default Swing bevel borders.
     *
     * Winning overlay (requirement #5): After children are painted, a 6px #C49A6C
     * line is drawn through the centers of the winning cells.
     */
    private class BoardPanel extends JPanel {
        BoardPanel() {
            setLayout(new GridLayout(3, 3, 2, 2));
            setBackground(TEXT_PRIMARY);
            // 2px padding around all edges so the grid frame matches the inner lines
            setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        }

        @Override
        protected void paintChildren(Graphics g) {
            super.paintChildren(g);

            if (winLine != null && winLine.length == 3) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ACCENT);
                g2.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                Component first = cells[winLine[0][0]][winLine[0][1]];
                Component last  = cells[winLine[2][0]][winLine[2][1]];

                int x1 = first.getX() + first.getWidth() / 2;
                int y1 = first.getY() + first.getHeight() / 2;
                int x2 = last.getX()  + last.getWidth() / 2;
                int y2 = last.getY()  + last.getHeight() / 2;

                g2.drawLine(x1, y1, x2, y2);
                g2.dispose();
            }
        }
    }

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    public TicTacToeUI() {
        gameLogic = new GameLogic();
        aiBot = new AIBot();
        isPlayerVsAI = false;
        initializeUI();
    }

    // ========================================================================
    // UI INITIALIZATION
    // ========================================================================

    private void initializeUI() {
        setTitle("Tic Tac Toe Ultimate");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(BG_PRIMARY);
        setLayout(new BorderLayout());

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createBoardWrapper(), BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Header: #E6D2B5 surface panel with title (left) and mode button (right).
     * No border boxes — padding only (requirement #6 principle).
     */
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(SURFACE);
        header.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        JLabel title = new JLabel("TIC TAC TOE ULTIMATE");
        title.setFont(TITLE_FONT);
        title.setForeground(TEXT_PRIMARY);

        modeButton = new FlatButton("Player vs Player");
        modeButton.addActionListener(e -> toggleMode());

        header.add(title, BorderLayout.WEST);
        header.add(modeButton, BorderLayout.EAST);

        return header;
    }

    /**
     * Board wrapper: #F7F3EE background with centered board panel.
     * Uses GridBagLayout to center the board without stretching it.
     */
    private JPanel createBoardWrapper() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(BG_PRIMARY);
        wrapper.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        boardPanel = new BoardPanel();
        cells = new GameCell[3][3];

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                cells[i][j] = new GameCell(i, j);
                boardPanel.add(cells[i][j]);
            }
        }

        wrapper.add(boardPanel);
        return wrapper;
    }

    /**
     * Footer: #E6D2B5 surface panel with score (left), status (center),
     * and control buttons (right). No border boxes — padding separates
     * sections (requirement #6).
     */
    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout(20, 0));
        footer.setBackground(SURFACE);
        footer.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        // Score (left)
        scoreLabel = new JLabel(getScoreText());
        scoreLabel.setFont(SCORE_FONT);
        scoreLabel.setForeground(TEXT_PRIMARY);

        // Status / turn indicator (center) — 20px+ semi-bold per requirement #7
        statusLabel = new JLabel("Player X's Turn", JLabel.CENTER);
        statusLabel.setFont(STATUS_FONT);
        statusLabel.setForeground(TEXT_PRIMARY);

        // Control buttons (right)
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setOpaque(false);

        newGameButton = new FlatButton("New Game");
        newGameButton.addActionListener(e -> startNewGameWithAnimation());

        resetScoreButton = new FlatButton("Reset Score");
        resetScoreButton.addActionListener(e -> {
            gameLogic.resetScores();
            scoreLabel.setText(getScoreText());
            showNotification("Scores Reset!");
        });

        controls.add(newGameButton);
        controls.add(resetScoreButton);

        footer.add(scoreLabel, BorderLayout.WEST);
        footer.add(statusLabel, BorderLayout.CENTER);
        footer.add(controls, BorderLayout.EAST);

        return footer;
    }

    // ========================================================================
    // MODE TOGGLE
    // ========================================================================

    private void toggleMode() {
        isPlayerVsAI = !isPlayerVsAI;
        if (isPlayerVsAI) {
            modeButton.setText("Player vs AI");
            modeButton.setActive(true);
            showNotification("AI Mode Activated!");
        } else {
            modeButton.setText("Player vs Player");
            modeButton.setActive(false);
            showNotification("2-Player Mode!");
        }
        startNewGameWithAnimation();
    }

    // ========================================================================
    // CELL CLICK HANDLING
    // ========================================================================

    private void handleCellClick(int row, int col) {
        if (!gameLogic.isValidMove(row, col) || gameLogic.isGameOver()) {
            shakeCell(cells[row][col]);
            return;
        }

        char player = gameLogic.getCurrentPlayer();
        gameLogic.makeMove(row, col);
        cells[row][col].setMark(player);
        animateCellPlace(cells[row][col]);

        if (checkGameEnd()) {
            return;
        }

        if (isPlayerVsAI && gameLogic.getCurrentPlayer() == 'O') {
            statusLabel.setText("AI is thinking...");
            statusLabel.setForeground(ACCENT_DARK);
            makeAIMove();
        } else {
            updateStatus();
        }
    }

    // ========================================================================
    // AI MOVE
    // ========================================================================

    private void makeAIMove() {
        SwingUtilities.invokeLater(() -> {
            Timer timer = new Timer(800, e -> {
                int[] aiMove = aiBot.getBestMove(gameLogic.getBoard(), 'O');
                if (aiMove != null) {
                    gameLogic.makeMove(aiMove[0], aiMove[1]);
                    cells[aiMove[0]][aiMove[1]].setMark('O');
                    animateCellPlace(cells[aiMove[0]][aiMove[1]]);
                    checkGameEnd();
                    updateStatus();
                }
            });
            timer.setRepeats(false);
            timer.start();
        });
    }

    // ========================================================================
    // GAME END DETECTION
    // ========================================================================

    private boolean checkGameEnd() {
        char winner = gameLogic.checkWinner();
        if (winner != ' ') {
            if (winner == 'D') {
                statusLabel.setText("It's a Draw!");
                statusLabel.setForeground(ACCENT_DARK);
            } else {
                String text;
                if (isPlayerVsAI) {
                    text = winner == 'X' ? "YOU WIN!" : "AI WINS!";
                } else {
                    text = "PLAYER " + winner + " WINS!";
                }
                statusLabel.setText(text);
                statusLabel.setForeground(winner == 'X' ? TEXT_PRIMARY : ACCENT_DARK);
                highlightWinningLine();
            }
            scoreLabel.setText(getScoreText());
            gameLogic.setGameOver(true);
            celebrateWin();
            return true;
        }
        return false;
    }

    // ========================================================================
    // WINNING LINE HIGHLIGHT (requirement #5)
    // ========================================================================

    /**
     * Animates winning cells one-by-one with #C49A6C-tinted backgrounds,
     * then triggers the BoardPanel to repaint the #C49A6C overlay line.
     */
    private void highlightWinningLine() {
        int[][] line = gameLogic.getWinningLine();
        if (line == null) return;

        this.winLine = line;

        Timer timer = new Timer(120, null);
        final int[] idx = {0};

        timer.addActionListener(e -> {
            if (idx[0] < line.length) {
                cells[line[idx[0]][0]][line[idx[0]][1]].setWinCell(true);
                idx[0]++;
            } else {
                boardPanel.repaint();
                timer.stop();
            }
        });
        timer.start();
    }

    // ========================================================================
    // ANIMATIONS — adapted to palette colors
    // ========================================================================

    /**
     * Subtle scale bounce when a mark is placed.
     * Uses a paint-only Graphics2D transform — no layout change, no squishing.
     */
    private void animateCellPlace(GameCell cell) {
        Timer scaleTimer = new Timer(10, null);
        final long start = System.currentTimeMillis();
        final int duration = 300;

        scaleTimer.addActionListener(e -> {
            double progress = Math.min(1.0, (System.currentTimeMillis() - start) / (double) duration);
            double scale = 1.0 + 0.2 * Math.sin(progress * Math.PI);
            cell.setMarkScale(scale);

            if (progress >= 1.0) {
                cell.setMarkScale(1.0);
                scaleTimer.stop();
            }
        });
        scaleTimer.start();
    }

    /**
     * Horizontal shake for invalid move attempts.
     */
    private void shakeCell(GameCell cell) {
        Point original = cell.getLocation();
        Timer shakeTimer = new Timer(20, null);
        final long start = System.currentTimeMillis();
        final int duration = 300;

        shakeTimer.addActionListener(e -> {
            double progress = (System.currentTimeMillis() - start) / (double) duration;
            if (progress >= 1.0) {
                cell.setLocation(original);
                shakeTimer.stop();
                return;
            }
            int offset = (int) (3 * (1.0 - progress) * Math.sin(progress * Math.PI * 10));
            cell.setLocation(original.x + offset, original.y);
        });
        shakeTimer.start();
    }

    /**
     * Celebration: pulse the status label through palette accent colors
     * and spawn confetti particles.
     */
    private void celebrateWin() {
        Timer pulse = new Timer(50, null);
        final long start = System.currentTimeMillis();
        final int duration = 2500;
        final Font origFont = statusLabel.getFont();
        final Color[] colors = {ACCENT, ACCENT_DARK, TEXT_PRIMARY, ACCENT};

        pulse.addActionListener(e -> {
            double progress = (System.currentTimeMillis() - start) / (double) duration;
            if (progress >= 1.0) {
                statusLabel.setFont(origFont);
                statusLabel.setForeground(TEXT_PRIMARY);
                pulse.stop();
                return;
            }

            // Smooth color cycling through palette accents
            double cp = (progress * colors.length) % colors.length;
            int i1 = (int) cp;
            int i2 = (i1 + 1) % colors.length;
            double blend = cp - i1;
            Color c1 = colors[i1], c2 = colors[i2];
            statusLabel.setForeground(new Color(
                    (int) (c1.getRed() + (c2.getRed() - c1.getRed()) * blend),
                    (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * blend),
                    (int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * blend)
            ));

            // Gentle size pulse
            double scale = 1.0 + 0.12 * Math.sin(progress * Math.PI * 6);
            statusLabel.setFont(new Font(origFont.getName(), origFont.getStyle(),
                    (int) (origFont.getSize() * scale)));
        });

        showConfettiEffect();
        pulse.start();
    }

    /**
     * Spawn confetti particles using only palette colors.
     */
    private void showConfettiEffect() {
        String[] symbols = {"●", "■", "◆", "▲", "★"};
        Color[] colors = {ACCENT, ACCENT_DARK, SURFACE, TEXT_PRIMARY};

        for (int i = 0; i < 8; i++) {
            final int delay = i * 80;
            Timer t = new Timer(delay, null);
            t.setRepeats(false);
            t.addActionListener(ev -> createConfetti(
                    symbols[(int) (Math.random() * symbols.length)],
                    colors[(int) (Math.random() * colors.length)]
            ));
            t.start();
        }
    }

    private void createConfetti(String symbol, Color color) {
        JLabel p = new JLabel(symbol);
        p.setFont(new Font(FONT_FAMILY, Font.BOLD, 14 + (int) (Math.random() * 8)));
        p.setForeground(color);

        int startX = 40 + (int) (Math.random() * (getWidth() - 80));
        final double vx = (Math.random() - 0.5) * 3;
        final double vy = 1.5 + Math.random() * 2;
        p.setBounds(startX, -25, 26, 26);
        getContentPane().add(p, 0);

        Timer anim = new Timer(16, null);
        final long start = System.currentTimeMillis();
        final int duration = 2500 + (int) (Math.random() * 1500);

        anim.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - start;
            double progress = (double) elapsed / duration;

            if (progress >= 1.0) {
                anim.stop();
                getContentPane().remove(p);
                repaint();
                return;
            }

            int nx = (int) (startX + vx * elapsed / 16);
            int ny = (int) (-25 + vy * elapsed / 16 + 0.04 * Math.pow(elapsed / 16.0, 2));
            nx += (int) (Math.sin(elapsed * 0.005) * 10);
            p.setLocation(nx, ny);

            // Fade out in final 25%
            if (progress > 0.75) {
                float alpha = (float) (1.0 - (progress - 0.75) / 0.25);
                p.setForeground(new Color(
                        color.getRed(), color.getGreen(), color.getBlue(),
                        Math.max(0, Math.min(255, (int) (255 * alpha)))
                ));
            }
        });
        anim.start();
    }

    // ========================================================================
    // NEW GAME
    // ========================================================================

    private void startNewGameWithAnimation() {
        Timer transition = new Timer(16, null);
        final long start = System.currentTimeMillis();
        final int duration = 400;
        final boolean[] boardReset = {false};

        transition.addActionListener(e -> {
            double progress = (System.currentTimeMillis() - start) / (double) duration;

            if (progress < 0.5) {
                scaleBoard(1.0 - progress * 0.15);
            } else {
                if (!boardReset[0]) {
                    resetGameBoard();
                    boardReset[0] = true;
                }
                double scale = 0.925 + (progress - 0.5) * 0.15;
                scaleBoard(Math.min(1.0, scale));

                if (progress >= 1.0) {
                    scaleBoard(1.0);
                    transition.stop();
                }
            }
        });
        transition.start();
    }

    private void scaleBoard(double scale) {
        int size = (int) (120 * scale);
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                cells[i][j].setPreferredSize(new Dimension(size, size));
        boardPanel.revalidate();
        boardPanel.repaint();
    }

    private void resetGameBoard() {
        gameLogic.resetGame();
        winLine = null;
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                cells[i][j].reset();
        updateStatus();
    }

    // ========================================================================
    // STATUS & SCORE
    // ========================================================================

    private void updateStatus() {
        if (!gameLogic.isGameOver()) {
            if (isPlayerVsAI) {
                statusLabel.setText(gameLogic.getCurrentPlayer() == 'X' ? "Your Turn" : "AI's Turn");
            } else {
                statusLabel.setText("Player " + gameLogic.getCurrentPlayer() + "'s Turn");
            }
            statusLabel.setForeground(TEXT_PRIMARY);
        }
    }

    private String getScoreText() {
        if (isPlayerVsAI) {
            return String.format("You: %d  ·  AI: %d  ·  Draws: %d",
                    gameLogic.getPlayerXScore(), gameLogic.getPlayerOScore(), gameLogic.getDraws());
        }
        return String.format("X: %d  ·  O: %d  ·  Draws: %d",
                gameLogic.getPlayerXScore(), gameLogic.getPlayerOScore(), gameLogic.getDraws());
    }

    // ========================================================================
    // NOTIFICATIONS
    // ========================================================================

    private void showNotification(String message) {
        JLabel label = new JLabel(message, JLabel.CENTER);
        label.setFont(new Font(FONT_FAMILY, Font.BOLD, 15));
        label.setForeground(BG_PRIMARY);
        label.setOpaque(true);
        label.setBackground(ACCENT);
        label.setBorder(BorderFactory.createEmptyBorder(12, 28, 12, 28));

        JWindow popup = new JWindow();
        popup.add(label);
        popup.pack();
        popup.setLocationRelativeTo(this);
        popup.setVisible(true);

        Timer t = new Timer(1400, e -> popup.dispose());
        t.setRepeats(false);
        t.start();
    }

    // ========================================================================
    // GLOBAL RENDERING HINTS
    // ========================================================================

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        super.paint(g2);
    }

    // ========================================================================
    // ENTRY POINT
    // ========================================================================

    public static void main(String[] args) {
        // Use system look-and-feel for native title bar (requirement #8),
        // but our custom painting overrides all widget rendering
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            TicTacToeUI game = new TicTacToeUI();
            game.setVisible(true);

            Timer welcome = new Timer(800, e -> game.showNotification("Welcome to Tic Tac Toe!"));
            welcome.setRepeats(false);
            welcome.start();
        });
    }
}
