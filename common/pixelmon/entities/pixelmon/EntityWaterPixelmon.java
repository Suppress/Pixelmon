package pixelmon.entities.pixelmon;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;

import pixelmon.Pixelmon;
import pixelmon.WorldHelper;
import pixelmon.battles.BattleController;
import pixelmon.battles.Moveset;
import pixelmon.battles.attacks.statusEffects.StatusEffectBase;
import pixelmon.battles.participants.IBattleParticipant;
import pixelmon.battles.participants.PlayerParticipant;
import pixelmon.battles.participants.TrainerParticipant;
import pixelmon.battles.participants.WildPixelmonParticipant;
import pixelmon.comm.ChatHandler;
import pixelmon.database.BattleStats;
import pixelmon.database.DatabaseMoves;
import pixelmon.database.DatabaseStats;
import pixelmon.database.PixelmonIVStore;
import pixelmon.database.Stats;
import pixelmon.entities.EntityTrainer;
import pixelmon.entities.pixelmon.helpers.IHaveHelper;
import pixelmon.entities.pixelmon.helpers.LevelHelper;
import pixelmon.entities.pixelmon.helpers.PixelmonEntityHelper;
import pixelmon.entities.pixelmon.helpers.RidingHelper;
import pixelmon.enums.EnumType;
import pixelmon.storage.PixelmonStorage;
import pixelmon.storage.PokeballManager;

import net.minecraft.client.Minecraft;
import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Block;
import net.minecraft.src.DamageSource;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityAITasks;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.EntityWaterMob;

import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;

public abstract class EntityWaterPixelmon extends EntityTameableWaterPokemon implements IHaveHelper {
	public String name;
	public ArrayList<EnumType> type = new ArrayList<EnumType>();
	private float field_21080_l;
	public float swimFrequency = 0.6f;
	public float swimSpeed = 1.5f;
	public float decayRate = 0.99f;
	public int refreshRate = 100;
	protected int depthRangeStart = 0;
	protected int depthRangeEnd = 100;
	public boolean isSwimming = true;
	public PixelmonEntityHelper helper = new PixelmonEntityHelper(this);
	private RidingHelper ridingHelper;
	private float length = 1.0f;

	public EntityWaterPixelmon(World par1World) {
		super(par1World);
		helper.stats.IVs = PixelmonIVStore.CreateNewIVs();
		dataWatcher.addObject(18, "");
		dataWatcher.addObject(19, -1);
		dataWatcher.addObject(20, (short) 0);
		dataWatcher.addObject(21, (short) 0); // roasted
	}

	public void init() {
		if ((new Random()).nextFloat() < 1 / 8192f) {
			System.out.println("Shiny " + name + " spawned");
			dataWatcher.updateObject(20, (short) 1);
		}
		helper.stats.BaseStats = DatabaseStats.GetBaseStats(name);
		helper.giScale = helper.stats.BaseStats.giScale;
		type.add(helper.stats.BaseStats.Type1);
		if (helper.stats.BaseStats.Type2 != EnumType.Mystery)
			type.add(helper.stats.BaseStats.Type2);
		health = 10;
		if (rand.nextInt(100) < helper.stats.BaseStats.MalePercent)
			helper.isMale = true;
		else
			helper.isMale = false;
		setSize(helper.stats.BaseStats.Width, helper.stats.BaseStats.Height);
		length = helper.stats.BaseStats.Length;
		helper.getLvl();
		this.field_21080_l = 1.0F / (this.rand.nextFloat() + 1.0F) * swimFrequency;
		// ridingHelper = new RidingHelper(this, worldObj);
	}

	@Override
	public String getTexture() {
		if (dataWatcher.getWatchableObjectShort(20) == 1 && Minecraft.getMinecraft().renderEngine.texturePack.getSelectedTexturePack().getResourceAsStream("/pixelmon/texture/pokemon-shiny/shiny" + name.toLowerCase() + ".png")!=null)
			return "/pixelmon/texture/pokemon-shiny/shiny" + name.toLowerCase() + ".png";
		else if (dataWatcher.getWatchableObjectShort(21) == 1 && Minecraft.getMinecraft().renderEngine.texturePack.getSelectedTexturePack().getResourceAsStream("/pixelmon/texture/pokemon-roasted/roasted" + name.toLowerCase() + ".png")!=null)
			return "/pixelmon/texture/pokemon-roasted/roasted" + name.toLowerCase() + ".png";
		else
			return "/pixelmon/texture/pokemon/" + name.toLowerCase() + ".png";
	}

	@Override
	public void setPosition(double par1, double par3, double par5) {
		this.posX = par1;
		this.posY = par3;
		this.posZ = par5;
		float halfWidth = this.width / 2.0F;
		float halfLength = this.length / 2.0F;
		this.boundingBox.setBounds(par1 - (double) halfWidth, par3 - (double) this.yOffset + (double) this.ySize, par5 - (double) halfLength, par1 + (double) halfWidth, par3 - (double) this.yOffset
				+ (double) this.ySize + (double) height, par5 + (double) halfLength);
	}

	/**
	 * Returns a boundingBox used to collide the entity with other entities and
	 * blocks. This enables the entity to be pushable on contact, like boats or
	 * minecarts.
	 */
	@Override
	public AxisAlignedBB getCollisionBox(Entity par1Entity) {
		return par1Entity.boundingBox;
	}

	/**
	 * returns the bounding box for this entity
	 */
	@Override
	public AxisAlignedBB getBoundingBox() {
		return this.boundingBox;
	}

	protected boolean isValidTarget(Entity entity) {
		return helper.isValidTarget(entity);
	}

	public boolean interact(EntityPlayer entity) {
		return helper.interact(entity);
	}

	public void onDeath(DamageSource damagesource) {
		super.onDeath(damagesource);
		if (getOwner() != null) {
			String s = "Your " + helper.getName() + " fainted!";
			ChatHandler.sendChat(getOwner(), s);
			helper.isFainted = true;
			health = 0;
			catchInPokeball();
		} else {
			super.onDeath(damagesource);
			this.setDead();
		}
	}

	public void setHealth(int i) {
		health = i;
	}

	float randomMotionSpeed;
	float randomMotionVecX;
	float randomMotionVecY;
	float randomMotionVecZ;
	private float field_21085_g;

	public void onLivingUpdate() {
		super.onLivingUpdate();
		// if (ridingHelper != null)
		// ridingHelper.onLivingUpdate();
		if (!isSwimming || worldObj.isRemote) {
			motionX = motionY = motionZ = 0;
			return;
		}
		this.field_21085_g += this.field_21080_l;
		if (this.field_21085_g > ((float) Math.PI * 2F)) {
			this.field_21085_g -= ((float) Math.PI * 2F);
		}
		if (this.isInWater()) {
			if (randomMotionSpeed <= swimSpeed / 2)
				randomMotionSpeed = swimSpeed;
			randomMotionSpeed *= decayRate;

			if (!worldObj.isRemote) {
				motionX = randomMotionVecX * randomMotionSpeed;
				motionY = randomMotionVecY * randomMotionSpeed * 0.05;
				motionZ = randomMotionVecZ * randomMotionSpeed;
			}

			renderYawOffset += ((-(float) Math.atan2(motionX, motionZ) * 180F) / (float) Math.PI - renderYawOffset) * 0.1F;
			rotationYaw = renderYawOffset;
		} else {
			surfaceHeight = this.posY;
			this.motionX = 0.0D;
			this.motionY -= 0.08D;
			this.motionY *= 0.9800000190734863D;
			this.motionZ = 0.0D;
		}
	}

	double surfaceHeight = 1000;

	protected void fall(float f) {
		if (!this.isInWater()) {
			super.fall(f);
		}
	}

	public void moveEntityWithHeading(float par1, float par2) {
		super.moveEntityWithHeading((float) motionX, (float) motionZ);
		// moveEntity(motionX, motionY, motionZ);
	}

	public boolean canBreatheUnderwater() {
		return true;
	}

	int count = 0;
	public EntityTrainer trainer;

	protected void updateEntityActionState() {
		if (count == 0) {
			int wdepth = WorldHelper.getWaterDepth((int) posX, (int) posY, (int) posZ, worldObj);
			// if (rand.nextInt(90) == 0 || randomMotionVecX == 0.0F
			// && randomMotionVecY == 0.0F && randomMotionVecZ == 0.0F) {
			count = refreshRate - refreshRate / 2 + rand.nextInt(refreshRate / 2);
			randomMotionVecX = (-0.5f + rand.nextFloat()) * 0.2F;
			if (wdepth >= depthRangeEnd)
				randomMotionVecY = (0.02F + rand.nextFloat()) * 0.1F;
			else if (wdepth <= depthRangeStart)
				randomMotionVecY = (-0.02F + rand.nextFloat()) * 0.1F;
			else
				randomMotionVecY = rand.nextFloat() * 0.1F;
			randomMotionVecZ = (-0.5f + rand.nextFloat()) * 0.2F;
		}
		count--;

		despawnEntity();
	}

	/**
	 * Checks if the entity's current position is a valid location to spawn this
	 * entity.
	 */
	public boolean getCanSpawnHere() {
		int wdepth = WorldHelper.getWaterDepth((int) posX, (int) posY, (int) posZ, worldObj);
		if (wdepth >= depthRangeStart && wdepth < depthRangeEnd)
			return true;
		return false;
	}

	public abstract void evolve();

	protected void evolve(PixelmonEntityHelper entity) {
		helper.evolve(entity);
	}

	public void catchInPokeball() {
		PixelmonStorage.PokeballManager.getPlayerStorage((EntityPlayerMP) getOwner()).updateNBT(helper);
		helper.isInBall = true;
		unloadEntity();
	}

	public void unloadEntity() {
		ArrayList<Entity> list = new ArrayList<Entity>();
		list.add(this);
		worldObj.unloadEntities(list);
		clearAttackTarget();
		if (helper.bc != null) {
			helper.bc = null;
		}
	}

	public int getMaxHealth() {
		if (helper == null || helper.stats == null)
			return 1;
		return helper.stats.HP;
	}

	public void releaseFromPokeball() {
		worldObj.spawnEntityInWorld(this);
		helper.isInBall = false;
	}

	public void setMotion(int i, int j, int k) {
		motionX = i;
		motionY = j;
		motionZ = k;
	}

	public void clearAttackTarget() {
		setTarget(null);
		setAttackTarget(null);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound var1) {
		if (getPokemonId() < -1) {
			return;
		}
		super.writeEntityToNBT(var1);
		helper.writeToNBT(var1);
	}

	boolean isStorage = false;

	public void writeEntityToStorageNBT(NBTTagCompound var1) {
		isStorage = true;
		writeEntityToNBT(var1);
		isStorage = false;
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound var1) {
		super.readEntityFromNBT(var1);
		helper.readFromNBT(var1);
	}

	public void setLocationAndAngles(IHaveHelper currentPixelmon) {
		if (currentPixelmon instanceof BaseEntityPixelmon) {
			this.posX = ((BaseEntityPixelmon) currentPixelmon).posX;
			this.posY = ((BaseEntityPixelmon) currentPixelmon).posY;
			this.posZ = ((BaseEntityPixelmon) currentPixelmon).posZ;
		} else if (currentPixelmon instanceof EntityWaterPixelmon) {
			this.posX = ((EntityWaterPixelmon) currentPixelmon).posX;
			this.posY = ((EntityWaterPixelmon) currentPixelmon).posY;
			this.posZ = ((EntityWaterPixelmon) currentPixelmon).posZ;
		}
	}

	public PixelmonEntityHelper getHelper() {
		return helper;
	}

	public void onUpdate() {
		if (helper != null) {
			helper.onUpdate();
		}
		super.onUpdate();
	}

	public EntityPlayer getOwner() {
		if (super.getOwner() instanceof EntityPlayer)
			return (EntityPlayer) super.getOwner();
		return null;
	}

	public void setOwner(EntityPlayer owner) {
		super.setOwner(owner.username);
	}

	public void setLvlString(String string) {
		dataWatcher.updateObject(18, string);
	}

	public String getLvlString() {
		return dataWatcher.getWatchableObjectString(18);
	}

	public int getPokemonId() {
		return dataWatcher.getWatchableObjectInt(19);
	}

	public void setPokemonId(int id) {
		dataWatcher.updateObject(19, id);
	}

	public boolean getIsShiny() {
		return dataWatcher.getWatchableObjectShort(20) == 1;
	}

	public void setIsShiny(boolean isShiny) {
		if (isShiny)
			dataWatcher.updateObject(20, (short) 1);
		else
			dataWatcher.updateObject(20, (short) 0);
	}

	@Override
	public void jump() {
		super.jump();
	}

	// @Override
	// public double getMountedYOffset() {
	// if (ridingHelper != null)
	// return ridingHelper.getMountedYOffset();
	// else
	// return super.getMountedYOffset();
	// }
	//
	// @Override
	// public void moveEntity(double d, double d1, double d2) {
	// if (ridingHelper != null)
	// ridingHelper.moveEntity(d, d1, d2);
	// else
	// super.moveEntity(d, d1, d2);
	// }
	//
	// @Override
	// public void updateRidden() {
	// if (ridingHelper != null)
	// ridingHelper.updateRidden();
	// else
	// super.updateRidden();
	// }

	public void doMoveEntity(double motionX, double motionY, double motionZ) {
		super.moveEntity(motionX, motionY, motionZ);
	}

	public World getWorldObj() {
		return worldObj;
	}

	public boolean attackEntityFrom(DamageSource par1DamageSource, int par2) {
		if (!worldObj.isRemote) {
			if (health - par2 < 0) {
				par2 = health;
				this.onDeath(par1DamageSource);
			}
			if (par1DamageSource.fireDamage()) dataWatcher.updateObject(21, (short)1);
			boolean flag = super.attackEntityFrom(par1DamageSource, par2);
			Entity entity = par1DamageSource.getEntity();
			if (getOwner() != null)
				PixelmonStorage.PokeballManager.getPlayerStorage((EntityPlayerMP) getOwner()).updateNBT(helper);
			if (isValidTarget(entity)) {
				setAttackTarget((EntityLiving) entity);
				setTarget(entity);
			}
			helper.lvl.updateEntityString();
			return flag;
		}
		
		return false;
	}

	/**
	 * Returns the sound this mob makes while it's alive.
	 */
	@Override
	protected String getLivingSound() {
		return ("pixelmon." + name.toLowerCase());
	}

	/**
	 * Returns the sound this mob makes when it is hurt.
	 */
	// @Override
	// protected String getHurtSound()
	// {
	// return "mob.cowhurt";
	// }
	//
	// /**
	// * Returns the sound this mob makes on death.
	// */
	// @Override
	// protected String getDeathSound()
	// {
	// return "mob.cowhurt";
	// }

	/**
	 * Returns the volume for the sounds this mob makes.
	 */
	@Override
	protected float getSoundVolume() {
		return 0.4F;
	}
}
