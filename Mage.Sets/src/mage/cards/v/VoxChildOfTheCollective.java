package mage.cards.v;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.SpellAbility;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.cost.CostModificationEffectImpl;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.stack.Spell;
import mage.util.CardUtil;
import mage.watchers.Watcher;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class VoxChildOfTheCollective extends CardImpl {

    public VoxChildOfTheCollective(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{4}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.BORG);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // When Võx enters and at the beginning of your first main phase,
        // the next artifact creature spell and the next Spacecraft spell you cast this turn have undaunted.
        this.addAbility(new VoxTriggeredAbility(), new VoxWatcher());
    }

    private VoxChildOfTheCollective(final VoxChildOfTheCollective card) {
        super(card);
    }

    @Override
    public VoxChildOfTheCollective copy() {
        return new VoxChildOfTheCollective(this);
    }
}

class VoxTriggeredAbility extends TriggeredAbilityImpl {

    VoxTriggeredAbility() {
        super(Zone.BATTLEFIELD, new VoxEffect(), false);
        setTriggerPhrase("When {this} enters and at the beginning of your first main phase, ");
    }

    private VoxTriggeredAbility(final VoxTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public VoxTriggeredAbility copy() {
        return new VoxTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ENTERS_THE_BATTLEFIELD
                || event.getType() == GameEvent.EventType.PRECOMBAT_MAIN_PHASE_PRE;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (event.getType() == GameEvent.EventType.ENTERS_THE_BATTLEFIELD) {
            return event.getTargetId().equals(getSourceId());
        }
        return event.getType() == GameEvent.EventType.PRECOMBAT_MAIN_PHASE_PRE
                && isControlledBy(event.getPlayerId());
    }
}

class VoxEffect extends OneShotEffect {

    VoxEffect() {
        super(Outcome.Benefit);
        this.staticText = "the next artifact creature spell and the next Spacecraft spell you cast this turn have undaunted. "
                + "<i>(They cost {1} less to cast for each opponent.)</i>";
    }

    private VoxEffect(final VoxEffect effect) {
        super(effect);
    }

    @Override
    public VoxEffect copy() {
        return new VoxEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        game.addEffect(new VoxNextSpellUndauntedEffect(true), source);
        game.addEffect(new VoxNextSpellUndauntedEffect(false), source);
        return true;
    }
}

class VoxNextSpellUndauntedEffect extends CostModificationEffectImpl {

    private final boolean matchArtifactCreature;
    private int spellsCastAtStart;

    VoxNextSpellUndauntedEffect(boolean matchArtifactCreature) {
        super(Duration.EndOfTurn, Outcome.Benefit, CostModificationType.REDUCE_COST);
        this.matchArtifactCreature = matchArtifactCreature;
    }

    private VoxNextSpellUndauntedEffect(final VoxNextSpellUndauntedEffect effect) {
        super(effect);
        this.matchArtifactCreature = effect.matchArtifactCreature;
        this.spellsCastAtStart = effect.spellsCastAtStart;
    }

    @Override
    public VoxNextSpellUndauntedEffect copy() {
        return new VoxNextSpellUndauntedEffect(this);
    }

    @Override
    public void init(Ability source, Game game) {
        super.init(source, game);
        VoxWatcher watcher = game.getState().getWatcher(VoxWatcher.class);
        if (watcher != null) {
            spellsCastAtStart = watcher.getCount(source.getControllerId(), matchArtifactCreature);
        }
    }

    @Override
    public boolean apply(Game game, Ability source, Ability abilityToModify) {
        int opponentsCount = game.getOpponents(source.getControllerId()).size();
        if (opponentsCount > 0) {
            CardUtil.reduceCost(abilityToModify, opponentsCount);
        }
        return true;
    }

    @Override
    public boolean applies(Ability abilityToModify, Ability source, Game game) {
        if (!(abilityToModify instanceof SpellAbility)
                || !abilityToModify.isControlledBy(source.getControllerId())) {
            return false;
        }
        VoxWatcher watcher = game.getState().getWatcher(VoxWatcher.class);
        if (watcher == null || watcher.getCount(source.getControllerId(), matchArtifactCreature) != spellsCastAtStart) {
            discard();
            return false;
        }
        Card characteristics = ((SpellAbility) abilityToModify).getCharacteristics(game);
        if (characteristics == null) {
            return false;
        }
        if (matchArtifactCreature) {
            return characteristics.isArtifact(game) && characteristics.isCreature(game);
        }
        return characteristics.hasSubtype(SubType.SPACECRAFT, game);
    }
}

class VoxWatcher extends Watcher {

    private final Map<UUID, Integer> artifactCreatureCount = new HashMap<>();
    private final Map<UUID, Integer> spacecraftCount = new HashMap<>();

    VoxWatcher() {
        super(WatcherScope.GAME);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() != GameEvent.EventType.SPELL_CAST) {
            return;
        }
        Spell spell = game.getSpellOrLKIStack(event.getTargetId());
        if (spell == null) {
            return;
        }
        UUID controllerId = spell.getControllerId();
        if (spell.isArtifact(game) && spell.isCreature(game)) {
            artifactCreatureCount.merge(controllerId, 1, Integer::sum);
        }
        if (spell.hasSubtype(SubType.SPACECRAFT, game)) {
            spacecraftCount.merge(controllerId, 1, Integer::sum);
        }
    }

    @Override
    public void reset() {
        super.reset();
        artifactCreatureCount.clear();
        spacecraftCount.clear();
    }

    int getCount(UUID playerId, boolean artifactCreature) {
        return (artifactCreature ? artifactCreatureCount : spacecraftCount).getOrDefault(playerId, 0);
    }
}
