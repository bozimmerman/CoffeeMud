package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Prayer_Marry extends Prayer
{
	public String ID() { return "Prayer_Marry"; }
	public String name(){ return "Marry";}
	public long flags(){return Ability.FLAG_HOLY;}
	public int quality(){return Ability.OK_OTHERS;}
	public Environmental newInstance(){	return new Prayer_Marry();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()<2)
		{
			mob.tell("Whom to whom?");
			return false;
		}
		String name1=(String)commands.lastElement();
		String name2=Util.combine(commands,0,commands.size()-1);
		MOB husband=mob.location().fetchInhabitant(name1);
		if((husband==null)||(!Sense.canBeSeenBy(mob,husband)))
		{
			mob.tell("You don't see "+name1+" here!");
			return false;
		}
		MOB wife=mob.location().fetchInhabitant(name2);
		if((wife==null)||(!Sense.canBeSeenBy(mob,wife)))
		{
			mob.tell("You don't see "+name2+" here!");
			return false;
		}
		if(wife.charStats().getStat(CharStats.GENDER)=='M')
		{
			MOB M=wife;
			wife=husband;
			husband=M;
		}
		if(wife.isMarriedToLeige())
		{
			mob.tell(wife.name()+" is already married!!");
			return false;
		}
		if(husband.isMarriedToLeige())
		{
			mob.tell(husband.name()+" is already married!!");
			return false;
		}
		if(wife.getLeigeID().length()>0)
		{
			mob.tell(wife.name()+" is leiged to "+wife.getLeigeID()+", and cannot marry.");
			return false;
		}
		if(husband.getLeigeID().length()>0)
		{
			mob.tell(husband.name()+" is leiged to "+husband.getLeigeID()+", and cannot marry.");
			return false;
		}
		if((wife.isMonster())||(wife.playerStats()==null))
		{
			mob.tell(wife.name()+" must be a player to marry.");
			return false;
		}
		if((husband.isMonster())||(husband.playerStats()==null))
		{
			mob.tell(wife.name()+" must be a player to marry.");
			return false;
		}
		Item I=husband.fetchWornItem("wedding band");
		if(I==null)
		{
			mob.tell(husband.name()+" isn't wearing a wedding band!");
			return false;
		}
		I=wife.fetchWornItem("wedding band");
		if(I==null)
		{
			mob.tell(wife.name()+" isn't wearing a wedding band!");
			return false;
		}
		MOB witness=null;
		for(int i=0;i<mob.location().numInhabitants();i++)
		{
			MOB M=mob.location().fetchInhabitant(i);
			if((M!=null)
			&&(M!=mob)
			&&(M!=husband)
			&&(M!=wife))
				witness=M;
		}
		if(witness==null)
		{
			mob.tell("You need a witness present.");
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> "+prayForWord(mob)+" to bless the holy union between "+husband.name()+" and "+wife.name()+".^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				husband.setLeigeID(wife.Name());
				wife.setLeigeID(husband.Name());
				CommonMsgs.say(mob,husband,"You may kiss your bride!",false,false);
			}
		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> start(s) 'Dearly beloved', and then clear(s) <S-HIS-HER> throat.");

		return success;
	}
}
