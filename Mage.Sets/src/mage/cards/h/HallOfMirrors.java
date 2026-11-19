package mage.cards.h;

import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.OneShotEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.RollDieType;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.events.DieRolledEvent;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.target.common.TargetControlledCreaturePermanent;
import mage.util.functions.EmptyCopyApplier;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class HallOfMirrors extends CardImpl {

    public HallOfMirrors(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "");

        this.subtype.add(SubType.ATTRACTION);

        // Visit — Choose target creature you control. Each other creature you control becomes a copy of that creature until end of turn, except it isn't legendary.
        Ability ability = new HallOfMirrorsTriggeredAbility();
        ability.addTarget(new TargetControlledCreaturePermanent());
        this.addAbility(ability.withFlavorWord("Visit"));
    }

    private HallOfMirrors(final HallOfMirrors card) {
        super(card);
    }

    @Override
    public HallOfMirrors copy() {
        return new HallOfMirrors(this);
    }
}

class HallOfMirrorsTriggeredAbility extends TriggeredAbilityImpl {

    HallOfMirrorsTriggeredAbility() {
        super(Zone.BATTLEFIELD, new HallOfMirrorsEffect(), false);
    }

    private HallOfMirrorsTriggeredAbility(final HallOfMirrorsTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public HallOfMirrorsTriggeredAbility copy() {
        return new HallOfMirrorsTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DIE_ROLLED;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!isControlledBy(event.getTargetId())) {
            return false;
        }
        DieRolledEvent drEvent = (DieRolledEvent) event;
        if (drEvent.getRollDieType() != RollDieType.NUMERICAL) {
            return false;
        }
        int result = drEvent.getResult();
        return result == 2 || result == 6;
    }

    @Override
    public String getRule() {
        return "Choose target creature you control. Each other creature you control becomes a copy of that creature until end of turn, except it isn't legendary.";
    }
}

class HallOfMirrorsEffect extends OneShotEffect {

    HallOfMirrorsEffect() {
        super(Outcome.Copy);
        staticText = "Choose target creature you control. "
                + "Each other creature you control becomes a copy of that creature until end of turn, except it isn't legendary";
    }

    private HallOfMirrorsEffect(final HallOfMirrorsEffect effect) {
        super(effect);
    }

    @Override
    public HallOfMirrorsEffect copy() {
        return new HallOfMirrorsEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent targetPerm = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (targetPerm == null) {
            return false;
        }
        for (Permanent otherPerm : game.getBattlefield().getAllActivePermanents(
                StaticFilters.FILTER_PERMANENT_CREATURE, source.getControllerId(), game
        )) {
            if (!otherPerm.getId().equals(targetPerm.getId())) {
                game.copyPermanent(Duration.EndOfTurn, targetPerm, otherPerm.getId(), source, new EmptyCopyApplier());
            }
        }
        return true;
    }
}
