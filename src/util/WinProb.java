package util;

public class WinProb {
	private int judgement;
	private int win;
	
	public int getJudgement() {
		return judgement;
	}
	public void setJudgement(int judgement) {
		this.judgement = judgement;
	}
	public int getWin() {
		return win;
	}
	public void setWin(int win) {
		this.win = win;
	}
	public void plusWin(){
		win++;
	}
	public void plusJudgement(){
		judgement++;
	}
	
	public double getProbability(){
		return ((double)win/judgement)*100;
	}
	
}
