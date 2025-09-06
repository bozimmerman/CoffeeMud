package com.planet_ink.coffee_mud.core.collections;

import java.math.BigDecimal;

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
 * Represents an absolute position in 3d space as a
 * BigVector of signed longs (Long.MIN_VALUE to Long.MAX_VALUE).
 * The first is the x coordinate, the second is y, and the last
 * is z.
 *
 * @author Bo Zimmerman
 *
 */
public class Coord3D extends BigVector
{
	/**
	 * Constructs a new Coord3D, with x, y, and z all set to 0.
	 */
	public Coord3D()
	{
		super(3);
	}

	/**
	 * Constructs a new Coord3D, with the given x, y, and z values.
	 *
	 * @param v the vector of x,y,zs
	 */
	public Coord3D(final BigVector v)
	{
		super(3);
		if(v.length()!=3)
			throw new IllegalArgumentException();
		b[0]=v.b[0];
		b[1]=v.b[1];
		b[2]=v.b[2];
	}

	/**
	 * Constructs a new Coord3D, with the given x, y, and z values.
	 *
	 * @param v the array of x, y, and z coordinates
	 */
	public Coord3D(final long[] v)
	{
		super(3);
		if(v.length!=3)
			throw new IllegalArgumentException();
		b[0]=BigDecimal.valueOf(v[0]);
		b[1]=BigDecimal.valueOf(v[1]);
		b[2]=BigDecimal.valueOf(v[2]);
	}

	/**
	 * Constructs a new Coord3D, with the given x, y, and z values.
	 *
	 * @param v the array of x, y, and z coordinates
	 */
	public Coord3D(final BigDecimal[] v)
	{
		super(3);
		if(v.length!=3)
			throw new IllegalArgumentException();
		if(v[0] != null)
			b[0]=v[0];
		if(v[1] != null)
			b[1]=v[1];
		if(v[2] != null)
			b[2]=v[2];
	}

	/**
	 * Constructs a new Coord3D, with the given x, y, and z values.
	 *
	 * @param v0 the x coordinate
	 * @param v1 the y coordinate
	 * @param v2 the z coordinate
	 */
	public Coord3D(final BigDecimal v0, final BigDecimal v1, final BigDecimal v2)
	{
		super(3);
		if(v0 != null)
			b[0]=v0;
		if(v1 != null)
			b[1]=v1;
		if(v2 != null)
			b[2]=v2;
	}

	/**
	 * Returns the x coordinate.
	 * @return the x coordinate
	 */
	public BigDecimal x()
	{
		if(b.length>0)
			return b[0];
		return null;
	}

	/**
	 * Returns the y coordinate.
	 *
	 * @return the y coordinate
	 */
	public BigDecimal y()
	{
		if(b.length>1)
			return b[1];
		return null;
	}

	/**
	 * Returns the z coordinate.
	 *
	 * @return the z coordinate
	 */
	public BigDecimal z()
	{
		if(b.length>2)
			return b[2];
		return null;
	}

	/**
	 * Returns the x coordinate as a long.
	 *
	 * @return the x coordinate
	 */
	public long xl()
	{
		if(b.length>0)
			return b[0].longValue();
		return 0;
	}

	/**
	 * Returns the y coordinate as a long.
	 *
	 * @return the y coordinate
	 */
	public long yl()
	{
		if(b.length>1)
			return b[1].longValue();
		return 0;
	}

	/**
	 * Returns the z coordinate as a long.
	 *
	 * @return the z coordinate
	 */
	public long zl()
	{
		if(b.length>2)
			return b[2].longValue();
		return 0;
	}

	/**
	 * Returns the coordinate at the given index as a long.
	 *
	 * @param x the index
	 * @return the coordinate at the given index
	 */
	public long getl(final int x)
	{
		if((x>=0)&&(x<b.length))
			return b[x].longValue();
		return 0;
	}

	/**
	 * Returns the coordinate at the given index as a BigDecimal.
	 *
	 * @param x the index
	 * @return the coordinate at the given index
	 */
	public BigDecimal get(final int x)
	{
		if((x>=0)&&(x<b.length))
			return b[x];
		return null;
	}

	/**
	 * Sets the x coordinate to the given value.
	 *
	 * @param d the new x coordinate
	 * @return this
	 */
	public Coord3D x(final BigDecimal d)
	{
		if(d!=null)
			b[0] = d;
		return this;
	}

	/**
	 * Sets the y coordinate to the given value.
	 *
	 * @param d the new y coordinate
	 * @return this
	 */
	public Coord3D y(final BigDecimal d)
	{
		if(d!=null)
			b[1] = d;
		return this;
	}

	/**
	 * Sets the z coordinate to the given value.
	 *
	 * @param d the new z coordinate
	 * @return this
	 */
	public Coord3D z(final BigDecimal d)
	{
		if(d!=null)
			b[2] = d;
		return this;
	}

	/**
	 * Returns a copy of this Coord3D.
	 *
	 * @return a copy of this Coord3D
	 */
	public Coord3D copyOf()
	{
		return new Coord3D(this);
	}

	/**
	 * Sets the x coordinate to the given value.
	 *
	 * @param d the new x coordinate
	 * @return this
	 */
	public Coord3D x(final long d)
	{
		return x(new BigDecimal(d));
	}

	/**
	 * Sets the y coordinate to the given value.
	 *
	 * @param d the new y coordinate
	 * @return this
	 */
	public Coord3D y(final long d)
	{
		return y(new BigDecimal(d));
	}

	/**
	 * Sets the z coordinate to the given value.
	 *
	 * @param d the new z coordinate
	 * @return this
	 */
	public Coord3D z(final long d)
	{
		return z(new BigDecimal(d));
	}

	/**
	 * Sets the coordinate at the given index to the given value.
	 *
	 * @param index the index
	 * @param d the new value
	 */
	public void set(final int index, final long d)
	{
		if((index>=0)&&(index<b.length))
			b[index]=new BigDecimal(d);
	}

	/**
	 * Sets the coordinate at the given index to the given value.
	 *
	 * @param index the index
	 * @param d the new value
	 */
	public void set(final int index, final BigDecimal d)
	{
		if((index>=0)&&(index<b.length)&&(d!=null))
			b[index]=d;
	}

	/**
	 * Adds the given vector to this one, returning a new Coord3D.
	 *
	 * @param v the vector to add
	 * @return the new Coord3D
	 */
	public Coord3D add(final Coord3D v)
	{
		return new Coord3D(super.add(v));
	}

	/**
	 * Subtracts the given vector from this one, returning a new Coord3D.
	 *
	 * @param v the vector to subtract
	 * @return the new Coord3D
	 */
	public Coord3D subtract(final Coord3D v)
	{
		return new Coord3D(super.subtract(v));
	}

	/**
	 * Returns whether the given Coord3D is equal to this one.
	 * @param o the other object
	 * @return true if equal
	 */
	@Override
	public boolean equals(final Object o)
	{
		if(o instanceof Coord3D)
		{
			final Coord3D v = (Coord3D)o;
			if(v.length()!=b.length)
				return false;
			for(int i=0;i<b.length;i++)
				if(b[i].longValue() != v.b[i].longValue())
					return false;
			return true;
		}
		return super.equals(o);
	}
}
