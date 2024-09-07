package com.planet_ink.coffee_mud.core.collections;

import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.CMath;
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
* The interface represents a 3d tube thing.
* @author Bo Zimmerman
*
*/
public class BoundedTube extends BoundedSphere
{
	public Coord3D	exp;
	public double[] 	dir;
	public long			dist;

	public BoundedTube(final BoundedSphere l, final double[] direction, final long distance)
	{
		super(l);
		this.dir = direction;
		this.dist = distance;
		exp = new Coord3D(extendTo(distance));
	}

	public BoundedTube(final BoundedSphere l)
	{
		super(l);
		this.exp = null;
		this.dir = new double[2];
		this.dist = 2;
	}

	private long[] extendTo(final long distance)
	{
		final long[] start = xyz.toLongs();
		final double x1=Math.cos(dir[0])*Math.sin(dir[1]);
		final double y1=Math.sin(dir[0])*Math.sin(dir[1]);
		final double z1=Math.cos(dir[1]);
		final long speed = this.dist/2;
		return new long[]{start[0]+Math.round(CMath.mul(speed,x1)),
						start[1]+Math.round(CMath.mul(speed,y1)),
						start[2]+Math.round(CMath.mul(speed,z1))};
	}

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

	@Override
	public BoundedSphere getSphere()
	{
		if(exp != null)
		{
			final long[] mid = extendTo(this.dist/2);
			return new BoundedSphere(mid,dist);
		}
		return new BoundedSphere(xyz.toLongs(), radius);
	}

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
