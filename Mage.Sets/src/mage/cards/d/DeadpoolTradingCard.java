package mage.cards.d;

import mage.MageInt;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.AsEntersBattlefieldAbility;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.SacrificeSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.DrawCardAllEffect;
import mage.abilities.effects.common.LoseLifeSourceControllerEffect;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.TargetPermanent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class DeadpoolTradingCard extends CardImpl {

    public DeadpoolTradingCard(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{B}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.MUTANT);
        this.subtype.add(SubType.MERCENARY);
        this.subtype.add(SubType.HERO);
        this.power = new MageInt(5);
        this.toughness = new MageInt(3);

        // As Deadpool enters, you may exchange his text box and another creature's.
        this.addAbility(new AsEntersBattlefieldAbility(
                new DeadpoolExchangeEffect(),
                "you may exchange his text box and another creature's"
        ));

        // At the beginning of your upkeep, you lose 3 life.
        this.addAbility(new BeginningOfUpkeepTriggeredAbility(new LoseLifeSourceControllerEffect(3)));

        // {3}, Sacrifice this creature: Each other player draws a card.
        Ability ability = new SimpleActivatedAbility(
                new DrawCardAllEffect(1, TargetController.OPPONENT).setText("Each other player draws a card"),
                new GenericManaCost(3)
        );
        ability.addCost(new SacrificeSourceCost());
        this.addAbility(ability);
    }

    private DeadpoolTradingCard(final DeadpoolTradingCard card) {
        super(card);
    }

    @Override
    public DeadpoolTradingCard copy() {
        return new DeadpoolTradingCard(this);
    }
}

class DeadpoolExchangeEffect extends OneShotEffect {

    DeadpoolExchangeEffect() {
        super(Outcome.Benefit);
        this.staticText = "you may exchange {this}'s text box and another creature's";
    }

    private DeadpoolExchangeEffect(final DeadpoolExchangeEffect effect) {
        super(effect);
    }

    @Override
    public DeadpoolExchangeEffect copy() {
        return new DeadpoolExchangeEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Permanent entering = game.getPermanentEntering(source.getSourceId());
        if (controller == null || entering == null) {
            return false;
        }
        TargetPermanent target = new TargetPermanent(1, 1, StaticFilters.FILTER_PERMANENT_CREATURE, true);
        target.withChooseHint("to exchange text boxes with");
        if (!target.canChoose(controller.getId(), source, game)) {
            return true;
        }
        if (!controller.chooseUse(Outcome.Benefit, "Exchange " + entering.getLogName() + "'s text box with another creature's?", source, game)) {
            return true;
        }
        if (!controller.chooseTarget(Outcome.Benefit, target, source, game)) {
            return true;
        }
        Permanent other = game.getPermanent(target.getFirstTarget());
        if (other == null) {
            return false;
        }

        List<Ability> deadpoolAbilities = new ArrayList<>();
        for (Ability a : entering.getAbilities()) {
            deadpoolAbilities.add(a.copy());
        }
        List<Ability> otherAbilities = new ArrayList<>();
        for (Ability a : other.getAbilities()) {
            otherAbilities.add(a.copy());
        }

        MageObjectReference deadpoolRef = new MageObjectReference(entering.getId(), entering.getZoneChangeCounter(game), game);
        MageObjectReference otherRef = new MageObjectReference(other, game);
        game.addEffect(new DeadpoolTextBoxContinuousEffect(deadpoolRef, otherRef, deadpoolAbilities, otherAbilities), source);
        return true;
    }
}

class DeadpoolTextBoxContinuousEffect extends ContinuousEffectImpl {

    private final MageObjectReference deadpoolRef;
    private final MageObjectReference otherRef;
    private final List<Ability> deadpoolAbilities;
    private final List<Ability> otherAbilities;

    DeadpoolTextBoxContinuousEffect(
            MageObjectReference deadpoolRef,
            MageObjectReference otherRef,
            List<Ability> deadpoolAbilities,
            List<Ability> otherAbilities
    ) {
        super(Duration.Custom, Layer.TextChangingEffects_3, SubLayer.NA, Outcome.Benefit);
        this.deadpoolRef = deadpoolRef;
        this.otherRef = otherRef;
        this.deadpoolAbilities = new ArrayList<>();
        for (Ability a : deadpoolAbilities) {
            this.deadpoolAbilities.add(a.copy());
        }
        this.otherAbilities = new ArrayList<>();
        for (Ability a : otherAbilities) {
            this.otherAbilities.add(a.copy());
        }
    }

    private DeadpoolTextBoxContinuousEffect(final DeadpoolTextBoxContinuousEffect effect) {
        super(effect);
        this.deadpoolRef = effect.deadpoolRef;
        this.otherRef = effect.otherRef;
        this.deadpoolAbilities = new ArrayList<>();
        for (Ability a : effect.deadpoolAbilities) {
            this.deadpoolAbilities.add(a.copy());
        }
        this.otherAbilities = new ArrayList<>();
        for (Ability a : effect.otherAbilities) {
            this.otherAbilities.add(a.copy());
        }
    }

    @Override
    public DeadpoolTextBoxContinuousEffect copy() {
        return new DeadpoolTextBoxContinuousEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent deadpool = deadpoolRef.getPermanent(game);
        Permanent other = otherRef.getPermanent(game);
        if (deadpool == null && other == null) {
            discard();
            return false;
        }
        if (deadpool != null) {
            deadpool.removeAllAbilities(source.getSourceId(), game);
            for (Ability a : otherAbilities) {
                deadpool.addAbility(a.copy(), source.getSourceId(), game);
            }
        }
        if (other != null) {
            other.removeAllAbilities(source.getSourceId(), game);
            for (Ability a : deadpoolAbilities) {
                other.addAbility(a.copy(), source.getSourceId(), game);
            }
        }
        return true;
    }
}
