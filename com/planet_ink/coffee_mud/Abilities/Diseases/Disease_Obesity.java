package com.planet_ink.coffee_mud.Abilities.Diseases;
import java.util.Vector;

import com.planet_ink.coffee_mud.common.FullMsg;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	public int difficultyLevel(){return 10;}

	protected int DISEASE_TICKS(){return 999999;}
	protected int DISEASE_DELAY(){return 50;}
	protected String DISEASE_DONE(){return "You've become fit and trim!";}
	protected String DISEASE_START(){return "^G<S-NAME> look(s) like <S-HE-SHE> <S-HAS-HAVE> been gaining some weight.^?";}
	protected String DISEASE_AFFECT(){return "";}
	public int abilityCode(){return 0;}
	public boolean canBeUninvoked(){canBeUninvoked=!(amountOfFat()>0);return super.canBeUninvoked();}
	private long lastLoss=-1;
	private int fatAmount=-1;
	
	private int amountOfFat()
	{
	    if((fatAmount<0)&&(Util.isNumber(text()))) 
	        fatAmount=Util.s_int(text());
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

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
	    super.affectEnvStats(affected,affectableStats);
	    affectableStats.setWeight(affectableStats.weight()+(int)Math.round(Util.mul(affectableStats.weight(),Util.div(Util.s_int(text()),100.0))));
	}
	
	public void affectCharState(MOB affected, CharState affectableState)
	{
	    super.affectCharState(affected,affectableState);
	    int oldMovement=affectableState.getMovement();
	    affectableState.setMovement(affectableState.getMovement()-(int)Math.round(Util.mul(affectableState.getMovement(),Util.div(Util.s_int(text()),100.0))));
	    if((affectableState.getMovement()<20)&&(oldMovement>20)) affectableState.setMovement(20);
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
	    if((ticking==affected)&&(tickID==MudHost.TICK_MOB)&&(affected instanceof MOB))
	    {
	        MOB mob=(MOB)affected;
	        if((mob.curState().getMovement()<mob.maxState().getMovement()/10)
	        &&((lastLoss<0)||((System.currentTimeMillis()-lastLoss)>30000)))
	        {
	            lastLoss=System.currentTimeMillis();
	            int change=Dice.roll(1,5,0);
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
		    setFatAmountChange(Dice.roll(1,5,0));
		    msg.source().recoverEnvStats();
		    msg.source().recoverCharStats();
		    msg.source().recoverMaxState();
		}
		else
		if((msg.target()==affected)
		&&((msg.targetMinor()==CMMsg.TYP_LOOK)||(msg.targetMinor()==CMMsg.TYP_EXAMINE))
		&&(Sense.canBeSeenBy(affected,msg.source()))
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
			msg.addTrailerMsg(new FullMsg(msg.source(),null,null,
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
		    if(A!=null) A.setMiscText(""+Dice.roll(1,5,0));
			return true;
		}
		else
			return false;
	}
}
