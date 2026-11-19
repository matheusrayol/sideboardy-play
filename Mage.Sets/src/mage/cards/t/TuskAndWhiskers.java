package mage.cards.t;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.counter.AddCountersTargetEffect;
import mage.abilities.effects.common.counter.GetTicketCountersControllerEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.counters.Counter;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetControlledPermanent;
import mage.target.targetpointer.FixedTarget;

import java.util.Locale;
import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class TuskAndWhiskers extends CardImpl {

    public TuskAndWhiskers(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{G}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.ELEPHANT);
        this.subtype.add(SubType.MOUSE);
        this.subtype.add(SubType.PERFORMER);
        this.power = new MageInt(4);
        this.toughness = new MageInt(4);

        // Whenever you put an ability sticker on a creature, put a +1/+1 counter on that creature.
        this.addAbility(new TuskAndWhiskersTriggeredAbility());

        // {2}{G}{W}, {T}: You get {TK}. You may put a sticker on a nonland permanent you own.
        Ability ability = new SimpleActivatedAbility(
                new GetTicketCountersControllerEffect(1), new ManaCostsImpl<>("{2}{G}{W}")
        );
        ability.addCost(new TapSourceCost());
        ability.addEffect(new TuskAndWhiskersStickerEffect());
        this.addAbility(ability);
    }

    private TuskAndWhiskers(final TuskAndWhiskers card) {
        super(card);
    }

    @Override
    public TuskAndWhiskers copy() {
        return new TuskAndWhiskers(this);
    }
}

class TuskAndWhiskersTriggeredAbility extends TriggeredAbilityImpl {

    TuskAndWhiskersTriggeredAbility() {
        super(Zone.BATTLEFIELD, new AddCountersTargetEffect(CounterType.P1P1.createInstance()), false);
    }

    private TuskAndWhiskersTriggeredAbility(final TuskAndWhiskersTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public TuskAndWhiskersTriggeredAbility copy() {
        return new TuskAndWhiskersTriggeredAbility(this);
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
        Permanent p = game.getPermanent(event.getTargetId());
        if (p == null || !p.isCreature(game)) {
            return false;
        }
        this.getEffects().setTargetPointer(new FixedTarget(p, game));
        return true;
    }

    @Override
    public String getRule() {
        return "Whenever you put an ability sticker on a creature, put a +1/+1 counter on that creature.";
    }
}

class TuskAndWhiskersStickerEffect extends OneShotEffect {

    TuskAndWhiskersStickerEffect() {
        super(Outcome.BoostCreature);
        staticText = "You may put a sticker on a nonland permanent you own";
    }

    private TuskAndWhiskersStickerEffect(final TuskAndWhiskersStickerEffect effect) {
        super(effect);
    }

    @Override
    public TuskAndWhiskersStickerEffect copy() {
        return new TuskAndWhiskersStickerEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        TargetControlledPermanent target = new TargetControlledPermanent(
                0, 1, StaticFilters.FILTER_CONTROLLED_PERMANENT_NON_LAND, true
        );
        if (controller.choose(Outcome.Benefit, target, source, game)) {
            Permanent permanent = game.getPermanent(target.getFirstTarget());
            if (permanent != null) {
                permanent.addCounters(new Counter("sticker"), source.getControllerId(), source, game);
            }
        }
        return true;
    }
}
