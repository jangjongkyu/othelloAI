package ai;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import game.Block;
import gui.GameView;
import util.WinProb;



/*
 * AlphaOStrongMonteCarlo2_1 ������
 * TODO : �������ɰ���
 * git ����
 */
public class AlphaOStrongMonteCarlo20220606 implements OthelloAI {
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
	private List<MonteCarloSearch> monteCarloList;
	private GameView gameView;
	
	// ��� ���� Alpha�� ����
	private int probSum;
	// ������ ��� ���� cnt
	private int calculatedCnt;
	
	//�ɸ��ð�
	private Long time1 = 0L;
	private Long time2 = 0L;
	private Long time3 = 0L;
	private Long time4 = 0L;
	private Long time5 = 0L;
	private Long time6 = 0L;

	// private Integer max;
	public AlphaOStrongMonteCarlo20220606(int player, Block[][] board, int cnt, int lastCnt, int level, GameView gameView) {
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
		plusCorner = 5;
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

	class MultiThinking extends Thread {
		private Block[][] copyBoard;
		private int alpha = -200;
		private Block choiceBlock;
		private Block originBlock;
		private WinProb winProb;
		private MonteCarloSearch monteCarloSearch;

		public MultiThinking(Block[][] originBoard, Block originBlock, MonteCarloSearch monteCarloSearch) {
			this.copyBoard = board;
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
					copyBoard[i][j].setX(j);
					copyBoard[i][j].setY(i);
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
			// choiceBlock �ʱ�ȭ, emptyBlock
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
			DecimalFormat df = new DecimalFormat("0.##");
			gameView.appendText(" [ ��ϰ˻���.. (" + originBlock.getStrSave() + ")�� ��� ] Alpha : " + this.alpha + " , [MTC] : " + ( df.format(this.monteCarloSearch.getWinProb().getProbability()) ) + " % , avg : " + this.monteCarloSearch.getAlphaAvg());
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
			
			double prob = (monteCarloSearch.winProb.getProbability() - (double)50) / 10;
			//double res = (double)this.alpha + prob;
			double res = (double)this.alpha + (monteCarloSearch.getAlphaAvg());
			//System.out.println("[ "+originBlock.getPosValue()+" ] alpha : "+this.alpha + " , ����� : "+ monteCarloSearch.winProb.getProbability() + " alphaPlus : "+ (monteCarloSearch.getAlphaAvg()));
			return res;
		}
		
		public MonteCarloSearch getmonteCarloSearch() {
			return this.monteCarloSearch;
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
			
			gameView.appendText("���� �Ϸ�");
		} catch (Exception e) {
			System.out.println("���˽���");
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
		gameView.appendText("Ž������ : " + level);
		gameView.appendText("�г�Ƽ : " + timePenalty);
		gameView.appendText("�����ؾ��� ���� : " + impBlockList.size());
		Block choiceBlock = impBlockList.get(0);
		double maxScore = -200;
		
		Collections.shuffle(impBlockList); // ����

		thinkingList = new ArrayList<MultiThinking>();
		monteCarloList = new ArrayList<MonteCarloSearch>();
		try {
			for (Block b : impBlockList) {
				// TODO: ������
				MonteCarloSearch monte = new MonteCarloSearch(board, b, 15000 , this.cnt);
				MultiThinking multi = new MultiThinking(board, b, monte);
				thinkingList.add(multi);
				monteCarloList.add(monte);
				//multi.start();
				if(!isPerfectLearnning) monte.start();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		/* ����ī���η� �߿��� ���� �б�*/
		if(!isPerfectLearnning){
			for(MonteCarloSearch mont: monteCarloList){
				try {
					mont.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			monteCarloList.sort(new Comparator<MonteCarloSearch>() {
				@Override
				public int compare(MonteCarloSearch o1, MonteCarloSearch o2) {
					if( o1.getAlphaAvg() < o2.getAlphaAvg() ) {
						return 1;
					} else {
						return -1;
					}
				}
			});
			
			double avgSum = 0;
			for(MonteCarloSearch mont : monteCarloList){
				avgSum += mont.getAlphaAvg();
			}
			avgSum /= monteCarloList.size();
			
			for(MonteCarloSearch mont : monteCarloList){
				if(mont.getAlphaAvg() >= avgSum){
					mont.setEnable(true);
				}
			}
			
			thinkingList = thinkingList
					.stream()
					.filter(x -> x.getmonteCarloSearch().getEnable())
					.collect(Collectors.toList());
		}
		
		for(MultiThinking m : thinkingList){
			m.start();
		}

		gameView.appendText(" [ ���б���.. ]");
		int minRouteNum = 0;
		MultiThinking choiceM = thinkingList.get(0);
		for (MultiThinking m : thinkingList) {
			// cnt++;
			try {
				//System.out.println("[������ ��ٸ�����]");
				m.join();
				//if(!isPerfectLearnning)
				//	m.getmonteCarloSearch().join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			double winCnt = (int) m.getAlphaProb();
			probSum += m.getAlphaProb();
			Block currBlock = m.getOriginBlock();
			if (maxScore == winCnt) {
				Random r = new Random();
				//System.out.println("origin : " + originRouteCnt + " routeNum : " + m.choiceBlock.getRouteNum());
				if(r.nextBoolean()){
					choiceM = m;
					choiceBlock = currBlock;
					maxScore = winCnt;
					minRouteNum = m.getRouteNum();
					originRouteCnt = m.choiceBlock.getRouteNum();
					probability = (int) maxScore;
				} 
			} else if (maxScore < winCnt) {
				choiceM = m;
				choiceBlock = currBlock;
				maxScore = winCnt;
				minRouteNum = m.getRouteNum();
				originRouteCnt = m.choiceBlock.getRouteNum();
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
		gameView.appendText("AI��  " + choiceBlock.getStr() + "������ ������ �Ӵϴ�. ������� : " + df.format(calculCnt));
		gameView.appendText("[MonteCarlo Value] �·� : "+(choiceM.getmonteCarloSearch().getWinProb().getProbability())+" % ");
		gameView.appendText("[MonteCarlo Value] AI ����� : "+(choiceM.getmonteCarloSearch().getAlphaAvg()));
		
		// �ð����
		/*
		gameView.appendText("##NanoTime");
		gameView.appendText("[time1 : "+(this.time1/1000000)+".ms]");
		gameView.appendText("[time2 : "+(this.time2/1000000)+".ms]");
		gameView.appendText("[time3 : "+(this.time3/1000000)+".ms]");
		gameView.appendText("[time4 : "+(this.time4/1000000)+".ms]");
		gameView.appendText("[time5 : "+(this.time5/1000000)+".ms]");
		gameView.appendText("[time6 : "+(this.time6/1000000)+".ms]");
		*/
		return choiceBlock;
	}

	private int routeSearchABP2(int currPlayer, Block b, Block[][] copyBoard, List<Block> emptyBlockList, int cnt,
			boolean searchMax, Integer max, Integer min) {
		// TODO ABP1
		Long timeStart1 = System.nanoTime();
		List<Block> cloneEmptyBlockList = new ArrayList<Block>();
		cloneEmptyBlockList.addAll(emptyBlockList);
		cloneEmptyBlockList.remove(b);
		Long timeEnd1 = System.nanoTime();
		this.time1 += (timeEnd1 - timeStart1);

		Long timeStart2 = System.nanoTime();
		b.settingBoardVirtual(currPlayer);
		Long timeEnd2 = System.nanoTime();
		this.time2 += (timeEnd2 - timeStart2);
		
		if (cnt > deps || cnt > lastCnt) {
			int res = 0;
			if (isPerfectLearnning) {
				res = calculateScore(cnt, copyBoard);
			} else {
				res = calculateScoreOpenning(cnt, copyBoard, cloneEmptyBlockList);
			}
			Long timeStart3 = System.nanoTime();
			b.backUpLoad();
			Long timeEnd3 = System.nanoTime();
			this.time3 += (timeEnd3 - timeStart3);
			return res;
		}

		Long timeStart4 = System.nanoTime();
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
		Long timeEnd4 = System.nanoTime();
		this.time4 += (timeEnd4 - timeStart4);

		int nextPlayer = currPlayer;
		if (impBlockList.isEmpty()) {
			int res = calculateScore(cnt, copyBoard);
			Long timeStart3 = System.nanoTime();
			b.backUpLoad();
			Long timeEnd3 = System.nanoTime();
			this.time3 += (timeEnd3 - timeStart3);
			
			return res;
		}
		// TODO : �Ʒ��� for������ ������ ���

		int forSize = impBlockList.size();
		++cnt;
		
		//����
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

		// ó���� false
		Long timeStart3 = System.nanoTime();
		b.backUpLoad();
		Long timeEnd3 = System.nanoTime();
		this.time3 += (timeEnd3 - timeStart3);
		if (searchMax) {
			return max;
		} else {
			return min;
		}
	}
	
	private int calculateScore(int currCnt, Block[][] copyBoard) {
		// TODO:calculateScore
		Long timeStart5 = System.nanoTime();
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
		Long timeEnd5 = System.nanoTime();
		this.time5 += (timeEnd5 - timeStart5);
		return aiArea - enemyArea;
	}

	public int getProbability() {
		return probability;
		//System.out.println(" probSum : "+probSum +" , calculatedCnt : "+calculatedCnt+" , prob : "+(probSum / calculatedCnt));
		//return probSum / calculatedCnt;
	}

	private int calculateScoreOpenning(int currCnt, Block[][] copyBoard, List<Block> emptyBlockList) {
		// TODO:calculateScore
		Long timeStart6 = System.nanoTime();
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
		
		// TODO : ������ ���� �԰������� �������� ������  -> ���߿� ������ �ƴ� �������� �ٲ����
		for(int i = 0 ; i < scale ; i++) {
			
			for(int j = 0 ; j < scale ; j++ ) {
				Block currBoard = copyBoard[i][j];
				// TODO : ���̵��ϰ�� ���ھ� ���� �Ƚ�Ű�� ���� �ӽû���
				if(currBoard.getPlayer() == this.player) {
					if(currBoard.isCorner) {
						aiImpossible += plusCorner;
					}else if(isFixedBlock(copyBoard, i, j)){
						aiImpossible += plusFixedBlock;
					}else /*if(!currBoard.isSide)*/{
						aiImpossible --;
					}
				}else if(currBoard.getPlayer() == enemyPlayer){
					if(currBoard.isCorner) {
						enemyImpossible += plusCorner;
					}else if(isFixedBlock(copyBoard, i, j)){
						enemyImpossible += plusFixedBlock;
					}else /*if(!currBoard.isSide)*/{
						enemyImpossible --;
					}
				}
				
			}
			
		}
		Long timeEnd6 = System.nanoTime();
		this.time6 += (timeEnd6 - timeStart6);
		
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
		
		if(copyBoard[checkI][checkJ].isSide) {
			
			if(checkI == 0) {
				for(int j = 0 ; j < scale ; j++) {
					if(copyBoard[0][j].getPlayer() == 0) break;
					if(j == scale-1) return true;
				}
			} else if(checkI == scale-1) {
				for(int j = 0 ; j < scale ; j++) {
					if(copyBoard[scale-1][j].getPlayer() == 0) break;
					if(j == scale-1) return true;
				}
			} else if(checkJ == 0) {
				for(int i = 0 ; i < scale ; i++) {
					if(copyBoard[i][0].getPlayer() == 0) break;
					if(i == scale-1) return true;
				}
			} else if(checkJ == scale-1) {
				for(int i = 0 ; i < scale ; i++) {
					if(copyBoard[i][scale-1].getPlayer() == 0) break;
					if(i == scale-1) return true;
				}
			}
			
		}
		
		
		return false;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	class MonteCarloSearch extends Thread {
		private Block[][] copyBoard;
		private int alpha = -200;
		private Block choiceBlock;
		private Block originBlock;
		private WinProb winProb;
		private int maxPlay;
		private int playCnt;
		private int cnt;
		private long alphaSum;
		private boolean enable;
		
		public boolean getEnable(){
			return this.enable;
		}
		
		public void setEnable(boolean enable){
			this.enable = enable;
		}

		public MonteCarloSearch(Block[][] originBoard, Block originBlock, int maxPlay, int cnt) {
			this.copyBoard = board;
			this.originBlock = originBlock;
			this.winProb = new WinProb();
			this.maxPlay = maxPlay;
			this.cnt = cnt;
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
			//System.out.println("monte Start");
			
			try {
			// choiceBlock �ʱ�ȭ, emptyBlock
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
				int alpha = routeSearchMontecarlo(player, choiceBlock, copyBoard, emptyBlockList, cnt + 1, false);
				winProb.plusJudgement();
				this.alphaSum += alpha;
				if(alpha > 0) {
					winProb.plusWin();
				}
			}
			//System.out.println(originBlock.getPosValue()+" playCnt : "+this.playCnt);

			synchronized (this) {
				if (rootMax == null || rootMax < this.alpha)
					rootMax = this.alpha;
				
				calculatedCnt++;
			}
			
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		
		// �ӽ� ����ī���ν�
		
		private int routeSearchMontecarlo(int currPlayer, Block b, Block[][] copyBoard, List<Block> emptyBlockList, int cnt,
				boolean searchMax/*, Integer max, Integer min*/) {
			// TODO ABP1
			this.playCnt++; 
			//System.out.println("monte Search Cnt : "+(cnt));
			
			List<Block> cloneEmptyBlockList = new ArrayList<Block>();
			cloneEmptyBlockList.addAll(emptyBlockList);
			cloneEmptyBlockList.remove(b);

			b.settingBoardVirtual(currPlayer);

			if(this.cnt > 38) {
				if(cnt > (scale * scale - 4)) {
					int res = 0;
					res = calculateScore(cnt, copyBoard);
					//res = calculateScoreOpenning(currCnt, copyBoard, cloneEmptyBlockList)(cnt, copyBoard);
					b.backUpLoad();
					return res;
				}
			}else {
			
				if (/*cnt > lastCnt*/ cnt > this.cnt + 12 || cnt > lastCnt) {
					int res = 0;
					res = calculateScoreOpenning(cnt, copyBoard, cloneEmptyBlockList);
					//res = calculateScoreOpenning(currCnt, copyBoard, cloneEmptyBlockList)(cnt, copyBoard);
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
				//int res = calculateScore(cnt, copyBoard);
				b.backUpLoad();
				return res;
			}
			// TODO : �Ʒ��� for������ ������ ���

			++cnt;
			//����
			Random random = new Random();
			Block nextChoice = impBlockList.get(random.nextInt(impBlockList.size()));
			int tempWinCnt = routeSearchMontecarlo(nextPlayer, nextChoice, copyBoard, cloneEmptyBlockList, cnt,
					!searchMax);
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
	
	public Map<String, Object> getDataMap(){
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("time1", this.time1);
		dataMap.put("time2", this.time2);
		dataMap.put("time3", this.time3);
		dataMap.put("time4", this.time4);
		dataMap.put("time5", this.time5);
		dataMap.put("time6", this.time6);
		return dataMap;
	}

}
