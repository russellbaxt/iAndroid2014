package org.wintrisstech.erik.iaroc;

/**************************************************************************
 * Super Happy version...ultrasonics working...Version 140512A...mods by Vic
 * Added compass class...works..updatged to adt bundle 20140321
 **************************************************************************/
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import org.wintrisstech.irobot.ioio.IRobotCreateAdapter;
import org.wintrisstech.irobot.ioio.IRobotCreateInterface;
import org.wintrisstech.sensors.UltraSonicSensors;

import android.os.SystemClock;

/**
 * A Lada is an implementation of the IRobotCreateInterface, inspired by Vic's
 * awesome API. It is entirely event driven.
 * @author Erik
 * Version 140529A modified by Phil Cruz to include the Robot "API" class
 */
public class Lada extends IRobotCreateAdapter {
	private final Dashboard dashboard;
	public UltraSonicSensors sonar;
	private boolean firstPass = true;;
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
			throws ConnectionLostException {
		super(create);
		sonar = new UltraSonicSensors(ioio);
		this.dashboard = dashboard;
	}

	public void initialize() throws ConnectionLostException {
	
		myRobot = new Robot(dashboard, this);
		myRobot.log("iAndroid2014 version 140529A");
		myRobot.log("Ready!");
		myRobot.goForward(10);
		myRobot.log("I'm done.");
	}

	/**
	 * This method is called repeatedly
	 * 
	 * @throws ConnectionLostException
	 */
	public void loop() throws ConnectionLostException {
		
		//SystemClock.sleep(100);
		//dashboard.log(String.valueOf(readCompass()));
	}

	public void turn(int commandAngle) throws ConnectionLostException //Doesn't work for turns through 360
	{
		int startAzimuth = 0;
		if (firstPass) {
			startAzimuth += readCompass();
			commandAzimuth = (startAzimuth + commandAngle) % 360;
			dashboard.log("commandaz = " + commandAzimuth + " startaz = " + startAzimuth);
			firstPass = false;
		}
		int currentAzimuth = readCompass();
		dashboard.log("now = " + currentAzimuth);
		if (currentAzimuth >= commandAzimuth) {
			driveDirect(0, 0);
			firstPass = true;
			dashboard.log("finalaz = " + readCompass());
		}
	}

	public int readCompass() {
		return (int) (dashboard.getAzimuth() + 360) % 360;
	}
}
