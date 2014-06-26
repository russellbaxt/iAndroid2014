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

public class Lada extends IRobotCreateAdapter
{
	private static final int DEGREE_ANGLE = 11;
	private static final int BLOCK = 60;
	public final Dashboard dashboard;
	public UltraSonicSensors sonar;
	private Robot myRobot;
	public static Lada instance;
	public static int preferredAz;
	public int x = 0;
	public int y = 4;
	public int dx = 1; //directionX
	public int dy = 0; //directionY
	public int startAz;
	public int finalAz;
	public int currentAz;
	public static final int TURN_TOLERANCE = 5;
	public static final int AZ_TOLERANCE = 2;
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
		dashboard.log("Lada constructor");
	}

	public void initialize() throws ConnectionLostException,
			InterruptedException
	{
		currentAz = getAngle();
		preferredAz = getAngle();
		dashboard.log("Initialize");
		mapMaze();
	}

	private void solveMaze() throws ConnectionLostException
	{
		boolean done = false;
		int bestPath;
		while (!done)
		{
			bestPath = findBestPath();
			if (bestPath == mapintYX[y][x + dx])
			{
				turnLeft();
			} else if (bestPath == mapintYX[y + dy][x])
			{
				turnRight();
			}
			myRobot.goForward(BLOCK);
		}
	}

	private int findBestPath()
	{
		return Math.min(mapintYX[y + dy][x + dx],
				Math.min(mapintYX[y + dy][x], mapintYX[y][x + dx]));
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
			sonar.read();
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
				done = true;
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
		currentAz = (int) getAngle();
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
		startAz = getAngle();
		finalAz = startAz + 90;
		while (getAngle() - finalAz > TURN_TOLERANCE)
		{
			if (getAngle() - finalAz >= 45)
			{
				driveDirect(-450, 450);
			} else if (getAngle() - finalAz >= 30)
			{
				driveDirect(-300, 300);
			} else
			{
				driveDirect(-150, 150);
			}
			preferredAz += 90;
		}
		stop();
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
		startAz = getAngle();
		finalAz = startAz + -90;
		while (getAngle() + finalAz > TURN_TOLERANCE)
		{
			if (getAngle() + finalAz >= 45)
			{
				driveDirect(450, -450);
			} else if (getAngle() + finalAz >= 30)
			{
				driveDirect(300, 0300);
			} else
			{
				driveDirect(150, -150);
			}
		}
		stop();
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
		dashboard.log("left");
	}

	public boolean isWallFront() throws ConnectionLostException,
			InterruptedException
	{
		return getWallFront() < BLOCK;
	}

	public boolean isWallLeft() throws ConnectionLostException,
			InterruptedException
	{
		return getWallLeft() < BLOCK;
	}

	public boolean isWallRight() throws ConnectionLostException,
			InterruptedException
	{
		return getWallRight() < BLOCK;
	}

	public int getWallFront() throws ConnectionLostException,
			InterruptedException
	{
		sonar.read();
		return sonar.getFrontDistance();
	}

	public int getWallLeft() throws ConnectionLostException,
			InterruptedException
	{
		sonar.read();
		return sonar.getLeftDistance();
	}

	public int getWallRight() throws ConnectionLostException,
			InterruptedException
	{
		sonar.read();
		return sonar.getRightDistance();
	}

	public void straighten() throws ConnectionLostException
	{
		int diff;
		if (dx == 1 && dy == 0)
		{
			diff = currentAz - preferredAz;
			while (Math.abs(diff) >= AZ_TOLERANCE)
			{
				if (diff > 0)
				{
					driveDirect(15, -15);
				} else
				{
					driveDirect(-15, 15);
				}
				diff = currentAz - preferredAz;
			}
		} else if (dx == -1 && dy == 0)
		{
			diff = currentAz - (preferredAz - 180);
		}
		stop();
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
