package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import game.Block;
import game.Game;

public class GameView extends JFrame {

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Block[][] board = new Block[8][8];
					GameView frame = new GameView(board,null);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	
	private BlockPanel[][] blockPanel;
	private int scale = 8;
	private Block[][] board;
	private JLabel scoreLabel1 = new JLabel("2");
	private JLabel scoreLabel2 = new JLabel("2");
	private JLabel turnLabel = new JLabel("1");
	private JLabel p1probLabel = new JLabel("");
	private JLabel p2ProbLabel = new JLabel("");
	private JScrollPane scrollPane;
	private JTextArea textArea;
	private Game game;
	
	public GameView(Block[][] board,Game game) {
		scale = 8;
		this.game = game;
		this.board = board;
		setTitle("[작성자 : 장종규] Alpha Beta Pruning을 이용한 Othello AI");
		setResizable(false);
		//int scale = board.length;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1000, 700);
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((dim.width/2)-(this.getWidth()/2), (dim.height/2)-(this.getHeight()/2));
		
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		contentPane.setBackground(Color.GREEN);
		
		JPanel boardPanel = new JPanel();
		boardPanel.setBorder(new LineBorder(new Color(0, 0, 0), 3));
		boardPanel.setBounds(12, 176, 403, 386);
		contentPane.add(boardPanel);
		boardPanel.setLayout(new GridLayout(scale, scale, 0, 0));
		
		BlockPanel titleBlack = new BlockPanel(null,-1);
		titleBlack.setPlayer(1);
		titleBlack.setBackground(Color.green);
		titleBlack.setBounds(117, 10, 40, 40);
		contentPane.add(titleBlack);
		
		BlockPanel titleWhite = new BlockPanel(null,-1);
		titleWhite.setBackground(Color.green);
		titleWhite.setPlayer(2);
		titleWhite.setBounds(243, 10, 40, 40);
		contentPane.add(titleWhite);
		
		JLabel lblNewLabel_1 = new JLabel("Turn");
		lblNewLabel_1.setFont(new Font("굴림", Font.BOLD, 16));
		lblNewLabel_1.setBounds(12, 10, 63, 35);
		contentPane.add(lblNewLabel_1);
		
		JPanel panel = new JPanel();
		panel.setBounds(12, 43, 63, 31);
		contentPane.add(panel);
		
		turnLabel = new JLabel("1");
		turnLabel.setFont(new Font("굴림", Font.PLAIN, 18));
		panel.add(turnLabel);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBounds(243, 52, 47, 31);
		contentPane.add(panel_1);
		panel_1.add(scoreLabel2);
		scoreLabel2.setFont(new Font("굴림", Font.BOLD, 15));
		
		JPanel panel_2 = new JPanel();
		panel_2.setBounds(110, 52, 47, 31);
		contentPane.add(panel_2);
		panel_2.add(scoreLabel1);
		scoreLabel1.setFont(new Font("굴림", Font.BOLD, 15));
		
		JLabel label = new JLabel("\uD751");
		label.setBounds(482, 43, 57, 15);
		contentPane.add(label);
		
		JLabel label_1 = new JLabel("\uBC31");
		label_1.setBounds(577, 43, 57, 15);
		contentPane.add(label_1);
		
		p1probLabel.setFont(new Font("굴림", Font.BOLD, 14));
		p1probLabel.setBounds(465, 80, 84, 21);
		contentPane.add(p1probLabel);
		
		p2ProbLabel.setFont(new Font("굴림", Font.BOLD, 14));
		p2ProbLabel.setBounds(561, 80, 84, 21);
		contentPane.add(p2ProbLabel);
		
		textArea = new JTextArea();
		textArea.setForeground(Color.WHITE);
		textArea.setBackground(Color.BLACK);
		textArea.setEditable(false);
		scrollPane = new JScrollPane(textArea);
		scrollPane.setBounds(427, 124, 555, 522);
		contentPane.add(scrollPane);
		
		JButton btnNewButton = new JButton("\uC7AC\uCD09\uD558\uAE30");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				game.stopThinking();
			}
		});
		btnNewButton.setFont(new Font("굴림", Font.BOLD, 18));
		btnNewButton.setBounds(272, 584, 137, 62);
		contentPane.add(btnNewButton);
		
		JLabel lblNewLabel = new JLabel("\uC790\uC2E0\uAC10");
		lblNewLabel.setFont(new Font("굴림", Font.BOLD, 15));
		lblNewLabel.setBounds(507, 21, 57, 15);
		contentPane.add(lblNewLabel);
		
		blockPanel = new BlockPanel[scale][scale];
		for(int i = 0 ; i < scale ; i++){
			for(int j = 0 ; j < scale ; j++){
				blockPanel[i][j] = new BlockPanel(game,(scale*i)+j);
				blockPanel[i][j].setBorder(new LineBorder(new Color(0,0,0),1));
				if((i+j)%2 == 0 ){
					blockPanel[i][j].setBackground(Color.orange);
				}else{
					blockPanel[i][j].setBackground(Color.gray);
				}
				boardPanel.add(blockPanel[i][j]);
			}
		}
	}

	public void drawBoard(int currPlayer,int currTurn) {
		//this.board = board;
		blockPaint(currPlayer,currTurn);
	}
	
	public void blockPaint(int currPlayer,int currTurn){
		int p1 = 0 ;
		int p2 = 0 ;
		for(int i = 0 ; i < scale ; i++){
			for(int j = 0 ; j < scale ; j++){
				blockPanel[i][j].clearData();
				
				if(board[i][j].getPlayer() > 0){
					if(board[i][j].getEatNum() == currTurn - 1){
						blockPanel[i][j].setLastBlock();
					}
					blockPanel[i][j].setPlayer(board[i][j].getPlayer());
				}else{
					if(board[i][j].impossible(currPlayer)){
						blockPanel[i][j].setImpBlock();
					}
				}
				blockPanel[i][j].repaint();
				if(board[i][j].getPlayer() == 1){
					p1++;
				}else if(board[i][j].getPlayer() == 2){
					p2++;
				}
			}
		}
		scoreLabel1.setText(p1+"");
		scoreLabel2.setText(p2+"");
		turnLabel.setText(currTurn+"");
	}
	
	public void setProb(int player,int probability,int cnt){
		if(player == 1){
			p1probLabel.setText("[ "+probability+" ]");
		}else{
			p2ProbLabel.setText("[ "+probability+" ]");
		}
		
		if(cnt < 44){
			if(probability > 15){
				textArea.append("AI : 제가 매우 유리하군요. \n");  // 로그 내용을 JTextArea 위에 붙여주고
			}else if(probability > 10){
				textArea.append("AI : 이번판은 자신있네요. \n");
			}else if(probability > 5){
				textArea.append("AI : 근소하게 제가 앞서고 있는듯 하네요. \n"); 
			}else if(probability > 0){
				textArea.append("AI : 아직 모르겠습니다. \n");
			}else if(probability == 0){
				textArea.append("AI : 당신과 나는 막상막하 입니다. \n");
			}else if(probability > -5){
				textArea.append("AI : 조금 불안하네요. \n"); 
			}else if(probability > -10){
				textArea.append("AI : 당신 잘하시네요. \n");  
			}else{
				textArea.append("AI : 이번판은 힘들 것 같네요. \n");  
			}
		}else{
			if(probability > 0){
				textArea.append("AI : 미래가 보였습니다. \n 대략 "+probability+"점 차이로 승리할 것 같네요. \n");
			}else if(probability > -10){
				textArea.append("AI : 이번 게임은 아직 모르겠네요. \n"); 
			}else if(probability > -20){
				textArea.append("AI : 저에게 약간 불리한 것 같지만 포기하지 않겠어요. \n"); 
			}else{
				textArea.append("AI(흑) : 이번 게임은 제가 패배할 것 같네요. \n"); 
			}
		}
		textArea.setCaretPosition(textArea.getDocument().getLength());  // 맨아래로 스크롤한다.
	}
	
	public void appendText(String text){
		textArea.append(text+"\n");
		textArea.setCaretPosition(textArea.getDocument().getLength());  // 맨아래로 스크롤한다.
	}

	public void setTimeSum(int currPlayer, String mmssFromMillisecondString) {
		// TODO Auto-generated method stub
		
	}
	
	
	
	
}
