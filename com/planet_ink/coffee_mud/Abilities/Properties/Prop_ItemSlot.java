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
   Copyright 2015-2020 Bo Zimmerman

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
	protected boolean	removable	= true;
	protected int		levelShift	= 0;
	protected int		levelDiff	= 0;

	private boolean		setAffected = true;

	@Override
	public CMObject copyOf()
	{
		final Prop_ItemSlot pA = (Prop_ItemSlot)super.copyOf();
		if(slots != null)
		{
			pA.slots = new Item[slots.length];
			for(int i=0;i<slots.length;i++)
			{
				if(slots[i]!=null)
					pA.slots[i] = (Item)slots[i].copyOf();
			}
		}
		if(slotProps != null)
		{
			pA.slotProps = new Ability[slotProps.length];
			for(int i=0;i<slotProps.length;i++)
			{
				if(slotProps[i]!=null)
					pA.slotProps[i] = (Ability)slotProps[i].copyOf();
			}
		}
		return pA;
	}

	protected int getNumberOfEmptySlots()
	{
		int num=0;
		for(final Item I : slots)
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
		final int x=text.indexOf(';');
		if(x >=0)
		{
			itemXml = text.substring(x+1).trim();
			text = text.substring(0,x).trim();
		}
		final int numSlots = CMParms.getParmInt(text, "NUM", 1);
		if(numSlots > 0)
		{
			slots = new Item[numSlots];
			slotProps	= new Ability[numSlots];
		}
		final String lvlCode=CMParms.getParmStr(text, "LEVEL", "NONE").toUpperCase().trim();
		levelShift=0;
		if(lvlCode.startsWith("A"))
			levelShift=1;
		else
		if(lvlCode.startsWith("H"))
			levelShift=2;
		slotType= CMParms.getParmStr(text, "TYPE", "");
		removable = CMParms.getParmBool(text, "REMOVEABLE", CMParms.getParmBool(text, "REMOVABLE", true));
		levelDiff=0;
		if(itemXml.length()>0)
		{
			if(itemXml.startsWith("<ITEM>"))
				itemXml="<ITEMS>"+itemXml+"</ITEMS>";
			final List<Item> items = new LinkedList<Item>();
			CMLib.coffeeMaker().addItemsFromXML(itemXml, items, null);
			int islot = 0;
			int aslot = 0;
			for(final Item I : items)
			{
				final Ability A=I.fetchEffect("Prop_ItemSlotFiller");
				if(A!=null)
				{
					final int aSlotNumbs= CMParms.getParmInt(A.text(), "NUM", 1);
					for(int a=0; (a<aSlotNumbs) && (islot<slots.length);a++)
						slots[islot++]=I;
					if(aslot < slotProps.length)
					{
						slotProps[aslot++]=A;
					}
					if(levelShift == 1)
					{
						levelDiff += I.phyStats().level();
					}
					else
					if(levelShift == 2)
					{
						if(I.phyStats().level()>levelDiff)
							levelDiff=I.phyStats().level();
					}
				}
			}
		}
		setAffected = true;
	}

	@Override
	public String text()
	{
		final StringBuilder str=new StringBuilder("");
		str.append("NUM="+slots.length+" ");
		str.append("REMOVABLE="+(""+removable).toUpperCase()+" ");
		str.append("TYPE=\""+slotType+"\" ");
		final String[] levelShifts = new String[] {"NONE","ADD","HIGH"};
		if(levelShift != 0)
			str.append("LEVEL=\""+levelShifts[levelShift]+"\" ");
		str.append("; ");
		final List<Item> items=new ArrayList<Item>(slots.length);
		for(final Item I : slots)
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
		final StringBuilder str=new StringBuilder("");
		for(int slotNum=0;slotNum<slots.length;slotNum++)
		{
			final Item I=slots[slotNum];
			final Ability A=slotProps[slotNum];
			if((I!=null)&&(A instanceof Prop_ItemSlotFiller))
			{
				final Prop_ItemSlotFiller P=(Prop_ItemSlotFiller)A;
				if(P.getAffects() != null)
				{
					for(final Ability A2 : P.getAffects())
						str.append(A2.accountForYourself());
				}
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
		if((msg.target() == affected)
		&& (msg.targetMinor() == CMMsg.TYP_PUT)
		&& (msg.tool() instanceof Item))
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
					if(removable)
						msg.source().tell(msg.source(),msg.target(),msg.tool(),L("<T-NAME> doesn't have enough empty slots.  It requires @x1.  You should remove something first.",""+aSlotNumbs));
					else
						msg.source().tell(msg.source(),msg.target(),msg.tool(),L("<T-NAME> doesn't have enough empty slots.  It requires @x1.",""+aSlotNumbs));
				}
				else
				{
					if(removable)
						msg.source().tell(msg.source(),msg.target(),msg.tool(),L("<T-NAME> has no more empty slots.  You should remove something first."));
					else
						msg.source().tell(msg.source(),msg.target(),msg.tool(),L("<T-NAME> has no available slots."));
				}
				return false;
			}
			msg.setTargetCode(CMMsg.TYP_WAND_USE | msg.targetMajor());
		}
		else
		if(removable
		&&(msg.sourceMinor()==CMMsg.TYP_COMMANDFAIL)
		&&(msg.targetMessage()!=null)
		&&(msg.targetMessage().length()>0)
		&&("Rr".indexOf(msg.targetMessage().charAt(0))>=0)
		&&(affected!=null))
		{
			final Vector<String> V=CMParms.parse(msg.targetMessage().toUpperCase());
			if((V.size()>2) // keep this block, even though the "Rr" above makes it unused
			&&("PUT".startsWith(V.get(0).toUpperCase())))
			{
				int x=V.lastIndexOf("INTO");
				if((x>0)&&(x<V.size()-1))
					V.remove(x);
				else
					x=V.size()-1;
				final String intoWhat = CMParms.combine(V,x);
				final String what=CMParms.combine(V,1,x);
				if(CMLib.english().containsString(affected.name(), intoWhat)||CMLib.english().containsString(affected.displayText(), intoWhat))
				{
					final List<Item> items=new ArrayList<Item>(slots.length);
					for(final Enumeration<Item> i = msg.source().items();i.hasMoreElements();)
					{
						final Item I = i.nextElement();
						if((I!=null)
						&&(I != affected)
						&&(I.container()==null)
						&&(CMLib.flags().canBeSeenBy(I, msg.source()))
						&&(I.amWearingAt(Item.IN_INVENTORY))
						&&(!items.contains(I)))
							items.add(I);
					}
					Environmental E=CMLib.english().fetchEnvironmental(items, what, true);
					if(E==null)
						E=CMLib.english().fetchEnvironmental(items, what, false);
					if(!(E instanceof Item))
					{
						msg.setSourceMessage(L("You don't seem to have '@x1' handy.",what));
						return true;
					}
					final Item slotItem = (Item)E;
					final Ability A=slotItem.fetchEffect("Prop_ItemSlotFiller");
					if(A==null)
					{
						if(!(affected instanceof Container))
						{
							msg.setSourceMessage(L("@x1 will not fit in @x2.",slotItem.name(),affected.name()));
							return true;
						}
						return true;
					}
					final String aSlotType= CMParms.getParmStr(A.text(), "TYPE", "");
					final int aSlotNumbs= CMParms.getParmInt(A.text(), "NUM", 1);
					if(!aSlotType.equalsIgnoreCase(slotType))
					{
						msg.setSourceMessage(L("@x1 will not fit in @x2.",slotItem.name(),affected.name()));
						return true;
					}
					if(getNumberOfEmptySlots() < aSlotNumbs)
					{
						if((aSlotNumbs > 1) && (getNumberOfEmptySlots() > 0))
						{
							if(removable)
								msg.setSourceMessage(L("@x1 doesn't have enough empty slots.  It requires @x2.  You should remove something first.",affected.name(),""+aSlotNumbs));
							else
								msg.setSourceMessage(L("@x1 doesn't have enough empty slots.  It requires @x2.",affected.name(),""+aSlotNumbs));
						}
						else
						{
							if(removable)
								msg.setSourceMessage(L("@x1 has no more empty slots.  You should remove something first.",affected.name()));
							else
								msg.setSourceMessage(L("@x1 has no more available slots.",affected.name()));
						}
						return true;
					}
					final CMMsg msg2=(CMMsg)msg.copyOf();
					msg2.setTarget(affected);
					msg2.setTool(slotItem);
					msg2.setSourceCode(CMMsg.TYP_WAND_USE | CMMsg.MASK_HANDS);
					msg2.setTargetCode(CMMsg.TYP_WAND_USE | CMMsg.MASK_HANDS);
					msg2.setSourceCode(CMMsg.TYP_WAND_USE | CMMsg.MASK_HANDS);
					msg2.setSourceMessage(L("<S-NAME> put(s) <O-NAME> into <T-NAME>."));
					msg2.setTargetMessage(L("<S-NAME> put(s) <O-NAME> into <T-NAME>."));
					msg2.setOthersMessage(L("<S-NAME> put(s) <O-NAME> into <T-NAME>."));
					final Room R=CMLib.map().roomLocation(affected);
					if((R!=null)
					&&(R.okMessage(msg.source(), msg2)))
						R.send(msg.source(), msg2);
					return false;
				}
			}
			else
			if((V.size()>2)&&("REMOVE".startsWith(V.get(0).toUpperCase())))
			{
				int x=V.lastIndexOf("FROM");
				if((x>0)&&(x<V.size()-1))
					V.remove(x);
				else
					x=V.size()-1;
				final String fromWhat = CMParms.combine(V,x);
				final String what=CMParms.combine(V,1,x);
				if(CMLib.english().containsString(affected.name(), fromWhat)||CMLib.english().containsString(affected.displayText(), fromWhat))
				{
					final List<Item> items=new ArrayList<Item>(slots.length);
					for(final Item I : slots)
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
						final Item gemI=(Item)E;
						final Ability A=gemI.fetchEffect("Prop_ItemSlotFiller");
						msg.modify(msg.source(),affected,gemI,CMMsg.MSG_HANDS,CMLib.lang().L("<S-NAME> remove(s) <O-NAME> from <T-NAME>."));
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
						levelDiff = 0;
						for(int i=0;i<slots.length;i++)
						{
							if(slots[i]==null)
								continue;
							if(levelShift == 1)
							{
								levelDiff += slots[i].phyStats().level();
							}
							else
							if(levelShift == 2)
							{
								if(slots[i].phyStats().level()>levelDiff)
									levelDiff=slots[i].phyStats().level();
							}
						}
					}
				}
			}
		}
		for(final Ability A : slotProps)
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
			if((msg.targetMinor() == CMMsg.TYP_WAND_USE)
			&& (msg.tool() instanceof Item))
			{
				final Item slotItem = (Item)msg.tool();
				final Ability A=slotItem.fetchEffect("Prop_ItemSlotFiller");
				if(A!=null)
				{
					int islot=0;
					int aSlotNumbs= CMParms.getParmInt(A.text(), "NUM", 1);
					for(islot=0;islot<slots.length;islot++)
					{
						if(slots[islot]==null)
						{
							slotItem.removeFromOwnerContainer();
							slotItem.setOwner(null);
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
					if(levelShift == 1)
					{
						levelDiff += slotItem.phyStats().level();
					}
					else
					if(levelShift == 2)
					{
						if(slotItem.phyStats().level()>levelDiff)
							levelDiff=slotItem.phyStats().level();
					}
				}
			}
			else
			if((msg.targetMinor()==CMMsg.TYP_EXAMINE) || (msg.targetMinor()==CMMsg.TYP_LOOK))
			{
				final StringBuilder str=new StringBuilder();
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

		for(final Ability A : slotProps)
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
			final Item I=affected;
			I.recoverPhyStats();
			final Room R=CMLib.map().roomLocation(I);
			if(R!=null)
				R.recoverRoomStats();
			setAffected = false;
		}
		super.executeMsg(myHost, msg);
	}

	@Override
	public void affectPhyStats(final Physical host, final PhyStats affectableStats)
	{
		if(host == affected)
		{
			if(!(affected instanceof Container))
				affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.SENSE_INSIDEACCESSIBLE);
			switch(levelShift)
			{
			case 0:
				break;
			case 1:
				affectableStats.setLevel(affectableStats.level()+levelDiff);
				break;
			case 2:
				if(levelDiff > affectableStats.level())
					affectableStats.setLevel(levelDiff);
				break;
			}
		}
		else
		{
			for(final Ability A : slotProps)
			{
				if(A!=null)
				{
					A.affectPhyStats(A.affecting(), affectableStats);
				}
			}
		}
		super.affectPhyStats(host,affectableStats);
	}

	@Override
	public void affectCharStats(final MOB affectedMOB, final CharStats affectedStats)
	{
		for(final Ability A : slotProps)
		{
			if(A!=null)
				A.affectCharStats(affectedMOB, affectedStats);
		}
		super.affectCharStats(affectedMOB,affectedStats);
	}

	@Override
	public void affectCharState(final MOB affectedMOB, final CharState affectedState)
	{
		for(final Ability A : slotProps)
		{
			if(A!=null)
				A.affectCharState(affectedMOB, affectedState);
		}
		super.affectCharState(affectedMOB,affectedState);
	}
}
