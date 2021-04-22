import java.util.List;
import java.util.ArrayList;

public class tickAgents implements Runnable{
	List<Thing> agents;
	ArrayList<Thing> prevTickThings;
	public tickAgents(List<Thing> agents, ArrayList<Thing> prevTickThings) {
		this.agents = agents;
		this.prevTickThings = prevTickThings;
	}
	
	public void run() {
		for(int i = 0;i < agents.size();i++) {
			if(agents.get(i) instanceof Agent) {
				((Agent) agents.get(i)).move(prevTickThings);
			}
		}
	}
	
}