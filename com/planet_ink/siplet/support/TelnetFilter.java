package com.planet_ink.siplet.support;

import java.io.*;
import java.util.*;

import com.jcraft.jzlib.*;
import com.planet_ink.siplet.applet.Siplet;
import com.planet_ink.siplet.applet.Siplet.MSPStatus;
import com.planet_ink.siplet.support.MiniJSON.JSONObject;
import com.planet_ink.siplet.support.MiniJSON.MJSONException;

/*
   Copyright 2005-2018 Bo Zimmerman

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

public class TelnetFilter
{
	public final static boolean	debugChars				= false;
	public final static boolean	debugTelnetCodes		= false;

	protected static final char	IAC_SE					= 240;
	protected static final char	IAC_					= 255;
	protected static final char	IAC_SB					= 250;
	protected static final char	IAC_DO					= 253;
	protected static final char	IAC_WILL				= 251;
	protected static final char	IAC_WONT				= 252;
	protected static final char	IAC_DONT				= 254;
	protected static final char	IAC_MSDP				= 69;
	protected static final char	IAC_GMCP				= 201;
	protected static final char	IAC_MSP					= 90;
	protected static final char	IAC_MXP					= 91;
	protected static final char	IAC_GA					= 249;
	protected static final char	IAC_NOP					= 241;
	protected static final char	TELNET_ATCP				= 200;
	protected static final char	TELNET_GMCP				= 201;
	protected static final char	TELOPT_BINARY			= 0;
	protected static final char	TELOPT_EOR				= 25;
	protected static final char	TELOPT_ECHO				= 1;
	protected static final char	TELOPT_NAWS				= 31;
	protected static final char	TELOPT_LOGOUT			= 18;
	protected static final char	TELOPT_TTYPE			= 24;
	protected static final char	TELOPT_TSPEED			= 32;
	protected static final char	MCCP_COMPRESS			= 85;
	protected static final char	MCCP_COMPRESS2			= 86;
	protected static final char	TELOPT_NEWENVIRONMENT	= 39;

	private static String		defaultBackground		= "black";
	private static String		defaultForeground		= "white";
	private static String[]		colorCodes1				= { // 30-37
														"black", // black
			"#993300", // red
			"green", // green
			"#999966", // brown
			"#000099", // blue
			"purple", // purple
			"darkcyan", // cyan
			"lightgrey"								};							// grey
	private static String[]		colorCodes2				= { "gray", // dark grey
			"red", // light red
			"lightgreen", // light green
			"yellow", // yellow
			"blue", // light blue
			"violet", // light purple
			"cyan", // light cyan
			"white"									};							// white

	protected String			lastBackground			= null;
	protected String			lastForeground			= null;
	protected boolean			blinkOn					= false;
	protected boolean			fontOn					= false;
	protected boolean			boldOn					= false;
	protected boolean			underlineOn				= false;
	protected boolean			italicsOn				= false;
	private Siplet				codeBase				= null;
	protected boolean			comment					= false;

	protected MSPStatus			neverSupportMSP			= MSPStatus.Internal;
	protected boolean			neverSupportMXP			= false;
	protected boolean			neverSupportMSDP		= false;
	protected boolean			neverSupportGMCP		= false;
	protected boolean			neverSupportMCCP		= false;
	protected boolean			MSPsupport				= false;
	protected boolean			MSDPsupport				= false;
	protected boolean			GMCPsupport				= false;
	protected boolean			MXPsupport				= false;
	protected boolean			MCCPsupport				= false;
	private final StringBuilder	msdpInforms				= new StringBuilder("");
	private final StringBuilder	gmcpInforms				= new StringBuilder("");

	private final MSP			mspModule				= new MSP();
	private final MSDP			msdpModule				= new MSDP();
	private final GMCP			gmcpModule				= new GMCP();
	private final MXP			mxpModule				= new MXP();

	private TelnetFilter()
	{
	}

	public TelnetFilter(Siplet codebase)
	{
		this();
		codeBase = codebase;
	}

	public static String getSipletVersion()
	{
		return Siplet.VERSION_MAJOR + "." + Siplet.VERSION_MINOR;
	}

	public String getEnquedResponses()
	{
		return (MXPsupport() ? mxpModule.getAnyResponses() : "");
	}

	public String getEnquedJScript()
	{
		return ((mxpModule != null) ? mxpModule.getAnyJScript() : "") + ((mspModule != null) ? mspModule.getAnyJScript() : "");
	}

	public boolean MSPsupport()
	{
		return MSPsupport;
	}

	public void setMSPSupport(boolean truefalse)
	{
		MSPsupport = truefalse;
	}

	public boolean MXPsupport()
	{
		return MXPsupport;
	}

	public void setMXPSupport(boolean truefalse)
	{
		MXPsupport = truefalse;
	}

	public boolean MCCPsupport()
	{
		return MCCPsupport;
	}

	public void setMCCPSupport(boolean truefalse)
	{
		MCCPsupport = truefalse;
	}

	public boolean MSDPsupport()
	{
		return MSDPsupport;
	}

	public void setMSDPSupport(boolean truefalse)
	{
		MSDPsupport = truefalse;
	}

	public boolean GMCPsupport()
	{
		return GMCPsupport;
	}

	public void setGMCPSupport(boolean truefalse)
	{
		GMCPsupport = truefalse;
	}

	public void setNeverMXPSupport(boolean truefalse)
	{
		neverSupportMXP = truefalse;
	}

	public void setNeverMSPSupport(MSPStatus status)
	{
		neverSupportMSP = status;
	}

	public void setNeverMCCPSupport(boolean truefalse)
	{
		neverSupportMCCP = truefalse;
	}

	public void setNeverMSDPSupport(boolean truefalse)
	{
		neverSupportMSDP = truefalse;
	}

	public void setNeverGMCPSupport(boolean truefalse)
	{
		neverSupportGMCP = truefalse;
	}

	public boolean isUIonHold()
	{
		return MXPsupport() && mxpModule.isUIonHold();
	}

	private String blinkOff()
	{
		if (blinkOn)
		{
			blinkOn = false;
			return "</BLINK>";
		}
		return "";
	}

	private String underlineOff()
	{
		if (underlineOn)
		{
			underlineOn = false;
			return "</U>";
		}
		return "";
	}

	private String fontOff()
	{
		if (fontOn)
		{
			setLastBackground(defaultBackground);
			setLastForeground(defaultForeground);
			fontOn = false;
			return "</FONT>";
		}
		return "";
	}

	private String italicsOff()
	{
		if (italicsOn)
		{
			italicsOn = false;
			return "</I>";
		}
		return "";
	}

	private String allOff()
	{
		final StringBuffer off = new StringBuffer("");
		off.append(blinkOff());
		off.append(underlineOff());
		off.append(fontOff());
		off.append(italicsOff());
		return off.toString();
	}

	public String getMsdpHtml()
	{
		synchronized (msdpInforms)
		{
			if (msdpInforms.length() == 0)
				return "";
			final String bah = msdpInforms.toString();
			msdpInforms.setLength(0);
			return "<BR><PRE>" + bah + "</PRE><BR>";
		}
	}

	public String getGmcpHtml()
	{
		synchronized (gmcpInforms)
		{
			if (gmcpInforms.length() == 0)
				return "";
			final String bah = gmcpInforms.toString();
			gmcpInforms.setLength(0);
			return "<BR><PRE>" + bah + "</PRE>";
		}
	}

	public static int getColorCodeIndex(String word)
	{
		if (word == null)
			word = defaultForeground;
		for (int i = 0; i < colorCodes1.length; i++)
		{
			if (word.equalsIgnoreCase(colorCodes1[i]))
				return (40 + i);
		}
		for (int i = 0; i < colorCodes2.length; i++)
		{
			if (word.equalsIgnoreCase(colorCodes2[i]))
				return (30 + i);
		}
		return 30;
	}

	public static int getRelativeColorCodeIndex(String word)
	{
		final int x = getColorCodeIndex(word);
		if (x < 40)
			return x - 30;
		if (x > 50)
			return x % 10;
		return x - 40;
	}

	private void setLastBackground(String val)
	{
		if (MXPsupport())
			mxpModule.lastBackground = val;
		else
			lastBackground = val;
	}

	private void setLastForeground(String val)
	{
		if (MXPsupport())
			mxpModule.lastForeground = val;
		else
			lastForeground = val;
	}

	private String lastBackground()
	{
		return MXPsupport() ? mxpModule.lastBackground : lastBackground;
	}

	private String lastForeground()
	{
		return MXPsupport() ? mxpModule.lastForeground : lastForeground;
	}

	private String escapeTranslate(String escapeString)
	{
		if (escapeString.endsWith("m"))
		{
			final Vector<String> V = Util.parseSemicolons(escapeString.substring(0, escapeString.length() - 1), true);
			final StringBuffer str = new StringBuffer("");
			String s = null;
			int code = 0;
			String background = null;
			String foreground = null;
			for (int i = 0; i < V.size(); i++)
			{
				s = V.elementAt(i);
				code = Util.s0_int(s);
				switch (code)
				{
				case 0:
					if (i == (V.size() - 1))
						str.append(allOff());
					boldOn = false;
					break;
				case 1:
					boldOn = true;
					if ((V.size() == 1) && (lastForeground() != null))
						foreground = colorCodes2[getRelativeColorCodeIndex(lastForeground())];
					break;
				case 4:
				{
					if (!underlineOn)
					{
						underlineOn = true;
						str.append("<U>");
					}
					break;
				}
				case 5:
				{
					if (!blinkOn)
					{
						blinkOn = true;
						str.append("<BLINK>");
					}
					break;
				}
				case 6:
				{
					if (!italicsOn)
					{
						italicsOn = true;
						str.append("<I>");
					}
					break;
				}
				case 7:
				{
					// this is reverse on, and requires a wierd color reversal
					// from whatever the previous colors were.
					// do it later
					break;
				}
				case 8:
				{
					background = defaultBackground;
					foreground = defaultBackground;
					break;
				}
				case 22:
					str.append(allOff());
					break;
				case 24:
					str.append(underlineOff());
					break;
				case 25:
					str.append(blinkOff());
					break;
				case 26:
					str.append(italicsOff());
					break;
				case 30:
				case 31:
				case 32:
				case 33:
				case 34:
				case 35:
				case 36:
				case 37:
					foreground = boldOn ? colorCodes2[code - 30] : colorCodes1[code - 30];
					break;
				case 39:
					foreground = defaultForeground;
					break;
				case 40:
				case 41:
				case 42:
				case 43:
				case 44:
				case 45:
				case 46:
				case 47:
					background = colorCodes1[code - 40];
					break;
				case 49:
					background = defaultForeground;
					break;
				}
				if ((background != null) || (foreground != null))
				{
					if (lastBackground() == null)
						setLastBackground(defaultBackground);
					if (lastForeground() == null)
						setLastForeground(defaultForeground);
					if (background == null)
						background = lastBackground();
					if (foreground == null)
						foreground = lastForeground();

					if ((!lastBackground().equals(background)) || (!lastForeground().equals(foreground)))
					{
						str.append(fontOff());
						setLastBackground(background);
						setLastForeground(foreground);
						fontOn = true;
						if (MXPsupport())
							str.append("<FONT COLOR=" + foreground + " BACK=" + background + ">");
						else
							str.append("<FONT STYLE=\"color: " + foreground + ";background-color: " + background + "\">");
					}
				}
			}
			return str.toString();
		}
		return escapeString;
	}

	public static final char[]	mccppattern	= { IAC_, IAC_SB, MCCP_COMPRESS2, IAC_, IAC_SE };
	public int					patDex		= 0;

	public void TelnetRead(StringBuffer buf, InputStream rawin, BufferedReader in[]) throws InterruptedIOException, IOException
	{
		final char c = (char) in[0].read();
		if (mccppattern[patDex] == c)
		{
			patDex++;
			if ((patDex >= mccppattern.length) && (!neverSupportMCCP))
			{
				while (rawin.available() > 0)
					rawin.read();
				final ZInputStream zIn = new ZInputStream(rawin);
				if (debugTelnetCodes)
					System.out.println("MCCP compression started");
				in[0] = new BufferedReader(new InputStreamReader(zIn));
				patDex = 0;
			}
			return;
		}
		else 
		if (patDex > 0)
		{
			for (int i = 0; i < patDex; i++)
				buf.append(mccppattern[i]);
			patDex = 0;
		}
		buf.append(c);
		if (c == 65535)
			throw new java.io.InterruptedIOException("ARGH!");
	}

	public int TelenetFilter(StringBuffer buf, DataOutputStream response, InputStream rawin, BufferedReader[] in) throws IOException
	{
		int i = 0;
		while (i < buf.length())
		{
			switch (buf.charAt(i))
			{
			case 0:
				buf.delete(i, i + 1);
				break;
			case IAC_:
			{
				if (debugTelnetCodes)
					System.out.println("Got IAC in " + i + "/" + buf.length());
				if (i >= buf.length() - 2)
					return i;
				if (debugTelnetCodes)
					System.out.println("Receiving " + (int) buf.charAt(i + 1));
				final int oldI = i;
				int end = oldI + 3;
				switch (buf.charAt(++i))
				{
				case IAC_SB:
				{
					final ByteArrayOutputStream subOptionData = new ByteArrayOutputStream();
					final int subOptionCode = buf.charAt(++i);
					if (debugTelnetCodes)
						System.out.println("Got sub-option " + subOptionCode);
					int last = 0;
					while ((i < (buf.length() - 1)) && ((last = buf.charAt(++i)) != -1))
					{
						if ((last == IAC_) && (i < (buf.length() - 1)))
						{
							last = buf.charAt(++i);
							if (last == IAC_)
							{
								subOptionData.write(IAC_); // this is iac iac --
															// escape, dup type
															// thing?
							}
							else 
							if (last == IAC_SE)
								break;
						}
						else
							subOptionData.write((char) last);
					}
					end = i + 1;
					if (debugTelnetCodes)
						System.out.println("Got SB " + subOptionCode);
					if (subOptionCode == TELOPT_TTYPE)
					{
						if (debugTelnetCodes)
							System.out.println("Responding with termtype.");
						final byte[] data = new byte[6];
						data[0] = (short) 's';
						data[1] = (short) 'i';
						data[2] = (short) 'p';
						data[3] = (short) 'l';
						data[4] = (short) 'e';
						data[5] = (short) 't';
						response.writeBytes("" + IAC_ + IAC_SB + TELOPT_TTYPE + (char) 0);
						response.write(data);
						response.writeBytes("" + IAC_ + IAC_SE);
						response.flush();
					}
					else 
					if (subOptionCode == TELOPT_NAWS)
					{
						if (debugTelnetCodes)
							System.out.println("Responding with screen size.");
						final byte[] data = new byte[4];
						data[1] = 80;
						data[3] = 25;
						response.writeBytes("" + IAC_ + IAC_SB + TELOPT_NAWS);
						response.write(data);
						response.writeBytes("" + IAC_ + IAC_SE);
						response.flush();
					}
					else
					if (subOptionCode == MCCP_COMPRESS2)
					{
						// probably need to handle this earlier
					}
					else 
					if (subOptionCode == IAC_MSDP)
					{
						final String received = this.msdpModule.msdpReceive(subOptionData.toByteArray());
						synchronized (msdpInforms)
						{
							msdpInforms.append(received);
						}
						if (debugTelnetCodes)
							System.out.println("Got MSDP: " + received);
					}
					else 
					if (subOptionCode == IAC_GMCP)
					{
						final String received = this.gmcpModule.gmcpReceive(subOptionData.toByteArray());
						synchronized (gmcpInforms)
						{
							gmcpInforms.append(received + "\n");
						}
						if (debugTelnetCodes)
							System.out.println("Got GMCP: " + received);
					}
					break;
				}
				case IAC_WILL:
					i++;
					if (buf.charAt(i) == TELOPT_NAWS)
					{
						if (debugTelnetCodes)
							System.out.println("Responding with screen size to WILL NAWS.");
						final byte[] data = new byte[4];
						data[1] = 80;
						data[3] = 25;
						response.writeBytes("" + IAC_ + IAC_SB + TELOPT_NAWS);
						response.write(data);
						response.writeBytes("" + IAC_ + IAC_SE);
						break;
					}
					else 
					if (buf.charAt(i) == IAC_MSP)
					{
						if (debugTelnetCodes)
							System.out.println("Got WILL MSP!");
						if (neverSupportMSP == MSPStatus.Disabled)
						{
							if (MSPsupport())
							{
								if (debugTelnetCodes)
									System.out.println("Sent DONT MSP!");
								response.writeBytes("" + IAC_ + IAC_DONT + IAC_MSP);
								response.flush();
								setMSPSupport(false);
							}
						}
						else 
						if (!MSPsupport())
						{
							if (debugTelnetCodes)
								System.out.println("Sent DO MSP!");
							response.writeBytes("" + IAC_ + IAC_DO + IAC_MSP);
							response.flush();
							setMSPSupport(true);
						}
					}
					else 
					if (buf.charAt(i) == IAC_MSDP)
					{
						if (debugTelnetCodes)
							System.out.println("Got WILL MSDP!");
						if (neverSupportMSDP)
						{
							if (MSDPsupport())
							{
								if (debugTelnetCodes)
									System.out.println("Sent DONT MSDP!");
								response.writeBytes("" + IAC_ + IAC_DONT + IAC_MSDP);
								response.flush();
								setMSDPSupport(false);
							}
						}
						else 
						if (!MSDPsupport())
						{
							if (debugTelnetCodes)
								System.out.println("Sent DO MSDP!");
							response.writeBytes("" + IAC_ + IAC_DO + IAC_MSDP);
							response.flush();
							setMSDPSupport(true);
						}
					}
					else 
					if (buf.charAt(i) == IAC_GMCP)
					{
						if (debugTelnetCodes)
							System.out.println("Got WILL GMCP!");
						if (neverSupportGMCP)
						{
							if (GMCPsupport())
							{
								if (debugTelnetCodes)
									System.out.println("Sent DONT GMCP!");
								response.writeBytes("" + IAC_ + IAC_DONT + IAC_GMCP);
								response.flush();
								setGMCPSupport(false);
							}
						}
						else 
						if (!GMCPsupport())
						{
							if (debugTelnetCodes)
								System.out.println("Sent DO GMCP!");
							response.writeBytes("" + IAC_ + IAC_DO + IAC_GMCP);
							response.flush();
							setGMCPSupport(true);
							response.writeBytes("" + IAC_ + IAC_SB + IAC_GMCP);
							response.writeBytes("core.hello {\"client\":\"siplet\",\"version\":" + Siplet.VERSION_MAJOR + "}");
							response.writeBytes("" + IAC_ + IAC_SE);
						}
					}
					else 
					if (buf.charAt(i) == IAC_MXP)
					{
						if (debugTelnetCodes)
							System.out.println("Got WILL MXP!");
						if (neverSupportMXP)
						{
							if (MXPsupport())
							{
								if (debugTelnetCodes)
									System.out.println("Send DONT MXP!");
								response.writeBytes("" + IAC_ + IAC_DONT + IAC_MXP);
								response.flush();
								setMXPSupport(false);
							}
						}
						else 
						if (!MXPsupport())
						{
							if (debugTelnetCodes)
								System.out.println("Send DO MXP!");
							response.writeBytes("" + IAC_ + IAC_DO + IAC_MXP);
							response.flush();
							setMXPSupport(true);
						}
					}
					else 
					if (buf.charAt(i) == MCCP_COMPRESS2)
					{
						if (debugTelnetCodes)
							System.out.println("Got WILL COMPRESS2!");
						if (neverSupportMCCP)
						{
							if (MCCPsupport())
							{
								if (debugTelnetCodes)
									System.out.println("Send DONT COMPRESS2!");
								response.writeBytes("" + IAC_ + IAC_DONT + MCCP_COMPRESS2);
								response.flush();
								setMXPSupport(false);
							}
						}
						else 
						if (!MCCPsupport())
						{
							if (debugTelnetCodes)
								System.out.println("Send DO MCCP!");
							response.writeBytes("" + IAC_ + IAC_DO + MCCP_COMPRESS2);
							response.flush();
							setMCCPSupport(true);
						}
					}
					else 
					if (buf.charAt(i) == TELOPT_LOGOUT)
					{
						// goot for you serverdude
					}
					else 
					if (buf.charAt(i) != TELOPT_BINARY)
					{
						if (debugTelnetCodes)
							System.out.println("Sent DONT " + ((int) buf.charAt(i)) + "!");
						response.writeBytes("" + IAC_ + IAC_DONT + buf.charAt(i));
						response.flush();
					}
					break;
				case IAC_WONT:
					i++;
					if (buf.charAt(i) == IAC_MSP)
					{
						if (debugTelnetCodes)
							System.out.println("Got WONT MSP!");
						if (MSPsupport())
						{
							if (debugTelnetCodes)
								System.out.println("Sent DONT MSP!");
							response.writeBytes("" + IAC_ + IAC_DONT + IAC_MSP);
							response.flush();
							setMSPSupport(false);
						}
					}
					else 
					if (buf.charAt(i) == IAC_MSDP)
					{
						if (debugTelnetCodes)
							System.out.println("Got WONT MSDP!");
						if (MSDPsupport())
						{
							if (debugTelnetCodes)
								System.out.println("Sent DONT MSDP!");
							response.writeBytes("" + IAC_ + IAC_DONT + IAC_MSDP);
							response.flush();
							setMSDPSupport(false);
						}
					}
					else 
					if (buf.charAt(i) == IAC_GMCP)
					{
						if (debugTelnetCodes)
							System.out.println("Got WONT GMCP!");
						if (GMCPsupport())
						{
							if (debugTelnetCodes)
								System.out.println("Sent DONT GMCP!");
							response.writeBytes("" + IAC_ + IAC_DONT + IAC_GMCP);
							response.flush();
							setGMCPSupport(false);
						}
					}
					else 
					if (buf.charAt(i) == IAC_MXP)
					{
						if (debugTelnetCodes)
							System.out.println("Got WONT MXP!");
						if (MXPsupport())
						{
							if (debugTelnetCodes)
								System.out.println("Sent DONT MXP!");
							response.writeBytes("" + IAC_ + IAC_DONT + IAC_MXP);
							response.flush();
							setMXPSupport(false);
							if (mxpModule != null)
								mxpModule.shutdownMXP();
						}
					}
					break;
				case IAC_DO:
					i++;
					if (buf.charAt(i) == IAC_MSP)
					{
						if (debugTelnetCodes)
							System.out.println("Got DO MSP!");
						if (neverSupportMSP == MSPStatus.Disabled)
						{
							if (MSPsupport())
							{
								if (debugTelnetCodes)
									System.out.println("Sent WONT MSP!");
								response.writeBytes("" + IAC_ + IAC_WONT + IAC_MSP);
								response.flush();
								setMSPSupport(false);
							}
						}
						else 
						if (!MSPsupport())
						{
							if (debugTelnetCodes)
								System.out.println("Sent WILL MSP!");
							response.writeBytes("" + IAC_ + IAC_WILL + IAC_MSP);
							response.flush();
							setMSPSupport(true);
						}
					}
					else 
					if (buf.charAt(i) == IAC_GMCP)
					{
						if (debugTelnetCodes)
							System.out.println("Got DO GMCP!");
						if (neverSupportGMCP)
						{
							if (GMCPsupport())
							{
								if (debugTelnetCodes)
									System.out.println("Sent WONT GMCP!");
								response.writeBytes("" + IAC_ + IAC_WONT + IAC_GMCP);
								response.flush();
								setGMCPSupport(false);
							}
						}
						else 
						if (!GMCPsupport())
						{
							if (debugTelnetCodes)
								System.out.println("Sent WILL GMCP!");
							response.writeBytes("" + IAC_ + IAC_WILL + IAC_GMCP);
							response.flush();
							setGMCPSupport(true);
						}
					}
					else 
					if (buf.charAt(i) == TELOPT_LOGOUT)
					{
						// good for you serverdude
					}
					else 
					if (buf.charAt(i) == IAC_MXP)
					{
						if (debugTelnetCodes)
							System.out.println("Got DO MXP!");
						if (neverSupportMXP)
						{
							if (MXPsupport())
							{
								if (debugTelnetCodes)
									System.out.println("Sent WONT MXP!");
								response.writeBytes("" + IAC_ + IAC_WONT + IAC_MXP);
								response.flush();
								setMXPSupport(false);
							}
						}
						else 
						if (!MXPsupport())
						{
							response.writeBytes("" + IAC_ + IAC_WILL + IAC_MXP);
							if (debugTelnetCodes)
								System.out.println("Sent WILL MXP!");
							response.flush();
							setMXPSupport(true);
						}
					}
					else 
					if (buf.charAt(i) != TELOPT_BINARY)
					{
						if (debugTelnetCodes)
							System.out.println("Send WONT " + (int) buf.charAt(i) + "!");
						response.writeBytes("" + IAC_ + IAC_WONT + buf.charAt(i));
						response.flush();
					}
					break;
				case IAC_DONT:
					i++;
					if (buf.charAt(i) == IAC_MSP)
					{
						if (debugTelnetCodes)
							System.out.println("Got DONT MSP!");
						if (MSPsupport())
						{
							if (debugTelnetCodes)
								System.out.println("Sent WONT MSP!");
							response.writeBytes("" + IAC_ + IAC_WONT + IAC_MSP);
							response.flush();
							setMSPSupport(false);
						}
					}
					else 
					if (buf.charAt(i) == IAC_MXP)
					{
						if (MXPsupport())
						{
							if (debugTelnetCodes)
								System.out.println("Sent WONT MXP!");
							response.writeBytes("" + IAC_ + IAC_WONT + IAC_MXP);
							response.flush();
							setMXPSupport(false);
							if (mxpModule != null)
								mxpModule.shutdownMXP();
						}
					}
					break;
				case IAC_GA:
				case IAC_NOP:
					end = oldI + 2;
					break;
				}
				buf.delete(oldI, end);
				i = oldI - 1;
				break;
			}
			}
			i++;
		}
		return buf.length();
	}

	// filters out color codes -> <FONT>
	// CRS -> <BR>
	// SPACES -> &nbsp;
	// < -> &lt;
	// TELNET codes -> response outputstream
	public int HTMLFilter(StringBuffer buf)
	{
		int i = 0;
		final boolean[] eolEater = new boolean[1];
		while (i < buf.length())
		{
			if (debugChars)
				System.out.println(">" + buf.charAt(i));
			if (comment)
			{
				if (((i + 3) < buf.length()) && buf.substring(i, i + 3).equals("-->"))
				{
					comment = false;
					i += 3;
				}
			}
			else 
			if ((MXPsupport() && (mxpModule.eatTextUntilNextEOLN()) && (buf.charAt(i) != '\n') && (buf.charAt(i) != '\r')))
			{
				buf.deleteCharAt(i);
				i--;
			}
			else
				switch (buf.charAt(i))
				{
				case '!':
					if ((i < buf.length() - 3) && (buf.charAt(i + 1) == '!'))
					{
						if (MSPsupport())
						{
							final int endl = mspModule.process(buf, i, codeBase, neverSupportMSP == MSPStatus.External);
							if (endl == -1)
								i--;
							else 
							if (endl > 0)
								return endl;
						}
					}
					break;
				case '&':
				{
					if (!MXPsupport())
					{
						buf.insert(i + 1, "amp;");
						i += 4;
					}
					else
					{
						final int x = mxpModule.processEntity(buf, i, null, true);
						if (x == Integer.MAX_VALUE)
							return i;
						i += x;
					}
					break;
				}
				case ' ':
					buf.setCharAt(i, '&');
					buf.insert(i + 1, "nbsp;");
					i += 5;
					break;
				case '>':
					buf.setCharAt(i, '&');
					buf.insert(i + 1, "gt;");
					i += 3;
					break;
				case '<':
					if (!MXPsupport())
					{
						buf.setCharAt(i, '&');
						buf.insert(i + 1, "lt;");
						i += 3;
					}
					else 
					if (((i + 4) < buf.length()) && (buf.substring(i + 1, i + 4).equals("!--")))
						comment = true;
					else
					{
						final int x = mxpModule.processTag(buf, i);
						if (x == Integer.MAX_VALUE)
							return i;
						i += x;
					}
					break;
				case '\n':
				{
					if (MXPsupport())
					{
						final int x = mxpModule.newlineDetected(buf, i + 1, eolEater);
						if (eolEater[0])
							buf.deleteCharAt(i);
						else
						{
							buf.setCharAt(i, '<');
							buf.insert(i + 1, "BR>");
							i += 3;
						}
						i += x;
					}
					else
					{
						buf.setCharAt(i, '<');
						buf.insert(i + 1, "BR>");
						i += 3;
					}
					break;
				}
				case '\r':
					buf.deleteCharAt(i);
					i--;
					break;
				case IAC_:
				{
					if (i >= buf.length() - 3)
						return i;
					break;
				}
				case '\033':
				{
					final int savedI = i;
					if (i == buf.length() - 1)
						return i;
					if (buf.charAt(++i) != '[')
						buf.setCharAt(i, ' ');
					else
					{
						boolean quote = false;
						while (((++i) < buf.length()) && ((quote) || (!Character.isLetter(buf.charAt(i)))))
						{
							if (buf.charAt(i) == '"')
								quote = !quote;
						}
						if (i == buf.length())
							return savedI;
						final String oldStr = buf.substring(savedI + 2, i + 1);
						final String translate = escapeTranslate(oldStr);
						if (translate.equals(oldStr))
						{
							final int x = mxpModule.escapeTranslate(oldStr, buf, savedI);
							if (x == Integer.MAX_VALUE)
								return i;
							i = savedI + x;
						}
						else 
						if (!translate.equals(oldStr))
						{
							buf.replace(savedI, i + 1, translate);
							i = savedI + translate.length() - 1;
						}
					}
				}
					break;
				}
			i++;
		}
		return buf.length();
	}

	public final byte[] peruseInput(final String data)
	{
		if (data == null)
			return null;
		try
		{
			if (data.startsWith("\\"))
			{
				final int x = data.indexOf(' ');
				if (x < 0)
					return data.getBytes("UTF-8");
				final String cmd = data.substring(1, x).toUpperCase().trim();
				final String rest = data.substring(x + 1).trim();
				if (cmd.equalsIgnoreCase("MSDP"))
				{
					try
					{
						final byte[] newOutput = this.msdpModule.convertStringToMsdp(rest);
						if (newOutput != null)
							return newOutput;
					}
					catch (final MJSONException e)
					{
						if (debugTelnetCodes)
							System.out.println("JSON Parse Error: " + e.getMessage());
					}
					return null;
				}
				else 
				if (cmd.equalsIgnoreCase("GMCP"))
				{
					try
					{
						final byte[] newOutput = gmcpModule.convertStringToGmcp(rest);
						if (newOutput != null)
							return newOutput;
					}
					catch (final MJSONException e)
					{
						if (debugTelnetCodes)
							System.out.println("JSON Parse Error: " + e.getMessage());
					}
					return null;
				}
				else
					return data.getBytes("UTF-8");
			}
			return data.getBytes("UTF-8");
		}
		catch (final UnsupportedEncodingException e)
		{
			return data.getBytes();
		}
	}
}
