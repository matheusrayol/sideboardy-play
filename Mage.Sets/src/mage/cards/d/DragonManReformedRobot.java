package mage.cards.d;

import mage.MageIdentifier;
import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.SpellAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.Cost;
import mage.abilities.costs.Costs;
import mage.abilities.costs.CostsImpl;
import mage.abilities.costs.common.DiscardCardCost;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.effects.AsThoughEffectImpl;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.continuous.SetBasePowerSourceEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class DragonManReformedRobot extends CardImpl {

    public DragonManReformedRobot(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{2}{W}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.DRAGON);
        this.subtype.add(SubType.ROBOT);
        this.power = new MageInt(0);
        this.toughness = new MageInt(5);

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // Dragon Man's power is equal to the greatest mana value among noncreature permanents you control and noncreature cards in your graveyard.
        this.addAbility(new SimpleStaticAbility(
                Zone.ALL,
                new SetBasePowerSourceEffect(DragonManValue.instance)
                        .setText("{this}'s power is equal to the greatest mana value among noncreature permanents you control and noncreature cards in your graveyard")
        ));

        // You may cast this card from your graveyard by discarding a card in addition to paying its other costs.
        this.addAbility(new SimpleStaticAbility(Zone.ALL, new DragonManPlayFromGraveyardEffect()));
    }

    private DragonManReformedRobot(final DragonManReformedRobot card) {
        super(card);
    }

    @Override
    public DragonManReformedRobot copy() {
        return new DragonManReformedRobot(this);
    }
}

enum DragonManValue implements DynamicValue {
    instance;

    @Override
    public int calculate(Game game, Ability sourceAbility, Effect effect) {
        int max = 0;
        UUID controllerId = sourceAbility.getControllerId();
        for (Permanent permanent : game.getBattlefield().getAllActivePermanents(controllerId)) {
            if (!permanent.isCreature(game)) {
                max = Math.max(max, permanent.getManaValue());
            }
        }
        Player controller = game.getPlayer(controllerId);
        if (controller != null) {
            for (Card card : controller.getGraveyard().getCards(game)) {
                if (!card.isCreature(game)) {
                    max = Math.max(max, card.getManaValue());
                }
            }
        }
        return max;
    }

    @Override
    public DragonManValue copy() {
        return instance;
    }

    @Override
    public String getMessage() {
        return "greatest mana value among noncreature permanents you control and noncreature cards in your graveyard";
    }
}

class DragonManPlayFromGraveyardEffect extends AsThoughEffectImpl {

    DragonManPlayFromGraveyardEffect() {
        super(AsThoughEffectType.CAST_FROM_NOT_OWN_HAND_ZONE, Duration.EndOfGame, Outcome.Benefit);
        this.staticText = "You may cast this card from your graveyard by discarding a card in addition to paying its other costs";
    }

    private DragonManPlayFromGraveyardEffect(final DragonManPlayFromGraveyardEffect effect) {
        super(effect);
    }

    @Override
    public DragonManPlayFromGraveyardEffect copy() {
        return new DragonManPlayFromGraveyardEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        return true;
    }

    @Override
    public boolean applies(UUID objectId, Ability source, UUID affectedControllerId, Game game) {
        throw new IllegalArgumentException("Wrong code usage: can't call applies method on empty affectedAbility");
    }

    @Override
    public boolean applies(UUID objectId, Ability affectedAbility, Ability source, Game game, UUID playerId) {
        if (!objectId.equals(source.getSourceId())
                || !source.isControlledBy(playerId)
                || Zone.GRAVEYARD != game.getState().getZone(objectId)
                || !(affectedAbility instanceof SpellAbility)) {
            return false;
        }
        Player controller = game.getPlayer(playerId);
        if (controller == null) {
            return false;
        }
        SpellAbility spellAbility = (SpellAbility) affectedAbility;
        if (spellAbility.getManaCosts().isEmpty()) {
            return false;
        }
        Costs<Cost> costs = new CostsImpl<>();
        costs.add(new DiscardCardCost());
        controller.setCastSourceIdWithAlternateMana(
                objectId, spellAbility.getManaCosts(), costs, MageIdentifier.Default
        );
        return true;
    }
}
