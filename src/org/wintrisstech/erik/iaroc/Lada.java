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
	private final Dashboard dashboard;
	public UltraSonicSensors sonar;
	private boolean firstPass = true;
	private int commandAzimuth;
	private Robot myRobot;

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
	}

	public void initialize() throws ConnectionLostException
	{
		dashboard.log("iAndroid2014 happy version 140523A");
		myRobot = new Robot(dashboard, this);
		myRobot.log("Ready!");
		myRobot.goForward(10);
		myRobot.log("I'm done.");
		turnRight();		
	}

	/**
	 * This method is called repeatedly
	 * 
	 * @throws ConnectionLostException
	 * @throws InterruptedException
	 */
	public void loop() throws ConnectionLostException, InterruptedException
	{

//		SystemClock.sleep(500);
//		sonar.read();
//		dashboard.log(String.valueOf(sonar.getLeftDistance() + "..."
//				+ sonar.getFrontDistance() + "..." + sonar.getRightDistance()));
	}

	public void turn(int commandAngle) throws ConnectionLostException 
	{
		// int startAzimuth = 0;
		// if (firstPass)
		// {
		// startAzimuth += readCompass();
		// commandAzimuth = (startAzimuth + commandAngle) % 360;
		// dashboard.log("commandaz = " + commandAzimuth + " startaz = "
		// + startAzimuth);
		// firstPass = false;
		// }
		// int currentAzimuth = readCompass();
		// dashboard.log("now = " + currentAzimuth);
		// if (currentAzimuth >= commandAzimuth)
		// {
		// driveDirect(0, 0);
		// firstPass = true;
		// dashboard.log("finalaz = " + readCompass());
		// }
	}

	public void turnRight() throws ConnectionLostException
	{
		int ls = 220;
		int rs = -ls;
		driveDirect(rs, ls);
		SystemClock.sleep(1000);
		driveDirect(0, 0);
	}

	public int readCompass()
	{
		return (int) (dashboard.getAzimuth() + 360) % 360;
	}
}
