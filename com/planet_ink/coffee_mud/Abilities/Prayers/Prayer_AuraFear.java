package com.planet_ink.coffee_mud.Abilities.Prayers;
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
public class Prayer_AuraFear extends Prayer
{
	public String ID() { return "Prayer_AuraFear"; }
	public String name(){ return "Aura of Fear";}
	public String displayText(){ return "(Fear Aura)";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_COMMUNING;}
	protected int canAffectCode(){return Ability.CAN_MOBS|Ability.CAN_ROOMS|Ability.CAN_ITEMS;}
	protected int canTargetCode(){return Ability.CAN_MOBS|Ability.CAN_ROOMS|Ability.CAN_ITEMS;}
	public int abstractQuality(){ return Ability.QUALITY_MALICIOUS;}
	public long flags(){return Ability.FLAG_UNHOLY;}

    public Prayer_AuraFear()
    {
        super();

        tickDown = 4;
    }


	public void unInvoke()
	{
		// undo the affects of this spell
		Room R=CMLib.map().roomLocation(affected);
		Environmental E=affected;

		super.unInvoke();

		if((canBeUninvoked())&&(R!=null)&&(E!=null))
			R.showHappens(CMMsg.MSG_OK_VISUAL,"The fearful aura around "+E.name()+" fades.");
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(affected==null)
			return super.tick(ticking,tickID);

		if((--tickDown)>=0)
		    return super.tick(ticking,tickID);
		tickDown=4;
		Room R=CMLib.map().roomLocation(affected);
		if(R==null)
		    return super.tick(ticking,tickID);

		HashSet H=null;
		if((invoker()!=null)&&(invoker().location()==R))
		{
			H=new HashSet();
			invoker().getGroupMembers(H);
			H.add(invoker());
		}
		if((affected instanceof MOB)&&(affected!=invoker()))
		{
		    if(H==null) H=new HashSet();
		    ((MOB)affected).getGroupMembers(H);
			H.add(affected);
		}
		for(int i=0;i<R.numInhabitants();i++)
		{
			MOB M=R.fetchInhabitant(i);
			MOB blame=((invoker!=null)&&(invoker!=M))?invoker:M;
			if((M!=null)&&((H==null)||(!H.contains(M))))
			{
			    if(CMLib.dice().rollPercentage()<M.charStats().getSave(CharStats.STAT_SAVE_MIND))
		            R.show(M,null,affected,CMMsg.MASK_EYES|CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> shudder(s) at the sight of <O-NAME>.");
			    else
			    {
				    // do that fear thing
				    // sit and cringe, or flee if mobile
				    if(M.isMonster())
				    {
				        if((!CMLib.flags().isMobile(M))||(!M.isInCombat()))
				        {
					        Command C=CMClass.getCommand("Sit");
					        try{if(C!=null) C.execute(M,CMParms.makeVector("Sit"),Command.METAFLAG_FORCED);}catch(Exception e){}
					        if(CMLib.flags().isSitting(M))
					        {
					            R.show(M,null,affected,CMMsg.MASK_EYES|CMMsg.MSG_HANDS|CMMsg.MASK_SOUND,"<S-NAME> cringe(s) in fear at the sight of <O-NAME>.");
					            Ability A=CMClass.getAbility("Spell_Fear");
					            if(A!=null) A.startTickDown(blame,M,Integer.MAX_VALUE/3);
					        }
				        }
				        else
					    if(M.isInCombat())
					    {
				            R.show(M,null,affected,CMMsg.MASK_EYES|CMMsg.MSG_NOISE,"<S-NAME> scream(s) in fear at the sight of <O-NAME>.");
					        Command C=CMClass.getCommand("Flee");
					        try{if(C!=null) C.execute(M,CMParms.makeVector("Flee"),Command.METAFLAG_FORCED);}catch(Exception e){}
					    }
					    else
					    {
				            R.show(M,null,affected,CMMsg.MASK_EYES|CMMsg.MSG_NOISE,"<S-NAME> scream(s) in fear at the sight of <O-NAME>.");
				            CMLib.tracking().beMobile(M,false,true,false,false,null,null);
					    }
				    }
				    else
				    {
					    if(M.isInCombat())
					    {
				            R.show(M,null,affected,CMMsg.MASK_EYES|CMMsg.MSG_NOISE,"<S-NAME> scream(s) in fear at the sight of <O-NAME>.");
					        Command C=CMClass.getCommand("Flee");
					        try{if(C!=null) C.execute(M,CMParms.makeVector("Flee"),Command.METAFLAG_FORCED);}catch(Exception e){}
					    }
					    else
					    {
				            R.show(M,null,affected,CMMsg.MASK_EYES|CMMsg.MSG_NOISE,"<S-NAME> scream(s) in fear at the sight of <O-NAME>.");
				            CMLib.tracking().beMobile(M,false,true,false,false,null,null);
				            if(M.location()==R)
				            {
					            R.show(M,null,affected,CMMsg.MASK_EYES|CMMsg.MSG_HANDS|CMMsg.MASK_SOUND,"<S-NAME> cringe(s) in fear at the sight of <O-NAME>.");
					            Ability A=CMClass.getAbility("Spell_Fear");
					            if(A!=null) A.startTickDown(blame,M,Integer.MAX_VALUE/3);
				            }
					    }
				    }
			    }
			}
		}
		return super.tick(ticking,tickID);
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Environmental target=getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_UNWORNONLY);
		if(target==null) return false;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell("The aura of fear is already surrounding "+target.name()+".");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
		    int affectType=verbalCastCode(mob,target,auto);
		    if((mob==target)&&(CMath.bset(affectType,CMMsg.MASK_MALICIOUS)))
		        affectType=CMath.unsetb(affectType,CMMsg.MASK_MALICIOUS);
			CMMsg msg=CMClass.getMsg(mob,target,this,affectType,auto?"":"^S<S-NAME> "+prayWord(mob)+" for an aura of fear to surround <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,"An aura descends over <T-NAME>!");
				maliciousAffect(mob,target,asLevel,0,-1);
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" for an aura of fear, but <S-HIS-HER> plea is not answered.");


		// return whether it worked
		return success;
	}
}


