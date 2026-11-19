package mage.cards.s;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.InfoEffect;
import mage.abilities.keyword.DefenderAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.token.TokenImpl;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class SentinelDispatch extends CardImpl {

    public SentinelDispatch(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CONSPIRACY}, "");

        // (Start the game with this conspiracy face up in the command zone.)
        this.addAbility(new SimpleStaticAbility(
                Zone.COMMAND,
                new InfoEffect("(Start the game with this conspiracy face up in the command zone.)")
        ));

        // At the beginning of the first upkeep, create a 1/1 colorless Construct artifact creature token with defender.
        this.addAbility(new SentinelDispatchTriggeredAbility());
    }

    private SentinelDispatch(final SentinelDispatch card) {
        super(card);
    }

    @Override
    public SentinelDispatch copy() {
        return new SentinelDispatch(this);
    }
}

class SentinelDispatchTriggeredAbility extends TriggeredAbilityImpl {

    private boolean triggeredOnce = false;

    SentinelDispatchTriggeredAbility() {
        super(Zone.COMMAND, new CreateTokenEffect(new SentinelDispatchConstructToken()), false);
    }

    private SentinelDispatchTriggeredAbility(final SentinelDispatchTriggeredAbility ability) {
        super(ability);
        this.triggeredOnce = ability.triggeredOnce;
    }

    @Override
    public SentinelDispatchTriggeredAbility copy() {
        return new SentinelDispatchTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.UPKEEP_STEP_PRE;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!triggeredOnce && game.getTurnNum() == 1) {
            triggeredOnce = true;
            return true;
        }
        return false;
    }

    @Override
    public String getRule() {
        return "At the beginning of the first upkeep, create a 1/1 colorless Construct artifact creature token with defender.";
    }
}

class SentinelDispatchConstructToken extends TokenImpl {

    SentinelDispatchConstructToken() {
        super("Construct Token", "1/1 colorless Construct artifact creature token with defender");
        cardType.add(CardType.ARTIFACT);
        cardType.add(CardType.CREATURE);
        subtype.add(SubType.CONSTRUCT);
        power = new MageInt(1);
        toughness = new MageInt(1);
        this.addAbility(DefenderAbility.getInstance());
    }

    private SentinelDispatchConstructToken(final SentinelDispatchConstructToken token) {
        super(token);
    }

    @Override
    public SentinelDispatchConstructToken copy() {
        return new SentinelDispatchConstructToken(this);
    }
}
