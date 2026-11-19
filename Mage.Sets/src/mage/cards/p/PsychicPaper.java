package mage.cards.p;

import mage.abilities.Ability;
import mage.abilities.common.AsBecomesAttachedToCreatureSourceAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.ChooseACardNameEffect;
import mage.abilities.effects.common.ChooseCreatureTypeEffect;
import mage.abilities.effects.common.combat.CantBeBlockedAttachedEffect;
import mage.abilities.effects.common.continuous.GainAbilityAttachedEffect;
import mage.abilities.keyword.EquipAbility;
import mage.abilities.keyword.WardAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class PsychicPaper extends CardImpl {

    public PsychicPaper(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{2}");

        this.subtype.add(SubType.EQUIPMENT);

        // As this Equipment becomes attached to a creature, choose a creature card name and a creature type.
        AsBecomesAttachedToCreatureSourceAbility attachAbility = new AsBecomesAttachedToCreatureSourceAbility(
                new ChooseACardNameEffect(ChooseACardNameEffect.TypeOfName.CREATURE_NAME),
                "choose a creature card name and a creature type."
        );
        attachAbility.addEffect(new ChooseCreatureTypeEffect(Outcome.Benefit));
        this.addAbility(attachAbility);

        // Equipped creature has ward {1}, it can't be blocked, and its name and creature type are the last chosen name and creature type.
        Ability staticAbility = new SimpleStaticAbility(new GainAbilityAttachedEffect(
                new WardAbility(new ManaCostsImpl<>("{1}"), false),
                AttachmentType.EQUIPMENT
        ));
        staticAbility.addEffect(new CantBeBlockedAttachedEffect(AttachmentType.EQUIPMENT)
                .setText(", it can't be blocked"));
        staticAbility.addEffect(new PsychicPaperContinuousEffect());
        this.addAbility(staticAbility);

        // Equip {2}
        this.addAbility(new EquipAbility(2));
    }

    private PsychicPaper(final PsychicPaper card) {
        super(card);
    }

    @Override
    public PsychicPaper copy() {
        return new PsychicPaper(this);
    }
}

class PsychicPaperContinuousEffect extends ContinuousEffectImpl {

    PsychicPaperContinuousEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Benefit);
        this.staticText = ", and its name and creature type are the last chosen name and creature type";
    }

    private PsychicPaperContinuousEffect(final PsychicPaperContinuousEffect effect) {
        super(effect);
    }

    @Override
    public PsychicPaperContinuousEffect copy() {
        return new PsychicPaperContinuousEffect(this);
    }

    @Override
    public boolean apply(Layer layer, SubLayer sublayer, Ability source, Game game) {
        Permanent permanent = source.getPermanentSourceAttachedToIfItStillExists(game);
        if (permanent == null) {
            return false;
        }
        switch (layer) {
            case TextChangingEffects_3:
                String chosenName = (String) game.getState().getValue(
                        source.getSourceId().toString() + ChooseACardNameEffect.INFO_KEY
                );
                if (chosenName != null && !chosenName.isEmpty()) {
                    permanent.setName(chosenName);
                }
                return true;
            case TypeChangingEffects_4:
                SubType chosenType = ChooseCreatureTypeEffect.getChosenCreatureType(source.getSourceId(), game);
                if (chosenType != null) {
                    permanent.removeAllCreatureTypes(game);
                    permanent.addSubType(game, chosenType);
                }
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean apply(Game game, Ability source) {
        return false;
    }

    @Override
    public boolean hasLayer(Layer layer) {
        return layer == Layer.TextChangingEffects_3 || layer == Layer.TypeChangingEffects_4;
    }
}
