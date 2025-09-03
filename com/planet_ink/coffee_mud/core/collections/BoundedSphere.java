package com.planet_ink.coffee_mud.core.collections;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.planet_ink.coffee_mud.core.CMLib;
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
 * A bounded sphere is a 3D sphere with a center and radius.
 *
 * @author Bo Zimmerman
 */
public class BoundedSphere implements Comparable<BoundedObject>, BoundedObject
{
	public Coord3D	xyz;
	public long		radius;

	/**
	 * Constructs a new bounded sphere that copies the given one.
	 * @param l the bounded sphere to copy
	 */
	public BoundedSphere(final BoundedSphere l)
	{
		super();
		set(l);
	}

	/**
	 * Constructs a new bounded sphere at the given center with a
	 * the given radius.
	 *
	 * @param center the center of the sphere
	 * @param radius the radius of the sphere
	 */
	public BoundedSphere(final long[] center, final long radius)
	{
		super();
		xyz = new Coord3D(center);
		this.radius = radius;
	}

	/**
	 * Constructs a new bounded sphere at the given center with a the given
	 * radius.
	 *
	 * @param center the center of the sphere
	 * @param radius the radius of the sphere
	 */
	public BoundedSphere(final Coord3D center, final long radius)
	{
		super();
		xyz = new Coord3D(center);
		this.radius = radius;
	}

	/**
	 * Sets this bounded sphere to be a copy of the given one.
	 *
	 * @param l the bounded sphere to copy
	 */
	public void set(final BoundedSphere l)
	{
		this.xyz = new Coord3D(l.xyz);
		this.radius = l.radius;
	}

	/**
	 * Returns the center of this sphere.
	 */
	@Override
	public Coord3D center()
	{
		return xyz;
	}

	/**
	 * Returns the radius of this sphere.
	 */
	@Override
	public long radius()
	{
		return radius;
	}

	/**
	 * Returns a BoundedCube that fully contains this sphere.
	 */
	@Override
	public BoundedCube getCube()
	{
		return new BoundedCube(xyz.toLongs(), radius);
	}

	/**
	 * Returns this sphere.
	 */
	@Override
	public BoundedSphere getSphere()
	{
		return this;
	}

	/**
	 * Expands this sphere in the given direction by the given distance,
	 * returning a new bounded tube.
	 *
	 * @param direction the direction to expand
	 * @param distance the distance to expand
	 * @return a new bounded tube
	 */
	public BoundedTube expand(final Dir3D direction, final long distance)
	{
		return new BoundedTube(this, direction, distance);
	}

	/**
	 * Returns the distance from this sphere's center to the given coordinate.
	 *
	 * @param coord2 the coordinate to measure to
	 * @return the distance from this sphere's center to the given coordinate
	 */
	public long distanceFrom(final Coord3D coord2)
	{
		final BigDecimal coord_0 = xyz.x().subtract(coord2.x());
		final BigDecimal coord_0m = coord_0.multiply(coord_0);
		final BigDecimal coord_1 = xyz.y().subtract(coord2.y());
		final BigDecimal coord_1m = coord_1.multiply(coord_1);
		final BigDecimal coord_2 = xyz.z().subtract(coord2.z());
		final BigDecimal coord_2m = coord_2.multiply(coord_2);
		final BigDecimal coords_all = coord_0m.add(coord_1m).add(coord_2m);
		return Math.round(Math.sqrt(coords_all.doubleValue()));
	}

	/**
	 * Returns whether the given coordinate is contained within this sphere.
	 *
	 * @param c the coordinate to check
	 * @return true if the given coordinate is contained within this sphere
	 */
	public boolean contains(final Coord3D c)
	{
		return distanceFrom(c) <= radius;
	}

	/**
	 * Returns whether the given bounded object intersects with this sphere.
	 *
	 * @param two the bounded object to check
	 * @return true if the given bounded object intersects with this sphere
	 */
	public boolean intersects(final BoundedObject two)
	{
		if(two==null)
			return false;
		if(((two instanceof BoundedTube)
		&&((BoundedTube)two).exp != null))
			return ((BoundedTube)two).intersects(this);
		if(two instanceof BoundedSphere)
			return distanceFrom(((BoundedSphere)two).xyz) < ( radius() + two.radius());
		if(two instanceof BoundedCube)
			return getCube().intersects(two);
		return false;
	}

	/**
	 * Compares this bounded object with the given one.
	 * @param o the bounded object to compare to
	 * @return -1, 0, or 1 as per Comparable
	 */
	@Override
	public int compareTo(final BoundedObject o)
	{
		if(o==null)
			return -1;
		final long oRadius = o.radius();
		if(radius<oRadius)
			return -1;
		if(radius>oRadius)
			return 1;
		if(o instanceof BoundedSphere)
			return xyz.compareTo(((BoundedSphere)o).xyz);
		if(o instanceof BoundedTube)
			return getCube().compareTo(o);
		return -1;
	}
}
