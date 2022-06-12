package game;

import java.text.DecimalFormat;
import java.util.Map;

import javax.swing.JOptionPane;

import ai.AlphaOStrongMonteCarlo20220606;
import ai.AlphaOStrongMonteCarlo2_1;
import ai.OthelloAI;
import gui.GameView;
import gui.MenuView;
import util.TimeCalculator;

public class Game {
	private Block[][] board;
	private int scale;
	public static final int PLAYER1 = 1;
	public static final int PLAYER2 = 2;
	private int currPlayer = 1;
	private int hermesPlayer;
	private int cnt = 0;
	private int lastCnt;

	private int p1level;
	private int p2level;
	private boolean watchingMode;
	private boolean impossibleCheck;

	private GameView frame;
	private int probability;
	
	private long player1TimeSum = 0;
	private long player2TimeSum = 0;
	
	private Long time1 = 0L;
	private Long time2 = 0L;
	private Long time3 = 0L;
	private Long time4 = 0L;
	private Long time5 = 0L;
	private Long time6 = 0L;
	
	public Game(int hermesPlayer, int level, int scale) {
		this.hermesPlayer = hermesPlayer;
		this.scale = scale;
		this.lastCnt = scale * scale - 4;
		this.p1level = level;
		this.p2level = level;
	}

	public Game(boolean watchingMode, int p1level, int p2level, int scale) {
		this.watchingMode = true;
		this.scale = scale;
		this.lastCnt = scale * scale - 4;
		this.p1level = p1level;
		this.p2level = p2level;
	}

	public void init() {
		board = new Block[scale][scale];
		for (int i = 0; i < scale; i++) {
			for (int j = 0; j < scale; j++) {
				board[i][j] = new Block();
				board[i][j].setStr((scale * i) + j + "");
				board[i][j].saveStr();
				if ((i == (scale / 2) - 1 && j == (scale / 2) - 1) || (i == (scale / 2) && j == (scale / 2))) {
					board[i][j].setPlayer(PLAYER2);
					continue;
				}
				if ((i == (scale / 2) - 1 && j == scale / 2) || (i == scale / 2 && j == (scale / 2) - 1)) {
					board[i][j].setPlayer(PLAYER1);
					continue;
				}
			}
		}
		initView();
		initNeigher();
	}

	private void initView() {
		frame = new GameView(board, this);
		frame.setVisible(true);
		//frame.drawBoard(currPlayer, cnt);
	}

	private void initNeigher() {
		for (int i = 0; i < scale; i++) {
			for (int j = 0; j < scale; j++) {
				if (i > 0 && j > 0)
					board[i][j].setNorth_west(board[i - 1][j - 1]);
				if (i > 0)
					board[i][j].setNorth(board[i - 1][j]);
				if (i > 0 && j < scale - 1)
					board[i][j].setNorth_east(board[i - 1][j + 1]);
				if (j > 0)
					board[i][j].setWest(board[i][j - 1]);
				if (j < scale - 1)
					board[i][j].setEast(board[i][j + 1]);
				if (i < scale - 1 && j > 0)
					board[i][j].setSouth_west(board[i + 1][j - 1]);
				if (i < scale - 1)
					board[i][j].setSouth(board[i + 1][j]);
				if (i < scale - 1 && j < scale - 1)
					board[i][j].setSouth_east(board[i + 1][j + 1]);
			}
		}
	}

	public void drawBoard() {
		int p1Block = 0;
		int p2Block = 0;
		for (int i = 0; i < scale; i++) {
			for (int j = 0; j < scale; j++) {
				if (board[i][j].getPlayer() == 1) {
					p1Block++;
				} else if (board[i][j].getPlayer() == 2) {
					p2Block++;
				}
				System.out.printf("[%2s]", board[i][j].getStr() + "");
			}
			System.out.println();
		}
		System.out.println("\n[" + this.cnt + "]현재 스코어 : player1(" + p1Block + ") : (" + p2Block + ")player2");
	}

	public void drawBoardGUI() {
		//drawBoard();
		frame.drawBoard(currPlayer, cnt);
	}

	public void start() {
		int twoEndRule = 0;
		while (true) {
			drawBoardGUI();
			if (twoEndRule >= 2)
				break;
			if (!impossible(currPlayer)) {
				if (!watchingMode)
					JOptionPane.showMessageDialog(null, currPlayer + "플레이어가 진행불가능하므로 턴을 넘김");
				currPlayer = currPlayer == 1 ? 2 : 1;
				twoEndRule++;
				continue;
			}
			twoEndRule = 0;
			cnt++;
			// 그래픽으로 바꾸는중
			drawBoardGUI();
			if (!watchingMode && currPlayer != hermesPlayer) {
				doIt();
			} else {
				doItAI();
			}
			if (cnt == lastCnt)
				break;
		}
		drawBoardGUI();
		GameOver();
	}

	private boolean impossible(int currPlayer) {
		for (int i = 0; i < scale; i++) {
			for (int j = 0; j < scale; j++) {
				if (board[i][j].getPlayer() == 0 && board[i][j].impossible(currPlayer))
					return true;
			}
		}
		return false;
	}

	private void GameOver() {
		frame.appendText("# GAME OVER #");
		int p1_cnt = 0;
		int p2_cnt = 0;
		for (int i = 0; i < scale; i++) {
			for (int j = 0; j < scale; j++) {
				if (board[i][j].getPlayer() == 1) {
					p1_cnt++;
				} else if (board[i][j].getPlayer() == 2) {
					p2_cnt++;
				}
			}
		}
		if (p1_cnt > p2_cnt)
			JOptionPane.showMessageDialog(null, "player1(" + p1_cnt + ") : (" + p2_cnt + ")player2 로  [1p WIN]");
		else if (p1_cnt < p2_cnt)
			JOptionPane.showMessageDialog(null, "player1(" + p1_cnt + ") : (" + p2_cnt + ")player2 로  [2p WIN]");
		else
			JOptionPane.showMessageDialog(null, "player1(" + p1_cnt + ") : (" + p2_cnt + ")player2 로  [DROW]");
		frame.dispose();
		MenuView menu = new MenuView();
		menu.setVisible(true);
	}
	private Integer max = null;
	private OthelloAI h = null;
	private DecimalFormat df = new DecimalFormat("#,###");
	private void doItAI() {
		long start = System.currentTimeMillis();
		int level = 0;
		if (currPlayer == 1) {
			level = p1level;
		} else {
			level = p2level;
		}
		frame.appendText("["+cnt+" Turn]"+currPlayer + "p (level:" + level + ") AI 생각중 ..");
		h = null;
		if(currPlayer == 1)
			// 테스트버젼
			//h = new AlphaOStrongMonteCarlo(currPlayer, board, cnt, lastCnt, level, frame);
			
			// 2022-06-06 임시주석
			//h = new AlphaOStrongMonteCarlo2_1(currPlayer, board, cnt, lastCnt, level, frame);
			h = new AlphaOStrongMonteCarlo20220606(currPlayer, board, cnt, lastCnt, level, frame);
		else
			h = new AlphaOStrongMonteCarlo2_1(currPlayer, board, cnt, lastCnt, level, frame);
		//	h = new AlphaOStrongMonteCarlo2(currPlayer, board, cnt, lastCnt, level, frame);
			//h = new AlphaOStrongSpeed5_180421(currPlayer, board, cnt, lastCnt, level, frame);
			//현재 최강버젼 : 2018-09-22  
		
		Block b = h.choice();
		
		//20220606_로직트래픽데이터
		Map<String, Object> dataMap = h.getDataMap();
		if(dataMap != null){
			Long time1 = (Long)dataMap.get("time1");
			Long time2 = (Long)dataMap.get("time2");
			Long time3 = (Long)dataMap.get("time3");
			Long time4 = (Long)dataMap.get("time4");
			Long time5 = (Long)dataMap.get("time5");
			Long time6 = (Long)dataMap.get("time6");
			this.time1 += time1 == null ? 0 : time1;
			this.time2 += time2 == null ? 0 : time2;
			this.time3 += time3 == null ? 0 : time3;
			this.time4 += time4 == null ? 0 : time4;
			this.time5 += time5 == null ? 0 : time5;
			this.time6 += time6 == null ? 0 : time6;
		}
		probability = h.getProbability();
		frame.setProb(currPlayer, probability, cnt);
		b.settingBoard(currPlayer);
		b.setEatNum(cnt);
		long time = (System.currentTimeMillis() - start);
		frame.appendText(currPlayer + "p(level:" + level + ") HERMES 착수완료 !" + b.getStrSave() + " 걸린시간 : ["
				+ df.format(time) + " ms.]");
		if(currPlayer == 1) {
			player1TimeSum += time;
			frame.appendText("[누적된 시간 : " + TimeCalculator.getMMSSFromMillisecondString(player1TimeSum) + " ]");
		}else {
			player2TimeSum += time;
			frame.appendText("[누적된 시간 : " + TimeCalculator.getMMSSFromMillisecondString(player2TimeSum) + " ]");
		}
		
		frame.appendText("##AI Search NanoTime");
		frame.appendText("-time1 : " + Double.parseDouble((this.time1/1000000)+"")/1000 + " (초)");
		frame.appendText("-time2 : " + Double.parseDouble((this.time2/1000000)+"")/1000 + " (초)");
		frame.appendText("-time3 : " + Double.parseDouble((this.time3/1000000)+"")/1000 + " (초)");
		frame.appendText("-time4 : " + Double.parseDouble((this.time4/1000000)+"")/1000 + " (초)");
		frame.appendText("-time5 : " + Double.parseDouble((this.time5/1000000)+"")/1000 + " (초)");
		frame.appendText("-time6 : " + Double.parseDouble((this.time6/1000000)+"")/1000 + " (초)");
		
		
		frame.appendText("=================================================================");
		currPlayer = currPlayer == 1 ? 2 : 1;
	}

	public void doIt() {
		impossibleCheck = true;
		synchronized (this) {
			try {
				this.wait();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}

	public void wakeUpLock() {
		synchronized (this) {
			this.notifyAll();
		}
		currPlayer = currPlayer == 1 ? 2 : 1;
		impossibleCheck = false;
	}

	public boolean getChoiceBlock(int choicePosition) {
		if (impossibleCheck) {
			for (int i = 0; i < scale; i++) {
				for (int j = 0; j < scale; j++) {
					if ((i * scale) + j == choicePosition && board[i][j].getPlayer() == 0
							&& board[i][j].impossible(currPlayer)) {
						board[i][j].settingBoard(currPlayer);
						board[i][j].setEatNum(cnt);
						wakeUpLock();
						return true;
					}
				}
			}
		}
		return false;
	}

	public void stopThinking() {
		h.stopThinking();
	}

}
