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
 * A bounded cube is a rectangular prism defined by two corners, the lower left
 * corner (lx,ty,iz) and the upper right corner (rx,by,oz). It can be used to
 * represent a 3D area in space.
 *
 * @author Bo Zimmerman
 */
public class BoundedCube implements Comparable<BoundedObject>, BoundedObject
{
	public long	lx, ty, iz = 0;
	public long	rx, by, oz = 0;

	/**
	 * Creates a new bounded cube with all coordinates set to zero.
	 */
	public BoundedCube()
	{
		super();
	}

	/**
	 * Creates a new bounded cube with the given coordinates.
	 *
	 * @param lx the left x coordinate
	 * @param rx the right x coordinate
	 * @param ty the top y coordinate
	 * @param by the bottom y coordinate
	 * @param iz the inner z coordinate
	 * @param oz the outer z coordinate
	 */
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

	/**
	 * Creates a new bounded cube with the given coordinates.
	 *
	 * @param coords the coordinates as an array of longs: [x,y,z]
	 * @param radius the radius around the coordinates
	 */
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

	/**
	 * Creates a new bounded cube with the given coordinates.
	 *
	 * @param coords the coordinates as a Coord3D object
	 * @param radius the radius around the coordinates
	 */
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

	/**
	 * Creates a new bounded cube that is a copy of the given bounded cube.
	 *
	 * @param l the bounded cube to copy
	 */
	public BoundedCube(final BoundedCube l)
	{
		super();
		set(l);
	}

	/**
	 * Sets this bounded cube to be a copy of the given bounded cube.
	 *
	 * @param l the bounded cube to copy
	 */
	public void set(final BoundedCube l)
	{
		this.lx = l.lx;
		this.rx = l.rx;
		this.ty = l.ty;
		this.by = l.by;
		this.iz = l.iz;
		this.oz = l.oz;
	}

	/**
	 * Expands this bounded cube to include the given bounded cube.
	 *
	 * @param l the bounded cube to include
	 */
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

	/**
	 * Returns the radius of this bounded cube, which is the distance from its center
	 * 	to one of its corners.
	 */
	@Override
	public long radius()
	{
		return Math.round(Math.sqrt(((rx - lx) * (rx - lx))
								   +((by - ty) * (by - ty))
								   +((oz - iz) * (oz - iz))));
	}

	/**
	 * Expands this bounded cube in the given direction by the given distance.
	 *
	 * @param direction the direction to expand in
	 * @param distance the distance to expand
	 * @return a new bounded cube that is the result of the expansion
	 */
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

	/**
	 * Returns whether this bounded cube intersects with the given bounded object.
	 * @param two the other bounded object
	 * @return true if they intersect, false otherwise
	 */
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

	/**
	 * Returns whether this bounded cube contains the given point.
	 *
	 * @param x the x coordinate of the point
	 * @param y the y coordinate of the point
	 * @param z the z coordinate of the point
	 * @return true if the point is contained within this bounded cube, false
	 *         otherwise
	 */
	public boolean contains(final long x, final long y, final long z)
	{
		return ((x >= lx)
			  &&(x <= rx)
			  &&(y >= ty)
			  &&(y <= by)
			  &&(z >= iz)
			  &&(z <= oz));
	}

	/**
	 * Returns whether this bounded cube contains the given point.
	 *
	 * @param c the coordinates of the point as an array of longs: [x,y,z]
	 * @return true if the point is contained within this bounded cube, false
	 *         otherwise
	 */
	public boolean contains(final long[] c)
	{
		return ((c[0] >= lx)
			  &&(c[0] <= rx)
			  &&(c[1] >= ty)
			  &&(c[1] <= by)
			  &&(c[2] >= iz)
			  &&(c[2] <= oz));
	}

	/**
	 * Returns whether this bounded cube contains the given point.
	 *
	 * @param c the coordinates of the point as a Coord3D object
	 * @return true if the point is contained within this bounded cube, false
	 *         otherwise
	 */
	public boolean contains(final Coord3D c)
	{
		return ((c.x().longValue() >= lx)
			  &&(c.x().longValue() <= rx)
			  &&(c.y().longValue() >= ty)
			  &&(c.y().longValue() <= by)
			  &&(c.z().longValue() >= iz)
			  &&(c.z().longValue() <= oz));
	}

	/**
	 * Returns the width of this bounded cube.
	 *
	 * @return the width of this bounded cube
	 */
	public long width()
	{
		return rx - lx;
	}

	/**
	 * Returns the height of this bounded cube.
	 *
	 * @return the height of this bounded cube
	 */
	public long height()
	{
		return by - ty;
	}

	/**
	 * Returns the depth of this bounded cube.
	 *
	 * @return the depth of this bounded cube
	 */
	public long depth()
	{
		return oz - iz;
	}

	/**
	 * Returns a BoundedSphere that completely contains this bounded cube.
	 * @return a BoundedSphere that completely contains this bounded cube
	 */
	@Override
	public BoundedSphere getSphere()
	{
		return new BoundedSphere(center(), radius());
	}

	/**
	 * Returns the center of this bounded cube.
	 *
	 * @return the center of this bounded cube as a Coord3D object
	 */
	@Override
	public Coord3D center()
	{
		return new Coord3D(new long[]{((lx+rx)/2),((ty+rx)/2),((iz+oz)/2)});
	}

	/**
	 * Compares this bounded cube to another bounded object.
	 */
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

	/**
	 * Returns this bounded cube.
	 */
	@Override
	public BoundedCube getCube()
	{
		return this;
	}
}
