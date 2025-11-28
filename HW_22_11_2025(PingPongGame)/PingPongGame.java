package org.PingPong;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PingPongGame extends JPanel implements ActionListener, KeyListener {
    private final int W = 600;
    private final int H = 400;

    private int racket1Y = H / 2 - 50;
    private int racket2Y = H / 2 - 50;
    private final int RACKET_W = 10;
    private final int RACKET_H = 80;

    private int ballX = W / 2;
    private int ballY = H / 2;
    private final int BALL_SIZE = 15;
    private int ballSpeedX = 3;
    private int ballSpeedY = 3;

    private int p1Score = 0;
    private int p2Score = 0;
    private boolean gameEnd = false;
    private String win = "";

    private boolean wPressed = false;
    private boolean sPressed = false;
    private boolean upPressed = false;
    private boolean downPressed = false;

    public PingPongGame() {
        setPreferredSize(new Dimension(W, H));
        setBackground(Color.BLACK);

        Timer timer = new Timer(16, this);
        timer.start();

        addKeyListener(this);
        setFocusable(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameEnd) {
            moveBall();
            moveRacket();
            checkCollisions();
        }
        repaint();
    }

    private void moveBall() {
        ballX += ballSpeedX;
        ballY += ballSpeedY;
    }

    private void moveRacket() {
        if (wPressed && racket1Y > 0) racket1Y -= 5;
        if (sPressed && racket1Y < H - RACKET_H) racket1Y += 5;

        if (upPressed && racket2Y > 0) racket2Y -= 5;
        if (downPressed && racket2Y < H - RACKET_H) racket2Y += 5;
    }

    private void checkCollisions() {
        if (ballY <= 0 || ballY >= H - BALL_SIZE) {
            ballSpeedY = -ballSpeedY;
        }

        if (ballX <= RACKET_W &&
                ballY + BALL_SIZE >= racket1Y &&
                ballY <= racket1Y + RACKET_H) {
            ballSpeedX = Math.abs(ballSpeedX);
            ballSpeedY += (Math.random() > 0.5 ? 1 : -1) * 1;
        }

        if (ballX >= W - RACKET_W - BALL_SIZE &&
                ballY + BALL_SIZE >= racket2Y &&
                ballY <= racket2Y + RACKET_H) {
            ballSpeedX = -Math.abs(ballSpeedX);
            ballSpeedY += (Math.random() > 0.5 ? 1 : -1) * 1;
        }

        if (ballX < 0) {
            p2Score++;
            resetBall();
            checkGameEnd();
        }

        if (ballX > W) {
            p1Score++;
            resetBall();
            checkGameEnd();
        }

        if (Math.abs(ballSpeedX) > 8) ballSpeedX = ballSpeedX > 0 ? 8 : -8;
        if (Math.abs(ballSpeedY) > 8) ballSpeedY = ballSpeedY > 0 ? 8 : -8;
    }

    private void resetBall() {
        ballX = W / 2;
        ballY = H / 2;
        ballSpeedX = (Math.random() > 0.5 ? 1 : -1) * 4;
        ballSpeedY = (Math.random() > 0.5 ? 1 : -1) * 3;
    }

    private void checkGameEnd() {
        if (p1Score >= 11 || p2Score >= 11) {
            if (Math.abs(p1Score - p2Score) >= 2) {
                gameEnd = true;
                win = (p1Score > p2Score) ? "Игрок 1" : "Игрок 2";
            }
        }

        if (p1Score >= 10 && p2Score >= 10) {
            if (Math.abs(p1Score - p2Score) >= 2) {
                gameEnd = true;
                win = (p1Score > p2Score) ? "Игрок 1" : "Игрок 2";
            }
        }
    }

    private void restartGame() {
        p1Score = 0;
        p2Score = 0;
        gameEnd = false;
        win = "";
        resetBall();
        racket1Y = H / 2 - 50;
        racket2Y = H / 2 - 50;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.WHITE);
        g.fillRect(0, racket1Y, RACKET_W, RACKET_H);
        g.fillRect(W - RACKET_W, racket2Y, RACKET_W, RACKET_H);

        g.fillRect(ballX, ballY, BALL_SIZE, BALL_SIZE);

        g.setColor(Color.GRAY);
        for (int y = 0; y < H; y += 20) {
            g.fillRect(W / 2 - 1, y, 2, 10);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString(String.valueOf(p1Score), W / 4, 30);
        g.drawString(String.valueOf(p2Score), 3 * W / 4, 30);

        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString("Игрок 1: W/S", 10, H - 20);
        g.drawString("Игрок 2: ↑/↓", W - 100, H - 20);

        if (gameEnd) {
            g.setColor(new Color(255, 255, 255, 200));
            g.fillRect(0, 0, W, H);

            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.drawString("ПОБЕДА!", W / 2 - 80, H / 2 - 30);

            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString(win + " победил!", W / 2 - 80, H / 2 + 20);
            g.drawString("Счет: " + p1Score + ":" + p2Score, W / 2 - 60, H / 2 + 50);

            g.setFont(new Font("Arial", Font.PLAIN, 16));
            g.drawString("Нажмите R для новой игры", W / 2 - 100, H / 2 + 80);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameEnd) {
            if (e.getKeyCode() == KeyEvent.VK_R) {
                restartGame();
            }
            return;
        }

        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> wPressed = true;
            case KeyEvent.VK_S -> sPressed = true;
            case KeyEvent.VK_UP -> upPressed = true;
            case KeyEvent.VK_DOWN -> downPressed = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> wPressed = false;
            case KeyEvent.VK_S -> sPressed = false;
            case KeyEvent.VK_UP -> upPressed = false;
            case KeyEvent.VK_DOWN -> downPressed = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Пинг-Понг");
        PingPongGame game = new PingPongGame();

        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}