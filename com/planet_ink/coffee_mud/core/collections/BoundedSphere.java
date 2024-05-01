package com.planet_ink.coffee_mud.core.collections;

import java.math.BigInteger;

import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.interfaces.BoundedObject;

/*
Copyright 2024-2024 Bo Zimmerman

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
* The interface represents a 3d sphere thing.
* @author Bo Zimmerman
*
*/
public class BoundedSphere implements Comparable<BoundedObject>, BoundedObject
{
	public BigVector	xyz;
	public long			radius;

	public BoundedSphere(final BoundedSphere l)
	{
		super();
		set(l);
	}

	public BoundedSphere(final long[] center, final long radius)
	{
		super();
		xyz = new BigVector(center);
		this.radius = radius;
	}

	public void set(final BoundedSphere l)
	{
		this.xyz = new BigVector(l.xyz);
		this.radius = l.radius;
	}

	@Override
	public long[] center()
	{
		return xyz.toLongs();
	}

	@Override
	public long radius()
	{
		return radius;
	}

	@Override
	public BoundedCube getCube()
	{
		return new BoundedCube(xyz.toLongs(), radius);
	}

	@Override
	public BoundedSphere getSphere()
	{
		return this;
	}

	public BoundedTube expand(final double[] direction, final long distance)
	{
		return new BoundedTube(this, direction, distance);
	}

	public long distanceFrom(final long[] coord2)
	{
		final BigInteger coord_0 = BigInteger.valueOf(xyz.x().longValue()).subtract(BigInteger.valueOf(coord2[0]));
		final BigInteger coord_0m = coord_0.multiply(coord_0);
		final BigInteger coord_1 = BigInteger.valueOf(xyz.y().longValue()).subtract(BigInteger.valueOf(coord2[1]));
		final BigInteger coord_1m = coord_1.multiply(coord_1);
		final BigInteger coord_2 = BigInteger.valueOf(xyz.z().longValue()).subtract(BigInteger.valueOf(coord2[2]));
		final BigInteger coord_2m = coord_2.multiply(coord_2);
		final BigInteger coords_all = coord_0m.add(coord_1m).add(coord_2m);
		return Math.round(Math.sqrt(coords_all.doubleValue()));
	}

	public boolean contains(final long x, final long y, final long z)
	{
		return distanceFrom(new long[] {x,y,z}) <= radius;
	}

	public boolean contains(final long[] c)
	{
		return distanceFrom(c) <= radius;
	}

	public boolean intersects(final BoundedObject two)
	{
		if(two==null)
			return false;
		if(((two instanceof BoundedTube)
		&&((BoundedTube)two).exp != null))
			return ((BoundedTube)two).intersects(this);
		if(two instanceof BoundedSphere)
			return distanceFrom(((BoundedSphere)two).xyz.toLongs()) < ( radius() + two.radius());
		if(two instanceof BoundedCube)
			return getCube().intersects(two);
		return false;
	}

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
