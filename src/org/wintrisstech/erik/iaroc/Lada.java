package org.wintrisstech.erik.iaroc;

/**************************************************************************
 * Super Happy version...ultrasonics working...Version 140512A...mods by Vic
 * Added compass class...works..updatged to adt bundle 20140321
 **************************************************************************/
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;

import org.wintrisstech.irobot.ioio.IRobotCreateAdapter;
import org.wintrisstech.irobot.ioio.IRobotCreateInterface;

import android.os.SystemClock;

/**
 * A Lada is an implementation of the IRobotCreateInterface, inspired by Vic's
 * awesome API. It is entirely event driven.
 * 
 * @author Erik Simplified "API" class by Phil version 140523A
 */
public class Lada extends IRobotCreateAdapter
{
	private static final int BLOCK = 60;
	public final Dashboard dashboard;
	public UltraSonicSensors sonar;
	private Robot myRobot;
	public static Lada instance;
	public int x = 0;
	public int y = 6;
	public int[][] mapintYX = new int[9][15];

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
	public Lada(IOIO ioio, IRobotCreateInterface create, Dashboard dashboard)
			throws ConnectionLostException
	{
		super(create);
		sonar = new UltraSonicSensors(ioio);
		this.dashboard = dashboard;
		instance = this;
	}

	public void initialize() throws ConnectionLostException
	{
	}

	public void mapMaze() throws ConnectionLostException
	{
		boolean done = false;
		while (!done)
		{

			myRobot.goForward(BLOCK);
		}
	}

	/**
	 * This method is called repeatedly
	 * 
	 * @throws ConnectionLostException
	 * @throws InterruptedException
	 */
	public void loop() throws ConnectionLostException, InterruptedException
	{
		isWallFront();
	}

	public void turnRight() throws ConnectionLostException
	{
		int ls = 230;
		int rs = -ls;
		driveDirect(rs, ls);
		SystemClock.sleep(1000);
		driveDirect(0, 0);
	}

	public void turnLeft() throws ConnectionLostException
	{
		int rs = 230;
		int ls = -rs;
		driveDirect(rs, ls);
		SystemClock.sleep(1000);
		driveDirect(0, 0);
	}

	public void isWallFront() throws ConnectionLostException,
			InterruptedException
	{
		sonar.read();
		dashboard.log(String.valueOf(sonar.getFrontDistance()) + "..." + String.valueOf(sonar.getRightDistance())  + "..." +  String.valueOf(sonar.getLeftDistance()));
		SystemClock.sleep(250);
	}

	public int readCompass()
	{
		return (int) (dashboard.getAzimuth() + 360) % 360;
	}
}
