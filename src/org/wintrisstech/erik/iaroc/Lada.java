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
	private static final int DEGREE_ANGLE = 11;
	private static final int BLOCK = 60;
	public final Dashboard dashboard;
	public UltraSonicSensors sonar;
	private Robot myRobot;
	public static Lada instance;
	public static int startAz;
	public int x = 0;
	public int y = 4;
	public int dx = 1;
	public int dy = 0;
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

	public void initialize() throws ConnectionLostException,
			InterruptedException
	{
		
	}

	private void solveMaze()
	{
		dashboard.log(map());
		for (int i = 0; i < mapintYX.length; i++)
		{
			for (int j = 0; j < mapintYX[i].length; j++)
			{
				if (mapintYX[i][j] <= 0)
				{
					mapintYX[i][j] = 9;
				}
			}
		}
	}

	private String map()
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < mapintYX.length; i++)
		{
			for (int j = 0; j < mapintYX[i].length; j++)
			{
				sb.append(mapintYX[i][j]);
			}
		}
		return sb.toString();
	}

	public void mapMaze() throws ConnectionLostException, InterruptedException
	{
		boolean done = false;
		while (!done)
		{
			mapintYX[y][x] += 1;
			if (!isWallLeft())
			{
				turnLeft();
			} else if (isWallFront())
			{
				turnRight();
				if (isWallRight())
				{
					turnRight();
				}
			}
			x += dx;
			y += dy;
			myRobot.goForward(BLOCK);
			if (atEnd())
			{
				mapintYX[y][x] += 1;
				done = false;
			}
		}
	}

	private boolean atEnd() throws ConnectionLostException
	{
		readSensors(SENSORS_INFRARED_BYTE);
		readSensors(SENSORS_BUMPS_AND_WHEEL_DROPS);
		return isHomeBaseChargerAvailable() && isBumpLeft() && isBumpRight();
	}

	/**
	 * This method is called repeatedly
	 * 
	 * @throws ConnectionLostException
	 * @throws InterruptedException
	 */
	public void loop() throws ConnectionLostException, InterruptedException
	{
		dashboard.log("Front distance" + getWallFront());
		SystemClock.sleep(250);
	}

	public void turn(int commandAngle) throws ConnectionLostException
	{
		int ls = 230;
		int rs = -ls;
		driveDirect(rs, ls);
		SystemClock.sleep(DEGREE_ANGLE * commandAngle);
		stop();
	}

	public void turnRight() throws ConnectionLostException
	{
		turn(90);
		if (dx == 0 && dy == 1)
		{
			dx = 1;
			dy = 0;
		} else if (dx == 0 && dy == -1)
		{
			dx = -1;
			dy = 0;
		} else if (dx == 1 && dy == 0)
		{
			dx = 0;
			dy = -1;
		} else
		{
			dx = 0;
			dy = 1;
		}
		dashboard.log("right");
	}

	public void turnLeft() throws ConnectionLostException
	{
		turn(270);
		if (dx == 0 && dy == 1)
		{
			dx = -1;
			dy = 0;
		} else if (dx == 0 && dy == -1)
		{
			dx = 1;
			dy = 0;
		} else if (dx == 1 && dy == 0)
		{
			dx = 0;
			dy = 1;
		} else
		{
			dx = 0;
			dy = -1;
		}
		dashboard.log("right");
	}

	public boolean isWallFront() throws ConnectionLostException, InterruptedException
	{
		return getWallFront() < BLOCK;
	}

	public boolean isWallLeft() throws ConnectionLostException, InterruptedException
	{
		return getWallLeft() < BLOCK;
	}

	public boolean isWallRight() throws ConnectionLostException, InterruptedException
	{
		return getWallRight() < BLOCK;
	}
	
	public int getWallFront() throws ConnectionLostException, InterruptedException
	{
		sonar.read();
		return sonar.getFrontDistance();
	}

	public int getWallLeft() throws ConnectionLostException, InterruptedException
	{
		sonar.read();
		return sonar.getLeftDistance();
	}

	public int getWallRight() throws ConnectionLostException, InterruptedException
	{
		sonar.read();
		return sonar.getRightDistance();
	}

	public void straighten() throws ConnectionLostException
	{
		
	}

	private void stop() throws ConnectionLostException
	{
		driveDirect(0, 0);
	}

	public int readCompass()
	{
		return (int) (dashboard.getAzimuth() + 360) % 360;
	}
}
