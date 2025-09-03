package com.planet_ink.coffee_mud.core.collections;

import java.math.BigDecimal;

import com.planet_ink.coffee_mud.core.BigCMath;

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
 * Represents a directional vector in 3d space as a pair of BigDecimals
 * in radians, stored in a 2d BigVector.
 *
 * The first of the pair is the full horizontal circle (2*pi), and the
 * second of the pair is the half z-axis circle (0-pi).
 *
 * @author Bo Zimmerman
 *
 */
public class Dir3D extends BigVector
{
	boolean safe = true;

	/**
	 * Construct an empty direction
	 */
	public Dir3D()
	{
		super(2);
	}

	/**
	 * Construct a direction, optionally enforcing safeness
	 *
	 * @param safeness true to enforce safeness limits on the angles
	 */
	public Dir3D(final boolean safeness)
	{
		super(2);
		safe=safeness;
	}

	/**
	 * Construct a direction from another direction
	 *
	 * @param v the other direction
	 * @throws IllegalArgumentException if the other direction is not 2d
	 */
	public Dir3D(final BigVector v)
	{
		super(2);
		if(v.length()!=2)
			throw new IllegalArgumentException();
		b[0]=v.b[0];
		b[1]=v.b[1];
	}

	/**
	 * Construct a direction from an array of doubles
	 *
	 * @param v the array of doubles, must be length 2
	 * @throws IllegalArgumentException if the array is not length 2
	 */
	public Dir3D(final double[] v)
	{
		super(2);
		if(v.length!=2)
			throw new IllegalArgumentException();
		xy(v[0]).z(BigDecimal.valueOf(v[1]));
	}

	/**
	 * Construct a direction from an array of BigDecimals
	 *
	 * @param v the array of BigDecimals, must be length 2
	 * @throws IllegalArgumentException if the array is not length 2
	 */
	public Dir3D(final BigDecimal[] v)
	{
		super(2);
		if(v.length!=2)
			throw new IllegalArgumentException();
		xy(v[0]).z(v[1]);
	}

	/**
	 * Construct a direction from two BigDecimals
	 *
	 * @param v0 the first BigDecimal, the horizontal angle
	 * @param v1 the second BigDecimal, the vertical angle
	 */
	public Dir3D(final BigDecimal v0, final BigDecimal v1)
	{
		super(2);
		xy(v0).z(v1);
	}

	/**
	 * Construct a direction from two doubles
	 *
	 * @param v0 the first double, the horizontal angle
	 * @param v1 the second double, the vertical angle
	 */
	public Dir3D(final double v0, final double v1)
	{
		super(2);
		xy(v0).z(v1);
	}

	/**
	 * Convert a double to a BigDecimal
	 *
	 * @param d the double
	 * @return the BigDecimal
	 */
	public BigDecimal BigDecimal(final double d)
	{
		return BigDecimal.valueOf(d);
	}

	/**
	 * Get the horizontal angle
	 * @return the horizontal angle
	 */
	public BigDecimal xy()
	{
		if(b.length>0)
			return b[0];
		return ZERO;
	}

	/**
	 * Get the vertical angle
	 *
	 * @return the vertical angle
	 */
	public BigDecimal z()
	{
		if(b.length>1)
			return b[1];
		return ZERO;
	}

	/**
	 * Get the horizontal angle as a double
	 *
	 * @return the horizontal angle
	 */
	public double xyd()
	{
		if(b.length>0)
			return b[0].doubleValue();
		return 0;
	}

	/**
	 * Get the vertical angle as a double
	 *
	 * @return the vertical angle
	 */
	public double zd()
	{
		if(b.length>1)
			return b[1].doubleValue();
		return 0;
	}

	/**
	 * Get the indexed angle as a double
	 *
	 * @param x the index, 0 for horizontal, 1 for vertical
	 * @return the angle at that index as a double
	 */
	public double getd(final int x)
	{
		if((x>=0)&&(x<b.length))
			return b[x].doubleValue();
		return 0;
	}

	/**
	 * Get the indexed angle as a BigDecimal
	 *
	 * @param x the index, 0 for horizontal, 1 for vertical
	 * @return the angle at that index as a BigDecimal
	 */
	public BigDecimal get(final int x)
	{
		if((x>=0)&&(x<b.length))
			return b[x];
		return null;
	}

	/**
	 * Set the horizontal angle
	 *
	 * @param d the horizontal angle
	 * @return this
	 */
	public Dir3D xy(BigDecimal d)
	{
		if(d!=null)
		{
			if(safe)
			{
				while(d.compareTo(BigCMath.PI_TIMES_2) >=0)
					d=d.subtract(BigCMath.PI_TIMES_2);
				while(d.compareTo(ZERO) <0)
					d=d.add(BigCMath.PI_TIMES_2);
			}
			b[0] = d;
		}
		return this;
	}

	/**
	 * Set the vertical angle
	 *
	 * @param d the vertical angle
	 * @return this
	 */
	public Dir3D z(BigDecimal d)
	{
		if(d!=null)
		{
			if(safe)
			{
				while(d.compareTo(BigCMath.PI_TIMES_2) >=0)
					d=d.subtract(BigCMath.PI_TIMES_2);
				while(d.compareTo(BigCMath.PI_TIMES_2.negate()) <0)
					d=d.add(BigCMath.PI_TIMES_2);
				while(d.compareTo(BigCMath.PI) > 0)
				{
					d = d.subtract(BigCMath.PI);
					b[0] = b[0].add((b[0].compareTo(BigCMath.PI) <= 0)?BigCMath.PI:BigCMath.PI.negate());
				}
				while(d.compareTo(ZERO) < 0)
				{
					d = d.abs();
					b[0] = b[0].add((b[0].compareTo(BigCMath.PI) <= 0)?BigCMath.PI:BigCMath.PI.negate());
				}
			}
			b[1] = d;
		}
		return this;
	}

	/**
	 * Make a copy of this direction
	 *
	 * @return the copy
	 */
	public Dir3D copyOf()
	{
		return new Dir3D(this);
	}

	/**
	 * Set the horizontal angle
	 *
	 * @param d the horizontal angle
	 * @return this
	 */
	public Dir3D xy(final double d)
	{
		return xy(new BigDecimal(d));
	}

	/**
	 * Set the vertical angle
	 *
	 * @param d the vertical angle
	 * @return this
	 */
	public Dir3D z(final double d)
	{
		return z(new BigDecimal(d));
	}

	/**
	 * Sets the indexed angle
	 * Use 0 for horizontal, 1 for vertical
	 *
	 * @param index the index
	 * @param d the angle
	 */
	public void set(final int index, final double d)
	{
		if(index == 0)
			xy(d);
		else
		if(index == 1)
			z(d);
	}

	/**
	 * Sets the indexed angle Use 0 for horizontal, 1 for vertical
	 *
	 * @param index the index
	 * @param d the angle
	 */
	public void set(final int index, final BigDecimal d)
	{
		if(index == 0)
			xy(d);
		else
		if(index == 1)
			z(d);
	}

	/**
	 * Add another direction to this one, producing a new direction
	 *
	 * @param v the other direction
	 * @return the new direction
	 */
	public Dir3D add(final Dir3D v)
	{
		return new Dir3D(super.add(v));
	}

	/**
	 * Subtract another direction from this one, producing a new direction
	 *
	 * @param v the other direction
	 * @return the new direction
	 */
	public Dir3D subtract(final Dir3D v)
	{
		return new Dir3D(super.subtract(v));
	}

	/**
	 * Create a 3d vector from this direction, given a magnitude
	 *
	 * @param magnitude the magnitude of the new vector
	 * @return the new vector as an array of doubles
	 */
	public double[] toArray3(final double magnitude)
	{
		final double[] vector = new double[3];
		final double xyRad = xyd();
		final double zRad = zd();
		vector[0] = magnitude * Math.cos(zRad) * Math.cos(xyRad);
		vector[1] = magnitude * Math.cos(zRad) * Math.sin(xyRad);
		vector[2] = magnitude * Math.sin(zRad);
		return vector;
	}

	/**
	 * Create a direction from a 3d vector
	 *
	 * @param vector the 3d vector
	 * @return the new direction
	 */
	public static Dir3D fromArray3(final double[] vector)
	{
		final double magnitude = Math.sqrt(vector[0]*vector[0] + vector[1]*vector[1] + vector[2]*vector[2]);
		if (magnitude == 0)
			return new Dir3D(0.0, 0.0);
		final double xyRad = Math.atan2(vector[1], vector[0]);
		final double zRad = Math.asin(vector[2] / magnitude);
		return new Dir3D(xyRad, zRad);
	}

	/**
	 * Negate this direction, producing a new direction
	 *
	 * @return the new direction
	 */
	public Dir3D negate()
	{
		return new Dir3D(BigCMath.PI.add(xy()),BigCMath.PI.subtract(z()));
	}

	/**
	 * Returns the hash code for this direction
	 */
	@Override
	public int hashCode()
	{
		return xy().hashCode() << 32 | z().hashCode();
	}

	/**
	 * Compares this direction to another object
	 *
	 * @param o the other object
	 * @return true if the other object is a Dir3D and has the same angles
	 */
	@Override
	public boolean equals(final Object o)
	{
		if(o instanceof Dir3D)
		{
			final Dir3D v = (Dir3D)o;
			if(!xy().equals(v.xy()))
				return false;
			if(!z().equals(v.z()))
				return false;
			return true;
		}
		return super.equals(o);
	}
}
