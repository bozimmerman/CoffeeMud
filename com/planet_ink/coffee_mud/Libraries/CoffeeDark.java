package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMProps.Str;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
   Copyright 2022-2026 Bo Zimmerman

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

	protected final int									QUADRANT_WIDTH	= 10;
	protected long										tickCounter		= 0;
	protected final RTree<SpaceObject>					space			= new RTree<SpaceObject>();
	protected final Map<String, BoundedCube>			sectorMap		= new Hashtable<String, BoundedCube>();
	protected final Map<SpaceObject, GravityCacheEntry>	gravCache		= new Hashtable<SpaceObject, GravityCacheEntry>();

	private static class GravityCacheEntry
	{
		public final long	tick;
		public double		accel;
		public Dir3D		dir;

		private GravityCacheEntry(final double a, final Dir3D d, final long tick)
		{
			accel = a;
			dir = d;
			this.tick = tick;
		}
		private GravityCacheEntry(final long tick)
		{
			this(0.0,null,tick);
		}
	}

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
	public boolean isCollinear(final SpaceObject a, final SpaceObject b, final SpaceObject c, final double tol)
	{
		final Dir3D ab = getDirection(a, b);
		final Dir3D bc = getDirection(b, c);
		return getAngleDelta(ab, bc) < tol;
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
		if(angle.z().compareTo(BigCMath.PI_BY_2)>=0)
			set.add(new Dir3D( angle.xy(), angle.z().subtract(BigCMath.PI_BY_2)));
		else
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

		final BigDecimal angle03 = angle.xy().add(BigCMath.PI_BY_4);
		set.add(new Dir3D(angle03.compareTo(BigCMath.PI_TIMES_2) >= 0 ?
				angle03.subtract(BigCMath.PI_TIMES_2) : angle03, angle10));
		final BigDecimal angle04 = angle.xy().subtract(BigCMath.PI_BY_4);
		set.add(new Dir3D(angle04.compareTo(BigCMath.ZERO) < 0 ?
				angle04.add(BigCMath.PI_TIMES_2) : angle04, angle10));
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
		final String[] xsecs = CMProps.getListFileStringList(CMProps.ListFile.TECH_SECTOR_X_NAMES);
		final String[] ysecs = CMProps.getListFileStringList(CMProps.ListFile.TECH_SECTOR_Y_NAMES);
		final String[] zsecs = CMProps.getListFileStringList(CMProps.ListFile.TECH_SECTOR_Z_NAMES);

		final int xOffset = xsecs.length / 2;
		final int yOffset = ysecs.length / 2;
		final int zOffset = zsecs.length / 2;
		if ((xOffset * 2 != xsecs.length) || (yOffset * 2 != ysecs.length) || (zOffset * 2 != zsecs.length))
			throw new IllegalStateException("Sector name lists must have even lengths for symmetry");

		// size of each sector in distance-measure units, covering only -R..+R
		final long dmsPerXSector = (2L * SpaceObject.Distance.GalaxyRadius.dm) / xsecs.length;
		final long dmsPerYSector = (2L * SpaceObject.Distance.GalaxyRadius.dm) / ysecs.length;
		final long dmsPerZSector = (2L * SpaceObject.Distance.GalaxyRadius.dm) / zsecs.length;

		// which sector index the coord falls into
		final long secDeX = coords.x().longValue() / dmsPerXSector;
		final long secDeY = coords.y().longValue() / dmsPerYSector;
		final long secDeZ = coords.z().longValue() / dmsPerZSector;

		// shift by offset so 0 is centered, wrap safely into bounds
		final int fSecDeX = Math.floorMod((int) (secDeX + xOffset), xsecs.length);
		final int fSecDeY = Math.floorMod((int) (secDeY + yOffset), ysecs.length);
		final int fSecDeZ = Math.floorMod((int) (secDeZ + zOffset), zsecs.length);

		return xsecs[fSecDeX] + " " + ysecs[fSecDeY] + " " + zsecs[fSecDeZ];
	}

	@Override
	public Coord3D getInSectorCoords(final Coord3D coords)
	{
		final String[] xsecs=CMProps.getListFileStringList(CMProps.ListFile.TECH_SECTOR_X_NAMES);
		final String[] ysecs=CMProps.getListFileStringList(CMProps.ListFile.TECH_SECTOR_Y_NAMES);
		final String[] zsecs=CMProps.getListFileStringList(CMProps.ListFile.TECH_SECTOR_Z_NAMES);

		final long dmsPerXSector = (2L * SpaceObject.Distance.GalaxyRadius.dm) / xsecs.length;
		final long dmsPerYSector = (2L * SpaceObject.Distance.GalaxyRadius.dm) / ysecs.length;
		final long dmsPerZSector = (2L * SpaceObject.Distance.GalaxyRadius.dm) / zsecs.length;

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
			final int xOffset = xsecs.length / 2;
			final int yOffset = ysecs.length / 2;
			final int zOffset = zsecs.length / 2;
			if (xOffset * 2 != xsecs.length || yOffset * 2 != ysecs.length || zOffset * 2 != zsecs.length)
				throw new IllegalStateException("Sector name lists must have even lengths for symmetry");

			final long dmsPerXSector = (2L * SpaceObject.Distance.GalaxyRadius.dm) / xsecs.length;
			final long dmsPerYSector = (2L * SpaceObject.Distance.GalaxyRadius.dm) / ysecs.length;
			final long dmsPerZSector = (2L * SpaceObject.Distance.GalaxyRadius.dm) / zsecs.length;

			for (long x = -SpaceObject.Distance.GalaxyRadius.dm; x < SpaceObject.Distance.GalaxyRadius.dm; x += dmsPerXSector)
			{
				final long maxX = Math.min(x + dmsPerXSector, SpaceObject.Distance.GalaxyRadius.dm);
				for (long y = -SpaceObject.Distance.GalaxyRadius.dm; y < SpaceObject.Distance.GalaxyRadius.dm; y += dmsPerYSector)
				{
					final long maxY = Math.min(y + dmsPerYSector, SpaceObject.Distance.GalaxyRadius.dm);
					for (long z = -SpaceObject.Distance.GalaxyRadius.dm; z < SpaceObject.Distance.GalaxyRadius.dm; z += dmsPerZSector)
					{
						final long maxZ = Math.min(z + dmsPerZSector, SpaceObject.Distance.GalaxyRadius.dm);

						final Coord3D coords = new Coord3D(new long[] { x, y, z });
						final BoundedCube cube = new BoundedCube(x, maxX, y, maxY, z, maxZ);
						final String name = getSectorName(coords);

						if (tempMap.containsKey(name))
							Log.errOut("Duplicate sector name: " + name);
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
			dotProd=BigCMath.ONE;
		if(dotProd.compareTo(BigCMath.MIN_ONE)<0)
			dotProd=BigCMath.MIN_ONE;
		final double finalDelta = BigCMath.acos(dotProd).doubleValue();
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
		final BigVector v1 = new Dir3D(angle1).sphereToCartesian();
		final BigVector v2 = new Dir3D(angle2).sphereToCartesian();
		final BigVector sum = v1.add(v2);
		final BigDecimal mag = sum.magnitude();
		if (mag.compareTo(BigCMath.ZERO) == 0)
			return angle1.copyOf(); // fallback if perfect cancel
		final BigVector unit = sum.scalarProduct(BigCMath.ONE.divide(mag, BigVector.SCALE, BigVector.ROUND));
		final double[] unitDoubles = unit.toDoubles();
		final double yaw = Math.atan2(unitDoubles[1], unitDoubles[0]);
		final double pitch = Math.acos(Math.max(-1.0, Math.min(1.0, unitDoubles[2])));
		return new Dir3D(BigDecimal.valueOf(yaw), BigDecimal.valueOf(pitch));
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
		final BigDecimal yawMin =  BigCMath.max(BigCMath.ZERO,deltaMultiplier.multiply((BigCMath.POINT01.add(yawDelta.multiply(BigCMath.ONEPOINT01.subtract(BigDecimal.valueOf(Math.sin(curDirectionPitch.doubleValue()))))))));
		BigDecimal accelerationMultiplier;
		if(currentSpeed.compareTo(BigCMath.ZERO)==0)
			accelerationMultiplier = BigCMath.ONE;
		else
		if(currentSpeed.compareTo(acceleration)<=0)
			accelerationMultiplier = BigCMath.ONE;
		else
		{
			accelerationMultiplier = acceleration.divide(currentSpeed,Dir3D.SCALE,RoundingMode.UP).multiply(deltaMultiplier,MathContext.DECIMAL128);
			if((accelerationMultiplier.compareTo(BigCMath.POINT2)<0)
			&&(anglesDelta.compareTo(BigCMath.PI_BY_2)<0))
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
			return ShipDir.AFT;
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
			dir.xy(BigCMath.atan2(y,x));
		if((x.doubleValue()!=0)||(y.doubleValue()!=0)||(z.doubleValue()!=0))
		{
			final BigDecimal zNorm = z.divide(BigCMath.sqrt(z.multiply(z).add(xy)), BigVector.SCALE, BigVector.ROUND)
					 .max(BigCMath.MIN_ONE).min(BigCMath.ONE);
			dir.z(BigVector.acos(zNorm));
		}
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
		final BigDecimal speed1 = BigDecimal.valueOf(O1.speed());
		final BigDecimal speed2 = BigDecimal.valueOf(O2.speed());
		final Dir3D dir1 = O1.direction();
		final BigDecimal vx1 = speed1.multiply(Dir3D.cos(dir1.xy())).multiply(Dir3D.sin(dir1.z()));
		final BigDecimal vy1 = speed1.multiply(Dir3D.sin(dir1.xy())).multiply(Dir3D.sin(dir1.z()));
		final BigDecimal vz1 = speed1.multiply(Dir3D.cos(dir1.z()));

		final Dir3D dir2 = O2.direction();
		final BigDecimal vx2 = speed2.multiply(Dir3D.cos(dir2.xy())).multiply(Dir3D.sin(dir2.z()));
		final BigDecimal vy2 = speed2.multiply(Dir3D.sin(dir2.xy())).multiply(Dir3D.sin(dir2.z()));
		final BigDecimal vz2 = speed2.multiply(Dir3D.cos(dir2.z()));

		final BigDecimal dx = vx1.subtract(vx2);
		final BigDecimal dy = vy1.subtract(vy2);
		final BigDecimal dz = vz1.subtract(vz2);
		return Math.round(Math.sqrt(dx.multiply(dx).add(dy.multiply(dy)).add(dz.multiply(dz)).doubleValue()));
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
	public List<LocationRoom> getLandingPoints(final SpaceObject ship, Environmental O)
	{
		final List<LocationRoom> rooms=new LinkedList<LocationRoom>();
		if(O instanceof SpaceObject.SensedSpaceObject)
			O=((SpaceObject.SensedSpaceObject)O).get();
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
			final BigDecimal A=V0.dotProduct(V0).subtract(S1.multiply(S1));
			if(A.compareTo(BigCMath.ZERO_ALMOST) <= 0)
			{
				final BigDecimal perpDist = V0.crossProduct(P1.subtract(P0)).magnitude();
				if (perpDist.compareTo(BigDecimal.valueOf(chaserO.radius() + runnerO.radius())) > 0)
					return null; // no intersection
				final BigDecimal projT = P0.subtract(P1).dotProduct(V0).divide(V0.dotProduct(V0), Coord3D.SCALE, Coord3D.ROUND);
				if (projT.compareTo(BigDecimal.ZERO) < 0)
					return null;
				final Coord3D intersectPt = P0.add(new Coord3D(V0.scalarProduct(projT)));
				final Dir3D finalDir = getDirection(chaserO.coordinates(), intersectPt);
				final long timeToIntercept = projT.multiply(S1).longValueExact();
				if (timeToIntercept > maxTicks * speedToUse)
					return null;
				return new Pair<Dir3D, Long>(finalDir, Long.valueOf(speedToUse));
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
		final BigDecimal d1x = vec1e.x().subtract(vec1s.x());
		final BigDecimal d1y = vec1e.y().subtract(vec1s.y());
		final BigDecimal d1z = vec1e.z().subtract(vec1s.z());

		final BigDecimal d2x = vec2e.x().subtract(vec2s.x());
		final BigDecimal d2y = vec2e.y().subtract(vec2s.y());
		final BigDecimal d2z = vec2e.z().subtract(vec2s.z());

		final BigDecimal w0x = vec1s.x().subtract(vec2s.x());
		final BigDecimal w0y = vec1s.y().subtract(vec2s.y());
		final BigDecimal w0z = vec1s.z().subtract(vec2s.z());

		final BigDecimal a = d1x.multiply(d1x).add(d1y.multiply(d1y)).add(d1z.multiply(d1z));
		final BigDecimal b = d1x.multiply(d2x).add(d1y.multiply(d2y)).add(d1z.multiply(d2z));
		final BigDecimal c = d2x.multiply(d2x).add(d2y.multiply(d2y)).add(d2z.multiply(d2z));
		final BigDecimal d = d1x.multiply(w0x).add(d1y.multiply(w0y)).add(d1z.multiply(w0z));
		final BigDecimal e = d2x.multiply(w0x).add(d2y.multiply(w0y)).add(d2z.multiply(w0z));

		final BigDecimal denom = a.multiply(c).subtract(b.multiply(b));
		final BigDecimal SMALL_DENOM = BigDecimal.valueOf(0.001);
		if((denom.compareTo(SMALL_DENOM.negate())>0)&&(denom.compareTo(SMALL_DENOM)<0))
		{
			// Parallel: min of 4 endpoint-to-segment distances
			final double d11 = pointToSegmentDistance(vec2s, vec1s, vec1e);
			final double d12 = pointToSegmentDistance(vec2e, vec1s, vec1e);
			final double d21 = pointToSegmentDistance(vec1s, vec2s, vec2e);
			final double d22 = pointToSegmentDistance(vec1e, vec2s, vec2e);
			return CMath.posMin(d11, CMath.posMin(d12, CMath.posMin(d21, d22)));
		}

		final BigDecimal s = b.multiply(e).subtract(c.multiply(d)).divide(denom, Dir3D.SCALE, RoundingMode.UP);
		final BigDecimal t = a.multiply(e).subtract(b.multiply(d)).divide(denom, Dir3D.SCALE, RoundingMode.UP);

		final boolean sInSegment = s.compareTo(BigDecimal.ZERO) >= 0 && s.compareTo(BigDecimal.ONE) <= 0;
		final boolean tInSegment = t.compareTo(BigDecimal.ZERO) >= 0 && t.compareTo(BigDecimal.ONE) <= 0;

		if (sInSegment && tInSegment)
		{
			// Closest points on segments
			final BigDecimal v1x = vec1s.x().add(s.multiply(d1x));
			final BigDecimal v1y = vec1s.y().add(s.multiply(d1y));
			final BigDecimal v1z = vec1s.z().add(s.multiply(d1z));

			final BigDecimal v2x = vec2s.x().add(t.multiply(d2x));
			final BigDecimal v2y = vec2s.y().add(t.multiply(d2y));
			final BigDecimal v2z = vec2s.z().add(t.multiply(d2z));

			final BigDecimal dx = v1x.subtract(v2x);
			final BigDecimal dy = v1y.subtract(v2y);
			final BigDecimal dz = v1z.subtract(v2z);
			return Math.sqrt(dx.multiply(dx).add(dy.multiply(dy)).add(dz.multiply(dz)).doubleValue());
		}
		else
		{
			// Min of endpoint-to-segment distances
			final double d11 = pointToSegmentDistance(vec2s, vec1s, vec1e);
			final double d12 = pointToSegmentDistance(vec2e, vec1s, vec1e);
			final double d21 = pointToSegmentDistance(vec1s, vec2s, vec2e);
			final double d22 = pointToSegmentDistance(vec1e, vec2s, vec2e);
			return CMath.posMin(d11, CMath.posMin(d12, CMath.posMin(d21, d22)));
		}
	}

	private double pointToSegmentDistance(final Coord3D p, final Coord3D a, final Coord3D b)
	{
		final BigDecimal abx = b.x().subtract(a.x());
		final BigDecimal aby = b.y().subtract(a.y());
		final BigDecimal abz = b.z().subtract(a.z());

		final BigDecimal len2 = abx.multiply(abx).add(aby.multiply(aby)).add(abz.multiply(abz));
		if (len2.compareTo(BigDecimal.ZERO) == 0)
		{
			final BigDecimal dx = p.x().subtract(a.x());
			final BigDecimal dy = p.y().subtract(a.y());
			final BigDecimal dz = p.z().subtract(a.z());
			return Math.sqrt(dx.multiply(dx).add(dy.multiply(dy)).add(dz.multiply(dz)).doubleValue());
		}

		final BigDecimal apx = p.x().subtract(a.x());
		final BigDecimal apy = p.y().subtract(a.y());
		final BigDecimal apz = p.z().subtract(a.z());

		BigDecimal tt = apx.multiply(abx).add(apy.multiply(aby)).add(apz.multiply(abz)).divide(len2, 64, RoundingMode.HALF_UP);
		if (tt.compareTo(BigDecimal.ZERO) < 0)
			tt = BigDecimal.ZERO;
		else if (tt.compareTo(BigDecimal.ONE) > 0)
			tt = BigDecimal.ONE;

		final BigDecimal cx = a.x().add(tt.multiply(abx));
		final BigDecimal cy = a.y().add(tt.multiply(aby));
		final BigDecimal cz = a.z().add(tt.multiply(abz));

		final BigDecimal dx = p.x().subtract(cx);
		final BigDecimal dy = p.y().subtract(cy);
		final BigDecimal dz = p.z().subtract(cz);
		return Math.sqrt(dx.multiply(dx).add(dy.multiply(dy)).add(dz.multiply(dz)).doubleValue());
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
	public Triad<Dir3D, Double, Coord3D> getOrbitalMaintenance(final SpaceShip ship, final SpaceObject planet, final long desiredRadius)
	{
		if (planet == null)
			return null;
		final Coord3D currentCoords = ship.coordinates();
		final long currentRadius = CMLib.space().getDistanceFrom(currentCoords, planet.coordinates());
		final Dir3D radial = CMLib.space().getDirection(ship, planet);
		final Dir3D currentDir = ship.direction();
		final double g = CMLib.space().getGravityForce(ship, planet);
		final double baseSpeed = Math.sqrt(g * desiredRadius);
		final long drift = currentRadius - desiredRadius;
		final double dv = (Math.abs(drift) / (double) desiredRadius) * baseSpeed * Math.signum(drift);
		final double candidateSpeed = baseSpeed + dv;
		final BigVector radialVec = new BigVector(radial).sphereToCartesian();
		final BigVector currentVec = new BigVector(currentDir).sphereToCartesian();
		final BigVector perpVec = radialVec.crossProduct(currentVec);
		final BigDecimal perpMag = perpVec.magnitude();
		BigVector unitPerpVec;
		if (perpMag.compareTo(BigCMath.ZERO) == 0)
			unitPerpVec = new BigVector(BigCMath.ZERO, BigCMath.ONE, BigCMath.ZERO);
		else
			unitPerpVec = perpVec.scalarProduct(BigCMath.ONE.divide(perpMag, BigVector.SCALE, BigVector.ROUND));
		final double blendFactor = 0.1;
		final BigVector blendedVec = currentVec.scalarProduct(BigDecimal.valueOf(1.0 - blendFactor)).add(unitPerpVec.scalarProduct(BigDecimal.valueOf(blendFactor)));
		final Dir3D candidateDir = blendedVec.cartesianToSphere();
		candidateDir.z(BigCMath.PI_BY_2);
		final Dir3D targetDir = candidateDir;
		final double targetSpeed = candidateSpeed;
		final Coord3D orbitalPoint = CMLib.space().moveSpaceObject(planet.coordinates(), CMLib.space().getOppositeDir(radial), desiredRadius);
		return new Triad<Dir3D, Double, Coord3D>(targetDir, Double.valueOf(targetSpeed), orbitalPoint);
	}

	@Override
	public Pair<Dir3D, Double> calculateOrbit(final SpaceObject o, final SpaceObject p)
	{
		if((o == null)||(p == null)||(o.getMass() <= 0)||(p.getMass() <= 0))
			return null;
		final double force = getGravityForce(o, p);
		if(force > 0.0)
		{
			final BigDecimal dist = this.getBigDistanceFrom(o.coordinates(), p.coordinates());
			final Dir3D[] perp3ds = getPerpendicularAngles(getDirection(o, p));
			Dir3D min3D = perp3ds[0];
			double minDiff = getAngleDelta(o.direction(), min3D);
			for(int i=1;i<perp3ds.length;i++)
			{
				final double thisDiff = getAngleDelta(o.direction(), perp3ds[i]);
				if(thisDiff < minDiff)
				{
					min3D = perp3ds[i];
					minDiff = thisDiff;
				}
			}
			final BigDecimal speed = BigCMath.sqrt(dist.multiply(BigDecimal.valueOf(force)));
			return new Pair<Dir3D,Double>( min3D, Double.valueOf(speed.doubleValue()) );
		}
		return null;
	}

	@Override
	public boolean isSafeTrajectory(final SpaceObject ship, final Coord3D target, final long maxDistance, final double safePeriFactor)
	{
		if (ship == null || target == null || maxDistance <= 0)
			return false;
		final long planetR = ship.radius() * 2;
		final double safePeri = planetR * safePeriFactor;
		final Coord3D startPos = ship.coordinates().copyOf();
		Dir3D dirToTarget = getDirection(startPos, target);
		final double thrustAccel = Math.min(ship.speed() / 10.0, SpaceObject.ACCELERATION_TYPICALSPACEROCKET);
		final long ticks = Math.max(1, (long) (maxDistance / Math.max(1.0, ship.speed())));
		final double dt = 1.0;
		Coord3D pos = startPos.copyOf();
		final Dir3D velDir = ship.direction().copyOf();
		double speed = ship.speed();
		double minR = Double.MAX_VALUE;
		for (int t=0; t<ticks; t++)
		{
			final Pair<Dir3D, Double> grav = getGravityForcer(ship);
			if (grav != null && grav.second.doubleValue() > 0)
				speed = accelSpaceObject(velDir, speed, grav.first, grav.second.doubleValue() * dt);
			dirToTarget = getDirection(pos, target);
			final Dir3D thrustDir = getMiddleAngle(velDir, dirToTarget);
			speed = accelSpaceObject(velDir, speed, thrustDir, thrustAccel * dt);
			pos = moveSpaceObject(pos, velDir, (long) speed);
			final double currR = getDistanceFrom(pos, startPos);
			if (currR < minR)
				minR = currR;
			if (minR < safePeri)
				return false;
			if (getDistanceFrom(pos, target) < ship.radius() * 2)
				break;
		}
		return minR >= safePeri;
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
									   final Set<Coord3D> exceptions, final Set<Coord3D> ignoreTargets, final int depth)
	{
		final int MAX_DEPTH = 50;
		if (depth > MAX_DEPTH)
			return null;

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
					final List<Coord3D> subCourse =  plotFullCourse(newSubTarget, sradius, target, tradius, exceptions, ignore, depth+1);
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
		final List<Coord3D> course = plotFullCourse(osrc, sradius, otarget, tradius, exceptions, ignoreTargets,0);
		if(course == null)
			return new ArrayList<Coord3D>();
		while(course.size()>maxTicks)
			course.remove(course.size()-1);
		return course;
	}

	protected double calculateGravityForce(final double distance, final double graviRadiusMax)
	{
		return 0.5 + (0.5 * (1.0 - (distance / graviRadiusMax)));
	}

	@Override
	public Pair<Dir3D, Double> getNetAccelerationAfterGravity(final SpaceObject ship, final Dir3D thrustDir, final double thrustAccel)
	{
		if (thrustAccel <= 0.0)
			return null;
		final Pair<Dir3D, Double> grav = getGravityForcer(ship);
		if (grav == null || grav.second.doubleValue() <= 0.01)
			return new Pair<Dir3D, Double>(thrustDir, Double.valueOf(thrustAccel));
		final BigVector gravVec = new BigVector(grav.first).sphereToCartesian().scalarProduct(BigDecimal.valueOf(grav.second.doubleValue()));
		final BigVector thrustVec = new BigVector(thrustDir).sphereToCartesian().scalarProduct(BigDecimal.valueOf(thrustAccel));
		final BigVector netVec = thrustVec.subtract(gravVec);
		final BigDecimal netMag = netVec.magnitude();
		if (netMag.compareTo(BigCMath.ZERO_ALMOST) <= 0)
			return null; // Grav wins, no thrust
		final Dir3D netDir = netVec.cartesianToSphere();
		return new Pair<Dir3D, Double>(netDir, Double.valueOf(netMag.doubleValue()));
	}

	@Override
	public double estimateOrbitalSpeed(final SpaceObject planet)
	{
		if((planet == null)||(planet.getMass() <= 0)||(planet.radius() <= 0))
			return 0.0;
		final long maxDistance = Math.round(CMath.mul(planet.radius(), SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS));
		final long minDistance = planet.radius() + Math.round(CMath.mul(0.75, maxDistance - planet.radius()));
		final long orbitalRadius = (minDistance + maxDistance) / 2;
		if(orbitalRadius <= planet.radius())
			return 0.0;
		final long distance = orbitalRadius - planet.radius();
		if(((planet instanceof Area)||(planet.getMass() >=SpaceObject.ASTEROID_MASS))
		&& (distance > 0)
		&& (distance <= CMath.mul(SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS, planet.radius())))
		{
			final double graviRadiusMax = (planet.radius() * (SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS - 1.0));
			if (distance < graviRadiusMax)
			{
				final double gravityInfluence = calculateGravityForce(distance, graviRadiusMax);
				return Math.sqrt(CMath.mul(gravityInfluence , orbitalRadius));
			}
		}
		return 0.0;
	}

	@Override
	public Pair<Dir3D, Double> getGravityForcer(final SpaceObject S)
	{
		if (S.amDestroyed())
			return null;

		final GravityCacheEntry entry = gravCache.get(S);
		if((entry != null)
		&&(entry.tick == tickCounter))
		{
			if (entry.accel > 0.0)
				return new Pair<Dir3D, Double>(entry.dir, Double.valueOf(entry.accel));
			return null;
		}
		BigVector totalGrav = new BigVector(BigCMath.ZERO, BigCMath.ZERO, BigCMath.ZERO);
		final BoundedCube baseCube = new BoundedCube(S.coordinates(), 10*SpaceObject.Distance.LightSecond.dm);
		for(final SpaceObject O : getSpaceObjectsInBound(baseCube))
		{
			if((O == S) || (O.amDestroyed()))
				continue;
			final double f = getGravityForce(S, O);
			if(f > 0)
			{
				final Dir3D thisDir = getDirection(S.coordinates(), O.coordinates());
				final BigVector unitVec = new BigVector(thisDir).sphereToCartesian();
				final BigVector forceVec = unitVec.scalarProduct(BigDecimal.valueOf(f));
				totalGrav = totalGrav.add(forceVec); // Sum vectors
			}
		}
		final BigDecimal mag = totalGrav.magnitude();
		Pair<Dir3D, Double> pair = null;
		if(mag.compareTo(BigCMath.ZERO) > 0)
		{
			final BigVector unit = totalGrav.scalarProduct(BigCMath.ONE.divide(mag, BigVector.SCALE, BigVector.ROUND)); // Normalize
			final Dir3D gravDir = unit.cartesianToSphere();
			final double gravForce = mag.doubleValue();
			pair = new Pair<Dir3D, Double>(gravDir, Double.valueOf(gravForce));
			final GravityCacheEntry newEntry = new GravityCacheEntry(gravForce, gravDir, tickCounter);
			gravCache.put(S, newEntry);
		}
		else
		{
			// Cache zero
			final GravityCacheEntry zeroEntry = new GravityCacheEntry(tickCounter);
			gravCache.put(S, zeroEntry);
		}
		return pair;
	}

	@Override
	public double getGravityForce(final SpaceObject S, final SpaceObject cO)
	{
		final long distance=getDistanceFrom(S.coordinates(), cO.coordinates()) - cO.radius();
		final long oMass = S.getMass();
		if(((cO instanceof Area)||(cO.getMass() >= SpaceObject.ASTEROID_MASS))
		&&(distance > 0)
		&&(oMass < SpaceObject.MOONLET_MASS)
		&&(distance <= CMath.mul(SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS, cO.radius())))
		{
			final double graviRadiusMax=(cO.radius()*(SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS-1.0));
			if(distance<graviRadiusMax)
				return calculateGravityForce(distance, graviRadiusMax);
		}
		return 0;
	}

	@Override
	public Dir3D getGraviticCourseCorrection(final Dir3D dir, final double thrustAccel, final Dir3D gravDir, final double gravAccel)
	{
		final Dir3D antiGravDir = CMLib.space().getOppositeDir(gravDir);
		if(thrustAccel <= 0)
			return dir;
		final double tilt = Math.asin(Math.min(gravAccel / thrustAccel, 1.0));
		final double weight = Math.sin(tilt);
		final double[] baseVec = dir.toArray3(1.0);
		final double[] antiVec = antiGravDir.toArray3(1.0);
		final double[] blended = new double[3];
		for(int i=0; i<3; i++)
			blended[i] = ((1.0 - weight) * baseVec[i]) + (weight * antiVec[i]);
		double norm = 0.0;
		for (final double v : blended)
			norm += v * v;
		norm = Math.sqrt(norm);
		if(norm > BigCMath.ALMOST_ZERO.doubleValue())
		{
			for (int i=0; i<3; i++)
				blended[i] /= norm;
		}
		return Dir3D.fromArray3(blended);
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
		tickCounter++;
		for(final Enumeration<SpaceObject> o = space.elements(); o.hasMoreElements(); )
		{
			final SpaceObject O=o.nextElement();
			if(O.amDestroyed())
			{
				gravCache.remove(O);
				continue;
			}
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
				final List<SpaceObject> cOs=getSpaceObjectsWithin(O, 0, Math.max(10*SpaceObject.Distance.LightSecond.dm,2*Math.round(speed)));
				final long oMass = O.getMass();
				// objects should already be sorted by closeness for good collision detection
				if(isDebuggingHARD && moving)
				{
					Log.debugOut("Space Object "+O.name()+" moved "+Math.round(speed)+"dam/s in dir " +
							CMLib.english().directionDescShort(O.direction().toDoubles())+" to "+
							CMLib.english().coordDescShort(O.coordinates().toLongs()));
				}
				BigVector totalGrav = new BigVector(BigCMath.ZERO, BigCMath.ZERO, BigCMath.ZERO);
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
								Log.debugOut("SpaceShip "+O.name()+" is gravitating "+gravitationalMove+"dam towards " +cO.Name());
							final Dir3D directionTo=getDirection(O, cO);
							final BigVector unitVec = new BigVector(directionTo).sphereToCartesian();
							final BigVector forceVec = unitVec.scalarProduct(BigDecimal.valueOf(gravitationalMove));
							totalGrav = totalGrav.add(forceVec);
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
				final BigDecimal mag = totalGrav.magnitude();
				if(mag.compareTo(BigCMath.ZERO) > 0)
				{
					final BigVector unit = totalGrav.scalarProduct(BigCMath.ONE.divide(mag, BigVector.SCALE, BigVector.ROUND));
					final Dir3D gravDir = unit.cartesianToSphere();
					final double gravAccel = mag.doubleValue();
					accelSpaceObject(O, gravDir, gravAccel);
					final GravityCacheEntry newEntry = new GravityCacheEntry(gravAccel, gravDir, tickCounter);
					inAirFlag = true;
					gravCache.put(O, newEntry);
				}
				else
					gravCache.put(O, new GravityCacheEntry(tickCounter));
				if(S!=null)
					S.setShipFlag(SpaceShip.ShipFlag.IN_THE_AIR,inAirFlag);
			}
		}
		gravCache.keySet().retainAll(space);
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
