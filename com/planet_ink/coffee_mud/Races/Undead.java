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
}
