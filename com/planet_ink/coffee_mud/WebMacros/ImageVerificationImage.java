package com.planet_ink.coffee_mud.WebMacros;

/**
  * Copyright (c) 2006 by Jeff Haynie
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  *
  * You may obtain a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
 */
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.imageio.ImageIO;

import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine;
import com.planet_ink.coffee_web.http.MIMEType;
import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PlayerData;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.B64Encoder;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.coffee_mud.core.Resources;
import com.planet_ink.coffee_mud.core.collections.Pair;
import com.planet_ink.coffee_mud.core.collections.SLinkedList;
import com.planet_ink.coffee_mud.core.exceptions.HTTPServerException;
/**
  * ImageVerification is a simple utility class for
  * creating an image verification PNG file that will
  * allow you to make sure that only a human can read
  * the alphanumeric values and enter them into a text
  * field during verification.
  *
  * Make sure that when you can getVerificationCode
  * you don't encode the value in the URL or inside the
  * HTML form - otherwise, this whole excerise is pointless
  * (dummy!).
  *
  * @author Jeff Haynie
  * Copyright (c) by Jeff Haynie. All Rights Reserved.
  */
public class ImageVerificationImage extends StdWebMacro
{
	private String value;
	public  static Object sync=new Object();
	private static Random rand=new Random();

	@Override
	public boolean isAWebPath()
	{
		return true;
	}

	@Override
	public boolean preferBinary()
	{
		return true;
	}
	
	public ImageVerificationImage (){}

	public static class ImgCacheEntry
	{
		public String key;
		public String value;
		public long createdTimeMillis=System.currentTimeMillis();
		public String ip;
	}

	@SuppressWarnings("unchecked")
	public static SLinkedList<ImgCacheEntry> getVerifyCache()
	{
		SLinkedList<ImgCacheEntry> verSet=(SLinkedList<ImgCacheEntry>)Resources.getResource("SYSTEM_WEB_IMGVER_CACHE");
		if(verSet==null)
		{
			verSet=new SLinkedList<ImgCacheEntry>();
			Resources.submitResource("SYSTEM_WEB_IMGVER_CACHE", verSet);
		}
		try
		{
			while(verSet.size()>1000)
				verSet.removeFirst();
			for(final Iterator<ImgCacheEntry> i=verSet.iterator();i.hasNext();)
			{
				final ImgCacheEntry I=i.next();
				if((System.currentTimeMillis()-I.createdTimeMillis)>(20 * 60 * 60 * 1000))
					i.remove();
			}
		}
		catch(final Exception e)
		{

		}
		return verSet;
	}

	public String getFilename(HTTPRequest httpReq, String filename)
	{
		final String foundFilename=httpReq.getUrlParameter("FILENAME");
		if((foundFilename!=null)&&(foundFilename.length()>0))
			return foundFilename;
		return filename;
	}

	@Override
	public byte[] runBinaryMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp) throws HTTPServerException
	{
		final ByteArrayOutputStream bout=new ByteArrayOutputStream();
		try
		{
			synchronized(sync)
			{
				final boolean imageRequest=httpReq.isUrlParameter("IMAGE");
				if(imageRequest)
				{
					final MIMEType mimeType = MIMEType.All.getMIMEType(getFilename(httpReq,""));
					if(mimeType != null)
						httpResp.setHeader("Content-Type", mimeType.getType());
				}
				final SLinkedList<ImgCacheEntry> cache = getVerifyCache();
				String value=null;
				String key=null;
				final String hisIp=httpReq.getClientAddress().getHostAddress();
				if(imageRequest)
				{
					for(final Iterator<ImageVerificationImage.ImgCacheEntry> p =cache.descendingIterator();p.hasNext();)
					{
						final ImageVerificationImage.ImgCacheEntry entry=p.next();
						if(entry.ip.equalsIgnoreCase(hisIp))
						{
							value=entry.value;
							key=entry.key;
							break;
						}
					}
				}
				final ImageVerificationImage  img=new ImageVerificationImage(value,bout);
				if(key==null)
					key=Long.toHexString(Math.round(Math.abs(rand.nextDouble() * (Long.MAX_VALUE/2.0))));
				if(value==null)
				{
					final ImgCacheEntry entry=new ImgCacheEntry();
					entry.ip=hisIp;
					entry.key=key;
					entry.value=img.getVerificationValue();
					cache.addLast(entry);
				}
				if(!imageRequest)
				{
					bout.reset();
					bout.write(key.getBytes());
				}
				httpReq.addFakeUrlParameter("IMGVERKEY", key);
			}
		}
		catch(final IOException ioe)
		{
			Log.errOut("ImgVerWM",ioe);
		}
		return bout.toByteArray();
	}

	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp) throws HTTPServerException
	{
		return "[Unimplemented string method!]";
	}

	public ImageVerificationImage (String oldValue, OutputStream out) throws IOException
	{
		this(34,120,oldValue,out);
	}

	public ImageVerificationImage (int height, int width, String oldValue, OutputStream out) throws IOException
	{
		final BufferedImage bimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		final Random rand=new Random(System.currentTimeMillis());
		final Graphics2D g = bimage.createGraphics();
		// create a random color
		final Color color = new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
		// the background to the random color to fill the
		// background and make it darker
		g.setColor(color.darker());
		g.fillRect(0, 0, width, height);
		// set the font
		g.setFont(new Font("arial",Font.BOLD,24));
		// generate a random value
		if(oldValue!=null)
			this.value = oldValue;
		else
			this.value = UUID.randomUUID().toString().replace("-","").substring(0,5);
		final int w = (g.getFontMetrics()).stringWidth(value);
		final int d = (g.getFontMetrics()).getDescent();
		final int a = (g.getFontMetrics()).getMaxAscent();
		int x = 0, y =0;
		// randomly set the color and draw some straight lines through it
		for (int i = 0; i < height; i += 5)
		{
			g.setColor(new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)));
			g.drawLine(x, y + i, width, y+i);
		}
		// reset x and y
		x=0;
		y=0;
		// randomly set the color of the lines and just draw think at an angle
		for (int i = 0; i < height; i += 5)
		{
			g.setColor(new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)));
			g.drawLine(x, y + d - i, width + w, height + d - i);
		}
		// randomly set the color and make it really bright for more readability
		g.setColor(new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)).brighter().brighter());
		// we need to position the text in the center of the box
		x = width/2 - w/2;
		y = height/2 + a/2 - 2;
		// affine transform is used to rock the text a bit
		final AffineTransform fontAT = new AffineTransform();
		int xp = x-2;
		// walk through each character and rotate it randomly
		for (int c=0;c<value.length();c++)
		{
			// apply a random radian either left or right (left is half since it's too far back)
			final int rotate = rand.nextInt(20);
			fontAT.rotate(rand.nextBoolean() ? Math.toRadians(rotate) : -Math.toRadians(rotate/2));
			final Font fx = new Font("arial", Font.BOLD, 24).deriveFont(fontAT);
			g.setFont(fx);
			final String ch = String.valueOf(value.charAt(c));
			final int ht = rand.nextInt(3);
			// draw the string and move the y either up or down slightly
			g.drawString(ch, xp, y + (rand.nextBoolean()?-ht:ht));
			// move our pointer
			xp+=g.getFontMetrics().stringWidth(ch) + 2;
		}
		// write out the PNG file
		ImageIO.write(bimage, "png", out);
		// make sure your clean up the graphics object
		g.dispose();
	}

	/**
	 * return the value to check for when the user enters it in. Make sure you
	 * store this off in the session or something like a database and NOT in the
	 * form of the webpage since the whole point of this exercise is to ensure that
	 * only humans and not machines are entering the data.
	 *
	 * @return the value to enter
	 */
	public String getVerificationValue ()
	{
		return this.value;
	}
}
