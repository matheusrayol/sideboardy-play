package mage.cards.c;

import mage.MageInt;
import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.token.TreasureToken;
import mage.game.stack.StackObject;
import mage.target.Target;

import java.util.Locale;
import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class ChiraAllIn extends CardImpl {

    public ChiraAllIn(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.ROGUE);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Whenever you discard one or more cards at random, select one or more targets at random, choose one or more modes at random, flip one or more coins, or roll one or more dice, put a +1/+1 counter on Chira and create a tapped Treasure token.
        this.addAbility(new ChiraAllInTriggeredAbility());
    }

    private ChiraAllIn(final ChiraAllIn card) {
        super(card);
    }

    @Override
    public ChiraAllIn copy() {
        return new ChiraAllIn(this);
    }
}

class ChiraAllInTriggeredAbility extends TriggeredAbilityImpl {

    ChiraAllInTriggeredAbility() {
        super(Zone.BATTLEFIELD, new AddCountersSourceEffect(CounterType.P1P1.createInstance()), false);
        this.addEffect(new CreateTokenEffect(new TreasureToken(), 1, true).concatBy("and"));
        setTriggerPhrase("Whenever you discard one or more cards at random, select one or more targets at random, "
                + "choose one or more modes at random, flip one or more coins, or roll one or more dice, ");
    }

    private ChiraAllInTriggeredAbility(final ChiraAllInTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public ChiraAllInTriggeredAbility copy() {
        return new ChiraAllInTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.COIN_FLIPPED
                || event.getType() == GameEvent.EventType.DICE_ROLLED
                || event.getType() == GameEvent.EventType.DISCARDED_CARDS
                || event.getType() == GameEvent.EventType.SPELL_CAST
                || event.getType() == GameEvent.EventType.ACTIVATED_ABILITY
                || event.getType() == GameEvent.EventType.TRIGGERED_ABILITY;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        switch (event.getType()) {
            case COIN_FLIPPED:
                return isControlledBy(event.getPlayerId());
            case DICE_ROLLED:
                return isControlledBy(event.getTargetId());
            case DISCARDED_CARDS:
                return isControlledBy(event.getPlayerId()) && isRandomDiscardSource(event, game);
            case SPELL_CAST:
            case ACTIVATED_ABILITY:
            case TRIGGERED_ABILITY:
                return isControlledBy(event.getPlayerId()) && isRandomTargetOrMode(event, game);
            default:
                return false;
        }
    }

    private boolean isRandomDiscardSource(GameEvent event, Game game) {
        if (event.getSourceId() == null) {
            return false;
        }
        for (StackObject so : game.getStack()) {
            if (so.getSourceId().equals(event.getSourceId()) && so.getStackAbility() != null) {
                String rule = so.getStackAbility().getRule(true);
                if (rule != null && rule.toLowerCase(Locale.ENGLISH).contains("at random")) {
                    return true;
                }
            }
        }
        MageObject obj = game.getObject(event.getSourceId());
        if (obj != null) {
            for (Ability ability : obj.getAbilities()) {
                String rule = ability.getRule(true);
                if (rule != null) {
                    String lower = rule.toLowerCase(Locale.ENGLISH);
                    if (lower.contains("discard") && lower.contains("at random")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isRandomTargetOrMode(GameEvent event, Game game) {
        StackObject so = game.getStack().getStackObject(event.getTargetId());
        if (so == null) {
            for (StackObject candidate : game.getStack()) {
                if (candidate.getId().equals(event.getTargetId())
                        || (candidate.getStackAbility() != null && candidate.getStackAbility().getId().equals(event.getTargetId()))) {
                    so = candidate;
                    break;
                }
            }
        }
        if (so == null || so.getStackAbility() == null) {
            return false;
        }
        Ability ability = so.getStackAbility();
        for (Target target : ability.getTargets()) {
            if (target.isRandom() && !target.getTargets().isEmpty()) {
                return true;
            }
        }
        if (ability.getModes().size() > 1) {
            String rule = ability.getRule(true);
            if (rule != null && rule.toLowerCase(Locale.ENGLISH).contains("at random")) {
                return true;
            }
            try {
                String modesText = ability.getModes().getText();
                if (modesText != null && modesText.toLowerCase(Locale.ENGLISH).contains("at random")) {
                    return true;
                }
            } catch (UnsupportedOperationException ignored) {
                // Non-standard mode configuration
            }
        }
        return false;
    }
}
