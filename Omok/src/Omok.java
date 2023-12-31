import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Random;
import java.util.Stack;
import javax.sound.sampled.*;
import java.io.File;


public class Omok extends JPanel {
    GoEgg goEgg[][];
    ImageIcon img = new ImageIcon("images//empty.png");
    ImageIcon white = new ImageIcon("images//white.png");
    ImageIcon black = new ImageIcon("images//black.png");
    ImageIcon turn = black;
    Timer timer;
    boolean win = false;
    Stack<GoEgg> stoneStack = new Stack<>();
    Stack<ImageIcon> turnStack = new Stack<>();

    public boolean isWin() {
        return win;
    }

    Util util;
    public JLabel timerLabel;
    public int timeLeft;
    Random random = new Random();

    public int getTimeLeft() {
        return this.timeLeft;
    }

    public ImageIcon getTurn() {
        return this.turn;
    }

    public Omok(int initialTime) {

        this.util = util;
        this.setPreferredSize(new Dimension(1000, 1000));

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(1000, 1000));
        this.add(layeredPane);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(26, 26));
        panel.setBounds(0, 0, 1000, 1000);
        goEgg = new GoEgg[26][];

        myActionListener goAction = new myActionListener(initialTime);
        for (int i = 0; i < 26; i++) {
            goEgg[i] = new GoEgg[26];
            for (int j = 0; j < 26; j++) {
                goEgg[i][j] = new GoEgg(i, j, img);
                panel.add(goEgg[i][j]);
                goEgg[i][j].addActionListener(goAction);
                goEgg[i][j].setBorderPainted(false);
            }
        }

        layeredPane.add(panel, Integer.valueOf(1));
        timeLeft = initialTime;

        timerLabel = new JLabel(String.valueOf(initialTime), SwingConstants.RIGHT);
        timerLabel.setFont(new Font("Serif", Font.BOLD, 30));
        timerLabel.setForeground(Color.WHITE);
        layeredPane.add(timerLabel, Integer.valueOf(2));
        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timeLeft--;
                timerLabel.setText(String.valueOf(timeLeft));
                if (timeLeft <= 0) {
                    timer.stop();
                    placeRandomStone();
                    timeLeft = initialTime;
                    timer.restart();
                }
            }
        });
        timer.start();


//        pack();
        setVisible(true);
    }

    void placeRandomStone() {
        int x, y;
        do {
            x = random.nextInt(26);
            y = random.nextInt(26);
        } while (!goEgg[x][y].state.equals("N"));

        goEgg[x][y].setIcon(turn);
        goEgg[x][y].state = turn == white ? "W" : "B";
        stoneStack.push(goEgg[x][y]);
        turnStack.push(turn);
        if (goEgg[x][y].getActionListeners().length > 0) {
            goEgg[x][y].removeActionListener(goEgg[x][y].getActionListeners()[0]);
        }
        checkWin(goEgg[x][y]);

        turn = turn == white ? black : white; // Switch turn
    }

    class myActionListener implements ActionListener {
        private int initialTime; // 멤버 변수 추가

        public myActionListener(int initialTime) { // 생성자 추가
            this.initialTime = initialTime; // 멤버 변수 초기화
        }
        public void playSound() {
            try {
                File soundFile = new File("sounds//Omok.wav"); 
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                FloatControl gainControl = 
                        (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                gainControl.setValue(-20.0f);
                clip.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            GoEgg wi = (GoEgg) e.getSource();
            playSound();
            if (turn == white) {
                turnStack.push(turn);
                wi.setIcon(white);
                wi.state = "W";
                turn = black;

            } else {
                turnStack.push(turn);
                wi.setIcon(black);
                wi.state = "B";
                turn = white;
            }
            stoneStack.push(wi);
            checkWin(wi);

            wi.removeActionListener(this);
            timeLeft = this.initialTime;
            timer.restart();
        }
    }


    public void checkWin(GoEgg e) {
        int checkx = e.x;
        int checky = e.y;
        int count = 0;
        while (checky >= 0 && goEgg[checkx][checky].state.equals(e.state)) {
            checky -= 1;
        }
        checky += 1;
        while (checky < 26 && goEgg[checkx][checky].state.equals(e.state)) {
            checky += 1;
            count++;
        }
        if (count == 5) {
            win = true;
            if (e.state.equals("B")) {
                JOptionPane.showMessageDialog(null, "흑돌 승리", "흑돌 승리", JOptionPane.QUESTION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "백돌 승리", "백돌 승리", JOptionPane.QUESTION_MESSAGE);
            }
            timer.stop();
            for (int i = 0; i < 26; i++) {
                for (int j = 0; j < 26; j++) {
                    ActionListener[] actionListeners = goEgg[i][j].getActionListeners();
                    for (ActionListener actionListener : actionListeners) {
                        goEgg[i][j].removeActionListener(actionListener);
                    }
                }
            }
        }
        if (count == 6) {
        	if(e.state.equals("B")) {
                JOptionPane.showMessageDialog(null, "6목 감지! 게임을 계속합니다", "6목 감지", JOptionPane.QUESTION_MESSAGE);
                  if (!stoneStack.isEmpty() && !turnStack.isEmpty()) {
                      GoEgg lastStone = stoneStack.pop();
                      ImageIcon lastTurn = turnStack.pop();
                      lastStone.setIcon(img);
                      lastStone.state = "N";
                      turn = lastTurn;
                      lastStone.addActionListener(new myActionListener(getTimeLeft()));
                  }
             }
             else {
                JOptionPane.showMessageDialog(null, "백돌 승리", "백돌 승리", JOptionPane.QUESTION_MESSAGE);
                timer.stop();
                  for (int i = 0; i < 26; i++) {
                      for (int j = 0; j < 26; j++) {
                          ActionListener[] actionListeners = goEgg[i][j].getActionListeners();
                          for (ActionListener actionListener : actionListeners) {
                              goEgg[i][j].removeActionListener(actionListener);
                          }
                      }
                  }
             }
        }

        checkx = e.x;
        checky = e.y;
        count = 0;

        while (checkx >= 0 && goEgg[checkx][checky].state.equals(e.state)) {
            checkx -= 1;
        }
        checkx += 1;
        while (checkx < 26 && goEgg[checkx][checky].state.equals(e.state)) {
            checkx += 1;
            count++;
        }
        if (count == 5) {
            win = true;
            if (e.state.equals("B")) {
                JOptionPane.showMessageDialog(null, "흑돌 승리", "흑돌 승리", JOptionPane.QUESTION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "백돌 승리", "백돌 승리", JOptionPane.QUESTION_MESSAGE);
            }
            timer.stop();
            for (int i = 0; i < 26; i++) {
                for (int j = 0; j < 26; j++) {
                    ActionListener[] actionListeners = goEgg[i][j].getActionListeners();
                    for (ActionListener actionListener : actionListeners) {
                        goEgg[i][j].removeActionListener(actionListener);
                    }
                }
            }
        }
        if (count == 6) {
        	if(e.state.equals("B")) {
                JOptionPane.showMessageDialog(null, "6목 감지! 게임을 계속합니다", "6목 감지", JOptionPane.QUESTION_MESSAGE);
                  if (!stoneStack.isEmpty() && !turnStack.isEmpty()) {
                      GoEgg lastStone = stoneStack.pop();
                      ImageIcon lastTurn = turnStack.pop();
                      lastStone.setIcon(img);
                      lastStone.state = "N";
                      turn = lastTurn;
                      lastStone.addActionListener(new myActionListener(getTimeLeft()));
                  }
             }
             else {
                JOptionPane.showMessageDialog(null, "백돌 승리", "백돌 승리", JOptionPane.QUESTION_MESSAGE);
                timer.stop();
                  for (int i = 0; i < 26; i++) {
                      for (int j = 0; j < 26; j++) {
                          ActionListener[] actionListeners = goEgg[i][j].getActionListeners();
                          for (ActionListener actionListener : actionListeners) {
                              goEgg[i][j].removeActionListener(actionListener);
                          }
                      }
                  }
             }


        }

        checkx = e.x;
        checky = e.y;
        count = 0;

        while (checkx >= 0 && checky >= 0 && goEgg[checkx][checky].state.equals(e.state)) {
            checkx -= 1;
            checky -= 1;
        }
        checkx += 1;
        checky += 1;
        while (checkx < 26 && checky < 26 && goEgg[checkx][checky].state.equals(e.state)) {
            checkx += 1;
            checky += 1;
            count++;
        }
        if (count == 5) {
            win = true;
            if (e.state.equals("B")) {
                JOptionPane.showMessageDialog(null, "흑돌 승리", "흑돌 승리", JOptionPane.QUESTION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "백돌 승리", "백돌 승리", JOptionPane.QUESTION_MESSAGE);
            }
            timer.stop();
            for (int i = 0; i < 26; i++) {
                for (int j = 0; j < 26; j++) {
                    ActionListener[] actionListeners = goEgg[i][j].getActionListeners();
                    for (ActionListener actionListener : actionListeners) {
                        goEgg[i][j].removeActionListener(actionListener);
                    }
                }
            }

        }
        if (count == 6) {
        	if(e.state.equals("B")) {
                JOptionPane.showMessageDialog(null, "6목 감지! 게임을 계속합니다", "6목 감지", JOptionPane.QUESTION_MESSAGE);
                  if (!stoneStack.isEmpty() && !turnStack.isEmpty()) {
                      GoEgg lastStone = stoneStack.pop();
                      ImageIcon lastTurn = turnStack.pop();
                      lastStone.setIcon(img);
                      lastStone.state = "N";
                      turn = lastTurn;
                      lastStone.addActionListener(new myActionListener(getTimeLeft()));
                  }
             }
             else {
                JOptionPane.showMessageDialog(null, "백돌 승리", "백돌 승리", JOptionPane.QUESTION_MESSAGE);
                timer.stop();
                  for (int i = 0; i < 26; i++) {
                      for (int j = 0; j < 26; j++) {
                          ActionListener[] actionListeners = goEgg[i][j].getActionListeners();
                          for (ActionListener actionListener : actionListeners) {
                              goEgg[i][j].removeActionListener(actionListener);
                          }
                      }
                  }
             }
        }

        checkx = e.x;
        checky = e.y;
        count = 0;

        while (checkx >= 0 && checky < 26 && goEgg[checkx][checky].state.equals(e.state)) {
            checkx -= 1;
            checky += 1;
        }
        checkx += 1;
        checky -= 1;
        while (checkx < 26 && checky >= 0 && goEgg[checkx][checky].state.equals(e.state)) {
            checkx += 1;
            checky -= 1;
            count++;
        }

        if (count == 5) {
            win = true;
            if (e.state.equals("B")) {
                JOptionPane.showMessageDialog(null, "흑돌 승리", "흑돌 승리", JOptionPane.QUESTION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "백돌 승리", "백돌 승리", JOptionPane.QUESTION_MESSAGE);
            }
            timer.stop();
            for (int i = 0; i < 26; i++) {
                for (int j = 0; j < 26; j++) {
                    ActionListener[] actionListeners = goEgg[i][j].getActionListeners();
                    for (ActionListener actionListener : actionListeners) {
                        goEgg[i][j].removeActionListener(actionListener);
                    }
                }
            }

        }
        if (count == 6) {
        	if(e.state.equals("B")) {
                JOptionPane.showMessageDialog(null, "6목 감지! 게임을 계속합니다", "6목 감지", JOptionPane.QUESTION_MESSAGE);
                  if (!stoneStack.isEmpty() && !turnStack.isEmpty()) {
                      GoEgg lastStone = stoneStack.pop();
                      ImageIcon lastTurn = turnStack.pop();
                      lastStone.setIcon(img);
                      lastStone.state = "N";
                      turn = lastTurn;
                      lastStone.addActionListener(new myActionListener(getTimeLeft()));
                  }
             }
             else {
                JOptionPane.showMessageDialog(null, "백돌 승리", "백돌 승리", JOptionPane.QUESTION_MESSAGE);
                timer.stop();
                  for (int i = 0; i < 26; i++) {
                      for (int j = 0; j < 26; j++) {
                          ActionListener[] actionListeners = goEgg[i][j].getActionListeners();
                          for (ActionListener actionListener : actionListeners) {
                              goEgg[i][j].removeActionListener(actionListener);
                          }
                      }
                  }
             }
        }

    }


}


class GoEgg extends JButton {
    int x;
    int y;
    String state;

    public GoEgg(int x, int y, ImageIcon image) {
        super(image);
        this.x = x;
        this.y = y;
        state = "N";
    }
}
