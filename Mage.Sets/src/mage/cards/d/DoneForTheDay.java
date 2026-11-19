package mage.cards.d;

import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.triggers.BeginningOfEndStepTriggeredAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.TargetController;
import mage.counters.Counter;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.game.permanent.token.TreasureToken;
import mage.players.Player;
import mage.target.common.TargetControlledPermanent;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class DoneForTheDay extends CardImpl {

    public DoneForTheDay(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{2}{G}");

        // At the beginning of your end step, if you control an Employee, a Performer, or a Robot, you may get {TK} or create a Treasure token. If you control all three, you may put a sticker on a nonland permanent you own.
        this.addAbility(new BeginningOfEndStepTriggeredAbility(
                TargetController.YOU, new DoneForTheDayEffect(), true
        ));
    }

    private DoneForTheDay(final DoneForTheDay card) {
        super(card);
    }

    @Override
    public DoneForTheDay copy() {
        return new DoneForTheDay(this);
    }
}

class DoneForTheDayEffect extends OneShotEffect {

    DoneForTheDayEffect() {
        super(Outcome.Benefit);
        staticText = "if you control an Employee, a Performer, or a Robot, you may get {TK} or create a Treasure token. "
                + "If you control all three, you may put a sticker on a nonland permanent you own";
    }

    private DoneForTheDayEffect(final DoneForTheDayEffect effect) {
        super(effect);
    }

    @Override
    public DoneForTheDayEffect copy() {
        return new DoneForTheDayEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        boolean hasEmployee = false;
        boolean hasPerformer = false;
        boolean hasRobot = false;
        for (Permanent perm : game.getBattlefield().getAllActivePermanents(
                StaticFilters.FILTER_PERMANENT, source.getControllerId(), game
        )) {
            if (perm.hasSubtype(SubType.EMPLOYEE, game)) {
                hasEmployee = true;
            }
            if (perm.hasSubtype(SubType.PERFORMER, game)) {
                hasPerformer = true;
            }
            if (perm.hasSubtype(SubType.ROBOT, game)) {
                hasRobot = true;
            }
        }
        if (!hasEmployee && !hasPerformer && !hasRobot) {
            return true;
        }
        if (controller.chooseUse(Outcome.Benefit, "Get {TK}? (No = create a Treasure token)", source, game)) {
            controller.addCounters(CounterType.TICKET.createInstance(), source.getControllerId(), source, game);
        } else {
            new TreasureToken().putOntoBattlefield(1, game, source);
        }
        if (hasEmployee && hasPerformer && hasRobot) {
            TargetControlledPermanent target = new TargetControlledPermanent(
                    0, 1, StaticFilters.FILTER_CONTROLLED_PERMANENT_NON_LAND, true
            );
            if (controller.choose(Outcome.Benefit, target, source, game)) {
                Permanent permanent = game.getPermanent(target.getFirstTarget());
                if (permanent != null) {
                    permanent.addCounters(new Counter("sticker"), source.getControllerId(), source, game);
                }
            }
        }
        return true;
    }
}
