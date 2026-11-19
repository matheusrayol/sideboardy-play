package mage.cards.s;

import mage.MageObject;
import mage.ObjectColor;
import mage.abilities.Ability;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.keyword.ProtectionAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.choices.ChoiceColor;
import mage.constants.*;
import mage.filter.Filter;
import mage.filter.FilterImpl;
import mage.filter.predicate.mageobject.ColorPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.game.stack.Spell;
import mage.players.Player;
import mage.target.Target;
import mage.target.common.TargetSpellOrPermanent;

import java.util.List;
import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class SleightOfMind extends CardImpl {

    public SleightOfMind(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{U}");

        // Change the text of target spell or permanent by replacing all instances of one color word with another. (This effect lasts indefinitely.)
        this.getSpellAbility().addEffect(new ColorWordChangeEffect(
                Duration.Custom,
                "change the text of target spell or permanent by replacing all instances of one color word with another. "
                        + "<i>(This effect lasts indefinitely.)</i>"
        ));
        this.getSpellAbility().addTarget(new TargetSpellOrPermanent());
    }

    private SleightOfMind(final SleightOfMind card) {
        super(card);
    }

    @Override
    public SleightOfMind copy() {
        return new SleightOfMind(this);
    }

    public static MageObject resolveTrackedObject(Game game, UUID trackedId, int[] state) {
        if (trackedId == null) {
            return null;
        }
        // state[0] = trackedZcc, state[1] = startedAsSpell (1 or 0)
        if (state[1] == 1) {
            Spell spell = game.getSpell(trackedId);
            if (spell != null && game.getState().getZoneChangeCounter(trackedId) == state[0]) {
                return spell;
            }
            Permanent perm = game.getPermanent(trackedId);
            if (perm != null && game.getState().getZoneChangeCounter(trackedId) == state[0] + 1) {
                state[0] = state[0] + 1;
                state[1] = 0;
                return perm;
            }
            return null;
        }
        Permanent perm = game.getPermanent(trackedId);
        if (perm != null && game.getState().getZoneChangeCounter(trackedId) == state[0]) {
            return perm;
        }
        return null;
    }

    public static void applyColorWordChange(MageObject obj, ObjectColor from, ObjectColor to) {
        String fromPredicateStr = "Color(" + from.toString() + ')';
        for (Ability ability : obj.getAbilities()) {
            if (ability instanceof ProtectionAbility) {
                ProtectionAbility pa = (ProtectionAbility) ability;
                replaceColorInFilter(pa.getFilter(), fromPredicateStr, from, to);
                if (pa.getFromColor().shares(from)) {
                    clearColorFlag(pa.getFromColor(), from);
                    pa.getFromColor().addColor(to);
                }
            }
            for (Target target : ability.getTargets()) {
                replaceColorInFilter(target.getFilter(), fromPredicateStr, from, to);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void replaceColorInFilter(Filter filter, String fromPredicateStr, ObjectColor from, ObjectColor to) {
        if (!(filter instanceof FilterImpl)) {
            return;
        }
        FilterImpl rawFilter = (FilterImpl) filter;
        if (rawFilter.isLockedFilter()) {
            return;
        }
        List preds = rawFilter.getPredicates();
        for (int i = 0; i < preds.size(); i++) {
            Object p = preds.get(i);
            if (p instanceof ColorPredicate && fromPredicateStr.equals(p.toString())) {
                preds.set(i, new ColorPredicate(to));
            }
        }
        String msg = rawFilter.getMessage();
        if (msg != null && msg.contains(from.getDescription())) {
            rawFilter.setMessage(msg.replace(from.getDescription(), to.getDescription()));
        }
    }

    private static void clearColorFlag(ObjectColor target, ObjectColor toRemove) {
        if (toRemove.isWhite()) target.setWhite(false);
        if (toRemove.isBlue()) target.setBlue(false);
        if (toRemove.isBlack()) target.setBlack(false);
        if (toRemove.isRed()) target.setRed(false);
        if (toRemove.isGreen()) target.setGreen(false);
    }

    public static class ColorWordChangeEffect extends ContinuousEffectImpl {

        private ObjectColor fromColor;
        private ObjectColor toColor;
        private UUID trackedId;
        private final int[] trackState = new int[2];

        public ColorWordChangeEffect(Duration duration, String text) {
            super(duration, Layer.TextChangingEffects_3, SubLayer.NA, Outcome.Neutral);
            this.staticText = text;
        }

        protected ColorWordChangeEffect(final ColorWordChangeEffect effect) {
            super(effect);
            this.fromColor = effect.fromColor;
            this.toColor = effect.toColor;
            this.trackedId = effect.trackedId;
            this.trackState[0] = effect.trackState[0];
            this.trackState[1] = effect.trackState[1];
        }

        @Override
        public ColorWordChangeEffect copy() {
            return new ColorWordChangeEffect(this);
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

            ChoiceColor fromChoice = new ChoiceColor(true, "Choose a color word to replace");
            if (!controller.choose(Outcome.Neutral, fromChoice, game) || fromChoice.getColor() == null) {
                discard();
                return;
            }
            fromColor = fromChoice.getColor();

            ChoiceColor toChoice = new ChoiceColor(true, "Choose a new color word");
            if (!controller.choose(Outcome.Neutral, toChoice, game) || toChoice.getColor() == null) {
                discard();
                return;
            }
            toColor = toChoice.getColor();

            if (!game.isSimulation()) {
                game.informPlayers(controller.getLogName() + " changes " + fromColor.getDescription() + " to " + toColor.getDescription());
            }
        }

        @Override
        public boolean apply(Layer layer, SubLayer sublayer, Ability source, Game game) {
            if (fromColor == null || toColor == null || trackedId == null) {
                return false;
            }
            MageObject targetObject = resolveTrackedObject(game, trackedId, trackState);
            if (targetObject == null) {
                discard();
                return false;
            }
            applyColorWordChange(targetObject, fromColor, toColor);
            return true;
        }

        @Override
        public boolean apply(Game game, Ability source) {
            return false;
        }
    }
}
