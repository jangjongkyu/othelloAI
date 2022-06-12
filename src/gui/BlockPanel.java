package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import game.Game;

public class BlockPanel extends JPanel implements MouseListener{
	private int savePosition;
	private int player;
	private boolean isImpBlock;
	private boolean isLastBlock;
	private Game game;
	
	public BlockPanel(Game game,int savePosition){
		this.game = game;
		this.savePosition = savePosition;
		addMouseListener(this);
	}
	
	public int getSavePosition() {
		return savePosition;
	}

	public void setSavePosition(int savePosition) {
		this.savePosition = savePosition;
	}


	public int getPlayer() {
		return player;
	}

	public void setPlayer(int player) {
		this.player = player;
	}

	public boolean isLastBlock() {
		return isLastBlock;
	}

	public void setLastBlock() {
		this.isLastBlock = true;
	}

	public void clearLastBlock() {
		this.isLastBlock = false;
	}

	public boolean isImpBlock() {
		return isImpBlock;
	}

	public void clearImpBlock() {
		this.isImpBlock = false;
	}

	public void setImpBlock() {
		this.isImpBlock = true;
	}
	
	public void clearData(){
		clearImpBlock();
		clearLastBlock();
	}

	@Override
	public void paint(Graphics g) {
		g.setColor(Color.white);
		g.clearRect(0, 0, this.getWidth(), this.getHeight());
		super.paint(g);

		if (this.player > 0) {
			if (this.player == 1) g.setColor(Color.BLACK);
			else g.setColor(Color.WHITE);
			
			g.fillOval(3, 3, this.getWidth() - 6, this.getHeight() - 6);
			if(this.isLastBlock){
				g.setColor(Color.RED);
				g.drawString("last", this.getWidth()/2 -10, this.getHeight()/2);
			}
			//return;
		}
		if (isImpBlock) {
			g.setColor(Color.RED);
			g.drawOval(3, 3, this.getWidth() - 6, this.getHeight() - 6);
		}

		if(this.savePosition > 0){
			g.setColor(Color.blue);
			g.drawString(this.savePosition+"", 0, this.getHeight()/2-10);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if(!game.getChoiceBlock(this.savePosition)){
			JOptionPane.showMessageDialog(null, this.savePosition+"착수할 수 없는곳이거나 상대방을 기다리는중입니다.");
		}
	}

}
