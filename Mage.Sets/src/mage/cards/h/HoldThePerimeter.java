package mage.cards.h;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.CantBlockAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.InfoEffect;
import mage.abilities.keyword.DefenderAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.token.TokenImpl;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class HoldThePerimeter extends CardImpl {

    public HoldThePerimeter(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CONSPIRACY}, "");

        // (Start the game with this conspiracy face up in the command zone.)
        this.addAbility(new SimpleStaticAbility(
                Zone.COMMAND,
                new InfoEffect("(Start the game with this conspiracy face up in the command zone.)")
        ));

        // At the beginning of your first upkeep, create a 1/2 white Soldier creature token with defender.
        // At the beginning of each other player's first upkeep, that player creates a 1/1 red Goblin creature token with "This token can't block."
        this.addAbility(new HoldThePerimeterTriggeredAbility());
    }

    private HoldThePerimeter(final HoldThePerimeter card) {
        super(card);
    }

    @Override
    public HoldThePerimeter copy() {
        return new HoldThePerimeter(this);
    }
}

class HoldThePerimeterTriggeredAbility extends TriggeredAbilityImpl {

    private final Set<UUID> triggeredPlayers = new HashSet<>();

    HoldThePerimeterTriggeredAbility() {
        super(Zone.COMMAND, new HoldThePerimeterEffect(), false);
    }

    private HoldThePerimeterTriggeredAbility(final HoldThePerimeterTriggeredAbility ability) {
        super(ability);
        this.triggeredPlayers.addAll(ability.triggeredPlayers);
    }

    @Override
    public HoldThePerimeterTriggeredAbility copy() {
        return new HoldThePerimeterTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.UPKEEP_STEP_PRE;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        UUID activePlayerId = event.getPlayerId();
        if (activePlayerId != null && triggeredPlayers.add(activePlayerId)) {
            this.getEffects().setValue("upkeepPlayerId", activePlayerId);
            return true;
        }
        return false;
    }

    @Override
    public String getRule() {
        return "At the beginning of your first upkeep, create a 1/2 white Soldier creature token with defender. "
                + "At the beginning of each other player's first upkeep, that player creates a 1/1 red Goblin creature token with \"This token can't block.\"";
    }
}

class HoldThePerimeterEffect extends OneShotEffect {

    HoldThePerimeterEffect() {
        super(Outcome.PutCreatureInPlay);
    }

    private HoldThePerimeterEffect(final HoldThePerimeterEffect effect) {
        super(effect);
    }

    @Override
    public HoldThePerimeterEffect copy() {
        return new HoldThePerimeterEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        UUID upkeepPlayerId = (UUID) getValue("upkeepPlayerId");
        if (upkeepPlayerId == null) {
            return false;
        }
        if (source.isControlledBy(upkeepPlayerId)) {
            return new HoldThePerimeterSoldierToken().putOntoBattlefield(1, game, source, upkeepPlayerId);
        }
        return new HoldThePerimeterGoblinToken().putOntoBattlefield(1, game, source, upkeepPlayerId);
    }
}

class HoldThePerimeterSoldierToken extends TokenImpl {

    HoldThePerimeterSoldierToken() {
        super("Soldier Token", "1/2 white Soldier creature token with defender");
        cardType.add(CardType.CREATURE);
        color.setWhite(true);
        subtype.add(SubType.SOLDIER);
        power = new MageInt(1);
        toughness = new MageInt(2);
        this.addAbility(DefenderAbility.getInstance());
    }

    private HoldThePerimeterSoldierToken(final HoldThePerimeterSoldierToken token) {
        super(token);
    }

    @Override
    public HoldThePerimeterSoldierToken copy() {
        return new HoldThePerimeterSoldierToken(this);
    }
}

class HoldThePerimeterGoblinToken extends TokenImpl {

    HoldThePerimeterGoblinToken() {
        super("Goblin Token", "1/1 red Goblin creature token with \"This token can't block.\"");
        cardType.add(CardType.CREATURE);
        color.setRed(true);
        subtype.add(SubType.GOBLIN);
        power = new MageInt(1);
        toughness = new MageInt(1);
        this.addAbility(new CantBlockAbility());
    }

    private HoldThePerimeterGoblinToken(final HoldThePerimeterGoblinToken token) {
        super(token);
    }

    @Override
    public HoldThePerimeterGoblinToken copy() {
        return new HoldThePerimeterGoblinToken(this);
    }
}
