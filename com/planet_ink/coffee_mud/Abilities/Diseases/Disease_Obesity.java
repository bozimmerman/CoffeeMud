package com.planet_ink.coffee_mud.Abilities.Diseases;
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
import java.util.Vector;



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
public class Disease_Obesity extends Disease
{
	public String ID() { return "Disease_Obesity"; }
	public String name(){ return "Obesity";}
	public String displayText()
	{
	    int amount=amountOfFat();
	    if(amount<20)
		    return "(Chubby)";
	    else
	    if(amount<60)
		    return "(Fat)";
	    else
	    if(amount<120)
		    return "(Obese)";
	    else
		    return "(Morbid obesity)";
	}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	public int difficultyLevel(){return 10;}

	protected int DISEASE_TICKS(){return 999999;}
	protected int DISEASE_DELAY(){return 50;}
	protected String DISEASE_DONE(){return "You've become fit and trim!";}
	protected String DISEASE_START(){return "^G<S-NAME> look(s) like <S-HE-SHE> <S-HAS-HAVE> been gaining some weight.^?";}
	protected String DISEASE_AFFECT(){return "";}
	public int abilityCode(){return 0;}
	public boolean canBeUninvoked(){canBeUninvoked=!(amountOfFat()>0);return super.canBeUninvoked();}
    protected long lastLoss=-1;
	protected int fatAmount=-1;
	
	protected int amountOfFat()
	{
	    if((fatAmount<0)&&(CMath.isNumber(text()))) 
	        fatAmount=CMath.s_int(text());
        if(fatAmount<0) fatAmount=0;
	    if(fatAmount>=0) return fatAmount;
	    return 1;
	}
	
	public void setMiscText(String newText)
	{
	    super.setMiscText(newText);
	    fatAmount=-1;
	}
	
	private void setFatAmountChange(int change)
	{
        setMiscText(""+(amountOfFat()+change));
	}

	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{
		super.affectCharStats(affectedMob, affectableStats);
	    affectableStats.setStat(CharStats.STAT_WEIGHTADJ, 
    		affectableStats.getStat(CharStats.STAT_WEIGHTADJ)
    		+(int)Math.round(CMath.mul(affectedMob.baseEnvStats().weight(),CMath.div(CMath.s_int(text()),100.0))));
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
	    super.affectEnvStats(affected,affectableStats);
	}
	
	public void affectCharState(MOB affected, CharState affectableState)
	{
	    super.affectCharState(affected,affectableState);
	    int oldMovement=affectableState.getMovement();
	    affectableState.setMovement(affectableState.getMovement()-(int)Math.round(CMath.mul(affectableState.getMovement(),CMath.div(CMath.s_int(text()),100.0))));
	    if((affectableState.getMovement()<20)&&(oldMovement>20)) affectableState.setMovement(20);
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
	    if((ticking==affected)&&(tickID==Tickable.TICKID_MOB)&&(affected instanceof MOB))
	    {
	        MOB mob=(MOB)affected;
	        if((mob.curState().getMovement()<mob.maxState().getMovement()/10)
	        &&((lastLoss<0)||((System.currentTimeMillis()-lastLoss)>10000)))
	        {
	            lastLoss=System.currentTimeMillis();
	            int change=CMLib.dice().roll(1,10,0);
	            int fat=amountOfFat();
	            if(fat>=0)
	            {
		            if(fat<change)
					    setFatAmountChange(-fat);
		            else
					    setFatAmountChange(-change);
	            }
	        }
	        if(amountOfFat()<=0)
	            unInvoke();
	    }
	    return super.tick(ticking,tickID);
	}
	
	public void executeMsg(Environmental host, CMMsg msg)
	{
		if((msg.source()==affected)
		&&(msg.targetMinor()==CMMsg.TYP_EAT)
		&&(msg.target()!=null)
		&&(msg.source().curState().getHunger()>=msg.source().maxState().maxHunger(msg.source().baseWeight())))
		{
		    setFatAmountChange(CMLib.dice().roll(1,5,0));
		    msg.source().recoverEnvStats();
		    msg.source().recoverCharStats();
		    msg.source().recoverMaxState();
		}
		else
		if((msg.target()==affected)
		&&((msg.targetMinor()==CMMsg.TYP_LOOK)||(msg.targetMinor()==CMMsg.TYP_EXAMINE))
		&&(CMLib.flags().canBeSeenBy(affected,msg.source()))
		&&(affected instanceof MOB))
		{
		    int amount=amountOfFat();
		    String str="";
		    if(amount<20)
			    str="a bit chubby";
		    else
		    if(amount<60)
			    str="fat";
		    else
		    if(amount<120)
			    str="obese";
		    else
			    str="morbidly obese";
			msg.addTrailerMsg(CMClass.getMsg(msg.source(),null,null,
										  CMMsg.MSG_OK_VISUAL,"\n\r"+affected.name()+" is "+str+".\n\r",
										  CMMsg.NO_EFFECT,null,
										  CMMsg.NO_EFFECT,null));
		}
		super.executeMsg(host,msg);
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(super.invoke(mob,commands,givenTarget,auto,asLevel))
		{
		    Ability A=target.fetchEffect(ID());
		    if(A!=null) A.setMiscText(""+CMLib.dice().roll(1,5,0));
			return true;
		}
		return false;
	}
}
