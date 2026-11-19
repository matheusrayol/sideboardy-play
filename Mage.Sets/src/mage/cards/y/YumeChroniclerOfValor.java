package mage.cards.y;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.keyword.AssistAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.command.CommandObject;
import mage.game.command.Commander;
import mage.game.events.GameEvent;
import mage.game.events.ManaPaidEvent;
import mage.game.stack.Spell;
import mage.players.Player;
import mage.target.targetpointer.FixedTarget;
import mage.watchers.Watcher;
import mage.watchers.common.SpellsCastWatcher;

import java.util.*;

/**
 * @author Sideboardy
 */
public final class YumeChroniclerOfValor extends CardImpl {

    public YumeChroniclerOfValor(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.DRUID);
        this.power = new MageInt(1);
        this.toughness = new MageInt(2);

        // The first spell you cast during each of your turns has assist.
        this.addAbility(new SimpleStaticAbility(new YumeGrantAssistEffect()));

        // When you cast that spell, if another player spent mana to cast it, you and that player each draw a card.
        Ability triggered = new YumeAssistTriggeredAbility();
        triggered.addWatcher(new YumeManaPaidWatcher());
        this.addAbility(triggered);
    }

    private YumeChroniclerOfValor(final YumeChroniclerOfValor card) {
        super(card);
    }

    @Override
    public YumeChroniclerOfValor copy() {
        return new YumeChroniclerOfValor(this);
    }
}

class YumeGrantAssistEffect extends ContinuousEffectImpl {

    YumeGrantAssistEffect() {
        super(Duration.WhileOnBattlefield, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        this.staticText = "the first spell you cast during each of your turns has assist";
    }

    private YumeGrantAssistEffect(final YumeGrantAssistEffect effect) {
        super(effect);
    }

    @Override
    public YumeGrantAssistEffect copy() {
        return new YumeGrantAssistEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null || !game.isActivePlayer(controller.getId())) {
            return false;
        }
        SpellsCastWatcher watcher = game.getState().getWatcher(SpellsCastWatcher.class);
        if (watcher != null && watcher.getCount(controller.getId()) > 0) {
            return false;
        }
        for (Card card : controller.getHand().getCards(game)) {
            if (!card.isLand(game)) {
                game.getState().addOtherAbility(card, new AssistAbility());
            }
        }
        for (Card card : controller.getGraveyard().getCards(game)) {
            if (!card.isLand(game)) {
                game.getState().addOtherAbility(card, new AssistAbility());
            }
        }
        for (Card card : game.getExile().getCardsOwned(game, controller.getId())) {
            if (!card.isLand(game)) {
                game.getState().addOtherAbility(card, new AssistAbility());
            }
        }
        for (Card card : controller.getLibrary().getCards(game)) {
            if (!card.isLand(game)) {
                game.getState().addOtherAbility(card, new AssistAbility());
            }
        }
        for (CommandObject cmd : game.getState().getCommand()) {
            if (cmd instanceof Commander && cmd.isControlledBy(controller.getId())) {
                Card commanderCard = game.getCard(cmd.getId());
                if (commanderCard != null && game.getState().getZone(commanderCard.getId()) == Zone.COMMAND) {
                    game.getState().addOtherAbility(commanderCard, new AssistAbility());
                }
            }
        }
        return true;
    }
}

class YumeAssistTriggeredAbility extends TriggeredAbilityImpl {

    YumeAssistTriggeredAbility() {
        super(Zone.BATTLEFIELD, new YumeAssistDrawEffect(), false);
        this.addWatcher(new SpellsCastWatcher());
        setTriggerPhrase("When you cast that spell, if another player spent mana to cast it, ");
    }

    private YumeAssistTriggeredAbility(final YumeAssistTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public YumeAssistTriggeredAbility copy() {
        return new YumeAssistTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.SPELL_CAST;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!isControlledBy(event.getPlayerId()) || !game.isActivePlayer(event.getPlayerId())) {
            return false;
        }
        SpellsCastWatcher castWatcher = game.getState().getWatcher(SpellsCastWatcher.class);
        if (castWatcher == null || castWatcher.getCount(event.getPlayerId()) != 1) {
            return false;
        }
        Spell spell = game.getSpellOrLKIStack(event.getTargetId());
        if (spell == null) {
            return false;
        }
        YumeManaPaidWatcher manaWatcher = game.getState().getWatcher(YumeManaPaidWatcher.class);
        UUID helperId = manaWatcher != null
                ? manaWatcher.getOtherManaPayer(spell.getSpellAbility().getId(), event.getPlayerId())
                : null;
        if (helperId == null) {
            Object assisted = game.getState().getValue(
                    spell.getSourceId().toString() + spell.getZoneChangeCounter(game) + "_assisted"
            );
            if (!Boolean.TRUE.equals(assisted)) {
                return false;
            }
        }
        getEffects().setTargetPointer(helperId != null ? new FixedTarget(helperId) : null);
        return true;
    }
}

class YumeAssistDrawEffect extends OneShotEffect {

    YumeAssistDrawEffect() {
        super(Outcome.DrawCard);
        this.staticText = "you and that player each draw a card";
    }

    private YumeAssistDrawEffect(final YumeAssistDrawEffect effect) {
        super(effect);
    }

    @Override
    public YumeAssistDrawEffect copy() {
        return new YumeAssistDrawEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller != null) {
            controller.drawCards(1, source, game);
        }
        Player otherPlayer = game.getPlayer(getTargetPointer().getFirst(game, source));
        if (otherPlayer != null) {
            otherPlayer.drawCards(1, source, game);
        }
        return true;
    }
}

class YumeManaPaidWatcher extends Watcher {

    private final Map<UUID, Set<UUID>> manaPayersByAbility = new HashMap<>();

    YumeManaPaidWatcher() {
        super(WatcherScope.GAME);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() != GameEvent.EventType.MANA_PAID || !(event instanceof ManaPaidEvent)) {
            return;
        }
        UUID abilityId = event.getTargetId();
        UUID manaSourceId = event.getSourceId();
        if (abilityId == null || manaSourceId == null) {
            return;
        }
        UUID payerId = game.getControllerId(manaSourceId);
        if (payerId != null) {
            manaPayersByAbility.computeIfAbsent(abilityId, k -> new LinkedHashSet<>()).add(payerId);
        }
    }

    UUID getOtherManaPayer(UUID spellAbilityId, UUID casterId) {
        Set<UUID> payers = manaPayersByAbility.get(spellAbilityId);
        if (payers == null) {
            return null;
        }
        for (UUID payer : payers) {
            if (!payer.equals(casterId)) {
                return payer;
            }
        }
        return null;
    }

    @Override
    public void reset() {
        super.reset();
        manaPayersByAbility.clear();
    }
}
