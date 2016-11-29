package org.usfirst.frc.team4206.robot;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Timer;

public class Ticks
{
	int tick, trunkTick, safeTick, stopTick;
	double speed;
	boolean trunkToggle;
	NumberFormat formatter;
	
    public Ticks()    
    {
    	tick = 1;
    	stopTick = 1;
    	trunkTick = 1;
    	safeTick = 1;
    	speed = 0.0;
    	trunkToggle = false;
    	formatter = new DecimalFormat("#0.00");
    }
    
    public void stopThread(double time) {
    	Timer.delay(time);
    }
    
    public void ds_notify(String msg){
    	System.out.println(msg);
    }
    
    public void ds_notify(double msg){
    	System.out.println(Double.toString(msg));
    }
    
    public void ds_notify(int msg){
    	System.out.println(Integer.toString(msg));
    }
    
    public void iterateStop() {
    	stopTick += 1;
    	if (stopTick % 500 == 0) {
    		stopTick = 1;
    	}
    }
    
    public void iterateTick(Encoder left, Encoder right) {
    	tick += 1;
		// Factored out the lengthy and clunky equation that determined speed
    	speed = 0.001 * ((int) 24.9265 * (left.get() + right.get()));
    	if (tick % 50 == 0) {
			if (speed != 0) ds_notify("Speed: " + formatter.format(speed) + " feet per second.");
			tick = 1;
		}
    	left.reset();
    	right.reset();
    }
    
    public void iterateTrunk() {
    	if (trunkToggle) trunkTick += 1;
    }
    
    public boolean get(String var) {
    	return trunkToggle;	
    }
    
    public void set(String var, boolean set) {
    	trunkToggle = set;
    }
    
    public void set(String var, int set) {
    	if (var == "trunk") {
    		trunkTick = set;
    	} else if (var == "tick") {
    		tick = set;
    	}
    }
    
    public void reset() {
    	speed = 0.0;
    }
    
    public double getSpeed() {
    	return speed;
    }
}