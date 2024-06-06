package com.planet_ink.coffee_mud.core.collections;

import com.planet_ink.coffee_mud.core.CMath;
import com.planet_ink.coffee_mud.core.interfaces.BoundedObject;
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
/**
* The interface represents a 3d cubed thing.
* @author Bo Zimmerman
*
*/
public class BoundedCube implements Comparable<BoundedObject>, BoundedObject
{
	public long	lx, ty, iz = 0;
	public long	rx, by, oz = 0;

	public BoundedCube()
	{
		super();
	}

	public BoundedCube(final long lx, final long rx, final long ty, final long by, final long iz, final long oz)
	{
		super();
		this.lx = lx;
		this.rx = rx;
		this.ty = ty;
		this.by = by;
		this.iz = iz;
		this.oz = oz;
	}

	public BoundedCube(final long[] coords, final long radius)
	{
		super();
		this.lx = coords[0] - radius;
		this.rx = coords[0] + radius;
		this.ty = coords[1] - radius;
		this.by = coords[1] + radius;
		this.iz = coords[2] - radius;
		this.oz = coords[2] + radius;
	}

	public BoundedCube(final BoundedCube l)
	{
		super();
		set(l);
	}

	public void set(final BoundedCube l)
	{
		this.lx = l.lx;
		this.rx = l.rx;
		this.ty = l.ty;
		this.by = l.by;
		this.iz = l.iz;
		this.oz = l.oz;
	}

	public void union(final BoundedCube l)
	{
		if(l.lx < lx)
			lx=l.lx;
		if(l.rx > rx)
			rx=l.rx;
		if(l.ty < ty)
			ty=l.ty;
		if(l.by > by)
			by=l.by;
		if(l.iz < iz)
			iz=l.iz;
		if(l.oz > oz)
			oz=l.oz;
	}

	@Override
	public long radius()
	{
		return Math.round(Math.sqrt(((rx - lx) * (rx - lx))
								   +((by - ty) * (by - ty))
								   +((oz - iz) * (oz - iz))));
	}

	public BoundedCube expand(final double[] direction, final long distance)
	{
		// this is silly -- it's just a giant cube
		final BoundedCube cube=new BoundedCube(this);
		final double x1=Math.cos(direction[0])*Math.sin(direction[1]);
		final double y1=Math.sin(direction[0])*Math.sin(direction[1]);
		final double z1=Math.cos(direction[1]);
		final long[] oldCenter=center();
		final long[] newCenter=new long[]{
				oldCenter[0]+Math.round(CMath.mul(distance,x1)),
				oldCenter[1]+Math.round(CMath.mul(distance,y1)),
				oldCenter[2]+Math.round(CMath.mul(distance,z1))};
		if(newCenter[0]>oldCenter[0])
			cube.rx+=newCenter[0]-oldCenter[0];
		else
			cube.lx+=newCenter[0]-oldCenter[0];
		if(newCenter[1]>oldCenter[1])
			cube.ty+=newCenter[1]-oldCenter[1];
		else
			cube.by+=newCenter[1]-oldCenter[1];
		if(newCenter[2]>oldCenter[2])
			cube.iz+=newCenter[2]-oldCenter[2];
		else
			cube.oz+=newCenter[2]-oldCenter[2];
		return cube;
	}

	public boolean intersects(final BoundedObject two)
	{
		if(two==null)
			return false;
		if(two instanceof BoundedSphere)
			return ((BoundedSphere)two).intersects(this);
		final BoundedCube cub = (BoundedCube)two;
		// this is silly -- it's just a giant cube
		return (
			((lx <= cub.lx && cub.lx <= rx) || (cub.lx <= lx && lx <= cub.rx))
		&&	((ty <= cub.ty && cub.ty <= by) || (cub.ty <= ty && ty <= cub.by))
		&&	((iz <= cub.iz && cub.iz <= oz) || (cub.iz <= iz && iz <= cub.oz))
		);
	}

	public boolean contains(final long x, final long y, final long z)
	{
		return ((x >= lx)
			  &&(x <= rx)
			  &&(y >= ty)
			  &&(y <= by)
			  &&(z >= iz)
			  &&(z <= oz));
	}

	public boolean contains(final long[] c)
	{
		return ((c[0] >= lx)
			  &&(c[0] <= rx)
			  &&(c[1] >= ty)
			  &&(c[1] <= by)
			  &&(c[2] >= iz)
			  &&(c[2] <= oz));
	}

	public long width()
	{
		return rx - lx;
	}

	public long height()
	{
		return by - ty;
	}

	public long depth()
	{
		return oz - iz;
	}

	@Override
	public BoundedSphere getSphere()
	{
		return new BoundedSphere(center(), radius());
	}

	@Override
	public long[] center()
	{
		return new long[]{((lx+rx)/2),((ty+rx)/2),((iz+oz)/2)};
	}

	@Override
	public int compareTo(final BoundedObject o)
	{
		if(o == null)
			return 1;
		if(o instanceof BoundedSphere)
			return ((BoundedSphere)o).compareTo(this);
		final BoundedCube cub = (BoundedCube)o;
		if(lx<cub.lx)
			return -1;
		if(lx>cub.lx)
			return 1;
		if(rx<cub.rx)
			return -1;
		if(rx>cub.rx)
			return 1;
		if(ty<cub.ty)
			return -1;
		if(ty>cub.ty)
			return 1;
		if(by<cub.by)
			return -1;
		if(by>cub.by)
			return 1;
		if(iz<cub.iz)
			return -1;
		if(iz>cub.iz)
			return 1;
		if(oz<cub.oz)
			return -1;
		if(oz>cub.oz)
			return 1;
		return 0;
	}

	@Override
	public BoundedCube getCube()
	{
		return this;
	}
}
