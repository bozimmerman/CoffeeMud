package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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


import java.io.IOException;
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
public class EnglishParser extends StdLibrary implements EnglishParsing
{
    public String ID(){return "EnglishParser";}
    
    public String cleanArticles(String s)
    {
        String[] articles={"a","an","all of","some one","a pair of","one of","all","the","some"};
        boolean didSomething=true;
        while(didSomething)
        {
            didSomething=false;
            for(int a=0;a<articles.length;a++)
            {
                if(s.toLowerCase().startsWith(articles[a]+" "))
                {
                    didSomething=true;
                    s=s.substring(articles[a].length()+1);
                }
            }
        }
        return s;
    }

	public Object findCommand(MOB mob, Vector commands)
	{
		if((mob==null)
		||(commands==null)
		||(mob.location()==null)
		||(commands.size()==0))
			return null;

		String firstWord=((String)commands.elementAt(0)).toUpperCase();
        
		if((firstWord.length()>1)&&(!Character.isLetterOrDigit(firstWord.charAt(0))))
		{
			commands.insertElementAt(((String)commands.elementAt(0)).substring(1),1);
			commands.setElementAt(""+firstWord.charAt(0),0);
			firstWord=""+firstWord.charAt(0);
		}
		
		// first, exacting pass
		Command C=CMClass.findCommandByTrigger(firstWord,true);
		if((C!=null)
        &&(C.securityCheck(mob))
        &&(!CMSecurity.isDisabled("COMMAND_"+CMClass.className(C).toUpperCase()))) 
            return C;

		for(int a=0;a<mob.numAbilities();a++)
		{
			Ability A=mob.fetchAbility(a);
			if(A.triggerStrings()!=null)
				for(int t=0;t<A.triggerStrings().length;t++)
					if((A.triggerStrings()[t].equalsIgnoreCase(firstWord))
                    &&(!CMSecurity.isDisabled("ABILITY_"+A.ID().toUpperCase())))
						return A;
		}

		Social social=CMLib.socials().FetchSocial(commands,true);
		if(social!=null) return social;

		for(int c=0;c<CMLib.channels().getNumChannels();c++)
		{
			if(CMLib.channels().getChannelName(c).equalsIgnoreCase(firstWord))
			{
				C=CMClass.getCommand("Channel");
				if((C!=null)&&(C.securityCheck(mob))) return C;
			}
			else
			if(("NO"+CMLib.channels().getChannelName(c)).equalsIgnoreCase(firstWord))
			{
				C=CMClass.getCommand("NoChannel");
				if((C!=null)&&(C.securityCheck(mob))) return C;
			}
		}
		
        for(int c=0;c<CMLib.journals().getNumCommandJournals();c++)
        {
            if(CMLib.journals().getCommandJournalName(c).equalsIgnoreCase(firstWord))
            {
                C=CMClass.getCommand("CommandJournal");
                if((C!=null)&&(C.securityCheck(mob))) return C;
            }
        }
        
		// second, inexacting pass
		C=CMClass.findCommandByTrigger(firstWord,false);
        if((C!=null)
        &&(C.securityCheck(mob))
        &&(!CMSecurity.isDisabled("COMMAND_"+CMClass.className(C).toUpperCase()))) 
            return C;

		for(int a=0;a<mob.numAbilities();a++)
		{
			Ability A=mob.fetchAbility(a);
			if(A.triggerStrings()!=null)
				for(int t=0;t<A.triggerStrings().length;t++)
					if((A.triggerStrings()[t].toUpperCase().startsWith(firstWord))
                    &&(!CMSecurity.isDisabled("ABILITY_"+A.ID().toUpperCase())))
					{
						commands.setElementAt(A.triggerStrings()[t],0);
						return A;
					}
		}

		social=CMLib.socials().FetchSocial(commands,false);
		if(social!=null)
		{
			commands.setElementAt(social.ID(),0);
			return social;
		}
		
		for(int c=0;c<CMLib.channels().getNumChannels();c++)
		{
			if(CMLib.channels().getChannelName(c).startsWith(firstWord))
			{
				commands.setElementAt(CMLib.channels().getChannelName(c),0);
				C=CMClass.getCommand("Channel");
				if((C!=null)&&(C.securityCheck(mob))) return C;
			}
			else
			if(("NO"+CMLib.channels().getChannelName(c)).startsWith(firstWord))
			{
				commands.setElementAt("NO"+CMLib.channels().getChannelName(c),0);
				C=CMClass.getCommand("NoChannel");
				if((C!=null)&&(C.securityCheck(mob))) return C;
			}
		}
        
        for(int c=0;c<CMLib.journals().getNumCommandJournals();c++)
        {
            if(CMLib.journals().getCommandJournalName(c).startsWith(firstWord))
            {
                C=CMClass.getCommand("CommandJournal");
                if((C!=null)&&(C.securityCheck(mob))) return C;
            }
        }
		return null;
	}

	public boolean evokedBy(Ability thisAbility, String thisWord)
	{
		for(int i=0;i<thisAbility.triggerStrings().length;i++)
		{
			if(thisAbility.triggerStrings()[i].equalsIgnoreCase(thisWord))
				return true;
		}
		return false;
	}

	public boolean evokedBy(Ability thisAbility, String thisWord, String secondWord)
	{
		for(int i=0;i<thisAbility.triggerStrings().length;i++)
		{
			if(thisAbility.triggerStrings()[i].equalsIgnoreCase(thisWord))
			{
				if((thisAbility.name().toUpperCase().startsWith(secondWord)))
					return true;
			}
		}
		return false;
	}

	public Ability getToEvoke(MOB mob, Vector commands)
	{
		String evokeWord=((String)commands.elementAt(0)).toUpperCase();

		boolean foundMoreThanOne=false;
		Ability evokableAbility=null;
		for(int a=0;a<mob.numAbilities();a++)
		{
			Ability thisAbility=mob.fetchAbility(a);
			if((thisAbility!=null)
			&&(evokedBy(thisAbility,evokeWord)))
            {
				if(evokableAbility!=null)
				{
					foundMoreThanOne=true;
					evokableAbility=null;
					break;
				}
				evokableAbility=thisAbility;
            }
		}

		if((evokableAbility!=null)&&(commands.size()>1))
		{
			int classCode=evokableAbility.classificationCode()&Ability.ALL_CODES;
			switch(classCode)
			{
			case Ability.SPELL:
			case Ability.SONG:
			case Ability.PRAYER:
			case Ability.CHANT:
				evokableAbility=null;
				foundMoreThanOne=true;
				break;
			default:
				break;
			}
		}

		if(evokableAbility!=null)
			commands.removeElementAt(0);
		else
		if((foundMoreThanOne)&&(commands.size()>1))
		{
			commands.removeElementAt(0);
			foundMoreThanOne=false;
			String secondWord=((String)commands.elementAt(0)).toUpperCase();
			for(int a=0;a<mob.numAbilities();a++)
			{
				Ability thisAbility=mob.fetchAbility(a);
				if((thisAbility!=null)
				&&(evokedBy(thisAbility,evokeWord,secondWord.toUpperCase())))
				{
					if(thisAbility.name().equalsIgnoreCase(secondWord))
					{
						evokableAbility=thisAbility;
						foundMoreThanOne=false;
						break;
					}
					else
					if(evokableAbility!=null)
						foundMoreThanOne=true;
					else
						evokableAbility=thisAbility;
				}
			}
			if((evokableAbility!=null)&&(!foundMoreThanOne))
				commands.removeElementAt(0);
			else
			if((foundMoreThanOne)&&(commands.size()>1))
			{
				String secondAndThirdWord=secondWord+" "+((String)commands.elementAt(1)).toUpperCase();

				for(int a=0;a<mob.numAbilities();a++)
				{
					Ability thisAbility=mob.fetchAbility(a);
					if((thisAbility!=null)
					   &&(evokedBy(thisAbility,evokeWord,secondAndThirdWord.toUpperCase())))
					{
						evokableAbility=thisAbility;
						break;
					}
				}
				if(evokableAbility!=null)
				{
					commands.removeElementAt(0);
					commands.removeElementAt(0);
				}
			}
			else
			{
				for(int a=0;a<mob.numAbilities();a++)
				{
					Ability thisAbility=mob.fetchAbility(a);
					if((thisAbility!=null)
					&&(evokedBy(thisAbility,evokeWord))
					&&(thisAbility.name().toUpperCase().indexOf(" "+secondWord.toUpperCase())>0))
					{
						evokableAbility=thisAbility;
						commands.removeElementAt(0);
						break;
					}
				}
			}
		}
		return evokableAbility;
	}

    public boolean preEvoke(MOB mob, Vector commands)
    {
        commands=(Vector)commands.clone();
        Ability evokableAbility=getToEvoke(mob,commands);
        if(evokableAbility==null)
        {
            mob.tell(getScr("AbilityEvoker","evokeerr1"));
            return false;
        }
        if((CMLib.ableMapper().qualifyingLevel(mob,evokableAbility)>=0)
        &&(!CMLib.ableMapper().qualifiesByLevel(mob,evokableAbility)))
        {
            mob.tell(getScr("AbilityEvoker","evokeerr2"));
            return false;
        }
        return evokableAbility.preInvoke(mob,commands,null,false,0);
    }
	public void evoke(MOB mob, Vector commands)
	{
		Ability evokableAbility=getToEvoke(mob,commands);
		if(evokableAbility==null)
		{
			mob.tell(getScr("AbilityEvoker","evokeerr1"));
			return;
		}
		if((CMLib.ableMapper().qualifyingLevel(mob,evokableAbility)>=0)
		&&(!CMLib.ableMapper().qualifiesByLevel(mob,evokableAbility)))
		{
			mob.tell(getScr("AbilityEvoker","evokeerr2"));
			return;
		}
		evokableAbility.invoke(mob,commands,null,false,0);
	}

	public boolean containsString(String toSrchStr, String srchStr)
	{
		if(srchStr.equalsIgnoreCase("all")) return true;
		if(srchStr.equalsIgnoreCase(toSrchStr)) return true;
        if(Util.stripPunctuation(srchStr).trim().equalsIgnoreCase(Util.stripPunctuation(toSrchStr).trim())) 
            return true;
        boolean topOnly=false;
        if(srchStr.startsWith("$")&&(srchStr.length()>1))
        {
            srchStr=srchStr.substring(1);
            topOnly=true;
        }
		int tos=0;
		int tolen=toSrchStr.length();
		int srlen=srchStr.length();
		boolean found=false;
		while((!found)&&(tos<tolen))
		{
			for(int x=0;x<srlen;x++)
			{
				if(tos>=tolen)
				{
					if(srchStr.charAt(x)=='$')
						found=true;
					break;
				}

				switch(toSrchStr.charAt(tos))
				{
				case '^':
					tos=tos+2;
					break;
				case ',':
				case '?':
				case '!':
				case '.':
				case ';':
					tos++;
					break;
				}
				switch(srchStr.charAt(x))
				{
				case '^': x=x+2;
					break;
				case ',':
				case '?':
				case '!':
				case '.':
				case ';': x++;
					break;
				}
				if(x<srlen)
				{
					if(tos<tolen)
					{
						if(Character.toUpperCase(srchStr.charAt(x))!=Character.toUpperCase(toSrchStr.charAt(tos)))
							break;
						else
						if(x==(srlen-1))
						   found=true;
						else
							tos++;
					}
					else
					if(srchStr.charAt(x)=='$')
						found=true;
					else
						break;
				}
				else
				{
					found=true;
					break;
				}
			}
            if((topOnly)&&(!found)) break;
			while((!found)&&(tos<tolen)&&(Character.isLetter(toSrchStr.charAt(tos))))
				tos++;
			tos++;
		}
		return found;
	}
	
	public String bumpDotNumber(String srchStr)
	{
		Object[] flags=fetchFlags(srchStr);
		if(flags==null) return srchStr;
		if(((Boolean)flags[FLAG_ALL]).booleanValue())
			return srchStr;
		if(((Integer)flags[FLAG_DOT]).intValue()==0)
			return "1."+((String)flags[FLAG_STR]);
		return (((Integer)flags[FLAG_DOT]).intValue()+1)+"."+((String)flags[FLAG_STR]);
	}
	
	public Object[] fetchFlags(String srchStr)
	{
		if(srchStr.length()==0) return null;
		if((srchStr.length()<2)||(srchStr.equalsIgnoreCase("THE")))
		   return null;
		Object[] flags=new Object[3];
		
		boolean allFlag=false;
		if(srchStr.toUpperCase().startsWith("ALL "))
		{
			srchStr=srchStr.substring(4);
			allFlag=true;
		}
		else
		if(srchStr.equalsIgnoreCase("ALL"))
			allFlag=true;

		int dot=srchStr.lastIndexOf(".");
		int occurrance=0;
		if(dot>0)
		{
			String sub=srchStr.substring(dot+1);
			occurrance=Util.s_int(sub);
			if(occurrance>0)
				srchStr=srchStr.substring(0,dot);
			else
			{
				dot=srchStr.indexOf(".");
				sub=srchStr.substring(0,dot);
				occurrance=Util.s_int(sub);
				if(occurrance>0)
					srchStr=srchStr.substring(dot+1);
				else
					occurrance=0;
			}
		}
		flags[0]=srchStr;
		flags[1]=new Integer(occurrance);
		flags[2]=new Boolean(allFlag);
		return flags;
	}

	public Environmental fetchEnvironmental(Vector list, String srchStr, boolean exactOnly)
	{
		Object[] flags=fetchFlags(srchStr);
		if(flags==null) return null;
		
		srchStr=(String)flags[FLAG_STR];
		int myOccurrance=((Integer)flags[FLAG_DOT]).intValue();
		boolean allFlag=((Boolean)flags[FLAG_ALL]).booleanValue();
		
		if(exactOnly)
		{
			try
			{
				for(int i=0;i<list.size();i++)
				{
					Environmental thisThang=(Environmental)list.elementAt(i);
					if(thisThang.ID().equalsIgnoreCase(srchStr)
					   ||thisThang.name().equalsIgnoreCase(srchStr)
					   ||thisThang.Name().equalsIgnoreCase(srchStr))
						if((!allFlag)||(thisThang.displayText().length()>0))
							if((--myOccurrance)<=0)
								return thisThang;
				}
			}
			catch(java.lang.ArrayIndexOutOfBoundsException x){}
		}
		else
		{
			myOccurrance=((Integer)flags[FLAG_DOT]).intValue();
			try
			{
				for(int i=0;i<list.size();i++)
				{
					Environmental thisThang=(Environmental)list.elementAt(i);
					if((containsString(thisThang.name(),srchStr)||containsString(thisThang.Name(),srchStr))
					   &&((!allFlag)||(thisThang.displayText().length()>0)))
						if((--myOccurrance)<=0)
							return thisThang;
				}
			}
			catch(java.lang.ArrayIndexOutOfBoundsException x){}
			myOccurrance=((Integer)flags[FLAG_DOT]).intValue();
			try
			{
				for(int i=0;i<list.size();i++)
				{
					Environmental thisThang=(Environmental)list.elementAt(i);
					if((!(thisThang instanceof Ability))
					&&(thisThang.displayText().length()>0)
					&&(containsString(thisThang.displayText(),srchStr)))
						if((--myOccurrance)<=0)
							return thisThang;
				}
			}
			catch(java.lang.ArrayIndexOutOfBoundsException x){}
		}
		return null;
	}

	public Environmental fetchEnvironmental(Hashtable list, String srchStr, boolean exactOnly)
	{
		Object[] flags=fetchFlags(srchStr);
		if(flags==null) return null;
		
		srchStr=(String)flags[FLAG_STR];
		int myOccurrance=((Integer)flags[FLAG_DOT]).intValue();
		boolean allFlag=((Boolean)flags[FLAG_ALL]).booleanValue();

		if(list.get(srchStr)!=null)
			return (Environmental)list.get(srchStr);
		if(exactOnly)
		{
			for(Enumeration e=list.elements();e.hasMoreElements();)
			{
				Environmental thisThang=(Environmental)e.nextElement();
				if(thisThang.ID().equalsIgnoreCase(srchStr)
				||thisThang.Name().equalsIgnoreCase(srchStr)
				||thisThang.name().equalsIgnoreCase(srchStr))
					if((!allFlag)||(thisThang.displayText().length()>0))
						if((--myOccurrance)<=0)
							return thisThang;
			}
		}
		else
		{
			myOccurrance=((Integer)flags[FLAG_DOT]).intValue();
			for(Enumeration e=list.elements();e.hasMoreElements();)
			{
				Environmental thisThang=(Environmental)e.nextElement();
				if((containsString(thisThang.name(),srchStr)||containsString(thisThang.Name(),srchStr))
				&&((!allFlag)||(thisThang.displayText().length()>0)))
					if((--myOccurrance)<=0)
						return thisThang;
			}
			myOccurrance=((Integer)flags[FLAG_DOT]).intValue();
			for(Enumeration e=list.elements();e.hasMoreElements();)
			{
				Environmental thisThang=(Environmental)e.nextElement();
				if((thisThang.displayText().length()>0)&&(containsString(thisThang.displayText(),srchStr)))
					if((--myOccurrance)<=0)
						return thisThang;
			}
		}
		return null;
	}

	public Environmental fetchEnvironmental(Environmental[] list, String srchStr, boolean exactOnly)
	{
		Object[] flags=fetchFlags(srchStr);
		if(flags==null) return null;
		
		srchStr=(String)flags[FLAG_STR];
		int myOccurrance=((Integer)flags[FLAG_DOT]).intValue();
		boolean allFlag=((Boolean)flags[FLAG_ALL]).booleanValue();
		
		if(exactOnly)
		{
			for(int i=0;i<list.length;i++)
			{
				Environmental thisThang=list[i];
				if(thisThang!=null)
					if(thisThang.ID().equalsIgnoreCase(srchStr)
					||thisThang.Name().equalsIgnoreCase(srchStr)
					||thisThang.name().equalsIgnoreCase(srchStr))
						if((!allFlag)||(thisThang.displayText().length()>0))
							if((--myOccurrance)<=0)
								return thisThang;
			}
		}
		else
		{
			myOccurrance=((Integer)flags[FLAG_DOT]).intValue();
			for(int i=0;i<list.length;i++)
			{
				Environmental thisThang=list[i];
				if(thisThang!=null)
					if((containsString(thisThang.name(),srchStr)||containsString(thisThang.Name(),srchStr))
					   &&((!allFlag)||(thisThang.displayText().length()>0)))
						if((--myOccurrance)<=0)
							return thisThang;
			}
			myOccurrance=((Integer)flags[FLAG_DOT]).intValue();
			for(int i=0;i<list.length;i++)
			{
				Environmental thisThang=list[i];
				if((thisThang!=null)&&(thisThang.displayText().length()>0))
					if(containsString(thisThang.displayText(),srchStr))
						if((--myOccurrance)<=0)
							return thisThang;
			}
		}
		return null;
	}

	public Item fetchAvailableItem(Vector list, String srchStr, Item goodLocation, int wornReqCode, boolean exactOnly)
	{
		Object[] flags=fetchFlags(srchStr);
		if(flags==null) return null;
		
		srchStr=(String)flags[FLAG_STR];
		int myOccurrance=((Integer)flags[FLAG_DOT]).intValue();
		boolean allFlag=((Boolean)flags[FLAG_ALL]).booleanValue();
		
		if(exactOnly)
		{
			try
			{
				for(int i=0;i<list.size();i++)
				{
					Item thisThang=(Item)list.elementAt(i);
					boolean beingWorn=!thisThang.amWearingAt(Item.INVENTORY);

					if((thisThang.container()==goodLocation)
					&&((wornReqCode==Item.WORN_REQ_ANY)||(beingWorn&(wornReqCode==Item.WORN_REQ_WORNONLY))||((!beingWorn)&&(wornReqCode==Item.WORN_REQ_UNWORNONLY)))
					&&(thisThang.ID().equalsIgnoreCase(srchStr)
					   ||(thisThang.Name().equalsIgnoreCase(srchStr))
					   ||(thisThang.name().equalsIgnoreCase(srchStr))))
						if((!allFlag)||(thisThang.displayText().length()>0))
							if((--myOccurrance)<=0)
								return thisThang;
				}
			}
			catch(java.lang.ArrayIndexOutOfBoundsException x){}
		}
		else
		{
			try
			{
				for(int i=0;i<list.size();i++)
				{
					Item thisThang=(Item)list.elementAt(i);
					boolean beingWorn=!thisThang.amWearingAt(Item.INVENTORY);

					if((thisThang.container()==goodLocation)
					&&((wornReqCode==Item.WORN_REQ_ANY)||(beingWorn&(wornReqCode==Item.WORN_REQ_WORNONLY))||((!beingWorn)&&(wornReqCode==Item.WORN_REQ_UNWORNONLY)))
					&&((containsString(thisThang.name(),srchStr)||containsString(thisThang.Name(),srchStr))
					   &&((!allFlag)||(thisThang.displayText().length()>0))))
						if((--myOccurrance)<=0)
							return thisThang;
				}
			}
			catch(java.lang.ArrayIndexOutOfBoundsException x){}
			myOccurrance=((Integer)flags[FLAG_DOT]).intValue();
			try
			{
				for(int i=0;i<list.size();i++)
				{
					Item thisThang=(Item)list.elementAt(i);
					boolean beingWorn=!thisThang.amWearingAt(Item.INVENTORY);
					if((thisThang.container()==goodLocation)
					&&(thisThang.displayText().length()>0)
					&&((wornReqCode==Item.WORN_REQ_ANY)||(beingWorn&(wornReqCode==Item.WORN_REQ_WORNONLY))||((!beingWorn)&&(wornReqCode==Item.WORN_REQ_UNWORNONLY)))
					&&(containsString(thisThang.displayText(),srchStr)))
						if((--myOccurrance)<=0)
							return thisThang;
				}
			}
			catch(java.lang.ArrayIndexOutOfBoundsException x){}
		}
		return null;
	}

	public Environmental fetchAvailable(Vector list, String srchStr, Item goodLocation, int wornReqCode, boolean exactOnly)
	{
		Object[] flags=fetchFlags(srchStr);
		if(flags==null) return null;
		
		srchStr=(String)flags[FLAG_STR];
		int myOccurrance=((Integer)flags[FLAG_DOT]).intValue();
		boolean allFlag=((Boolean)flags[FLAG_ALL]).booleanValue();
		
	    Environmental E=null;
	    Item thisThang=null;
		if(exactOnly)
		{
			try
			{
				for(int i=0;i<list.size();i++)
				{
				    E=(Environmental)list.elementAt(i);
				    if(E instanceof Item)
				    {
						thisThang=(Item)E;
						boolean beingWorn=!thisThang.amWearingAt(Item.INVENTORY);
	
						if((thisThang.container()==goodLocation)
						&&((wornReqCode==Item.WORN_REQ_ANY)||(beingWorn&(wornReqCode==Item.WORN_REQ_WORNONLY))||((!beingWorn)&&(wornReqCode==Item.WORN_REQ_UNWORNONLY)))
						&&(thisThang.ID().equalsIgnoreCase(srchStr)
						   ||(thisThang.Name().equalsIgnoreCase(srchStr))
						   ||(thisThang.name().equalsIgnoreCase(srchStr))))
							if((!allFlag)||(thisThang.displayText().length()>0))
								if((--myOccurrance)<=0)
									return thisThang;
				    }
				    else
					if(E.ID().equalsIgnoreCase(srchStr)
					||E.Name().equalsIgnoreCase(srchStr)
					||E.name().equalsIgnoreCase(srchStr))
						if((!allFlag)||(E.displayText().length()>0))
							if((--myOccurrance)<=0)
								return E;
				    }
			}
			catch(java.lang.ArrayIndexOutOfBoundsException x){}
		}
		else
		{
			try
			{
				for(int i=0;i<list.size();i++)
				{
					E=(Environmental)list.elementAt(i);
					if(E instanceof Item)
					{
					    thisThang=(Item)E;
						boolean beingWorn=!thisThang.amWearingAt(Item.INVENTORY);
	
						if((thisThang.container()==goodLocation)
						&&((wornReqCode==Item.WORN_REQ_ANY)||(beingWorn&(wornReqCode==Item.WORN_REQ_WORNONLY))||((!beingWorn)&&(wornReqCode==Item.WORN_REQ_UNWORNONLY)))
						&&((containsString(thisThang.name(),srchStr)||containsString(thisThang.Name(),srchStr))
						   &&((!allFlag)||(thisThang.displayText().length()>0))))
							if((--myOccurrance)<=0)
								return thisThang;
					}
					else
					if((containsString(E.name(),srchStr)||containsString(E.Name(),srchStr))
				    &&((!allFlag)||(E.displayText().length()>0)))
						if((--myOccurrance)<=0)
							return E;
					    
				}
			}
			catch(java.lang.ArrayIndexOutOfBoundsException x){}
			myOccurrance=((Integer)flags[FLAG_DOT]).intValue();
			try
			{
				for(int i=0;i<list.size();i++)
				{
					E=(Environmental)list.elementAt(i);
					if(E instanceof Item)
					{
					    thisThang=(Item)E;
						boolean beingWorn=!thisThang.amWearingAt(Item.INVENTORY);
						if((thisThang.container()==goodLocation)
						&&(thisThang.displayText().length()>0)
						&&((wornReqCode==Item.WORN_REQ_ANY)||(beingWorn&(wornReqCode==Item.WORN_REQ_WORNONLY))||((!beingWorn)&&(wornReqCode==Item.WORN_REQ_UNWORNONLY)))
						&&(containsString(thisThang.displayText(),srchStr)))
							if((--myOccurrance)<=0)
								return thisThang;
					}
					else
					if((E.displayText().length()>0)
					&&(containsString(E.displayText(),srchStr)))
						if((--myOccurrance)<=0)
							return E;
				}
			}
			catch(java.lang.ArrayIndexOutOfBoundsException x){}
		}
		return null;
	}

	public Environmental parseShopkeeper(MOB mob, Vector commands, String error)
	{
		if(commands.size()==0)
		{
            if(error.length()>0) mob.tell(error);
			return null;
		}
		commands.removeElementAt(0);

		Vector V=CMLib.coffeeShops().getAllShopkeepers(mob.location(),mob);
		if(V.size()==0)
		{
            if(error.length()>0) mob.tell(error);
			return null;
		}
		if(V.size()>1)
		{
			if(commands.size()<2)
			{
                if(error.length()>0) mob.tell(error);
				return null;
			}
            String what=Util.combine(commands,0);
            Environmental shopkeeper=fetchEnvironmental(V,what,false);
            if((shopkeeper==null)&&(what.equals("shop")||what.equals("the shop")))
                for(int v=0;v<V.size();v++)
                    if(V.elementAt(v) instanceof Area)
                    { shopkeeper=(Environmental)V.elementAt(v); break;}
			if((shopkeeper!=null)&&(CMLib.coffeeShops().getShopKeeper(shopkeeper)!=null)&&(CMLib.flags().canBeSeenBy(shopkeeper,mob)))
				commands.removeElementAt(commands.size()-1);
			else
			{
				mob.tell("You don't see anyone called '"+(String)commands.lastElement()+"' here buying or selling.");
				return null;
			}
			return shopkeeper;
		}
		Environmental shopkeeper=(Environmental)V.firstElement();
		if(commands.size()>1)
		{
			MOB M=mob.location().fetchInhabitant((String)commands.lastElement());
			if((M!=null)&&(CMLib.coffeeShops().getShopKeeper(M)!=null)&&(CMLib.flags().canBeSeenBy(M,mob)))
			{
				shopkeeper=M;
				commands.removeElementAt(commands.size()-1);
			}
		}
		return shopkeeper;
	}
	
	public Vector fetchItemList(Environmental from,
									   MOB mob,
                                       Item container,
                                       Vector commands,
                                       int preferredLoc,
                                       boolean visionMatters)
	{
		int addendum=1;
		String addendumStr="";
		Vector V=new Vector();
		
		int maxToItem=Integer.MAX_VALUE;
		if((commands.size()>1)
		&&(Util.s_int((String)commands.firstElement())>0))
		{
			maxToItem=Util.s_int((String)commands.firstElement());
			commands.setElementAt("all",0);
		}
		
		String name=Util.combine(commands,0);
		boolean allFlag=(commands.size()>0)?((String)commands.elementAt(0)).equalsIgnoreCase("all"):false;
		if(name.toUpperCase().startsWith("ALL.")){ allFlag=true; name="ALL "+name.substring(4);}
		if(name.toUpperCase().endsWith(".ALL")){ allFlag=true; name="ALL "+name.substring(0,name.length()-4);}
		do
		{
			Environmental item=null;
			if(from instanceof MOB)
			{
				if(preferredLoc==Item.WORN_REQ_UNWORNONLY)
					item=((MOB)from).fetchCarried(container,name+addendumStr);
				else
				if(preferredLoc==Item.WORN_REQ_WORNONLY)
					item=((MOB)from).fetchWornItem(name+addendumStr);
				else
					item=((MOB)from).fetchInventory(null,name+addendumStr);
			}
			else
			if(from instanceof Room)
				item=((Room)from).fetchFromMOBRoomFavorsItems(mob,container,name+addendumStr,preferredLoc);
			if((item!=null)
			&&(item instanceof Item)
			&&((!visionMatters)||(CMLib.flags().canBeSeenBy(item,mob))||(item instanceof Light))
			&&(!V.contains(item)))
				V.addElement(item);
			if(item==null) return V;
			addendumStr="."+(++addendum);
		}
		while((allFlag)&&(addendum<=maxToItem));
		return V;
	}
	
	public long numPossibleGold(Environmental mine, String itemID)
	{
		if(itemID.toUpperCase().trim().startsWith("A PILE OF "))
			itemID=itemID.substring(10);
		if(Util.isInteger(itemID))
		{
            long num=Util.s_long(itemID);
		    if(mine instanceof MOB)
		    {
		        Vector V=CMLib.beanCounter().getStandardCurrency((MOB)mine,CMLib.beanCounter().getCurrency(mine));
		        for(int v=0;v<V.size();v++)
		            if(((Coins)V.elementAt(v)).getNumberOfCoins()>=num)
		                return num;
		        V=CMLib.beanCounter().getStandardCurrency((MOB)mine,null);
		        for(int v=0;v<V.size();v++)
		            if(((Coins)V.elementAt(v)).getNumberOfCoins()>=num)
		                return num;
		    }
		    return Util.s_long(itemID);
		}
	    Vector V=Util.parse(itemID);
	    if((V.size()>1)
	    &&((Util.isInteger((String)V.firstElement()))
        &&(matchAnyCurrencySet(Util.combine(V,1))!=null)))
	        return Util.s_long((String)V.firstElement());
	    else
	    if((V.size()>1)&&(((String)V.firstElement()).equalsIgnoreCase("all")))
	    {
	        String currency=matchAnyCurrencySet(Util.combine(V,1));
	        if(currency!=null)
	        {
	            if(mine instanceof MOB)
	            {
		            Vector V2=CMLib.beanCounter().getStandardCurrency((MOB)mine,currency);
		            double denomination=matchAnyDenomination(currency,Util.combine(V,1));
		            Coins C=null;
		            for(int v2=0;v2<V2.size();v2++)
		            {
		                C=(Coins)V2.elementAt(v2);
		                if(C.getDenomination()==denomination)
		                    return C.getNumberOfCoins();
		            }
	            }
	            return 1;
	        }
	    }
	    else
	    if((V.size()>0)&&(matchAnyCurrencySet(Util.combine(V,0))!=null))
	        return 1;
		return 0;
	}
	public String numPossibleGoldCurrency(Environmental mine, String itemID)
	{
		if(itemID.toUpperCase().trim().startsWith("A PILE OF "))
			itemID=itemID.substring(10);
		if(Util.isInteger(itemID))
		{
		    long num=Util.s_long(itemID);
            if(mine instanceof MOB)
            {
    	        Vector V=CMLib.beanCounter().getStandardCurrency((MOB)mine,CMLib.beanCounter().getCurrency(mine));
    	        for(int v=0;v<V.size();v++)
    	            if(((Coins)V.elementAt(v)).getNumberOfCoins()>=num)
    	                return ((Coins)V.elementAt(v)).getCurrency();
    	        V=CMLib.beanCounter().getStandardCurrency((MOB)mine,null);
    	        for(int v=0;v<V.size();v++)
    	            if(((Coins)V.elementAt(v)).getNumberOfCoins()>=num)
    	                return ((Coins)V.elementAt(v)).getCurrency();
            }
            return CMLib.beanCounter().getCurrency(mine);
		}
	    Vector V=Util.parse(itemID);
	    if((V.size()>1)&&(Util.isInteger((String)V.firstElement())))
	        return matchAnyCurrencySet(Util.combine(V,1));
	    else
	    if((V.size()>1)&&(((String)V.firstElement()).equalsIgnoreCase("all")))
	        return matchAnyCurrencySet(Util.combine(V,1));
	    else
	    if(V.size()>0)
	        return matchAnyCurrencySet(Util.combine(V,0));
		return CMLib.beanCounter().getCurrency(mine);
	}
    
    
	public double numPossibleGoldDenomination(Environmental mine, String currency, String itemID)
	{
		if(itemID.toUpperCase().trim().startsWith("A PILE OF "))
			itemID=itemID.substring(10);
		if(Util.isInteger(itemID))
		{
		    long num=Util.s_long(itemID);
            if(mine instanceof MOB)
            {
    	        Vector V=CMLib.beanCounter().getStandardCurrency((MOB)mine,currency);
    	        for(int v=0;v<V.size();v++)
    	            if(((Coins)V.elementAt(v)).getNumberOfCoins()>=num)
    	                return ((Coins)V.elementAt(v)).getDenomination();
            }
		    return CMLib.beanCounter().getLowestDenomination(currency);
		}
	    Vector V=Util.parse(itemID);
	    if((V.size()>1)&&(Util.isInteger((String)V.firstElement())))
	        return matchAnyDenomination(currency,Util.combine(V,1));
	    else
	    if((V.size()>1)&&(((String)V.firstElement()).equalsIgnoreCase("all")))
	        return matchAnyDenomination(currency,Util.combine(V,1));
	    else
	    if(V.size()>0)
	        return matchAnyDenomination(currency,Util.combine(V,0));
		return 0;
	}
	
	public String matchAnyCurrencySet(String itemID)
	{
	    Vector V=CMLib.beanCounter().getAllCurrencies();
	    Vector V2=null;
	    for(int v=0;v<V.size();v++)
	    {
	        V2=CMLib.beanCounter().getDenominationNameSet((String)V.elementAt(v));
	        for(int v2=0;v2<V2.size();v2++)
	        {
	            String s=(String)V2.elementAt(v2);
	            if(s.toLowerCase().endsWith("(s)")) 
	                s=s.substring(0,s.length()-3)+"s";
	            if(containsString(s,itemID))
	                return (String)V.elementAt(v);
	        }
	    }
	    return null;
	}
	
	public double matchAnyDenomination(String currency, String itemID)
	{
        DVector V2=CMLib.beanCounter().getCurrencySet(currency);
        itemID=itemID.toUpperCase();
        String s=null;
        if(V2!=null)
        for(int v2=0;v2<V2.size();v2++)
        {
            s=((String)V2.elementAt(v2,2)).toUpperCase();
            if(s.endsWith("(S)")) 
                s=s.substring(0,s.length()-3)+"S";
            if(containsString(s,itemID))
                return ((Double)V2.elementAt(v2,1)).doubleValue();
            else
            if((s.length()>0)
            &&(containsString(s,itemID)))
                return ((Double)V2.elementAt(v2,1)).doubleValue();
        }
	    return 0.0;
	}
	
	public Item possibleRoomGold(MOB seer, Room room, Item container, String itemID)
	{
		if(itemID.toUpperCase().trim().startsWith("A PILE OF "))
			itemID=itemID.substring(10);
		long gold=0;
		if(Util.isInteger(itemID))
		{
		    gold=Util.s_long(itemID);
		    itemID="";
		}
		else
		{
		    Vector V=Util.parse(itemID);
		    if((V.size()>1)&&(Util.isInteger((String)V.firstElement())))
		        gold=Util.s_long((String)V.firstElement());
		    else
		        return null;
		    itemID=Util.combine(V,1);
		}
		if(gold>0)
		{
			for(int i=0;i<room.numItems();i++)
			{
				Item I=room.fetchItem(i);
				if((I.container()==container)
				&&(I instanceof Coins)
				&&(CMLib.flags().canBeSeenBy(I,seer))
				&&((itemID.length()==0)||(containsString(I.name(),itemID))))
				{
					if(((Coins)I).getNumberOfCoins()<=gold)
						return I;
					((Coins)I).setNumberOfCoins(((Coins)I).getNumberOfCoins()-gold);
					Coins C=(Coins)CMClass.getItem("StdCoins");
					C.setCurrency(((Coins)I).getCurrency());
					C.setNumberOfCoins(gold);
					C.setDenomination(((Coins)I).getDenomination());
					C.setContainer(container);
					C.recoverEnvStats();
					room.addItem(C);
					C.setDispossessionTime(I.dispossessionTime());
					return C;
				}
			}
		}
		return null;
	}

	public Item bestPossibleGold(MOB mob, Container container, String itemID)
	{
		if(itemID.toUpperCase().trim().startsWith("A PILE OF "))
			itemID=itemID.substring(10);
		long gold=0;
		double denomination=0.0;
		String currency=CMLib.beanCounter().getCurrency(mob);
		if(Util.isInteger(itemID))
		{
		    gold=Util.s_long(itemID);
	        Vector V=CMLib.beanCounter().getStandardCurrency(mob,CMLib.beanCounter().getCurrency(mob));
	        boolean skipNextCheck=false;
	        for(int v=0;v<V.size();v++)
	            if(((Coins)V.elementAt(v)).getNumberOfCoins()>=gold)
	            {
	                currency=((Coins)V.elementAt(v)).getCurrency();
	                denomination=((Coins)V.elementAt(v)).getDenomination();
	                break;
	            }
	        if(!skipNextCheck)
	        {
		        V=CMLib.beanCounter().getStandardCurrency(mob,null);
		        for(int v=0;v<V.size();v++)
		            if(((Coins)V.elementAt(v)).getNumberOfCoins()>=gold)
		            {
		                currency=((Coins)V.elementAt(v)).getCurrency();
		                denomination=((Coins)V.elementAt(v)).getDenomination();
		                break;
		            }
	        }
		}
		else
		{
		    Vector V=Util.parse(itemID);
		    if(V.size()<1) return null;
		    if((!Util.isInteger((String)V.firstElement()))
		    &&(!((String)V.firstElement()).equalsIgnoreCase("all")))
		        V.insertElementAt("1",0);
		    Item I=mob.fetchInventory(container,Util.combine(V,1));
		    if(I instanceof Coins)
		    {
		        if(((String)V.firstElement()).equalsIgnoreCase("all"))
		            gold=((Coins)I).getNumberOfCoins();
		        else
			        gold=Util.s_long((String)V.firstElement());
		        currency=((Coins)I).getCurrency();
		        denomination=((Coins)I).getDenomination();
		    }
		    else
		        return null;
		}
		if(gold>0)
		{
			if(CMLib.beanCounter().getNumberOfCoins(mob,currency,denomination)>=gold)
			{
			    CMLib.beanCounter().subtractMoney(mob,currency,denomination,Util.mul(denomination,gold));
			    Coins C=(Coins)CMClass.getItem("StdCoins");
			    C.setCurrency(currency);
			    C.setDenomination(denomination);
			    C.setNumberOfCoins(gold);
				C.recoverEnvStats();
				mob.addInventory(C);
				return C;
			}
			mob.tell("You don't have that many "+CMLib.beanCounter().getDenominationName(currency,denomination)+".");
			Vector V=CMLib.beanCounter().getStandardCurrency(mob,currency);
			for(int v=0;v<V.size();v++)
			    if(((Coins)V.elementAt(v)).getDenomination()==denomination)
			        return (Item)V.elementAt(v);
		}
		return null;
	}

	public Vector possibleContainers(MOB mob, Vector commands, int wornReqCode, boolean withContentOnly)
	{
		Vector V=new Vector();
		if(commands.size()==1)
			return V;

		int fromDex=-1;
		int containerDex=commands.size()-1;
		for(int i=commands.size()-2;i>0;i--)
		    if(((String)commands.elementAt(i)).equalsIgnoreCase("from"))
		    { 
		        fromDex=i; 
			    containerDex=i+1;
			    if(((containerDex+1)<commands.size())
			    &&((((String)commands.elementAt(containerDex)).equalsIgnoreCase("all"))
			    ||(Util.s_int((String)commands.elementAt(containerDex))>0)))
			        containerDex++;
			    break;
			}
		
		String possibleContainerID=Util.combine(commands,containerDex);
		    
		boolean allFlag=false;
		String preWord="";
		if(possibleContainerID.equalsIgnoreCase("all"))
			allFlag=true;
		else
		if(containerDex>1)
			preWord=(String)commands.elementAt(containerDex-1);

		int maxContained=Integer.MAX_VALUE;
		if(Util.s_int(preWord)>0)
		{
			maxContained=Util.s_int(preWord);
			commands.setElementAt("all",containerDex-1);
			containerDex--;
			preWord="all";
		}

		if(preWord.equalsIgnoreCase("all")){ allFlag=true; possibleContainerID="ALL "+possibleContainerID;}
		else
		if(possibleContainerID.toUpperCase().startsWith("ALL.")){ allFlag=true; possibleContainerID="ALL "+possibleContainerID.substring(4);}
		else
		if(possibleContainerID.toUpperCase().endsWith(".ALL")){ allFlag=true; possibleContainerID="ALL "+possibleContainerID.substring(0,possibleContainerID.length()-4);}

		int addendum=1;
		String addendumStr="";
		do
		{
			Environmental thisThang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,possibleContainerID+addendumStr,wornReqCode);
			if((thisThang!=null)
			&&(thisThang instanceof Item)
			&&(((Item)thisThang) instanceof Container)
			&&((!withContentOnly)||(((Container)thisThang).getContents().size()>0))
            &&(CMLib.flags().canBeSeenBy(thisThang,mob)||mob.isMine(thisThang)))
			{
				V.addElement(thisThang);
				if(V.size()==1)
				{
				    while((fromDex>=0)&&(commands.size()>fromDex))
						commands.removeElementAt(fromDex);
				    while(commands.size()>containerDex)
						commands.removeElementAt(containerDex);
					preWord="";
				}
			}
			if(thisThang==null)
			    return V;
			addendumStr="."+(++addendum);
		}
		while((allFlag)&&(addendum<=maxContained));
		return V;
	}

	public Item possibleContainer(MOB mob, Vector commands, boolean withStuff, int wornReqCode)
	{
		if(commands.size()==1)
			return null;

		int fromDex=-1;
		int containerDex=commands.size()-1;
		for(int i=commands.size()-2;i>=1;i--)
		    if(((String)commands.elementAt(i)).equalsIgnoreCase("from"))
		    { fromDex=i; containerDex=i+1;  break;}
		String possibleContainerID=Util.combine(commands,containerDex);
		
		Environmental thisThang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,possibleContainerID,wornReqCode);
		if((thisThang!=null)
		&&(thisThang instanceof Item)
		&&(((Item)thisThang) instanceof Container)
		&&((!withStuff)||(((Container)thisThang).getContents().size()>0)))
		{
		    while((fromDex>=0)&&(commands.size()>fromDex))
				commands.removeElementAt(fromDex);
		    while(commands.size()>containerDex)
				commands.removeElementAt(containerDex);
			return (Item)thisThang;
		}
		return null;
	}

    public String promptText(MOB mob, String oldVal, int showNumber, int showFlag, String FieldDisp)
    throws IOException
    {
        return promptText(mob,oldVal,showNumber,showFlag,FieldDisp,false,false);
    }
    public String promptText(MOB mob, String oldVal, int showNumber, int showFlag, String FieldDisp, boolean emptyOK)
    throws IOException
    {
        return promptText(mob,oldVal,showNumber,showFlag,FieldDisp,emptyOK,false);
    }
    public String promptText(MOB mob, String oldVal, int showNumber, int showFlag, String FieldDisp, boolean emptyOK, boolean rawPrint)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return oldVal;
        if(rawPrint)
            mob.session().rawPrintln(showNumber+". "+FieldDisp+": '"+oldVal+"'.");
        else
            mob.tell(showNumber+". "+FieldDisp+": '"+oldVal+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return oldVal;
        String newName=mob.session().prompt("Enter a new value "+(emptyOK?"(or NULL)":"")+"\n\r:","");
        if((newName.equalsIgnoreCase("null"))&&(emptyOK))
            return "";
        else
        if(newName.length()>0)
            return newName;
        else
        {
            mob.tell("(no change)");
            return oldVal;
        }
    }
    public boolean promptBool(MOB mob, boolean oldVal, int showNumber, int showFlag, String FieldDisp)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return oldVal;
        mob.tell(showNumber+". "+FieldDisp+": '"+oldVal+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return oldVal;
        String newName=mob.session().prompt("Enter true or false:","");
        if(newName.toUpperCase().startsWith("T")||newName.toUpperCase().startsWith("F"))
            return newName.toUpperCase().startsWith("T");
        mob.tell("(no change)");
        return oldVal;
    }
    
    public double promptDouble(MOB mob, double oldVal, int showNumber, int showFlag, String FieldDisp)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return oldVal;
        mob.tell(showNumber+". "+FieldDisp+": '"+oldVal+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return oldVal;
        String newName=mob.session().prompt("Enter a new value:","");
        if(Util.isNumber(newName))
            return Util.s_double(newName);
        mob.tell("(no change)");
        return oldVal;
    }
    
    public int promptInteger(MOB mob, int oldVal, int showNumber, int showFlag, String FieldDisp)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return oldVal;
        mob.tell(showNumber+". "+FieldDisp+": '"+oldVal+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return oldVal;
        String newName=mob.session().prompt("Enter a new value:","");
        if(Util.isInteger(newName))
            return Util.s_int(newName);
        mob.tell("(no change)");
        return oldVal;
    }
}
