package com.planet_ink.coffee_mud.WebMacros;
import com.planet_ink.coffee_mud.WebMacros.interfaces.*;
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

import com.planet_ink.coffee_mud.core.exceptions.*;

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
public class StdWebMacro implements WebMacro
{
	public String ID()		{return name();}
	public String name()	{return "UNKNOWN";}

    public boolean isAWebPath(){return false;}
    public boolean preferBinary(){return false;}
	public boolean isAdminMacro()	{return false;}
    public CMObject newInstance(){return this;}
    public void initializeClass(){}
    public CMObject copyOf(){return this;}
    public String getSpecialContentHeader(String filename){return null;}
	
    public byte[] runBinaryMacro(ExternalHTTPRequests httpReq, String parm) throws HTTPServerException
    {
        return runMacro(httpReq,parm).getBytes();
    }
	public String runMacro(ExternalHTTPRequests httpReq, String parm) throws HTTPServerException
	{
		return "[Unimplemented macro!]";
	}
	
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
	
    public String getFilename(ExternalHTTPRequests httpReq, String filename)
    {
        return filename;
    }
    
    protected StringBuffer colorwebifyOnly(StringBuffer s)
    {
        if(s==null) return null;
        int i=0;
        String[] lookup=CMLib.color().standardHTMLlookups();
        while(i<s.length())
        {
        	switch(s.charAt(i))
	        {
            case '^':
                if(i<(s.length()-1))
                {
                    char c=s.charAt(i+1);
                    String code=lookup[c];
                    if(code!=null)
                    {
                        s.delete(i,i+2);
                        if(code.startsWith("<"))
                        {
	                        s.insert(i,code+">");
	                        i+=code.length();
                        }
                        else
                        {
                            s.insert(i,code);
                            i+=code.length()-1;
                        }
                    }
                }
                break;
	        }
        	i++;
        }
        return s;
    }
    
    protected StringBuffer webify(StringBuffer s)
    {
        if(s==null) return null;
        int i=0;
        while(i<s.length())
        {
            switch(s.charAt(i))
            {
            case '\n':
            case '\r':
                if((i<s.length()-1)
                &&(s.charAt(i+1)!=s.charAt(i))
                &&((s.charAt(i+1)=='\r')||(s.charAt(i+1)=='\n')))
                {
                	s.delete(i,i+2);
                	s.insert(i,"<BR>");
                    i+=3;
                }
                else
                {
                	s.delete(i,i+1);
                	s.insert(i,"<BR>");
                    i+=3;
                }
                break;
            case ' ':
                s.setCharAt(i,'&');
                s.insert(i+1,"nbsp;");
                i+=5;
                break;
            case '>':
                s.setCharAt(i,'&');
                s.insert(i+1,"gt;");
                i+=3;
                break;
            case '<':
                s.setCharAt(i,'&');
                s.insert(i+1,"lt;");
                i+=3;
                break;
            }
            i++;
        }
        s=colorwebifyOnly(s);
        return s;
    }
    
    protected String clearWebMacros(String s)
    {
        if(s.length()==0) return "";
        return clearWebMacros(new StringBuffer(s));
    }
    
    private String parseFoundMacro(StringBuffer s, int i, boolean lookOnly)
    {
        String foundMacro=null;
        boolean extend=false;
        for(int x=i+1;x<s.length();x++)
        {
            if((s.charAt(x)=='@')
            &&(extend)
            &&(x<(s.length()-1))
            &&(s.charAt(x+1)=='@'))
            {
                if(!lookOnly)
                    s.deleteCharAt(x);
                while((x<s.length())&&(s.charAt(x)=='@'))
                    x++;
                x--;
            }
            else
            if((s.charAt(x)=='@')
            &&((!extend)||(x>=s.length()-1)||(s.charAt(x+1)!='@')))
            {
                foundMacro=s.substring(i+1,x);
                break;
            }
            else
            if((s.charAt(x)=='?')&&(Character.isLetterOrDigit(s.charAt(x-1))))
                extend=true;
            else
            if(((x-i)>CMClass.longestWebMacro)&&(!extend))
                break;
        }
        return foundMacro;
    }
    protected String clearWebMacros(StringBuffer s)
    {
        if(s.length()==0) return "";
        for(int i=0;i<s.length();i++)
        {
            if(s.charAt(i)=='@')
            {
                String foundMacro=parseFoundMacro(s,i,false);
                if((foundMacro!=null)&&(foundMacro.length()>0))
                {
                    if(foundMacro.equalsIgnoreCase("break"))
                        i+=(foundMacro.length()+2);
                    else
                    if((foundMacro.startsWith("if?"))
                    ||(foundMacro.equalsIgnoreCase("else"))
                    ||(foundMacro.equalsIgnoreCase("loop"))
                    ||(foundMacro.equalsIgnoreCase("back"))
                    ||(foundMacro.equalsIgnoreCase("endif"))
                    ||(foundMacro.equalsIgnoreCase("/jscript"))
                    ||(foundMacro.equalsIgnoreCase("jscript")))
                        s.replace(i,i+foundMacro.length()+2,foundMacro);
                    else
                    {
                        int x=foundMacro.indexOf("?");
                        int len=foundMacro.length();
                        if(x>=0) foundMacro=foundMacro.substring(0,x);
                        WebMacro W=CMClass.getWebMacro(foundMacro.toUpperCase());
                        if(W!=null) s.replace(i,i+len+2,foundMacro);
                    }
                }
            }
        }
        return s.toString();
    }
    
	protected StringBuilder helpHelp(StringBuilder s){return helpHelp(s,70);}
	protected StringBuilder helpHelp(StringBuilder s, int limit)
	{
		if(s!=null)
		{
            String[] lookup=CMLib.color().standardHTMLlookups();
			s=new StringBuilder(s.toString());
			int x=s.toString().indexOf("\n\r");
			while(x>=0){	s.replace(x,x+2,"<BR>"); x=s.toString().indexOf("\n\r");}
			x=s.toString().indexOf("\r\n");
			while(x>=0){	s.replace(x,x+2,"<BR>"); x=s.toString().indexOf("\r\n");}
			
			int count=0;
			x=0;
			int lastSpace=0;
			while((x>=0)&&(x<s.length()))
			{
				count++;
                switch(s.charAt(x))
                {
                case ' ':
                    lastSpace=x;
                    break;
                case '<':
                   if((x<=s.length()-4)
                   &&(s.substring(x,x+4).equalsIgnoreCase("<BR>")))
                   {
                        count=0;
                        x=x+3;
                        lastSpace=x+4;
                   }
                   else
                   {
	                   s.setCharAt(x,'&');
	                   s.insert(x+1,"lt;");
                   }
                   break;
                case '-':
                    if((x>4)
                    &&(s.charAt(x-1)=='-')
                    &&(s.charAt(x-2)=='-')
                    &&(s.charAt(x-3)=='-'))
                    {
                        count=0;
                        lastSpace=x;
                    }
                    break;
                case '!':
                    if((x>4)
                    &&(s.charAt(x-1)==' ')
                    &&(s.charAt(x-2)==' ')
                    &&(s.charAt(x-3)==' '))
                    {
                        count=0;
                        lastSpace=x;
                    }
                    break;
                case '^':
                    if(x<(s.length()-1))
                    {
                        char c=s.charAt(x+1);
                        String code=lookup[c];
                        if(code!=null)
                        {
                            s.delete(x,x+2);
                            if(code.startsWith("<"))
                            {
	                            s.insert(x,code+">");
	                            x+=code.length();
                            }
                            else
                            {
	                            s.insert(x,code);
	                            x+=code.length()-1;
                            }
                        }
                    }
                    break;
                }
				if(count==limit)
				{
					s.replace(lastSpace,lastSpace+1,"<BR>");
					lastSpace=lastSpace+4;
					x=lastSpace+4;
					count=0;
				}
				else
					x++;
			}
			return s;
		}
		return new StringBuilder("");
	}
	
	protected DVector parseOrderedParms(String parm)
	{
		DVector requestParms=new DVector(2);
		if((parm!=null)&&(parm.length()>0))
		{
			while(parm.length()>0)
			{
				int x=parm.indexOf("&");
				String req=null;
				if(x>=0)
				{
					req=parm.substring(0,x);
					parm=parm.substring(x+1);
				}
				else
				{
					req=parm;
					parm="";
				}
				if(req!=null)
				{
					x=req.indexOf("=");
					if(x>=0)
						requestParms.addElement(req.substring(0,x).trim().toUpperCase(),req.substring(x+1).trim());
					else
						requestParms.addElement(req.trim().toUpperCase(),req.trim());
				}
			}
		}
		return requestParms;
	}
	
	protected String htmlIncomingFilter(String buf){return htmlIncomingFilter(new StringBuffer(buf)).toString();}
	protected StringBuffer htmlIncomingFilter(StringBuffer buf)
	{
		int loop=0;

		while(buf.length()>loop)
		{
			if((buf.charAt(loop)=='&')
			&&(loop<buf.length()-3))
			{
				int endloop=loop+1;
				while((endloop<buf.length())&&(endloop<loop+10)&&(buf.charAt(endloop)!=';'))
					endloop++;
				if(endloop<buf.length())
				{
					String s=buf.substring(loop,endloop+1);
					if(s.equalsIgnoreCase("&gt;"))
					{
						buf.setCharAt(loop,'>');
						buf.delete(loop+1,endloop+1);
					}
					else
					if(s.equalsIgnoreCase("&lt;"))
					{
						buf.setCharAt(loop,'<');
						buf.delete(loop+1,endloop+1);
					}
					else
					if(s.equalsIgnoreCase("&amp;"))
					{
						buf.setCharAt(loop,'&');
						buf.delete(loop+1,endloop+1);
					}
					else
					if(s.equalsIgnoreCase("&quot;"))
					{
						buf.setCharAt(loop,'\"');
						buf.delete(loop+1,endloop+1);
					}
				}
			}
			loop++;
		}
		return buf;
	}
	protected String htmlOutgoingFilter(String buf){return htmlOutgoingFilter(new StringBuffer(buf)).toString();}
	protected StringBuffer htmlOutgoingFilter(StringBuffer buf)
	{
		int loop=0;

		while(buf.length()>loop)
		{
			switch(buf.charAt(loop))
			{
			case '>':
				buf.delete(loop,loop+1);
				buf.insert(loop,"&gt;".toCharArray());
				loop+=3;
			    break;
			case '"':
				buf.delete(loop,loop+1);
				buf.insert(loop,"&quot;".toCharArray());
				loop+=5;
			    break;
			case '&':
				if((loop+3>=buf.length())
				||((!buf.substring(loop,loop+3).equalsIgnoreCase("lt;"))
					&&(!buf.substring(loop,loop+3).equalsIgnoreCase("amp;"))
					&&(!buf.substring(loop,loop+3).equalsIgnoreCase("quot;"))
					&&(!buf.substring(loop,loop+3).equalsIgnoreCase("gt;"))))
				{
					buf.delete(loop,loop+1);
					buf.insert(loop,"&amp;".toCharArray());
					loop+=4;
				}
				else
					loop++;
				break;
			case '<':
				buf.delete(loop,loop+1);
				buf.insert(loop,"&lt;".toCharArray());
				loop+=3;
			    break;
			default:
				loop++;
			}
		}
		return buf;
	}
	
	protected Hashtable parseParms(String parm)
	{
		Hashtable requestParms=new Hashtable();
		if((parm!=null)&&(parm.length()>0))
		{
			while(parm.length()>0)
			{
				int x=parm.indexOf("&");
				String req=null;
				if(x>=0)
				{
					req=parm.substring(0,x);
					parm=parm.substring(x+1);
				}
				else
				{
					req=parm;
					parm="";
				}
				if(req!=null)
				{
					x=req.indexOf("=");
					if(x>=0)
						requestParms.put(req.substring(0,x).trim().toUpperCase(),req.substring(x+1).trim());
					else
						requestParms.put(req.trim().toUpperCase(),req.trim());
				}
			}
		}
		return requestParms;
	}
}
