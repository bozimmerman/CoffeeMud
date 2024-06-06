package com.planet_ink.coffee_mud.core.collections;

import java.math.BigDecimal;
import java.math.RoundingMode;

/*
   Copyright 2022-2024 Bo Zimmerman

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
	public static final BigDecimal	ZERO	= BigDecimal.valueOf(0.0);
	public static final BigDecimal	TWO		= BigDecimal.valueOf(2L);
	public final static int			SCALE	= 25;

	protected final BigDecimal[] b;

	public BigVector(final int len)
	{
		b=new BigDecimal[len];
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

	public BigDecimal x()
	{
		if(b.length>0)
			return b[0];
		return null;
	}

	public BigDecimal y()
	{
		if(b.length>1)
			return b[1];
		return null;
	}

	public BigDecimal z()
	{
		if(b.length>2)
			return b[2];
		return null;
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

	public final static BigDecimal bigSqrt(final BigDecimal A)
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

	public BigDecimal magnitude()
	{
		BigDecimal a = ZERO;
		for(int i=0;i<b.length;i++)
			a=a.add(b[i].pow(2));
		return bigSqrt(a);
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
		return BigVector.bigSqrt(x0);
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

	public BigVector sphereToCartesian()
	{
		if(b.length != 2)
			throw new IllegalArgumentException("Different sphere dimensions");
		final BigVector d = new BigVector(3);
		d.b[0]=new BigDecimal(Math.sin(b[1].doubleValue())).multiply(new BigDecimal(Math.cos(b[0].doubleValue())));
		d.b[1]=new BigDecimal(Math.sin(b[1].doubleValue())).multiply(new BigDecimal(Math.sin(b[0].doubleValue())));
		d.b[2]=new BigDecimal(Math.cos(b[1].doubleValue()));
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
