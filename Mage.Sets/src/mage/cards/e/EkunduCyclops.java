package mage.cards.e;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.RequirementEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.SubType;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class EkunduCyclops extends CardImpl {

    public EkunduCyclops(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{R}");

        this.subtype.add(SubType.CYCLOPS);
        this.power = new MageInt(3);
        this.toughness = new MageInt(4);

        // If a creature you control attacks, this creature also attacks if able.
        this.addAbility(new SimpleStaticAbility(new EkunduCyclopsEffect()));
    }

    private EkunduCyclops(final EkunduCyclops card) {
        super(card);
    }

    @Override
    public EkunduCyclops copy() {
        return new EkunduCyclops(this);
    }
}

class EkunduCyclopsEffect extends RequirementEffect {

    EkunduCyclopsEffect() {
        super(Duration.WhileOnBattlefield);
        staticText = "If a creature you control attacks, {this} also attacks if able";
    }

    private EkunduCyclopsEffect(final EkunduCyclopsEffect effect) {
        super(effect);
    }

    @Override
    public EkunduCyclopsEffect copy() {
        return new EkunduCyclopsEffect(this);
    }

    @Override
    public boolean applies(Permanent permanent, Ability source, Game game) {
        if (!permanent.getId().equals(source.getSourceId())) {
            return false;
        }
        for (UUID attackerId : game.getCombat().getAttackers()) {
            if (!attackerId.equals(permanent.getId())) {
                Permanent attacker = game.getPermanent(attackerId);
                if (attacker != null && attacker.isControlledBy(source.getControllerId())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean mustAttack(Game game) {
        return true;
    }

    @Override
    public boolean mustBlock(Game game) {
        return false;
    }
}
