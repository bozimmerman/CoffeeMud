package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Undead extends StdRace
{
	public Undead()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name=myID;
	}
	public boolean playerSelectable(){return false;}

	public void newCharacter(MOB mob)
	{
		super.newCharacter(mob);
	}
	public void setWeight(MOB mob)
	{	super.setWeight(mob);}

	public void affect(MOB myChar, Affect affect)
	{
		if(affect.amITarget(myChar)&&(affect.targetMinor()==Affect.TYP_MIND))
		{
			String tool=null;
			if(affect.tool()!=null)
			{
			    if(affect.tool() instanceof Ability)
					tool=((Ability)affect.tool()).name();
			}
			affect.addTrailerMsg(new FullMsg(affect.source(),myChar,Affect.MSG_OK_VISUAL,"<T-NAME> seems(s) completely unaffected by the "+((tool==null)?"mental attack":tool)+" from <S-NAME>."));
			affect.tagModified(true);
		}
		else
		if(affect.amITarget(myChar)&&(affect.targetMinor()==Affect.TYP_GAS))
		{
			String tool=null;
			if(affect.tool()!=null)
			{
			    if(affect.tool() instanceof Ability)
					tool=((Ability)affect.tool()).name();
			}
			affect.addTrailerMsg(new FullMsg(affect.source(),myChar,Affect.MSG_OK_VISUAL,"<T-NAME> seems(s) completely unaffected by the "+((tool==null)?"gas attack":tool)+" from <S-NAME>."));
			affect.tagModified(true);
		}
		else
		if(affect.amITarget(myChar)&&(affect.targetMinor()==Affect.TYP_UNDEAD))
		{
			String tool=null;
			if(affect.tool()!=null)
			{
			    if(affect.tool() instanceof Ability)
					tool=((Ability)affect.tool()).name();
			}
			affect.addTrailerMsg(new FullMsg(affect.source(),myChar,Affect.MSG_OK_VISUAL,"<T-NAME> seems(s) completely unaffected by the "+((tool==null)?"undead":tool)+" from <S-NAME>."));
			affect.tagModified(true);
		}
	}
	public String healthText(MOB mob)
	{
		double pct=(Util.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.name() + "^r is near destruction!^N";
		else
		if(pct<.20)
			return "^r" + mob.name() + "^r is massively broken and damaged.^N";
		else
		if(pct<.30)
			return "^r" + mob.name() + "^r is very damaged.^N";
		else
		if(pct<.40)
			return "^y" + mob.name() + "^y is somewhat damaged.^N";
		else
		if(pct<.50)
			return "^y" + mob.name() + "^y has grown weak and slightly damaged.^N";
		else
		if(pct<.60)
			return "^p" + mob.name() + "^p has lost stability and is very weak.^N";
		else
		if(pct<.70)
			return "^p" + mob.name() + "^p is unstable and slightly weak.^N";
		else
		if(pct<.80)
			return "^g" + mob.name() + "^g is unbalanced and unstable.^N";
		else
		if(pct<.90)
			return "^g" + mob.name() + "^g is in an somewhat unbalanced.^N";
		else
		if(pct<.99)
			return "^g" + mob.name() + "^g is no longer in perfect condition.^N";
		else
			return "^c" + mob.name() + "^c is in perfect condition.^N";
	}
}
