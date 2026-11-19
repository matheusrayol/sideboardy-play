package mage.cards.m;

import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.keyword.*;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.s.SleightOfMind;
import mage.choices.Choice;
import mage.choices.ChoiceBasicLandType;
import mage.constants.*;
import mage.filter.Filter;
import mage.filter.FilterImpl;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.Target;
import mage.target.common.TargetSpellOrPermanent;

import java.util.List;
import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class MagicalHack extends CardImpl {

    public MagicalHack(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{U}");

        // Change the text of target spell or permanent by replacing all instances of one basic land type with another. (This effect lasts indefinitely.)
        this.getSpellAbility().addEffect(new BasicLandTypeChangeEffect(
                Duration.Custom,
                "change the text of target spell or permanent by replacing all instances of one basic land type with another. "
                        + "<i>(This effect lasts indefinitely.)</i>"
        ));
        this.getSpellAbility().addTarget(new TargetSpellOrPermanent());
    }

    private MagicalHack(final MagicalHack card) {
        super(card);
    }

    @Override
    public MagicalHack copy() {
        return new MagicalHack(this);
    }

    public static void applyBasicLandTypeChange(MageObject obj, SubType from, SubType to) {
        for (Ability ability : obj.getAbilities()) {
            if (ability instanceof ProtectionAbility) {
                replaceLandSubTypeInFilter(((ProtectionAbility) ability).getFilter(), from, to);
            }
            for (Target target : ability.getTargets()) {
                replaceLandSubTypeInFilter(target.getFilter(), from, to);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void replaceLandSubTypeInFilter(Filter filter, SubType from, SubType to) {
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

    public static void replaceLandwalkAbility(Permanent perm, SubType from, SubType to, UUID sourceId, Game game) {
        Ability found = null;
        for (Ability ability : perm.getAbilities(game)) {
            if (matchesLandwalk(ability, from)) {
                found = ability;
                break;
            }
        }
        if (found != null && from != to) {
            perm.removeAbility(found, sourceId, game);
            Ability replacement = createLandwalk(to);
            if (replacement != null) {
                perm.addAbility(replacement, sourceId, game);
            }
        }
    }

    private static boolean matchesLandwalk(Ability ability, SubType landType) {
        switch (landType) {
            case PLAINS:
                return ability instanceof PlainswalkAbility;
            case ISLAND:
                return ability instanceof IslandwalkAbility;
            case SWAMP:
                return ability instanceof SwampwalkAbility;
            case MOUNTAIN:
                return ability instanceof MountainwalkAbility;
            case FOREST:
                return ability instanceof ForestwalkAbility;
            default:
                return false;
        }
    }

    private static Ability createLandwalk(SubType landType) {
        switch (landType) {
            case PLAINS:
                return new PlainswalkAbility();
            case ISLAND:
                return new IslandwalkAbility();
            case SWAMP:
                return new SwampwalkAbility();
            case MOUNTAIN:
                return new MountainwalkAbility();
            case FOREST:
                return new ForestwalkAbility();
            default:
                return null;
        }
    }

    public static class BasicLandTypeChangeEffect extends ContinuousEffectImpl {

        private SubType fromLandType;
        private SubType toLandType;
        private UUID trackedId;
        private final int[] trackState = new int[2];

        public BasicLandTypeChangeEffect(Duration duration, String text) {
            super(duration, Layer.TextChangingEffects_3, SubLayer.NA, Outcome.Neutral);
            this.staticText = text;
        }

        protected BasicLandTypeChangeEffect(final BasicLandTypeChangeEffect effect) {
            super(effect);
            this.fromLandType = effect.fromLandType;
            this.toLandType = effect.toLandType;
            this.trackedId = effect.trackedId;
            this.trackState[0] = effect.trackState[0];
            this.trackState[1] = effect.trackState[1];
        }

        @Override
        public BasicLandTypeChangeEffect copy() {
            return new BasicLandTypeChangeEffect(this);
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
            trackState[0] = game.getState().getZoneChangeCounter(trackedId);
            trackState[1] = game.getSpell(trackedId) != null ? 1 : 0;

            Choice fromChoice = new ChoiceBasicLandType();
            fromChoice.setMessage("Choose a basic land type to replace");
            if (!controller.choose(Outcome.Neutral, fromChoice, game)) {
                discard();
                return;
            }
            fromLandType = SubType.byDescription(fromChoice.getChoice());

            Choice toChoice = new ChoiceBasicLandType();
            toChoice.setMessage("Choose a new basic land type");
            if (!controller.choose(Outcome.Neutral, toChoice, game)) {
                discard();
                return;
            }
            toLandType = SubType.byDescription(toChoice.getChoice());
            if (fromLandType == null || toLandType == null) {
                discard();
                return;
            }
            if (!game.isSimulation()) {
                game.informPlayers(controller.getLogName() + " changes " + fromLandType + " to " + toLandType);
            }
        }

        @Override
        public boolean hasLayer(Layer layer) {
            return layer == Layer.TextChangingEffects_3 || layer == Layer.AbilityAddingRemovingEffects_6;
        }

        @Override
        public boolean apply(Layer layer, SubLayer sublayer, Ability source, Game game) {
            if (fromLandType == null || toLandType == null || trackedId == null) {
                return false;
            }
            MageObject targetObject = SleightOfMind.resolveTrackedObject(game, trackedId, trackState);
            if (targetObject == null) {
                discard();
                return false;
            }
            if (layer == Layer.TextChangingEffects_3) {
                applyBasicLandTypeChange(targetObject, fromLandType, toLandType);
            } else if (layer == Layer.AbilityAddingRemovingEffects_6 && targetObject instanceof Permanent) {
                replaceLandwalkAbility((Permanent) targetObject, fromLandType, toLandType, source.getSourceId(), game);
            }
            return true;
        }

        @Override
        public boolean apply(Game game, Ability source) {
            return false;
        }
    }
}
