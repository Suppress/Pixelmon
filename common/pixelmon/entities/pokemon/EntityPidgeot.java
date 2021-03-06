package pixelmon.entities.pokemon;

import pixelmon.entities.pixelmon.BaseEntityPixelmon;
import pixelmon.entities.pixelmon.EntityFlyingPixelmon;
import pixelmon.entities.pixelmon.EntityGroundPixelmon;
import net.minecraft.src.Item;
import net.minecraft.src.World;

public class EntityPidgeot extends EntityFlyingPixelmon
{
	
	public EntityPidgeot(World world)
	{
		super(world);
		init();
	}
	 protected int getDropItemId()
	    {
	        return Item.feather.shiftedIndex;
	    }
	public void init()
	{
		name = "Pidgeot";
		isImmuneToFire = false;
		super.init();
	}
	public void evolve() 
	{		
	}
}
