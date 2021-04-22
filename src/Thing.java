import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

public abstract class Thing implements Cloneable {
	public float x;
	protected int xish;
	public float y;
	protected int yish;
	public int size;
	public Color col;
	public short id;
	public char type;
	public int lifespan = 0;
	boolean dead = false;
	
	private static short idVar;
	
	Thing(float x, float y, int size, Color c, char t){
		this.x = x;
		xish = (int) x;
		this.y = y;
		yish = (int) y;
		this.size = size;
		col = c;
		id = idVar++;
		type = t;
	}
	
	Thing(String s){
		type = s.charAt(0);
		size = Integer.parseInt(s.substring(1, s.indexOf('(')));
		x = Float.parseFloat(s.substring(s.indexOf('(') + 1, s.indexOf(',')));
		y = Float.parseFloat(s.substring(s.indexOf(',') + 1, s.indexOf(')')));
		if(s.indexOf(' ') != -1) {
			id = Short.parseShort(s.substring(s.indexOf(')') + 1, s.indexOf(' ')));
		}else {
			id = Short.parseShort(s.substring(s.indexOf(')') + 1, s.length()));
		}
		xish = (int) x;
		yish = (int) y;
	}

	public String toString() {
		return type + "" + size +"(" + x + "," + y + ")" + id;
	}
	
	public Thing clone(){
		
		Thing t;
		try {
			t = (Thing)super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
		
		t.x = x;
		t.y = y;
		t.xish = xish;
		t.yish = yish;
		t.size = size;
		t.col = col;
		t.id = id;
		t.type = type;
		
		return t;
	}
	
	public abstract void paint(int x, int y, double scale, Graphics p);
	public abstract void log();
	
	protected float dist(Thing other, int maxD) {
		int oxish = other.xish;
		int oyish = other.yish;
		if(maxD - xish > ActualSimulator.xmax - oxish) {
			oxish -= ActualSimulator.xmax;
		}else if(ActualSimulator.xmax - xish < maxD - oxish) {
			oxish += ActualSimulator.xmax;
		}
		if(maxD - yish > ActualSimulator.ymax - oyish) {
			oyish -= ActualSimulator.ymax;
		}else if(ActualSimulator.ymax - yish < maxD - oyish) {
			oyish += ActualSimulator.ymax;
		}
		
		if(abs(xish - oxish) > maxD || abs(yish - oyish) > maxD) {
			return maxD + 1;
		}
		return (float) Math.sqrt(sqr(x - other.x) + sqr(y - other.y));
	}
	
	public Thing getClosest(ArrayList<Thing> things, float MaxDist) {

		float shortestDistance = (float) (200);
		Thing target = null;
		for(Thing obj : things) {
			float tempD = dist(obj, (int) shortestDistance);
			if(tempD < shortestDistance) {
				target = obj;
				shortestDistance = tempD;
			}
		}
		
		return target;
	}
	
	protected int abs(int a) {
		if(a < 0) {return -a;}return a;
	}
	
	protected float sqr(float x) {
		return x * x;
	}

	protected static float fuzzy(float base, float range) {
		return (float) ((base - range) + (Math.random() * range * 2));
	}
	
	public abstract void tick(ArrayList<Thing> allThings);
	
	protected boolean inRange(float t, float r, float v) {
		return (t + r > v && t - r < v);
	}
	
	protected abstract void eat();
}
