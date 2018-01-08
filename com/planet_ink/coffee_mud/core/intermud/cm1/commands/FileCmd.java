package com.planet_ink.coffee_mud.core.intermud.cm1.commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.intermud.cm1.RequestHandler;
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
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.io.*;
import java.util.concurrent.atomic.*;

/*
   Copyright 2014-2018 Bo Zimmerman

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
public class FileCmd extends CM1Command
{
	@Override 
	public String getCommandWord()
	{ 
		return "FILE";
	}

	public FileCmd(RequestHandler req, String parameters)
	{
		super(req, parameters);
	}

	@Override
	public void run()
	{
		try
		{
			final String firstWord=CMLib.english().getFirstWord(parameters).toUpperCase().trim();
			if(firstWord.equals("LENGTH"))
			{
				final String filename= CMStrings.trimQuotes(parameters.substring(7).trim());
				final CMFile f = new CMFile(filename,req.getUser());
				if(!f.exists())
					req.sendMsg("[FAIL FILE NOT FOUND "+filename+"]");
				else
					req.sendMsg("[OK "+f.length()+"]");
			}
			else
			if(firstWord.equals("AUTHOR"))
			{
				final String filename= CMStrings.trimQuotes(parameters.substring(7).trim());
				final CMFile f = new CMFile(filename,req.getUser());
				if(!f.exists())
					req.sendMsg("[FAIL FILE NOT FOUND "+filename+"]");
				else
					req.sendMsg("[OK "+f.author()+"]");
			}
			else
			if(firstWord.equals("LASTMODIFIED"))
			{
				final String filename= CMStrings.trimQuotes(parameters.substring(13).trim());
				final CMFile f = new CMFile(filename,req.getUser());
				if(!f.exists())
					req.sendMsg("[FAIL FILE NOT FOUND "+filename+"]");
				else
					req.sendMsg("[OK "+CMLib.time().date2String(f.lastModified())+"]");
			}
			else
			if(firstWord.equals("DELETE"))
			{
				final String filename= CMStrings.trimQuotes(parameters.substring(7).trim());
				final CMFile f = new CMFile(filename,req.getUser());
				if(!f.exists())
					req.sendMsg("[FAIL FILE NOT FOUND "+filename+"]");
				else
				if((!f.canWrite())||(!f.delete()))
					req.sendMsg("[FAIL UNAUTHORIZED "+filename+"]");
				else
					req.sendMsg("[OK "+CMLib.time().date2String(f.lastModified())+"]");
			}
			else
			if(firstWord.equals("READ"))
			{
				final String filename= CMStrings.trimQuotes(parameters.substring(5).trim());
				final CMFile f = new CMFile(filename,req.getUser());
				if(!f.exists())
					req.sendMsg("[FAIL FILE NOT FOUND "+filename+"]");
				else
				if(f.isDirectory() && f.canRead())
				{
					final StringBuilder str=new StringBuilder("");
					for(final CMFile f2 : f.listFiles())
						str.append(f2.getName()).append(f2.isDirectory()?"/ ":" ");
					req.sendMsg("[OK "+str.toString().trim()+"]");
				}
				else
				if(f.canRead())
				{
					String eob="/BLOCK:"+Math.random();
					final String data=f.textUnformatted().toString();
					while(data.indexOf(eob)>=0)
						eob="/BLOCK:"+Math.random();
					req.sendMsg("[BLOCK "+eob+"]");
					req.sendMsg(data+eob);
				}
				else
					req.sendMsg("[FAIL UNAUTHORIZED "+filename+"]");
			}
			else
			if(firstWord.equals("READB64"))
			{
				final String filename= CMStrings.trimQuotes(parameters.substring(5).trim());
				final CMFile f = new CMFile(filename,req.getUser());
				if(!f.exists())
					req.sendMsg("[FAIL FILE NOT FOUND "+filename+"]");
				else
				if(f.isDirectory() && f.canRead())
				{
					final StringBuilder str=new StringBuilder("");
					for(final CMFile f2 : f.listFiles())
						str.append(f2.getName()).append(f2.isDirectory()?"/ ":" ");
					req.sendMsg("[OK "+str.toString().trim()+"]");
				}
				else
				if(f.canRead())
				{
					String eob="/BLOCK:"+Math.random();
					final String data=B64Encoder.B64encodeBytes(f.raw());
					while(data.indexOf(eob)>=0)
						eob="/BLOCK:"+Math.random();
					req.sendMsg("[BLOCK "+eob+"]");
					req.sendMsg(data+eob);
				}
				else
					req.sendMsg("[FAIL UNAUTHORIZED "+filename+"]");
			}
			else
			if(firstWord.equals("WRITE"))
			{
				String rest=parameters.substring(6);
				while(rest.startsWith(" "))
					rest=rest.substring(1);
				CMFile f;
				if(rest.startsWith("\""))
				{
					final int x=rest.indexOf("\" ",1);
					if(x<0)
						f=null;
					else
					{
						f=new CMFile(rest.substring(1,x),req.getUser());
						rest=rest.substring(x+2);
					}
				}
				else
				{
					final int x=rest.indexOf(' ');
					if(x<0)
						f=null;
					else
					{
						f=new CMFile(rest.substring(1,x),req.getUser());
						rest=rest.substring(x+1);
					}
				}
				if((f==null)||((f.exists()&&f.isDirectory())||(!f.canWrite())))
					req.sendMsg("[FAIL BAD FILENAME]");
				else
				{
					if(f.saveText(rest))
						req.sendMsg("[OK]");
					else
						req.sendMsg("[FAIL UNABLE TO WRITE]");
				}
			}
			else
			if(firstWord.equals("WRITEB64"))
			{
				String rest=parameters.substring(9);
				while(rest.startsWith(" "))
					rest=rest.substring(1);
				CMFile f;
				if(rest.startsWith("\""))
				{
					final int x=rest.indexOf("\" ",1);
					if(x<0)
						f=null;
					else
					{
						f=new CMFile(rest.substring(1,x),req.getUser());
						rest=rest.substring(x+2);
					}
				}
				else
				{
					final int x=rest.indexOf(' ');
					if(x<0)
						f=null;
					else
					{
						f=new CMFile(rest.substring(1,x),req.getUser());
						rest=rest.substring(x+1);
					}
				}
				if((f==null)||((f.exists()&&f.isDirectory())||(!f.canWrite())))
					req.sendMsg("[FAIL BAD FILENAME]");
				else
				{
					final byte[] binData=B64Encoder.B64decode(rest);
					if(f.saveRaw(binData))
						req.sendMsg("[OK]");
					else
						req.sendMsg("[FAIL UNABLE TO WRITE]");
				}
			}
			else
				req.sendMsg("[FAIL "+getHelp(req.getUser(), null, null)+"]");
		}
		catch(final java.io.IOException ioe)
		{
			Log.errOut(className,ioe);
			req.close();
		}
	}

	@Override
	public boolean passesSecurityCheck(MOB user, PhysicalAgent target)
	{
		if(user == null)
			return false;
		return CMSecurity.hasAccessibleDir(user,null);
	}

	@Override
	public String getHelp(MOB user, PhysicalAgent target, String rest)
	{
		final String word=CMLib.english().getFirstWord(rest==null?"":rest).toUpperCase().trim();
		if (word.equals("READ"))
			return "USAGE: "+getCommandWord()+" READ \"<FILENAME>\": returns contents of the file or directory as a block.";
		else
		if (word.equals("DELETE"))
			return "USAGE: "+getCommandWord()+" DELETE \"<FILENAME>\": deletes the file or directory (if empty).";
		else
		if (word.equals("WRITE"))
			return "USAGE: "+getCommandWord()+" WRITE \"<FILENAME>\" <CONTENT>: creates or overwrites file.";
		else
		if (word.equals("READB64"))
			return "USAGE: "+getCommandWord()+" READB64 \"<FILENAME>\": returns contents of the file as a block of b64 encoded data.";
		else
		if (word.equals("WRITEB64"))
			return "USAGE: "+getCommandWord()+" WRITEB64 \"<FILENAME>\" <CONTENT>: creates or overwrites file with b64 encoded data.";
		else
		if (word.equals("LENGTH"))
			return "USAGE: "+getCommandWord()+" LENGTH \"<FILENAME>\": returns length of the file.";
		else
		if (word.equals("AUTHOR"))
			return "USAGE: "+getCommandWord()+" AUTHOR \"<FILENAME>\": returns author of the file.";
		else
		if (word.equals("LASTMODIFIED"))
			return "USAGE: "+getCommandWord()+" LASTMODIFIED \"<FILENAME>\": returns last modified date/time of the file.";
		else
			return "USAGE: "+getCommandWord()+" READ, READB64, WRITE, WRITEB64, LENGTH, AUTHOR, LASTMODIFIED";
	}
}
