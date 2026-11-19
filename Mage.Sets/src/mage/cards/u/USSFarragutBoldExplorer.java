package mage.cards.u;

import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.LandfallAbility;
import mage.abilities.common.delayed.AtTheBeginOfNextEndStepDelayedTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.GainAbilityTargetEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.HasteAbility;
import mage.abilities.keyword.StationAbility;
import mage.abilities.keyword.StationLevelAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetCardInHand;
import mage.target.targetpointer.FixedTarget;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class USSFarragutBoldExplorer extends CardImpl {

    public USSFarragutBoldExplorer(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{6}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.SPACECRAFT);

        // Landfall — Whenever a land you control enters, you may put a creature card from your hand onto the battlefield.
        // That creature gains haste. At the beginning of the next end step, put it on the bottom of your library. When you do, draw a card.
        this.addAbility(new LandfallAbility(new USSFarragutEffect(), false));

        // Station
        this.addAbility(new StationAbility());

        // STATION 7+
        // Flying
        // 4/4
        this.addAbility(new StationLevelAbility(7)
                .withLevelAbility(FlyingAbility.getInstance())
                .withPT(4, 4));
    }

    private USSFarragutBoldExplorer(final USSFarragutBoldExplorer card) {
        super(card);
    }

    @Override
    public USSFarragutBoldExplorer copy() {
        return new USSFarragutBoldExplorer(this);
    }
}

class USSFarragutEffect extends OneShotEffect {

    USSFarragutEffect() {
        super(Outcome.PutCreatureInPlay);
        this.staticText = "you may put a creature card from your hand onto the battlefield. That creature gains haste. "
                + "At the beginning of the next end step, put it on the bottom of your library. When you do, draw a card";
    }

    private USSFarragutEffect(final USSFarragutEffect effect) {
        super(effect);
    }

    @Override
    public USSFarragutEffect copy() {
        return new USSFarragutEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        TargetCardInHand target = new TargetCardInHand(0, 1, StaticFilters.FILTER_CARD_CREATURE);
        if (!controller.choose(Outcome.PutCreatureInPlay, target, source, game)) {
            return true;
        }
        Card card = game.getCard(target.getFirstTarget());
        if (card == null) {
            return true;
        }
        if (!controller.moveCards(card, Zone.BATTLEFIELD, source, game)) {
            return false;
        }
        Permanent permanent = CardUtil.getPermanentFromCardPutToBattlefield(card, game);
        if (permanent != null) {
            game.addEffect(new GainAbilityTargetEffect(HasteAbility.getInstance(), Duration.Custom)
                    .setTargetPointer(new FixedTarget(permanent, game)), source);
            game.addDelayedTriggeredAbility(new AtTheBeginOfNextEndStepDelayedTriggeredAbility(
                    new USSFarragutDelayedEffect(new MageObjectReference(permanent, game))
            ), source);
        }
        return true;
    }
}

class USSFarragutDelayedEffect extends OneShotEffect {

    private final MageObjectReference targetMor;

    USSFarragutDelayedEffect(MageObjectReference targetMor) {
        super(Outcome.ReturnToHand);
        this.targetMor = targetMor;
        this.staticText = "put it on the bottom of your library. When you do, draw a card";
    }

    private USSFarragutDelayedEffect(final USSFarragutDelayedEffect effect) {
        super(effect);
        this.targetMor = effect.targetMor;
    }

    @Override
    public USSFarragutDelayedEffect copy() {
        return new USSFarragutDelayedEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Permanent permanent = targetMor.getPermanent(game);
        if (controller == null || permanent == null) {
            return true;
        }
        if (controller.putCardsOnBottomOfLibrary(permanent, game, source)) {
            ReflexiveAbilityTriggerHelper.drawIfPutOnBottom(controller, source, game);
        }
        return true;
    }
}

final class ReflexiveAbilityTriggerHelper {
    private ReflexiveAbilityTriggerHelper() {
    }

    static void drawIfPutOnBottom(Player controller, Ability source, Game game) {
        controller.drawCards(1, source, game);
    }
}
