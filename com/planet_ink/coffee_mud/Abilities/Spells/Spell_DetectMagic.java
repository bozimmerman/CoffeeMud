package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Spell_DetectMagic extends Spell
{
	public String ID() { return "Spell_DetectMagic"; }
	public String name(){return "Detect Magic";}
	public String displayText(){return "(Detecting Magic)";}
	public int quality(){ return OK_SELF;}
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Spell_DetectMagic();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_DIVINATION;	}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> eyes cease to sparkle.");
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.target()!=null)
		&&(msg.amISource((MOB)affected))
		&&(msg.sourceMinor()==CMMsg.TYP_EXAMINESOMETHING)
		&&(Sense.canBeSeenBy(msg.target(),affected)))
		{
			String msg2=null;
			for(int a=0;a<msg.target().numEffects();a++)
			{
				Ability A=msg.target().fetchEffect(a);
				if((A!=null)
				&&(!A.isAutoInvoked())
				&&(A.displayText().length()>0)
				&&(((A.classificationCode()&Ability.ALL_CODES)==Ability.SPELL)
				   ||((A.classificationCode()&Ability.ALL_CODES)==Ability.PRAYER)
				   ||((A.classificationCode()&Ability.ALL_CODES)==Ability.SONG)
				   ||((A.classificationCode()&Ability.ALL_CODES)==Ability.CHANT)))
				{
					if(msg2==null)
						msg2=msg.target().name()+" is affected by: "+A.name();
					else
						msg2+=" "+A.name();
				}
			}
			if((msg2==null)&&(Sense.isABonusItems(msg.target())))
				msg2=msg.target().name()+" is enchanted";
			if(msg2!=null)
			{
				FullMsg msg3=new FullMsg(msg.source(),msg.target(),this,
										CMMsg.MSG_OK_VISUAL,msg2+".",
										msg.NO_EFFECT,null,
										msg.NO_EFFECT,null);

				msg.addTrailerMsg(msg3);
			}
		}
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE_BONUS);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already detecting magic.");
			return false;
		}

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> gain(s) sparkling eyes!":"^S<S-NAME> incant(s) softly, and gain(s) sparkling eyes!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> incant(s) and open(s) <S-HIS-HER> eyes sparkling, but the spell fizzles.");

		return success;
	}
}
