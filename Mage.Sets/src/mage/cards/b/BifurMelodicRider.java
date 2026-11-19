package mage.cards.b;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldOrAttacksSourceTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.EnduringStoryCondition;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.abilities.effects.common.counter.AddCountersTargetEffect;
import mage.abilities.hint.common.EnduringStoryHint;
import mage.abilities.keyword.StoriedAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterControlledPermanent;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.target.common.TargetCreaturePermanent;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author sideboardy-play
 */
public final class BifurMelodicRider extends CardImpl {

    public BifurMelodicRider(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{4}{R/W}{R/W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.DWARF);
        this.subtype.add(SubType.BARD);
        this.power = new MageInt(4);
        this.toughness = new MageInt(5);

        // Storied
        this.addAbility(new StoriedAbility());

        // Whenever Bifur enters or attacks, put a +1/+1 counter on target creature.
        Ability ability = new EntersBattlefieldOrAttacksSourceTriggeredAbility(
                new AddCountersTargetEffect(CounterType.P1P1.createInstance())
        );
        ability.addTarget(new TargetCreaturePermanent());
        this.addAbility(ability);

        // As long as you have an enduring story, if a triggered ability of a Dwarf you control triggers, that ability triggers an additional time.
        this.addAbility(new SimpleStaticAbility(new BifurMelodicRiderEffect())
                .addHint(EnduringStoryHint.instance));
    }

    private BifurMelodicRider(final BifurMelodicRider card) {
        super(card);
    }

    @Override
    public BifurMelodicRider copy() {
        return new BifurMelodicRider(this);
    }
}

class BifurMelodicRiderEffect extends ReplacementEffectImpl {

    private static final FilterPermanent filter = new FilterControlledPermanent(SubType.DWARF);

    BifurMelodicRiderEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Benefit);
        this.staticText = "as long as you have an enduring story, if a triggered ability of a Dwarf you control triggers, that ability triggers an additional time";
    }

    private BifurMelodicRiderEffect(final BifurMelodicRiderEffect effect) {
        super(effect);
    }

    @Override
    public BifurMelodicRiderEffect copy() {
        return new BifurMelodicRiderEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.NUMBER_OF_TRIGGERS;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        if (!source.isControlledBy(event.getPlayerId())
                || !EnduringStoryCondition.instance.apply(game, source)) {
            return false;
        }
        Permanent permanentSource = game.getPermanentOrLKIBattlefield(event.getSourceId());
        return permanentSource != null && filter.match(permanentSource, source.getControllerId(), source, game);
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        event.setAmount(CardUtil.overflowInc(event.getAmount(), 1));
        return false;
    }
}
