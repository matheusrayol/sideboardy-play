package mage.cards.n;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.BecomesTappedSourceTriggeredAbility;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.BecomesCreatureTypeTargetEffect;
import mage.abilities.effects.common.continuous.GainAbilityTargetEffect;
import mage.abilities.effects.common.continuous.PlayAdditionalLandsControllerEffect;
import mage.abilities.effects.common.continuous.SetBasePowerToughnessTargetEffect;
import mage.abilities.effects.common.ruleModifying.PlayFromGraveyardControllerEffect;
import mage.abilities.effects.keyword.SurveilEffect;
import mage.abilities.keyword.MenaceAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.targetpointer.FixedTarget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class NogAmbitiousCadet extends CardImpl {

    public NogAmbitiousCadet(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{B}{G}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.FERENGI);
        this.subtype.add(SubType.ROGUE);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Menace
        this.addAbility(new MenaceAbility(false));

        // {1}: If Nog is a Rogue, he becomes a Ferengi Scout with base power and toughness 3/4 and gains "Whenever Nog becomes tapped, surveil 2."
        this.addAbility(new SimpleActivatedAbility(new NogFirstStageEffect(), new GenericManaCost(1)));

        // {3}: If Nog is a Scout, he becomes a Ferengi Officer and gains "You may play lands from your graveyard" and "You may play an additional land on each of your turns."
        this.addAbility(new SimpleActivatedAbility(new NogSecondStageEffect(), new GenericManaCost(3)));
    }

    private NogAmbitiousCadet(final NogAmbitiousCadet card) {
        super(card);
    }

    @Override
    public NogAmbitiousCadet copy() {
        return new NogAmbitiousCadet(this);
    }
}

class NogFirstStageEffect extends OneShotEffect {

    NogFirstStageEffect() {
        super(Outcome.BoostCreature);
        this.staticText = "if {this} is a Rogue, he becomes a Ferengi Scout with base power and toughness 3/4 "
                + "and gains \"Whenever {this} becomes tapped, surveil 2.\"";
    }

    private NogFirstStageEffect(final NogFirstStageEffect effect) {
        super(effect);
    }

    @Override
    public NogFirstStageEffect copy() {
        return new NogFirstStageEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent nog = source.getSourcePermanentIfItStillExists(game);
        if (nog == null || !nog.hasSubtype(SubType.ROGUE, game)) {
            return false;
        }
        game.addEffect(new BecomesCreatureTypeTargetEffect(
                Duration.Custom,
                new ArrayList<>(Arrays.asList(SubType.FERENGI, SubType.SCOUT)),
                true
        ).setTargetPointer(new FixedTarget(nog, game)), source);
        game.addEffect(new SetBasePowerToughnessTargetEffect(
                3, 4, Duration.Custom
        ).setTargetPointer(new FixedTarget(nog, game)), source);
        game.addEffect(new GainAbilityTargetEffect(
                new BecomesTappedSourceTriggeredAbility(new SurveilEffect(2)),
                Duration.Custom
        ).setTargetPointer(new FixedTarget(nog, game)), source);
        return true;
    }
}

class NogSecondStageEffect extends OneShotEffect {

    NogSecondStageEffect() {
        super(Outcome.BoostCreature);
        this.staticText = "if {this} is a Scout, he becomes a Ferengi Officer and gains "
                + "\"You may play lands from your graveyard\" and "
                + "\"You may play an additional land on each of your turns.\"";
    }

    private NogSecondStageEffect(final NogSecondStageEffect effect) {
        super(effect);
    }

    @Override
    public NogSecondStageEffect copy() {
        return new NogSecondStageEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent nog = source.getSourcePermanentIfItStillExists(game);
        if (nog == null || !nog.hasSubtype(SubType.SCOUT, game)) {
            return false;
        }
        game.addEffect(new BecomesCreatureTypeTargetEffect(
                Duration.Custom,
                new ArrayList<>(Arrays.asList(SubType.FERENGI, SubType.OFFICER)),
                true
        ).setTargetPointer(new FixedTarget(nog, game)), source);
        game.addEffect(new GainAbilityTargetEffect(
                new SimpleStaticAbility(PlayFromGraveyardControllerEffect.playLands()),
                Duration.Custom
        ).setTargetPointer(new FixedTarget(nog, game)), source);
        game.addEffect(new GainAbilityTargetEffect(
                new SimpleStaticAbility(new PlayAdditionalLandsControllerEffect(1, Duration.WhileOnBattlefield)),
                Duration.Custom
        ).setTargetPointer(new FixedTarget(nog, game)), source);
        return true;
    }
}
