package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.core.CMClass;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.CMParms;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.CharClass;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.Races.interfaces.Race;

import java.util.*;

/*
   Copyright 2015-2018 Bo Zimmerman

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
public class Prop_ItemSlot extends Property
{
	@Override
	public String ID()
	{
		return "Prop_ItemSlot";
	}

	@Override
	public String name()
	{
		return "Has slots for enhancement items";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ITEMS;
	}

	@Override
	public boolean bubbleAffect()
	{
		return true;
	}
	
	protected Item[] 	slots		= new Item[0];
	protected Ability[] slotProps	= new Ability[0];
	
	protected String	slotType	= "";
	protected boolean	removeable	= true;
	
	private boolean		setAffected = true;

	protected int getNumberOfEmptySlots()
	{
		int num=0;
		for(Item I : slots)
		{
			if(I==null)
				num++;
		}
		return num;
	}
	
	@Override
	public void setMiscText(String text)
	{
		super.setMiscText("");
		slots = new Item[0];
		slotProps	= new Ability[0];
		String itemXml = ""; 
		int x=text.indexOf(';');
		if(x >=0)
		{
			itemXml = text.substring(x+1).trim();
			text = text.substring(0,x).trim();
		}
		int numSlots = CMParms.getParmInt(text, "NUM", 1);
		if(numSlots > 0)
		{
			slots = new Item[numSlots];
			slotProps	= new Ability[numSlots];
		}
		slotType= CMParms.getParmStr(text, "TYPE", "");
		removeable = CMParms.getParmBool(text, "REMOVEABLE", true);
		if(itemXml.length()>0)
		{
			if(itemXml.startsWith("<ITEM>"))
				itemXml="<ITEMS>"+itemXml+"</ITEMS>";
			final List<Item> items = new LinkedList<Item>();
			CMLib.coffeeMaker().addItemsFromXML(itemXml, items, null);
			int islot = 0;
			int aslot = 0;
			for(Item I : items)
			{
				final Ability A=I.fetchEffect("Prop_ItemSlotFiller");
				if(A!=null)
				{
					int aSlotNumbs= CMParms.getParmInt(A.text(), "NUM", 1);
					for(int a=0; (a<aSlotNumbs) && (islot<slots.length);a++)
						slots[islot++]=I;
					if(aslot < slotProps.length)
					{
						slotProps[aslot++]=A;
					}
				}
			}
		}
		setAffected = true;
	}
	
	@Override
	public String text()
	{
		StringBuilder str=new StringBuilder("");
		str.append("NUM="+slots.length+" ");
		str.append("REMOVEABLE="+(""+removeable).toUpperCase()+" ");
		str.append("TYPE=\""+slotType+"\" ");
		str.append("; ");
		List<Item> items=new ArrayList<Item>(slots.length);
		for(Item I : slots)
		{
			if((I != null)&&(!items.contains(I)))
				items.add(I);
		}
		str.append("<ITEMS>"+CMLib.coffeeMaker().getItemsXML(items, new Hashtable<String,List<Item>>(),new HashSet<String>(),null)+"</ITEMS>");
		return str.toString();
	}
	
	@Override
	public String accountForYourself()
	{
		StringBuilder str=new StringBuilder("");
		for(int slotNum=0;slotNum<slots.length;slotNum++)
		{
			Item I=slots[slotNum];
			Ability A=slotProps[slotNum];
			if((I!=null)&&(A instanceof Prop_ItemSlotFiller))
			{
				Prop_ItemSlotFiller P=(Prop_ItemSlotFiller)A;
				for(Ability A2 : P.affects)
					str.append(A2.accountForYourself());
				str.append(".  ");
			}
		}
		return str.toString();
	}
	
	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if((msg.target() == affected) && (msg.targetMinor() == CMMsg.TYP_PUT) && (msg.tool() instanceof Item))
		{
			final Item slotItem = (Item)msg.tool();
			final Ability A=slotItem.fetchEffect("Prop_ItemSlotFiller");
			if(A==null)
			{
				if(!(affected instanceof Container))
				{
					msg.source().tell(msg.source(),msg.target(),msg.tool(),L("<O-NAME> will not fit in <T-NAME>."));
				}
				return false;
			}
			final String aSlotType= CMParms.getParmStr(A.text(), "TYPE", "");
			final int aSlotNumbs= CMParms.getParmInt(A.text(), "NUM", 1);
			if(!aSlotType.equalsIgnoreCase(slotType))
			{
				msg.source().tell(msg.source(),msg.target(),msg.tool(),L("<O-NAME> will not fit in <T-NAME>."));
				return false;
			}
			if(getNumberOfEmptySlots() < aSlotNumbs)
			{
				if((aSlotNumbs > 1) && (getNumberOfEmptySlots() > 0))
				{
					if(removeable)
						msg.source().tell(msg.source(),msg.target(),msg.tool(),L("<T-NAME> doesn't have enough empty slots.  It requires @x1.  You should remove something first.",""+aSlotNumbs));
					else
						msg.source().tell(msg.source(),msg.target(),msg.tool(),L("<T-NAME> doesn't have enough empty slots.  It requires @x1.",""+aSlotNumbs));
				}
				else
				{
					if(removeable)
						msg.source().tell(msg.source(),msg.target(),msg.tool(),L("<T-NAME> has no more empty slots.  You should remove something first."));
					else
						msg.source().tell(msg.source(),msg.target(),msg.tool(),L("<T-NAME> has no available slots."));
				}
				return false;
			}
			
			msg.setTargetCode(CMMsg.TYP_WAND_USE | msg.targetMajor());
		}
		else
		if(removeable
		&&(msg.sourceMinor()==CMMsg.TYP_COMMANDFAIL)
		&&(msg.targetMessage()!=null)
		&&(msg.targetMessage().length()>0)
		&&(msg.targetMessage().startsWith("R")||msg.targetMessage().startsWith("r"))
		&&(affected!=null))
		{
			Vector<String> V=CMParms.parse(msg.targetMessage().toUpperCase());
			if((V.size()>2)&&("REMOVE".startsWith(V.get(0).toUpperCase())))
			{
				int x=V.lastIndexOf("FROM");
				if((x>0)&&(x<V.size()-1))
					V.remove(x);
				else
					x=V.size()-1;
				String fromWhat = CMParms.combine(V,x);
				String what=CMParms.combine(V,1,x);
				if(CMLib.english().containsString(affected.name(), fromWhat)||CMLib.english().containsString(affected.displayText(), fromWhat))
				{
					List<Item> items=new ArrayList<Item>(slots.length);
					for(Item I : slots)
					{
						if((I != null)&&(!items.contains(I)))
							items.add(I);
					}
					Environmental E=CMLib.english().fetchEnvironmental(items, what, true);
					if(E==null)
						E=CMLib.english().fetchEnvironmental(items, what, false);
					if(E==null)
						msg.setSourceMessage(L("You don't see '@x1' in any of the slots in @x2.",what,fromWhat));
					else
					{
						Item gemI=(Item)E;
						Ability A=gemI.fetchEffect("Prop_ItemSlotFiller");
						msg.modify(msg.source(),affected,gemI,CMMsg.MSG_GET,CMLib.lang().L("<S-NAME> removes(s) <O-NAME> from <T-NAME>."));
						for(int i=0;i<slots.length;i++)
						{
							if(slots[i]==gemI)
								slots[i]=null;
						}
						for(int i=0;i<slotProps.length;i++)
						{
							if(slotProps[i]==A)
								slotProps[i]=null;
						}
						A.setAffectedOne(gemI);
						msg.source().addItem(gemI);
					}
				}
			}
		}
		for(Ability A : slotProps)
		{
			if((A!=null)&&(!A.okMessage(myHost, msg)))
				return false;
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		final Item affected = (this.affected instanceof Item)? (Item)this.affected : null;
		if(msg.target() == affected)
		{
			if((msg.targetMinor() == CMMsg.TYP_WAND_USE) && (msg.tool() instanceof Item))
			{
				final Item slotItem = (Item)msg.tool();
				final Ability A=slotItem.fetchEffect("Prop_ItemSlotFiller");
				int islot=0;
				int aSlotNumbs= CMParms.getParmInt(A.text(), "NUM", 1);
				for(islot=0;islot<slots.length;islot++)
				{
					if(slots[islot]==null)
					{
						slotItem.removeFromOwnerContainer();
						slots[islot] = slotItem;
						if((--aSlotNumbs) == 0)
						{
							break;
						}
					}
				}
				if(islot<slots.length)
				{
					for(int aslot=0;aslot<slotProps.length;aslot++)
					{
						if(slotProps[aslot]==null)
						{
							slotProps[aslot] = A;
							setAffected = true;
							break;
						}
					}
				}
			}
			else
			if((msg.targetMinor()==CMMsg.TYP_EXAMINE) || (msg.targetMinor()==CMMsg.TYP_LOOK))
			{
				StringBuilder str=new StringBuilder();
				if(slotType.length()>0)
					str.append(L("\n\rIt appears to have slots on it that '@x1' might fit in:\n\r",slotType));
				else
					str.append(L("\n\rIt appears to have slots on it:\n\r"));
				for(int i=0;i<slots.length;i++)
				{
					str.append(L("Slot @x1: ",""+(i+1)));
					if(slots[i]==null)
						str.append("Empty");
					else
						str.append(slots[i].name());
					str.append("\n\r");
				}
				msg.addTrailerMsg(CMClass.getMsg(msg.source(),null,null,CMMsg.MSG_OK_VISUAL,str.toString(),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
			}
		}
		
		for(Ability A : slotProps)
		{
			if(A!=null)
			{
				if(setAffected)
				{
					A.setAffectedOne(affected);
				}
				A.executeMsg(myHost, msg);
			}
		}
		if(setAffected && (affected != null))
		{
			Item I=affected;
			I.recoverPhyStats();
			Room R=CMLib.map().roomLocation(I);
			if(R!=null)
				R.recoverRoomStats();
			setAffected = false;
		}
		super.executeMsg(myHost, msg);
	}

	@Override
	public void affectPhyStats(Physical host, PhyStats affectableStats)
	{
		if((host == affected)&&(!(affected instanceof Container)))
			affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.SENSE_INSIDEACCESSIBLE);
		for(Ability A : slotProps)
		{
			if(A!=null)
			{
				A.affectPhyStats(host, affectableStats);
			}
		}
		super.affectPhyStats(host,affectableStats);
	}

	@Override
	public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
	{
		for(Ability A : slotProps)
		{
			if(A!=null)
				A.affectCharStats(affectedMOB, affectedStats);
		}
		super.affectCharStats(affectedMOB,affectedStats);
	}

	@Override
	public void affectCharState(MOB affectedMOB, CharState affectedState)
	{
		for(Ability A : slotProps)
		{
			if(A!=null)
				A.affectCharState(affectedMOB, affectedState);
		}
		super.affectCharState(affectedMOB,affectedState);
	}
}
