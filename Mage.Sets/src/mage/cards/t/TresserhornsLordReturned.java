package mage.cards.t;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SacrificePermanentTriggeredAbility;
import mage.abilities.costs.common.PayLifeCost;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.keyword.IndestructibleAbility;
import mage.abilities.keyword.TrampleAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetOpponent;
import mage.target.common.TargetSacrifice;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class TresserhornsLordReturned extends CardImpl {

    public TresserhornsLordReturned(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{U}{B}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.ZOMBIE);
        this.subtype.add(SubType.NOBLE);
        this.power = new MageInt(10);
        this.toughness = new MageInt(4);

        // Trample
        this.addAbility(TrampleAbility.getInstance());

        // Indestructible
        this.addAbility(IndestructibleAbility.getInstance());

        // When Tresserhorn's Lord enters, sacrifice him unless you sacrifice three creatures, pay 3 life, and have target opponent draw three cards.
        Ability etbAbility = new EntersBattlefieldTriggeredAbility(new TresserhornsLordReturnedEffect());
        etbAbility.addTarget(new TargetOpponent());
        this.addAbility(etbAbility);

        // Whenever you sacrifice a creature, draw a card.
        this.addAbility(new SacrificePermanentTriggeredAbility(
                new DrawCardSourceControllerEffect(1),
                StaticFilters.FILTER_PERMANENT_CREATURE
        ));
    }

    private TresserhornsLordReturned(final TresserhornsLordReturned card) {
        super(card);
    }

    @Override
    public TresserhornsLordReturned copy() {
        return new TresserhornsLordReturned(this);
    }
}

class TresserhornsLordReturnedEffect extends OneShotEffect {

    TresserhornsLordReturnedEffect() {
        super(Outcome.Sacrifice);
        this.staticText = "sacrifice him unless you sacrifice three creatures, pay 3 life, and have target opponent draw three cards";
    }

    private TresserhornsLordReturnedEffect(final TresserhornsLordReturnedEffect effect) {
        super(effect);
    }

    @Override
    public TresserhornsLordReturnedEffect copy() {
        return new TresserhornsLordReturnedEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Permanent sourcePermanent = source.getSourcePermanentIfItStillExists(game);
        Player opponent = game.getPlayer(getTargetPointer().getFirst(game, source));
        if (controller == null) {
            return false;
        }
        PayLifeCost lifeCost = new PayLifeCost(3);
        int controlledCreatures = game.getBattlefield().count(
                StaticFilters.FILTER_CONTROLLED_CREATURE, controller.getId(), source, game
        );
        boolean canPay = controlledCreatures >= 3
                && opponent != null
                && controller.canPayLifeCost(source)
                && lifeCost.canPay(source, source, controller.getId(), game);

        if (canPay && controller.chooseUse(
                Outcome.Detriment,
                "Sacrifice three creatures, pay 3 life, and have " + opponent.getName() + " draw three cards?",
                source,
                game
        )) {
            TargetSacrifice target = new TargetSacrifice(3, 3, StaticFilters.FILTER_CONTROLLED_CREATURE);
            if (controller.choose(Outcome.Sacrifice, target, source, game) && target.getTargets().size() == 3) {
                for (UUID targetId : target.getTargets()) {
                    Permanent sac = game.getPermanent(targetId);
                    if (sac != null) {
                        sac.sacrifice(source, game);
                    }
                }
                lifeCost.pay(source, game, source, controller.getId(), false, null);
                opponent.drawCards(3, source, game);
                return true;
            }
        }
        if (sourcePermanent != null) {
            sourcePermanent.sacrifice(source, game);
        }
        return true;
    }
}
