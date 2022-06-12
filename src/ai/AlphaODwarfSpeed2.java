package ai;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import game.Block;
import gui.GameView;
import util.WinProb;

public class AlphaODwarfSpeed2 implements OthelloAI {
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
	/*private String ne;
	private	String sw;
	private String se;*/
	private int ne_int;
	private int sw_int;
	private int se_int;
	
	private int scale;
	
	private int enemyPlayer;
	
	private int plusSide;
	private int plusCorner;
	private boolean isPerfectLearnning;
	private Integer rootMax;
	private int deps;
	private List<MultiThinking> thinkingList;
	private GameView gameView;
	private boolean isSecondEnding;
	//private Integer max;
	public AlphaODwarfSpeed2(int player, Block[][] board, int cnt, int lastCnt, int level, GameView gameView) {
		this.gameView = gameView;
		this.player = player;
		this.board = board;
		this.cnt = cnt;
		this.lastCnt = lastCnt;
		this.level = level;
		this.scale = board.length;
		//this.max = max;
		plusLevel();
		second_nw = scale+1;
		second_ne = scale*2-2;
		second_sw = (((scale-2)*scale)+1);
		second_se = ((scale*(scale-1))-2);
		if(this.cnt < lastCnt/2){
			plusSide = 2;
		}else{
			plusSide = 1;
		}
		plusCorner = 20;
		if (isPerfectLearnning){
			plusCorner = 1;
		}
		ne_int = scale - 1;
		sw_int = (scale * (scale - 1));
		se_int = (scale * scale) - 1;
		enemyPlayer = this.player == 1 ? 2 : 1;
	}
	
	private void plusLevel() {
		int plusdeps = 0;
		if(this.cnt >= 44){
			plusdeps = 10;
			level += plusdeps;
			isPerfectLearnning = true;
		}else if(this.cnt >= 42){
			plusdeps = 0;
			level += plusdeps;
			lastCnt = 44;
			isSecondEnding = true;
		}
		else if (this.cnt >= 34) {
			level += plusdeps;
			lastCnt = 44;
			isSecondEnding = true;
		}else if(this.cnt >= lastCnt/2){
			plusdeps = 1;
			level += plusdeps;
			lastCnt = 44;
		}
		
		/*else if(this.cnt >= lastCnt/2){
			plusdeps = 1;
			level += plusdeps;
		}*/
		this.deps = this.cnt + level;
		gameView.appendText("탐색깊이 : " + level);
	}

	class MultiThinking extends Thread{
		private Block[][] copyBoard;
		private int alpha = -200;
		private Block choiceBlock;
		private Block originBlock;
		private WinProb winProb;
		public MultiThinking(Block[][] originBoard,Block originBlock){
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
					if(copyBoard[i][j].getStrSave().equals(originBlock.getStrSave())){
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
			//choiceBlock 초기화, emptyBlock
			List<Block> emptyBlockList = new ArrayList<Block>();
			for (int i = 0; i < scale; i++) {
				for (int j = 0; j < scale; j++) {
					Block testBlock = copyBoard[i][j];
					if (testBlock.getPlayer() == 0) {
						emptyBlockList.add(testBlock);
					}
				}
			}
			this.alpha = routeSearchABP2(player, choiceBlock, copyBoard , emptyBlockList, cnt+1, false, null, null, winProb);
			if(cnt+level < lastCnt ){
				int posValue = choiceBlock.getPosValue();
				if((posValue == second_nw && copyBoard[0][0].getPlayer()==0) || 
						(posValue == second_ne && copyBoard[0][scale-1].getPlayer()==0) || 
						(posValue == second_sw && copyBoard[scale-1][0].getPlayer()==0) || 
						(posValue == second_se && copyBoard[scale-1][scale-1].getPlayer()==0)){
					this.alpha -= plusCorner;
				}
			}
			
			synchronized (this) {
				if(rootMax == null || rootMax < this.alpha)
					rootMax = this.alpha;
			}
			gameView.appendText(" [ 블록검사중.. (" + originBlock.getStrSave() + ")번 블록 ] Alpha : "+this.alpha);
		}
		
		public WinProb getWinProb() {
			return winProb;
		}
		public void setWinProb(WinProb winProb) {
			this.winProb = winProb;
		}
		public double getProbability(){
			return winProb.getProbability();
		}
		public Block getOriginBlock() {
			return originBlock;
		}
		public int getAlpha(){
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
	public void stopThinking(){
		try{
			for(MultiThinking m : thinkingList){
				m.stop();
			}
			gameView.appendText("재촉 완료");
		}catch(Exception e){
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
		gameView.appendText("패널티 : " + timePenalty);
		gameView.appendText("생각해야할 변수 : " + impBlockList.size());
		Block choiceBlock = impBlockList.get(0);
		int maxScore = -200;
		
		thinkingList = new ArrayList<MultiThinking>();
		for (Block b : impBlockList) {
			// TODO: 갈림돌
			MultiThinking multi = new MultiThinking(board, b);
			thinkingList.add(multi);
			multi.start();
		}
		
		gameView.appendText(" [ 수읽기중.. ]");
		for(MultiThinking m : thinkingList){
			//cnt++;
			try {
				m.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			int winCnt = m.getAlpha();
			Block currBlock = m.getOriginBlock();
			if (maxScore == winCnt) {
				Random r = new Random();
				System.out.println("origin : "+originRouteCnt+" routeNum : "+m.choiceBlock.getRouteNum());
				if(originRouteCnt > m.choiceBlock.getRouteNum()){
					choiceBlock = currBlock;
					maxScore = winCnt;
					originRouteCnt = m.choiceBlock.getRouteNum();
					probability = maxScore;
				}else if (r.nextBoolean()) {
					choiceBlock = currBlock;
					maxScore = winCnt;
					originRouteCnt = m.choiceBlock.getRouteNum();
					probability = maxScore;
				} 
			} else if (maxScore < winCnt) {
				choiceBlock = currBlock;
				maxScore = winCnt;
				originRouteCnt = m.choiceBlock.getRouteNum();
				probability =  maxScore;
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
		gameView.appendText("AI가  " + choiceBlock.getStr() + "지점에 착수를 둡니다. 검토수순 : "+df.format(calculCnt));
		return choiceBlock;
	}
	
	
	private int routeSearchABP2(int currPlayer, Block b, Block[][] copyBoard, List<Block> emptyBlockList, int cnt,
			boolean searchMax, Integer max, Integer min, WinProb winProb) {
		// TODO ABP1
		//System.out.println(cnt - this.cnt);
		List<Block> cloneEmptyBlockList = new ArrayList<Block>();
		cloneEmptyBlockList.addAll(emptyBlockList);
		cloneEmptyBlockList.remove(b);
		
		b.settingBoardVirtual(currPlayer);
		
		if (cnt > deps || cnt > lastCnt) {
			int res = 0;
			if(isPerfectLearnning){
				res = calculateScore(cnt,copyBoard,winProb);
			}else if(isSecondEnding){
				res = calculateScoreSecondEnding(cnt, copyBoard, winProb);
			}else{
				res = calculateScoreOpenning(cnt,copyBoard,winProb);
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
			int res = 0;
			if(isPerfectLearnning){
				res = calculateScore(cnt,copyBoard,winProb);
			}else if(isSecondEnding){
				res = calculateScoreSecondEnding(cnt, copyBoard, winProb);
			}else{
				res = calculateScoreOpenning(cnt,copyBoard,winProb);
			}
			b.backUpLoad();
			return res;
		}
		//TODO : 아래쪽 for문에서 돌릴지 고민
		
		int forSize =  impBlockList.size();
		for (int i = 0; i < forSize ; i++) {
			Block nextChoice = impBlockList.get(i);
			int tempWinCnt = routeSearchABP2( nextPlayer, nextChoice, copyBoard , cloneEmptyBlockList, cnt + 1,
					!searchMax, max, min, winProb);
			
				
			if (searchMax) {
				if (max == null || max < tempWinCnt){
					max = tempWinCnt;
				}
				if (min != null && tempWinCnt >= min)
					break;
			} else {
				if (min == null || min > tempWinCnt){
					min = tempWinCnt;
				}
				if (max != null && tempWinCnt <= max )
					break;
			}
		}
		
		//처음엔 false
		b.backUpLoad();
		if(searchMax){
			return max;
		}else{
			return min;
		}
	}
	
	private int calculateScore(int currCnt, Block[][] copyBoard, WinProb winProb) {
		// TODO:calculateScore
		int aiArea = 0;
		int enemyArea = 0;
		
		for (int i = 0; i < scale ; i++) {
			for (int j = 0; j < scale ; j++) {
				if (copyBoard[i][j].getPlayer() == this.player) {
					aiArea ++;
				} else if (copyBoard[i][j].getPlayer() == enemyPlayer) {
					enemyArea ++;
				}
			}
		}
		return aiArea - enemyArea;
	}

	public int getProbability() {
		return probability;
	}
	
	private int calculateScoreOpenning(int currCnt, Block[][] copyBoard, WinProb winProb) {
		calculCnt++;
		// TODO:calculateScore
		int aiArea = 0;
		int enemyArea = 0;
		
		for (int i = 0; i < scale ; i++) {
			for (int j = 0; j < scale ; j++) {
				if (copyBoard[i][j].getPlayer() == this.player) {
					if (copyBoard[i][j].isCorner) {
						aiArea += plusCorner;
					} else {
						if(copyBoard[i][j].isSide){
							int posValue = copyBoard[i][j].getPosValue();
							if (posValue % scale == 0 ) {
								if((this.player == copyBoard[0][0].getPlayer()) || (this.player == copyBoard[7][0].getPlayer())){
									aiArea++;
								}else{
									aiArea--;
								}
							}
							if(posValue % scale == 7){
								if((this.player == copyBoard[0][7].getPlayer()) || (this.player == copyBoard[7][7].getPlayer())){
									aiArea++;
								}else{
									aiArea--;
								}
							}
							if(posValue < scale){
								if((this.player == copyBoard[0][0].getPlayer()) || (this.player == copyBoard[0][7].getPlayer())){
									aiArea++;
								}else{
									aiArea--;
								}
							}
							if(posValue >= 56){
								if((this.player == copyBoard[7][0].getPlayer()) || (this.player == copyBoard[7][7].getPlayer())){
									aiArea++;
								}else{
									aiArea--;
								}
							}
						}else{
							aiArea --;
						}
					}
				} else if (copyBoard[i][j].getPlayer() == enemyPlayer) {
					if (copyBoard[i][j].isCorner) {
						enemyArea += plusCorner;
					} else {
						if(copyBoard[i][j].isSide){
							int posValue = copyBoard[i][j].getPosValue();
							if (posValue % scale == 0 ) {
								if((enemyPlayer == copyBoard[0][0].getPlayer()) || (enemyPlayer == copyBoard[7][0].getPlayer())){
									aiArea++;
								}else{
									aiArea--;
								}
							}
							if(posValue % scale == 7){
								if((enemyPlayer == copyBoard[0][7].getPlayer()) || (enemyPlayer == copyBoard[7][7].getPlayer())){
									aiArea++;
								}else{
									aiArea--;
								}
							}
							if(posValue < scale){
								if((enemyPlayer == copyBoard[0][0].getPlayer()) || (enemyPlayer == copyBoard[0][7].getPlayer())){
									aiArea++;
								}else{
									aiArea--;
								}
							}
							if(posValue >= 56){
								if((enemyPlayer == copyBoard[7][0].getPlayer()) || (enemyPlayer == copyBoard[7][7].getPlayer())){
									aiArea++;
								}else{
									aiArea--;
								}
							}
						}else{
							enemyArea --;
						}
					}
				}
			}
		}
		if(aiArea == 0){
			return -200;
		}
		return aiArea - enemyArea;
	}
	private int calculateScoreSecondEnding(int currCnt, Block[][] copyBoard, WinProb winProb) {
		calculCnt++;
		// TODO:calculateScore
		int aiArea = 0;
		int enemyArea = 0;
		
		for (int i = 0; i < scale ; i++) {
			for (int j = 0; j < scale ; j++) {
				if (copyBoard[i][j].getPlayer() == this.player) {
					if (copyBoard[i][j].isCorner) {
						aiArea += plusCorner;
					}else{
						aiArea++;
					}
				} else if (copyBoard[i][j].getPlayer() == enemyPlayer) {
					if (copyBoard[i][j].isCorner) {
						enemyArea += plusCorner;
					}else{
						enemyArea ++;
					}
				}
			}
		}
		if(aiArea == 0){
			return -200;
		}
		return aiArea - enemyArea;
	}

}
