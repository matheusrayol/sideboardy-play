package mage.cards.r;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.DrawNthCardTriggeredAbility;
import mage.abilities.common.delayed.AtTheBeginOfNextEndStepDelayedTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CreateTokenCopyTargetEffect;
import mage.abilities.effects.common.ExileTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.filter.predicate.mageobject.AnotherPredicate;
import mage.game.Game;
import mage.target.common.TargetControlledPermanent;
import mage.target.targetpointer.FixedTargets;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class RegBarclayFidgetyFantasist extends CardImpl {

    private static final FilterControlledCreaturePermanent filter =
            new FilterControlledCreaturePermanent("another target creature you control");

    static {
        filter.add(AnotherPredicate.instance);
    }

    public RegBarclayFidgetyFantasist(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{U}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.ARTIFICER);
        this.power = new MageInt(1);
        this.toughness = new MageInt(5);

        // Seek out — Whenever you draw your second card each turn, create a token that's a copy of another target creature you control,
        // except it isn't legendary, it's an Illusion artifact in addition to its other types, and it has haste.
        // Exile that token at the beginning of the next end step.
        Ability ability = new DrawNthCardTriggeredAbility(new RegBarclayEffect(), false, 2);
        ability.addTarget(new TargetControlledPermanent(filter));
        ability.withFlavorWord("Seek out");
        this.addAbility(ability);
    }

    private RegBarclayFidgetyFantasist(final RegBarclayFidgetyFantasist card) {
        super(card);
    }

    @Override
    public RegBarclayFidgetyFantasist copy() {
        return new RegBarclayFidgetyFantasist(this);
    }
}

class RegBarclayEffect extends OneShotEffect {

    RegBarclayEffect() {
        super(Outcome.PutCreatureInPlay);
        this.staticText = "create a token that's a copy of another target creature you control, "
                + "except it isn't legendary, it's an Illusion artifact in addition to its other types, and it has haste. "
                + "Exile that token at the beginning of the next end step";
    }

    private RegBarclayEffect(final RegBarclayEffect effect) {
        super(effect);
    }

    @Override
    public RegBarclayEffect copy() {
        return new RegBarclayEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        CreateTokenCopyTargetEffect effect = new CreateTokenCopyTargetEffect(
                null, CardType.ARTIFACT, true, 1, false, false
        );
        effect.setPermanentModifier(token -> {
            token.removeSuperType(SuperType.LEGENDARY);
            token.addSubType(SubType.ILLUSION);
        });
        effect.setTargetPointer(getTargetPointer().copy());
        if (!effect.apply(game, source)) {
            return false;
        }
        game.addDelayedTriggeredAbility(new AtTheBeginOfNextEndStepDelayedTriggeredAbility(
                new ExileTargetEffect().setTargetPointer(new FixedTargets(effect.getAddedPermanents(), game))
        ), source);
        return true;
    }
}
