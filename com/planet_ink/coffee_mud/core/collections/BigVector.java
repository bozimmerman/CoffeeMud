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
public class BigVector implements Comparable<BigVector>
{
	public static final BigDecimal	 ZERO	= BigDecimal.ZERO;
	public static final BigDecimal	 TWO	= BigDecimal.valueOf(2L);
	public final static int			 SCALE	= BigCMath.SCALE;
	public final static RoundingMode ROUND	= BigCMath.ROUND;
	public static final BigDecimal[] ZEROS	= new BigDecimal[] { ZERO, ZERO, ZERO };

	protected final BigDecimal[] b;

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

	public BigVector(final long[] l)
	{
		b=new BigDecimal[l.length];
		for(int i=0;i<l.length;i++)
			b[i]=BigDecimal.valueOf(l[i]);
	}

	public BigVector(final long lx, final long ly)
	{
		b=new BigDecimal[2];
		b[0]=BigDecimal.valueOf(lx);
		b[1]=BigDecimal.valueOf(ly);
	}

	public BigVector(final long lx, final long ly, final long lz)
	{
		b=new BigDecimal[3];
		b[0]=BigDecimal.valueOf(lx);
		b[1]=BigDecimal.valueOf(ly);
		b[2]=BigDecimal.valueOf(lz);
	}

	public BigVector(final double[] l)
	{
		b=new BigDecimal[l.length];
		for(int i=0;i<l.length;i++)
			b[i]=BigDecimal.valueOf(l[i]);
	}

	public BigVector(final BigVector v)
	{
		b=v.b.clone();
	}

	public BigVector(final BigDecimal... vs)
	{
		b=vs.clone();
	}

	public final int length()
	{
		return b.length;
	}

	public BigVector subtract(final BigVector v)
	{
		if(b.length != v.length())
			throw new IllegalArgumentException("Different dimensions");
		final BigVector ab=new BigVector(b.length);
		for(int i=0;i<b.length;i++)
			ab.b[i] = b[i].subtract(v.b[i]);
		return ab;
	}

	public void subtractFrom(final BigVector v)
	{
		if(b.length != v.length())
			throw new IllegalArgumentException("Different dimensions");
		for(int i=0;i<b.length;i++)
			b[i] = b[i].subtract(v.b[i]);
	}

	public BigVector add(final BigVector v)
	{
		if(b.length != v.length())
			throw new IllegalArgumentException("Different dimensions");
		final BigVector ab=new BigVector(b.length);
		for(int i=0;i<b.length;i++)
			ab.b[i] = b[i].add(v.b[i]);
		return ab;
	}

	public void addFrom(final BigVector v)
	{
		if(b.length != v.length())
			throw new IllegalArgumentException("Different dimensions");
		for(int i=0;i<b.length;i++)
			b[i] = b[i].add(v.b[i]);
	}

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

	@Override
	public int hashCode()
	{
		return Arrays.hashCode(b);
	}

	public BigDecimal magnitude()
	{
		BigDecimal a = ZERO;
		for(int i=0;i<b.length;i++)
			a=a.add(b[i].pow(2));
		return sqrt(a);
	}

	public BigVector unitVector()
	{
		final BigVector ab=new BigVector(b.length);
		final BigDecimal mag = magnitude();
		for(int i=0;i<b.length;i++)
			ab.b[i] = b[i].divide(mag, SCALE, RoundingMode.UP);
		return ab;
	}

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

	public void unitVectorFrom()
	{
		final BigDecimal mag = magnitude();
		for(int i=0;i<b.length;i++)
			b[i] = b[i].divide(mag, SCALE, RoundingMode.UP);
	}

	public BigVector scalarProduct(final BigDecimal num)
	{
		final BigVector ab=new BigVector(b.length);
		for(int i=0;i<b.length;i++)
			ab.b[i] = b[i].multiply(num);
		return ab;
	}

	public void scalarProductFrom(final BigDecimal num)
	{
		for(int i=0;i<b.length;i++)
			b[i] = b[i].multiply(num);
	}

	public BigDecimal dotProduct(final BigVector v)
	{
		if(b.length != v.length())
			throw new IllegalArgumentException("Different dimensions");
		BigDecimal d=ZERO;
		for(int i=0;i<b.length;i++)
			d=d.add(b[i].multiply(v.b[i]));
		return d;

	}

	public static BigDecimal cos(final BigDecimal d)
	{
		return BigDecimal.valueOf(Math.cos(d.doubleValue()));
	}

	public static BigDecimal acos(final BigDecimal d)
	{
		return BigDecimal.valueOf(Math.acos(d.doubleValue()));
	}

	public static BigDecimal sin(final BigDecimal d)
	{
		return BigDecimal.valueOf(Math.sin(d.doubleValue()));
	}

	public static BigDecimal tan(final BigDecimal d)
	{
		return BigDecimal.valueOf(Math.tan(d.doubleValue()));
	}

	public static BigDecimal atan(final BigDecimal d)
	{
		return BigDecimal.valueOf(Math.atan(d.doubleValue()));
	}

	public static BigDecimal cos(final double d)
	{
		return BigDecimal.valueOf(Math.cos(d));
	}

	public static BigDecimal acos(final double d)
	{
		return BigDecimal.valueOf(Math.acos(d));
	}

	public static BigDecimal sin(final double d)
	{
		return BigDecimal.valueOf(Math.sin(d));
	}

	public static BigDecimal tan(final double d)
	{
		return BigDecimal.valueOf(Math.tan(d));
	}

	public static BigDecimal atan(final double d)
	{
		return BigDecimal.valueOf(Math.atan(d));
	}

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

	public BigDecimal dotRemainder(final BigVector v)
	{
		if(b.length != v.length())
			throw new IllegalArgumentException("Different dimensions");
		BigDecimal d=ZERO;
		for(int i=0;i<b.length;i++)
			d=d.subtract(b[i].multiply(v.b[i]));
		return d;

	}

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

	public void vectorProductFrom(final BigVector v)
	{
		if((b.length != 3)||(v.length()!=3))
			throw new IllegalArgumentException("Only 3 dims implemented");
		b[0] = b[1].multiply(v.b[2]).subtract(b[2].multiply(v.b[1]));
		b[1] = b[2].multiply(v.b[0]).subtract(b[0].multiply(v.b[2]));
		b[2] = b[0].multiply(v.b[1]).subtract(b[1].multiply(v.b[0]));
	}

	public double[] toDoubles()
	{
		final double[] res=new double[b.length];
		for(int i=0;i<res.length;i++)
			res[i]=b[i].doubleValue();
		return res;
	}

	public long[] toLongs()
	{
		final long[] res=new long[b.length];
		for(int i=0;i<res.length;i++)
			res[i]=Math.round(b[i].doubleValue());
		return res;
	}

	@Override
	public String toString()
	{
		final StringBuilder val = new StringBuilder("");
		for(int i=0;i<b.length;i++)
			val.append(b[i]).append(",");
		return val.toString();

	}

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
