package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Play_Solo extends Play
{
	public String ID() { return "Play_Solo"; }
	public String name(){ return "Solo";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	public Environmental newInstance(){	return new Play_Solo();}
	protected boolean persistantSong(){return false;}
	protected boolean skipStandardSongTick(){return true;}
	protected String songOf(){return "a "+name();}

	public boolean okAffect(Environmental E, Affect msg)
	{
		if(!super.okAffect(E,msg)) return false;
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB myChar=(MOB)affected;
			if(!msg.amISource(myChar)
			&&(msg.tool()!=null)
			&&(!msg.tool().ID().equals(ID()))
			&&(msg.tool() instanceof Ability)
			&&(((((Ability)msg.tool()).classificationCode()&Ability.ALL_CODES)==Ability.SONG)))
			{
				MOB otherBard=msg.source();
				if(((otherBard.envStats().level()+Dice.roll(1,30,0))>(myChar.envStats().level()+Dice.roll(1,20,0)))
				&&(otherBard.location()!=null))
				{
					if(otherBard.location().show(otherBard,myChar,null,Affect.MSG_OK_ACTION,"<S-NAME> upstage(s) <T-NAMESELF>, stopping <T-HIS-HER> solo!"))
						unplay(myChar);
				}
				else
				if(otherBard.location()!=null)
				{
					otherBard.tell("You can't seem to upstage "+myChar.name()+"'s solo.");
					return false;
				}
			}
		}
		return true;
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		
		boolean success=profficiencyCheck(0,auto);
		unplay(mob);
		if(success)
		{
			String str=auto?"^S"+songOf()+" begins to play!^?":"^S<S-NAME> begin(s) to play "+songOf()+" on "+instrumentName()+".^?";
			if((!auto)&&(mob.fetchAffect(this.ID())!=null))
				str="^S<S-NAME> start(s) playing "+songOf()+" on "+instrumentName()+" again.^?";

			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),str);
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				Play newOne=(Play)this.copyOf();
				newOne.referencePlay=newOne;

				Vector songsToCancel=new Vector();
				for(int i=0;i<mob.location().numInhabitants();i++)
				{
					MOB M=mob.location().fetchInhabitant(i);
					if(M!=null)
					for(int a=0;a<M.numAffects();a++)
					{
						Ability A=M.fetchAffect(a);
						if((A!=null)
						&&(A.invoker()!=mob)
						&&((A.classificationCode()&Ability.ALL_CODES)==Ability.SONG))
							songsToCancel.addElement(A);
					}
				}
				int reqMana=songsToCancel.size()*10;
				if(mob.curState().getMana()<reqMana)
				{
					mob.tell("You needed "+reqMana+" mana to play this solo!");
					return false;
				}
				mob.curState().adjMana(-reqMana,mob.maxState());
				for(int i=0;i<songsToCancel.size();i++)
				{
					Ability A=(Ability)songsToCancel.elementAt(i);
					if((A.affecting()!=null)
					&&(A.affecting() instanceof MOB))
					{
						MOB M=(MOB)A.affecting();
						if(A instanceof Song) ((Song)A).unsing(M);
						else
						if(A instanceof Dance) ((Dance)A).undance(M);
						else
						if(A instanceof Play) ((Play)A).unplay(M);
						else
							A.unInvoke();
					}
					else
						A.unInvoke();
				}
				mob.addAffect(newOne);
				mob.location().recoverRoomStats();
			}
		}
		else
			mob.location().show(mob,null,Affect.MSG_NOISE,"<S-NAME> hit(s) a foul note.");

		return success;
	}
}
