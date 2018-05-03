package com.planet_ink.coffee_mud.Behaviors;
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
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2018 Bo Zimmerman

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
	@Override
	public String ID()
	{
		return "WaterCurrents";
	}

	@Override
	protected int canImproveCode()
	{
		return Behavior.CAN_ROOMS | Behavior.CAN_AREAS;
	}

	protected String	dirs	= "";
	protected boolean	doBoats = false;

	public WaterCurrents()
	{
		super();
		minTicks=3;maxTicks=5;chance=75;
		tickReset();
	}

	@Override
	public String accountForYourself()
	{
		return "water current moving";
	}

	@Override
	public void setParms(String newParms)
	{
		super.setParms(newParms);
		final Vector<String> V=CMParms.parse(newParms);
		dirs="";
		for(int v=0;v<V.size();v++)
		{
			final String str=V.get(v);
			if(str.equalsIgnoreCase("BOATS"))
				this.doBoats=true;
			else
			{
				final int dir=CMLib.directions().getGoodDirectionCode(str);
				if(dir>=0)
					dirs=dirs+CMLib.directions().getDirectionChar(dir);
			}
		}
		if(dirs.length()==0)
			dirs="NE";
	}
	
	public void applyCurrents(Room R, Vector<Physical> done)
	{
		final Vector<Physical> todo=new Vector<Physical>();
		if((R!=null)&&(R.numInhabitants()>0))
		{
			MOB M=null;
			for(int m=0;m<R.numInhabitants();m++)
			{
				M=R.fetchInhabitant(m);
				if((M!=null)
				&&(!M.isMonster())
				&&(M.riding()==null)
				&&(!CMLib.flags().isInFlight(M))
				&&((!(M instanceof Rideable))||(((Rideable)M).numRiders()==0))
				&&(!M.isInCombat())
				&&(!CMLib.flags().isMobile(M))
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
				I=R.getItem(i);
				if((I!=null)
				&&(I.container()==null)
				&&((!(I instanceof Rideable))
					||(((Rideable)I).rideBasis()!=Rideable.RIDEABLE_WATER)
					||(((Rideable)I).numRiders()==0)
					||(doBoats))
				&&(!CMLib.flags().isInFlight(I))
				&&(!CMLib.flags().isMobile(I))
				&&((!(I instanceof Exit))||(doBoats))
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
				dir=CMLib.directions().getDirectionCode(""+dirs.charAt(dl));
				if(dir>=0)
				{
					R2=R.getRoomInDir(dir);
					if(R2!=null)
					{
						if((R.getExitInDir(dir)!=null)
						&&(R.getExitInDir(dir).isOpen())
						&&((R2.domainType()==R.domainType())
							||(CMLib.flags().isWateryRoom(R2))))
								break;
						R2=null;
					}
				}
			}
			if(R2!=null)
			{
				MOB M=null;
				Item I=null;
				MOB srcM=CMClass.getFactoryMOB("the water", 1, R2);
				for(int m=0;m<todo.size();m++)
				{
					if(todo.elementAt(m) instanceof MOB)
					{
						M=(MOB)todo.elementAt(m);
						final CMMsg themsg=CMClass.getMsg(srcM,M,new AWaterCurrent(),CMMsg.MSG_OK_ACTION,L("<T-NAME> <T-IS-ARE> swept @x1 by the current.",
								CMLib.directions().getDirectionName(dir).toLowerCase()));
						if(R.okMessage(M,themsg))
						{
							R.send(M,themsg);
							R2.bringMobHere(M,true);
							R2.showOthers(srcM,M,new AWaterCurrent(),CMMsg.MSG_OK_ACTION,L("<T-NAME> <T-IS-ARE> swept in from @x1 by the current.",
									CMLib.directions().getFromCompassDirectionName(Directions.getOpDirectionCode(dir)).toLowerCase()));
							CMLib.commands().postLook(M,true);
						}
					}
					else
					if(todo.elementAt(m) instanceof Item)
					{
						I=(Item)todo.elementAt(m);
						if(R.show(srcM,I,new AWaterCurrent(),CMMsg.MSG_OK_ACTION,L("@x1 is swept @x2 by the current.",
								I.name(),CMLib.directions().getDirectionName(dir).toLowerCase())))
						{
							if(I instanceof BoardableShip)
							{
								for(Enumeration<Room> r = ((BoardableShip)I).getShipArea().getProperMap();r.hasMoreElements();)
								{
									final Room R3=r.nextElement();
									if((R3!=null)&&((R3.domainType()&Room.INDOORS)==0))
										R3.showHappens(CMMsg.MSG_OK_ACTION, L("@x1 is swept @x2 by the current.",I.name(),CMLib.directions().getDirectionName(dir).toLowerCase()));
								}
							}
							R2.moveItemTo(I,ItemPossessor.Expire.Player_Drop);
							R2.showOthers(srcM,I,new AWaterCurrent(),CMMsg.MSG_OK_ACTION,L("@x1 is swept in from @x2 by the current.",
									I.name(),CMLib.directions().getFromCompassDirectionName(Directions.getOpDirectionCode(dir)).toLowerCase()));
						}
					}
				}
				srcM.destroy();
			}
		}
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(canAct(ticking,tickID))
		{
			final Vector<Physical> sweeps=new Vector<Physical>();
			if(ticking instanceof Room)
			{
				final Room R=(Room)ticking;
				applyCurrents(R,sweeps);
				final Room below=R.rawDoors()[Directions.DOWN];
				if((below!=null)
				&&(below.roomID().length()==0)
				&&(below instanceof GridLocale)
				&&((below.domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
				   ||(below.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)))
				{
					for (final Room R2 : ((GridLocale)below).getAllRooms())
					{
						applyCurrents(R2,sweeps);
					}
				}
			}
			else
			if(ticking instanceof Area)
			{
				for(final Enumeration<Room> r=((Area)ticking).getMetroMap();r.hasMoreElements();)
				{
					final Room R=r.nextElement();
					if(CMLib.flags().isWateryRoom(R))
						applyCurrents(R,sweeps);
				}
			}
		}
		return true;
	}

	protected static final String[] empty={};
	protected static final String[] CODES={"CLASS","TEXT"};
	
	protected static class AWaterCurrent implements Ability, Cloneable
	{
		public AWaterCurrent()
		{
			super();
			// CMClass.bumpCounter(this,CMClass.CMObjectType.ABILITY);//removed
			// for mem & perf
		}

		@Override
		public String ID()
		{
			return "AWaterCurrent";
		}

		@Override
		public String name()
		{
			return "a water current";
		}

		@Override
		public String Name()
		{
			return name();
		}

		@Override
		public String description()
		{
			return "";
		}

		@Override
		public String displayText()
		{
			return "";
		}

		protected boolean	savable		= true;
		protected String	miscText	= "";
		protected Physical	affected	= null;

		protected int canAffectCode()
		{
			return 0;
		}

		protected int canTargetCode()
		{
			return 0;
		}

		@Override
		public boolean canTarget(int can_code)
		{
			return false;
		}

		@Override
		public boolean canAffect(int can_code)
		{
			return false;
		}

		@Override
		public double castingTime(final MOB mob, final List<String> cmds)
		{
			return 0.0;
		}

		@Override
		public double combatCastingTime(final MOB mob, final List<String> cmds)
		{
			return 0.0;
		}

		@Override
		public double checkedCastingCost(final MOB mob, final List<String> cmds)
		{
			return 0.0;
		}

		@Override
		public void initializeClass()
		{
		}

		@Override
		public int abilityCode()
		{
			return 0;
		}

		@Override
		public void setAbilityCode(int newCode)
		{
		}

		@Override
		public int adjustedLevel(MOB mob, int asLevel)
		{
			return -1;
		}

		@Override
		public boolean bubbleAffect()
		{
			return false;
		}

		@Override
		public ExpertiseLibrary.SkillCost getTrainingCost(MOB mob)
		{
			return CMLib.expertises().createNewSkillCost(ExpertiseLibrary.CostType.TRAIN, Double.valueOf(1.0));
		}

		@Override
		public String L(final String str, final String... xs)
		{
			return CMLib.lang().fullSessionTranslation(str, xs);
		}

		@Override
		public long flags()
		{
			return Ability.FLAG_TRANSPORTING;
		}

		@Override
		public int getTickStatus()
		{
			return Tickable.STATUS_NOT;
		}

		@Override
		public int usageType()
		{
			return 0;
		}

		// protected void finalize(){
		// CMClass.unbumpCounter(this,CMClass.CMObjectType.ABILITY); }//removed
		// for mem & perf
		@Override
		public long expirationDate()
		{
			return 0;
		}

		@Override
		public void setExpirationDate(long time)
		{
		}

		@Override
		public void setName(String newName)
		{
		}

		@Override
		public void setDescription(String newDescription)
		{
		}

		@Override
		public void setDisplayText(String newDisplayText)
		{
		}

		@Override
		public String image()
		{
			return "";
		}

		@Override
		public String rawImage()
		{
			return "";
		}

		@Override
		public void setImage(String newImage)
		{
		}

		@Override
		public MOB invoker()
		{
			return null;
		}

		@Override
		public void setInvoker(MOB mob)
		{
		}

		@Override
		public String[] triggerStrings()
		{
			return empty;
		}

		@Override
		public boolean preInvoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel, int secondsElapsed, double actionsRemaining)
		{
			return true;
		}

		@Override
		public boolean invoke(MOB mob, List<String> commands, Physical target, boolean auto, int asLevel)
		{
			return false;
		}

		@Override
		public boolean invoke(MOB mob, Physical target, boolean auto, int asLevel)
		{
			return false;
		}

		@Override
		public boolean autoInvocation(MOB mob, boolean force)
		{
			return false;
		}

		@Override
		public void unInvoke()
		{
		}

		@Override
		public boolean canBeUninvoked()
		{
			return false;
		}

		@Override
		public boolean isAutoInvoked()
		{
			return true;
		}

		@Override
		public boolean isNowAnAutoEffect()
		{
			return true;
		}

		@Override
		public List<String> externalFiles()
		{
			return null;
		}

		@Override
		public boolean canBeTaughtBy(MOB teacher, MOB student)
		{
			return false;
		}

		@Override
		public boolean canBePracticedBy(MOB teacher, MOB student)
		{
			return false;
		}

		@Override
		public boolean canBeLearnedBy(MOB teacher, MOB student)
		{
			return false;
		}

		@Override
		public void teach(MOB teacher, MOB student)
		{
		}

		@Override
		public void practice(MOB teacher, MOB student)
		{
		}

		@Override
		public int maxRange()
		{
			return Integer.MAX_VALUE;
		}

		@Override
		public int minRange()
		{
			return Integer.MIN_VALUE;
		}

		@Override
		public void startTickDown(MOB invokerMOB, Physical affected, int tickTime)
		{
			if (affected.fetchEffect(ID()) == null)
				affected.addEffect(this);
		}

		@Override
		public int proficiency()
		{
			return 0;
		}

		@Override
		public void setProficiency(int newProficiency)
		{
		}

		@Override
		public boolean proficiencyCheck(MOB mob, int adjustment, boolean auto)
		{
			return false;
		}

		@Override
		public void helpProficiency(MOB mob, int adjustment)
		{
		}

		@Override
		public Physical affecting()
		{
			return affected;
		}

		@Override
		public void setAffectedOne(Physical P)
		{
			affected = P;
		}

		@Override
		public boolean putInCommandlist()
		{
			return false;
		}

		@Override
		public int abstractQuality()
		{
			return Ability.QUALITY_INDIFFERENT;
		}

		@Override
		public int enchantQuality()
		{
			return Ability.QUALITY_INDIFFERENT;
		}

		@Override
		public int castingQuality(MOB mob, Physical target)
		{
			return Ability.QUALITY_INDIFFERENT;
		}

		@Override
		public int classificationCode()
		{
			return Ability.ACODE_PROPERTY;
		}

		@Override
		public boolean isSavable()
		{
			return savable;
		}

		@Override
		public void setSavable(boolean truefalse)
		{
			savable = truefalse;
		}

		protected boolean	amDestroyed	= false;

		@Override
		public void destroy()
		{
			amDestroyed = true;
		}

		@Override
		public boolean amDestroyed()
		{
			return amDestroyed;
		}

		@Override
		public CMObject newInstance()
		{
			try
			{
				return this.getClass().newInstance();
			}
			catch (final Exception e)
			{
				Log.errOut(ID(), e);
			}
			return new AWaterCurrent();
		}

		@Override
		public int getSaveStatIndex()
		{
			return getStatCodes().length;
		}

		@Override
		public String[] getStatCodes()
		{
			return CODES;
		}

		@Override
		public boolean isStat(String code)
		{
			return CMParms.indexOf(getStatCodes(), code.toUpperCase().trim()) >= 0;
		}

		protected int getCodeNum(String code)
		{
			for(int i=0;i<CODES.length;i++)
			{
				if(code.equalsIgnoreCase(CODES[i]))
					return i;
			}
			return -1;
		}

		@Override
		public String getStat(String code)
		{
			switch(getCodeNum(code))
			{
			case 0:
				return ID();
			case 1:
				return text();
			}
			return "";
		}

		@Override
		public void setStat(String code, String val)
		{
			switch(getCodeNum(code))
			{
			case 0:
				return;
			case 1:
				setMiscText(val);
				break;
			}
		}
		
		@Override
		public boolean sameAs(Environmental E)
		{
			if(!(E instanceof AWaterCurrent))
				return false;
			final String[] codes=getStatCodes();
			for(int i=0;i<codes.length;i++)
			{
				if(!E.getStat(codes[i]).equals(getStat(codes[i])))
					return false;
			}
			return true;
		}

		protected void cloneFix(Ability E)
		{
		}

		@Override
		public CMObject copyOf()
		{
			try
			{
				final AWaterCurrent E=(AWaterCurrent)this.clone();
				//CMClass.bumpCounter(E,CMClass.CMObjectType.ABILITY);//removed for mem & perf
				E.cloneFix(this);
				return E;

			}
			catch(final CloneNotSupportedException e)
			{
				return this.newInstance();
			}
		}

		@Override
		public int compareTo(CMObject o)
		{
			return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
		}

		@Override
		public void setMiscText(String newMiscText)
		{
			miscText = newMiscText;
		}

		@Override
		public String text()
		{
			return miscText;
		}

		@Override
		public String miscTextFormat()
		{
			return CMParms.FORMAT_UNDEFINED;
		}

		@Override
		public boolean appropriateToMyFactions(MOB mob)
		{
			return true;
		}

		@Override
		public String accountForYourself()
		{
			return "";
		}

		@Override
		public String requirements(MOB mob)
		{
			return "";
		}

		@Override
		public boolean canAffect(Physical P)
		{
			if((P==null)&&(canAffectCode()==0))
				return true;
			if(P==null)
				return false;
			if((P instanceof MOB)&&((canAffectCode()&Ability.CAN_MOBS)>0))
				return true;
			if((P instanceof Item)&&((canAffectCode()&Ability.CAN_ITEMS)>0))
				return true;
			if((P instanceof Exit)&&((canAffectCode()&Ability.CAN_EXITS)>0))
				return true;
			if((P instanceof Room)&&((canAffectCode()&Ability.CAN_ROOMS)>0))
				return true;
			if((P instanceof Area)&&((canAffectCode()&Ability.CAN_AREAS)>0))
				return true;
			return false;
		}

		@Override
		public boolean canTarget(Physical P)
		{
			return false;
		}

		@Override
		public void affectPhyStats(Physical affected, PhyStats affectableStats)
		{
		}

		@Override
		public void affectCharStats(MOB affectedMob, CharStats affectableStats)
		{
		}

		@Override
		public void affectCharState(MOB affectedMob, CharState affectableMaxState)
		{
		}

		@Override
		public void executeMsg(final Environmental myHost, final CMMsg msg)
		{
			return;
		}

		@Override
		public boolean okMessage(final Environmental myHost, final CMMsg msg)
		{
			return true;
		}

		@Override
		public boolean tick(Tickable ticking, int tickID)
		{
			return true;
		}

		@Override
		public void makeLongLasting()
		{
		}

		@Override
		public void makeNonUninvokable()
		{
		}

		protected static final int[]	cost	= new int[3];

		@Override
		public int[] usageCost(MOB mob, boolean ignoreClassOverride)
		{
			return cost;
		}

		@Override
		public boolean isGeneric()
		{
			return false;
		}
	}
}
