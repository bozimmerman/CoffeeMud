package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Take extends BaseItemParser
{
	public Take(){}

	private String[] access={"TAKE"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(mob.isASysOp(mob.location()))
		{
			if(commands.size()<3)
			{
				mob.tell("Take what from whom?");
				return false;
			}
			commands.removeElementAt(0);
			if(commands.size()<2)
			{
				mob.tell("From whom should I take the "+(String)commands.elementAt(0));
				return false;
			}

			MOB victim=mob.location().fetchInhabitant((String)commands.elementAt(commands.size()-1));
			if((victim==null)||((victim!=null)&&(!Sense.canBeSeenBy(victim,mob))))
			{
				mob.tell("I don't see anyone called "+(String)commands.elementAt(commands.size()-1)+" here.");
				return false;
			}
			commands.removeElementAt(commands.size()-1);
			if((commands.size()>0)&&(((String)commands.elementAt(commands.size()-1)).equalsIgnoreCase("from")))
				commands.removeElementAt(commands.size()-1);

			int maxToGive=Integer.MAX_VALUE;
			if((commands.size()>1)
			&&(Util.s_int((String)commands.firstElement())>0))
			{
				maxToGive=Util.s_int((String)commands.firstElement());
				commands.setElementAt("all",0);
			}

			String thingToGive=Util.combine(commands,0);
			int addendum=1;
			String addendumStr="";
			Vector V=new Vector();
			boolean allFlag=((String)commands.elementAt(0)).equalsIgnoreCase("all");
			if(thingToGive.toUpperCase().startsWith("ALL.")){ allFlag=true; thingToGive="ALL "+thingToGive.substring(4);}
			if(thingToGive.toUpperCase().endsWith(".ALL")){ allFlag=true; thingToGive="ALL "+thingToGive.substring(0,thingToGive.length()-4);}
			do
			{
				Environmental giveThis=possibleGold(victim,thingToGive);
				if(giveThis!=null)
					allFlag=false;
				else
					giveThis=victim.fetchCarried(null,thingToGive+addendumStr);
				if((giveThis==null)
				&&(V.size()==0)
				&&(addendumStr.length()==0)
				&&(!allFlag))
					giveThis=victim.fetchWornItem(thingToGive);
				if(giveThis==null) break;
				if(giveThis instanceof Item)
				{
					((Item)giveThis).unWear();
					V.addElement(giveThis);
				}
				addendumStr="."+(++addendum);
			}
			while((allFlag)&&(addendum<=maxToGive));

			if(V.size()==0)
				mob.tell(victim.name()+" does not seem to be carrying that.");
			else
			for(int i=0;i<V.size();i++)
			{
				Item giveThis=(Item)V.elementAt(i);
				FullMsg newMsg=new FullMsg(victim,mob,giveThis,CMMsg.MASK_GENERAL|CMMsg.MSG_GIVE,"<T-NAME> take(s) <O-NAME> from <S-NAMESELF>.");
				if(victim.location().okMessage(victim,newMsg))
					victim.location().send(victim,newMsg);
				if(!mob.isMine(giveThis)) mob.giveItem(giveThis);
			}
		}
		else
		{
			if(((String)commands.elementAt(commands.size()-1)).equalsIgnoreCase("off"))
			{
				commands.removeElementAt(commands.size()-1);
				Command C=CMClass.getCommand("Remove");
				if(C!=null) C.execute(mob,commands);
			}
			else
			if((commands.size()>1)&&(((String)commands.elementAt(1)).equalsIgnoreCase("off")))
			{
				commands.removeElementAt(1);
				Command C=CMClass.getCommand("Remove");
				if(C!=null) C.execute(mob,commands);
			}
			else
			{
				Command C=CMClass.getCommand("Get");
				if(C!=null) C.execute(mob,commands);
			}
		}
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
