package pixelmon.battles.participants;

import net.minecraft.src.EntityPlayer;

import net.minecraft.src.NBTTagCompound;
import pixelmon.battles.BattleController;
import pixelmon.battles.attacks.Attack;
import pixelmon.comm.ChatHandler;
import pixelmon.entities.EntityTrainer;
import pixelmon.entities.pixelmon.helpers.PixelmonEntityHelper;

public class TrainerParticipant implements IBattleParticipant {

	public EntityTrainer trainer;
	private BattleController bc;

	public TrainerParticipant(EntityTrainer trainer, EntityPlayer opponent) {
		this.trainer = trainer;
		trainer.releasePokemon();
		trainer.startBattle(opponent);
	}

	@Override
	public void setBattleController(BattleController bc) {
		this.bc = bc;
	}

	@Override
	public PixelmonEntityHelper currentPokemon() {
		return trainer.releasedPokemon.getHelper();
	}

	@Override
	public boolean hasMorePokemon() {
		return trainer.hasAblePokemon();
	}

	@Override
	public boolean canGainXP() {
		return false;
	}

	@Override
	public void EndBattle(boolean didWin, IBattleParticipant foe) {
		trainer.releasedPokemon.getHelper().battleStats.clearBattleStats();
		trainer.releasedPokemon.getHelper().EndBattle();
		trainer.healAllPokemon();
		this.trainer.setAttackTarget(null);
		if (didWin)
			trainer.winBattle(foe.currentPokemon().getOwner());
		else
			trainer.loseBattle(foe.currentPokemon().getOwner());
	}

	@Override
	public void getNextPokemon() {
		trainer.pokemonStorage.updateNBT(currentPokemon());
		trainer.pokemonStorage.getNBT(currentPokemon().getPokemonId()).setBoolean("IsFainted", true);
		trainer.releasePokemon();
	}

	@Override
	public boolean getIsFaintedOrDead() {
		return trainer.releasedPokemon.getHelper().getIsDead() || trainer.releasedPokemon.getHelper().isFainted || trainer.releasedPokemon.getHelper().getHealth() <= 0;
	}

	@Override
	public String getName() {
		return trainer.info.name;
	}

	@Override
	public Attack getMove(IBattleParticipant participant2) {
		return Attack.getWhichMoveIsBest(trainer.releasedPokemon.getHelper().moveset, participant2.currentPokemon().getType(), trainer.releasedPokemon.getHelper(), participant2.currentPokemon());
	}

	@Override
	public void switchPokemon(IBattleParticipant participant2, int newPixelmonId) {	}

	@Override
	public boolean checkPokemon() {
		for (NBTTagCompound n : trainer.pokemonStorage.partyPokemon) {
			if (n != null && n.getInteger("PixelmonNumberMoves") == 0) {
				System.out.println("Couldn't load pokemon's moves");
				return false;
			}
		}
		return true;
	}

	@Override
	public void updatePokemon() {
		trainer.pokemonStorage.getNBT(currentPokemon().getPokemonId()).setBoolean("IsFainted", true);
	}
}
