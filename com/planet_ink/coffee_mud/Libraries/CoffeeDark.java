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
	protected static final BigDecimal	BZERO_ALMOST			= BigDecimal.valueOf(0.000001);
	protected static final BigDecimal	ZERO					= BigDecimal.valueOf(0.0);
	protected static final BigDecimal	POINT01					= BigDecimal.valueOf(0.01);
	protected static final BigDecimal	POINT1					= BigDecimal.valueOf(0.1);
	protected static final BigDecimal	ONEPOINT01				= BigDecimal.valueOf(1.01);
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
	protected static final BigDecimal	BPI_TIMES_2				= BigDecimal.valueOf(Math.PI).multiply(TWO);
	protected static final double		PI_BY_2					= Math.PI / 2.0;
	protected static final BigDecimal	BPI_BY_2				= BigDecimal.valueOf(Math.PI / 2.0);
	protected static final BigDecimal	BPI						= BigDecimal.valueOf(Math.PI);
	protected static final double		PI_BY_12				= Math.PI / 12.0;
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
		if(angle.z().compareTo(BPI_BY_2)>0)
			set.add(new Dir3D( angle.xy(), angle.z().subtract(BPI_BY_2)));
		else
		if(angle.z().compareTo(BPI_BY_2)<0)
			set.add(new Dir3D ( angle.xy(), angle.z().add(BPI_BY_2) ));

		final BigDecimal angle10 = angle.z().compareTo(BPI_BY_2) > 0 ?  angle.z().subtract(BPI_BY_2) : BPI_BY_2.subtract(angle.z());
		BigDecimal angle00 = angle.xy().add(BPI_BY_2);
		if(angle00.compareTo(BPI_TIMES_2) >=0)
			angle00 = angle00.subtract(BPI_TIMES_2);
		set.add(new Dir3D (angle00, angle10 ));

		BigDecimal angle01 = angle.xy().add(BPI);
		if(angle01.compareTo(BPI_TIMES_2) >=0)
			angle01 = angle01.subtract(BPI_TIMES_2);
		set.add(new Dir3D(angle01, BPI_BY_2));

		BigDecimal angle02 = angle.xy().subtract(BPI_BY_2);
		if(angle02.compareTo(ZERO) < 0)
			angle02 = angle02.add(BPI_TIMES_2);
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
		final BigDecimal val = bigSqrt(coords_all);
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
			sectorCoords.x(sectorCoords.x().multiply(MIN_ONE));
		if(sectorCoords.y().longValue()<0)
			sectorCoords.y(sectorCoords.y().multiply(MIN_ONE));
		if(sectorCoords.z().longValue()<0)
			sectorCoords.z(sectorCoords.z().multiply(MIN_ONE));
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
		if(dotProd.compareTo(ONE)>0)
			dotProd=TWO.subtract(dotProd);
		if(dotProd.compareTo(MIN_ONE)<0)
			dotProd=MIN_ONE.multiply(dotProd).subtract(TWO);
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

	@Override
	public Dir3D getMiddleAngle(final Dir3D angle1, final Dir3D angle2)
	{
		final Dir3D middleAngle = new Dir3D (angle1.xy(), angle1.z());
		if(!angle1.xy().equals(angle2.xy()))
		{
			final BigDecimal xy1 = angle1.xy().compareTo(angle2.xy())>0?angle1.xy():angle2.xy();
			final BigDecimal xy2 = xy1.equals(angle1.xy()) ? angle2.xy() : angle1.xy();
			if(xy2.compareTo(xy1.subtract(BPI))<0)
				middleAngle.xy(((BPI_TIMES_2.subtract(xy1)).add(xy2)).divide(TWO,Dir3D.SCALE,RoundingMode.UP));
			else
				middleAngle.xy((xy1.add(xy2)).divide(TWO,Dir3D.SCALE,RoundingMode.UP));
		}
		middleAngle.z((angle1.z().add(angle2.z())).divide(TWO,Dir3D.SCALE,RoundingMode.UP));
		return middleAngle;
	}

	@Override
	public Dir3D getOffsetAngle(final Dir3D correctAngle, final Dir3D wrongAngle)
	{
		final Dir3D offsetAngles = new Dir3D (correctAngle.xy(), correctAngle.z());
		if(!correctAngle.xy().equals(wrongAngle.xy()))
		{
			final BigDecimal xy1 = correctAngle.xy().compareTo(wrongAngle.xy())>0?correctAngle.xy():wrongAngle.xy();
			final BigDecimal xy2 = xy1.equals(correctAngle.xy()) ? wrongAngle.xy() : correctAngle.xy();
			if(xy2.compareTo(xy1.subtract(BPI))<0)
				offsetAngles.xy(((BPI_TIMES_2.subtract(xy1)).add(xy2)));
			else
				offsetAngles.xy(xy1.subtract(xy2));
			if((wrongAngle.xy().compareTo(correctAngle.xy())>0)
			&&((wrongAngle.xy().subtract(correctAngle.xy()).compareTo(BPI)<0)))
			{
				offsetAngles.xy(correctAngle.xy().subtract(offsetAngles.xy()));
				if(offsetAngles.xy().compareTo(ZERO) < 0)
					offsetAngles.xy(offsetAngles.xy().add(BPI_TIMES_2));
			}
			else
			{
				offsetAngles.xy(correctAngle.xy().add(offsetAngles.xy()));
				if(offsetAngles.xy().compareTo(BPI_TIMES_2) >= 0)
					offsetAngles.xy(offsetAngles.xy().subtract(BPI_TIMES_2));
			}
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
	public void applyAngleDiff(final Dir3D angle, final Dir3D delta)
	{
		angle.xy(angle.xy().add(delta.xy()));
		angle.z(angle.z().add(delta.z()));
	}

	@Override
	public Dir3D getAngleDiff(final Dir3D fromAngle, final Dir3D toAngle)
	{
		final Dir3D delta = new Dir3D();
		delta.xy(toAngle.xy().subtract(fromAngle.xy()));
		if(delta.xy().compareTo(BPI) > 0)
			delta.xy(BPI_TIMES_2.subtract(delta.xy()));
		else
		if(delta.xy().compareTo(BPI.negate()) < 0)
			delta.xy(BPI_TIMES_2.add(delta.xy()));
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

		final BigDecimal yawSign;
		BigDecimal yawDelta;
		if(curDirectionYaw.compareTo(accelDirectionYaw) >0)
		{
			yawSign = BigDecimal.valueOf(-1.0);
			yawDelta = curDirectionYaw.subtract(accelDirectionYaw);
		}
		else
		{
			yawSign = BigDecimal.valueOf(1.0);
			yawDelta = accelDirectionYaw.subtract(curDirectionYaw);
		}
		// 350 and 10, diff = 340 + -360 = 20
		if(yawDelta.compareTo(BPI)>0) // a delta is never more than 180 degrees
			yawDelta=BPI_TIMES_2.subtract(yawDelta);
		final BigDecimal pitchSign;
		final BigDecimal pitchDelta;
		if(curDirectionPitch.compareTo(accelDirectionPitch) >0)
		{
			pitchSign = BigDecimal.valueOf(-1.0);
			pitchDelta = curDirectionPitch.subtract(accelDirectionPitch);
		}
		else
		{
			pitchSign = BigDecimal.valueOf(1.0);
			pitchDelta = accelDirectionPitch.subtract(curDirectionPitch);
		}
		final BigDecimal anglesDelta =  BigDecimal.valueOf(getAngleDelta(curDirection, accelDirection));
		if((anglesDelta.subtract(BPI).abs().compareTo(BZERO_ALMOST)<=0)
		&&(currentSpeed.compareTo(acceleration)>0))
			return currentSpeed.subtract(acceleration).doubleValue();
		BigDecimal newDirectionYaw;
		BigDecimal newDirectionPitch;
		final BigDecimal deltaMultiplier = Dir3D.sin(anglesDelta);
		final BigDecimal yawMin =  deltaMultiplier.multiply((POINT01.add(yawDelta.multiply(ONEPOINT01.subtract(BigDecimal.valueOf(Math.sin(curDirectionPitch.doubleValue())))))));
		final BigDecimal accelerationMultiplier;
		if(currentSpeed.equals(ZERO))
			accelerationMultiplier = ONE;
		else
			accelerationMultiplier = acceleration.multiply(TEN).divide(currentSpeed,Dir3D.SCALE,RoundingMode.UP).multiply(deltaMultiplier,MathContext.DECIMAL128);
		if((yawDelta.compareTo(yawMin) <= 0))
			newDirectionYaw = accelDirectionYaw;
		else
		{
			BigDecimal nearFinalYawDelta = Dir3D.sin(yawDelta).multiply(accelerationMultiplier,MathContext.DECIMAL128);
			if((nearFinalYawDelta.compareTo(yawMin)<0)&&(yawDelta.compareTo(yawMin)>0))
				nearFinalYawDelta = yawMin;
			newDirectionYaw = curDirectionYaw.add(nearFinalYawDelta.multiply(yawSign));
			if((newDirectionYaw.compareTo(ZERO) > 0) && ((BPI_TIMES_2.subtract(newDirectionYaw)).compareTo(BZERO_ALMOST)<0))
				newDirectionYaw=ZERO;
		}
		final BigDecimal pitchMin = POINT1;
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
		if(newSpeed.compareTo(ZERO)<0) // cos >=180deg is a negative number, so negative acceleration, new direction
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
		return new Dir3D(BPI.add(dir.xy()),BPI.subtract(dir.z()));
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
		/*
		final Dir3D dir=new double[2];
		final double x=toCoords.xy()-fromCoords.xy();
		final double y=toCoords.z()-fromCoords.z();
		final double z=toCoords[2]-fromCoords[2];
		final double xy = (x*x)+(y*y);
		if((x!=0)||(y!=0))
		{
			final double sqrtxy = Math.sqrt(xy);
			final double ybysqrtxy=y/sqrtxy;
			if(x<0)
				dir.xy()=Math.PI-Math.asin(ybysqrtxy);
			else
				dir.xy()=Math.asin(ybysqrtxy);
		}
		if((x!=0)||(y!=0)||(z!=0))
			dir.z()=Math.acos(z/Math.sqrt((z*z)+xy));
		fixDirectionBounds(dir);
		return dir;
		*/
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
			final BigDecimal sqrtxy = bigSqrt(xy);
			final BigDecimal ybysqrtxy=y.divide(sqrtxy,50,RoundingMode.HALF_EVEN);
			if(x.doubleValue()<0)
				dir.xy(Math.PI-Math.asin(ybysqrtxy.doubleValue()));
			else
				dir.xy(Math.asin(ybysqrtxy.doubleValue()));
		}
		if((x.doubleValue()!=0)||(y.doubleValue()!=0)||(z.doubleValue()!=0))
			dir.z(Math.acos(z.divide(bigSqrt(z.multiply(z).add(xy)),50,RoundingMode.HALF_EVEN).doubleValue()));
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
	public List<SpaceObject> getSpaceObjectsInBound(final BoundedTube tube)
	{
		final List<SpaceObject> within=new ArrayList<SpaceObject>(1);
		synchronized(space)
		{
			space.query(within, tube.getCube());
		}
		for(final Iterator<SpaceObject> i=within.iterator();i.hasNext();)
		{
			if(!tube.intersects(i.next().getSphere()))
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
			final BigDecimal B=TWO.multiply(P0.dotProduct(V0).add(P1.scalarProduct(ONE.negate()).dotProduct(V0)));
			final BigDecimal C=P0.dotProduct(P0).add(P1.dotProduct(P1)).add(P1.scalarProduct(TWO.negate()).dotProduct(P0));
			final BigDecimal T1 = B.negate().add(bigSqrt(B.multiply(B).subtract(FOUR.multiply(A).multiply(C)))).divide(TWO.multiply(A),Coord3D.SCALE,RoundingMode.UP);
			final BigDecimal T2 = B.negate().subtract(bigSqrt(B.multiply(B).subtract(FOUR.multiply(A).multiply(C)))).divide(TWO.multiply(A),Coord3D.SCALE,RoundingMode.UP);
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

	//@Override
	public Pair<Dir3D,Long> calculateIntercept2(final SpaceObject chaserO, final SpaceObject runnerO, final long maxChaserSpeed, final int maxTicks)
	{
		if(maxTicks < 1)
			return null; // not possible, too late
		Dir3D dirTo = getDirection(chaserO, runnerO);
		if((maxChaserSpeed>0)
		&&(runnerO.speed()>0))
		{
			long distance = getDistanceFrom(chaserO, runnerO);
			long speedToUse = maxChaserSpeed;
			if(distance < maxChaserSpeed)
			{
				speedToUse = distance;
				return new Pair<Dir3D, Long>(dirTo, Long.valueOf(speedToUse));
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
				Coord3D runnerCoords = runnerO.coordinates().copyOf();
				Coord3D chaserCoords = chaserO.coordinates().copyOf();
				chaserCoords=moveSpaceObject(chaserCoords, dirTo, speedToUse*(newTicks-1));
				runnerCoords=moveSpaceObject(runnerCoords, runnerO.direction(), Math.round(runnerO.speed())*newTicks-1);
				final Coord3D oldCoords = chaserCoords.copyOf();
				chaserCoords=moveSpaceObject(chaserCoords, dirTo, speedToUse);
				if(getMinDistanceFrom(oldCoords, chaserCoords, runnerCoords)<radius)
				{
					return new Pair<Dir3D, Long>(dirTo, Long.valueOf(speedToUse));
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
			return new Pair<Dir3D, Long>(dirTo,Long.valueOf(speedToUse));
		}
		else
		if(chaserO.speed()>0) // runner isn't moving, so straight shot
			return new Pair<Dir3D, Long>(dirTo,Long.valueOf(maxChaserSpeed));
		return null; // something is not
	}

	protected final double getDirDiffSum(final Dir3D d1, final Dir3D d2)
	{
		final BigDecimal sum1=(d1.xy().compareTo(d2.xy())>0)?(d1.xy().subtract(d2.xy())):(d2.xy().subtract(d1.xy()));
		final BigDecimal sum2=(d1.xy().compareTo(d2.xy())>0)?(BPI_TIMES_2.subtract(d1.xy()).add(d2.xy())):(BPI_TIMES_2.subtract(d2.xy()).add(d1.xy()));
		final BigDecimal sum3=(d1.z().compareTo(d2.z())>0)?(d1.z().subtract(d2.z())):(d2.z().subtract(d1.z()));
		return ((sum1.compareTo(sum2)>0)?(sum2.add(sum3)):(sum1.add(sum3))).doubleValue();
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

	protected double getOldMinDistFrom(final Coord3D prevPos, final double speed, final Dir3D dir, final Coord3D curPosition,
									   final Dir3D directionTo, final Coord3D objPos)
	{
		final BigDecimal currentDistance=getBigDistanceFrom(curPosition, objPos);
		if(prevPos.equals(curPosition))
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
		final Dir3D travelDir = dir;
		final Dir3D prevDirToObject = getDirection(prevPos, objPos);
		final double diDelta=getDirDiffSum(travelDir,prevDirToObject);
		if(diDelta<ZERO_ALMOST)
		{
			final Dir3D currDirToObject = directionTo;
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

	@Override
	public List<Coord3D> plotCourse(final Coord3D osrc, final long sradius, final Coord3D otarget, final long tradius, int maxTicks)
	{
		final List<Coord3D> course = new LinkedList<Coord3D>();
		Coord3D src=osrc.copyOf();
		Coord3D target = otarget.copyOf();
		BoundedTube courseRay;
		List<SpaceObject> objs;
		while(!src.equals(target))
		{
			final Dir3D dir = getDirection(src, target);
			courseRay = makeCourseTubeRay(src, sradius, target, tradius,dir);
			if(courseRay == null)
				return course; // we are on top of the target, so done
			objs = getSpaceObjectsInBound(courseRay);
			double err = 1.0;
			int tries=100;
			while((objs.size()>0)&&(--tries>0))
			{
				err *= 2.0;
				final List<Coord3D> choices = new ArrayList<Coord3D>(4);
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
						if(bobjdist == 0.0)
							continue;
						final BigDecimal distanceToBobj = new BigDecimal(bobjdist);
						final double dsgradius = CMath.mul(sradius, err);
						final double dtgradius = CMath.mul(bobj.radius(),(SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS)) * err;
						final double dirDelta = Dir3D.atan(dsgradius + dtgradius)
											.divide(distanceToBobj, Coord3D.SCALE, RoundingMode.HALF_UP).doubleValue();
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
						final Coord3D newSubTarget = moveSpaceObject(src, newDir, distanceToBobj.longValue());
						courseRay = makeCourseTubeRay(src, sradius, newSubTarget, bobj.radius(), newDir);
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
				src = target.copyOf();
				target = otarget.copyOf();
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
