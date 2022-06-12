package util;

import game.Block;

public class BlockCalculator {

	public static boolean isImpossible(Block currentBlock, final int currPlayer){
		/**
		 * EX) Im 2
		 * 
		 *  0 1 1 2 	OK
		 *  0 1 2		OK
		 *  0 1 1 0		NO
		 *  0 0 1		NO
		 *  0 2			NO
		 */
		for(int i = 0 ; i < 8 ; i++) {
			Block targetBlock = null;
			switch(i){
				case 0 : targetBlock = currentBlock.getNorth_west(); break;
				case 1 : targetBlock = currentBlock.getNorth(); break;
				case 2 : targetBlock = currentBlock.getNorth_east(); break;
				case 3 : targetBlock = currentBlock.getWest(); break;
				case 4 : targetBlock = currentBlock.getEast(); break;
				case 5 : targetBlock = currentBlock.getSouth_west(); break;
				case 6 : targetBlock = currentBlock.getSouth(); break;
				case 7 : targetBlock = currentBlock.getSouth_east(); break;
			}
			
			//상대방돌이 하나이상 발견되었을경우 true
			boolean markedEnemy = false;
			while(targetBlock != null && targetBlock.getPlayer() != 0){
				int targetPlayer = targetBlock.getPlayer();
				
				if(!markedEnemy ){
					//최초루프. 바로옆 인접Block일경우
					
					if(targetPlayer != currPlayer){
						//인접Block이 상대방돌일경우 check하고 다음으로 패스
						markedEnemy = true;
					} else {
						//인접Block이 내  돌일경우 실패
						break;
					}
				} else if(targetPlayer == currPlayer){
					return true;
				}
				
				switch(i){
					case 0 : targetBlock = targetBlock.getNorth_west(); break;
					case 1 : targetBlock = targetBlock.getNorth(); break;
					case 2 : targetBlock = targetBlock.getNorth_east(); break;
					case 3 : targetBlock = targetBlock.getWest(); break;
					case 4 : targetBlock = targetBlock.getEast(); break;
					case 5 : targetBlock = targetBlock.getSouth_west(); break;
					case 6 : targetBlock = targetBlock.getSouth(); break;
					case 7 : targetBlock = targetBlock.getSouth_east(); break;
				}
			}
			
		}
		
		return false;
	}
}
