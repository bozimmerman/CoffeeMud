package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Spell_DetectAmbush extends Spell
{
	public String ID() { return "Spell_DetectAmbush"; }
	public String name(){return "Detect Ambush";}
	public String displayText(){return "(Detecting Ambushes)";}
	public int quality(){ return OK_SELF;}
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Spell_DetectAmbush();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_DIVINATION;	}

	Room lastRoom=null;
	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		lastRoom=null;
		super.unInvoke();
		if(canBeUninvoked())
			mob.tell("You are no longer detecting ambushes.");
	}

	public boolean okAffect(Environmental myHost, Affect msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.okAffect(myHost,msg);
		MOB mob=(MOB)affected;
		if((msg.amISource(mob)
		&&(msg.targetMinor()==Affect.TYP_ENTER)
		&&(msg.target()!=null)
		&&(msg.target()!=lastRoom)
		&&(msg.target() instanceof Room)))
		{
			Room R=(Room)msg.target();
			boolean found=false;
			for(int m=0;m<R.numInhabitants();m++)
			{
				MOB M=R.fetchInhabitant(m);
				if(Sense.isHidden(M))
				{ found=true; break;}
				else
				for(int b=0;b<M.numBehaviors();b++)
				{
					Behavior B=M.fetchBehavior(b);
					if((B!=null)&&(B.grantsAggressivenessTo(M)))
					{ found=true; break;}
				}
			}
			lastRoom=R;
			mob.tell("Potential danger in that direction stops you for a second.");
			return false;
		}

		return super.okAffect(myHost,msg);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB)) 
			target=(MOB)givenTarget;
		if(target.fetchAffect(this.ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already detecting ambushes.");
			return false;
		}

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> gain(s) careful senses!":"^S<S-NAME> incant(s) softly, and gain(s) careful senses!^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> incant(s) and open(s) <S-HIS-HER> careful eyes, but the spell fizzles.");

		return success;
	}
}