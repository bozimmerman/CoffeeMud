package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class Spell_Grease extends Spell
	implements ConjurationDevotion
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


		malicious=true;

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(3);

		addQualifyingClass(new Mage().ID(),3);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Grease();
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		affectableStats.setDexterity(affectableStats.getDexterity()-4);
	}

	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

        FullMsg msg = null;
        Item weapon = null;

		MOB mob=(MOB)affected;

		if(affect.amITarget(mob))
		{
			switch(affect.targetType())
			{
			case Affect.MOVE:
				if(invoker()!=null)
				{
					int pctDodge=mob.charStats().getDexterity();
					if(Dice.rollPercentage()<pctDodge)
					{
                        int greaseEffect = (int) Math.round(Math.random()*3);
                        switch(greaseEffect)
                        {
                            case SIT:
						        msg=new FullMsg(mob,affect.source(),null,Affect.VISUAL_WNOISE,Affect.VISUAL_WNOISE,Affect.VISUAL_WNOISE,"<S-NAME> slip(s) and slide(s) around in the grease!");
						        mob.envStats().setDisposition(mob.envStats().disposition() | Sense.IS_SITTING);
						        mob.location().send(mob,msg);
						        return false;
                            case FUMBLE_WEAPON:
                                weapon = (Item) mob.fetchWieldedItem();
								if((weapon!=null)&&(Dice.rollPercentage()>(mob.charStats().getDexterity()*4)))
                                {
									msg=new FullMsg(mob,weapon,null,Affect.HANDS_DROP,Affect.HANDS_DROP,Affect.VISUAL_WNOISE,"<S-NAME> can't hold onto <S-HIS-HER> weapon since it's covered with grease.");
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
						            msg=new FullMsg(mob,affect.source(),null,Affect.VISUAL_WNOISE,Affect.VISUAL_WNOISE,Affect.VISUAL_WNOISE,"<S-NAME> slip(s) and slide(s) around in the grease and loses <S-HIS-HER> weapon.");
                                else
						            msg=new FullMsg(mob,affect.source(),null,Affect.VISUAL_WNOISE,Affect.VISUAL_WNOISE,Affect.VISUAL_WNOISE,"<S-NAME> slip(s) in the grease and fall(s) down.");
								if(mob.location().okAffect(msg))
								{
									mob.envStats().setDisposition(mob.envStats().disposition() | Sense.IS_SITTING);
									mob.location().send(mob,msg);
									if((weapon!=null)&&(Dice.rollPercentage()>(mob.charStats().getDexterity()*4)))
									{
										msg=new FullMsg(mob,weapon,null,Affect.HANDS_DROP,Affect.HANDS_DROP,Affect.VISUAL_WNOISE,"<S-NAME> can't hold onto <S-HIS-HER> weapon since it's covered with grease.");
										if(mob.location().okAffect(msg))
										{
											weapon.remove();
											mob.location().send(mob,msg);
										}
									}
								}
						        return false;
                            default:
						        msg=new FullMsg(mob,affect.source(),null,Affect.VISUAL_WNOISE,Affect.VISUAL_WNOISE,Affect.VISUAL_WNOISE,"<S-NAME> slip(s) and slide(s) around in the grease!");
								if(mob.location().okAffect(msg))
								{
									mob.envStats().setDisposition(mob.envStats().disposition() | Sense.IS_SITTING);
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

		mob.tell("You manage to work your way out of the grease.");
	}



	public boolean invoke(MOB mob, Vector commands)
	{
		MOB target=this.getTarget(mob,commands);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.STRIKE_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> invoke(s) a spell at the feet of <T-NAME>.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					mob.location().show(mob,target,Affect.VISUAL_WNOISE,"<T-NAME> begin(s) to slip and slide!");
					success=maliciousAffect(mob,target,0,-1);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) to the feet of <T-NAME>, but the spell fizzles.");

		// return whether it worked
		return success;
	}
}
