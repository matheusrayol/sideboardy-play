package mage.cards.r;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.AttacksTriggeredAbility;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.BoostSourceEffect;
import mage.abilities.keyword.CrewAbility;
import mage.abilities.keyword.FlyingAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.counters.Counter;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class Rocketship extends CardImpl {

    public Rocketship(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{4}");

        this.subtype.add(SubType.VEHICLE);
        this.power = new MageInt(2);
        this.toughness = new MageInt(3);

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // When this Vehicle enters, you may put up to two name stickers on it.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new RocketshipEnterEffect(), true));

        // Whenever this Vehicle attacks, choose a letter. This Vehicle gets +1/+1 until end of turn for each name sticker on it that begins with the chosen letter.
        this.addAbility(new AttacksTriggeredAbility(new BoostSourceEffect(1, 1, Duration.EndOfTurn)
                .setText("choose a letter. {this} gets +1/+1 until end of turn for each name sticker on it that begins with the chosen letter")));

        // Crew 2
        this.addAbility(new CrewAbility(2));
    }

    private Rocketship(final Rocketship card) {
        super(card);
    }

    @Override
    public Rocketship copy() {
        return new Rocketship(this);
    }
}

class RocketshipEnterEffect extends OneShotEffect {

    RocketshipEnterEffect() {
        super(Outcome.BoostCreature);
        staticText = "put up to two name stickers on {this}";
    }

    private RocketshipEnterEffect(final RocketshipEnterEffect effect) {
        super(effect);
    }

    @Override
    public RocketshipEnterEffect copy() {
        return new RocketshipEnterEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = source.getSourcePermanentIfItStillExists(game);
        if (permanent != null) {
            permanent.addCounters(new Counter("name sticker", 2), source.getControllerId(), source, game);
        }
        return true;
    }
}
