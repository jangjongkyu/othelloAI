package game;

import gui.MenuView;
/*
 * branch 분기 임시 commit
 */
public class Launcher {
	public static void main(String[] args) {
		MenuView menu = new MenuView();
		menu.setVisible(true);
	}
	public static void start1P(){
		Game g = new Game(1,8,8);
		g.init();
		g.start();
	}
	public static void start2P(){
		Game g = new Game(2,8,8);
		g.init();
		g.start();
	}
	public static void startAItoAI(){
		Game g = new Game(true, 7,7,8);
		g.init();
		g.start();
	}
}
