package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_Conduct extends StdAbility
{
	public String ID() { return "Skill_Conduct"; }
	public String name(){ return "Conduct Symphony";}
	public String displayText(){ return "("+name()+")";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	private static final String[] triggerStrings = {"CONDUCT"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public int maxRange(){return 2;}
	public Environmental newInstance(){	return new Skill_Conduct();}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Ability SYMPHONY=mob.fetchAbility("Play_Symphony");
		if((!auto)&&(SYMPHONY==null))
		{
			mob.tell("But you don't know how to play a symphony.");
			return false;
		}
		if(SYMPHONY==null)
		{
			SYMPHONY=CMClass.getAbility("Play_Symphony");
			SYMPHONY.setProfficiency(100);
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		if((!auto)&&(!Sense.aliveAwakeMobile(mob,false)))
			return false;

		boolean success=profficiencyCheck(0,auto);
		Play.unplay(mob,mob,null);
		if(success)
		{
			String str=auto?"^SSymphonic Conduction Begins!^?":"^S<S-NAME> begin(s) to wave <S-HIS-HER> arms in a mystical way!^?";
			if((!auto)&&(mob.fetchEffect(this.ID())!=null))
				str="^S<S-NAME> start(s) conducting the symphony over again.^?";

			FullMsg msg=new FullMsg(mob,null,this,(auto?CMMsg.MASK_GENERAL:0)|CMMsg.MSG_CAST_SOMANTIC_SPELL,str);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				Play newOne=(Play)this.copyOf();
				newOne.referencePlay=newOne;

				Hashtable h=properTargets(mob,givenTarget,auto);
				if(h==null) return false;
				if(h.get(mob)==null) h.put(mob,mob);

				for(Enumeration f=h.elements();f.hasMoreElements();)
				{
					MOB follower=(MOB)f.nextElement();

					// malicious songs must not affect the invoker!
					int affectType=CMMsg.MSG_CAST_SOMANTIC_SPELL;
					if(auto) affectType=affectType|CMMsg.MASK_GENERAL;
					if(Sense.canBeSeenBy(invoker,follower))
					{
						FullMsg msg2=new FullMsg(mob,follower,this,affectType,null);
						if(mob.location().okMessage(mob,msg2))
						{
							follower.location().send(follower,msg2);
							if(msg2.value()<=0)
								SYMPHONY.invoke(follower,new Vector(),null,false);
						}
					}
				}
				mob.location().recoverRoomStats();
			}
		}
		else
			mob.location().show(mob,null,CMMsg.MSG_NOISE,"<S-NAME> wave(s) <S-HIS-HER> arms around, looking silly.");

		return success;
	}
}