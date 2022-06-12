package util;

import game.Block;

public class Judge {

	public static boolean isFixedBlock(Block[][] copyBoard, int checkI, int checkJ) {
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

		loof1 : for(int i = 0 ; i < Globals.scale ; i++) {

			for(int j = 0 ; j <= checkJ + controllValue ; j++) {

				if(copyBoard[i][j].getPlayer() != currPlayer) {
					break loof1;
				}
			}

			if (i >= checkI)
				controllValue--;

			if(i == Globals.scale-1 )
				return true;
		}
		controllValue = 0;

		}
		
		
		if (copyBoard[7][0].getPlayer() == currPlayer) {

			loof2 : for(int i = Globals.scale-1 ; i >= 0 ; i--) {

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
			
			if(checkJ + controllValue < Globals.scale) controllValue++;
			
			for (int j = 0 ; j < checkJ + controllValue; j++) {
				if(copyBoard[i][j].getPlayer() != currPlayer) {
					break loof7;
				}
			}

			if(i == Globals.scale-1 )
				return true;
		}
		controllValue = 0;

		}
		
		
		
		if (copyBoard[7][7].getPlayer() == currPlayer) {

			loof3 : for(int i = checkI ; i < Globals.scale ; i++) {

				for(int j = checkJ - controllValue ; j < Globals.scale ; j++) {

					if(copyBoard[i][j].getPlayer() != currPlayer) {
						break loof3;
					}
				}

				if(controllValue < checkJ)
					controllValue++;

				if(i == Globals.scale-1 )
					return true;
			}
		controllValue = 0;



		loof4 : for(int i = 7 ; i >= Globals.scale ; i--) {

			if(i < checkI) controllValue++;

			for(int j = checkJ - controllValue ; j < Globals.scale ; j++) {

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

			loof5 : for(int i = 0 ; i < Globals.scale ; i++) {

				if(i > checkI) controllValue++;

				for(int j = checkJ + controllValue ; j < Globals.scale ; j++) {

					if(copyBoard[i][j].getPlayer() != currPlayer) {
						break loof5;
					}
				}

				if(i == Globals.scale-1 )
					return true;
			}
		controllValue = 0;


		loof6 : for(int i = checkI ; i >= 0 ; i--) {

			if (i < checkI && checkJ > controllValue ) controllValue++;

			for(int j = checkJ - controllValue ; j < Globals.scale ; j++) {

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
