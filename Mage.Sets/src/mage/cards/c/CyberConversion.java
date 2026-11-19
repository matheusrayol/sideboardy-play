package mage.cards.c;

import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.costs.Costs;
import mage.abilities.costs.Cost;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.BecomesFaceDownCreatureEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.common.TargetCreaturePermanent;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class CyberConversion extends CardImpl {

    public CyberConversion(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{U}{U}");

        // Turn target creature face down. It's a 2/2 Cyberman artifact creature.
        this.getSpellAbility().addEffect(new CyberConversionEffect());
        this.getSpellAbility().addTarget(new TargetCreaturePermanent());
    }

    private CyberConversion(final CyberConversion card) {
        super(card);
    }

    @Override
    public CyberConversion copy() {
        return new CyberConversion(this);
    }
}

class CyberConversionEffect extends OneShotEffect {

    CyberConversionEffect() {
        super(Outcome.Transform);
        this.staticText = "Turn target creature face down. It's a 2/2 Cyberman artifact creature.";
    }

    private CyberConversionEffect(final CyberConversionEffect effect) {
        super(effect);
    }

    @Override
    public CyberConversionEffect copy() {
        return new CyberConversionEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (permanent == null || !BecomesFaceDownCreatureEffect.canBeTurnedFaceDown(permanent)) {
            return false;
        }
        Ability newSource = source.copy();
        newSource.setWorksFaceDown(true);
        game.addEffect(new CyberConversionFaceDownEffect(new MageObjectReference(permanent, game)), newSource);
        return true;
    }
}

class CyberConversionFaceDownEffect extends BecomesFaceDownCreatureEffect {

    CyberConversionFaceDownEffect(MageObjectReference objectReference) {
        super((Costs<Cost>) null, objectReference, Duration.Custom, FaceDownType.MANUAL);
    }

    private CyberConversionFaceDownEffect(final CyberConversionFaceDownEffect effect) {
        super(effect);
    }

    @Override
    public CyberConversionFaceDownEffect copy() {
        return new CyberConversionFaceDownEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        if (!super.apply(game, source)) {
            return false;
        }
        Permanent permanent = objectReference != null ? objectReference.getPermanent(game) : null;
        if (permanent != null && permanent.isFaceDown(game)) {
            permanent.addCardType(game, CardType.ARTIFACT);
            permanent.addSubType(game, SubType.CYBERMAN);
        }
        return true;
    }
}
