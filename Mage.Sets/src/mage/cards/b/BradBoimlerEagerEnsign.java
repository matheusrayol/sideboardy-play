package mage.cards.b;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.BecomesTappedSourceTriggeredAbility;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.abilities.effects.common.AddContinuousEffectToGame;
import mage.abilities.keyword.LifelinkAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author sideboardy-play
 */
public final class BradBoimlerEagerEnsign extends CardImpl {

    public BradBoimlerEagerEnsign(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.OFFICER);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Lifelink
        this.addAbility(LifelinkAbility.getInstance());

        // Whenever Brad Boimler becomes tapped, until end of turn, if one or more counters would be put on a permanent you control, that many plus one of each of those kinds of counters are put on that permanent instead.
        this.addAbility(new BecomesTappedSourceTriggeredAbility(
                new AddContinuousEffectToGame(new BradBoimlerEagerEnsignEffect())
        ));
    }

    private BradBoimlerEagerEnsign(final BradBoimlerEagerEnsign card) {
        super(card);
    }

    @Override
    public BradBoimlerEagerEnsign copy() {
        return new BradBoimlerEagerEnsign(this);
    }
}

class BradBoimlerEagerEnsignEffect extends ReplacementEffectImpl {

    BradBoimlerEagerEnsignEffect() {
        super(Duration.EndOfTurn, Outcome.BoostCreature, false);
        this.staticText = "until end of turn, if one or more counters would be put on a permanent you control, "
                + "that many plus one of each of those kinds of counters are put on that permanent instead";
    }

    private BradBoimlerEagerEnsignEffect(final BradBoimlerEagerEnsignEffect effect) {
        super(effect);
    }

    @Override
    public BradBoimlerEagerEnsignEffect copy() {
        return new BradBoimlerEagerEnsignEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ADD_COUNTERS;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        if (event.getAmount() <= 0) {
            return false;
        }
        Permanent permanent = game.getPermanent(event.getTargetId());
        if (permanent == null) {
            permanent = game.getPermanentEntering(event.getTargetId());
        }
        return permanent != null
                && StaticFilters.FILTER_CONTROLLED_PERMANENT.match(permanent, source.getControllerId(), source, game);
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        event.setAmountForCounters(CardUtil.overflowInc(event.getAmount(), 1), true);
        return false;
    }
}
