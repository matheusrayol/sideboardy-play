package mage.cards.s;

import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTappedAbility;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.mana.BlueManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.common.TargetControlledCreaturePermanent;
import mage.util.functions.CopyApplier;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class ShoreLeavePlanet extends CardImpl {

    public ShoreLeavePlanet(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");

        this.subtype.add(SubType.PLANET);

        // This land enters tapped.
        this.addAbility(new EntersBattlefieldTappedAbility());

        // {T}: Add {U}.
        this.addAbility(new BlueManaAbility());

        // {4}{U}: This land becomes a copy of target creature you control until end of turn,
        // except it isn't legendary and it's a Robot artifact in addition to its other types.
        Ability ability = new SimpleActivatedAbility(new ShoreLeavePlanetEffect(), new ManaCostsImpl<>("{4}{U}"));
        ability.addTarget(new TargetControlledCreaturePermanent());
        this.addAbility(ability);
    }

    private ShoreLeavePlanet(final ShoreLeavePlanet card) {
        super(card);
    }

    @Override
    public ShoreLeavePlanet copy() {
        return new ShoreLeavePlanet(this);
    }
}

class ShoreLeavePlanetEffect extends OneShotEffect {

    ShoreLeavePlanetEffect() {
        super(Outcome.Copy);
        this.staticText = "{this} becomes a copy of target creature you control until end of turn, "
                + "except it isn't legendary and it's a Robot artifact in addition to its other types";
    }

    private ShoreLeavePlanetEffect(final ShoreLeavePlanetEffect effect) {
        super(effect);
    }

    @Override
    public ShoreLeavePlanetEffect copy() {
        return new ShoreLeavePlanetEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent sourcePermanent = source.getSourcePermanentIfItStillExists(game);
        Permanent targetPermanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (sourcePermanent == null || targetPermanent == null) {
            return false;
        }
        game.copyPermanent(Duration.EndOfTurn, targetPermanent, sourcePermanent.getId(), source, ShoreLeavePlanetCopyApplier.instance);
        return true;
    }
}

class ShoreLeavePlanetCopyApplier extends CopyApplier {

    static final ShoreLeavePlanetCopyApplier instance = new ShoreLeavePlanetCopyApplier();

    @Override
    public boolean apply(Game game, MageObject blueprint, Ability source, UUID copyToObjectId) {
        blueprint.removeSuperType(SuperType.LEGENDARY);
        blueprint.addCardType(CardType.ARTIFACT);
        blueprint.addSubType(SubType.ROBOT);
        return true;
    }
}
