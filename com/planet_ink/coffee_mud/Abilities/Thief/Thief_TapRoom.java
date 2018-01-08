package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2006-2018 Bo Zimmerman

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

public class Thief_TapRoom extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_TapRoom";
	}

	private final static String localizedName = CMLib.lang().L("Tap Room");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ITEMS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ROOMS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	private static final String[] triggerStrings =I(new String[] {"TAPROOM"});
	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_ALERT;
	}

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT|USAGE_MANA;
	}

	public boolean norecurse=false;

	public boolean isMyPair(List<String> myParsedTextV, Item I)
	{
		Thief_TapRoom A=null;
		if(I instanceof Drink)
		{
			A=(Thief_TapRoom)I.fetchEffect(ID());
			if((A!=null)
			&&(A.text().startsWith("DST;")||A.text().startsWith("SRC;"))
			&&(!text().startsWith(A.text().substring(0,4))))
			{
				final List<String> p2=A.getParsedText();
				if((p2.size()==myParsedTextV.size())
				&&((myParsedTextV.get(myParsedTextV.size()-1)).equals(p2.get(p2.size()-1))))
					return true;
			}
		}
		return false;
	}

	public Item getMyPair()
	{
		final List<String> p=getParsedText();
		Room R=null;
		if(p.size()>=2)
			R=CMLib.map().getRoom(p.get(1));
		if(R==null)
			return null;
		Item I=null;
		for(int i=0;i<R.numItems();i++)
		{
			I=R.getItem(i);
			if(isMyPair(p,I))
				return I;
		}
		MOB M=null;
		for(int m=0;m<R.numInhabitants();m++)
		{
			M=R.fetchInhabitant(m);
			if(M==null)
				continue;
			for(int i=0;i<M.numItems();i++)
			{
				I=M.getItem(i);
				if(isMyPair(p,I))
					return I;
			}
		}
		return null;
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked)
		{
			final Item I=getMyPair();
			super.unInvoke();
			if((I!=null)&&(unInvoked))
			{
				final Thief_TapRoom A=(Thief_TapRoom)I.fetchEffect(ID());
				if((A!=null)&&(!A.unInvoked))
				{
					A.canBeUninvoked=true;
					A.unInvoke();
				}
			}
			else
				super.unInvoke();
		}
		else
			super.unInvoke();
	}

	@Override
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if(!super.okMessage(host,msg))
			return false;
		if(affected instanceof Item)
		{
			if((msg.targetMinor()==CMMsg.TYP_ENTER)
			&&(msg.target() instanceof Room)
			&&(msg.source()==((Item)affected).owner())
			&&(text().startsWith("DST;")))
			{
				if(getAvailableLine(msg.source()).size()==0)
				{
					msg.source().tell(L("You have run out of cloth to make tapline from!  Better put down the cup..."));
					return false;
				}
				int roomsLeft=0;
				final List<String> V=getParsedText();
				if(V.size()>3)
					roomsLeft=CMath.s_int(V.get(3));
				if(roomsLeft<=0)
				{
					msg.source().tell(L("Go any further, and your tap line won't work at all.  Better just put it down here..."));
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public int maxRange()
	{
		return (invoker()==null)?50:adjustedLevel(invoker(),0);
	}

	@Override
	public void executeMsg(Environmental host, CMMsg msg)
	{
		synchronized(this)
		{
			if((affected instanceof Item)
			&&(!norecurse))
			{
				norecurse=true;
				if((msg.targetMinor()==CMMsg.TYP_ENTER)
				&&(msg.target() instanceof Room)
				&&(msg.source()==((Item)affected).owner())
				&&(text().startsWith("DST;")))
				{
					final Room newRoom=(Room)msg.target();
					final List<String> p=getParsedText();
					if(p.size()<2)
					{
						canBeUninvoked=true;
						unInvoke();
					}
					else
					{
						final Item pairI=getMyPair();
						Room lastRoom=null;
						Thief_TapRoom pairA=null;
						if(pairI!=null)
						{
							pairA=(Thief_TapRoom)pairI.fetchEffect(ID());
							if((pairA!=null)&&(pairA.getParsedText().size()>0))
								lastRoom=CMLib.map().getRoom(pairA.getParsedText().get(1));
						}
						boolean ok=lastRoom==newRoom;
						for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
						{
							if(newRoom.getRoomInDir(d)==lastRoom)
								ok=true;
						}
						final List<RawMaterial> lineV=getAvailableLine(msg.source());
						if((!ok)||(lineV.size()==0)||(lastRoom==null)||(pairA==null)||(pairA.getParsedText().size()<2))
						{
							canBeUninvoked=true;
							unInvoke();
							msg.addTrailerMsg(CMClass.getMsg(msg.source(),null,null,CMMsg.MSG_OK_VISUAL,L("Oh no! You've lost your tap line! It was all for naught!"),null,null));
						}
						else
						{
							final RawMaterial I=lineV.get(0);
							CMLib.materials().destroyResourcesValue(msg.source(),1,I.material(),0,null);
							msg.addTrailerMsg(CMClass.getMsg(msg.source(),I,this,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,L("You stretch out another length of tap-line here using some of <T-NAME>"),null,L("<S-NAME> do(es) something in the corner with <T-NAME>")));
							int roomsLeft=0;
							if(p.size()>3)
								roomsLeft=CMath.s_int(p.get(3));
							p.set(3,(""+(roomsLeft-1)));
							super.miscText=CMParms.toSemicolonListString(p);
							final List<String> p2=pairA.getParsedText();
							p2.set(1,CMLib.map().getExtendedRoomID(newRoom));
							pairA.miscText=CMParms.toSemicolonListString(p2);
						}
					}
				}
				else
				if((msg.target()==affected)
				&&((msg.targetMinor()==CMMsg.TYP_GET)||(msg.targetMinor()==CMMsg.TYP_PUSH)||(msg.targetMinor()==CMMsg.TYP_PULL))
				&&(text().startsWith("SRC;")))
				{
					canBeUninvoked=true;
					unInvoke();
					msg.addTrailerMsg(CMClass.getMsg(msg.source(),null,null,CMMsg.MSG_OK_VISUAL,L("The tap line is broken."),null,null));
				}
				else
				if((msg.sourceMinor()==CMMsg.TYP_SPEAK)
				&&(msg.othersMessage()!=null)
				&&(text().startsWith("SRC;")))
				{
					final String str=CMStrings.getSayFromMessage(msg.othersMessage());
					if((str!=null)&&(str.length()>0))
					{
						final Item I=getMyPair();
						if(I==null)
						{
							canBeUninvoked=true;
							unInvoke();
						}
						else
						{
							final List<String> p=getParsedText();
							Room R=null;
							if(p.size()>=2)
								R=CMLib.map().getRoom(p.get(1));
							final CMMsg msg2=(CMMsg)msg.copyOf();
							msg2.setOthersMessage("^TFrom "+I.name()+" "+msg2.othersMessage());
							if((R!=null)&&(R.okMessage(msg.source(),msg2)))
								R.sendOthers(msg.source(),msg2);
						}
					}
				}
			}
			norecurse=false;
		}
	}

	public Item[] getCups(MOB mob)
	{
		Item I=null;
		final Item[] returnI=new Item[2];
		for(int i=0;i<mob.numItems();i++)
		{
			I=mob.getItem(i);
			if((I instanceof Drink)
			&&(I.container()==null)
			&&((I.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_LIQUID)
			&&((I.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_ENERGY)
			&&((I.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_GAS)
			&&((I.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_FLESH)
			&&((I.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_LEATHER)
			&&(!(I instanceof Potion))
			&&(((Drink)I).liquidHeld()<400)
			&&(I.fetchEffect(ID())==null)
			&&(CMLib.flags().canBeSeenBy(I,mob))
			&&(!CMLib.flags().isEnchanted(I)))
			{
				if(returnI[0]==null)
					returnI[0]=I;
				else
				if(returnI[1]==null)
					returnI[1]=I;
				if(returnI[1]!=null)
					break;
			}
		}
		if((returnI[0]==null)||(returnI[1]==null))
			return null;
		return returnI;
	}

	public List<RawMaterial> getAvailableLine(MOB mob)
	{
		Item I=null;
		final List<RawMaterial> available=new Vector<RawMaterial>();
		for(int i=0;i<mob.numItems();i++)
		{
			I=mob.getItem(i);
			if((I instanceof RawMaterial)
			&&((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_CLOTH)
			&&(!CMLib.flags().isOnFire(I))
			&&(!CMLib.flags().isEnchanted(I))
			&&(I.container()==null)
			&&(I.fetchEffect(ID())==null))
				available.add((RawMaterial)I);
		}
		return available;
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if((affected instanceof Item)
		&&((!(affected instanceof Drink))||(!text().startsWith("DST;"))))
			affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_HIDDEN);
	}

	public List<String> getParsedText()
	{
		return CMParms.parseSemicolons(text(),false);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Room target=mob.location();
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof Room))
			target=(Room)givenTarget;
		if(mob.isInCombat())
		{
			mob.tell(mob,null,null,L("Not while <S-NAME> <S-IS-ARE> fighting."));
			return false;
		}

		boolean abort=false;
		final Item[] cups=getCups(mob);
		if((!auto)&&(cups==null))
		{
			mob.tell(L("You'll need 2 unused glass cups, emptied of liquid, to start tapping a room."));
			abort=true;
		}
		final List<RawMaterial> line=getAvailableLine(mob);
		if((!auto)&&(line.size()==0))
		{
			mob.tell(L("You'll need several pounds of raw cloth material (like cotton or wool) to start tapping a room."));
			abort=true;
		}
		if(abort)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		final CMMsg msg=CMClass.getMsg(mob,target,this,auto?CMMsg.MASK_ALWAYS:CMMsg.MSG_DELICATE_HANDS_ACT,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,auto?"":L("<S-NAME> lay(s) down @x1 and <S-IS-ARE> ready to lay down a tap line.",(cups!=null?cups[0].name():"")));
		if((success)&&(mob.location().okMessage(mob,msg))&&((cups==null)||CMLib.commands().postDrop(mob,cups[0],true,false,false)))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,target,asLevel,0);

			final String code=""+System.currentTimeMillis()+Math.random();
			Thief_TapRoom TR=(Thief_TapRoom)copyOf();
			final int level=1+(adjustedLevel(mob,asLevel)/5)+(getXLEVELLevel(mob)*5);
			TR.setInvoker(mob);
			TR.setMiscText("SRC;"+CMLib.map().getExtendedRoomID(target)+";"+mob.Name()+";"+level+";"+code);
			if(cups!=null)
				cups[0].addNonUninvokableEffect(TR);
			TR=(Thief_TapRoom)copyOf();
			TR.setInvoker(mob);
			TR.setMiscText("DST;"+CMLib.map().getExtendedRoomID(target)+";"+mob.Name()+";"+level+";"+code);
			if(cups!=null)
				cups[1].addNonUninvokableEffect(TR);
			mob.tell(L("You should now walk to a listening room and put down the last cup.  Your skill will allow you to stretch the line @x1 rooms.",""+level));
			target.recoverRoomStats();
		}
		else
			return beneficialVisualFizzle(mob,target,auto?"":L("<S-NAME> fail(s) to tap this room."));
		return success;
	}
}
