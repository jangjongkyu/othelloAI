package test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TestCom {

	public static class Point {
		public int x;
	}
	
	public static void main(String[] args) {
		List<Point> list = new ArrayList<Point>();
		Point point1 = new Point();
		Point point2 = new Point();
		Point point3 = new Point();
		point1.x = 3;
		point2.x = 1;
		point3.x = 2;
		list.add(point1);
		list.add(point2);
		list.add(point3);
		
		list.sort(new Comparator<Point>() {

			@Override
			public int compare(Point o1, Point o2) {
				if(o1.x < o2.x){
					return 1;
				} else {
					return -1;
				}
			}
			
		});
		
		list.forEach(point -> {
			System.out.println(point.x);
		});
	}
	
}
