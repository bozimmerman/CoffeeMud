package com.planet_ink.coffee_mud.core;

/**
 * Encodes and decodes to and from Base64 notation.
 *
 * 
 * Change Log:
 * <ul>
 *  <li>v2.0.2 - Now specifies UTF-8 encoding in places where the code fails on systems
 *   with other encodings (like EBCDIC).</li>
 *  <li>v2.0.1 - Fixed an error when decoding a single byte, that is, when the
 *   encoded data was a single byte.</li>
 *  <li>v2.0 - I got rid of methods that used booleans to set options.
 *   Now everything is more consolidated and cleaner. The code now detects
 *   when data that's being decoded is gzip-compressed and will decompress it
 *   automatically. Generally things are cleaner. You'll probably have to
 *   change some method calls that you were making to support the new
 *   options format (<tt>int</tt>s that you "OR" together).</li>
 *  <li>v1.5.1 - Fixed bug when decompressing and decoding to a
 *   byte[] using <tt>decode( String s, boolean gzipCompressed )</tt>.
 *   Added the ability to "suspend" encoding in the Output Stream so
 *   you can turn on and off the encoding if you need to embed base64
 *   data in an otherwise "normal" stream (like an XML file).</li>
 *  <li>v1.5 - Output stream pases on flush() command but doesn't do anything itself.
 *  	This helps when using GZIP streams.
 *  	Added the ability to GZip-compress objects before encoding them.</li>
 *  <li>v1.4 - Added helper methods to read/write files.</li>
 *  <li>v1.3.6 - Fixed OutputStream.flush() so that 'position' is reset.</li>
 *  <li>v1.3.5 - Added flag to turn on and off line breaks. Fixed bug in input stream
 *  	where last buffer being read, if not completely full, was not returned.</li>
 *  <li>v1.3.4 - Fixed when "improperly padded stream" error was thrown at the wrong time.</li>
 *  <li>v1.3.3 - Fixed I/O streams which were totally messed up.</li>
 * </ul>
 *
 * 
 * I am placing this code in the Public Domain. Do with it as you will.
 * This software comes with no guarantees or warranties but with
 * plenty of well-wishing instead!
 * Please visit <a href="http://iharder.net/xmlizable">http://iharder.net/base64</a>
 * periodically to check for updates or to contribute improvements.
 *
 * @author Robert Harder
 * @author rob@iharder.net
 * @version 2.0
 */
public class B64Encoder
{
	private B64Encoder(){}
	/* Base 64 Encoding stuff */
	private static byte[] ALPHABET;

	public static final int NO_OPTIONS = 0;
	public static final int ENCODE = 1;
	public static final int DECODE = 0;
	public static final int GZIP = 2;
	public static final int DONT_BREAK_LINES = 8;
	public static final int MAX_LINE_LENGTH = 76;
	public static final byte EQUALS_SIGN = (byte)'=';
	public static final byte NEW_LINE = (byte)'\n';
	public static final String PREFERRED_ENCODING = "UTF-8";
	public static final byte[] _NATIVE_ALPHABET = /* May be something funny like EBCDIC */
	{
		(byte)'A', (byte)'B', (byte)'C', (byte)'D', (byte)'E', (byte)'F', (byte)'G',
		(byte)'H', (byte)'I', (byte)'J', (byte)'K', (byte)'L', (byte)'M', (byte)'N',
		(byte)'O', (byte)'P', (byte)'Q', (byte)'R', (byte)'S', (byte)'T', (byte)'U',
		(byte)'V', (byte)'W', (byte)'X', (byte)'Y', (byte)'Z',
		(byte)'a', (byte)'b', (byte)'c', (byte)'d', (byte)'e', (byte)'f', (byte)'g',
		(byte)'h', (byte)'i', (byte)'j', (byte)'k', (byte)'l', (byte)'m', (byte)'n',
		(byte)'o', (byte)'p', (byte)'q', (byte)'r', (byte)'s', (byte)'t', (byte)'u',
		(byte)'v', (byte)'w', (byte)'x', (byte)'y', (byte)'z',
		(byte)'0', (byte)'1', (byte)'2', (byte)'3', (byte)'4', (byte)'5',
		(byte)'6', (byte)'7', (byte)'8', (byte)'9', (byte)'+', (byte)'/'
	};
	public static final byte[] DECODABET =
	{
		-9,-9,-9,-9,-9,-9,-9,-9,-9, 				// Decimal  0 -  8
		-5,-5,  									// Whitespace: Tab and Linefeed
		-9,-9,  									// Decimal 11 - 12
		-5, 										// Whitespace: Carriage Return
		-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9, 	// Decimal 14 - 26
		-9,-9,-9,-9,-9, 							// Decimal 27 - 31
		-5, 										// Whitespace: Space
		-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,  			// Decimal 33 - 42
		62, 										// Plus sign at decimal 43
		-9,-9,-9,   								// Decimal 44 - 46
		63, 										// Slash at decimal 47
		52,53,54,55,56,57,58,59,60,61,  			// Numbers zero through nine
		-9,-9,-9,   								// Decimal 58 - 60
		-1, 										// Equals sign at decimal 61
		-9,-9,-9,   								   // Decimal 62 - 64
		0,1,2,3,4,5,6,7,8,9,10,11,12,13,			// Letters 'A' through 'N'
		14,15,16,17,18,19,20,21,22,23,24,25,		// Letters 'O' through 'Z'
		-9,-9,-9,-9,-9,-9,  						// Decimal 91 - 96
		26,27,28,29,30,31,32,33,34,35,36,37,38, 	// Letters 'a' through 'm'
		39,40,41,42,43,44,45,46,47,48,49,50,51, 	// Letters 'n' through 'z'
		-9,-9,-9,-9 								// Decimal 123 - 126
		/*,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 127 - 139
		-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9, 	// Decimal 140 - 152
		-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9, 	// Decimal 153 - 165
		-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9, 	// Decimal 166 - 178
		-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9, 	// Decimal 179 - 191
		-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9, 	// Decimal 192 - 204
		-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9, 	// Decimal 205 - 217
		-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9, 	// Decimal 218 - 230
		-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9, 	// Decimal 231 - 243
		-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9 		// Decimal 244 - 255 */
	};
	public static final byte WHITE_SPACE_ENC = -5; // Indicates white space in encoding
	public static final byte EQUALS_SIGN_ENC = -1; // Indicates equals sign in encoding

	protected static byte[] encode3to4( byte[] b4, byte[] threeBytes, int numSigBytes )
	{
		encode3to4( threeBytes, 0, numSigBytes, b4, 0 );
		return b4;
	}

	static
	{
		byte[] __bytes;
		try
		{
			__bytes = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".getBytes( PREFERRED_ENCODING );
		}   // end try
		catch (final java.io.UnsupportedEncodingException use)
		{
			__bytes = _NATIVE_ALPHABET; // Fall back to native encoding
		}   // end catch
		if(ALPHABET==null)
			ALPHABET = __bytes;
	}

	protected static byte[] encode3to4(byte[] source, int srcOffset, int numSigBytes,
									   byte[] destination, int destOffset )
	{
		final int inBuff =   ( numSigBytes > 0 ? ((source[ srcOffset	 ] << 24) >>>  8) : 0 )
					 | ( numSigBytes > 1 ? ((source[ srcOffset + 1 ] << 24) >>> 16) : 0 )
					 | ( numSigBytes > 2 ? ((source[ srcOffset + 2 ] << 24) >>> 24) : 0 );

		switch( numSigBytes )
		{
			case 3:
				destination[ destOffset 	] = ALPHABET[ (inBuff >>> 18)   	 ];
				destination[ destOffset + 1 ] = ALPHABET[ (inBuff >>> 12) & 0x3f ];
				destination[ destOffset + 2 ] = ALPHABET[ (inBuff >>>  6) & 0x3f ];
				destination[ destOffset + 3 ] = ALPHABET[ (inBuff   	) & 0x3f ];
				return destination;

			case 2:
				destination[ destOffset 	] = ALPHABET[ (inBuff >>> 18)   	 ];
				destination[ destOffset + 1 ] = ALPHABET[ (inBuff >>> 12) & 0x3f ];
				destination[ destOffset + 2 ] = ALPHABET[ (inBuff >>>  6) & 0x3f ];
				destination[ destOffset + 3 ] = EQUALS_SIGN;
				return destination;

			case 1:
				destination[ destOffset 	] = ALPHABET[ (inBuff >>> 18)   	 ];
				destination[ destOffset + 1 ] = ALPHABET[ (inBuff >>> 12) & 0x3f ];
				destination[ destOffset + 2 ] = EQUALS_SIGN;
				destination[ destOffset + 3 ] = EQUALS_SIGN;
				return destination;

			default:
				return destination;
		}
	}

	public static String B64encodeObject( java.io.Serializable serializableObject )
	{
		return B64encodeObject( serializableObject, NO_OPTIONS );
	}

	public static String B64encodeObject( java.io.Serializable serializableObject, int options )
	{
		final java.io.ByteArrayOutputStream  baos  = new java.io.ByteArrayOutputStream();
		java.io.OutputStream		   b64os = null;
		java.io.ObjectOutputStream     oos   = null;
		java.util.zip.GZIPOutputStream gzos  = null;

		final int gzip		   = (options & GZIP);
		final int dontBreakLines = (options & DONT_BREAK_LINES);

		try
		{
			b64os = new B64OutputStream( baos, ENCODE | dontBreakLines );

			if( gzip == GZIP )
			{
				gzos = new java.util.zip.GZIPOutputStream( b64os );
				oos  = new java.io.ObjectOutputStream( gzos );
			}
			else
				oos   = new java.io.ObjectOutputStream( b64os );

			oos.writeObject( serializableObject );
		}
		catch( final java.io.IOException e )
		{
			e.printStackTrace();
			return null;
		}
		finally
		{
			try{ if(oos!=null)oos.close();   } catch( final Exception e ){}
			try{ if(gzos!=null)gzos.close();  } catch( final Exception e ){}
			try{ if(b64os!=null)b64os.close(); } catch( final Exception e ){}
			try{ baos.close();  } catch( final Exception e ){}
		}

		try
		{
			return new String( baos.toByteArray(), PREFERRED_ENCODING );
		}
		catch (final java.io.UnsupportedEncodingException uue)
		{
			return new String( baos.toByteArray() );
		}
	}

	public static String B64encodeBytes( byte[] source )
	{
		return B64encodeBytes( source, 0, source.length, NO_OPTIONS );
	}   // end encodeBytes

	public static String B64encodeBytes( byte[] source, int options )
	{
		return B64encodeBytes( source, 0, source.length, options );
	}   // end encodeBytes

	public static String B64encodeBytes( byte[] source, int off, int len )
	{
		return B64encodeBytes( source, off, len, NO_OPTIONS );
	}   // end encodeBytes

	public static String B64encodeBytes( byte[] source, int off, int len, int options )
	{
		final int dontBreakLines = ( options & DONT_BREAK_LINES );
		final int gzip		   = ( options & GZIP   );

		if( gzip == GZIP )
		{
			final java.io.ByteArrayOutputStream  baos  = new java.io.ByteArrayOutputStream();
			java.util.zip.GZIPOutputStream gzos  = null;
			B64OutputStream 		b64os = null;

			try
			{
				b64os = new B64OutputStream( baos, ENCODE | dontBreakLines );
				gzos  = new java.util.zip.GZIPOutputStream( b64os );

				gzos.write( source, off, len );
				gzos.close();
			}
			catch( final java.io.IOException e )
			{
				e.printStackTrace();
				return null;
			}
			finally
			{
				try{ if(gzos!=null)gzos.close();  } catch( final Exception e ){}
				try{ if(b64os!=null)b64os.close(); } catch( final Exception e ){}
				try{ baos.close();  } catch( final Exception e ){}
			}
			try
			{
				return new String( baos.toByteArray(), PREFERRED_ENCODING );
			}
			catch (final java.io.UnsupportedEncodingException uue)
			{
				return new String( baos.toByteArray() );
			}
		}
		final boolean breakLines = dontBreakLines == 0;

		final int    len43   = len * 4 / 3;
		final byte[] outBuff = new byte[   ( len43 )  					// Main 4:3
								   + ( (len % 3) > 0 ? 4 : 0 )  	// Account for padding
								   + (breakLines ? ( len43 / MAX_LINE_LENGTH ) : 0) ]; // New lines
		int d = 0;
		int e = 0;
		final int len2 = len - 2;
		int lineLength = 0;
		for( ; d < len2; d+=3, e+=4 )
		{
			encode3to4( source, d+off, 3, outBuff, e );

			lineLength += 4;
			if( breakLines && lineLength == MAX_LINE_LENGTH )
			{
				outBuff[e+4] = NEW_LINE;
				e++;
				lineLength = 0;
			}
		}

		if( d < len )
		{
			encode3to4( source, d+off, len - d, outBuff, e );
			e += 4;
		}
		try
		{
			return new String( outBuff, 0, e, PREFERRED_ENCODING );
		}
		catch (final java.io.UnsupportedEncodingException uue)
		{
			return new String( outBuff, 0, e );
		}

	}

	protected static int decode4to3( byte[] source, int srcOffset, byte[] destination, int destOffset )
	{
		if( source[ srcOffset + 2] == EQUALS_SIGN )
		{
			final int outBuff =   ( ( DECODABET[ source[ srcOffset	] ] & 0xFF ) << 18 )
						  | ( ( DECODABET[ source[ srcOffset + 1] ] & 0xFF ) << 12 );

			destination[ destOffset ] = (byte)( outBuff >>> 16 );
			return 1;
		}

		else if( source[ srcOffset + 3 ] == EQUALS_SIGN )
		{
			final int outBuff =   ( ( DECODABET[ source[ srcOffset	 ] ] & 0xFF ) << 18 )
						  | ( ( DECODABET[ source[ srcOffset + 1 ] ] & 0xFF ) << 12 )
						  | ( ( DECODABET[ source[ srcOffset + 2 ] ] & 0xFF ) <<  6 );

			destination[ destOffset 	] = (byte)( outBuff >>> 16 );
			destination[ destOffset + 1 ] = (byte)( outBuff >>>  8 );
			return 2;
		}

		else
		{
			try
			{
			final int outBuff =   ( ( DECODABET[ source[ srcOffset	 ] ] & 0xFF ) << 18 )
						  | ( ( DECODABET[ source[ srcOffset + 1 ] ] & 0xFF ) << 12 )
						  | ( ( DECODABET[ source[ srcOffset + 2 ] ] & 0xFF ) <<  6)
						  | ( ( DECODABET[ source[ srcOffset + 3 ] ] & 0xFF )      );

			destination[ destOffset 	] = (byte)( outBuff >> 16 );
			destination[ destOffset + 1 ] = (byte)( outBuff >>  8 );
			destination[ destOffset + 2 ] = (byte)( outBuff 	  );

			return 3;
			}
			catch( final Exception e)
			{
				Log.errOut("CMEncoder",e);
				return -1;
			}
		}
	}

	public static byte[] B64decode( byte[] source, int off, int len )
	{
		final int    len34   = len * 3 / 4;
		final byte[] outBuff = new byte[ len34 ]; // Upper limit on size of output
		int    outBuffPosn = 0;

		final byte[] b4   	 = new byte[4];
		int    b4Posn    = 0;
		int    i		 = 0;
		byte   sbiCrop   = 0;
		byte   sbiDecode = 0;
		for( i = off; i < off+len; i++ )
		{
			sbiCrop = (byte)(source[i] & 0x7f); // Only the low seven bits
			sbiDecode = DECODABET[ sbiCrop ];

			if( sbiDecode >= WHITE_SPACE_ENC ) // White space, Equals sign or better
			{
				if( sbiDecode >= EQUALS_SIGN_ENC )
				{
					b4[ b4Posn++ ] = sbiCrop;
					if( b4Posn > 3 )
					{
						outBuffPosn += decode4to3( b4, 0, outBuff, outBuffPosn );
						b4Posn = 0;

						if( sbiCrop == EQUALS_SIGN )
							break;
					}
				}
			}
			else
			{
				System.err.println( "Bad Base64 input character at " + i + ": " + source[i] + "(decimal)" );
				return null;
			}
		}

		final byte[] out = new byte[ outBuffPosn ];
		System.arraycopy( outBuff, 0, out, 0, outBuffPosn );
		return out;
	}

	public static byte[] B64decode( String s )
	{
		byte[] bytes;
		try
		{
			bytes = s.getBytes( PREFERRED_ENCODING );
		}
		catch( final java.io.UnsupportedEncodingException uee )
		{
			bytes = s.getBytes();
		}
		bytes = B64decode( bytes, 0, bytes.length );
		if( bytes != null && bytes.length >= 4 )
		{

			final int head = (bytes[0] & 0xff) | ((bytes[1] << 8) & 0xff00);
			if( java.util.zip.GZIPInputStream.GZIP_MAGIC == head )
			{
				java.io.ByteArrayInputStream  bais = null;
				java.util.zip.GZIPInputStream gzis = null;
				java.io.ByteArrayOutputStream baos = null;
				final byte[] buffer = new byte[2048];
				int    length = 0;

				try
				{
					baos = new java.io.ByteArrayOutputStream();
					bais = new java.io.ByteArrayInputStream( bytes );
					gzis = new java.util.zip.GZIPInputStream( bais );

					while( ( length = gzis.read( buffer ) ) >= 0 )
					{
						baos.write(buffer,0,length);
					}
					bytes = baos.toByteArray();

				}
				catch( final java.io.IOException e )
				{
				}
				finally
				{
					try{ if(baos!=null)baos.close(); } catch( final Exception e ){}
					try{ if(gzis!=null)gzis.close(); } catch( final Exception e ){}
					try{ if(bais!=null)bais.close(); } catch( final Exception e ){}
				}
			}
		}

		return bytes;
	}

	public static Object B64decodeToObject( String encodedObject )
	{
		final byte[] objBytes = B64decode( encodedObject );

		java.io.ByteArrayInputStream  bais = null;
		java.io.ObjectInputStream     ois  = null;
		Object obj = null;

		try
		{
			bais = new java.io.ByteArrayInputStream( objBytes );
			ois  = new java.io.ObjectInputStream( bais );

			obj = ois.readObject();
		}
		catch( final java.io.IOException e )
		{
			e.printStackTrace();
		}
		catch( final java.lang.ClassNotFoundException e )
		{
			e.printStackTrace();
		}
		finally
		{
			try{ if(bais!=null)bais.close(); } catch( final Exception e ){}
			try{ if(ois!=null)ois.close();  } catch( final Exception e ){}
		}

		return obj;
	}

	public static boolean B64encodeToFile( byte[] dataToEncode, String filename )
	{
		boolean success = false;
		B64OutputStream bos = null;
		try
		{
			bos = new B64OutputStream(
					  new java.io.FileOutputStream( filename ), ENCODE );
			bos.write( dataToEncode );
			success = true;
		}
		catch( final java.io.IOException e )
		{

			success = false;
		}
		finally
		{
			try{ if(bos!=null)bos.close(); } catch( final Exception e ){}
		}

		return success;
	}

	public static boolean B64decodeToFile( String dataToDecode, String filename )
	{
		boolean success = false;
		B64OutputStream bos = null;
		try
		{
				bos = new B64OutputStream(
						  new java.io.FileOutputStream( filename ), DECODE );
				bos.write( dataToDecode.getBytes( PREFERRED_ENCODING ) );
				success = true;
		}
		catch( final java.io.IOException e )
		{
			success = false;
		}
		finally
		{
				try{ if(bos!=null)bos.close(); } catch( final Exception e ){}
		}

		return success;
	}

	public static byte[] B64decodeFromFile( String filename )
	{
		byte[] decodedData = null;
		B64InputStream bis = null;
		try
		{
			final java.io.File file = new java.io.File( filename );
			byte[] buffer = null;
			int length   = 0;
			int numBytes = 0;

			if( file.length() > Integer.MAX_VALUE )
			{
				System.err.println( "File is too big for this convenience method (" + file.length() + " bytes)." );
				return null;
			}
			buffer = new byte[ (int)file.length() ];

			bis = new B64InputStream(
					  new java.io.BufferedInputStream(
					  new java.io.FileInputStream( file ) ), DECODE );

			while( ( numBytes = bis.read( buffer, length, 4096 ) ) >= 0 )
				length += numBytes;

			decodedData = new byte[ length ];
			System.arraycopy( buffer, 0, decodedData, 0, length );

		}
		catch( final java.io.IOException e )
		{
			System.err.println( "Error decoding from file " + filename );
		}
		finally
		{
			try{ if(bis!=null)bis.close(); } catch( final Exception e) {}
		}

		return decodedData;
	}

	public static String B64encodeFromFile( String filename )
	{
		String encodedData = null;
		B64InputStream bis = null;
		try
		{
			final java.io.File file = new java.io.File( filename );
			final byte[] buffer = new byte[ (int)(file.length() * 1.4) ];
			int length   = 0;
			int numBytes = 0;
			bis = new B64InputStream(
					  new java.io.BufferedInputStream(
					  new java.io.FileInputStream( file ) ), ENCODE );
			while( ( numBytes = bis.read( buffer, length, 4096 ) ) >= 0 )
				length += numBytes;
			encodedData = new String( buffer, 0, length, PREFERRED_ENCODING );

		}
		catch( final java.io.IOException e )
		{
			System.err.println( "Error encoding from file " + filename );
		}
		finally
		{
			try{ if(bis!=null)bis.close(); } catch( final Exception e) {}
		}

		return encodedData;
	}

	private static class B64InputStream extends java.io.FilterInputStream
	{
		private final boolean encode; 		// Encoding or decoding
		private int 	position;   	// Current position in the buffer
		private final byte[]  buffer; 		// Small buffer holding converted data
		private final int 	bufferLength;   // Length of buffer (3 or 4)
		private int 	numSigBytes;	// Number of meaningful bytes in the buffer
		private int 	lineLength;
		private final boolean breakLines; 	// Break lines at less than 80 characters

		public B64InputStream( java.io.InputStream in, int options )
		{
			super( in );
			this.breakLines   = (options & DONT_BREAK_LINES) != DONT_BREAK_LINES;
			this.encode 	  = (options & ENCODE) == ENCODE;
			this.bufferLength = encode ? 4 : 3;
			this.buffer   = new byte[ bufferLength ];
			this.position = -1;
			this.lineLength = 0;
		}

		@Override
		public int read() throws java.io.IOException
		{
			if( position < 0 )
			{
				if( encode )
				{
					final byte[] b3 = new byte[3];
					int numBinaryBytes = 0;
					for( int i = 0; i < 3; i++ )
					{
						try
						{
							final int b = in.read();

							if( b >= 0 )
							{
								b3[i] = (byte)b;
								numBinaryBytes++;
							}

						}
						catch( final java.io.IOException e )
						{
							if( i == 0 )
								throw e;

						}
					}

					if( numBinaryBytes > 0 )
					{
						encode3to4( b3, 0, numBinaryBytes, buffer, 0 );
						position = 0;
						numSigBytes = 4;
					}
					else
					{
						return -1;
					}
				}
				else
				{
					final byte[] b4 = new byte[4];
					int i = 0;
					for( i = 0; i < 4; i++ )
					{
						int b = 0;
						do{ b = in.read(); }
						while( b >= 0 && DECODABET[ b & 0x7f ] <= WHITE_SPACE_ENC )
							;

						if( b < 0 )
							break; // Reads a -1 if end of stream

						b4[i] = (byte)b;
					}

					if( i == 4 )
					{
						numSigBytes = decode4to3( b4, 0, buffer, 0 );
						position = 0;
					}
					else if( i == 0 )
					{
						return -1;
					}
					else
					{
						throw new java.io.IOException( "Improperly padded Base64 input." );
					}

				}
			}
			if( position >= 0 )
			{
				if( position >= numSigBytes )
					return -1;

				if( encode && breakLines && lineLength >= MAX_LINE_LENGTH )
				{
					lineLength = 0;
					return '\n';
				}
				lineLength++;
				final int b = buffer[ position++ ];
				if( position >= bufferLength )
					position = -1;
				return b & 0xFF;
			}
			throw new java.io.IOException( "Error in Base64 code reading stream." );
		}

		@Override
		public int read( byte[] dest, int off, int len ) throws java.io.IOException
		{
			int i;
			int b;
			for( i = 0; i < len; i++ )
			{
				b = read();

				if( b >= 0 )
					dest[off + i] = (byte)b;
				else if( i == 0 )
					return -1;
				else
					break;
			}
			return i;
		}
	}

	private static class B64OutputStream extends java.io.FilterOutputStream
	{
		private final boolean encode;
		private int 	position;
		private byte[]  buffer;
		private final int 	bufferLength;
		private int 	lineLength;
		private final boolean breakLines;
		private final byte[]  b4; // Scratch used in a few places
		private final boolean suspendEncoding;

		public B64OutputStream( java.io.OutputStream out, int options )
		{
			super( out );
			this.breakLines   = (options & DONT_BREAK_LINES) != DONT_BREAK_LINES;
			this.encode 	  = (options & ENCODE) == ENCODE;
			this.bufferLength = encode ? 3 : 4;
			this.buffer 	  = new byte[ bufferLength ];
			this.position     = 0;
			this.lineLength   = 0;
			this.suspendEncoding = false;
			this.b4 		  = new byte[4];
		}

		@Override
		public void write(int theByte) throws java.io.IOException
		{
			if( suspendEncoding )
			{
				super.out.write( theByte );
				return;
			}
			if( encode )
			{
				buffer[ position++ ] = (byte)theByte;
				if( position >= bufferLength )
				{
					out.write( encode3to4( b4, buffer, bufferLength ) );

					lineLength += 4;
					if( breakLines && lineLength >= MAX_LINE_LENGTH )
					{
						out.write( NEW_LINE );
						lineLength = 0;
					}
					position = 0;
				}
			}
			else
			{
				if( DECODABET[ theByte & 0x7f ] > WHITE_SPACE_ENC )
				{
					buffer[ position++ ] = (byte)theByte;
					if( position >= bufferLength )
					{
						final int len = decode4to3( buffer, 0, b4, 0 );
						out.write( b4, 0, len );
						position = 0;
					}
				}
				else if( DECODABET[ theByte & 0x7f ] != WHITE_SPACE_ENC )
				{
					throw new java.io.IOException( "Invalid character in Base64 data." );
				}
			}
		}

		@Override
		public void write( byte[] theBytes, int off, int len ) throws java.io.IOException
		{
			if( suspendEncoding )
			{
				super.out.write( theBytes, off, len );
				return;
			}
			for( int i = 0; i < len; i++ )
			{
				write( theBytes[ off + i ] );
			}
		}

		public void flushBase64() throws java.io.IOException
		{
			if( position > 0 )
			{
				if( encode )
				{
					out.write( encode3to4( b4, buffer, position ) );
					position = 0;
				}
				else
				{
					throw new java.io.IOException( "Base64 input not properly padded." );
				}
			}
		}

		@Override
		public void close() throws java.io.IOException
		{
			flushBase64();
			super.close();

			buffer = null;
			out    = null;
		}
	}
}
