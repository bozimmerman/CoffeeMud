package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.exceptions.BadEmailAddressException;
import com.planet_ink.coffee_mud.core.interfaces.*;
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
   Copyright 2013-2024 Bo Zimmerman

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
	protected static final double		PI_TIMES_2				= Math.PI * 2.0;

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
	public void addObjectToSpace(final SpaceObject O, final Coord3D coords)
	{
		synchronized(space)
		{
			O.setCoords(coords.copyOf());
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
	public long getDistanceFrom(final Coord3D coord1, final Coord3D coord2)
	{
		final BigDecimal coord_0 = coord1.x().subtract(coord2.x());
		final BigDecimal coord_0m = coord_0.multiply(coord_0);
		final BigDecimal coord_1 = coord1.y().subtract(coord2.y());
		final BigDecimal coord_1m = coord_1.multiply(coord_1);
		final BigDecimal coord_2 = coord1.z().subtract(coord2.z());
		final BigDecimal coord_2m = coord_2.multiply(coord_2);
		final BigDecimal coords_all = coord_0m.add(coord_1m).add(coord_2m);
		return Math.round(Math.sqrt(coords_all.doubleValue()));
	}

	@Override
	public long getDistanceFrom(final SpaceObject O1, final SpaceObject O2)
	{
		return getDistanceFrom(O1.coordinates(),O2.coordinates());
	}

	@Override
	public Dir3D[] getPerpendicularAngles(final Dir3D angle)
	{
		final List<Dir3D> set = new ArrayList<Dir3D>(5);
		if(angle.z().compareTo(BigCMath.PI_BY_2)>0)
			set.add(new Dir3D( angle.xy(), angle.z().subtract(BigCMath.PI_BY_2)));
		else
		if(angle.z().compareTo(BigCMath.PI_BY_2)<0)
			set.add(new Dir3D ( angle.xy(), angle.z().add(BigCMath.PI_BY_2) ));

		final BigDecimal angle10 = angle.z().compareTo(BigCMath.PI_BY_2) > 0 ?  angle.z().subtract(BigCMath.PI_BY_2) : BigCMath.PI_BY_2.subtract(angle.z());
		BigDecimal angle00 = angle.xy().add(BigCMath.PI_BY_2);
		if(angle00.compareTo(BigCMath.PI_TIMES_2) >=0)
			angle00 = angle00.subtract(BigCMath.PI_TIMES_2);
		set.add(new Dir3D (angle00, angle10 ));

		BigDecimal angle01 = angle.xy().add(BigCMath.PI);
		if(angle01.compareTo(BigCMath.PI_TIMES_2) >=0)
			angle01 = angle01.subtract(BigCMath.PI_TIMES_2);
		set.add(new Dir3D(angle01, BigCMath.PI_BY_2));

		BigDecimal angle02 = angle.xy().subtract(BigCMath.PI_BY_2);
		if(angle02.compareTo(BigCMath.ZERO) < 0)
			angle02 = angle02.add(BigCMath.PI_TIMES_2);
		set.add(new Dir3D (angle02, angle10 ));

		set.add(CMLib.space().getOppositeDir(angle));
		return set.toArray(new Dir3D[set.size()]);
	}

	@Override
	public Coord3D[] getPerpendicularPoints(final Coord3D origin, final Dir3D angle, final long distance)
	{
		final Dir3D[] angles = getPerpendicularAngles(angle);
		final Coord3D[] points = new Coord3D[angles.length];
		for(int i=0;i<angles.length;i++)
		{
			final Dir3D a = angles[i];
			points[i] = moveSpaceObject(origin, a, distance);
		}
		return points;
	}

	protected BigDecimal getBigDistanceFrom(final Coord3D coord1, final Coord3D coord2)
	{
		final BigDecimal coord_0 = coord1.x().subtract(coord2.x());
		final BigDecimal coord_0m = coord_0.multiply(coord_0);
		final BigDecimal coord_1 = coord1.y().subtract(coord2.y());
		final BigDecimal coord_1m = coord_1.multiply(coord_1);
		final BigDecimal coord_2 = coord1.z().subtract(coord2.z());
		final BigDecimal coord_2m = coord_2.multiply(coord_2);
		final BigDecimal coords_all = coord_0m.add(coord_1m).add(coord_2m);
		final BigDecimal val = BigCMath.sqrt(coords_all);
		return val;
	}

	@Override
	public String getSectorName(final Coord3D coords)
	{
		final String[] xsecs=CMProps.getListFileStringList(CMProps.ListFile.TECH_SECTOR_X_NAMES);
		final String[] ysecs=CMProps.getListFileStringList(CMProps.ListFile.TECH_SECTOR_Y_NAMES);
		final String[] zsecs=CMProps.getListFileStringList(CMProps.ListFile.TECH_SECTOR_Z_NAMES);

		final long dmsPerXSector = (SpaceObject.Distance.GalaxyRadius.dm / (xsecs.length)) * 2L;
		final long dmsPerYSector = (SpaceObject.Distance.GalaxyRadius.dm / (ysecs.length)) * 2L;
		final long dmsPerZSector = (SpaceObject.Distance.GalaxyRadius.dm / (zsecs.length)) * 2L;

		final long secDeX = coords.x().longValue() / dmsPerXSector;
		final long secDeY = coords.y().longValue() / dmsPerYSector;
		final long secDeZ = coords.z().longValue() / dmsPerZSector;

		final StringBuilder sectorName = new StringBuilder("");
		sectorName.append(xsecs[(int)secDeX + xsecs.length/2]).append(" ");
		sectorName.append(ysecs[(int)secDeY + ysecs.length/2]).append(" ");
		sectorName.append(zsecs[(int)secDeZ + zsecs.length/2]);
		return sectorName.toString();
	}

	@Override
	public Coord3D getInSectorCoords(final Coord3D coords)
	{
		final String[] xsecs=CMProps.getListFileStringList(CMProps.ListFile.TECH_SECTOR_X_NAMES);
		final String[] ysecs=CMProps.getListFileStringList(CMProps.ListFile.TECH_SECTOR_Y_NAMES);
		final String[] zsecs=CMProps.getListFileStringList(CMProps.ListFile.TECH_SECTOR_Z_NAMES);

		final long dmsPerXSector = (SpaceObject.Distance.GalaxyRadius.dm / (xsecs.length)) * 2L;
		final long dmsPerYSector = (SpaceObject.Distance.GalaxyRadius.dm / (ysecs.length)) * 2L;
		final long dmsPerZSector = (SpaceObject.Distance.GalaxyRadius.dm / (zsecs.length)) * 2L;

		final Coord3D sectorCoords = coords.copyOf();
		if(sectorCoords.x().longValue()<0)
			sectorCoords.x(sectorCoords.x().negate());
		if(sectorCoords.y().longValue()<0)
			sectorCoords.y(sectorCoords.y().negate());
		if(sectorCoords.z().longValue()<0)
			sectorCoords.z(sectorCoords.z().negate());
		sectorCoords.x(sectorCoords.x().longValue() % dmsPerXSector);
		sectorCoords.y(sectorCoords.y().longValue() % dmsPerYSector);
		sectorCoords.z(sectorCoords.z().longValue() % dmsPerZSector);
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
						final Coord3D coords = new Coord3D(new long[] {x, y, z});
						final BoundedCube cube = new BoundedCube(x,x+dmsPerXSector,y,y+dmsPerYSector,z,z+dmsPerZSector);
						final String name = getSectorName(coords);
						if(tempMap.containsKey(name) || (coords.z().longValue()<0L))
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
						final Coord3D coords = new Coord3D(new long[] {-x, -y, -z});
						final BoundedCube cube = new BoundedCube(-x,-x+dmsPerXSector,-y,-y+dmsPerYSector,-z,-z+dmsPerZSector);
						final String name = getSectorName(coords);
						if(tempMap.containsKey(name) || (coords.z().longValue()>0L))
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
	public void accelSpaceObject(final SpaceObject O, final Dir3D accelDirection, final double newAcceleration)
	{
		final double newSpeed = accelSpaceObject(O.direction(),O.speed(),accelDirection,newAcceleration);
		O.setSpeed(newSpeed);
	}

	@Override
	public double getAngleDelta(final Dir3D fromAngle, final Dir3D toAngle)
	{
		if(fromAngle.equals(toAngle))
			return 0.0;
		final BigVector from;
		try
		{
			from = new BigVector(fromAngle).sphereToCartesian();
		}
		catch(final Exception e)
		{
			Log.errOut("AngleDelta",(fromAngle.xy()+","+fromAngle.z()));
			Log.errOut("AngleDelta",e);
			return 0.0;
		}
		final BigVector to = new BigVector(toAngle).sphereToCartesian();
		BigDecimal dotProd = from.dotProduct(to);
		if(dotProd.compareTo(BigCMath.ONE)>0)
			dotProd=BigCMath.TWO.subtract(dotProd);
		if(dotProd.compareTo(BigCMath.MIN_ONE)<0)
			dotProd=BigCMath.MIN_ONE.multiply(dotProd).subtract(BigCMath.TWO);
		//final BigDecimal fromag = from.magnitude();
		//final BigDecimal tomag = to.magnitude();
		final double finalDelta = Math.acos(dotProd.doubleValue());
		if(Double.isNaN(finalDelta) || Double.isInfinite(finalDelta))
		{
			Log.errOut("NaN finalDelta = "+ finalDelta+"= ("+fromAngle.xy()+","+fromAngle.z()+") -> ("+toAngle.xy()+","+toAngle.z()+")");
			Log.errOut("NaN dotprod = " + dotProd+", from = " + from + ", to=" +to);
			throw new java.lang.IllegalArgumentException();
		}
		return finalDelta;
	}
	
	protected BigDecimal getShortestYawDelta(final BigDecimal correctAngle, final BigDecimal wrongAngle)
	{
		BigDecimal xyd = correctAngle.subtract(wrongAngle);
		final int zcp = xyd.compareTo(BigCMath.ZERO);
		final boolean add;
		add = ((zcp>0) && (xyd.compareTo(BigCMath.PI)>0))
			||((zcp<0) && (xyd.compareTo(BigCMath.PI.negate())>0));
		if(add)
		{
			if(zcp<0)
				xyd = wrongAngle.subtract(correctAngle);
			else
				xyd = BigCMath.PI_TIMES_2.subtract(correctAngle).add(wrongAngle);
		}
		else
		if(zcp<0)
			xyd = BigCMath.PI_TIMES_2.subtract(wrongAngle).add(correctAngle);
		return add?xyd:xyd.negate();
	}

	@Override
	public Dir3D getMiddleAngle(final Dir3D angle1, final Dir3D angle2)
	{
		final Dir3D middleAngle = new Dir3D (angle1.xy(), angle1.z());
		if(!angle1.xy().equals(angle2.xy()))
		{
			final BigDecimal xyd = getShortestYawDelta(angle1.xy(),angle2.xy());
			middleAngle.xy(middleAngle.xy().add(xyd.divide(BigCMath.TWO,Dir3D.SCALE,RoundingMode.UP)));
		}
		middleAngle.z((angle1.z().add(angle2.z())).divide(BigCMath.TWO,Dir3D.SCALE,RoundingMode.UP));
		return middleAngle;
	}

	@Override
	public Dir3D getOffsetAngle(final Dir3D correctAngle, final Dir3D wrongAngle)
	{
		final Dir3D offsetAngles = new Dir3D (correctAngle.xy(), correctAngle.z());
		if(!correctAngle.xy().equals(wrongAngle.xy()))
		{
			final BigDecimal xyd = getShortestYawDelta(correctAngle.xy(),wrongAngle.xy());
			offsetAngles.xy(offsetAngles.xy().subtract(xyd));
		}
		if(!correctAngle.z().equals(wrongAngle.z()))
		{
			final BigDecimal xy1 = correctAngle.z().compareTo(wrongAngle.z())>0?correctAngle.z():wrongAngle.z();
			final BigDecimal xy2 = xy1.equals(correctAngle.z()) ? wrongAngle.z() : correctAngle.z();
			offsetAngles.z(xy1.subtract(xy2));
			if(wrongAngle.z().compareTo(correctAngle.z()) > 0)
				offsetAngles.z(correctAngle.z().subtract(offsetAngles.z()));
			else
				offsetAngles.z(correctAngle.z().add(offsetAngles.z()));
		}
		return offsetAngles;
	}

	@Override
	public Dir3D getExaggeratedAngle(final Dir3D correctAngle, final Dir3D wrongAngle)
	{
		final Dir3D exaggAngle = new Dir3D (wrongAngle.xy(), wrongAngle.z());
		if(!correctAngle.xy().equals(wrongAngle.xy()))
		{
			final BigDecimal xyd = getShortestYawDelta(correctAngle.xy(),wrongAngle.xy());
			exaggAngle.xy(exaggAngle.xy().add(xyd));
		}
		if(!correctAngle.z().equals(wrongAngle.z()))
		{
			final BigDecimal zd = correctAngle.z().subtract(wrongAngle.z()).abs();
			if(correctAngle.z().compareTo(wrongAngle.z())<0)
				exaggAngle.z(exaggAngle.z().add(zd));
			else
				exaggAngle.z(exaggAngle.z().subtract(zd).abs());
		}
		return exaggAngle;
	}

	@Override
	public void applyAngleDiff(final Dir3D angle, final Dir3D delta)
	{
		angle.xy(angle.xy().add(delta.xy()));
		angle.z(angle.z().add(delta.z()));
	}

	@Override
	public Dir3D getAngleDiff(final Dir3D fromAngle, final Dir3D toAngle)
	{
		final Dir3D delta = new Dir3D(false); // not safe, because negatives OK
		delta.xy(toAngle.xy().subtract(fromAngle.xy()));
		if(delta.xy().compareTo(BigCMath.PI) > 0)
			delta.xy(BigCMath.PI_TIMES_2.subtract(delta.xy()));
		else
		if(delta.xy().compareTo(BigCMath.PI.negate()) < 0)
			delta.xy(BigCMath.PI_TIMES_2.add(delta.xy()));
		delta.z(toAngle.z().subtract(fromAngle.z()));
		return delta;
	}

	@Override
	public double accelSpaceObject(final Dir3D curDirection, final double curSpeed, final Dir3D accelDirection, final double newAcceleration)
	{
		if(newAcceleration <= 0.0)
			return curSpeed;

		final BigDecimal curDirectionYaw = curDirection.xy();
		final BigDecimal curDirectionPitch = curDirection.z();

		final BigDecimal accelDirectionYaw = accelDirection.xy();
		final BigDecimal accelDirectionPitch = accelDirection.z();

		final BigDecimal currentSpeed = BigDecimal.valueOf(curSpeed);
		final BigDecimal acceleration = BigDecimal.valueOf(newAcceleration);

		final BigDecimal anglesDelta =  BigDecimal.valueOf(getAngleDelta(curDirection, accelDirection));
		if((anglesDelta.subtract(BigCMath.PI).abs().compareTo(BigCMath.ZERO_ALMOST)<=0)
		&&(currentSpeed.compareTo(acceleration)>0))
			return currentSpeed.subtract(acceleration).doubleValue();
		
		final BigDecimal xyd = getShortestYawDelta(curDirectionYaw,accelDirectionYaw);
		final BigDecimal yawSign = (xyd.signum() >= 0) ? BigCMath.ONE : BigCMath.MIN_ONE;
		final BigDecimal yawDelta = xyd.abs();
		final BigDecimal zd = curDirectionPitch.subtract(accelDirectionPitch);
		final BigDecimal pitchSign = (zd.signum() >= 0) ? BigCMath.MIN_ONE : BigCMath.ONE;
		final BigDecimal pitchDelta = zd.abs();
		BigDecimal newDirectionYaw;
		BigDecimal newDirectionPitch;
		final BigDecimal deltaMultiplier =  Dir3D.sin(anglesDelta);//BigCMath.sqrt(Dir3D.sin(anglesDelta));
		final BigDecimal yawMin =  deltaMultiplier.multiply((BigCMath.POINT01.add(yawDelta.multiply(BigCMath.ONEPOINT01.subtract(BigDecimal.valueOf(Math.sin(curDirectionPitch.doubleValue())))))));
		BigDecimal accelerationMultiplier;
		if(currentSpeed.compareTo(BigCMath.ZERO)==0)
			accelerationMultiplier = BigCMath.ONE;
		else
		if(currentSpeed.compareTo(acceleration)<=0)
			accelerationMultiplier = BigCMath.ONE;
		else
		{
			accelerationMultiplier = acceleration.divide(currentSpeed,Dir3D.SCALE,RoundingMode.UP).multiply(deltaMultiplier,MathContext.DECIMAL128);
			if(accelerationMultiplier.compareTo(BigCMath.POINT2)<0)
				accelerationMultiplier=BigCMath.POINT2;
		}
		if((yawDelta.compareTo(yawMin) <= 0))
			newDirectionYaw = accelDirectionYaw;
		else
		{
			BigDecimal nearFinalYawDelta = Dir3D.sin(yawDelta).multiply(accelerationMultiplier,MathContext.DECIMAL128);
			if((nearFinalYawDelta.compareTo(yawMin)<0)&&(yawDelta.compareTo(yawMin)>0))
				nearFinalYawDelta = yawMin;
			newDirectionYaw = curDirectionYaw.add(nearFinalYawDelta.multiply(yawSign));
			if((newDirectionYaw.compareTo(BigCMath.ZERO) > 0) && ((BigCMath.PI_TIMES_2.subtract(newDirectionYaw)).compareTo(BigCMath.ZERO_ALMOST)<0))
				newDirectionYaw=BigCMath.ZERO;
		}
		final BigDecimal pitchMin = BigCMath.POINT1;
		if(pitchDelta.compareTo(pitchMin)<=0)
			newDirectionPitch = accelDirectionPitch;
		else
		{
			BigDecimal nearFinalPitchDelta = BigDecimal.valueOf(Math.sin(pitchDelta.doubleValue())).multiply(accelerationMultiplier);
			if((nearFinalPitchDelta.compareTo(pitchMin)<0)&&(pitchDelta.compareTo(pitchMin)>0))
				nearFinalPitchDelta = pitchMin;
			newDirectionPitch = curDirectionPitch.add(nearFinalPitchDelta.multiply(pitchSign));
		}
		BigDecimal newSpeed = currentSpeed.add(acceleration.multiply(Dir3D.cos(anglesDelta)));
		if(newSpeed.compareTo(BigCMath.ZERO)<0) // cos >=180deg is a negative number, so negative acceleration, new direction
		{
			newSpeed = newSpeed.negate();
			newDirectionYaw = accelDirectionYaw;
			newDirectionPitch = accelDirectionPitch;
		}
		curDirection.xy(newDirectionYaw);
		curDirection.z(newDirectionPitch);
		return newSpeed.doubleValue();
	}

	@Override
	public void changeDirection(final Dir3D dir, final double delta0, final double delta1)
	{
		dir.xy(dir.xy().add(BigDecimal.valueOf(delta0 % PI_TIMES_2)));
		dir.z(dir.z().add(BigDecimal.valueOf(delta1 % Math.PI)));
	}

	@Override
	public void changeDirection(final Dir3D dir, final Dir3D delta)
	{
		dir.xy(dir.xy().add(BigDecimal.valueOf(delta.xy().doubleValue() % PI_TIMES_2)));
		dir.z(dir.z().add(BigDecimal.valueOf(delta.z().doubleValue() % Math.PI)));
	}

	@Override
	public Dir3D getOppositeDir(final Dir3D dir)
	{
		return new Dir3D(BigCMath.PI.add(dir.xy()),BigCMath.PI.subtract(dir.z()));
	}

	@Override
	public ShipDirectional.ShipDir getDirectionFromDir(final Dir3D facing, final double roll, final Dir3D direction)
	{
		//Log.debugOut("facing="+(Math.toDegrees(facing.xy()) % 360.0)+","+(Math.toDegrees(facing.z()) % 180.0));
		//Log.debugOut("direction="+(Math.toDegrees(direction.xy()) % 360.0)+","+(Math.toDegrees(direction.z()) % 180.0));
		double yD = ((Math.toDegrees(facing.xy().doubleValue()) % 360.0) - (Math.toDegrees(direction.xy().doubleValue()) % 360.0)) % 360.0;
		if(yD < 0)
			yD = 360.0 + yD;
		final double pD = Math.abs(((Math.toDegrees(facing.z().doubleValue()) % 180.0) - (Math.toDegrees(direction.z().doubleValue()) % 180.0)) % 180.0);
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
	public ShipDirectional.ShipDir getAbsoluteDirectionalFromDir(final Dir3D direction)
	{
		double yD = Math.toDegrees(direction.xy().doubleValue()) % 360.0;
		if(yD < 0)
			yD = 360.0 + yD;
		double rD = Math.toDegrees(direction.z().doubleValue()) % 180.0;
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
	public Dir3D getDirection(final SpaceObject fromObj, final SpaceObject toObj)
	{
		return getDirection(fromObj.coordinates(),toObj.coordinates());
	}

	protected void moveSpaceObject(final SpaceObject O, final BigDecimal x, final BigDecimal y, final BigDecimal z)
	{
		synchronized(space)
		{
			final boolean reAdd=space.contains(O);
			if(reAdd)
				space.remove(O);
			O.coordinates().x(x);
			O.coordinates().y(y);
			O.coordinates().z(z);
			if(reAdd)
				space.insert(O);
		}
	}

	@Override
	public void moveSpaceObject(final SpaceObject O, final Coord3D coords)
	{
		moveSpaceObject(O, coords.x(), coords.y(), coords.z());
	}

	@Override
	public void moveSpaceObject(final SpaceObject O)
	{
		if(O.speed()>0)
		{
			final BigDecimal speed = new BigDecimal(O.speed());
			final BigDecimal x1=Dir3D.cos(O.direction().xy()).multiply(Dir3D.sin(O.direction().z()));
			final BigDecimal y1=Dir3D.sin(O.direction().xy()).multiply(Dir3D.sin(O.direction().z()));
			final BigDecimal z1=Dir3D.cos(O.direction().z());
			moveSpaceObject(O,x1.multiply(speed).add(O.coordinates().x()),
								y1.multiply(speed).add(O.coordinates().y()),
								z1.multiply(speed).add(O.coordinates().z()));
		}
	}

	@Override
	public Dir3D getDirection(final Coord3D fromCoords, final Coord3D toCoords)
	{
		return getBigDirection(fromCoords, toCoords);
	}

	protected Dir3D getBigDirection(final Coord3D fromCoords, final Coord3D toCoords)
	{
		final Dir3D dir=new Dir3D();
		final BigDecimal x=toCoords.x().subtract(fromCoords.x());
		final BigDecimal y=toCoords.y().subtract(fromCoords.y());
		final BigDecimal z=toCoords.z().subtract(fromCoords.z());
		final BigDecimal xy = x.multiply(x).add(y.multiply(y));
		if((x.doubleValue()!=0)||(y.doubleValue()!=0))
		{
			final BigDecimal sqrtxy = BigCMath.sqrt(xy);
			final BigDecimal ybysqrtxy=y.divide(sqrtxy,50,RoundingMode.HALF_EVEN);
			if(x.doubleValue()<0)
				dir.xy(Math.PI-Math.asin(ybysqrtxy.doubleValue()));
			else
				dir.xy(Math.asin(ybysqrtxy.doubleValue()));
		}
		if((x.doubleValue()!=0)||(y.doubleValue()!=0)||(z.doubleValue()!=0))
			dir.z(Math.acos(z.divide(BigCMath.sqrt(z.multiply(z).add(xy)),50,RoundingMode.HALF_EVEN).doubleValue()));
		return dir;
	}

	@Override
	public Coord3D moveSpaceObject(final Coord3D coordinates, final Dir3D direction, final long speed)
	{
		if(speed>0)
		{
			final BigDecimal bigSpeed = new BigDecimal(speed);
			final BigDecimal x1=Dir3D.cos(direction.xy()).multiply(Dir3D.sin(direction.z()));
			final BigDecimal y1=Dir3D.sin(direction.xy()).multiply(Dir3D.sin(direction.z()));
			final BigDecimal z1=Dir3D.cos(direction.z());
			return new Coord3D(coordinates.x().add(x1.multiply(bigSpeed)),
							coordinates.y().add(y1.multiply(bigSpeed)),
							coordinates.z().add(z1.multiply(bigSpeed)));
		}
		return coordinates;
	}

	@Override
	public Coord3D getLocation(final Coord3D oldLocation, final Dir3D direction, final long distance)
	{
		final BigDecimal bigDistance = new BigDecimal(distance);
		final BigDecimal x1=Dir3D.cos(direction.xy()).multiply(Dir3D.sin(direction.z()));
		final BigDecimal y1=Dir3D.sin(direction.xy()).multiply(Dir3D.sin(direction.z()));
		final BigDecimal z1=Dir3D.cos(direction.z());
		final Coord3D location=oldLocation.copyOf();
		location.x(oldLocation.x().add(bigDistance.multiply(x1)));
		location.y(oldLocation.y().add(bigDistance.multiply(y1)));
		location.z(oldLocation.z().add(bigDistance.multiply(z1)));
		return location;
	}

	@Override
	public long getRelativeSpeed(final SpaceObject O1, final SpaceObject O2)
	{
		final BigDecimal speed1 = new BigDecimal(O1.speed());
		final BigDecimal speed2 = new BigDecimal(O1.speed());
		final BigDecimal x1 = O1.coordinates().x();
		final BigDecimal y1 = O1.coordinates().y();
		final BigDecimal z1 = O1.coordinates().z();
		final BigDecimal x2 = O2.coordinates().x();
		final BigDecimal y2 = O2.coordinates().y();
		final BigDecimal z2 = O2.coordinates().z();
		return Math.round(Math.sqrt((speed1.multiply(x1)
										.subtract(speed2.multiply(x2).multiply(speed1.multiply(x1)))
										.subtract(speed2.multiply(x2)))
									.add(speed1.multiply(y1)
										.subtract(speed2.multiply(y1).multiply(speed1.multiply(y1)))
										.subtract(speed2.multiply(y2)))
									.add(speed1.multiply(z1)
										.subtract(speed2.multiply(z1).multiply(speed1.multiply(z1)))
										.subtract(speed2.multiply(z2))).doubleValue()));
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
	public List<SpaceObject> getSpaceObjectsByCenterpointWithin(final Coord3D centerCoordinates, final long minDistance, final long maxDistance)
	{
		final List<SpaceObject> within=new ArrayList<SpaceObject>(1);
		if((centerCoordinates==null)||(centerCoordinates.length()!=3))
			return within;
		synchronized(space)
		{
			space.query(within, new BoundedCube(centerCoordinates, maxDistance));
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
	public List<SpaceObject> getSpaceObjectsInBound(final BoundedTube tube, final Set<Coord3D> except)
	{
		final List<SpaceObject> within=new ArrayList<SpaceObject>(1);
		synchronized(space)
		{
			space.query(within, tube.getCube());
		}
		for(final Iterator<SpaceObject> i=within.iterator();i.hasNext();)
		{
			final SpaceObject o = i.next();
			if((!tube.intersects(o.getSphere()))||(except.contains(o.coordinates())))
				i.remove();
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
			space.query(within, new BoundedCube(ofObj.coordinates(), maxDistance));
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

	@Override
	public boolean canMaybeIntercept(final SpaceObject chaserO, final SpaceObject runnerO, final int maxTicks, final double maxSpeed)
	{
		final BoundedSphere runB = runnerO.getSphere();
		final BoundedTube tubeB = runB.expand(runnerO.direction(), Math.round(CMath.mul(runnerO.speed(),maxTicks)));
		final BoundedSphere chaB = runnerO.getSphere();
		final BoundedTube tubeC = chaB.expand(chaserO.direction(), Math.round(CMath.mul(maxSpeed,maxTicks)));
		return tubeB.intersects(tubeC);
	}

	@Override
	public Pair<Dir3D,Long> calculateIntercept(final SpaceObject chaserO, final SpaceObject runnerO, final long maxChaserSpeed, final int maxTicks)
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
				final Dir3D dirTo = getDirection(chaserO, runnerO);
				return new Pair<Dir3D, Long>(dirTo, Long.valueOf(distance));
			}
			final Coord3D P0=new Coord3D(runnerO.coordinates()); // runners position
			final Coord3D P1=new Coord3D(chaserO.coordinates()); // chasers position (torpedo/ship/whatever)
			final Coord3D P0S=new Coord3D(moveSpaceObject(runnerO.coordinates(), runnerO.direction(), Math.round(runnerO.speed())));
			final Coord3D V0=P0S.subtract(P0);
			final BigDecimal S1=BigDecimal.valueOf(speedToUse);
			BigDecimal A=V0.dotProduct(V0).subtract(S1.multiply(S1));
			if(A.doubleValue()==0.0)
			{
				final Coord3D V01=new Coord3D(V0.x().add(BigDecimal.ONE),V0.y(),V0.z());
				A=V01.dotProduct(V01).subtract(S1.multiply(S1));
			}
			final BigDecimal B=BigCMath.TWO.multiply(P0.dotProduct(V0).add(P1.scalarProduct(BigCMath.ONE.negate()).dotProduct(V0)));
			final BigDecimal C=P0.dotProduct(P0).add(P1.dotProduct(P1)).add(P1.scalarProduct(BigCMath.TWO.negate()).dotProduct(P0));
			final BigDecimal T1 = B.negate().add(BigCMath.sqrt(B.multiply(B).subtract(BigCMath.FOUR.multiply(A).multiply(C)))).divide(BigCMath.TWO.multiply(A),Coord3D.SCALE,RoundingMode.UP);
			final BigDecimal T2 = B.negate().subtract(BigCMath.sqrt(B.multiply(B).subtract(BigCMath.FOUR.multiply(A).multiply(C)))).divide(BigCMath.TWO.multiply(A),Coord3D.SCALE,RoundingMode.UP);
			final BigDecimal T;
			if((T1.doubleValue() < 0)
			|| ((T2.doubleValue() < T1.doubleValue()) && (T2.doubleValue() >= 0)))
				T = T2;
			else
				T = T1;
			if(T.doubleValue()<=0)
				return null;
			final BigVector P0T = P0.add(V0.scalarProduct(T));
			final Dir3D finalDir = getDirection(chaserO.coordinates(),new Coord3D(P0T));
			return new Pair<Dir3D, Long>(finalDir,Long.valueOf(maxChaserSpeed));
		}
		else
		if(chaserO.speed()>0) // runner isn't moving, so straight shot
		{
			final Dir3D dirTo = getDirection(chaserO, runnerO);
			return new Pair<Dir3D, Long>(dirTo,Long.valueOf(maxChaserSpeed));
		}
		return null; // something is not
	}

	@Override
	public double getMinDistanceFrom(final Coord3D vec1s, final Coord3D vec1e, final Coord3D vec2s, final Coord3D vec2e)
	{
		if(vec1s.equals(vec1e))
			return this.getMinDistanceFrom(vec2s,vec2e,vec1s);
		if(vec2s.equals(vec2e))
			return this.getMinDistanceFrom(vec1s,vec1e,vec2s);
		final Coord3D bigVec1s = new Coord3D(vec1s);
		final Coord3D bigVec1e = new Coord3D(vec2e);
		final Coord3D bigVec2s = new Coord3D(vec2s);
		final Coord3D bigVec2e = new Coord3D(vec2e);

		final Coord3D d1 = bigVec1e.subtract(bigVec1s);
		final Coord3D d2 = bigVec2e.subtract(bigVec2s);

		final Coord3D w0 = bigVec1s.subtract(bigVec2s);
		final Coord3D w1 = bigVec1e.subtract(bigVec2e);

		final BigDecimal a = d1.dotProduct(d1);
		final BigDecimal b = d1.dotProduct(d2);
		final BigDecimal c = d2.dotProduct(d2);
		//final BigDecimal d = d1.dotProduct(w0);
		final BigDecimal e = d2.dotProduct(w0);
		final BigDecimal f = d1.dotProduct(w1);
		final BigDecimal g = d2.dotProduct(w1);

		final BigDecimal denom = a.multiply(c).subtract(b.multiply(b));
		if(denom.doubleValue() < 0.001)
			return CMath.posMin(getDistanceFrom(vec1s, vec2s),getDistanceFrom(vec1e, vec2e));

		final BigDecimal s = b.multiply(e).subtract(c.multiply(f)).divide(denom,Dir3D.SCALE,RoundingMode.UP);
		final BigDecimal t = a.multiply(g).subtract(b.multiply(f)).divide(denom,Dir3D.SCALE,RoundingMode.UP);

		final BigDecimal[] v1 = new BigDecimal[] {
			bigVec1s.x().add(s.multiply(bigVec1e.x().subtract(bigVec1s.x()))),
			bigVec1s.y().add(s.multiply(bigVec1e.y().subtract(bigVec1s.y()))),
			bigVec1s.z().add(s.multiply(bigVec1e.z().subtract(bigVec1s.z())))
		};

		final BigDecimal[] v2 = new BigDecimal[] {
			bigVec1e.x().add(t.multiply(bigVec2e.x().subtract(bigVec2s.x()))),
			bigVec2e.y().add(t.multiply(bigVec2e.y().subtract(bigVec2s.y()))),
			bigVec2e.z().add(t.multiply(bigVec2e.z().subtract(bigVec2s.z())))
		};
		final BigDecimal minDist = Coord3D.sqrt(
			v2[0].subtract(v1[0]).multiply(v2[0].subtract(v1[0])).add(
			v2[1].subtract(v1[1]).multiply(v2[1].subtract(v1[1]))).add(
			v2[2].subtract(v1[2]).multiply(v2[2].subtract(v1[2]))));
		return minDist.doubleValue();
	}

	@Override
	public double getMinDistanceFrom(final Coord3D prevPos, final Coord3D curPos, final Coord3D objPos)
	{
		if(prevPos.equals(curPos))
			return this.getDistanceFrom(curPos, objPos);
		final Coord3D bigPrevPos = new Coord3D(prevPos);
		final Coord3D bigCurPos = new Coord3D(curPos);
		final Coord3D bigObjPos = new Coord3D(objPos);

		final Coord3D AB = bigCurPos.subtract(bigPrevPos);
		final Coord3D BE = bigObjPos.subtract(bigCurPos);
		final Coord3D AE = bigObjPos.subtract(bigPrevPos);

		if(AB.dotProduct(BE).doubleValue() > 0)
			return BE.magnitude().doubleValue();
		else
		if(AB.dotProduct(AE).doubleValue() < 0)
			return AE.magnitude().doubleValue();
		else
		{
			final Coord3D bigDistance = bigPrevPos.subtract(bigCurPos);
			bigDistance.unitVectorFrom(); // divides each point by the vectors magnitude
			final BigDecimal dp = BE.dotProduct(bigDistance);
			return bigCurPos.add(bigDistance.scalarProduct(dp)).subtract(bigObjPos).magnitude().doubleValue();
		}
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


	protected BoundedTube makeCourseTubeRay(final Coord3D src, final long sradius,
											final Coord3D target, final long tradius,
											final Dir3D dir)
	{
		// never add source, it is implied!
		final long sgradius=Math.round(CMath.mul(sradius,(SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS)));
		final long tgradius=Math.round(CMath.mul(tradius,(SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS)));
		final Coord3D srcCoord = moveSpaceObject(src, dir, sgradius+1);
		final Coord3D tgtCoord = moveSpaceObject(target, getOppositeDir(dir), tgradius+1);
		final long distance = getDistanceFrom(srcCoord, tgtCoord);
		final BoundedSphere courseRay = new BoundedSphere(srcCoord, sgradius);
		if(courseRay.contains(tgtCoord)
		//||courseRay.contains(srcCoord) I don't get this .. courseRay is ONLY around the source, so isn't it ALWAYS colliding?
		||(distance <= sradius))
		{
			// this means we are already right on top of it, nowhere to go!
			return null;
		}
		return courseRay.expand(dir, distance);
	}

	protected List<SpaceObject> checkSubCourse(final Coord3D osrc, final long sradius, final Coord3D otarget, final long tradius,
											final Set<Coord3D> exceptions)
	{
		BoundedTube courseRay;
		final Dir3D dir = getDirection(osrc, otarget);
		// ray is a source-sized tube stretching from end-of-src coord to beginning of target coord.
		courseRay = makeCourseTubeRay(osrc, sradius, otarget, tradius, dir);
		if(courseRay == null)
			return null;
		return getSpaceObjectsInBound(courseRay, exceptions);
	}

	protected List<Coord3D> plotFullCourse(final Coord3D osrc, final long sradius, final Coord3D otarget, final long tradius,
									   final Set<Coord3D> exceptions, final Set<Coord3D> ignoreTargets)
	{
		final List<Coord3D> course = new LinkedList<Coord3D>();
		final Coord3D src=osrc.copyOf();
		final Coord3D target = otarget.copyOf();
		final Dir3D dir = getDirection(src, target);
		final List<SpaceObject> collisions = checkSubCourse(src, sradius, target, tradius, exceptions);
		if(collisions == null)
			return course; // we were already there
		if(collisions.size()==0)
		{
			course.add(target);
			return course;
		}

		// find the closest collider and go around that
		SpaceObject closestColliderObj = collisions.get(0);
		long closestDistance = getDistanceFrom(src, closestColliderObj.coordinates());
		for(int i=1;i<collisions.size();i++)
		{
			final SpaceObject otherColliderObj = collisions.get(i);
			final long otherColliderDistance = getDistanceFrom(src, otherColliderObj.coordinates());
			if((otherColliderDistance > 0) && (otherColliderDistance < closestDistance))
			{
				closestColliderObj = otherColliderObj;
				closestDistance = otherColliderDistance;
			}
		}

		// now try every direction to get around it -- recursively, increasing radius until one is found.
		for(double err = 2.0; err <= 65536.0; err *= 2.0)
		{
			final BigDecimal distanceToColliderObj = new BigDecimal(closestDistance);
			final double newSrcRadius = CMath.mul(sradius, err);
			final double newTgtRradius = CMath.mul(closestColliderObj.radius(),(SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS)) * err;
			final double dirDelta = Dir3D.atan(newSrcRadius + newTgtRradius)
								.divide(distanceToColliderObj, Coord3D.SCALE, Coord3D.ROUND).doubleValue();
			final List<Coord3D> allSub = new ArrayList<Coord3D>();
			// now try four different tangents
			for(int dd=0;dd<4;dd++)
			{
				final Dir3D newDir = dir.copyOf();
				switch(dd)
				{
				case 0:
					changeDirection(newDir, new Dir3D (dirDelta,0.0));
					break;
				case 1:
					changeDirection(newDir, new Dir3D (-dirDelta,0.0));
					break;
				case 2:
					changeDirection(newDir, new Dir3D (0.0,dirDelta));
					break;
				case 3:
					changeDirection(newDir, new Dir3D (0.0,-dirDelta));
					break;
				}
				final Coord3D newSubTarget = moveSpaceObject(src, newDir, closestDistance);
				if(ignoreTargets.contains(newSubTarget))
					continue;
				allSub.add(newSubTarget);
			}
			final Map<Coord3D,Long> distanceMap = new HashMap<Coord3D,Long>();
			for(final Coord3D d3 : allSub)
				distanceMap.put(d3, Long.valueOf(CMLib.space().getDistanceFrom(d3, otarget)));
			Collections.sort(allSub, new Comparator<Coord3D>()
			{
				@Override
				public int compare(final Coord3D o1, final Coord3D o2)
				{
					final long dist1 = distanceMap.get(o1).longValue();
					final long dist2 = distanceMap.get(o2).longValue();
					if(dist1 == dist2)
						return 0;
					if(dist1<dist2)
						return -1;
					return 1;
				}
			});
			for(final Coord3D newSubTarget : allSub)
			{
				final List<SpaceObject> intermediateCheck = checkSubCourse(src,sradius,newSubTarget,sradius,exceptions);
				if(intermediateCheck == null) // this should never happen
				{
					course.add(newSubTarget);
					return course;
				}
				if(intermediateCheck.size()==0) // clear to recurse
				{
					final Set<Coord3D> ignore = new XHashSet<Coord3D>(ignoreTargets);
					ignore.add(newSubTarget);
					final List<Coord3D> subCourse =  plotFullCourse(newSubTarget, sradius, target, tradius, exceptions, ignore);
					if(subCourse != null)
					{
						course.add(newSubTarget);
						course.addAll(subCourse);
						return course;
					}
				}
			}
		}
		return null;
	}

	@Override
	public List<Coord3D> plotCourse(final Coord3D osrc, final long sradius, final Coord3D otarget, final long tradius, final int maxTicks)
	{
		final HashSet<Coord3D> exceptions = new HashSet<Coord3D>();
		exceptions.add(osrc);
		exceptions.add(otarget);

		final HashSet<Coord3D> ignoreTargets = new HashSet<Coord3D>();
		final List<Coord3D> course = plotFullCourse(osrc, sradius, otarget, tradius, exceptions, ignoreTargets);
		if(course == null)
			return new ArrayList<Coord3D>();
		while(course.size()>maxTicks)
			course.remove(course.size()-1);
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
				BoundedTube tube=new BoundedTube(O.getSphere());
				final double speed=O.speed();
				final Coord3D startCoords=O.coordinates().copyOf();
				final boolean moving;
				if(speed>=1)
				{
					tube=tube.expand(O.direction(),(long)speed);
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
				{
					Log.debugOut("Space Object "+O.name()+" moved "+speed+" in dir " +
							CMLib.english().directionDescShort(O.direction().toDoubles())+" to "+
							CMLib.english().coordDescShort(O.coordinates().toLongs()));
				}

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
							final Dir3D directionTo=getDirection(O, cO);
							accelSpaceObject(O, directionTo, gravitationalMove);
							inAirFlag = true;
						}
						if((O instanceof Weapon)&&(isDebugging) && moving)
						{
							final long dist = CMLib.space().getDistanceFrom(O, cO);
							Log.debugOut("SpaceShip Weapon "+O.Name()+" closest distance is "+minDistance+" to "+cO.name()
								+"@("+CMParms.toListString(cO.coordinates().toLongs())+"):<"+(O.radius()+cO.radius())+"/"+dist);
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
		if(!super.activate())
			return false;
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
