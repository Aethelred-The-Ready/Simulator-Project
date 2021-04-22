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

public class ActualSimulator {

	private static boolean NNDisp = false;
	static JPanel j;
	public static ArrayList<Thing> things = new ArrayList<Thing>(20);
	public static ArrayList<Thing> prevTickThings = new ArrayList<Thing>(20);
	static ReentrantLock lock = new ReentrantLock();
	
	public static int numDumbAgents = 20;
	public static int numFood = 1000;
	public static double foodGrowth = 0.005;
	public static int foodLooked = 0;
	
	public static double simScale = 6;
	public static int maxIslandSize = 40;
	public static double islandFreq = 0.0003;
	public static int xmax = (int) (simScale * 1800), ymax = (int) (simScale * 1000);
	public static int[][] landFertility = new int[xmax/20][ymax/20];
	
	public static boolean zoomed = false;
	public static int zoom = 2;
	public static int xmid = xmax/2;
	public static int ymid = ymax/2;
	public static int[][] landFertilityMiniMap = new int[90][50];
	public static boolean slowed = false;
	public static float frameRate = 24;
	public static boolean paused = false;
	
	public static Thing clicked;
	
	public static boolean wantSave = false;
	public static boolean wantLoad = false;
	public static boolean wantClick = false;
	public static MouseEvent click = null;
	public static boolean was = false;
	
	public static double avgSpeed;
	public static double avgGen;
	public static double avgSize;
	public static double avgRad;
	public static double avgTick;
	public static ArrayList<Integer> graphFood = new ArrayList<Integer>();
	public static ArrayList<Integer> graphHerbs = new ArrayList<Integer>();
	public static ArrayList<Integer> graphCarns = new ArrayList<Integer>();
	
	public static long curTime;
	static KeyListener k = new KeyListener() {

		public void keyPressed(KeyEvent e) {
			was = paused;
			switch(e.getKeyCode()) {
			case KeyEvent.VK_Q :
				saveNN();
			break;
			case KeyEvent.VK_Z :
				zoomed = !zoomed;
			break;
			case KeyEvent.VK_X :
				slowed = !slowed;
			break;
			case KeyEvent.VK_C :
				clicked = null;
			break;
			case KeyEvent.VK_V :
				if(clicked != null) {
					xmid = clicked.xish;
					ymid = clicked.yish;
				}
			break;
			case KeyEvent.VK_W :
				ymid-=(simScale/zoom)*20;
			break;
			case KeyEvent.VK_S :
				ymid+=(simScale/zoom)*20;
			break;
			case KeyEvent.VK_A :
				xmid-=(simScale/zoom)*20;
			break;
			case KeyEvent.VK_D :
				xmid+=(simScale/zoom)*20;
			break;
			case KeyEvent.VK_R :
				zoom++;
			break;
			case KeyEvent.VK_F :
				zoom--;
				if(zoom == 0)
					zoom = 1;
			break;
			case KeyEvent.VK_T :
				if(frameRate > 2 && frameRate < 4) {
					frameRate = 4;
				}else if(frameRate <= 2) {
					frameRate *= 2;
				}else {
					frameRate += 2;
				}
			break;
			case KeyEvent.VK_G :
				if(frameRate <= 2) {
					frameRate /= 2;
				}else {
					frameRate -= 2;
				}
			break;
			case KeyEvent.VK_P :
				paused = !paused;
			break;
			case KeyEvent.VK_H :
				paused = true;
				wantSave = true;
			break;
			case KeyEvent.VK_Y :
				paused = true;
				wantLoad = true;
			break;
			case KeyEvent.VK_N :
				NNDisp = !NNDisp;
			break;
			}
			j.repaint();
		}

		public void keyReleased(KeyEvent e) {
			
		}

		public void keyTyped(KeyEvent e) {
			
		}
			
	};
	static MouseListener m = new MouseListener() {

		@Override
		public void mouseClicked(MouseEvent e) {
			was = paused;
			paused = true;
			wantClick = true;
			click = e;
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
			
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			
		}

			
	};
	
	static Runnable r = new Runnable() {

		public void run() {
			render();
			t.start();
		}
	};
	static ActionListener render = new ActionListener(){

		@Override
		public void actionPerformed(ActionEvent a) {
			j.repaint();
			t.start();
		}
		
		
	};
	static Timer t = new Timer(40,render);
	
	public static void main(String[] args) {
		r.run();
		for(int i = 0;i < landFertility.length;i++) {
			for(int k = 0;k < landFertility[i].length;k++) {
				landFertility[i][k] = 15;
			}
		}
		for(int i = 0;i < landFertility.length;i++) {
			for(int k = 0;k < landFertility[i].length;k++) {
				if(Math.random() < islandFreq) {
					addLandFert(i, k, 0);
				}
			}
		}
		
		avgLandFert();
		avgLandFert();
		
		avgLandFert();
		genMiniMap();
		
		for(int i = 0;i < numDumbAgents;i++) {
			//things.add(new Herbivour(fuzzy(xmax), fuzzy(ymax), 10));
		}
		for(int i = 0;i < numFood;i++) {
			things.add(new Food(fuzzy(xmax), fuzzy(ymax)));
		}
		//render();
		//load("start.txt");
		curTime = System.currentTimeMillis();
		clicked = things.get(0);
		
		int s = 0;
		while(true) {

			
			while(paused) {
				if(wantSave) {
					save("test.txt");
					wantSave = false;
					paused = was;
				}else if(wantLoad) {
					load("test.txt");
					wantLoad = false;
					paused = was;
				}else if(wantClick) {
					Thing me = new MouseThing(click);
					clicked = me.getClosest(things, 100);
					wantClick = false;
					paused = was;
				}
				Thread.yield();
			}
			

			
			if(Food.foodCount <= 0) {
				Food.foodCount = 0;
				things.add(new Food(fuzzy(xmax), fuzzy(ymax)));
			}
			for(int i = 0;i < 10;i++) {
				if(Math.random() < (0.01 * (simScale * simScale))) {
					things.add(new Food(fuzzy(xmax), fuzzy(ymax)));
				}
			}

			for(int i = things.size() - 1;i >= 0 ;i--) {
				things.get(i).tick(things);
				if(things.get(i).dead) {
					things.remove(i);
				}
			}
			//ThingRemover.removeDeads(things);
			
			for(int i = 0; i < things.size();i++) {
				prevTickThings.add(things.get(i).clone());
			}
			
			int herbCount = 0;
			int carnCount = 0;
			Agent.AGCount = 0;
			Food.foodCount = 0;
			for(Thing temp : things) {
				switch(temp.type) {
				case 'C':
					carnCount++;
					Agent.AGCount++;
					break;
				case 'H':
					herbCount++;
					Agent.AGCount++;
					break;
				case 'F':
					Food.foodCount++;
				}
			}

			int divs = (int) Math.max(1, simScale * simScale);
			ArrayList<Thread> threads = new ArrayList<Thread>(divs + 1);
			int prevStart = 0;
			while(true){
				int nextStart = Math.min(things.size(), prevStart + things.size()/divs);
				if(prevStart >= nextStart) {
					break;
				}
				Runnable task = new tickAgents(things.subList(prevStart, nextStart), prevTickThings);
				Thread worker = new Thread(task);
				worker.start();
				threads.add(worker);
				prevStart = nextStart;
			}
			
			@SuppressWarnings("unused")
			int loops = 0;
			int left = 0;
			do {
				Thread.yield();
				loops++;
				left = 0;
				for(Thread t : threads) {
					if(t.isAlive()) {
						left++;
					}
				}
			}while(left > 0);
			//System.out.println(loops);

			for(int i = 0; i < prevTickThings.size();i++) {
				if(prevTickThings.get(i).dead && things.size() > i) {
					things.get(i).dead = true;
				}
			}
			prevTickThings.clear();
			
			graphHerbs.add(herbCount);
			graphCarns.add(carnCount);
			graphFood.add(Food.foodCount);
			if(graphFood.size() > 1800) {
				graphFood.remove(0);
				graphHerbs.remove(0);
				graphCarns.remove(0);
			}
			
			if(slowed) {
				runAt(frameRate);
			}
			s++;
			if(s > 100000) {
				wantSave = true;
				save("autosave.txt");
				wantSave = false;
				s = 0;
			}
			
		}
	}
	
	private static void genMiniMap() {
		for(int i = 0;i < 90;i++) {
			for(int k = 0;k < 50;k++) {
				int tot = 0;
				int c = 1;
				for(int m = 0;m < simScale;m++) {
					for(int n = 0;n < simScale;n++) {
						tot+=landFertility[(int) (i*simScale + m)][(int) (k*simScale + n)];
						c++;
					}
				}
				landFertilityMiniMap[i][k] = (byte) (tot/c);
			}
		}
	}

	private static void runAt(float frame) {
		int rate = (int) (1000 / frame);
		try {
			long temp = System.currentTimeMillis();
			if((temp - curTime) < rate) {
				;
				j.repaint();
				;
				Thread.sleep(rate - (temp - curTime));
			}
			curTime = System.currentTimeMillis();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private static float fuzzy(float base, float range) {
		return (float) ((base - range) + (Math.random() * range * 2));
	}
	
	private static float fuzzy(float range) {
		return (float) (Math.random() * range);
	}
	
	private static void avgLandFert() {
		int[][] tempLF = new int[xmax/20][ymax/20];
		for(int i = 0;i < landFertility.length;i++) {
			for(int k = 0;k < landFertility[i].length;k++) {
				tempLF[i][k] = landFertility[i][k];
			}
		}
		
		for(int i = 0;i < landFertility.length;i++) {
			for(int k = 0;k < landFertility[i].length;k++) {
				int total = get(tempLF, i - 1, k - 1);
				total += get(tempLF, i - 1, k);
				total += get(tempLF, i - 1, k + 1);
				total += get(tempLF, i, k - 1);
				total += get(tempLF, i, k);
				total += get(tempLF, i, k + 1);
				total += get(tempLF, i + 1, k - 1);
				total += get(tempLF, i + 1, k);
				total += get(tempLF, i + 1, k + 1);
				landFertility[i][k] = (total / 9);
				if(landFertility[i][k] > 150) {
					landFertility[i][k] = 150;
				}else if(landFertility[i][k] < 0) {
					landFertility[i][k] = 0;
				}
			}
		}
	}
	
	private static void save(String filename) {
		System.out.println("SAVING TO " + filename);
		try {
			FileWriter fw = new FileWriter(filename);
			
			fw.write(foodGrowth + " " + simScale + "\n");

			for(int i = 0;i < landFertility.length;i++) {
				fw.write("" + landFertility[i][0]);
				for(int k = 1;k < landFertility[i].length;k++) {
					fw.write(" " + landFertility[i][k]);
				}
				fw.write("\n");
			}
			
			for(int i = 0;i < NeuralNet.bank.size();i++) {
				fw.write(NeuralNet.bank.get(i).toString() + "\n");
			}
			
			for(int i = 0;i < things.size();i++) {
				fw.write(things.get(i).toString() + "\n");
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	


	private static void saveNN() {
		String filename = "nn.txt";
		System.out.println("SAVING NN TO " + filename);
		try {
			FileWriter fw = new FileWriter(filename);
			
			for(int i = 0;i < NeuralNet.bank.size();i++) {
				fw.write(NeuralNet.bank.get(i).toString() + "\n");
			}
			
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		NNDisplay.main(new String[0]);
	}
	
	private static void load(String filename) {
		System.out.println("LOADING FROM " + filename);
		try {
			File f = new File(filename);
			Scanner fr = new Scanner(f);
			foodGrowth = fr.nextDouble();
			simScale = fr.nextDouble();

			xmax = (int) (simScale * 1800);
			ymax = (int) (simScale * 1000);
			landFertility = new int[xmax/20][ymax/20];
			
			xmid = xmax/2;
			ymid = ymax/2;
			landFertilityMiniMap = new int[90][50];
			
			for(int i = 0;i < landFertility.length;i++) {
				for(int k = 0;k < landFertility[i].length;k++) {
					landFertility[i][k] = fr.nextInt();
				}
			}
			genMiniMap();
			NeuralNet.bank.clear();
			for(int i = 0;i < NeuralNet.bankSize;i++) {
				NeuralNet.bank.add(new NeuralNet(fr.next()));
			}
				
			things.clear();
			prevTickThings.clear();
			
			while(fr.hasNext()) {
				String in = fr.nextLine();
				if(in.length() < 1) {
					continue;
				}
				switch (in.charAt(0)) {
				case 'F':
					things.add(new Food(in));
					break;
				case 'C':
					things.add(new Carnivour(in));
					break;
				case 'H':
					things.add(new Herbivour(in));
					break;
				}
			}
			
			fr.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		;
	}
	
	private static void addLandFert(int i, int k, int g) {
		set(landFertility, get(landFertility, i, k) + 40, i, k);
		if(get(landFertility, i, k) > 150) {
			set(landFertility, 150, i, k);
			return;
		}else if(get(landFertility, i, k) < 0) {
			set(landFertility, 0, i, k);
			return;
		}
		if(g > maxIslandSize) {
			return;
		}
		int rand = (int) (Math.random()*100);
		if(rand > 80) {
			return;
		}else if(rand > 60) {
			addLandFert(i, k + 1, g+1);
			addLandFert(i + 1, k, g+1);
		}else if(rand > 40) {
			addLandFert(i - 1, k, g+1);
			addLandFert(i, k + 1, g+1);
		}else if(rand > 20) {
			addLandFert(i, k - 1, g+1);
			addLandFert(i + 1, k, g+1);
		}else{
			addLandFert(i - 1, k, g+1);
			addLandFert(i, k - 1, g+1);
		}
	}

	
	private static void render(){
		JFrame frame = new JFrame("Cell simulator");
		
		j = new JPanel(){
			public void paint(Graphics p) {	
				
				if(wantLoad || wantSave) {
					return;
				}
				
				avgSpeed = 0;
				avgGen = 0;
				avgRad = 0;
				avgTick = 0;
				int aC = 0;
				
				int maxDispx = xmax;
				int maxDispy = ymax;
				
				int minDispx = 0;
				int minDispy = 0;
				
				double scale = 1/simScale;
				
				if(zoomed) {
					int xfield = (xmax / zoom);
					int yfield = (ymax / zoom);
					minDispx = xmid - xfield/2;
					minDispy = ymid - yfield/2;
					
					maxDispx = minDispx + xfield;
					maxDispy = minDispy + yfield;
					
					scale *= zoom;
					
					//System.out.println(minDispx + " " + minDispy + " " + xmid + " " + ymid + " " + maxDispx + " " + maxDispy + " " + xfield + " " + yfield + " " + zoom);
									
				}
				
				p.setColor(Color.WHITE);
				p.fillRect(0, 0, 2000, 2000);
				for(int i = 0;i < landFertility.length;i++) {
					if(zoomed && 20 * (i - 1) > maxDispx) {
						break;
					}
					for(int k = 0;k < landFertility[i].length;k++) {
						if(zoomed && 20 * (k - 1) > maxDispy) {
							break;
						}
						p.setColor(Color.BLACK);
						if(i % 2 == 0 ^ k % 2 == 0) {
							//p.drawString(i + " " + k, (int)(scale * 20 * i), (int)(scale * 20 * k));
						}
						if(landFertility[i][k] < 255 && landFertility[i][k] > 0) {
							p.setColor(new Color(0, 0, 0, landFertility[i][k]));
						} else {
							p.setColor(Color.RED);
						}
						p.fillRect((int)(scale * (20 * i - minDispx)), (int)(scale * (20 * k - minDispy)), (int)(scale * 20) + 1, (int)(scale * 20) + 1);
					}
				}
				

				;
				for(int i = 0;i < things.size();i++) {
					Thing curT = things.get(i);
					int x = (int) (curT.x);
					int y = (int) (curT.y);
					if(curT.dead || x < minDispx || y < minDispy || x > maxDispx + 200 || y > maxDispy + 200) {
						continue;
					}
					
					x -= minDispx;
					y -= minDispy;
					
					x *= scale;
					y *= scale;
					
					curT.paint(x, y, scale, p);
					curT.log();
					
					if(curT.type == 'H' || curT.type == 'C') {
						aC++;
					}

				}
				;

				p.setColor(Color.BLACK);
				ArrayList<Integer> tots = new ArrayList<Integer>();
				ArrayList<Integer> totA = new ArrayList<Integer>();
				ArrayList<Integer> rtID = new ArrayList<Integer>();
				if(NNDisp) {
					for(int i = 0;i < NeuralNet.rootIDs;i++) {
						int liveTot = 0;
						for(int k = 0;k < things.size();k++) {
							if(things.get(k) != null && things.get(k).type == 'C') {
								if(things.get(k) instanceof Carnivour && ((Carnivour) things.get(k)).nn.rootID == i) {
									liveTot++;
								}
							}
						}
						int tot = 0;
						for(int k = 0;k < NeuralNet.bank.size();k++) {
							if(NeuralNet.bank.get(k).rootID == i) {
								tot++;
							}
						}
						if(liveTot > 0 || tot > 0) {
							tots.add(tot);
							totA.add(liveTot);
							rtID.add(i);
						}
					}
					
					for(int i = 0;i < tots.size();i++) {
						p.setColor(Color.RED);
						p.fillRect(i * (1800/tots.size()),1000 - tots.get(i)*5 - totA.get(i)*5,(1800/tots.size()),totA.get(i)*5);
						p.setColor(Color.BLACK);
						p.fillRect(i * (1800/tots.size()),1000 - tots.get(i)*5,(1800/tots.size()),tots.get(i)*5);
						p.drawString(rtID.get(i) + "", i*(1800/tots.size()), 1010);
					}
				} else {
					p.setColor(new Color(0, 128, 0, 100));
					int curPos = 0;
					double numScale = 1/(simScale*simScale);
					for(int i = 0;i < graphFood.size();i++) {
						p.setColor(new Color(0, 128, 0, 100));
						int num = (int) (numScale * graphFood.get(i) * 1);
						p.fillRect(curPos, 1000 - num, 1800 / graphFood.size(), num);
						p.setColor(new Color(0, 0, 128, 100));
						num = (int) (numScale * graphHerbs.get(i)*8.0);
						p.fillRect(curPos, 1000 - num, 1800 / graphFood.size(), num);
						p.setColor(new Color(128, 0, 0, 100));
						int num2 = (int) (numScale * graphCarns.get(i)*8.0);
						p.fillRect(curPos, 1000 - num - num2, 1800 / graphFood.size(), num2);
						curPos += 1800 / graphFood.size();
					}
				}
				
				
				avgSpeed = avgSpeed/(aC * 1.0);
				avgRad = avgRad/(aC * 1.0);
				avgSize = avgSize/(aC * 1.0);
				avgTick = avgTick/(aC * 1.0);

				if(zoomed) {				
					p.setColor(Color.BLACK);	
					p.drawRect(1600, 50, 180, 100);
					for(int i = 0;i < 90;i++) {
						for(int k = 0;k < 50;k++) {
							if(landFertilityMiniMap[i][k] < 150 && landFertilityMiniMap[i][k] > 0) {
								p.setColor(new Color(0, 0, 0, landFertilityMiniMap[i][k]));
							}
							p.fillRect(1600 + i*2, 50 + k*2, 2, 2);
						}
					}
					p.setColor(Color.RED);
					p.drawRect(1600 + (180 * minDispx/xmax), 50 + (100 * minDispy/ymax), 180 / zoom, 100 / zoom);
				}
				
				p.setColor(Color.BLACK);
				if(zoomed) {
					p.drawString("Zoom: " + zoom, 200, 30);
				}
				if(slowed) {
					p.drawString("Frame Rate: " + frameRate, 200, 50);
				}
				if(paused) {
					p.drawString("PAUSED", 200, 70);
				}
				if(clicked != null) {
					p.drawString("Clicked Thing", 200, 90);
					p.drawString("ID:      " + clicked.id, 200, 110);
					p.drawString("(X, Y): (" + clicked.x + ", " + clicked.y + ")", 200, 130);
					p.drawString("Size:    " + clicked.size, 200, 150);
				}
				
				p.drawString(Math.round(avgSpeed * 100)/100.0 + " Speed", 10, 30);
				p.drawString(Math.round(avgGen * 100)/100.0 + " Gen", 10, 50);
				p.drawString(Math.round(avgRad * 100)/100.0 + " Rad", 10, 70);
				p.drawString(Math.round(avgSize * 100)/100.0 + " Size", 10, 90);
				p.drawString(Math.round(avgTick * 100)/100.0 + " TickCost", 10, 110);
				p.drawString(Math.round(DumbAgent.car * 100)/100.0 + " Car", 10, 130);
				p.drawString(Math.round(DumbAgent.veg * 100)/100.0 + " Veg", 10, 150);
				p.drawString(Math.round(aC * 100)/100.0 + " aC", 10, 170);
			}
		};
			
		frame.add(j);
		frame.addKeyListener(k);
		frame.addMouseListener(m);
		frame.setLocation(200,0);
		frame.setSize(1500, 1000);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	private static void set(int[] a, int v, int i) {
		while(i < 0) {
			i+=a.length;
		}
		a[i%a.length] = v;
	}
	private static void set(int[][] a, int v, int i, int k) {
		while(i < 0) {
			i+=a.length;
		}
		set(a[i%a.length], v, k);
	}
	
	private static int get(int[] a, int i) {
		while(i < 0) {
			i+=a.length;
		}
		return a[i%a.length];
	}
	private static int get(int[][] a, int i, int k) {
		while(i < 0) {
			i+=a.length;
		}
		return get(a[i%a.length], k);
	}
	
}

