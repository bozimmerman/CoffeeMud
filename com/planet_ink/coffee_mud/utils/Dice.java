package com.planet_ink.coffee_mud.utils;

import java.util.*;

public class Dice
{
    private static Random randomizer = null;

    public static void seed()
    {
        randomizer = new Random(System.currentTimeMillis());
    }
	
	public static int rollHP(int level, int code)
	{
		int mul=1;
		if(code<0)
		{
			code=code*-1;
			mul=-1;
		}
		// old style
		if(code<32768) return 10+(level*level)+(Dice.roll(level,code,0)*mul);
		// new style
		int r=code>>23;
		int d=(code-(r<<23))>>15;
		int p=(((code-(r<<23))-(d<<15)))*mul;
		return Dice.roll(r,d,p);
	}
	
	public static int getHPCode(String str)
	{
		int i=str.indexOf("d");
		if(i<0) return 11;
		int roll=Util.s_int(str.substring(0,i).trim());
		str=str.substring(i+1).trim();

		i=str.indexOf("+");
		int dice=0;
		int plus=0;
		if(i<0)
		{
			i=str.indexOf("-");
			if(i<0)
				dice=Util.s_int(str.trim());
			else
			{
				dice=Util.s_int(str.substring(0,i).trim());
				plus=Util.s_int(str.substring(i));
			}
		}
		else
		{
			dice=Util.s_int(str.substring(0,i).trim());
			plus=Util.s_int(str.substring(i+1));
		}
		return getHPCode(roll,dice,plus);
	}

	public static int getHPCode(int roll, int dice, int plus)
	{
		if(roll<=0) roll=1;
		if(dice<=0) dice=0;
		
		if(roll>255)
		{
			int diff=roll-255;
			roll=255;
			plus+=(diff*dice)/2;
		}
		if(dice>255)
		{
			int diff=dice-255;
			dice=255;
			plus+=(diff*roll)/2;
		}
		int mul=1;
		if(plus<0)
		{
			plus=plus*-1;
			mul=-1;
		}
		if(plus>32768) plus=32768;
		return 	(plus+(dice<<15)+(roll<<(23)))*mul;
	}

	public static int[] getHPBreakup(int level, int code)
	{
		int mul=1;
		if(code<0)
		{
			code=code*-1;
			mul=-1;
		}
		int stuff[]=new int[3];
		// old style
		if(code<32768)
		{
			stuff[0]=level;
			stuff[1]=(code*mul);
			stuff[2]=(level*level);
		}
		else
		{
			// new style
			int r=code>>23;
			int d=(code-(r<<23))>>15;
			int p=(((code-(r<<23))-(d<<15)))*mul;
			stuff[0]=r;
			stuff[1]=d;
			stuff[2]=p;
		}
		return stuff;
	}
	
    public static int roll(int number, int die, int modifier)
    {
        if (randomizer == null)
            seed();

        if (die == 0)
            die = 6;

        int total = 0;
        for (int i = 0; i < number; i++)
        {
            total += (Math.abs(randomizer.nextInt() % die)) + 1;
        }
        total += modifier;
        return total;
    }

    public static int rollPercentage()
    {
        if (randomizer == null)
            seed();
        return (Math.abs(randomizer.nextInt() % 100)) + 1;
    }

}