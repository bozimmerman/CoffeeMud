package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Delirium extends Spell
{
	public String ID() { return "Spell_Delirium"; }
	public String name(){return "Delirium";}
	public String displayText(){return "(Delirium)";}
	public int maxRange(){return 1;}
	public int quality(){return MALICIOUS;};
	protected int canAffectCode(){return CAN_MOBS;}
	int amountRemaining=0;
	public Environmental newInstance(){	return new Spell_Delirium();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ILLUSION;}

	private Environmental getRandomOtherName(Environmental likeThisOne)
	{
		if(likeThisOne instanceof Room)
		{
			int tries=0;
			while((++tries)<1000)
			{
				Room R=(Room)invoker.location().getArea().getRandomRoom();
				if(!R.displayText().equals(likeThisOne.displayText()))
					return R;
			}
		}
		else
		if(likeThisOne instanceof Exit)
		{
			int tries=0;
			while((++tries)<1000)
			{
				Room R=(Room)invoker.location().getArea().getRandomRoom();
				for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
				{
					Exit x=R.getExitInDir(d);
					if((x!=null)&&(!x.name().equals(likeThisOne.name())))
						return x;
				}
			}
		}
		else
		if(likeThisOne instanceof MOB)
		{
			int tries=0;
			while((++tries)<1000)
			{
				Room R=(Room)invoker.location().getArea().getRandomRoom();
				if(R.numInhabitants()>0)
				{
					MOB possible=R.fetchInhabitant(Dice.roll(1,R.numInhabitants(),-1));
					if((possible!=null)&&(!possible.name().equalsIgnoreCase(likeThisOne.name())))
						return possible;
				}
			}
		}
		else
		if(likeThisOne instanceof Item)
		{
			int tries=0;
			while((++tries)<1000)
			{
				Room R=(Room)invoker.location().getArea().getRandomRoom();
				if(R.numItems()>0)
				{
					Item possible=R.fetchItem(Dice.roll(1,R.numItems(),-1));
					if((possible!=null)&&(!possible.name().equalsIgnoreCase(likeThisOne.name())))
						return possible;
				}
				if(R.numInhabitants()>0)
				{
					MOB owner=R.fetchInhabitant(Dice.roll(1,R.numInhabitants(),-1));
					if((owner!=null)&&(owner.inventorySize()>0))
					{
						Item possible=owner.fetchInventory(Dice.roll(1,owner.inventorySize(),-1));
						if((possible!=null)&&(!possible.name().equalsIgnoreCase(likeThisOne.name())))
							return possible;
					}
				}
			}
		}
		return null;
	}

	private String getRand(Environmental likeThis)
	{
		Environmental E=this.getRandomOtherName(likeThis);
		if(E==null)
		{
			if(likeThis instanceof MOB)
				return "someone";
			else
				return "something";
		}
		else
			return E.name();
	}

	private String process(MOB mob, String str, Environmental obj)
	{
		if(obj==null) return str;

		int x=str.indexOf("<S-NAME>");
		if(x>=0)
			str=str.substring(0,x)+getRand(obj)+str.substring(x+("<S-NAME>").length());
		x=str.indexOf("<S-HIS-HER>");
		if(x>=0)
			str=str.substring(0,x)+getRand(obj)+str.substring(x+("<S-HIS-HER>").length());
		x=str.indexOf("<T-NAME>");
		if(x>=0)
			str=str.substring(0,x)+getRand(obj)+str.substring(x+("<T-NAME>").length());
		x=str.indexOf("<T-HIS-HER>");
		if(x>=0)
			str=str.substring(0,x)+getRand(obj)+str.substring(x+("<T-HIS-HER>").length());
		x=str.indexOf("<T-NAMESELF>");
		if(x>=0)
			str=str.substring(0,x)+getRand(obj)+str.substring(x+("<T-NAMESELF>").length());
		str=" "+str+" ";
		x=str.toUpperCase().indexOf(" "+obj.name().toUpperCase()+" ");
		if(x>=0)
			str=str.substring(0,x)+" "+getRand(obj)+" "+str.substring(x+(" "+obj.name()+" ").length());
		x=str.toUpperCase().indexOf(" YOU ");
		if(x>=0)
			str=str.substring(0,x)+" "+getRand(mob)+" "+str.substring(x+(" YOU ").length());
		x=str.toUpperCase().indexOf(" "+mob.name().toUpperCase()+" ");
		if(x>=0)
			str=str.substring(0,x)+" "+getRand(mob)+" "+str.substring(x+(" "+mob.name().toUpperCase()+" ").length());
		MOB victim=mob.getVictim();
		if(victim!=null)
		{
			x=str.toUpperCase().indexOf(" "+victim.name().toUpperCase()+" ");
			if(x>=0)
				str=str.substring(0,x)+" "+getRand(victim)+" "+str.substring(x+(" "+victim.name().toUpperCase()+" ").length());
		}
		return str.trim();
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==Host.MOB_TICK)
		&&(affected!=null)
		&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			amountRemaining-=mob.charStats().getStat(CharStats.INTELLIGENCE);
			if(amountRemaining<0)
				unInvoke();
		}
		return super.tick(ticking,tickID);
	}


	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		String othersMessage=affect.othersMessage();
		String sourceMessage=affect.sourceMessage();
		String targetMessage=affect.targetMessage();
		if((affect.amITarget(mob))&&(targetMessage!=null))
		{
			targetMessage=process(mob,process(mob,targetMessage,affect.target()),affect.target());
			if(!targetMessage.equals(affect.targetMessage()))
				affect.modify(affect.source(),affect.target(),affect.tool(),affect.sourceCode(),sourceMessage,affect.targetCode(),targetMessage,affect.othersCode(),othersMessage);
		}
		if((affect.amISource(mob))&&(sourceMessage!=null))
		{
			sourceMessage=process(mob,process(mob,sourceMessage,affect.source()),affect.source());
			if(!sourceMessage.equals(affect.sourceMessage()))
				affect.modify(affect.source(),affect.target(),affect.tool(),affect.sourceCode(),sourceMessage,affect.targetCode(),targetMessage,affect.othersCode(),othersMessage);
		}
		return true;
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();

			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"<S-NAME> begin(s) to feel a bit less delirius.");
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;


		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> whisper(s) to <T-NAMESELF>.^?");
			FullMsg msg2=new FullMsg(mob,target,this,Affect.MSK_CAST_MALICIOUS_VERBAL|Affect.TYP_MIND|(auto?Affect.MASK_GENERAL:0),null);
			if((mob.location().okAffect(mob,msg))||(mob.location().okAffect(mob,msg2)))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				if(!msg.wasModified())
				{
					amountRemaining=300;
					maliciousAffect(mob,target,0,-1);
					target.location().show(target,null,Affect.MSG_OK_ACTION,"<S-NAME> go(es) under the grip of delirium!!");
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> whisper(s) to <T-NAMESELF>, but the spell fades.");

		// return whether it worked
		return success;
	}
}
