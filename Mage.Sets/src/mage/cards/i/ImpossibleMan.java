package mage.cards.i;

import java.util.UUID;
import mage.MageInt;
import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.TargetPermanent;
import mage.util.functions.CopyApplier;

/**
 *
 * @author muz
 */
public final class ImpossibleMan extends CardImpl {

    public ImpossibleMan(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.ALIEN);
        this.subtype.add(SubType.SHAPESHIFTER);
        this.power = new MageInt(1);
        this.toughness = new MageInt(4);

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // {2}{U}: Impossible Man becomes a copy of another target permanent until end of turn, except his name is Impossible Man.
        Ability ability = new SimpleActivatedAbility(new ImpossibleManCopyEffect(), new ManaCostsImpl<>("{2}{U}"));
        ability.addTarget(new TargetPermanent(StaticFilters.FILTER_ANOTHER_PERMANENT));
        this.addAbility(ability);
    }

    private ImpossibleMan(final ImpossibleMan card) {
        super(card);
    }

    @Override
    public ImpossibleMan copy() {
        return new ImpossibleMan(this);
    }
}

class ImpossibleManCopyEffect extends OneShotEffect {

    ImpossibleManCopyEffect() {
        super(Outcome.Copy);
        staticText = "{this} becomes a copy of another target permanent until end of turn, except his name is Impossible Man";
    }

    private ImpossibleManCopyEffect(final ImpossibleManCopyEffect effect) {
        super(effect);
    }

    @Override
    public ImpossibleManCopyEffect copy() {
        return new ImpossibleManCopyEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent sourcePermanent = game.getPermanent(source.getSourceId());
        Permanent copyFromPermanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (sourcePermanent != null && copyFromPermanent != null) {
            game.copyPermanent(Duration.EndOfTurn, copyFromPermanent, sourcePermanent.getId(), source, new CopyApplier() {
                @Override
                public boolean apply(Game game, MageObject blueprint, Ability source, UUID copyToObjectId) {
                    blueprint.setName("Impossible Man");
                    return true;
                }
            });
            return true;
        }
        return false;
    }
}
