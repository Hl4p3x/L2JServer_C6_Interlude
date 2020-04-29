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

/**
 * @author FBIagent
 */
public class AccessLevel
{
	private int _accessLevel = 0;
	private String _name = null;
	private int _nameColor = 0;
	private int _titleColor = 0;
	private boolean _isGm = false;
	private boolean _allowPeaceAttack = false;
	private boolean _allowFixedRes = false;
	private boolean _allowTransaction = false;
	private boolean _allowAltG = false;
	private boolean _giveDamage = false;
	private boolean _takeAggro = false;
	private boolean _gainExp = false;
	private boolean _useNameColor = true;
	private boolean _useTitleColor = false;
	private boolean _canDisableGmStatus = false;
	
	/**
	 * Initializes members
	 * @param accessLevel as int
	 * @param name as String
	 * @param nameColor as int
	 * @param titleColor as int
	 * @param isGm as boolean
	 * @param allowPeaceAttack as boolean
	 * @param allowFixedRes as boolean
	 * @param allowTransaction as boolean
	 * @param allowAltG as boolean
	 * @param giveDamage as boolean
	 * @param takeAggro as boolean
	 * @param gainExp as boolean
	 * @param useNameColor as boolean
	 * @param useTitleColor as boolean
	 * @param canDisableGmStatus
	 */
	public AccessLevel(int accessLevel, String name, int nameColor, int titleColor, boolean isGm, boolean allowPeaceAttack, boolean allowFixedRes, boolean allowTransaction, boolean allowAltG, boolean giveDamage, boolean takeAggro, boolean gainExp, boolean useNameColor, boolean useTitleColor, boolean canDisableGmStatus)
	{
		_accessLevel = accessLevel;
		_name = name;
		_nameColor = nameColor;
		_titleColor = titleColor;
		_isGm = isGm;
		_allowPeaceAttack = allowPeaceAttack;
		_allowFixedRes = allowFixedRes;
		_allowTransaction = allowTransaction;
		_allowAltG = allowAltG;
		_giveDamage = giveDamage;
		_takeAggro = takeAggro;
		_gainExp = gainExp;
		_useNameColor = useNameColor;
		_useTitleColor = useTitleColor;
		_canDisableGmStatus = canDisableGmStatus;
	}
	
	/**
	 * Returns the access level
	 * @return int: access level
	 */
	public int getLevel()
	{
		return _accessLevel;
	}
	
	/**
	 * Returns the access level name
	 * @return String: access level name
	 */
	public String getName()
	{
		return _name;
	}
	
	/**
	 * Returns the name color of the access level
	 * @return int: the name color for the access level
	 */
	public int getNameColor()
	{
		return _nameColor;
	}
	
	/**
	 * Returns the title color color of the access level
	 * @return int: the title color for the access level
	 */
	public int getTitleColor()
	{
		return _titleColor;
	}
	
	/**
	 * Retuns if the access level has GM access or not
	 * @return boolean: true if access level have GM access, otherwise false
	 */
	public boolean isGm()
	{
		return _isGm;
	}
	
	/**
	 * Returns if the access level is allowed to attack in peace zone or not
	 * @return boolean: true if the access level is allowed to attack in peace zone, otherwise false
	 */
	public boolean allowPeaceAttack()
	{
		return _allowPeaceAttack;
	}
	
	/**
	 * @return true if the access level is allowed to use fixed res, otherwise false.
	 */
	public boolean allowFixedRes()
	{
		return _allowFixedRes;
	}
	
	/**
	 * Returns if the access level is allowed to perform transactions or not
	 * @return boolean: true if access level is allowed to perform transactions, otherwise false
	 */
	public boolean allowTransaction()
	{
		return _allowTransaction;
	}
	
	/**
	 * Returns if the access level is allowed to use AltG commands or not
	 * @return boolean: true if access level is allowed to use AltG commands, otherwise false
	 */
	public boolean allowAltG()
	{
		return _allowAltG;
	}
	
	/**
	 * Returns if the access level can give damage or not
	 * @return boolean: true if the access level can give damage, otherwise false
	 */
	public boolean canGiveDamage()
	{
		return _giveDamage;
	}
	
	/**
	 * Returns if the access level can take aggro or not
	 * @return boolean: true if the access level can take aggro, otherwise false
	 */
	public boolean canTakeAggro()
	{
		return _takeAggro;
	}
	
	/**
	 * Returns if the access level can gain exp or not
	 * @return boolean: true if the access level can gain exp, otherwise false
	 */
	public boolean canGainExp()
	{
		return _gainExp;
	}
	
	public boolean useNameColor()
	{
		return _useNameColor;
	}
	
	public boolean useTitleColor()
	{
		return _useTitleColor;
	}
	
	/**
	 * Retuns if the access level is a GM that can temp disable GM access
	 * @return boolean: true if is a GM that can temp disable GM access, otherwise false
	 */
	public boolean canDisableGmStatus()
	{
		return _canDisableGmStatus;
	}
}
