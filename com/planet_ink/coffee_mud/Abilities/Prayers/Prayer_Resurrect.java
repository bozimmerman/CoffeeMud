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
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine;
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
public class Prayer_Resurrect extends Prayer implements MendingSkill
{
	public String ID() { return "Prayer_Resurrect"; }
	public String name(){ return "Resurrect";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_DEATHLORE;}
	public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}
	public long flags(){return Ability.FLAG_HOLY;}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}

	public boolean supportsMending(Environmental E)
	{ 
		return (E instanceof DeadBody);
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Environmental body=null;
        body=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_UNWORNONLY);
        DatabaseEngine.PlayerData nonPlayerData=null;
        boolean playerCorpse=false;
        if((body==null)&&(CMSecurity.isASysOp(mob)))
        {
            Vector V=CMLib.database().DBReadData("HEAVEN");
            Vector allObjs=new Vector();
            Vector allDataPs=new Vector();
            if((V!=null)&&(V.size()>0))
            for(int v=0;v<V.size();v++)
            {
            	DatabaseEngine.PlayerData dataP=(DatabaseEngine.PlayerData)V.elementAt(v);
                String data=dataP.xml;
                Environmental obj=parseHeavenlyData(data);
                if(obj!=null)
                {
                    allDataPs.addElement(dataP);
                    allObjs.addElement(obj);
                }
            }
            if(allObjs.size()==0) return false;
            String name=CMParms.combine(commands,0);
            if(name.equalsIgnoreCase("list"))
            {
                mob.tell("^x"+CMStrings.padRight("Guardian",15)
                        +CMStrings.padRight("Child name",45)
                        +CMStrings.padRight("Birth date",16)+"^?");
                for(int i=0;i<allObjs.size();i++)
                {
                    body=(Environmental)allObjs.elementAt(i);
                    Ability age=body.fetchEffect("Age");
                    mob.tell(CMStrings.padRight(((DatabaseEngine.PlayerData)allDataPs.elementAt(i)).who,15)
                            +CMStrings.padRight(body.name(),45)
                            +CMStrings.padRight(((age==null)?"":CMLib.time().date2String(CMath.s_long(age.text()))),16)+"\n\r"+CMStrings.padRight("",15)+body.description());
                }
                return false;
            }
            Environmental E=CMLib.english().fetchEnvironmental(allObjs,name,true);
            if(E==null) E=CMLib.english().fetchEnvironmental(allObjs,name,false);
            if(E==null) return false;
            for(int i=0;i<allObjs.size();i++)
                if(allObjs.elementAt(i)==E)
                {
                    nonPlayerData=(DatabaseEngine.PlayerData)allDataPs.elementAt(i);
                    body=E;
                    break;
                }
        }
        if(nonPlayerData==null)
        {
            if(body==null) return false;
            if((!(body instanceof DeadBody))
            ||(((DeadBody)body).mobName().length()==0))
            {
                mob.tell("You can't resurrect that.");
                return false;
            }
            playerCorpse=((DeadBody)body).playerCorpse();
    		if(!playerCorpse)
    		{
    			Ability AGE=body.fetchEffect("Age");
    			if((AGE!=null)&&(CMath.isLong(AGE.text()))&&(CMath.s_long(AGE.text())>Short.MAX_VALUE))
    			{
    				MOB M=null;
    				for(int i=0;i<mob.location().numInhabitants();i++)
    				{
    					M=mob.location().fetchInhabitant(i);
    					if((M!=null)&&(!M.isMonster()))
    					{
    						Vector V=CMLib.database().DBReadData(M.Name(),"HEAVEN",M.Name()+"/HEAVEN/"+AGE.text());
    						if((V!=null)&&(V.size()>0))
    						{
    							nonPlayerData=(DatabaseEngine.PlayerData)V.firstElement();
    							break;
    						}
    					}
    				}
    				if(nonPlayerData==null)
    				{
    					mob.tell("You can't seem to focus on "+body.Name()+"'s spirit.  Perhaps if loved ones were here?");
    					return false;
    				}
    			}
    			else
    			{
    				mob.tell("You can't resurrect "+((DeadBody)body).charStats().himher()+".");
    				return false;
    			}
    		}
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
			CMMsg msg=CMClass.getMsg(mob,body,this,verbalCastCode(mob,body,auto),auto?"<T-NAME> is resurrected!":"^S<S-NAME> resurrect(s) <T-NAMESELF>!^?");
			if(mob.location().okMessage(mob,msg))
			{
				invoker=mob;
				mob.location().send(mob,msg);
				if(playerCorpse)
					success = CMLib.utensils().resurrect(mob,mob.location(), (DeadBody)body, super.getXPCOSTLevel(mob));
				else
				if((nonPlayerData!=null) && (body != null))
				{
					String data=nonPlayerData.xml;
					Environmental object=parseHeavenlyData(data);
					if(object==null)
						mob.location().show(mob,body,CMMsg.MSG_OK_VISUAL,"<T-NAME> twitch(es) for a moment, but the spirit is too far gone.");
					else
					if(object instanceof Item)
					{
						body.destroy();
						mob.location().showHappens(CMMsg.MSG_OK_VISUAL,object.Name()+" comes back to life!");
						mob.location().addItem((Item)object);
					}
					else
					{
						MOB rejuvedMOB=(MOB)object;
						rejuvedMOB.recoverCharStats();
						rejuvedMOB.recoverMaxState();
                        body.delEffect(body.fetchEffect("Age")); // so misskids doesn't record it
						body.destroy();
						rejuvedMOB.bringToLife(mob.location(),true);
						rejuvedMOB.location().show(rejuvedMOB,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> get(s) up!");
					}
					mob.location().recoverRoomStats();
				}
			}
		}
		else
			beneficialWordsFizzle(mob,body,auto?"":"<S-NAME> attempt(s) to resurrect <T-NAMESELF>, but nothing happens.");


		// return whether it worked
		return success;
	}
    
    public Environmental parseHeavenlyData(String data)
    {
        String classID=null;
        int ability=0;
        int x=data.indexOf("/");
        if(x>=0)
        {
            classID=data.substring(0,x);
            data=data.substring(x+1);
        }
        x=data.indexOf("/");
        if(x>=0)
        {
            ability=CMath.s_int(data.substring(0,x));
            data=data.substring(x+1);
        }
        Environmental object=CMClass.getItem(classID);
        if(object==null) object=CMClass.getMOB(classID);
        if(object==null) return null;
        object.setMiscText(data);
        object.baseEnvStats().setAbility(ability);
        object.recoverEnvStats();
        return object;
        
    }
}
