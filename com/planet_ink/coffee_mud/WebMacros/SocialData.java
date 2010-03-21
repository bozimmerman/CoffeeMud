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
public class SocialData extends StdWebMacro
{
    public String name()    {return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
    static String[] BTYPES={"NONE","ALL","SELF","TARGETMOB","TARGETITEM","TARGETINV","TARGETEQUIP"};
    static String[] BEXTNS={""," ALL"," SELF"," <T-NAME>"," <I-NAME>"," <V-NAME>"," <E-NAME>"};
    static String[] BFIELDS={"YOM","YONM","YOM","YTONM","YONM","YONM","YONM"};
    
    static String[] CODESTR={"WORDS","MOVEMENT","SOUND","VISUAL","HANDS"};
    static int[] CODES={CMMsg.MSG_SPEAK,CMMsg.MSG_NOISYMOVEMENT,CMMsg.MSG_NOISE,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_HANDS};

    public String runMacro(ExternalHTTPRequests httpReq, String parm)
    {
        Hashtable parms=parseParms(parm);
        String last=httpReq.getRequestParameter("SOCIAL");
        if(parms.containsKey("ISVFS"))
            return ""+(new CMFile("::/resources/socials.txt",null,true).exists());
        if(parms.containsKey("ISLFS"))
            return ""+(new CMFile("///resources/socials.txt",null,true).exists());
        if(parms.containsKey("NEWVFS"))
        {
            CMFile lf=new CMFile("///resources/socials.txt",null,true);
            if(!lf.exists()) return "true";
            CMFile vf=new CMFile("::/resources/socials.txt",null,true);
            if(!vf.exists()) return "false";
            return ""+(vf.lastModified() > lf.lastModified());
        }
        if(parms.containsKey("NEWLFS"))
        {
            CMFile lf=new CMFile("///resources/socials.txt",null,true);
            if(!lf.exists()) return "false";
            CMFile vf=new CMFile("::/resources/socials.txt",null,true);
            if(!vf.exists()) return "true";
            return ""+(vf.lastModified() < lf.lastModified());
        }
        if(parms.containsKey("TOVFS"))
        {
            MOB M = Authenticate.getAuthenticatedMob(httpReq);
            if(M==null) return "[authentication error]";
            CMFile lf=new CMFile("///resources/socials.txt",M,true);
            if(!lf.exists()) return "No local file.";
            CMFile vf=new CMFile("::/resources/socials.txt",M,false);
            if(vf.exists()) 
                if(!vf.delete())
                    return "Unable to delete existing vfs file.";
            vf=new CMFile("::/resources/socials.txt",M,false);
            if(!vf.canWrite())
                return "Unable to write new vfs file.";
            byte[] raw=lf.raw();
            if(!vf.saveRaw(raw))
                return "Unable to save new vfs file.";
            CMLib.socials().unloadSocials();
            return "Socials file copied from local filesystem to vfs";
        }
        if(parms.containsKey("TOLFS"))
        {
            MOB M = Authenticate.getAuthenticatedMob(httpReq);
            if(M==null) return "[authentication error]";
            CMFile lf=new CMFile("::/resources/socials.txt",M,true);
            if(!lf.exists()) return "No vfs file.";
            CMFile vf=new CMFile("///resources/socials.txt",M,false);
            if(vf.exists()) 
                if(!vf.delete())
                    return "Unable to delete existing local file.";
            vf=new CMFile("///resources/socials.txt",M,false);
            if(!vf.canWrite())
                return "Unable to write new local file.";
            byte[] raw=lf.raw();
            if(!vf.saveRaw(raw))
                return "Unable to save new local file.";
            CMLib.socials().unloadSocials();
            return "Socials file copied from vfs filesystem to local file.";
        }
        if(parms.containsKey("NOVFS"))
        {
            MOB M = Authenticate.getAuthenticatedMob(httpReq);
            if(M==null) return "[authentication error]";
            CMFile vf=new CMFile("::/resources/socials.txt",M,false);
            if(vf.exists()) 
                if(!vf.delete())
                    return "Unable to delete existing vfs file.";
            CMLib.socials().unloadSocials();
            return "Socials file removed from vfs";
        }
        if(parms.containsKey("NOLFS"))
        {
            MOB M = Authenticate.getAuthenticatedMob(httpReq);
            if(M==null) return "[authentication error]";
            CMFile vf=new CMFile("///resources/socials.txt",M,false);
            if(vf.exists()) 
                if(!vf.delete())
                    return "Unable to delete existing local file.";
            CMLib.socials().unloadSocials();
            return "Socials file removed from local file system.";
        }
                
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
            MOB M = Authenticate.getAuthenticatedMob(httpReq);
            if(M==null) return "[authentication error]";
            if(!CMSecurity.isAllowed(M,M.location(),"CMDSOCIALS")) return "[authentication error]";
            
            boolean create=false;
            Vector SV=CMLib.socials().getSocialsSet(last);
            Vector OSV=null;
            if(SV==null)
            	create=true;
            else
            	OSV=(Vector)SV.clone();
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
            
            Vector TYPES=new Vector();
            Vector EXTNS=new Vector();
            for(int b=0;b<BTYPES.length;b++)
                TYPES.addElement(BTYPES[b]);
            for(int b=0;b<BEXTNS.length;b++)
                EXTNS.addElement(BEXTNS[b]);
            old=httpReq.getRequestParameter("NUMXTRAS");
            if(old!=null)
            {
                int numXtras=CMath.s_int(httpReq.getRequestParameter("NUMXTRAS"));
                for(int n=0;n<numXtras;n++)
                {
                    old=httpReq.getRequestParameter("XSOCIAL"+n);
                    if((old!=null)
                    &&(old.length()>0)
                    &&(httpReq.isRequestParameter("IS"+old.toUpperCase().trim()))
                    &&(httpReq.getRequestParameter("IS"+old.toUpperCase().trim()).equalsIgnoreCase("on")))
                    {
                        TYPES.addElement(old.toUpperCase().trim());
                        EXTNS.addElement(" "+old.toUpperCase().trim());
                    }
                }
            }
            
            for(int t=0;t<TYPES.size();t++)
            {
                String TYPE=(String)TYPES.elementAt(t);
                String EXTN=(String)EXTNS.elementAt(t);
                
                old=httpReq.getRequestParameter("IS"+TYPE);
                if((old==null)||(!old.equalsIgnoreCase("on"))) continue;
                
                Social S=CMLib.socials().makeDefaultSocial(last,EXTN);
                String field=(t<BTYPES.length)?BFIELDS[t]:BFIELDS[0];
                for(int f=0;f<field.length();f++)
                {
                    String fnam="SDAT_"+TYPE+"_"+field.charAt(f);
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
                    old=httpReq.getRequestParameter(fnam+"C");
                    if(old!=null) {
                        switch(field.charAt(f)) {
                            case 'Y': S.setSourceCode(CMath.s_int(old)); 
                                      break;
                            case 'O': S.setTargetCode(CMath.s_int(old));
                                      S.setOthersCode(CMath.s_int(old)); 
                                      break;
                            case 'N': break;
                            case 'M': break;
                            case 'T': break;
                        }
                    }
                }
                SV.addElement(S);
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
            Log.sysOut(M.name()+" updated social "+last);
            return "Social "+last+" updated";
        }
        else
        if(parms.containsKey("DELETE"))
        {
            MOB M = Authenticate.getAuthenticatedMob(httpReq);
            if(M==null) return "[authentication error]";
            if(!CMSecurity.isAllowed(M,M.location(),"CMDSOCIALS")) return "[authentication error]";
            if(last==null) return " @break@";
            Vector SV=CMLib.socials().getSocialsSet(last);
            if(SV==null)
                return "Unknown social!";
            SV=(Vector)SV.clone();
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
                    String old;
                    
                    if(parms.containsKey("TITLE"))
                    {
                        old=httpReq.getRequestParameter("TITLE");
                        if(old==null) 
                            old=last;
                        if(old!=null)
                            str.append(old+", ");
                    }
                    Vector TYPES=new Vector();
                    Vector EXTNS=new Vector();
                    for(int b=0;b<BTYPES.length;b++)
                        TYPES.addElement(BTYPES[b]);
                    for(int b=0;b<BEXTNS.length;b++)
                        EXTNS.addElement(BEXTNS[b]);
                    old=httpReq.getRequestParameter("NUMXTRAS");
                    if(old!=null)
                    {
                        int numXtras=CMath.s_int(httpReq.getRequestParameter("NUMXTRAS"));
                        for(int n=0;n<numXtras;n++)
                        {
                            old=httpReq.getRequestParameter("XSOCIAL"+n);
                            if((old!=null)
                            &&(old.length()>0)
                            &&(httpReq.isRequestParameter("IS"+old.toUpperCase().trim()))
                            &&(httpReq.getRequestParameter("IS"+old.toUpperCase().trim()).equalsIgnoreCase("on")))
                            {
                                TYPES.addElement(old.toUpperCase().trim());
                                EXTNS.addElement(old.toUpperCase().trim());
                            }
                        }
                    }
                    else
                    for(int s=0;s<SV.size();s++)
                    {
                        Social S=(Social)SV.elementAt(s);
                        boolean found=false;
                        for(int b=0;b<BEXTNS.length;b++)
                            if(S.Name().equalsIgnoreCase(last+BEXTNS[b]))
                                found=true;
                        if(!found)
                        {
                            int x=S.Name().indexOf(' ');
                            if(x>0)
                            {
                                String TYPE=S.Name().substring(x+1).trim().toUpperCase();
                                TYPES.addElement(TYPE);
                                EXTNS.addElement(" "+TYPE);
                                httpReq.addRequestParameters("IS"+TYPE,"on");
                            }
                        }
                    }
                    
                    old=httpReq.getRequestParameter("DOADDXSOCIAL");
                    if((old!=null)
                    &&(old.equalsIgnoreCase("on"))
                    &&(httpReq.getRequestParameter("ADDXSOCIAL")!=null)
                    &&(httpReq.getRequestParameter("ADDXSOCIAL").trim().length()>0)
                    &&(!TYPES.contains(httpReq.getRequestParameter("ADDXSOCIAL").toUpperCase().trim())))
                    {
                        String TYPE=httpReq.getRequestParameter("ADDXSOCIAL").toUpperCase().trim();
                        TYPES.addElement(TYPE);
                        EXTNS.addElement(" "+TYPE);
                        httpReq.addRequestParameters("IS"+TYPE,"on");
                    }
                    
                    int numxtras=TYPES.size()-BTYPES.length;
                    if(parms.containsKey("NUMEXTRAS"))
                        str.append(""+numxtras+", ");
                    if(parms.containsKey("GETEXTRA")
                    &&(CMath.s_int((String)parms.get("GETEXTRA"))<numxtras))
                        str.append(TYPES.elementAt(BTYPES.length+CMath.s_int((String)parms.get("GETEXTRA")))+", ");
                    
                    
                    for(int t=0;t<TYPES.size();t++)
                    {
                        String TYPE=(String)TYPES.elementAt(t);
                        String EXTN=(String)EXTNS.elementAt(t);
                        Social S=null;
                        for(int s=0;s<SV.size();s++)
                            if(((Social)SV.elementAt(s)).Name().equalsIgnoreCase(last+EXTN))
                            {
                                S=(Social)SV.elementAt(s);
                                break;
                            }
                        if(parms.containsKey("IS"+TYPE))
                        {
                            old=httpReq.getRequestParameter("IS"+TYPE);
                            if(old==null)
                                old=(((S!=null)&&(!httpReq.isRequestParameter("NUMXTRAS")))?"on":"");
                            str.append(""+old.equalsIgnoreCase("on")+", ");
                            if(!old.equalsIgnoreCase("on"))
                                continue;
                        }
                        String field=(t<BTYPES.length)?BFIELDS[t]:BFIELDS[0];
                        for(int f=0;f<field.length();f++)
                        {
                            String fnam="SDAT_"+TYPE+"_"+field.charAt(f);
                            if(parms.containsKey(fnam))
                            {
                                old=httpReq.getRequestParameter(fnam);
                                if(old==null) {
                                    if(S==null) 
                                        S=CMLib.socials().makeDefaultSocial(last,EXTN);
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
                                old=httpReq.getRequestParameter(fnam+"C");
                                if(old==null) {
                                    if(S==null) 
                                        S=CMLib.socials().makeDefaultSocial(last,EXTN);
                                    switch(field.charAt(f)) {
                                        case 'Y': old=(S==null)?null:""+S.sourceCode(); break;
                                        case 'O': old=(S==null)?null:""+S.targetCode(); break;
                                        case 'N': old=null; break;
                                        case 'M': old=null; break;
                                        case 'T': old=null; break;
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
