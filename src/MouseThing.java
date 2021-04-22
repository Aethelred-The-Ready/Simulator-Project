import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class MouseThing extends Thing {

	public MouseThing(MouseEvent e) {
		super(e.getX(), e.getY(), 0, Color.BLACK, 'M');
	}

	@Override
	public void tick(ArrayList<Thing> allThings) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void eat() {
		// TODO Auto-generated method stub
		
	}
	
	public void log() {
		
	}
	
	public void paint(int x, int y, double scale, Graphics p) {
		
	}

}
