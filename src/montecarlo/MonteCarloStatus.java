package montecarlo;

import game.Block;
import util.Globals;
import util.WinProb;

public class MonteCarloStatus {
	private Block[][] copyBoard;
	private Block choiceBlock;
	private Block originBlock;
	private int cnt;
	private int currPlayer;
	private int ne_int ;
	private int sw_int ;
	private int se_int ;
	private Integer max;
	private Integer min;
	private int res;
	
	public MonteCarloStatus(Block[][] originBoard, Block originBlock, int cnt, int res, int currPlayer) {
		this.ne_int = Globals.scale - 1;
		this.sw_int = (Globals.scale * (Globals.scale - 1));
		this.se_int = (Globals.scale * Globals.scale) - 1;
		this.cnt = cnt;
		this.res = res;
		init(originBoard, originBlock, cnt, currPlayer);
	}

	public void init(Block[][] originBoard, Block originBlock, int cnt , int currPlayer) {
		this.originBlock = originBlock;
		this.cnt = cnt;
		this.currPlayer = currPlayer;
		copyBoard = new Block[Globals.scale][Globals.scale];
		for (int i = 0; i < Globals.scale; i++) {
			for (int j = 0; j < Globals.scale; j++) {
				copyBoard[i][j] = new Block();
				copyBoard[i][j].setStr((Globals.scale * i) + j + "");
				copyBoard[i][j].saveStr();
				copyBoard[i][j].setPlayer(originBoard[i][j].getPlayer());
				if (copyBoard[i][j].getStrSave().equals(originBlock.getStrSave())) {
					this.choiceBlock = copyBoard[i][j];
				}

				int posValue = copyBoard[i][j].getPosValue();
				if (posValue == 0 || posValue == ne_int || posValue == sw_int || posValue == se_int) {
					copyBoard[i][j].isCorner = true;
				} else {
					if (posValue % Globals.scale == 0 || posValue % Globals.scale == 7 || posValue < Globals.scale
							|| posValue >= (Globals.scale * Globals.scale - Globals.scale)) {
						copyBoard[i][j].isSide = true;
					}
				}
			}
		}
	}

	
	public Block getOriginBlock() {
		return originBlock;
	}

	public int getCnt() {
		return cnt;
	}

	public void setCnt(int cnt) {
		this.cnt = cnt;
	}

	public int getCurrPlayer() {
		return currPlayer;
	}

	public void setCurrPlayer(int currPlayer) {
		this.currPlayer = currPlayer;
	}

	public Block[][] getCopyBoard() {
		return copyBoard;
	}

	public Block getChoiceBlock() {
		return choiceBlock;
	}

	public Integer getMax() {
		return max;
	}

	public void setMax(Integer max) {
		this.max = max;
	}

	public Integer getMin() {
		return min;
	}

	public void setMin(Integer min) {
		this.min = min;
	}

	public int getRes() {
		return res;
	}

	public void setRes(int res) {
		this.res = res;
	}
	
}


