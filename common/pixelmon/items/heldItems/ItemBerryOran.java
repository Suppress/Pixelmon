package pixelmon.items.heldItems;

import pixelmon.battles.attacks.statusEffects.StatusEffectBase;
import pixelmon.battles.attacks.statusEffects.StatusEffectType;
import pixelmon.comm.ChatHandler;
import pixelmon.entities.pixelmon.helpers.PixelmonEntityHelper;
import pixelmon.enums.EnumHeldItems;
import pixelmon.items.ItemHeld;

public class ItemBerryOran extends ItemHeld {

	public ItemBerryOran(int id) {
		super(id, EnumHeldItems.oran);
		SetUsableInBattle(true);
	}

	public boolean effectEntity(PixelmonEntityHelper helper) {
		if (helper.getHealth() < (int) ((float) helper.getMaxHealth() / .3f)) {
			helper.setHealth(helper.getHealth() + 10);
			ChatHandler.sendChat(helper.getOwner(), helper.getName() + " just consumed an Oran Berry and gained 10 health!");
			return true;
		}
		return false;
	}
	
	@Override
	public void useFromBag(PixelmonEntityHelper userPokemon, PixelmonEntityHelper targetPokemon) {
		if (userPokemon.getHealth()+10 > userPokemon.stats.HP)
			userPokemon.setHealth(userPokemon.stats.HP);
		else
			userPokemon.setHealth(userPokemon.getHealth() + 10);
		ChatHandler.sendChat(userPokemon.getOwner(), userPokemon.getName() + " gained 10 health!");
	}

}
