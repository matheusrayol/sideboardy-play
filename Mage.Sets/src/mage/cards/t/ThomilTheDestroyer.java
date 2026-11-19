package mage.cards.t;

import mage.MageInt;
import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.LoyaltyAbility;
import mage.abilities.common.CanBeYourCommanderAbility;
import mage.abilities.costs.common.SacrificeTargetCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.DoIfCostPaid;
import mage.abilities.effects.mana.BasicManaEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.TrampleAbility;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.game.permanent.token.TokenImpl;
import mage.game.permanent.token.ZombieToken;
import mage.players.Player;
import mage.target.common.TargetSacrifice;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class ThomilTheDestroyer extends CardImpl {

    public ThomilTheDestroyer(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.PLANESWALKER}, "{3}{B}{B}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.THOMIL);
        this.setStartingLoyalty(4);

        // +2: Create a 2/2 black Zombie creature token.
        this.addAbility(new LoyaltyAbility(new CreateTokenEffect(new ZombieToken()), 2));

        // 0: You may sacrifice a creature. If you do, add {B}{B}{B}.
        this.addAbility(new LoyaltyAbility(
                new DoIfCostPaid(
                        new BasicManaEffect(Mana.BlackMana(3)),
                        new SacrificeTargetCost(StaticFilters.FILTER_PERMANENT_CREATURE)
                ),
                0
        ));

        // −5: Create a Lord of the Pit token. (It's a {4}{B}{B}{B} 7/7 Demon creature with flying, trample, and "At the beginning of your upkeep, sacrifice another creature. If you can't, this token deals 7 damage to you.")
        this.addAbility(new LoyaltyAbility(new CreateTokenEffect(new ThomilLordOfThePitToken()), -5));

        // Thomil, the Destroyer can be your commander.
        this.addAbility(CanBeYourCommanderAbility.getInstance());
    }

    private ThomilTheDestroyer(final ThomilTheDestroyer card) {
        super(card);
    }

    @Override
    public ThomilTheDestroyer copy() {
        return new ThomilTheDestroyer(this);
    }
}

class ThomilLordOfThePitToken extends TokenImpl {

    ThomilLordOfThePitToken() {
        super("Lord of the Pit", "Lord of the Pit token");
        cardType.add(CardType.CREATURE);
        subtype.add(SubType.DEMON);
        color.setBlack(true);
        manaCost = new ManaCostsImpl<>("{4}{B}{B}{B}");
        power = new MageInt(7);
        toughness = new MageInt(7);

        addAbility(FlyingAbility.getInstance());
        addAbility(TrampleAbility.getInstance());
        addAbility(new BeginningOfUpkeepTriggeredAbility(new ThomilLordOfThePitUpkeepEffect()));
    }

    private ThomilLordOfThePitToken(final ThomilLordOfThePitToken token) {
        super(token);
    }

    @Override
    public ThomilLordOfThePitToken copy() {
        return new ThomilLordOfThePitToken(this);
    }
}

class ThomilLordOfThePitUpkeepEffect extends OneShotEffect {

    ThomilLordOfThePitUpkeepEffect() {
        super(Outcome.Damage);
        this.staticText = "sacrifice another creature. If you can't, this token deals 7 damage to you";
    }

    private ThomilLordOfThePitUpkeepEffect(final ThomilLordOfThePitUpkeepEffect effect) {
        super(effect);
    }

    @Override
    public ThomilLordOfThePitUpkeepEffect copy() {
        return new ThomilLordOfThePitUpkeepEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        TargetSacrifice target = new TargetSacrifice(StaticFilters.FILTER_CONTROLLED_ANOTHER_CREATURE);
        if (target.canChoose(controller.getId(), source, game)
                && controller.choose(Outcome.Sacrifice, target, source, game)) {
            Permanent sacrifice = game.getPermanent(target.getFirstTarget());
            if (sacrifice != null && sacrifice.sacrifice(source, game)) {
                return true;
            }
        }
        controller.damage(7, source.getSourceId(), source, game);
        return true;
    }
}
