import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

public class Food extends Thing {
	public int foodValue = (int) (foodGen * 3 + Math.random()*100)*foodDensity/10;
	public static int foodCount = 0;
	private static int foodGen = (int) (1/ActualSimulator.foodGrowth);
	private static int foodDensity = 50;
	private static double myLandFert = 0;
	
	public Food(float x, float y) {
		super(x, y, (byte) Math.random() * 5 + 3, Color.GREEN, 'F');
		//System.out.println(this.x + " " + this.y);
		if(x > ActualSimulator.xmax ){
			this.x -= ActualSimulator.xmax;
		}
		if ( y > ActualSimulator.ymax ){
			this.y -= ActualSimulator.ymax;
		}
		if ( x < 0 ){
			this.x += ActualSimulator.xmax;
		}
		if ( y < 0) {
			this.y += ActualSimulator.ymax;
		}
		if (x + 1 > ActualSimulator.xmax || y + 1 > ActualSimulator.ymax || x < 1 || y < 1) {
			dead = true;
			return;
		}
		//System.out.println(this.x + " " + this.y);
		myLandFert = ActualSimulator.landFertility[(int) (this.x/20)][(int) (this.y/20)] / 20.0;
		int lim = (int) Math.max(size, (foodDensity / myLandFert));
		for(Thing thing : ActualSimulator.things) {
			float d = dist(thing, 100);
			if(d < lim) {
				dead = true;
				return;
			}
		}
		lifespan = id % foodGen;
	}
	
	public Food(String s) {
		super(s);
		s = s.substring(s.indexOf(' ') + 1);
		String[] obj = s.split(" ");
		foodValue = Integer.parseInt(obj[0]);
		lifespan = (int) (id % foodGen);
		col = Color.GREEN;
	}
	
	public String toString() {
		return super.toString() + " " + foodValue;
	}
	
	public void paint(int x, int y, double scale, Graphics p) {
		int size = (int) (3*scale*this.size);
		if(size > 1) {
			p.setColor(col);
			p.fillOval(x - size/2, y - size/2, size, size);
		}
	}
	
	public void log() {
		
	}

	public void eat() {
		dead = true;
	}
	
	public float dist(Thing other, int maxD) {
		if(other.type == 'F' && (abs(xish - other.xish) > (foodDensity + 10) || abs(yish - other.yish) > (foodDensity + 10))) {
			return (foodDensity + 10);
		}
		return super.dist(other, maxD);
	}

	public void tick(ArrayList<Thing> allThings) {
		lifespan++;
		foodValue--;
		if(lifespan % foodGen == 0 ) {
			for(int i = 0;i < myLandFert;i++) {
				allThings.add(new Food(fuzzy(x, 200), fuzzy(y, 200)));
			}
			if(Agent.AGCount < 5 && Math.random() < ActualSimulator.foodGrowth) {
				allThings.add(new Herbivour(fuzzy(x, 200), fuzzy(y, 200), 5));
			}
		}
		
		if(foodValue < 50/myLandFert) {
			dead = true;
		}
	}

}
