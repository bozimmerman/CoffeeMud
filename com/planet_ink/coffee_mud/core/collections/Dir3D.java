package com.planet_ink.coffee_mud.core.collections;

import java.math.BigDecimal;

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
public class Dir3D extends BigVector
{
	private static final BigDecimal		PI_TIMES_2				= BigDecimal.valueOf(Math.PI * 2.0);
	private static final BigDecimal		PIP						= BigDecimal.valueOf(Math.PI+0.000000000000001);
	private static final BigDecimal		PIP_TIMES_2				= PIP.multiply(BigDecimal.valueOf(2.0));
	private static final BigDecimal		PI						= BigDecimal.valueOf(Math.PI);
	private static final BigDecimal		NEGPI					= BigDecimal.valueOf(-Math.PI);

	public Dir3D()
	{
		super(2);
	}

	public Dir3D(final BigVector v)
	{
		super(2);
		if(v.length()!=2)
			throw new IllegalArgumentException();
		b[0]=v.b[0];
		b[1]=v.b[1];
	}

	public Dir3D(final double[] v)
	{
		super(2);
		if(v.length!=2)
			throw new IllegalArgumentException();
		xy(v[0]).z(BigDecimal.valueOf(v[1]));
	}

	public Dir3D(final BigDecimal[] v)
	{
		super(2);
		if(v.length!=2)
			throw new IllegalArgumentException();
		xy(v[0]).z(v[1]);
	}

	public Dir3D(final BigDecimal v0, final BigDecimal v1)
	{
		super(2);
		xy(v0).z(v1);
	}

	public Dir3D(final double v0, final double v1)
	{
		super(2);
		xy(v0).z(v1);
	}

	public BigDecimal BigDecimal(final double d)
	{
		return BigDecimal.valueOf(d);
	}

	public BigDecimal xy()
	{
		if(b.length>0)
			return b[0];
		return ZERO;
	}

	public BigDecimal z()
	{
		if(b.length>1)
			return b[1];
		return ZERO;
	}

	public double xyd()
	{
		if(b.length>0)
			return b[0].doubleValue();
		return 0;
	}

	public double zd()
	{
		if(b.length>1)
			return b[1].doubleValue();
		return 0;
	}

	public double getd(final int x)
	{
		if((x>=0)&&(x<b.length))
			return b[x].doubleValue();
		return 0;
	}

	public BigDecimal get(final int x)
	{
		if((x>=0)&&(x<b.length))
			return b[x];
		return null;
	}

	public Dir3D xy(BigDecimal d)
	{
		if(d!=null)
		{
			while(d.compareTo(PIP_TIMES_2) >=0)
				d=d.subtract(PI_TIMES_2);
			while(d.compareTo(ZERO) <0)
				d=d.add(PI_TIMES_2);
			b[0] = d;
		}
		return this;
	}

	public Dir3D z(BigDecimal d)
	{
		if(d!=null)
		{
			while(d.compareTo(PIP_TIMES_2) >=0)
				d=d.subtract(PI_TIMES_2);
			while(d.compareTo(PIP_TIMES_2.negate()) <0)
				d=d.add(PI_TIMES_2);
			while(d.compareTo(PIP) > 0)
			{
				d = d.subtract(PI);
				b[0] = b[0].add((b[0].compareTo(PI) <= 0)?PI:NEGPI);
			}
			while(d.compareTo(ZERO) < 0)
			{
				d = d.abs();
				b[0] = b[0].add((b[0].compareTo(PI) <= 0)?PI:NEGPI);
			}
			b[1] = d;
		}
		return this;
	}

	public Dir3D copyOf()
	{
		return new Dir3D(this);
	}

	public Dir3D xy(final double d)
	{
		return xy(new BigDecimal(d));
	}

	public Dir3D z(final double d)
	{
		return z(new BigDecimal(d));
	}

	public void set(final int index, final double d)
	{
		if(index == 0)
			xy(d);
		else
		if(index == 1)
			z(d);
	}

	public void set(final int index, final BigDecimal d)
	{
		if(index == 0)
			xy(d);
		else
		if(index == 1)
			z(d);
	}

	public Dir3D add(final Dir3D v)
	{
		return new Dir3D(super.add(v));
	}

	public Dir3D subtract(final Dir3D v)
	{
		return new Dir3D(super.subtract(v));
	}

	@Override
	public boolean equals(final Object o)
	{
		if(o instanceof Dir3D)
		{
			final Dir3D v = (Dir3D)o;
			if(v.length()!=b.length)
				return false;
			for(int i=0;i<b.length;i++)
				if(b[i].doubleValue() != v.b[i].doubleValue())
					return false;
			return true;
		}
		return super.equals(o);
	}
}
