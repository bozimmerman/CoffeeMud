package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Sheath extends BaseItemParser
{
	public Sheath(){}

	private String[] access={"SHEATH"};
	public String[] getAccessWords(){return access;}

	public static Vector getSheaths(MOB mob)
	{
		Vector sheaths=new Vector();
		if(mob!=null)
		for(int i=0;i<mob.inventorySize();i++)
		{
			Item I=mob.fetchInventory(i);
			if((I!=null)
			&&(!I.amWearingAt(Item.INVENTORY))
			&&(I instanceof Container)
			&&(((Container)I).capacity()>0)
			&&(((Container)I).containTypes()!=Container.CONTAIN_ANYTHING))
				sheaths.addElement(I);
		}
		return sheaths;
	}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		boolean quiet=false;
		boolean noerrors=false;
		if((commands.size()>0)&&(((String)commands.lastElement()).equalsIgnoreCase("QUIETLY")))
		{
			commands.removeElementAt(commands.size()-1);
			quiet=true;
		}
		if((commands.size()>0)&&(((String)commands.lastElement()).equalsIgnoreCase("IFPOSSIBLE")))
		{
			commands.removeElementAt(commands.size()-1);
			noerrors=true;
		}

		Item item1=null;
		Item item2=null;
		if(commands.size()>0)
			commands.removeElementAt(0);
		if(commands.size()==0)
		{
			for(int i=0;i<mob.inventorySize();i++)
			{
				Item I=mob.fetchInventory(i);
				if((I!=null)
				&&(I instanceof Weapon)
				&&(!I.amWearingAt(Item.INVENTORY)))
				{
					if(I.amWearingAt(Item.WIELD))
						item1=I;
					else
					if(I.amWearingAt(Item.HELD))
						item2=I;
				}
			}
			if((noerrors)&&(item1==null)&&(item2==null))
				return false;
		}
		Vector sheaths=getSheaths(mob);
		Vector items=new Vector();
		Vector containers=new Vector();
		Item sheathable=null;
		if(commands.size()==0)
		{
			if(item2==item1) item2=null;
			for(int i=0;i<sheaths.size();i++)
			{
				Container sheath=(Container)sheaths.elementAt(i);
				if((item1!=null)
				&&(!items.contains(item1))
				&&(sheath.canContain(item1)))
				{
					items.addElement(item1);
					containers.addElement(sheath);
				}
				else
				if((item2!=null)
				&&(!items.contains(item2))
				&&(sheath.canContain(item2)))
				{
					items.addElement(item2);
					containers.addElement(sheath);
				}
			}
			if(item2!=null)
			for(int i=0;i<sheaths.size();i++)
			{
				Container sheath=(Container)sheaths.elementAt(i);
				if((sheath.canContain(item2))
				&&(!items.contains(item2)))
				{
					items.addElement(item2);
					containers.addElement(sheath);
				}
			}
			if(item1!=null)	sheathable=item1;
			else
			if(item2!=null)	sheathable=item2;
		}
		else
		{
			commands.insertElementAt("all",0);
			Container container=(Container)possibleContainer(mob,commands,false,Item.WORN_REQ_WORNONLY);
			String thingToPut=Util.combine(commands,0);
			int addendum=1;
			String addendumStr="";
			boolean allFlag=(commands.size()>0)?((String)commands.elementAt(0)).equalsIgnoreCase("all"):false;
			if(thingToPut.toUpperCase().startsWith("ALL.")){ allFlag=true; thingToPut="ALL "+thingToPut.substring(4);}
			if(thingToPut.toUpperCase().endsWith(".ALL")){ allFlag=true; thingToPut="ALL "+thingToPut.substring(0,thingToPut.length()-4);}
			do
			{
				Item putThis=mob.fetchWornItem(thingToPut+addendumStr);
				if(putThis==null) break;
				if(((putThis.amWearingAt(Item.WIELD))
				   ||(putThis.amWearingAt(Item.HELD)))
				   &&(putThis instanceof Weapon))
				{
					if(Sense.canBeSeenBy(putThis,mob)&&(!items.contains(putThis)))
					{
						sheathable=putThis;
						items.addElement(putThis);
						if((container!=null)&&(container.canContain(putThis)))
							containers.addElement(container);
						else
						{
							Container tempContainer=null;
							for(int i=0;i<sheaths.size();i++)
							{
								Container sheath=(Container)sheaths.elementAt(i);
								if(sheath.canContain(putThis))
								{tempContainer=sheath; break;}
							}
							if(tempContainer==null)
								items.remove(putThis);
							else
								containers.addElement(tempContainer);
						}
					}
				}
				addendumStr="."+(++addendum);
			}
			while(allFlag);
		}

		if(items.size()==0)
		{
			if(!noerrors)
				if(sheaths.size()==0)
					mob.tell("You are not wearing an appropriate sheath.");
				else
				if(sheathable!=null)
					mob.tell("You aren't wearing anything you can sheath "+sheathable.name()+" in.");
				else
				if(commands.size()==0)
					mob.tell("You don't seem to be wielding anything you can sheath.");
				else
					mob.tell("You don't seem to be wielding that.");
		}
		else
		for(int i=0;i<items.size();i++)
		{
			Item putThis=(Item)items.elementAt(i);
			Container container=(Container)containers.elementAt(i);
			if(CommonMsgs.remove(mob,putThis,true))
			{
				FullMsg putMsg=new FullMsg(mob,container,putThis,CMMsg.MSG_PUT,((quiet?null:"<S-NAME> sheath(s) <O-NAME> in <T-NAME>.")));
				if(mob.location().okMessage(mob,putMsg))
					mob.location().send(mob,putMsg);
			}
		}
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
