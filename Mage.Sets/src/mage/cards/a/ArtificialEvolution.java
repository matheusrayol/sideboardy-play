package mage.cards.a;

import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.keyword.ProtectionAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.choices.Choice;
import mage.choices.ChoiceCreatureType;
import mage.constants.*;
import mage.filter.Filter;
import mage.filter.FilterImpl;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.game.stack.Spell;
import mage.players.Player;
import mage.target.Target;
import mage.target.common.TargetSpellOrPermanent;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Sideboardy
 */
public final class ArtificialEvolution extends CardImpl {

    public ArtificialEvolution(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{U}");

        // Change the text of target spell or permanent by replacing all instances of one creature type with another. The new creature type can't be Wall. (This effect lasts indefinitely.)
        this.getSpellAbility().addEffect(new ArtificialEvolutionEffect());
        this.getSpellAbility().addTarget(new TargetSpellOrPermanent());
    }

    private ArtificialEvolution(final ArtificialEvolution card) {
        super(card);
    }

    @Override
    public ArtificialEvolution copy() {
        return new ArtificialEvolution(this);
    }
}

class ArtificialEvolutionEffect extends ContinuousEffectImpl {

    private SubType fromSubType;
    private SubType toSubType;
    private UUID trackedId;
    private int trackedZcc;
    private boolean startedAsSpell;

    ArtificialEvolutionEffect() {
        super(Duration.Custom, Layer.TextChangingEffects_3, SubLayer.NA, Outcome.Neutral);
        this.staticText = "change the text of target spell or permanent by replacing all instances of one creature type with another. "
                + "The new creature type can't be Wall. <i>(This effect lasts indefinitely.)</i>";
    }

    private ArtificialEvolutionEffect(final ArtificialEvolutionEffect effect) {
        super(effect);
        this.fromSubType = effect.fromSubType;
        this.toSubType = effect.toSubType;
        this.trackedId = effect.trackedId;
        this.trackedZcc = effect.trackedZcc;
        this.startedAsSpell = effect.startedAsSpell;
    }

    @Override
    public ArtificialEvolutionEffect copy() {
        return new ArtificialEvolutionEffect(this);
    }

    @Override
    public void init(Ability source, Game game) {
        super.init(source, game);
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            discard();
            return;
        }
        trackedId = getTargetPointer().getFirst(game, source);
        if (trackedId == null) {
            discard();
            return;
        }
        Spell spell = game.getSpell(trackedId);
        startedAsSpell = spell != null;
        trackedZcc = game.getState().getZoneChangeCounter(trackedId);

        Choice fromChoice = new ChoiceCreatureType(game, source, true, "Choose a creature type to replace");
        if (!controller.choose(Outcome.Neutral, fromChoice, game)) {
            discard();
            return;
        }
        fromSubType = SubType.byDescription(fromChoice.getChoiceKey());

        List<SubType> allowedToTypes = SubType.getCreatureTypes().stream()
                .filter(st -> st != SubType.WALL)
                .collect(Collectors.toList());
        Choice toChoice = new ChoiceCreatureType(game, source, true, "Choose a new creature type (can't be Wall)", allowedToTypes);
        if (!controller.choose(Outcome.Neutral, toChoice, game)) {
            discard();
            return;
        }
        toSubType = SubType.byDescription(toChoice.getChoiceKey());
        if (fromSubType == null || toSubType == null || toSubType == SubType.WALL) {
            discard();
            return;
        }
        if (!game.isSimulation()) {
            game.informPlayers(controller.getLogName() + " changes " + fromSubType + " to " + toSubType);
        }
    }

    @Override
    public boolean apply(Layer layer, SubLayer sublayer, Ability source, Game game) {
        if (fromSubType == null || toSubType == null || trackedId == null) {
            return false;
        }
        MageObject targetObject = resolveTrackedObject(game);
        if (targetObject == null) {
            discard();
            return false;
        }
        replaceCreatureTypeInAbilities(targetObject, fromSubType, toSubType);
        return true;
    }

    @Override
    public boolean apply(Game game, Ability source) {
        return false;
    }

    private MageObject resolveTrackedObject(Game game) {
        if (startedAsSpell) {
            Spell spell = game.getSpell(trackedId);
            if (spell != null && game.getState().getZoneChangeCounter(trackedId) == trackedZcc) {
                return spell;
            }
            Permanent perm = game.getPermanent(trackedId);
            if (perm != null && game.getState().getZoneChangeCounter(trackedId) == trackedZcc + 1) {
                startedAsSpell = false;
                trackedZcc = trackedZcc + 1;
                return perm;
            }
            return null;
        }
        Permanent perm = game.getPermanent(trackedId);
        if (perm != null && game.getState().getZoneChangeCounter(trackedId) == trackedZcc) {
            return perm;
        }
        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void replaceCreatureTypeInAbilities(MageObject obj, SubType from, SubType to) {
        for (Ability ability : obj.getAbilities()) {
            if (ability instanceof ProtectionAbility) {
                Filter filter = ((ProtectionAbility) ability).getFilter();
                replaceSubTypeInFilter(filter, from, to);
            }
            for (Target target : ability.getTargets()) {
                replaceSubTypeInFilter(target.getFilter(), from, to);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void replaceSubTypeInFilter(Filter filter, SubType from, SubType to) {
        if (!(filter instanceof FilterImpl)) {
            return;
        }
        FilterImpl rawFilter = (FilterImpl) filter;
        if (rawFilter.isLockedFilter()) {
            return;
        }
        List preds = rawFilter.getPredicates();
        for (int i = 0; i < preds.size(); i++) {
            if (preds.get(i) == from.getPredicate()) {
                preds.set(i, to.getPredicate());
            }
        }
        String msg = rawFilter.getMessage();
        if (msg != null && msg.contains(from.getDescription())) {
            rawFilter.setMessage(msg.replace(from.getDescription(), to.getDescription()));
        }
    }
}
