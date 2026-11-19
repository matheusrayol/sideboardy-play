package mage.cards.t;

import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.ReturnToHandSourceEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.m.MelkotianAdjudicator;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.players.Player;
import mage.target.common.TargetOpponent;

import java.util.UUID;

/**
 * @author sideboardy-play
 */
public final class TellMeWhatYouSee extends CardImpl {

    public TellMeWhatYouSee(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{1}{B}");

        // When this enchantment enters, target opponent may have you draw a card. If they don't, they lose 4 life and you surveil 1.
        Ability etb = new EntersBattlefieldTriggeredAbility(new TellMeWhatYouSeeEffect());
        etb.addTarget(new TargetOpponent());
        this.addAbility(etb);

        // Whenever you face a dilemma, you may return this enchantment to its owner's hand.
        this.addAbility(new TellMeWhatYouSeeDilemmaTriggeredAbility());
    }

    private TellMeWhatYouSee(final TellMeWhatYouSee card) {
        super(card);
    }

    @Override
    public TellMeWhatYouSee copy() {
        return new TellMeWhatYouSee(this);
    }
}

class TellMeWhatYouSeeEffect extends OneShotEffect {

    TellMeWhatYouSeeEffect() {
        super(Outcome.DrawCard);
        this.staticText = "target opponent may have you draw a card. If they don't, they lose 4 life and you surveil 1";
    }

    private TellMeWhatYouSeeEffect(final TellMeWhatYouSeeEffect effect) {
        super(effect);
    }

    @Override
    public TellMeWhatYouSeeEffect copy() {
        return new TellMeWhatYouSeeEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Player opponent = game.getPlayer(getTargetPointer().getFirst(game, source));
        if (controller == null || opponent == null) {
            return false;
        }
        if (opponent.chooseUse(Outcome.Detriment, "Have " + controller.getLogName() + " draw a card?", source, game)) {
            controller.drawCards(1, source, game);
        } else {
            opponent.loseLife(4, game, source, false);
            controller.surveil(1, source, game);
        }
        return true;
    }
}

class TellMeWhatYouSeeDilemmaTriggeredAbility extends TriggeredAbilityImpl {

    TellMeWhatYouSeeDilemmaTriggeredAbility() {
        super(Zone.BATTLEFIELD, new ReturnToHandSourceEffect(true), true);
        setTriggerPhrase("Whenever you face a dilemma, ");
    }

    private TellMeWhatYouSeeDilemmaTriggeredAbility(final TellMeWhatYouSeeDilemmaTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public TellMeWhatYouSeeDilemmaTriggeredAbility copy() {
        return new TellMeWhatYouSeeDilemmaTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.SPELL_CAST
                || event.getType() == GameEvent.EventType.ACTIVATED_ABILITY
                || event.getType() == GameEvent.EventType.TRIGGERED_ABILITY;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        return MelkotianAdjudicator.isDilemmaEvent(event, getControllerId(), game);
    }
}
