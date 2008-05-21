package com.planet_ink.coffee_mud.WebMacros;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;



/*
   Copyright 2000-2008 Bo Zimmerman

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
public class SocialData extends StdWebMacro
{
    public String name()    {return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
    static String[] TYPES={"NONE","ALL","SELF","TARGET"};
    static String[] EXTNS={""," ALL"," SELF"," <T-NAME>"};
    static String[] FIELDS={"YOM","YONM","YOM","YTONM"};
    static String[] CODESTR={"WORDS","MOVEMENT","SOUND","VISUAL","HANDS"};
    static int[] CODES={CMMsg.MSG_SPEAK,CMMsg.MSG_NOISYMOVEMENT,CMMsg.MSG_NOISE,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_HANDS};

    public String runMacro(ExternalHTTPRequests httpReq, String parm)
    {
        Hashtable parms=parseParms(parm);
        String last=httpReq.getRequestParameter("SOCIAL");
        if((last==null)&&(!parms.containsKey("EDIT"))) return " @break@";

        
        String replaceCommand=httpReq.getRequestParameter("REPLACE");
        if((replaceCommand != null) 
        && (replaceCommand.length()>0)
        && (replaceCommand.indexOf('=')>0))
        {
            int eq=replaceCommand.indexOf('=');
            String field=replaceCommand.substring(0,eq);
            String value=replaceCommand.substring(eq+1);
            httpReq.addRequestParameters(field, value);
            httpReq.addRequestParameters("REPLACE","");
        }
        
        if(parms.containsKey("EDIT"))
        {
            MOB M=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
            if(M==null) return "[authentication error]";
            if(!CMSecurity.isAllowed(M,M.location(),"CMDSOCIALS")) return "[authentication error]";
            
            Vector SV=CMLib.socials().getSocialsSet(last);
            Vector OSV=SV;
            boolean create=false;
            create=(SV==null);
            SV=new Vector();
            
            String old=httpReq.getRequestParameter("TITLE");
            if((old!=null)
            &&(old.trim().length()>0)
            &&(!old.equalsIgnoreCase(last)))
            {
                old=CMStrings.replaceAll(old.toUpperCase()," ","_").trim();
                if(CMLib.socials().getSocialsSet(last)!=null)
                    return "[new social name already exists]";
                last=old;
            }
            
            for(int t=0;t<TYPES.length;t++)
            {
                if(parms.containsKey("IS"+TYPES[t]))
                {
                    old=httpReq.getRequestParameter("IS"+TYPES[t]);
                    if(!old.equalsIgnoreCase("on")) continue;
                }
                Social S=CMLib.socials().makeDefaultSocial(last,EXTNS[t]);
                String field=FIELDS[t];
                for(int f=0;f<field.length();f++)
                {
                    String fnam="SDAT_"+TYPES[t]+"_"+field.charAt(f);
                    old=httpReq.getRequestParameter(fnam);
                    if(old!=null) {
                        switch(field.charAt(f)) {
                            case 'Y': S.setYou_see(old); break;
                            case 'O': S.setThird_party_sees(old); break;
                            case 'N': S.setSee_when_no_target(old); break;
                            case 'M': S.setMSPfile(old); break;
                            case 'T': S.setTarget_sees(old); break;
                        }
                    }
                    old=httpReq.getRequestParameter(fnam);
                    if(old!=null) {
                        switch(field.charAt(f)) {
                            case 'Y': S.setSourceCode(CMath.s_int(old)); break;
                            case 'O': S.setOthersCode(CMath.s_int(old)); break;
                            case 'N': break;
                            case 'M': break;
                            case 'T': S.setTargetCode(CMath.s_int(old)); break;
                        }
                    }
                }
            }
            if(OSV!=null)
                for(int s=0;s<OSV.size();s++)
                    CMLib.socials().remove(((Social)OSV.elementAt(s)).Name());
            
            for(int s=0;s<SV.size();s++)
                CMLib.socials().addSocial((Social)SV.elementAt(s));
            
            CMLib.socials().save(M);
            if(create)
            {
                Log.sysOut(M.name()+" created social "+last);
                return "Social "+last+" created";
            }
            else
            {
                Log.sysOut(M.name()+" updated social "+last);
                return "Social "+last+" updated";
            }
        }
        else
        if(parms.containsKey("DELETE"))
        {
            MOB M=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
            if(M==null) return "[authentication error]";
            if(!CMSecurity.isAllowed(M,M.location(),"CMDSOCIALS")) return "[authentication error]";
            if(last==null) return " @break@";
            Vector SV=CMLib.socials().getSocialsSet(last);
            if(SV==null)
                return "Unknown social!";
            for(int s=0;s<SV.size();s++)
                CMLib.socials().remove(((Social)SV.elementAt(s)).Name());
            CMLib.socials().save(M);
            Log.sysOut(M.name()+" deleted social "+last);
            return "Social deleted.";
        }
        else
        {
            if(last==null) return " @break@";
            if(last.length()>0)
            {
                Vector SV=null;
                String newSocialID=httpReq.getRequestParameter("NEWSOCIAL");
                if(SV==null)
                    SV=(Vector)httpReq.getRequestObjects().get("SOCIAL-"+last);
                if((SV==null)
                &&(newSocialID!=null)
                &&(newSocialID.length()>0)
                &&(CMLib.socials().getSocialsSet(newSocialID)==null))
                {
                    SV=new Vector();
                    last=newSocialID;
                    httpReq.addRequestParameters("SOCIAL",newSocialID);
                }
                if(SV==null)
                    SV=CMLib.socials().getSocialsSet(last);
                if(parms.containsKey("ISNEWSOCIAL"))
                    return ""+(CMLib.socials().getSocialsSet(last)==null);
                if(SV!=null)
                {
                    StringBuffer str=new StringBuffer("");
                    if(parms.containsKey("TITLE"))
                    {
                        String old=httpReq.getRequestParameter("TITLE");
                        if(old==null) 
                            old=last;
                        if(old!=null)
                            str.append(old+", ");
                    }
                    String old;
                    for(int t=0;t<TYPES.length;t++)
                    {
                        Social S=null;
                        for(int s=0;s<SV.size();s++)
                            if(((Social)SV.elementAt(s)).Name().equalsIgnoreCase(last+EXTNS[t]))
                            {
                                S=(Social)SV.elementAt(s);
                                break;
                            }
                        if(parms.containsKey("IS"+TYPES[t]))
                        {
                            old=httpReq.getRequestParameter("IS"+TYPES[t]);
                            if(old==null) old=((S!=null)?"on":"");
                            str.append(""+old.equalsIgnoreCase("on")+", ");
                            if(!old.equalsIgnoreCase("on"))
                                continue;
                        }
                        String field=FIELDS[t];
                        for(int f=0;f<field.length();f++)
                        {
                            String fnam="SDAT_"+TYPES[t]+"_"+field.charAt(f);
                            if(parms.containsKey(fnam))
                            {
                                old=httpReq.getRequestParameter(fnam);
                                if(old==null) {
                                    if(S==null) 
                                        S=CMLib.socials().makeDefaultSocial(last,EXTNS[t]);
                                    switch(field.charAt(f)) {
                                        case 'Y': old=S.You_see(); break;
                                        case 'O': old=S.Third_party_sees(); break;
                                        case 'N': old=S.See_when_no_target(); break;
                                        case 'M': old=S.MSPfile(); break;
                                        case 'T': old=S.Target_sees(); break;
                                    }
                                }
                                str.append(old+", ");
                            }
                            if(parms.containsKey(fnam+"C"))
                            {
                                old=httpReq.getRequestParameter(fnam);
                                if(old==null) {
                                    if(S==null) 
                                        S=CMLib.socials().makeDefaultSocial(last,EXTNS[t]);
                                    switch(field.charAt(f)) {
                                        case 'Y': old=""+S.sourceCode(); break;
                                        case 'O': old=""+S.othersCode(); break;
                                        case 'N': old=null; break;
                                        case 'M': old=null; break;
                                        case 'T': old=""+S.targetCode(); break;
                                    }
                                }
                                if(old!=null)
                                    for(int c=0;c<CODES.length;c++)
                                    {
                                        str.append("<OPTION VALUE="+CODES[c]);
                                        if(CMath.s_int(old)==CODES[c])
                                            str.append(" SELECTED");
                                        str.append(">"+CODESTR[c]);
                                    }
                            }
                        }
                    }
                    
/*
public String Name();
public void setName(String newName);
public String You_see();
public String Third_party_sees();
public String Target_sees();
public String See_when_no_target();
public int sourceCode();
public int othersCode();
public int targetCode();
public void setYou_see(String str);
public void setThird_party_sees(String str);
public void setTarget_sees(String str);
public void setSee_when_no_target(String str);
public void setSourceCode(int code);
public void setOthersCode(int code);
public void setTargetCode(int code);
public boolean targetable();
public String MSPfile();
public void setMSPfile(String newFile);
*/
                    httpReq.getRequestObjects().put("SOCIAL-"+last,SV);
                    String strstr=str.toString();
                    if(strstr.endsWith(", "))
                        strstr=strstr.substring(0,strstr.length()-2);
                    return clearWebMacros(strstr);
                }
            }
        }
        return " @break@";
    }
}
