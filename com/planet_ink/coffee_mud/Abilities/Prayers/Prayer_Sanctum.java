package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_Sanctum extends Prayer
{
	public String ID() { return "Prayer_Sanctum"; }
	public String name(){return "Sanctum";}
	public String displayText(){return "(Sanctum)";}
	public int quality(){ return OK_OTHERS;}
	protected int canAffectCode(){return CAN_ROOMS;}
	public Environmental newInstance(){	return new Prayer_Sanctum();}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(affected==null)
			return super.okMessage(myHost,msg);

		Room R=(Room)affected;
		if((msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(msg.target()==R)
		&&(!msg.source().Name().equals(text()))
		&&((msg.source().amFollowing()==null)||(!msg.source().amFollowing().Name().equals(text())))
		&&(!CoffeeUtensils.doesOwnThisProperty(msg.source(),R)))
		{
			msg.source().tell("You feel your muscles unwilling to cooperate.");
			return false;
		}
		if((Util.bset(msg.sourceCode(),CMMsg.MASK_MALICIOUS))
		||(Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
		||(Util.bset(msg.othersCode(),CMMsg.MASK_MALICIOUS)))
		{
			if((msg.source()!=null)
			&&(msg.target()!=null)
			&&(msg.source()!=affected)
			&&(msg.source()!=msg.target()))
			{
				if(affected instanceof MOB)
				{
					MOB mob=(MOB)affected;
					if((Sense.aliveAwakeMobile(mob,true))
					&&(!mob.isInCombat()))
					{
						String t="No fighting!";
						if(text().length()>0)
						{
							Vector V=Util.parseSemicolons(text(),true);
							t=(String)V.elementAt(Dice.roll(1,V.size(),-1));
						}
						CommonMsgs.say(mob,msg.source(),t,false,false);
					}
					else
						return super.okMessage(myHost,msg);
				}
				else
				{
					String t="You feel too peaceful here.";
					if(text().length()>0)
					{
						Vector V=Util.parseSemicolons(text(),true);
						t=(String)V.elementAt(Dice.roll(1,V.size(),-1));
					}
					msg.source().tell(t);
				}
				if(msg.source().getVictim()!=null)
					msg.source().getVictim().makePeace();
				msg.source().makePeace();
				msg.modify(msg.source(),msg.target(),msg.tool(),CMMsg.NO_EFFECT,"",CMMsg.NO_EFFECT,"",CMMsg.NO_EFFECT,"");
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Environmental target=mob.location();
		if(target==null) return false;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell("This place is already a sanctum.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> "+prayForWord(mob)+" to make this place a sanctum.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				setMiscText(mob.Name());
				if((target instanceof Room)
				&&((CoffeeUtensils.doesOwnThisProperty(mob,((Room)target)))
					||((mob.amFollowing()!=null)&&(CoffeeUtensils.doesOwnThisProperty(mob.amFollowing(),((Room)target))))))
				{
					target.addNonUninvokableEffect((Ability)this.copyOf());
					CMClass.DBEngine().DBUpdateRoom((Room)target);
				}
				else
					beneficialAffect(mob,target,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> "+prayForWord(mob)+" to make this place a sanctum, but <S-IS-ARE> not answered.");

		return success;
	}
}