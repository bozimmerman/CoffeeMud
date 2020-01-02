package com.planet_ink.coffee_mud.Libraries;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import java.net.NetworkInterface;

import com.planet_ink.coffee_mud.Libraries.interfaces.TextEncoders;
import com.planet_ink.coffee_mud.core.B64Encoder;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.CMProps;
import com.planet_ink.coffee_mud.core.CMath;
import com.planet_ink.coffee_mud.core.Log;

/*
   Copyright 2005-2020 Bo Zimmerman

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
public class CMEncoder extends StdLibrary implements TextEncoders
{
	@Override
	public String ID()
	{
		return "CMEncoder";
	}

	private byte[]			encodeBuffer	= new byte[65536];
	private final Deflater	compresser		= new Deflater(Deflater.BEST_COMPRESSION);
	private final Inflater	decompresser	= new Inflater();
	protected static byte[]	encrFilter		= null;

	public CMEncoder()
	{
		super();
	}

	@Override
	public synchronized String decompressString(final byte[] b)
	{
		try
		{
			if ((b == null)||(b.length==0)) return "";

			decompresser.reset();
			decompresser.setInput(b);

			synchronized (encodeBuffer)
			{
				final int len = decompresser.inflate(encodeBuffer);
				return new String(encodeBuffer, 0, len, CMProps.getVar(CMProps.Str.CHARSETINPUT));
			}
		}
		catch (final Exception ex)
		{
			Log.errOut(Thread.currentThread().getName(), "Error occurred during decompression: "+ex.getMessage());
			encodeBuffer=new byte[65536];
			return "";
		}
	}

	@Override
	public synchronized byte[] compressString(final String s)
	{
		byte[] result = null;

		try
		{
			compresser.reset();
			compresser.setInput(s.getBytes(CMProps.getVar(CMProps.Str.CHARSETINPUT)));
			compresser.finish();

			synchronized (encodeBuffer)
			{
				if(s.length()>encodeBuffer.length)
					encodeBuffer=new byte[s.length()];
				encodeBuffer[0]=0;

				final int len = compresser.deflate(encodeBuffer);
				result = new byte[len];
				System.arraycopy(encodeBuffer, 0, result, 0, len);
			}
		}
		catch (final Exception ex)
		{
			Log.errOut("MUD", "Error occurred during compression: "+ex.getMessage());
			encodeBuffer=new byte[65536];
		}

		return result;
	}

	@Override
	public String makeRandomHashString(final String password)
	{
		final int salt=(int)Math.round(CMath.random() * Integer.MAX_VALUE);
		final int passHash=(password+salt).toLowerCase().hashCode();
		return "|"+B64Encoder.B64encodeBytes(ByteBuffer.allocate(4).putInt(salt).array())
			  +"|"+B64Encoder.B64encodeBytes(ByteBuffer.allocate(4).putInt(passHash).array());
	}

	@Override
	public String makeRepeatableHashString(final String str)
	{
		final int passHash=str.toLowerCase().hashCode();
		return "|"+B64Encoder.B64encodeBytes(enDeCrypt(ByteBuffer.allocate(4).putInt(passHash).array()));
	}

	@Override
	public boolean isARandomHashString(final String password)
	{
		return ((password.length()>2) && (password.startsWith("|")) && (password.indexOf('|',1)>1));
	}

	@Override
	public boolean checkPasswordAgainstRandomHashString(final String passwordString, final String hashString)
	{
		final int hashDex=hashString.indexOf('|',1);
		final int salt=ByteBuffer.wrap(B64Encoder.B64decode(hashString.substring(1,hashDex))).getInt();
		final int hash=ByteBuffer.wrap(B64Encoder.B64decode(hashString.substring(hashDex+1))).getInt();
		return hash==(passwordString+salt).toLowerCase().hashCode();
	}

	@Override
	public boolean checkHashStringPairs(final String hashString1, final String hashString2)
	{
		final int hashDex1=hashString1.indexOf('|',1);
		final int salt1=ByteBuffer.wrap(B64Encoder.B64decode(hashString1.substring(1,hashDex1))).getInt();
		final int hash1=ByteBuffer.wrap(B64Encoder.B64decode(hashString1.substring(hashDex1+1))).getInt();

		final int hashDex2=hashString2.indexOf('|',1);
		final int salt2=ByteBuffer.wrap(B64Encoder.B64decode(hashString2.substring(1,hashDex2))).getInt();
		final int hash2=ByteBuffer.wrap(B64Encoder.B64decode(hashString2.substring(hashDex2+1))).getInt();

		return (hash1==(hashString2+salt1).toLowerCase().hashCode()) || (hash2==(hashString1+salt2).toLowerCase().hashCode());
	}

	@Override
	public String generateRandomPassword()
	{
		final StringBuilder str=new StringBuilder("");
		for(int i=0;i<10;i++)
		{
			if((i%2)==0)
				str.append(CMLib.dice().roll(1, 10, -1));
			else
				str.append((char)('a'+CMLib.dice().roll(1, 26, -1)));
		}
		return str.toString();
	}

	@Override
	public boolean passwordCheck(final String pass1, final String pass2)
	{
		if(pass1 == null)
		{
			return (pass2==null);
		}
		else
		if(pass2 == null)
			return false;

		if(pass1.equalsIgnoreCase(pass2))
			return true;
		if(isARandomHashString(pass2))
		{
			if(isARandomHashString(pass1))
				return checkHashStringPairs(pass1,pass2);
			else
				return checkPasswordAgainstRandomHashString(pass1, pass2);
		}
		else
		{
			if(isARandomHashString(pass1))
				return checkPasswordAgainstRandomHashString(pass2, pass1);
			return pass1.equalsIgnoreCase(pass2);
		}
	}

	public static byte[] getFilter()
	{
		if(encrFilter==null)
		{
			// this is coffeemud's unsophisticated xor(mac address) encryption system.
			final byte[] filterc = new String("wrinkletellmetrueisthereanythingasnastyasyouwellmaybesothenumber7470issprettybad").getBytes();
			encrFilter=new byte[256];
			try
			{
				for(int i=0;i<256;i++)
					encrFilter[i]=filterc[i % filterc.length];
				final String domain=CMProps.getVar(CMProps.Str.MUDDOMAIN);
				if(domain.length()>0)
				{
					for(int i=0;i<256;i++)
						encrFilter[i]^=domain.charAt(i % domain.length());
				}
				final String name=CMProps.getVar(CMProps.Str.MUDNAME);
				if(name.length()>0)
				{
					for(int i=0;i<256;i++)
						encrFilter[i]^=name.charAt(i % name.length());
				}
				final String email=CMProps.getVar(CMProps.Str.ADMINEMAIL);
				if(email.length()>0)
				{
					for(int i=0;i<256;i++)
						encrFilter[i]^=email.charAt(i % email.length());
				}
				for(final Enumeration<NetworkInterface> nie = NetworkInterface.getNetworkInterfaces(); nie.hasMoreElements();)
				{
					final NetworkInterface ni = nie.nextElement();
					if(ni != null)
					{
						final byte[] mac = ni.getHardwareAddress();
						if((mac != null) && (mac.length > 0))
						{
							for(int i=0;i<256;i++)
								encrFilter[i]^=Math.abs(mac[i % mac.length]);
						}
					}
				}
			}
			catch(final Exception e)
			{
				e.printStackTrace();
			}
		}
		return encrFilter;
	}

	protected byte[] enDeCrypt(final byte[] bytes)
	{
		final byte[] encrFilter=getFilter();
		for ( int i = 0, j = 0; i < bytes.length; i++, j++ )
		{
			if ( j >= encrFilter.length )
				j = 0;
			bytes[i]=(byte)((bytes[i] ^ encrFilter[j]) & 0xff);
		}
		return bytes;
	}

	@Override
	public String filterEncrypt(final String str)
	{
		try
		{
			final byte[] buf=B64Encoder.B64encodeBytes(enDeCrypt(str.getBytes()),B64Encoder.DONT_BREAK_LINES).getBytes();
			final StringBuilder s=new StringBuilder("");
			for(final byte b : buf)
			{
				String s2=Integer.toHexString(b);
				while(s2.length()<2)
					s2="0"+s2;
				s.append(s2);
			}
			return s.toString();
		}
		catch(final Exception e)
		{
			return "";
		}
	}

	@Override
	public String filterDecrypt(final String str)
	{
		try
		{
			final byte[] buf=new byte[str.length()/2];
			for(int i=0;i<str.length();i+=2)
				buf[i/2]=(byte)(Integer.parseInt(str.substring(i,i+2),16) & 0xff);
			return new String(enDeCrypt(B64Encoder.B64decode(new String(buf))));
		}
		catch(final Exception e)
		{
			return "";
		}
	}
}
