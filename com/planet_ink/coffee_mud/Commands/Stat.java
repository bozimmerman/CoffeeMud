package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Stat extends BaseAbleLister
{
	public Stat(){}

	private String[] access={"STAT"};
	public String[] getAccessWords(){return access;}

	public MOB getTarget(MOB mob, Vector commands, boolean quiet)
	{
		String targetName=Util.combine(commands,0);
		MOB target=null;
		if(targetName.length()>0)
		{
			target=mob.location().fetchInhabitant(targetName);
			if(target==null)
			{
				Environmental t=mob.location().fetchFromRoomFavorItems(null,targetName,Item.WORN_REQ_UNWORNONLY);
				if((t!=null)&&(!(t instanceof MOB)))
				{
					if(!quiet)
						mob.tell(mob,t,null,"You can't do that to <T-NAMESELF>.");
					return null;
				}
			}
		}

		if(target!=null)
			targetName=target.name();

		if((target==null)||((!Sense.canBeSeenBy(target,mob))&&((!Sense.canBeHeardBy(target,mob))||(!target.isInCombat()))))
		{
			if(!quiet)
			{
				if(targetName.trim().length()==0)
					mob.tell("You don't see them here.");
				else
					mob.tell("You don't see '"+targetName+"' here.");
			}
			return null;
		}

		return target;
	}


	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(!mob.isASysOp(mob.location()))
		{
			mob.tell("You are not powerful enough to do that.");
			return false;
		}
		commands.removeElementAt(0);
		int ableTypes=-1;
		if(commands.size()>1)
		{
			String s=((String)commands.elementAt(0)).toUpperCase();
			for(int a=0;a<Ability.TYPE_DESCS.length;a++)
			{
				if(Ability.TYPE_DESCS[a].equals(s))
				{
					ableTypes=a;
					commands.removeElementAt(0);
					break;
				}
			}
		}
		String MOBname=(String)Util.combine(commands,0);
		MOB target=getTarget(mob,commands,true);
		if((target==null)||((target!=null)&&(!target.isMonster())))
			target=mob.location().fetchInhabitant(MOBname);
		if((target==null)||((target!=null)&&(!target.isMonster())))
		{
			Enumeration r=mob.isASysOp(null)?CMMap.rooms():mob.location().getArea().getMap();
			for(;r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				MOB mob2=R.fetchInhabitant(MOBname);
				if(mob2!=null)
				{
					target=mob2;
					break;
				}
			}
		}
		if(target==null)
		{
			mob.tell("You can't stat '"+MOBname+"'  -- he doesn't exist.");
			return false;
		}

		StringBuffer str=new StringBuffer("");
		if(ableTypes>=0)
		{
			Vector V=new Vector();
			int mask=Ability.ALL_CODES;
			V.addElement(new Integer(ableTypes));
			str=getAbilities(target,V,mask,false,-1);
		}
		else
			str=CommonMsgs.getScore(target);
		if(!mob.isMonster())
			mob.session().unfilteredPrintln(str.toString());
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean arcCommand(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
