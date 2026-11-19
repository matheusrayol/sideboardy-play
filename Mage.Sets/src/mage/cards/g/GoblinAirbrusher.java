package mage.cards.g;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.OneShotEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.token.TreasureToken;

import java.util.Locale;
import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class GoblinAirbrusher extends CardImpl {

    public GoblinAirbrusher(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{R}");

        this.subtype.add(SubType.GOBLIN);
        this.subtype.add(SubType.EMPLOYEE);
        this.power = new MageInt(2);
        this.toughness = new MageInt(1);

        // Whenever you place a sticker, create a Treasure token. If it's an art sticker, instead create two Treasure tokens.
        this.addAbility(new GoblinAirbrusherTriggeredAbility());
    }

    private GoblinAirbrusher(final GoblinAirbrusher card) {
        super(card);
    }

    @Override
    public GoblinAirbrusher copy() {
        return new GoblinAirbrusher(this);
    }
}

class GoblinAirbrusherTriggeredAbility extends TriggeredAbilityImpl {

    GoblinAirbrusherTriggeredAbility() {
        super(Zone.BATTLEFIELD, new GoblinAirbrusherEffect(), false);
    }

    private GoblinAirbrusherTriggeredAbility(final GoblinAirbrusherTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public GoblinAirbrusherTriggeredAbility copy() {
        return new GoblinAirbrusherTriggeredAbility(this);
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
        getEffects().setValue("artSticker", "art sticker".equalsIgnoreCase(event.getData()));
        return true;
    }

    @Override
    public String getRule() {
        return "Whenever you place a sticker, create a Treasure token. If it's an art sticker, instead create two Treasure tokens.";
    }
}

class GoblinAirbrusherEffect extends OneShotEffect {

    GoblinAirbrusherEffect() {
        super(Outcome.PutCardInPlay);
    }

    private GoblinAirbrusherEffect(final GoblinAirbrusherEffect effect) {
        super(effect);
    }

    @Override
    public GoblinAirbrusherEffect copy() {
        return new GoblinAirbrusherEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        int count = Boolean.TRUE.equals(getValue("artSticker")) ? 2 : 1;
        return new TreasureToken().putOntoBattlefield(count, game, source);
    }
}
