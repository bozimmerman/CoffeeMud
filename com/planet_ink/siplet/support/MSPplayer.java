package com.planet_ink.siplet.support;

import com.planet_ink.siplet.applet.*;
import java.applet.*;
import java.util.*;
import java.net.*;

/*
   Copyright 2000-2018 Bo Zimmerman

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
public class MSPplayer extends Thread
{
	public String		key				= null;
	public int			volume			= 100;
	public int			repeats			= 1;
	public int			iterations		= 0;
	public int			priority		= 50;
	public int			continueValue	= 1;
	public AudioClip	clip			= null;
	public String		url				= null;
	public boolean		playing			= false;
	public boolean		orderedStopped	= false;
	public String		tag				= "soundplayer";
	private Object		applet			= null;

	public MSPplayer(Object theApplet)
	{
		super();
		applet = theApplet;
	}

	public String stopPlaying(String playerName, boolean useExternal)
	{
		if (playing)
		{
			orderedStopped = true;
			if (useExternal)
				return "StopSound('" + key + "','" + playerName + "');\n\r";
			if (clip != null)
				clip.stop();
			try
			{
				Thread.sleep(50);
			}
			catch (final Exception e)
			{
			}
			if (playing)
				interrupt();
		}
		return "";
	}

	@Override
	public void run()
	{
		playing = true;
		orderedStopped = false;
		try
		{
			if ((clip != null) && (applet instanceof Applet))
			{
				if (url == null)
					clip = ((Applet) applet).getAudioClip(((Applet) applet).getCodeBase(), key);
				else
					clip = ((Applet) applet).getAudioClip(new URL(url + key));
			}
		}
		catch (final MalformedURLException m)
		{
			clip = null;
			playing = false;
			return;
		}
		if (clip != null)
		{
			// dunno how to set volume, but that should go here.
			while ((!orderedStopped) && (iterations < repeats))
			{
				iterations++;
				clip.play();
			}
		}
		playing = false;
	}

	public String startPlaying(String playerName, boolean useExternal)
	{
		if (useExternal)
		{
			this.run();
			return "PlaySound('" + key + "','" + playerName + "','" + url + "'," + repeats + "," + volume + "," + priority + ");\n\r";
		}
		else
		{
			this.start();
			return "";
		}
	}

}
