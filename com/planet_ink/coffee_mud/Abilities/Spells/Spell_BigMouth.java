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

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.okMessage(myHost,msg);

		MOB mob=(MOB)affected;
		if((msg.amISource(mob))
		&&(msg.targetMinor()==CMMsg.TYP_EAT)
		&&(msg.target()!=null)
		&&(Stomach()!=null)
		&&(msg.target() instanceof MOB))
		{
			MOB target=(MOB)msg.target();
			if(target.envStats().weight()<(mob.envStats().weight()/3))
			{
				if((Stomach()!=null)&&(Stomach().numInhabitants()>(CMAble.qualifyingClassLevel(mob,this)-CMAble.qualifyingLevel(mob,this))))
				{
					mob.tell("Your stomach is too full.");
					return false;
				}
				boolean isHit=(Dice.normalizeAndRollLess(msg.source().adjustedAttackBonus(target)+target.adjustedArmor()));
				if(!isHit)
					mob.tell("You fail to eat "+target.name());
				else
					msg.modify(msg.source(),msg.target(),msg.tool(),
							  msg.sourceCode()|CMMsg.MASK_GENERAL,msg.sourceMessage(),
							  CMMsg.MSG_NOISYMOVEMENT,msg.targetMessage(),
							  msg.othersCode()|CMMsg.MASK_GENERAL,msg.othersMessage());
			}
			else
			{
				mob.tell(target.name()+" is too large to eat, even with the big mouth!");
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	private Room myStomach = null;
	private Room lastKnownRoom=null;
	private Room lastKnownLocation()
	{
		Room R=null;
		if(affected instanceof MOB) 
			R=((MOB)affected).location();
		if(R==null)R=CoffeeUtensils.roomLocation(affected);
		if(R!=null) lastKnownRoom=R;
		return lastKnownRoom;
	}
	private Room Stomach()
	{
		if((myStomach==null)&&(affected!=null))
		{
			myStomach = CMClass.getLocale("StdRoom");
			myStomach.setArea(CMMap.getRandomArea());
			myStomach.setName("The Stomach of "+affected.name());
			myStomach.setDescription("You are in the stomach of "+affected.name()+".  It is wet with digestive acids, and the walls are grinding you to a pulp.  You have been Swallowed whole and are being digested.");
		}
		return myStomach;
	}
	private int digestDown=4;

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
				lastKnownLocation().addItemRefuse(PartiallyDigestedItem,Item.REFUSE_PLAYER_DROP);
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
		MOB mob=(MOB)invoker();
		if((!mob.amDead())&&((--digestDown)<=0)&&(Stomach()!=null))
		{
			digestDown=2;
			for (int x=0;x<Stomach().numInhabitants();x++)
			{
				// ===== get a tasty morsel
				MOB TastyMorsel = Stomach().fetchInhabitant(x);
				if (TastyMorsel != null)
				{
					FullMsg DigestMsg=new FullMsg(mob,
											   TastyMorsel,
											   null,
											   CMMsg.MASK_GENERAL|CMMsg.TYP_ACID,
											   "<S-NAME> Digests <T-NAMESELF>!!");
					// no OKaffectS, since the dragon is not in his own stomach.
					Stomach().send(mob,DigestMsg);
					int damage=(int)Math.round(Util.div(TastyMorsel.curState().getHitPoints(),2));
					if(damage<(TastyMorsel.envStats().level()+6)) damage=TastyMorsel.curState().getHitPoints()+1;
					MUDFight.postDamage(mob,TastyMorsel,null,damage,CMMsg.MASK_GENERAL|CMMsg.TYP_ACID,Weapon.TYPE_MELTING,"The stomach acid <DAMAGE> <T-NAME>!");
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
		&&(msg.target() instanceof MOB)
		&&(Stomach()!=null)
		&&(msg.target().envStats().weight()<(mob.envStats().weight()/2)))
		{
			MOB TastyMorsel=(MOB)msg.target();
			Stomach().bringMobHere(TastyMorsel,false);
			FullMsg enterMsg=new FullMsg(TastyMorsel,Stomach(),null,CMMsg.MSG_ENTER,Stomach().description(),CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,"<S-NAME> slide(s) down the gullet into the stomach!");
			Stomach().send(TastyMorsel,enterMsg);
		}
		if((msg.amISource(mob))
		&&((msg.sourceMinor()==CMMsg.TYP_QUIT)||(msg.sourceMinor()==CMMsg.TYP_DEATH)))
			kill();

		super.executeMsg(myHost,msg);
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

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
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
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> invoke(s) a spell.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> feel(s) <S-HIS-HER> mouth grow to an enormous size!");
				beneficialAffect(mob,target,4);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke a spell, but fail(s) miserably.");

		// return whether it worked
		return success;
	}
}
