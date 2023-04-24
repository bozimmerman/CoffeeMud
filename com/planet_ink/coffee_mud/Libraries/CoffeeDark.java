package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.exceptions.BadEmailAddressException;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.BoundedObject.BoundedCube;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMProps.Str;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.MiniJSON.MJSONException;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.CharCreationLibrary.LoginSession;
import com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary.ThinPlayer;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.ShipDirectional.ShipDir;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.util.Map.Entry;
import java.lang.ref.WeakReference;
import java.math.*;
/*
   Copyright 2013-2023 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class CoffeeDark extends StdLibrary implements GalacticMap
{
	@Override
	public String ID()
	{
		return "CoffeeDark";
	}
	protected static final double		ZERO_ALMOST				= 0.000001;
	protected static final BigDecimal	ZERO					= BigDecimal.valueOf(0.0);
	protected static final BigDecimal	ALMOST_ZERO				= BigDecimal.valueOf(ZERO_ALMOST);
	protected static final BigDecimal	ONE						= BigDecimal.valueOf(1L);
	protected static final BigDecimal	MIN_ONE					= BigDecimal.valueOf(-1L);
	protected static final BigDecimal	TWO						= BigDecimal.valueOf(2L);
	protected static final BigDecimal	FOUR					= BigDecimal.valueOf(4L);
	protected static final BigDecimal	TEN						= BigDecimal.valueOf(10L);
	protected static final BigDecimal	ONE_THOUSAND			= BigDecimal.valueOf(1000);
	protected static final double		PI_ALMOST				= Math.PI - ZERO_ALMOST;
	protected static final double		PI_TIMES_2_ALMOST		= Math.PI * 2.0 - ZERO_ALMOST;
	protected static final double		PI_TIMES_2				= Math.PI * 2.0;
	protected static final double		PI_BY_2					= Math.PI / 2.0;
	protected static final double		PI_TIMES_1ANDAHALF		= Math.PI * 1.5;
	protected final int					QUADRANT_WIDTH			= 10;

	protected final RTree<SpaceObject>			space		= new RTree<SpaceObject>();
	protected final Map<String, BoundedCube>	sectorMap	= new Hashtable<String, BoundedCube>();

	private static Filterer<Area> planetsAreaFilter=new Filterer<Area>()
	{
		@Override
		public boolean passesFilter(final Area obj)
		{
			return (obj instanceof SpaceObject) && (!(obj instanceof SpaceShip));
		}
	};

	@Override
	public boolean isObjectInSpace(final SpaceObject O)
	{
		synchronized(space)
		{
			return space.contains(O);
		}
	}

	@Override
	public void delObjectInSpace(final SpaceObject O)
	{
		synchronized(space)
		{
			space.remove(O);
		}
	}

	@Override
	public void addObjectToSpace(final SpaceObject O, final long[] coords)
	{
		synchronized(space)
		{
			O.coordinates()[0]=coords[0];
			O.coordinates()[1]=coords[1];
			O.coordinates()[2]=coords[2];
			space.insert(O); // won't accept dups, so is ok
		}
	}

	@Override
	public void addObjectToSpace(final SpaceObject O)
	{
		synchronized(space)
		{
			space.insert(O); // won't accept dups, so is ok
		}
	}

	@Override
	public Enumeration<Area> spaceAreas()
	{
		return new FilteredEnumeration<Area>(CMLib.map().areas(),planetsAreaFilter);
	}

	@Override
	public long getDistanceFrom(final long[] coord1, final long[] coord2)
	{
		final BigInteger coord_0 = BigInteger.valueOf(coord1[0]).subtract(BigInteger.valueOf(coord2[0]));
		final BigInteger coord_0m = coord_0.multiply(coord_0);
		final BigInteger coord_1 = BigInteger.valueOf(coord1[1]).subtract(BigInteger.valueOf(coord2[1]));
		final BigInteger coord_1m = coord_1.multiply(coord_1);
		final BigInteger coord_2 = BigInteger.valueOf(coord1[2]).subtract(BigInteger.valueOf(coord2[2]));
		final BigInteger coord_2m = coord_2.multiply(coord_2);
		final BigInteger coords_all = coord_0m.add(coord_1m).add(coord_2m);
		return Math.round(Math.sqrt(coords_all.doubleValue()));
	}

	@Override
	public long getDistanceFrom(final SpaceObject O1, final SpaceObject O2)
	{
		return getDistanceFrom(O1.coordinates(),O2.coordinates());
	}

	protected BigDecimal getBigDistanceFrom(final long[] coord1, final long[] coord2)
	{
		final BigDecimal coord_0 = BigDecimal.valueOf(coord1[0]).subtract(BigDecimal.valueOf(coord2[0]));
		final BigDecimal coord_0m = coord_0.multiply(coord_0);
		final BigDecimal coord_1 = BigDecimal.valueOf(coord1[1]).subtract(BigDecimal.valueOf(coord2[1]));
		final BigDecimal coord_1m = coord_1.multiply(coord_1);
		final BigDecimal coord_2 = BigDecimal.valueOf(coord1[2]).subtract(BigDecimal.valueOf(coord2[2]));
		final BigDecimal coord_2m = coord_2.multiply(coord_2);
		final BigDecimal coords_all = coord_0m.add(coord_1m).add(coord_2m);
		final BigDecimal val = bigSqrt(coords_all);
		return val;
	}

	@Override
	public String getSectorName(final long[] coords)
	{
		final String[] xsecs=CMProps.getListFileStringList(CMProps.ListFile.TECH_SECTOR_X_NAMES);
		final String[] ysecs=CMProps.getListFileStringList(CMProps.ListFile.TECH_SECTOR_Y_NAMES);
		final String[] zsecs=CMProps.getListFileStringList(CMProps.ListFile.TECH_SECTOR_Z_NAMES);

		final long dmsPerXSector = (SpaceObject.Distance.GalaxyRadius.dm / (xsecs.length)) * 2L;
		final long dmsPerYSector = (SpaceObject.Distance.GalaxyRadius.dm / (ysecs.length)) * 2L;
		final long dmsPerZSector = (SpaceObject.Distance.GalaxyRadius.dm / (zsecs.length)) * 2L;

		final long secDeX = coords[0] / dmsPerXSector;
		final long secDeY = coords[1] / dmsPerYSector;
		final long secDeZ = coords[2] / dmsPerZSector;

		final StringBuilder sectorName = new StringBuilder("");
		sectorName.append(xsecs[(int)secDeX + xsecs.length/2]).append(" ");
		sectorName.append(ysecs[(int)secDeY + ysecs.length/2]).append(" ");
		sectorName.append(zsecs[(int)secDeZ + zsecs.length/2]);
		return sectorName.toString();
	}

	@Override
	public long[] getInSectorCoords(final long[] coords)
	{
		final String[] xsecs=CMProps.getListFileStringList(CMProps.ListFile.TECH_SECTOR_X_NAMES);
		final String[] ysecs=CMProps.getListFileStringList(CMProps.ListFile.TECH_SECTOR_Y_NAMES);
		final String[] zsecs=CMProps.getListFileStringList(CMProps.ListFile.TECH_SECTOR_Z_NAMES);

		final long dmsPerXSector = (SpaceObject.Distance.GalaxyRadius.dm / (xsecs.length)) * 2L;
		final long dmsPerYSector = (SpaceObject.Distance.GalaxyRadius.dm / (ysecs.length)) * 2L;
		final long dmsPerZSector = (SpaceObject.Distance.GalaxyRadius.dm / (zsecs.length)) * 2L;

		final long[] sectorCoords = coords.clone();
		for(int i=0;i<sectorCoords.length;i++)
		{
			if(sectorCoords[i]<0)
				sectorCoords[i]*=-1;
		}
		sectorCoords[0] = (sectorCoords[0] % dmsPerXSector);
		sectorCoords[1] = (sectorCoords[1] % dmsPerYSector);
		sectorCoords[2] = (sectorCoords[2] % dmsPerZSector);
		return sectorCoords;
	}


	@Override
	public Map<String,BoundedCube> getSectorMap()
	{
		if(sectorMap.size()==0)
		{
			final Map<String,BoundedCube> tempMap = new HashMap<String,BoundedCube>();
			final String[] xsecs=CMProps.getListFileStringList(CMProps.ListFile.TECH_SECTOR_X_NAMES);
			final String[] ysecs=CMProps.getListFileStringList(CMProps.ListFile.TECH_SECTOR_Y_NAMES);
			final String[] zsecs=CMProps.getListFileStringList(CMProps.ListFile.TECH_SECTOR_Z_NAMES);

			final long dmsPerXSector = (SpaceObject.Distance.GalaxyRadius.dm / (xsecs.length)) * 2L;
			final long dmsPerYSector = (SpaceObject.Distance.GalaxyRadius.dm / (ysecs.length)) * 2L;
			final long dmsPerZSector = (SpaceObject.Distance.GalaxyRadius.dm / (zsecs.length)) * 2L;
			for(long x=0;x<SpaceObject.Distance.GalaxyRadius.dm-dmsPerXSector;x+=dmsPerXSector)
			{
				for(long y=0;y<SpaceObject.Distance.GalaxyRadius.dm-dmsPerYSector;y+=dmsPerYSector)
				{
					for(long z=0;z<SpaceObject.Distance.GalaxyRadius.dm-dmsPerZSector;z+=dmsPerZSector)
					{
						final long[] coords = new long[] {x, y, z};
						final BoundedCube cube = new BoundedCube(x,x+dmsPerXSector,y,y+dmsPerYSector,z,z+dmsPerZSector);
						final String name = getSectorName(coords);
						if(tempMap.containsKey(name) || (coords[2]<0L))
							Log.errOut("Argh!");
						else
							tempMap.put(name, cube);
					}
				}
			}
			for(long x=dmsPerXSector;x<SpaceObject.Distance.GalaxyRadius.dm-dmsPerXSector;x+=dmsPerXSector)
			{
				for(long y=dmsPerYSector;y<SpaceObject.Distance.GalaxyRadius.dm-dmsPerYSector;y+=dmsPerYSector)
				{
					for(long z=dmsPerZSector;z<SpaceObject.Distance.GalaxyRadius.dm-dmsPerZSector;z+=dmsPerZSector)
					{
						final long[] coords = new long[] {-x, -y, -z};
						final BoundedCube cube = new BoundedCube(-x,-x+dmsPerXSector,-y,-y+dmsPerYSector,-z,-z+dmsPerZSector);
						final String name = getSectorName(coords);
						if(tempMap.containsKey(name) || (coords[2]>0L))
							Log.errOut("Argh!!");
						else
							tempMap.put(name, cube);
					}
				}
			}
			sectorMap.putAll(tempMap);
		}
		return sectorMap;
	}

	@Override
	public void accelSpaceObject(final SpaceObject O, final double[] accelDirection, final double newAcceleration)
	{
		final double newSpeed = accelSpaceObject(O.direction(),O.speed(),accelDirection,newAcceleration);
		O.setSpeed(newSpeed);
	}

	@Override
	public double getAngleDelta(final double[] fromAngle, final double[] toAngle)
	{
		if(Arrays.equals(fromAngle,  toAngle))
			return 0.0;
		final BigVector from = new BigVector(fromAngle).sphereToCartesian();
		final BigVector to = new BigVector(toAngle).sphereToCartesian();
		BigDecimal dotProd = from.dotProduct(to);
		if(dotProd.compareTo(ONE)>0)
			dotProd=TWO.subtract(dotProd);
		if(dotProd.compareTo(MIN_ONE)<0)
			dotProd=MIN_ONE.multiply(dotProd).subtract(TWO);
		//final BigDecimal fromag = from.magnitude();
		//final BigDecimal tomag = to.magnitude();
		final double finalDelta = Math.acos(dotProd.doubleValue());
		if(Double.isNaN(finalDelta) || Double.isInfinite(finalDelta))
		{
			Log.errOut("NaN finalDelta = "+ finalDelta+"= ("+fromAngle[0]+","+fromAngle[1]+") -> ("+toAngle[0]+","+toAngle[1]+")");
			Log.errOut("NaN dotprod = " + dotProd+", from = " + from + ", to=" +to);
			throw new java.lang.IllegalArgumentException();
		}
		return finalDelta;
	}

	@Override
	public double[] getMiddleAngle(final double[] angle1, final double[] angle2)
	{
		final double[] middleAngle = new double[] {angle1[0], angle1[1]};
		if(angle1[0] != angle2[0])
		{
			final double xy1 = Math.max(angle1[0], angle2[0]);
			final double xy2 = (xy1 == angle1[0]) ? angle2[0] : angle1[0];
			if(xy2<(xy1-Math.PI))
				middleAngle[0] = ((PI_TIMES_2-xy1)+xy2)/2.0;
			else
				middleAngle[0] = (xy1 + xy2)/2.0;
		}
		middleAngle[1]=(angle1[1]+angle2[1])/2.0;
		/*
		final double x1=Math.sin(angle1[1])*Math.cos(angle1[0]);
		final double y1=Math.sin(angle1[1])*Math.sin(angle1[0]);
		final double z1=Math.cos(angle1[1]);
		final double x2=Math.sin(angle2[1])*Math.cos(angle2[0]);
		final double y2=Math.sin(angle2[1])*Math.sin(angle2[0]);
		final double z2=Math.cos(angle2[1]);
		final double xSum = (x1 + x2);
		final double ySum = (y1 + y2);
		final double zSum = (z1 + z2);
		middleAngle[0] = Math.atan2(ySum, xSum);
		if(middleAngle[0] < 0)
			middleAngle[0] += PI_TIMES_2;
		middleAngle[1] = Math.acos(zSum);
		}
		*/
		return middleAngle;
	}

	@Override
	public double[] getOffsetAngle(final double[] correctAngle, final double[] wrongAngle)
	{
		final double[] offsetAngles = new double[] {correctAngle[0], correctAngle[1]};
		if(correctAngle[0] != wrongAngle[0])
		{
			final double xy1 = Math.max(correctAngle[0], wrongAngle[0]);
			final double xy2 = (xy1 == correctAngle[0]) ? wrongAngle[0] : correctAngle[0];
			if(xy2<(xy1-Math.PI))
				offsetAngles[0] = (PI_TIMES_2-xy1)+xy2;
			else
				offsetAngles[0] = xy1 - xy2;
			if((wrongAngle[0] > correctAngle[0])
			&&((wrongAngle[0]-correctAngle[0]) < Math.PI))
			{
				offsetAngles[0] = correctAngle[0] - offsetAngles[0];
				if(offsetAngles[0] < 0)
					offsetAngles[0] += PI_TIMES_2;
			}
			else
			{
				offsetAngles[0] = correctAngle[0] + offsetAngles[0];
				if(offsetAngles[0] >= PI_TIMES_2)
					offsetAngles[0] -= PI_TIMES_2;
			}
		}
		if(correctAngle[1] != wrongAngle[1])
		{
			final double xy1 = Math.max(correctAngle[1], wrongAngle[1]);
			final double xy2 = (xy1 == correctAngle[1]) ? wrongAngle[1] : correctAngle[1];
			offsetAngles[1] = xy1 - xy2;
			if(wrongAngle[1] > correctAngle[1])
				offsetAngles[1] = correctAngle[1] - offsetAngles[1];
			else
				offsetAngles[1] = correctAngle[1] + offsetAngles[1];
		}
		return offsetAngles;
	}

	@Override
	public double[] getFacingAngleDiff(final double[] fromAngle, final double[] toAngle)
	{
		final double fromYaw = fromAngle[0];
		final double fromPitch = (fromAngle[1] > Math.PI) ? Math.abs(Math.PI-fromAngle[1]) : fromAngle[1];

		final double toYaw = toAngle[0];
		final double toPitch = (toAngle[1] > Math.PI) ? Math.abs(Math.PI-toAngle[1]) : toAngle[1];

		final double[] delta = new double[2];
		if(toYaw != fromYaw)
		{
			if(toYaw > fromYaw)
			{
				delta[0]=(toYaw-fromYaw);
				if(delta[0] > Math.PI)
					delta[0] = -((PI_TIMES_2-toYaw)+fromYaw);
			}
			else
			{
				delta[0]=(toYaw-fromYaw);
				if(delta[0] < -Math.PI)
					delta[0] = -((PI_TIMES_2-fromYaw)+toYaw);
			}
		}
		delta[1]=(toPitch-fromPitch);
		//fixDirectionBounds(delta); // normalizing directions makes NO SENSE for a delta!
		return delta;
	}

	@Override
	public double accelSpaceObject(final double[] curDirection, final double curSpeed, final double[] accelDirection, final double newAcceleration)
	{
		if(newAcceleration <= 0.0)
			return curSpeed;

		fixDirectionBounds(curDirection);
		final double curDirectionYaw = curDirection[0];
		final double curDirectionPitch = curDirection[1];

		fixDirectionBounds(accelDirection);
		final double accelDirectionYaw = accelDirection[0];
		final double accelDirectionPitch = accelDirection[1];

		final double currentSpeed = curSpeed;
		final double acceleration = newAcceleration;

		final double yawSign;
		double yawDelta;
		if(curDirectionYaw > accelDirectionYaw)
		{
			yawSign = -1.0;
			yawDelta = (curDirectionYaw - accelDirectionYaw);
		}
		else
		{
			yawSign = 1.0;
			yawDelta = (accelDirectionYaw - curDirectionYaw);
		}
		// 350 and 10, diff = 340 + -360 = 20
		if(yawDelta > Math.PI) // a delta is never more than 180 degrees
			yawDelta=PI_TIMES_2-yawDelta;
		final double pitchSign;
		final double pitchDelta;
		if(curDirectionPitch > accelDirectionPitch)
		{
			pitchSign = -1.0;
			pitchDelta = (curDirectionPitch - accelDirectionPitch);
		}
		else
		{
			pitchSign = 1.0;
			pitchDelta = (accelDirectionPitch - curDirectionPitch);
		}
		final double anglesDelta =  getAngleDelta(curDirection, accelDirection);
		double newDirectionYaw;
		double newDirectionPitch;
		final double deltaMultiplier = Math.sin(anglesDelta);
		final double yawMin =  deltaMultiplier * (0.1 + (yawDelta * (1.01-Math.sin(curDirectionPitch))));
System.out.println(Math.round(yawMin*100)/100.0);
		final double accelerationMultiplier = (acceleration * 10.0 / currentSpeed) * deltaMultiplier;
		if(yawDelta < yawMin)
			newDirectionYaw = accelDirectionYaw;
		else
		{
			double nearFinalYawDelta = Math.sin(yawDelta) * accelerationMultiplier;
			if((nearFinalYawDelta < yawMin)&&(yawDelta > yawMin))
				nearFinalYawDelta = yawMin;
			newDirectionYaw = curDirectionYaw + (nearFinalYawDelta * yawSign);
			if((newDirectionYaw > 0.0) && ((PI_TIMES_2 - newDirectionYaw) < ZERO_ALMOST))
				newDirectionYaw=0.0;
		}
		final double pitchMin = 0.1;
		if(pitchDelta <pitchMin)
			newDirectionPitch = accelDirectionPitch;
		else
		{
			double nearFinalPitchDelta = Math.sin(pitchDelta) * accelerationMultiplier;
			if((nearFinalPitchDelta < pitchMin)&&(pitchDelta > pitchMin))
				nearFinalPitchDelta = pitchMin;
			newDirectionPitch = curDirectionPitch + (nearFinalPitchDelta * pitchSign);
		}
		double newSpeed = currentSpeed + (acceleration * Math.cos(anglesDelta));
		if(newSpeed < 0)
		{
			newSpeed = -newSpeed;
			newDirectionYaw = accelDirectionYaw;
			newDirectionPitch = accelDirectionPitch;
		}
		curDirection[0]=newDirectionYaw;
		curDirection[1]=newDirectionPitch;
		fixDirectionBounds(curDirection);
		if(Double.isInfinite(newSpeed) || Double.isNaN(newSpeed))
		{
			Log.errOut("Invalid new speed: "+newSpeed + "("+currentSpeed+"+"+"("+acceleration+"*Math.cos("+anglesDelta+"))");
			return curSpeed;
		}
		return newSpeed;
	}

	protected void fixDirectionBounds(final double[] dir)
	{
		if(Double.isInfinite(dir[0]))
			dir[0] = Math.PI;
		if(Double.isInfinite(dir[1]))
			dir[1] = Math.PI/2.0;
		while(dir[0] >= PI_TIMES_2)
			dir[0] -= PI_TIMES_2;
		while(dir[0] < 0)
			dir[0] += PI_TIMES_2;
		while(dir[1] >= PI_TIMES_2)
			dir[1] -= PI_TIMES_2;
		while(dir[1] < -PI_TIMES_2)
			dir[1] += PI_TIMES_2;
		while(dir[1] > Math.PI)
		{
			dir[1] = Math.PI - dir[1];
			dir[0] = dir[0] + ((dir[0] <= Math.PI)?Math.PI:(-Math.PI));
		}
		while(dir[1] < 0)
		{
			dir[1] = Math.abs(dir[1]);
			dir[0] = dir[0] + ((dir[0] <= Math.PI)?Math.PI:(-Math.PI));
		}
	}

	@Override
	public void changeDirection(final double[] dir, final double delta0, final double delta1)
	{
		dir[0] += delta0 % PI_TIMES_2;
		dir[1] += delta1 % Math.PI;
		fixDirectionBounds(dir);
	}

	@Override
	public void changeDirection(final double[] dir, final double[] delta)
	{
		changeDirection(dir, delta[0], delta[1]);
	}

	@Override
	public double[] getOppositeDir(final double[] dir)
	{
		fixDirectionBounds(dir);
		final double[] newDir = new double[]{Math.PI+dir[0],Math.PI-dir[1]};
		fixDirectionBounds(newDir);
		return newDir;
	}

	@Override
	public ShipDirectional.ShipDir getDirectionFromDir(final double[] facing, final double roll, final double[] direction)
	{
		//Log.debugOut("facing="+(Math.toDegrees(facing[0]) % 360.0)+","+(Math.toDegrees(facing[1]) % 180.0));
		//Log.debugOut("direction="+(Math.toDegrees(direction[0]) % 360.0)+","+(Math.toDegrees(direction[1]) % 180.0));
		double yD = ((Math.toDegrees(facing[0]) % 360.0) - (Math.toDegrees(direction[0]) % 360.0)) % 360.0;
		if(yD < 0)
			yD = 360.0 + yD;
		final double pD = Math.abs(((Math.toDegrees(facing[1]) % 180.0) - (Math.toDegrees(direction[1]) % 180.0)) % 180.0);
		//Log.debugOut("yD,pD="+yD+","+pD);
		double rD = (yD + (Math.toDegrees(roll) % 360.0)) % 360.0;
		if(rD < 0)
			rD = 360.0 + rD;
		//Log.debugOut("rD="+rD);
		if(pD<45 || pD > 135)
		{
			if(yD < 45.0 || yD > 315.0)
				return ShipDir.FORWARD;
			if(yD> 135.0 && yD < 225.0)
				return ShipDir.AFT;
		}
		if(rD >= 315.0 || rD<45.0)
			return ShipDir.DORSEL;
		if(rD >= 45.0 && rD <135.0)
			return ShipDir.PORT;
		if(rD >= 135.0 && rD <225.0)
			return ShipDir.VENTRAL;
		if(rD >= 225.0 && rD <315.0)
			return ShipDir.STARBOARD;
		return ShipDir.AFT;
	}

	@Override
	public ShipDirectional.ShipDir getAbsoluteDirectionalFromDir(final double[] direction)
	{
		double yD = Math.toDegrees(direction[0]) % 360.0;
		if(yD < 0)
			yD = 360.0 + yD;
		double rD = Math.toDegrees(direction[1]) % 180.0;
		if(rD < 0)
			rD = 180.0 + rD;
		if(rD<45)
			return ShipDir.DORSEL;
		if(rD>135)
			return ShipDir.VENTRAL;
		if(yD >= 315.0 || yD<45.0)
			return ShipDir.FORWARD;
		if(yD >= 135.0 && yD <225.0)
			return ShipDir.VENTRAL;
		if(yD >= 45.0 && yD <135.0)
			return ShipDir.PORT;
		if(yD >= 225.0 && yD <315.0)
			return ShipDir.STARBOARD;
		return ShipDir.AFT;
	}

	@Override
	public double[] getDirection(final SpaceObject fromObj, final SpaceObject toObj)
	{
		return getDirection(fromObj.coordinates(),toObj.coordinates());
	}

	protected void moveSpaceObject(final SpaceObject O, final long x, final long y, final long z)
	{
		synchronized(space)
		{
			final boolean reAdd=space.contains(O);
			if(reAdd)
				space.remove(O);
			O.coordinates()[0]=x;
			O.coordinates()[1]=y;
			O.coordinates()[2]=z;
			if(reAdd)
				space.insert(O);
		}
	}

	@Override
	public void moveSpaceObject(final SpaceObject O, final long[] coords)
	{
		moveSpaceObject(O, coords[0], coords[1], coords[2]);
	}

	@Override
	public void moveSpaceObject(final SpaceObject O)
	{
		if(O.speed()>0)
		{
			final double x1=Math.cos(O.direction()[0])*Math.sin(O.direction()[1]);
			final double y1=Math.sin(O.direction()[0])*Math.sin(O.direction()[1]);
			final double z1=Math.cos(O.direction()[1]);
			moveSpaceObject(O,Math.round(CMath.mul(O.speed(),x1)+O.coordinates()[0]),
							Math.round(CMath.mul(O.speed(),y1)+O.coordinates()[1]),
							Math.round(CMath.mul(O.speed(),z1))+O.coordinates()[2]);
		}
	}

	@Override
	public double[] getDirection(final long[] fromCoords, final long[] toCoords)
	{
		return getBigDirection(fromCoords, toCoords);
		/*
		final double[] dir=new double[2];
		final double x=toCoords[0]-fromCoords[0];
		final double y=toCoords[1]-fromCoords[1];
		final double z=toCoords[2]-fromCoords[2];
		final double xy = (x*x)+(y*y);
		if((x!=0)||(y!=0))
		{
			final double sqrtxy = Math.sqrt(xy);
			final double ybysqrtxy=y/sqrtxy;
			if(x<0)
				dir[0]=Math.PI-Math.asin(ybysqrtxy);
			else
				dir[0]=Math.asin(ybysqrtxy);
		}
		if((x!=0)||(y!=0)||(z!=0))
			dir[1]=Math.acos(z/Math.sqrt((z*z)+xy));
		fixDirectionBounds(dir);
		return dir;
		*/
	}

	protected double[] getBigDirection(final long[] fromCoords, final long[] toCoords)
	{
		final double[] dir=new double[2];
		final BigDecimal x=BigDecimal.valueOf(toCoords[0]-fromCoords[0]);
		final BigDecimal y=BigDecimal.valueOf(toCoords[1]-fromCoords[1]);
		final BigDecimal z=BigDecimal.valueOf(toCoords[2]-fromCoords[2]);
		final BigDecimal xy = x.multiply(x).add(y.multiply(y));
		if((x.doubleValue()!=0)||(y.doubleValue()!=0))
		{
			final BigDecimal sqrtxy = bigSqrt(xy);
			final BigDecimal ybysqrtxy=y.divide(sqrtxy,50,RoundingMode.HALF_EVEN);
			if(x.doubleValue()<0)
				dir[0]=Math.PI-Math.asin(ybysqrtxy.doubleValue());
			else
				dir[0]=Math.asin(ybysqrtxy.doubleValue());
		}
		if((x.doubleValue()!=0)||(y.doubleValue()!=0)||(z.doubleValue()!=0))
			dir[1]=Math.acos(z.divide(bigSqrt(z.multiply(z).add(xy)),50,RoundingMode.HALF_EVEN).doubleValue());
		fixDirectionBounds(dir);
		return dir;
	}

	@Override
	public long[] moveSpaceObject(final long[] coordinates, final double[] direction, final long speed)
	{
		if(speed>0)
		{
			final double x1=Math.cos(direction[0])*Math.sin(direction[1]);
			final double y1=Math.sin(direction[0])*Math.sin(direction[1]);
			final double z1=Math.cos(direction[1]);
			return new long[]{coordinates[0]+Math.round(CMath.mul(speed,x1)),
							coordinates[1]+Math.round(CMath.mul(speed,y1)),
							coordinates[2]+Math.round(CMath.mul(speed,z1))};
		}
		return coordinates;
	}

	@Override
	public long[] getLocation(final long[] oldLocation, final double[] direction, final long distance)
	{
		final double x1=Math.cos(direction[0])*Math.sin(direction[1]);
		final double y1=Math.sin(direction[0])*Math.sin(direction[1]);
		final double z1=Math.cos(direction[1]);
		final long[] location=new long[3];
		location[0]=oldLocation[0]+Math.round(CMath.mul(distance,x1));
		location[1]=oldLocation[1]+Math.round(CMath.mul(distance,y1));
		location[2]=oldLocation[2]+Math.round(CMath.mul(distance,z1));
		return location;
	}

	@Override
	public long getRelativeSpeed(final SpaceObject O1, final SpaceObject O2)
	{
		return Math.round(Math.sqrt((CMath.bigMultiply(O1.speed(),O1.coordinates()[0])
										.subtract(CMath.bigMultiply(O2.speed(),O2.coordinates()[0]).multiply(CMath.bigMultiply(O1.speed(),O1.coordinates()[0])))
										.subtract(CMath.bigMultiply(O2.speed(),O2.coordinates()[0])))
									.add(CMath.bigMultiply(O1.speed(),O1.coordinates()[1])
										.subtract(CMath.bigMultiply(O2.speed(),O2.coordinates()[1]).multiply(CMath.bigMultiply(O1.speed(),O1.coordinates()[1])))
										.subtract(CMath.bigMultiply(O2.speed(),O2.coordinates()[1])))
									.add(CMath.bigMultiply(O1.speed(),O1.coordinates()[2])
										.subtract(CMath.bigMultiply(O2.speed(),O2.coordinates()[2]).multiply(CMath.bigMultiply(O1.speed(),O1.coordinates()[2])))
										.subtract(CMath.bigMultiply(O2.speed(),O2.coordinates()[2]))).doubleValue()));
	}

	@Override
	public SpaceObject findSpaceObject(final String s, final boolean exactOnly)
	{
		final Iterable<SpaceObject> i=new Iterable<SpaceObject>()
		{
			@Override
			public Iterator<SpaceObject> iterator()
			{
				return new EnumerationIterator<SpaceObject>(space.objects());
			}

		};
		return (SpaceObject)CMLib.english().fetchEnvironmental(i, s, exactOnly);
	}

	@Override
	public SpaceObject getSpaceObject(final CMObject o, final boolean ignoreMobs)
	{
		if(o instanceof SpaceObject)
		{
			if(o instanceof Boardable)
			{
				final Item I=((Boardable)o).getBoardableItem();
				if(I instanceof SpaceObject)
					return (SpaceObject)I;
			}
			return (SpaceObject)o;
		}
		if(o instanceof Item)
		{
			if(((Item)o).container()!=null)
				return getSpaceObject(((Item)o).container(),ignoreMobs);
			else
				return getSpaceObject(((Item)o).owner(),ignoreMobs);
		}
		if(o instanceof MOB)
			return ignoreMobs?null:getSpaceObject(((MOB)o).location(),false);
		if(o instanceof Room)
			return getSpaceObject(((Room)o).getArea(),ignoreMobs);
		if(o instanceof Area)
		{
			for(final Enumeration<Area> a=((Area)o).getParents();a.hasMoreElements();)
			{
				final SpaceObject obj=getSpaceObject(a.nextElement(),ignoreMobs);
				if(obj != null)
					return obj;
			}
		}
		return null;
	}

	@Override
	public Enumeration<SpaceObject> getSpaceObjects()
	{
		return this.space.objects();
	}

	@Override
	public Enumeration<Entry<SpaceObject, List<WeakReference<TrackingVector<SpaceObject>>>>>  getSpaceObjectEntries()
	{
		return this.space.objectEntries();
	}

	@Override
	public List<SpaceObject> getSpaceObjectsByCenterpointWithin(final long[] centerCoordinates, final long minDistance, final long maxDistance)
	{
		final List<SpaceObject> within=new ArrayList<SpaceObject>(1);
		if((centerCoordinates==null)||(centerCoordinates.length!=3))
			return within;
		synchronized(space)
		{
			space.query(within, new BoundedObject.BoundedCube(centerCoordinates, maxDistance));
		}
		if(within.size()<1)
			return within;
		for (final Iterator<SpaceObject> o=within.iterator();o.hasNext();)
		{
			final SpaceObject O=o.next();
			final long dist=getDistanceFrom(O.coordinates(),centerCoordinates);
			if((dist<minDistance)||(dist>maxDistance))
				o.remove();
		}
		return within;
	}

	@Override
	public List<SpaceObject> getSpaceObjectsInBound(final BoundedCube cube)
	{
		final List<SpaceObject> within=new ArrayList<SpaceObject>(1);
		synchronized(space)
		{
			space.query(within, cube);
		}
		return within;
	}

	@Override
	public List<SpaceObject> getSpaceObjectsWithin(final SpaceObject ofObj, final long minDistance, final long maxDistance)
	{
		final List<SpaceObject> within=new ArrayList<SpaceObject>(1);
		if(ofObj==null)
			return within;
		synchronized(space)
		{
			space.query(within, new BoundedObject.BoundedCube(ofObj.coordinates(), maxDistance));
		}
		for (final Iterator<SpaceObject> o=within.iterator();o.hasNext();)
		{
			final SpaceObject O=o.next();
			if(O!=ofObj)
			{
				final long dist=Math.round(Math.abs(getDistanceFrom(O,ofObj) - O.radius() - ofObj.radius()));
				if((dist<minDistance)||(dist>maxDistance))
					o.remove();
			}
		}
		if(within.size()<=1)
			return within;
		Collections.sort(within, new Comparator<SpaceObject>()
		{
			@Override
			public int compare(final SpaceObject o1, final SpaceObject o2)
			{
				final long distTo1=getDistanceFrom(o1,ofObj);
				final long distTo2=getDistanceFrom(o2,ofObj);
				if(distTo1==distTo2)
					return 0;
				return distTo1>distTo2?1:-1;
			}
		});
		return within;
	}

	@Override
	public List<LocationRoom> getLandingPoints(final SpaceObject ship, final Environmental O)
	{
		final List<LocationRoom> rooms=new LinkedList<LocationRoom>();
		final Area A;
		if(O instanceof Area)
			A=(Area)O;
		else
		if(O instanceof Boardable)
			A=((Boardable)O).getArea();
		else
		if(O instanceof Room)
			A=((Room)O).getArea();
		else
			return rooms;
		for(final Enumeration<Room> r=A.getMetroMap();r.hasMoreElements();)
		{
			final Room R2=r.nextElement();
			if(R2 instanceof LocationRoom)
			{
				rooms.add((LocationRoom)R2);
			}
		}
		// TODO: really should look to see if there is something between the ship and
		// the landing point that could collide.
		Collections.sort(rooms,new Comparator<LocationRoom>()
		{
			final SpaceObject sship = ship;

			@Override
			public int compare(final LocationRoom o1, final LocationRoom o2)
			{
				if(o1.domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT)
				{
					if(o2.domainType()!=Room.DOMAIN_OUTDOORS_SPACEPORT)
						return -1;
				}
				else
				if(o2.domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT)
					return 1;
				if(ship != null)
				{
					final long distanceFrom1=getDistanceFrom(sship.coordinates(), o1.coordinates());
					final long distanceFrom2=getDistanceFrom(sship.coordinates(), o2.coordinates());
					if(distanceFrom1 > distanceFrom2)
						return 1;
					if(distanceFrom1 < distanceFrom2)
						return -1;
					return 0;
				}
				else
					return 0;
			}
		});
		return rooms;
	}

	@Override
	public int numSpaceObjects()
	{
		return space.count();
	}

	public static BigDecimal bigSqrt(final BigDecimal A)
	{
		if(A.doubleValue()<0)
			return ZERO;
		final int SCALE=50;
		BigDecimal x0 = BigDecimal.valueOf(0);
		BigDecimal x1 = BigDecimal.valueOf(Math.sqrt(A.doubleValue()));
		int times=0;
		while ((!x0.equals(x1))&&(++times<20))
		{
			x0 = x1;
			x1 = A.divide(x0, SCALE, RoundingMode.UP);
			x1 = x1.add(x0);
			x1 = x1.divide(TWO, SCALE, RoundingMode.UP);
		}
		return x1;
	}

	@Override
	public boolean canMaybeIntercept(final SpaceObject chaserO, final SpaceObject runnerO, final int maxTicks, final double maxSpeed)
	{
		final BoundedCube runB = runnerO.getBounds();
		runB.expand(runnerO.direction(), Math.round(CMath.mul(runnerO.speed(),maxTicks)));
		final BoundedCube chaB = runnerO.getBounds();
		chaB.expand(chaserO.direction(), Math.round(CMath.mul(maxSpeed,maxTicks)));
		return runB.intersects(chaB);
	}

	@Override
	public Pair<double[],Long> calculateIntercept(final SpaceObject chaserO, final SpaceObject runnerO, final long maxChaserSpeed, final int maxTicks)
	{
		if(maxTicks < 1)
			return null; // not possible, too late
		if((maxChaserSpeed>0)
		&&(runnerO.speed()>0))
		{
			final long distance = getDistanceFrom(chaserO, runnerO);
			final long speedToUse = maxChaserSpeed;
			if(distance < maxChaserSpeed)
			{
				final double[] dirTo = getDirection(chaserO, runnerO);
				return new Pair<double[], Long>(dirTo, Long.valueOf(distance));
			}
			final BigVector P0=new BigVector(runnerO.coordinates()); // runners position
			final BigVector P1=new BigVector(chaserO.coordinates()); // chasers position (torpedo/ship/whatever)
			final BigVector P0S=new BigVector(moveSpaceObject(runnerO.coordinates(), runnerO.direction(), Math.round(runnerO.speed())));
			final BigVector V0=P0S.subtract(P0);
			final BigDecimal S1=BigDecimal.valueOf(speedToUse);
			BigDecimal A=V0.dotProduct(V0).subtract(S1.multiply(S1));
			if(A.doubleValue()==0.0)
			{
				final BigVector V01=new BigVector(V0.x().add(BigDecimal.ONE),V0.y(),V0.z());
				A=V01.dotProduct(V01).subtract(S1.multiply(S1));
			}
			final BigDecimal B=TWO.multiply(P0.dotProduct(V0).add(P1.scalarProduct(ONE.negate()).dotProduct(V0)));
			final BigDecimal C=P0.dotProduct(P0).add(P1.dotProduct(P1)).add(P1.scalarProduct(TWO.negate()).dotProduct(P0));
			final BigDecimal T1 = B.negate().add(bigSqrt(B.multiply(B).subtract(FOUR.multiply(A).multiply(C)))).divide(TWO.multiply(A),BigVector.SCALE,RoundingMode.UP);
			final BigDecimal T2 = B.negate().subtract(bigSqrt(B.multiply(B).subtract(FOUR.multiply(A).multiply(C)))).divide(TWO.multiply(A),BigVector.SCALE,RoundingMode.UP);
			final BigDecimal T;
			if((T1.doubleValue() < 0)
			|| ((T2.doubleValue() < T1.doubleValue()) && (T2.doubleValue() >= 0)))
				T = T2;
			else
				T = T1;
			if(T.doubleValue()<=0)
				return null;
			final BigVector P0T = P0.add(V0.scalarProduct(T));
			final double[] finalDir = getDirection(chaserO.coordinates(),P0T.toLongs());
			return new Pair<double[], Long>(finalDir,Long.valueOf(maxChaserSpeed));
		}
		else
		if(chaserO.speed()>0) // runner isn't moving, so straight shot
		{
			final double[] dirTo = getDirection(chaserO, runnerO);
			return new Pair<double[], Long>(dirTo,Long.valueOf(maxChaserSpeed));
		}
		return null; // something is not
	}

	//@Override
	public Pair<double[],Long> calculateIntercept2(final SpaceObject chaserO, final SpaceObject runnerO, final long maxChaserSpeed, final int maxTicks)
	{
		if(maxTicks < 1)
			return null; // not possible, too late
		double[] dirTo = getDirection(chaserO, runnerO);
		if((maxChaserSpeed>0)
		&&(runnerO.speed()>0))
		{
			long distance = getDistanceFrom(chaserO, runnerO);
			long speedToUse = maxChaserSpeed;
			if(distance < maxChaserSpeed)
			{
				speedToUse = distance;
				return new Pair<double[], Long>(dirTo, Long.valueOf(speedToUse));
			}
			long curTicks = Math.round(CMath.div(distance, speedToUse));
			if((curTicks > maxTicks)||(curTicks==0))
				return null; // not enough time
			long newTicks = curTicks;
			curTicks = 0;
			long tries = 0;
			final long radius = runnerO.radius() + chaserO.radius();
			final long maxTries = (curTicks<100)?100:curTicks+1;
			while(++tries < maxTries)
			{
				curTicks = newTicks;
				long[] runnerCoords = runnerO.coordinates().clone();
				long[] chaserCoords = chaserO.coordinates().clone();
				chaserCoords=moveSpaceObject(chaserCoords, dirTo, speedToUse*(newTicks-1));
				runnerCoords=moveSpaceObject(runnerCoords, runnerO.direction(), Math.round(runnerO.speed())*newTicks-1);
				final long[] oldCoords = chaserCoords.clone();
				chaserCoords=moveSpaceObject(chaserCoords, dirTo, speedToUse);
				if(getMinDistanceFrom(oldCoords, chaserCoords, runnerCoords)<radius)
				{
					return new Pair<double[], Long>(dirTo, Long.valueOf(speedToUse));
				}

				dirTo = getDirection(chaserO.coordinates(), runnerCoords);
				distance = getDistanceFrom(chaserO.coordinates(), runnerCoords);
				newTicks = Math.round(CMath.div(distance, speedToUse))-1; // this is the absolute best I can do
				if(newTicks<2)
					newTicks=2;
				if(newTicks > maxTicks)
					return null; // not enough time
				if(newTicks == curTicks)
					newTicks = newTicks+1;
			}
			return new Pair<double[], Long>(dirTo,Long.valueOf(speedToUse));
		}
		else
		if(chaserO.speed()>0) // runner isn't moving, so straight shot
			return new Pair<double[], Long>(dirTo,Long.valueOf(maxChaserSpeed));
		return null; // something is not
	}

	protected final double getDirDiffSum(final double[] d1, final double d2[])
	{
		final double sum1=d1[0]>d2[0]?d1[0]-d2[0]:d2[0]-d1[0];
		final double sum2=d1[0]>d2[0]?(PI_TIMES_2-d1[0]+d2[0]):(PI_TIMES_2-d2[0]+d1[0]);
		final double sum3=d1[1]>d2[1]?d1[1]-d2[1]:d2[1]-d1[1];
		return sum1>sum2?(sum2+sum3):(sum1+sum3);
	}

	@Override
	public double getMinDistanceFrom(final long[] prevPos, final long[] curPos, final long[] objPos)
	{
		if(Arrays.equals(prevPos, curPos))
			return this.getDistanceFrom(curPos, objPos);
		final BigVector bigPrevPos = new BigVector(prevPos);
		final BigVector bigCurPos = new BigVector(curPos);
		final BigVector bigObjPos = new BigVector(objPos);

		final BigVector AB = bigCurPos.subtract(bigPrevPos);
		final BigVector BE = bigObjPos.subtract(bigCurPos);
		final BigVector AE = bigObjPos.subtract(bigPrevPos);

		if(AB.dotProduct(BE).doubleValue() > 0)
			return BE.magnitude().doubleValue();
		else
		if(AB.dotProduct(AE).doubleValue() < 0)
			return AE.magnitude().doubleValue();
		else
		{
			final BigVector bigDistance = bigPrevPos.subtract(bigCurPos);
			bigDistance.unitVectorFrom(); // divides each point by the vectors magnitude
			final BigDecimal dp = BE.dotProduct(bigDistance);
			return bigCurPos.add(bigDistance.scalarProduct(dp)).subtract(bigObjPos).magnitude().doubleValue();
		}
	}

	protected double getOldMinDistFrom(final long[] prevPos, final double speed, final double[] dir, final long[] curPosition,
									   final double[] directionTo, final long[] objPos)
	{
		final BigDecimal currentDistance=getBigDistanceFrom(curPosition, objPos);
		if(Arrays.equals(prevPos, curPosition))
			return currentDistance.doubleValue();
		final BigDecimal prevDistance=getBigDistanceFrom(prevPos, objPos);
		final BigDecimal baseDistance=BigDecimal.valueOf(speed);
		if(baseDistance.compareTo(currentDistance.add(prevDistance))>=0)
		{
			//Log.debugOut("0:prevDistance="+prevDistance.longValue()+", baseDistance="+baseDistance.longValue()+", currentDistance="+currentDistance.longValue());
			return 0;
		}
		if(prevDistance.subtract(baseDistance).equals(currentDistance)
		||currentDistance.subtract(baseDistance).equals(prevDistance))
		{
			//Log.debugOut("1:prevDistance="+prevDistance.longValue()+", baseDistance="+baseDistance.longValue()+", currentDistance="+currentDistance.longValue());
			return Math.min(prevDistance.doubleValue(), currentDistance.doubleValue());
		}
		//Log.debugOut("2:prevDistance="+prevDistance.longValue()+", baseDistance="+baseDistance.longValue()+", currentDistance="+currentDistance.longValue());
		final double[] travelDir = dir;
		final double[] prevDirToObject = getDirection(prevPos, objPos);
		final double diDelta=getDirDiffSum(travelDir,prevDirToObject);
		if(diDelta<ZERO_ALMOST)
		{
			final double[] currDirToObject = directionTo;
			final double fiDelta=getDirDiffSum(currDirToObject,prevDirToObject);
			if(fiDelta>ZERO_ALMOST)
				return 0;
			if(prevDistance.compareTo(currentDistance)>0)
				return currentDistance.doubleValue();
			else
				return prevDistance.doubleValue();
		}

		final BigDecimal semiPerimeter=currentDistance.add(prevDistance).add(baseDistance).divide(TWO, RoundingMode.HALF_UP);
		final BigDecimal partOfTriangle=semiPerimeter.multiply(semiPerimeter.subtract(currentDistance))
													.multiply(semiPerimeter.subtract(baseDistance))
													.multiply(semiPerimeter.subtract(prevDistance));

		final BigDecimal areaOfTriangle=bigSqrt(partOfTriangle);
		//Log.debugOut("3:semiPerimeter="+semiPerimeter.longValue()+", areaOfTriangle="+areaOfTriangle.doubleValue());
		if(areaOfTriangle.doubleValue()==0.0)
		{
			//Log.debugOut("3.5:semiPerimeter="+semiPerimeter.longValue()+", areaOfTriangle="+areaOfTriangle.doubleValue());
			if (semiPerimeter.subtract(baseDistance).abs().doubleValue() <= 1)
				return 0;
			else
				return Math.min(prevDistance.doubleValue(), currentDistance.doubleValue());
		}
		//Log.debugOut("4:getMinDistanceFrom="+TWO.multiply(areaOfTriangle).divide(baseDistance, RoundingMode.HALF_UP).doubleValue());
		if((baseDistance.multiply(ONE_THOUSAND).compareTo(currentDistance)<0)
		&&(baseDistance.multiply(ONE_THOUSAND).compareTo(prevDistance)<0))
			return Math.min(prevDistance.doubleValue(), currentDistance.doubleValue());
		return TWO.multiply(areaOfTriangle).divide(baseDistance, RoundingMode.HALF_UP).doubleValue();
	}

	@Override
	public ShipDir[] getCurrentBattleCoveredDirections(final ShipDirectional comp)
	{
		final ShipDir[] currCoverage;
		final ShipDir[] permitted = comp.getPermittedDirections();
		final int numDirs = comp.getPermittedNumDirections();
		if(numDirs >= permitted.length)
			currCoverage = comp.getPermittedDirections();
		else
		{
			final int centralIndex = CMLib.dice().roll(1, numDirs, -1);
			final List<ShipDir> theDirs = new ArrayList<ShipDir>(numDirs);
			int offset = 0;
			final List<ShipDir> permittedDirs = new XVector<ShipDir>(permitted);
			permittedDirs.addAll(Arrays.asList(permitted));
			permittedDirs.addAll(Arrays.asList(permitted));
			while(theDirs.size() < numDirs)
			{
				if(!theDirs.contains(permittedDirs.get(centralIndex+offset)))
					theDirs.add(permittedDirs.get(centralIndex+offset));
				if(!theDirs.contains(permittedDirs.get(centralIndex-offset)))
					theDirs.add(permittedDirs.get(centralIndex-offset));
				offset+=1;
			}
			currCoverage = theDirs.toArray(new ShipDir[theDirs.size()]);
		}
		return currCoverage;
	}


	protected BoundedCube makeCourseCubeRay(final long[] src, final long sradius,
											final long[] target, final long tradius,
											final double[] dir)
	{
		// never add source, it is implied!
		final long sgradius=Math.round(CMath.mul(sradius,(SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS)));
		final long tgradius=Math.round(CMath.mul(tradius,(SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS)));
		final long[] srcCoord = moveSpaceObject(src, dir, sgradius+1);
		final long[] tgtCoord = moveSpaceObject(target, getOppositeDir(dir), tgradius+1);
		final long distance = getDistanceFrom(srcCoord, tgtCoord);
		final BoundedCube courseRay = new BoundedCube(srcCoord, sgradius);
		if(courseRay.contains(tgtCoord)
		||courseRay.contains(srcCoord)
		||(distance <= sradius))
		{
			// this means we are already right on top of it, nowhere to go!
			return null;
		}
		courseRay.expand(dir, distance);
		return courseRay;
	}

	@Override
	public List<long[]> plotCourse(final long[] osrc, final long sradius, final long[] otarget, final long tradius, int maxTicks)
	{
		final List<long[]> course = new LinkedList<long[]>();
		final SpaceObject me = getSpaceObject(this, true);
		if(me == null)
			return course;
		long[] src=osrc.clone();
		long[] target = otarget.clone();
		BoundedCube courseRay;
		List<SpaceObject> objs;
		while(!Arrays.equals(src, target))
		{
			final double[] dir = getDirection(src, target);
			courseRay = makeCourseCubeRay(src, sradius, target, tradius,dir);
			if(courseRay == null)
				return course; // we are on top of the target, so done
			objs = getSpaceObjectsInBound(courseRay);
			double err = 1.0;
			int tries=100;
			while((objs.size()>0)&&(--tries>0))
			{
				err *= 2.0;
				final List<long[]> choices = new ArrayList<long[]>(4);
				for(int dd=0;dd<4;dd++)
				{
					if(objs.size()>0)
					{
						// find closest
						SpaceObject bobj = objs.get(0);
						long bobjdist = getDistanceFrom(src, bobj.coordinates());
						for(int i=1;i<objs.size();i++)
						{
							final SpaceObject notBobj = objs.get(i);
							final long notbobjdist = getDistanceFrom(src, notBobj.coordinates());
							if(notbobjdist > bobjdist)
							{
								bobjdist = notbobjdist;
								bobj = notBobj;
							}
						}
						final BigDecimal distanceToBobj = new BigDecimal(bobjdist);
						final double dsgradius = CMath.mul(sradius, err);
						final double dtgradius = CMath.mul(bobj.radius(),(SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS)) * err;
						final double dirDelta = new BigDecimal(Math.atan(dsgradius + dtgradius))
											.divide(distanceToBobj, BigVector.SCALE, RoundingMode.HALF_UP).doubleValue();
						final double[] newDir = dir.clone();
						switch(dd)
						{
						case 0:
							changeDirection(newDir, new double[] {dirDelta,0});
							break;
						case 1:
							changeDirection(newDir, new double[] {-dirDelta,0});
							break;
						case 2:
							changeDirection(newDir, new double[] {0,dirDelta});
							break;
						case 3:
							changeDirection(newDir, new double[] {0,-dirDelta});
							break;
						}
						final long[] newSubTarget = moveSpaceObject(src, newDir, distanceToBobj.longValue());
						courseRay = makeCourseCubeRay(src, sradius, newSubTarget, bobj.radius(), newDir);
						if(courseRay == null)
							return course; // we are on top of the target, so done
						objs = getSpaceObjectsInBound(courseRay);
						if(objs.size()==0)
							choices.add(newSubTarget);
					}
				}
				if(choices.size()>0)
				{
					target=choices.get(0);
					if(choices.size()>1)
					{
						long dist = getDistanceFrom(target, otarget);
						for(int i=1;i<choices.size();i++)
						{
							final long dist2 = this.getDistanceFrom(choices.get(i), otarget);
							if(dist2<dist)
							{
								target=choices.get(i);
								dist=dist2;
							}
						}
					}
					objs.clear();
					break;
				}
			}
			if(objs.size()==0)
			{
				course.add(target); // WIN!
				src = target.clone();
				target = otarget.clone();
				if(--maxTicks<=0)
					return course;
			}
			else
				break;
		}
		return course;
	}




	@Override
	public double getGravityForce(final SpaceObject S, final SpaceObject cO)
	{
		final long distance=getDistanceFrom(S.coordinates(), cO.coordinates()) - cO.radius();
		final long oMass = S.getMass();
		if(((cO instanceof Area)||(cO.getMass() >= SpaceObject.ASTEROID_MASS))
		&&(distance > 0)
		&&(oMass < SpaceObject.MOONLET_MASS))
		{
			final double graviRadiusMax=(cO.radius()*(SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS-1.0));
			if(distance<graviRadiusMax)
			{
				return 1.0-(distance/graviRadiusMax);
			}
		}
		return 0;
	}

	@Override
	public boolean sendSpaceEmissionEvent(final SpaceObject srcP, final Environmental tool, final int emissionType, final String msgStr)
	{
		return sendSpaceEmissionEvent(srcP, tool, 30L*SpaceObject.Distance.LightSecond.dm, emissionType, msgStr);
	}

	@Override
	public boolean sendSpaceEmissionEvent(final SpaceObject srcP, final Environmental tool, final long range, final int emissionType, final String msgStr)
	{
		final List<SpaceObject> cOs=getSpaceObjectsWithin(srcP, 0, range);
		if(cOs.size()==0)
			return false;
		final MOB deityM=CMLib.map().deity();
		final CMMsg msg=CMClass.getMsg(deityM, srcP, tool, CMMsg.MSG_EMISSION, null, CMMsg.MSG_EMISSION, null, emissionType, msgStr);
		if(srcP.okMessage(srcP, msg))
		{
			for(final SpaceObject cO : cOs)
				cO.executeMsg(srcP, msg);
			if(!cOs.contains(srcP))
				srcP.executeMsg(srcP, msg);
			return true;
		}
		return false;
	}

	protected void swapTargetTool(final CMMsg msg)
	{
		final Environmental E=msg.target();
		msg.setTarget(msg.tool());
		msg.setTool(E);
	}

	private static final Comparator<SpaceObject> speedSorter = new Comparator<SpaceObject>() {

		@Override
		public int compare(final SpaceObject o1, final SpaceObject o2)
		{
			if(o1.speed()==o2.speed())
				return 0;
			return (o1.speed()>o2.speed())?-1:1;
		}
	};

	public void runSpace()
	{
		final boolean isDebuggingHARD=CMSecurity.isDebugging(DbgFlag.SPACEMOVES);
		final boolean isDebugging=CMSecurity.isDebugging(DbgFlag.SPACESHIP)||isDebuggingHARD;
		final Vector<SpaceObject> space = new XVector<SpaceObject>(getSpaceObjects());
		Collections.sort(space, speedSorter);
		for(final Enumeration<SpaceObject> o = space.elements(); o.hasMoreElements(); )
		{
			final SpaceObject O=o.nextElement();
			if(!(O instanceof Area))
			{
				final SpaceShip S=(O instanceof SpaceShip)?(SpaceShip)O:null;
				if((S!=null)
				&&(S.getArea()!=null)
				&&(S.getArea().getAreaState()!=Area.State.ACTIVE))
					continue;
				BoundedCube cube=O.getBounds();
				final double speed=O.speed();
				final long[] startCoords=O.coordinates().clone();
				final boolean moving;
				if(speed>=1)
				{
					cube=cube.expand(O.direction(),(long)speed);
					moveSpaceObject(O);
					moving=true;
				}
				else
					moving=false;
				// why are we doing all this for an object that's not even moving?!  Because gravity!
				//TODO: passify stellar objects that never show up on each others gravitational map, for a period of time
				boolean inAirFlag = false;
				final List<SpaceObject> cOs=getSpaceObjectsWithin(O, 0, Math.max(4*SpaceObject.Distance.LightSecond.dm,2*Math.round(speed)));
				final long oMass = O.getMass();
				// objects should already be sorted by closeness for good collision detection
				if(isDebuggingHARD && moving)
					Log.debugOut("Space Object "+O.name()+" moved "+speed+" in dir " +CMLib.english().directionDescShort(O.direction())+" to "+CMLib.english().coordDescShort(O.coordinates()));

				for(final SpaceObject cO : cOs)
				{
					if((cO != O)
					&&(!cO.amDestroyed())
					&&(!O.amDestroyed()))
					{
						final double minDistance=getMinDistanceFrom(startCoords, O.coordinates(), cO.coordinates());
						if(isDebuggingHARD && moving)
						{
							final long dist = CMLib.space().getDistanceFrom(O, cO);
							Log.debugOut("Space Object "+O.name()+" is "+CMLib.english().distanceDescShort(dist)+" from "+cO.Name()+", minDistance="+CMLib.english().distanceDescShort(Math.round(minDistance)));
						}
						final double gravitationalMove=getGravityForce(O, cO);
						if(gravitationalMove > 0)
						{
							if(isDebugging)
								Log.debugOut("SpaceShip "+O.name()+" is gravitating "+gravitationalMove+" towards " +cO.Name());
							final double[] directionTo=getDirection(O, cO);
							accelSpaceObject(O, directionTo, gravitationalMove);
							inAirFlag = true;
						}
						if((O instanceof Weapon)&&(isDebugging) && moving)
						{
							final long dist = CMLib.space().getDistanceFrom(O, cO);
							Log.debugOut("SpaceShip Weapon "+O.Name()+" closest distance is "+minDistance+" to "+cO.name()+"@("+CMParms.toListString(cO.coordinates())+"):<"+(O.radius()+cO.radius())+"/"+dist);
						}
						if ((minDistance<(O.radius()+cO.radius()))
						&&((speed>0)||(cO.speed()>0))
						&&((oMass < SpaceObject.MOONLET_MASS)||(cO.getMass() < SpaceObject.MOONLET_MASS)))
						{
							final MOB host=CMLib.map().deity();
							final CMMsg msg;
							O.setCoords(cO.coordinates()); // during a collision, the moving thing stops!
							if(O instanceof Weapon)
							{
								if(isDebugging) Log.debugOut("Weapon "+O.name()+" collided with "+cO.Name());
								final Integer weaponDamageType = Weapon.MSG_TYPE_MAP.get(Integer.valueOf(((Weapon)O).weaponDamageType()));
								final int srcMinor = CMMsg.MASK_MOVE|CMMsg.MASK_SOUND|(weaponDamageType!=null?weaponDamageType.intValue():CMMsg.TYP_COLLISION);
								msg=CMClass.getMsg(host, O, cO, srcMinor,CMMsg.MSG_DAMAGE,CMMsg.MSG_COLLISION,null);
								msg.setValue(((Weapon)O).phyStats().damage());
							}
							else
							if(cO instanceof Weapon)
							{
								if(cO.knownSource() == O) // a ship can run into its weapon?!
									continue;
								if(isDebugging) Log.debugOut("Space Object "+O.name()+" collided with weapon "+cO.Name());
								final Integer weaponDamageType = Weapon.MSG_TYPE_MAP.get(Integer.valueOf(((Weapon)cO).weaponDamageType()));
								final int srcMinor = CMMsg.MASK_MOVE|CMMsg.MASK_SOUND|(weaponDamageType!=null?weaponDamageType.intValue():CMMsg.TYP_COLLISION);
								msg=CMClass.getMsg(host, cO, O, srcMinor,CMMsg.MSG_DAMAGE,CMMsg.MSG_COLLISION,null);
								msg.setValue(((Weapon)cO).phyStats().damage());
							}
							else
							{
								if(isDebugging) Log.debugOut("Space Object "+O.name()+" collided with "+cO.Name());
								msg=CMClass.getMsg(host, O, cO, CMMsg.MSG_COLLISION,null);
							}
							if(msg.target().okMessage(host, msg))
							{
								swapTargetTool(msg);
								if(msg.target().okMessage(host, msg))
								{
									swapTargetTool(msg);
									msg.target().executeMsg(host, msg);
									swapTargetTool(msg);
									msg.target().executeMsg(host, msg);
								}
							}
						}
					}
				}
				if(S!=null)
				{
					S.setShipFlag(SpaceShip.ShipFlag.IN_THE_AIR,inAirFlag);
				}
			}
		}
	}

	@Override
	public boolean activate()
	{
		if(serviceClient==null)
		{
			name="THSpace"+Thread.currentThread().getThreadGroup().getName().charAt(0);
			serviceClient=CMLib.threads().startTickDown(this, Tickable.TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK, CMProps.getTickMillis(), 1);
		}
		return true;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		try
		{
			if(!CMSecurity.isDisabled(CMSecurity.DisFlag.SPACETHREAD))
			{
				isDebugging=CMSecurity.isDebugging(DbgFlag.UTILITHREAD);
				tickStatus=Tickable.STATUS_ALIVE;
				runSpace();
			}
		}
		finally
		{
			tickStatus=Tickable.STATUS_NOT;
			setThreadStatus(serviceClient,"sleeping");
		}
		return true;
	}

	@Override
	public boolean shutdown()
	{
		space.clear();
		sectorMap.clear();
		if(CMLib.threads().isTicking(this, TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK))
		{
			CMLib.threads().deleteTick(this, TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK);
			serviceClient=null;
		}
		return true;
	}

}
