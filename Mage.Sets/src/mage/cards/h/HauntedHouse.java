package mage.cards.h;

import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.delayed.AtTheBeginOfNextEndStepDelayedTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.ExileTargetEffect;
import mage.abilities.effects.common.continuous.GainAbilityTargetEffect;
import mage.abilities.keyword.HasteAbility;
import mage.cards.Card;
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
import mage.players.Player;
import mage.target.common.TargetCardInYourGraveyard;
import mage.target.targetpointer.FixedTarget;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class HauntedHouse extends CardImpl {

    public HauntedHouse(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "");

        this.subtype.add(SubType.ATTRACTION);

        // Visit — Return target creature card from your graveyard to the battlefield. It gains haste. Exile it at the beginning of your next end step.
        Ability ability = new HauntedHouseTriggeredAbility();
        ability.addTarget(new TargetCardInYourGraveyard(StaticFilters.FILTER_CARD_CREATURE_YOUR_GRAVEYARD));
        this.addAbility(ability.withFlavorWord("Visit"));
    }

    private HauntedHouse(final HauntedHouse card) {
        super(card);
    }

    @Override
    public HauntedHouse copy() {
        return new HauntedHouse(this);
    }
}

class HauntedHouseTriggeredAbility extends TriggeredAbilityImpl {

    HauntedHouseTriggeredAbility() {
        super(Zone.BATTLEFIELD, new HauntedHouseEffect(), false);
    }

    private HauntedHouseTriggeredAbility(final HauntedHouseTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public HauntedHouseTriggeredAbility copy() {
        return new HauntedHouseTriggeredAbility(this);
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
        return result == 3 || result == 6;
    }

    @Override
    public String getRule() {
        return "Return target creature card from your graveyard to the battlefield. "
                + "It gains haste. Exile it at the beginning of your next end step.";
    }
}

class HauntedHouseEffect extends OneShotEffect {

    HauntedHouseEffect() {
        super(Outcome.PutCreatureInPlay);
        staticText = "Return target creature card from your graveyard to the battlefield. "
                + "It gains haste. Exile it at the beginning of your next end step";
    }

    private HauntedHouseEffect(final HauntedHouseEffect effect) {
        super(effect);
    }

    @Override
    public HauntedHouseEffect copy() {
        return new HauntedHouseEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Card card = game.getCard(getTargetPointer().getFirst(game, source));
        if (controller == null || card == null) {
            return false;
        }
        if (controller.moveCards(card, Zone.BATTLEFIELD, source, game)) {
            Permanent permanent = game.getPermanent(card.getId());
            if (permanent != null) {
                GainAbilityTargetEffect hasteEffect = new GainAbilityTargetEffect(
                        HasteAbility.getInstance(), Duration.Custom
                );
                hasteEffect.setTargetPointer(new FixedTarget(permanent, game));
                game.addEffect(hasteEffect, source);

                ExileTargetEffect exileEffect = new ExileTargetEffect();
                exileEffect.setTargetPointer(new FixedTarget(permanent, game));
                game.addDelayedTriggeredAbility(
                        new AtTheBeginOfNextEndStepDelayedTriggeredAbility(exileEffect), source
                );
            }
        }
        return true;
    }
}
