package mage.cards.b;

import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.DamageTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.counters.Counter;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.target.common.TargetAnyTarget;

import java.util.Locale;
import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class BallsOfFire extends CardImpl {

    public BallsOfFire(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{3}{R}");

        // When this enchantment enters, you may put a name sticker on it.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new BallsOfFireEnterEffect(), true));

        // Whenever you put a sticker on this enchantment, it deals damage equal to the number of o's in name stickers on this enchantment to any target.
        this.addAbility(new BallsOfFireTriggeredAbility());
    }

    private BallsOfFire(final BallsOfFire card) {
        super(card);
    }

    @Override
    public BallsOfFire copy() {
        return new BallsOfFire(this);
    }
}

class BallsOfFireEnterEffect extends OneShotEffect {

    BallsOfFireEnterEffect() {
        super(Outcome.Benefit);
        staticText = "put a name sticker on {this}";
    }

    private BallsOfFireEnterEffect(final BallsOfFireEnterEffect effect) {
        super(effect);
    }

    @Override
    public BallsOfFireEnterEffect copy() {
        return new BallsOfFireEnterEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = source.getSourcePermanentIfItStillExists(game);
        if (permanent != null) {
            permanent.addCounters(new Counter("name sticker"), source.getControllerId(), source, game);
        }
        return true;
    }
}

class BallsOfFireTriggeredAbility extends TriggeredAbilityImpl {

    BallsOfFireTriggeredAbility() {
        super(Zone.BATTLEFIELD, new DamageTargetEffect(2), false);
        this.addTarget(new TargetAnyTarget());
    }

    private BallsOfFireTriggeredAbility(final BallsOfFireTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public BallsOfFireTriggeredAbility copy() {
        return new BallsOfFireTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.COUNTER_ADDED;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        return event.getTargetId().equals(getSourceId())
                && event.getData() != null
                && event.getData().toLowerCase(Locale.ROOT).contains("sticker");
    }

    @Override
    public String getRule() {
        return "Whenever you put a sticker on {this}, it deals damage equal to the number of o's in name stickers on {this} to any target.";
    }
}
