package mage.cards.o;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.keyword.TrampleAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.counters.Counter;
import mage.counters.CounterType;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class OSaurus extends CardImpl {

    public OSaurus(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{4}{G}{G}");

        this.subtype.add(SubType.ALIEN);
        this.subtype.add(SubType.DINOSAUR);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Trample
        this.addAbility(TrampleAbility.getInstance());

        // When this creature enters, you may put a name sticker on it. Put a +1/+1 counter on it for each unique vowel on that sticker.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new OSaurusEffect(), true));
    }

    private OSaurus(final OSaurus card) {
        super(card);
    }

    @Override
    public OSaurus copy() {
        return new OSaurus(this);
    }
}

class OSaurusEffect extends OneShotEffect {

    OSaurusEffect() {
        super(Outcome.BoostCreature);
        staticText = "put a name sticker on {this}. Put a +1/+1 counter on it for each unique vowel on that sticker";
    }

    private OSaurusEffect(final OSaurusEffect effect) {
        super(effect);
    }

    @Override
    public OSaurusEffect copy() {
        return new OSaurusEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = source.getSourcePermanentIfItStillExists(game);
        if (permanent != null) {
            permanent.addCounters(new Counter("name sticker"), source.getControllerId(), source, game);
            permanent.addCounters(CounterType.P1P1.createInstance(3), source.getControllerId(), source, game);
        }
        return true;
    }
}
