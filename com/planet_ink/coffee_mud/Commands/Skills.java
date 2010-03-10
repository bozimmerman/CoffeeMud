package com.planet_ink.coffee_mud.Commands;
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

import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Skills extends StdCommand
{
	public Skills(){}

	private String[] access={"SKILLS","SK"};
	public String[] getAccessWords(){return access;}
    
    protected boolean parsedOutIndividualSkill(MOB mob, String qual, int acode)
    {
        return parsedOutIndividualSkill(mob,qual,CMParms.makeVector(Integer.valueOf(acode)));
    }
    
    protected boolean parsedOutIndividualSkill(MOB mob, String qual, Vector acodes)
    {
        if((qual==null)||(qual.length()==0)||(qual.equalsIgnoreCase("all")))
            return false;
        if(qual.length()>0)
        for(int i=1;i<Ability.DOMAIN_DESCS.length;i++)
            if(Ability.DOMAIN_DESCS[i].replace('_',' ').equalsIgnoreCase(qual))
            	return false;
            else
            if((Ability.DOMAIN_DESCS[i].replace('_',' ').indexOf("/")>=0)
            &&(Ability.DOMAIN_DESCS[i].replace('_',' ').substring(Ability.DOMAIN_DESCS[i].indexOf("/")+1).equalsIgnoreCase(qual)))
            	return false;
        Ability A=CMClass.findAbility(qual);
        if((A!=null)
        &&(CMLib.ableMapper().qualifiesByAnyCharClass(A.ID()))
        &&(acodes.contains(Integer.valueOf(A.classificationCode()&Ability.ALL_ACODES))))
        {
            Ability A2=mob.fetchAbility(A.ID());
            if(A2==null)
                mob.tell("You don't know '"+A.name()+"'.");
            else
            {
                int level=CMLib.ableMapper().qualifyingLevel(mob,A2);
                if(level<0) level=0;
                StringBuffer line=new StringBuffer("");
                line.append("\n\rLevel ^!"+level+"^?:\n\r");
                line.append("^N[^H"+CMStrings.padRight(Integer.toString(A2.proficiency()),3)+"%^?]^N "+CMStrings.padRight("^<HELP^>"+A2.name()+"^</HELP^>",19));
                line.append("^?\n\r");
                if(mob.session()!=null)
                    mob.session().wraplessPrintln(line.toString());
            }
            return true;
        }
        return false;
    }
    
    protected int parseOutLevel(Vector commands)
    {
        if((commands.size()>1)
        &&(commands.lastElement() instanceof String)
        &&(CMath.isNumber((String)commands.lastElement())))
        {
            int x=CMath.s_int((String)commands.lastElement());
            commands.removeElementAt(commands.size()-1);
            return x;
        }
        return -1;
    }

    protected void parseDomainInfo(MOB mob, Vector commands, Vector acodes, int[] level, int[] domain, String[] domainName)
    {
        level[0]=parseOutLevel(commands);
        String qual=CMParms.combine(commands,1).toUpperCase();
        domain[0]=-1;
        if(qual.length()>0)
        for(int i=1;i<Ability.DOMAIN_DESCS.length;i++)
            if(Ability.DOMAIN_DESCS[i].replace('_',' ').startsWith(qual))
            { domain[0]=i<<5; break;}
            else
            if((Ability.DOMAIN_DESCS[i].replace('_',' ').indexOf("/")>=0)
            &&(Ability.DOMAIN_DESCS[i].replace('_',' ').substring(Ability.DOMAIN_DESCS[i].indexOf("/")+1).startsWith(qual)))
            { domain[0]=i<<5; break;}
        if(domain[0]>0)
            domainName[0]=Ability.DOMAIN_DESCS[domain[0]>>5].toLowerCase();
        if((domain[0]<0)&&(qual.length()>0))
        {
            StringBuffer domains=new StringBuffer("");
            domains.append("\n\rValid schools/domains are: ");
            for(int i=1;i<Ability.DOMAIN_DESCS.length;i++)
            {
                boolean found=false;
                for(int a=0;a<acodes.size();a++)
                    found=found||CMLib.ableMapper().isDomainIncludedInAnyAbility(i<<5,((Integer)acodes.elementAt(a)).intValue());
                if(found)
                    domains.append(Ability.DOMAIN_DESCS[i].toLowerCase().replace('_',' ')+", ");
            }
            if(domains.toString().endsWith(", "))
                domains=new StringBuffer(domains.substring(0,domains.length()-2));
            if(!mob.isMonster())
                mob.session().wraplessPrintln(domains.toString()+"\n\r");
        }
        else
        if(qual.length()>0)
            domainName[0]+=" ";
    }
    
    
    protected StringBuilder getAbilities(MOB able, int ofType, int ofDomain, boolean addQualLine, int maxLevel)
    {
        Vector V=new Vector();
        int mask=Ability.ALL_ACODES;
        if(ofDomain>=0)
        {
            mask=Ability.ALL_ACODES|Ability.ALL_DOMAINS;
            ofType=ofType|ofDomain;
        }
        V.addElement(Integer.valueOf(ofType));
        return getAbilities(able,V,mask,addQualLine,maxLevel);
    }
    
    protected StringBuilder getAbilities(MOB able, Vector ofTypes, int mask, boolean addQualLine, int maxLevel)
    {
        int highestLevel=0;
        int lowestLevel=able.envStats().level()+1;
        StringBuilder msg=new StringBuilder("");
        for(int a=0;a<able.numAbilities();a++)
        {
            Ability thisAbility=able.fetchAbility(a);
            int level=CMLib.ableMapper().qualifyingLevel(able,thisAbility);
            if(level<0) level=0;
            if((thisAbility!=null)
            &&(level>highestLevel)
            &&(level<lowestLevel)
            &&(ofTypes.contains(Integer.valueOf(thisAbility.classificationCode()&mask))))
                highestLevel=level;
        }
        if((maxLevel>=0)&&(maxLevel<highestLevel))
            highestLevel=maxLevel;
        for(int l=0;l<=highestLevel;l++)
        {
        	StringBuilder thisLine=new StringBuilder("");
            int col=0;
            for(int a=0;a<able.numAbilities();a++)
            {
                Ability thisAbility=able.fetchAbility(a);
                int level=CMLib.ableMapper().qualifyingLevel(able,thisAbility);
                if(level<0) level=0;
                if((thisAbility!=null)
                &&(level==l)
                &&(ofTypes.contains(Integer.valueOf(thisAbility.classificationCode()&mask))))
                {
                    if(thisLine.length()==0)
                        thisLine.append("\n\rLevel ^!"+l+"^?:\n\r");
                    if((++col)>3)
                    {
                        thisLine.append("\n\r");
                        col=1;
                    }
                    thisLine.append("^N[^H"+CMStrings.padRight(Integer.toString(thisAbility.proficiency()),3)+"%^?]^N "+CMStrings.padRight("^<HELP^>"+thisAbility.name()+"^</HELP^>",(col==3)?18:19));
                }
            }
            if(thisLine.length()>0)
                msg.append(thisLine);
        }
        if(msg.length()==0)
            msg.append("^!None!^?");
        else
        if(addQualLine)
            msg.append("\n\r\n\rUse QUALIFY to see additional skills you can GAIN.");
        return msg;
    }
    
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		StringBuilder msg=new StringBuilder("");
		Vector V=new Vector();
		V.addElement(Integer.valueOf(Ability.ACODE_THIEF_SKILL));
		V.addElement(Integer.valueOf(Ability.ACODE_SKILL));
		V.addElement(Integer.valueOf(Ability.ACODE_COMMON_SKILL));
        String qual=CMParms.combine(commands,1).toUpperCase();
		if(parsedOutIndividualSkill(mob,qual,V))
			return true;
        int[] level=new int[1];
        int[] domain=new int[1];
        String[] domainName=new String[1];
        domainName[0]="";
        level[0]=-1;
        parseDomainInfo(mob,commands,V,level,domain,domainName);
        int mask=Ability.ALL_ACODES;
        if(domain[0]>=0)
        {
            mask=mask|Ability.ALL_DOMAINS;
            for(int v=0;v<V.size();v++)
                V.setElementAt(Integer.valueOf(((Integer)V.elementAt(v)).intValue()+domain[0]),v);
        }
        if((domain[0]>=0)||(qual.length()==0))
            msg.append("\n\r^HYour "+domainName[0].replace('_',' ')+"skills:^? "+getAbilities(mob,V,mask,true,level[0]));
		if(!mob.isMonster())
			mob.session().wraplessPrintln(msg.toString());
		return false;
	}
	
	public boolean canBeOrdered(){return true;}

	
}
