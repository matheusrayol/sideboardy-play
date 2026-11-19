package mage.cards.s;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.SourceOnBattlefieldOrCommandZoneCondition;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.abilities.effects.common.DamageControllerEffect;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.watchers.common.AbilityResolvedWatcher;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class SahirVisitorInDarkness extends CardImpl {

    public SahirVisitorInDarkness(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{B}{B}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.WIZARD);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Eminence — At the beginning of your upkeep, Sahir deals 1 damage to you. This ability triggers only if Sahir is in the command zone or on the battlefield.
        this.addAbility(new BeginningOfUpkeepTriggeredAbility(
                Zone.ALL,
                TargetController.YOU,
                new DamageControllerEffect(1),
                false
        ).withInterveningIf(SourceOnBattlefieldOrCommandZoneCondition.instance).withFlavorWord("Eminence"));

        // Track damage-to-life-loss conversion so 1 damage from a source you control does not also trigger as life loss
        this.addAbility(new SimpleStaticAbility(new SahirDamageToLifeLossMarkerEffect()));

        // Whenever a source you control deals exactly 1 damage to you and whenever a spell or ability you control causes you to lose exactly 1 life, put a +1/+1 counter on Sahir. If this is the second time this ability has resolved this turn, draw a card.
        Ability ability = new SahirVisitorInDarknessTriggeredAbility();
        ability.addWatcher(new AbilityResolvedWatcher());
        this.addAbility(ability);
    }

    private SahirVisitorInDarkness(final SahirVisitorInDarkness card) {
        super(card);
    }

    @Override
    public SahirVisitorInDarkness copy() {
        return new SahirVisitorInDarkness(this);
    }
}

class SahirDamageToLifeLossMarkerEffect extends ReplacementEffectImpl {

    static final String KEY_PREFIX = "SahirDamageLifeLossAmount_";

    SahirDamageToLifeLossMarkerEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Neutral);
        this.staticText = "";
    }

    private SahirDamageToLifeLossMarkerEffect(final SahirDamageToLifeLossMarkerEffect effect) {
        super(effect);
    }

    @Override
    public SahirDamageToLifeLossMarkerEffect copy() {
        return new SahirDamageToLifeLossMarkerEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DAMAGE_CAUSES_LIFE_LOSS;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        return source.isControlledBy(event.getPlayerId());
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        game.getState().setValue(KEY_PREFIX + event.getPlayerId(), event.getAmount());
        return false;
    }
}

class SahirVisitorInDarknessTriggeredAbility extends TriggeredAbilityImpl {

    SahirVisitorInDarknessTriggeredAbility() {
        super(Zone.BATTLEFIELD, new SahirVisitorInDarknessEffect(), false);
        setTriggerPhrase("Whenever a source you control deals exactly 1 damage to you "
                + "and whenever a spell or ability you control causes you to lose exactly 1 life, ");
    }

    private SahirVisitorInDarknessTriggeredAbility(final SahirVisitorInDarknessTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public SahirVisitorInDarknessTriggeredAbility copy() {
        return new SahirVisitorInDarknessTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DAMAGED_PLAYER
                || event.getType() == GameEvent.EventType.LOST_LIFE;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (event.getType() == GameEvent.EventType.DAMAGED_PLAYER) {
            game.getState().setValue(SahirDamageToLifeLossMarkerEffect.KEY_PREFIX + event.getTargetId(), null);
            return event.getAmount() == 1
                    && isControlledBy(event.getTargetId())
                    && event.getSourceId() != null
                    && isControlledBy(game.getControllerId(event.getSourceId()));
        }
        // LOST_LIFE
        if (!isControlledBy(event.getPlayerId())) {
            return false;
        }
        Object skippedObj = game.getState().getValue(SahirDamageToLifeLossMarkerEffect.KEY_PREFIX + event.getPlayerId());
        if (skippedObj instanceof Integer) {
            game.getState().setValue(SahirDamageToLifeLossMarkerEffect.KEY_PREFIX + event.getPlayerId(), null);
            return false;
        }
        if (event.getAmount() != 1 || event.getFlag() || event.getSourceId() == null) {
            return false;
        }
        return isControlledBy(game.getControllerId(event.getSourceId()));
    }
}

class SahirVisitorInDarknessEffect extends OneShotEffect {

    SahirVisitorInDarknessEffect() {
        super(Outcome.BoostCreature);
        this.staticText = "put a +1/+1 counter on {this}. If this is the second time this ability has resolved this turn, draw a card";
    }

    private SahirVisitorInDarknessEffect(final SahirVisitorInDarknessEffect effect) {
        super(effect);
    }

    @Override
    public SahirVisitorInDarknessEffect copy() {
        return new SahirVisitorInDarknessEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = source.getSourcePermanentIfItStillExists(game);
        if (permanent != null) {
            permanent.addCounters(CounterType.P1P1.createInstance(), source.getControllerId(), source, game);
        }
        if (AbilityResolvedWatcher.getResolutionCount(game, source) == 2) {
            Player controller = game.getPlayer(source.getControllerId());
            if (controller != null) {
                controller.drawCards(1, source, game);
            }
        }
        return true;
    }
}
