package org.usfirst.frc.team4206.robot;
import edu.wpi.first.wpilibj.SampleRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.buttons.JoystickButton;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.Compressor;

import org.usfirst.frc.team4206.robot.Ticks;

public class Robot extends SampleRobot {
	// Create used variables
	Joystick controller;
	JoystickButton gearShift, toggleCompressor, slow;
	CANTalon frontLeft, frontRight, rearLeft, rearRight, frontArm;
	RobotDrive drive;
	DoubleSolenoid shift, piston;
	Encoder leftEncoder, rightEncoder;
	DigitalInput trunkSwitch;
	Compressor compressor;
	Ticks ticks;
	//CameraServer server;
	
	/*
	public Robot() {
		robotInit();
	}
	*/
    public void robotInit() {
    	// Constructor instantiates all necessary classes
    	
    	// Constructs Ticks() class for better measurement of tick-based time used in speed detection
    	ticks = new Ticks();
    	/*
    	server = CameraServer.getInstance();
    	server.setQuality(50);
    	server.startAutomaticCapture("cam0");
    	*/
    	// Constructs all user-input devices
    	controller = new Joystick(0);
    	gearShift = new JoystickButton(controller, 1);
    	toggleCompressor = new JoystickButton(controller, 2);
    	slow = new JoystickButton(controller, 3);
    	trunkSwitch = new DigitalInput(4);

    	// Constructs motor controllers
    	frontRight = new CANTalon(1);
    	frontLeft = new CANTalon(2);
    	rearLeft = new CANTalon(3);
    	rearRight = new CANTalon(4);
    	frontArm = new CANTalon(5);
    	
    	// Constructs pneumatic elements
    	shift = new DoubleSolenoid(0,1);
    	piston = new DoubleSolenoid(2,3);
    	shift.set(DoubleSolenoid.Value.kReverse);
        piston.set(DoubleSolenoid.Value.kReverse);
        compressor = new Compressor();
        
        // Constructs encoders and declares their settings
    	leftEncoder = new Encoder(0, 1);
    	rightEncoder = new Encoder(2, 3);
    	/*
    	leftEncoder.setMaxPeriod(.1);
    	leftEncoder.setMinRate(10);
    	leftEncoder.setDistancePerPulse(((Math.PI*7.8)/12.0)/256.0);
    	leftEncoder.setReverseDirection(true);
    	leftEncoder.setSamplesToAverage(1);
    	rightEncoder.setMaxPeriod(.1);
    	rightEncoder.setMinRate(10);
    	rightEncoder.setDistancePerPulse(((Math.PI*7.8)/12.0)/256.0);
        rightEncoder.setReverseDirection(true);
        rightEncoder.setSamplesToAverage(1);
        */

        // Constructs drive class and declares its settings
    	drive = new RobotDrive(rearLeft, frontLeft, rearRight, frontRight);
    	drive.setSafetyEnabled(true);
    	drive.setExpiration(0.1);
        drive.setSensitivity(0.5);
        drive.setMaxOutput(1.0);
        drive.tankDrive(0, 0);
    }
    
    // Easy-access methods that print to the driver station console using various input data-types
    public void ds_notify(String msg){
    	System.out.println(msg);
    }
    
    public void ds_notify(double msg){
    	System.out.println(Double.toString(msg));
    }
    
    public void ds_notify(int msg){
    	System.out.println(Integer.toString(msg));
    }
    
    // Operator control method that is called when the robot enters teleop
    public void operatorControl() {
    	// Necessary variables created and assigned
    	drive.tankDrive(0, 0);
    	boolean isClosing = false, isSlow = false, gearIsHigh = false, shifted = false, slowed = false;
    	double speed = 0.0, move = 0.0, turn = 0.0;
    	int armDir = 0;
    	// While loop that refreshes driver inputs and commands for devices, currently active only when
    	// the robot is enabled, in competition this should be both that and active when operator control
    	while (isEnabled()) {
    		long time = System.currentTimeMillis();
    		if (isSlow) {
    			move = -controller.getY() * 0.65;
    			turn = -controller.getX() * 0.75;
    		} else {
    			move = -controller.getY();
    			turn = -controller.getX();
    		}
    		
    		ticks.iterateTrunk();
    		ticks.iterateTick(leftEncoder, rightEncoder);
    		speed = ticks.getSpeed();
    		/*
    		if (speed > 4.0 | speed < -4.0) {
    			gearIsHigh = true;
    		}
    		*/
    		
    		if (controller.getPOV() >= 135 & controller.getPOV() <= 225) armDir = 1;
    		else if ((controller.getPOV() >= 315 | controller.getPOV() <= 45) & controller.getPOV() != -1) armDir = -1;
    		else armDir = 0;
    		
    		frontArm.set(armDir);
    		
    		if (gearShift.get()) {
    			if (!shifted) {
    				gearIsHigh = !gearIsHigh;
    				shifted = true;
    			}
    		} else shifted = false;
    		/*
    		if (slow.get()) {
    			if (!slowed) {
    				isSlow = !isSlow;
    				slowed = true;
    			}
    		} else slowed = false;
    		*/
    		if (gearIsHigh) shift.set(DoubleSolenoid.Value.kForward);
    		else shift.set(DoubleSolenoid.Value.kReverse);
    		
    		if (toggleCompressor.get()) compressor.start();
    		else compressor.stop();
    		/*
    		if (!trunkSwitch.get() | ticks.get("trunk")) {
    			if (!ticks.get("trunk")) ds_notify("Opening trunk!");
    			ticks.set("trunk", true);
    			piston.set(DoubleSolenoid.Value.kReverse);
    			if (ticks.trunkTick % 500 == 0 | isClosing) {
    				if (!isClosing) {
    					ds_notify("Trunk closing!");
    					ticks.set("trunk", 1);
    				}
    				isClosing = true;
    				piston.set(DoubleSolenoid.Value.kForward);
    				if (ticks.trunkTick % 300 == 0) {
    					ds_notify("Trunk closed!");
    					piston.set(DoubleSolenoid.Value.kReverse);
    					isClosing = false;
    					ticks.set("trunk", 1);
    					ticks.set("trunk", false);
    				}
    			}
    		}
    		*/
    		drive.arcadeDrive(move, turn);
    		
    		// This block finds the difference between when calculations began and when they ended. It then determines if the 1 microsecond period has passed
    		// and, if so, chooses to skip waiting again in order to execute the next iteration, saving time. If it finds that it completed calculations EARLY, 
    		// then it determines how much longer it has to wait in order to finish at the same microsecond time (0.01 - time taken) and waits that period before 
    		// iterating.
    		//
    		// This is important because some methods rely on precise time-keeping based on iteration and Timer.delay() does not keep time, it only waits and
    		// does not take the time taken prior to its calling into account.
    		if ((System.currentTimeMillis() - time)/1000 <= 0.01) {
    			Timer.delay(0.01 - (System.currentTimeMillis() - time)/1000);
    		}
    	}
    }
    
    public void disabled() {
    	drive.tankDrive(0,0);
    	ds_notify("Robot disabled."); // This simply overrides the default disabled message so it doesn't complain about us using it.
    }
}