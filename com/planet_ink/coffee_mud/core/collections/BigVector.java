package com.planet_ink.coffee_mud.core.collections;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

import com.planet_ink.coffee_mud.core.BigCMath;

/*
   Copyright 2022-2025 Bo Zimmerman

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
 * A class for representing and manipulating vectors of arbitrary
 * dimensionality, using BigDecimal math.
 *
 * @author Bo Zimmerman
 */
public class BigVector implements Comparable<BigVector>
{
	public static final BigDecimal	 ZERO	= BigDecimal.ZERO;
	public static final BigDecimal	 TWO	= BigDecimal.valueOf(2L);
	public final static int			 SCALE	= BigCMath.SCALE;
	public final static RoundingMode ROUND	= BigCMath.ROUND;
	public static final BigDecimal[] ZEROS	= new BigDecimal[] { ZERO, ZERO, ZERO };

	protected final BigDecimal[] b;

	/**
	 * Constructs a BigVector of the given length, initialized to zero.
	 *
	 * @param len the length (number of dimensions) of the vector
	 */
	public BigVector(final int len)
	{
		if(len == 3)
			b=ZEROS.clone();
		else
		{
			b=new BigDecimal[len];
			for(int i=0;i<len;i++)
				b[i]=ZERO;
		}
	}

	/**
	 * Constructs a BigVector from the given array of long values.
	 *
	 * @param l the array of long values representing the vector's components
	 */
	public BigVector(final long[] l)
	{
		b=new BigDecimal[l.length];
		for(int i=0;i<l.length;i++)
			b[i]=BigDecimal.valueOf(l[i]);
	}

	/**
	 * Constructs a 2D BigVector from the given long values.
	 *
	 * @param lx the x component of the vector
	 * @param ly the y component of the vector
	 */
	public BigVector(final long lx, final long ly)
	{
		b=new BigDecimal[2];
		b[0]=BigDecimal.valueOf(lx);
		b[1]=BigDecimal.valueOf(ly);
	}

	/**
	 * Constructs a 3D BigVector from the given long values.
	 *
	 * @param lx the x component of the vector
	 * @param ly the y component of the vector
	 * @param lz the z component of the vector
	 */
	public BigVector(final long lx, final long ly, final long lz)
	{
		b=new BigDecimal[3];
		b[0]=BigDecimal.valueOf(lx);
		b[1]=BigDecimal.valueOf(ly);
		b[2]=BigDecimal.valueOf(lz);
	}

	/**
	 * Constructs a BigVector from the given array of double values.
	 *
	 * @param l the array of double values representing the vector's components
	 */
	public BigVector(final double[] l)
	{
		b=new BigDecimal[l.length];
		for(int i=0;i<l.length;i++)
			b[i]=BigDecimal.valueOf(l[i]);
	}

	/**
	 * Copy constructor.
	 *
	 * @param v the BigVector to copy
	 */
	public BigVector(final BigVector v)
	{
		b=v.b.clone();
	}

	/**
	 * Constructs a BigVector from the given array of BigDecimal values.
	 *
	 * @param vs the array of BigDecimal values representing the vector's
	 *            components
	 */
	public BigVector(final BigDecimal... vs)
	{
		b=vs.clone();
	}

	/**
	 * Returns the length (number of dimensions) of this vector.
	 * @return the length (number of dimensions) of this vector
	 */
	public final int length()
	{
		return b.length;
	}

	/**
	 * Returns a new BigVector that is the difference of this vector and the
	 * given vector.
	 *
	 * @param v the vector to subtract
	 * @return the difference of this vector and the given vector
	 * @throws IllegalArgumentException if the vectors are of different
	 *             dimensions
	 */
	public BigVector subtract(final BigVector v)
	{
		if(b.length != v.length())
			throw new IllegalArgumentException("Different dimensions");
		final BigVector ab=new BigVector(b.length);
		for(int i=0;i<b.length;i++)
			ab.b[i] = b[i].subtract(v.b[i]);
		return ab;
	}

	/**
	 * Subtracts the given vector from this vector, modifying this vector.
	 *
	 * @param v the vector to subtract
	 * @throws IllegalArgumentException if the vectors are of different
	 *             dimensions
	 */
	public void subtractFrom(final BigVector v)
	{
		if(b.length != v.length())
			throw new IllegalArgumentException("Different dimensions");
		for(int i=0;i<b.length;i++)
			b[i] = b[i].subtract(v.b[i]);
	}

	/**
	 * Returns a new BigVector that is the sum of this vector and the given
	 * vector.
	 *
	 * @param v the vector to add
	 * @return the sum of this vector and the given vector
	 * @throws IllegalArgumentException if the vectors are of different
	 *             dimensions
	 */
	public BigVector add(final BigVector v)
	{
		if(b.length != v.length())
			throw new IllegalArgumentException("Different dimensions");
		final BigVector ab=new BigVector(b.length);
		for(int i=0;i<b.length;i++)
			ab.b[i] = b[i].add(v.b[i]);
		return ab;
	}

	/**
	 * Adds the given vector to this vector, modifying this vector.
	 *
	 * @param v the vector to add
	 * @throws IllegalArgumentException if the vectors are of different
	 *             dimensions
	 */
	public void addFrom(final BigVector v)
	{
		if(b.length != v.length())
			throw new IllegalArgumentException("Different dimensions");
		for(int i=0;i<b.length;i++)
			b[i] = b[i].add(v.b[i]);
	}

	/**
	 * Returns the square root of a BigDecimal value using the Newton-Raphson
	 * method.
	 *
	 * @param A the value to compute the square root of
	 * @return the square root of the given BigDecimal value
	 */
	public final static BigDecimal sqrt(final BigDecimal A)
	{
		if(A.doubleValue()<0)
			return ZERO;
		BigDecimal x0 = ZERO;
		BigDecimal x1 = BigDecimal.valueOf(Math.sqrt(A.doubleValue()));
		int times=0;
		while ((!x0.equals(x1))&&(!x0.equals(ZERO))&&(++times<20))
		{
			x0 = x1;
			x1 = A.divide(x0, SCALE, RoundingMode.UP);
			x1 = x1.add(x0);
			x1 = x1.divide(TWO, SCALE, RoundingMode.UP);
		}
		return x1;
	}

	/**
	 * Compares this BigVector to another object. The result is true if and only
	 * if the argument is not null and is a BigVector object that has the same
	 * dimensions and the same components as this vector.
	 *
	 * @param o the object to compare to
	 * @return true if the objects are equal, false otherwise
	 */
	@Override
	public boolean equals(final Object o)
	{
		if(o instanceof BigVector)
		{
			final BigVector v = (BigVector)o;
			if(v.length()!=b.length)
				return false;
			for(int i=0;i<b.length;i++)
				if(!b[i].equals(v.b[i]))
					return false;
			return true;
		}
		return o==this;
	}

	/**
	 * Returns a hash code for this BigVector. The hash code is computed by
	 * calling Arrays.hashCode on the internal array of BigDecimal components.
	 *
	 * @return a hash code for this BigVector
	 */
	@Override
	public int hashCode()
	{
		return Arrays.hashCode(b);
	}

	/**
	 * Returns the magnitude (length) of this vector.
	 *
	 * @return the magnitude of this vector
	 */
	public BigDecimal magnitude()
	{
		BigDecimal a = ZERO;
		for(int i=0;i<b.length;i++)
			a=a.add(b[i].pow(2));
		return sqrt(a);
	}

	/**
	 * Returns a new BigVector that is the unit vector of this vector.
	 *
	 * @return the unit vector of this vector
	 */
	public BigVector unitVector()
	{
		final BigVector ab=new BigVector(b.length);
		final BigDecimal mag = magnitude();
		for(int i=0;i<b.length;i++)
			ab.b[i] = b[i].divide(mag, SCALE, RoundingMode.UP);
		return ab;
	}

	/**
	 * Returns the distance between this vector and the given vector.
	 *
	 * @param v the other vector
	 * @return the distance between this vector and the given vector
	 * @throws IllegalArgumentException if the vectors are of different
	 *             dimensions
	 */
	public BigDecimal unitDistanceFrom(final BigVector v)
	{
		if(b.length != v.length())
			throw new IllegalArgumentException("Different dimensions");
		BigDecimal x0 = ZERO;
		for(int i=0;i<b.length;i++)
		{
			final BigDecimal p=v.b[i].subtract(b[i]);
			x0=x0.add(p.multiply(p));
		}
		return BigVector.sqrt(x0);
	}

	/**
	 * Sets this vector to be the unit vector of itself.
	 */
	public void unitVectorFrom()
	{
		final BigDecimal mag = magnitude();
		for(int i=0;i<b.length;i++)
			b[i] = b[i].divide(mag, SCALE, RoundingMode.UP);
	}

	/**
	 * Returns a new BigVector that is the scalar product of this vector and the
	 * given number.
	 *
	 * @param num the number to multiply by
	 * @return the scalar product of this vector and the given number
	 */
	public BigVector scalarProduct(final BigDecimal num)
	{
		final BigVector ab=new BigVector(b.length);
		for(int i=0;i<b.length;i++)
			ab.b[i] = b[i].multiply(num);
		return ab;
	}

	/**
	 * Sets this vector to be the scalar product of itself and the given number.
	 *
	 * @param num the number to multiply by
	 */
	public void scalarProductFrom(final BigDecimal num)
	{
		for(int i=0;i<b.length;i++)
			b[i] = b[i].multiply(num);
	}

	/**
	 * Returns the dot product of this vector and the given vector.
	 *
	 * @param v the other vector
	 * @return the dot product of this vector and the given vector
	 * @throws IllegalArgumentException if the vectors are of different
	 * 	dimensions
	 */
	public BigDecimal dotProduct(final BigVector v)
	{
		if(b.length != v.length())
			throw new IllegalArgumentException("Different dimensions");
		BigDecimal d=ZERO;
		for(int i=0;i<b.length;i++)
			d=d.add(b[i].multiply(v.b[i]));
		return d;

	}

	/**
	 * Returns the cosine of a BigDecimal value.
	 *
	 * @param d the value
	 * @return the cosine of a BigDecimal value.
	 */
	public static BigDecimal cos(final BigDecimal d)
	{
		return BigDecimal.valueOf(Math.cos(d.doubleValue()));
	}

	/**
	 * Returns the arc-cosine of a BigDecimal value.
	 *
	 * @param d the value
	 * @return the arc-cosine of a BigDecimal value.
	 */
	public static BigDecimal acos(final BigDecimal d)
	{
		return BigDecimal.valueOf(Math.acos(d.doubleValue()));
	}

	/**
	 * Returns the sine of a BigDecimal value.
	 *
	 * @param d the value
	 * @return the sine of a BigDecimal value.
	 */
	public static BigDecimal sin(final BigDecimal d)
	{
		return BigDecimal.valueOf(Math.sin(d.doubleValue()));
	}

	/**
	 * Returns the tangent of a BigDecimal value.
	 *
	 * @param d the value
	 * @return the tangent of a BigDecimal value.
	 */
	public static BigDecimal tan(final BigDecimal d)
	{
		return BigDecimal.valueOf(Math.tan(d.doubleValue()));
	}

	/**
	 * Returns the arc-tangent of a BigDecimal value.
	 *
	 * @param d the value
	 * @return the arc-tangent of a BigDecimal value.
	 */
	public static BigDecimal atan(final BigDecimal d)
	{
		return BigDecimal.valueOf(Math.atan(d.doubleValue()));
	}

	/**
	 * Returns the cosine of a double value.
	 *
	 * @param d the value
	 * @return the cosine of a double value.
	 */
	public static BigDecimal cos(final double d)
	{
		return BigDecimal.valueOf(Math.cos(d));
	}

	/**
	 * Returns the arc-cosine of a double value.
	 *
	 * @param d the value
	 * @return the arc-cosine of a double value.
	 */
	public static BigDecimal acos(final double d)
	{
		return BigDecimal.valueOf(Math.acos(d));
	}

	/**
	 * Returns the sine of a double value.
	 *
	 * @param d the value
	 * @return the sine of a double value.
	 */
	public static BigDecimal sin(final double d)
	{
		return BigDecimal.valueOf(Math.sin(d));
	}

	/**
	 * Returns the tangent of a double value.
	 *
	 * @param d the value
	 * @return the tangent of a double value.
	 */
	public static BigDecimal tan(final double d)
	{
		return BigDecimal.valueOf(Math.tan(d));
	}

	/**
	 * Returns the arc-tangent of a double value.
	 *
	 * @param d the value
	 * @return the arc-tangent of a double value.
	 */
	public static BigDecimal atan(final double d)
	{
		return BigDecimal.valueOf(Math.atan(d));
	}

	/**
	 * Converts a 3D Cartesian coordinate BigVector to a 2D spherical coordinate
	 * BigVector. The first element is the azimuthal angle in radians, the
	 * second element is the polar angle in radians.
	 *
	 * @return the spherical coordinate BigVector
	 */
	public BigVector sphereToCartesian()
	{
		if(b.length != 2)
			throw new IllegalArgumentException("Different sphere dimensions");
		final BigVector d = new BigVector(3);
		d.b[0]=sin(b[1]).multiply(cos(b[0]));
		d.b[1]=sin(b[1]).multiply(sin(b[0]));
		d.b[2]=cos(b[1]);
		return d;
	}

	/**
	 * Returns the negative of the dot product of this vector and the given
	 * vector.
	 *
	 * @param v the other vector
	 * @return the negative of the dot product of this vector and the given
	 *         vector
	 */
	public BigDecimal dotRemainder(final BigVector v)
	{
		if(b.length != v.length())
			throw new IllegalArgumentException("Different dimensions");
		BigDecimal d=ZERO;
		for(int i=0;i<b.length;i++)
			d=d.subtract(b[i].multiply(v.b[i]));
		return d;

	}

	/**
	 * Returns a new BigVector that is the vector product of this vector and the
	 * given vector. Only implemented for 3-dimensional vectors.
	 *
	 * @param v the other vector
	 * @return the vector product of this vector and the given vector
	 */
	public BigVector vectorProduct(final BigVector v)
	{
		if((b.length != 3)||(v.length()!=3))
			throw new IllegalArgumentException("Only 3 dims implemented");
		final BigVector ab=new BigVector(b.length);
		ab.b[0] = b[1].multiply(v.b[2]).subtract(b[2].multiply(v.b[1]));
		ab.b[1] = b[2].multiply(v.b[0]).subtract(b[0].multiply(v.b[2]));
		ab.b[2] = b[0].multiply(v.b[1]).subtract(b[1].multiply(v.b[0]));
		return ab;
	}

	/**
	 * Sets this vector to be the vector product of itself and the given vector.
	 *
	 * @param v the other vector
	 */
	public void vectorProductFrom(final BigVector v)
	{
		if((b.length != 3)||(v.length()!=3))
			throw new IllegalArgumentException("Only 3 dims implemented");
		b[0] = b[1].multiply(v.b[2]).subtract(b[2].multiply(v.b[1]));
		b[1] = b[2].multiply(v.b[0]).subtract(b[0].multiply(v.b[2]));
		b[2] = b[0].multiply(v.b[1]).subtract(b[1].multiply(v.b[0]));
	}

	/**
	 * Converts this BigVector to an array of doubles by converting each
	 * component to a double value.
	 *
	 * @return an array of doubles representing the components of this BigVector
	 */
	public double[] toDoubles()
	{
		final double[] res=new double[b.length];
		for(int i=0;i<res.length;i++)
			res[i]=b[i].doubleValue();
		return res;
	}

	/**
	 * Converts this BigVector to an array of longs by rounding each component
	 * to the nearest long value.
	 *
	 * @return an array of longs representing the components of this BigVector
	 */
	public long[] toLongs()
	{
		final long[] res=new long[b.length];
		for(int i=0;i<res.length;i++)
			res[i]=Math.round(b[i].doubleValue());
		return res;
	}

	/**
	 * Returns a string representation of this BigVector. The string
	 * representation consists of a list of the vector's components in order,
	 * enclosed in square brackets ("[]"). Adjacent components are separated by
	 * the characters ", ".
	 *
	 * @return a string representation of this BigVector
	 */
	@Override
	public String toString()
	{
		final StringBuilder val = new StringBuilder("");
		for(int i=0;i<b.length;i++)
			val.append(b[i]).append(",");
		return val.toString();

	}

	/**
	 * Compares this BigVector to another BigVector. The comparison is done
	 * lexicographically, starting from the first dimension. If the dimensions
	 * are different, the shorter vector is considered "less than" the longer
	 * one.
	 *
	 * @param o the BigVector to compare to
	 * @return a negative integer, zero, or a positive integer as this BigVector
	 *         is less than, equal to, or greater than the specified BigVector
	 */
	@Override
	public int compareTo(final BigVector o)
	{
		if(o == null)
			return 1;
		if(b.length < o.b.length)
			return -1;
		if(b.length > o.b.length)
			return 1;
		for(int i=0;i<b.length;i++)
		{
			final int c = b[i].compareTo(o.b[i]);
			if(c != 0)
				return c;
		}
		return 0;
	}
}
