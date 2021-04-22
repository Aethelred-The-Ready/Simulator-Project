import java.awt.Color;
import java.util.ArrayList;

public class Carnivour extends DumbAgent{
	private int parentId = 0;
	public NeuralNet nn;
	ArrayList<Agent> closest = new ArrayList<Agent>();
	ArrayList<Float> distans = new ArrayList<Float>();
	
	public Carnivour(DumbAgent parent) {
		super(parent);
		if(parent.type == 'C') {
			nn = new NeuralNet(((Carnivour) parent).nn);
		}else {
			nn = new NeuralNet();
		}
		parentId = parent.id;
		type = 'C';
		nutrition = 500;
		skewrate = 20;
		col = Color.RED;
		if(senseRadius < 0) {
			senseRadius = 0;
		}else if(senseRadius > 400) {
			senseRadius = 400;
		}
	}
	
	public Carnivour clone() {
		Carnivour c = (Carnivour) super.clone();
		c.parentId = parentId;
		return c;
	}
	
	public Carnivour(float x, float y, int size) {
		super(x, y, size);
		type = 'C';
		skewrate = 20;
		nutrition = 500;
		col = Color.RED;
		if(senseRadius < 0) {
			senseRadius = 0;
		}else if(senseRadius > 400) {
			senseRadius = 400;
		}
	}
	
	public Carnivour(String s) {
		super(s);
		s = s.substring(s.indexOf(' ')) + 1;
		String[] obj = s.split(" ");
		parentId = Integer.parseInt(obj[8]);
		col = Color.RED;
		nn = new NeuralNet(obj[9]);
	}
	
	public String toString() {
		return super.toString() + " " + parentId + " " + nn.toString();
	}

	public void move(ArrayList<Thing> allThings) {
		if(dead) {
			return;
		}
		if(digesting != 0) {
			digesting--;
			return;
		}
		float shortestDistance = (float) (2*senseRadius);
		Thing target = null;
		boolean tooClose = false;
		
		
		for(Thing obj : allThings) {
			float tempD = dist(obj, (int) shortestDistance);
			if(obj.id == id || tempD > shortestDistance) {
				continue;
			}
			if(obj.type == 'C' || obj.type == 'H') {
				Agent ca = ((Agent)obj);
				//if(ca.parentId != parentId && ca.id != parentId && ca.parentId != id && ca.speed < this.speed - 2 && ca.size < this.size) {
					target = obj;
					shortestDistance = tempD;
					addToClosest((Agent)obj, tempD);
				//}//else if(tempD < speed * 4 + size) {
				//	tooClose = true;
				//	target = ca;
				//	shortestDistance = speed * 4 + size;
				//}
			}else if(obj.type == 'H') {
				DumbAgent ag = ((DumbAgent)obj);
				if(ag.speed < this.speed - 2 && ag.size < this.size + 1) {
					target = obj;
					shortestDistance = tempD;
				}
			}
		}
		if(target == null) {
			moveRandom();
			return;
		}
		while(closest.size() < 3) {
			closest.add(new DumbyAgent());
		}
		//byte dir = nn.getDir(x, y, size, speed, closest.get(0), closest.get(1), closest.get(2));
		byte dir = nn.getDir(x, y, size, speed, (Agent) target);
		//System.out.println(nn.getDir(x, y, size, speed, closest.get(0), closest.get(1), closest.get(2)));
		closest.clear();
		distans.clear();
		if(shortestDistance < speed * 2 + size && target.size < this.size) {
			car++;
			nutrition += ((Agent)target).nutrition / 2 + target.size * target.size;
			target.eat();
		}
		moveDir(dir);
		if(1 == 1) {
			return;
		}
		if(!tooClose) {
			moveCloser(target);
			if(shortestDistance < speed * 2 + size) {
				car++;
				nutrition += ((Agent)target).nutrition / 2 + target.size * target.size;
				target.eat();
			}
		} else {
			moveAway(target);
		}
		
	}

	public void tick(ArrayList<Thing> allThings) {
		lifespan++;
		if(digesting == 0 && lifespan > 50) {
			nutrition -= tickCost/2.0;
			if(nutrition >= reprTrigMult*tickCost) {
				nutrition = reprTrigMult*tickCost - size * size * size;
				reproduce(allThings);
			}
		} else {
			nutrition -= tickCost/4.0;
		}
		if(nutrition <= 0) {
			nn.addToBank(lifespan);
			dead = true;
		}
	}

	public void reproduce(ArrayList<Thing> allThings) {
		if(Math.random() < 0.9) {
			allThings.add(new Carnivour(this));
		}
		digesting+=20;
	}
	
	public void eat() {
		nn.addToBank(lifespan);
		dead = true;
	}
	
	private void addToClosest(Agent a, Float d) {
		if(closest.size() < 3) {
			closest.add(a);
			distans.add(d);
			return;
		}
		if(distans.get(0) < distans.get(1) && distans.get(0) < distans.get(2)) {
			closest.set(0, a);
			distans.set(0, d);
		} else if(distans.get(1) < distans.get(2)) {
			closest.set(1, a);
			distans.set(1, d);
		} else {
			closest.set(2, a);
			distans.set(2, d);
		}
		
	}

}
