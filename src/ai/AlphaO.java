package ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import game.Block;
import util.WinProb;

public class AlphaO implements OthelloAI {
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
	private String second_nw;
	private String second_ne;
	private String second_sw;
	private String second_se;
	private String ne;
	private	String sw;
	private String se;
	private int ne_int;
	private int sw_int;
	private int se_int;
	
	private int scale;
	
	private int enemyPlayer;
	
	int plusSide;
	int plusCorner;
	
	public AlphaO(int player, Block[][] board, int cnt, int lastCnt, int level) {
		this.player = player;
		this.board = board;
		this.cnt = cnt;
		this.lastCnt = lastCnt;
		this.level = level;
		second_nw = (board.length+1)+"";
		second_ne = (board.length*2-2)+"";
		second_sw = (((board.length-2)*board.length)+1)+"";
		second_se = ((board.length*(board.length-1))-2)+"";
		ne = (board.length - 1) + "";
		sw = ((board.length * board.length) - board.length) + "";
		se = ((board.length * board.length) - 1) + "";
		this.scale = board.length;
		if(this.cnt < (scale*scale-4)/2){
			plusSide = 2;
		}else{
			plusSide = 1;
		}
		plusCorner = (((scale * scale - 4) - this.cnt) / (scale / 2)) + 1;
		if (this.cnt >= (scale * scale - 4) - level){
			plusCorner = 1;
		}
		ne_int = scale - 1;
		sw_int = (scale * (scale - 1));
		se_int = (scale * scale) - 1;
		enemyPlayer = this.player == 1 ? 2 : 1;
	}
	
	class MultiThinking extends Thread{
		private Block[][] copyBoard;
		private int alpha;
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
			for (int i = 0; i < copyBoard.length; i++) {
				for (int j = 0; j < copyBoard[0].length; j++) {
					Block testBlock = copyBoard[i][j];
					if (testBlock.getPlayer() == 0) {
						emptyBlockList.add(testBlock);
					}
				}
			}
			
			this.alpha = routeSearchABP(player, choiceBlock, copyBoard , emptyBlockList, cnt+1, false, null,winProb);
			if(cnt+level < (board.length*board.length-4) && level > 5){
				if((choiceBlock.getStrSave().equals(second_nw) && copyBoard[0][0].getPlayer()==0) || 
						(choiceBlock.getStrSave().equals(second_ne) && copyBoard[0][board.length-1].getPlayer()==0) || 
						(choiceBlock.getStrSave().equals(second_sw) && copyBoard[board.length-1][0].getPlayer()==0) || 
						(choiceBlock.getStrSave().equals(second_se) && copyBoard[board.length-1][board.length-1].getPlayer()==0)){
					System.out.println("놓을 수 : "+choiceBlock.getStrSave()+" , 원래 알파 : "+this.alpha);
					this.alpha -= ((((board.length*board.length-4)-cnt)/(board.length/4))+1);
				}
			}
			System.out.println(" [ 블록검사중.. (" + originBlock.getStrSave() + ")번 블록 ] max : "+this.alpha+" 승률 : ("+winProb.getProbability()+" %)");
			
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
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[0].length; j++) {
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

	private Block think(List<Block> emptyBlockList) {
		List<Block> cloneList = new ArrayList<Block>();
		cloneList.addAll(emptyBlockList);
		List<Block> impBlockList = new ArrayList<Block>();
		for (Block b : cloneList) {
			if (b.impossible(player)) {
				impBlockList.add(b);
				if(this.cnt + level < (board.length*board.length-4)){
				/*if (b.getStrSave().equals("0") || b.getStrSave().equals(ne) || b.getStrSave().equals(sw)
						|| b.getStrSave().equals(se)) {
					return b;
				}*/
				}
			}
		}

		int plusdeps = 0;
		if(this.cnt >= 45){
			plusdeps = 4;
			level += plusdeps;
		}
		else if (this.cnt >= (board.length*board.length-4)-(board.length*board.length-4)/3) {
			plusdeps = 2;
			level += plusdeps;
		}else if(this.cnt >= (board.length*board.length-4)-((board.length*board.length-4)/6*3)){
			plusdeps = 1;
			level += plusdeps;
		}
		System.out.println("plusLevel : " + plusdeps);
		System.out.println("패널티 : " + timePenalty);
		System.out.println("생각해야할 변수 : " + impBlockList.size());
		Block choiceBlock = impBlockList.get(0);
		int cnt = 0;
		int maxScore = -1000;
		
		List<MultiThinking> thinkingList = new ArrayList<MultiThinking>();
		for (Block b : impBlockList) {
			// TODO: 갈림돌
			MultiThinking multi = new MultiThinking(board, b);
			thinkingList.add(multi);
			multi.start();
		}
		
		System.out.println(" [ 수읽기중.. ]");
		for(MultiThinking m : thinkingList){
			cnt++;
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
				probability = maxScore;
			}
		}
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[0].length; j++) {
				if (board[i][j].getPlayer() == 0)
					board[i][j].setStr((i * board.length) + j + "");
				board[i][j].setWinPro(0);
			}
		}
		System.out.println("총 검토한 수순 : " + calculCnt + ", 마지막 도달한 수순 : " + endCnt);
		System.out.println("Hermes가 승률이 높은 " + choiceBlock.getStr() + "지점에 착수를 둡니다.");
		return choiceBlock;
	}

	private int routeSearchABP(int currPlayer, Block b, Block[][] copyBoard, List<Block> emptyBlockList, int cnt,
			boolean searchMax, Integer minmax, WinProb winProb) {
		// TODO ABP1
		/*List<Block> cloneEmptyBlockList = new ArrayList<Block>();
		cloneEmptyBlockList.addAll(emptyBlockList);
		cloneEmptyBlockList.remove(b);*/

		b.settingBoardVirtual(currPlayer);
		
		int plusValue = 0;
		if (this.cnt + level < (board.length * board.length - 4)) {
				String num = b.getStrSave();
				if (num.equals("0") || num.equals(ne) || num.equals(sw) || num.equals(se)) {
					if(this.player == currPlayer){
						plusValue = ((((board.length * board.length - 4) - cnt) / (board.length / 4)) + 1);
					}else{
						plusValue = -((((board.length * board.length - 4) - cnt) / (board.length / 4)) + 1);
					}
				}
		}
		
		if (cnt > this.cnt + level || cnt > lastCnt) {
			int res = calculateScore(cnt,copyBoard,winProb);
			b.backUpLoad();
			return res+plusValue;
		}
		List<Block> cloneEmptyBlockList = new ArrayList<Block>();
		cloneEmptyBlockList.addAll(emptyBlockList);
		cloneEmptyBlockList.remove(b);
		currPlayer = currPlayer == 1 ? 2 : 1;
		
		List<Block> impBlockList = new ArrayList<Block>();
		for (Block impBlock : cloneEmptyBlockList) {
			if (impBlock.impossible(currPlayer)) {
				impBlockList.add(impBlock);
			}
		}
		boolean turnOver = false;
		if (impBlockList.isEmpty()) {
			currPlayer = currPlayer == 1 ? 2 : 1;
			searchMax = !searchMax;
			turnOver = true;
			for (Block impBlock : cloneEmptyBlockList) {
				if (impBlock.impossible(currPlayer)) {
					impBlockList.add(impBlock);
				}
			}
		}

		Integer ab = null;
		int nextPlayer = currPlayer;
		if (impBlockList.isEmpty()) {
			int res = calculateScore(cnt,copyBoard,winProb);
			b.backUpLoad();
			return res+plusValue;
		}

		for (int i = 0; i < impBlockList.size(); i++) {
			int tempWinCnt = 0;
			if(!turnOver)
				tempWinCnt = routeSearchABP( nextPlayer, impBlockList.get(i),copyBoard , cloneEmptyBlockList, cnt + 1,
					!searchMax, ab, winProb);
			else
				tempWinCnt = routeSearchABP( nextPlayer, impBlockList.get(i),copyBoard , cloneEmptyBlockList, cnt + 1,
						!searchMax, minmax, winProb);
			
			tempWinCnt += plusValue;
			
			if (ab == null) {
				ab = tempWinCnt;
			}
			if (searchMax) {
				if (minmax != null && tempWinCnt > minmax)
					break;
				if (ab < tempWinCnt)
					ab = tempWinCnt;
			} else {
				if (minmax != null && tempWinCnt < minmax)
					break;
				if (ab > tempWinCnt)
					ab = tempWinCnt;
			}
		}
		if ( cnt - this.cnt == 1 && this.player != currPlayer
				&& this.cnt + level < (board.length * board.length - 4)) {
			b.setRouteNum(impBlockList.size());
			int currPos = Integer.parseInt(b.getStrSave());
			if (currPos % scale == 0 || currPos % scale == 7 || currPos < scale
					|| currPos >= (scale * scale- scale)) {
				if(b.isDanger(this.player,scale)){
					System.out.println("# 놓을 수 : "+b.getStrSave()+" , 원래 알파 : "+ab+" 마이너스 : "+((((scale * scale - 4) - cnt) / scale) + 1));
					ab -= (((scale * scale - 4) - cnt) / scale) + 1;
				}else{
					ab += (((scale * scale - 4) - cnt) / scale) + 1;
				}
			}
			for(Block nextBlock : impBlockList){
				String num = nextBlock.getStrSave();
					if (num.equals("0") || num.equals(ne) || num.equals(sw) || num.equals(se)) {
						if(this.player != currPlayer){
							ab -= (((scale * scale - 4) - cnt) / (scale/2)) + 1;
						}
					}
					
				}
		}
		b.backUpLoad();
		return ab;
	}

	private int calculateScore(int currCnt,Block[][] copyBoard,WinProb winProb) {
		// TODO:calculateScore
		int aiArea = 0;
		int enemyArea = 0;
		
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[0].length; j++) {
				int posValue = (i * scale) + j;
				if (copyBoard[i][j].getPlayer() == this.player) {

					if (posValue == 0 || posValue == ne_int || posValue == sw_int || posValue == se_int) {
						aiArea += plusCorner;
					} else {
						if (posValue % scale == 0 || posValue % scale == 7 || posValue < scale
								|| posValue >= (scale * scale - scale)) {
							aiArea += plusSide;
						} else if (currCnt >= (scale * scale - 4) - level) {
							aiArea ++;
						} 
					}
				} else if (copyBoard[i][j].getPlayer() == enemyPlayer) {
					if (posValue == 0 || posValue == ne_int || posValue == sw_int || posValue == se_int) {
						enemyArea += (((scale * scale - 4) - currCnt) / scale) + 1;
					} else {
						if (posValue % scale == 0 || posValue % scale == 7) {
							enemyArea += plusSide;
						} else if (currCnt >= (scale * scale - 4) - level) {
							enemyArea ++;
						} 
					}
				}
			}
		}
		winProb.plusJudgement();
		if(aiArea > enemyArea){
			winProb.plusWin();
		}
		return aiArea - enemyArea;
	}

	public int getProbability() {
		return probability;
	}

	@Override
	public void stopThinking() {
		// TODO Auto-generated method stub
		
	}

	
}
