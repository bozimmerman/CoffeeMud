package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_PolymorphSelf extends Spell
{
	public String ID() { return "Spell_PolymorphSelf"; }
	public String name(){return "Polymorph Self";}
	public String displayText(){return "(Polymorph Self)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Spell_PolymorphSelf();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_TRANSMUTATION;}

	Race newRace=null;

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(newRace!=null)
		{
			if(affected.name().indexOf(" ")>0)
				affectableStats.setName("a "+newRace.name()+" called "+affected.name());
			else
				affectableStats.setName(affected.name()+" the "+newRace.name());
			newRace.setHeightWeight(affectableStats,'M');
		}
	}
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(newRace!=null)
			affectableStats.setMyRace(newRace);
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();
		if(canBeUninvoked())
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> morph(s) back into <S-HIM-HERSELF> again.");
	}



	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((auto||mob.isMonster())&&((commands.size()<1)||(((String)commands.firstElement()).equals(mob.name()))))
		{
			commands.clear();
			Vector V=Util.denumerate(CMClass.races());
			for(int v=V.size()-1;v>=0;v--)
				if(((Race)V.elementAt(v)).availability()==Race.AVAILABLE_NONE)
					V.removeElementAt(v);
			if(V.size()>0)
				commands.addElement(((Race)V.elementAt(Dice.roll(1,V.size(),-1))).name());
		}
		if(commands.size()==0)
		{
			mob.tell("You need to specify what to turn yourself into!");
			return false;
		}
		String race=Util.combine(commands,0);
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		Race R=CMClass.getRace(race);
		if((R==null)||(R.availability()==Race.AVAILABLE_NONE))
		{
			mob.tell("You can't turn yourself into a '"+race+"'!");
			return false;
		}
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already polymorphed.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int mobStatTotal=0;
		for(int s=0;s<CharStats.NUM_BASE_STATS;s++)
			mobStatTotal+=mob.baseCharStats().getStat(s);

		MOB fakeMOB=CMClass.getMOB("StdMOB");
		for(int s=0;s<CharStats.NUM_BASE_STATS;s++)
			fakeMOB.baseCharStats().setStat(s,mob.baseCharStats().getStat(s));
		fakeMOB.baseCharStats().setMyRace(R);
		fakeMOB.recoverCharStats();
		fakeMOB.recoverEnvStats();
		fakeMOB.recoverMaxState();
		int fakeStatTotal=0;
		for(int s=0;s<CharStats.NUM_BASE_STATS;s++)
			fakeStatTotal+=fakeMOB.charStats().getStat(s);

		int statDiff=mobStatTotal-fakeStatTotal;
		boolean success=profficiencyCheck(mob,-(statDiff*5),auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> whisper(s) to <T-NAMESELF> about "+R.name()+"s.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					newRace=R;
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> become(s) a "+newRace.name()+"!");
					success=beneficialAffect(mob,target,0);
					target.recoverCharStats();
					target.confirmWearability();
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> whisper(s) to <T-NAMESELF>, but the spell fizzles.");

		// return whether it worked
		return success;
	}
}