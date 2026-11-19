package mage.cards.b;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.counters.Counter;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class BirdGetsTheWorm extends CardImpl {

    public BirdGetsTheWorm(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{W}{W}");

        this.subtype.add(SubType.BIRD);
        this.subtype.add(SubType.GUEST);
        this.power = new MageInt(2);
        this.toughness = new MageInt(4);

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // When this creature enters, you may put a name sticker on it. You gain X life, where X is the number of unique vowels on that sticker.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new BirdGetsTheWormEffect(), true));
    }

    private BirdGetsTheWorm(final BirdGetsTheWorm card) {
        super(card);
    }

    @Override
    public BirdGetsTheWorm copy() {
        return new BirdGetsTheWorm(this);
    }
}

class BirdGetsTheWormEffect extends OneShotEffect {

    BirdGetsTheWormEffect() {
        super(Outcome.GainLife);
        staticText = "put a name sticker on {this}. You gain X life, where X is the number of unique vowels on that sticker";
    }

    private BirdGetsTheWormEffect(final BirdGetsTheWormEffect effect) {
        super(effect);
    }

    @Override
    public BirdGetsTheWormEffect copy() {
        return new BirdGetsTheWormEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        Permanent permanent = source.getSourcePermanentIfItStillExists(game);
        if (permanent != null) {
            permanent.addCounters(new Counter("name sticker"), source.getControllerId(), source, game);
        }
        controller.gainLife(3, game, source);
        return true;
    }
}
