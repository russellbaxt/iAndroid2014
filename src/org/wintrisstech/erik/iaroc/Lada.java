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
	/**
	 * Multiply by angle using speed 225
	 */
	private static final int DEGREE_ANGLE = 11;
	/**
	 * Centimeters of a block
	 */
	private static final int BLOCK = 68;
	/**
	 * Olaf!
	 */
	private static final int MAX_SPEED = 425;
	private static final int CHANGE_SPEED = 415;
	private static final int SLIDY = 5;
	private static final int AZ_TOLERANCE = 10;
	public final Dashboard dashboard;
	public UltraSonicSensors sonar;
	private Robot myRobot;
	public static Lada instance;
	public int x = 15;
	public int y = 4;
	public int dx = 1;
	public int dy = 0;
	public int[][] mapintYX = new int[9][29];
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
	public static final int BLOCK_TOLERANCE_HIGH = 20;
	public static final int BLOCK_TOLERANCE_LOW = 10;
	public static final int HARMONY_NUMBER = 15;

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

		instance = this;
	}

	public void doDragRace() throws ConnectionLostException,
			InterruptedException {
		while (true && !killed) {
			driveDirect(this.rs, this.ls);
			straightenDrag();
		}
		driveDirect(0, 0);
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
//		dashboard.speak("My name is " + name);
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
				for (int i = 0; i < mapintYX.length; i++) {
					for (int j = 0; j < mapintYX[i].length; j++) {
						if (mapintYX[i][j] <= 0) {
							mapintYX[i][j] = 9;
						}
					}
				}
				/*while(true && !killed){
					dashboard.log(""+readCompass()+"");
					Object lock = new Object();
					synchronized(lock){
						try {
							lock.wait(500);
						} catch (InterruptedException e) {}
					}
				}*/
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
	
	public void mapMazeLeft() throws ConnectionLostException,
			InterruptedException {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					startAz = readCompass();
					preferredAz = startAz;
					boolean done = false;
					while (!done && !killed) {
						mapintYX[y][x] += 1;
//						straighten();
						sonar.read();
						if (!isWallLeft()) {
							turnLeft();
						} else if (isWallFront()) {
							turnRight();
							if (isWallRight()) {
								turnRight();
							}
						}
						x += dx;
						y += dy;
						myRobot.goForward(BLOCK);
						fixPosition();
						if (atEnd()) {
							mapintYX[y][x] += 1;
							done = false;
						}
					}
					mapped = true;
				} catch (Exception e) {

				}
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
//						straighten();
						mapintYX[y][x] += 1;
						sonar.read();
						if (!isWallRight()) {
							turnRight();
						} else if (isWallFront()) {
							turnLeft();
							if (isWallLeft()) {
								turnLeft();
							}
						}
						x += dx;
						y += dy;
						myRobot.goForward(BLOCK);
						fixPosition();
						if (atEnd()) {
							mapintYX[y][x] += 1;
							done = false;
						}
					}
					mapped = true;
				} catch (Exception e) {

				}
			}

		});
		t.start();
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
		preferredAz = (preferredAz + commandAngle + 360) % 360;
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

	public void fixPosition() throws ConnectionLostException
	{
		int front = getWallFront();
		int right = getWallRight();
		int left = getWallLeft();

		if (front <= BLOCK_TOLERANCE_LOW || front >= BLOCK_TOLERANCE_HIGH)
		{
			myRobot.goForward(front - HARMONY_NUMBER);
		}

		if (right <= BLOCK_TOLERANCE_LOW || right >= BLOCK_TOLERANCE_HIGH)
		{
			turnLeft();
			myRobot.goForward(HARMONY_NUMBER - right);
			turnRight();
		}

		if (left <= BLOCK_TOLERANCE_LOW || left >= BLOCK_TOLERANCE_HIGH)
		{
			turnRight();
			myRobot.goForward(HARMONY_NUMBER - left);
			turnLeft();
		}
	}

}
