package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
   Copyright 2008-2018 Bo Zimmerman

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
	@Override
	public String name()
	{
		return "SocialData";
	}
	static String[] BTYPES={"NONE","ALL","SELF","TARGETMOB","TARGETITEM","TARGETINV","TARGETEQUIP"};
	static String[] BEXTNS={""," ALL"," SELF"," <T-NAME>"," <I-NAME>"," <V-NAME>"," <E-NAME>"};
	static String[] BFIELDS={"YOM","YONM","YOM","YTONM","YONM","YONM","YONM"};

	static String[] CODESTR={"WORDS","MOVEMENT","SOUND","VISUAL","HANDS"};
	static int[] CODES={CMMsg.MSG_SPEAK,CMMsg.MSG_NOISYMOVEMENT,CMMsg.MSG_NOISE,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_HANDS};

	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		String last=httpReq.getUrlParameter("SOCIAL");
		if(parms.containsKey("ISVFS"))
			return ""+(new CMFile("::/resources/socials.txt",null,CMFile.FLAG_LOGERRORS).exists());
		if(parms.containsKey("ISLFS"))
			return ""+(new CMFile("///resources/socials.txt",null,CMFile.FLAG_LOGERRORS).exists());
		if(parms.containsKey("NEWVFS"))
		{
			final CMFile lf=new CMFile("///resources/socials.txt",null,CMFile.FLAG_LOGERRORS);
			if(!lf.exists())
				return "true";
			final CMFile vf=new CMFile("::/resources/socials.txt",null,CMFile.FLAG_LOGERRORS);
			if(!vf.exists())
				return "false";
			return ""+(vf.lastModified() > lf.lastModified());
		}
		if(parms.containsKey("NEWLFS"))
		{
			final CMFile lf=new CMFile("///resources/socials.txt",null,CMFile.FLAG_LOGERRORS);
			if(!lf.exists())
				return "false";
			final CMFile vf=new CMFile("::/resources/socials.txt",null,CMFile.FLAG_LOGERRORS);
			if(!vf.exists())
				return "true";
			return ""+(vf.lastModified() < lf.lastModified());
		}
		if(parms.containsKey("TOVFS"))
		{
			final MOB M = Authenticate.getAuthenticatedMob(httpReq);
			if(M==null)
				return "[authentication error]";
			final CMFile lf=new CMFile("///resources/socials.txt",M,CMFile.FLAG_LOGERRORS);
			if(!lf.exists())
				return "No local file.";
			CMFile vf=new CMFile("::/resources/socials.txt",M);
			if(vf.exists())
			{
				if(!vf.delete())
					return "Unable to delete existing vfs file.";
			}
			vf=new CMFile("::/resources/socials.txt",M);
			if(!vf.canWrite())
				return "Unable to write new vfs file.";
			final byte[] raw=lf.raw();
			if(!vf.saveRaw(raw))
				return "Unable to save new vfs file.";
			CMLib.socials().unloadSocials();
			return "Socials file copied from local filesystem to vfs";
		}
		if(parms.containsKey("TOLFS"))
		{
			final MOB M = Authenticate.getAuthenticatedMob(httpReq);
			if(M==null)
				return "[authentication error]";
			final CMFile lf=new CMFile("::/resources/socials.txt",M,CMFile.FLAG_LOGERRORS);
			if(!lf.exists())
				return "No vfs file.";
			CMFile vf=new CMFile("///resources/socials.txt",M);
			if(vf.exists())
			{
				if(!vf.delete())
					return "Unable to delete existing local file.";
			}
			vf=new CMFile("///resources/socials.txt",M);
			if(!vf.canWrite())
				return "Unable to write new local file.";
			final byte[] raw=lf.raw();
			if(!vf.saveRaw(raw))
				return "Unable to save new local file.";
			CMLib.socials().unloadSocials();
			return "Socials file copied from vfs filesystem to local file.";
		}
		if(parms.containsKey("NOVFS"))
		{
			final MOB M = Authenticate.getAuthenticatedMob(httpReq);
			if(M==null)
				return "[authentication error]";
			final CMFile vf=new CMFile("::/resources/socials.txt",M);
			if(vf.exists())
			{
				if(!vf.delete())
					return "Unable to delete existing vfs file.";
			}
			CMLib.socials().unloadSocials();
			return "Socials file removed from vfs";
		}
		if(parms.containsKey("NOLFS"))
		{
			final MOB M = Authenticate.getAuthenticatedMob(httpReq);
			if(M==null)
				return "[authentication error]";
			final CMFile vf=new CMFile("///resources/socials.txt",M);
			if(vf.exists())
			{
				if(!vf.delete())
					return "Unable to delete existing local file.";
			}
			CMLib.socials().unloadSocials();
			return "Socials file removed from local file system.";
		}

		if((last==null)&&(!parms.containsKey("EDIT")))
			return " @break@";

		final String replaceCommand=httpReq.getUrlParameter("REPLACE");
		if((replaceCommand != null)
		&& (replaceCommand.length()>0)
		&& (replaceCommand.indexOf('=')>0))
		{
			final int eq=replaceCommand.indexOf('=');
			final String field=replaceCommand.substring(0,eq);
			final String value=replaceCommand.substring(eq+1);
			httpReq.addFakeUrlParameter(field, value);
			httpReq.addFakeUrlParameter("REPLACE","");
		}

		if(parms.containsKey("EDIT"))
		{
			final MOB M = Authenticate.getAuthenticatedMob(httpReq);
			if(M==null)
				return "[authentication error]";
			if(!CMSecurity.isAllowed(M,M.location(),CMSecurity.SecFlag.CMDSOCIALS))
				return "[authentication error]";

			boolean create=false;
			List<Social> SV=CMLib.socials().getSocialsSet(last);
			List<Social> OSV=null;
			if(SV==null)
				create=true;
			else
				OSV=new XVector<Social>(SV);
			SV=new Vector<Social>();

			String old=httpReq.getUrlParameter("TITLE");
			if((old!=null)
			&&(old.trim().length()>0)
			&&(!old.equalsIgnoreCase(last)))
			{
				old=CMStrings.replaceAll(old.toUpperCase()," ","_").trim();
				if(CMLib.socials().getSocialsSet(last)!=null)
					return "[new social name already exists]";
				last=old;
			}

			final Vector<String> TYPES=new Vector<String>();
			final Vector<String> EXTNS=new Vector<String>();
			for (final String element : BTYPES)
				TYPES.addElement(element);
			for (final String element : BEXTNS)
				EXTNS.addElement(element);
			old=httpReq.getUrlParameter("NUMXTRAS");
			if(old!=null)
			{
				final int numXtras=CMath.s_int(httpReq.getUrlParameter("NUMXTRAS"));
				for(int n=0;n<numXtras;n++)
				{
					old=httpReq.getUrlParameter("XSOCIAL"+n);
					if((old!=null)
					&&(old.length()>0)
					&&(httpReq.isUrlParameter("IS"+old.toUpperCase().trim()))
					&&(httpReq.getUrlParameter("IS"+old.toUpperCase().trim()).equalsIgnoreCase("on")))
					{
						TYPES.addElement(old.toUpperCase().trim());
						EXTNS.addElement(" "+old.toUpperCase().trim());
					}
				}
			}

			for(int t=0;t<TYPES.size();t++)
			{
				final String TYPE=TYPES.elementAt(t);
				final String EXTN=EXTNS.elementAt(t);

				old=httpReq.getUrlParameter("IS"+TYPE);
				if((old==null)||(!old.equalsIgnoreCase("on")))
					continue;

				final Social S=CMLib.socials().makeDefaultSocial(last,EXTN);
				final String field=(t<BTYPES.length)?BFIELDS[t]:BFIELDS[0];
				for(int f=0;f<field.length();f++)
				{
					final String fnam="SDAT_"+TYPE+"_"+field.charAt(f);
					old=httpReq.getUrlParameter(fnam);
					if(old!=null)
					{
						switch(field.charAt(f))
						{
						case 'Y':
							S.setYou_see(old);
							break;
						case 'O':
							S.setThird_party_sees(old);
							break;
						case 'N':
							S.setSee_when_no_target(old);
							break;
						case 'M':
							S.setMSPfile(old);
							break;
						case 'T':
							S.setTarget_sees(old);
							break;
						}
					}
					old=httpReq.getUrlParameter(fnam+"C");
					if(old!=null)
					{
						switch(field.charAt(f))
						{
						case 'Y':
							S.setSourceCode(CMath.s_int(old));
							break;
						case 'O':
							S.setTargetCode(CMath.s_int(old));
							S.setOthersCode(CMath.s_int(old));
							break;
						case 'N':
							break;
						case 'M':
							break;
						case 'T':
							break;
						}
					}
				}
				SV.add(S);
			}
			if(OSV!=null)
			{
				for(int s=0;s<OSV.size();s++)
					CMLib.socials().remove(OSV.get(s).Name());
			}

			for(int s=0;s<SV.size();s++)
				CMLib.socials().addSocial(SV.get(s));

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
			final MOB M = Authenticate.getAuthenticatedMob(httpReq);
			if(M==null)
				return "[authentication error]";
			if(!CMSecurity.isAllowed(M,M.location(),CMSecurity.SecFlag.CMDSOCIALS))
				return "[authentication error]";
			if(last==null)
				return " @break@";
			List<Social> SV=CMLib.socials().getSocialsSet(last);
			if(SV==null)
				return "Unknown social!";
			SV=new XVector<Social>(SV);
			for(int s=0;s<SV.size();s++)
				CMLib.socials().remove(SV.get(s).Name());
			CMLib.socials().save(M);
			Log.sysOut(M.name()+" deleted social "+last);
			return "Social deleted.";
		}
		else
		{
			if(last==null)
				return " @break@";
			if(last.length()>0)
			{
				final String newSocialID=httpReq.getUrlParameter("NEWSOCIAL");
				@SuppressWarnings("unchecked")
				List<Social> SV=(List<Social>)httpReq.getRequestObjects().get("SOCIAL-"+last);
				if((SV==null)
				&&(newSocialID!=null)
				&&(newSocialID.length()>0)
				&&(CMLib.socials().getSocialsSet(newSocialID)==null))
				{
					SV=new Vector<Social>();
					last=newSocialID;
					httpReq.addFakeUrlParameter("SOCIAL",newSocialID);
				}
				if(SV==null)
					SV=CMLib.socials().getSocialsSet(last);
				if(parms.containsKey("ISNEWSOCIAL"))
					return ""+(CMLib.socials().getSocialsSet(last)==null);
				if(SV!=null)
				{
					final StringBuffer str=new StringBuffer("");
					String old;

					if(parms.containsKey("TITLE"))
					{
						old=httpReq.getUrlParameter("TITLE");
						if(old==null)
							old=last;
						str.append(old+", ");
					}
					final Vector<String> TYPES=new Vector<String>();
					final Vector<String> EXTNS=new Vector<String>();
					for (final String element : BTYPES)
						TYPES.addElement(element);
					for (final String element : BEXTNS)
						EXTNS.addElement(element);
					old=httpReq.getUrlParameter("NUMXTRAS");
					if(old!=null)
					{
						final int numXtras=CMath.s_int(httpReq.getUrlParameter("NUMXTRAS"));
						for(int n=0;n<numXtras;n++)
						{
							old=httpReq.getUrlParameter("XSOCIAL"+n);
							if((old!=null)
							&&(old.length()>0)
							&&(httpReq.isUrlParameter("IS"+old.toUpperCase().trim()))
							&&(httpReq.getUrlParameter("IS"+old.toUpperCase().trim()).equalsIgnoreCase("on")))
							{
								TYPES.addElement(old.toUpperCase().trim());
								EXTNS.addElement(old.toUpperCase().trim());
							}
						}
					}
					else
					for(int s=0;s<SV.size();s++)
					{
						final Social S=SV.get(s);
						boolean found=false;
						for (final String element : BEXTNS)
						{
							if(S.Name().equalsIgnoreCase(last+element))
								found=true;
						}
						if(!found)
						{
							final int x=S.Name().indexOf(' ');
							if(x>0)
							{
								final String TYPE=S.Name().substring(x+1).trim().toUpperCase();
								TYPES.addElement(TYPE);
								EXTNS.addElement(" "+TYPE);
								httpReq.addFakeUrlParameter("IS"+TYPE,"on");
							}
						}
					}

					old=httpReq.getUrlParameter("DOADDXSOCIAL");
					if((old!=null)
					&&(old.equalsIgnoreCase("on"))
					&&(httpReq.getUrlParameter("ADDXSOCIAL")!=null)
					&&(httpReq.getUrlParameter("ADDXSOCIAL").trim().length()>0)
					&&(!TYPES.contains(httpReq.getUrlParameter("ADDXSOCIAL").toUpperCase().trim())))
					{
						final String TYPE=httpReq.getUrlParameter("ADDXSOCIAL").toUpperCase().trim();
						TYPES.addElement(TYPE);
						EXTNS.addElement(" "+TYPE);
						httpReq.addFakeUrlParameter("IS"+TYPE,"on");
					}

					final int numxtras=TYPES.size()-BTYPES.length;
					if(parms.containsKey("NUMEXTRAS"))
						str.append(""+numxtras+", ");
					if(parms.containsKey("GETEXTRA")
					&&(CMath.s_int(parms.get("GETEXTRA"))<numxtras))
						str.append(TYPES.elementAt(BTYPES.length+CMath.s_int(parms.get("GETEXTRA")))+", ");

					for(int t=0;t<TYPES.size();t++)
					{
						final String TYPE=TYPES.elementAt(t);
						final String EXTN=EXTNS.elementAt(t);
						Social S=null;
						for(int s=0;s<SV.size();s++)
						{
							if(SV.get(s).Name().equalsIgnoreCase(last+EXTN))
							{
								S=SV.get(s);
								break;
							}
						}
						if(parms.containsKey("IS"+TYPE))
						{
							old=httpReq.getUrlParameter("IS"+TYPE);
							if(old==null)
								old=(((S!=null)&&(!httpReq.isUrlParameter("NUMXTRAS")))?"on":"");
							str.append(""+old.equalsIgnoreCase("on")+", ");
							if(!old.equalsIgnoreCase("on"))
								continue;
						}
						final String field=(t<BTYPES.length)?BFIELDS[t]:BFIELDS[0];
						for(int f=0;f<field.length();f++)
						{
							final String fnam="SDAT_"+TYPE+"_"+field.charAt(f);
							if(parms.containsKey(fnam))
							{
								old=httpReq.getUrlParameter(fnam);
								if(old==null)
								{
									if(S==null)
										S=CMLib.socials().makeDefaultSocial(last,EXTN);
									switch(field.charAt(f))
									{
									case 'Y':
										old = S.You_see();
										break;
									case 'O':
										old = S.Third_party_sees();
										break;
									case 'N':
										old = S.See_when_no_target();
										break;
									case 'M':
										old = S.MSPfile();
										break;
									case 'T':
										old = S.Target_sees();
										break;
									}
								}
								str.append(old+", ");
							}
							if(parms.containsKey(fnam+"C"))
							{
								old=httpReq.getUrlParameter(fnam+"C");
								if(old==null)
								{
									if(S==null)
										S=CMLib.socials().makeDefaultSocial(last,EXTN);
									switch(field.charAt(f))
									{
									case 'Y':
										old = (S == null) ? null : "" + S.sourceCode();
										break;
									case 'O':
										old = (S == null) ? null : "" + S.targetCode();
										break;
									case 'N':
										break;
									case 'M':
										break;
									case 'T':
										break;
									}
								}
								if(old!=null)
								{
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
