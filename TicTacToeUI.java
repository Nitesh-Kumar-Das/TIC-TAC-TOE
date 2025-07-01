import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;

public class TicTacToeUI extends JFrame {
    private GameLogic gameLogic;
    private AIBot aiBot;
    private JButton[][] buttons;
    private JLabel statusLabel;
    private JLabel scoreLabel;
    private JButton newGameButton;
    private JButton modeButton;
    private JButton resetScoreButton;
    private JPanel gamePanel;
    private boolean isPlayerVsAI;
    
    private Color primaryColor = new Color(45, 52, 54);
    private Color secondaryColor = new Color(99, 110, 114);
    private Color accentColor = new Color(0, 184, 148);
    private Color dangerColor = new Color(255, 118, 117);
    private Color warningColor = new Color(255, 177, 66);
    private Color successColor = new Color(85, 239, 196);
    private Color backgroundColor = new Color(223, 228, 234);
    private Color cardColor = Color.WHITE;
    
    private Font titleFont = new Font("Segoe UI", Font.BOLD, 28);
    private Font gameFont = new Font("Segoe UI", Font.BOLD, 36);
    private Font uiFont = new Font("Segoe UI", Font.PLAIN, 14);
    private Font statusFont = new Font("Segoe UI", Font.BOLD, 18);
    private Font scoreFont = new Font("Segoe UI", Font.PLAIN, 16);

    public TicTacToeUI() {
        gameLogic = new GameLogic();
        aiBot = new AIBot();
        isPlayerVsAI = false;
        
        initializeUI();
        setupEventListeners();
        addWindowEffects();
    }

    private void initializeUI() {
        setTitle("Modern Tic Tac Toe - Ultimate Edition");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setResizable(false);

        getContentPane().setBackground(backgroundColor);

        JPanel headerPanel = createModernHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        gamePanel = createModernGamePanel();
        add(gamePanel, BorderLayout.CENTER);

        JPanel footerPanel = createModernFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);

        JPanel mainWrapper = new JPanel(new BorderLayout());
        mainWrapper.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainWrapper.setBackground(backgroundColor);
        
        pack();
        setLocationRelativeTo(null);
        
        setVisible(false);
    }

    private JPanel createModernHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBackground(primaryColor);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));

        JLabel titleLabel = new JLabel("TIC TAC TOE ULTIMATE", JLabel.CENTER);
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(Color.WHITE);
        
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        modeButton = createModernButton("Player vs Player", accentColor);
        modeButton.setPreferredSize(new Dimension(180, 40));

        addHoverEffect(modeButton, accentColor, new Color(0, 206, 166));

        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(modeButton, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createModernGamePanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(backgroundColor);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JPanel cardPanel = new JPanel(new BorderLayout());
        cardPanel.setBackground(cardColor);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));

        JPanel gridPanel = new JPanel(new GridLayout(3, 3, 8, 8));
        gridPanel.setBackground(secondaryColor);
        gridPanel.setBorder(BorderFactory.createLoweredBevelBorder());

        buttons = new JButton[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j] = createModernGameButton();
                
                final int row = i;
                final int col = j;
                buttons[i][j].addActionListener(e -> handleButtonClick(row, col));
                
                gridPanel.add(buttons[i][j]);
            }
        }

        cardPanel.add(gridPanel, BorderLayout.CENTER);
        mainPanel.add(cardPanel, BorderLayout.CENTER);

        return mainPanel;
    }

    private JButton createModernGameButton() {
        JButton button = new JButton("");
        button.setFont(gameFont);
        button.setBackground(cardColor);
        button.setForeground(primaryColor);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(100, 100));
        
        addGameButtonHoverEffect(button);
        
        return button;
    }

    private JPanel createModernFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout(15, 0));
        footerPanel.setBackground(backgroundColor);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 25, 25));

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        statusPanel.setBackground(cardColor);
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        statusLabel = new JLabel("Player X's Turn", JLabel.CENTER);
        statusLabel.setFont(statusFont);
        statusLabel.setForeground(primaryColor);
        statusPanel.add(statusLabel);

        JPanel scorePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        scorePanel.setBackground(cardColor);
        scorePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        scoreLabel = new JLabel(getScoreText(), JLabel.LEFT);
        scoreLabel.setFont(scoreFont);
        scoreLabel.setForeground(secondaryColor);
        scorePanel.add(scoreLabel);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controlPanel.setBackground(backgroundColor);

        newGameButton = createModernButton("New Game", warningColor);
        resetScoreButton = createModernButton("Reset Score", dangerColor);

        addHoverEffect(newGameButton, warningColor, new Color(255, 159, 26));
        addHoverEffect(resetScoreButton, dangerColor, new Color(255, 99, 99));

        controlPanel.add(newGameButton);
        controlPanel.add(resetScoreButton);

        footerPanel.add(scorePanel, BorderLayout.WEST);
        footerPanel.add(statusPanel, BorderLayout.CENTER);
        footerPanel.add(controlPanel, BorderLayout.EAST);

        return footerPanel;
    }

    private JButton createModernButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(uiFont);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        return button;
    }

    private void addHoverEffect(JButton button, Color normalColor, Color hoverColor) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverColor);
                button.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(normalColor);
                button.repaint();
            }
        });
    }

    private void addGameButtonHoverEffect(JButton button) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.getText().isEmpty()) {
                    button.setBackground(new Color(240, 248, 255));
                    button.setText("?");
                    button.setForeground(new Color(169, 169, 169));
                    button.repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (button.getText().equals("?")) {
                    button.setText("");
                    button.setBackground(cardColor);
                    button.repaint();
                }
            }
        });
    }

    private void addWindowEffects() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }

    private void setupEventListeners() {
        newGameButton.addActionListener(e -> startNewGameWithAnimation());
        
        resetScoreButton.addActionListener(e -> {
            gameLogic.resetScores();
            scoreLabel.setText(getScoreText());
            showNotification("Scores Reset!", successColor);
        });
        
        modeButton.addActionListener(e -> {
            isPlayerVsAI = !isPlayerVsAI;
            if (isPlayerVsAI) {
                modeButton.setText("Player vs AI");
                showNotification("AI Mode Activated!", accentColor);
            } else {
                modeButton.setText("Player vs Player");
                showNotification("2-Player Mode!", accentColor);
            }
            startNewGameWithAnimation();
        });
    }

    private void handleButtonClick(int row, int col) {
        if (!gameLogic.isValidMove(row, col) || gameLogic.isGameOver()) {
            shakeButton(buttons[row][col]);
            return;
        }

        char currentPlayerChar = gameLogic.getCurrentPlayer();
        gameLogic.makeMove(row, col);
        animateButtonClick(row, col, currentPlayerChar);

        if (checkGameEnd()) {
            return;
        }

        if (isPlayerVsAI && gameLogic.getCurrentPlayer() == 'O') {
            statusLabel.setText("AI is thinking...");
            statusLabel.setForeground(warningColor);
            makeAIMove();
        } else {
            updateStatus();
        }
    }

    private void animateButtonClick(int row, int col, char player) {
        JButton button = buttons[row][col];
        
        button.setText(String.valueOf(player));
        button.setFont(gameFont);
        
        if (player == 'X') {
            button.setForeground(new Color(52, 152, 219));
            button.setText("X");
            showOptimizedSparkleEffect(button, new Color(135, 206, 250));
        } else {
            button.setForeground(new Color(231, 76, 60));
            button.setText("O");
            showOptimizedGlowEffect(button, new Color(255, 182, 193));
        }
        
        animateButtonScale(button);
    }

    private void animateButtonScale(JButton button) {
        Timer scaleTimer = new Timer(8, null);
        final long startTime = System.currentTimeMillis();
        final int duration = 400;
        final int originalSize = 100;
        final int maxSize = 110;
        
        scaleTimer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            double progress = Math.min(1.0, (double) elapsed / duration);
            
            double easedProgress;
            if (progress < 0.5) {
                easedProgress = 2 * progress * progress * ((1.7 + 1) * progress - 1.7);
            } else {
                easedProgress = 1 + 2 * (progress - 1) * (progress - 1) * ((1.7 + 1) * (progress - 1) + 1.7);
            }
            
            int currentSize = originalSize + (int)((maxSize - originalSize) * Math.sin(easedProgress * Math.PI));
            button.setPreferredSize(new Dimension(currentSize, currentSize));
            button.revalidate();
            
            if (progress >= 1.0) {
                button.setPreferredSize(new Dimension(originalSize, originalSize));
                button.revalidate();
                scaleTimer.stop();
            }
        });
        scaleTimer.start();
    }

    private void showOptimizedSparkleEffect(JButton button, Color sparkleColor) {
        Timer sparkleTimer = new Timer(60, null);
        final long startTime = System.currentTimeMillis();
        final int duration = 600;
        final Border originalBorder = button.getBorder();
        
        sparkleTimer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            double progress = (double) elapsed / duration;
            
            if (progress < 1.0) {
                int alpha = (int)(128 + 127 * Math.sin(progress * Math.PI * 4));
                Color pulseColor = new Color(sparkleColor.getRed(), sparkleColor.getGreen(), 
                                           sparkleColor.getBlue(), alpha);
                button.setBorder(BorderFactory.createLineBorder(pulseColor, 2));
            } else {
                button.setBorder(originalBorder);
                sparkleTimer.stop();
            }
        });
        sparkleTimer.start();
    }

    private void showOptimizedGlowEffect(JButton button, Color glowColor) {
        Timer glowTimer = new Timer(50, null);
        final long startTime = System.currentTimeMillis();
        final int duration = 800;
        final Color originalBg = button.getBackground();
        
        glowTimer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            double progress = (double) elapsed / duration;
            
            if (progress < 1.0) {
                double intensity = 0.5 + 0.5 * Math.sin(progress * Math.PI * 3);
                int red = (int)(originalBg.getRed() + (glowColor.getRed() - originalBg.getRed()) * intensity);
                int green = (int)(originalBg.getGreen() + (glowColor.getGreen() - originalBg.getGreen()) * intensity);
                int blue = (int)(originalBg.getBlue() + (glowColor.getBlue() - originalBg.getBlue()) * intensity);
                
                button.setBackground(new Color(Math.max(0, Math.min(255, red)),
                                             Math.max(0, Math.min(255, green)),
                                             Math.max(0, Math.min(255, blue))));
            } else {
                button.setBackground(originalBg);
                glowTimer.stop();
            }
        });
        glowTimer.start();
    }

    private void shakeButton(JButton button) {
        Point originalLocation = button.getLocation();
        Timer shakeTimer = new Timer(20, null);
        final long startTime = System.currentTimeMillis();
        final int duration = 400;
        final int maxShake = 4;
        
        shakeTimer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            double progress = (double) elapsed / duration;
            
            if (progress >= 1.0) {
                button.setLocation(originalLocation);
                shakeTimer.stop();
                return;
            }
            
            double amplitude = maxShake * (1.0 - progress) * Math.sin(progress * Math.PI * 12);
            int offsetX = (int) amplitude;
            
            button.setLocation(originalLocation.x + offsetX, originalLocation.y);
        });
        shakeTimer.start();
    }

    private void makeAIMove() {
        SwingUtilities.invokeLater(() -> {
            Timer timer = new Timer(1000, e -> {
                int[] aiMove = aiBot.getBestMove(gameLogic.getBoard(), 'O');
                if (aiMove != null) {
                    gameLogic.makeMove(aiMove[0], aiMove[1]);
                    animateButtonClick(aiMove[0], aiMove[1], 'O');
                    
                    JButton aiButton = buttons[aiMove[0]][aiMove[1]];
                    aiButton.setBackground(new Color(255, 235, 235));
                    
                    showAIThinkingEffect(aiButton);
                    
                    Timer resetTimer = new Timer(200, null);
                    resetTimer.addActionListener(resetEvent -> {
                        aiButton.setBackground(cardColor);
                        resetTimer.stop();
                    });
                    resetTimer.setRepeats(false);
                    resetTimer.start();
                    
                    checkGameEnd();
                    updateStatus();
                }
            });
            timer.setRepeats(false);
            timer.start();
        });
    }

    private void showAIThinkingEffect(JButton button) {
        String[] thinkingSymbols = {".", "..", "...", "....", ".....", "....."};
        Timer thinkTimer = new Timer(150, null);
        final int[] symbolIndex = {0};
        final JLabel thinkLabel = new JLabel();
        
        thinkLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        thinkLabel.setForeground(warningColor);
        thinkLabel.setHorizontalAlignment(JLabel.CENTER);
        
        Point buttonLocation = button.getLocationOnScreen();
        Point panelLocation = gamePanel.getLocationOnScreen();
        int relativeX = buttonLocation.x - panelLocation.x + 25;
        int relativeY = buttonLocation.y - panelLocation.y - 25;
        
        thinkLabel.setBounds(relativeX, relativeY, 50, 25);
        gamePanel.add(thinkLabel, 0);
        
        thinkTimer.addActionListener(e -> {
            if (symbolIndex[0] < thinkingSymbols.length) {
                thinkLabel.setText(thinkingSymbols[symbolIndex[0]]);
                
                double pulse = 1.0 + 0.2 * Math.sin(symbolIndex[0] * Math.PI / 2);
                Font currentFont = thinkLabel.getFont();
                thinkLabel.setFont(new Font(currentFont.getName(), currentFont.getStyle(), 
                                          (int)(16 * pulse)));
                
                symbolIndex[0]++;
                gamePanel.repaint();
            } else {
                thinkTimer.stop();
                gamePanel.remove(thinkLabel);
                gamePanel.repaint();
            }
        });
        thinkTimer.start();
    }

    private boolean checkGameEnd() {
        char winner = gameLogic.checkWinner();
        if (winner != ' ') {
            if (winner == 'D') {
                statusLabel.setText("It's a Draw!");
                statusLabel.setForeground(warningColor);
                showWinAnimation(null);
            } else {
                String winnerText;
                if (isPlayerVsAI) {
                    winnerText = winner == 'X' ? "YOU WIN!" : "AI WINS!";
                } else {
                    winnerText = "PLAYER " + winner + " WINS!";
                }
                statusLabel.setText(winnerText);
                statusLabel.setForeground(winner == 'X' ? accentColor : dangerColor);
                highlightWinningLineAnimated();
            }
            scoreLabel.setText(getScoreText());
            gameLogic.setGameOver(true);
            
            celebrateWin();
            
            return true;
        }
        return false;
    }

    private void highlightWinningLineAnimated() {
        int[][] winningLine = gameLogic.getWinningLine();
        if (winningLine != null) {
            Timer highlightTimer = new Timer(100, null);
            final int[] index = {0};
            
            highlightTimer.addActionListener(e -> {
                if (index[0] < winningLine.length) {
                    int[] pos = winningLine[index[0]];
                    JButton button = buttons[pos[0]][pos[1]];
                    
                    animateButtonHighlight(button, successColor, accentColor);
                    index[0]++;
                } else {
                    highlightTimer.stop();
                }
            });
            highlightTimer.start();
        }
    }
    
    private void animateButtonHighlight(JButton button, Color targetBg, Color targetBorder) {
        final Color originalBg = button.getBackground();
        
        Timer highlightTimer = new Timer(20, null);
        final long startTime = System.currentTimeMillis();
        final int duration = 300;
        
        highlightTimer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            double progress = Math.min(1.0, (double) elapsed / duration);
            
            int red = (int)(originalBg.getRed() + (targetBg.getRed() - originalBg.getRed()) * progress);
            int green = (int)(originalBg.getGreen() + (targetBg.getGreen() - originalBg.getGreen()) * progress);
            int blue = (int)(originalBg.getBlue() + (targetBg.getBlue() - originalBg.getBlue()) * progress);
            
            button.setBackground(new Color(red, green, blue));
            
            if (progress >= 1.0) {
                button.setBorder(BorderFactory.createLineBorder(targetBorder, 3));
                highlightTimer.stop();
            }
        });
        highlightTimer.start();
    }

    private void celebrateWin() {
        Timer pulseTimer = new Timer(60, null);
        final long startTime = System.currentTimeMillis();
        final int duration = 3000;
        final Font originalFont = statusLabel.getFont();
        final Color[] celebrationColors = {
            new Color(255, 99, 99),
            new Color(255, 159, 26),
            new Color(255, 206, 84),
            new Color(85, 239, 196),
            new Color(116, 185, 255),
            new Color(162, 155, 254)
        };
        
        pulseTimer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            double progress = (double) elapsed / duration;
            
            if (progress >= 1.0) {
                statusLabel.setFont(originalFont);
                statusLabel.setForeground(primaryColor);
                pulseTimer.stop();
                return;
            }
            
            double colorProgress = (progress * celebrationColors.length) % celebrationColors.length;
            int colorIndex1 = (int) colorProgress;
            int colorIndex2 = (colorIndex1 + 1) % celebrationColors.length;
            double blend = colorProgress - colorIndex1;
            
            Color color1 = celebrationColors[colorIndex1];
            Color color2 = celebrationColors[colorIndex2];
            Color blendedColor = new Color(
                (int)(color1.getRed() + (color2.getRed() - color1.getRed()) * blend),
                (int)(color1.getGreen() + (color2.getGreen() - color1.getGreen()) * blend),
                (int)(color1.getBlue() + (color2.getBlue() - color1.getBlue()) * blend)
            );
            statusLabel.setForeground(blendedColor);
            
            double pulse = 1.0 + 0.3 * Math.sin(progress * Math.PI * 8);
            int newSize = (int)(originalFont.getSize() * pulse);
            statusLabel.setFont(new Font(originalFont.getName(), originalFont.getStyle(), newSize));
        });
        
        showConfettiEffect();
        pulseTimer.start();
    }

    private void showConfettiEffect() {
        String[] confettiSymbols = {"*", "+", "o", "^", "#", "~", "@"};
        final int confettiCount = 8;
        
        for (int i = 0; i < confettiCount; i++) {
            final int delay = i * 100;
            Timer startTimer = new Timer(delay, null);
            startTimer.setRepeats(false);
            
            startTimer.addActionListener(startEvent -> {
                createOptimizedConfetti(confettiSymbols[(int)(Math.random() * confettiSymbols.length)]);
            });
            startTimer.start();
        }
    }
    
    private void createOptimizedConfetti(String symbol) {
        final JLabel confetti = new JLabel(symbol);
        confetti.setFont(new Font("Segoe UI", Font.BOLD, 20 + (int)(Math.random() * 8)));
        
        Color[] colors = {
            new Color(255, 215, 0),
            new Color(255, 99, 132),
            new Color(54, 162, 235),
            new Color(255, 159, 64),
            new Color(153, 102, 255),
            new Color(75, 192, 192)
        };
        confetti.setForeground(colors[(int)(Math.random() * colors.length)]);
        
        int startX = 50 + (int)(Math.random() * (getWidth() - 100));
        int startY = -50;
        final double velocityX = (Math.random() - 0.5) * 4;
        final double velocityY = 2 + Math.random() * 3;
        final double rotation = Math.random() * 0.2;
        
        confetti.setBounds(startX, startY, 30, 30);
        getContentPane().add(confetti, 0);
        
        Timer animationTimer = new Timer(16, null);
        final long startTime = System.currentTimeMillis();
        final int duration = 3000 + (int)(Math.random() * 2000);
        
        animationTimer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            double progress = (double) elapsed / duration;
            
            if (progress >= 1.0) {
                animationTimer.stop();
                getContentPane().remove(confetti);
                repaint();
                return;
            }
            
            int newX = (int)(startX + velocityX * elapsed / 16);
            int newY = (int)(startY + velocityY * elapsed / 16 + 0.5 * 0.1 * Math.pow(elapsed / 16, 2));
            
            newX += (int)(Math.sin(elapsed * rotation / 100) * 15);
            
            confetti.setLocation(newX, newY);
            
            if (progress > 0.8) {
                float alpha = (float)(1.0 - (progress - 0.8) / 0.2);
                confetti.setForeground(new Color(
                    confetti.getForeground().getRed(),
                    confetti.getForeground().getGreen(),
                    confetti.getForeground().getBlue(),
                    (int)(255 * alpha)
                ));
            }
        });
        
        animationTimer.start();
    }

    private void showWinAnimation(int[][] winningLine) {
        Timer flashTimer = new Timer(300, null);
        final int[] flashCount = {0};
        final Color originalBg = statusLabel.getBackground();
        
        flashTimer.addActionListener(e -> {
            if (flashCount[0] < 6) {
                statusLabel.setOpaque(true);
                statusLabel.setBackground(flashCount[0] % 2 == 0 ? warningColor : originalBg);
                flashCount[0]++;
            } else {
                statusLabel.setOpaque(false);
                flashTimer.stop();
            }
        });
        flashTimer.start();
    }

    private void updateStatus() {
        if (!gameLogic.isGameOver()) {
            if (isPlayerVsAI) {
                statusLabel.setText(gameLogic.getCurrentPlayer() == 'X' ? "Your Turn" : "AI's Turn");
            } else {
                statusLabel.setText("Player " + gameLogic.getCurrentPlayer() + "'s Turn");
            }
            statusLabel.setForeground(primaryColor);
        }
    }

    private void startNewGameWithAnimation() {
        Timer transitionTimer = new Timer(16, null);
        final long startTime = System.currentTimeMillis();
        final int duration = 600;
        
        transitionTimer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            double progress = (double) elapsed / duration;
            
            if (progress < 0.5) {
                double scale = 1.0 - (progress * 2 * 0.1);
                scaleGamePanel(scale);
            } else if (progress == 0.5 || (progress > 0.5 && progress < 0.6)) {
                if (progress > 0.5 && progress < 0.52) {
                    resetGameBoard();
                }
                scaleGamePanel(0.9);
            } else {
                double scale = 0.9 + ((progress - 0.5) * 2 * 0.1);
                scaleGamePanel(scale);
                
                if (progress >= 1.0) {
                    scaleGamePanel(1.0);
                    transitionTimer.stop();
                }
            }
        });
        transitionTimer.start();
    }
    
    private void scaleGamePanel(double scale) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int size = (int)(100 * scale);
                buttons[i][j].setPreferredSize(new Dimension(size, size));
            }
        }
        gamePanel.revalidate();
        gamePanel.repaint();
    }

    private void resetGameBoard() {
        gameLogic.resetGame();
        
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j].setText("");
                buttons[i][j].setBackground(cardColor);
                buttons[i][j].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createRaisedBevelBorder(),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
                buttons[i][j].setEnabled(true);
                buttons[i][j].setPreferredSize(new Dimension(100, 100));
            }
        }
        
        updateStatus();
    }

    private void showNotification(String message, Color color) {
        JLabel notification = new JLabel(message, JLabel.CENTER);
        notification.setFont(new Font("Segoe UI", Font.BOLD, 16));
        notification.setForeground(Color.WHITE);
        notification.setOpaque(true);
        notification.setBackground(color);
        notification.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JWindow popup = new JWindow();
        popup.add(notification);
        popup.pack();
        popup.setLocationRelativeTo(this);
        popup.setVisible(true);
        
        Timer timer = new Timer(1500, e -> popup.dispose());
        timer.setRepeats(false);
        timer.start();
    }

    private String getScoreText() {
        if (isPlayerVsAI) {
            return String.format("You: %d | AI: %d | Draws: %d", 
                gameLogic.getPlayerXScore(), gameLogic.getPlayerOScore(), gameLogic.getDraws());
        } else {
            return String.format("Player X: %d | Player O: %d | Draws: %d", 
                gameLogic.getPlayerXScore(), gameLogic.getPlayerOScore(), gameLogic.getDraws());
        }
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        super.paint(g2d);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TicTacToeUI game = new TicTacToeUI();
            game.setVisible(true);
            
            Timer welcomeTimer = new Timer(1000, e -> {
                game.showNotification("Welcome to Tic Tac Toe Ultimate!", game.accentColor);
            });
            welcomeTimer.setRepeats(false);
            welcomeTimer.start();
        });
    }
}
