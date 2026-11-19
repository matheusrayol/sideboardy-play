package mage.cards.p;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.InfoEffect;
import mage.abilities.keyword.MenaceAbility;
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
public final class PyreticHunter extends CardImpl {

    public PyreticHunter(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{4}{R}");

        this.subtype.add(SubType.ELEMENTAL);
        this.subtype.add(SubType.CAT);
        this.power = new MageInt(0);
        this.toughness = new MageInt(0);

        // Reveal Pyretic Hunter as you draft it and note how many cards you've drafted this draft round, including this card.
        this.addAbility(new SimpleStaticAbility(
                Zone.ALL,
                new InfoEffect("Reveal {this} as you draft it and note how many cards you've drafted this draft round, including this card - not implemented.")
        ));

        // Menace
        this.addAbility(new MenaceAbility());

        // Pyretic Hunter enters with X +1/+1 counters on it, where X is the highest number you noted for cards named Pyretic Hunter.
        this.addAbility(new EntersBattlefieldAbility(new PyreticHunterEffect()));
    }

    private PyreticHunter(final PyreticHunter card) {
        super(card);
    }

    @Override
    public PyreticHunter copy() {
        return new PyreticHunter(this);
    }
}

class PyreticHunterEffect extends OneShotEffect {

    PyreticHunterEffect() {
        super(Outcome.BoostCreature);
        staticText = "with X +1/+1 counters on it, where X is the highest number you noted for cards named {this}";
    }

    private PyreticHunterEffect(final PyreticHunterEffect effect) {
        super(effect);
    }

    @Override
    public PyreticHunterEffect copy() {
        return new PyreticHunterEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = game.getPermanentEntering(source.getSourceId());
        if (permanent == null) {
            return false;
        }
        Object raw = game.getState().getValue(source.getControllerId() + "_pyretic_hunter_max");
        if (raw instanceof Integer && ((Integer) raw) > 0) {
            permanent.addCounters(CounterType.P1P1.createInstance((Integer) raw), source.getControllerId(), source, game);
        }
        return true;
    }
}
