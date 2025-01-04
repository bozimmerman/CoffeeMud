package com.planet_ink.coffee_mud.core.collections;

import java.math.BigDecimal;

import com.planet_ink.coffee_mud.core.CMath;
import com.planet_ink.coffee_mud.core.interfaces.BoundedObject;
/*
Copyright 2013-2025 Bo Zimmerman

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

	public BoundedCube(final Coord3D coords, final long radius)
	{
		super();
		this.lx = coords.x().longValue() - radius;
		this.rx = coords.x().longValue() + radius;
		this.ty = coords.y().longValue() - radius;
		this.by = coords.y().longValue() + radius;
		this.iz = coords.z().longValue() - radius;
		this.oz = coords.z().longValue() + radius;
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

	public BoundedCube expand(final Dir3D direction, final long distance)
	{
		// this is silly -- it's just a giant cube
		final BoundedCube cube=new BoundedCube(this);
		final BigDecimal bigDistance=BigDecimal.valueOf(distance);
		final BigDecimal x1=Dir3D.cos(direction.xy()).multiply(Dir3D.sin(direction.z()));
		final BigDecimal y1=Dir3D.sin(direction.xy()).multiply(Dir3D.sin(direction.z()));
		final BigDecimal z1=Dir3D.cos(direction.z());
		final Coord3D oldCenter=center();
		final Coord3D newCenter=new Coord3D(
			oldCenter.x().add(bigDistance.multiply(x1)),
			oldCenter.y().add(bigDistance.multiply(y1)),
			oldCenter.z().add(bigDistance.multiply(z1))
		);
		if(newCenter.x().compareTo(oldCenter.x())>0)
			cube.rx+=newCenter.x().subtract(oldCenter.x()).longValue();
		else
			cube.lx+=newCenter.x().subtract(oldCenter.x()).longValue();
		if(newCenter.y().compareTo(oldCenter.y())>0)
			cube.ty+=newCenter.y().subtract(oldCenter.y()).longValue();
		else
			cube.by+=newCenter.y().subtract(oldCenter.y()).longValue();
		if(newCenter.z().compareTo(oldCenter.z())>0)
			cube.iz+=newCenter.z().subtract(oldCenter.z()).longValue();
		else
			cube.oz+=newCenter.z().subtract(oldCenter.z()).longValue();
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

	public boolean contains(final Coord3D c)
	{
		return ((c.x().longValue() >= lx)
			  &&(c.x().longValue() <= rx)
			  &&(c.y().longValue() >= ty)
			  &&(c.y().longValue() <= by)
			  &&(c.z().longValue() >= iz)
			  &&(c.z().longValue() <= oz));
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
	public Coord3D center()
	{
		return new Coord3D(new long[]{((lx+rx)/2),((ty+rx)/2),((iz+oz)/2)});
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
