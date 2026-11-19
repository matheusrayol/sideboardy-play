package mage.cards.s;

import java.util.UUID;
import mage.abilities.Ability;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.token.FoodAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Layer;
import mage.constants.Outcome;
import mage.constants.SubLayer;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.targetpointer.FixedTarget;
import mage.watchers.common.CardsPutIntoGraveyardWatcher;

/**
 *
 * @author muz
 */
public final class SupperForSpiders extends CardImpl {

    public SupperForSpiders(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{1}{B}");

        // Put onto the battlefield under your control all creature cards in your opponents' graveyards that were put there from the battlefield this turn. They are Food artifacts with "{2}, {T}, Sacrifice this artifact: You gain 3 life." (They lose all other types and subtypes.)
        this.getSpellAbility().addEffect(new SupperForSpidersEffect());
        this.getSpellAbility().addWatcher(new CardsPutIntoGraveyardWatcher());
    }

    private SupperForSpiders(final SupperForSpiders card) {
        super(card);
    }

    @Override
    public SupperForSpiders copy() {
        return new SupperForSpiders(this);
    }
}

class SupperForSpidersEffect extends OneShotEffect {

    SupperForSpidersEffect() {
        super(Outcome.PutCreatureInPlay);
        staticText = "Put onto the battlefield under your control all creature cards in your opponents' graveyards that were put there from the battlefield this turn. They are Food artifacts with \"{2}, {T}, Sacrifice this artifact: You gain 3 life.\" <i>(They lose all other types and subtypes.)</i>";
    }

    private SupperForSpidersEffect(final SupperForSpidersEffect effect) {
        super(effect);
    }

    @Override
    public SupperForSpidersEffect copy() {
        return new SupperForSpidersEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        CardsPutIntoGraveyardWatcher watcher = game.getState().getWatcher(CardsPutIntoGraveyardWatcher.class);
        if (controller == null || watcher == null) {
            return false;
        }
        Cards cards = new CardsImpl();
        for (Card graveCard : watcher.getCardsPutIntoGraveyardFromBattlefield(game)) {
            Card card = graveCard != null ? game.getCard(graveCard.getId()) : null;
            if (card != null
                    && card.isCreature(game)
                    && game.getOpponents(controller.getId()).contains(card.getOwnerId())
                    && game.getState().getZone(card.getId()) == Zone.GRAVEYARD) {
                cards.add(card);
                SupperForSpidersContinuousEffect effect = new SupperForSpidersContinuousEffect();
                effect.setTargetPointer(new FixedTarget(card.getId(), game.getState().getZoneChangeCounter(card.getId()) + 1));
                game.addEffect(effect, source);
            }
        }
        if (cards.isEmpty()) {
            return true;
        }
        return controller.moveCards(cards, Zone.BATTLEFIELD, source, game);
    }
}

class SupperForSpidersContinuousEffect extends ContinuousEffectImpl {

    SupperForSpidersContinuousEffect() {
        super(Duration.Custom, Outcome.Neutral);
    }

    private SupperForSpidersContinuousEffect(final SupperForSpidersContinuousEffect effect) {
        super(effect);
    }

    @Override
    public SupperForSpidersContinuousEffect copy() {
        return new SupperForSpidersContinuousEffect(this);
    }

    @Override
    public boolean apply(Layer layer, SubLayer sublayer, Ability source, Game game) {
        Permanent permanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (permanent == null) {
            discard();
            return false;
        }
        switch (layer) {
            case TypeChangingEffects_4:
                permanent.removeAllCardTypes(game);
                permanent.removeAllSubTypes(game);
                permanent.addCardType(game, CardType.ARTIFACT);
                permanent.addSubType(game, SubType.FOOD);
                return true;
            case AbilityAddingRemovingEffects_6:
                permanent.addAbility(new FoodAbility(), source.getSourceId(), game);
                return true;
        }
        return false;
    }

    @Override
    public boolean apply(Game game, Ability source) {
        return false;
    }

    @Override
    public boolean hasLayer(Layer layer) {
        return layer == Layer.TypeChangingEffects_4 || layer == Layer.AbilityAddingRemovingEffects_6;
    }
}
