package mage.cards.l;

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
public final class LurkingAutomaton extends CardImpl {

    public LurkingAutomaton(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{5}");

        this.subtype.add(SubType.CONSTRUCT);
        this.power = new MageInt(0);
        this.toughness = new MageInt(0);

        // Reveal Lurking Automaton as you draft it and note how many cards you've drafted this draft round, including this card.
        this.addAbility(new SimpleStaticAbility(
                Zone.ALL,
                new InfoEffect("Reveal {this} as you draft it and note how many cards you've drafted this draft round, including this card - not implemented.")
        ));

        // Lurking Automaton enters with X +1/+1 counters on it, where X is the highest number you noted for cards named Lurking Automaton.
        this.addAbility(new EntersBattlefieldAbility(new LurkingAutomatonEffect()));
    }

    private LurkingAutomaton(final LurkingAutomaton card) {
        super(card);
    }

    @Override
    public LurkingAutomaton copy() {
        return new LurkingAutomaton(this);
    }
}

class LurkingAutomatonEffect extends OneShotEffect {

    LurkingAutomatonEffect() {
        super(Outcome.BoostCreature);
        staticText = "with X +1/+1 counters on it, where X is the highest number you noted for cards named {this}";
    }

    private LurkingAutomatonEffect(final LurkingAutomatonEffect effect) {
        super(effect);
    }

    @Override
    public LurkingAutomatonEffect copy() {
        return new LurkingAutomatonEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = game.getPermanentEntering(source.getSourceId());
        if (permanent == null) {
            return false;
        }
        Object raw = game.getState().getValue(source.getControllerId() + "_lurking_automaton_max");
        if (raw instanceof Integer && ((Integer) raw) > 0) {
            permanent.addCounters(CounterType.P1P1.createInstance((Integer) raw), source.getControllerId(), source, game);
        }
        return true;
    }
}
