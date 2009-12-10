package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Spell_BigMouth extends Spell
{
	public String ID() { return "Spell_BigMouth"; }
	public String name(){return "Big Mouth";}
	public String displayText(){return "(Big Mouth)";}
	public int abstractQuality(){return Ability.QUALITY_OK_SELF;}
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_TRANSMUTATION;	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.okMessage(myHost,msg);

		MOB mob=(MOB)affected;
		if((msg.amISource(mob))
		&&(msg.targetMinor()==CMMsg.TYP_EAT)
		&&(msg.target()!=null)
		&&(Stomach()!=null))
		{
			if(msg.target().envStats().weight()<(mob.envStats().weight()/3))
			{
				if((Stomach()!=null)&&(Stomach().numInhabitants()>(CMLib.ableMapper().qualifyingClassLevel(mob,this)-CMLib.ableMapper().qualifyingLevel(mob,this))))
				{
					mob.tell("Your stomach is too full.");
					return false;
				}

				if(msg.target() instanceof MOB)
				{
					MOB target=(MOB)msg.target();
					boolean isHit=CMLib.combat().rollToHit(msg.source(),target);
					if(!isHit)
					{
						mob.tell("You fail to eat "+target.name());
						return false;
					}
				}
				else
				if(msg.target() instanceof Food)
					return super.okMessage(myHost,msg);
				else
				if(!(msg.target() instanceof Item))
					return super.okMessage(myHost,msg);
				else
				if((!CMLib.flags().isGettable((Item)msg.target()))||(msg.target().displayText().length()==0))
				{
					mob.tell("You can not eat "+msg.target().name());
					return false;
				}

				msg.modify(msg.source(),msg.target(),msg.tool(),
						  msg.sourceCode()|CMMsg.MASK_ALWAYS,msg.sourceMessage(),
						  CMMsg.MSG_NOISYMOVEMENT,msg.targetMessage(),
						  msg.othersCode()|CMMsg.MASK_ALWAYS,msg.othersMessage());
			}
			else
			{
				mob.tell(msg.target().name()+" is just too large for you to eat!");
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
		{
			super.executeMsg(myHost,msg);
			return;
		}

		MOB mob=(MOB)affected;

		if((msg.amISource(mob))
		&&(msg.sourceMinor()==CMMsg.TYP_EAT)
		&&(msg.target()!=null)
		&&(Stomach()!=null)
		&&(msg.target().envStats().weight()<(mob.envStats().weight()/2)))
		{
			if(msg.target() instanceof MOB)
			{
				MOB TastyMorsel=(MOB)msg.target();
				Stomach().bringMobHere(TastyMorsel,false);
				CMMsg enterMsg=CMClass.getMsg(TastyMorsel,Stomach(),null,CMMsg.MSG_ENTER,"<S-NAME> <S-IS-ARE> swallowed whole by "+mob.name()+"!",CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,"<S-NAME> slide(s) down the gullet into the stomach!");
				Stomach().send(TastyMorsel,enterMsg);
			}
			if((msg.target() instanceof Item)
			&&(!(msg.target() instanceof Food)))
				Stomach().bringItemHere((Item)msg.target(),CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_MONSTER_EQ),false);
		}
		if((msg.amISource(mob))
		&&((msg.sourceMinor()==CMMsg.TYP_QUIT)||(msg.sourceMinor()==CMMsg.TYP_DEATH)))
			kill();

		super.executeMsg(myHost,msg);
	}

	protected Room myStomach = null;
	protected Room lastKnownRoom=null;
	protected Room lastKnownLocation()
	{
		Room R=null;
		if(affected instanceof MOB)
			R=((MOB)affected).location();
		if(R==null)R=CMLib.map().roomLocation(affected);
		if(R!=null) lastKnownRoom=R;
		return lastKnownRoom;
	}
	protected Room Stomach()
	{
		if((myStomach==null)&&(affected!=null))
		{
			myStomach = CMClass.getLocale("StdRoom");
			myStomach.setArea(CMLib.map().getRandomArea());
			myStomach.setName("The Stomach of "+affected.name());
			myStomach.setDescription("You are in the stomach of "+affected.name()+".  It is wet with digestive acids, and the walls are grinding you to a pulp.  You have been swallowed whole and are being digested.");
		}
		return myStomach;
	}
	protected int digestDown=4;

	public void kill()
	{
		if((Stomach()==null)||(lastKnownLocation()==null))
		   return;

		// ===== move all inhabitants to the dragons location
		// ===== loop through all inhabitants of the stomach
		int morselCount = Stomach().numInhabitants();
		for (int x=morselCount-1;x>=0;x--)
		{
			// ===== get the tasty morsels
			MOB TastyMorsel = Stomach().fetchInhabitant(x);
			if(TastyMorsel!=null)
				lastKnownLocation().bringMobHere(TastyMorsel,false);
		}

		// =====move the inventory of the stomach to the room
		int itemCount = Stomach().numItems();
		for (int y=itemCount-1;y>=0;y--)
		{
			Item PartiallyDigestedItem = Stomach().fetchItem(y);
			if (PartiallyDigestedItem!=null)
			{
				lastKnownLocation().addItemRefuse(PartiallyDigestedItem,CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_PLAYER_DROP));
				Stomach().delItem(PartiallyDigestedItem);
			}
		}
		if((morselCount>0)||(itemCount>0))
			lastKnownLocation().recoverRoomStats();
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if(invoker()==null) return true;
		MOB mob=invoker();
		if((!mob.amDead())&&((--digestDown)<=0)&&(Stomach()!=null))
		{
			digestDown=2;
			for (int x=0;x<Stomach().numInhabitants();x++)
			{
				// ===== get a tasty morsel
				MOB TastyMorsel = Stomach().fetchInhabitant(x);
				if (TastyMorsel != null)
				{
					CMMsg DigestMsg=CMClass.getMsg(mob,
											   TastyMorsel,
											   null,
											   CMMsg.MASK_ALWAYS|CMMsg.TYP_ACID,
											   "<S-NAME> digest(s) <T-NAMESELF>!!");
					// no OKaffectS, since the dragon is not in his own stomach.
					Stomach().send(mob,DigestMsg);
					int damage=(int)Math.round(CMath.div(TastyMorsel.curState().getHitPoints(),2));
					if(damage<(TastyMorsel.envStats().level()+6)) damage=TastyMorsel.curState().getHitPoints()*100;
					CMLib.combat().postDamage(mob,TastyMorsel,null,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_ACID,Weapon.TYPE_MELTING,"The stomach acid <DAMAGE> <T-NAME>!");
				}
			}
		}
		else
		if(mob.amDead())
			kill();

		if((affected instanceof Room)
		&&(((Room)affected).numInhabitants()==0))
			unInvoke();
		return true;
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected==null) return;

		Environmental thang=affected;
		super.unInvoke();

		if(canBeUninvoked())
		{
			if(thang instanceof MOB)
			{
				((MOB)thang).tell("Your mouth shrinks to normal size.");
				if((Stomach()!=null)&&(Stomach().numInhabitants()>0))
				{
					unInvoked=false;
					Spell_BigMouth A =(Spell_BigMouth)this.copyOf();
					A.startTickDown(invoker,Stomach(),10000);
				}
			}
			else
			if(thang instanceof Room)
				kill();
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if(target==null) return false;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already the owner of a huge mouth.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> invoke(s) a spell.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> feel(s) <S-HIS-HER> mouth grow to an enormous size!");
				beneficialAffect(mob,target,asLevel,4);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke a spell, but fail(s) miserably.");

		// return whether it worked
		return success;
	}
}
