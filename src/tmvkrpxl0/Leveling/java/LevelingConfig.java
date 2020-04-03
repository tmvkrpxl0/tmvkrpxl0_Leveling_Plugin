package tmvkrpxl0.Leveling.java;

public class LevelingConfig {
	private int level;
	private int exp;
	private int statPoint;
	private int skillPoint;
	private int attack;
	private int defense;
	private int health;
	private int agility;
	private boolean side;
	private int previous;
	private int maxLevel;
	private boolean actionbar;
	protected LevelingConfig(int level, int exp, int statPoint, int skillPoint, int attack, int defense, int health, int agility, boolean side, int previous, int maxLevel, boolean actionbar) {
		this.level = level;
		this.exp = exp;
		this.statPoint = statPoint;
		this.skillPoint = skillPoint;
		this.attack = attack;
		this.defense = defense;
		this.health = health;
		this.agility = agility;
		this.side = side;
		this.previous = previous;
		this.maxLevel = maxLevel;
		this.actionbar = actionbar;
	}
	protected int getLevel() {
		return this.level;
	}

	protected void setLevel(int level) {
		this.level = level;
	}

	protected int getExp() {
		return this.exp;
	}

	protected void setExp(int exp) {
		this.exp = exp;
	}

	protected int getStatPoint(){
		return this.statPoint;
	}

	protected void setStatPoint(int statPoint){
		this.statPoint = statPoint;
	}

	protected int getSkillPoint(){
		return this.skillPoint;
	}

	protected void setSkillPoint(int skillPoint){
		this.skillPoint = skillPoint;
	}

	protected void setAttack(int attack){
		this.attack = attack;
	}

	protected int getAttack() {
		return this.attack;
	}

	protected void setDefense(int defense){
		this.defense = defense;
	}

	protected int getDefense(){
		return this.defense;
	}

	protected void setHealth(int health){
		this.health = health;
	}

	protected int getHealth(){
		return this.health;
	}

	protected void setAgility(int agility){
		this.agility = agility;
	}

	protected int getAgility(){
		return this.agility;
	}

	protected boolean getSide(){
		return this.side;
	}

	protected int getPrevious(){
		return this.previous;
	}

	protected void setPrevious(int previous){
		this.previous = previous;
	}

	protected int getMaxLevel(){
		return this.maxLevel;
	}

	protected void setMaxLevel(int maxLevel){
		this.maxLevel = maxLevel;
	}

	protected boolean isActionbar(){
		return this.actionbar;
	}

	protected void setActionbar(boolean actionbar){
		this.actionbar = actionbar;
	}
}
