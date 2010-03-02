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
public class Prayer_AuraDivineEdict extends Prayer
{
	public String ID() { return "Prayer_AuraDivineEdict"; }
	public String name(){ return "Aura of the Divine Edict";}
	public String displayText(){ return "(Edict Aura)";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_EVANGELISM;}
	public int abstractQuality(){ return Ability.QUALITY_BENEFICIAL_SELF;}
	protected int overrideMana(){return Integer.MAX_VALUE;}
	public long flags(){return Ability.FLAG_HOLY;}
    protected String godName="the gods";
    protected boolean noRecurse=false;


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if((canBeUninvoked())&&(mob.location()!=null)&&(!mob.amDead()))
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"The divine edict aura around <S-NAME> fades.");
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((affected==null)||(!(affected instanceof MOB))||(noRecurse))
			return true;

		if(CMath.bset(msg.sourceCode(),CMMsg.MASK_MALICIOUS)
		   ||CMath.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
		{
			msg.source().tell(godName+" DEMANDS NO FIGHTING!");
			msg.source().makePeace();
			return false;
		}
		else
		if((msg.source()==invoker())
		&&(msg.targetMinor()==CMMsg.TYP_SPEAK)
		&&(!CMath.bset(msg.sourceMajor(), CMMsg.MASK_ALWAYS))
		&&(msg.target() instanceof MOB)
		&&(msg.target().envStats().level()<invoker().envStats().level()+(super.getXLEVELLevel(invoker())*2))
		&&(msg.sourceMessage()!=null)
		&&(CMStrings.getSayFromMessage(msg.sourceMessage().toUpperCase()).equals(CMStrings.getSayFromMessage(msg.sourceMessage()))))
		{
			Vector V=CMParms.parse("ORDER \""+msg.target().Name()+"\" "+CMStrings.getSayFromMessage(msg.sourceMessage()));
			Object O=CMLib.english().findCommand((MOB)msg.target(),(Vector)V.clone());
			if((!((MOB)msg.target()).isMonster())
			&&(CMClass.classID(O).equalsIgnoreCase("DROP")
			   ||CMClass.classID(O).equalsIgnoreCase("SELL")
			   ||CMClass.classID(O).equalsIgnoreCase("GIVE")))
			{
			   msg.source().tell("The divine care not about such orders.");
			   return false;
			}
			noRecurse=true;
			String oldLiege=((MOB)msg.target()).getLiegeID();
			((MOB)msg.target()).setLiegeID(msg.source().Name());
			msg.source().doCommand(V,Command.METAFLAG_FORCED);
			((MOB)msg.target()).setLiegeID(oldLiege);
			noRecurse=false;
			return false;
		}
		noRecurse=false;
		return true;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected==null)||(!(affected instanceof Room)))
			return super.tick(ticking,tickID);

		if(!super.tick(ticking,tickID))
			return false;
		if(invoker()==null) return true;

		Room R=invoker().location();
		for(int i=0;i<R.numInhabitants();i++)
		{
			MOB M=R.fetchInhabitant(i);
			if((M!=null)&&(M.isInCombat()))
			{
				M.tell(invoker().getWorshipCharID().toUpperCase()+" DEMANDS NO FIGHTING!");
				M.makePeace();
			}
		}
		return true;
	}
	
    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(mob.isInCombat())
                return Ability.QUALITY_INDIFFERENT;
        }
        return super.castingQuality(mob,target);
    }
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(target,null,null,"The aura of the divine edict is already with <S-NAME>.");
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
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> "+prayWord(mob)+" for the aura of the divine edict.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				godName="THE GODS";
				if(mob.getWorshipCharID().length()>0)
					godName=mob.getWorshipCharID().toUpperCase();
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" for an aura of divine edict, but <S-HIS-HER> plea is not answered.");


		// return whether it worked
		return success;
	}
}
