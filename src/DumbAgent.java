import java.awt.Color;
import java.util.ArrayList;

public abstract class DumbAgent extends Agent implements Cloneable {
	public static int reprTrig = 5000;
	public static int veg = 0;
	public static int car = 0;

	private void calcTick() {
		tickCost = (float) (cub((float) (size))*0.002 + sqr(speed)*0.3 + senseRadius * 0.1) + 1;
		if(tickCost < 1) {
			tickCost = 1;
		}
	}
	
	public DumbAgent(float x, float y, int size) {
		super(x, y, (byte) size, Color.BLUE, 3, 'H');
		nutrition = 1000;
		calcTick();
	}
	
	public DumbAgent(DumbAgent parent) {
		super(fuzzy(parent.x, 40), fuzzy(parent.y, 40), parent.size, Color.BLUE, parent.speed, 'H');
		senseRadius = (short) (parent.senseRadius - 50 + Math.random() * 100);
		if(senseRadius < 0) {
			senseRadius = 0;
		}else if(senseRadius > 400) {
			senseRadius = 400;
		}
		size = (byte) fuzzy(parent.size, 2);
		reprTrigMult = (int) fuzzy(parent.reprTrigMult, 2);
		if(size < 1) {
			size = 1;
		}else if(size > 30) {
			size = 30;
		}
		gen = parent.gen + 1;
		calcTick();
	}

	public DumbAgent(String s) {
		super(s);
		calcTick();
	}

	public DumbAgent clone() {
		DumbAgent da = (DumbAgent) super.clone();
		return da;
	}
	
	public void tick(ArrayList<Thing> allThings) {
		lifespan++;
		if(digesting == 0 && lifespan > 50) {
			nutrition -= tickCost;
			if(nutrition >= reprTrigMult * tickCost) {
				nutrition = reprTrigMult * tickCost - size * size * size;
				reproduce(allThings);
			}
		}else {
			nutrition -= tickCost/2;
		}
		if(nutrition <= 0) {
			dead = true;
		}
	}
	
	public abstract void reproduce(ArrayList<Thing> allThings);
	
	private float cub(float f) {
		return f*f*f;
	}
	
	protected void eat() {
		dead = true;
	}
	
}
