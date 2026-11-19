package mage.cards.m;

import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.TapTargetEffect;
import mage.abilities.keyword.FlashAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.counters.Counter;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.target.common.TargetCreaturePermanent;

import java.util.Locale;
import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class MakeASplash extends CardImpl {

    public MakeASplash(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{2}{U}");

        // Flash
        this.addAbility(FlashAbility.getInstance());

        // When this enchantment enters, you may put a name sticker on it.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new MakeASplashEnterEffect(), true));

        // Whenever you put a sticker on this enchantment, tap up to X target creatures, where X is the number of u's in name stickers on this enchantment.
        this.addAbility(new MakeASplashTriggeredAbility());
    }

    private MakeASplash(final MakeASplash card) {
        super(card);
    }

    @Override
    public MakeASplash copy() {
        return new MakeASplash(this);
    }
}

class MakeASplashEnterEffect extends OneShotEffect {

    MakeASplashEnterEffect() {
        super(Outcome.Benefit);
        staticText = "put a name sticker on {this}";
    }

    private MakeASplashEnterEffect(final MakeASplashEnterEffect effect) {
        super(effect);
    }

    @Override
    public MakeASplashEnterEffect copy() {
        return new MakeASplashEnterEffect(this);
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

class MakeASplashTriggeredAbility extends TriggeredAbilityImpl {

    MakeASplashTriggeredAbility() {
        super(Zone.BATTLEFIELD, new TapTargetEffect(), false);
        this.addTarget(new TargetCreaturePermanent(0, 2));
    }

    private MakeASplashTriggeredAbility(final MakeASplashTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public MakeASplashTriggeredAbility copy() {
        return new MakeASplashTriggeredAbility(this);
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
        return "Whenever you put a sticker on {this}, tap up to X target creatures, "
                + "where X is the number of u's in name stickers on {this}.";
    }
}
