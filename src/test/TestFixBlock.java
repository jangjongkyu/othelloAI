package test;
import java.util.List;



public class TestFixBlock {
	
	private static int player = 2;
	private static int enemyPlayer = 1;
	
	private static int scale = 8;
	
	public static void main(String[] args) {
		
		int[][] state = new int[][] {
			{ 2 , 0 , 2 , 2 , 2 , 1 , 1 , 1 },
			{ 0 , 0 , 0 , 0 , 0 , 0 , 0 , 0 },
			{ 0 , 0 , 0 , 0 , 0 , 0 , 0 , 0 },
			{ 0 , 0 , 0 , 0 , 0 , 0 , 0 , 0 },
			{ 0 , 0 , 0 , 0 , 0 , 0 , 0 , 0 },
			{ 0 , 0 , 0 , 0 , 0 , 0 , 0 , 0 },
			{ 0 , 0 , 0 , 0 , 0 , 0 , 0 , 0 },
			{ 0 , 0 , 0 , 0 , 0 , 0 , 0 , 0 }
		};
		
		
		int score = calculateScoreOpenning(state);
		
		
		int arr[][] = new int[8][8];
		int arr2[][] = new int[8][8];
		int arr3[][] = new int[8][8];
		int arr4[][] = new int[8][8];
		int arr5[][] = new int[8][8];
		int arr6[][] = new int[8][8];
		int arr7[][] = new int[8][8];
		int arr8[][] = new int[8][8];

		int controllValue = 0;

		int checkI = 1;
		int checkJ = 2;

		for (int i = 0; i < 8; i++) {

			for (int j = 0; j <= checkJ + controllValue; j++) {

				arr[i][j] = 1;
			}

			if (i >= checkI)
				controllValue--;

		}
		controllValue = 0;

		for (int i = 7; i >= 0; i--) {

			if (i < checkI)
				controllValue++;

			for (int j = 0; j < checkJ + 1 - controllValue; j++) {
				arr2[i][j] = 1;
			}

		}
		controllValue = 0;

		for (int i = checkI; i < 8; i++) {

			for (int j = checkJ - controllValue; j < 8; j++) {
				arr3[i][j] = 1;
			}

			if (controllValue < checkJ)
				controllValue++;

		}
		controllValue = 0;

		for (int i = 7; i >= 0; i--) {

			if (i < checkI)
				controllValue++;

			for (int j = checkJ + controllValue; j < 8; j++) {
				arr4[i][j] = 1;
			}

		}
		controllValue = 0;

		for (int i = 0; i < 8; i++) {

			if (i > checkI)
				controllValue++;

			for (int j = checkJ + controllValue; j < 8; j++) {
				arr5[i][j] = 1;
			}

		}
		controllValue = 0;

		for (int i = checkI; i >= 0; i--) {

			if (i < checkI && checkJ > controllValue)
				controllValue++;

			for (int j = checkJ - controllValue; j < 8; j++) {
				arr6[i][j] = 1;
			}

		}
		controllValue = 0;

		for (int i = checkI; i < 8; i++) {

			if(checkJ + controllValue < 8)
				controllValue++;
			
			for (int j = 0 ; j < checkJ + controllValue; j++) {
				System.out.println(i+" , "+j);
				arr7[i][j] = 1;
			}

		}
		controllValue = 0;
		
		
		for (int i = checkI; i >= 0; i--) {

			if(checkJ + controllValue < 8)
			controllValue++;

			for (int j = 0 ; j < checkJ + controllValue; j++) {
				arr8[i][j] = 1;
			}

		}
		controllValue = 0;

		
		
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				System.out.print(arr8[i][j] + " ");
			}
			System.out.println();
		}
		System.out.println();
		
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				System.out.print(arr[i][j] + " ");
			}
			System.out.println();
		}
		
		
		System.out.println();
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				System.out.print(arr2[i][j] + " ");
			}
			System.out.println();
		}
		System.out.println();
		
		
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				System.out.print(arr7[i][j] + " ");
			}
			System.out.println();
		}
		System.out.println();
		
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				System.out.print(arr3[i][j] + " ");
			}
			System.out.println();
		}
		System.out.println();
		
		

		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				System.out.print(arr4[i][j] + " ");
			}
			System.out.println();
		}
		System.out.println();

		
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				System.out.print(arr5[i][j] + " ");
			}
			System.out.println();
		}
		System.out.println();

		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				System.out.print(arr6[i][j] + " ");
			}
			System.out.println();
		}
		System.out.println();
		
		
		System.out.println("점수계 : "+score);
		
	}
	
	
	
	private static int calculateScoreOpenning(int[][] copyBoard ) {
		// TODO:calculateScore
		int aiImpossible = 0;
		int enemyImpossible = 0;

		// TODO : 구석의 돌을 먹고있으면 구석점수 더먹음  -> 나중에 구석이 아닌 굳힘돌로 바꿔야함
		for(int i = 0 ; i < 8 ; i++) {
			
			for(int j = 0 ; j < 8 ; j++ ) {
				if(copyBoard[i][j] != 0 && isFixedBlock(copyBoard, i, j)) {
					
					if(copyBoard[i][j] == player) {
						aiImpossible ++;
					}else if(copyBoard[i][j] == enemyPlayer) {
						enemyImpossible ++;
					}
					
				}
			}
			
		}
		System.out.println("aiImpossible : "+aiImpossible + " , enemyImpossible : "+enemyImpossible);
		return aiImpossible - enemyImpossible;
	}
	
	
	
	
	
	
	public static boolean isFixedBlock(int[][] copyBoard, int checkI, int checkJ) {
		int currPlayer = copyBoard[checkI][checkJ];
		System.out.println("currPlayer : "+currPlayer);
		
		int controllValue = 0;
		
		
		if (copyBoard[0][0] == currPlayer) {
			
			loof8 : for (int i = checkI; i >= 0; i--) {

				if(checkJ + controllValue < 8)
				controllValue++;
				for (int j = 0 ; j < checkJ + controllValue; j++) {
					if(copyBoard[i][j] != currPlayer) {
						break loof8;
					}
				}

				if(i == 0 )
					return true;
			}
		controllValue = 0;

		loof1 : for(int i = 0 ; i < scale ; i++) {

			for(int j = 0 ; j <= checkJ + controllValue ; j++) {

				if(copyBoard[i][j] != currPlayer) {
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
		
		
		if (copyBoard[7][0] == currPlayer) {

			loof2 : for(int i = scale-1 ; i >= 0 ; i--) {

				if(i <= checkI) controllValue ++;

				for(int j = 0 ; j < checkJ+1 - controllValue ; j++) {

					if(copyBoard[i][j] != currPlayer) {
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
				if(copyBoard[i][j] != currPlayer) {
					break loof7;
				}
			}

			if(i == scale-1 )
				return true;
		}
		controllValue = 0;

		}
		
		
		
		if (copyBoard[7][7] == currPlayer) {

			loof3 : for(int i = checkI ; i < scale ; i++) {

				for(int j = checkJ - controllValue ; j < scale ; j++) {

					if(copyBoard[i][j] != currPlayer) {
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

				if(copyBoard[i][j] != currPlayer) {
					break loof4;
				}
			}

			if(i == 0 )
				return true;
		}
		controllValue = 0;

		}
		
		
		if (copyBoard[0][7] == currPlayer) {

			loof5 : for(int i = 0 ; i < scale ; i++) {

				if(i > checkI) controllValue++;

				for(int j = checkJ + controllValue ; j < scale ; j++) {

					if(copyBoard[i][j] != currPlayer) {
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

				if(copyBoard[i][j] != currPlayer) {
					break loof6;
				}
			}

			if(i == 0 )
				return true;
		}
		controllValue = 0;

		}
		
		if(checkI == 0) {
			for(int j = 0 ; j < 8 ; j++) {
				if(copyBoard[0][j] == 0) {
					break;
				}
				if(j == 7) return true;
			}
		} else if(checkI == 7) {
			for(int j = 0 ; j < 8 ; j++) {
				if(copyBoard[7][j] == 0) {
					break;
				}
				if(j == 7) return true;
			}
		} else if(checkJ == 0) {
			for(int i = 0 ; i < 8 ; i++) {
				if(copyBoard[i][0] == 0) {
					break;
				}
				if(i == 7) return true;
			}
		} else if(checkJ == 7) {
			for(int i = 0 ; i < 8 ; i++) {
				if(copyBoard[i][7] == 0) {
					break;
				}
				if(i == 7) return true;
			}
		}
		
		
		return false;
	}

}
