package mage.cards.l;

import mage.MageInt;
import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.common.ActivateAsSorceryActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.OneShotEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.common.TargetControlledCreaturePermanent;
import mage.util.functions.CopyApplier;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class LokiLordOfMisrule extends CardImpl {

    public LokiLordOfMisrule(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.GOD);
        this.subtype.add(SubType.SORCERER);
        this.subtype.add(SubType.VILLAIN);
        this.power = new MageInt(3);
        this.toughness = new MageInt(4);

        // {U}, {T}: Choose target creature you control. Each creature you control other than the chosen creature becomes a copy of that creature until end of turn, except it isn't legendary. Activate only as a sorcery.
        Ability ability = new ActivateAsSorceryActivatedAbility(new LokiEffect(), new ManaCostsImpl<>("{U}"));
        ability.addCost(new TapSourceCost());
        ability.addTarget(new TargetControlledCreaturePermanent());
        this.addAbility(ability);
    }

    private LokiLordOfMisrule(final LokiLordOfMisrule card) {
        super(card);
    }

    @Override
    public LokiLordOfMisrule copy() {
        return new LokiLordOfMisrule(this);
    }
}

class LokiEffect extends OneShotEffect {

    LokiEffect() {
        super(Outcome.Copy);
        this.staticText = "choose target creature you control. Each creature you control other than the chosen creature "
                + "becomes a copy of that creature until end of turn, except it isn't legendary";
    }

    private LokiEffect(final LokiEffect effect) {
        super(effect);
    }

    @Override
    public LokiEffect copy() {
        return new LokiEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent chosen = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (chosen == null) {
            return false;
        }
        for (Permanent permanent : game.getBattlefield().getAllActivePermanents(
                StaticFilters.FILTER_PERMANENT_CREATURE, source.getControllerId(), game
        )) {
            if (!permanent.getId().equals(chosen.getId())) {
                game.copyPermanent(Duration.EndOfTurn, chosen, permanent.getId(), source, LokiCopyApplier.instance);
            }
        }
        return true;
    }
}

class LokiCopyApplier extends CopyApplier {

    static final LokiCopyApplier instance = new LokiCopyApplier();

    @Override
    public boolean apply(Game game, MageObject blueprint, Ability source, UUID copyToObjectId) {
        blueprint.removeSuperType(SuperType.LEGENDARY);
        return true;
    }
}
