import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class Puzzle extends JPanel implements KeyListener, MouseMotionListener, MouseListener{
	private Piece[][] puzzle;
	private int[] PX;
	private int[] PY;
	private int[] priority;
	private int[] puzzleSizes = new int[] {3, 5, 8, 10, 16, 20};
	private Image image;
	private ImageObserver obs;
	private boolean selectingSize;
	private boolean selectingImage = true;
	private String userInput = "";
	private boolean holdingShift = false;
	private boolean fileDoesntExist = false;
	private boolean fileIncorrectSize = false;
	private String errorFile = "";
	private boolean highlightStart;
	private boolean doingPuzzle = false;
	private int pieces = 9;
	private int sliderPosition = 0;
	private boolean draggingSlider = false;
	private final int edgeTypes = 2;

	private boolean draggingPiece;
	private int pieceDragged;
	private int dragDifX;
	private int dragDifY;
	
	private int pieceSize;
	
	private Piece[] Pieces;
	private int[][] mapT, mapL, mapB, mapR;
	private mapType map;
	private int colorPlaceholder = -1;
	public Puzzle() {
		selectingImage = true;
		update();
	}
	
	public void paint(Graphics g) {
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(0, 0, 800, 800);
		if(selectingImage) {
			g.setColor(Color.black);
			g.setFont(new Font("Courier new", 0, 30));
			g.drawString("Enter the Image Filename", 110, 100);
			
			int fontsize = 75;
			while(0.8 * fontsize * userInput.length() > 600) {
				fontsize --;
			}
			g.setFont(new Font("Courier new", 0, fontsize));
			g.drawString(userInput, 300 - (int)((userInput.length() * 0.6 * fontsize)/2), 300);
			if(fileDoesntExist) {
				g.setColor(Color.red);
				fontsize = 75;
				while(0.8 * fontsize * ("File entered \""+errorFile+"\" does not exist").length() > 600) {
					fontsize --;
				}
				g.setFont(new Font("Courier new", 0, fontsize));
				g.drawString("File entered \""+errorFile+"\" does not exist", 300 - (int)((("File entered \""+errorFile+"\" does not exist").length() * 0.6 * fontsize)/2), 400);
				
			}
			else if(fileIncorrectSize) {
				g.setColor(Color.red);
				fontsize = 75;
				while(0.8 * fontsize * ("Image selected \""+errorFile+"\" does not have").length() > 600) {
					fontsize --;
				}
				g.setFont(new Font("Courier new", 0, fontsize));
				g.drawString("Image selected \""+errorFile+"\" does not have", 300 - (int)((("Image selected \""+errorFile+"\" does not have").length() * 0.6 * fontsize)/2), 400);
				g.drawString("equal height and width", 300 - (int)((("equal height and width").length() * 0.6 * fontsize)/2), 400 + fontsize + 10);
				
			}
		}
		else if(selectingSize) {
			if(highlightStart) {
				g.setColor(Color.white);
				g.fillRoundRect(98, 528, 104, 54, 10, 10);
			}
			g.setColor(Color.orange);
			g.fillRoundRect(100, 530, 100, 50, 10, 10);
			g.setColor(Color.black);
			g.setFont(new Font("Courier New", 0, 20));
			g.drawString("Start", 120, 550);
			g.drawString("Puzzle", 115, 575);
			Graphics2D g2d = (Graphics2D)g;
			g2d.drawImage(image, 50, 10, 550, 510, 0, 0, 500, 500, obs);
			
			g.setColor(Color.DARK_GRAY);
			g.drawLine(250, 552, 520, 552);
			g.drawString("9   25  64  100      400", 250, 580);
			g.drawString("256", 450, 580);
			for(int i = 0; i < 6; i++) {
				g.drawLine(250 + i*54, 550, 250 + i*54, 555);
			}
			
			g.setColor(new Color(245,245,245));
			g.fillRoundRect(sliderPosition+245, 537, 10, 30, 5, 5);
			
			g.setColor(Color.yellow);
			for(int i = 1; i < (int)Math.sqrt(pieces); i++) {
				g.drawLine(i*(500/(int)Math.sqrt(pieces)) + 50, 10, i*(500/(int)Math.sqrt(pieces)) + 50, 510);
				g.drawLine( 50, i*(500/(int)Math.sqrt(pieces)) + 10,  550, i*(500/(int)Math.sqrt(pieces)) + 10);
			}
		}
		else if(doingPuzzle) {
			g.setColor(Color.black);
			g.fillRect(45,45,510, 510);

			g.setColor(Color.LIGHT_GRAY);
			g.fillRect(50, 50, 500, 500);
			
			
			for(int i = priority.length - 1; i >= 0; i--) {
				drawPiece(g, Pieces[priority[i]]);
			}
			
		}
	}
	
	public void drawPiece(Graphics g, Piece p) {
		BufferedImage B = p.getPieceInfo();
		Graphics2D g2d = (Graphics2D)g;
		g2d.drawImage(B, p.getX() - B.getWidth()/2, p.getY() - B.getHeight()/2, B.getWidth(), B.getHeight(), null);
	}
	
	public void setImage(String s) throws IOException{
		boolean valid = true;
		try {
			ImageIO.read(new File(s));
		}
		catch(Exception e) {
			valid = false;
			fileDoesntExist = true;	
			fileIncorrectSize = false;
			errorFile = userInput;
			userInput = "";
		}
		if(valid && ImageIO.read(new File(s)).getWidth() != ImageIO.read(new File(s)).getHeight()) {
				valid = false;
				fileIncorrectSize = true;
				fileDoesntExist = false;
				errorFile = userInput;
				userInput = "";
		}
		if(valid) {
			if(ImageIO.read(new File(s)).getWidth() != 500){
				Image in = ImageIO.read(new File(s));
				BufferedImage b = new BufferedImage(500,500,BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2d = b.createGraphics();
				g2d.drawImage(in, 0, 0, 500, 500, null);
				image = b;
			}
			else {
				image = ImageIO.read(new File(s));
			}
			selectingSize = true;
			selectingImage = false;
			Point p = MouseInfo.getPointerInfo().getLocation();
			if(p.x >= 100 && p.x <= 200 && p.y >= 530 && p.y <= 580) {
				highlightStart = true;
			}
		}
		update();
	}
	
	
	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if(selectingSize) {
			if(e.getX() >= sliderPosition + 245 && e.getX() <= sliderPosition + 255 && e.getY() >= 560 && e.getY() <= 590) {
				draggingSlider = true;
			}
			else if(e.getX() >= 100 && e.getX() <= 200 && e.getY() >= 560 && e.getY() <= 605) {
				try {
					generatePuzzle();
				} catch (IOException e1) {}
				doingPuzzle = true;
				selectingSize = false;
				update();
			}
		}
		else if(doingPuzzle) {
			int pieceChosen = -1;
			for(int i = 0; i < priority.length && pieceChosen == -1; i++) {
				if(e.getX() > (Pieces[priority[i]].getX() - pieceSize/2) && 
				   e.getX() < (Pieces[priority[i]].getX() + pieceSize/2) && 
				   e.getY() > (Pieces[priority[i]].getY() - pieceSize/2) &&
				   e.getY() < (Pieces[priority[i]].getX() + pieceSize/2)) {
					pieceChosen = priority[i];
					if(i != 0) {
						for(int j = i - 1; j >= 0; j--) {
							priority[j + 1] = priority[j];
						}
						priority[0] = pieceChosen;
					}
					dragDifX = Pieces[priority[i]].getX() - e.getX();
					dragDifY = Pieces[priority[i]].getY() - e.getY();
				}
			}
			if(pieceChosen != -1) {
				draggingPiece = true;
				pieceDragged = pieceChosen;
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		if(draggingSlider) {
			draggingSlider = false;
			if(sliderPosition < 27) 
				sliderPosition = 0;
			else if(sliderPosition < 81)
				sliderPosition = 54;
			else if(sliderPosition < 135)
				sliderPosition = 108;
			else if(sliderPosition < 189)
				sliderPosition = 162;
			else if(sliderPosition < 233)
				sliderPosition = 216;
			else
				sliderPosition = 270;
			pieces = (int)Math.pow(puzzleSizes[sliderPosition/54],2);
			update();
		}

		if(draggingPiece) {
			draggingPiece = false;
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if(selectingSize && draggingSlider) {
			if(e.getX() < 250) {
				sliderPosition = 0;
			}
			else if(e.getX() > 520) {
				sliderPosition = 270;
			}
			else {
				sliderPosition = e.getX() - 250;
			}
			update();
		}
		if(draggingPiece) {
			Pieces[pieceDragged].setX(e.getX() + dragDifX);
			Pieces[pieceDragged].setY(e.getY() + dragDifY);
			update();
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if(selectingSize) {
			if(e.getX() >= 100 && e.getX() <= 200 && e.getY() >= 560 && e.getY() <= 605 && !highlightStart) {
				highlightStart = true;
				update();
			}
			else if(!(e.getX() >= 100 && e.getX() <= 200 && e.getY() >= 560 && e.getY() < 605) && highlightStart) {
				highlightStart = false;
				update();
			}
		}
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		
	}


	public void keyPressed(KeyEvent e) {
		if(selectingImage) {
			if(e.getKeyCode() == KeyEvent.VK_SHIFT) {
				holdingShift = true;
			}
			if(e.getKeyCode() >= KeyEvent.VK_A && 
					e.getKeyCode() <= KeyEvent.VK_Z) {
				if(holdingShift) {
					userInput = userInput + ((char)e.getKeyCode());
				}
				else {
					userInput = userInput + ((char)(e.getKeyCode() + 32));
				}
			}
			else if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
				try {
					userInput = userInput.substring(0, userInput.length()-1);
				}
				catch(Exception ex) {}
			}
			else if(e.getKeyCode() == KeyEvent.VK_MINUS) {
				if(holdingShift)
					userInput = userInput + "_";
				else
					userInput = userInput + "-";
			}
			else if(e.getKeyCode() == KeyEvent.VK_PERIOD && !holdingShift) {
				userInput = userInput + ".";
			}
			else if(e.getKeyCode() == KeyEvent.VK_ENTER) {
				try {
					if(userInput == "")
						setImage("Confused.jpg");
					else
						setImage(userInput);
				}
				catch(Exception ex) {}
			}
		}
		update();
	}
	public boolean isValidName(String s) {
		return true;
	}
	
	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_SHIFT) {
			holdingShift = false;
		}
		update();
	}

	public void update() {
		this.setSize(this.getSize().width + 1, this.getSize().height);
		this.setSize(this.getSize().width - 1, this.getSize().height);
	}

	public void generatePuzzle() throws IOException {
		int width = (int)Math.sqrt(pieces);
		puzzle = new Piece[pieces][pieces];
		BufferedImage t,l,b,r, scaledTop, scaledLeft, scaledBottom, scaledRight, edges;
		
		pieceSize = 500/width;
		BufferedImage finalPiece;
		Random random = new Random();
		AffineTransform tx;
		AffineTransformOp op;
		AffineTransform tx2; 
		AffineTransformOp op2;
		Graphics2D g2d;
		
		Graphics2D G2D;

		BufferedImage imageWithBorder = new BufferedImage(500 + (500/width), 500 + (500/width), BufferedImage.TYPE_INT_ARGB);
		G2D = imageWithBorder.createGraphics();
		G2D.setColor(Color.black);
		G2D.fillRect(0, 0, imageWithBorder.getWidth(), imageWithBorder.getHeight());
		G2D.drawImage(image, (500/width)/2, (500/width)/2, 500 + (500/width)/2, 500 + (500/width)/2, 0, 0, image.getWidth(obs), image.getHeight(obs), obs);
		
		Image IWB = imageWithBorder;
		for(int row = 0; row < width; row++) {
			for(int col = 0; col < width; col++) {
				
				if(row == 0) {  //generates the top edge
					t = ImageIO.read(new File("Edge-0.png"));
				} else {
					t = puzzle[row - 1][col].getBottom();
				}
				
				scaledTop = new BufferedImage(500/width, 500/width, BufferedImage.TYPE_INT_ARGB); //scales the image
				g2d = scaledTop.createGraphics();
				g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g2d.drawImage(t, 0, 0, 500/width, 500/width, null);
				g2d.dispose();
				
				for(int i = 0; i < scaledTop.getWidth(); i++) { // gets rid of different colors from scaling
					for(int j = 0; j < scaledTop.getHeight(); j++) {
						if(scaledTop.getRGB(i,j) != 0) {
							scaledTop.setRGB(i,j, -16777216);
						}
					}
				}
				map = mapType.t;
				mapT = new int[scaledTop.getWidth()][scaledTop.getHeight()]; //generates a blank map of the image
				for(int i = 0; i < scaledTop.getWidth(); i++) {
					for(int x = 0; x < scaledTop.getHeight(); x++) {
						mapT[i][x] = 2;
					}
				}
				
				for(int i = 0; i < scaledTop.getWidth(); i++) {  //maps out the piece
					searchColumn(i, 0, scaledTop, false, false);
				}
				
				for(int i = 0; i < mapT.length; i++) {  //sets the map pieces to white
					for(int x = 0; x < mapT[0].length; x++) {
						if(mapT[i][x] == 0) {
							scaledTop.setRGB(i, x, colorPlaceholder);
						}
					}
				}
			    
				//Top edge is done generation
				
				
				if(col == 0) {
					l = ImageIO.read(new File("Edge-0.png"));
				} else {
					l = puzzle[row][col - 1].getRight();
					tx2 = AffineTransform.getScaleInstance(1, -1);
					tx2.translate(0, -l.getHeight(null));
					op2 = new AffineTransformOp(tx2, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
					l = op2.filter(l, null);
				}
				
				scaledLeft = new BufferedImage(500/width, 500/width, BufferedImage.TYPE_INT_ARGB); //scales the image
				g2d = scaledLeft.createGraphics();
				g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g2d.drawImage(l, 0, 0, 500/width, 500/width, null);
				g2d.dispose();
				for(int i = 0; i < scaledLeft.getWidth(); i++) { // gets rid of different colors from scaling
					for(int j = 0; j < scaledLeft.getHeight(); j++) {
						if(scaledLeft.getRGB(i,j) != 0) {
							scaledLeft.setRGB(i,j, -16777216);
						}
					}
				}
				///*

				map = mapType.l;
				mapL = new int[scaledLeft.getWidth()][scaledLeft.getHeight()]; //generates a blank map of the image
				for(int i = 0; i < scaledLeft.getWidth(); i++) {
					for(int x = 0; x < scaledLeft.getHeight(); x++) {
						mapL[i][x] = 2;
					}
				}
				for(int i = 0; i < scaledLeft.getWidth(); i++) {  //maps out the piece
					searchColumn(i, 0, scaledLeft, false, false);
				}
				
				for(int i = 0; i < mapL.length; i++) {  //sets the map pieces to white
					for(int x = 0; x < mapL[0].length; x++) {
						if(mapL[i][x] == 0) {
							scaledLeft.setRGB(i, x, colorPlaceholder);
						}
					}
				}
				tx = AffineTransform.getRotateInstance(Math.toRadians (270), scaledLeft.getWidth()/2, scaledLeft.getHeight()/2);
				op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
				scaledLeft = op.filter(scaledLeft, null);
				
				//Left Edge is done generation
				
				
				
				if(row == width - 1) {
					b = ImageIO.read(new File("Edge-0.png"));
				}
				else {
					b = ImageIO.read(new File("Edge-"+ (random.nextInt(edgeTypes)+1) + ".png"));
					if(random.nextBoolean()) {
						tx2 = AffineTransform.getScaleInstance(1, -1);
						tx2.translate(0, -b.getHeight(null));
						op2 = new AffineTransformOp(tx2, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
						b = op2.filter(b, null);
					}
				}
				
				
				scaledBottom = new BufferedImage(500/width, 500/width, BufferedImage.TYPE_INT_ARGB); //scales the image
				g2d = scaledBottom.createGraphics();
				g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g2d.drawImage(b, 0, 0, 500/width, 500/width, null);
				g2d.dispose();
				for(int i = 0; i < scaledBottom.getWidth(); i++) { // gets rid of different colors from scaling
					for(int j = 0; j < scaledBottom.getHeight(); j++) {
						if(scaledBottom.getRGB(i,j) != 0) {
							scaledBottom.setRGB(i,j, -16777216);
						}
					}
				}
				///*
				map = mapType.b;
				mapB = new int[scaledBottom.getWidth()][scaledBottom.getHeight()]; //generates a blank map of the image
				for(int i = 0; i < scaledBottom.getWidth(); i++) {
					for(int x = 0; x < scaledBottom.getHeight(); x++) {
						mapB[i][x] = 2;
					}
				}
				for(int i = 0; i < scaledBottom.getWidth(); i++) {  //maps out the piece
					searchColumn(i, 0, scaledBottom, false, false);
				}
				
				for(int i = 0; i < mapB.length; i++) {  //sets the map pieces to white
					for(int x = 0; x < mapB[0].length; x++) {
						if(mapB[i][x] == 0) {
							scaledBottom.setRGB(i, x, colorPlaceholder);
						}
					}
				}
				tx = AffineTransform.getRotateInstance(Math.toRadians (180), scaledBottom.getWidth()/2, scaledLeft.getHeight()/2);
				op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
				scaledBottom = op.filter(scaledBottom, null);
				
				
				//bottom edge is done generation
				
				if(col == width - 1) {
					r = ImageIO.read(new File("Edge-0.png"));
				}
				else {
					r = ImageIO.read(new File("Edge-"+ (random.nextInt(edgeTypes)+1) + ".png"));
					if(random.nextBoolean()) {
						tx2 = AffineTransform.getScaleInstance(1, -1);
						tx2.translate(0, -r.getHeight(null));
						op2 = new AffineTransformOp(tx2, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
						r = op2.filter(r, null);
					}
				}
				
				scaledRight = new BufferedImage(500/width, 500/width, BufferedImage.TYPE_INT_ARGB); //scales the image
				g2d = scaledRight.createGraphics();
				g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g2d.drawImage(r, 0, 0, 500/width, 500/width, null);
				g2d.dispose();
				for(int i = 0; i < scaledRight.getWidth(); i++) { // gets rid of different colors from scaling
					for(int j = 0; j < scaledRight.getHeight(); j++) {
						if(scaledRight.getRGB(i,j) != 0) {
							scaledRight.setRGB(i,j, -16777216);
						}
					}
				}
				///*
				map = mapType.r;
				mapR = new int[scaledRight.getWidth()][scaledRight.getHeight()]; //generates a blank map of the image
				for(int i = 0; i < scaledRight.getWidth(); i++) {
					for(int x = 0; x < scaledRight.getHeight(); x++) {
						mapR[i][x] = 2;
					}
				}
				for(int i = 0; i < scaledRight.getWidth(); i++) {  //maps out the piece
					searchColumn(i, 0, scaledRight, false, false);
				}
				
				for(int i = 0; i < mapR.length; i++) {  //sets the map pieces to white
					for(int x = 0; x < mapR[0].length; x++) {
						if(mapR[i][x] == 0) {
							scaledRight.setRGB(i, x, colorPlaceholder);
						}
					}
				}
				tx = AffineTransform.getRotateInstance(Math.toRadians (90), scaledRight.getWidth()/2, scaledLeft.getHeight()/2);
				op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
				scaledRight = op.filter(scaledRight, null);
				
				//right edge is done generation
				
				
				
				//Makes a buffered image to represent the finished piece
				edges = new BufferedImage(scaledLeft.getWidth() + scaledRight.getWidth(), scaledTop.getHeight() + scaledBottom.getHeight(), BufferedImage.TYPE_INT_ARGB);
				
				//draws the respective part of the complete image onto the piece
				g2d = edges.createGraphics();
				g2d.drawImage(IWB, 0, 0, edges.getWidth(), edges.getHeight(), (col*(500/width)), (row*(500/width)), ((col)*(500/width)) + (500/width)*2, ((row)*(500/width)) + (500/width)*2, obs);
				g2d.setColor(Color.white);
				
				
				
				//Draws white space over the empty corners
				g2d.fillRect(0,0,(500/width)/2,(500/width)/2);
				g2d.fillRect(edges.getWidth()-(500/width)/2,0,(500/width)/2,(500/width)/2);
				g2d.fillRect(0, edges.getHeight() - (500/width)/2, (500/width)/2, (500/width)/2);
				g2d.fillRect(edges.getWidth()-(500/width)/2,edges.getHeight() - (500/width)/2, (500/width)/2, (500/width)/2);
				
				//g2d.fillRect(0, 0, 10, 10);
				
				//Draws the edges onto the piece
				g2d.drawImage(scaledTop, (500/width)/2, 0, scaledTop.getWidth(), scaledTop.getHeight(),null);
				g2d.drawImage(scaledLeft, 0, (500/width)/2, scaledLeft.getWidth(), scaledLeft.getHeight(),null);
				g2d.drawImage(scaledRight, (500/width), (500/width)/2, scaledRight.getWidth(), scaledRight.getHeight(),null);
				g2d.drawImage(scaledBottom, (500/width)/2, (500/width), scaledBottom.getWidth(), scaledBottom.getHeight(),null);
				
				g2d.dispose();
				
				for(int i = 0; i < edges.getWidth(); i++) {
					for(int j = 0; j < edges.getHeight(); j++) {
						if(edges.getRGB(i,j) == -1)
							edges.setRGB(i,j,0);
					}
				}
				
				puzzle[row][col] = new Piece(edges);
				puzzle[row][col].setEdges(t, l, b, r);	
				puzzle[row][col].setRow(row);
				puzzle[row][col].setCol(col);
			}
		}
		
		priority = new int[(int)Math.pow(width, 2)];
		Pieces = new Piece[(int)Math.pow(width, 2)];
		ArrayList<Piece> a = new ArrayList<Piece> ();
		ArrayList<Integer> a2 = new ArrayList<Integer> ();
		int x;
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < width; j++) {
				if(i != 0 && j!= 0) {
					x = random.nextInt(a.size());
					a.add(x, puzzle[i][j]);
					a2.add(x, i*width + j);
				}
				else {
					a.add(puzzle[i][j]);
					a2.add(i*width + j);
				}
			}
		}
		for(int i = 0; i < a.size(); i++) {
			priority[i] = a2.get(i);
			Pieces[i] = a.get(i);
		}
		
		for(int i = 0; i < Pieces.length; i++) {
			Pieces[i].setX(random.nextInt(500 - (500/width)) + (500/width)/2);
		}
		for(int i = 0; i < Pieces.length; i++) {
			Pieces[i].setY(random.nextInt(500 - (500/width)) + (500/width)/2);
		}
		update();
	}
	enum mapType {
		t, r, l, b;
	}
	public int searchColumn(int col, int row, BufferedImage target, boolean fromSearch, boolean searchLeft) {	
		boolean contains = false;
		for(int i = row; i < target.getHeight(); i++) {
			try {
				if(target.getRGB(col, i) != 0) {
					contains = true;
				}
			}
			catch(Exception e) {}
		}
		if(!contains) {
			//System.out.println("Col: " + col + "   Row: "+ row);
			return col;
		}
		boolean blackOnRight = false, blackOnLeft = false;
		int initRow = row;
		while(target.getRGB(col,row) != -16777216) {
			if(map == mapType.t) {
				mapT[col][row] = 0;
			}
			else if(map == mapType.l) {
				mapL[col][row] = 0;
			}
			else if(map == mapType.b) {
				mapB[col][row] = 0;
			}
			else {
				mapR[col][row] = 0;
			}
			
			if(col != target.getWidth() - 1 && col != 0) {
				if(target.getRGB(col - 1, row) == -16777216)
					blackOnLeft = true;
				if(target.getRGB(col + 1, row) == -16777216)
					blackOnRight = true;
				if(!(target.getRGB(col + 1, row) == -16777216 || target.getRGB(col - 1, row) == -16777216) && row == initRow) {
					if(fromSearch) {
						if(searchLeft)
							searchColumn(col - 1, row, target, true, true);
						else
							searchColumn(col + 1, row, target, true, false);
					}
				}
				
				if(blackOnLeft && target.getRGB(col - 1, row) != -16777216) {
					searchColumn(col - 1, row, target, true, true);
					blackOnLeft = false;
				}
				if(blackOnRight && target.getRGB(col + 1, row) != -16777216) {
					searchColumn(col + 1, row, target, true, false);
					blackOnRight = false;
				}
			}
			row++;
		}
		
		return -1;
	}
	
	
	
}
	
	
	
	/*
	
	public void search(int x, int y, BufferedImage target, int depth) throws Exception{
		if(target.getRGB(x,y) == -16777216) {
			map[x][y] = 1;
			if(!(depth > 200)) {
				try 
				{
					if(target.getRGB(x+1,y) == -16777216 && map[x+1][y] == 2)
						search(x+1, y, target, depth + 1);
				} catch(Exception e) {}
	
				try 
				{
					if(target.getRGB(x,y+1) == -16777216 && map[x][y+1] == 2)
						search(x, y+1, target, depth + 1);
				} catch(Exception e) {}
	
				try 
				{
					if(target.getRGB(x-1,y) == -16777216 && map[x-1][y] == 2)
						search(x-1, y, target, depth + 1);
				} catch(Exception e) {}
	
				try 
				{
					if(target.getRGB(x-1,y) == -16777216 && map[x][y-1] == 2)
						search(x-1, y, target, depth + 1);
				} catch(Exception e) {}
			}
		}
		else if(target.getRGB(x,y) == 0) {
			map[x][y] = 0;
			if(!(depth > 200)) {
				if(map[x+1][y] == 2) {
					try { search(x+1, y, target, depth + 1); } 
					catch(Exception e) {}
				}
				if(map[x][y+1] == 2) {
					try { search(x, y+1, target, depth + 1); } 
					catch(Exception e) {}
				}
				if(map[x-1][y] == 2) {
					try { search(x-1, y, target, depth + 1); } 
					catch(Exception e) {}
				}
				if(map[x][y-1] == 2) {
					try { search(x, y-1, target, depth + 1); } 
					catch(Exception e) {}
				}
			}
		}
	}
}

/*if(row == 0) {
					t = ImageIO.read(new File("Edge-0.png"));
				} else {
					t = puzzle[row - 1][col].getBottom();
				}
				
				if(col == 0) {
					l = ImageIO.read(new File("Edge-0.png"));					
					l = op.filter(l, null);
				} else {
					l = puzzle[row][col - 1].getRight();
				}
				
				if(row == width - 1) {
					b = ImageIO.read(new File("Edge-0.png"));
				}
				else {
					b = ImageIO.read(new File("Edge-"+ (random.nextInt(edgeTypes)+1) + ".png"));
					if(random.nextBoolean()) {
						tx2 = AffineTransform.getScaleInstance(1, -1);
						tx2.translate(0, -b.getHeight(null));
						op2 = new AffineTransformOp(tx2, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
						b = op2.filter(b, null);
					}
				}
				
				if(col == width - 1) {
					r = ImageIO.read(new File("Edge-0.png"));
				}
				else {
					r = ImageIO.read(new File("Edge-"+ (random.nextInt(edgeTypes)+1) + ".png"));
					if(random.nextBoolean()) {
						tx2 = AffineTransform.getScaleInstance(1, -1);
						tx2.translate(0, -r.getHeight(null));
						op2 = new AffineTransformOp(tx2, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
						r = op2.filter(r, null);
					}
				}
				
				r = op.filter(r, null);
				
				
				construction = new BufferedImage(200,200, BufferedImage.TYPE_INT_ARGB);
				g2d = construction.createGraphics();
				
				t2 = t;
				l2 = l;
				b2 = b;
				r2 = r;
				g2d.drawImage(t2, 50, 0, 150, 100, 0, 0, 100, 100, obs);
				g2d.drawImage(l2, 0, 50, 100, 150, 0, 0, 100, 100, obs);
				g2d.drawImage(b2, 50, 100, 150, 200, 0, 0, 100, 100, obs);
				g2d.drawImage(r2, 100, 50, 200, 150, 0, 0, 100, 100, obs);
				construction2 = construction;
				construction3 = new BufferedImage((500/width)*2,(500/width)*2, BufferedImage.TYPE_INT_ARGB);
				g2d = construction3.createGraphics();
				g2d.drawImage(construction2, 0, 0, (500/width)*2,(500/width)*2, null);
				scaled = construction3;
				
				puzzle[row][col] = new Piece(scaled);
				puzzle[row][col].setEdges(t, l, b, r);
				*/
