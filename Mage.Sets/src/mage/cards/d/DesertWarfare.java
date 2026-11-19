package mage.cards.d;

import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.delayed.AtTheBeginOfNextEndStepDelayedTriggeredAbility;
import mage.abilities.condition.Condition;
import mage.abilities.condition.common.PermanentsOnTheBattlefieldCondition;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.GainAbilityTargetEffect;
import mage.abilities.hint.ConditionHint;
import mage.abilities.hint.Hint;
import mage.abilities.keyword.HasteAbility;
import mage.abilities.triggers.BeginningOfCombatTriggeredAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterControlledPermanent;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.events.ZoneChangeEvent;
import mage.game.permanent.Permanent;
import mage.game.permanent.token.SandWarriorToken;
import mage.game.permanent.token.Token;
import mage.players.Player;
import mage.target.targetpointer.FixedTargets;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class DesertWarfare extends CardImpl {

    private static final FilterPermanent filter = new FilterControlledPermanent(SubType.DESERT, "Deserts");
    private static final Condition condition = new PermanentsOnTheBattlefieldCondition(filter, ComparisonType.MORE_THAN, 4);
    private static final Hint hint = new ConditionHint(condition, "You control five or more Deserts");

    public DesertWarfare(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{3}{G}");

        // Whenever you sacrifice a Desert and whenever a Desert card is put into your graveyard from your hand or library, put that card onto the battlefield under your control at the beginning of your next end step.
        this.addAbility(new DesertWarfareTriggeredAbility());

        // At the beginning of combat on your turn, if you control five or more Deserts, create that many 1/1 red, green, and white Sand Warrior creature tokens. They gain haste.
        this.addAbility(new BeginningOfCombatTriggeredAbility(
                new DesertWarfareTokensEffect()
        ).withInterveningIf(condition).addHint(hint));
    }

    private DesertWarfare(final DesertWarfare card) {
        super(card);
    }

    @Override
    public DesertWarfare copy() {
        return new DesertWarfare(this);
    }
}

class DesertWarfareTriggeredAbility extends TriggeredAbilityImpl {

    DesertWarfareTriggeredAbility() {
        super(Zone.BATTLEFIELD, null, false);
    }

    private DesertWarfareTriggeredAbility(final DesertWarfareTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public DesertWarfareTriggeredAbility copy() {
        return new DesertWarfareTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.SACRIFICED_PERMANENT
                || event.getType() == GameEvent.EventType.ZONE_CHANGE;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        Card card = null;
        if (event.getType() == GameEvent.EventType.SACRIFICED_PERMANENT) {
            if (!event.getPlayerId().equals(getControllerId())) {
                return false;
            }
            Permanent permanent = game.getPermanentOrLKIBattlefield(event.getTargetId());
            if (permanent == null || !permanent.hasSubtype(SubType.DESERT, game)) {
                return false;
            }
            card = game.getCard(event.getTargetId());
        } else {
            ZoneChangeEvent zEvent = (ZoneChangeEvent) event;
            if (zEvent.getToZone() != Zone.GRAVEYARD
                    || (zEvent.getFromZone() != Zone.HAND && zEvent.getFromZone() != Zone.LIBRARY)) {
                return false;
            }
            card = game.getCard(zEvent.getTargetId());
            if (card == null || !card.isOwnedBy(getControllerId()) || !card.hasSubtype(SubType.DESERT, game)) {
                return false;
            }
        }
        if (card == null) {
            return false;
        }
        this.getEffects().clear();
        this.addEffect(new DesertWarfareScheduleReturnEffect(new MageObjectReference(card, game)));
        return true;
    }

    @Override
    public String getRule() {
        return "Whenever you sacrifice a Desert and whenever a Desert card is put into your graveyard from your hand or library, "
                + "put that card onto the battlefield under your control at the beginning of your next end step.";
    }
}

class DesertWarfareScheduleReturnEffect extends OneShotEffect {

    private final MageObjectReference cardRef;

    DesertWarfareScheduleReturnEffect(MageObjectReference cardRef) {
        super(Outcome.PutLandInPlay);
        this.cardRef = cardRef;
        this.staticText = "put that card onto the battlefield under your control at the beginning of your next end step";
    }

    private DesertWarfareScheduleReturnEffect(final DesertWarfareScheduleReturnEffect effect) {
        super(effect);
        this.cardRef = effect.cardRef;
    }

    @Override
    public DesertWarfareScheduleReturnEffect copy() {
        return new DesertWarfareScheduleReturnEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        game.addDelayedTriggeredAbility(new AtTheBeginOfNextEndStepDelayedTriggeredAbility(
                new DesertWarfareReturnCardEffect(cardRef), TargetController.YOU
        ), source);
        return true;
    }
}

class DesertWarfareReturnCardEffect extends OneShotEffect {

    private final MageObjectReference cardRef;

    DesertWarfareReturnCardEffect(MageObjectReference cardRef) {
        super(Outcome.PutLandInPlay);
        this.cardRef = cardRef;
        this.staticText = "put that card onto the battlefield under your control";
    }

    private DesertWarfareReturnCardEffect(final DesertWarfareReturnCardEffect effect) {
        super(effect);
        this.cardRef = effect.cardRef;
    }

    @Override
    public DesertWarfareReturnCardEffect copy() {
        return new DesertWarfareReturnCardEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Card card = cardRef.getCard(game);
        if (controller == null || card == null) {
            return false;
        }
        return controller.moveCards(card, Zone.BATTLEFIELD, source, game);
    }
}

class DesertWarfareTokensEffect extends OneShotEffect {

    private static final FilterPermanent filter = new FilterControlledPermanent(SubType.DESERT, "Deserts");

    DesertWarfareTokensEffect() {
        super(Outcome.PutCreatureInPlay);
        this.staticText = "create that many 1/1 red, green, and white Sand Warrior creature tokens. They gain haste";
    }

    private DesertWarfareTokensEffect(final DesertWarfareTokensEffect effect) {
        super(effect);
    }

    @Override
    public DesertWarfareTokensEffect copy() {
        return new DesertWarfareTokensEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        int count = game.getBattlefield().count(filter, source.getControllerId(), source, game);
        if (count <= 0) {
            return false;
        }
        Token token = new SandWarriorToken();
        token.putOntoBattlefield(count, game, source);
        game.addEffect(new GainAbilityTargetEffect(HasteAbility.getInstance(), Duration.Custom)
                .setTargetPointer(new FixedTargets(token, game)), source);
        return true;
    }
}
