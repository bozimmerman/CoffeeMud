package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Grease extends Spell
{
    public final static int SIT = 0;
    public final static int FUMBLE_WEAPON = 1;
    public final static int BOTH = 2;
	public Spell_Grease()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Grease";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Covered in Grease)";

		canAffectCode=Ability.CAN_MOBS;
		canTargetCode=Ability.CAN_MOBS;
		

		quality=Ability.MALICIOUS;

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(3);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Grease();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_CONJURATION;
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		affectableStats.setStat(CharStats.DEXTERITY,affectableStats.getStat(CharStats.DEXTERITY)-4);
	}

	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

        FullMsg msg = null;
        Item weapon = null;

		MOB mob=(MOB)affected;

		if(affect.amISource(mob))
		{
			switch(affect.sourceMinor())
			{
			case Affect.TYP_LEAVE:
			case Affect.TYP_ENTER:
			case Affect.TYP_WEAPONATTACK:
			case Affect.TYP_FLEE:
				if(invoker()!=null)
				{
					if(Dice.rollPercentage()>(mob.charStats().getStat(CharStats.DEXTERITY)*4))
					{
                        int greaseEffect = (int) Math.round(Math.random()*3);
                        switch(greaseEffect)
                        {
                            case SIT:
						        msg=new FullMsg(mob,affect.source(),null,Affect.MSG_OK_ACTION,"<S-NAME> slip(s) and slide(s) around in the grease!");
						        mob.envStats().setDisposition(mob.envStats().disposition() | EnvStats.IS_SITTING);
								if(mob.location().okAffect(msg))
							        mob.location().send(mob,msg);
						        return false;
                            case FUMBLE_WEAPON:
                                weapon = (Item) mob.fetchWieldedItem();
								if((weapon!=null)&&(Dice.rollPercentage()>(mob.charStats().getStat(CharStats.DEXTERITY)*5))
								&&((weapon.rawProperLocationBitmap()==Item.WIELD)||(weapon.rawProperLocationBitmap()==Item.WIELD+Item.HELD)))
                                {
									msg=new FullMsg(mob,weapon,null,Affect.MSG_DROP,"<S-NAME> can't hold onto <S-HIS-HER> weapon since it's covered with grease.");
									if(mob.location().okAffect(msg))
									{
										weapon.remove();
										mob.location().send(mob,msg);
									}
                                }
						        return false;
                            case BOTH:
                                weapon = (Item) mob.fetchWieldedItem();
                                if(weapon != null)
						            msg=new FullMsg(mob,affect.source(),null,Affect.MSG_OK_ACTION,"<S-NAME> slip(s) and slide(s) around in the grease and lose(s) <S-HIS-HER> weapon.");
                                else
						            msg=new FullMsg(mob,affect.source(),null,Affect.MSG_OK_ACTION,"<S-NAME> slip(s) in the grease and fall(s) down.");
								if(mob.location().okAffect(msg))
								{
									mob.envStats().setDisposition(mob.envStats().disposition() | EnvStats.IS_SITTING);
									mob.location().send(mob,msg);
									if((weapon!=null)&&(Dice.rollPercentage()>(mob.charStats().getStat(CharStats.DEXTERITY)*4))
									&&((weapon.rawProperLocationBitmap()==Item.WIELD)||(weapon.rawProperLocationBitmap()==Item.WIELD+Item.HELD)))
									{
										msg=new FullMsg(mob,weapon,null,Affect.MSG_DROP,"<S-NAME> can't hold onto <S-HIS-HER> weapon since it's covered with grease.");
										if(mob.location().okAffect(msg))
										{
											weapon.remove();
											mob.location().send(mob,msg);
										}
									}
								}
						        return false;
                            default:
						        msg=new FullMsg(mob,affect.source(),null,Affect.MSG_OK_ACTION,"<S-NAME> slip(s) and slide(s) around in the grease!");
								if(mob.location().okAffect(msg))
								{
									mob.envStats().setDisposition(mob.envStats().disposition() | EnvStats.IS_SITTING);
									mob.location().send(mob,msg);
								}
						        return false;
                        }
					}
				}
				break;
			default:
				break;
			}
		}
		return true;
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();

		if(canBeUninvoked)
			mob.tell("You manage to work your way out of the grease.");
	}



	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

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
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"":"<S-NAME> invoke a spell at <T-NAME>s feet..",Affect.MSG_CAST_ATTACK_VERBAL_SPELL,auto?"":"<S-NAME> invoke(s) a spell at your feet.",affectType,auto?"":"<S-NAME> invokes a spell at <T-NAME>s feet");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					if(target.location()==mob.location())
					{
						target.location().show(target,null,Affect.MSG_OK_ACTION,"<S-NAME> begin(s) to slip and slide!");
						success=maliciousAffect(mob,target,0,-1);
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) at <T-NAMESELF>, but the spell fizzles.");

		// return whether it worked
		return success;
	}
}
