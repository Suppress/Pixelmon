package pixelmon.battles.attacks.specialAttacks;

import java.util.ArrayList;

import pixelmon.battles.attacks.Attack;
import pixelmon.battles.attacks.EffectType;
import pixelmon.battles.attacks.attackEffects.EffectBase;
import pixelmon.battles.attacks.attackEffects.EffectBase.ApplyStage;
import pixelmon.battles.attacks.attackModifiers.AttackModifierType;
import pixelmon.entities.pixelmon.helpers.PixelmonEntityHelper;


public abstract class SpecialAttackBase extends EffectBase{
	public SpecialAttackType type;
	public int value = -1;
	public int value2 = -1;
	
	public SpecialAttackBase(SpecialAttackType type, ApplyStage a, boolean persists){
		super(EffectType.SpecialAttack, a, persists);
		this.type = type;
	}

	public abstract boolean ApplyEffect(PixelmonEntityHelper user, PixelmonEntityHelper target, Attack a, ArrayList<String> attackList);
	
	@Override
	public void ApplyEffect(PixelmonEntityHelper user, PixelmonEntityHelper target, ArrayList<String> attackList) {
	}
	
	public boolean cantMiss() {
		return false;
	}
}
