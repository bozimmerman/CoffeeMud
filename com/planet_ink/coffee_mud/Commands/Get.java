package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Get extends BaseItemParser
{
	public Get(){}

	private String[] access={"GET","G"};
	public String[] getAccessWords(){return access;}

	public static boolean get(MOB mob, Item container, Item getThis, boolean quiet)
	{ return get(mob,container,getThis,quiet,"get",false);}

	public static boolean get(MOB mob,
							  Item container,
							  Item getThis,
							  boolean quiet,
							  String getWord,
							  boolean optimize)
	{
		String theWhat="<T-NAME>";
		Item target=getThis;
		Item tool=null;
		if(container!=null)
		{
			tool=getThis;
			target=container;
			theWhat="<O-NAME> from <T-NAME>";
		}
		if(!getThis.amWearingAt(Item.INVENTORY))
		{
			FullMsg msg=new FullMsg(mob,getThis,null,(optimize?CMMsg.MASK_OPTIMIZE:0)|CMMsg.MSG_REMOVE,null);
			if(!mob.location().okMessage(mob,msg))
				return false;
			mob.location().send(mob,msg);
		}
		FullMsg msg=new FullMsg(mob,target,tool,(optimize?CMMsg.MASK_OPTIMIZE:0)|CMMsg.MSG_GET,quiet?null:"<S-NAME> "+getWord+"(s) "+theWhat+".");
		if(!mob.location().okMessage(mob,msg))
			return false;
		mob.location().send(mob,msg);
		// we do this next step because, when a container is involved,
		// the item deserves to be the target of the GET.
		if(!mob.isMine(target))
		{
			msg=new FullMsg(mob,getThis,null,(optimize?CMMsg.MASK_OPTIMIZE:0)|CMMsg.MSG_GET,null);
			if(!mob.location().okMessage(mob,msg))
				return false;
			mob.location().send(mob,msg);
		}
		return true;
	}

	public Item possibleRoomGold(MOB seer, Room room, Item container, String itemID)
	{
		int gold=numPossibleGold(itemID);
		if(gold>0)
		{
			for(int i=0;i<room.numItems();i++)
			{
				Item I=room.fetchItem(i);
				if((I.container()==container)
				&&(I instanceof Coins)
				&&(Sense.canBeSeenBy(I,seer)))
				{
					if(((Coins)I).numberOfCoins()<=gold)
						return I;
					((Coins)I).setNumberOfCoins(((Coins)I).numberOfCoins()-gold);
					Item C=(Item)CMClass.getItem("StdCoins");
					C.baseEnvStats().setAbility(gold);
					C.recoverEnvStats();
					room.addItem(C);
					C.setDispossessionTime(I.dispossessionTime());
					return C;
				}
			}
		}
		return null;
	}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if((commands.size()>1)&&(commands.firstElement() instanceof Item))
		{
			Item item=(Item)commands.firstElement();
			Item container=null;
			boolean quiet=false;
			if(commands.elementAt(1) instanceof Item)
			{
				container=(Item)commands.elementAt(1);
				if((commands.size()>2)&&(commands.elementAt(2) instanceof Boolean))
					quiet=((Boolean)commands.elementAt(2)).booleanValue();
			}
			else
			if(commands.elementAt(1) instanceof Boolean)
				quiet=((Boolean)commands.elementAt(1)).booleanValue();
			return get(mob,container,item,quiet);
		}

		if(commands.size()<2)
		{
			mob.tell("Get what?");
			return false;
		}
		commands.removeElementAt(0);

		String containerName="";
		if(commands.size()>0)
			containerName=(String)commands.lastElement();
		Vector containers=possibleContainers(mob,commands,Item.WORN_REQ_ANY);
		int c=0;

		int maxToGet=Integer.MAX_VALUE;
		if((commands.size()>1)
		&&(Util.s_int((String)commands.firstElement())>0)
		&&(numPossibleGold(Util.combine(commands,0))==0))
		{
			maxToGet=Util.s_int((String)commands.firstElement());
			commands.setElementAt("all",0);
		}

		String whatToGet=Util.combine(commands,0);
		boolean allFlag=(commands.size()>0)?((String)commands.elementAt(0)).equalsIgnoreCase("all"):false;
		if(whatToGet.toUpperCase().startsWith("ALL.")){ allFlag=true; whatToGet="ALL "+whatToGet.substring(4);}
		if(whatToGet.toUpperCase().endsWith(".ALL")){ allFlag=true; whatToGet="ALL "+whatToGet.substring(0,whatToGet.length()-4);}
		boolean doneSomething=false;
		while((c<containers.size())||(containers.size()==0))
		{
			Vector V=new Vector();
			Item container=null;
			if(containers.size()>0) container=(Item)containers.elementAt(c++);
			int addendum=1;
			String addendumStr="";
			do
			{
				Environmental getThis=null;
				if((container!=null)&&(mob.isMine(container)))
				   getThis=mob.location().fetchFromMOBRoomFavorsItems(mob,(Item)container,whatToGet+addendumStr,Item.WORN_REQ_UNWORNONLY);
				else
				{
					if(!allFlag)
						getThis=possibleRoomGold(mob,mob.location(),container,whatToGet);
					if(getThis==null)
						getThis=mob.location().fetchFromRoomFavorItems((Item)container,whatToGet+addendumStr,Item.WORN_REQ_UNWORNONLY);
				}
				if(getThis==null) break;
				if((getThis instanceof Item)
				&&(Sense.canBeSeenBy(getThis,mob))
				&&((!allFlag)||Sense.isGettable(((Item)getThis))||(getThis.displayText().length()>0))
				&&(!V.contains(getThis)))
					V.addElement(getThis);
				addendumStr="."+(++addendum);
			}
			while((allFlag)&&(addendum<=maxToGet));

			for(int i=0;i<V.size();i++)
			{
				Item getThis=(Item)V.elementAt(i);
				if(!get(mob,container,(Item)getThis,false,"get",true))
					if(getThis instanceof Coins)
						((Coins)getThis).putCoinsBack();
				doneSomething=true;
			}
			mob.location().recoverRoomStats();
			mob.location().recoverRoomStats();

			if(containers.size()==0) break;
		}
		if(!doneSomething)
		{
			if(containers.size()>0)
			{
				Item container=(Item)containers.elementAt(0);
				if(((Container)container).isOpen())
					mob.tell("You don't see that in "+container.name()+".");
				else
					mob.tell(container.name()+" is closed.");
			}
			else
			if(containerName.equalsIgnoreCase("all"))
				mob.tell("You don't see anything here.");
			else
				mob.tell("You don't see '"+containerName+"' here.");
		}
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
