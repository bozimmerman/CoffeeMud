package com.planet_ink.coffee_mud.core.collections;

import java.math.BigDecimal;

import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.CMath;
import com.planet_ink.coffee_mud.core.interfaces.BoundedObject;
/*
Copyright 2024-2025 Bo Zimmerman

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

/**
 * A bounded tube is a bounded sphere with a direction and distance. It
 * represents a line segment with a radius.
 *
 * @author Bo Zimmerman
 */
public class BoundedTube extends BoundedSphere
{
	public Coord3D	exp;
	public Dir3D 	dir;
	public long		dist;

	/**
	 * Construct a bounded tube
	 *
	 * @param l the bounded sphere representing the start point and radius
	 * @param direction the direction from the start point
	 * @param distance the distance from the start point
	 */
	public BoundedTube(final BoundedSphere l, final Dir3D direction, final long distance)
	{
		super(l);
		this.dir = direction;
		this.dist = distance;
		exp = new Coord3D(extendTo(distance));
	}

	/**
	 * Construct a bounded tube with no direction or distance
	 *
	 * @param l the bounded sphere representing the start point and radius
	 */
	public BoundedTube(final BoundedSphere l)
	{
		super(l);
		this.exp = null;
		this.dir = new Dir3D();
		this.dist = 2;
	}

	/**
	 * Extend the line to the given distance
	 * @param distance the distance to extend to
	 * @return the coordinate at that distance
	 */
	private Coord3D extendTo(final long distance)
	{
		final Coord3D start = xyz.copyOf();
		final BigDecimal x1=Dir3D.cos(dir.xy()).multiply(Dir3D.sin(dir.z()));
		final BigDecimal y1=Dir3D.sin(dir.xy()).multiply(Dir3D.sin(dir.z()));
		final BigDecimal z1=Dir3D.cos(dir.z());
		final BigDecimal speed = BigDecimal.valueOf(this.dist/2);
		return new Coord3D(start.x().add(speed.multiply(x1)),
						   start.y().add(speed.multiply(y1)),
						   start.z().add(speed.multiply(z1)));
	}

	/**
	 * Compare this bounded tube to another bounded object
	 * @param o the other bounded object
	 * @return negative if less than, positive if greater than, 0 if equal
	 */
	@Override
	public int compareTo(final BoundedObject o)
	{
		if(o instanceof BoundedTube)
		{
			if(exp != null)
			{
				if(((BoundedTube)o).exp == null)
					return 1;
				final int c = exp.compareTo(((BoundedTube)o).exp);
				if(c != 0)
					return c;
			}
			else
			if(((BoundedTube)o).exp != null)
				return -1;
		}
		else
		if(exp != null)
			return 1;
		return super.compareTo(o);
	}

	/**
	 * Get the bounding sphere of this tube
	 *
	 * @return the bounding sphere
	 */
	@Override
	public BoundedSphere getSphere()
	{
		if(exp != null)
		{
			final Coord3D mid = extendTo(this.dist/2);
			return new BoundedSphere(mid,dist);
		}
		return new BoundedSphere(xyz, radius);
	}

	/**
	 * Check to see if this bounded tube intersects another bounded object
	 *
	 * @param two the other bounded object
	 * @return true if they intersect
	 */
	@Override
	public boolean intersects(final BoundedObject two)
	{
		if(exp == null)
		{
			if(two instanceof BoundedTube)
			{
				if(((BoundedTube)two).exp == null)
					return super.intersects(two);
				final double dist = CMLib.space().getMinDistanceFrom(((BoundedTube)two).xyz,
																	((BoundedTube)two).exp,
																	xyz);
				return dist < radius() + two.radius();
			}
			else
				return super.intersects(two);
		}
		else
		if(two instanceof BoundedTube)
		{
			if(((BoundedTube)two).exp == null) // line vs point
			{
				final double dist = CMLib.space().getMinDistanceFrom(xyz, exp,
																	((BoundedTube)two).xyz);
				return dist < radius() + two.radius();
			}
			// line vs line
			final double dist = CMLib.space().getMinDistanceFrom(xyz, exp,
														((BoundedTube)two).xyz,
														((BoundedTube)two).exp);
			return dist < radius() + two.radius();
		}
		else
		if(two instanceof BoundedSphere)
		{
			final double dist = CMLib.space().getMinDistanceFrom(xyz, exp,
																((BoundedSphere)two).xyz);
			return dist < radius() + two.radius();
		}
		else
			return super.intersects(two);
	}
}
