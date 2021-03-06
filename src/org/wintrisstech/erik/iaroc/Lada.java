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
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * A Lada is an implementation of the IRobotCreateInterface, inspired by Vic's
 * awesome API. It is entirely event driven.
 * 
 * @author Erik Simplified "API" class by Phil version 140523A
 */
public class Lada extends IRobotCreateAdapter implements EventListener {
	private static final int MAZE_SPEED = 250;
	private static final int CSD = 14;
	private static final int CFD = 18;
	/**
	 * Multiply by angle using//#noMoss
	 * 
	 * speed 225
	 */
	private static final double DEGREE_ANGLE = 10.6666D;
	/**
	 * Centimeters of a block
	 */
	private static final int BLOCK = 68;
	/**
	 * Olaf!
	 */
	private static final int MAX_SPEED = 500;
	private static final int CHANGE_SPEED = 415;
	private static final int SLIDY = 7;
	private static final int AZ_TOLERANCE = 10;
	public final Dashboard dashboard;
	public UltraSonicSensors sonar;
	private Robot myRobot;
	public static Lada instance;
	public int x = 15;
	public int y = 5;
	public int dx = 1;
	public int dy = 0;
	public int[][] mapintYX = new int[11][29];
	public Button leftMap;
	public Button rightMap;
	public Button solveMap;
	private int correctAz;
	public boolean mapped;
	public boolean killed;
	public ToggleButton killRun;
	private Button dragRace;
	private int ls = 400;
	private int rs = 400;
	private double currentAz;
	private double preferredAz;
	private Button goldRush;
	private int front;
	private int right;
	private int left;
	private Button turnLeft;
	private Button turnRight;
	public static final int BLOCK_TOLERANCE_HIGH = 20;
	public static final int BLOCK_TOLERANCE_LOW = 10;
	public static final int HARMONY_NUMBER = 15;
	private static final int SPEED = 200;
	protected static final int FRONT_TOLERANCE = 25;
	private static final int TOO_CLOSE = 5;
	private static final long TOL = 6L;
	public boolean dirLeft = true;
	private Button calibrate;
	private Button dr2;
	private Button dr3;
	private Button infrared;

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
	public Lada(IOIO ioio, IRobotCreateInterface create,
			final Dashboard dashboard) throws ConnectionLostException {
		super(create);
		sonar = new UltraSonicSensors(ioio);
		this.dashboard = dashboard;
		sayTheName();
		leftMap = (Button) this.dashboard.findViewById(R.id.leftHand);
		leftMap.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				try {
					Lada.instance.dashboard.speak("Mapping Left Hand Rule");
					mapMazeLeft();
				} catch (ConnectionLostException e) {
				} catch (InterruptedException e) {
				}
			}

		});
		rightMap = (Button) this.dashboard.findViewById(R.id.rightHand);
		rightMap.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				try {
					Lada.instance.dashboard.speak("Mapping Right Hand Rule");
					mapMazeRight();
				} catch (ConnectionLostException e) {
				} catch (InterruptedException e) {
				}
			}

		});
		solveMap = (Button) this.dashboard.findViewById(R.id.solveMaze);
		solveMap.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mapped) {
					Lada.instance.dashboard.speak("Solving Maze");
					solveMaze();
				} else {
					Toast.makeText(Lada.instance.dashboard, "Maze not Mapped",
							Toast.LENGTH_SHORT).show();
				}
			}

		});
		killRun = (ToggleButton) this.dashboard.findViewById(R.id.killProcess);
		killRun.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				killed = !killed;
			}

		});
		dragRace = (Button) this.dashboard.findViewById(R.id.dragRace);
		dragRace.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Thread t = new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							doDragRace();
						} catch (ConnectionLostException e) {
						} catch (InterruptedException e) {
						}
					}
				});
				t.start();
			}

		});
		goldRush = (Button) this.dashboard.findViewById(R.id.goldRush);
		goldRush.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				try {
					goldRush();
				} catch (ConnectionLostException e) {
				}
			}

		});
		turnLeft = (Button) this.dashboard.findViewById(R.id.turnLeft);
		turnLeft.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					turnLeft();
				} catch (ConnectionLostException e) {
				}
			}

		});
		turnRight = (Button) this.dashboard.findViewById(R.id.turnRight);
		turnRight.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					turnRight();
				} catch (ConnectionLostException e) {
				}
			}

		});
		calibrate = (Button) this.dashboard.findViewById(R.id.calibrate);
		calibrate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					calibrate();
				} catch (Exception e) {
				}
			}

		});
		dr2 = (Button) this.dashboard.findViewById(R.id.dragRace2);
		dr2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Thread t = new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							doDragRace2();
						} catch (Exception e) {
						}
					}

				});
				t.start();
			}

		});
		dr3 = (Button) this.dashboard.findViewById(R.id.dragRace3);
		dr3.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Thread t = new Thread(new Runnable() {

					@Override
					public void run() {
						doDragRace3();
					}

				});
				t.start();
			}

		});
		infrared = (Button) this.dashboard.findViewById(R.id.infrared);
		infrared.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				try {
					readSensors(SENSORS_INFRARED_BYTE);
				} catch (ConnectionLostException e) {
					dashboard.log(e.getLocalizedMessage());
				}
				dashboard.log(""+getInfraredByte());
			}
			
		});
		instance = this;
	}

	public void calibrate() throws ConnectionLostException,
			InterruptedException {
		sonar.read();
		int front = sonar.getFrontDistance();
		int left = sonar.getLeftDistance();
		int right = sonar.getRightDistance();
		/*
		 * if(right > left){ turnRight(); } else if (left > right){
		 * 
		 * } else { this.dashboard.log(""+(left+right)/2); }
		 */
		dashboard.log(left + " - " + front + " - " + right);
	}

	public void goldRush() throws ConnectionLostException {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					boolean done = false;
					boolean dirLeft = true;
					dashboard.log("start");
					while (getInfraredByte() == 255) {
						sonar.read();
						readSensors(SENSORS_BUMPS_AND_WHEEL_DROPS);
						readSensors(SENSORS_INFRARED_BYTE);
						front = getWallFront();
						right = getWallRight();
						left = getWallLeft();
						dashboard.log("F:" + front + " and R:" + right
								+ " and L:" + left);
						driveDirect(SPEED, SPEED);
						if (front <= FRONT_TOLERANCE || isBumpLeft()
								|| isBumpRight()) {
							fixFront(front);
							if (dirLeft) {
								dashboard.log("left");
								aroundLeft();
							} else {
								dashboard.log("right");
								aroundRight();
							}
						}
					}
				} catch (Exception e) {

				}
				try {
					dashboard.speak("Yahoo!");
					//dock();
				} catch (Exception e) { //ConnectionLostException e) {
					e.printStackTrace();
				}
			}
		});
		t.start();
	}

	// #noMoss
	// #noMoss
	// #noMoss
	// #noMoss
	// #noMoss
	// #noMoss
	// #noMoss
	// #noMoss
	// #noMoss
	// #noMoss
	// #noMoss
	// #noMoss
	// #noMoss
	// #noMoss
	// #noMoss
	// #noMoss
	// #noMoss
	// #noMoss
	// #noMoss
	// #noMoss
	// #noMoss
	// #noMoss
	// #noMoss
	// #noMoss
	// #noMoss
	// #noMoss
	// #noMoss
	// #noMoss
	// #noMoss
	// #noMoss
	// #noMoss
	// #noMoss
	// #noMoss
	// #noMoss
	// #noMoss
	// #noMoss
	// #noMoss
	// #noMoss
	// #noMoss
	// #noMoss
	// #noMoss
	// #noMoss
	// #noMoss
	// #noMoss

	public void dock() throws ConnectionLostException, InterruptedException {
		if (atEnd()){
			driveDirect(425, 425);
		}
	}

	public void fixFront(int front) throws ConnectionLostException {
		myRobot.goBackward(TOO_CLOSE - front);
	}

	public void aroundLeft() throws ConnectionLostException,
			InterruptedException {
		dirLeft = false;
		turnLeft();
		while (right <= FRONT_TOLERANCE) {
			sonar.read();
			right = getWallRight();
			driveDirect(SPEED, SPEED);
			if (front <= FRONT_TOLERANCE || isBumpLeft() || isBumpRight()) {
				turnLeft();
				driveDirect(SPEED, SPEED);
				while (left <= FRONT_TOLERANCE) {
					sonar.read();
					left = getWallLeft();
				}
				dashboard.speak("YO LOW!");
				myRobot.goForward(60);
				turnRight();
				myRobot.goForward(60);
				turnRight();
			}
		}
		myRobot.goForward(13);
		turnRight();
	}

	public void aroundRight() throws ConnectionLostException,
			InterruptedException {
		dirLeft = true;
		turnRight();
		while (left <= FRONT_TOLERANCE) {
			sonar.read();
			sonar.read();
			left = getWallLeft();
			driveDirect(SPEED, SPEED);
			if (front <= FRONT_TOLERANCE || isBumpLeft() || isBumpRight()) {
				dashboard.log("furthurer");
				turnRight();
				driveDirect(SPEED, SPEED);
				while (right <= FRONT_TOLERANCE) {
					sonar.read();
					right = getWallRight();
				}
				dashboard.speak("YO LOW!");
				myRobot.goForward(60);
				turnLeft();
				myRobot.goForward(60);
				turnLeft();
			}
		}
		myRobot.goForward(13);
		turnLeft();
	}

	public void doDragRace() throws ConnectionLostException,
			InterruptedException {
		while (true && !killed) {
			driveDirect(this.rs, this.ls);
			straightenDrag();
		}
		driveDirect(0, 0);
	}

	public void doDragRace2() throws ConnectionLostException,
			InterruptedException {
		while (true && !killed) {
			moveBlock(MAX_SPEED);
		}
	}

	public void doDragRace3() {
		try {
			readSensors(SENSORS_BUMPS_AND_WHEEL_DROPS);
			while (!killed && !(isBumpLeft() || isBumpRight())) {
				sonar.read();
				int distLeft1 = sonar.getLeftDistance();
				int distRight1 = sonar.getRightDistance();
				int totalDistance = 0;
				readSensors(Lada.SENSORS_GROUP_ID6);
				driveDirect(MAX_SPEED, MAX_SPEED);
				while (totalDistance < 100 * 10) {
					readSensors(Lada.SENSORS_GROUP_ID6);
					int dd = getDistance();
					totalDistance += dd;
					// log("" + totalDistance / 10 + " cm");
				}
				sonar.read();
				int distLeft2 = sonar.getLeftDistance();
				int distRight2 = sonar.getRightDistance();
				double leftAngle = 0.0D;
				double rightAngle = 0.0D;
				dashboard.log("Drive: " + totalDistance);
				if (distLeft1 < BLOCK && distLeft2 < BLOCK) {
					try {
						dashboard.log(distLeft1 + ", " + distLeft2);
						leftAngle = Math
								.toDegrees(Math
										.asin(((double) (distLeft1 - distLeft2) / (double) totalDistance)));
						dashboard.log("LA: " + leftAngle);
					} catch (ArithmeticException ae) {

					}
				}
				if (distRight1 < BLOCK && distRight2 < BLOCK) {
					try {
						dashboard.log(distRight1 + ", " + distRight2);
						rightAngle = Math
								.toDegrees(Math
										.asin(((double) (distRight2 - distRight1) / (double) totalDistance)));
						dashboard.log("RA: " + rightAngle);
					} catch (ArithmeticException ae) {

					}
				}
				double correctAngle = 0.0D;
				if (Math.round(leftAngle) != 0 && Math.round(rightAngle) == 0) {
					correctAngle = leftAngle;
				} else if (Math.round(rightAngle) != 0
						&& Math.round(leftAngle) == 0) {
					correctAngle = rightAngle;
				} else if (Math.round(rightAngle) != 0
						&& Math.round(leftAngle) != 0) {
					correctAngle = (leftAngle + rightAngle) / 2;
				}
				readSensors(Lada.SENSORS_GROUP_ID6);
				while (Math.abs(Math.round(correctAngle)) > 0) {
					dashboard.log("CA: " + correctAngle);
					if (correctAngle > 0) {
						ls = MAX_SPEED;
						rs = MAX_SPEED - 50;
						dashboard.log("---------------RIGHT!--------------");
					} else {
						ls = MAX_SPEED - 50;
						rs = MAX_SPEED;
						dashboard.log("---------------LEFT!--------------");
					}
					driveDirect(rs, ls);
					readSensors(Lada.SENSORS_GROUP_ID6);
					int angle = getAngle();
					correctAngle += angle;
				}
				readSensors(SENSORS_BUMPS_AND_WHEEL_DROPS);
			}
		} catch (Exception e) {
		}
	}

	public void straightenDrag() throws ConnectionLostException,
			InterruptedException {
		sonar.read();
		int left = sonar.getLeftDistance();
		int right = sonar.getRightDistance();
		if (Math.abs(left - right) > SLIDY && Math.abs(left - right) < 30) {
			if (left > right) {
				this.ls = CHANGE_SPEED;
				this.rs = MAX_SPEED;
			}
			if (right > left) {
				this.ls = MAX_SPEED;
				this.rs = CHANGE_SPEED;
			}
		} else if (!(Math.abs(left - right) > SLIDY)) {
			this.rs = MAX_SPEED;
			this.ls = MAX_SPEED;
		}
		dashboard.log("L: " + sonar.getLeftDistance() + " R:"
				+ sonar.getRightDistance());
		dashboard.log("Left: " + ls + ". Right: " + rs + ".");
		dashboard.log("Dif: " + Math.abs(left - right));
	}

	private void sayTheName() {
		List<String> names = new ArrayList<String>();
		names.add("The Nerd Herd Robot");
		names.add("The Hash Tag No Moss Guy");
		names.add("Miss Moss");
		names.add("Fox News");
		names.add("Nyan Cat");
		names.add("Olaf");
		names.add("The Roomba");
		Random r = new Random();
		names.add("Prisoner Number " + Integer.toString(r.nextInt(1000)));
		String name = names.get(r.nextInt(names.size()));
		dashboard.speak("My name is " + name);
	}

	public void initialize() throws ConnectionLostException,
			InterruptedException {
		dashboard.log("iAndroid2014 happy version 140523A");
		myRobot = new Robot(dashboard, this);
		Lada.instance = this;
		myRobot.log("Ready!");

	}

	public void solveMaze() {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				dashboard.log(map());
				y = 5;
				x = 15;
				dx = 1;
				dy = 0;
				for (int i = 0; i < mapintYX.length; i++) {
					for (int j = 0; j < mapintYX[i].length; j++) {
						if (mapintYX[i][j] <= 0) {
							mapintYX[i][j] = 9;
						}
					}
				}
				try {
					while (!atEnd()) {
						int north = mapintYX[y + 1][x];
						int east = mapintYX[y][x + 1];
						int south = mapintYX[y - 1][x];
						int west = mapintYX[y][x - 1];
						int dir = 0;
						if (dx == 0 && dy == 1) {
							dir = 0;
							south = 9;
						} else if (dx == 1 && dy == 0) {
							dir = 1;
							west = 9;
						} else if (dx == 0 && dy == -1) {
							dir = 2;
							north = 9;
						} else {
							dir = 3;
							east = 9;
						}
						int least = Math.min(west,
								Math.min(south, Math.min(north, east)));
						if (least == north) {
							switch (dir) {
							case 0:

								break;
							case 1:
								turnLeft();
								break;
							case 2:

								break;
							case 3:
								turnRight();
								break;
							}
						} else if (least == south) {
							switch (dir) {
							case 0:

								break;
							case 1:
								turnRight();
								break;
							case 2:

								break;
							case 3:
								turnLeft();
								break;
							}
						} else if (least == west) {
							switch (dir) {
							case 0:
								turnRight();
								break;
							case 1:

								break;
							case 2:
								turnLeft();
								break;
							case 3:

								break;
							}
						} else {
							switch (dir) {
							case 0:
								turnRight();
								break;
							case 1:

								break;
							case 2:
								turnLeft();
								break;
							case 3:

								break;
							}
						}
						moveBlock();
						/*
						 * while(true && !killed){
						 * dashboard.log(""+readCompass()+""); Object lock = new
						 * Object(); synchronized(lock){ try { lock.wait(500); }
						 * catch (InterruptedException e) {} } }
						 */
					}
					demo(DEMO_COVER_AND_DOCK);
				} catch (Exception e) {
				}
			}

		});
		t.start();

	}

	private String map() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < mapintYX.length; i++) {
			for (int j = 0; j < mapintYX[i].length; j++) {
				sb.append(mapintYX[i][j]);
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	public double startAz = 0;

	public void move(int centimeters) throws ConnectionLostException {
		int totalDistance = 0;
		readSensors(Lada.SENSORS_GROUP_ID6);
		int go = centimeters > 0 ? 25 : -25;
		while (totalDistance < Math.abs(centimeters) * 10) {
			driveDirect(go, go);
			readSensors(Lada.SENSORS_GROUP_ID6);
			int dd = getDistance();
			totalDistance += Math.abs(dd);
		}
	}

	public void mapMazeLeft() throws ConnectionLostException,
			InterruptedException {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					boolean done = false;
					while (!done && !killed) {
						mapintYX[y][x] += 1;
						// straighten();
						sonar.read();
						if (isWallFront()) {
							move(getWallFront() - CFD);
						}
						sonar.read();
						if (!isWallLeft()) {
							turnLeft();
							if (isWallRight()) {
								move(CSD - getWallRight());
							}
						} else if (isWallFront()) {
							turnRight();
							if (isWallLeft()) {
								move(CSD - getWallLeft());
							}
							if (isWallRight()) {
								turnRight();
							}
						}
						x += dx;
						y += dy;
						moveBlock();
						// fixPosition();
						if (atEnd()) {
							mapintYX[y][x] += 1;
							done = true;
						}
					}
					mapped = true;
				} catch (Exception e) {

				}
				try {
					demo(DEMO_COVER_AND_DOCK);
				} catch (ConnectionLostException e) {
				}
				win();
			}

		});
		t.start();
	}

	private void straighten() throws ConnectionLostException {

		currentAz = readCompass();
		double diff = currentAz - preferredAz;
		dashboard.log(diff + "\t" + currentAz + "\t" + preferredAz);
		while (Math.abs(diff) >= AZ_TOLERANCE) {
			if (diff > 0) {
				driveDirect(30, -30);
			} else {
				driveDirect(-30, 30);
			}
			currentAz = readCompass();
			diff = currentAz - preferredAz;
		}
		dashboard.log(diff + "-" + currentAz + "-" + preferredAz);
		driveDirect(0, 0);

	}

	private int dif() {
		return Math.abs(sonar.getLeftDistance() - sonar.getRightDistance());
	}

	public void mapMazeRight() throws ConnectionLostException,
			InterruptedException {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					startAz = readCompass();
					preferredAz = startAz;
					boolean done = false;
					while (!done && !killed) {
						// straighten();
						mapintYX[y][x] += 1;
						sonar.read();
						if (isWallFront()) {
							move(getWallFront() - CFD);
						}
						if (!isWallRight()) {
							turnRight();
							if (isWallLeft()) {
								move(CSD - getWallLeft());
							}
						} else if (isWallFront()) {
							turnLeft();
							if (isWallRight()) {
								move(CSD - getWallRight());
							}
							if (isWallLeft()) {
								turnLeft();
							}
						}
						x += dx;
						y += dy;
						moveBlock();
						// fixPosition();
						if (atEnd()) {
							mapintYX[y][x] += 1;
							done = false;
						}
					}
					mapped = true;
				} catch (Exception e) {

				}
				try {
					move(40);
				} catch (Exception e) {
				}
				win();
			}

		});
		t.start();
	}

	protected void win() {
		dashboard.speak("I WIN!");
	}

	private void moveFrontBack() throws ConnectionLostException {
		if (sonar.getLeftDistance() > sonar.getFrontDistance()) {
			myRobot.goForward(-(sonar.getLeftDistance() - sonar
					.getFrontDistance()));
		} else {
			myRobot.goForward(sonar.getLeftDistance()
					- sonar.getFrontDistance());
		}
	}

	private boolean atEnd() throws ConnectionLostException,
			InterruptedException {
		sonar.read();
		dashboard.log("Beacon Signal: " + isFieldFound());
		return isFieldFound(); // && deadEnd();
	}

	public boolean isFieldFound() throws ConnectionLostException {
		readSensors(SENSORS_INFRARED_BYTE);
		return (getInfraredByte() & 2) != 0;
	}
	public boolean isRedField() throws ConnectionLostException{
		readSensors(SENSORS_INFRARED_BYTE);
		return (getInfraredByte() & 8) != 0;
	}
	
	public boolean isGreenField() throws ConnectionLostException{
		readSensors(SENSORS_INFRARED_BYTE);
		return (getInfraredByte() & 4) != 0;
	}
	
	public boolean isBothFields() throws ConnectionLostException{
		readSensors(SENSORS_INFRARED_BYTE);
		return isGreenField() && isRedField();
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

	public void turn(int commandAngle, int speed)
			throws ConnectionLostException {
		dashboard.log(commandAngle + ", " + speed);
		int ls = commandAngle > 0 ? speed : -speed;
		int rs = -ls;
		readSensors(Lada.SENSORS_GROUP_ID6);
		driveDirect(rs, ls);
		SystemClock.sleep(Math.round(DEGREE_ANGLE
				* (double) Math.abs(commandAngle)
				* ((double) 225 / (double) speed)));
		driveDirect(0, 0);
		readSensors(Lada.SENSORS_GROUP_ID6);
		int angle = getAngle();
		dashboard.log("" + angle);

		commandAngle += angle;
		/**
		 * if (commandAngle > 1) { turn(commandAngle, 100); }
		 */

		/*
		 * int diffAngle = commandAngle + angle; dashboard.log(""+diffAngle);
		 * int go = commandAngle > 0 ? -speed : speed; driveDirect(go, -go);
		 * while (Math.abs(commandAngle) > 0) {
		 * readSensors(Lada.SENSORS_GROUP_ID6); int newAngle = getAngle();
		 * commandAngle += newAngle; dashboard.log(newAngle+" "+commandAngle); }
		 */
	}

	public void turnRight() throws ConnectionLostException {
		turn(90, 225);
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
	}

	public void turnLeft() throws ConnectionLostException {
		turn(-90, 225);
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

	public int getWallFront() {
		return sonar.getFrontDistance();
	}

	public int getWallLeft() {
		return sonar.getLeftDistance();
	}

	public int getWallRight() {
		return sonar.getRightDistance();
	}

	public double readCompass() {
		return (dashboard.getAzimuth() + 360) % 360;
	}

	public void fixPosition() throws ConnectionLostException,
			InterruptedException {
		int front = getWallFront();
		int right = getWallRight();
		int left = getWallLeft();

		if (front <= BLOCK_TOLERANCE_LOW || front >= BLOCK_TOLERANCE_HIGH) {
			myRobot.goForward(front - HARMONY_NUMBER);
		}

		if (right <= BLOCK_TOLERANCE_LOW || right >= BLOCK_TOLERANCE_HIGH) {
			turnLeft();
			myRobot.goForward(HARMONY_NUMBER - right);
			turnRight();
		}

		if (left <= BLOCK_TOLERANCE_LOW || left >= BLOCK_TOLERANCE_HIGH) {
			turnRight();
			myRobot.goForward(HARMONY_NUMBER - left);
			turnLeft();
		}
	}

	public int goForward(int centimeters, int speed)
			throws ConnectionLostException {
		int totalDistance = 0;
		readSensors(Lada.SENSORS_GROUP_ID6);
		driveDirect(speed, speed);
		while (totalDistance < centimeters * 10) {
			readSensors(Lada.SENSORS_GROUP_ID6);
			int dd = getDistance();
			totalDistance += dd;
			// log("" + totalDistance / 10 + " cm");
		}
		driveDirect(0, 0);
		return totalDistance / 10;
	}

	public void moveBlock() throws ConnectionLostException,
			InterruptedException {
		moveBlock(MAZE_SPEED);
	}

	public void moveBlock(int speed) throws ConnectionLostException,
			InterruptedException {
		sonar.read();
		int distLeft1 = sonar.getLeftDistance();
		int distRight1 = sonar.getRightDistance();
		int distDrive = goForward(BLOCK, speed);
		sonar.read();
		int distLeft2 = sonar.getLeftDistance();
		int distRight2 = sonar.getRightDistance();
		double leftAngle = 0.0D;
		double rightAngle = 0.0D;
		dashboard.log("Drive: " + distDrive);
		if (distLeft1 < BLOCK && distLeft2 < BLOCK) {
			try {
				dashboard.log(distLeft1 + ", " + distLeft2);
				leftAngle = Math
						.toDegrees(Math
								.atan(((double) (distLeft1 - distLeft2) / (double) distDrive)));
				dashboard.log("LA: " + leftAngle);
			} catch (ArithmeticException ae) {

			}
		}
		if (distRight1 < BLOCK && distRight2 < BLOCK) {
			try {
				dashboard.log(distRight1 + ", " + distRight2);
				rightAngle = Math
						.toDegrees(Math
								.atan(((double) (distRight2 - distRight1) / (double) distDrive)));
				dashboard.log("RA: " + rightAngle);
			} catch (ArithmeticException ae) {

			}
		}
		double correctAngle = 0.0D;
		if (Math.round(leftAngle) != 0 && Math.round(rightAngle) == 0) {
			correctAngle = leftAngle;
		} else if (Math.round(rightAngle) != 0 && Math.round(leftAngle) == 0) {
			correctAngle = rightAngle;
		} else if (Math.round(rightAngle) != 0 && Math.round(leftAngle) != 0) {
			correctAngle = (leftAngle + rightAngle) / 2;
		}
		if (Math.round(correctAngle) < TOL) {
			dashboard.log("CA: " + correctAngle);
			turn((int) Math.round(correctAngle), 100);
		}
		sonar.read();
		if (!inCorner()) {
			if (isWallLeft() && getWallLeft() < CSD - TOL) {
				turnRight();
				move(CSD - getWallLeft());
				turnLeft();
			} else if (isWallRight() && getWallRight() < CSD - TOL) {
				turnLeft();
				move(CSD - getWallRight());
				turnRight();
			} else if (isWallLeft() && getWallLeft() > CSD + TOL) {
				turnRight();
				move(CSD - getWallLeft());
				turnLeft();
			} else if (isWallRight() && getWallRight() > CSD + TOL) {
				turnLeft();
				move(CSD - getWallRight());
				turnRight();
			}
		}
	}

	private boolean inCorner() {
		return isWallSide()&&isWallFront();
	}

	private boolean isWallSide() {
		return isWallLeft() || isWallRight();
	}

}
