package mage.cards.c;

import mage.MageInt;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.DealsCombatDamageToAPlayerTriggeredAbility;
import mage.abilities.costs.Cost;
import mage.abilities.costs.Costs;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.BecomesFaceDownCreatureEffect;
import mage.abilities.keyword.CrewAbility;
import mage.abilities.keyword.FlyingAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;

import java.util.Set;
import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class Cybership extends CardImpl {

    public Cybership(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{6}");

        this.subtype.add(SubType.VEHICLE);
        this.power = new MageInt(8);
        this.toughness = new MageInt(8);

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // Whenever this Vehicle deals combat damage to a player, put the top two cards of that player's library onto the battlefield face down under your control. They're 2/2 Cyberman artifact creatures.
        this.addAbility(new DealsCombatDamageToAPlayerTriggeredAbility(new CybershipEffect(), false, true));

        // Crew 4
        this.addAbility(new CrewAbility(4));
    }

    private Cybership(final Cybership card) {
        super(card);
    }

    @Override
    public Cybership copy() {
        return new Cybership(this);
    }
}

class CybershipEffect extends OneShotEffect {

    CybershipEffect() {
        super(Outcome.PutCreatureInPlay);
        this.staticText = "put the top two cards of that player's library onto the battlefield face down under your control. They're 2/2 Cyberman artifact creatures";
    }

    private CybershipEffect(final CybershipEffect effect) {
        super(effect);
    }

    @Override
    public CybershipEffect copy() {
        return new CybershipEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Player damagedPlayer = game.getPlayer(getTargetPointer().getFirst(game, source));
        if (controller == null || damagedPlayer == null) {
            return false;
        }
        Set<Card> topCards = damagedPlayer.getLibrary().getTopCards(game, 2);
        if (topCards.isEmpty()) {
            return true;
        }
        Ability newSource = source.copy();
        newSource.setWorksFaceDown(true);
        for (Card card : topCards) {
            Card bfCard = BecomesFaceDownCreatureEffect.findDefaultCardSideForFaceDown(game, card);
            MageObjectReference mor = new MageObjectReference(bfCard.getId(), bfCard.getZoneChangeCounter(game) + 1, game);
            game.addEffect(new CybershipFaceDownEffect(mor), newSource);
        }
        controller.moveCards(topCards, Zone.BATTLEFIELD, source, game, false, true, false, null);
        return true;
    }
}

class CybershipFaceDownEffect extends BecomesFaceDownCreatureEffect {

    CybershipFaceDownEffect(MageObjectReference objectReference) {
        super((Costs<Cost>) null, objectReference, Duration.Custom, FaceDownType.MANUAL);
    }

    private CybershipFaceDownEffect(final CybershipFaceDownEffect effect) {
        super(effect);
    }

    @Override
    public CybershipFaceDownEffect copy() {
        return new CybershipFaceDownEffect(this);
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
