package mage.cards.m;

import mage.MageObject;
import mage.ObjectColor;
import mage.abilities.Ability;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.s.SleightOfMind;
import mage.choices.Choice;
import mage.choices.ChoiceBasicLandType;
import mage.choices.ChoiceColor;
import mage.constants.*;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.TargetPermanent;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class MindBend extends CardImpl {

    public MindBend(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{U}");

        // Change the text of target permanent by replacing all instances of one color word with another or one basic land type with another. (This effect lasts indefinitely.)
        this.getSpellAbility().addEffect(new ColorOrLandTypeChangeEffect(
                Duration.Custom,
                "change the text of target permanent by replacing all instances of one color word with another "
                        + "or one basic land type with another. <i>(This effect lasts indefinitely.)</i>"
        ));
        this.getSpellAbility().addTarget(new TargetPermanent());
    }

    private MindBend(final MindBend card) {
        super(card);
    }

    @Override
    public MindBend copy() {
        return new MindBend(this);
    }

    public static class ColorOrLandTypeChangeEffect extends ContinuousEffectImpl {

        private ObjectColor fromColor;
        private ObjectColor toColor;
        private SubType fromLandType;
        private SubType toLandType;
        private UUID trackedId;
        private final int[] trackState = new int[2];

        public ColorOrLandTypeChangeEffect(Duration duration, String text) {
            super(duration, Layer.TextChangingEffects_3, SubLayer.NA, Outcome.Neutral);
            this.staticText = text;
        }

        protected ColorOrLandTypeChangeEffect(final ColorOrLandTypeChangeEffect effect) {
            super(effect);
            this.fromColor = effect.fromColor;
            this.toColor = effect.toColor;
            this.fromLandType = effect.fromLandType;
            this.toLandType = effect.toLandType;
            this.trackedId = effect.trackedId;
            this.trackState[0] = effect.trackState[0];
            this.trackState[1] = effect.trackState[1];
        }

        @Override
        public ColorOrLandTypeChangeEffect copy() {
            return new ColorOrLandTypeChangeEffect(this);
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

            boolean changeColor = controller.chooseUse(
                    Outcome.Neutral,
                    "Change a color word or a basic land type?",
                    null,
                    "Color word",
                    "Basic land type",
                    source,
                    game
            );
            if (changeColor) {
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
            } else {
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
        }

        @Override
        public boolean hasLayer(Layer layer) {
            return layer == Layer.TextChangingEffects_3 || layer == Layer.AbilityAddingRemovingEffects_6;
        }

        @Override
        public boolean apply(Layer layer, SubLayer sublayer, Ability source, Game game) {
            MageObject targetObject = SleightOfMind.resolveTrackedObject(game, trackedId, trackState);
            if (targetObject == null) {
                discard();
                return false;
            }
            if (fromColor != null && toColor != null && layer == Layer.TextChangingEffects_3) {
                SleightOfMind.applyColorWordChange(targetObject, fromColor, toColor);
            } else if (fromLandType != null && toLandType != null) {
                if (layer == Layer.TextChangingEffects_3) {
                    MagicalHack.applyBasicLandTypeChange(targetObject, fromLandType, toLandType);
                } else if (layer == Layer.AbilityAddingRemovingEffects_6 && targetObject instanceof Permanent) {
                    MagicalHack.replaceLandwalkAbility((Permanent) targetObject, fromLandType, toLandType, source.getSourceId(), game);
                }
            }
            return true;
        }

        @Override
        public boolean apply(Game game, Ability source) {
            return false;
        }
    }
}
