package com.planet_ink.coffee_mud.Abilities.Druid;
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
   Copyright 2002-2024 Bo Zimmerman

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
public class Druid_ShapeShift extends StdAbility
{
	@Override
	public String ID()
	{
		return "Druid_ShapeShift";
	}

	private final static String	localizedName	= CMLib.lang().L("Shape Shift");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_SHAPE_SHIFTING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	private static final String[]	triggerStrings	= I(new String[] { "SHAPESHIFT" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public long flags()
	{
		return super.flags() | Ability.FLAG_POLYMORPHING;
	}

	public int		myRaceCode	= -1;
	public int		myRaceLevel	= -1;
	public Race		newRace		= null;
	public String	raceName	= "";

	private String					uniqueFormFilename	= null;
	private List<ShiftShapeForm>	uniqueForm			= null;

	protected static class ShiftShapeForm
	{
		public String	ID;
		public String	form		= "";
		public double	attackAdj	= 0.0;
		public double	dmgAdj		= 0.0;
		public double	armorAdj	= 0.0;
		public double	speedAdj	= 0.0;
		public String[]	shapes		= new String[0];
		public String[]	raceIDs		= new String[0];

		public ShiftShapeForm(final String ID)
		{
			this.ID=ID;
		}
	}

	protected enum ShiftShapeField
	{
		NAME,
		ATTADJ,
		DMGADJ,
		ARMADJ,
		SPEEDADJ,
		SHAPES,
		RACES
	}

	private static final String buildShapeString(final ShiftShapeForm f)
	{
		final StringBuilder str = new StringBuilder(f.ID+";");
		for(final ShiftShapeField i : ShiftShapeField.values())
		{
			str.append(i.name()+"=");
			switch(i)
			{
			case NAME:
				str.append(f.form);
				break;
			case ATTADJ:
				str.append(f.attackAdj);
				break;
			case DMGADJ:
				str.append(f.dmgAdj);
				break;
			case ARMADJ:
				str.append(f.armorAdj);
				break;
			case SPEEDADJ:
				str.append(f.speedAdj);
				break;
			case SHAPES:
				str.append(CMParms.toListString(f.shapes));
				break;
			case RACES:
				str.append(CMParms.toListString(f.raceIDs));
				break;
			}
			str.append(";");
		}
		return str.toString();
	}

	private final String buildUniqueShapeStrings()
	{
		if(this.uniqueFormFilename!=null)
			return "[FILE="+this.uniqueFormFilename+"]";
		if(this.uniqueForm == null)
			return "";
		final StringBuilder str = new StringBuilder("");
		for(final ShiftShapeForm f : uniqueForm)
			str.append(buildShapeString(f));
		return str.toString();

	}

	private static final void fillShapeField(final ShiftShapeForm f, final String s)
	{
		if(f!=null)
		{
			final int x=s.indexOf('=');
			if(x>0)
			{
				final String fieldName = s.substring(0,x).toUpperCase().trim();
				final String fieldValue = s.substring(x+1).trim();
				final ShiftShapeField field = (ShiftShapeField)CMath.s_valueOf(ShiftShapeField.class, fieldName);
				if(field == null)
					Log.errOut("Druid_ShapeShift","Unknown field '"+fieldName+"' in shapeshift.txt");
				else
				{
					switch(field)
					{
					case ARMADJ:
						f.armorAdj=CMath.s_double(fieldValue);
						break;
					case ATTADJ:
						f.attackAdj=CMath.s_double(fieldValue);
						break;
					case DMGADJ:
						f.dmgAdj=CMath.s_double(fieldValue);
						break;
					case NAME:
						f.form=fieldValue;
						break;
					case RACES:
						f.raceIDs=CMParms.toStringArray(CMParms.parseCommas(fieldValue,true));
						break;
					case SHAPES:
						f.shapes=CMParms.toStringArray(CMParms.parseCommas(fieldValue,true));
						break;
					case SPEEDADJ:
						f.speedAdj=CMath.s_double(fieldValue);
						break;
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private final List<ShiftShapeForm> getShapeData()
	{
		if(uniqueForm != null)
			return uniqueForm;
		final String fileName = (this.uniqueFormFilename!=null)?this.uniqueFormFilename:"skills/shapeshift.txt";
		List<ShiftShapeForm> shapeData = (List<ShiftShapeForm>)Resources.getResource("DRUID_SHAPESHIFT_DATA: "+fileName);
		if(shapeData == null)
		{
			shapeData = new Vector<ShiftShapeForm>();
			final CMFile[] fileList = CMFile.getExistingExtendedFiles(Resources.makeFileResourceName(fileName),null,CMFile.FLAG_FORCEALLOW);
			final List<String> lines = new ArrayList<String>();
			for(final CMFile F : fileList)
				lines.addAll(Resources.getFileLineVector(Resources.getFileResource(F.getAbsolutePath(), true)));
			ShiftShapeForm f=null;
			for(String s : lines)
			{
				s=s.trim();
				if(s.length()>0)
				{
					if(s.startsWith("["))
					{
						f=new ShiftShapeForm(s);
						shapeData.add(f);
					}
					else
						fillShapeField(f,s);
				}
			}
			Resources.submitResource("DRUID_SHAPESHIFT_DATA: "+fileName,shapeData);
		}
		return shapeData;
	}

	@Override
	public String displayText()
	{
		if((myRaceCode<0)||(newRace==null))
			return super.displayText();
		return "(in "+newRace.name().toLowerCase()+" form)";
	}

	@Override
	public void setMiscText(final String newText)
	{
		if(newText.length()>0)
		{
			myRaceCode = -1;
			final String parm = newText.trim();
			if(parm.startsWith("["))
			{
				if(parm.startsWith("[FILE="))
				{
					final int x=parm.indexOf(']');
					this.uniqueFormFilename=parm.substring(6,x);
					if(CMath.isInteger(parm.substring(x+1)))
						myRaceCode=CMath.s_int(parm.substring(x+1));
					super.setMiscText(newText);
					return;
				}
				ShiftShapeForm f = null;
				final List<ShiftShapeForm> shapeData = new Vector<ShiftShapeForm>();
				for(final String line : CMParms.parseAny(parm,';',true))
				{
					if(line.startsWith("["))
					{
						f=new ShiftShapeForm(line);
						shapeData.add(f);
					}
					else
					if(CMath.isInteger(line))
						myRaceCode = CMath.s_int(line);
					else
						fillShapeField(f,line);
				}
				if(shapeData.size()>0)
				{
					this.uniqueForm = shapeData;
					if((shapeData.size()==1)
					&&(myRaceCode<0))
						myRaceCode = 0;
				}
			}
			else
			if(CMath.isInteger(parm))
				myRaceCode=CMath.s_int(parm);
		}
		super.setMiscText(newText);
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if((newRace!=null)&&(affected instanceof MOB)&&(myRaceCode>=0))
		{
			final PhyStats stats = affectableStats;
			final int xlvl=getXLEVELLevel(invoker());
			affectableStats.setName(CMLib.english().startWithAorAn(raceName.toLowerCase()));
			final int oldAdd=affectableStats.weight()-affected.basePhyStats().weight();
			final int raceCode = getRaceCode();
			final int maxRaceLevel = getMaxCharLevel(myRaceLevel);
			final int adjustedLevel = ((maxRaceLevel<affectableStats.level()) ? maxRaceLevel : affectableStats.level()) + xlvl;
			newRace.setHeightWeight(stats,((MOB)affected).charStats().reproductiveCode());
			if(oldAdd>0)
				stats.setWeight(stats.weight()+oldAdd);
			final ShiftShapeForm form=getShapeData().get(raceCode);
			stats.setAttackAdjustment(stats.attackAdjustment()+(int)Math.round(CMath.mul(adjustedLevel,form.attackAdj)/2.0));
			stats.setArmor(stats.armor()-(int)Math.round(CMath.mul(adjustedLevel,form.armorAdj)/2.0));
			stats.setDamage(stats.damage()+(int)Math.round(CMath.mul(adjustedLevel,form.dmgAdj)/2.0));
			stats.setSpeed(stats.speed()+(form.speedAdj * CMProps.getSpeedAdjustment() * (1.0+(xlvl/3.0))));
			//stats.setSensesMask(stats.sensesMask()|PhyStats.CAN_GRUNT_WHEN_STUPID);
		}
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(newRace!=null)
		{
			final int oldCat=affected.baseCharStats().ageCategory();
			if(affectableStats.getMyRace()!=newRace)
			{
				affectableStats.getMyRace().unaffectCharStats(affected, affectableStats);
				affectableStats.setMyRace(newRace);
				newRace.affectCharStats(affected, affectableStats);
			}
			affectableStats.setWearableRestrictionsBitmap(affectableStats.getWearableRestrictionsBitmap()|affectableStats.getMyRace().forbiddenWornBits());
			if(affected.baseCharStats().getStat(CharStats.STAT_AGE)>0)
				affectableStats.setStat(CharStats.STAT_AGE,newRace.getAgingChart()[oldCat]);
		}
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		super.unInvoke();
		if((canBeUninvoked())&&(mob.location()!=null))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> revert(s) to @x1 form.",mob.charStats().raceName().toLowerCase()));
			this.myRaceLevel=-1;
		}
	}

	public int getClassLevel(final MOB mob)
	{
		final int qualClassLevel=CMLib.ableMapper().qualifyingClassLevel(mob,this)+(2*getXLEVELLevel(mob));
		int classLevel=qualClassLevel-CMLib.ableMapper().qualifyingLevel(mob,this);
		if(qualClassLevel<0)
			classLevel=30;
		return classLevel;
	}

	public void setRaceName(final MOB mob)
	{
		final int classLevel=getClassLevel(mob);
		raceName=getRaceName(classLevel,myRaceCode);
		newRace=getRace(classLevel,myRaceCode);
	}

	public int getMaxRaceLevel(final int classLevel)
	{
		if(classLevel<6)
			return 0;
		else
		if(classLevel<12)
			return 1;
		else
		if(classLevel<18)
			return 2;
		else
		if(classLevel<24)
			return 3;
		else
			return 4;
	}

	public int getMaxCharLevel(final int raceLevel)
	{
		switch(raceLevel)
		{
		case 0:
			return 5;
		case 1:
			return 11;
		case 2:
			return 17;
		case 3:
			return 24;
		default:
			return 31;
		}
	}

	public int getRaceLevel(final MOB mob)
	{
		return getRaceLevel(getClassLevel(mob));
	}

	public int getRaceLevel(final int classLevel)
	{
		final int maxLevel=getMaxRaceLevel(classLevel);
		if((myRaceLevel<0)||(myRaceLevel>maxLevel))
			return maxLevel;
		return myRaceLevel;
	}

	public int getRaceCode()
	{
		if(myRaceCode<0)
			return 0;
		return myRaceCode;
	}

	public Race getRace(final int classLevel, final int raceCode)
	{
		final List<ShiftShapeForm> forms = getShapeData();
		return CMClass.getRace(forms.get(myRaceCode).raceIDs[getRaceLevel(classLevel)]);
	}

	public String getRaceName(final int classLevel, final int raceCode)
	{
		final List<ShiftShapeForm> forms = getShapeData();
		return forms.get(myRaceCode).shapes[getRaceLevel(classLevel)];
	}

	public static boolean isShapeShifted(final MOB mob)
	{
		if(mob==null)
			return false;
		for(final Enumeration<Ability> a=mob.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)
			&&((A instanceof Druid_ShapeShift)||(A instanceof Druid_Krakenform)))
				return true;
		}
		return false;
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if(mob!=null)
		{
			if(target instanceof MOB)
			{
				if((((MOB)target).isInCombat())
				&&(!Druid_ShapeShift.isShapeShifted((MOB)target)))
				{
					final int qualClassLevel=CMLib.ableMapper().qualifyingClassLevel(mob,this)+(2*getXLEVELLevel(mob));
					int classLevel=qualClassLevel-CMLib.ableMapper().qualifyingLevel(mob,this);
					if(qualClassLevel<0)
						classLevel=30;
					if(getRaceLevel(classLevel)>=3)
						return super.castingQuality(mob, target,Ability.QUALITY_BENEFICIAL_SELF);
				}
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		MOB targetM=mob;
		if((auto)&&(givenTarget instanceof MOB))
			targetM=(MOB)givenTarget;
		final Room R=targetM.location();
		if(R==null)
			return false;
		for(final Enumeration<Ability> a=targetM.personalEffects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)
			&&((A instanceof Druid_ShapeShift)||(A instanceof Druid_Krakenform)))
			{
				A.unInvoke();
				return true;
			}
		}

		this.myRaceLevel=-1;
		final List<ShiftShapeForm> forms = getShapeData();
		final int[] racesTaken=new int[forms.size()];
		final Druid_ShapeShift[] racesHandlerA=new Druid_ShapeShift[forms.size()];
		Vector<Druid_ShapeShift> allShapeshifts=new Vector<Druid_ShapeShift>();
		if((myRaceCode>=0)&&(myRaceCode<racesTaken.length))
			racesTaken[myRaceCode]++;

		for(int a=0;a<targetM.numAbilities();a++)
		{
			final Ability A=targetM.fetchAbility(a);
			if((A!=null)
			&&((A instanceof Druid_ShapeShift)))
			{
				final Druid_ShapeShift D=(Druid_ShapeShift)A;
				allShapeshifts.addElement(D);
			}
		}
		Collections.sort(allShapeshifts,new Comparator<Druid_ShapeShift>()
		{
			@Override
			public int compare(final Druid_ShapeShift o1, final Druid_ShapeShift o2)
			{
				return o1.ID().compareTo(o2.ID());
			}
		});
		for(final Ability A : allShapeshifts)
		{
			final Druid_ShapeShift D=(Druid_ShapeShift)A;
			if((D.myRaceCode>=0)
			&&(D.myRaceCode<racesTaken.length))
			{
				racesTaken[D.myRaceCode]++;
				racesHandlerA[D.myRaceCode]=D;
			}
		}
		if(myRaceCode<0)
		{
			if(targetM.isMonster() || (mob != targetM))
			{
				myRaceCode=CMLib.dice().roll(1,racesTaken.length,-1);
				final long t=System.currentTimeMillis();
				while((racesTaken[myRaceCode]>0)&&((System.currentTimeMillis()-t)<10000))
					myRaceCode=CMLib.dice().roll(1,racesTaken.length,-1);
			}
			else
			if(mob.isInCombat())
				return false;
			else
			{
				try
				{
					if(!mob.session().confirm(L("You have not yet chosen your form, would you like to now (Y/n)?"),"Y"))
						return false;
					while(!mob.session().isStopped())
					{
						final StringBuffer str=new StringBuffer(L("Choose from the following:\n\r"));
						final List<String> formNames=new ArrayList<String>();
						final Map<String,Integer> formMap=new Hashtable<String,Integer>();
						for(int i=0;i<forms.size();i++)
						{
							if(racesTaken[i]==0)
							{
								str.append(CMStrings.padLeft(""+(i+1),2)+") "+forms.get(i).form+"\n\r");
								formNames.add(forms.get(i).form.toLowerCase());
								formMap.put(forms.get(i).form.toLowerCase(), Integer.valueOf(i));
							}
						}
						str.append(L("Please select: "));
						final String choice=mob.session().prompt(str.toString(),"");
						if(choice.trim().length()==0)
						{
							mob.tell(L("Aborted."));
							return false;
						}
						if(CMath.isInteger(choice))
						{
							final int x=CMath.s_int(choice)-1;
							boolean found=false;
							for(final String key : formMap.keySet())
							{
								final Integer num=formMap.get(key);
								if(x == num.intValue())
								{
									myRaceCode = num.intValue();
									found=true;
									break;
								}
							}
							if(found)
								break;
						}
						else
						{
							int x=CMParms.indexOf(formNames.toArray(new String[0]), choice.toLowerCase().trim());
							if(x<0)
								x=CMParms.indexOfStartsWith(formNames.toArray(new String[0]), choice.toLowerCase().trim());
							if(x>=0)
							{
								myRaceCode = formMap.get(formNames.get(x)).intValue();
								break;
							}
						}
					}
				}
				catch (final Exception e)
				{
				}
			}
		}

		if(myRaceCode<0)
			return false;

		String parm=CMParms.combine(commands,0);
		if(parm.length()>0)
		{
			final int raceLevel=(racesHandlerA[myRaceCode]!=null)?racesHandlerA[myRaceCode].getRaceLevel(mob):0;
			for(int i1=raceLevel;i1>=0;i1--)
			{
				final String shape=forms.get(myRaceCode).shapes[i1];
				if(shape.equalsIgnoreCase(parm))
				{
					parm="";
					this.myRaceLevel=i1;
				}
			}
			if(parm.length()>0)
			{
				for(int i1=raceLevel;i1>=0;i1--)
				{
					final String shape=forms.get(myRaceCode).shapes[i1];
					if(CMLib.english().containsString(shape,parm))
					{
						parm="";
						this.myRaceLevel=i1;
					}
				}
			}
		}
		setMiscText(buildUniqueShapeStrings()+myRaceCode);
		setRaceName(mob);

		// now check for alternate shapeshifts
		if((triggerStrings().length>0)
		&&(parm.length()>0))
		{
			final Vector<Druid_ShapeShift> V=allShapeshifts;
			allShapeshifts=new Vector<Druid_ShapeShift>();
			while(V.size()>0)
			{
				Druid_ShapeShift choice=null;
				int sortByLevel=Integer.MAX_VALUE;
				for(int v=0;v<V.size();v++)
				{
					final Druid_ShapeShift A=V.elementAt(v);
					int lvl=CMLib.ableMapper().qualifyingLevel(mob,A);
					if(lvl<=0)
						lvl=CMLib.ableMapper().lowestQualifyingLevel(A.ID());
					lvl+=getXLEVELLevel(mob);
					if(lvl<sortByLevel)
					{
						sortByLevel=lvl;
						choice=A;
					}
				}
				if(choice==null)
					break;
				allShapeshifts.addElement(choice);
				V.removeElement(choice);
			}
			final StringBuffer list=new StringBuffer("");
			for(int i=0;i<allShapeshifts.size();i++)
			{
				final Druid_ShapeShift A=allShapeshifts.elementAt(i);
				if(A.myRaceCode>=0)
				{
					if((A.raceName==null)||(A.raceName.length()==0))
						A.setRaceName(mob);
					if((A.raceName==null)||(A.raceName.length()==0))
						list.append(CMStrings.padLeft(""+(i+1),2)+") Not yet chosen.\n\r");
					else
					{
						final String form=forms.get(A.myRaceCode).form;
						list.append(CMStrings.padLeft(""+(i+1),2)+") "+form+": ");
						final int raceLevel=A.getMaxRaceLevel(A.getClassLevel(mob));
						for(int i1=raceLevel;i1>=0;i1--)
						{
							final String shape=forms.get(A.myRaceCode).shapes[i1];
							list.append(shape);
							if(i1!=0)
								list.append(", ");
						}
						list.append("\n\r");
						if(CMLib.english().containsString(A.raceName,parm))
							return A.invoke(mob,new Vector<String>(),givenTarget,auto,asLevel);
						if(CMLib.english().containsString(form,parm))
							return A.invoke(mob,new Vector<String>(),givenTarget,auto,asLevel);
						for(int i1=raceLevel;i1>=0;i1--)
						{
							final String shape=forms.get(A.myRaceCode).shapes[i1];
							if(CMLib.english().containsString(shape,parm))
								return A.invoke(mob,new XVector<String>(parm),givenTarget,auto,asLevel);
						}
					}
				}
			}
			final int iparm=CMath.s_int(parm);
			if(iparm>0)
			{
				if(iparm<=allShapeshifts.size())
				{
					final Druid_ShapeShift A=allShapeshifts.elementAt(iparm-1);
					return A.invoke(mob,new Vector<String>(),givenTarget,auto,asLevel);
				}
			}
			if(parm.equalsIgnoreCase("LIST"))
				mob.tell(L("Valid forms include: \n\r@x1",list.toString()));
			else
				mob.tell(L("'@x1' is an illegal form!\n\rValid forms include: \n\r@x2",parm,list.toString()));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if((!appropriateToMyFactions(mob))&&(!auto))
		{
			if((CMLib.dice().rollPercentage()<50))
			{
				mob.tell(L("Extreme emotions disrupt your change."));
				return false;
			}
		}

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_OK_ACTION,null);
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				R.show(targetM,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> take(s) on @x1 form.",raceName.toLowerCase()));
				beneficialAffect(mob,targetM,asLevel,Ability.TICKS_FOREVER);
				raceName=CMStrings.capitalizeAndLower(CMLib.english().startWithAorAn(raceName.toLowerCase()));
				CMLib.utensils().confirmWearability(targetM);
			}
		}
		else
			beneficialWordsFizzle(mob,null,L("<S-NAME> chant(s) to <S-HIM-HERSELF>, but nothing happens."));

		// return whether it worked
		return success;
	}
}
