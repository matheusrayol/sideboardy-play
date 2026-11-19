package mage.cards.x;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldWithCountersAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.RollDieType;
import mage.constants.SubType;
import mage.counters.CounterType;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.events.RollDieEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class Xenosquirrels extends CardImpl {

    public Xenosquirrels(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{B}");

        this.subtype.add(SubType.ALIEN);
        this.subtype.add(SubType.SQUIRREL);
        this.power = new MageInt(0);
        this.toughness = new MageInt(0);

        // This creature enters with two +1/+1 counters on it.
        this.addAbility(new EntersBattlefieldWithCountersAbility(CounterType.P1P1.createInstance(2)));

        // After you roll a die, you may remove a +1/+1 counter from this creature. If you do, increase or decrease the result by 1.
        this.addAbility(new SimpleStaticAbility(new XenosquirrelsEffect()));
    }

    private Xenosquirrels(final Xenosquirrels card) {
        super(card);
    }

    @Override
    public Xenosquirrels copy() {
        return new Xenosquirrels(this);
    }
}

class XenosquirrelsEffect extends ReplacementEffectImpl {

    XenosquirrelsEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Benefit);
        staticText = "After you roll a die, you may remove a +1/+1 counter from {this}. "
                + "If you do, increase or decrease the result by 1";
    }

    private XenosquirrelsEffect(final XenosquirrelsEffect effect) {
        super(effect);
    }

    @Override
    public XenosquirrelsEffect copy() {
        return new XenosquirrelsEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ROLL_DIE;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        if (!source.isControlledBy(event.getTargetId())) {
            return false;
        }
        if (((RollDieEvent) event).getRollDieType() != RollDieType.NUMERICAL) {
            return false;
        }
        Permanent permanent = source.getSourcePermanentIfItStillExists(game);
        return permanent != null && permanent.getCounters(game).getCount(CounterType.P1P1) > 0;
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        Player controller = game.getPlayer(source.getControllerId());
        Permanent permanent = source.getSourcePermanentIfItStillExists(game);
        if (controller == null || permanent == null) {
            return false;
        }
        if (controller.chooseUse(
                Outcome.Benefit,
                "Remove a +1/+1 counter from Xenosquirrels to modify the die roll by 1?",
                source,
                game
        )) {
            if (permanent.removeCounters(CounterType.P1P1.createInstance(1), source, game) > 0) {
                boolean inc = controller.chooseUse(
                        Outcome.Benefit,
                        "Increase die result by 1? (No = decrease by 1)",
                        source,
                        game
                );
                ((RollDieEvent) event).incResultModifier(inc ? 1 : -1);
            }
        }
        return false;
    }
}
