package mage.cards.t;

import mage.MageInt;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.Cost;
import mage.abilities.costs.Costs;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.BecomesFaceDownCreatureEffect;
import mage.abilities.effects.common.continuous.BoostControlledEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.util.CardUtil;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class TheCyberController extends CardImpl {

    public TheCyberController(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{X}{U}{U}{B}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.CYBERMAN);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // When The Cyber-Controller enters, each opponent mills X cards. Put all creature cards milled this way onto the battlefield face down under your control. They're 2/2 Cyberman artifact creatures.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new TheCyberControllerEffect()));

        // Other artifact creatures you control get +1/+1.
        this.addAbility(new SimpleStaticAbility(new BoostControlledEffect(
                1, 1, Duration.WhileOnBattlefield,
                StaticFilters.FILTER_PERMANENT_ARTIFACT_CREATURE, true
        )));
    }

    private TheCyberController(final TheCyberController card) {
        super(card);
    }

    @Override
    public TheCyberController copy() {
        return new TheCyberController(this);
    }
}

class TheCyberControllerEffect extends OneShotEffect {

    TheCyberControllerEffect() {
        super(Outcome.PutCreatureInPlay);
        this.staticText = "each opponent mills X cards. Put all creature cards milled this way onto the battlefield face down under your control. They're 2/2 Cyberman artifact creatures";
    }

    private TheCyberControllerEffect(final TheCyberControllerEffect effect) {
        super(effect);
    }

    @Override
    public TheCyberControllerEffect copy() {
        return new TheCyberControllerEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        int xValue = CardUtil.getSourceCostsTag(game, source, "X", 0);
        if (xValue <= 0) {
            return true;
        }
        Set<Card> creatureCards = new LinkedHashSet<>();
        for (UUID opponentId : game.getOpponents(controller.getId())) {
            Player opponent = game.getPlayer(opponentId);
            if (opponent == null) {
                continue;
            }
            Cards milled = opponent.millCards(xValue, source, game);
            for (Card card : milled.getCards(game)) {
                if (card.isCreature(game)) {
                    creatureCards.add(card);
                }
            }
        }
        if (creatureCards.isEmpty()) {
            return true;
        }
        Ability newSource = source.copy();
        newSource.setWorksFaceDown(true);
        for (Card card : creatureCards) {
            Card bfCard = BecomesFaceDownCreatureEffect.findDefaultCardSideForFaceDown(game, card);
            MageObjectReference mor = new MageObjectReference(bfCard.getId(), bfCard.getZoneChangeCounter(game) + 1, game);
            game.addEffect(new TheCyberControllerFaceDownEffect(mor), newSource);
        }
        controller.moveCards(creatureCards, Zone.BATTLEFIELD, source, game, false, true, false, null);
        return true;
    }
}

class TheCyberControllerFaceDownEffect extends BecomesFaceDownCreatureEffect {

    TheCyberControllerFaceDownEffect(MageObjectReference objectReference) {
        super((Costs<Cost>) null, objectReference, Duration.Custom, FaceDownType.MANUAL);
    }

    private TheCyberControllerFaceDownEffect(final TheCyberControllerFaceDownEffect effect) {
        super(effect);
    }

    @Override
    public TheCyberControllerFaceDownEffect copy() {
        return new TheCyberControllerFaceDownEffect(this);
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
