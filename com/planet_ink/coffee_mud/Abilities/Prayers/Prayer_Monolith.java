package com.planet_ink.coffee_mud.Abilities.Prayers;
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
   Copyright 2003-2018 Bo Zimmerman

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

public class Prayer_Monolith extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_Monolith";
	}

	private final static String localizedName = CMLib.lang().L("Monolith");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Monolith)");

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
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_CREATION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
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
	public long flags()
	{
		return Ability.FLAG_NEUTRAL;
	}

	private final static int TYP_ICE=0;
	private final static int TYP_FIRE=1;
	private final static int TYP_EARTH=2;
	private final static int TYP_AIR=3;

	protected int wallType=0;
	protected int amountRemaining=0;
	protected Item theWall=null;

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof Item)))
			return true;

		final MOB mob=msg.source();
		switch(wallType)
		{
		case TYP_ICE:
			if((invoker!=null)
			&&(mob.isInCombat())
			&&(mob.getVictim()==invoker)
			&&(mob.rangeToTarget()==1))
			{
				if(msg.sourceMinor()==CMMsg.TYP_ADVANCE)
				{
					Item w=mob.fetchWieldedItem();
					if(w==null)
						w=mob.getNaturalWeapon();
					if(w==null)
						return false;
					final Room room=mob.location();
					final CMMsg msg2=CMClass.getMsg(mob,null,CMMsg.MSG_WEAPONATTACK,L("^F^<FIGHT^><S-NAME> hack(s) at the monolith of ice with @x1.^</FIGHT^>^?",w.name()));
					CMLib.color().fixSourceFightColor(msg2);
					if(room.okMessage(mob,msg2))
					{
						room.send(mob,msg2);
						amountRemaining-=mob.phyStats().damage();
						if(amountRemaining<0)
						{
							for(int i=0;i<room.numInhabitants();i++)
							{
								final MOB M=room.fetchInhabitant(i);
								if((M.isInCombat())
								&&(M.getVictim()==invoker)
								&&(M.rangeToTarget()>0)
								&&(M.rangeToTarget()<3)
								&&(!M.amDead()))
								{
									final MOB invoker=(invoker()!=null) ? invoker() : M;
									CMLib.combat().postDamage(invoker,M,this,CMLib.dice().roll(M.phyStats().level()/2,4,0),CMMsg.MASK_ALWAYS|CMMsg.MASK_MALICIOUS|CMMsg.TYP_COLD,Weapon.TYPE_PIERCING,L("A shard of ice <DAMAGE> <T-NAME>!"));
								}
							}
							mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The monolith of ice shatters!!!"));
							((Item)affected).destroy();
						}
					}
					return false;
				}
			}
			break;
		case TYP_FIRE:
			break;
		case TYP_AIR:
			if((invoker!=null)
			&&(mob.isInCombat())
			&&(mob.getVictim()==invoker)
			&&(mob.rangeToTarget()>=1)
			&&(msg.amITarget(invoker))
			&&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
			&&(msg.tool() instanceof Weapon)
			&&(!((Weapon)msg.tool()).amWearingAt(Wearable.IN_INVENTORY))
			&&(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_RANGED))
			{
				mob.location().show(mob,invoker,CMMsg.MSG_OK_VISUAL,L("<S-NAME> fire(s) @x1 at <T-NAME>.  The missile enters the monolith of air.",msg.tool().name()));
				final MOB M=CMClass.getFactoryMOB();
				M.setLocation(mob.location());
				M.setName(L("The monolith of air"));
				M.setVictim(mob);
				M.setRangeToTarget(mob.rangeToTarget());
				CMLib.combat().postWeaponAttackResult(M,mob,(Weapon)msg.tool(),true);
				M.setLocation(null);
				M.setVictim(null);
				if(mob.isMonster())
					CMLib.commands().postRemove(mob,(Weapon)msg.tool(),true);
				M.destroy();
				return false;
			}
			break;
		case TYP_EARTH:
			if((invoker!=null)
			&&(mob.isInCombat())
			&&(mob.getVictim()==invoker)
			&&(mob.rangeToTarget()==1))
			{
				if(msg.sourceMinor()==CMMsg.TYP_ADVANCE)
				{
					Item w=mob.fetchWieldedItem();
					if(w==null)
						w=mob.getNaturalWeapon();
					if(w==null)
						return false;
					if(mob.location().show(mob,null,w,CMMsg.MSG_WEAPONATTACK,L("^F^<FIGHT^><S-NAME> hack(s) at the monolith of stone with <O-NAME>.^</FIGHT^>^?")))
					{
						amountRemaining-=mob.phyStats().damage();
						if(amountRemaining<0)
						{
							mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The monolith of stone is destroyed!"));
							((Item)affected).destroy();
						}
					}
					return false;
				}
			}
			break;
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public void unInvoke()
	{
		super.unInvoke();
		if(canBeUninvoked())
		{
			if((theWall!=null)
			&&(invoker!=null)
			&&(theWall.owner()!=null)
			&&(theWall.owner() instanceof Room)
			&&(((Room)theWall.owner()).isContent(theWall)))
			{
				MOB actorM=invoker; if(actorM==null) actorM=CMLib.map().deity();
				switch(wallType)
				{
				case TYP_FIRE:
					((Room)theWall.owner()).show(actorM,null,CMMsg.MSG_OK_VISUAL,L("The monolith of fire fades."));
					break;
				case TYP_AIR:
					((Room)theWall.owner()).show(actorM,null,CMMsg.MSG_OK_VISUAL,L("The monolith of air dissipates."));
					break;
				case TYP_ICE:
					((Room)theWall.owner()).show(actorM,null,CMMsg.MSG_OK_VISUAL,L("The monolith of ice melts."));
					break;
				case TYP_EARTH:
					((Room)theWall.owner()).show(actorM,null,CMMsg.MSG_OK_VISUAL,L("The monolith of stone crumbles."));
					break;
				}
				final Item wall=theWall;
				theWall=null;
				wall.destroy();
			}
		}
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Tickable.TICKID_MOB)
		{
			switch(wallType)
			{
			case TYP_ICE:
			case TYP_EARTH:
			case TYP_AIR:
				if((invoker!=null)
				   &&(theWall!=null)
				   &&(invoker.location()!=null)
				   &&(!invoker.location().isContent(theWall)))
					unInvoke();
				break;
			case TYP_FIRE:
				if((invoker!=null)
				   &&(theWall!=null)
				   &&(invoker.location()!=null))
				{
					final Room room=invoker.location();
					if(!invoker.location().isContent(theWall))
						unInvoke();
					else
					for(int m=0;m<room.numInhabitants();m++)
					{
						final MOB mob=room.fetchInhabitant(m);
						if((mob!=null)
						&&(mob!=invoker)
						&&(mob.isInCombat())
						&&(mob.getVictim()==invoker)
						&&(mob.rangeToTarget()==1))
						{
							final int damage = CMLib.dice().roll((int)Math.round((adjustedLevel(invoker,0)+(2.0*super.getX1Level(invoker())))/4.0),6,1);
							CMLib.combat().postDamage(invoker,mob,this,damage,CMMsg.MASK_MALICIOUS|CMMsg.MASK_ALWAYS|CMMsg.TYP_FIRE,Weapon.TYPE_BURNING,L("The monolith of fire flares and <DAMAGE> <T-NAME>!"));
						}
					}
				}
				break;
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((!mob.isInCombat())||(mob.rangeToTarget()<1))
		{
			mob.tell(L("You really should be in ranged combat to cast this."));
			return false;
		}
		for(int i=0;i<mob.location().numItems();i++)
		{
			final Item I=mob.location().getItem(i);
			if((I!=null)&&(I.fetchEffect(ID())!=null))
			{
				mob.tell(L("There is already a monolith here."));
				return false;
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final Physical target = mob.location();

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{

			wallType=CMLib.dice().roll(1,4,-1);
			final String text=text().toUpperCase().trim();
			if((text.indexOf("STONE")>=0)||(text.indexOf("EARTH")>=0))
				wallType=TYP_EARTH;
			else
			if((text.indexOf("ICE")>=0)||(text.indexOf("WATER")>=0))
				wallType=TYP_ICE;
			else
			if(text.indexOf("AIR")>=0)
				wallType=TYP_AIR;
			else
			if(text.indexOf("FIRE")>=0)
				wallType=TYP_FIRE;
			Item I=null;
			switch(wallType)
			{
			case TYP_EARTH:
				amountRemaining=mob.baseState().getHitPoints()/6;
				I=CMClass.getItem("GenItem");
				I.setName(L("a monolith of stone"));
				I.setDisplayText(L("a monolith of stone has been erected here"));
				I.setDescription(L("The bricks are sold and sturdy."));
				I.setMaterial(RawMaterial.RESOURCE_STONE);
				break;
			case TYP_ICE:
				amountRemaining=20;
				I=CMClass.getItem("GenItem");
				I.setName(L("a monolith of ice"));
				I.setDisplayText(L("a monolith of ice has been erected here"));
				I.setDescription(L("The ice is crystal clear."));
				I.setMaterial(RawMaterial.RESOURCE_GLASS);
				break;
			case TYP_AIR:
				I=CMClass.getItem("GenItem");
				I.setName(L("a monolith of air"));
				I.setDisplayText("");
				I.setDescription(L("The air is swirling dangerously."));
				I.setMaterial(RawMaterial.RESOURCE_NOTHING);
				break;
			case TYP_FIRE:
				I=CMClass.getItem("GenItem");
				I.setName(L("a monolith of fire"));
				I.setDisplayText(L("a monolith of fire is burning here"));
				I.setDescription(L("The flames are high and hot."));
				I.setMaterial(RawMaterial.RESOURCE_NOTHING);
				I.basePhyStats().setDisposition(I.basePhyStats().disposition()|PhyStats.IS_LIGHTSOURCE);
				break;
			}
			if(I!=null)
			{
				final CMMsg msg = CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("@x1 appears!",I.name()):L("^S<S-NAME> @x1 to construct @x2!^?",prayForWord(mob),I.name()));
				if(mob.location().okMessage(mob,msg))
				{
					mob.location().send(mob,msg);
					CMLib.flags().setGettable(I,false);
					I.recoverPhyStats();
					mob.location().addItem(I);
					theWall=I;
					beneficialAffect(mob,I,asLevel,0);
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,null,L("<S-NAME> @x1, but nothing happens.",prayWord(mob)));

		// return whether it worked
		return success;
	}
}
