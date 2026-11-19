package mage.cards.d;

import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.SagaAbility;
import mage.abilities.costs.Cost;
import mage.abilities.costs.Costs;
import mage.abilities.effects.Effects;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.BecomesFaceDownCreatureEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.ExileZone;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.TargetPlayer;
import mage.util.CardUtil;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class DeathInHeaven extends CardImpl {

    public DeathInHeaven(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{3}{B}");

        this.subtype.add(SubType.SAGA);

        // (As this Saga enters and after your draw step, add a lore counter. Sacrifice after III.)
        SagaAbility sagaAbility = new SagaAbility(this, SagaChapter.CHAPTER_III);

        // I, II — Target player mills two cards, then exiles their graveyard.
        sagaAbility.addChapterEffect(
                this, SagaChapter.CHAPTER_I, SagaChapter.CHAPTER_II,
                new Effects(new DeathInHeavenExileEffect()), new TargetPlayer()
        );

        // III — Put all creature cards exiled with this enchantment onto the battlefield face down under your control. They're 2/2 Cyberman artifact creatures.
        sagaAbility.addChapterEffect(
                this, SagaChapter.CHAPTER_III, new DeathInHeavenReturnEffect()
        );

        this.addAbility(sagaAbility);
    }

    private DeathInHeaven(final DeathInHeaven card) {
        super(card);
    }

    @Override
    public DeathInHeaven copy() {
        return new DeathInHeaven(this);
    }
}

class DeathInHeavenExileEffect extends OneShotEffect {

    DeathInHeavenExileEffect() {
        super(Outcome.Exile);
        this.staticText = "target player mills two cards, then exiles their graveyard";
    }

    private DeathInHeavenExileEffect(final DeathInHeavenExileEffect effect) {
        super(effect);
    }

    @Override
    public DeathInHeavenExileEffect copy() {
        return new DeathInHeavenExileEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(getTargetPointer().getFirst(game, source));
        if (player == null) {
            return false;
        }
        player.millCards(2, source, game);
        if (!player.getGraveyard().isEmpty()) {
            player.moveCardsToExile(
                    player.getGraveyard().getCards(game),
                    source,
                    game,
                    true,
                    CardUtil.getExileZoneId(game, source),
                    CardUtil.getSourceName(game, source)
            );
        }
        return true;
    }
}

class DeathInHeavenReturnEffect extends OneShotEffect {

    DeathInHeavenReturnEffect() {
        super(Outcome.PutCreatureInPlay);
        this.staticText = "put all creature cards exiled with this enchantment onto the battlefield face down under your control. They're 2/2 Cyberman artifact creatures";
    }

    private DeathInHeavenReturnEffect(final DeathInHeavenReturnEffect effect) {
        super(effect);
    }

    @Override
    public DeathInHeavenReturnEffect copy() {
        return new DeathInHeavenReturnEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        ExileZone exileZone = game.getExile().getExileZone(CardUtil.getExileZoneId(game, source));
        if (exileZone == null || exileZone.isEmpty()) {
            return true;
        }
        Set<Card> creatures = new LinkedHashSet<>();
        for (Card card : exileZone.getCards(game)) {
            if (card.isCreature(game)) {
                creatures.add(card);
            }
        }
        if (creatures.isEmpty()) {
            return true;
        }
        Ability newSource = source.copy();
        newSource.setWorksFaceDown(true);
        for (Card card : creatures) {
            Card bfCard = BecomesFaceDownCreatureEffect.findDefaultCardSideForFaceDown(game, card);
            MageObjectReference mor = new MageObjectReference(bfCard.getId(), bfCard.getZoneChangeCounter(game) + 1, game);
            game.addEffect(new DeathInHeavenFaceDownEffect(mor), newSource);
        }
        controller.moveCards(creatures, Zone.BATTLEFIELD, source, game, false, true, false, null);
        return true;
    }
}

class DeathInHeavenFaceDownEffect extends BecomesFaceDownCreatureEffect {

    DeathInHeavenFaceDownEffect(MageObjectReference objectReference) {
        super((Costs<Cost>) null, objectReference, Duration.Custom, FaceDownType.MANUAL);
    }

    private DeathInHeavenFaceDownEffect(final DeathInHeavenFaceDownEffect effect) {
        super(effect);
    }

    @Override
    public DeathInHeavenFaceDownEffect copy() {
        return new DeathInHeavenFaceDownEffect(this);
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
