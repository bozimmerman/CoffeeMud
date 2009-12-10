package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary.ExpertiseDefinition;
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
public class ColumbiaUniv extends StdLibrary implements ExpertiseLibrary
{
    public String ID(){return "ColumbiaUniv";}
								
	protected Hashtable completeEduMap=new Hashtable();
    protected Hashtable[] completeUsageMap=new Hashtable[ExpertiseLibrary.NUM_XFLAGS];
    protected Properties helpMap=new Properties();
    protected DVector rawDefinitions=new DVector(7);

    public ExpertiseLibrary.ExpertiseDefinition addDefinition(String ID, String name, String listMask, String finalMask, int practices, int trains, int qpCost, int expCost, int timeCost)
    {
        ExpertiseLibrary.ExpertiseDefinition def=getDefinition(ID);
    	if(def!=null) return  def;
    	if(CMSecurity.isDisabled("EXPERTISE_"+ID.toUpperCase())) return null;
    	if(CMSecurity.isDisabled("EXPERTISE_*")) return null;
    	for(int i=1;i<ID.length();i++)
        	if(CMSecurity.isDisabled("EXPERTISE_"+ID.substring(0,i).toUpperCase()+"*")) 
        		return null;
        def=new  ExpertiseLibrary.ExpertiseDefinition();
    	def.ID=ID.toUpperCase();
    	def.name=name;
    	def.addListMask(listMask);
    	def.addFinalMask(finalMask);
    	def.practiceCost=practices;
    	def.trainCost=trains;
    	def.qpCost=qpCost;
    	def.expCost=expCost;
    	def.timeCost=timeCost;
    	completeEduMap.put(def.ID,def);
        return def;
    }
    public String getExpertiseHelp(String ID, boolean exact)
    {
    	if(ID==null) return null;
    	ID=ID.toUpperCase();
    	if(exact) return helpMap.getProperty(ID);
    	for(Enumeration<Object> e = helpMap.keys();e.hasMoreElements();)
    	{
    		String key = e.nextElement().toString();
    		if(key.startsWith(ID)) return helpMap.getProperty(key);
    	}
    	for(Enumeration<Object> e = helpMap.keys();e.hasMoreElements();)
    	{
    		String key = e.nextElement().toString();
    		if(CMLib.english().containsString(key, ID)) return helpMap.getProperty(key);
    	}
    	return null;
    }
    
    public void delDefinition(String ID){
    	completeEduMap.remove(ID);
    }
    public Enumeration definitions(){ return DVector.s_enum(completeEduMap,false);}
    public ExpertiseDefinition getDefinition(String ID){ return (ID==null)?null:(ExpertiseDefinition)completeEduMap.get(ID.trim().toUpperCase());}
    public ExpertiseDefinition findDefinition(String ID, boolean exactOnly)
    {
        ExpertiseDefinition D=getDefinition(ID);
        if(D!=null) return D;
        for(Enumeration e=definitions();e.hasMoreElements();)
        {
            D=(ExpertiseDefinition)e.nextElement();
            if(D.name.equalsIgnoreCase(ID)) return D;
        }
        if(exactOnly) return null;
        for(Enumeration e=definitions();e.hasMoreElements();)
        {
            D=(ExpertiseDefinition)e.nextElement();
            if(D.ID.startsWith(ID)) return D;
        }
        for(Enumeration e=definitions();e.hasMoreElements();)
        {
            D=(ExpertiseDefinition)e.nextElement();
            if(CMLib.english().containsString(D.name,ID)) return D;
        }
        return null;
    }
    
    public Vector myQualifiedExpertises(MOB mob)
    {
    	ExpertiseDefinition D=null;
    	Vector V=new Vector();
    	for(Enumeration e=definitions();e.hasMoreElements();)
    	{
    		D=(ExpertiseDefinition)e.nextElement();
    		if(((D.compiledFinalMask()==null)||(CMLib.masking().maskCheck(D.compiledFinalMask(),mob,true)))
    		&&((D.compiledListMask()==null)||(CMLib.masking().maskCheck(D.compiledListMask(),mob,true))))
    			V.addElement(D);
    	}
    	return V;
    }
    public Vector myListableExpertises(MOB mob)
    {
    	ExpertiseDefinition D=null;
    	Vector V=new Vector();
    	for(Enumeration e=definitions();e.hasMoreElements();)
    	{
    		D=(ExpertiseDefinition)e.nextElement();
    		if((D.compiledListMask()==null)||(CMLib.masking().maskCheck(D.compiledListMask(),mob,true)))
    			V.addElement(D);
    	}
    	return V;
    }
    public int numExpertises(){return completeEduMap.size();}
    
    private String expertMath(String s,int l)
    {
        int x=s.indexOf("{");
        while(x>=0)
        {
            int y=s.indexOf("}",x);
            if(y<0) break;
            s=s.substring(0,x)+CMath.parseIntExpression(s.substring(x+1,y))+s.substring(y+1);
            x=s.indexOf("{");
        }
        return s;
    }
    
    public int getExpertiseLevel(MOB mob, String expertise)
    {
        if((mob==null)||(expertise==null)) return 0;
        int level=0;
        expertise=expertise.toUpperCase();
        String X=null;
        for(int i=0;i<mob.numExpertises();i++)
        {
            X=mob.fetchExpertise(i);
            if((X!=null)&&(X.startsWith(expertise)))
            {
                int x=CMath.s_int(X.substring(expertise.length()));
                if(x>level) level=x;
            }
        }
        return level;
    }

    public Vector getStageCodes(String expertiseCode)
    {
        String key=null;
        Vector codes=new Vector();
        if(expertiseCode==null) return codes;
        expertiseCode=expertiseCode.toUpperCase();
        for(Enumeration e=completeEduMap.keys();e.hasMoreElements();)
        {
            key=(String)e.nextElement();
            if(key.startsWith(expertiseCode)
            &&(CMath.isInteger(key.substring(expertiseCode.length()))||CMath.isRomanNumeral(key.substring(expertiseCode.length()))))
                codes.addElement(key);
        }
        return codes;
    }
    public int getStages(String expertiseCode){return getStageCodes(expertiseCode).size();}
    
    public String getApplicableExpertise(String ID, int code)
    {
        return (String)completeUsageMap[code].get(ID);
    }
    public int getApplicableExpertiseLevel(String ID, int code, MOB mob)
    {
        return getExpertiseLevel(mob,(String)completeUsageMap[code].get(ID));
    }
    
    public String confirmExpertiseLine(String row, String ID, boolean addIfPossible)
    {
        int levels=0;
        HashSet flags=new HashSet();
        String s=null;
        String skillMask=null;
        int[] costs=new int[5];
        String WKID=null;
        String name,WKname=null;
        String listMask,WKlistMask=null;
        String finalMask,WKfinalMask=null;
        Vector skillsToRegister=null;
        ExpertiseLibrary.ExpertiseDefinition def=null;
        boolean didOne=false;
        if(row.trim().startsWith("#")||row.trim().startsWith(";")||(row.trim().length()==0)) return null;
        int x=row.indexOf("=");
        if(x<0) return "Error: Invalid line! Not comment, whitespace, and does not contain an = sign!"; 
        if(row.trim().toUpperCase().startsWith("HELP_"))
        {
            String lastID=ID;
            ID=row.substring(0,x).toUpperCase();
            row=row.substring(x+1);
            ID=ID.substring(5).toUpperCase();
            if(ID.length()==0) ID=lastID;
            if((lastID==null)||(lastID.length()==0))
                return "Error: No last expertise found for help: "+lastID+"="+row;
            else
            if(getDefinition(ID)!=null)
            {
                def=getDefinition(ID);
                WKID=def.name.toUpperCase().replace(' ','_');
                if(addIfPossible)
                {
                    helpMap.remove(WKID);
                    helpMap.put(WKID,row);
                }
            }
            else
            {
                Vector stages=getStageCodes(ID);
                if((stages==null)||(stages.size()==0))
                    return "Error: Expertise not yet defined: "+ID+"="+row;
                def=getDefinition((String)stages.elementAt(0));
                if(def!=null)
                {
                    WKID=def.name.toUpperCase().replace(' ','_');
                    x=WKID.lastIndexOf("_");
                    if((x>=0)&&(CMath.isInteger(WKID.substring(x+1))||CMath.isRomanNumeral(WKID.substring(x+1))))
                    {
                        WKID=WKID.substring(0,x);
                        if(addIfPossible)
                        if(!helpMap.containsKey(WKID))
                            helpMap.put(WKID,row+"\n\r(See help on "+def.name+").");
                    }
                }
                if(addIfPossible)
                for(int s1=0;s1<stages.size();s1++)
                {
                    def=getDefinition((String)stages.elementAt(s1));
                    if(def==null) continue;
                    WKID=def.name.toUpperCase().replace(' ','_');
                    if(!helpMap.containsKey(WKID)) helpMap.put(WKID,row);
                }
            }
            return null;
        }
        ID=row.substring(0,x).toUpperCase();
        row=row.substring(x+1);
        Vector parts=CMParms.parseCommas(row,false);
        if(parts.size()!=11)
            return "Error: Expertise row malformed (Requires 11 entries/10 commas): "+ID+"="+row;
        name=(String)parts.elementAt(0);
        if(name.length()==0)
            return "Error: Expertise name ("+name+") malformed: "+ID+"="+row;
        if(!CMath.isInteger((String)parts.elementAt(1)))
            return "Error: Expertise num ("+((String)parts.elementAt(1))+") malformed: "+ID+"="+row;
        levels=CMath.s_int((String)parts.elementAt(1));
        flags.clear();
        flags.addAll(CMParms.parseAny(((String)parts.elementAt(2)).toUpperCase(),"|",true));
        
        skillMask=(String)parts.elementAt(3);
        if(skillMask.length()==0)
            return "Error: Expertise skill mask ("+skillMask+") malformed: "+ID+"="+row;
        skillsToRegister=CMLib.masking().getAbilityEduReqs(skillMask);
        if(skillsToRegister.size()==0)
            return "Error: Expertise no skills ("+skillMask+") found: "+ID+"="+row;
        listMask=skillMask+" "+((String)parts.elementAt(4));
        finalMask=(((String)parts.elementAt(5)));
        for(int i=6;i<11;i++)
            costs[i-6]=CMath.s_int((String)parts.elementAt(i));
        didOne=false;
        for(int u=0;u<completeUsageMap.length;u++)
            didOne=didOne||flags.contains(ExpertiseLibrary.XFLAG_CODES[u]);
        if(!didOne)
            return "Error: No flags ("+((String)parts.elementAt(2)).toUpperCase()+") were set: "+ID+"="+row;
        if(addIfPossible)
        for(int l=1;l<=levels;l++)
        {
            WKID=CMStrings.replaceAll(ID,"@X1",""+l);
            WKID=CMStrings.replaceAll(WKID,"@X2",""+CMath.convertToRoman(l));
            WKname=CMStrings.replaceAll(name,"@x1",""+l);
            WKname=CMStrings.replaceAll(WKname,"@x2",""+CMath.convertToRoman(l));
            WKlistMask=CMStrings.replaceAll(listMask,"@x1",""+l);
            WKlistMask=CMStrings.replaceAll(WKlistMask,"@x2",""+CMath.convertToRoman(l));
            WKfinalMask=CMStrings.replaceAll(finalMask,"@x1",""+l);
            WKfinalMask=CMStrings.replaceAll(WKfinalMask,"@x2",""+CMath.convertToRoman(l));
            if((l>1)&&(listMask.toUpperCase().indexOf("-EXPERT")<0))
            {
                s=CMStrings.replaceAll(ID,"@X1",""+(l-1));
                s=CMStrings.replaceAll(s,"@X2",""+CMath.convertToRoman(l-1));
                WKlistMask="-EXPERTISE \"+"+s+"\" "+WKlistMask;
            }
            WKlistMask=expertMath(WKlistMask,l);
            WKfinalMask=expertMath(WKfinalMask,l);
            def=addDefinition(WKID,WKname,WKlistMask,WKfinalMask,costs[0],costs[1],costs[2],costs[3],costs[4]);
            if(def!=null){
                def.compiledFinalMask();
                def.compiledListMask();
            }
        }
        ID=CMStrings.replaceAll(ID,"@X1","");
        ID=CMStrings.replaceAll(ID,"@X2","");
        for(int u=0;u<completeUsageMap.length;u++)
            if(flags.contains(ExpertiseLibrary.XFLAG_CODES[u]))
                for(int k=0;k<skillsToRegister.size();k++)
                    completeUsageMap[u].put((String)skillsToRegister.elementAt(k),ID);
        return addIfPossible?ID:null;
    }
    
    public void recompileExpertises()
    {
        for(int u=0;u<completeUsageMap.length;u++)
            completeUsageMap[u]=new Hashtable();
        helpMap.clear();
        Vector V=Resources.getFileLineVector(Resources.getFileResource("skills/expertises.txt",true));
        String ID=null,WKID=null;
        for(int v=0;v<V.size();v++)
        {
            String row=(String)V.elementAt(v);
            WKID=this.confirmExpertiseLine(row,ID,true);
            if(WKID==null) continue;
            if(WKID.startsWith("Error: "))
                Log.errOut("ColumbiaUniv",WKID);
            else
                ID=WKID;
        }
    }
    
}
