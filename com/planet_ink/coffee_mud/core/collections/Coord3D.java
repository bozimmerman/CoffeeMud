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
public class Coord3D extends BigVector
{
	public Coord3D()
	{
		super(3);
	}

	public Coord3D(final BigVector v)
	{
		super(3);
		if(v.length()!=3)
			throw new IllegalArgumentException();
		b[0]=v.b[0];
		b[1]=v.b[1];
		b[2]=v.b[2];
	}
	
	public Coord3D(final long[] v)
	{
		super(3);
		if(v.length!=3)
			throw new IllegalArgumentException();
		b[0]=BigDecimal.valueOf(v[0]);
		b[1]=BigDecimal.valueOf(v[1]);
		b[2]=BigDecimal.valueOf(v[2]);
	}
	
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
	
	public long xl()
	{
		if(b.length>0)
			return b[0].longValue();
		return 0;
	}

	public long yl()
	{
		if(b.length>1)
			return b[1].longValue();
		return 0;
	}

	public long zl()
	{
		if(b.length>2)
			return b[2].longValue();
		return 0;
	}
	
	public long getl(final int x)
	{
		if((x>=0)&&(x<b.length))
			return b[x].longValue();
		return 0;
	}

	public BigDecimal get(final int x)
	{
		if((x>=0)&&(x<b.length))
			return b[x];
		return null;
	}

	public Coord3D x(final BigDecimal d)
	{
		if(d!=null)
			b[0] = d;
		return this;
	}

	public Coord3D y(final BigDecimal d)
	{
		if(d!=null)
			b[1] = d;
		return this;
	}

	public Coord3D z(final BigDecimal d)
	{
		if(d!=null)
			b[2] = d;
		return this;
	}

	public Coord3D copyOf()
	{
		return new Coord3D(this);
	}
	
	public Coord3D x(final long d)
	{
		return x(new BigDecimal(d));
	}

	public Coord3D y(final long d)
	{
		return y(new BigDecimal(d));
	}

	public Coord3D z(final long d)
	{
		return z(new BigDecimal(d));
	}
	
	public void set(final int index, final long d)
	{
		if((index>=0)&&(index<b.length))
			b[index]=new BigDecimal(d);
	}

	public void set(final int index, final BigDecimal d)
	{
		if((index>=0)&&(index<b.length)&&(d!=null))
			b[index]=d;
	}

	public Coord3D add(final Coord3D v)
	{
		return new Coord3D(super.add(v));
	}
	
	public Coord3D subtract(final Coord3D v)
	{
		return new Coord3D(super.subtract(v));
	}
	
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
