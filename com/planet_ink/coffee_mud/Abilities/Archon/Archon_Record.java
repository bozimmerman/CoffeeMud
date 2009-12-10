package com.planet_ink.coffee_mud.Abilities.Archon;
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


import java.io.IOException;
import java.net.Socket;
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
public class Archon_Record extends ArchonSkill
{
	boolean doneTicking=false;
	public String ID() { return "Archon_Record"; }
	public String name(){ return "Record";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	private static final String[] triggerStrings = {"RECORD"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_ARCHON;}
	public int maxRange(){return adjustedMaxInvokerRange(1);}
	public int usageType(){return USAGE_MOVEMENT;}
	Session sess=null;

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
		{
			if(mob.session()==null)
				mob.setSession(null);
			else
			if(sess!=null)
				mob.session().stopBeingSnoopedBy(sess);
			sess=null;
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if(sess==null) return false;
		if((affected instanceof MOB)
		&&(((MOB)affected).session()!=null)
		&&(!(((MOB)affected).session().amBeingSnoopedBy(sess))))
			((MOB)affected).session().startBeingSnoopedBy(sess);
		return true;
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=CMLib.players().getLoadPlayer(CMParms.combine(commands,0));
		if(target==null) target=getTargetAnywhere(mob,commands,givenTarget,false,true,false);
		if(target==null) return false;

		Archon_Record A=(Archon_Record)target.fetchEffect(ID());
		if(A!=null)
		{
			target.delEffect(A);
			if(target.playerStats()!=null) target.playerStats().setLastUpdated(0);
			mob.tell(target.Name()+" will no longer be recorded.");
			return true;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MASK_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),"^F<S-NAME> begin(s) recording <T-NAMESELF>.^?");
            CMLib.color().fixSourceFightColor(msg);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				String filename="/"+target.Name()+System.currentTimeMillis()+".log";
				CMFile file=new CMFile(filename,null,true);
				if(!file.canWrite())
                {
                    if(!CMSecurity.isASysOp(mob)||(CMSecurity.isASysOp(target)))
    	                Log.sysOut("Record",mob.name()+" failed to start recording "+target.name()+".");
                }
				else
				{
                    if(!CMSecurity.isASysOp(mob)||(CMSecurity.isASysOp(target)))
    	                Log.sysOut("Record",mob.name()+" started recording "+target.name()+" to /"+filename+".");
					Archon_Record A2=(Archon_Record)copyOf();
					Session F=(Session)CMClass.getCommon("FakeSession");
                    F.initializeSession(null,filename);
                    if((target instanceof MOB)
                    &&(((MOB)target.session()==null)))
                		target.setSession(F);
					A2.sess=F;
	                target.addNonUninvokableEffect(A2);
	                mob.tell("Enter RECORD "+target.Name()+" again to stop recording.");
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,target,"<S-NAME> attempt(s) to hush <T-NAMESELF>, but fail(s).");
		return success;
	}
}