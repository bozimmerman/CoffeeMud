package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Beggar extends StdBehavior
{
	public String ID(){return "Beggar";}
	Vector mobsHitUp=new Vector();
	int tickTock=0;
	public Behavior newInstance()
	{
		return new Beggar();
	}
	
	public void affect(Environmental oking, Affect msg)
	{
		super.affect(oking,msg);
		if((oking==null)||(!(oking instanceof MOB)))
			return;
		MOB mob=(MOB)oking;
		if((msg.amITarget(mob))&&(msg.targetMinor()==Affect.TYP_GIVE))
			msg.addTrailerMsg(new FullMsg(mob,msg.source(),Affect.MSG_SPEAK,"^T<S-NAME> say(s) 'Thank you gov'ner!' to <T-NAME> ^?"));
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);

		if(tickID!=Host.MOB_TICK) return true;
		if(!canFreelyBehaveNormal(ticking)) return true;
		tickTock++;
		if(tickTock<5) return true;
		tickTock=0;
		MOB mob=(MOB)ticking;
		for(int i=0;i<mob.location().numInhabitants();i++)
		{
			MOB mob2=mob.location().fetchInhabitant(i);
			if((mob2!=null)
			   &&(Sense.canBeSeenBy(mob2,mob))
			   &&(mob2!=mob)
			   &&(!mobsHitUp.contains(mob2))
			   &&(!mob2.isMonster()))
			{
				switch(Dice.roll(1,10,0))
				{
				case 1:
					ExternalPlay.quickSay(mob,mob2,"A little something for a vet please?",false,false);
					break;
				case 2:
					ExternalPlay.quickSay(mob,mob2,"Spare a gold piece "+((mob2.charStats().getStat(CharStats.GENDER)==(int)'M')?"mister?":"madam?"),false,false);
					break;
				case 3:
					ExternalPlay.quickSay(mob,mob2,"Spare some change?",false,false);
					break;
				case 4:
					ExternalPlay.quickSay(mob,mob2,"Please "+((mob2.charStats().getStat(CharStats.GENDER)==(int)'M')?"mister":"madam")+", a little something for an old man down on "+mob.charStats().hisher()+" luck?",false,false);
					break;
				case 5:
					ExternalPlay.quickSay(mob,mob2,"Hey, I lost my 'Will Work For Food' sign.  Can you spare me the money to buy one?",false,false);
					break;
				case 6:
					ExternalPlay.quickSay(mob,mob2,"Spread a little joy to an old fogie?",false,false);
					break;
				case 7:
					ExternalPlay.quickSay(mob,mob2,"Change?",false,false);
					break;
				case 8:
					ExternalPlay.quickSay(mob,mob2,"Can you spare a little change?",false,false);
					break;
				case 9:
					ExternalPlay.quickSay(mob,mob2,"Can you spare a little gold?",false,false);
					break;
				case 10:
					ExternalPlay.quickSay(mob,mob2,"Gold piece for a poor fogie down on "+mob.charStats().hisher()+" luck?",false,false);
					break;
				}
				mobsHitUp.addElement(mob2);
				break;
			}
		}
		if(mobsHitUp.size()>0)
			mobsHitUp.removeElementAt(0);
		return true;
	}
}