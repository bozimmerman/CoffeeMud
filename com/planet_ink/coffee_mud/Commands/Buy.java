package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Buy extends StdCommand
{
	public Buy(){}

	private String[] access={"BUY"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		MOB shopkeeper=EnglishParser.parseShopkeeper(mob,commands,"Buy what from whom?");
		if(shopkeeper==null) return false;
		if(commands.size()==0)
		{
			mob.tell("Buy what?");
			return false;
		}
		if(CoffeeUtensils.getShopKeeper(shopkeeper)==null)
		{
			mob.tell(shopkeeper.name()+" is not a shopkeeper!");
			return false;
		}

		int maxToDo=Integer.MAX_VALUE;
		if((commands.size()>1)
		&&(Util.s_int((String)commands.firstElement())>0))
		{
			maxToDo=Util.s_int((String)commands.firstElement());
			commands.setElementAt("all",0);
		}

		MOB mobFor=null;
		if((commands.size()>2)
		&&(((String)commands.elementAt(commands.size()-2)).equalsIgnoreCase("for")))
		{
			MOB M=mob.location().fetchInhabitant((String)commands.lastElement());
			if(M==null)
			{
				mob.tell("There is noone called '"+((String)commands.lastElement())+"' here.");
				return false;
			}
			mobFor=M;
		}
		
		String whatName=Util.combine(commands,0);
		Vector V=new Vector();
		boolean allFlag=((String)commands.elementAt(0)).equalsIgnoreCase("all");
		if(whatName.toUpperCase().startsWith("ALL.")){ allFlag=true; whatName="ALL "+whatName.substring(4);}
		if(whatName.toUpperCase().endsWith(".ALL")){ allFlag=true; whatName="ALL "+whatName.substring(0,whatName.length()-4);}
		int addendum=1;
		String addendumStr="";
		do
		{
			Environmental itemToDo=CoffeeUtensils.getShopKeeper(shopkeeper).getStock(whatName,mob);
			if(itemToDo==null) break;
			if(Sense.canBeSeenBy(itemToDo,mob))
				V.addElement(itemToDo);
			if(addendum>=CoffeeUtensils.getShopKeeper(shopkeeper).numberInStock(itemToDo))
				break;
			addendumStr="."+(++addendum);
		}
		while((allFlag)&&(addendum<=maxToDo));
		String forName="";
		if(mobFor!=null)
		{
			if(mobFor.name().indexOf(" ")>=0)
				forName=" for \""+mobFor.Name()+"\"";
			else
				forName=" for "+mob.Name();
		}

		if(V.size()==0)
			mob.tell(shopkeeper,null,null,"<S-NAME> doesn't appear to have any '"+whatName+"' for sale.  Try LIST.");
		else
		for(int v=0;v<V.size();v++)
		{
			Environmental thisThang=(Environmental)V.elementAt(v);
			FullMsg newMsg=new FullMsg(mob,shopkeeper,thisThang,CMMsg.MSG_BUY,"<S-NAME> buy(s) <O-NAME> from <T-NAMESELF>"+forName+".");
			if(mob.location().okMessage(mob,newMsg))
				mob.location().send(mob,newMsg);
		}
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
