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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.gameserver.datatables.HeroSkillTable;
import org.l2jserver.gameserver.datatables.SkillTable;
import org.l2jserver.gameserver.datatables.sql.SkillTreeTable;
import org.l2jserver.gameserver.geoengine.GeoEngine;
import org.l2jserver.gameserver.instancemanager.SiegeManager;
import org.l2jserver.gameserver.model.actor.Attackable;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.Playable;
import org.l2jserver.gameserver.model.actor.Summon;
import org.l2jserver.gameserver.model.actor.instance.ArtefactInstance;
import org.l2jserver.gameserver.model.actor.instance.ChestInstance;
import org.l2jserver.gameserver.model.actor.instance.ControlTowerInstance;
import org.l2jserver.gameserver.model.actor.instance.DoorInstance;
import org.l2jserver.gameserver.model.actor.instance.MonsterInstance;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PetInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.actor.instance.SummonInstance;
import org.l2jserver.gameserver.model.base.ClassId;
import org.l2jserver.gameserver.model.clan.Clan;
import org.l2jserver.gameserver.model.clan.ClanMember;
import org.l2jserver.gameserver.model.entity.event.CTF;
import org.l2jserver.gameserver.model.entity.event.DM;
import org.l2jserver.gameserver.model.entity.event.TvT;
import org.l2jserver.gameserver.model.entity.siege.Siege;
import org.l2jserver.gameserver.model.skills.BaseStat;
import org.l2jserver.gameserver.model.skills.Env;
import org.l2jserver.gameserver.model.skills.Formulas;
import org.l2jserver.gameserver.model.skills.Stat;
import org.l2jserver.gameserver.model.skills.conditions.Condition;
import org.l2jserver.gameserver.model.skills.effects.EffectCharge;
import org.l2jserver.gameserver.model.skills.effects.EffectTemplate;
import org.l2jserver.gameserver.model.skills.funcs.Func;
import org.l2jserver.gameserver.model.skills.funcs.FuncTemplate;
import org.l2jserver.gameserver.model.skills.handlers.SkillCharge;
import org.l2jserver.gameserver.model.skills.handlers.SkillChargeDmg;
import org.l2jserver.gameserver.model.skills.handlers.SkillChargeEffect;
import org.l2jserver.gameserver.model.skills.handlers.SkillCreateItem;
import org.l2jserver.gameserver.model.skills.handlers.SkillDefault;
import org.l2jserver.gameserver.model.skills.handlers.SkillDrain;
import org.l2jserver.gameserver.model.skills.handlers.SkillSeed;
import org.l2jserver.gameserver.model.skills.handlers.SkillSignet;
import org.l2jserver.gameserver.model.skills.handlers.SkillSignetCasttime;
import org.l2jserver.gameserver.model.skills.handlers.SkillSummon;
import org.l2jserver.gameserver.model.zone.ZoneId;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.EtcStatusUpdate;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;
import org.l2jserver.gameserver.util.Util;

public abstract class Skill
{
	protected static final Logger LOGGER = Logger.getLogger(Skill.class.getName());
	
	public static final int SKILL_CUBIC_MASTERY = 143;
	public static final int SKILL_LUCKY = 194;
	public static final int SKILL_CREATE_COMMON = 1320;
	public static final int SKILL_CREATE_DWARVEN = 172;
	public static final int SKILL_CRYSTALLIZE = 248;
	public static final int SKILL_DIVINE_INSPIRATION = 1405;
	
	public static final int SKILL_FAKE_INT = 9001;
	public static final int SKILL_FAKE_WIT = 9002;
	public static final int SKILL_FAKE_MEN = 9003;
	public static final int SKILL_FAKE_CON = 9004;
	public static final int SKILL_FAKE_DEX = 9005;
	public static final int SKILL_FAKE_STR = 9006;
	
	private final int _targetConsumeId;
	private final int _targetConsume;
	
	public enum SkillOpType
	{
		OP_PASSIVE,
		OP_ACTIVE,
		OP_TOGGLE,
		OP_CHANCE
	}
	
	/** Target types of skills : SELF, PARTY, CLAN, PET... */
	public enum SkillTargetType
	{
		TARGET_NONE,
		TARGET_SELF,
		TARGET_ONE,
		TARGET_PARTY,
		TARGET_ALLY,
		TARGET_CLAN,
		TARGET_PET,
		TARGET_AREA,
		TARGET_AURA,
		TARGET_CORPSE,
		TARGET_UNDEAD,
		TARGET_AREA_UNDEAD,
		TARGET_MULTIFACE,
		TARGET_CORPSE_ALLY,
		TARGET_CORPSE_CLAN,
		TARGET_CORPSE_PLAYER,
		TARGET_CORPSE_PET,
		TARGET_ITEM,
		TARGET_AREA_CORPSE_MOB,
		TARGET_CORPSE_MOB,
		TARGET_UNLOCKABLE,
		TARGET_HOLY,
		TARGET_PARTY_MEMBER,
		TARGET_PARTY_OTHER,
		TARGET_ENEMY_SUMMON,
		TARGET_OWNER_PET,
		TARGET_GROUND,
		TARGET_SIEGE,
		TARGET_TYRANNOSAURUS,
		TARGET_AREA_AIM_CORPSE,
		TARGET_CLAN_MEMBER
	}
	
	public enum SkillType
	{
		// Damage
		PDAM,
		MDAM,
		CPDAM,
		MANADAM,
		DOT,
		MDOT,
		DRAIN_SOUL,
		DRAIN(SkillDrain.class),
		DEATHLINK,
		FATALCOUNTER,
		BLOW,
		
		// Disablers
		BLEED,
		POISON,
		STUN,
		ROOT,
		CONFUSION,
		FEAR,
		SLEEP,
		CONFUSE_MOB_ONLY,
		MUTE,
		PARALYZE,
		WEAKNESS,
		
		// hp, mp, cp
		HEAL,
		HOT,
		BALANCE_LIFE,
		HEAL_PERCENT,
		HEAL_STATIC,
		COMBATPOINTHEAL,
		COMBATPOINTPERCENTHEAL,
		CPHOT,
		MANAHEAL,
		MANA_BY_LEVEL,
		MANAHEAL_PERCENT,
		MANARECHARGE,
		MPHOT,
		
		// Aggro
		AGGDAMAGE,
		AGGREDUCE,
		AGGREMOVE,
		AGGREDUCE_CHAR,
		AGGDEBUFF,
		
		// Fishing
		FISHING,
		PUMPING,
		REELING,
		
		// MISC
		UNLOCK,
		ENCHANT_ARMOR,
		ENCHANT_WEAPON,
		SOULSHOT,
		SPIRITSHOT,
		SIEGEFLAG,
		TAKECASTLE,
		DELUXE_KEY_UNLOCK,
		SOW,
		HARVEST,
		GET_PLAYER,
		
		// Creation
		COMMON_CRAFT,
		DWARVEN_CRAFT,
		CREATE_ITEM(SkillCreateItem.class),
		SUMMON_TREASURE_KEY,
		
		// Summons
		SUMMON(SkillSummon.class),
		FEED_PET,
		DEATHLINK_PET,
		STRSIEGEASSAULT,
		ERASE,
		BETRAY,
		
		// Cancel
		CANCEL,
		MAGE_BANE,
		WARRIOR_BANE,
		NEGATE,
		
		BUFF,
		DEBUFF,
		PASSIVE,
		CONT,
		SIGNET(SkillSignet.class),
		SIGNET_CASTTIME(SkillSignetCasttime.class),
		
		RESURRECT,
		CHARGE(SkillCharge.class),
		CHARGE_EFFECT(SkillChargeEffect.class),
		CHARGEDAM(SkillChargeDmg.class),
		MHOT,
		DETECT_WEAKNESS,
		LUCK,
		RECALL,
		SUMMON_FRIEND,
		REFLECT,
		SPOIL,
		SWEEP,
		FAKE_DEATH,
		UNBLEED,
		UNPOISON,
		UNDEAD_DEFENSE,
		SEED(SkillSeed.class),
		BEAST_FEED,
		FORCE_BUFF,
		CLAN_GATE,
		GIVE_SP,
		COREDONE,
		ZAKENPLAYER,
		ZAKENSELF,
		
		// unimplemented
		NOTDONE;
		
		private final Class<? extends Skill> _class;
		
		public Skill makeSkill(StatSet set)
		{
			try
			{
				final Constructor<? extends Skill> c = _class.getConstructor(StatSet.class);
				return c.newInstance(set);
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}
		
		private SkillType()
		{
			_class = SkillDefault.class;
		}
		
		private SkillType(Class<? extends Skill> classType)
		{
			_class = classType;
		}
	}
	
	protected ChanceCondition _chanceCondition = null;
	// elements
	public static final int ELEMENT_WIND = 1;
	public static final int ELEMENT_FIRE = 2;
	public static final int ELEMENT_WATER = 3;
	public static final int ELEMENT_EARTH = 4;
	public static final int ELEMENT_HOLY = 5;
	public static final int ELEMENT_DARK = 6;
	
	// stat effected
	public static final int STAT_PATK = 301; // pAtk
	public static final int STAT_PDEF = 302; // pDef
	public static final int STAT_MATK = 303; // mAtk
	public static final int STAT_MDEF = 304; // mDef
	public static final int STAT_MAXHP = 305; // maxHp
	public static final int STAT_MAXMP = 306; // maxMp
	public static final int STAT_CURHP = 307;
	public static final int STAT_CURMP = 308;
	public static final int STAT_HPREGEN = 309; // regHp
	public static final int STAT_MPREGEN = 310; // regMp
	public static final int STAT_CASTINGSPEED = 311; // sCast
	public static final int STAT_ATKSPD = 312; // sAtk
	public static final int STAT_CRITDAM = 313; // critDmg
	public static final int STAT_CRITRATE = 314; // critRate
	public static final int STAT_FIRERES = 315; // fireRes
	public static final int STAT_WINDRES = 316; // windRes
	public static final int STAT_WATERRES = 317; // waterRes
	public static final int STAT_EARTHRES = 318; // earthRes
	public static final int STAT_HOLYRES = 336; // holyRes
	public static final int STAT_DARKRES = 337; // darkRes
	public static final int STAT_ROOTRES = 319; // rootRes
	public static final int STAT_SLEEPRES = 320; // sleepRes
	public static final int STAT_CONFUSIONRES = 321; // confusRes
	public static final int STAT_BREATH = 322; // breath
	public static final int STAT_AGGRESSION = 323; // aggr
	public static final int STAT_BLEED = 324; // bleed
	public static final int STAT_POISON = 325; // poison
	public static final int STAT_STUN = 326; // stun
	public static final int STAT_ROOT = 327; // root
	public static final int STAT_MOVEMENT = 328; // move
	public static final int STAT_EVASION = 329; // evas
	public static final int STAT_ACCURACY = 330; // accu
	public static final int STAT_COMBAT_STRENGTH = 331;
	public static final int STAT_COMBAT_WEAKNESS = 332;
	public static final int STAT_ATTACK_RANGE = 333; // rAtk
	public static final int STAT_NOAGG = 334; // noagg
	public static final int STAT_SHIELDDEF = 335; // sDef
	public static final int STAT_MP_CONSUME_RATE = 336; // Rate of mp consume per skill use
	public static final int STAT_HP_CONSUME_RATE = 337; // Rate of hp consume per skill use
	public static final int STAT_MCRITRATE = 338; // Magic Crit Rate
	
	// COMBAT DAMAGE MODIFIER SKILLS...DETECT WEAKNESS AND WEAKNESS/STRENGTH
	public static final int COMBAT_MOD_ANIMAL = 200;
	public static final int COMBAT_MOD_BEAST = 201;
	public static final int COMBAT_MOD_BUG = 202;
	public static final int COMBAT_MOD_DRAGON = 203;
	public static final int COMBAT_MOD_MONSTER = 204;
	public static final int COMBAT_MOD_PLANT = 205;
	public static final int COMBAT_MOD_HOLY = 206;
	public static final int COMBAT_MOD_UNHOLY = 207;
	public static final int COMBAT_MOD_BOW = 208;
	public static final int COMBAT_MOD_BLUNT = 209;
	public static final int COMBAT_MOD_DAGGER = 210;
	public static final int COMBAT_MOD_FIST = 211;
	public static final int COMBAT_MOD_DUAL = 212;
	public static final int COMBAT_MOD_SWORD = 213;
	public static final int COMBAT_MOD_POISON = 214;
	public static final int COMBAT_MOD_BLEED = 215;
	public static final int COMBAT_MOD_FIRE = 216;
	public static final int COMBAT_MOD_WATER = 217;
	public static final int COMBAT_MOD_EARTH = 218;
	public static final int COMBAT_MOD_WIND = 219;
	public static final int COMBAT_MOD_ROOT = 220;
	public static final int COMBAT_MOD_STUN = 221;
	public static final int COMBAT_MOD_CONFUSION = 222;
	public static final int COMBAT_MOD_DARK = 223;
	
	// conditional values
	public static final int COND_RUNNING = 0x0001;
	public static final int COND_WALKING = 0x0002;
	public static final int COND_SIT = 0x0004;
	public static final int COND_BEHIND = 0x0008;
	public static final int COND_CRIT = 0x0010;
	public static final int COND_LOWHP = 0x0020;
	public static final int COND_ROBES = 0x0040;
	public static final int COND_CHARGES = 0x0080;
	public static final int COND_SHIELD = 0x0100;
	public static final int COND_GRADEA = 0x010000;
	public static final int COND_GRADEB = 0x020000;
	public static final int COND_GRADEC = 0x040000;
	public static final int COND_GRADED = 0x080000;
	public static final int COND_GRADES = 0x100000;
	
	private static final Func[] _emptyFunctionSet = new Func[0];
	private static final Effect[] _emptyEffectSet = new Effect[0];
	
	// these two build the primary key
	private final int _id;
	private final int _level;
	
	/** Identifier for a skill that client can't display */
	private int _displayId;
	
	// not needed, just for easier debug
	private final String _name;
	private final SkillOpType _operateType;
	private final boolean _magic;
	private final boolean _staticReuse;
	private final boolean _staticHitTime;
	private final int _mpConsume;
	private final int _mpInitialConsume;
	private final int _hpConsume;
	private final int _itemConsume;
	private final int _itemConsumeId;
	// item consume count over time
	protected int _itemConsumeOT;
	// item consume id over time
	protected int _itemConsumeIdOT;
	// how many times to consume an item
	protected int _itemConsumeSteps;
	// for summon spells:
	// a) What is the total lifetime of summons (in millisecs)
	private final int _summonTotalLifeTime;
	// b) how much lifetime is lost per second of idleness (non-fighting)
	protected int _summonTimeLostIdle;
	// c) how much time is lost per second of activity (fighting)
	protected int _summonTimeLostActive;
	
	// item consume time in milliseconds
	protected int _itemConsumeTime;
	private final int _castRange;
	private final int _effectRange;
	
	// all times in milliseconds
	private final int _hitTime;
	// private final int _skillInterruptTime;
	private final int _coolTime;
	private final int _reuseDelay;
	private final int _buffDuration;
	
	/** Target type of the skill : SELF, PARTY, CLAN, PET... */
	private final SkillTargetType _targetType;
	
	private final double _power;
	private final int _effectPoints;
	private final int _magicLevel;
	private final String[] _negateSkillTypes;
	private final String[] _negateEffectTypes;
	private final float _negatePower;
	private final int _negateId;
	private final int _levelDepend;
	
	// Effecting area of the skill, in radius.
	// The radius center varies according to the _targetType:
	// "caster" if targetType = AURA/PARTY/CLAN or "target" if targetType = AREA
	private final int _skillRadius;
	
	private final SkillType _skillType;
	private final SkillType _effectType;
	private final int _effectPower;
	private final int _effectId;
	private final int _effectLvl;
	
	private final boolean _ispotion;
	private final int _element;
	private final BaseStat _saveVs;
	
	private final boolean _isSuicideAttack;
	
	private final Stat _stat;
	
	private final int _condition;
	private final int _conditionValue;
	private final boolean _overhit;
	private final int _weaponsAllowed;
	private final int _armorsAllowed;
	
	private final int _addCrossLearn; // -1 disable, otherwice SP price for others classes, default 1000
	private final float _mulCrossLearn; // multiplay for others classes, default 2
	private final float _mulCrossLearnRace; // multiplay for others races, default 2
	private final float _mulCrossLearnProf; // multiplay for fighter/mage missmatch, default 3
	private final List<ClassId> _canLearn; // which classes can learn
	private final List<Integer> _teachers; // which NPC teaches
	private final int _minPledgeClass;
	
	private final boolean _isOffensive;
	private final int _numCharges;
	private final int _triggeredId;
	private final int _triggeredLevel;
	
	private final boolean _bestowed;
	
	private final boolean _isHeroSkill; // If true the skill is a Hero Skill
	
	private final int _baseCritRate; // percent of success for skill critical hit (especially for PDAM & BLOW - they're not affected by rCrit values or buffs). Default loads -1 for all other skills but 0 to PDAM & BLOW
	private final int _lethalEffect1; // percent of success for lethal 1st effect (hit cp to 1 or if mob hp to 50%) (only for PDAM skills)
	private final int _lethalEffect2; // percent of success for lethal 2nd effect (hit cp,hp to 1 or if mob hp to 1) (only for PDAM skills)
	private final boolean _directHpDmg; // If true then dmg is being make directly
	private final boolean _isDance; // If true then casting more dances will cost more MP
	private final int _nextDanceCost;
	private final float _sSBoost; // If true skill will have SoulShot boost (power*2)
	private final int _aggroPoints;
	
	private final float _pvpMulti;
	
	protected Condition _preCondition;
	protected Condition _itemPreCondition;
	protected FuncTemplate[] _funcTemplates;
	private EffectTemplate[] _effectTemplates;
	protected EffectTemplate[] _effectTemplatesSelf;
	
	private final boolean _nextActionIsAttack;
	
	private final int _minChance;
	private final int _maxChance;
	
	private final boolean _singleEffect;
	
	private final boolean _isDebuff;
	
	private final boolean _advancedFlag;
	private final int _advancedMultiplier;
	
	protected Skill(StatSet set)
	{
		_id = set.getInt("skill_id", 0);
		_level = set.getInt("level", 1);
		_advancedFlag = set.getBoolean("advancedFlag", false);
		_advancedMultiplier = set.getInt("advancedMultiplier", 1);
		_displayId = set.getInt("displayId", _id);
		_name = set.getString("name");
		_operateType = set.getEnum("operateType", SkillOpType.class);
		_magic = set.getBoolean("isMagic", false);
		_staticReuse = set.getBoolean("staticReuse", false);
		_staticHitTime = set.getBoolean("staticHitTime", false);
		_ispotion = set.getBoolean("isPotion", false);
		_mpConsume = set.getInt("mpConsume", 0);
		_mpInitialConsume = set.getInt("mpInitialConsume", 0);
		_hpConsume = set.getInt("hpConsume", 0);
		_itemConsume = set.getInt("itemConsumeCount", 0);
		_itemConsumeId = set.getInt("itemConsumeId", 0);
		_itemConsumeOT = set.getInt("itemConsumeCountOT", 0);
		_itemConsumeIdOT = set.getInt("itemConsumeIdOT", 0);
		_itemConsumeTime = set.getInt("itemConsumeTime", 0);
		_itemConsumeSteps = set.getInt("itemConsumeSteps", 0);
		_summonTotalLifeTime = set.getInt("summonTotalLifeTime", 1200000); // 20 minutes default
		_summonTimeLostIdle = set.getInt("summonTimeLostIdle", 0);
		_summonTimeLostActive = set.getInt("summonTimeLostActive", 0);
		_castRange = set.getInt("castRange", 0);
		_effectRange = set.getInt("effectRange", -1);
		_hitTime = set.getInt("hitTime", 0);
		_coolTime = set.getInt("coolTime", 0);
		// _skillInterruptTime = set.getInteger("hitTime", _hitTime / 2);
		_reuseDelay = set.getInt("reuseDelay", 0);
		_buffDuration = set.getInt("buffDuration", 0);
		_skillRadius = set.getInt("skillRadius", 80);
		_targetType = set.getEnum("target", SkillTargetType.class);
		_power = set.getFloat("power", 0.f);
		_effectPoints = set.getInt("effectPoints", 0);
		_negateSkillTypes = set.getString("negateSkillTypes", "").split(" ");
		_negateEffectTypes = set.getString("negateEffectTypes", "").split(" ");
		_negatePower = set.getFloat("negatePower", 0.f);
		_negateId = set.getInt("negateId", 0);
		_magicLevel = set.getInt("magicLvl", SkillTreeTable.getInstance().getMinSkillLevel(_id, _level));
		_levelDepend = set.getInt("lvlDepend", 0);
		_stat = set.getEnum("stat", Stat.class, null);
		_skillType = set.getEnum("skillType", SkillType.class);
		_effectType = set.getEnum("effectType", SkillType.class, null);
		_effectPower = set.getInt("effectPower", 0);
		_effectId = set.getInt("effectId", 0);
		_effectLvl = set.getInt("effectLevel", 0);
		_element = set.getInt("element", 0);
		_saveVs = set.getEnum("saveVs", BaseStat.class, null);
		_condition = set.getInt("condition", 0);
		_conditionValue = set.getInt("conditionValue", 0);
		_overhit = set.getBoolean("overHit", false);
		_isSuicideAttack = set.getBoolean("isSuicideAttack", false);
		_weaponsAllowed = set.getInt("weaponsAllowed", 0);
		_armorsAllowed = set.getInt("armorsAllowed", 0);
		_addCrossLearn = set.getInt("addCrossLearn", 1000);
		_mulCrossLearn = set.getFloat("mulCrossLearn", 2.f);
		_mulCrossLearnRace = set.getFloat("mulCrossLearnRace", 2.f);
		_mulCrossLearnProf = set.getFloat("mulCrossLearnProf", 3.f);
		_minPledgeClass = set.getInt("minPledgeClass", 0);
		_isOffensive = set.getBoolean("offensive", isSkillTypeOffensive());
		_numCharges = set.getInt("num_charges", 0);
		_triggeredId = set.getInt("triggeredId", 0);
		_triggeredLevel = set.getInt("triggeredLevel", 0);
		_bestowed = set.getBoolean("bestowed", false);
		_targetConsume = set.getInt("targetConsumeCount", 0);
		_targetConsumeId = set.getInt("targetConsumeId", 0);
		if (_operateType == SkillOpType.OP_CHANCE)
		{
			_chanceCondition = ChanceCondition.parse(set);
		}
		
		_isHeroSkill = HeroSkillTable.isHeroSkill(_id);
		_baseCritRate = set.getInt("baseCritRate", (_skillType == SkillType.PDAM) || (_skillType == SkillType.BLOW) ? 0 : -1);
		_lethalEffect1 = set.getInt("lethal1", 0);
		_lethalEffect2 = set.getInt("lethal2", 0);
		_directHpDmg = set.getBoolean("dmgDirectlyToHp", false);
		_isDance = set.getBoolean("isDance", false);
		_nextDanceCost = set.getInt("nextDanceCost", 0);
		_sSBoost = set.getFloat("SSBoost", 0.f);
		_aggroPoints = set.getInt("aggroPoints", 0);
		_pvpMulti = set.getFloat("pvpMulti", 1.f);
		_nextActionIsAttack = set.getBoolean("nextActionAttack", false);
		_minChance = set.getInt("minChance", 1);
		_maxChance = set.getInt("maxChance", 99);
		
		final String canLearn = set.getString("canLearn", null);
		if (canLearn == null)
		{
			_canLearn = null;
		}
		else
		{
			_canLearn = new ArrayList<>();
			final StringTokenizer st = new StringTokenizer(canLearn, " \r\n\t,;");
			
			while (st.hasMoreTokens())
			{
				final String cls = st.nextToken();
				try
				{
					_canLearn.add(ClassId.valueOf(cls));
				}
				catch (Throwable t)
				{
					LOGGER.warning("Bad class " + cls + " to learn skill " + t);
				}
			}
		}
		
		final String teachers = set.getString("teachers", null);
		if (teachers == null)
		{
			_teachers = null;
		}
		else
		{
			_teachers = new ArrayList<>();
			final StringTokenizer st = new StringTokenizer(teachers, " \r\n\t,;");
			while (st.hasMoreTokens())
			{
				final String npcid = st.nextToken();
				try
				{
					_teachers.add(Integer.parseInt(npcid));
				}
				catch (Throwable t)
				{
					LOGGER.warning("Bad teacher id " + npcid + " to teach skill " + t);
				}
			}
		}
		
		_singleEffect = set.getBoolean("singleEffect", false);
		_isDebuff = set.getBoolean("isDebuff", false);
	}
	
	public abstract void useSkill(Creature caster, WorldObject[] targets);
	
	public boolean isSingleEffect()
	{
		return _singleEffect;
	}
	
	public boolean isDebuff()
	{
		boolean typeDebuff = false;
		
		switch (_skillType)
		{
			case AGGDEBUFF:
			case DEBUFF:
			case STUN:
			case BLEED:
			case CONFUSION:
			case FEAR:
			case PARALYZE:
			case SLEEP:
			case ROOT:
			case POISON:
			case MUTE:
			case WEAKNESS:
			{
				typeDebuff = true;
			}
		}
		return _isDebuff || typeDebuff;
	}
	
	/**
	 * @return true if character should attack target after skill
	 */
	public boolean nextActionIsAttack()
	{
		return _nextActionIsAttack;
	}
	
	public boolean isPotion()
	{
		return _ispotion;
	}
	
	public int getArmorsAllowed()
	{
		return _armorsAllowed;
	}
	
	public int getConditionValue()
	{
		return _conditionValue;
	}
	
	public SkillType getSkillType()
	{
		return _skillType;
	}
	
	public boolean hasEffectWhileCasting()
	{
		return _skillType == SkillType.SIGNET_CASTTIME;
	}
	
	public BaseStat getSavevs()
	{
		return _saveVs;
	}
	
	public int getElement()
	{
		return _element;
	}
	
	/**
	 * @return the target type of the skill : SELF, PARTY, CLAN, PET...
	 */
	public SkillTargetType getTargetType()
	{
		return _targetType;
	}
	
	public int getCondition()
	{
		return _condition;
	}
	
	public boolean isOverhit()
	{
		return _overhit;
	}
	
	public boolean isSuicideAttack()
	{
		return _isSuicideAttack;
	}
	
	/**
	 * @param creature
	 * @return the power of the skill.
	 */
	public double getPower(Creature creature)
	{
		if ((_skillType == SkillType.FATALCOUNTER) && (creature != null))
		{
			return _power * 3.5 * (1 - (creature.getCurrentHp() / creature.getMaxHp()));
		}
		return _power;
	}
	
	public double getPower()
	{
		return _power;
	}
	
	public int getEffectPoints()
	{
		return _effectPoints;
	}
	
	public String[] getNegateSkillTypes()
	{
		return _negateSkillTypes;
	}
	
	public String[] getNegateEffectTypes()
	{
		return _negateEffectTypes;
	}
	
	public float getNegatePower()
	{
		return _negatePower;
	}
	
	public int getNegateId()
	{
		return _negateId;
	}
	
	public int getMagicLevel()
	{
		return _magicLevel;
	}
	
	/**
	 * @return Returns true to set static reuse.
	 */
	public boolean isStaticReuse()
	{
		return _staticReuse;
	}
	
	/**
	 * @return Returns true to set static hittime.
	 */
	public boolean isStaticHitTime()
	{
		return _staticHitTime;
	}
	
	public int getLevelDepend()
	{
		return _levelDepend;
	}
	
	/**
	 * @return the additional effect power or base probability.
	 */
	public int getEffectPower()
	{
		return _effectPower;
	}
	
	/**
	 * @return the additional effect Id.
	 */
	public int getEffectId()
	{
		return _effectId;
	}
	
	/**
	 * @return the additional effect level.
	 */
	public int getEffectLvl()
	{
		return _effectLvl;
	}
	
	/**
	 * @return the additional effect skill type (ex : STUN, PARALYZE,...).
	 */
	public SkillType getEffectType()
	{
		return _effectType;
	}
	
	/**
	 * @return Returns the buffDuration.
	 */
	public int getBuffDuration()
	{
		return _buffDuration;
	}
	
	/**
	 * @return Returns the castRange.
	 */
	public int getCastRange()
	{
		return _castRange;
	}
	
	/**
	 * @return Returns the effectRange.
	 */
	public int getEffectRange()
	{
		return _effectRange;
	}
	
	/**
	 * @return Returns the hpConsume.
	 */
	public int getHpConsume()
	{
		return _hpConsume;
	}
	
	/**
	 * @return Returns the id.
	 */
	public int getId()
	{
		return _id;
	}
	
	public int getDisplayId()
	{
		return _displayId;
	}
	
	public void setDisplayId(int id)
	{
		_displayId = id;
	}
	
	public int getTriggeredId()
	{
		return _triggeredId;
	}
	
	public int getTriggeredLevel()
	{
		return _triggeredLevel;
	}
	
	/**
	 * @return the skill type (ex : BLEED, SLEEP, WATER...).
	 */
	public Stat getStat()
	{
		return _stat;
	}
	
	/**
	 * @return Returns the itemConsume.
	 */
	public int getItemConsume()
	{
		return _itemConsume;
	}
	
	/**
	 * @return Returns the itemConsumeId.
	 */
	public int getItemConsumeId()
	{
		return _itemConsumeId;
	}
	
	/**
	 * @return Returns the itemConsume count over time.
	 */
	public int getItemConsumeOT()
	{
		return _itemConsumeOT;
	}
	
	/**
	 * @return Returns the itemConsumeId over time.
	 */
	public int getItemConsumeIdOT()
	{
		return _itemConsumeIdOT;
	}
	
	/**
	 * @return Returns the itemConsume count over time.
	 */
	public int getItemConsumeSteps()
	{
		return _itemConsumeSteps;
	}
	
	/**
	 * @return Returns the itemConsume count over time.
	 */
	public int getTotalLifeTime()
	{
		return _summonTotalLifeTime;
	}
	
	/**
	 * @return Returns the itemConsume count over time.
	 */
	public int getTimeLostIdle()
	{
		return _summonTimeLostIdle;
	}
	
	/**
	 * @return Returns the itemConsumeId over time.
	 */
	public int getTimeLostActive()
	{
		return _summonTimeLostActive;
	}
	
	/**
	 * @return Returns the itemConsume time in milliseconds.
	 */
	public int getItemConsumeTime()
	{
		return _itemConsumeTime;
	}
	
	/**
	 * @return Returns the level.
	 */
	public int getLevel()
	{
		return _level;
	}
	
	/**
	 * @return Returns the magic.
	 */
	public boolean isMagic()
	{
		return _magic;
	}
	
	/**
	 * @return Returns the mpConsume.
	 */
	public int getMpConsume()
	{
		return _mpConsume;
	}
	
	/**
	 * @return Returns the mpInitialConsume.
	 */
	public int getMpInitialConsume()
	{
		return _mpInitialConsume;
	}
	
	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		return _name;
	}
	
	/**
	 * @return Returns the reuseDelay.
	 */
	public int getReuseDelay()
	{
		return _reuseDelay;
	}
	
	public int getHitTime()
	{
		return _hitTime;
	}
	
	/**
	 * @return Returns the coolTime.
	 */
	public int getCoolTime()
	{
		return _coolTime;
	}
	
	public int getSkillRadius()
	{
		return _skillRadius;
	}
	
	public boolean isActive()
	{
		return _operateType == SkillOpType.OP_ACTIVE;
	}
	
	public boolean isPassive()
	{
		return _operateType == SkillOpType.OP_PASSIVE;
	}
	
	public boolean isToggle()
	{
		return _operateType == SkillOpType.OP_TOGGLE;
	}
	
	public boolean isChance()
	{
		return _operateType == SkillOpType.OP_CHANCE;
	}
	
	public ChanceCondition getChanceCondition()
	{
		return _chanceCondition;
	}
	
	public boolean isDance()
	{
		return _isDance;
	}
	
	public int getNextDanceMpCost()
	{
		return _nextDanceCost;
	}
	
	public float getSSBoost()
	{
		return _sSBoost;
	}
	
	public int getAggroPoints()
	{
		return _aggroPoints;
	}
	
	public float getPvpMulti()
	{
		return _pvpMulti;
	}
	
	public boolean useSoulShot()
	{
		return (_skillType == SkillType.PDAM) || (_skillType == SkillType.STUN) || (_skillType == SkillType.CHARGEDAM) || (_skillType == SkillType.BLOW);
	}
	
	public boolean useSpiritShot()
	{
		return _magic;
	}
	
	public boolean useFishShot()
	{
		return (_skillType == SkillType.PUMPING) || (_skillType == SkillType.REELING);
	}
	
	public int getWeaponsAllowed()
	{
		return _weaponsAllowed;
	}
	
	public int getCrossLearnAdd()
	{
		return _addCrossLearn;
	}
	
	public float getCrossLearnMul()
	{
		return _mulCrossLearn;
	}
	
	public float getCrossLearnRace()
	{
		return _mulCrossLearnRace;
	}
	
	public float getCrossLearnProf()
	{
		return _mulCrossLearnProf;
	}
	
	public boolean getCanLearn(ClassId cls)
	{
		return (_canLearn == null) || _canLearn.contains(cls);
	}
	
	public boolean canTeachBy(int npcId)
	{
		return (_teachers == null) || _teachers.contains(npcId);
	}
	
	public int getMinPledgeClass()
	{
		return _minPledgeClass;
	}
	
	public boolean isPvpSkill()
	{
		switch (_skillType)
		{
			case DOT:
			case AGGREDUCE:
			case AGGDAMAGE:
			case AGGREDUCE_CHAR:
			case CONFUSE_MOB_ONLY:
			case BLEED:
			case CONFUSION:
			case POISON:
			case DEBUFF:
			case AGGDEBUFF:
			case STUN:
			case ROOT:
			case FEAR:
			case SLEEP:
			case MDOT:
			case MANADAM:
			case MUTE:
			case WEAKNESS:
			case PARALYZE:
			case CANCEL:
			case MAGE_BANE:
			case WARRIOR_BANE:
			case FATALCOUNTER:
			case BETRAY:
			{
				return true;
			}
			default:
			{
				return false;
			}
		}
	}
	
	public boolean isOffensive()
	{
		return _isOffensive;
	}
	
	public boolean isHeroSkill()
	{
		return _isHeroSkill;
	}
	
	public int getNumCharges()
	{
		return _numCharges;
	}
	
	public int getBaseCritRate()
	{
		return _baseCritRate;
	}
	
	public int getLethalChance1()
	{
		return _lethalEffect1;
	}
	
	public int getLethalChance2()
	{
		return _lethalEffect2;
	}
	
	public boolean getDmgDirectlyToHP()
	{
		return _directHpDmg;
	}
	
	public boolean bestowed()
	{
		return _bestowed;
	}
	
	public boolean triggerAnotherSkill()
	{
		return _triggeredId > 1;
	}
	
	public boolean isSkillTypeOffensive()
	{
		switch (_skillType)
		{
			case PDAM:
			case MDAM:
			case CPDAM:
			case DOT:
			case BLEED:
			case POISON:
			case AGGDAMAGE:
			case DEBUFF:
			case AGGDEBUFF:
			case STUN:
			case ROOT:
			case CONFUSION:
			case ERASE:
			case BLOW:
			case FEAR:
			case DRAIN:
			case SLEEP:
			case CHARGEDAM:
			case CONFUSE_MOB_ONLY:
			case DEATHLINK:
			case DETECT_WEAKNESS:
			case MANADAM:
			case MDOT:
			case MUTE:
			case SOULSHOT:
			case SPIRITSHOT:
			case SPOIL:
			case WEAKNESS:
			case MANA_BY_LEVEL:
			case SWEEP:
			case PARALYZE:
			case DRAIN_SOUL:
			case AGGREDUCE:
			case CANCEL:
			case MAGE_BANE:
			case WARRIOR_BANE:
			case AGGREMOVE:
			case AGGREDUCE_CHAR:
			case FATALCOUNTER:
			case BETRAY:
			case DELUXE_KEY_UNLOCK:
			case SOW:
			case HARVEST:
			{
				return true;
			}
			default:
			{
				return false;
			}
		}
	}
	
	public boolean getWeaponDependancy(Creature creature)
	{
		if (calcWeaponDependancy(creature))
		{
			return true;
		}
		final SystemMessage message = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS);
		message.addSkillName(_id);
		creature.sendPacket(message);
		
		return false;
	}
	
	public boolean calcWeaponDependancy(Creature creature)
	{
		final int weaponsAllowed = _weaponsAllowed;
		// check to see if skill has a weapon dependency.
		if (weaponsAllowed == 0)
		{
			return true;
		}
		
		int mask = 0;
		if (creature.getActiveWeaponItem() != null)
		{
			mask |= creature.getActiveWeaponItem().getItemType().mask();
		}
		if (creature.getSecondaryWeaponItem() != null)
		{
			mask |= creature.getSecondaryWeaponItem().getItemType().mask();
		}
		
		if ((mask & weaponsAllowed) != 0)
		{
			return true;
		}
		
		return false;
	}
	
	public boolean checkCondition(Creature creature, WorldObject target, boolean itemOrWeapon)
	{
		Condition preCondition = _preCondition;
		if (itemOrWeapon)
		{
			preCondition = _itemPreCondition;
		}
		
		if (preCondition == null)
		{
			return true;
		}
		
		final Env env = new Env();
		env.player = creature;
		if (target instanceof Creature)
		{
			env.target = (Creature) target;
		}
		
		env.skill = this;
		if (!preCondition.test(env))
		{
			final String msg = preCondition.getMessage();
			if (msg != null)
			{
				creature.sendMessage(msg);
			}
			
			return false;
		}
		
		return true;
	}
	
	public WorldObject[] getTargetList(Creature creature, boolean onlyFirst)
	{
		// Init to null the target of the skill
		Creature target = null;
		
		// Get the L2Objcet targeted by the user of the skill at this moment
		final WorldObject objTarget = creature.getTarget();
		// If the WorldObject targeted is a Creature, it becomes the Creature target
		if (objTarget instanceof Creature)
		{
			target = (Creature) objTarget;
		}
		return getTargetList(creature, onlyFirst, target);
	}
	
	/**
	 * Return all targets of the skill in a table in function a the skill type.<br>
	 * <br>
	 * <b><u>Values of skill type</u>:</b><br>
	 * <li>ONE : The skill can only be used on the PlayerInstance targeted, or on the caster if it's a PlayerInstance and no PlayerInstance targeted</li>
	 * <li>SELF</li>
	 * <li>HOLY, UNDEAD</li>
	 * <li>PET</li>
	 * <li>AURA, AURA_CLOSE</li>
	 * <li>AREA</li>
	 * <li>MULTIFACE</li>
	 * <li>PARTY, CLAN</li>
	 * <li>CORPSE_PLAYER, CORPSE_MOB, CORPSE_CLAN</li>
	 * <li>UNLOCKABLE</li>
	 * <li>ITEM</li><br>
	 * @param creature The Creature who use the skill
	 * @param onlyFirst
	 * @param target
	 * @return
	 */
	public WorldObject[] getTargetList(Creature creature, boolean onlyFirst, Creature target)
	{
		// to avoid attacks during oly start period
		if ((creature instanceof PlayerInstance) && _isOffensive && (((PlayerInstance) creature).isInOlympiadMode() && !((PlayerInstance) creature).isOlympiadStart()))
		{
			creature.sendPacket(new SystemMessage(SystemMessageId.THAT_IS_THE_INCORRECT_TARGET));
			return null;
		}
		
		final List<Creature> targetList = new ArrayList<>();
		if (_ispotion)
		{
			return new Creature[]
			{
				creature
			};
		}
		
		switch (_targetType)
		{
			// The skill can only be used on the Creature targeted, or on the caster itself
			case TARGET_ONE:
			{
				boolean canTargetSelf = false;
				switch (_skillType)
				{
					case BUFF:
					case HEAL:
					case HOT:
					case HEAL_PERCENT:
					case MANARECHARGE:
					case MANAHEAL:
					case NEGATE:
					case REFLECT:
					case UNBLEED:
					case UNPOISON: // case CANCEL:
					case SEED:
					case COMBATPOINTHEAL:
					case COMBATPOINTPERCENTHEAL:
					case MAGE_BANE:
					case WARRIOR_BANE:
					case BETRAY:
					case BALANCE_LIFE:
					case FORCE_BUFF:
					{
						canTargetSelf = true;
						break;
					}
				}
				
				switch (_skillType)
				{
					case CONFUSION:
					case DEBUFF:
					case STUN:
					case ROOT:
					case FEAR:
					case SLEEP:
					case MUTE:
					case WEAKNESS:
					case PARALYZE:
					case CANCEL:
					case MAGE_BANE:
					case WARRIOR_BANE:
					{
						if (checkPartyClan(creature, target))
						{
							creature.sendPacket(new SystemMessage(SystemMessageId.THAT_IS_THE_INCORRECT_TARGET));
							return null;
						}
						break;
					}
				}
				
				switch (_skillType)
				{
					case AGGDEBUFF:
					case DEBUFF:
					case BLEED:
					case CONFUSION:
					case FEAR:
					case PARALYZE:
					case SLEEP:
					case ROOT:
					case WEAKNESS:
					case MUTE:
					case CANCEL:
					case DOT:
					case POISON:
					case AGGREDUCE_CHAR:
					case AGGDAMAGE:
					case AGGREMOVE:
					case MANADAM:
					{
						// Like L2OFF if the skills is TARGET_ONE (skillType) can't be used on Npc
						if ((target instanceof NpcInstance) && !(target instanceof MonsterInstance))
						{
							creature.sendPacket(new SystemMessage(SystemMessageId.THAT_IS_THE_INCORRECT_TARGET));
							return null;
						}
						break;
					}
				}
				
				// Like L2OFF Shield stun can't be used on Npc
				if ((_id == 92) && (target instanceof NpcInstance) && !(target instanceof MonsterInstance))
				{
					creature.sendPacket(new SystemMessage(SystemMessageId.THAT_IS_THE_INCORRECT_TARGET));
					return null;
				}
				
				// Check for null target or any other invalid target
				if ((target == null) || target.isDead() || ((target == creature) && !canTargetSelf))
				{
					creature.sendPacket(new SystemMessage(SystemMessageId.THAT_IS_THE_INCORRECT_TARGET));
					return null;
				}
				
				// If a target is found, return it in a table else send a system message TARGET_IS_INCORRECT
				return new Creature[]
				{
					target
				};
			}
			case TARGET_SELF:
			case TARGET_GROUND:
			{
				return new Creature[]
				{
					creature
				};
			}
			case TARGET_HOLY:
			{
				if ((creature instanceof PlayerInstance) && (creature.getTarget() instanceof ArtefactInstance))
				{
					return new Creature[]
					{
						(ArtefactInstance) creature.getTarget()
					};
				}
				return null;
			}
			
			case TARGET_PET:
			{
				target = creature.getPet();
				if ((target != null) && !target.isDead())
				{
					return new Creature[]
					{
						target
					};
				}
				return null;
			}
			case TARGET_OWNER_PET:
			{
				if (creature instanceof Summon)
				{
					target = ((Summon) creature).getOwner();
					if ((target != null) && !target.isDead())
					{
						return new Creature[]
						{
							target
						};
					}
				}
				return null;
			}
			case TARGET_CORPSE_PET:
			{
				if (creature instanceof PlayerInstance)
				{
					target = creature.getPet();
					if ((target != null) && target.isDead())
					{
						return new Creature[]
						{
							target
						};
					}
				}
				return null;
			}
			case TARGET_AURA:
			{
				final int radius = _skillRadius;
				final boolean srcInArena = creature.isInsideZone(ZoneId.PVP) && !creature.isInsideZone(ZoneId.SIEGE);
				PlayerInstance src = null;
				if (creature instanceof PlayerInstance)
				{
					src = (PlayerInstance) creature;
				}
				if (creature instanceof Summon)
				{
					src = ((Summon) creature).getOwner();
				}
				// Go through the Creature _knownList
				for (Creature nearby : creature.getKnownList().getKnownCharactersInRadius(radius))
				{
					if ((nearby == null) || (!(creature instanceof Playable) && !(nearby instanceof Playable)))
					{
						continue;
					}
					// Like L2OFF you can cast the skill on peace zone but hasn't any effect
					if (_isOffensive && Creature.isInsidePeaceZone(target, creature))
					{
						continue;
					}
					if ((src != null) && ((nearby instanceof Attackable) || (nearby instanceof Playable)))
					{
						// Don't add this target if this is a Pc->Pc pvp casting and pvp condition not met
						if ((nearby == creature) || (nearby == src))
						{
							continue;
						}
						if (!GeoEngine.getInstance().canSeeTarget(creature, nearby))
						{
							continue;
						}
						// check if both attacker and target are PlayerInstances and if they are in same party
						if (nearby instanceof PlayerInstance)
						{
							if (((PlayerInstance) nearby).isDead())
							{
								continue;
							}
							if (((PlayerInstance) nearby).getAppearance().isInvisible())
							{
								continue;
							}
							if (!src.checkPvpSkill(nearby, this))
							{
								continue;
							}
							if (!srcInArena && (!nearby.isInsideZone(ZoneId.PVP) || nearby.isInsideZone(ZoneId.SIEGE)))
							{
								if (checkPartyClan(src, nearby))
								{
									continue;
								}
								if ((src.getAllyId() != 0) && (src.getAllyId() == ((PlayerInstance) nearby).getAllyId()))
								{
									continue;
								}
							}
						}
						if (nearby instanceof Summon)
						{
							final PlayerInstance trg = ((Summon) nearby).getOwner();
							if (trg == null)
							{
								continue;
							}
							if (trg == src)
							{
								continue;
							}
							if (!src.checkPvpSkill(trg, this))
							{
								continue;
							}
							if ((src.getParty() != null) && (trg.getParty() != null) && (src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID()))
							{
								continue;
							}
							if (!srcInArena && (!nearby.isInsideZone(ZoneId.PVP) || nearby.isInsideZone(ZoneId.SIEGE)))
							{
								if (checkPartyClan(src, nearby))
								{
									continue;
								}
								if ((src.getAllyId() != 0) && (src.getAllyId() == trg.getAllyId()))
								{
									continue;
								}
							}
						}
					}
					if (!Util.checkIfInRange(radius, creature, nearby, true))
					{
						continue;
					}
					if (!onlyFirst)
					{
						targetList.add(nearby);
					}
					else
					{
						return new Creature[]
						{
							nearby
						};
					}
				}
				return targetList.toArray(new Creature[targetList.size()]);
			}
			case TARGET_AREA:
			{
				// Like L2OFF players can use TARGET_AREA skills on NPC in peacezone
				if ((!(target instanceof Attackable) && !(target instanceof Playable) && !(target instanceof NpcInstance)) || // Target is not Attackable or PlayableInstance or NpcInstance
					((_castRange >= 0) && ((target == creature) || target.isAlikeDead()))) // target is null or self or dead/faking
				{
					creature.sendPacket(new SystemMessage(SystemMessageId.THAT_IS_THE_INCORRECT_TARGET));
					return null;
				}
				Creature cha;
				if (_castRange >= 0)
				{
					cha = target;
					if (!onlyFirst)
					{
						targetList.add(cha); // Add target to target list
					}
					else
					{
						return new Creature[]
						{
							cha
						};
					}
				}
				else
				{
					cha = creature;
				}
				final boolean effectOriginIsPlayableInstance = cha instanceof Playable;
				PlayerInstance src = null;
				if (creature instanceof PlayerInstance)
				{
					src = (PlayerInstance) creature;
				}
				else if (creature instanceof Summon)
				{
					src = ((Summon) creature).getOwner();
				}
				final int radius = _skillRadius;
				final boolean srcInArena = creature.isInsideZone(ZoneId.PVP) && !creature.isInsideZone(ZoneId.SIEGE);
				for (WorldObject obj : creature.getKnownList().getKnownObjects().values())
				{
					if ((obj == null) || (!(creature instanceof Playable) && !(obj instanceof Playable)))
					{
						continue;
					}
					if ((!(obj instanceof Attackable) && !(obj instanceof Playable)))
					{
						continue;
					}
					if (obj == cha)
					{
						continue;
					}
					if ((src != null) && !src.checkPvpSkill(obj, this))
					{
						continue;
					}
					target = (Creature) obj;
					if (!GeoEngine.getInstance().canSeeTarget(creature, target))
					{
						continue;
					}
					if (_isOffensive && Creature.isInsidePeaceZone(creature, target))
					{
						continue;
					}
					if (!target.isAlikeDead() && (target != creature))
					{
						if (!Util.checkIfInRange(radius, obj, cha, true))
						{
							continue;
						}
						if (src != null) // caster is l2playableinstance and exists
						{
							// check for Events
							if (obj instanceof PlayerInstance)
							{
								final PlayerInstance trg = (PlayerInstance) obj;
								if (trg == src)
								{
									continue;
								}
								// if src is in event and trg not OR viceversa:
								// to be fixed for mixed events status (in TvT joining phase, someone can attack a partecipating CTF player with area attack)
								if (((src._inEvent || src._inEventCTF || src._inEventDM || src._inEventTvT || src._inEventVIP) && (!trg._inEvent && !trg._inEventCTF && !trg._inEventDM && !trg._inEventTvT && !trg._inEventVIP)) || ((trg._inEvent || trg._inEventCTF || trg._inEventDM || trg._inEventTvT || trg._inEventVIP) && (!src._inEvent && !src._inEventCTF && !src._inEventDM && !src._inEventTvT && !src._inEventVIP)))
								{
									continue;
								}
							}
							else if (obj instanceof Summon)
							{
								final PlayerInstance trg = ((Summon) obj).getOwner();
								if (trg == src)
								{
									continue;
								}
								// if src is in event and trg not OR viceversa:
								// to be fixed for mixed events status (in TvT joining phase, someone can attack a partecipating CTF player with area attack)
								if (((src._inEvent || src._inEventCTF || src._inEventDM || src._inEventTvT || src._inEventVIP) && (!trg._inEvent && !trg._inEventCTF && !trg._inEventDM && !trg._inEventTvT && !trg._inEventVIP)) || ((trg._inEvent || trg._inEventCTF || trg._inEventDM || trg._inEventTvT || trg._inEventVIP) && (!src._inEvent && !src._inEventCTF && !src._inEventDM && !src._inEventTvT && !src._inEventVIP)))
								{
									continue;
								}
							}
							if (obj instanceof PlayerInstance)
							{
								final PlayerInstance trg = (PlayerInstance) obj;
								if (trg == src)
								{
									continue;
								}
								if (((PlayerInstance) obj).getAppearance().isInvisible())
								{
									continue;
								}
								if ((src.getParty() != null) && (trg.getParty() != null) && (src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID()))
								{
									continue;
								}
								if (!srcInArena && (!trg.isInsideZone(ZoneId.PVP) || trg.isInsideZone(ZoneId.SIEGE)))
								{
									if ((src.getAllyId() == trg.getAllyId()) && (src.getAllyId() != 0))
									{
										continue;
									}
									if (checkPartyClan(src, obj))
									{
										continue;
									}
									if (!src.checkPvpSkill(obj, this))
									{
										continue;
									}
								}
							}
							if (obj instanceof Summon)
							{
								final PlayerInstance trg = ((Summon) obj).getOwner();
								if (trg == null)
								{
									continue;
								}
								if (trg == src)
								{
									continue;
								}
								if ((src.getParty() != null) && (trg.getParty() != null) && (src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID()))
								{
									continue;
								}
								if (!srcInArena && (!trg.isInsideZone(ZoneId.PVP) || trg.isInsideZone(ZoneId.SIEGE)))
								{
									if ((src.getAllyId() == trg.getAllyId()) && (src.getAllyId() != 0))
									{
										continue;
									}
									if (checkPartyClan(src, obj))
									{
										continue;
									}
									if (!src.checkPvpSkill(trg, this))
									{
										continue;
									}
								}
							}
						}
						else if (effectOriginIsPlayableInstance && // If effect starts at PlayableInstance and
							!(obj instanceof Playable))
						{
							continue;
						}
						targetList.add((Creature) obj);
					}
				}
				if (targetList.isEmpty())
				{
					return null;
				}
				return targetList.toArray(new Creature[targetList.size()]);
			}
			case TARGET_MULTIFACE:
			{
				if (!(target instanceof Attackable) && !(target instanceof PlayerInstance))
				{
					creature.sendPacket(new SystemMessage(SystemMessageId.THAT_IS_THE_INCORRECT_TARGET));
					return null;
				}
				if (!onlyFirst)
				{
					targetList.add(target);
				}
				else
				{
					return new Creature[]
					{
						target
					};
				}
				final int radius = _skillRadius;
				PlayerInstance src = null;
				if (creature instanceof PlayerInstance)
				{
					src = (PlayerInstance) creature;
				}
				else if (creature instanceof Summon)
				{
					src = ((Summon) creature).getOwner();
				}
				for (WorldObject obj : creature.getKnownList().getKnownObjects().values())
				{
					if (obj == null)
					{
						continue;
					}
					if (!Util.checkIfInRange(radius, creature, obj, true))
					{
						continue;
					}
					// check for Events
					if (src != null)
					{
						if (obj instanceof PlayerInstance)
						{
							final PlayerInstance trg = (PlayerInstance) obj;
							if (trg == src)
							{
								continue;
							}
							// if src is in event and trg not OR viceversa:
							// to be fixed for mixed events status (in TvT joining phase, someone can attack a partecipating CTF player with area attack)
							if (((src._inEvent || src._inEventCTF || src._inEventDM || src._inEventTvT || src._inEventVIP) && (!trg._inEvent && !trg._inEventCTF && !trg._inEventDM && !trg._inEventTvT && !trg._inEventVIP)) || ((trg._inEvent || trg._inEventCTF || trg._inEventDM || trg._inEventTvT || trg._inEventVIP) && (!src._inEvent && !src._inEventCTF && !src._inEventDM && !src._inEventTvT && !src._inEventVIP)))
							{
								continue;
							}
						}
						else if (obj instanceof Summon)
						{
							final PlayerInstance trg = ((Summon) obj).getOwner();
							if (trg == src)
							{
								continue;
							}
							// if src is in event and trg not OR viceversa:
							// to be fixed for mixed events status (in TvT joining phase, someone can attack a partecipating CTF player with area attack)
							if (((src._inEvent || src._inEventCTF || src._inEventDM || src._inEventTvT || src._inEventVIP) && (!trg._inEvent && !trg._inEventCTF && !trg._inEventDM && !trg._inEventTvT && !trg._inEventVIP)) || ((trg._inEvent || trg._inEventCTF || trg._inEventDM || trg._inEventTvT || trg._inEventVIP) && (!src._inEvent && !src._inEventCTF && !src._inEventDM && !src._inEventTvT && !src._inEventVIP)))
							{
								continue;
							}
						}
					}
					if ((obj instanceof Attackable) && (obj != target))
					{
						targetList.add((Creature) obj);
					}
					if (targetList.isEmpty())
					{
						creature.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_CANNOT_BE_FOUND));
						return null;
					}
				}
				return targetList.toArray(new Creature[targetList.size()]);
				// TODO multiface targets all around right now. need it to just get targets the character is facing.
			}
			case TARGET_PARTY:
			{
				if (onlyFirst)
				{
					return new Creature[]
					{
						creature
					};
				}
				targetList.add(creature);
				final PlayerInstance player = creature.getActingPlayer();
				if (player == null)
				{
					return new Creature[]
					{
						creature
					};
				}
				if (creature instanceof Summon)
				{
					targetList.add(player);
				}
				else if (creature instanceof PlayerInstance)
				{
					if (player.getPet() != null)
					{
						targetList.add(player.getPet());
					}
				}
				if (creature.getParty() != null)
				{
					// Get all visible objects in a spheric area near the Creature
					// Get a list of Party Members
					final List<PlayerInstance> partyList = creature.getParty().getPartyMembers();
					for (PlayerInstance partyMember : partyList)
					{
						if (partyMember == null)
						{
							continue;
						}
						if (partyMember == player)
						{
							continue;
						}
						// check if allow interference is allowed if player is not on event but target is on event
						if (((TvT.isStarted() && !Config.TVT_ALLOW_INTERFERENCE) || (CTF.isStarted() && !Config.CTF_ALLOW_INTERFERENCE) || (DM.hasStarted() && !Config.DM_ALLOW_INTERFERENCE))/* && !player.isGM() */)
						{
							if ((partyMember._inEventTvT && !player._inEventTvT) || (!partyMember._inEventTvT && player._inEventTvT))
							{
								continue;
							}
							if ((partyMember._inEventCTF && !player._inEventCTF) || (!partyMember._inEventCTF && player._inEventCTF))
							{
								continue;
							}
							if ((partyMember._inEventDM && !player._inEventDM) || (!partyMember._inEventDM && player._inEventDM))
							{
								continue;
							}
						}
						if (!partyMember.isDead() && Util.checkIfInRange(_skillRadius, creature, partyMember, true))
						{
							PlayerInstance src = null;
							if (creature instanceof PlayerInstance)
							{
								src = (PlayerInstance) creature;
							}
							else if (creature instanceof Summon)
							{
								src = ((Summon) creature).getOwner();
							}
							final PlayerInstance trg = partyMember;
							// if src is in event and trg not OR viceversa:
							// to be fixed for mixed events status (in TvT joining phase, someone can attack a partecipating CTF player with area attack)
							if ((src != null) && (((src._inEvent || src._inEventCTF || src._inEventDM || src._inEventTvT || src._inEventVIP) && (!trg._inEvent && !trg._inEventCTF && !trg._inEventDM && !trg._inEventTvT && !trg._inEventVIP)) || ((trg._inEvent || trg._inEventCTF || trg._inEventDM || trg._inEventTvT || trg._inEventVIP) && (!src._inEvent && !src._inEventCTF && !src._inEventDM && !src._inEventTvT && !src._inEventVIP))))
							{
								continue;
							}
							targetList.add(partyMember);
							if ((partyMember.getPet() != null) && !partyMember.getPet().isDead())
							{
								targetList.add(partyMember.getPet());
							}
						}
					}
				}
				return targetList.toArray(new Creature[targetList.size()]);
			}
			case TARGET_PARTY_MEMBER:
			{
				if ((target != null) && !target.isDead() && ((target == creature) || ((creature.getParty() != null) && (target.getParty() != null) && (creature.getParty().getPartyLeaderOID() == target.getParty().getPartyLeaderOID())) || (creature.getPet() == target) || (creature == target.getPet())))
				{
					// If a target is found, return it in a table else send a system message TARGET_IS_INCORRECT
					return new Creature[]
					{
						target
					};
				}
				creature.sendPacket(new SystemMessage(SystemMessageId.THAT_IS_THE_INCORRECT_TARGET));
				return null;
			}
			case TARGET_PARTY_OTHER:
			{
				if ((target != creature) && (target != null) && !target.isDead() && (creature.getParty() != null) && (target.getParty() != null) && (creature.getParty().getPartyLeaderOID() == target.getParty().getPartyLeaderOID()))
				{
					// If a target is found, return it in a table else send a system message TARGET_IS_INCORRECT
					return new Creature[]
					{
						target
					};
				}
				creature.sendPacket(new SystemMessage(SystemMessageId.THAT_IS_THE_INCORRECT_TARGET));
				return null;
			}
			case TARGET_CORPSE_ALLY:
			case TARGET_ALLY:
			{
				if (creature instanceof PlayerInstance)
				{
					final int radius = _skillRadius;
					final PlayerInstance player = (PlayerInstance) creature;
					final Clan clan = player.getClan();
					if (_targetType != SkillTargetType.TARGET_CORPSE_ALLY) // if corpose, the caster is not included
					{
						if (player.isInOlympiadMode())
						{
							return new Creature[]
							{
								player
							};
						}
						if (!onlyFirst)
						{
							targetList.add(player);
						}
						else
						{
							return new Creature[]
							{
								player
							};
						}
					}
					PlayerInstance src = null;
					if (creature instanceof PlayerInstance)
					{
						src = (PlayerInstance) creature;
					}
					else if (creature instanceof Summon)
					{
						src = ((Summon) creature).getOwner();
					}
					if (clan != null)
					{
						// Get all visible objects in a spheric area near the Creature
						// Get Clan Members
						for (WorldObject newTarget : creature.getKnownList().getKnownObjects().values())
						{
							if (!(newTarget instanceof PlayerInstance))
							{
								continue;
							}
							final PlayerInstance playerTarget = (PlayerInstance) newTarget;
							if (playerTarget.isDead() && (_targetType != SkillTargetType.TARGET_CORPSE_ALLY))
							{
								continue;
							}
							// if ally is different --> clan is different too, so --> continue
							if (player.getAllyId() != 0)
							{
								if (playerTarget.getAllyId() != player.getAllyId())
								{
									continue;
								}
							}
							else if (player.getClanId() != playerTarget.getClanId())
							{
								continue;
							}
							// check for Events
							if (src != null)
							{
								if (playerTarget == src)
								{
									continue;
								}
								// if src is in event and trg not OR viceversa:
								// to be fixed for mixed events status (in TvT joining phase, someone can attack a partecipating CTF player with area attack)
								if (((src._inEvent || src._inEventCTF || src._inEventDM || src._inEventTvT || src._inEventVIP) && (!playerTarget._inEvent && !playerTarget._inEventCTF && !playerTarget._inEventDM && !playerTarget._inEventTvT && !playerTarget._inEventVIP)) || ((playerTarget._inEvent || playerTarget._inEventCTF || playerTarget._inEventDM || playerTarget._inEventTvT || playerTarget._inEventVIP) && (!src._inEvent && !src._inEventCTF && !src._inEventDM && !src._inEventTvT && !src._inEventVIP)))
								{
									continue;
								}
							}
							final Summon pet = ((PlayerInstance) newTarget).getPet();
							if ((pet != null) && Util.checkIfInRange(radius, creature, pet, true) && !onlyFirst && (((_targetType == SkillTargetType.TARGET_CORPSE_ALLY) && pet.isDead()) || ((_targetType == SkillTargetType.TARGET_ALLY) && !pet.isDead())) && player.checkPvpSkill(newTarget, this))
							{
								targetList.add(pet);
							}
							if (_targetType == SkillTargetType.TARGET_CORPSE_ALLY)
							{
								if (!((PlayerInstance) newTarget).isDead())
								{
									continue;
								}
								if ((_skillType == SkillType.RESURRECT) && ((PlayerInstance) newTarget).isInsideZone(ZoneId.SIEGE))
								{
									continue;
								}
							}
							if (!Util.checkIfInRange(radius, creature, newTarget, true))
							{
								continue;
							}
							// Don't add this target if this is a Pc->Pc pvp casting and pvp condition not met
							if (!player.checkPvpSkill(newTarget, this))
							{
								continue;
							}
							if (!onlyFirst)
							{
								targetList.add((Creature) newTarget);
							}
							else
							{
								return new Creature[]
								{
									(Creature) newTarget
								};
							}
						}
					}
				}
				return targetList.toArray(new Creature[targetList.size()]);
			}
			case TARGET_CORPSE_CLAN:
			case TARGET_CLAN:
			{
				if (creature instanceof PlayerInstance)
				{
					final int radius = _skillRadius;
					final PlayerInstance player = (PlayerInstance) creature;
					final Clan clan = player.getClan();
					if (_targetType != SkillTargetType.TARGET_CORPSE_CLAN)
					{
						if (player.isInOlympiadMode())
						{
							return new Creature[]
							{
								player
							};
						}
						if (!onlyFirst)
						{
							targetList.add(player);
						}
						else
						{
							return new Creature[]
							{
								player
							};
						}
					}
					if (clan != null)
					{
						// Get all visible objects in a spheric area near the Creature
						// Get Clan Members
						for (ClanMember member : clan.getMembers())
						{
							final PlayerInstance newTarget = member.getPlayerInstance();
							if ((newTarget == null) || (newTarget == player))
							{
								continue;
							}
							if (player.isInDuel() && ((player.getDuelId() != newTarget.getDuelId()) || ((player.getParty() == null) && (player.getParty() != newTarget.getParty()))))
							{
								continue;
							}
							final PlayerInstance trg = newTarget;
							final PlayerInstance src = player;
							// if src is in event and trg not OR viceversa:
							// to be fixed for mixed events status (in TvT joining phase, someone can attack a partecipating CTF player with area attack)
							if (((src._inEvent || src._inEventCTF || src._inEventDM || src._inEventTvT || src._inEventVIP) && (!trg._inEvent && !trg._inEventCTF && !trg._inEventDM && !trg._inEventTvT && !trg._inEventVIP)) || ((trg._inEvent || trg._inEventCTF || trg._inEventDM || trg._inEventTvT || trg._inEventVIP) && (!src._inEvent && !src._inEventCTF && !src._inEventDM && !src._inEventTvT && !src._inEventVIP)))
							{
								continue;
							}
							final Summon pet = newTarget.getPet();
							if ((pet != null) && Util.checkIfInRange(radius, creature, pet, true) && !onlyFirst && (((_targetType == SkillTargetType.TARGET_CORPSE_CLAN) && pet.isDead()) || ((_targetType == SkillTargetType.TARGET_CLAN) && !pet.isDead())) && player.checkPvpSkill(newTarget, this))
							{
								targetList.add(pet);
							}
							if (_targetType == SkillTargetType.TARGET_CORPSE_CLAN)
							{
								if (!newTarget.isDead())
								{
									continue;
								}
								if (_skillType == SkillType.RESURRECT)
								{
									// check target is not in a active siege zone
									final Siege siege = SiegeManager.getInstance().getSiege(newTarget);
									if ((siege != null) && siege.isInProgress())
									{
										continue;
									}
								}
							}
							if (!Util.checkIfInRange(radius, creature, newTarget, true))
							{
								continue;
							}
							// Don't add this target if this is a Pc->Pc pvp casting and pvp condition not met
							if (!player.checkPvpSkill(newTarget, this))
							{
								continue;
							}
							if (!onlyFirst)
							{
								targetList.add(newTarget);
							}
							else
							{
								return new Creature[]
								{
									newTarget
								};
							}
						}
					}
				}
				else if (creature instanceof NpcInstance)
				{
					// for buff purposes, returns friendly mobs nearby and mob itself
					final NpcInstance npc = (NpcInstance) creature;
					if ((npc.getFactionId() == null) || npc.getFactionId().isEmpty())
					{
						return new Creature[]
						{
							creature
						};
					}
					targetList.add(creature);
					final Collection<WorldObject> objs = creature.getKnownList().getKnownObjects().values();
					// synchronized (activeChar.getKnownList().getKnownObjects())
					{
						for (WorldObject newTarget : objs)
						{
							if ((newTarget instanceof NpcInstance) && npc.getFactionId().equals(((NpcInstance) newTarget).getFactionId()))
							{
								if (!Util.checkIfInRange(_castRange, creature, newTarget, true))
								{
									continue;
								}
								targetList.add((NpcInstance) newTarget);
							}
						}
					}
				}
				return targetList.toArray(new Creature[targetList.size()]);
			}
			case TARGET_CORPSE_PLAYER:
			{
				if ((target != null) && target.isDead())
				{
					PlayerInstance player = null;
					if (creature instanceof PlayerInstance)
					{
						player = (PlayerInstance) creature;
					}
					PlayerInstance targetPlayer = null;
					if (target instanceof PlayerInstance)
					{
						targetPlayer = (PlayerInstance) target;
					}
					PetInstance targetPet = null;
					if (target instanceof PetInstance)
					{
						targetPet = (PetInstance) target;
					}
					if ((player != null) && ((targetPlayer != null) || (targetPet != null)))
					{
						boolean condGood = true;
						if (_skillType == SkillType.RESURRECT)
						{
							// check target is not in a active siege zone
							if (target.isInsideZone(ZoneId.SIEGE))
							{
								condGood = false;
								player.sendPacket(SystemMessageId.IT_IS_NOT_POSSIBLE_TO_RESURRECT_IN_BATTLEFIELDS_WHERE_A_SIEGE_WAR_IS_TAKING_PLACE);
							}
							if (targetPlayer != null)
							{
								if (targetPlayer.isReviveRequested())
								{
									if (targetPlayer.isRevivingPet())
									{
										player.sendPacket(SystemMessageId.WHILE_A_PET_IS_ATTEMPTING_TO_RESURRECT_IT_CANNOT_HELP_IN_RESURRECTING_ITS_MASTER); // While a pet is attempting to resurrect, it cannot help in resurrecting its master.
									}
									else
									{
										player.sendPacket(SystemMessageId.RESURRECTION_HAS_ALREADY_BEEN_PROPOSED); // Resurrection is already been proposed.
									}
									condGood = false;
								}
							}
							else if (targetPet != null)
							{
								if (targetPet.getOwner() != player)
								{
									condGood = false;
									player.sendMessage("You are not the owner of this pet");
								}
							}
						}
						if (condGood)
						{
							if (!onlyFirst)
							{
								targetList.add(target);
								return targetList.toArray(new WorldObject[targetList.size()]);
							}
							return new Creature[]
							{
								target
							};
						}
					}
				}
				creature.sendPacket(new SystemMessage(SystemMessageId.THAT_IS_THE_INCORRECT_TARGET));
				return null;
			}
			case TARGET_CORPSE_MOB:
			{
				if (!(target instanceof Attackable) || !target.isDead())
				{
					creature.sendPacket(new SystemMessage(SystemMessageId.THAT_IS_THE_INCORRECT_TARGET));
					return null;
				}
				if (!onlyFirst)
				{
					targetList.add(target);
					return targetList.toArray(new WorldObject[targetList.size()]);
				}
				return new Creature[]
				{
					target
				};
			}
			case TARGET_AREA_CORPSE_MOB:
			{
				if (!(target instanceof Attackable) || !target.isDead())
				{
					creature.sendPacket(new SystemMessage(SystemMessageId.THAT_IS_THE_INCORRECT_TARGET));
					return null;
				}
				if (!onlyFirst)
				{
					targetList.add(target);
				}
				else
				{
					return new Creature[]
					{
						target
					};
				}
				final boolean srcInArena = creature.isInsideZone(ZoneId.PVP) && !creature.isInsideZone(ZoneId.SIEGE);
				PlayerInstance src = null;
				if (creature instanceof PlayerInstance)
				{
					src = (PlayerInstance) creature;
				}
				PlayerInstance trg = null;
				final int radius = _skillRadius;
				if (creature.getKnownList() != null)
				{
					for (WorldObject obj : creature.getKnownList().getKnownObjects().values())
					{
						if (obj == null)
						{
							continue;
						}
						if ((!(obj instanceof Attackable) && !(obj instanceof Playable)) || ((Creature) obj).isDead() || ((Creature) obj == creature))
						{
							continue;
						}
						if (!Util.checkIfInRange(radius, target, obj, true))
						{
							continue;
						}
						if (!GeoEngine.getInstance().canSeeTarget(creature, obj))
						{
							continue;
						}
						if (_isOffensive && Creature.isInsidePeaceZone(creature, obj))
						{
							continue;
						}
						if ((obj instanceof PlayerInstance) && (src != null))
						{
							trg = (PlayerInstance) obj;
							if ((src.getParty() != null) && (trg.getParty() != null) && (src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID()))
							{
								continue;
							}
							if (!srcInArena && (!trg.isInsideZone(ZoneId.PVP) || trg.isInsideZone(ZoneId.SIEGE)))
							{
								if ((src.getAllyId() == trg.getAllyId()) && (src.getAllyId() != 0))
								{
									continue;
								}
								if ((src.getClan() != null) && (trg.getClan() != null) && (src.getClan().getClanId() == trg.getClan().getClanId()))
								{
									continue;
								}
								if (!src.checkPvpSkill(obj, this))
								{
									continue;
								}
							}
						}
						if ((obj instanceof Summon) && (src != null))
						{
							trg = ((Summon) obj).getOwner();
							if (trg == null)
							{
								continue;
							}
							if ((src.getParty() != null) && (trg.getParty() != null) && (src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID()))
							{
								continue;
							}
							if (!srcInArena && (!trg.isInsideZone(ZoneId.PVP) || trg.isInsideZone(ZoneId.SIEGE)))
							{
								if ((src.getAllyId() == trg.getAllyId()) && (src.getAllyId() != 0))
								{
									continue;
								}
								if ((src.getClan() != null) && (trg.getClan() != null) && (src.getClan().getClanId() == trg.getClan().getClanId()))
								{
									continue;
								}
								if (!src.checkPvpSkill(trg, this))
								{
									continue;
								}
							}
						}
						// check for Events
						if (trg == src)
						{
							continue;
						}
						// if src is in event and trg not OR viceversa:
						// to be fixed for mixed events status (in TvT joining phase, someone can attack a partecipating CTF player with area attack)
						if ((src != null) && (trg != null) && (((src._inEvent || src._inEventCTF || src._inEventDM || src._inEventTvT || src._inEventVIP) && (!trg._inEvent && !trg._inEventCTF && !trg._inEventDM && !trg._inEventTvT && !trg._inEventVIP)) || ((trg._inEvent || trg._inEventCTF || trg._inEventDM || trg._inEventTvT || trg._inEventVIP) && (!src._inEvent && !src._inEventCTF && !src._inEventDM && !src._inEventTvT && !src._inEventVIP))))
						{
							continue;
						}
						targetList.add((Creature) obj);
					}
				}
				if (targetList.isEmpty())
				{
					return null;
				}
				return targetList.toArray(new Creature[targetList.size()]);
			}
			case TARGET_UNLOCKABLE:
			{
				if (!(target instanceof DoorInstance) && !(target instanceof ChestInstance))
				{
					// Like L2OFF if target isn't door or chest send message of incorrect target
					creature.sendPacket(new SystemMessage(SystemMessageId.INVALID_TARGET));
					return null;
				}
				if (!onlyFirst)
				{
					targetList.add(target);
					return targetList.toArray(new WorldObject[targetList.size()]);
				}
				return new Creature[]
				{
					target
				};
			}
			case TARGET_ITEM:
			{
				creature.sendMessage("Target type of skill is not currently handled.");
				return null;
			}
			case TARGET_UNDEAD:
			{
				if ((target instanceof NpcInstance) || (target instanceof SummonInstance))
				{
					if (!target.isUndead() || target.isDead())
					{
						creature.sendPacket(new SystemMessage(SystemMessageId.THAT_IS_THE_INCORRECT_TARGET));
						return null;
					}
					if (!onlyFirst)
					{
						targetList.add(target);
					}
					else
					{
						return new Creature[]
						{
							target
						};
					}
					return targetList.toArray(new WorldObject[targetList.size()]);
				}
				creature.sendPacket(new SystemMessage(SystemMessageId.THAT_IS_THE_INCORRECT_TARGET));
				return null;
			}
			case TARGET_AREA_UNDEAD:
			{
				Creature cha;
				final int radius = _skillRadius;
				if ((_castRange >= 0) && ((target instanceof NpcInstance) || (target instanceof SummonInstance)) && target.isUndead() && !target.isAlikeDead())
				{
					cha = target;
					if (!onlyFirst)
					{
						targetList.add(cha); // Add target to target list
					}
					else
					{
						return new Creature[]
						{
							cha
						};
					}
				}
				else
				{
					cha = creature;
				}
				if ((cha != null) && (cha.getKnownList() != null))
				{
					for (WorldObject obj : cha.getKnownList().getKnownObjects().values())
					{
						if (obj == null)
						{
							continue;
						}
						if (obj instanceof NpcInstance)
						{
							target = (NpcInstance) obj;
						}
						else if (obj instanceof SummonInstance)
						{
							target = (SummonInstance) obj;
						}
						else
						{
							continue;
						}
						if (!GeoEngine.getInstance().canSeeTarget(creature, target))
						{
							continue;
						}
						if (!target.isAlikeDead()) // If target is not dead/fake death and not self
						{
							if (!target.isUndead())
							{
								continue;
							}
							if (!Util.checkIfInRange(radius, cha, obj, true))
							{
								continue;
							}
							if (!onlyFirst)
							{
								targetList.add((Creature) obj); // Add obj to target lists
							}
							else
							{
								return new Creature[]
								{
									(Creature) obj
								};
							}
						}
					}
				}
				if (targetList.isEmpty())
				{
					return null;
				}
				return targetList.toArray(new Creature[targetList.size()]);
			}
			case TARGET_ENEMY_SUMMON:
			{
				if (target instanceof Summon)
				{
					final Summon targetSummon = (Summon) target;
					if (((creature instanceof PlayerInstance) && (creature.getPet() != targetSummon) && !targetSummon.isDead() && ((targetSummon.getOwner().getPvpFlag() != 0) || (targetSummon.getOwner().getKarma() > 0) || targetSummon.getOwner().isInDuel())) || (targetSummon.getOwner().isInsideZone(ZoneId.PVP) && ((PlayerInstance) creature).isInsideZone(ZoneId.PVP)))
					{
						return new Creature[]
						{
							targetSummon
						};
					}
				}
				return null;
			}
			case TARGET_SIEGE:
			{
				if ((target != null) && !target.isDead() && ((target instanceof DoorInstance) || (target instanceof ControlTowerInstance)))
				{
					return new Creature[]
					{
						target
					};
				}
				return null;
			}
			case TARGET_TYRANNOSAURUS:
			{
				if (target instanceof PlayerInstance)
				{
					creature.sendPacket(new SystemMessage(SystemMessageId.THAT_IS_THE_INCORRECT_TARGET));
					return null;
				}
				if ((target instanceof MonsterInstance) && ((((MonsterInstance) target).getNpcId() == 22217) || (((MonsterInstance) target).getNpcId() == 22216) || (((MonsterInstance) target).getNpcId() == 22215)))
				{
					return new Creature[]
					{
						target
					};
				}
				return null;
			}
			case TARGET_AREA_AIM_CORPSE:
			{
				if ((target != null) && target.isDead())
				{
					return new Creature[]
					{
						target
					};
				}
				return null;
			}
			// npc only for now - untested
			case TARGET_CLAN_MEMBER:
			{
				if (creature instanceof NpcInstance)
				{
					// for buff purposes, returns friendly mobs nearby and mob itself
					final NpcInstance npc = (NpcInstance) creature;
					if ((npc.getFactionId() == null) || npc.getFactionId().isEmpty())
					{
						return new Creature[]
						{
							creature
						};
					}
					final Collection<WorldObject> objs = creature.getKnownList().getKnownObjects().values();
					for (WorldObject newTarget : objs)
					{
						if ((newTarget instanceof NpcInstance) && npc.getFactionId().equals(((NpcInstance) newTarget).getFactionId()))
						{
							if (!Util.checkIfInRange(_castRange, creature, newTarget, true))
							{
								continue;
							}
							if (((NpcInstance) newTarget).getFirstEffect(this) != null)
							{
								continue;
							}
							targetList.add((NpcInstance) newTarget);
							break; // found
						}
					}
					if (targetList.isEmpty())
					{
						targetList.add(npc);
					}
				}
				return null;
			}
			default:
			{
				creature.sendMessage("Target type of skill is not currently handled.");
				return null;
			}
		}
	}
	
	public WorldObject[] getTargetList(Creature creature)
	{
		return getTargetList(creature, false);
	}
	
	public WorldObject getFirstOfTargetList(Creature creature)
	{
		WorldObject[] targets;
		targets = getTargetList(creature, true);
		if ((targets == null) || (targets.length == 0))
		{
			return null;
		}
		return targets[0];
	}
	
	public Func[] getStatFuncs(Effect effect, Creature creature)
	{
		if (!(creature instanceof PlayerInstance) && !(creature instanceof Attackable) && !(creature instanceof Summon))
		{
			return _emptyFunctionSet;
		}
		
		if (_funcTemplates == null)
		{
			return _emptyFunctionSet;
		}
		
		final List<Func> funcs = new ArrayList<>();
		for (FuncTemplate t : _funcTemplates)
		{
			final Env env = new Env();
			env.player = creature;
			env.skill = this;
			final Func f = t.getFunc(env, this); // skill is owner
			if (f != null)
			{
				funcs.add(f);
			}
		}
		if (funcs.isEmpty())
		{
			return _emptyFunctionSet;
		}
		
		return funcs.toArray(new Func[funcs.size()]);
	}
	
	public boolean hasEffects()
	{
		return (_effectTemplates != null) && (_effectTemplates.length > 0);
	}
	
	public Effect[] getEffects(Creature effector, Creature effected)
	{
		return getEffects(effector, effected, false, false, false);
	}
	
	public Effect[] getEffects(Creature effector, Creature effected, boolean ss, boolean sps, boolean bss)
	{
		if (isPassive())
		{
			return _emptyEffectSet;
		}
		
		if (_effectTemplates == null)
		{
			return _emptyEffectSet;
		}
		
		if ((effector != effected) && effected.isInvul())
		{
			return _emptyEffectSet;
		}
		
		if ((_skillType == SkillType.BUFF) && effected.isBlockBuff())
		{
			return _emptyEffectSet;
		}
		
		final List<Effect> effects = new ArrayList<>();
		boolean skillMastery = false;
		if (!isToggle() && Formulas.getInstance().calcSkillMastery(effector))
		{
			skillMastery = true;
		}
		
		final Env env = new Env();
		env.player = effector;
		env.target = effected;
		env.skill = this;
		env.skillMastery = skillMastery;
		for (EffectTemplate et : _effectTemplates)
		{
			boolean success = true;
			if (et.effectPower > -1)
			{
				success = Formulas.calcEffectSuccess(effector, effected, et, this, ss, sps, bss);
			}
			
			if (success)
			{
				final Effect e = et.getEffect(env);
				if (e != null)
				{
					// e.scheduleEffect();
					effects.add(e);
				}
			}
		}
		
		if (effects.isEmpty())
		{
			return _emptyEffectSet;
		}
		
		return effects.toArray(new Effect[effects.size()]);
	}
	
	public Effect[] getEffectsSelf(Creature effector)
	{
		if (isPassive())
		{
			return _emptyEffectSet;
		}
		
		if (_effectTemplatesSelf == null)
		{
			return _emptyEffectSet;
		}
		
		final List<Effect> effects = new ArrayList<>();
		final Env env = new Env();
		env.player = effector;
		env.target = effector;
		env.skill = this;
		for (EffectTemplate et : _effectTemplatesSelf)
		{
			final Effect e = et.getEffect(env);
			if (e != null)
			{
				// Implements effect charge
				if (e.getEffectType() == Effect.EffectType.CHARGE)
				{
					env.skill = SkillTable.getInstance().getInfo(8, effector.getSkillLevel(8));
					final EffectCharge effect = (EffectCharge) env.target.getFirstEffect(Effect.EffectType.CHARGE);
					if (effect != null)
					{
						int effectcharge = effect.getLevel();
						if (effectcharge < _numCharges)
						{
							effectcharge++;
							effect.addNumCharges(effectcharge);
							if (env.target instanceof PlayerInstance)
							{
								env.target.sendPacket(new EtcStatusUpdate((PlayerInstance) env.target));
								final SystemMessage sm = new SystemMessage(SystemMessageId.YOUR_FORCE_HAS_INCREASED_TO_S1_LEVEL);
								sm.addNumber(effectcharge);
								env.target.sendPacket(sm);
							}
						}
					}
					else
					{
						effects.add(e);
					}
				}
				else
				{
					effects.add(e);
				}
			}
		}
		if (effects.isEmpty())
		{
			return _emptyEffectSet;
		}
		
		return effects.toArray(new Effect[effects.size()]);
	}
	
	public void attach(FuncTemplate f)
	{
		if (_funcTemplates == null)
		{
			_funcTemplates = new FuncTemplate[]
			{
				f
			};
		}
		else
		{
			final int len = _funcTemplates.length;
			final FuncTemplate[] tmp = new FuncTemplate[len + 1];
			System.arraycopy(_funcTemplates, 0, tmp, 0, len);
			tmp[len] = f;
			_funcTemplates = tmp;
		}
	}
	
	public void attach(EffectTemplate effect)
	{
		if (_effectTemplates == null)
		{
			setEffectTemplates(new EffectTemplate[]
			{
				effect
			});
		}
		else
		{
			final int len = _effectTemplates.length;
			final EffectTemplate[] tmp = new EffectTemplate[len + 1];
			System.arraycopy(_effectTemplates, 0, tmp, 0, len);
			tmp[len] = effect;
			setEffectTemplates(tmp);
		}
	}
	
	public void attachSelf(EffectTemplate effect)
	{
		if (_effectTemplatesSelf == null)
		{
			_effectTemplatesSelf = new EffectTemplate[]
			{
				effect
			};
		}
		else
		{
			final int len = _effectTemplatesSelf.length;
			final EffectTemplate[] tmp = new EffectTemplate[len + 1];
			System.arraycopy(_effectTemplatesSelf, 0, tmp, 0, len);
			tmp[len] = effect;
			_effectTemplatesSelf = tmp;
		}
	}
	
	public boolean isAbnormalEffectByName(int abnormalEffect)
	{
		// Function to know if the skill has "abnormalEffect"
		if (isPassive())
		{
			return false;
		}
		
		if (_effectTemplates == null)
		{
			return false;
		}
		
		for (EffectTemplate et : _effectTemplates)
		{
			if (et.abnormalEffect == abnormalEffect)
			{
				return true;
			}
		}
		return false;
	}
	
	public void attach(Condition c, boolean itemOrWeapon)
	{
		if (itemOrWeapon)
		{
			_itemPreCondition = c;
		}
		else
		{
			_preCondition = c;
		}
	}
	
	public boolean checkPartyClan(Creature creature, WorldObject target)
	{
		if ((creature instanceof PlayerInstance) && (target instanceof PlayerInstance))
		{
			final PlayerInstance targetChar = (PlayerInstance) target;
			final PlayerInstance activeCh = (PlayerInstance) creature;
			if (activeCh.isInOlympiadMode() && activeCh.isOlympiadStart() && targetChar.isInOlympiadMode() && targetChar.isOlympiadStart())
			{
				return false;
			}
			
			if (activeCh.isInDuel() && targetChar.isInDuel() && (activeCh.getDuelId() == targetChar.getDuelId()))
			{
				return false;
			}
			
			// if src is in event and trg not OR viceversa, the target must be not attackable
			// to be fixed for mixed events status (in TvT joining phase, someone can attack a partecipating CTF player with area attack)
			if (((activeCh._inEvent || activeCh._inEventCTF || activeCh._inEventDM || activeCh._inEventTvT || activeCh._inEventVIP) && (!targetChar._inEvent && !targetChar._inEventCTF && !targetChar._inEventDM && !targetChar._inEventTvT && !targetChar._inEventVIP)) || ((targetChar._inEvent || targetChar._inEventCTF || targetChar._inEventDM || targetChar._inEventTvT || targetChar._inEventVIP) && (!activeCh._inEvent && !activeCh._inEventCTF && !activeCh._inEventDM && !activeCh._inEventTvT && !activeCh._inEventVIP)))
			{
				return true;
			}
			
			if ((activeCh._inEvent && targetChar._inEvent) || (activeCh._inEventDM && targetChar._inEventDM) || (activeCh._inEventTvT && targetChar._inEventTvT) || (activeCh._inEventCTF && targetChar._inEventCTF) || (activeCh._inEventVIP && targetChar._inEventVIP))
			{
				return false;
			}
			
			if ((activeCh.getParty() != null) && (targetChar.getParty() != null) && // Is in the same party???
				(activeCh.getParty().getPartyLeaderOID() == targetChar.getParty().getPartyLeaderOID()))
			{
				return true;
			}
			if ((activeCh.getClan() != null) && (targetChar.getClan() != null) && // Is in the same clan???
				(activeCh.getClan().getClanId() == targetChar.getClan().getClanId()))
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString()
	{
		return "" + _name + "[id=" + _id + ",lvl=" + _level + "]";
	}
	
	/**
	 * @return Returns the _targetConsumeId.
	 */
	public int getTargetConsumeId()
	{
		return _targetConsumeId;
	}
	
	/**
	 * @return Returns the targetConsume.
	 */
	public int getTargetConsume()
	{
		return _targetConsume;
	}
	
	public boolean hasSelfEffects()
	{
		return ((_effectTemplatesSelf != null) && (_effectTemplatesSelf.length > 0));
	}
	
	/**
	 * @return minimum skill/effect land rate (default is 1).
	 */
	public int getMinChance()
	{
		return _minChance;
	}
	
	/**
	 * @return maximum skill/effect land rate (default is 99).
	 */
	public int getMaxChance()
	{
		return _maxChance;
	}
	
	/**
	 * @return the _advancedFlag
	 */
	public boolean isAdvancedFlag()
	{
		return _advancedFlag;
	}
	
	/**
	 * @return the _advancedMultiplier
	 */
	public int getAdvancedMultiplier()
	{
		return _advancedMultiplier;
	}
	
	/**
	 * @return the _effectTemplates
	 */
	public EffectTemplate[] getEffectTemplates()
	{
		return _effectTemplates;
	}
	
	/**
	 * @param effectTemplates the _effectTemplates to set
	 */
	public void setEffectTemplates(EffectTemplate[] effectTemplates)
	{
		_effectTemplates = effectTemplates;
	}
}