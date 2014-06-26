package org.wintrisstech.erik.iaroc;

import ioio.lib.api.exception.ConnectionLostException;

/**************************************************************************
 * A class to abstract a higher level API to control the robot
 * version 140523A
 **************************************************************************/
public class Robot {
	private Lada lada;
	private final Dashboard dashboard;

	public Robot(Dashboard dashboard, Lada lada)
	{
		this.dashboard = dashboard;
		this.lada = lada;
	}

	public void log(String message)
	{
		dashboard.log(message);
	}

	public void goForward(int centimeters) throws ConnectionLostException
	{
		int totalDistance = 0;
		lada.readSensors(Lada.SENSORS_GROUP_ID6);
		lada.driveDirect(250, 250);
		while (totalDistance < centimeters * 10)
		{
			lada.readSensors(Lada.SENSORS_GROUP_ID6);
			int dd = lada.getDistance();
			totalDistance += dd;
//			log("" + totalDistance / 10 + " cm");
		}
		stop();
	}

	public void stop() throws ConnectionLostException
	{
		lada.driveDirect(0, 0);
	}
}
