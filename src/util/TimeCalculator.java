package util;

public class TimeCalculator {

	public static String getMMSSFromMillisecondString(long millis) {
		int millisToSecond = (int) millis/1000;
		
		int minute = millisToSecond / 60;
		int second = millisToSecond % 60;
		
		return minute + "Ка " + second + " УЪ";
	}
	
}
