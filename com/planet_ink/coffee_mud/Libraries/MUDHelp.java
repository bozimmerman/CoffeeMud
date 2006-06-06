package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

/* 
   Copyright 2000-2006 Bo Zimmerman

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
public class MUDHelp extends StdLibrary implements HelpLibrary
{
    public String ID(){return "MUDHelp";}
    
    public boolean isPlayerSkill(String helpStr)
    {
        if(helpStr.length()==0) return false;
        if(getHelpFile().size()==0) return false;
		helpStr=helpStr.toUpperCase().trim();
		if(helpStr.indexOf(" ")>=0)
			helpStr=helpStr.replace(' ','_');
        if(helpStr.startsWith("SPELL_")
		 ||helpStr.startsWith("SONG_")
		 ||helpStr.startsWith("DANCE_")
		 ||helpStr.startsWith("BEHAVIOR_")
		 ||helpStr.startsWith("CHANT_")
		 ||helpStr.startsWith("PRAYER_")
		 ||helpStr.startsWith("SKILL_")
		 ||helpStr.startsWith("BEHAVIOR_")
		 ||helpStr.startsWith("PROP_"))
        	return true;
		String thisTag=getHelpFile().getProperty(helpStr);
		if((thisTag!=null)
		&&(thisTag.startsWith("<ABILITY>")||thisTag.startsWith("<EXPERTISE>")))
			return true;
		return CMClass.getAbility(helpStr)!=null;
    }
    
    public StringBuffer getHelpText(String helpStr, MOB forMOB, boolean favorAHelp)
    {
        if(helpStr.length()==0) return null;
        StringBuffer thisTag=null;
        if(favorAHelp)
        {
            if(getArcHelpFile().size()>0)
                thisTag=getHelpText(helpStr,getArcHelpFile(),forMOB);
            if(thisTag==null)
            {
                if(getHelpFile().size()==0) return null;
                thisTag=getHelpText(helpStr,getHelpFile(),forMOB);
            }
        }
        else
        {
            if(getHelpFile().size()>0)
                thisTag=getHelpText(helpStr,getHelpFile(),forMOB);
            if(thisTag==null)
            {
                if(getArcHelpFile().size()==0) return null;
                thisTag=getHelpText(helpStr,getArcHelpFile(),forMOB);
            }
        }
        return thisTag;
    }

	public Vector getTopics(boolean archonHelp, boolean standardHelp)
	{
        Vector reverseList=new Vector();
		Properties rHelpFile=null;
		if(archonHelp)
			rHelpFile=getArcHelpFile();
		if(standardHelp)
		{
			if(rHelpFile==null)
				rHelpFile=getHelpFile();
			else
			{
				for(Enumeration e=rHelpFile.keys();e.hasMoreElements();)
				{
					String ptop = (String)e.nextElement();
					String thisTag=rHelpFile.getProperty(ptop);
					if ((thisTag==null)||(thisTag.length()==0)||(thisTag.length()>=35)
						|| (rHelpFile.getProperty(thisTag)== null) )
							reverseList.addElement(ptop);
				}
				rHelpFile=getHelpFile();
			}
		}
		if(rHelpFile!=null)
		for(Enumeration e=rHelpFile.keys();e.hasMoreElements();)
		{
			String ptop = (String)e.nextElement();
			String thisTag=rHelpFile.getProperty(ptop);
			if ((thisTag==null)||(thisTag.length()==0)||(thisTag.length()>=35)
				|| (rHelpFile.getProperty(thisTag)== null) )
					reverseList.addElement(ptop);
		}
		Collections.sort(reverseList);
		return reverseList;
	}
	
	public String getActualUsage(Ability A, int which, MOB forMOB)
	{
        boolean destroymob=false;
		if(forMOB==null)
        {
            destroymob=true;
			forMOB=CMClass.getMOB("StdMOB");
			forMOB.maxState().setMana(Integer.MAX_VALUE/2);
			forMOB.maxState().setMovement(Integer.MAX_VALUE/2);
			forMOB.maxState().setHitPoints(Integer.MAX_VALUE/2);
		}

		int[] consumption=A.usageCost(forMOB);
		int whichConsumed=consumption[0];
		switch(which)
		{
		case Ability.USAGE_MOVEMENT: whichConsumed=consumption[1]; break;
		case Ability.USAGE_MANA: whichConsumed=consumption[0]; break;
		case Ability.USAGE_HITPOINTS: whichConsumed=consumption[2]; break;
		}
        if(destroymob) forMOB.destroy();
		if(whichConsumed==Integer.MAX_VALUE/2) return "all";
		return ""+whichConsumed;
	}

	private void appendAllowed(StringBuffer prepend, String ID)
	{
		Vector allows=CMLib.ableMapper().getAllowsList(ID);
		ExpertiseLibrary.ExpertiseDefinition def=null;
		Ability A=null;
		if((allows!=null)&&(allows.size()>0))
		{
			prepend.append("\n\rAllows   : ");
			int lastLine=11;
			for(int a=0;a<allows.size();a++)
			{
				String allowStr=(String)allows.elementAt(a);
				def=CMLib.expertises().getDefinition(allowStr);
				if(def!=null)
				{
					prepend.append(def.name+" ");
					lastLine+=(def.name.length()+1);
				}
				else
				{
					A=CMClass.getAbility(allowStr);
					if(A!=null)
					{
						prepend.append(A.Name()+" ");
						lastLine+=(A.Name().length()+1);
					}
				}
				if((lastLine>60)&&(a<(allows.size()-1)))
				{
					lastLine=11;
					prepend.append("\n\rAllows   : ");
				}
			}
		}
	}
	
	public String fixHelp(String tag, String str, MOB forMOB)
	{
		if(str.startsWith("<EXPERTISE>"))
		{
			str=str.substring(11);
			ExpertiseLibrary.ExpertiseDefinition def=null;
			for(Enumeration e=CMLib.expertises().definitions();e.hasMoreElements();)
			{
				def=(ExpertiseLibrary.ExpertiseDefinition)e.nextElement();
				if(def.name.toUpperCase().replace(' ','_').equals(tag.toUpperCase()))
				{
					StringBuffer prepend=new StringBuffer("");
					prepend.append("\n\rExpertise: "+def.name);
					prepend.append("\n\rRequires : "+CMLib.masking().maskDesc(def.allRequirements(),true));
					appendAllowed(prepend,def.ID);
					str=prepend.toString()+"\n\r"+str;
				}
			}
		}
		if(str.startsWith("<ABILITY>"))
		{
			str=str.substring(9);
			String name=tag;
			int type=-1;
			if(name.startsWith("SPELL_"))
			{
				type=Ability.ACODE_SPELL;
				name=name.substring(6);
			}
			else
			if(name.startsWith("PRAYER_"))
			{
				type=Ability.ACODE_PRAYER;
				name=name.substring(7);
			}
			else
			if(name.startsWith("DANCE_"))
			{
				type=Ability.ACODE_SONG;
				name=name.substring(6);
			}
			else
			if((name.startsWith("SONG_"))
			||(name.startsWith("PLAY_")))
			{
				type=Ability.ACODE_SONG;
				name=name.substring(5);
			}
			else
			if(name.startsWith("CHANT_"))
			{
				type=Ability.ACODE_CHANT;
				name=name.substring(6);
			}
			name=name.replace('_',' ');
			Vector helpedPreviously=new Vector();
			String subTag=tag;
			while(subTag.indexOf("_")!=subTag.lastIndexOf("_"))
			{
			    int x=subTag.lastIndexOf("_");
			    subTag=subTag.substring(0,x)+subTag.substring(x+1);
			}
			
			for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
			{
				Ability A=(Ability)a.nextElement();
				if(((A.ID().equalsIgnoreCase(tag)||A.ID().equalsIgnoreCase(subTag))
						&&((type<0)||(type==(A.classificationCode()&Ability.ALL_ACODES)))
					||(A.name().equalsIgnoreCase(name)))
				&&(!helpedPreviously.contains(A)))
				{
					helpedPreviously.addElement(A);
					StringBuffer prepend=new StringBuffer("");
					type=(A.classificationCode()&Ability.ALL_ACODES);
					prepend.append("\n\r");
					switch(type)
					{
					case Ability.ACODE_SPELL:
						prepend.append(CMStrings.padRight("Spell",9));
						break;
					case Ability.ACODE_PRAYER:
						prepend.append(CMStrings.padRight("Prayer",9));
						break;
					case Ability.ACODE_CHANT:
						prepend.append(CMStrings.padRight("Chant",9));
						break;
					case Ability.ACODE_SONG:
						prepend.append(CMStrings.padRight("Song",9));
						break;
					default:
						prepend.append(CMStrings.padRight("Skill",9));
						break;
					}
					prepend.append(": "+A.name());
					if(type==Ability.ACODE_SPELL)
					{
						prepend.append("\n\rSchool   : ");
						int school=(A.classificationCode()&Ability.ALL_DOMAINS)>>5;
						prepend.append(CMStrings.capitalizeAndLower(Ability.DOMAIN_DESCS[school]));
					}
					Vector avail=new Vector();
					Hashtable sortedByLevel=new Hashtable();
					for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
					{
						CharClass C=(CharClass)c.nextElement();
						int lvl=CMLib.ableMapper().getQualifyingLevel(C.ID(),true,A.ID());
						if((!C.ID().equalsIgnoreCase("Archon"))
						&&(lvl>=0)
						&&(C.availabilityCode()!=0)
						&&(!CMLib.ableMapper().getSecretSkill(C.ID(),true,A.ID())))
						{
							if(!sortedByLevel.containsKey(new Integer(lvl)))
								sortedByLevel.put(new Integer(lvl),new int[1]);
							((int[])sortedByLevel.get(new Integer(lvl)))[0]++;
							avail.addElement(C.name(lvl)+"("+lvl+")");
						}
					}
					for(Enumeration e=sortedByLevel.keys();e.hasMoreElements();)
					{
						Integer I=(Integer)e.nextElement();
						if(((int[])sortedByLevel.get(I))[0]>2)
						{
							String s=null;
							for(int i=avail.size()-1;i>=0;i--)
							{
								s=(String)avail.elementAt(i);
								if(s.endsWith("("+I.intValue()+")"))
									avail.removeElementAt(i);
							}
							if(((int[])sortedByLevel.get(I))[0]>5)
								avail.addElement("Numerous Classes("+I.intValue()+")");
							else
								avail.addElement("Several Classes("+I.intValue()+")");
						}
					}
					for(int c=0;c<avail.size();c++)
					{
						if((c%4)==0)
							prepend.append("\n\rAvailable: ");
						prepend.append(((String)avail.elementAt(c))+" ");
					}
					DVector preReqs=CMLib.ableMapper().getCommonPreRequisites(A);
					if(preReqs.size()>0)
					{
						String names=CMLib.ableMapper().formatPreRequisites(preReqs);
						prepend.append("\n\rRequires : "+names);
					}
					String mask=CMLib.ableMapper().getCommonExtraMask(A);
					if((mask!=null)&&(mask.length()>0))
						prepend.append("\n\rRequires : "+CMLib.masking().maskDesc(mask,true));
					appendAllowed(prepend,A.ID());
					if(type==Ability.ACODE_PRAYER)
					{
					    for(Enumeration e=CMLib.factions().factionSet().elements();e.hasMoreElements();)
					    {
					        Faction F=(Faction)e.nextElement();
					        if(F.usageFactors(A).length()>0)
					            prepend.append("\n\r"+CMStrings.capitalizeAndLower(F.name())+": "+F.usageFactors(A));
					    }
					}
					
					if(!A.isAutoInvoked())
					{
						prepend.append("\n\rUse Cost : ");
						if(A.usageType()==Ability.USAGE_NADA)
							prepend.append("None");
						if(CMath.bset(A.usageType(),Ability.USAGE_MANA))
							prepend.append("Mana ("+getActualUsage(A,Ability.USAGE_MANA,forMOB)+") ");
						if(CMath.bset(A.usageType(),Ability.USAGE_MOVEMENT))
							prepend.append("Movement ("+getActualUsage(A,Ability.USAGE_MOVEMENT,forMOB)+") ");
						if(CMath.bset(A.usageType(),Ability.USAGE_HITPOINTS))
							prepend.append("Hit Points ("+getActualUsage(A,Ability.USAGE_HITPOINTS,forMOB)+") ");
						prepend.append("\n\rQuality  : ");
						switch(A.abstractQuality())
						{
						case Ability.QUALITY_MALICIOUS:
							prepend.append("Malicious");
							break;
						case Ability.QUALITY_BENEFICIAL_OTHERS:
						case Ability.QUALITY_BENEFICIAL_SELF:
							prepend.append("Always Beneficial");
							break;
						case Ability.QUALITY_OK_OTHERS:
						case Ability.QUALITY_OK_SELF:
							prepend.append("Sometimes Beneficial");
							break;
						case Ability.QUALITY_INDIFFERENT:
							prepend.append("Circumstantial");
							break;
						}
						prepend.append("\n\rTargets  : ");
						if((A.abstractQuality()==Ability.QUALITY_BENEFICIAL_SELF)
						||(A.abstractQuality()==Ability.QUALITY_OK_SELF))
							prepend.append("Caster only");
						else
						if((CMClass.basicItems().hasMoreElements())
						&&(CMClass.mobTypes().hasMoreElements())
						&&(CMClass.exits().hasMoreElements())
						&&(CMClass.locales().hasMoreElements()))
						{
							if(A.canAffect(Ability.CAN_ITEMS)||A.canTarget(Ability.CAN_ITEMS))
								prepend.append("Items ");
							if(A.canAffect(Ability.CAN_MOBS)||A.canTarget(Ability.CAN_MOBS))
								prepend.append("Creatures ");
							if(A.canAffect(Ability.CAN_EXITS)||A.canTarget(Ability.CAN_EXITS))
								prepend.append("Exits ");
							if(A.canAffect(Ability.CAN_ROOMS)||A.canTarget(Ability.CAN_ROOMS))
								prepend.append("Rooms ");
						}
						else
						if(A.abstractQuality()==Ability.QUALITY_INDIFFERENT)
							prepend.append("Items or Rooms");
						else
						if(A.abstractQuality()==Ability.QUALITY_MALICIOUS)
							prepend.append("Others");
						else
						if((A.abstractQuality()==Ability.QUALITY_BENEFICIAL_OTHERS)
						||(A.abstractQuality()==Ability.QUALITY_OK_SELF))
							prepend.append("Caster, or others");
						prepend.append("\n\rRange    : ");
						int min=A.minRange();
						int max=A.maxRange();
						if(min+max==0)
							prepend.append("Touch, or not applicable");
						else
						{
							if(min==0)
								prepend.append("Touch");
							else
								prepend.append("Range "+min);
							if(max>0)
								prepend.append(" - Range "+max);
						}
						if((A.triggerStrings()!=null)
						   &&(A.triggerStrings().length>0))
						{
							prepend.append("\n\rCommands : ");
							for(int i=0;i<A.triggerStrings().length;i++)
							{
								prepend.append(A.triggerStrings()[i]);
								if(i<(A.triggerStrings().length-1))
								   prepend.append(", ");
							}
						}
					}
					else
						prepend.append("\n\rInvoked  : Automatic");
					str=prepend.toString()+"\n\r"+str;
				}
			}
		}
        try{
            if(str!=null)
                return CMLib.httpUtils().doVirtualPage(str);
        }catch(com.planet_ink.coffee_mud.core.exceptions.HTTPRedirectException x){}
		return str;
	}

	public StringBuffer getHelpText(String helpStr, Properties rHelpFile, MOB forMOB)
	{ return getHelpText(helpStr,rHelpFile,forMOB,false);}
	public StringBuffer getHelpText(String helpStr, Properties rHelpFile, MOB forMOB, boolean noFix)
	{
		helpStr=helpStr.toUpperCase().trim();
		if(helpStr.indexOf(" ")>=0)
			helpStr=helpStr.replace(' ','_');
		String thisTag=rHelpFile.getProperty(helpStr);
		if(thisTag==null){thisTag=rHelpFile.getProperty("SPELL_"+helpStr); if(thisTag!=null) helpStr="SPELL_"+helpStr;}
		if(thisTag==null){thisTag=rHelpFile.getProperty("PRAYER_"+helpStr); if(thisTag!=null) helpStr="PRAYER_"+helpStr;}
		if(thisTag==null){thisTag=rHelpFile.getProperty("SONG_"+helpStr); if(thisTag!=null) helpStr="SONG_"+helpStr;}
		if(thisTag==null){thisTag=rHelpFile.getProperty("DANCE_"+helpStr); if(thisTag!=null) helpStr="DANCE_"+helpStr;}
		if(thisTag==null){thisTag=rHelpFile.getProperty("PLAY_"+helpStr); if(thisTag!=null) helpStr="PLAY_"+helpStr;}
		if(thisTag==null){thisTag=rHelpFile.getProperty("CHANT_"+helpStr); if(thisTag!=null) helpStr="CHANT_"+helpStr;}
		if(thisTag==null){thisTag=rHelpFile.getProperty("PROP_"+helpStr); if(thisTag!=null) helpStr="PROP_"+helpStr;}
		if(thisTag==null){thisTag=rHelpFile.getProperty("BEHAVIOR_"+helpStr); if(thisTag!=null) helpStr="BEHAVIOR_"+helpStr;}

		if((thisTag==null)||((thisTag!=null)&&(thisTag.length()==0)))
		for(Enumeration e=rHelpFile.keys();e.hasMoreElements();)
		{
			String key=((String)e.nextElement()).toUpperCase();
			if(key.startsWith(helpStr))
			{
				thisTag=rHelpFile.getProperty(key);
				helpStr=key;
				break;
			}
		}
		if((thisTag==null)||((thisTag!=null)&&(thisTag.length()==0)))
		for(Enumeration e=rHelpFile.keys();e.hasMoreElements();)
		{
			String key=((String)e.nextElement()).toUpperCase();
			if(key.indexOf(helpStr)>=0)
			{
				thisTag=rHelpFile.getProperty(key);
				helpStr=key;
				break;
			}
		}
		if((thisTag==null)||((thisTag!=null)&&(thisTag.length()==0)))
		{
			String ahelpStr=helpStr.replaceAll("_"," ").trim();
			boolean found=false;
			for(Enumeration e=CMLib.map().areas();e.hasMoreElements();)
			{
				Area A=(Area)e.nextElement();
				if(A.name().equalsIgnoreCase(ahelpStr))
				{
					helpStr=A.name();
					found=true;
					break;
				}
			}
			if(!found)
				for(Enumeration e=CMLib.map().areas();e.hasMoreElements();)
				{
					Area A=(Area)e.nextElement();
					if(CMLib.english().containsString(A.name(),ahelpStr))
					{
						helpStr=A.name();
						break;
					}
				}
			if(!found)
			{
			    String currency=CMLib.english().matchAnyCurrencySet(ahelpStr);
			    if(currency!=null)
			    {
			        double denom=CMLib.english().matchAnyDenomination(currency,ahelpStr);
			        if(denom>0.0)
			        {
			            Coins C=CMLib.beanCounter().makeCurrency(currency,denom,1);
			            if((C!=null)&&(C.description().length()>0))
			                return new StringBuffer(C.name()+" is "+C.description().toLowerCase());
			        }
			    }
			}
		}
		while((thisTag!=null)&&(thisTag.length()>0)&&(thisTag.length()<31))
		{
			String thisOtherTag=rHelpFile.getProperty(thisTag);
			if((thisOtherTag!=null)&&(thisOtherTag.equals(thisTag)))
				thisTag=null;
			else
			if(thisOtherTag!=null)
			{
				helpStr=thisTag;
				thisTag=thisOtherTag;
			}
			else
			    break;
		}
		// the area exception
		if((thisTag==null)||((thisTag!=null)&&(thisTag.length()==0)))
			if(CMLib.map().getArea(helpStr.trim())!=null)
				return CMLib.map().getArea(helpStr.trim()).getAreaStats();
		if((thisTag==null)||((thisTag!=null)&&(thisTag.length()==0)))
			return null;
		if(noFix) return new StringBuffer(thisTag);
		return new StringBuffer(fixHelp(helpStr,thisTag,forMOB));
	}

	public StringBuffer getHelpList(String helpStr, 
                        	       Properties rHelpFile1, 
                        	       Properties rHelpFile2, 
                        	       MOB forMOB)
	{
		helpStr=helpStr.toUpperCase().trim();
		if(helpStr.indexOf(" ")>=0)
			helpStr=helpStr.replace(' ','_');
		Vector matches=new Vector();

		for(Enumeration e=rHelpFile1.keys();e.hasMoreElements();)
		{
			String key=(String)e.nextElement();
            String prop=rHelpFile1.getProperty(key,"");
			if((key.toUpperCase().indexOf(helpStr)>=0)||(CMLib.english().containsString(prop,helpStr)))
			    matches.addElement(key.toUpperCase());
		}
		if(rHelpFile2!=null)
		for(Enumeration e=rHelpFile2.keys();e.hasMoreElements();)
		{
            String key=(String)e.nextElement();
            String prop=rHelpFile1.getProperty(key,"");
            if((key.toUpperCase().indexOf(helpStr)>=0)||(CMLib.english().containsString(prop,helpStr)))
			    matches.addElement(key.toUpperCase());
		}
		if(matches.size()==0)
		    return new StringBuffer("");
		return CMLib.lister().fourColumns(matches);
	}
	
	public Properties getArcHelpFile()
	{
        try
        {
    		Properties arcHelpFile=(Properties)Resources.getResource("ARCHON HELP FILE");
    		if(arcHelpFile==null)
    		{
    			arcHelpFile=new Properties();
    			CMFile directory=new CMFile(Resources.buildResourcePath("help"),null,true);
    			if((directory.canRead())&&(directory.isDirectory()))
    			{
    				String[] list=directory.list();
    				for(int l=0;l<list.length;l++)
    				{
    					String item=list[l];
    					if((item!=null)
                        &&(item.length()>0)
    					&&item.toUpperCase().endsWith(".INI")
                        &&(item.toUpperCase().startsWith("ARC_")))
                            arcHelpFile.load(new ByteArrayInputStream(new CMFile(Resources.buildResourcePath("help")+item,null,true).raw()));
    				}
    			}
    			if(arcHelpFile!=null)
    			{
    				//DVector suspiciousPairs=suspiciousTags(arcHelpFile);
    				//for(int d=0;d<suspiciousPairs.size();d++)
    				//	Syst/em.out.pri/ntln(suspiciousPairs.elementAt(d,1)+": "+suspiciousPairs.elementAt(d,2));
    				for(Enumeration e=arcHelpFile.keys();e.hasMoreElements();)
    				{
    					String key=(String)e.nextElement();
    					String entry=(String)arcHelpFile.get(key);
    					int x=entry.indexOf("<ZAP=");
    					if(x>=0)
    					{
    						int y=entry.indexOf(">",x);
    						if(y>(x+5))
    						{
    							String word=entry.substring(x+5,y).trim();
    							entry=entry.substring(0,x)+CMLib.masking().maskHelp("\n\r",word)+entry.substring(y+1);
    							arcHelpFile.remove(key);
    							arcHelpFile.put(key,entry);
    						}
    					}
    				}
    				Resources.submitResource("ARCHON HELP FILE",arcHelpFile);
    			}
    		}
            return arcHelpFile;
        }
        catch(IOException e)
        {
            Log.errOut("MUDHelp",e);
        }
        return new Properties();
	}

	protected DVector suspiciousTags(Properties p)
	{
		String k=null;
		DVector pairs=new DVector(2);
		for(Enumeration e=p.keys();e.hasMoreElements();)
		{
			k=(String)e.nextElement();
			for(int i=0;i<k.length();i++)
				if(Character.isLowerCase(k.charAt(i)))
				{
					pairs.addElement(k,p.get(k));
					break;
				}
		}
		return pairs;
	}
	
	public Properties getHelpFile()
	{
        try
        {
    		Properties helpFile=(Properties)Resources.getResource("MAIN HELP FILE");
    		if(helpFile==null)
    		{
    			helpFile=new Properties();
    			CMFile directory=new CMFile(Resources.buildResourcePath("help"),null,true);
    			if((directory.canRead())&&(directory.isDirectory()))
    			{
    				String[] list=directory.list();
    				for(int l=0;l<list.length;l++)
    				{
    					String item=list[l];
    					if((item!=null)
                        &&(item.length()>0)
    					&&item.toUpperCase().endsWith(".INI")
                        &&(!item.toUpperCase().startsWith("ARC_")))
                            helpFile.load(new ByteArrayInputStream(new CMFile(Resources.buildResourcePath("help")+item,null,true).raw()));
    				}
    			}
    			if(helpFile!=null)
    			{
    				///DVector suspiciousPairs=suspiciousTags(helpFile);
    				//for(int d=0;d<suspiciousPairs.size();d++)
    				//	Sy/stem.out.pr/intln(suspiciousPairs.elementAt(d,1)+": "+suspiciousPairs.elementAt(d,2));
    				Resources.submitResource("MAIN HELP FILE",helpFile);
    			}
    		}
    		return helpFile;
        }
        catch(IOException e)
        {
            Log.errOut("MUDHelp",e);
        }
        return new Properties();
	}
	
	public void unloadHelpFile(MOB mob)
	{
		if(Resources.getResource("PLAYER TOPICS")!=null)
			Resources.removeResource("PLAYER TOPICS");
		if(Resources.getResource("ARCHON TOPICS")!=null)
			Resources.removeResource("ARCHON TOPICS");
		if(Resources.getResource("help"+"/help.txt")!=null)
			Resources.removeResource("help"+"/help.txt");
		if(Resources.getResource("text"+"/races.txt")!=null)
			Resources.removeResource("text"+"/races.txt");
		if(Resources.getResource("text"+"/newchar.txt")!=null)
			Resources.removeResource("text"+"/newchar.txt");
		if(Resources.getResource("text"+"/stats.txt")!=null)
			Resources.removeResource("text"+"/stats.txt");
		if(Resources.getResource("text"+"/classes.txt")!=null)
			Resources.removeResource("text"+"/classes.txt");
		if(Resources.getResource("text"+"/alignment.txt")!=null)
			Resources.removeResource("text"+"/alignment.txt");
		if(Resources.getResource("help"+"/arc_help.txt")!=null)
			Resources.removeResource("help"+"/arc_help.txt");
		if(Resources.getResource("MAIN HELP FILE")!=null)
			Resources.removeResource("MAIN HELP FILE");
		if(Resources.getResource("ARCHON HELP FILE")!=null)
			Resources.removeResource("ARCHON HELP FILE");

		// also the intro page
		if(Resources.getResource("text"+"/intro.txt")!=null)
			Resources.removeResource("text"+"/intro.txt");

		if(Resources.getResource("text"+"/offline.txt")!=null)
			Resources.removeResource("text"+"/offline.txt");

		if(mob!=null)
			mob.tell("Help files unloaded. Next HELP, AHELP, new char will reload.");
	}
}
