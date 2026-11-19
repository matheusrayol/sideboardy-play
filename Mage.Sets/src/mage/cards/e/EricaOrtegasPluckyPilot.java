package mage.cards.e;

import java.util.UUID;
import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.BecomesTappedSourceTriggeredAbility;
import mage.abilities.common.delayed.AtTheBeginOfNextEndStepDelayedTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.ReturnToHandChosenControlledPermanentEffect;
import mage.abilities.keyword.HasteAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.players.Player;
import mage.target.common.TargetCardInHand;

/**
 *
 * @author muz
 */
public final class EricaOrtegasPluckyPilot extends CardImpl {

    public EricaOrtegasPluckyPilot(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.PILOT);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Haste
        this.addAbility(HasteAbility.getInstance());

        // Whenever Erica Ortegas becomes tapped, you may put a land card from your hand onto the battlefield. If you do, return a land you control to its owner's hand at the beginning of the next end step.
        this.addAbility(new BecomesTappedSourceTriggeredAbility(new EricaOrtegasPluckyPilotEffect(), true));
    }

    private EricaOrtegasPluckyPilot(final EricaOrtegasPluckyPilot card) {
        super(card);
    }

    @Override
    public EricaOrtegasPluckyPilot copy() {
        return new EricaOrtegasPluckyPilot(this);
    }
}

class EricaOrtegasPluckyPilotEffect extends OneShotEffect {

    EricaOrtegasPluckyPilotEffect() {
        super(Outcome.PutLandInPlay);
        staticText = "put a land card from your hand onto the battlefield. If you do, return a land you control to its owner's hand at the beginning of the next end step";
    }

    private EricaOrtegasPluckyPilotEffect(final EricaOrtegasPluckyPilotEffect effect) {
        super(effect);
    }

    @Override
    public EricaOrtegasPluckyPilotEffect copy() {
        return new EricaOrtegasPluckyPilotEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        TargetCardInHand target = new TargetCardInHand(StaticFilters.FILTER_CARD_LAND);
        if (!target.canChoose(controller.getId(), source, game)) {
            return false;
        }
        if (!controller.choose(Outcome.PutLandInPlay, target, source, game)) {
            return false;
        }
        Card land = game.getCard(target.getFirstTarget());
        if (land == null) {
            return false;
        }
        if (controller.moveCards(land, Zone.BATTLEFIELD, source, game)) {
            game.addDelayedTriggeredAbility(new AtTheBeginOfNextEndStepDelayedTriggeredAbility(
                    new ReturnToHandChosenControlledPermanentEffect(StaticFilters.FILTER_CONTROLLED_PERMANENT_LAND)
            ), source);
            return true;
        }
        return false;
    }
}
