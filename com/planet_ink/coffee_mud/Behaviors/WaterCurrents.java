package com.planet_ink.coffee_mud.Behaviors;

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
public class WaterCurrents extends ActiveTicker
{
	public String ID(){return "WaterCurrents";}
	protected int canImproveCode(){return Behavior.CAN_ROOMS|Behavior.CAN_AREAS;}
	protected String dirs="";

	public WaterCurrents()
	{
		minTicks=3;maxTicks=5;chance=75;
		tickReset();
	}



	public void setParms(String newParms)
	{
		super.setParms(newParms);
		Vector V=Util.parse(newParms);
		dirs="";
		for(int v=0;v<V.size();v++)
		{
			int dir=Directions.getGoodDirectionCode((String)V.elementAt(v));
			if(dir>=0) dirs=dirs+Directions.getDirectionChar(dir);
		}
		if(dirs.length()==0)
			dirs="NE";
	}
	public void applyCurrents(Room R, Vector done)
	{
        Vector todo=new Vector();
		if((R!=null)&&(R.numInhabitants()>0))
		{
		    MOB M=null;
			for(int m=0;m<R.numInhabitants();m++)
			{
				M=R.fetchInhabitant(m);
				if((M!=null)
				&&(!M.isMonster())
				&&(M.riding()==null)
				&&(!Sense.isInFlight(M))
				&&((!(M instanceof Rideable))||(((Rideable)M).numRiders()==0))
				&&(!M.isInCombat())
				&&(!done.contains(M)))
                {
                    todo.addElement(M);
					done.addElement(M);
                }
			}
		}
		if((R!=null)&&(R.numItems()>0))
		{
		    Item I=null;
			for(int i=0;i<R.numItems();i++)
			{
				I=R.fetchItem(i);
				if((I!=null)
				&&(I.container()==null)
				&&((!(I instanceof Rideable))
				        ||(((Rideable)I).rideBasis()!=Rideable.RIDEABLE_WATER)
				        ||(((Rideable)I).numRiders()==0))
				&&(!Sense.isInFlight(I))
				&&(!done.contains(I)))
                {
                    todo.addElement(I);
				    done.addElement(I);
                }
			}
		}
		if((todo.size()>0)&&(R!=null))
		{
			int dir=-1;
			Room R2=null;
			for(int dl=0;dl<dirs.length();dl++)
			{
				dir=Directions.getDirectionCode(""+dirs.charAt(dl));
				if(dir>=0)
				{
					R2=R.getRoomInDir(dir);
					if(R2!=null)
					{
						if((R.getExitInDir(dir)!=null)
						&&(R.getExitInDir(dir).isOpen())
						&&((R2.domainType()==R.domainType())
							||(R2.domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
							||(R2.domainType()==Room.DOMAIN_INDOORS_WATERSURFACE)
							||(R2.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
							||(R2.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)))
								break;
						R2=null;
					}
				}
			}
			if(R2!=null)
			{
			    MOB M=null;
			    Item I=null;
				for(int m=0;m<todo.size();m++)
				{
				    if(todo.elementAt(m) instanceof MOB)
				    {
						M=(MOB)todo.elementAt(m);
                        FullMsg themsg=new FullMsg(M,M,new AWaterCurrent(),CMMsg.MSG_OK_VISUAL,"<S-NAME> <S-IS-ARE> swept "+Directions.getDirectionName(dir).toLowerCase()+" by the current.");
                        if(R.okMessage(M,themsg))
						{
                            R.send(M,themsg);
							R2.bringMobHere(M,false);
							R2.showOthers(M,null,new AWaterCurrent(),CMMsg.MSG_OK_VISUAL,"<S-NAME> <S-IS-ARE> swept in from "+Directions.getFromDirectionName(Directions.getOpDirectionCode(dir)).toLowerCase()+" by the current.");
							CommonMsgs.look(M,true);
						}
				    }
				    else
				    if(todo.elementAt(m) instanceof Item)
				    {
						I=(Item)todo.elementAt(m);
						R.showHappens(CMMsg.MSG_OK_VISUAL,I.name()+" is swept "+Directions.getDirectionName(dir).toLowerCase()+" by the current.");
						R2.bringItemHere(I,Item.REFUSE_PLAYER_DROP);
						R2.showHappens(CMMsg.MSG_OK_VISUAL,I.name()+" is swept in from "+Directions.getFromDirectionName(Directions.getOpDirectionCode(dir)).toLowerCase()+" by the current.");
				    }
				}
			}
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(canAct(ticking,tickID))
		{
			Vector sweeps=new Vector();
			if(ticking instanceof Room)
			{
				Room R=(Room)ticking;
				applyCurrents(R,sweeps);
				Room below=R.rawDoors()[Directions.DOWN];
				if((below!=null)
				&&(below.roomID().length()==0)
				&&(below instanceof GridLocale)
				&&((below.domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
				   ||(below.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)))
				{
					Vector V=((GridLocale)below).getAllRooms();
					for(int v=0;v<V.size();v++)
					{
						Room R2=(Room)V.elementAt(v);
						applyCurrents(R2,sweeps);
					}
				}
			}
			else
			if(ticking instanceof Area)
			{
				for(Enumeration r=((Area)ticking).getMetroMap();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					if((R.domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
					||(R.domainType()==Room.DOMAIN_INDOORS_WATERSURFACE)
					||(R.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
					||(R.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
						applyCurrents(R,sweeps);
				}
			}
		}
		return true;
	}
	protected static final String[] empty={};
	protected static final EnvStats envStats=new DefaultEnvStats();
	protected static final String[] CODES={"CLASS","TEXT"};
	protected static final int[] cost=new int[3];
	private class AWaterCurrent implements Ability, Cloneable
	{
		public String ID() { return "AWaterCurrent"; }
		public String name(){ return "a water current";}
		public String Name(){return name();}
		public String description(){return "";}
		public String displayText(){return "";}
		protected boolean borrowed=false;
		protected String miscText="";
		protected Environmental affected=null;
		protected int canAffectCode(){return 0;}
		protected int canTargetCode(){return 0;}
		public int castingTime(){return 0;}
		public int combatCastingTime(){return 0;}
		public int abilityCode(){return 0;}
		public void setAbilityCode(int newCode){}
		public int adjustedLevel(MOB mob, int asLevel){return -1;}
		public boolean bubbleAffect(){return false;}
		public long flags(){return Ability.FLAG_TRANSPORTING;}
		public long getTickStatus(){return Tickable.STATUS_NOT;}
		public int usageType(){return 0;}

		public void setName(String newName){}
		public void setDescription(String newDescription){}
		public void setDisplayText(String newDisplayText){}
		public String image(){return "";}
		public void setImage(String newImage){}
		public MOB invoker(){return null;}
		public void setInvoker(MOB mob){}
		public String[] triggerStrings(){return empty;}
        public boolean preInvoke(MOB mob, Vector commands, Environmental target, boolean auto, int asLevel){return true;}
		public boolean invoke(MOB mob, Vector commands, Environmental target, boolean auto, int asLevel){return false;}
		public boolean invoke(MOB mob, Environmental target, boolean auto, int asLevel){return false;}
		public boolean autoInvocation(MOB mob){return false;}
		public void unInvoke(){}
		public boolean canBeUninvoked(){return false;}
		public boolean isAutoInvoked(){return true;}
		public boolean isNowAnAutoEffect(){return true;}
		public Vector externalFiles(){return null;}

		public boolean canBeTaughtBy(MOB teacher, MOB student){return false;}
		public boolean canBePracticedBy(MOB teacher, MOB student){return false;}
		public boolean canBeLearnedBy(MOB teacher, MOB student){return false;}
		public void teach(MOB teacher, MOB student){}
		public void practice(MOB teacher, MOB student){}
		public int maxRange(){return Integer.MAX_VALUE;}
		public int minRange(){return Integer.MIN_VALUE;}

		public void startTickDown(MOB invokerMOB, Environmental affected, int tickTime)
		{
			if(affected.fetchEffect(ID())==null)
				affected.addEffect(this);
		}

		public int profficiency(){return 0;}
		public void setProfficiency(int newProfficiency){}
		public boolean profficiencyCheck(MOB mob, int adjustment, boolean auto){return false;}
		public void helpProfficiency(MOB mob){}

		public Environmental affecting(){return affected;}
		public void setAffectedOne(Environmental being){affected=being;}

		public boolean putInCommandlist(){return false;}
		public int quality(){return Ability.INDIFFERENT;}

		public int classificationCode(){ return Ability.PROPERTY;}
		public boolean isBorrowed(Environmental toMe){ return borrowed;	}
		public void setBorrowed(Environmental toMe, boolean truefalse)	{ borrowed=truefalse; }

		public EnvStats envStats(){return envStats;}
		public EnvStats baseEnvStats(){return envStats;}

		public void recoverEnvStats(){}
		public void setBaseEnvStats(EnvStats newBaseEnvStats){}
		public Environmental newInstance()
		{
			try{
				return (Environmental)this.getClass().newInstance();
			}
			catch(Exception e)
			{
				Log.errOut(ID(),e);
			}
			return new AWaterCurrent();
		}

		public String[] getStatCodes(){return CODES;}
		private int getCodeNum(String code){
			for(int i=0;i<CODES.length;i++)
				if(code.equalsIgnoreCase(CODES[i])) return i;
			return -1;
		}
		public String getStat(String code){
			switch(getCodeNum(code))
			{
			case 0: return ID();
			case 1: return text();
			}
			return "";
		}
		public void setStat(String code, String val)
		{
			switch(getCodeNum(code))
			{
			case 0: return;
			case 1: setMiscText(val); break;
			}
		}
		public boolean sameAs(Environmental E)
		{
			if(!(E instanceof AWaterCurrent)) return false;
			for(int i=0;i<CODES.length;i++)
				if(!E.getStat(CODES[i]).equals(getStat(CODES[i])))
					return false;
			return true;
		}
		private void cloneFix(Ability E){}

		public Environmental copyOf()
		{
			try
			{
			    AWaterCurrent E=(AWaterCurrent)this.clone();
				E.cloneFix(this);
				return E;

			}
			catch(CloneNotSupportedException e)
			{
				return this.newInstance();
			}
		}

		public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

		public void setMiscText(String newMiscText)
		{ miscText=newMiscText;}
		public String text()
		{ return miscText;}
		public boolean appropriateToMyFactions(MOB mob){return true;}
		public String accountForYourself(){return "";}
		public int affectType(){return 0;}
		public String requirements(){return "";}

		public boolean canAffect(Environmental E)
		{
			if((E==null)&&(canAffectCode()==0)) return true;
			if(E==null) return false;
			if((E instanceof MOB)&&((canAffectCode()&Ability.CAN_MOBS)>0)) return true;
			if((E instanceof Item)&&((canAffectCode()&Ability.CAN_ITEMS)>0)) return true;
			if((E instanceof Exit)&&((canAffectCode()&Ability.CAN_EXITS)>0)) return true;
			if((E instanceof Room)&&((canAffectCode()&Ability.CAN_ROOMS)>0)) return true;
			if((E instanceof Area)&&((canAffectCode()&Ability.CAN_AREAS)>0)) return true;
			return false;
		}

		public boolean canTarget(Environmental E)
		{ return false;}

		public void affectEnvStats(Environmental affected, EnvStats affectableStats)
		{}
		public void affectCharStats(MOB affectedMob, CharStats affectableStats)
		{}
		public void affectCharState(MOB affectedMob, CharState affectableMaxState)
		{}
		public void executeMsg(Environmental myHost, CMMsg msg)
		{
			return;
		}
		public boolean okMessage(Environmental myHost, CMMsg msg)
		{
			return true;
		}
		public boolean tick(Tickable ticking, int tickID)
		{ return true;	}
		public void makeLongLasting(){}
		public void makeNonUninvokable(){}
		public int[] usageCost(MOB mob){return cost;}


		public void addEffect(Ability to){}
		public void addNonUninvokableEffect(Ability to){}
		public void delEffect(Ability to){}
		public int numEffects(){ return 0;}
		public Ability fetchEffect(int index){return null;}
		public Ability fetchEffect(String ID){return null;}
		public void addBehavior(Behavior to){}
		public void delBehavior(Behavior to){}
		public int numBehaviors(){return 0;}
		public Behavior fetchBehavior(int index){return null;}
		public Behavior fetchBehavior(String ID){return null;}
		public boolean isGeneric(){return false;}
	}
}
