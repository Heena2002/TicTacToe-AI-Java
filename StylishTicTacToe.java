import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.sound.sampled.*;
import javax.swing.*;

public class StylishTicTacToe extends JFrame implements ActionListener {
    private JButton[][] buttons = new JButton[3][3];
    private boolean darkMode = true;
    private JPanel boardPanel;
    private JLabel statusLabel;
    private JButton toggleButton, restartButton;
    private boolean gameOver = false;

    public StylishTicTacToe() {
        setTitle("Heenaa Edition - Tic Tac Toe (vs AI)");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        statusLabel = new JLabel("Your Turn (X)", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 20));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        boardPanel = new JPanel(new GridLayout(3, 3));
        boardPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j] = new JButton();
                buttons[i][j].setFont(new Font("Arial", Font.BOLD, 40));
                buttons[i][j].setFocusPainted(false);
                buttons[i][j].addActionListener(this);
                boardPanel.add(buttons[i][j]);
            }
        }

        toggleButton = new JButton("Toggle Light/Dark Mode");
        toggleButton.setFocusPainted(false);
        toggleButton.addActionListener(e -> toggleMode());

        restartButton = new JButton("Restart Game");
        restartButton.setFocusPainted(false);
        restartButton.addActionListener(e -> resetGame());

        JPanel bottomPanel = new JPanel(new GridLayout(1, 2));
        bottomPanel.add(toggleButton);
        bottomPanel.add(restartButton);

        add(statusLabel, BorderLayout.NORTH);
        add(boardPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        applyTheme();
        setVisible(true);
    }

    private void applyTheme() {
        Color bgColor = darkMode ? Color.BLACK : Color.WHITE;
        Color fgColor = darkMode ? Color.WHITE : Color.BLACK;

        boardPanel.setBackground(bgColor);
        statusLabel.setForeground(fgColor);
        statusLabel.setBackground(bgColor);
        statusLabel.setOpaque(true);

        toggleButton.setBackground(fgColor);
        toggleButton.setForeground(bgColor);

        restartButton.setBackground(fgColor);
        restartButton.setForeground(bgColor);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j].setBackground(bgColor);
                buttons[i][j].setForeground(fgColor);
                buttons[i][j].setBorder(BorderFactory.createLineBorder(fgColor, 2));
                buttons[i][j].setText("");
                buttons[i][j].setEnabled(true);
            }
        }

        gameOver = false;
        statusLabel.setText("Your Turn (X)");
    }

    private void toggleMode() {
        darkMode = !darkMode;
        applyTheme();
    }

    private void resetGame() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j].setText("");
                buttons[i][j].setEnabled(true);
            }
        }
        gameOver = false;
        statusLabel.setText("Your Turn (X)");
    }

    private void playSound(String fileName) {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(fileName).getAbsoluteFile());
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (Exception e) {
            System.out.println("Sound error: " + e.getMessage());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver) return;
        JButton clicked = (JButton) e.getSource();
        if (clicked.getText().isEmpty()) {
            clicked.setText("X");
            playSound("click.wav");
            if (checkGameEnd("X")) return;
            statusLabel.setText("Computer's Turn (O)...");
            Timer timer = new Timer(500, ev -> {
                computerMove();
                ((Timer) ev.getSource()).stop();
            });
            timer.setRepeats(false);
            timer.start();
        }
    }

    private boolean checkGameEnd(String player) {
        if (hasWon(player)) {
            statusLabel.setText((player.equals("X") ? "You" : "Computer") + " Win!");
            disableButtons();
            playSound("win.wav");
            gameOver = true;
            return true;
        } else if (isBoardFull()) {
            statusLabel.setText("It's a draw!");
            playSound("draw.wav");
            gameOver = true;
            return true;
        }
        return false;
    }

    private void computerMove() {
        int[] move = bestMove();
        buttons[move[0]][move[1]].setText("O");
        playSound("click.wav");
        if (!checkGameEnd("O")) {
            statusLabel.setText("Your Turn (X)");
        }
    }

    private int[] bestMove() {
        int bestScore = Integer.MIN_VALUE;
        int[] move = new int[2];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (buttons[i][j].getText().isEmpty()) {
                    buttons[i][j].setText("O");
                    int score = minimax(0, false);
                    buttons[i][j].setText("");
                    if (score > bestScore) {
                        bestScore = score;
                        move[0] = i;
                        move[1] = j;
                    }
                }
            }
        }
        return move;
    }

    private int minimax(int depth, boolean isMax) {
        if (hasWon("O")) return 10 - depth;
        if (hasWon("X")) return depth - 10;
        if (isBoardFull()) return 0;

        int best = isMax ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (buttons[i][j].getText().isEmpty()) {
                    buttons[i][j].setText(isMax ? "O" : "X");
                    int score = minimax(depth + 1, !isMax);
                    buttons[i][j].setText("");
                    best = isMax ? Math.max(score, best) : Math.min(score, best);
                }
            }
        }
        return best;
    }

    private boolean hasWon(String player) {
        for (int i = 0; i < 3; i++) {
            if (buttons[i][0].getText().equals(player) &&
                buttons[i][1].getText().equals(player) &&
                buttons[i][2].getText().equals(player)) return true;
            if (buttons[0][i].getText().equals(player) &&
                buttons[1][i].getText().equals(player) &&
                buttons[2][i].getText().equals(player)) return true;
        }
        if (buttons[0][0].getText().equals(player) &&
            buttons[1][1].getText().equals(player) &&
            buttons[2][2].getText().equals(player)) return true;
        if (buttons[0][2].getText().equals(player) &&
            buttons[1][1].getText().equals(player) &&
            buttons[2][0].getText().equals(player)) return true;
        return false;
    }

    private boolean isBoardFull() {
        for (JButton[] row : buttons) {
            for (JButton btn : row) {
                if (btn.getText().isEmpty()) return false;
            }
        }
        return true;
    }

    private void disableButtons() {
        for (JButton[] row : buttons) {
            for (JButton btn : row) {
                btn.setEnabled(false);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(StylishTicTacToe::new);
    }
}
