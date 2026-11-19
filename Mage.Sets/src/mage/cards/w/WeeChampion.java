package mage.cards.w;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.BoostSourceEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;

import java.util.Locale;
import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class WeeChampion extends CardImpl {

    public WeeChampion(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{R}");

        this.subtype.add(SubType.MINOTAUR);
        this.subtype.add(SubType.CHILD);
        this.subtype.add(SubType.GUEST);
        this.power = new MageInt(0);
        this.toughness = new MageInt(1);

        // Whenever you place a sticker, this creature gets +1/+1 until end of turn. If it's an art sticker, instead put a +1/+1 counter on this creature.
        this.addAbility(new WeeChampionTriggeredAbility());
    }

    private WeeChampion(final WeeChampion card) {
        super(card);
    }

    @Override
    public WeeChampion copy() {
        return new WeeChampion(this);
    }
}

class WeeChampionTriggeredAbility extends TriggeredAbilityImpl {

    WeeChampionTriggeredAbility() {
        super(Zone.BATTLEFIELD, new WeeChampionEffect(), false);
    }

    private WeeChampionTriggeredAbility(final WeeChampionTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public WeeChampionTriggeredAbility copy() {
        return new WeeChampionTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.COUNTER_ADDED;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!isControlledBy(event.getPlayerId())
                || event.getData() == null
                || !event.getData().toLowerCase(Locale.ROOT).contains("sticker")) {
            return false;
        }
        getEffects().setValue("artSticker", "art sticker".equalsIgnoreCase(event.getData()));
        return true;
    }

    @Override
    public String getRule() {
        return "Whenever you place a sticker, {this} gets +1/+1 until end of turn. "
                + "If it's an art sticker, instead put a +1/+1 counter on {this}.";
    }
}

class WeeChampionEffect extends OneShotEffect {

    WeeChampionEffect() {
        super(Outcome.BoostCreature);
    }

    private WeeChampionEffect(final WeeChampionEffect effect) {
        super(effect);
    }

    @Override
    public WeeChampionEffect copy() {
        return new WeeChampionEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        if (Boolean.TRUE.equals(getValue("artSticker"))) {
            Permanent perm = source.getSourcePermanentIfItStillExists(game);
            if (perm != null) {
                perm.addCounters(CounterType.P1P1.createInstance(), source.getControllerId(), source, game);
            }
        } else {
            game.addEffect(new BoostSourceEffect(1, 1, Duration.EndOfTurn), source);
        }
        return true;
    }
}
