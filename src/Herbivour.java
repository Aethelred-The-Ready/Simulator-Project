import java.awt.Color;
import java.util.ArrayList;

public class Herbivour extends DumbAgent{

	public Herbivour(float x, float y, int size) {
		super(x, y, size);
		tickCost *= 0.5;
	}
	
	public Herbivour(DumbAgent parent) {
		super(parent);
		tickCost *= 0.5;
	}
	
	public Herbivour(String s) {
		super(s);
		col = Color.BLUE;
	}


	public void move(ArrayList<Thing> allThings) {
		if(dead) {
			return;
		}
		if(digesting != 0) {
			digesting--;
			return;
		}
		float shortestDistance = (float) senseRadius;
		Thing target = null;
		boolean tooClose = false;
		for(Thing obj : allThings) {
			float tempD = dist(obj, (int) shortestDistance);
			if(obj.id == id || tempD > shortestDistance) {
				continue;
			}
			if(lifespan < 50 && obj.type == 'H') {
				if(tempD < speed + size) {
					tooClose = true;
					target = ((Agent)obj);
					shortestDistance = speed + size;
				}
			}else if(obj.type == 'C') {
				if(tempD < speed * 4 + size) {
					tooClose = true;
					target = ((Agent)obj);
					shortestDistance = 0;
				}
			}else if(obj.type == 'F') {
				if(obj.size < size && Math.random() > 0.01) {
					target = (Food) obj;
					shortestDistance = tempD;
				}
			}
		}
		if(target == null) {
			moveRandom();
			return;
		}
		
		if(!tooClose) {
			moveCloser(target);
			if(shortestDistance < speed * 2 + size) {
				veg++;
				nutrition += ((Food) target).foodValue;
				//nutrition = Math.min(nutrition, size*size*size*40);
				digesting += 4;
				target.eat();
			}
		} else {
			moveAway(target);
		}
		
	}

	
	public void reproduce(ArrayList<Thing> allThings) {
		if(Math.random() < 0.95) {
			allThings.add(new Herbivour(this));
		}else{
			allThings.add(new Carnivour(this));
		}
		digesting+=20;
	}

}
