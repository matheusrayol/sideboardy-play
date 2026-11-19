package mage.cards.n;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.RollDieType;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.DieRolledEvent;
import mage.game.events.GameEvent;
import mage.game.events.RollDieEvent;
import mage.game.permanent.token.TokenImpl;
import mage.players.Player;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class NightShiftOfTheLivingDead extends CardImpl {

    public NightShiftOfTheLivingDead(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{3}{B}");

        // After you roll a die, you may pay 1 life. If you do, increase or decrease the result by 1. Do this only once each turn.
        this.addAbility(new SimpleStaticAbility(new NightShiftReplacementEffect()));

        // Whenever you roll a 6, create a 2/2 black Zombie Employee creature token.
        this.addAbility(new NightShiftTriggeredAbility());
    }

    private NightShiftOfTheLivingDead(final NightShiftOfTheLivingDead card) {
        super(card);
    }

    @Override
    public NightShiftOfTheLivingDead copy() {
        return new NightShiftOfTheLivingDead(this);
    }
}

class NightShiftReplacementEffect extends ReplacementEffectImpl {

    NightShiftReplacementEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Benefit);
        staticText = "After you roll a die, you may pay 1 life. If you do, increase or decrease the result by 1. "
                + "Do this only once each turn";
    }

    private NightShiftReplacementEffect(final NightShiftReplacementEffect effect) {
        super(effect);
    }

    @Override
    public NightShiftReplacementEffect copy() {
        return new NightShiftReplacementEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ROLL_DIE;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        if (!source.isControlledBy(event.getTargetId())) {
            return false;
        }
        if (((RollDieEvent) event).getRollDieType() != RollDieType.NUMERICAL) {
            return false;
        }
        Object usedTurn = game.getState().getValue("nightShift_turn_" + source.getSourceId());
        return !Integer.valueOf(game.getTurnNum()).equals(usedTurn);
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null || !controller.canPayLifeCost(source) || controller.getLife() < 1) {
            return false;
        }
        if (controller.chooseUse(
                Outcome.Benefit,
                "Pay 1 life to increase or decrease the die result by 1?",
                source,
                game
        )) {
            controller.loseLife(1, game, source, false);
            game.getState().setValue("nightShift_turn_" + source.getSourceId(), game.getTurnNum());
            boolean inc = controller.chooseUse(
                    Outcome.Benefit,
                    "Increase die result by 1? (No = decrease by 1)",
                    source,
                    game
            );
            ((RollDieEvent) event).incResultModifier(inc ? 1 : -1);
        }
        return false;
    }
}

class NightShiftTriggeredAbility extends TriggeredAbilityImpl {

    NightShiftTriggeredAbility() {
        super(Zone.BATTLEFIELD, new CreateTokenEffect(new NightShiftZombieEmployeeToken()), false);
    }

    private NightShiftTriggeredAbility(final NightShiftTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public NightShiftTriggeredAbility copy() {
        return new NightShiftTriggeredAbility(this);
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
        return drEvent.getRollDieType() == RollDieType.NUMERICAL && drEvent.getResult() == 6;
    }

    @Override
    public String getRule() {
        return "Whenever you roll a 6, create a 2/2 black Zombie Employee creature token.";
    }
}

class NightShiftZombieEmployeeToken extends TokenImpl {

    NightShiftZombieEmployeeToken() {
        super("Zombie Employee Token", "2/2 black Zombie Employee creature token");
        cardType.add(CardType.CREATURE);
        color.setBlack(true);
        subtype.add(SubType.ZOMBIE);
        subtype.add(SubType.EMPLOYEE);
        power = new MageInt(2);
        toughness = new MageInt(2);
    }

    private NightShiftZombieEmployeeToken(final NightShiftZombieEmployeeToken token) {
        super(token);
    }

    @Override
    public NightShiftZombieEmployeeToken copy() {
        return new NightShiftZombieEmployeeToken(this);
    }
}
