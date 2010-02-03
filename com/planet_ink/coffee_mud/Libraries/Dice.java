package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
public class Dice extends StdLibrary implements DiceLibrary
{
    public String ID(){return "Dice";}
    private Random randomizer = null;
    protected LinkedList<CMath.CompiledOperation>  baseNpcHitpointsFormula = null;

    public synchronized Random getRandomizer() {
    	if(randomizer == null)
    		randomizer = new Random(System.currentTimeMillis());
    	return randomizer;
    }
    public Dice()
    {
    	super();
    	baseNpcHitpointsFormula=CMath.compileMathExpression(CMProps.getVar(CMProps.SYSTEM_FORMULA_NPCHITPOINTS));
    	randomizer = new Random(System.currentTimeMillis());
    }
    
    public boolean activate()
    {
    	baseNpcHitpointsFormula = CMath.compileMathExpression(CMProps.getVar(CMProps.SYSTEM_FORMULA_NPCHITPOINTS));
    	return super.activate();
    }
    
    public void propertiesLoaded() { activate(); }
    
	public boolean normalizeAndRollLess(int score)
	{
		return (rollPercentage()<normalizeBy5(score));
	}
	public int normalizeBy5(int score)
	{
		if(score>95)
			return 95;
		else
		if(score<5)
			return 5;
		return score;
	}

	public int rollHP(int level, int code)
	{
		if(code<0) code=0;
		// new old style
		if(code<32768)
			return (int)Math.round(CMath.parseMathExpression(baseNpcHitpointsFormula, new double[]{level,code,0,0,0,0,0,0,0,0,0},0.0));

		// old old style
		//	return 10 +(int)Math.round(CMath.mul(level*level,0.85)) +(roll(level,code,0)*mul);
		
		// new style
		int r=code>>23;
		int d=(code-(r<<23))>>15;
		int p=(((code-(r<<23))-(d<<15)));
		return roll(r,d,p);
	}
	
	public int getHPCode(String str)
	{
		int i=str.indexOf("d");
		if(i<0) return 11;
		int roll=CMath.s_int(str.substring(0,i).trim());
		str=str.substring(i+1).trim();

		i=str.indexOf("+");
		int dice=0;
		int plus=0;
		if(i<0)
		{
			i=str.indexOf("-");
			if(i<0)
				dice=CMath.s_int(str.trim());
			else
			{
				dice=CMath.s_int(str.substring(0,i).trim());
				plus=CMath.s_int(str.substring(i));
			}
		}
		else
		{
			dice=CMath.s_int(str.substring(0,i).trim());
			plus=CMath.s_int(str.substring(i+1));
		}
		return getHPCode(roll,dice,plus);
	}

	public int getHPCode(int roll, int dice, int plus)
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

	public int[] getHPBreakup(int level, int code)
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
			stuff[2]=(int)Math.round(CMath.mul(level*level,0.85));
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
	
    public int roll(int number, int die, int modifier)
    {
    	if(die<=0) 
    		return modifier;
    	int total=0;
        for(int i=0;i<number;i++)
        	total+=randomizer.nextInt(die)+1;
        return total + modifier;
    }

    public int rollPercentage()
    {
        return (Math.abs(randomizer.nextInt() % 100)) + 1;
    }

}
