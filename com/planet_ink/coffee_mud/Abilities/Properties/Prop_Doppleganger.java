package com.planet_ink.coffee_mud.Abilities.Properties;
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
public class Prop_Doppleganger extends Property
{
	public String ID() { return "Prop_Doppleganger"; }
	public String name(){ return "Doppleganger";}
	protected int canAffectCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS;}
	//protected boolean lastLevelChangers=true;
    private int maxLevel=Integer.MAX_VALUE;
    private int minLevel=Integer.MIN_VALUE;
    protected Environmental lastOwner=null;

	public String accountForYourself()
	{ return "Level Changer";	}

    public void setMiscText(String text)
    {
        super.setMiscText(text);
        maxLevel=CMParms.getParmInt(text,"MAX",Integer.MAX_VALUE);
        minLevel=CMParms.getParmInt(text,"MIN",Integer.MIN_VALUE);
    }

    public void executeMsg(Environmental myHost, CMMsg msg)
    {
    	if((affected instanceof Item)
    	&&(((Item)affected).owner()!=lastOwner)
    	&&(((Item)affected).owner() instanceof MOB))
    	{
    		lastOwner=((Item)affected).owner();
			int level=((MOB)lastOwner).envStats().level()+CMath.s_int(text());
			if(text().endsWith("%")) level=(int)Math.round(CMath.mul(level,CMath.s_pct(text())));
			if(level<minLevel) level=minLevel;
            if(level>maxLevel) level=maxLevel;
			((Item)affected).baseEnvStats().setLevel(level);
			((Item)affected).envStats().setLevel(level);
			CMLib.itemBuilder().balanceItemByLevel((Item)affected);
			((Item)affected).baseEnvStats().setLevel(((MOB)lastOwner).envStats().level());
			((Item)affected).envStats().setLevel(((MOB)lastOwner).envStats().level());
			lastOwner.recoverEnvStats();
			Room R=((MOB)lastOwner).location();
			if(R!=null) R.recoverRoomStats();
    	}
    	super.executeMsg(myHost,msg);
    }

    public boolean qualifies(MOB mob, Room R)
    {
        if((mob==affected)||(mob==null)) return false;
        if(mob.fetchEffect(ID())!=null) return false;
        if(mob.isMonster())return true;
        if((!CMSecurity.isAllowed(mob,R,"CMDMOBS"))
        &&(!CMSecurity.isAllowed(mob,R,"CMDROOMS")))
            return true;
        return false;
    }

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected instanceof MOB)
		&&(((msg.target() instanceof Room)&&(msg.sourceMinor()==CMMsg.TYP_ENTER))
           ||(msg.sourceMinor()==CMMsg.TYP_LIFE)))
		//&&(lastLevelChangers))
		{
			//lastLevelChangers=false;
			MOB mob=(MOB)affected;
            Room R=(msg.target() instanceof Room)?((Room)msg.target()):msg.source().location();
			if((R!=null)
			&&(CMLib.flags().aliveAwakeMobile(mob,true))
			&&(mob.curState().getHitPoints()>=mob.maxState().getHitPoints()))
			{
				int total=0;
				int num=0;
				MOB victim=mob.getVictim();
                if(qualifies(victim,R))
				{
					total+=victim.envStats().level();
					num++;
				}
                MOB entrant=(MOB)msg.source();
                if(qualifies(entrant,R))
                {
                    total+=entrant.envStats().level();
                    num++;
                }
				for(int i=0;i<R.numInhabitants();i++)
				{
					MOB M=R.fetchInhabitant(i);
					if((M!=mob)
					&&((M.getVictim()==mob)||(victim==null))
                    &&((M!=victim)&&(M!=entrant))
                    &&(qualifies(M,R)))
					{
						total+=M.envStats().level();
						num++;
					}
				}
				if(num>0)
				{
					int level=(int)Math.round(CMath.div(total,num))+CMath.s_int(text());
					if(text().endsWith("%")) level=(int)Math.round(CMath.mul(CMath.div(total,num),CMath.s_pct(text())));
					if(level<minLevel) level=minLevel;
                    if(level>maxLevel) level=maxLevel;
					if(level!=mob.baseEnvStats().level())
					{
						mob.baseEnvStats().setLevel(level);
						mob.baseEnvStats().setArmor(CMLib.leveler().getLevelMOBArmor(mob));
						mob.baseEnvStats().setAttackAdjustment(CMLib.leveler().getLevelAttack(mob));
						mob.baseEnvStats().setDamage(CMLib.leveler().getLevelMOBDamage(mob));
						mob.baseEnvStats().setSpeed(1.0+(CMath.div(level,100)*4.0));
						mob.baseState().setHitPoints(CMLib.leveler().getPlayerHitPoints(mob));
						mob.baseState().setMana(CMLib.leveler().getLevelMana(mob));
						mob.baseState().setMovement(CMLib.leveler().getLevelMove(mob));
						mob.recoverEnvStats();
						mob.recoverCharStats();
						mob.recoverMaxState();
						mob.resetToMaxState();
					}
				}
			}
		}
		return super.okMessage(myHost,msg);
	}
}
