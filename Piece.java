import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
public class Piece{
	private boolean connectedT = false;
	private boolean connectedR = false;
	private boolean connectedB = false;
	private boolean connectedL = false;
	
	private BufferedImage pieceInfo;
	private ImageObserver obs;
	private boolean inPlace = false;

	private int row;
	private int col;
	private int x;
	private int y;
	
	private BufferedImage top;
	private BufferedImage left;
	private BufferedImage bottom;
	private BufferedImage right;
	public Piece(BufferedImage i) {
		pieceInfo = i;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	public void setY(int y) {
		this.y = y;
	}
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	
	public boolean isInPlace() {
		return inPlace;
	}
	public int getRow() {
		return row;
	}
	public int getCol() {
		return col;
	}
	public void setRow(int r) {
		row = r;
	}
	public void setCol(int c) {
		col = c;
	}
	public void setEdges(BufferedImage t, BufferedImage l, BufferedImage b, BufferedImage r) {
		top = t;
		bottom = b;
		left = l;
		right = r;
	}
	
	
	
	public BufferedImage getTop() {
		return top;
	}
	public BufferedImage getLeft() {
		return left;
	}
	public BufferedImage getBottom() {
		return bottom;
	}
	public BufferedImage getRight() {
		return right;
	}
	
	public BufferedImage getPieceInfo() {
		return pieceInfo;
	}
}
