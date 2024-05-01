package com.planet_ink.coffee_mud.core.collections;

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
* The interface represents a 3d tube thing.
* @author Bo Zimmerman
*
*/
public class BoundedTube extends BoundedSphere
{
	public BigVector	exp		= null;

	public BoundedTube(final BoundedSphere l, final double[] direction, final long distance)
	{
		super(l);
		exp = new BigVector(CMLib.space().moveSpaceObject(xyz.toLongs(), direction, distance));
	}

	public BoundedTube(final BoundedSphere l, final BigVector exp)
	{
		super(l);
		this.exp = exp;
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
			final long[] start = xyz.toLongs();
			final long[] end = exp.toLongs();
			final double[] dir = CMLib.space().getDirection(start, end);
			final long dist = CMLib.space().getDistanceFrom(start, end);
			final long[] mid = CMLib.space().moveSpaceObject(start, dir, dist/2);
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
				final double dist = CMLib.space().getMinDistanceFrom(((BoundedTube)two).xyz.toLongs(),
																	((BoundedTube)two).exp.toLongs(),
																	xyz.toLongs());
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
				final double dist = CMLib.space().getMinDistanceFrom(xyz.toLongs(), exp.toLongs(),
																	((BoundedTube)two).xyz.toLongs());
				return dist < radius() + two.radius();
			}
			// line vs line
			final double dist = CMLib.space().getMinDistanceFrom(xyz.toLongs(), exp.toLongs(),
														((BoundedTube)two).xyz.toLongs(),
														((BoundedTube)two).exp.toLongs());
			return dist < radius() + two.radius();
		}
		else
		if(two instanceof BoundedSphere)
		{
			final double dist = CMLib.space().getMinDistanceFrom(xyz.toLongs(), exp.toLongs(),
																((BoundedSphere)two).xyz.toLongs());
			return dist < radius() + two.radius();
		}
		else
			return super.intersects(two);
	}
}
