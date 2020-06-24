import java.util.Random;

import javax.swing.JFrame;

public class Runner {
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setResizable(false);
		frame.setSize(600,625);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Puzzle p = new Puzzle();
		frame.addMouseMotionListener(p);
		frame.addMouseListener(p);
		frame.addKeyListener(p);
		frame.add(p);
	}
}
