package com.planet_ink.coffee_mud.Abilities.Misc;

import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

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

public class Injury extends StdAbility
{
	public String ID() { return "Injury"; }
	public String name(){ return "Injury";}
	
	private CMMsg lastMsg=null;
	private String lastLoc=null;
	public int lastHP=-1;
	//public final static String[] BODYPARTSTR={
	//	"ANTENEA","EYE","EAR","HEAD","NECK","ARM","HAND","TORSO","LEG","FOOT",
	//	"NOSE","GILL","MOUTH","WAIST","TAIL","WING"};
	public final static int[] INJURYCHANCE={
		3,3,3,11,3,12,5,35,13,5,3,0,0,3,3,3};
	
	public String displayText()
	{
		StringBuffer buf=new StringBuffer("");
		Object[] O=null;
		Vector V=null;
		try{
			if(injuries!=null)
				for(int i=0;i<Race.BODY_PARTS;i++)
				{
				    V=injuries[i];
				    if(V!=null)
				    for(int i2=0;i2<V.size();i2++)
					{
					    O=(Object[])V.elementAt(i2);
						buf.append(", "+((String)O[0]).toLowerCase()+" ("+((Integer)O[1]).intValue()+"%)");
					}
				}
		}
		catch(Exception e){}
		if(buf.length()==0) return "";
		return "(Injuries:"+buf.substring(1)+")";
	}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.INDIFFERENT;}
	public boolean putInCommandlist(){return false;}
	private static final String[] triggerStrings = {"INJURE"};
	public String[] triggerStrings(){return triggerStrings;}
	public boolean canBeUninvoked(){return true;}
	public int classificationCode(){return Ability.PROPERTY;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	public Vector[] injuries=new Vector[Race.BODY_PARTS];

	public void unInvoke()
	{
	    Environmental E=affected;
	    super.unInvoke();
	    if((E instanceof MOB)&&(canBeUninvoked())&&(!((MOB)E).amDead()))
	        ((MOB)E).tell("Your injuries are healed.");
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
	    if((affected instanceof MOB)&&(tickID==MudHost.TICK_MOB))
	    {
	        MOB mob=(MOB)affected;
	        if(mob.curState().getHitPoints()>=mob.maxState().getHitPoints())
	        {
	            for(int i=0;i<injuries.length;i++)
	                injuries[i]=null;
	            unInvoke();
	        }
	        else
	        if((mob.curState().getHitPoints()>lastHP)&&(lastHP>=0))
	        {
	            Vector choicesToHeal=new Vector();
	            for(int i=0;i<injuries.length;i++)
	                if(injuries[i]!=null)
	                    for(int x=0;x<injuries[i].size();x++)
			            {
	                        int[] choice=new int[2];
	                        choice[0]=i; choice[1]=x;
		                    choicesToHeal.addElement(choice);
			            }
	            if(choicesToHeal.size()==0)
	            {
		            for(int i=0;i<injuries.length;i++)
		                injuries[i]=null;
		            unInvoke();
	            }
	            else
	            {
		            int pct=(int)Math.round(Util.div(mob.curState().getHitPoints()-lastHP,mob.maxState().getHitPoints())*100.0);
		            if(pct<=0) pct=1;
		            int tries=100;
		            while((pct>0)&&((--tries)>0)&&(choicesToHeal.size()>0))
		            {
		                int which=Dice.roll(1,choicesToHeal.size(),-1);
		                int[] choice=(int[])choicesToHeal.elementAt(which);
		                if(choice[0]<injuries.length)
		                {
		                    Vector V=injuries[choice[0]];
		                    if((V!=null)&&(choice[1]<V.size()))
		                    {
		                        Object[] O=(Object[])V.elementAt(choice[1]);
		                        if(pct>((Integer)O[1]).intValue())
		                        {
		                            V.removeElement(O);
		                            if(V.size()==0) injuries[choice[0]]=null;
		                            pct-=((Integer)O[1]).intValue();
		                            choicesToHeal.removeElementAt(which);
		                        }
		                        else
		                        {
		                            O[1]=new Integer(((Integer)O[1]).intValue()-pct);
		                            pct=0;
		                        }
		                    }
		                }
		            }
	            }
	        }
	        lastHP=mob.curState().getHitPoints();
	    }
	    return super.tick(ticking,tickID);
	}
	
	public static String[][] TRANSLATE={
	    {"<T-HIM-HER>","<T-HIS-HER>"},
	    {"<T-NAME>","<T-YOUPOSS>"},
	    {"<T-NAMESELF>","<T-YOUPOSS>"}
	};
	public String fixMessageString(String message, String loc)
	{
	    if(message==null) return null;
	    int x=message.indexOf("<DAMAGE>");
        if(x<0) x=message.indexOf("<DAMAGES>");
	    if(x<0) return message;
	    int y=Integer.MAX_VALUE;
	    int which=-1;
	    for(int i=0;i<TRANSLATE.length;i++)
	    {
	        int y1=message.indexOf(TRANSLATE[i][0],x);
	        if((y1>x)&&(y1<y)){ y=y1; which=i;}
	    }
	    if(which>=0)
	        message=message.substring(0,y)+TRANSLATE[which][1]+" "+loc+message.substring(y+TRANSLATE[which][0].length());
	    return message;
	}
	
	public boolean okMessage(Environmental host, CMMsg msg)
	{
	    if((msg.target()==affected)
	    &&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
	    &&(msg.value()>0)
	    &&(affected instanceof MOB)
	    &&(msg.targetMessage()!=null)
	    &&(CommonStrings.getIntVar(CommonStrings.SYSTEMI_INJPCTHP)>=(int)Math.round(Util.div(((MOB)affected).curState().getHitPoints(),((MOB)affected).maxState().getHitPoints())*100.0))
	    &&(msg.targetMessage().indexOf("<DAMAGE>")>=0)
	    &&(Dice.rollPercentage()<=CommonStrings.getIntVar(CommonStrings.SYSTEMI_INJPCTCHANCE)))
	    {
	        MOB mob=(MOB)affected;
	        Amputation A=(Amputation)mob.fetchEffect("Amputation");
	        if(A==null) A=new Amputation();
	        Vector remains=A.remainingLimbNameSet(mob);
	        if(mob.charStats().getBodyPart(Race.BODY_HEAD)>0)
		        remains.addElement("head");
	        if(mob.charStats().getBodyPart(Race.BODY_TORSO)>0)
		        remains.addElement("torso");
	        if(remains.size()>0)
	        {
		        int[] chances=new int[remains.size()]; 
		        int total=0;
			    for(int x=0;x<remains.size();x++)
			    {
				    int bodyPart=-1;
					for(int i=0;i<Race.BODY_PARTS;i++)
					{
				        if((" "+((String)remains.elementAt(x)).toUpperCase()).endsWith(" "+Race.BODYPARTSTR[i]))
				        { bodyPart=i; break;}
					}
				    if(bodyPart>=0)
				    {
				        int amount=INJURYCHANCE[bodyPart];
				        chances[x]+=amount;
				        total+=amount;
				    }
				}
				if(total>0)
				{
				    int randomRoll=Dice.roll(1,total,-1);
				    int chosenOne=-1;
					if((lastMsg!=null)
					&&(lastLoc!=null)
					&&((msg==lastMsg)||((lastMsg.trailerMsgs()!=null)&&(lastMsg.trailerMsgs().contains(msg))))
					&&(remains.contains(lastLoc)))
						chosenOne=remains.indexOf(lastLoc);
					else
					if((text().startsWith(msg.source().Name()+"/"))
					&&(remains.contains(text().substring(msg.source().Name().length()+1))))
					{
						chosenOne=remains.indexOf(text().substring(msg.source().Name().length()+1));
						setMiscText("");
					}
					else
					for(int i=0;i<chances.length;i++)
					{
					    if(chances[i]>0)
					    {
					        chosenOne=i;
					        randomRoll-=chances[i];
					        if(randomRoll<=0)
					            break;
					    }
					}
					int BodyPct=(int)Math.round(Util.div(msg.value(),mob.maxState().getHitPoints())*100.0);
					int LimbPct=BodyPct*CommonStrings.getIntVar(CommonStrings.SYSTEMI_INJMULTIPLIER);
					if(LimbPct<1) LimbPct=1;
					int bodyLoc=-1;
					for(int i=0;i<Race.BODY_PARTS;i++)
						if((" "+((String)remains.elementAt(chosenOne)).toUpperCase()).endsWith(" "+Race.BODYPARTSTR[i]))
					    { bodyLoc=i; break;}
					if(bodyLoc>=0)
					{
					    lastMsg=msg;
					    lastLoc=(String)remains.elementAt(chosenOne);
					    Vector bodyVec=injuries[bodyLoc];
					    if(bodyVec==null){ injuries[bodyLoc]=new Vector(); bodyVec=injuries[bodyLoc];}
					    int whichInjury=-1;
					    for(int i=0;i<bodyVec.size();i++)
					    {
					        Object[] O=(Object[])bodyVec.elementAt(i);
					        if(((String)O[0]).equalsIgnoreCase((String)remains.elementAt(chosenOne)))
					        { whichInjury=i; break;}
					    }
					    String newTarg=fixMessageString(msg.targetMessage(),((String)remains.elementAt(chosenOne)).toLowerCase());
					    if(!newTarg.equalsIgnoreCase(msg.targetMessage()))
					    {
					        msg.modify(msg.source(),msg.target(),msg.tool(),
					                msg.sourceCode(),fixMessageString(msg.sourceMessage(),((String)remains.elementAt(chosenOne)).toLowerCase()),
					                msg.targetCode(),newTarg,
					                msg.othersCode(),fixMessageString(msg.othersMessage(),((String)remains.elementAt(chosenOne)).toLowerCase()));
						    if(whichInjury<0)
						    {
						        Object[] O=new Object[2];
						        O[0]=((String)remains.elementAt(chosenOne)).toLowerCase();
						        O[1]=new Integer(LimbPct);
						        bodyVec.addElement(O);
						    }
						    else
						    {
						        Object[] O=(Object[])bodyVec.elementAt(whichInjury);
						        O[1]=new Integer(((Integer)O[1]).intValue()+LimbPct);
						        if(((Integer)O[1]).intValue()>100)
						            O[1]=new Integer(100);
						        if((((Integer)O[1]).intValue()>=100)
								||((BodyPct>5)
									&&((msg.tool() instanceof Electronics)||(BodyPct>=CommonStrings.getIntVar(CommonStrings.SYSTEMI_INJPCTHPAMP)))))
								{
								    boolean proceed=Dice.rollPercentage()<=CommonStrings.getIntVar(CommonStrings.SYSTEMI_INJPCTCHANCEAMP);
								    if(msg.tool() instanceof Weapon)
									{
										switch(((Weapon)msg.tool()).weaponType())
										{
										case Weapon.TYPE_FROSTING:
										case Weapon.TYPE_GASSING:
										    proceed=false;
											break;
										default:
											break;
										}
									}
						            if(Amputation.validamputees[bodyLoc]&&proceed)
				                    {
							            bodyVec.removeElement(O);
							            if(bodyVec.size()==0)
							                injuries[bodyLoc]=null;
							            Amputation.amputate(mob,A,((String)O[0]).toLowerCase());
							            if(mob.fetchEffect(A.ID())==null)
							                mob.addNonUninvokableEffect(A);
				                    }
						        }
						    }
					    }
					}
				}
	        }
	    }
	    return super.okMessage(host,msg);
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if((givenTarget!=null)&&(auto))
		{
		    if(givenTarget.fetchEffect(ID())!=null)
		        return false;
		    Ability A=(Ability)copyOf();
		    A.startTickDown(mob,mob,Integer.MAX_VALUE/2);
		    if((commands!=null)&&(commands.size()>0)&&(commands.firstElement() instanceof CMMsg))
		        return okMessage(mob,(CMMsg)commands.firstElement());
		    return true;
		}
		else
		    return super.invoke(mob,commands,givenTarget,auto,asLevel);
	}
}
