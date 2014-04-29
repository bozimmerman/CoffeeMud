package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
/*
   Copyright 2000-2014 Bo Zimmerman

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
public interface ExpertiseLibrary extends CMLibrary
{
	public static final int XFLAG_X1=0;
	public static final int XFLAG_X2=1;
	public static final int XFLAG_X3=2;
	public static final int XFLAG_X4=3;
	public static final int XFLAG_X5=4;
	public static final int XFLAG_LEVEL=5;
	public static final int XFLAG_TIME=6;
	public static final int XFLAG_MAXRANGE=7;
	public static final int XFLAG_LOWCOST=8;
	public static final int XFLAG_XPCOST=9;
	public static final int NUM_XFLAGS=10;
	public static final String[] XFLAG_CODES={
		"X1","X2","X3","X4","X5",
		"LEVEL","TIME","MAXRANGE","LOWCOST",
		"XPCOST"
	};
	public static class ExpertiseDefinition implements CMObject
	{
		public String 	ID="";
		public String 	name="";
		public String 	baseName="";
		public String[] data=new String[0];
		private String 	uncompiledListMask="";
		private String 	uncompiledFinalMask="";
		private int 	minLevel=Integer.MIN_VALUE+1;
		private MaskingLibrary.CompiledZapperMask compiledListMask=null;
		public ExpertiseDefinition parent=null;
		private MaskingLibrary.CompiledZapperMask compiledFinalMask=null;

		@Override
		public String name()
		{
			return name;
		}
		public int getMinimumLevel()
		{
			if(minLevel==Integer.MIN_VALUE+1)
				minLevel=CMLib.masking().minMaskLevel(allRequirements(),0);
			return minLevel;
		}
		public String[] getData()
		{
			return data;
		}
		public MaskingLibrary.CompiledZapperMask compiledListMask()
		{
			if((this.compiledListMask==null)&&(uncompiledListMask.length()>0))
			{
				compiledListMask=CMLib.masking().maskCompile(uncompiledListMask);
				CMLib.ableMapper().addPreRequisites(ID,new Vector<String>(),uncompiledListMask.trim());
			}
			return this.compiledListMask;
		}
		public MaskingLibrary.CompiledZapperMask compiledFinalMask()
		{
			if((this.compiledFinalMask==null)&&(uncompiledFinalMask.length()>0))
			{
				this.compiledFinalMask=CMLib.masking().maskCompile(uncompiledFinalMask);
				CMLib.ableMapper().addPreRequisites(ID,new Vector<String>(),uncompiledFinalMask.trim());
			}
			return this.compiledFinalMask;
		}
		public String allRequirements()
		{
			String req=uncompiledListMask;
			if(req==null) req=""; else req=req.trim();
			if((uncompiledFinalMask!=null)&&(uncompiledFinalMask.length()>0))
				req=req+" "+uncompiledFinalMask;
			return req.trim();
		}
		public String listRequirements(){return uncompiledListMask;}
		public String finalRequirements(){return uncompiledFinalMask;}
		public void addListMask(String mask)
		{
			if((mask==null)||(mask.length()==0)) return;
			if(uncompiledListMask==null)
				uncompiledListMask=mask;
			else
				uncompiledListMask+=mask;
			compiledListMask=null;
		}
		public void addFinalMask(String mask)
		{
			if((mask==null)||(mask.length()==0)) return;
			if(uncompiledFinalMask==null)
				uncompiledFinalMask=mask;
			else
				uncompiledFinalMask+=mask;
			compiledFinalMask=CMLib.masking().maskCompile(uncompiledFinalMask);
			CMLib.ableMapper().addPreRequisites(ID,new Vector<String>(),uncompiledFinalMask.trim());
		}

		private final List<SkillCost> costs=new LinkedList<SkillCost>();
		public void addCost(CostType type, Double value)
		{
			costs.add(new SkillCost(type,value));
		}
		public String costDescription()
		{
			final StringBuffer costStr=new StringBuffer("");
			for(final SkillCost cost : costs)
				costStr.append(cost.requirements(null)).append(", ");
			if(costStr.length()==0) return "";
			return costStr.substring(0,costStr.length()-2);
		}
		public boolean meetsCostRequirements(MOB mob)
		{
			for(final SkillCost cost : costs)
				if(!cost.doesMeetCostRequirements(mob))
					return false;
			return true;
		}
		public void spendCostRequirements(MOB mob)
		{
			for(final SkillCost cost : costs)
				cost.spendSkillCost(mob);
		}
		@Override public int compareTo(CMObject o) { return (o==this)?0:1; }
		@Override public String ID() { return ID; }
		@Override public CMObject newInstance() { return this; }
		@Override public CMObject copyOf() { return this; }
		@Override public void initializeClass() {}
		public String _(final String str, final String ... xs) { return CMLib.lang().fullSessionTranslation(str, xs); }
	}

	/** Enumeration of the types of costs of gaining this ability */
	public enum    CostType
	{
		TRAIN,
		PRACTICE,
		XP,
		GOLD,
		QP;
	}

	/**
	 * Class for the definition of the cost of a skill
	 * @author Bo Zimmerman
	 */
	public class SkillCostDefinition
	{
		public final CostType type;
		public final String costDefinition;

		public SkillCostDefinition(final CostType type, final String costDefinition)
		{
			this.type=type;
			this.costDefinition=costDefinition;
		}
	}

	/**
	 * Class for the cost of a skill, or similar things perhaps
	 * @author Bo Zimmerman
	 */
	public class SkillCost
	{
		public final Double   value;
		public final CostType costType;

		public SkillCost(final CostType costType, final Double value)
		{
			this.value=value;
			this.costType=costType;
		}

		/**
		 * Returns a simple description of the Type of
		 * this cost.  A MOB and sample value is required for
		 * money currencies.
		 * @param mob MOB, for GOLD type currency eval
		 * @return the type of currency
		 */
		public String costType(final MOB mob)
		{
			final String ofWhat;
			switch(costType)
			{
			case XP: ofWhat="experience points"; break;
			case GOLD: ofWhat=CMLib.beanCounter().getDenominationName(mob, value.doubleValue()); break;
			case PRACTICE: ofWhat="practice points"; break;
			case QP: ofWhat="quest points"; break;
			default: ofWhat=CMLib.english().makePlural(costType.name().toLowerCase()); break;
			}
			return ofWhat;
		}

		public String requirements(final MOB mob)
		{
			switch(costType)
			{
			case XP: return value.intValue()+" XP";
			case QP: return value.intValue()+" quest pts";
			case GOLD:
				if(mob==null)
					return CMLib.beanCounter().abbreviatedPrice("", value.doubleValue());
				else
					return CMLib.beanCounter().abbreviatedPrice(mob, value.doubleValue());
			default: return value.intValue()+" "
						   +((value.intValue()==1)
								   ?costType.name().toLowerCase()
								   :CMLib.english().makePlural(costType.name().toLowerCase()));
			}
		}

		/**
		 * Returns whether the given mob meets the given cost requirements.
		 * @param student the student to check
		 * @return true if it meets, false otherwise
		 */
		public boolean doesMeetCostRequirements(final MOB student)
		{
			switch(costType)
			{
			case XP: return student.getExperience() >= value.intValue();
			case GOLD: return CMLib.beanCounter().getTotalAbsoluteNativeValue(student) >= value.doubleValue();
			case TRAIN: return student.getTrains() >= value.intValue();
			case PRACTICE: return student.getPractices() >= value.intValue();
			case QP: return student.getQuestPoint() >= value.intValue();
			}
			return false;
		}

		/**
		 * Expends the given cost upon the given student
		 * @param student the student to check
		 */
		public void spendSkillCost(final MOB student)
		{
			switch(costType)
			{
			case XP:	   CMLib.leveler().postExperience(student, null, "", value.intValue(), true); break;
			case GOLD:     CMLib.beanCounter().subtractMoney(student, value.doubleValue()); break;
			case TRAIN:    student.setTrains(student.getTrains()-value.intValue()); break;
			case PRACTICE: student.setPractices(student.getPractices()-value.intValue()); break;
			case QP:	   student.setQuestPoint(student.getQuestPoint()-value.intValue()); break;
			}
		}
	}

	public ExpertiseDefinition addDefinition(String ID, String name, String baseName, String listMask, String finalMask, String[] costs, String[] data);
	public void delDefinition(String ID);
	public ExpertiseDefinition getDefinition(String ID);
	public ExpertiseDefinition findDefinition(String ID, boolean exactOnly);
	public Enumeration<ExpertiseDefinition> definitions();
	public List<ExpertiseDefinition> myQualifiedExpertises(MOB mob);
	public List<ExpertiseDefinition> myListableExpertises(MOB mob);
	public int numExpertises();
	public void recompileExpertises();
	public String getExpertiseHelp(String ID, boolean exact);
	public String getApplicableExpertise(String ID, int code);
	public int getApplicableExpertiseLevel(String ID, int code, MOB mob);
	public int getStages(String baseExpertiseCode);
	public List<String> getStageCodes(String baseExpertiseCode);
	public String confirmExpertiseLine(String row, String ID, boolean addIfPossible);
	public List<String> getPeerStageCodes(final String expertiseCode);
	public String getGuessedBaseExpertiseName(final String expertiseCode);
	public void handleBeingTaught(MOB teacher, MOB student, Environmental item, String msg);
	public boolean canBeTaught(MOB teacher, MOB student, Environmental item, String msg);
	public boolean postTeach(MOB teacher, MOB student, CMObject teachObj);
}
