package com.planet_ink.coffee_mud.Abilities.Misc;

import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
	
	public void setMiscText(String text)
	{
		injuries=new Vector[Race.BODY_PARTS];
		Vector V=null;
		Vector semiList=Util.parseSemicolons(text(),true);
		for(int v=0;v<semiList.size();v++)
		{
			String s=(String)semiList.elementAt(v);
			V=Util.parse(s);
			int amount=0;
			String leftRight="";
			if((V.size()>1)&&(Util.isNumber((String)V.lastElement())))
			{ 
			    amount=Util.s_int((String)V.lastElement()); 
				V.removeElementAt(V.size()-1);
			}
			if((V.size()>1)
			&&(((String)V.firstElement()).equalsIgnoreCase("left")
		        ||((String)V.firstElement()).equalsIgnoreCase("left")))
			{ 
			    leftRight=((String)V.firstElement())+" "; 
				V.removeElementAt(0);
			}
			s=Util.combine(V,0);
			int code=-1;
			for(int r=0;r<Race.BODYPARTSTR.length;r++)
				if(Race.BODYPARTSTR[r].equalsIgnoreCase(s))
				{ 
				    code=r; 
				    break;
				}
			if((code>=0)&&(amount>0))
		    {
			    if(injuries[code]==null) 
			        injuries[code]=new Vector();
			    Object[] data=new Object[2];
			    data[0]=(leftRight.toUpperCase()+" "+Race.BODYPARTSTR[code]).trim().toLowerCase();
			    data[1]=new Integer(amount);
			    injuries[code].addElement(data);
		    }
		}
	}
	
	public String text()
	{
		StringBuffer buf=new StringBuffer("");
		Object[] O=null;
		Vector V=null;
		if(injuries!=null)
		for(int i=0;i<Race.BODY_PARTS;i++)
		{
		    V=injuries[i];
		    if(V!=null)
			    for(int i2=0;i<V.size();i2++)
				{
				    O=(Object[])V.elementAt(i2);
					buf.append(((String)O[0]).toLowerCase()+" "+((Integer)O[1]).intValue());
				}
		}
		return buf.toString();
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
	    &&(Util.div(((MOB)affected).maxState().getHitPoints(),((MOB)affected).curState().getHitPoints())>=2.5)
	    &&(msg.targetMessage().indexOf("<DAMAGE>")>=0))
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
					BodyPct*=4; // works out to 160%
					int bodyLoc=-1;
					for(int i=0;i<Race.BODY_PARTS;i++)
						if((" "+((String)remains.elementAt(chosenOne)).toUpperCase()).endsWith(" "+Race.BODYPARTSTR[i]))
					    { bodyLoc=i; break;}
					if(bodyLoc>=0)
					{
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
						        O[1]=new Integer(BodyPct);
						        bodyVec.addElement(O);
						    }
						    else
						    {
						        Object[] O=(Object[])bodyVec.elementAt(whichInjury);
						        O[1]=new Integer(((Integer)O[1]).intValue()+BodyPct);
						        if(((Integer)O[1]).intValue()>100)
						            O[1]=new Integer(100);
						        if(((Integer)O[1]).intValue()>=100)
						        {
						            if(Amputation.validamputees[bodyLoc])
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
