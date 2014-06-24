package org.wintrisstech.erik.iaroc;

/**************************************************************************
 * Super Happy version...ultrasonics working...Version 140512A...mods by Vic
 * Added compass class...works..updatged to adt bundle 20140321
 **************************************************************************/
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Random;

import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;

import org.wintrisstech.irobot.ioio.IRobotCreateAdapter;
import org.wintrisstech.irobot.ioio.IRobotCreateInterface;

import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * A Lada is an implementation of the IRobotCreateInterface, inspired by Vic's
 * awesome API. It is entirely event driven.
 * 
 * @author Erik Simplified "API" class by Phil version 140523A
 */
public class Lada extends IRobotCreateAdapter implements EventListener {
	private static final int DEGREE_ANGLE = 11;
	private static final int BLOCK = 73;
	private static final int SLIDY = 5;
	public final Dashboard dashboard;
	public UltraSonicSensors sonar;
	private Robot myRobot;
	public static Lada instance;
	public int x = 0;
	public int y = 4;
	public int dx = 1;
	public int dy = 0;
	public int[][] mapintYX = new int[9][15];
	public Button leftMap;
	public Button rightMap;
	public Button solveMap;

	/**
	 * Constructs a Lada, an amazing machine!
	 * 
	 * @param ioio
	 *            the IOIO instance that the Lada can use to communicate with
	 *            other peripherals such as sensors
	 * @param create
	 *            an implementation of an iRobot
	 * @param dashboard
	 *            the Dashboard instance that is connected to the Lada
	 * @throws ConnectionLostException
	 */
	public Lada(IOIO ioio, IRobotCreateInterface create, final Dashboard dashboard)
			throws ConnectionLostException {
		super(create);
		sonar = new UltraSonicSensors(ioio);
		this.dashboard = dashboard;
//		sayTheName();
		leftMap = (Button) this.dashboard.findViewById(R.id.leftHand);
		leftMap.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				try {
					Lada.instance.dashboard.speak("Mapping Left Hand Rule");
					mapMazeLeft();
				} catch (ConnectionLostException e) {} catch (InterruptedException e) {}
			}
			
		});
		rightMap = (Button) this.dashboard.findViewById(R.id.rightHand);
		rightMap.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				try {
					mapMazeRight();
				} catch (ConnectionLostException e) {
				} catch (InterruptedException e) {
				}
			}
			
		});
		
		instance = this;
	}

	private void sayTheName() {
		List<String> names = new ArrayList<String>();
		names.add("The Nerd Herd Robot");
		names.add("The Hash Tag No Moss Guy");
		names.add("Miss Moss");
		names.add("Fox News");
		names.add("Nyan Cat");
		names.add("Olaf");
		Random r = new Random();
		names.add("Prisoner Number "+Integer.toString(r.nextInt(1000)));
		String name = names.get(r.nextInt(names.size()));
		dashboard.speak("My name is "+name);
	}

	public void initialize() throws ConnectionLostException,
			InterruptedException {
		dashboard.log("iAndroid2014 happy version 140523A");
		myRobot = new Robot(dashboard, this);
		Lada.instance = this;
		myRobot.log("Ready!");
		
	}

	public void solveMaze() {
		dashboard.log(map());
		for(int i = 0; i < mapintYX.length; i++){
			for(int j = 0; j < mapintYX[i].length; j++){
				if(mapintYX[i][j] <= 0){
					mapintYX[i][j] = 9;
				}
			}
		}
	}
	private String map() {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < mapintYX.length; i++){
			for(int j = 0; j < mapintYX[i].length; j++){
				sb.append(mapintYX[i][j]);
			}
		}
		return sb.toString();
	}

	public void mapMazeLeft() throws ConnectionLostException, InterruptedException {
		boolean done = false;
		while (!done) {
			mapintYX[y][x] += 1;
			sonar.read();
			if(deadEnd()){
				if(Math.abs(sonar.getLeftDistance()-sonar.getRightDistance()) > SLIDY){
					if(sonar.getLeftDistance() > sonar.getRightDistance()){
						turnLeft();
						myRobot.goForward((sonar.getLeftDistance()-sonar.getRightDistance())/2);
						turnRight();
						moveFrontBack();
					} else {
						turnRight();
						myRobot.goForward((sonar.getRightDistance()-sonar.getLeftDistance())/2);
						turnLeft();
						moveFrontBack();
					}
				}
			}
			sonar.read();
			if (!isWallLeft()){
				turnLeft();
			} else if (isWallFront()){
				turnRight();
				if(isWallRight()){
					turnRight();
				}
			}
			x += dx;
			y += dy;
			myRobot.goForward(BLOCK);
			if (atEnd()) {
				mapintYX[y][x] += 1;
				done = false;
			}
		}
	}
	
	public void mapMazeRight() throws ConnectionLostException, InterruptedException {
		boolean done = false;
		while (!done) {
			mapintYX[y][x] += 1;
			sonar.read();
			if(deadEnd()){
				if(Math.abs(sonar.getLeftDistance()-sonar.getRightDistance()) > SLIDY){
					if(sonar.getLeftDistance() > sonar.getRightDistance()){
						turnLeft();
						myRobot.goForward((sonar.getLeftDistance()-sonar.getRightDistance())/2);
						turnRight();
						moveFrontBack();
					} else {
						turnRight();
						myRobot.goForward((sonar.getRightDistance()-sonar.getLeftDistance())/2);
						turnLeft();
						moveFrontBack();
					}
				}
			}
			sonar.read();
			if (!isWallRight()){
				turnRight();
			} else if (isWallFront()){
				turnLeft();
				if(isWallLeft()){
					turnLeft();
				}
			}
			x += dx;
			y += dy;
			myRobot.goForward(BLOCK);
			if (atEnd()) {
				mapintYX[y][x] += 1;
				done = false;
			}
		}
	}

	private void moveFrontBack() throws ConnectionLostException {
		if(sonar.getLeftDistance() > sonar.getFrontDistance()){
			myRobot.goForward(-(sonar.getLeftDistance()-sonar.getFrontDistance()));
		} else {
			myRobot.goForward(sonar.getLeftDistance()-sonar.getFrontDistance());
		}
	}

	private boolean atEnd() throws ConnectionLostException, InterruptedException {
		readSensors(SENSORS_INFRARED_BYTE);
		sonar.read();
		return isHomeBaseChargerAvailable() && deadEnd();
	}

	private boolean deadEnd() {
		return isWallLeft() && isWallRight() && isWallFront();
	}

	/**
	 * This method is called repeatedly
	 * 
	 * @throws ConnectionLostException
	 * @throws InterruptedException
	 */
	public void loop() throws ConnectionLostException, InterruptedException {
		
	}

	public void turn(int commandAngle) throws ConnectionLostException {
		int ls = commandAngle > 0 ? 220 : -220;
		int rs = -ls;
		driveDirect(rs, ls);
		SystemClock.sleep(DEGREE_ANGLE * Math.abs(commandAngle));
		driveDirect(0, 0);
	}
	public void turnRight() throws ConnectionLostException {
		turn(90);
		if (dx == 0 && dy == 1) {
			dx = 1;
			dy = 0;
		} else if (dx == 0 && dy == -1) {
			dx = -1;
			dy = 0;
		} else if (dx == 1 && dy == 0) {
			dx = 0;
			dy = -1;
		} else {
			dx = 0;
			dy = 1;
		}
		dashboard.log("right");
	}
	public void turnLeft() throws ConnectionLostException {
		turn(-90);
		if (dx == 0 && dy == 1) {
			dx = -1;
			dy = 0;
		} else if (dx == 0 && dy == -1) {
			dx = 1;
			dy = 0;
		} else if (dx == 1 && dy == 0) {
			dx = 0;
			dy = 1;
		} else {
			dx = 0;
			dy = -1;
		}
		dashboard.log("right");
	}

	public boolean isWallFront() {
		return sonar.getFrontDistance() < BLOCK;
	}
	public boolean isWallLeft() {
		return sonar.getLeftDistance() < BLOCK;
	}

	public boolean isWallRight() {
		return sonar.getRightDistance() < BLOCK;
	}
	public int readCompass() {
		return (int) (dashboard.getAzimuth() + 360) % 360;
	}
}
