import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.locks.ReentrantLock;

public class NeuralNet implements Comparable<NeuralNet> {
	public static double mutationChance = 0.99;
	public static double mutationHRange = 0.01; //Half range +- this much
	ReentrantLock lock = new ReentrantLock();
	
	public static int bankSize = 1800;
	static int rootIDs = 0;
	public static int numNodes[] = {10,4,4,2};
	public static ArrayList<NeuralNet> bank = new ArrayList<NeuralNet>();
	static {
		for(int i = 0;i < bankSize;i++) {
			bank.add(new NeuralNet(0));
		}
	}
	public static int herbBankSize = 100;
	public static ArrayList<NeuralNet> herbBank = new ArrayList<NeuralNet>();
	static {
		for(int i = 0;i < herbBankSize;i++) {
			herbBank.add(new NeuralNet(0));
		}
	}

	public int rootID = -1;
	public int lifespan = 0;
	private byte prevDir = 0;
	private double feedThrough = 0;
	
	private double weightsi1[][] = new double[numNodes[1]][numNodes[0]]; 
	private double weights12[][] = new double[numNodes[2]][numNodes[1]]; 
	private double weights2o[][] = new double[numNodes[2]][numNodes[1]]; 
	public double weights[][][] = {new double[1][1], weightsi1, weights12, weights2o};
	private double bias1[] = new double[numNodes[1]]; 
	private double bias2[] = new double[numNodes[2]]; 
	private double biaso[] = new double[numNodes[2]]; 
	public double bias[][] = {new double[1], bias1, bias2, biaso};
	
	public NeuralNet() {
		int iTake = Math.abs(d(bank.size()) - d(bank.size()/2) - d(bank.size()/2));
		rootID = bank.get(iTake).rootID;
		closeCopy(bank.get(iTake));
	}
	
	private NeuralNet(int a) {
		rootID = rootIDs++;
		RandomNN(a);
	}
	
	public NeuralNet(NeuralNet parent) {
		rootID = parent.rootID;
		closeCopy(parent);
	}
	
	private void closeCopy(NeuralNet parent) {
		if(Math.random() < 1.0/bank.size()) {
			rootID = rootIDs++;
			RandomNN(0);
			return;
		}
		for(int l = 1; l < bias.length;l++) {
			for(int n = 0; n < numNodes[l];n++) {
				bias[l][n] = parent.bias[l][n];
				for(int p = 0;p < weights[l][n].length;p++) {
					weights[l][n][p] = parent.weights[l][n][p];
				}
			}
		}
		if(Math.random() > mutationChance) {
			return;
		}
		for(int l = 1; l < bias.length;l++) {
			for(int n = 0; n < numNodes[l];n++) {
				if(Math.random() > mutationChance) {
					bias[l][n] += (Math.random() * (mutationHRange * 2)) - mutationHRange;
				}
				for(int p = 0;p < weights[l][n].length;p++) {
					if(Math.random() > mutationChance) {
						weights[l][n][p] += (Math.random() * (mutationHRange * 2)) - mutationHRange;
					}
				}
			}
		}
	}
	
	private void RandomNN(int a) {
		for(int l = 1; l < bias.length;l++) {
			for(int n = 0; n < numNodes[l];n++) {
				bias[l][n] = Math.random() - 0.5;
				for(int p = 0;p < weights[l][n].length;p++) {
					weights[l][n][p] = Math.random() - 0.5;
				}
			}
		}
		lifespan = a;
	}
	
	public NeuralNet(String s) {
		String[] stuff = s.trim().split("\\|");
		int point = 0;
		if(stuff.length < 10) {
			System.out.println("ERROR");
			return;
		}
		lifespan = Integer.parseInt(stuff[point++]);
		rootID = Integer.parseInt(stuff[point++]);
		for(int l = 1; l < bias.length;l++) {
			for(int n = 0; n < numNodes[l];n++) {
				bias[l][n] = Double.parseDouble(stuff[point++]);
				for(int p = 0;p < weights[l][n].length;p++) {
					//System.out.println(l + " " + n + " " + p + " " + stuff[point]);
					weights[l][n][p] = Double.parseDouble(stuff[point++]);
				}
			}
		}
	}
	
	public String toString() {
		String tr = lifespan + "|" + rootID;
		for(int l = 1; l < bias.length;l++) {
			for(int n = 0; n < numNodes[l];n++) {
				tr += "|" + bias[l][n];
				for(int p = 0;p < weights[l][n].length;p++) {
					tr += "|" + weights[l][n][p];
				}
			}
		}
		return tr;
	}

	public byte getDir(float x, float y, int size, float speed, Agent closest, Agent sClosest, Agent tClosest) {

		double valsi[] = {prevDir, feedThrough, x, y, size, speed, closest.x, closest.y, closest.speed, closest.size, sClosest.x, sClosest.y, sClosest.speed, sClosest.size, tClosest.x, tClosest.y, tClosest.speed, tClosest.size};
		double vals1[] = new double[numNodes[1]]; 
		double vals2[] = new double[numNodes[2]]; 
		double valso[] = new double[numNodes[2]];
		double vals[][] = {valsi,vals1,vals2,valso};
		
		for(int l = 1; l < bias.length;l++) {
			
			for(int n = 0; n < numNodes[l];n++) {
				double tempSum = bias[l][n];
				for(int p = 0;p < numNodes[l - 1];p++) {
					tempSum += weights[l][n][p] * vals[l - 1][p];
				}
				vals[l][n] = 1.0/(1.0 + Math.exp(-tempSum));
			}
		}
		byte tr = (byte) (valso[0] * 255);
		feedThrough = valso[1];
		prevDir = tr;
		return tr;
	}

	public byte getDir(float x, float y, int size, float speed, Agent closest) {
		double valsi[] = {prevDir, feedThrough, x, y, size, speed, closest.x, closest.y, closest.speed, closest.size};
		double vals1[] = new double[numNodes[1]]; 
		double vals2[] = new double[numNodes[2]]; 
		double valso[] = new double[numNodes[2]];
		double vals[][] = {valsi,vals1,vals2,valso};
		
		for(int l = 1; l < bias.length;l++) {
			
			for(int n = 0; n < numNodes[l];n++) {
				double tempSum = bias[l][n];
				for(int p = 0;p < numNodes[l - 1];p++) {
					tempSum += weights[l][n][p] * vals[l - 1][p];
				}
				vals[l][n] = 1.0/(1.0 + Math.exp(-tempSum));
			}
		}
		byte tr = (byte) (valso[0] * 255);
		feedThrough = valso[1];
		prevDir = tr;
		return tr;
	}

	public byte getDirHerb(float x, float y, int size, float speed, Thing closest, Thing sClosest, Thing tClosest) {

		double valsi[] = {prevDir, feedThrough, x, y, size, speed, closest.x, closest.y, closest.type, closest.size, sClosest.x, sClosest.y, sClosest.type, sClosest.size, tClosest.x, tClosest.y, tClosest.type, tClosest.size};
		double vals1[] = new double[numNodes[1]]; 
		double vals2[] = new double[numNodes[2]]; 
		double valso[] = new double[numNodes[2]];
		double vals[][] = {valsi,vals1,vals2,valso};
		
		for(int l = 1; l < bias.length;l++) {
			
			for(int n = 0; n < numNodes[l];n++) {
				double tempSum = bias[l][n];
				for(int p = 0;p < numNodes[l - 1];p++) {
					tempSum += weights[l][n][p] * vals[l - 1][p];
				}
				vals[l][n] = 1.0/(1.0 + Math.exp(-tempSum));
			}
		}
		byte tr = (byte) (valso[0] * 255);
		feedThrough = valso[1];
		prevDir = tr;
		return tr;
	}
	
	public void addToBank(int lifespan) {
		this.lifespan = lifespan;
		lock.lock();
		if(bank.get(bank.size() - 1).lifespan < lifespan) {
			System.out.println("ADDING TO BANK " + rootID);
			bank.remove(bank.size() - 1);
			bank.add(this);
			Collections.sort(bank);
		}
		lock.unlock();
	}

	public int compareTo(NeuralNet o) {
		return o.lifespan - lifespan;
	}
	
	private static int d(int m) {
		return (int) Math.floor(Math.random() * m);
	}
}
