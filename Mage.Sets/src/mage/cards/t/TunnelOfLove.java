package mage.cards.t;

import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.OneShotEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.RollDieType;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.events.DieRolledEvent;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.TargetPermanent;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class TunnelOfLove extends CardImpl {

    public TunnelOfLove(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "");

        this.subtype.add(SubType.ATTRACTION);

        // Visit — Choose an opponent. They choose a creature they control, then you choose a creature you control. You may exile the chosen creatures. If you do, return them to the battlefield under their owners' control at the beginning of the next end step. Otherwise, the chosen creatures fight each other.
        this.addAbility(new TunnelOfLoveTriggeredAbility().withFlavorWord("Visit"));
    }

    private TunnelOfLove(final TunnelOfLove card) {
        super(card);
    }

    @Override
    public TunnelOfLove copy() {
        return new TunnelOfLove(this);
    }
}

class TunnelOfLoveTriggeredAbility extends TriggeredAbilityImpl {

    TunnelOfLoveTriggeredAbility() {
        super(Zone.BATTLEFIELD, new TunnelOfLoveEffect(), false);
    }

    private TunnelOfLoveTriggeredAbility(final TunnelOfLoveTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public TunnelOfLoveTriggeredAbility copy() {
        return new TunnelOfLoveTriggeredAbility(this);
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
        return "Choose an opponent. They choose a creature they control, then you choose a creature you control. "
                + "You may exile the chosen creatures. If you do, return them to the battlefield under their owners' control at the beginning of the next end step. "
                + "Otherwise, the chosen creatures fight each other.";
    }
}

class TunnelOfLoveEffect extends OneShotEffect {

    TunnelOfLoveEffect() {
        super(Outcome.Damage);
        staticText = "Choose an opponent. They choose a creature they control, then you choose a creature you control. "
                + "You may exile the chosen creatures. If you do, return them to the battlefield under their owners' control at the beginning of the next end step. "
                + "Otherwise, the chosen creatures fight each other";
    }

    private TunnelOfLoveEffect(final TunnelOfLoveEffect effect) {
        super(effect);
    }

    @Override
    public TunnelOfLoveEffect copy() {
        return new TunnelOfLoveEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        TargetPermanent oppTarget = new TargetPermanent(0, 1, StaticFilters.FILTER_OPPONENTS_PERMANENT_CREATURE, true);
        TargetPermanent myTarget = new TargetPermanent(0, 1, StaticFilters.FILTER_CONTROLLED_CREATURE, true);
        if (controller.choose(Outcome.Damage, oppTarget, source, game)
                && controller.choose(Outcome.Damage, myTarget, source, game)) {
            Permanent oppCreature = game.getPermanent(oppTarget.getFirstTarget());
            Permanent myCreature = game.getPermanent(myTarget.getFirstTarget());
            if (myCreature != null && oppCreature != null) {
                myCreature.fight(oppCreature, source, game);
            }
        }
        return true;
    }
}
