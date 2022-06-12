package ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import game.Block;

public class HermesAI implements OthelloAI{
	private int player;
	private Block[][] board;
	private int cnt;
	private int lastCnt;
	private int calculCnt;
	private int endCnt;
	
	private long indivEndCnt;
	private int timePenalty;
	private double maxWinPro;
	private int level;
	
	private int originRouteCont = 100;
	private int routeCount = 0;
	
	public HermesAI(int player, Block[][] board, int cnt, int lastCnt,int level) {
		this.player = player;
		this.board = board;
		this.cnt = cnt;
		this.lastCnt = lastCnt;
		this.level = level;
	}

	public Block choice() {
		List<Block> emptyBlockList = new ArrayList<Block>();
		Block choiceBlock = null;
		for(int i = 0 ; i < board.length ; i++){
			for(int j = 0 ; j < board[0].length ; j++){
				Block testBlock = board[i][j];
				if(choiceBlock == null) choiceBlock = testBlock;
				if(testBlock.getPlayer() == 0){
					emptyBlockList.add(testBlock);
				}
			}
		}
		
		choiceBlock = think(emptyBlockList);
		
		return choiceBlock;
	}

	public Block think(List<Block> emptyBlockList) {
		List<Block> cloneList = new ArrayList<Block>();
		cloneList.addAll(emptyBlockList);
		String ne = (board.length-1)+"";
		String sw = ((board.length*board.length)-board.length)+"";
		String se = ((board.length*board.length)-1)+"";
		List<Block> impBlockList = new ArrayList<Block>();
		int enemy = player == 1 ? 2 : 1;
		for(Block b : cloneList){
			if(b.impossible(player)){
				impBlockList.add(b);
				if(b.getStrSave().equals("0") || b.getStrSave().equals(ne) ||
						b.getStrSave().equals(sw) || b.getStrSave().equals(se)){
					return b;
				}
			}
		}
		if(player == 1){
			if(timePenalty%2 != 0){
				timePenalty++;
			}
		}else{
			if(timePenalty%2 == 0){
				timePenalty++;
			}
		}
		String second_nw = (board.length+1)+"";
		String second_ne = (board.length*2-2)+"";
		String second_sw = (((board.length-2)*board.length)+1)+"";
		String second_se = ((board.length*(board.length-1))-2)+"";
		
		int plusdeps = 0;
		if(this.cnt >= 40){
			plusdeps = 2;
			level += plusdeps;
		}else if(this.cnt >= 45){
			plusdeps = 4;
			level += plusdeps;
		}
		
		System.out.println("plusLevel : "+plusdeps);
		System.out.println("패널티 : "+timePenalty);
		System.out.println("생각해야할 변수 : "+impBlockList.size());
		Block choiceBlock = impBlockList.get(0);
		int cnt = 0;
		int maxScore = -1000;
		for(Block b : impBlockList){
			cnt++;
			System.out.print(cnt+") [ 블록검사중.. ("+b.getStrSave()+")번 블록 ]");
			int originPlayer = b.getPlayer();
			/*for(int x = 0 ; x < board.length ; x++){
				for(int j = 0 ; j < board[0].length ; j++){
					System.out.print(board[x][j].getPlayer()+" ");
				}
				System.out.println();
			}
			System.out.println();
			JOptionPane.showMessageDialog(null, "시행전");*/
			//long winCnt = routeSearch(player,player,b,cloneList,this.cnt+1);
			int winCnt = 0;
			//TODO: 갈림돌
			//if(player == 2){
			//	winCnt = routeSearchABP2(player,player,b,cloneList,this.cnt+1,false,null,max);
			//}else{
			if(level > 5)
				winCnt = routeSearchABP(player,player,b,cloneList,this.cnt+1,false,maxScore);
			else
				winCnt = routeSearch(player, player, b, cloneList, this.cnt);
			//}
			if(this.cnt+level < (board.length*board.length-4) && level > 5){
				if(b.getStrSave().equals(second_nw) && board[0][0].getPlayer()==0 || 
						b.getStrSave().equals(second_ne) && board[0][board.length-1].getPlayer()==0 || 
						b.getStrSave().equals(second_sw) && board[board.length-1][0].getPlayer()==0 || 
						b.getStrSave().equals(second_se) && board[board.length-1][board.length-1].getPlayer()==0){
					winCnt -= ((((board.length*board.length-4)-this.cnt)/(board.length/2))+1);
				}
			}
				
			b.setPlayer(originPlayer);
			b.setWinPro(winCnt);
			System.out.println(" [max :"+winCnt+"]");
			if(maxScore == winCnt){
				Random r = new Random();
				if(originRouteCont > routeCount){
					max = winCnt;
					choiceBlock = b;
					maxScore = winCnt;
					originRouteCont = routeCount;
				}else if(originRouteCont == routeCount && r.nextBoolean()){
					max = winCnt;
					choiceBlock = b;
					maxScore = winCnt;
					originRouteCont = routeCount;
				}
			}else if(maxScore < winCnt){
				max = winCnt;
				choiceBlock = b;
				maxScore = winCnt;
				originRouteCont = routeCount;
			}
			indivEndCnt = 0;
		}
		for(int i = 0 ; i < board.length ; i++){
			for(int j = 0 ; j < board[0].length ; j++){
				if(board[i][j].getPlayer() == 0 )
					board[i][j].setStr((i*board.length)+j+"");
				board[i][j].setWinPro(0);
			}
		}
		System.out.println("총 검토한 수순 : "+calculCnt+", 마지막 도달한 수순 : "+endCnt);
		System.out.println("Hermes가 승률이 높은 "+choiceBlock.getStr()+"지점에 착수를 둡니다.");
		return choiceBlock;
	}
	
	
	private Integer min;
	private Integer max;
	
	public int routeSearchABP2(int aiPlayer, int currPlayer, Block b, List<Block> emptyBlockList,
			int cnt,boolean searchMax,Integer min,Integer max){
		//TODO ABP2
		//System.out.println("aiplay : "+aiPlayer+", this.player : "+this.player+", currPlayer : "+currPlayer);
		calculCnt++;
		if(cnt >= this.cnt+level-timePenalty || cnt >= lastCnt){
			endCnt++;
			indivEndCnt++;
			//System.out.println("min : "+min+", max : "+max);
			return calculateScore(cnt);
		}
		
		List<Block> cloneEmptyBlockList = new ArrayList<Block>();
		cloneEmptyBlockList.addAll(emptyBlockList);
		cloneEmptyBlockList.remove(b);
		
		b.settingBoardVirtual(currPlayer);
		/*/////////////
		for(int x = 0 ; x < board.length ; x++){
			for(int j = 0 ; j < board[0].length ; j++){
				System.out.print(board[x][j].getPlayer()+" ");
			}
			System.out.println();
		}
		System.out.println();
		JOptionPane.showMessageDialog(null, "착수");
		*/////////////
		currPlayer = currPlayer == 1 ? 2 : 1;
		List<Block> impBlockList = new ArrayList<Block>();
		for(Block impBlock : cloneEmptyBlockList){
			if(impBlock.impossible(currPlayer)){
				impBlockList.add(impBlock);
			}
		}
		
		if(impBlockList.isEmpty()){
			currPlayer = currPlayer == 1 ? 2 : 1;
			searchMax = !searchMax;
			for(Block impBlock : cloneEmptyBlockList){
				if(impBlock.impossible(currPlayer)){
					impBlockList.add(impBlock);
				}
			}
		}
		
		Integer ab = null;
		int nextPlayer = currPlayer;
		if(impBlockList.isEmpty()){
			endCnt++;
			indivEndCnt++;
			b.backUpLoad();
			//System.out.println("min : "+min+", max : "+max);
			return calculateScore(cnt);
		}
		
		for(int i = 0 ; i < impBlockList.size() ; i++){
			int tempWinCnt = 0;
			if(searchMax){
				tempWinCnt = routeSearchABP2(aiPlayer, nextPlayer, impBlockList.get(i), cloneEmptyBlockList,cnt+1,!searchMax,ab,max);
			}else{
				tempWinCnt = routeSearchABP2(aiPlayer, nextPlayer, impBlockList.get(i), cloneEmptyBlockList,cnt+1,!searchMax,min,ab);
			}
			
			if(ab == null){
				ab = tempWinCnt;
				continue;
			}
			
			if(searchMax){
				//true
				if( ab < tempWinCnt ){
					ab = tempWinCnt;
				}
				if( min != null && tempWinCnt >= min )
					break;
				if( max != null && max < ab)
					max = ab;
			}else{
				if( ab > tempWinCnt ){
					ab = tempWinCnt;
				}
				if( max != null && tempWinCnt <= max )
					break;
				if( min != null && min > ab)
					min = ab;
			}
		}
		
		b.backUpLoad();
		return ab;
	}
	
	
	public int routeSearchABP(int aiPlayer, int currPlayer, Block b, List<Block> emptyBlockList,int cnt,boolean searchMax,Integer minmax){
		//TODO ABP1
		calculCnt++;
		if(cnt >= this.cnt+level-timePenalty || cnt >= lastCnt){
			endCnt++;
			indivEndCnt++;
				return calculateScore(cnt);
		}
		
		List<Block> cloneEmptyBlockList = new ArrayList<Block>();
		cloneEmptyBlockList.addAll(emptyBlockList);
		cloneEmptyBlockList.remove(b);
		
		b.settingBoardVirtual(currPlayer);
		
		
		currPlayer = currPlayer == 1 ? 2 : 1;
		List<Block> impBlockList = new ArrayList<Block>();
		for(Block impBlock : cloneEmptyBlockList){
			if(impBlock.impossible(currPlayer)){
				impBlockList.add(impBlock);
			}
		}
		
		
		if(impBlockList.isEmpty()){
			currPlayer = currPlayer == 1 ? 2 : 1;
			searchMax = !searchMax;
			for(Block impBlock : cloneEmptyBlockList){
				if(impBlock.impossible(currPlayer)){
					impBlockList.add(impBlock);
				}
			}
		}
		
		Integer ab = null;
		int nextPlayer = currPlayer;
		if(impBlockList.isEmpty()){
			endCnt++;
			indivEndCnt++;
			b.backUpLoad();
				return calculateScore(cnt);
		}
		//JOptionPane.showMessageDialog(null, searchMax);
		
		for(int i = 0 ; i < impBlockList.size() ; i++){
			int tempWinCnt = routeSearchABP(aiPlayer, nextPlayer, impBlockList.get(i), cloneEmptyBlockList,cnt+1,!searchMax,ab);
			
			if(ab == null){
				ab = tempWinCnt;
				//continue;
			}
			if(searchMax){
				if(minmax != null && tempWinCnt >= minmax)
					break;
				if(ab < tempWinCnt )
					ab = tempWinCnt;
			}else{
				if(minmax != null && tempWinCnt <= minmax)
					break;
				if(ab > tempWinCnt )
					ab = tempWinCnt;
			}
		}
		b.backUpLoad();
/*		for(int x = 0 ; x < board.length ; x++){
			for(int j = 0 ; j < board[0].length ; j++){
				System.out.print(board[x][j].getPlayer()+" ");
			}
			System.out.println();
		}
		System.out.println();
		JOptionPane.showMessageDialog(null, "복기");*/
		if(level > 5 && cnt - this.cnt == 1 && this.player != currPlayer && this.cnt+level < (board.length*board.length-4) ){
			String ne = (board.length-1)+"";
			String sw = ((board.length*board.length)-board.length)+"";
			String se = ((board.length*board.length)-1)+"";
			routeCount = impBlockList.size();
			for(Block impBlock : impBlockList){
				String num = impBlock.getStrSave();
				System.out.print("("+num+")");
				if(num.equals("0") || num.equals(ne) || num.equals(sw) || num.equals(se) ){
					int origin = ab;
					ab -= ((((board.length*board.length-4)-this.cnt)/(board.length/2))+1);
					System.out.println("위험감지 ! origin : "+origin+" , 감소 : "+ab);
				}
			}
		}
		return ab;
	}
	
	
	
	private int calculateScore(int currCnt) {
		//TODO:calculateScore
		int aiArea = 0;
		int enemyArea = 0;
		int enemyPlayer = this.player == 1 ? 2 : 1;
		int ne = board.length-1;
		int sw = (board.length*(board.length-1));
		int se = (board.length*board.length)-1;
		int scale = board.length;
		
		
		int plusCorner =(((scale*scale-4)-currCnt)/(scale/2))+1;
		int plusSide = 1;
		int plusNormal = this.cnt/((scale*scale/2));
		//JOptionPane.showMessageDialog(null, second_nw+","+second_ne+","+second_sw+","+second_se+"("+second_score+")");
		//JOptionPane.showMessageDialog(null, ((board.length*board.length)-4)-currCnt);
		for(int i = 0 ; i < board.length ; i++){
			for(int j = 0 ; j < board[0].length ; j++){
				int posValue = (i*scale)+j;
				if(board[i][j].getPlayer() == this.player){
					
					if(posValue == 0 || posValue == ne || posValue == sw || posValue == se ){
						aiArea+= plusCorner;
					}
					else{
						if(scale%(posValue) == 0 || scale%(posValue) == 7 || posValue < scale || posValue >= (scale*scale-scale)){
							aiArea+= plusSide;
						}else if(currCnt > (scale*scale-4)-10){
							aiArea+= plusNormal;
						}
					}
				}else if(board[i][j].getPlayer() == enemyPlayer){
					if(posValue == 0 || posValue == ne || posValue == sw || posValue == se ){
						enemyArea+= (((scale*scale-4)-currCnt)/scale)+1;
					}
					else{
						if(scale%(posValue) == 0 || scale%(posValue) == 7){
							enemyArea+= plusSide;
						}else if(currCnt > (scale*scale-4)-10){
							enemyArea+= plusNormal;
						}
					}
				}
			}
		}
		return aiArea-enemyArea;
	}

	//확률AI
	private int routeSearch(int aiPlayer, int currPlayer, Block b, List<Block> emptyBlockList,int cnt) {
	    //System.out.println(timePenalty+","+(this.cnt+level-timePenalty)+","+cnt);
		
		calculCnt++;
		if(cnt >= this.cnt+level-timePenalty || cnt >= lastCnt){
			endCnt++;
			indivEndCnt++;
			int aiArea = 0;
			int enemyArea = 0;
			int enemyPlayer = aiPlayer == 1 ? 2 : 1;
			for(int i = 0 ; i < board.length ; i++){
				for(int j = 0 ; j < board[0].length ; j++){
					if(board[i][j].getPlayer() == aiPlayer){
						aiArea++;
					}else if(board[i][j].getPlayer() == enemyPlayer){
						enemyArea++;
					}
				}
			}
			//System.out.println(aiArea+" : "+enemyArea);
			if(aiArea >= enemyArea){
				//System.out.println("최종 승!!");
				return 1;
			}
			else return 0;
		}
		
		List<Block> cloneEmptyBlockList = new ArrayList<Block>();
		cloneEmptyBlockList.addAll(emptyBlockList);
		cloneEmptyBlockList.remove(b);
		
		b.settingBoardVirtual(currPlayer);
		/*for(int x = 0 ; x < board.length ; x++){
			for(int j = 0 ; j < board[0].length ; j++){
				System.out.print(board[x][j].getPlayer()+" ");
			}
			System.out.println();
		}
		System.out.println();
		JOptionPane.showMessageDialog(null, "착수");*/
		currPlayer = currPlayer == 1 ? 2 : 1;
		List<Block> impBlockList = new ArrayList<Block>();
		for(Block impBlock : cloneEmptyBlockList){
			if(impBlock.impossible(currPlayer)){
				impBlockList.add(impBlock);
			}
		}
		
		if(impBlockList.isEmpty()){
			currPlayer = currPlayer == 1 ? 2 : 1;
			for(Block impBlock : cloneEmptyBlockList){
				if(impBlock.impossible(currPlayer)){
					impBlockList.add(impBlock);
				}
			}
		}
		
		int winCnt = 0;
		int nextPlayer = currPlayer;
		int plusValue = ((cnt - this.cnt)/5)+1;
		if((cnt - this.cnt) > 5){
			plusValue += (cnt - this.cnt - 5);
		}
		for(int i = 0 ; i < impBlockList.size() ; i+=plusValue){
			winCnt += routeSearch(aiPlayer, nextPlayer, impBlockList.get(i), cloneEmptyBlockList,cnt+1);
		}
		b.backUpLoad();
		/*for(int x = 0 ; x < board.length ; x++){
			for(int j = 0 ; j < board[0].length ; j++){
				System.out.print(board[x][j].getPlayer()+" ");
			}
			System.out.println();
		}
		System.out.println();
		JOptionPane.showMessageDialog(null, "복기");*/
		return winCnt;
	}
	@Override
	public int getProbability() {
		return 0;
	}

	@Override
	public void stopThinking() {
		// TODO Auto-generated method stub
		
	}
}
