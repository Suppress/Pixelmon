package pixelmon.entities.pokemon;

import pixelmon.entities.pixelmon.EntityGroundPixelmon;
import pixelmon.entities.pixelmon.helpers.IHaveHelper;
import net.minecraft.src.World;

public class EntityGrowlithe extends EntityGroundPixelmon
{
	
	public EntityGrowlithe(World world)
	{
		super(world);
		init();
	}

	public void init()
	{
		name = "Growlithe";
		isImmuneToFire = true;
		super.init();
	}
	
	public void evolve() 
	{
		IHaveHelper entity = new EntityArcanine(worldObj);
		helper.evolve(entity.getHelper());
	}

}
