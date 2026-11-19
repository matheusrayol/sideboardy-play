package mage.cards.c;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.InfoEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class CogworkGrinder extends CardImpl {

    public CogworkGrinder(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{6}");

        this.subtype.add(SubType.CONSTRUCT);
        this.power = new MageInt(0);
        this.toughness = new MageInt(0);

        // Draft Cogwork Grinder face up.
        this.addAbility(new SimpleStaticAbility(
                Zone.ALL,
                new InfoEffect("Draft {this} face up - not implemented.")
        ));

        // As you draft a card, you may remove it from the draft face down.
        this.addAbility(new SimpleStaticAbility(
                Zone.ALL,
                new InfoEffect("As you draft a card, you may remove it from the draft face down - not implemented.")
        ));

        // Cogwork Grinder enters with X +1/+1 counters on it, where X is the number of cards you removed from the draft with cards named Cogwork Grinder.
        this.addAbility(new EntersBattlefieldAbility(new CogworkGrinderEffect()));
    }

    private CogworkGrinder(final CogworkGrinder card) {
        super(card);
    }

    @Override
    public CogworkGrinder copy() {
        return new CogworkGrinder(this);
    }
}

class CogworkGrinderEffect extends OneShotEffect {

    CogworkGrinderEffect() {
        super(Outcome.BoostCreature);
        staticText = "with X +1/+1 counters on it, where X is the number of cards you removed from the draft with cards named {this}";
    }

    private CogworkGrinderEffect(final CogworkGrinderEffect effect) {
        super(effect);
    }

    @Override
    public CogworkGrinderEffect copy() {
        return new CogworkGrinderEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = game.getPermanentEntering(source.getSourceId());
        if (permanent == null) {
            return false;
        }
        Object raw = game.getState().getValue(source.getControllerId() + "_cogwork_grinder_count");
        if (raw instanceof Integer && ((Integer) raw) > 0) {
            permanent.addCounters(CounterType.P1P1.createInstance((Integer) raw), source.getControllerId(), source, game);
        }
        return true;
    }
}
