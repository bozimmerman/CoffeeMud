package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Deviations extends StdCommand
{
	public Deviations(){}

	private String[] access={"DEVIATIONS"};
	public String[] getAccessWords(){return access;}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return false;}

	private void fillCheckDeviations(Room R, String type, Vector check)
	{
		if(type.equalsIgnoreCase("mobs")||type.equalsIgnoreCase("both"))
		{
			for(int m=0;m<R.numInhabitants();m++)
			{
				MOB M=R.fetchInhabitant(m);
				if((M!=null)&&(M.isEligibleMonster()))
					check.addElement(M);
			}
		}
		if(type.equalsIgnoreCase("items")||type.equalsIgnoreCase("both"))
		{
			for(int i=0;i<R.numItems();i++)
			{
				Item I=R.fetchItem(i);
				if((I!=null)
				&&((I instanceof Armor)||(I instanceof Weapon)))
					check.addElement(I);
			}
			for(int m=0;m<R.numInhabitants();m++)
			{
				MOB M=R.fetchInhabitant(m);
				if(M!=null)
				{
					for(int i=0;i<M.inventorySize();i++)
					{
						Item I=M.fetchInventory(i);
						if((I!=null)
						&&((I instanceof Armor)||(I instanceof Weapon)))
							check.addElement(I);
					}
					ShopKeeper sk=CoffeeUtensils.getShopKeeper(M);
					if(sk!=null)
					{
						Vector V=sk.getBaseInventory();
						for(int i=0;i<V.size();i++)
							if(V.elementAt(i) instanceof Item)
							{
								Item I=(Item)V.elementAt(i);
								if((I instanceof Armor)||(I instanceof Weapon))
									check.addElement(I);
							}
					}
				}
			}
		}
	}

	private String getDeviation(int val, Hashtable vals, String key)
	{
		if(!vals.containsKey(key))
			return "N/A";
		int val2=Util.s_int((String)vals.get(key));
		return getDeviation(val,val2);
	}
	private String getDeviation(int val, int val2)
	{
		if(val==val2) return "0%";
		int oval=val-val2;
		if(oval>0) return "+"+oval;
		else return ""+oval;
	}

	public StringBuffer deviations(MOB mob, String rest)
	{
		Vector V=Util.parse(rest);
		if((V.size()==0)
		||((!((String)V.firstElement()).equalsIgnoreCase("mobs"))
		   &&(!((String)V.firstElement()).equalsIgnoreCase("items"))
		   &&(!((String)V.firstElement()).equalsIgnoreCase("both"))))
			return new StringBuffer("You must specify whether you want deviations on MOBS, ITEMS, or BOTH.");

		String type=((String)V.firstElement()).toLowerCase();
		if(V.size()==1)
			return new StringBuffer("You must also specify a mob or item name, or the word room, or the word area.");

		String where=((String)V.elementAt(1)).toLowerCase();
		Environmental E=mob.location().fetchFromMOBRoomFavorsItems(mob,null,where,Item.WORN_REQ_ANY);
		Vector check=new Vector();
		if(where.equalsIgnoreCase("room"))
			fillCheckDeviations(mob.location(),type,check);
		else
		if(where.equalsIgnoreCase("area"))
		{
			for(Enumeration r=mob.location().getArea().getMap();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				fillCheckDeviations(R,type,check);
			}
		}
		else
		if(E==null)
			return new StringBuffer("'"+where+"' is an unknown item or mob name.");
		else
		if(type.equals("items")
		&&(!(E instanceof Weapon))
		&&(!(E instanceof Armor)))
			return new StringBuffer("'"+where+"' is not a weapon or armor item.");
		else
		if(type.equals("mobs")
		&&(!(E instanceof MOB)))
			return new StringBuffer("'"+where+"' is not a MOB.");
		else
		if((!(E instanceof Weapon))
		&&(!(E instanceof Armor))
		&&(!(E instanceof MOB)))
			return new StringBuffer("'"+where+"' is not a MOB, or Weapon, or Item.");
		else
			check.addElement(E);
		StringBuffer str=new StringBuffer("");
		str.append("Deviations Report:\n\r");
		str.append(Util.padRight("Name",20)+" ");
		str.append(Util.padRight("Lvl",5)+" ");
		str.append(Util.padRight("Att",5)+" ");
		str.append(Util.padRight("Dmg",5)+" ");
		str.append(Util.padRight("Val",5)+" ");
		str.append(Util.padRight("Weigt",5)+" ");
		str.append(Util.padRight("Speed",5)+" ");
		str.append(Util.padRight("MinRg",5)+" ");
		str.append(Util.padRight("MaxRg",5)+" ");
		str.append("\n\r");
		for(int c=0;c<check.size();c++)
		{
			if(check.elementAt(c) instanceof Item)
			{
				Item I=(Item)check.elementAt(c);
				Weapon W=null;
				if(I instanceof Weapon)
					W=(Weapon)I;
				Hashtable vals=CoffeeMaker.timsItemAdjustments(
								I,I.envStats().level(),I.material(),I.baseEnvStats().weight(),
								I.rawLogicalAnd()?2:1,
								(W==null)?0:W.weaponClassification(),
								I.maxRange(),
								I.rawProperLocationBitmap());
				str.append(Util.padRight(I.name(),20)+" ");
				str.append(Util.padRight(""+I.envStats().level(),5)+" ");
				str.append(Util.padRight(""+getDeviation(
											I.baseEnvStats().attackAdjustment(),
											vals,"ATTACK"),5)+" ");
				str.append(Util.padRight(""+getDeviation(
											I.baseEnvStats().damage(),
											vals,"DAMAGE"),5)+" ");
				str.append(Util.padRight(""+getDeviation(
											I.baseGoldValue(),
											vals,"VALUE"),5)+" ");
				str.append(Util.padRight(""+getDeviation(
											I.baseEnvStats().weight(),
											vals,"WEIGHT"),5)+" ");
				str.append(Util.padRight("N/A",5)+" ");
				str.append(Util.padRight(""+getDeviation(
											I.minRange(),
											vals,"MINRANGE"),5)+" ");
				str.append(Util.padRight(""+getDeviation(
											I.maxRange(),
											vals,"MAXRANGE"),5)+" ");
				str.append("\n\r");
			}
			else
			{
				MOB M=(MOB)check.elementAt(c);
				str.append(Util.padRight(M.name(),20)+" ");
				str.append(Util.padRight(""+M.envStats().level(),5)+" ");
				str.append(Util.padRight(""+getDeviation(
											M.baseEnvStats().attackAdjustment(),
											M.baseCharStats().getCurrentClass().getLevelAttack(M)),5)+" ");
				str.append(Util.padRight(""+getDeviation(
											M.baseEnvStats().damage(),
											M.baseCharStats().getCurrentClass().getLevelDamage(M)),5)+" ");
				str.append(Util.padRight("N/A",5)+" ");
				str.append(Util.padRight("N/A",5)+" ");
				str.append(Util.padRight(""+getDeviation(
											(int)Math.round(M.baseEnvStats().speed()),
											(int)Math.round(M.baseCharStats().getCurrentClass().getLevelSpeed(M))),5)+" ");
				str.append(Util.padRight("N/A",5)+" ");
				str.append(Util.padRight("N/A",5)+" ");
				str.append("\n\r");
			}
		}
		return str;
	}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		mob.tell(deviations(mob,Util.combine(commands,1)).toString());
		return false;
	}
	
	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
	public boolean securityCheck(MOB mob)
	{
		return CMSecurity.isAllowedStartsWith(mob,mob.location(),"CMDITEMS")
			|| CMSecurity.isAllowed(mob,mob.location(),"LISTADMIN")
			|| CMSecurity.isAllowed(mob,mob.location(),"CMDMOBS");
	}
}
