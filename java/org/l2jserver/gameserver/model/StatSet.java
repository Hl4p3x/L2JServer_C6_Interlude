/*
 * This file is part of the L2JServer project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2jserver.gameserver.model;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.l2jserver.commons.util.TimeUtil;
import org.l2jserver.gameserver.model.interfaces.IParserAdvUtils;
import org.l2jserver.gameserver.model.skills.holders.SkillHolder;
import org.l2jserver.gameserver.util.Util;

/**
 * This class is meant to hold a set of (key,value) pairs.<br>
 * They are stored as object but can be retrieved in any type wanted. As long as cast is available.<br>
 * @author mkizub
 */
public class StatSet implements IParserAdvUtils
{
	private static final Logger LOGGER = Logger.getLogger(StatSet.class.getName());
	/** Static empty immutable map, used to avoid multiple null checks over the source. */
	public static final StatSet EMPTY_STATSET = new StatSet(Collections.emptyMap());
	
	private final Map<String, Object> _set;
	
	public StatSet()
	{
		this(HashMap::new);
	}
	
	public StatSet(Supplier<Map<String, Object>> mapFactory)
	{
		this(mapFactory.get());
	}
	
	public StatSet(Map<String, Object> map)
	{
		_set = map;
	}
	
	/**
	 * Returns the set of values
	 * @return HashMap
	 */
	public Map<String, Object> getSet()
	{
		return _set;
	}
	
	/**
	 * Add a set of couple values in the current set
	 * @param newSet : StatSet pointing out the list of couples to add in the current set
	 */
	public void merge(StatSet newSet)
	{
		_set.putAll(newSet.getSet());
	}
	
	/**
	 * Verifies if the stat set is empty.
	 * @return {@code true} if the stat set is empty, {@code false} otherwise
	 */
	public boolean isEmpty()
	{
		return _set.isEmpty();
	}
	
	/**
	 * Return the boolean value associated with key.
	 * @param key : String designating the key in the set
	 * @return boolean : value associated to the key
	 * @throws IllegalArgumentException : If value is not set or value is not boolean
	 */
	@Override
	public boolean getBoolean(String key)
	{
		final Object val = _set.get(key);
		if (val == null)
		{
			throw new IllegalArgumentException("Boolean value required, but not specified");
		}
		if (val instanceof Boolean)
		{
			return ((Boolean) val).booleanValue();
		}
		try
		{
			return Boolean.parseBoolean((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Boolean value required, but found: " + val);
		}
	}
	
	/**
	 * Return the boolean value associated with key.<br>
	 * If no value is associated with key, or type of value is wrong, returns defaultValue.
	 * @param key : String designating the key in the entry set
	 * @return boolean : value associated to the key
	 */
	@Override
	public boolean getBoolean(String key, boolean defaultValue)
	{
		final Object val = _set.get(key);
		if (val == null)
		{
			return defaultValue;
		}
		if (val instanceof Boolean)
		{
			return ((Boolean) val).booleanValue();
		}
		try
		{
			return Boolean.parseBoolean((String) val);
		}
		catch (Exception e)
		{
			return defaultValue;
		}
	}
	
	@Override
	public byte getByte(String key)
	{
		final Object val = _set.get(key);
		if (val == null)
		{
			throw new IllegalArgumentException("Byte value required, but not specified");
		}
		if (val instanceof Number)
		{
			return ((Number) val).byteValue();
		}
		try
		{
			return Byte.parseByte((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Byte value required, but found: " + val);
		}
	}
	
	@Override
	public byte getByte(String key, byte defaultValue)
	{
		final Object val = _set.get(key);
		if (val == null)
		{
			return defaultValue;
		}
		if (val instanceof Number)
		{
			return ((Number) val).byteValue();
		}
		try
		{
			return Byte.parseByte((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Byte value required, but found: " + val);
		}
	}
	
	public short increaseByte(String key, byte increaseWith)
	{
		final byte newValue = (byte) (getByte(key) + increaseWith);
		set(key, newValue);
		return newValue;
	}
	
	public short increaseByte(String key, byte defaultValue, byte increaseWith)
	{
		final byte newValue = (byte) (getByte(key, defaultValue) + increaseWith);
		set(key, newValue);
		return newValue;
	}
	
	public byte[] getByteArray(String key, String splitOn)
	{
		final Object val = _set.get(key);
		if (val == null)
		{
			throw new IllegalArgumentException("Byte value required, but not specified");
		}
		if (val instanceof Number)
		{
			return new byte[]
			{
				((Number) val).byteValue()
			};
		}
		int c = 0;
		final String[] vals = ((String) val).split(splitOn);
		final byte[] result = new byte[vals.length];
		for (String v : vals)
		{
			try
			{
				result[c++] = Byte.parseByte(v);
			}
			catch (Exception e)
			{
				throw new IllegalArgumentException("Byte value required, but found: " + val);
			}
		}
		return result;
	}
	
	public List<Byte> getByteList(String key, String splitOn)
	{
		final List<Byte> result = new ArrayList<>();
		for (Byte i : getByteArray(key, splitOn))
		{
			result.add(i);
		}
		return result;
	}
	
	@Override
	public short getShort(String key)
	{
		final Object val = _set.get(key);
		if (val == null)
		{
			throw new IllegalArgumentException("Short value required, but not specified");
		}
		if (val instanceof Number)
		{
			return ((Number) val).shortValue();
		}
		try
		{
			return Short.parseShort((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Short value required, but found: " + val);
		}
	}
	
	@Override
	public short getShort(String key, short defaultValue)
	{
		final Object val = _set.get(key);
		if (val == null)
		{
			return defaultValue;
		}
		if (val instanceof Number)
		{
			return ((Number) val).shortValue();
		}
		try
		{
			return Short.parseShort((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Short value required, but found: " + val);
		}
	}
	
	public short increaseShort(String key, short increaseWith)
	{
		final short newValue = (short) (getShort(key) + increaseWith);
		set(key, newValue);
		return newValue;
	}
	
	public short increaseShort(String key, short defaultValue, short increaseWith)
	{
		final short newValue = (short) (getShort(key, defaultValue) + increaseWith);
		set(key, newValue);
		return newValue;
	}
	
	@Override
	public int getInt(String key)
	{
		final Object val = _set.get(key);
		if (val == null)
		{
			throw new IllegalArgumentException("Integer value required, but not specified: " + key + "!");
		}
		
		if (val instanceof Number)
		{
			return ((Number) val).intValue();
		}
		
		try
		{
			return Integer.parseInt((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Integer value required, but found: " + val + "!");
		}
	}
	
	@Override
	public int getInt(String key, int defaultValue)
	{
		final Object val = _set.get(key);
		if (val == null)
		{
			return defaultValue;
		}
		if (val instanceof Number)
		{
			return ((Number) val).intValue();
		}
		try
		{
			return Integer.parseInt((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Integer value required, but found: " + val);
		}
	}
	
	public int increaseInt(String key, int increaseWith)
	{
		final int newValue = getInt(key) + increaseWith;
		set(key, newValue);
		return newValue;
	}
	
	public int increaseInt(String key, int defaultValue, int increaseWith)
	{
		final int newValue = getInt(key, defaultValue) + increaseWith;
		set(key, newValue);
		return newValue;
	}
	
	public int[] getIntArray(String key, String splitOn)
	{
		final Object val = _set.get(key);
		if (val == null)
		{
			throw new IllegalArgumentException("Integer value required, but not specified");
		}
		if (val instanceof Number)
		{
			return new int[]
			{
				((Number) val).intValue()
			};
		}
		int c = 0;
		final String[] vals = ((String) val).split(splitOn);
		final int[] result = new int[vals.length];
		for (String v : vals)
		{
			try
			{
				result[c++] = Integer.parseInt(v);
			}
			catch (Exception e)
			{
				throw new IllegalArgumentException("Integer value required, but found: " + val);
			}
		}
		return result;
	}
	
	public List<Integer> getIntegerList(String key, String splitOn)
	{
		final List<Integer> result = new ArrayList<>();
		for (int i : getIntArray(key, splitOn))
		{
			result.add(i);
		}
		return result;
	}
	
	@Override
	public long getLong(String key)
	{
		final Object val = _set.get(key);
		if (val == null)
		{
			throw new IllegalArgumentException("Long value required, but not specified");
		}
		if (val instanceof Number)
		{
			return ((Number) val).longValue();
		}
		try
		{
			return Long.parseLong((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Long value required, but found: " + val);
		}
	}
	
	@Override
	public long getLong(String key, long defaultValue)
	{
		final Object val = _set.get(key);
		if (val == null)
		{
			return defaultValue;
		}
		if (val instanceof Number)
		{
			return ((Number) val).longValue();
		}
		try
		{
			return Long.parseLong((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Long value required, but found: " + val);
		}
	}
	
	public long increaseLong(String key, long increaseWith)
	{
		final long newValue = getLong(key) + increaseWith;
		set(key, newValue);
		return newValue;
	}
	
	public long increaseLong(String key, long defaultValue, long increaseWith)
	{
		final long newValue = getLong(key, defaultValue) + increaseWith;
		set(key, newValue);
		return newValue;
	}
	
	@Override
	public float getFloat(String key)
	{
		final Object val = _set.get(key);
		if (val == null)
		{
			throw new IllegalArgumentException("Float value required, but not specified");
		}
		if (val instanceof Number)
		{
			return ((Number) val).floatValue();
		}
		try
		{
			return Float.parseFloat((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Float value required, but found: " + val);
		}
	}
	
	@Override
	public float getFloat(String key, float defaultValue)
	{
		final Object val = _set.get(key);
		if (val == null)
		{
			return defaultValue;
		}
		if (val instanceof Number)
		{
			return ((Number) val).floatValue();
		}
		try
		{
			return Float.parseFloat((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Float value required, but found: " + val);
		}
	}
	
	public float increaseFloat(String key, float increaseWith)
	{
		final float newValue = getFloat(key) + increaseWith;
		set(key, newValue);
		return newValue;
	}
	
	public float increaseFloat(String key, float defaultValue, float increaseWith)
	{
		final float newValue = getFloat(key, defaultValue) + increaseWith;
		set(key, newValue);
		return newValue;
	}
	
	@Override
	public double getDouble(String key)
	{
		final Object val = _set.get(key);
		if (val == null)
		{
			throw new IllegalArgumentException("Double value required, but not specified");
		}
		if (val instanceof Number)
		{
			return ((Number) val).doubleValue();
		}
		try
		{
			return Double.parseDouble((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Double value required, but found: " + val);
		}
	}
	
	@Override
	public double getDouble(String key, double defaultValue)
	{
		final Object val = _set.get(key);
		if (val == null)
		{
			return defaultValue;
		}
		if (val instanceof Number)
		{
			return ((Number) val).doubleValue();
		}
		try
		{
			return Double.parseDouble((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Double value required, but found: " + val);
		}
	}
	
	public double increaseDouble(String key, double increaseWith)
	{
		final double newValue = getDouble(key) + increaseWith;
		set(key, newValue);
		return newValue;
	}
	
	public double increaseDouble(String key, double defaultValue, double increaseWith)
	{
		final double newValue = getDouble(key, defaultValue) + increaseWith;
		set(key, newValue);
		return newValue;
	}
	
	@Override
	public String getString(String key)
	{
		final Object val = _set.get(key);
		if (val == null)
		{
			throw new IllegalArgumentException("String value required, but not specified");
		}
		return String.valueOf(val);
	}
	
	@Override
	public String getString(String key, String defaultValue)
	{
		final Object val = _set.get(key);
		if (val == null)
		{
			return defaultValue;
		}
		return String.valueOf(val);
	}
	
	@Override
	public Duration getDuration(String key)
	{
		final Object val = _set.get(key);
		if (val == null)
		{
			throw new IllegalArgumentException("String value required, but not specified");
		}
		return TimeUtil.parseDuration(String.valueOf(val));
	}
	
	@Override
	public Duration getDuration(String key, Duration defaultValue)
	{
		final Object val = _set.get(key);
		if (val == null)
		{
			return defaultValue;
		}
		return TimeUtil.parseDuration(String.valueOf(val));
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T extends Enum<T>> T getEnum(String key, Class<T> enumClass)
	{
		final Object val = _set.get(key);
		if (val == null)
		{
			throw new IllegalArgumentException("Enum value of type " + enumClass.getName() + " required, but not specified");
		}
		if (enumClass.isInstance(val))
		{
			return (T) val;
		}
		try
		{
			return Enum.valueOf(enumClass, String.valueOf(val));
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Enum value of type " + enumClass.getName() + " required, but found: " + val);
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T extends Enum<T>> T getEnum(String key, Class<T> enumClass, T defaultValue)
	{
		final Object val = _set.get(key);
		if (val == null)
		{
			return defaultValue;
		}
		if (enumClass.isInstance(val))
		{
			return (T) val;
		}
		try
		{
			return Enum.valueOf(enumClass, String.valueOf(val));
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Enum value of type " + enumClass.getName() + " required, but found: " + val);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <A> A getObject(String name, Class<A> type)
	{
		final Object obj = _set.get(name);
		if ((obj == null) || !type.isAssignableFrom(obj.getClass()))
		{
			return null;
		}
		return (A) obj;
	}
	
	@SuppressWarnings("unchecked")
	public <A> A getObject(String name, Class<A> type, A defaultValue)
	{
		final Object obj = _set.get(name);
		if ((obj == null) || !type.isAssignableFrom(obj.getClass()))
		{
			return defaultValue;
		}
		return (A) obj;
	}
	
	public SkillHolder getSkillHolder(String key)
	{
		final Object obj = _set.get(key);
		if (!(obj instanceof SkillHolder))
		{
			return null;
		}
		return (SkillHolder) obj;
	}
	
	public Location getLocation(String key)
	{
		final Object obj = _set.get(key);
		if (!(obj instanceof Location))
		{
			return null;
		}
		return (Location) obj;
	}
	
	@SuppressWarnings("unchecked")
	public <T> List<T> getList(String key, Class<T> clazz)
	{
		final Object obj = _set.get(key);
		if (!(obj instanceof List<?>))
		{
			return null;
		}
		
		final List<Object> originalList = (List<Object>) obj;
		if (!originalList.isEmpty() && !originalList.stream().allMatch(clazz::isInstance))
		{
			if (clazz.getSuperclass() == Enum.class)
			{
				throw new IllegalAccessError("Please use getEnumList if you want to get list of Enums!");
			}
			
			// Attempt to convert the list
			final List<T> convertedList = convertList(originalList, clazz);
			if (convertedList == null)
			{
				LOGGER.log(Level.WARNING, "getList(\"" + key + "\", " + clazz.getSimpleName() + ") requested with wrong generic type: " + obj.getClass().getGenericInterfaces()[0] + "!", new ClassNotFoundException());
				return null;
			}
			
			// Overwrite the existing list with proper generic type
			_set.put(key, convertedList);
			return convertedList;
		}
		return (List<T>) obj;
	}
	
	public <T> List<T> getList(String key, Class<T> clazz, List<T> defaultValue)
	{
		final List<T> list = getList(key, clazz);
		return list == null ? defaultValue : list;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Enum<T>> List<T> getEnumList(String key, Class<T> clazz)
	{
		final Object obj = _set.get(key);
		if (!(obj instanceof List<?>))
		{
			return null;
		}
		
		final List<Object> originalList = (List<Object>) obj;
		if (!originalList.isEmpty() && (obj.getClass().getGenericInterfaces()[0] != clazz) && originalList.stream().allMatch(name -> Util.isEnum(name.toString(), clazz)))
		{
			final List<T> convertedList = originalList.stream().map(Object::toString).map(name -> Enum.valueOf(clazz, name)).map(clazz::cast).collect(Collectors.toList());
			
			// Overwrite the existing list with proper generic type
			_set.put(key, convertedList);
			return convertedList;
		}
		return (List<T>) obj;
	}
	
	/**
	 * @param <T>
	 * @param originalList
	 * @param clazz
	 * @return
	 */
	private <T> List<T> convertList(List<Object> originalList, Class<T> clazz)
	{
		if (clazz == Integer.class)
		{
			if (originalList.stream().map(Object::toString).allMatch(Util::isInteger))
			{
				return originalList.stream().map(Object::toString).map(Integer::valueOf).map(clazz::cast).collect(Collectors.toList());
			}
		}
		else if (clazz == Float.class)
		{
			if (originalList.stream().map(Object::toString).allMatch(Util::isFloat))
			{
				return originalList.stream().map(Object::toString).map(Float::valueOf).map(clazz::cast).collect(Collectors.toList());
			}
		}
		else if (clazz == Double.class)
		{
			if (originalList.stream().map(Object::toString).allMatch(Util::isDouble))
			{
				return originalList.stream().map(Object::toString).map(Double::valueOf).map(clazz::cast).collect(Collectors.toList());
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public <K, V> Map<K, V> getMap(String key, Class<K> keyClass, Class<V> valueClass)
	{
		final Object obj = _set.get(key);
		if (!(obj instanceof Map<?, ?>))
		{
			return null;
		}
		
		final Map<?, ?> originalList = (Map<?, ?>) obj;
		if (!originalList.isEmpty() && ((!originalList.keySet().stream().allMatch(keyClass::isInstance)) || (!originalList.values().stream().allMatch(valueClass::isInstance))))
		{
			LOGGER.log(Level.WARNING, "getMap(\"" + key + "\", " + keyClass.getSimpleName() + ", " + valueClass.getSimpleName() + ") requested with wrong generic type: " + obj.getClass().getGenericInterfaces()[0] + "!", new ClassNotFoundException());
		}
		return (Map<K, V>) obj;
	}
	
	public void set(String name, Object value)
	{
		if (value == null)
		{
			return;
		}
		_set.put(name, value);
	}
	
	public void set(String key, byte value)
	{
		_set.put(key, value);
	}
	
	public void set(String key, short value)
	{
		_set.put(key, value);
	}
	
	public void set(String key, int value)
	{
		_set.put(key, value);
	}
	
	public void set(String key, long value)
	{
		_set.put(key, value);
	}
	
	public void set(String key, float value)
	{
		_set.put(key, value);
	}
	
	public void set(String key, double value)
	{
		_set.put(key, value);
	}
	
	public void set(String key, String value)
	{
		if (value == null)
		{
			return;
		}
		_set.put(key, value);
	}
	
	public void set(String key, Enum<?> value)
	{
		if (value == null)
		{
			return;
		}
		_set.put(key, value);
	}
	
	/**
	 * Safe version of "set". Expected values are within [min, max[<br>
	 * Add the int hold in param "value" for the key "name".
	 * @param name : String designating the key in the set
	 * @param value : int corresponding to the value associated with the key
	 * @param min
	 * @param max
	 * @param reference
	 */
	public synchronized void safeSet(String name, int value, int min, int max, String reference)
	{
		assert ((min > max) || ((value >= min) && (value < max)));
		if ((min <= max) && ((value < min) || (value >= max)))
		{
			LOGGER.info("[StatSet][safeSet] Incorrect value: " + value + "for: " + name + "Ref: " + reference);
		}
		
		set(name, value);
	}
	
	public void remove(String key)
	{
		_set.remove(key);
	}
	
	public boolean contains(String name)
	{
		return _set.containsKey(name);
	}
	
	@Override
	public String toString()
	{
		return "StatSet{_set=" + _set + '}';
	}
}
