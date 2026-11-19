package mage.abilities.keyword;

import mage.ApprovingObject;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.DelayedTriggeredAbility;
import mage.abilities.SpellAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.cards.Card;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.events.ZoneChangeEvent;
import mage.game.stack.Spell;
import mage.players.Player;

/**
 * Secrets of Strixhaven - Paradigm keyword ability:
 * "Paradigm (Then exile this spell. After you first resolve a spell with this name,
 * you may cast a copy of it from exile without paying its mana cost
 * at the beginning of each of your first main phases.)"
 *
 * @author TheElk801
 */
public class ParadigmAbility extends SimpleStaticAbility {

    public ParadigmAbility() {
        super(Zone.STACK, new ParadigmReplacementEffect());
    }

    protected ParadigmAbility(final ParadigmAbility ability) {
        super(ability);
    }

    @Override
    public ParadigmAbility copy() {
        return new ParadigmAbility(this);
    }

    @Override
    public String getRule() {
        return "Paradigm <i> (Then exile this spell. After you first resolve a spell with this name, " +
                "you may cast a copy of it from exile without paying its mana cost " +
                "at the beginning of each of your first main phases.)</i>";
    }
}

class ParadigmReplacementEffect extends ReplacementEffectImpl {

    ParadigmReplacementEffect() {
        super(Duration.WhileOnStack, Outcome.Benefit);
    }

    private ParadigmReplacementEffect(final ParadigmReplacementEffect effect) {
        super(effect);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ZONE_CHANGE;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        ZoneChangeEvent zEvent = (ZoneChangeEvent) event;
        if (zEvent.getFromZone() != Zone.STACK
                || zEvent.getToZone() != Zone.GRAVEYARD
                || event.getSourceId() == null
                || !event.getSourceId().equals(source.getSourceId())) {
            return false;
        }
        Spell spell = game.getStack().getSpell(event.getTargetId());
        return spell != null && !spell.isCopy();
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        Spell sourceSpell = game.getStack().getSpell(source.getSourceId());
        if (sourceSpell != null && sourceSpell.isCopy()) {
            return false;
        }
        Card sourceCard = game.getCard(source.getSourceId());
        if (sourceCard == null) {
            return false;
        }
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        controller.moveCardsToExile(sourceCard, source, game, true, null, "Paradigm");
        if (game.getState().getZone(sourceCard.getId()) != Zone.EXILED) {
            return true;
        }
        String resolvedKey = "ParadigmResolved_" + controller.getId() + "_" + sourceCard.getName();
        MageObjectReference existingRef = (MageObjectReference) game.getState().getValue(resolvedKey);
        if (existingRef == null || existingRef.getCard(game) == null
                || game.getState().getZone(existingRef.getSourceId()) != Zone.EXILED) {
            MageObjectReference exiledRef = new MageObjectReference(sourceCard, game);
            game.getState().setValue(resolvedKey, exiledRef);
            game.addDelayedTriggeredAbility(new ParadigmCastCopyDelayedTrigger(exiledRef), source);
        }
        return true;
    }

    @Override
    public ParadigmReplacementEffect copy() {
        return new ParadigmReplacementEffect(this);
    }
}

class ParadigmCastCopyDelayedTrigger extends DelayedTriggeredAbility {

    private final MageObjectReference exiledCardRef;

    ParadigmCastCopyDelayedTrigger(MageObjectReference exiledCardRef) {
        super(new ParadigmCastCopyFromExileEffect(exiledCardRef), Duration.Custom, false, false);
        this.exiledCardRef = exiledCardRef;
    }

    private ParadigmCastCopyDelayedTrigger(final ParadigmCastCopyDelayedTrigger ability) {
        super(ability);
        this.exiledCardRef = ability.exiledCardRef;
    }

    @Override
    public ParadigmCastCopyDelayedTrigger copy() {
        return new ParadigmCastCopyDelayedTrigger(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.PRECOMBAT_MAIN_PHASE_PRE;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!game.isActivePlayer(getControllerId())) {
            return false;
        }
        Card exiledCard = exiledCardRef.getCard(game);
        return exiledCard != null && game.getState().getZone(exiledCard.getId()) == Zone.EXILED;
    }

    @Override
    public String getRule() {
        return "Paradigm - At the beginning of each of your first main phases, " +
                "you may cast a copy of {this} from exile without paying its mana cost.";
    }
}

class ParadigmCastCopyFromExileEffect extends OneShotEffect {

    private final MageObjectReference exiledCardRef;

    ParadigmCastCopyFromExileEffect(MageObjectReference exiledCardRef) {
        super(Outcome.PlayForFree);
        this.exiledCardRef = exiledCardRef;
    }

    private ParadigmCastCopyFromExileEffect(final ParadigmCastCopyFromExileEffect effect) {
        super(effect);
        this.exiledCardRef = effect.exiledCardRef;
    }

    @Override
    public ParadigmCastCopyFromExileEffect copy() {
        return new ParadigmCastCopyFromExileEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Card card = exiledCardRef.getCard(game);
        if (controller == null || card == null || game.getState().getZone(card.getId()) != Zone.EXILED) {
            return false;
        }
        if (!controller.chooseUse(Outcome.PlayForFree,
                "Cast a copy of " + card.getLogName() + " from exile without paying its mana cost?",
                source, game)) {
            return true;
        }
        Card copiedCard = game.copyCard(card, source, controller.getId());
        SpellAbility ability = controller.chooseAbilityForCast(copiedCard, game, true);
        if (ability == null) {
            return false;
        }
        String key = "PlayFromNotOwnHandZone" + copiedCard.getId();
        game.getState().setValue(key, Boolean.TRUE);
        try {
            return controller.cast(ability, game, true, new ApprovingObject(source, game));
        } finally {
            game.getState().setValue(key, null);
        }
    }
}
