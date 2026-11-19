package mage.cards.g;

import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.InfoEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.common.TargetCreaturePermanent;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class GarbageFire extends CardImpl {

    public GarbageFire(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{2}{R}");

        // Reveal Garbage Fire as you draft it and note how many cards you've drafted this draft round, including this card.
        this.addAbility(new SimpleStaticAbility(
                Zone.ALL,
                new InfoEffect("Reveal {this} as you draft it and note how many cards you've drafted this draft round, including this card - not implemented.")
        ));

        // Garbage Fire deals damage to target creature equal to the highest number you noted for cards named Garbage Fire.
        this.getSpellAbility().addEffect(new GarbageFireEffect());
        this.getSpellAbility().addTarget(new TargetCreaturePermanent());
    }

    private GarbageFire(final GarbageFire card) {
        super(card);
    }

    @Override
    public GarbageFire copy() {
        return new GarbageFire(this);
    }
}

class GarbageFireEffect extends OneShotEffect {

    GarbageFireEffect() {
        super(Outcome.Damage);
        staticText = "{this} deals damage to target creature equal to the highest number you noted for cards named {this}";
    }

    private GarbageFireEffect(final GarbageFireEffect effect) {
        super(effect);
    }

    @Override
    public GarbageFireEffect copy() {
        return new GarbageFireEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (permanent == null) {
            return false;
        }
        Object raw = game.getState().getValue(source.getControllerId() + "_garbage_fire_max");
        int damage = raw instanceof Integer ? Math.max(0, (Integer) raw) : 0;
        if (damage > 0) {
            permanent.damage(damage, source.getSourceId(), source, game);
        }
        return true;
    }
}
