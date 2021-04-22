import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

public abstract class Agent extends Thing implements Cloneable{
	public byte face;
	public float speed;
	public float nutrition = 100;
	public double senseRadius = 200;
	public float tickCost;
	protected byte skewrate = 10;
	protected float curSpeed = speed;
	protected byte digesting = 0;
	public static int AGCount = 0;
	public int reprTrigMult = 250;
	public int gen = 0;
	
	Agent(float x, float y, int size, Color c, float speedBase, char type){
		super(x, y, size, c, type);
		this.face = (byte)(Math.random() * 255 - 127);
		speed = (float) (speedBase - 2 + Math.random() * 4);
		if(speed < 0.1) {
			speed = (float) 0.1;
		}
		if(senseRadius < 0) {
			senseRadius = 0;
		}else if(senseRadius > 400) {
			senseRadius = 400;
		}
	}
	
	Agent(String s){
		super(s);
		s = s.substring(s.indexOf(' ') + 1);
		String[] obj = s.split(" ");
		face = Byte.parseByte(obj[0]);
		speed = Float.parseFloat(obj[1]);
		nutrition = Float.parseFloat(obj[2]);
		senseRadius = Double.parseDouble(obj[3]);
		curSpeed = Float.parseFloat(obj[4]);
		digesting = Byte.parseByte(obj[5]);
		reprTrigMult = Integer.parseInt(obj[6]);
		gen = Integer.parseInt(obj[7]);
	}
	
	public String toString() {
		return super.toString() + " " + face + " " + speed + " " + nutrition + " " + senseRadius + " " + curSpeed + " " + digesting + " " + reprTrigMult + " " + gen;
	}
	

	public Agent clone() {
		Agent a = (Agent) super.clone();
		
		a.speed = speed;
		a.nutrition = nutrition;
		
		return a;
	}
	
	public void paint(int x, int y, double scale, Graphics p) {
		int dSize = (int) ((2*scale*this.size));
		p.setColor(col);
		p.fillOval(x - dSize/2, y - dSize/2, dSize, dSize);
		int barHeight = ((int)(dSize * (nutrition) / (reprTrigMult*tickCost)));
		p.fillRect(x + (dSize*2)/4, y + dSize/2 - barHeight, dSize/2, barHeight);
	}
	
	public void log() {
		ActualSimulator.avgSpeed += speed;
		if(gen > ActualSimulator.avgGen) {
			ActualSimulator.avgGen = gen;
		}
		ActualSimulator.avgSize += size;
		ActualSimulator.avgRad += senseRadius;
		ActualSimulator.avgTick += tickCost;
	}
	
	public abstract void move(ArrayList<Thing> allThings);
	
	protected void moveRandom() {
		face = (byte) (face - (skewrate/2) + Math.random() * skewrate + 1);
		double dir = Math.PI * face/127.0;
		move(speed * Math.sin(dir), speed * Math.cos(dir));
	}
	
	protected void moveDir(byte targDir) {
		float tSpeed = speed; 
		byte faceTarget = (byte) ((targDir / Math.PI)*127);
		if(inRange(face, skewrate, faceTarget)){
			if(curSpeed < speed) {
				curSpeed *= 1.1;
			}
		}else{
			curSpeed *= 0.8;
			if(faceTarget > face){
	            if(faceTarget - face > 127){
	            	face -= skewrate;
	            }else{
	            	face += skewrate;
	            }
	        }else{
	            if(face - faceTarget >= 127){
	            	face += skewrate;
	            }else{
	            	face -= skewrate;
	            }
	        }
		}
		double dir = Math.PI * face/127.0;
		move(tSpeed * Math.sin(dir), tSpeed * Math.cos(dir));
	}
	
	protected void moveCloser(Thing target) {
		double dir = Math.atan((x - target.x)/(y - target.y));
		if(target.y < y) {
			dir += Math.PI;
		}
		if(dir < 0) {
			dir += Math.PI * 2.0;
		}
		float tSpeed = speed; 
		byte faceTarget = (byte) ((dir / Math.PI)*127);
		if(inRange(face, skewrate, faceTarget)){
			if(curSpeed < speed) {
				curSpeed *= 1.1;
			}
		}else{
			curSpeed *= 0.8;
			if(faceTarget > face){
	            if(faceTarget - face > 127){
	            	face -= skewrate;
	            }else{
	            	face += skewrate;
	            }
	        }else{
	            if(face - faceTarget >= 127){
	            	face += skewrate;
	            }else{
	            	face -= skewrate;
	            }
	        }
		}
		dir = Math.PI * face/127.0;
		move(tSpeed * Math.sin(dir), tSpeed * Math.cos(dir));
	}
	
	protected void moveAway(Thing target) {
		double dir = Math.atan((x - target.x)/(y - target.y));
		if(target.y < y) {
			dir += Math.PI;
		}
		if(dir < 0) {
			dir += Math.PI * 2.0;
		}
		dir += Math.PI;
		float tSpeed = speed; 
		byte faceTarget = (byte) ((dir / Math.PI)*127);
		if(inRange(face, skewrate, faceTarget)){
			if(curSpeed < speed) {
				curSpeed *= 1.1;
			}
		}else{
			curSpeed *= 0.8;
			if(faceTarget > face){
	            if(faceTarget - face > 127){
	            	face -= skewrate;
	            }else{
	            	face += skewrate;
	            }
	        }else{
	            if(face - faceTarget >= 127){
	            	face += skewrate;
	            }else{
	            	face -= skewrate;
	            }
	        }
		}
		dir = Math.PI * face/127.0;
		move(tSpeed * Math.sin(dir), tSpeed * Math.cos(dir));
	}
	
	private void move(double dx, double dy) {
		x += dx;
		y += dy;
		if(x >= ActualSimulator.xmax) {
			x -= ActualSimulator.xmax;
		}else if(x < 0) {
			x += ActualSimulator.xmax;
		}
		xish = (int)x;
		
		if(y >= ActualSimulator.ymax) {
			y -= ActualSimulator.ymax;
		}else if(y < 0) {
			y += ActualSimulator.ymax;
		}
		yish = (int)y;
	}
	
	protected boolean inRange(byte t, byte r, byte v) {
		return (t + r > v && t - r < v);
	}
}
