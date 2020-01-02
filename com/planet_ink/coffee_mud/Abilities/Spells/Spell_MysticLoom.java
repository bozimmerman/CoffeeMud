package com.planet_ink.coffee_mud.Abilities.Spells;
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
   Copyright 2018-2020 Bo Zimmerman

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
public class Spell_MysticLoom extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_MysticLoom";
	}

	private final static String localizedName = CMLib.lang().L("Mystic Loom");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Mystic Loom)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int maxRange()
	{
		return adjustedMaxInvokerRange(10);
	}

	@Override
	public int minRange()
	{
		return 1;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	@Override
	public int enchantQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ITEMS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_EVOCATION;
	}

	protected Item theLoom=null;
	protected MOB theLoomer=null;
	protected Ability skill = null;

	@Override
	public void unInvoke()
	{
		super.unInvoke();
		if(canBeUninvoked())
		{
			final Item theLoom=this.theLoom;
			final MOB theLoomer=this.theLoomer;
			final Ability skill=this.skill;
			if((theLoom!=null)
			&&(theLoom.owner()!=null)
			&&(theLoom.owner() instanceof Room)
			&&(((Room)theLoom.owner()).isContent(theLoom)))
			{
				final MOB actorM=(invoker!=null)? invoker : CMLib.map().deity();
				((Room)theLoom.owner()).show(actorM,null,CMMsg.MSG_OK_VISUAL,L("The mystical loom fades away..."));
				theLoom.destroy();
			}
			this.theLoom=null;
			if(theLoomer != null)
			{
				if(skill != null)
				{
					CMLib.threads().deleteTick(skill, Tickable.TICKID_MOB);
					final Ability A=theLoomer.fetchEffect(skill.ID());
					if(A!=null)
					{
						A.unInvoke();
						CMLib.threads().deleteTick(A, Tickable.TICKID_MOB);
					}
				}
				theLoomer.destroy();
				this.theLoomer=null;
			}
			if(skill != null)
				skill.destroy();
			this.skill=null;
		}
	}

	protected String removePercent(final String thisStr)
	{
		int x=thisStr.indexOf("% ");
		if(x>=0)
			return new StringBuffer(thisStr).replace(x,x+2,"").toString();
		x=thisStr.indexOf(" %");
		if(x>=0)
			return new StringBuffer(thisStr).replace(x,x+2,"").toString();
		x=thisStr.indexOf('%');
		if(x>=0)
			return new StringBuffer(thisStr).replace(x,x+1,"").toString();
		return thisStr;
	}


	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(tickID==Tickable.TICKID_MOB)
		{
			final Item theLoom=this.theLoom;
			if((theLoom != null)
			&&(theLoom.owner() instanceof Room)
			&&((skill==null)
				||((theLoomer != null)&&(theLoomer.fetchEffect(skill.ID())==null))))
			{
				final Room R=(Room)theLoom.owner();
				final int roomRange=1 + super.getXMAXRANGELevel(invoker());
				final List<Room> rooms;
				if(roomRange == 1)
					rooms = new XVector<Room>(R);
				else
				{
					final TrackingLibrary.TrackingFlags flags = CMLib.tracking().newFlags()
							.plus(TrackingLibrary.TrackingFlag.NOAIR)
							.plus(TrackingLibrary.TrackingFlag.OPENONLY);
					rooms = CMLib.tracking().getRadiantRooms(R, flags, roomRange);
				}
				final List<Triad<Item,ItemPossessor,Container>> cloths = new LinkedList<Triad<Item,ItemPossessor,Container>>();
				for(final Room R1 : rooms)
				{
					for(final Enumeration<Item> i=R1.items();i.hasMoreElements();)
					{
						final Item I=i.nextElement();
						if(I instanceof RawMaterial)
						{
							final Triad<Item,ItemPossessor,Container> t=new Triad<Item,ItemPossessor,Container>(I,R1,I.container());
							I.setContainer(null);
							if(I.owner() != R)
								R.moveItemTo(I);
							cloths.add(t);
						}
					}
					for(final Enumeration<MOB> m=R1.inhabitants();m.hasMoreElements();)
					{
						final MOB M=m.nextElement();
						if(M==null)
							continue;
						for(final Enumeration<Item> i=M.items();i.hasMoreElements();)
						{
							final Item I=i.nextElement();
							if(I instanceof RawMaterial)
							{
								final Triad<Item,ItemPossessor,Container> t=new Triad<Item,ItemPossessor,Container>(I,M,I.container());
								I.setContainer(null);
								if(I.owner() != R)
									R.moveItemTo(I);
								cloths.add(t);
							}
						}
					}
				}
				if(skill != null)
					CMLib.threads().deleteTick(skill, Tickable.TICKID_MOB);
				final Ability skill=CMClass.getAbility("Textiling");
				if(skill != null)
				{
					theLoomer.setLocation(R);
					final List<List<String>> recipes = ((CraftorAbility)skill).fetchRecipes();
					for(int i=0;i<10;i++)
					{
						final List<String> recipe = recipes.get(CMLib.dice().roll(1, recipes.size(), -1));
						final List<String> commands = new ArrayList<String>();
						commands.add(removePercent(recipe.get(0)));
						theLoomer.curState().setMovement(100);
						theLoomer.curState().setMana(100);
						theLoomer.curState().setHitPoints(20);
						skill.invoke(theLoomer, commands, null, true, 0);
						final Ability A=this.skill=theLoomer.fetchEffect(skill.ID());
						if(A!=null)
						{
							//System.out.println("GO: "+recipe.get(0));//BZ:COMMENTMEOUT
							int tickDown = CMath.s_int(A.getStat("TICKDOWN"));
							if(tickDown > 0)
							{
								final double quickPct = getXTIMELevel(invoker()) * 0.05;
								tickDown-=(int)Math.round(CMath.mul(tickDown, quickPct));
								if(tickDown < 5)
									tickDown=5;
								A.setStat("TICKDOWN", ""+tickDown);
							}
							CMLib.threads().startTickDown(A, Tickable.TICKID_MOB, 1);
							break;
						}
						else
						{
							//System.out.println("FAIL: "+recipe.get(0));//BZ:COMMENTMEOUT
						}
					}
				}
				final List<Item> spares=new LinkedList<Item>();
				for(final Enumeration<Item> i=R.items();i.hasMoreElements();)
				{
					final Item I=i.nextElement();
					if(I instanceof RawMaterial)
					{
						boolean found=false;
						for(final Triad<Item,ItemPossessor,Container> T : cloths)
						{
							if(T.first == I)
								found=true;
						}
						if(!found)
							spares.add(I);
					}
				}
				Triad<Item,ItemPossessor,Container> lastT=null;
				for(final Triad<Item,ItemPossessor,Container> T : cloths)
				{
					final ItemPossessor P;
					final Container C;
					final Item I;
					if(!T.first.amDestroyed())
					{
						C=T.third;
						P=T.second;
						I=T.first;
					}
					else
					if((lastT != null)
					&&(spares.size()>0))
					{
						I=spares.remove(0);
						C=lastT.third;
						P=lastT.second;
					}
					else
						continue;
					if((I.owner()!=P)
					||(!P.isContent(I)))
					{
						I.setContainer(C);
						if(I.owner()!=null)
							I.owner().delItem(I);
						P.addItem(I, ItemPossessor.Expire.Player_Drop);
					}
					lastT=T;
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		for(int i=0;i<mob.location().numItems();i++)
		{
			final Item I=mob.location().getItem(i);
			if((I!=null)&&(I.fetchEffect(ID())!=null))
			{
				mob.tell(L("There is already a mystic loom here."));
				return false;
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final Physical target = mob.location();

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{

			final CMMsg msg = CMClass.getMsg(mob, target, this,verbalCastCode(mob,target,auto),auto?L("A mystical loom appears!"):L("^S<S-NAME> evoke(s) a mystical loom!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final Item I=CMClass.getItem("GenItem");
				I.setName(L("a mystic loom"));
				I.setDisplayText(L("a mystical loom sits here"));
				I.setDescription(L("It's a loom for spinning cloth.  Also, it's mystical looking."));
				I.setMaterial(RawMaterial.RESOURCE_WOOD);
				I.basePhyStats().setDisposition(I.basePhyStats().disposition()|PhyStats.IS_BONUS);
				CMLib.flags().setGettable(I,false);
				I.recoverPhyStats();
				mob.location().addItem(I);
				theLoom=I;
				final MOB M=CMClass.getFactoryMOB();
				M.setName(L("a mystic loom"));
				M.basePhyStats().setLevel(adjustedLevel(mob,asLevel));
				M.phyStats().setLevel(adjustedLevel(mob,asLevel));
				M.setDisplayText(L("a mystical loom sits here"));
				M.setDescription(L("It's a loom for spinning cloth.  Also, it's mystical looking."));
				theLoomer=M;
				beneficialAffect(mob,I,asLevel,75 + super.adjustedLevel(mob, asLevel) + (30 * super.getXTIMELevel(mob)));
			}
		}
		else
			return beneficialWordsFizzle(mob,null,L("<S-NAME> incant(s), but the magic fizzles."));

		// return whether it worked
		return success;
	}
}
