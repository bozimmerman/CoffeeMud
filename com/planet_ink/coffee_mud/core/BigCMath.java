package com.planet_ink.coffee_mud.core;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;
import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

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
/**
 * A core singleton class handling various BigDecimal mathematical operations and
 * functions
 */
public class BigCMath
{
	public static final BigDecimal		ZERO			= BigDecimal.ZERO;
	public static final BigDecimal		ONE				= BigDecimal.valueOf(1L);
	public static final BigDecimal		TWO				= BigDecimal.valueOf(2L);
	public static final BigDecimal		THREE			= BigDecimal.valueOf(3L);
	public static final BigDecimal		FOUR			= BigDecimal.valueOf(4L);
	public static final BigDecimal		FIVE			= BigDecimal.valueOf(5L);
	public static final BigDecimal		TEN				= BigDecimal.valueOf(10L);
	public static final BigDecimal		ONE_THOUSAND	= BigDecimal.valueOf(1000);
	public static final BigDecimal		ZERO_ALMOST		= BigDecimal.valueOf(0.000001);
	public static final BigDecimal		POINT01			= BigDecimal.valueOf(0.01);
	public static final BigDecimal		POINT1			= BigDecimal.valueOf(0.1);
	public static final BigDecimal		ONEPOINT01		= BigDecimal.valueOf(1.01);
	public static final BigDecimal		ALMOST_ZERO		= ZERO_ALMOST;
	public static final BigDecimal		MIN_ONE			= BigDecimal.valueOf(-1L);
	
	public static final BigDecimal		PI;
	public static final BigDecimal		PI_TIMES_2;
	public static final BigDecimal		PI_BY_2;
	public static final BigDecimal		PIPLUS;
	public static final BigDecimal		PIPLUS_TIMES_2;

	public final static int			 SCALE	= 34;
	public final static RoundingMode ROUND	= RoundingMode.HALF_UP;
	
	static 
	{
		PI = new BigDecimal("3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986280348253421170679");
		PI.setScale(SCALE, ROUND);
		PI_TIMES_2 = PI.multiply(TWO);
		PI_BY_2 = PI.divide(TWO, SCALE, ROUND);
		PIPLUS = BigCMath.PI.add(BigDecimal.valueOf(0.000000000000001));
		PIPLUS_TIMES_2 = PIPLUS.multiply(BigCMath.TWO);
	}
	
	public static BigDecimal atan(BigDecimal invX)
	{
		return atan(invX, SCALE);
	}

	public static BigDecimal atan(BigDecimal invX, int scale)
	{
		final BigDecimal invXsq = invX.multiply(invX);
		BigDecimal num = BigDecimal.ONE.divide(invX, SCALE, ROUND);
		BigDecimal result = num;
		BigDecimal term;
		int i=1;
		do
		{
			num=num.divide(invXsq, SCALE, ROUND);
			BigDecimal denom=TWO.multiply(BigDecimal.valueOf(i).add(BigDecimal.ONE));
			term = num.divide(denom, SCALE, ROUND);
			result = ((i % 2) == 0) ? result.add(term) : result.subtract(term);
			i++;
		} while(term.compareTo(BigDecimal.ZERO) != 0);
		return result;
	}
	
	public static BigDecimal sqrt(final BigDecimal A)
	{
		if(A.doubleValue()<0)
			return BigCMath.ZERO;
		final int SCALE=BigCMath.SCALE*2;
		BigDecimal x0 = BigDecimal.valueOf(0);
		BigDecimal x1 = BigDecimal.valueOf(Math.sqrt(A.doubleValue()));
		int times=0;
		while ((!x0.equals(x1))&&(++times<20))
		{
			x0 = x1;
			x1 = A.divide(x0, SCALE, RoundingMode.UP);
			x1 = x1.add(x0);
			x1 = x1.divide(BigCMath.TWO, SCALE, RoundingMode.UP);
		}
		x1.setScale(BigCMath.SCALE, ROUND);
		return x1;
	}

}
