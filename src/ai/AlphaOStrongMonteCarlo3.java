package ai;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.swing.JOptionPane;

import game.Block;
import gui.GameView;
import montecarlo.MonteCarloStatus;
import util.Judge;
import util.WinProb;



/*
 * 2018-04-10 jang jongkyu corner를 굳힘돌로 수정함
 * 2018-04-21 jang jongkyu 둘 수가 많은곳, 소식, 굳힘돌 계싼을 모두 합산
 * 
 */
public class AlphaOStrongMonteCarlo3 implements OthelloAI {
	private int player;
	private Block[][] board;
	private int cnt;
	private int lastCnt;
	private int calculCnt;
	private int endCnt;

	private int timePenalty;
	private int level;

	private int probability;
	private int second_nw;
	private int second_ne;
	private int second_sw;
	private int second_se;
	/*
	 * private String ne; private String sw; private String se;
	 */
	private int ne_int;
	private int sw_int;
	private int se_int;

	private int scale;

	private int enemyPlayer;

	private int plusSide;
	private int plusCorner;
	private int plusFixedBlock;
	
	
	private boolean isPerfectLearnning;
	private Integer rootMax;
	private int deps;
	private List<MultiThinking> thinkingList;
	private List<MonteCarloSearch> monteCarloList;
	private GameView gameView;
	
	// 모든 수의 Alpha값 저장
	private int probSum;
	// 연산한 모든 수의 cnt
	private int calculatedCnt;

	// private Integer max;
	public AlphaOStrongMonteCarlo3(int player, Block[][] board, int cnt, int lastCnt, int level, GameView gameView) {
		this.gameView = gameView;
		this.player = player;
		this.board = board;
		this.cnt = cnt;
		this.lastCnt = lastCnt;
		this.level = level;
		this.scale = board.length;
		// this.max = max;
		plusLevel();
		second_nw = scale + 1;
		second_ne = scale * 2 - 2;
		second_sw = (((scale - 2) * scale) + 1);
		second_se = ((scale * (scale - 1)) - 2);
		if (this.cnt < lastCnt / 2) {
			plusSide = 2;
		} else {
			plusSide = 1;
		}
		plusCorner = 15;
		plusFixedBlock = 3;
		
		if (isPerfectLearnning) {
			plusCorner = 1;
		}
		ne_int = scale - 1;
		sw_int = (scale * (scale - 1));
		se_int = (scale * scale) - 1;
		enemyPlayer = this.player == 1 ? 2 : 1;
	}

	private void plusLevel() {
		int plusdeps = 0;
		if (this.cnt >= 44) {
			plusdeps = 10;
			level += plusdeps;
			isPerfectLearnning = true;
		} else {
			lastCnt = 48;
		}

		this.deps = this.cnt + level;
	}

	@Override
	public Block choice() {
		List<Block> emptyBlockList = new ArrayList<Block>();
		Block choiceBlock = null;
		for (int i = 0; i < scale; i++) {
			for (int j = 0; j < scale; j++) {
				Block testBlock = board[i][j];
				if (choiceBlock == null)
					choiceBlock = testBlock;
				if (testBlock.getPlayer() == 0) {
					emptyBlockList.add(testBlock);
				}
			}
		}
		choiceBlock = think(emptyBlockList);
		return choiceBlock;
	}

	public void stopThinking() {
		try {
			for (MultiThinking m : thinkingList) {
				m.stop();
			}
			for(MonteCarloSearch c : monteCarloList) {
				c.stop();
			}
			
			gameView.appendText("재촉 완료");
		} catch (Exception e) {
			System.out.println("재촉실패");
			e.printStackTrace();
		}
	}

	private Block think(List<Block> emptyBlockList) {
		List<Block> cloneList = new ArrayList<Block>();
		cloneList.addAll(emptyBlockList);
		List<Block> impBlockList = new ArrayList<Block>();
		for (Block b : cloneList) {
			if (b.impossible(player)) {
				impBlockList.add(b);
			}
		}
		gameView.appendText("탐색깊이 : " + level);
		gameView.appendText("패널티 : " + timePenalty);
		gameView.appendText("생각해야할 변수 : " + impBlockList.size());
		Block choiceBlock = impBlockList.get(0);
		double maxScore = -200;
		
		Collections.shuffle(impBlockList); // 셔플

		thinkingList = new ArrayList<MultiThinking>();
		monteCarloList = new ArrayList<MonteCarloSearch>();
		try {
		for (Block b : impBlockList) {
			MonteCarloSearch monte = new MonteCarloSearch(board, b, 10000 , this.cnt);
			MultiThinking multi = new MultiThinking(board, b, monte);
			thinkingList.add(multi);
			monteCarloList.add(monte);
			multi.start();
			//if(!isPerfectLearnning) monte.start();
		}
		}catch(Exception e) {
			e.printStackTrace();
		}

		gameView.appendText(" [ 수읽기중.. ]");
		MultiThinking choiceM = thinkingList.get(0);
		for (MultiThinking m : thinkingList) {
			try {
				m.join();
				if(!isPerfectLearnning)
					m.getmonteCarloSearch().join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			double winCnt = (int) m.getAlphaProb();
			probSum += m.getAlphaProb();
			Block currBlock = m.getOriginBlock();
			if (maxScore == winCnt) {
				Random r = new Random();
				if(r.nextBoolean()){
					choiceM = m;
					choiceBlock = currBlock;
					maxScore = winCnt;
					probability = (int) maxScore;
				} 
			} else if (maxScore < winCnt) {
				choiceM = m;
				choiceBlock = currBlock;
				maxScore = winCnt;
				probability = (int) maxScore;
			}
		}
		for (int i = 0; i < scale; i++) {
			for (int j = 0; j < scale; j++) {
				if (board[i][j].getPlayer() == 0)
					board[i][j].setStr((i * scale) + j + "");
				board[i][j].setWinPro(0);
			}
		}
		DecimalFormat df = new DecimalFormat("#,###");
		gameView.appendText("AI가  " + choiceBlock.getStr() + "지점에 착수를 둡니다. 검토수순 : " + df.format(calculCnt));
		gameView.appendText("[MonteCarlo Value] 승률 : "+(choiceM.getmonteCarloSearch().getWinProb().getProbability())+" % ");
		gameView.appendText("[MonteCarlo Value] AI 컨디션 : "+(choiceM.getmonteCarloSearch().getAlphaAvg()));
		return choiceBlock;
	}

	
	private int calculateScore(int currCnt, Block[][] copyBoard) {
		// TODO:calculateScore
		calculCnt++;
		int aiArea = 0;
		int enemyArea = 0;

		for (int i = 0; i < scale; i++) {
			for (int j = 0; j < scale; j++) {
				if (copyBoard[i][j].getPlayer() == this.player) {
					aiArea++;
				} else if (copyBoard[i][j].getPlayer() == enemyPlayer) {
					enemyArea++;
				}
			}
		}
		return aiArea - enemyArea;
	}

	public int getProbability() {
		return probability;
	}

	private int calculateScoreOpenning(int currCnt, Block[][] copyBoard, List<Block> emptyBlockList) {
		// TODO:calculateScore
		calculCnt++;
		int aiImpossible = 0;
		int enemyImpossible = 0;

		for (Block b : emptyBlockList) {
				if (b.impossible(this.player)) {
					aiImpossible += 2;
				}
				if (b.impossible(enemyPlayer)) {
					enemyImpossible += 2;
				}
		}
		
		// TODO : 구석의 돌을 먹고있으면 구석점수 더먹음  -> 나중에 구석이 아닌 굳힘돌로 바꿔야함
		for(int i = 0 ; i < scale ; i++) {
			
			for(int j = 0 ; j < scale ; j++ ) {
				Block currBoard = copyBoard[i][j];
				// TODO : 사이드일경우 스코어 감소 안시키는 로직 임시삭제
				if(currBoard.getPlayer() == this.player) {
					if(currBoard.isCorner) {
						aiImpossible += plusCorner;
					}else if(Judge.isFixedBlock(copyBoard, i, j)){
						aiImpossible += plusFixedBlock;
					}else /*if(!currBoard.isSide)*/{
						aiImpossible --;
					}
				}else if(currBoard.getPlayer() == enemyPlayer){
					if(currBoard.isCorner) {
						enemyImpossible += plusCorner;
					}else if(Judge.isFixedBlock(copyBoard, i, j)){
						enemyImpossible += plusFixedBlock;
					}else /*if(!currBoard.isSide)*/{
						enemyImpossible --;
					}
				}
				
			}
			
		}
		
		return aiImpossible - enemyImpossible;
	}
	
	
	
	
	
	
	
	
	
	class MultiThinking extends Thread {
		private Block[][] copyBoard;
		private int alpha = -200;
		private Block choiceBlock;
		private Block originBlock;
		private WinProb winProb;
		private MonteCarloSearch monteCarloSearch;

		public MultiThinking(Block[][] originBoard, Block originBlock, MonteCarloSearch monteCarloSearch) {
			//this.copyBoard = board;
			this.originBlock = originBlock;
			this.winProb = new WinProb();
			this.monteCarloSearch = monteCarloSearch;
			init(originBoard);
		}

		public void init(Block[][] originBoard) {
			copyBoard = new Block[scale][scale];
			for (int i = 0; i < scale; i++) {
				for (int j = 0; j < scale; j++) {
					copyBoard[i][j] = new Block();
					copyBoard[i][j].setStr((scale * i) + j + "");
					copyBoard[i][j].saveStr();
					copyBoard[i][j].setPlayer(originBoard[i][j].getPlayer());
					if (copyBoard[i][j].getStrSave().equals(originBlock.getStrSave())) {
						this.choiceBlock = copyBoard[i][j];
					}

					int posValue = copyBoard[i][j].getPosValue();
					if (posValue == 0 || posValue == ne_int || posValue == sw_int || posValue == se_int) {
						copyBoard[i][j].isCorner = true;
					} else {
						if (posValue % scale == 0 || posValue % scale == 7 || posValue < scale
								|| posValue >= (scale * scale - scale)) {
							copyBoard[i][j].isSide = true;
						}
					}
				}
			}
			initNeigher();
		}

		private void initNeigher() {
			for (int i = 0; i < scale; i++) {
				for (int j = 0; j < scale; j++) {
					if (i > 0 && j > 0)
						copyBoard[i][j].setNorth_west(copyBoard[i - 1][j - 1]);
					if (i > 0)
						copyBoard[i][j].setNorth(copyBoard[i - 1][j]);
					if (i > 0 && j < scale - 1)
						copyBoard[i][j].setNorth_east(copyBoard[i - 1][j + 1]);
					if (j > 0)
						copyBoard[i][j].setWest(copyBoard[i][j - 1]);
					if (j < scale - 1)
						copyBoard[i][j].setEast(copyBoard[i][j + 1]);
					if (i < scale - 1 && j > 0)
						copyBoard[i][j].setSouth_west(copyBoard[i + 1][j - 1]);
					if (i < scale - 1)
						copyBoard[i][j].setSouth(copyBoard[i + 1][j]);
					if (i < scale - 1 && j < scale - 1)
						copyBoard[i][j].setSouth_east(copyBoard[i + 1][j + 1]);
				}
			}
		}

		@Override
		public void run() {
			// choiceBlock 초기화, emptyBlock
			List<Block> emptyBlockList = new ArrayList<Block>();
			for (int i = 0; i < scale; i++) {
				for (int j = 0; j < scale; j++) {
					Block testBlock = copyBoard[i][j];
					if (testBlock.getPlayer() == 0) {
						emptyBlockList.add(testBlock);
					}
				}
			}
			//this.alpha = routeSearchABP2(player, choiceBlock, copyBoard, emptyBlockList, cnt + 1, false, null, null);
			if(!isPerfectLearnning) {
				MonteCarloStatus monte = routeSearchABP3(player, choiceBlock, copyBoard, emptyBlockList, cnt + 1, false, null, null);
				this.alpha = monte.getRes();
				monteCarloSearch.setMonteCarloStatus(monte);
				monteCarloSearch.start();
			}else {
				this.alpha = routeSearchABP2(player, choiceBlock, copyBoard, emptyBlockList, cnt + 1, false, null, null);
			}
			
			
			if (!isPerfectLearnning) {
				int posValue = choiceBlock.getPosValue();
				if ((posValue == second_nw && copyBoard[0][0].getPlayer() == 0)
						|| (posValue == second_ne && copyBoard[0][scale - 1].getPlayer() == 0)
						|| (posValue == second_sw && copyBoard[scale - 1][0].getPlayer() == 0)
						|| (posValue == second_se && copyBoard[scale - 1][scale - 1].getPlayer() == 0)) {
					this.alpha -= plusCorner;
				}
			}

			synchronized (this) {
				if (rootMax == null || rootMax < this.alpha)
					rootMax = this.alpha;
				
				calculatedCnt++;
			}
			gameView.appendText(" [ 블록검사중.. (" + originBlock.getStrSave() + ")번 블록 ] Alpha : " + this.alpha);
		}

		public int getRouteNum(){
			return choiceBlock.getRouteNum();
		}
		
		public WinProb getWinProb() {
			return winProb;
		}

		public void setWinProb(WinProb winProb) {
			this.winProb = winProb;
		}

		public double getProbability() {
			return winProb.getProbability();
		}

		public Block getOriginBlock() {
			return originBlock;
		}

		public int getAlpha() {
			return alpha;
		}
		
		public double getAlphaProb() {
			if(isPerfectLearnning) return getAlpha();
			
			double res = (double)this.alpha + (monteCarloSearch.getAlphaAvg()/(double)2);
			System.out.println("[ "+originBlock.getPosValue()+" ] alpha : "+this.alpha + " , 백분율 : "+ monteCarloSearch.winProb.getProbability() + " alphaPlus : "+ (monteCarloSearch.getAlphaAvg()/(double)2));
			return res;
		}
		
		public MonteCarloSearch getmonteCarloSearch() {
			return this.monteCarloSearch;
		}
		
		
		private int routeSearchABP2(int currPlayer, Block b, Block[][] copyBoard, List<Block> emptyBlockList, int cnt,
				boolean searchMax, Integer max, Integer min) {
			List<Block> cloneEmptyBlockList = new ArrayList<Block>();
			cloneEmptyBlockList.addAll(emptyBlockList);
			cloneEmptyBlockList.remove(b);

			b.settingBoardVirtual(currPlayer);

			if (cnt > deps || cnt > lastCnt) {
				int res = 0;
				if (isPerfectLearnning) {
					res = calculateScore(cnt, copyBoard);
				} else {
					res = calculateScoreOpenning(cnt, copyBoard, cloneEmptyBlockList);
				}
				b.backUpLoad();
				return res;
			}

			currPlayer = currPlayer == 1 ? 2 : 1;
			List<Block> impBlockList = new ArrayList<Block>();
			for (Block impBlock : cloneEmptyBlockList) {
				if (impBlock.impossible(currPlayer)) {
					impBlockList.add(impBlock);
				}
			}
			

			if (impBlockList.isEmpty()) {
				currPlayer = currPlayer == 1 ? 2 : 1;
				searchMax = !searchMax;
				for (Block impBlock : cloneEmptyBlockList) {
					if (impBlock.impossible(currPlayer)) {
						impBlockList.add(impBlock);
					}
				}
			}

			int nextPlayer = currPlayer;
			if (impBlockList.isEmpty()) {
				int res = calculateScore(cnt, copyBoard);
				b.backUpLoad();
				return res;
			}
			// TODO : 아래쪽 for문에서 돌릴지 고민

			int forSize = impBlockList.size();
			++cnt;
			
			//섞기
			Collections.shuffle(impBlockList);
			for (int i = 0; i < forSize; i++) {
				Block nextChoice = impBlockList.get(i);
				int tempWinCnt = routeSearchABP2(nextPlayer, nextChoice, copyBoard, cloneEmptyBlockList, cnt,
						!searchMax, max, min);

				if (searchMax) {
					if (max == null || max < tempWinCnt) {
						max = tempWinCnt;
						//this.monteCarloSearch.init(copyBoard, b, cnt, nextPlayer);
					}
					if (min != null && tempWinCnt >= min)
						break;
				} else {
					if (min == null || min > tempWinCnt) {
						min = tempWinCnt;
						//this.monteCarloSearch.init(copyBoard, b, cnt, nextPlayer);
					}
					if (max != null && tempWinCnt <= max)
						break;
				}
			}

			// 처음엔 false
			b.backUpLoad();
			if (searchMax) {
				return max;
			} else {
				return min;
			}
		}
		
		
		
		private MonteCarloStatus routeSearchABP3(int currPlayer, Block b, Block[][] copyBoard, List<Block> emptyBlockList, int cnt,
				boolean searchMax, Integer max, Integer min) {
			List<Block> cloneEmptyBlockList = new ArrayList<Block>();
			cloneEmptyBlockList.addAll(emptyBlockList);
			cloneEmptyBlockList.remove(b);

			b.settingBoardVirtual(currPlayer);

			if (cnt > deps || cnt > lastCnt) {
				int res = 0;
				MonteCarloStatus monte = null;
				if (isPerfectLearnning) {
					res = calculateScore(cnt, copyBoard);
				} else {
					res = calculateScoreOpenning(cnt, copyBoard, cloneEmptyBlockList);
				}
				monte = new MonteCarloStatus(copyBoard, b, cnt, res, currPlayer);
				
				b.backUpLoad();
				return monte;
			}

			currPlayer = currPlayer == 1 ? 2 : 1;
			List<Block> impBlockList = new ArrayList<Block>();
			for (Block impBlock : cloneEmptyBlockList) {
				if (impBlock.impossible(currPlayer)) {
					impBlockList.add(impBlock);
				}
			}
			

			if (impBlockList.isEmpty()) {
				currPlayer = currPlayer == 1 ? 2 : 1;
				searchMax = !searchMax;
				for (Block impBlock : cloneEmptyBlockList) {
					if (impBlock.impossible(currPlayer)) {
						impBlockList.add(impBlock);
					}
				}
			}

			int nextPlayer = currPlayer;
			if (impBlockList.isEmpty()) {
				MonteCarloStatus monte = null;
				int res = calculateScore(cnt, copyBoard);
				monte = new MonteCarloStatus(copyBoard, b, cnt, res, currPlayer);
				b.backUpLoad();
				return monte;
			}
			// TODO : 아래쪽 for문에서 돌릴지 고민

			int forSize = impBlockList.size();
			++cnt;
			
			//섞기
			Collections.shuffle(impBlockList);
			MonteCarloStatus monte = null;
			for (int i = 0; i < forSize; i++) {
				Block nextChoice = impBlockList.get(i);
				MonteCarloStatus monteTemp = routeSearchABP3(nextPlayer, nextChoice, copyBoard, cloneEmptyBlockList, cnt,
						!searchMax, max, min);

				if (searchMax) {
					if (monte == null || max == null || max < monteTemp.getRes()) {
						max = monteTemp.getRes();
						monte = monteTemp;
						//this.monteCarloSearch.init(copyBoard, b, cnt, nextPlayer);
					}
					if (monte != null && min != null && monteTemp.getRes() >= min)
						break;
				} else {
					if (monte == null || min == null || min > monteTemp.getRes()) {
						min = monteTemp.getRes();
						monte = monteTemp;
						//this.monteCarloSearch.init(copyBoard, b, cnt, nextPlayer);
					}
					if (monte != null && max != null && monteTemp.getRes() <= max)
						break;
				}
			}

			// 처음엔 false
			b.backUpLoad();
			return monte;
/*			if (searchMax) {
				return max;
			} else {
				return min;
			}*/
		}
		
	}
	
	
	// 임시 보드 console Draw
	
	public void drawBoard(Block[][] board,int res) {
		int p1Block = 0;
		int p2Block = 0;
		for (int i = 0; i < scale; i++) {
			for (int j = 0; j < scale; j++) {
				if (board[i][j].getPlayer() == 1) {
					p1Block++;
					board[i][j].setStr("O");
				} else if (board[i][j].getPlayer() == 2) {
					p2Block++;
					board[i][j].setStr("X");
				} 
				
				System.out.printf("[%2s]", board[i][j].getStr() + "");
			}
			System.out.println();
		}
		System.out.println("\n[" + this.cnt + "]현재 스코어 : player1(" + p1Block + ") : (" + p2Block + ")player2");
		System.out.println(" RES : "+ res);
	}
	
	
	
	
	
	class MonteCarloSearch extends Thread {
		private Block[][] copyBoard;
		private int alpha = -200;
		private Block choiceBlock;
		private Block originBlock;
		private WinProb winProb;
		private int maxPlay;
		private int cnt;
		private long alphaSum;
		private int currPlayer;
		private int res;

		public MonteCarloSearch(Block[][] originBoard, Block originBlock, int maxPlay, int cnt) {
			this.winProb = new WinProb();
			this.maxPlay = maxPlay;
		}
		
		public void setMonteCarloStatus(MonteCarloStatus monteCarloStatus) {
			this.originBlock = monteCarloStatus.getOriginBlock();
			this.choiceBlock = monteCarloStatus.getChoiceBlock();
			this.cnt = monteCarloStatus.getCnt();
			this.currPlayer = monteCarloStatus.getCurrPlayer();
			this.copyBoard = monteCarloStatus.getCopyBoard();
			this.res = monteCarloStatus.getRes();
			initNeigher();
			//this.notify();
		}


		private void initNeigher() {
			for (int i = 0; i < scale; i++) {
				for (int j = 0; j < scale; j++) {
					if (i > 0 && j > 0)
						copyBoard[i][j].setNorth_west(copyBoard[i - 1][j - 1]);
					if (i > 0)
						copyBoard[i][j].setNorth(copyBoard[i - 1][j]);
					if (i > 0 && j < scale - 1)
						copyBoard[i][j].setNorth_east(copyBoard[i - 1][j + 1]);
					if (j > 0)
						copyBoard[i][j].setWest(copyBoard[i][j - 1]);
					if (j < scale - 1)
						copyBoard[i][j].setEast(copyBoard[i][j + 1]);
					if (i < scale - 1 && j > 0)
						copyBoard[i][j].setSouth_west(copyBoard[i + 1][j - 1]);
					if (i < scale - 1)
						copyBoard[i][j].setSouth(copyBoard[i + 1][j]);
					if (i < scale - 1 && j < scale - 1)
						copyBoard[i][j].setSouth_east(copyBoard[i + 1][j + 1]);
				}
			}
		}

		@Override
		public void run() {
			try {
				drawBoard(copyBoard, res);
				
				List<Block> emptyBlockList = new ArrayList<Block>();
				for (int i = 0; i < scale; i++) {
					for (int j = 0; j < scale; j++) {
						Block testBlock = copyBoard[i][j];
						if (testBlock.getPlayer() == 0) {
							emptyBlockList.add(testBlock);
						}
					}
				}
			
				for(int i = 0 ; i < maxPlay ; i++) {
					int alpha = routeSearchMontecarlo(currPlayer, choiceBlock, copyBoard, emptyBlockList, cnt);
					winProb.plusJudgement();
					this.alphaSum += alpha;
					if(alpha > 0) {
						winProb.plusWin();
					}
				}

			synchronized (this) {
				if (rootMax == null || rootMax < this.alpha)
					rootMax = this.alpha;
				
				calculatedCnt++;
			}
			
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		
		// 임시 몬테카를로식
		
		private int routeSearchMontecarlo(int currPlayer, Block b, Block[][] copyBoard, List<Block> emptyBlockList, int cnt) {
			
			List<Block> cloneEmptyBlockList = new ArrayList<Block>();
			cloneEmptyBlockList.addAll(emptyBlockList);
			cloneEmptyBlockList.remove(b);

			b.settingBoardVirtual(currPlayer);

			if(this.cnt > 38) {
				if(cnt > (scale * scale - 4)) {
					int res = 0;
					res = calculateScore(cnt, copyBoard);
					b.backUpLoad();
					return res;
				}
			}else {
			
				if (cnt > this.cnt + 12 || cnt > lastCnt) {
					int res = 0;
					res = calculateScoreOpenning(cnt, copyBoard, cloneEmptyBlockList);
					b.backUpLoad();
					return res;
				}
			}

			currPlayer = currPlayer == 1 ? 2 : 1;
			List<Block> impBlockList = new ArrayList<Block>();
			for (Block impBlock : cloneEmptyBlockList) {
				if (impBlock.impossible(currPlayer)) {
					impBlockList.add(impBlock);
				}
			}
			

			if (impBlockList.isEmpty()) {
				currPlayer = currPlayer == 1 ? 2 : 1;
				for (Block impBlock : cloneEmptyBlockList) {
					if (impBlock.impossible(currPlayer)) {
						impBlockList.add(impBlock);
					}
				}
			}

			int nextPlayer = currPlayer;
			if (impBlockList.isEmpty()) {
				int res = calculateScore(cnt, copyBoard);
				//int res = calculateScore(cnt, copyBoard);
				b.backUpLoad();
				return res;
			}
			// TODO : 아래쪽 for문에서 돌릴지 고민

			++cnt;
			//섞기
			Random random = new Random();
			Block nextChoice = impBlockList.get(random.nextInt(impBlockList.size()));
			int tempWinCnt = routeSearchMontecarlo(nextPlayer, nextChoice, copyBoard, cloneEmptyBlockList, cnt);
			b.backUpLoad();
			return tempWinCnt;
			
		}
		
		public double getAlphaAvg() {
			return this.alphaSum / (double)this.maxPlay;
		}
		public int getRouteNum(){
			return choiceBlock.getRouteNum();
		}
		
		public WinProb getWinProb() {
			return winProb;
		}

		public void setWinProb(WinProb winProb) {
			this.winProb = winProb;
		}

		public double getProbability() {
			return winProb.getProbability();
		}

		public Block getOriginBlock() {
			return originBlock;
		}

		public int getAlpha() {
			return alpha;
		}
		
	}
	
	
	
	
	
	
	

}
