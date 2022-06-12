package game;

import java.util.ArrayList;
import java.util.List;

import util.BlockCalculator;

public class Block {
	private String str;
	private String strSave;
	private int posValue;
	private int player;
	private int playerSave;
	private int x;
	private int y;
	
	private Block north_west;
	private Block north;
	private Block north_east;
	private Block west;
	private Block east;
	private Block south_west;
	private Block south;
	private Block south_east;
	private long winPro;
	private int routeNum;
	private int eatNum = -1;
	public boolean isCorner;
	public boolean isSide;
	public boolean isDanger;
	
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	//���� ArrayList������ �߰� ������ ���������� LinkedList�� �ٲ�
	private List<Block> backUpList = new ArrayList<Block>();
	
	public int getBackUpListSize(){
		return backUpList.size();
	}
	public int getPosValue() {
		return posValue;
	}
	public void setPosValue(int posValue) {
		this.posValue = posValue;
	}
	public int getEatNum() {
		return eatNum;
	}
	public void setEatNum(int eatNum) {
		this.eatNum = eatNum;
	}
	public int getRouteNum() {
		return routeNum;
	}
	public void setRouteNum(int routeNum) {
		this.routeNum = routeNum;
	}
	
	public String getStr() {
		return str;
	}

	public void setStr(String str) {
		this.str = str;
	}

	public int getPlayer() {
		return player;
	}

	public void setPlayer(int player) {
		this.player = player;
		//�ֿܼ� view �ʿ����
		//str = player == 1 ? "O" : "X";
	}

	public Block getNorth_west() {
		return north_west;
	}

	public void setNorth_west(Block north_west) {
		this.north_west = north_west;
	}

	public Block getNorth() {
		return north;
	}

	public void setNorth(Block north) {
		this.north = north;
	}

	public Block getNorth_east() {
		return north_east;
	}

	public void setNorth_east(Block north_east) {
		this.north_east = north_east;
	}

	public Block getWest() {
		return west;
	}

	public void setWest(Block west) {
		this.west = west;
	}

	public Block getEast() {
		return east;
	}

	public void setEast(Block east) {
		this.east = east;
	}

	public Block getSouth_west() {
		return south_west;
	}

	public void setSouth_west(Block south_west) {
		this.south_west = south_west;
	}

	public Block getSouth() {
		return south;
	}

	public void setSouth(Block south) {
		this.south = south;
	}

	public Block getSouth_east() {
		return south_east;
	}

	public void setSouth_east(Block south_east) {
		this.south_east = south_east;
	}

	public long getWinPro() {
		return winPro;
	}

	public void setWinPro(long winPro) {
		this.winPro = winPro;
	}
	
	public void winPlus(){
		winPro++;
	}
	
	public void winReset(){
		winPro = 0;
	}
	
	public String getStrSave() {
		return strSave;
	}
	public void saveStr() {
		this.strSave = this.str;
		this.posValue = Integer.parseInt(this.strSave);
	}
	public void loadStr(){
		this.str = strSave;
	}
	public int getPlayerSave() {
		return playerSave;
	}
	public void savePlayer() {
		this.playerSave = this.player;
	}
	public void loadPlayer(){
		this.player = playerSave;
	}
	
	public void backUpLoad(){
		int backUpPlayer = player == 1 ? 2 : 1;
		for(Block b : backUpList){
			b.player = backUpPlayer;
		}
		backUpList.clear();
		player = 0;
	}
	/* �����ڵ�
	public void backUpLoad(){
		int backUpPlayer = player == 1 ? 2 : 1;
		for(Block b : backUpList){
			b.player = backUpPlayer;
		}
		backUpList.clear();
		//this.setPlayer(0);
		player = 0;
	}*/
	
	public boolean isNeigher(Block other){
		if(north != null && north == other){
			return true;
		}
		if(west != null && west == other){
			return true;
		}
		if(east != null && east == other){
			return true;
		}
		if(south != null && south == other){
			return true;
		}
		return false;
	}
	private boolean eastSafe(int player,int enemy){
		if(east == null){
			return true;
		}
		
		if(east.getPlayer() == player){
			return east.eastSafe(player, enemy);
		}else if(east.getPlayer() == enemy){
			return true;
		}else{
			return false;
		}
	}
	private boolean westSafe(int player,int enemy){
		if(west == null){
			return true;
		}

		if(west.getPlayer() == player){
			return west.westSafe(player, enemy);
		}else if(west.getPlayer() == enemy){
			return true;
		}else{
			return false;
		}
	}
	private boolean northSafe(int player,int enemy){
		if(north == null){
			return true;
		}
		
		if(north.getPlayer() == player){
			return north.northSafe(player, enemy);
		}else if(north.getPlayer() == enemy){
			return true;
		}else{
			return false;
		}
	}
	private boolean southSafe(int player,int enemy){
		if(south == null){
			return true;
		}

		if(south.getPlayer() == player){
			return south.southSafe(player, enemy);
		}else if(south.getPlayer() == enemy){
			return true;
		}else{
			return false;
		}
	}
	
	public boolean isDanger(int player,int scale){
		int enemy = player == 1 ? 2 : 1;
		
		if ( posValue < scale || posValue >= (scale * scale- scale)) {
			this.settingBoardVirtual(player);
			if(west != null && west.getPlayer() == enemy){
				if(!eastSafe(player, enemy)){
					if(west.getPlayer() == enemy){
						backUpLoad();
						return true;
					}
				}
			}
			if(east != null && east.getPlayer() == enemy){
				if(!westSafe(player, enemy)){
					this.settingBoardVirtual(player);
					if(east.getPlayer() == enemy){
						backUpLoad();
						return true;
					}
				}
			}
			backUpLoad();
		}
		
		if ( posValue % scale == 0 || posValue % scale == 7 ) {
			this.settingBoardVirtual(player);
			if(north != null && north.getPlayer() == enemy){
				if(!southSafe(player, enemy)){
					this.settingBoardVirtual(player);
					if(north.getPlayer() == enemy){
						backUpLoad();
						return true;
					}
				}
			}
			if(south != null && south.getPlayer() == enemy){
				if(!northSafe(player, enemy)){
					this.settingBoardVirtual(player);
					if(south.getPlayer() == enemy){
						backUpLoad();
						return true;
					}
				}
			}
			backUpLoad();
		}
		return false;
	}
	
	public boolean isDanger2(int player,int scale){
		int enemy = player == 1 ? 2 : 1;
		int posValue = Integer.parseInt(this.strSave);
		
		if ( posValue < scale || posValue >= (scale * scale- scale)) {
			if(west != null && west.getPlayer() == enemy){
				if(!eastSafe(player, enemy)){
					this.settingBoardVirtual(player);
					if(west.getPlayer() == enemy){
						backUpLoad();
						return true;
					}
					backUpLoad();
				}
			}
			if(east != null && east.getPlayer() == enemy){
				if(!westSafe(player, enemy)){
					this.settingBoardVirtual(player);
					if(east.getPlayer() == enemy){
						backUpLoad();
						return true;
					}
					backUpLoad();
				}
			}
		}
		
		if ( posValue % scale == 0 || posValue % scale == 7 ) {
			if(north != null && north.getPlayer() == enemy){
				if(!southSafe(player, enemy)){
					this.settingBoardVirtual(player);
					if(north.getPlayer() == enemy){
						backUpLoad();
						return true;
					}
					backUpLoad();
				}
			}
			if(south != null && south.getPlayer() == enemy){
				if(!northSafe(player, enemy)){
					this.settingBoardVirtual(player);
					if(south.getPlayer() == enemy){
						backUpLoad();
						return true;
					}
					backUpLoad();
				}
			}
		}
		return false;
	}

	public boolean checkNorthWest(int player, int cnt, boolean realMode, List<Block> backUpList) {
		if((cnt >= 1 && this.player == 0) || (cnt == 1 && this.player == player)) return false;
		if (cnt > 1 && this.player == player) return true;
		if (north_west != null && north_west.checkNorthWest(player, cnt + 1, realMode, backUpList)) {
			if(realMode) {
				if(backUpList != null && cnt > 0)
					backUpList.add(this);
				north_west.setPlayer(player);
			}
			return true;
		}
		return false;
	}

	public boolean checkNorth(int player, int cnt, boolean realMode, List<Block> backUpList) {
		if((cnt >= 1 && this.player == 0) || (cnt == 1 && this.player == player)) return false;
		if (cnt > 1 && this.player == player) return true;
		if (north != null && north.checkNorth(player, cnt + 1, realMode, backUpList)) {
			if(realMode) {
				if(backUpList != null && cnt > 0)
					backUpList.add(this);
				north.setPlayer(player);
			}
			return true;	
		}
		return false;
	}

	public boolean checkNorthEast(int player, int cnt, boolean realMode, List<Block> backUpList) {
		if((cnt >= 1 && this.player == 0) || (cnt == 1 && this.player == player)) return false;
		if (cnt > 1 && this.player == player) return true;
		if (north_east != null && north_east.checkNorthEast(player, cnt + 1, realMode, backUpList)) {
			if(realMode) {
				if(backUpList != null && cnt > 0)
					backUpList.add(this);
				north_east.setPlayer(player);
			}
			return true;
		}
		return false;
	}

	public boolean checkWest(int player, int cnt, boolean realMode, List<Block> backUpList) {
		if((cnt >= 1 && this.player == 0) || (cnt == 1 && this.player == player)) return false;
		if (cnt > 1 && this.player == player) return true;
		if (west != null && west.checkWest(player, cnt + 1, realMode, backUpList)) {
			if(realMode){
				if(backUpList != null && cnt > 0)
					backUpList.add(this);
				west.setPlayer(player);
			}
			return true;	
		}
		return false;
	}

	public boolean checkEast(int player, int cnt, boolean realMode, List<Block> backUpList) {
		if((cnt >= 1 && this.player == 0) || (cnt == 1 && this.player == player)) return false;
		if (cnt > 1 && this.player == player) return true;
		if (east != null && east.checkEast(player, cnt + 1, realMode, backUpList)) {
			if(realMode){
				if(backUpList != null && cnt > 0)
					backUpList.add(this);
				east.setPlayer(player);
			}
			return true;
		}
		return false;
	}

	
	public boolean checkSouthWest(int player, int cnt, boolean realMode, List<Block> backUpList) {
		if((cnt >= 1 && this.player == 0) || (cnt == 1 && this.player == player)) return false;
		if (cnt > 1 && this.player == player) return true;
		if (south_west != null && south_west.checkSouthWest(player, cnt + 1, realMode, backUpList)) {
			if(realMode){
				if(backUpList != null && cnt > 0)
					backUpList.add(this);
				south_west.setPlayer(player);
			}
			return true;
		}
		return false;
	}
	
	public boolean checkSouth(int player, int cnt, boolean realMode, List<Block> backUpList) {
		if((cnt >= 1 && this.player == 0) || (cnt == 1 && this.player == player)) return false;
		if (cnt > 1 && this.player == player) return true;
		if (south != null && south.checkSouth(player, cnt + 1, realMode, backUpList)) {
			if(realMode){
				if(backUpList != null && cnt > 0)
					backUpList.add(this);
				south.setPlayer(player);
			}
			return true;
		}
		return false;
	}

	public boolean checkSouthEast(int player, int cnt, boolean realMode, List<Block> backUpList) {
		if((cnt >= 1 && this.player == 0) || (cnt == 1 && this.player == player)) return false;
		if (cnt > 1 && this.player == player) return true;
		if (south_east != null && south_east.checkSouthEast(player, cnt + 1,realMode, backUpList)) {
			if(realMode){ 
				if(backUpList != null && cnt > 0)
					backUpList.add(this);
				south_east.setPlayer(player);
			}
			return true;
		}
		return false;
	}
	public boolean impossible(int currPlayer){
		return BlockCalculator.isImpossible(this, currPlayer);
		/*
		if(checkNorthWest(currPlayer, 0, false, null)||
		checkNorth(currPlayer, 0, false, null)||
		checkNorthEast(currPlayer, 0, false, null)||
		checkWest(currPlayer, 0, false, null)||
		checkEast(currPlayer, 0, false, null)||
		checkSouthWest(currPlayer, 0, false, null)||
		checkSouth(currPlayer, 0, false, null)||
		checkSouthEast(currPlayer, 0, false, null)){
			return true;
		}
		return false;
		*/
	}
	
	public void settingBoard(int currPlayer) {
		setPlayer(currPlayer);
		checkNorthWest(currPlayer, 0, true, null);
		checkNorth(currPlayer, 0, true, null);
		checkNorthEast(currPlayer, 0, true, null);
		checkWest(currPlayer, 0, true, null);
		checkEast(currPlayer, 0, true, null);
		checkSouthWest(currPlayer, 0, true, null);
		checkSouth(currPlayer, 0, true, null);
		checkSouthEast(currPlayer, 0, true, null);
	}
	public void settingBoardVirtual(int currPlayer) {
		setPlayer(currPlayer);
		checkNorthWest(currPlayer, 0, true, this.backUpList);
		checkNorth(currPlayer, 0, true, this.backUpList);
		checkNorthEast(currPlayer, 0, true, this.backUpList);
		checkWest(currPlayer, 0, true, this.backUpList);
		checkEast(currPlayer, 0, true, this.backUpList);
		checkSouthWest(currPlayer, 0, true, this.backUpList);
		checkSouth(currPlayer, 0, true, this.backUpList);
		checkSouthEast(currPlayer, 0, true, this.backUpList);
	}
	
	@Override
	public String toString() {
		return "Block [str=" + str + ", player=" + player + "]";
	}
	public boolean isFixedBlock() {
		// TODO Auto-generated method stub
		return false;
	}
}
