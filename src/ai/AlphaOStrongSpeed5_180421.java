package ai;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import game.Block;
import gui.GameView;
import util.WinProb;



/*
 * 2018-04-10 jang jongkyu corner를 굳힘돌로 수정함
 * 2018-04-21 jang jongkyu 둘 수가 많은곳, 소식, 굳힘돌 계싼을 모두 합산
 * 
 */
public class AlphaOStrongSpeed5_180421 implements OthelloAI {
	private int player;
	private Block[][] board;
	private int cnt;
	private int lastCnt;
	private int calculCnt;
	private int endCnt;

	private int timePenalty;
	private int level;

	private int originRouteCnt = 100;

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
	private GameView gameView;
	
	// 모든 수의 Alpha값 저장
	private int probSum;
	// 연산한 모든 수의 cnt
	private int calculatedCnt;

	// private Integer max;
	public AlphaOStrongSpeed5_180421(int player, Block[][] board, int cnt, int lastCnt, int level, GameView gameView) {
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

		/*
		 * else if(this.cnt >= lastCnt/2){ plusdeps = 1; level += plusdeps; }
		 */
		this.deps = this.cnt + level;
	}

	class MultiThinking extends Thread {
		private Block[][] copyBoard;
		private int alpha = -200;
		private Block choiceBlock;
		private Block originBlock;
		private WinProb winProb;

		public MultiThinking(Block[][] originBoard, Block originBlock) {
			this.copyBoard = board;
			this.originBlock = originBlock;
			this.winProb = new WinProb();
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
			this.alpha = routeSearchABP2(player, choiceBlock, copyBoard, emptyBlockList, cnt + 1, false, null, null);
			/*if(choiceBlock.isSide && !isPerfectLearnning){
				this.alpha++;
				gameView.appendText(choiceBlock.getStrSave()+" , Side Plus ++");
			}*/
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
		int playerImp = 0;
		int enemyImp = 0;
		for (Block b : cloneList) {
			if (b.impossible(player)) {
				impBlockList.add(b);
				playerImp++;
			}
			if(b.impossible(enemyPlayer)){
				enemyImp++;
			}
		}
/*		if(playerImp+enemyImp > 20){
			level--;
		}*/
		gameView.appendText("탐색깊이 : " + level);
		gameView.appendText("패널티 : " + timePenalty);
		gameView.appendText("생각해야할 변수 : " + impBlockList.size());
		Block choiceBlock = impBlockList.get(0);
		int maxScore = -200;
		
		Collections.shuffle(impBlockList); // 셔플

		thinkingList = new ArrayList<MultiThinking>();
		for (Block b : impBlockList) {
			// TODO: 갈림돌
			MultiThinking multi = new MultiThinking(board, b);
			thinkingList.add(multi);
			multi.start();
		}

		gameView.appendText(" [ 수읽기중.. ]");
		int minRouteNum = 0;
		for (MultiThinking m : thinkingList) {
			// cnt++;
			try {
				m.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			int winCnt = m.getAlpha();
			probSum += m.getAlpha();
			Block currBlock = m.getOriginBlock();
			if (maxScore == winCnt) {
				Random r = new Random();
				System.out.println("origin : " + originRouteCnt + " routeNum : " + m.choiceBlock.getRouteNum());
				if(r.nextBoolean()){
					choiceBlock = currBlock;
					maxScore = winCnt;
					minRouteNum = m.getRouteNum();
					originRouteCnt = m.choiceBlock.getRouteNum();
					probability = maxScore;
				} 
			} else if (maxScore < winCnt) {
				choiceBlock = currBlock;
				maxScore = winCnt;
				minRouteNum = m.getRouteNum();
				originRouteCnt = m.choiceBlock.getRouteNum();
				probability = maxScore;
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
		return choiceBlock;
	}

	private int routeSearchABP2(int currPlayer, Block b, Block[][] copyBoard, List<Block> emptyBlockList, int cnt,
			boolean searchMax, Integer max, Integer min) {
		// TODO ABP1
		// System.out.println(cnt - this.cnt);
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
				}
				if (min != null && tempWinCnt >= min)
					break;
			} else {
				if (min == null || min > tempWinCnt) {
					min = tempWinCnt;
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
		//System.out.println(" probSum : "+probSum +" , calculatedCnt : "+calculatedCnt+" , prob : "+(probSum / calculatedCnt));
		//return probSum / calculatedCnt;
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
				
				if(currBoard.getPlayer() == this.player) {
					if(currBoard.isCorner) {
						aiImpossible += plusCorner;
					}else if(isFixedBlock(copyBoard, i, j)){
						aiImpossible += plusFixedBlock;
					}else if(!currBoard.isSide){
						aiImpossible --;
					}
				}else if(currBoard.getPlayer() == enemyPlayer){
					if(currBoard.isCorner) {
						enemyImpossible += plusCorner;
					}else if(isFixedBlock(copyBoard, i, j)){
						enemyImpossible += plusFixedBlock;
					}else if(!currBoard.isSide){
						enemyImpossible --;
					}
				}
				
			}
			
		}
		
		return aiImpossible - enemyImpossible;
	}
	
	private int calculateScoreDwarfOpenning(int currCnt, Block[][] copyBoard, List<Block> emptyBlockList) {
		// TODO:calculateScore
		calculCnt++;
		int aiImpossible = 0;
		int enemyImpossible = 0;
		
		// TODO : 구석의 돌을 먹고있으면 구석점수 더먹음  -> 나중에 구석이 아닌 굳힘돌로 바꿔야함
		for(int i = 0 ; i < scale ; i++) {
			
			for(int j = 0 ; j < scale ; j++ ) {
				if(copyBoard[i][j].isCorner) {
					if(copyBoard[i][j].getPlayer() == this.player) {
						aiImpossible += plusCorner;
					}else if(copyBoard[i][j].getPlayer() == enemyPlayer) {
						enemyImpossible += plusCorner;
					}
				}else {
					if(copyBoard[i][j].getPlayer() == this.player) {
						aiImpossible--;
					}else if(copyBoard[i][j].getPlayer() == enemyPlayer) {
						enemyImpossible--;
					}
				}
			}
			
		}
		
		
		return aiImpossible - enemyImpossible;
	}
	
	public boolean isFixedBlock(Block[][] copyBoard, int checkI, int checkJ) {
		int currPlayer = copyBoard[checkI][checkJ].getPlayer();
		
		int controllValue = 0;
		
		
		
		if (copyBoard[0][0].getPlayer() == currPlayer) {
			
			loof8 : for (int i = checkI; i >= 0; i--) {

				if(checkJ + controllValue < 8)
				controllValue++;
				for (int j = 0 ; j < checkJ + controllValue; j++) {
					if(copyBoard[i][j].getPlayer() != currPlayer) {
						break loof8;
					}
				}

				if(i == 0 )
					return true;
			}
		controllValue = 0;

		loof1 : for(int i = 0 ; i < scale ; i++) {

			for(int j = 0 ; j <= checkJ + controllValue ; j++) {

				if(copyBoard[i][j].getPlayer() != currPlayer) {
					break loof1;
				}
			}

			if (i >= checkI)
				controllValue--;

			if(i == scale-1 )
				return true;
		}
		controllValue = 0;

		}
		
		
		if (copyBoard[7][0].getPlayer() == currPlayer) {

			loof2 : for(int i = scale-1 ; i >= 0 ; i--) {

				if(i <= checkI) controllValue ++;

				for(int j = 0 ; j < checkJ+1 - controllValue ; j++) {

					if(copyBoard[i][j].getPlayer() != currPlayer) {
						break loof2;
					}
				}

				if(i == 0 )
					return true;
			}
		controllValue = 0;


		loof7 : for (int i = checkI; i < 8; i++) {
			
			if(checkJ + controllValue < scale) controllValue++;
			
			for (int j = 0 ; j < checkJ + controllValue; j++) {
				if(copyBoard[i][j].getPlayer() != currPlayer) {
					break loof7;
				}
			}

			if(i == scale-1 )
				return true;
		}
		controllValue = 0;

		}
		
		
		
		if (copyBoard[7][7].getPlayer() == currPlayer) {

			loof3 : for(int i = checkI ; i < scale ; i++) {

				for(int j = checkJ - controllValue ; j < scale ; j++) {

					if(copyBoard[i][j].getPlayer() != currPlayer) {
						break loof3;
					}
				}

				if(controllValue < checkJ)
					controllValue++;

				if(i == scale-1 )
					return true;
			}
		controllValue = 0;



		loof4 : for(int i = 7 ; i >= scale ; i--) {

			if(i < checkI) controllValue++;

			for(int j = checkJ - controllValue ; j < scale ; j++) {

				if(copyBoard[i][j].getPlayer() != currPlayer) {
					break loof4;
				}
			}

			if(i == 0 )
				return true;
		}
		controllValue = 0;

		}
		
		
		if (copyBoard[0][7].getPlayer() == currPlayer) {

			loof5 : for(int i = 0 ; i < scale ; i++) {

				if(i > checkI) controllValue++;

				for(int j = checkJ + controllValue ; j < scale ; j++) {

					if(copyBoard[i][j].getPlayer() != currPlayer) {
						break loof5;
					}
				}

				if(i == scale-1 )
					return true;
			}
		controllValue = 0;


		loof6 : for(int i = checkI ; i >= 0 ; i--) {

			if (i < checkI && checkJ > controllValue ) controllValue++;

			for(int j = checkJ - controllValue ; j < scale ; j++) {

				if(copyBoard[i][j].getPlayer() != currPlayer) {
					break loof6;
				}
			}

			if(i == 0 )
				return true;
		}
		controllValue = 0;

		}
		
		
		
		return false;
	}
	

}
