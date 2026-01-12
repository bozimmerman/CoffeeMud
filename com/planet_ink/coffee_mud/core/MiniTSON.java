package com.planet_ink.coffee_mud.core;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.planet_ink.coffee_mud.Locales.interfaces.Room;
import com.planet_ink.coffee_mud.core.MiniJSON.JSONObject;
import com.planet_ink.coffee_mud.core.MiniJSON.MJSONException;
import com.planet_ink.coffee_mud.core.collections.*;
import com.sun.tools.javac.code.Type;

/*
   Copyright 2026-2026 Bo Zimmerman

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
/**
 * A JSON parser, except that it supports type specifications to override normal
 * MiniJSON parser behavior.
 *
 * @author Bo Zimmerman
 *
 */
public final class MiniTSON extends MiniJSON
{
	/**
	 * Represents a type converter with both parsing and serialization
	 */
	public static class TSONTypeHandler
	{
		private final String prefix;
		private final Class<?>[] classC;
		@SuppressWarnings("rawtypes")
		private final Converter parseConverter;
		@SuppressWarnings("rawtypes")
		private final Converter serializeConverter;


		public TSONTypeHandler(final String prefix,
							   final Class<?>[] C,
							   @SuppressWarnings("rawtypes") final Converter parseConverter,
							   @SuppressWarnings("rawtypes") final Converter serializeConverter)
		{
			this.prefix = prefix;
			this.parseConverter = parseConverter;
			this.serializeConverter = serializeConverter;
			this.classC = C;

		}

		public static void create(final String prefix,
								  final Class<?>[] C,
								  @SuppressWarnings("rawtypes") final Converter parseConverter,
								  @SuppressWarnings("rawtypes") final Converter serializeConverter)
		{
			final TSONTypeHandler handler = new TSONTypeHandler(prefix, C, parseConverter, serializeConverter);
			prefixToHandler.put(prefix, handler);
			for(final Class<?> c : C)
				classToHandler.put(c, handler);
		}

		public String getPrefix()
		{
			return prefix;
		}

		public Class<?>[] getClasses()
		{
			return classC;
		}

		@SuppressWarnings("unchecked")
		public Object parse(final Object obj)
		{
			return parseConverter.convert(obj);
		}

		@SuppressWarnings("unchecked")
		public Object serialize(final Object obj)
		{
			return serializeConverter.convert(obj);
		}
	}

	// Maps prefix string to handler for parsing
	private final static Map<String, TSONTypeHandler> prefixToHandler = new Hashtable<String, TSONTypeHandler>();

	// Maps class to handler for serialization
	private final static Map<Class<?>, TSONTypeHandler> classToHandler = new Hashtable<Class<?>, TSONTypeHandler>();
	{
		// Integer handler
		TSONTypeHandler.create("I",
			new Class<?>[] {Integer.class, Integer[].class},
			new Converter<Object,Object>()
			{
				@Override
				public Object convert(final Object obj)
				{
					if(obj instanceof JSONObject)
						throw new IllegalArgumentException("Cannot convert JSONObject to Integer");
					if(obj == NULL || obj == null)
						return null;
					if(obj instanceof Object[])
					{
						final Object[] arr = (Object[])obj;
						final Integer[] result = new Integer[arr.length];
						for(int i = 0; i < arr.length; i++)
							result[i] = (Integer)convert(arr[i]);
						return result;
					}
					if(obj instanceof Integer)
						return obj;
					if(obj instanceof Long)
						return Integer.valueOf(((Long)obj).intValue());
					if(obj instanceof Double)
						return Integer.valueOf(((Double)obj).intValue());
					if(obj instanceof Boolean)
						return ((Boolean)obj).booleanValue() ? Integer.valueOf(1) : Integer.valueOf(0);
					if(obj instanceof String)
					{
						try {
							return Integer.valueOf((String)obj);
						} catch(final NumberFormatException e) {
							throw new IllegalArgumentException("Cannot convert String '"+obj+"' to Integer");
						}
					}
					throw new IllegalArgumentException("Cannot convert "+obj.getClass().getName()+" to Integer");
				}
			},
			new Converter<Object,Object>()
			{
				@Override
				public Object convert(final Object obj)
				{
					return obj;
				}
			}
		);

		// Long handler
		TSONTypeHandler.create("L",
			new Class<?>[] {Long.class, Long[].class},
			new Converter<Object,Object>()
			{
				@Override
				public Object convert(final Object obj)
				{
					if(obj instanceof JSONObject)
						throw new IllegalArgumentException("Cannot convert JSONObject to Long");
					if(obj == NULL || obj == null)
						return null;
					if(obj instanceof Object[])
					{
						final Object[] arr = (Object[])obj;
						final Long[] result = new Long[arr.length];
						for(int i = 0; i < arr.length; i++)
							result[i] = (Long)convert(arr[i]);
						return result;
					}
					if(obj instanceof Long)
						return obj;
					if(obj instanceof Integer)
						return Long.valueOf(((Integer)obj).longValue());
					if(obj instanceof Double)
						return Long.valueOf(((Double)obj).longValue());
					if(obj instanceof Boolean)
						return ((Boolean)obj).booleanValue() ? Long.valueOf(1L) : Long.valueOf(0L);
					if(obj instanceof String)
					{
						try {
							return Long.valueOf((String)obj);
						} catch(final NumberFormatException e) {
							throw new IllegalArgumentException("Cannot convert String '"+obj+"' to Long");
						}
					}
					throw new IllegalArgumentException("Cannot convert "+obj.getClass().getName()+" to Long");
				}
			},
			new Converter<Object,Object>()
			{
				@Override
				public Object convert(final Object obj)
				{
					return obj;
				}
			}
		);

		// Boolean handler
		TSONTypeHandler.create("B",
			new Class<?>[] {Boolean.class, Boolean.class},
			new Converter<Object,Object>()
			{
				@Override
				public Object convert(final Object obj)
				{
					if(obj instanceof JSONObject)
						throw new IllegalArgumentException("Cannot convert JSONObject to Boolean");
					if(obj == NULL || obj == null)
						return null;
					if(obj instanceof Object[])
					{
						final Object[] arr = (Object[])obj;
						final Boolean[] result = new Boolean[arr.length];
						for(int i = 0; i < arr.length; i++)
							result[i] = (Boolean)convert(arr[i]);
						return result;
					}
					if(obj instanceof Boolean)
						return obj;
					if(obj instanceof Long)
						return Boolean.valueOf(((Long)obj).longValue() != 0);
					if(obj instanceof Integer)
						return Boolean.valueOf(((Integer)obj).intValue() != 0);
					if(obj instanceof Double)
						return Boolean.valueOf(((Double)obj).doubleValue() != 0.0);
					if(obj instanceof String)
						return Boolean.valueOf((String)obj);
					throw new IllegalArgumentException("Cannot convert "+obj.getClass().getName()+" to Boolean");
				}
			},
			new Converter<Object,Object>()
			{
				@Override
				public Object convert(final Object obj)
				{
					return obj;
				}
			}
		);

		// Float handler
		TSONTypeHandler.create("F",
			new Class<?>[] {Float.class, Float[].class},
			new Converter<Object,Object>()
			{
				@Override
				public Object convert(final Object obj)
				{
					if(obj instanceof JSONObject)
						throw new IllegalArgumentException("Cannot convert JSONObject to Float");
					if(obj == NULL || obj == null)
						return null;
					if(obj instanceof Object[])
					{
						final Object[] arr = (Object[])obj;
						final Float[] result = new Float[arr.length];
						for(int i = 0; i < arr.length; i++)
							result[i] = (Float)convert(arr[i]);
						return result;
					}
					if(obj instanceof Float)
						return obj;
					if(obj instanceof Double)
						return Float.valueOf(((Double)obj).floatValue());
					if(obj instanceof Long)
						return Float.valueOf(((Long)obj).floatValue());
					if(obj instanceof Integer)
						return Float.valueOf(((Integer)obj).floatValue());
					if(obj instanceof Boolean)
						return ((Boolean)obj).booleanValue() ? Float.valueOf(1.0f) : Float.valueOf(0.0f);
					if(obj instanceof String)
					{
						try {
							return Float.valueOf((String)obj);
						} catch(final NumberFormatException e) {
							throw new IllegalArgumentException("Cannot convert String '"+obj+"' to Float");
						}
					}
					throw new IllegalArgumentException("Cannot convert "+obj.getClass().getName()+" to Float");
				}
			},
			new Converter<Object,Object>()
			{
				@Override
				public Object convert(final Object obj)
				{
					return obj;
				}
			}
		);

		// Double handler
		TSONTypeHandler.create("D",
			new Class<?>[] {Double.class, Double[].class},
			new Converter<Object,Object>()
			{
				@Override
				public Object convert(final Object obj)
				{
					if(obj instanceof JSONObject)
						throw new IllegalArgumentException("Cannot convert JSONObject to Double");
					if(obj == NULL || obj == null)
						return null;
					if(obj instanceof Object[])
					{
						final Object[] arr = (Object[])obj;
						final Double[] result = new Double[arr.length];
						for(int i = 0; i < arr.length; i++)
							result[i] = (Double)convert(arr[i]);
						return result;
					}
					if(obj instanceof Double)
						return obj;
					if(obj instanceof Float)
						return Double.valueOf(((Float)obj).doubleValue());
					if(obj instanceof Long)
						return Double.valueOf(((Long)obj).doubleValue());
					if(obj instanceof Integer)
						return Double.valueOf(((Integer)obj).doubleValue());
					if(obj instanceof Boolean)
						return ((Boolean)obj).booleanValue() ? Double.valueOf(1.0) : Double.valueOf(0.0);
					if(obj instanceof String)
					{
						try {
							return Double.valueOf((String)obj);
						} catch(final NumberFormatException e) {
							throw new IllegalArgumentException("Cannot convert String '"+obj+"' to Double");
						}
					}
					throw new IllegalArgumentException("Cannot convert "+obj.getClass().getName()+" to Double");
				}
			},
			new Converter<Object,Object>()
			{
				@Override
				public Object convert(final Object obj)
				{
					return obj;
				}
			}
		);

		// String handler
		TSONTypeHandler.create("S",
			new Class<?>[] {String.class, String[].class},
			new Converter<Object,Object>()
			{
				@Override
				public Object convert(final Object obj)
				{
					if(obj instanceof JSONObject)
						throw new IllegalArgumentException("Cannot convert JSONObject to String");
					if(obj == NULL || obj == null)
						return null;
					if(obj instanceof Object[])
					{
						final Object[] arr = (Object[])obj;
						final String[] result = new String[arr.length];
						for(int i = 0; i < arr.length; i++)
							result[i] = (String)convert(arr[i]);
						return result;
					}
					if(obj instanceof String)
						return obj;
					return obj.toString();
				}
			},
			new Converter<Object,Object>()
			{
				@Override
				public Object convert(final Object obj)
				{
					return obj;
				}
			}
		);

		// Short handler
		TSONTypeHandler.create("H",
			new Class<?>[] {Short.class,Short[].class},
			new Converter<Object,Object>()
			{
				@Override
				public Object convert(final Object obj)
				{
					if(obj instanceof JSONObject)
						throw new IllegalArgumentException("Cannot convert JSONObject to Short");
					if(obj == NULL || obj == null)
						return null;
					if(obj instanceof Object[])
					{
						final Object[] arr = (Object[])obj;
						final Short[] result = new Short[arr.length];
						for(int i = 0; i < arr.length; i++)
							result[i] = (Short)convert(arr[i]);
						return result;
					}
					if(obj instanceof Short)
						return obj;
					if(obj instanceof Long)
						return Short.valueOf(((Long)obj).shortValue());
					if(obj instanceof Integer)
						return Short.valueOf(((Integer)obj).shortValue());
					if(obj instanceof Double)
						return Short.valueOf(((Double)obj).shortValue());
					if(obj instanceof Boolean)
						return ((Boolean)obj).booleanValue() ? Short.valueOf((short)1) : Short.valueOf((short)0);
					if(obj instanceof String)
					{
						try {
							return Short.valueOf((String)obj);
						} catch(final NumberFormatException e) {
							throw new IllegalArgumentException("Cannot convert String '"+obj+"' to Short");
						}
					}
					throw new IllegalArgumentException("Cannot convert "+obj.getClass().getName()+" to Short");
				}
			},
			new Converter<Object,Object>()
			{
				@Override
				public Object convert(final Object obj)
				{
					return obj;
				}
			}
		);

		// Byte handler
		TSONTypeHandler.create("Y",
			new Class<?>[] {Byte.class, Byte[].class},
			new Converter<Object,Object>()
			{
				@Override
				public Object convert(final Object obj)
				{
					if(obj instanceof JSONObject)
						throw new IllegalArgumentException("Cannot convert JSONObject to Byte");
					if(obj == NULL || obj == null)
						return null;
					if(obj instanceof Object[])
					{
						final Object[] arr = (Object[])obj;
						final Byte[] result = new Byte[arr.length];
						for(int i = 0; i < arr.length; i++)
							result[i] = (Byte)convert(arr[i]);
						return result;
					}
					if(obj instanceof Byte)
						return obj;
					if(obj instanceof Long)
						return Byte.valueOf(((Long)obj).byteValue());
					if(obj instanceof Integer)
						return Byte.valueOf(((Integer)obj).byteValue());
					if(obj instanceof Double)
						return Byte.valueOf(((Double)obj).byteValue());
					if(obj instanceof Boolean)
						return ((Boolean)obj).booleanValue() ? Byte.valueOf((byte)1) : Byte.valueOf((byte)0);
					if(obj instanceof String)
					{
						try {
							return Byte.valueOf((String)obj);
						} catch(final NumberFormatException e) {
							throw new IllegalArgumentException("Cannot convert String '"+obj+"' to Byte");
						}
					}
					throw new IllegalArgumentException("Cannot convert "+obj.getClass().getName()+" to Byte");
				}
			},
			new Converter<Object,Object>()
			{
				@Override
				public Object convert(final Object obj)
				{
					return obj;
				}
			}
		);

		// Hashtable handler - converts to JSONObject for serialization
		TSONTypeHandler.create("ht",
			new Class<?>[] {Hashtable.class},
			new Converter<Object,Object>()
			{
				@Override
				public Object convert(final Object obj)
				{
					if(!(obj instanceof JSONObject))
						throw new IllegalArgumentException("Expected JSONObject for Hashtable conversion");
					final JSONObject json = (JSONObject)obj;
					final Hashtable<String,Object> result = new Hashtable<String,Object>();
					result.putAll(json);
					return result;
				}
			},
			new Converter<Object,Object>()
			{
				@Override
				public Object convert(final Object obj)
				{
					if(!(obj instanceof Hashtable))
						throw new IllegalArgumentException("Expected Hashtable for serialization");
					@SuppressWarnings("unchecked")
					final Hashtable<String,Object> ht = (Hashtable<String,Object>)obj;
					final TSONObject result = new TSONObject();
					result.putAll(ht);
					return result;
				}
			}
		);

		// HashMap handler - converts to JSONObject for serialization
		TSONTypeHandler.create("hm",
			new Class<?>[] {java.util.HashMap.class},
			new Converter<Object,Object>()
			{
				@Override
				public Object convert(final Object obj)
				{
					if(!(obj instanceof JSONObject))
						throw new IllegalArgumentException("Expected JSONObject for HashMap conversion");
					final JSONObject json = (JSONObject)obj;
					final Map<String,Object> result = new HashMap<String,Object>();
					result.putAll(json);
					return result;
				}
			},
			new Converter<Object,Object>()
			{
				@Override
				public Object convert(final Object obj)
				{
					if(!(obj instanceof Map))
						throw new IllegalArgumentException("Expected Map for serialization");
					@SuppressWarnings("unchecked")
					final Map<String,Object> map = (Map<String,Object>)obj;
					final TSONObject result = new TSONObject();
					result.putAll(map);
					return result;
				}
			}
		);

		// TreeMap converter
		TSONTypeHandler.create("tm",
			new Class<?>[] {java.util.TreeMap.class},
			new Converter<Object,Object>()
			{
				@Override
				public Object convert(final Object obj)
				{
					if(!(obj instanceof JSONObject))
						throw new IllegalArgumentException("Expected JSONObject for HashMap conversion");
					final JSONObject json = (JSONObject)obj;
					final Map<String,Object> result = new TreeMap<String,Object>();
					result.putAll(json);
					return result;
				}
			},
			prefixToHandler.get("hm").serializeConverter
		);

		// ArrayList handler - converts to Object[] for serialization
		TSONTypeHandler.create("al",
			new Class<?>[] {ArrayList.class},
			new Converter<Object,Object>()
			{
				@Override
				public Object convert(final Object obj)
				{
					if(!(obj instanceof Object[]))
						throw new IllegalArgumentException("Expected array for ArrayList conversion");
					final Object[] arr = (Object[])obj;
					final ArrayList<Object> result = new ArrayList<Object>(arr.length);
					for(final Object o : arr)
						result.add(o);
					return result;
				}
			},
			new Converter<Object,Object>()
			{
				@Override
				public Object convert(final Object obj)
				{
					if(!(obj instanceof ArrayList))
						throw new IllegalArgumentException("Expected ArrayList for serialization");
					@SuppressWarnings("unchecked")
					final ArrayList<Object> list = (ArrayList<Object>)obj;
					return list.toArray(new Object[list.size()]);
				}
			}
		);

		// Vector handler - converts to Object[] for serialization
		TSONTypeHandler.create("v",
			new Class<?>[] {Vector.class},
			new Converter<Object,Object>()
			{
				@Override
				public Object convert(final Object obj)
				{
					if(!(obj instanceof Object[]))
						throw new IllegalArgumentException("Expected array for Vector conversion");
					final Object[] arr = (Object[])obj;
					final java.util.Vector<Object> result = new java.util.Vector<Object>(arr.length);
					for(final Object o : arr)
						result.add(o);
					return result;
				}
			},
			new Converter<Object,Object>()
			{
				@Override
				public Object convert(final Object obj)
				{
					if(!(obj instanceof java.util.Vector))
						throw new IllegalArgumentException("Expected Vector for serialization");
					@SuppressWarnings("unchecked")
					final java.util.Vector<Object> vec = (java.util.Vector<Object>)obj;
					return vec.toArray(new Object[vec.size()]);
				}
			}
		);

		// int[] handler
		TSONTypeHandler.create("i",
			new Class<?>[] {int[].class},
			new Converter<Object,Object>()
			{
				@Override
				public Object convert(final Object obj)
				{
					if(!(obj instanceof Object[]))
						throw new IllegalArgumentException("Expected array for int[] conversion");
					final Object[] arr = (Object[])obj;
					final int[] result = new int[arr.length];
					for(int i = 0; i < arr.length; i++)
					{
						if(arr[i] instanceof Long)
							result[i] = ((Long)arr[i]).intValue();
						else if(arr[i] instanceof Integer)
							result[i] = ((Integer)arr[i]).intValue();
						else if(arr[i] instanceof Double)
							result[i] = ((Double)arr[i]).intValue();
						else
							throw new IllegalArgumentException("Cannot convert " + arr[i].getClass() + " to int");
					}
					return result;
				}
			},
			new Converter<Object,Object>()
			{
				@Override
				public Object convert(final Object obj)
				{
					return obj; // Handled in appendJSONValue
				}
			}
		);

		// long[] handler
		TSONTypeHandler.create("l",
			new Class<?>[] {long[].class},
			new Converter<Object,Object>()
			{
				@Override
				public Object convert(final Object obj)
				{
					if(!(obj instanceof Object[]))
						throw new IllegalArgumentException("Expected array for long[] conversion");
					final Object[] arr = (Object[])obj;
					final long[] result = new long[arr.length];
					for(int i = 0; i < arr.length; i++)
					{
						if(arr[i] instanceof Long)
							result[i] = ((Long)arr[i]).longValue();
						else if(arr[i] instanceof Integer)
							result[i] = ((Integer)arr[i]).longValue();
						else if(arr[i] instanceof Double)
							result[i] = ((Double)arr[i]).longValue();
						else
							throw new IllegalArgumentException("Cannot convert " + arr[i].getClass() + " to long");
					}
					return result;
				}
			},
			new Converter<Object,Object>()
			{
				@Override
				public Object convert(final Object obj)
				{
					return obj;
				}
			}
		);

		// short[] handler
		TSONTypeHandler.create("h",
			new Class<?>[] {short[].class},
			new Converter<Object,Object>()
			{
				@Override
				public Object convert(final Object obj)
				{
					if(!(obj instanceof Object[]))
						throw new IllegalArgumentException("Expected array for short[] conversion");
					final Object[] arr = (Object[])obj;
					final short[] result = new short[arr.length];
					for(int i = 0; i < arr.length; i++)
					{
						if(arr[i] instanceof Long)
							result[i] = ((Long)arr[i]).shortValue();
						else if(arr[i] instanceof Integer)
							result[i] = ((Integer)arr[i]).shortValue();
						else if(arr[i] instanceof Double)
							result[i] = ((Double)arr[i]).shortValue();
						else
							throw new IllegalArgumentException("Cannot convert " + arr[i].getClass() + " to short");
					}
					return result;
				}
			},
			new Converter<Object,Object>()
			{
				@Override
				public Object convert(final Object obj)
				{
					return obj;
				}
			}
		);

		// byte[] handler
		TSONTypeHandler.create("y",
			new Class<?>[] {byte[].class},
			new Converter<Object,Object>()
			{
				@Override
				public Object convert(final Object obj)
				{
					if(!(obj instanceof Object[]))
						throw new IllegalArgumentException("Expected array for byte[] conversion");
					final Object[] arr = (Object[])obj;
					final byte[] result = new byte[arr.length];
					for(int i = 0; i < arr.length; i++)
					{
						if(arr[i] instanceof Long)
							result[i] = ((Long)arr[i]).byteValue();
						else if(arr[i] instanceof Integer)
							result[i] = ((Integer)arr[i]).byteValue();
						else if(arr[i] instanceof Double)
							result[i] = ((Double)arr[i]).byteValue();
						else
							throw new IllegalArgumentException("Cannot convert " + arr[i].getClass() + " to byte");
					}
					return result;
				}
			},
			new Converter<Object,Object>()
			{
				@Override
				public Object convert(final Object obj)
				{
					return obj;
				}
			}
		);

		// float[] handler
		TSONTypeHandler.create("f",
			new Class<?>[] {float[].class},
			new Converter<Object,Object>()
			{
				@Override
				public Object convert(final Object obj)
				{
					if(!(obj instanceof Object[]))
						throw new IllegalArgumentException("Expected array for float[] conversion");
					final Object[] arr = (Object[])obj;
					final float[] result = new float[arr.length];
					for(int i = 0; i < arr.length; i++)
					{
						if(arr[i] instanceof Double)
							result[i] = ((Double)arr[i]).floatValue();
						else if(arr[i] instanceof Float)
							result[i] = ((Float)arr[i]).floatValue();
						else if(arr[i] instanceof Long)
							result[i] = ((Long)arr[i]).floatValue();
						else if(arr[i] instanceof Integer)
							result[i] = ((Integer)arr[i]).floatValue();
						else
							throw new IllegalArgumentException("Cannot convert " + arr[i].getClass() + " to float");
					}
					return result;
				}
			},
			new Converter<Object,Object>()
			{
				@Override
				public Object convert(final Object obj)
				{
					return obj;
				}
			}
		);

		// double[] handler
		TSONTypeHandler.create("d",
			new Class<?>[] {double[].class},
			new Converter<Object,Object>()
			{
				@Override
				public Object convert(final Object obj)
				{
					if(!(obj instanceof Object[]))
						throw new IllegalArgumentException("Expected array for double[] conversion");
					final Object[] arr = (Object[])obj;
					final double[] result = new double[arr.length];
					for(int i = 0; i < arr.length; i++)
					{
						if(arr[i] instanceof Double)
							result[i] = ((Double)arr[i]).doubleValue();
						else if(arr[i] instanceof Float)
							result[i] = ((Float)arr[i]).doubleValue();
						else if(arr[i] instanceof Long)
							result[i] = ((Long)arr[i]).doubleValue();
						else if(arr[i] instanceof Integer)
							result[i] = ((Integer)arr[i]).doubleValue();
						else
							throw new IllegalArgumentException("Cannot convert " + arr[i].getClass() + " to double");
					}
					return result;
				}
			},
			new Converter<Object,Object>()
			{
				@Override
				public Object convert(final Object obj)
				{
					return obj;
				}
			}
		);

		// boolean[] handler
		TSONTypeHandler.create("b",
			new Class<?>[] {boolean[].class},
			new Converter<Object,Object>()
			{
				@Override
				public Object convert(final Object obj)
				{
					if(!(obj instanceof Object[]))
						throw new IllegalArgumentException("Expected array for boolean[] conversion");
					final Object[] arr = (Object[])obj;
					final boolean[] result = new boolean[arr.length];
					for(int i = 0; i < arr.length; i++)
					{
						if(arr[i] instanceof Boolean)
							result[i] = ((Boolean)arr[i]).booleanValue();
						else if(arr[i] instanceof Long)
							result[i] = ((Long)arr[i]).longValue() != 0;
						else if(arr[i] instanceof Integer)
							result[i] = ((Integer)arr[i]).intValue() != 0;
						else
							throw new IllegalArgumentException("Cannot convert " + arr[i].getClass() + " to boolean");
					}
					return result;
				}
			},
			new Converter<Object,Object>()
			{
				@Override
				public Object convert(final Object obj)
				{
					return obj;
				}
			}
		);

		// Pair handler - converts to Object[] with 2 elements for serialization
		TSONTypeHandler.create("p",
			new Class<?>[] {Pair.class},
			new Converter<Object,Object>() // parse: Object[] -> Pair
			{
				@Override
				public Object convert(final Object obj)
				{
					if(!(obj instanceof Object[]))
						throw new IllegalArgumentException("Expected array for Pair conversion");
					final Object[] arr = (Object[])obj;
					if(arr.length != 2)
						throw new IllegalArgumentException("Expected array of length 2 for Pair conversion, got " + arr.length);
					return new com.planet_ink.coffee_mud.core.collections.Pair<Object,Object>(arr[0], arr[1]);
				}
			},
			new Converter<Object,Object>() // serialize: Pair -> Object[]
			{
				@Override
				public Object convert(final Object obj)
				{
					if(!(obj instanceof com.planet_ink.coffee_mud.core.collections.Pair))
						throw new IllegalArgumentException("Expected Pair for serialization");
					@SuppressWarnings("unchecked")
					final com.planet_ink.coffee_mud.core.collections.Pair<Object,Object> pair =
						(com.planet_ink.coffee_mud.core.collections.Pair<Object,Object>)obj;
					return new Object[] { pair.first, pair.second };
				}
			}
		);

		// Triad handler - converts to Object[] with 3 elements for serialization
		TSONTypeHandler.create("tri",
			new Class<?>[] {Triad.class},
			new Converter<Object,Object>() // parse: Object[] -> Triad
			{
				@Override
				public Object convert(final Object obj)
				{
					if(!(obj instanceof Object[]))
						throw new IllegalArgumentException("Expected array for Triad conversion");
					final Object[] arr = (Object[])obj;
					if(arr.length != 3)
						throw new IllegalArgumentException("Expected array of length 3 for Triad conversion, got " + arr.length);
					return new com.planet_ink.coffee_mud.core.collections.Triad<Object,Object,Object>(arr[0], arr[1], arr[2]);
				}
			},
			new Converter<Object,Object>() // serialize: Triad -> Object[]
			{
				@Override
				public Object convert(final Object obj)
				{
					if(!(obj instanceof com.planet_ink.coffee_mud.core.collections.Triad))
						throw new IllegalArgumentException("Expected Triad for serialization");
					@SuppressWarnings("unchecked")
					final com.planet_ink.coffee_mud.core.collections.Triad<Object,Object,Object> triad =
						(com.planet_ink.coffee_mud.core.collections.Triad<Object,Object,Object>)obj;
					return new Object[] { triad.first, triad.second, triad.third };
				}
			}
		);

		// Room reference handler - converts to String (room id)
		TSONTypeHandler.create("RmR",
			new Class<?>[] {Room.class},
			new Converter<Object,Object>() // parse: String -> Room
			{
				@Override
				public Object convert(final Object obj)
				{
					if(!(obj instanceof String))
						throw new IllegalArgumentException("Expected array for Triad conversion");
					return CMLib.map().getRoom((String)obj);
				}
			},
			new Converter<Object,Object>() // serialize: Room -> String
			{
				@Override
				public Object convert(final Object obj)
				{
					if(!(obj instanceof Room))
						throw new IllegalArgumentException("Expected Room for serialization");
					return CMLib.map().getExtendedRoomID((Room)obj);
				}
			}
		);
	}


	/**
	 * An official JSON object. Implemented as a Map, this class has numerous
	 * methods for accessing the internal keys and their mapped values in
	 * different ways, both raw, and checked.
	 *
	 * @author Bo Zimmerman
	 */
	public static class TSONObject extends MiniJSON.JSONObject
	{
		private static final long serialVersionUID = 8390276973120912175L;

		@Override
		public void appendJSONValue(final StringBuilder value, final Object obj)
		{
			if(obj == null || obj == NULL)
			{
				super.appendJSONValue(value, obj);
				return;
			}

			if(obj.getClass().isArray() && obj.getClass().getComponentType().isPrimitive())
			{
				final Class<?> componentType = obj.getClass().getComponentType();
				String typePrefix = "";

				if(componentType == int.class)
					typePrefix = "i";
				else if(componentType == long.class)
					typePrefix = "l";
				else if(componentType == short.class)
					typePrefix = "h";
				else if(componentType == byte.class)
					typePrefix = "y";
				else if(componentType == float.class)
					typePrefix = "f";
				else if(componentType == double.class)
					typePrefix = "d";
				else if(componentType == boolean.class)
					typePrefix = "b";

				if(!typePrefix.equals("l") && !typePrefix.equals("d") && !typePrefix.equals("b"))
					value.append(typePrefix);

				value.append("[");
				final int length = Array.getLength(obj);
				for(int i = 0; i < length; i++)
				{
					if(i > 0)
						value.append(",");
					final Object element = Array.get(obj, i);
					super.appendJSONValue(value, element);
				}
				value.append("]");
				return;
			}

			final TSONTypeHandler handler = classToHandler.get(obj.getClass());
			if(handler != null)
			{
				final String prefix = handler.getPrefix();
				if(!prefix.equals("L") && !prefix.equals("D") && !prefix.equals("S") && !prefix.equals("B"))
					value.append(prefix);

				final Object serialized = handler.serialize(obj);
				super.appendJSONValue(value, serialized);
			}
			else
			{
				String typePrefix = "";

				if(obj instanceof Integer || (obj instanceof Object[] && ((Object[])obj).length > 0 && ((Object[])obj)[0] instanceof Integer))
					typePrefix = "I";
				else if(obj instanceof Short || (obj instanceof Object[] && ((Object[])obj).length > 0 && ((Object[])obj)[0] instanceof Short))
					typePrefix = "H";
				else if(obj instanceof Byte || (obj instanceof Object[] && ((Object[])obj).length > 0 && ((Object[])obj)[0] instanceof Byte))
					typePrefix = "Y";
				else if(obj instanceof Float || (obj instanceof Object[] && ((Object[])obj).length > 0 && ((Object[])obj)[0] instanceof Float))
					typePrefix = "F";
				value.append(typePrefix);
				super.appendJSONValue(value, obj);
			}
		}


		/**
		 * Makes a deep copy of this JSONObject and returns it.
		 * @return a deep copy of this JSONObject.
		 */
 		@Override
		public JSONObject copyOf()
		{
			final JSONObject newObj = new TSONObject();
			for(final String key : this.keySet())
				newObj.put(key, jsonDeepCopy(this.get(key)));
			return newObj;
		}
	}

	/**
	 * Generates a new JSON Object for your jsoning pleasure.
	 * @return a new JSON Object
	 */
	@Override
	public JSONObject createJSONObject()
	{
		return new TSONObject();
	}


	/**
	 * Given a TSON document char array, and an index into it, parses a value
	 * object at the indexed point of the char array and returns its value
	 * object. A value object may be anything from a string, array, a TSON
	 * object, boolean, null, or a number.
	 *
	 * @param doc the TSON doc containing the value
	 * @param index the index into that TSON doc where the value begins
	 * @param depth the current parsing depth, to prevent stack overflows
	 * @return the value object of the found value
	 * @throws MJSONException a parse exception, meaning no recognized value was there
	 */
	@Override
	protected Object parseElement(final char[] doc, final int[] index, final int depth) throws MJSONException
	{
		while (index[0] < doc.length && Character.isWhitespace(doc[index[0]])) {
			index[0]++ ;
		}
		if (index[0] >= doc.length)
			throw new MJSONException("Unexpected end of document @"+index[0]);
		final StringBuilder type = new StringBuilder("");
		while(index[0] < doc.length && Character.isLetter(doc[index[0]]))
			type.append(doc[index[0]++]);
		if((type.length()>0)
		&& ((type.charAt(type.length()-1)=='l')||(type.charAt(type.length()-1)=='e')))
		{
			final String ts = type.toString();
			if(ts.endsWith(TRUE_STR))
			{
				index[0] -= TRUE_STR.length();
				type.setLength(type.length()-TRUE_STR.length());
			}
			else
			if(ts.endsWith(FALSE_STR))
			{
				index[0] -= FALSE_STR.length();
				type.setLength(type.length()-FALSE_STR.length());
			}
			else
			if(ts.endsWith(NULL_STR))
			{
				index[0] -= NULL_STR.length();
				type.setLength(type.length()-NULL_STR.length());
			}
		}
		if (index[0] >= doc.length)
			throw new MJSONException("Unexpected end of document @"+index[0]);
		final Object o = super.parseElement(doc, index, depth);
		if(type.length()==0)
			return o;
		final TSONTypeHandler handler = prefixToHandler.get(type.toString());
		if(handler == null)
			throw new MJSONException("Unknown data type '"+type.toString()+"' @ "+index[0]);
		try
		{
			return handler.parse(o);
		}
		catch(final IllegalArgumentException e)
		{
			throw new MJSONException(e.getMessage());
		}
	}
}