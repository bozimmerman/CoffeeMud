package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Chant_SenseMetal extends Chant
{
	public String ID() { return "Chant_SenseMetal"; }
	public String name(){return "Sense Metal";}
	public String displayText(){return "(Sensing Metal)";}
	public int quality(){ return OK_SELF;}
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Chant_SenseMetal();}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();
		if(canBeUninvoked())
			mob.tell("Your senses are no longer tuned to metals.");
	}
	
	public void executeMsg(Environmental host, CMMsg msg)
	{
		if((msg.source()==affected)
		&&(msg.targetMinor()==CMMsg.TYP_EXAMINESOMETHING)
		&&(msg.target() instanceof Room)
		&&(msg.tool()==null)
		&&(((((Room)msg.target()).myResource()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_METAL)
		   ||((((Room)msg.target()).myResource()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_MITHRIL)))
			msg.source().tell("You sense metals strongly in the earth here.");
		super.executeMsg(host,msg);
	}
	
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE_METAL);
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
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already sensing metals.");
			return false;
		}
		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> gain(s) metallic senses!":"^S<S-NAME> chant(s) softly, attaining metallic senses!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> chant(s) softly, but nothing happens.");

		return success;
	}
}