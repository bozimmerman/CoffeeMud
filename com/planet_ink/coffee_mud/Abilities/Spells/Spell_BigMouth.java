package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_BigMouth extends Spell
{
	public String ID() { return "Spell_BigMouth"; }
	public String name(){return "Big Mouth";}
	public String displayText(){return "(Big Mouth)";}
	public int quality(){return OK_SELF;};
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance()	{ return new Spell_BigMouth();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_TRANSMUTATION;	}

	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.okAffect(affect);

		MOB mob=(MOB)affected;
		if((affect.amISource(mob))
		&&(affect.targetMinor()==Affect.TYP_EAT)
		&&(affect.target()!=null)
		&&(Stomach!=null)
		&&(affect.target() instanceof MOB))
		{
			MOB target=(MOB)affect.target();
			if(target.envStats().weight()<(mob.envStats().weight()/2))
			{
				boolean isHit=(CoffeeUtensils.normalizeAndRollLess(affect.source().adjustedAttackBonus()+target.adjustedArmor()));
				if(!isHit)
					mob.tell("You fail to eat "+target.name());
				else
					affect.modify(affect.source(),affect.target(),affect.tool(),
							  affect.sourceCode()|Affect.MASK_GENERAL,affect.sourceMessage(),
							  Affect.MSG_NOISYMOVEMENT,affect.targetMessage(),
							  affect.othersCode()|Affect.MASK_GENERAL,affect.othersMessage());
			}
			else
			{
				mob.tell(target.name()+" is too large to eat, even with the big mouth!");
				return false;
			}
		}
		return super.okAffect(affect);
	}
	
	private Room Stomach = null;
	private int digestDown=4;
	private Room lastKnownLocation=null;

	public void kill()
	{
		if(lastKnownLocation==null) return;
		if(Stomach==null) return;

		// ===== move all inhabitants to the dragons location
		// ===== loop through all inhabitants of the stomach
		int morselCount = Stomach.numInhabitants();
		for (int x=morselCount-1;x>=0;x--)
		{
			// ===== get the tasty morsels
			MOB TastyMorsel = Stomach.fetchInhabitant(x);
			if(TastyMorsel!=null)
				lastKnownLocation.bringMobHere(TastyMorsel,false);
		}

		// =====move the inventory of the stomach to the room
		int itemCount = Stomach.numItems();
		for (int y=itemCount-1;y>=0;y--)
		{
			Item PartiallyDigestedItem = Stomach.fetchItem(y);
			if (PartiallyDigestedItem!=null)
			{
				lastKnownLocation.addItemRefuse(PartiallyDigestedItem,Item.REFUSE_PLAYER_DROP);
				Stomach.delItem(PartiallyDigestedItem);
			}
		}
		if((morselCount>0)||(itemCount>0))
		{
			lastKnownLocation.recoverRoomStats();
		}
		lastKnownLocation=null;
	}

	public boolean tick(int tickID)
	{
		if(!super.tick(tickID)) return false;
		if(invoker()==null) return true;
		MOB mob=(MOB)invoker();
		if(mob.location()!=null)
			lastKnownLocation=mob.location();
		if((!mob.amDead())&&((--digestDown)<=0)&&(Stomach!=null))
		{
			digestDown=2;
			for (int x=0;x<Stomach.numInhabitants();x++)
			{
				// ===== get a tasty morsel
				MOB TastyMorsel = Stomach.fetchInhabitant(x);
				if (TastyMorsel != null)
				{
					FullMsg DigestMsg=new FullMsg(mob,
											   TastyMorsel,
											   null,
											   Affect.MASK_GENERAL|Affect.TYP_ACID,
											   "<S-NAME> Digests <T-NAMESELF>!!");
					// no OKaffectS, since the dragon is not in his own stomach.
					Stomach.send(mob,DigestMsg);
					int damage=(int)Math.round(Util.div(TastyMorsel.curState().getHitPoints(),2));
					if(damage<(TastyMorsel.envStats().level()+6)) damage=TastyMorsel.curState().getHitPoints()+1;
					ExternalPlay.postDamage(mob,TastyMorsel,null,damage,Affect.MASK_GENERAL|Affect.TYP_ACID,Weapon.TYPE_MELTING,"The stomach acid <DAMAGE> <T-NAME>!");
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

	public void affect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
		{
			super.affect(affect);
			return;
		}

		MOB mob=(MOB)affected;

		if((affect.amISource(mob))
		&&(affect.sourceMinor()==Affect.TYP_EAT)
		&&(affect.target()!=null)
		&&(affect.target() instanceof MOB)
		&&(Stomach!=null)
		&&(affect.target().envStats().weight()<(mob.envStats().weight()/2)))
		{
			lastKnownLocation=mob.location();
			MOB TastyMorsel=(MOB)affect.target();
			Stomach.bringMobHere(TastyMorsel,false);
			FullMsg enterMsg=new FullMsg(TastyMorsel,Stomach,null,Affect.MSG_ENTER,Stomach.description(),Affect.MSG_ENTER,null,Affect.MSG_ENTER,"<S-NAME> slide(s) down the gullet into the stomach!");
			Stomach.send(TastyMorsel,enterMsg);
		}
		if((affect.amISource(mob))
		&&((affect.sourceMinor()==Affect.TYP_QUIT)||(affect.sourceMinor()==Affect.TYP_DEATH)))
			kill();
		
		super.affect(affect);
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
				if((Stomach!=null)&&(Stomach.numInhabitants()>0))
				{
					unInvoked=false;
					Spell_BigMouth A =(Spell_BigMouth)this.copyOf();
					A.startTickDown(Stomach,10000);
				}
			}
			else
			if(thang instanceof Room)
				kill();
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=mob;
		if(target==null) return false;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB)) 
			target=(MOB)givenTarget;
		
		if(Stomach==null)
		{
			Stomach = CMClass.getLocale("StdRoom");
			lastKnownLocation=mob.location();
			if(lastKnownLocation!=null)
				Stomach.setArea(lastKnownLocation.getArea());
			Stomach.setName("The Stomach of "+mob.name());
			Stomach.setDescription("You are in the stomach of "+mob.name()+".  It is wet with digestive acids, and the walls are grinding you to a pulp.  You have been Swallowed whole and are being digested.");
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> invoke(s) a spell.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> feel(s) <S-HIS-HER> mouth grow to an enormous size!");
				beneficialAffect(mob,target,4);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke a spell, but fail(s) miserably.");

		// return whether it worked
		return success;
	}
}
