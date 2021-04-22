import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class NNDisplay {

	static JPanel j;
	
	public static int currentNNNum = 0;
	public static NeuralNet currentNN;
	
	public static long curTime;
	static KeyListener k = new KeyListener() {

		public void keyPressed(KeyEvent e) {
			switch(e.getKeyCode()) {
			case KeyEvent.VK_A :
				currentNNNum--;
			break;
			case KeyEvent.VK_D :
				currentNNNum++;
			break;
			case KeyEvent.VK_R :
				load("nn.txt");
			break;
			}
			if(currentNNNum < 0) {
				currentNNNum += NeuralNet.bankSize;
			}else if(currentNNNum >= NeuralNet.bankSize) {
				currentNNNum -= NeuralNet.bankSize;
			}
			currentNN = NeuralNet.bank.get(currentNNNum);
			j.repaint();
		}

		public void keyReleased(KeyEvent e) {
			
		}

		public void keyTyped(KeyEvent e) {
			
		}
			
	};
	
	public static void main(String[] args) {
		currentNN = new NeuralNet();
		load("nn.txt");
		currentNN = NeuralNet.bank.get(currentNNNum);
		render();
	}
	
	private static void load(String filename) {
		System.out.println("LOADING FROM " + filename);
		try {
			File f = new File(filename);
			Scanner fr = new Scanner(f);
			NeuralNet.bank.clear();
			for(int i = 0;i < NeuralNet.bankSize;i++) {
				NeuralNet.bank.add(new NeuralNet(fr.next()));
			}
				
			fr.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		;
	}
	
	private static void render(){
		JFrame frame = new JFrame("Neural Net");
		
		j = new JPanel(){
			public void paint(Graphics p) {	
				
				p.setColor(Color.WHITE);
				p.fillRect(0, 0, 2000, 2000);
				p.setColor(Color.BLACK);
				
				int size = 50;
				int xgap = 80;
				int ygap = 300;
				int ixgap = 100;

				for(int l = 0; l < NeuralNet.numNodes.length;l++) {
					int lxgap = (20 - NeuralNet.numNodes[l])/2 * xgap;
					for(int n = 0; n < NeuralNet.numNodes[l];n++) {
						p.setColor(Color.BLACK);
						p.drawOval(n * xgap + lxgap + ixgap, l * ygap, size, size);
						p.drawString(n + "", n * xgap + lxgap + ixgap + size/2 - 5, l * ygap + size/2 + 3);
						if(l == NeuralNet.numNodes.length - 1) {
							continue;
						}
						int nxgap = (20 - NeuralNet.numNodes[l + 1])/2 * xgap;
						for(int on = 0;on < NeuralNet.numNodes[l + 1];on++) {
							p.setColor(getCol(currentNN.weights[l + 1][on][n]));
							p.drawLine(n * xgap + lxgap + ixgap + (size/2), l * ygap + size, on * xgap + nxgap + ixgap + (size/2), ((l + 1) * ygap));
						}
						
					}
				}
				
				p.setColor(Color.BLACK);
				ArrayList<Integer> tots = new ArrayList<Integer>();
				ArrayList<Integer> rtID = new ArrayList<Integer>();
				for(int i = 0;i < NeuralNet.rootIDs;i++) {
					int tot = 0;
					for(int k = 0;k < NeuralNet.bank.size();k++) {
						if(NeuralNet.bank.get(k).rootID == i) {
							tot++;
						}
					}
					if(tot > 0) {
						tots.add(tot);
						rtID.add(i);
					}
				}
				
				for(int i = 0;i < tots.size();i++) {
					p.setColor(Color.BLACK);
					p.fillRect(i * (1800/tots.size()),1000 - tots.get(i)*5,(1800/tots.size()),tots.get(i)*5);
					p.drawString(rtID.get(i) + "", i*(1800/tots.size()), 1010);
				}
				
				p.drawString("Number  : " + currentNNNum + "", 10, 30);
				p.drawString("Lifespan: " + currentNN.lifespan, 10, 50);
				p.drawString("RootID  : " + currentNN.rootID + "", 10, 70);
			}
		};
			
		frame.add(j);
		frame.addKeyListener(k);
		frame.setLocation(200,0);
		frame.setSize(1500, 1000);
		frame.setVisible(true);
	}
	
	private static Color getCol(double v) {
		if(v > 1) {
			v = 0.99;
		}else if(v < -1) {
			v = -0.99;
		}
		if(v > 0) {
			return new Color(0,(int) (v*255),0,(int) ((v)*255));
		}else if(v < 0) {
			return new Color((int) ((-v)*255),0,0,(int) ((-v)*255));
		}
		return new Color(255,255,255,0);
	}
	
}

