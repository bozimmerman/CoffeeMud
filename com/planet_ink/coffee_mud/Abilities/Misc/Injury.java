package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2014 Bo Zimmerman

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

@SuppressWarnings({"unchecked","rawtypes"})
public class Injury extends StdAbility implements HealthCondition
{
	@Override public String ID() { return "Injury"; }
	private final static String localizedName = CMLib.lang().L("Injury");
	@Override public String name() { return localizedName; }

	protected CMMsg lastMsg=null;
	protected String lastLoc=null;
	public int lastHP=-1;
	//public final static String[] BODYPARTSTR={
	//    "ANTENEA","EYE","EAR","HEAD","NECK","ARM","HAND","TORSO","LEG","FOOT",
	//    "NOSE","GILL","MOUTH","WAIST","TAIL","WING"};
	public final static int[] INJURYCHANCE={
		3,3,3,11,3,12,5,35,13,5,3,0,0,3,3,3};

	@Override
	public String getHealthConditionDesc()
	{
		final StringBuffer buf=new StringBuffer("");
		Object[] O=null;
		Vector V=null;
		try
		{
			if(injuries!=null)
				for(int i=0;i<Race.BODY_PARTS;i++)
				{
					V=injuries[i];
					if(V!=null)
					for(int i2=0;i2<V.size();i2++)
					{
						O=(Object[])V.elementAt(i2);
						String wounds="";
						final int dmg = ((Integer)O[1]).intValue();
						if (dmg<5)
							wounds=("a bruised ");
						else if (dmg<10)
							wounds=("a scratched ");
						else if (dmg<20)
							wounds=("a cut ");
						else if (dmg<30)
							wounds=("a sliced ");
						else if (dmg<40)
							wounds=("a gashed ");
						else if (dmg<60)
							wounds=("a bloody ");
						else if ((dmg<75)||(i==Race.BODY_TORSO))
							wounds=("a mangled ");
						else if ((dmg<100)||(i==Race.BODY_HEAD))
							wounds=("a dangling ");
						else
							wounds=("a shredded ");
						buf.append(", "+wounds+((String)O[0]).toLowerCase()+" ("+dmg+"%)");
					}
				}
		}
		catch(final Exception e){}
		if(buf.length()==0) return "";
		return buf.substring(1);
	}

	@Override
	public String displayText()
	{
		final String buf=getHealthConditionDesc();
		if(buf.length()==0) return "";
		return "(Injuries:"+buf+")";
	}
	@Override protected int canAffectCode(){return CAN_MOBS;}
	@Override protected int canTargetCode(){return CAN_MOBS;}
	@Override public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	@Override public boolean putInCommandlist(){return false;}
	@Override public boolean canBeUninvoked(){return true;}
	@Override public int classificationCode(){return Ability.ACODE_PROPERTY;}
	@Override public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	public Vector[] injuries=new Vector[Race.BODY_PARTS];

	@Override
	public void unInvoke()
	{
		final Environmental E=affected;
		super.unInvoke();
		if((E instanceof MOB)&&(canBeUninvoked())&&(!((MOB)E).amDead()))
			((MOB)E).tell(L("Your injuries are healed."));
	}

	@Override
	public String text()
	{
		Vector V=null;
		Object[] O=null;
		final StringBuffer buf=new StringBuffer("");
		if(injuries!=null)
			for(int i=0;i<Race.BODY_PARTS;i++)
			{
				V=injuries[i];
				if(V!=null)
				for(int i2=0;i2<V.size();i2++)
				{
					O=(Object[])V.elementAt(i2);
					buf.append(i+":"+((String)O[0]).toLowerCase()+":"+((Integer)O[1]).intValue()+";");
				}
			}
		return buf.toString();
	}

	@Override
	public void setMiscText(String txt)
	{
		if(txt.startsWith("+"))
		{
			if(affected instanceof MOB)
			{
				final MOB mob=(MOB)affected;
				txt=txt.substring(1);
				final int x=txt.indexOf('=');
				if(x<0)
					return;
				final String chosenName=txt.substring(0,x);
				final String amount=txt.substring(x+1);
				Amputation A=(Amputation)mob.fetchEffect("Amputation");
				if(A==null) A=new Amputation();
				final List<String> remains=A.remainingLimbNameSet(mob);
				if(mob.charStats().getBodyPart(Race.BODY_HEAD)>0)
					remains.add("head");
				if(mob.charStats().getBodyPart(Race.BODY_TORSO)>0)
					remains.add("torso");
				final int chosenOne=remains.indexOf(chosenName);
				if(chosenOne<0)
					return;
				if(injuries==null)
					injuries=new Vector[Race.BODY_PARTS];
				int bodyLoc=-1;
				for(int i=0;i<Race.BODY_PARTS;i++)
					if((" "+remains.get(chosenOne).toUpperCase()).endsWith(" "+Race.BODYPARTSTR[i]))
					{ bodyLoc=i; break;}
				if(bodyLoc>=0)
				{
					Vector bodyVec=injuries[bodyLoc];
					if(bodyVec==null){ injuries[bodyLoc]=new Vector(); bodyVec=injuries[bodyLoc];}
					int whichInjury=-1;
					for(int i=0;i<bodyVec.size();i++)
					{
						final Object[] O=(Object[])bodyVec.elementAt(i);
						if(((String)O[0]).equalsIgnoreCase(remains.get(chosenOne)))
						{ whichInjury=i; break;}
					}
					Object[] O=null;
					if(whichInjury<0)
					{
						O=new Object[2];
						O[0]=remains.get(chosenOne).toLowerCase();
						O[1]=Integer.valueOf(0);
						bodyVec.addElement(O);
						whichInjury=bodyVec.size()-1;
					}
					O=(Object[])bodyVec.elementAt(whichInjury);
					O[1]=Integer.valueOf(((Integer)O[1]).intValue()+CMath.s_int(amount));
					if(((Integer)O[1]).intValue()>100)
						O[1]=Integer.valueOf(100);
				}
			}
		}
		else
		if(txt.indexOf('/')>0)
			super.setMiscText(txt);
		else
		{
			injuries=new Vector[Race.BODY_PARTS];
			final List<String> sets=CMParms.parseSemicolons(txt,true);
			for(int s=0;s<sets.size();s++)
			{
				final String set=sets.get(s);
				final List<String> V=CMParms.parseAny(set,':',false);
				if(V.size()==3)
				{
					final int part=CMath.s_int(V.get(0));
					if((part>=0)&&(part<Race.BODY_PARTS))
					{
						final String msg=V.get(1);
						final int hurt=CMath.s_int(V.get(V.size()-1));
						if(injuries[part]==null)
							injuries[part] = new Vector();
						injuries[part].addElement(new Object[]{msg,Integer.valueOf(hurt)});
					}
				}
			}
		}
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			if(lastHP<0)
				lastHP=mob.curState().getHitPoints();
		}
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			final MOB mob=(MOB)affected;
			if(mob.curState().getHitPoints()>=mob.maxState().getHitPoints())
			{
				for(int i=0;i<injuries.length;i++)
					injuries[i]=null;
				unInvoke();
			}
			else
			if((mob.curState().getHitPoints()>lastHP)&&(lastHP>=0))
			{
				final Vector choicesToHeal=new Vector();
				for(int i=0;i<injuries.length;i++)
					if(injuries[i]!=null)
						for(int x=0;x<injuries[i].size();x++)
						{
							final int[] choice=new int[2];
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
					int pct=(int)Math.round(CMath.div(mob.curState().getHitPoints()-lastHP,mob.maxState().getHitPoints())*100.0);
					if(pct<=0) pct=1;
					int tries=100;
					while((pct>0)&&((--tries)>0)&&(choicesToHeal.size()>0))
					{
						final int which=CMLib.dice().roll(1,choicesToHeal.size(),-1);
						final int[] choice=(int[])choicesToHeal.elementAt(which);
						if(choice[0]<injuries.length)
						{
							final Vector V=injuries[choice[0]];
							if((V!=null)&&(choice[1]<V.size()))
							{
								final Object[] O=(Object[])V.elementAt(choice[1]);
								if(pct>((Integer)O[1]).intValue())
								{
									V.removeElement(O);
									if(V.size()==0) injuries[choice[0]]=null;
									pct-=((Integer)O[1]).intValue();
									choicesToHeal.removeElementAt(which);
								}
								else
								{
									O[1]=Integer.valueOf(((Integer)O[1]).intValue()-pct);
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

	public static String[][] TRANSLATE=
	{
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
			final int y1=message.indexOf(TRANSLATE[i][0],x);
			if((y1>x)&&(y1<y)){ y=y1; which=i;}
		}
		if(which>=0)
			message=message.substring(0,y)+TRANSLATE[which][1]+" "+loc+message.substring(y+TRANSLATE[which][0].length());
		return message;
	}

	@Override
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if((msg.target()==affected)
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.value()>0)
		&&(msg.target() instanceof MOB)
		&&(msg.targetMessage()!=null)
		&&(msg.targetMessage().indexOf("<DAMAGE>")>=0)
		&&(super.miscText.startsWith(msg.source().Name()+"/")
		   ||((CMProps.getIntVar(CMProps.Int.INJPCTHP)>=(int)Math.round(CMath.div(((MOB)msg.target()).curState().getHitPoints(),((MOB)msg.target()).maxState().getHitPoints())*100.0))
			&&(CMLib.dice().rollPercentage()<=CMProps.getIntVar(CMProps.Int.INJPCTCHANCE)))))
		{
			final MOB mob=(MOB)msg.target();
			Amputation A=(Amputation)mob.fetchEffect("Amputation");
			if(A==null) A=new Amputation();
			final List<String> remains=A.remainingLimbNameSet(mob);
			if(mob.charStats().getBodyPart(Race.BODY_HEAD)>0)
				remains.add("head");
			if(mob.charStats().getBodyPart(Race.BODY_TORSO)>0)
				remains.add("torso");
			if(remains.size()>0)
			{
				final int[] chances=new int[remains.size()];
				int total=0;
				for(int x=0;x<remains.size();x++)
				{
					int bodyPart=-1;
					for(int i=0;i<Race.BODY_PARTS;i++)
					{
						if((" "+remains.get(x).toUpperCase()).endsWith(" "+Race.BODYPARTSTR[i]))
						{ bodyPart=i; break;}
					}
					if(bodyPart>=0)
					{
						final int amount=INJURYCHANCE[bodyPart];
						chances[x]+=amount;
						total+=amount;
					}
				}
				if(total>0)
				{
					int randomRoll=CMLib.dice().roll(1,total,-1);
					int chosenOne=-1;
					if((lastMsg!=null)
					&&(lastLoc!=null)
					&&((msg==lastMsg)||((lastMsg.trailerMsgs()!=null)&&(lastMsg.trailerMsgs().contains(msg))))
					&&(remains.contains(lastLoc)))
						chosenOne=remains.indexOf(lastLoc);
					else
					if((super.miscText.startsWith(msg.source().Name()+"/"))
					&&(remains.contains(super.miscText.substring(msg.source().Name().length()+1))))
					{
						chosenOne=remains.indexOf(super.miscText.substring(msg.source().Name().length()+1));
						super.miscText="";
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
					final int BodyPct=(int)Math.round(CMath.div(msg.value(),mob.maxState().getHitPoints())*100.0);
					int LimbPct=BodyPct*CMProps.getIntVar(CMProps.Int.INJMULTIPLIER);
					if(LimbPct<1) LimbPct=1;
					int bodyLoc=-1;
					for(int i=0;i<Race.BODY_PARTS;i++)
						if((" "+remains.get(chosenOne).toUpperCase()).endsWith(" "+Race.BODYPARTSTR[i]))
						{ bodyLoc=i; break;}
					if(bodyLoc>=0)
					{
						lastMsg=msg;
						lastLoc=remains.get(chosenOne);
						Vector bodyVec=injuries[bodyLoc];
						if(bodyVec==null){ injuries[bodyLoc]=new Vector(); bodyVec=injuries[bodyLoc];}
						int whichInjury=-1;
						for(int i=0;i<bodyVec.size();i++)
						{
							final Object[] O=(Object[])bodyVec.elementAt(i);
							if(((String)O[0]).equalsIgnoreCase(remains.get(chosenOne)))
							{ whichInjury=i; break;}
						}
						final String newTarg=fixMessageString(msg.targetMessage(),remains.get(chosenOne).toLowerCase());
						if(!newTarg.equalsIgnoreCase(msg.targetMessage()))
						{
							msg.modify(msg.source(),msg.target(),msg.tool(),
									msg.sourceCode(),fixMessageString(msg.sourceMessage(),remains.get(chosenOne).toLowerCase()),
									msg.targetCode(),newTarg,
									msg.othersCode(),fixMessageString(msg.othersMessage(),remains.get(chosenOne).toLowerCase()));
							Object[] O=null;
							if(whichInjury<0)
							{
								O=new Object[2];
								O[0]=remains.get(chosenOne).toLowerCase();
								O[1]=Integer.valueOf(0);
								bodyVec.addElement(O);
								whichInjury=bodyVec.size()-1;
							}
							O=(Object[])bodyVec.elementAt(whichInjury);
							O[1]=Integer.valueOf(((Integer)O[1]).intValue()+LimbPct);
							if(((Integer)O[1]).intValue()>100)
								O[1]=Integer.valueOf(100);
							if((((Integer)O[1]).intValue()>=100)
							||((BodyPct>5)
								&&((msg.tool() instanceof Electronics)||(BodyPct>=CMProps.getIntVar(CMProps.Int.INJPCTHPAMP)))))
							{
								boolean proceed=(CMLib.dice().rollPercentage()<=CMProps.getIntVar(CMProps.Int.INJPCTCHANCEAMP))
												&&(mob.phyStats().level()>=CMProps.getIntVar(CMProps.Int.INJMINLEVEL));
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
									if(A.amputate(mob,A,((String)O[0]).toLowerCase())!=null)
									{
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

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((givenTarget!=null)&&(auto))
		{
			if(givenTarget.fetchEffect(ID())!=null)
				return false;
			super.tickDown=2;
			Ability A=(Ability)copyOf();
			A.startTickDown(mob,givenTarget,Ability.TICKS_ALMOST_FOREVER);
			if((commands!=null)&&(commands.size()>0)&&(commands.firstElement() instanceof CMMsg))
			{
				A=givenTarget.fetchEffect(ID());
				if(A!=null)
					return A.okMessage(mob,(CMMsg)commands.firstElement());
				return false;
			}
			return true;
		}
		return super.invoke(mob,commands,givenTarget,auto,asLevel);
	}
}
