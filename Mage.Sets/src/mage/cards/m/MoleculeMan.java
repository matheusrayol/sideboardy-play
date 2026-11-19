package mage.cards.m;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.keyword.MiracleAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.players.Player;
import mage.watchers.common.MiracleWatcher;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author sideboardy-play
 */
public final class MoleculeMan extends CardImpl {

    public MoleculeMan(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{6}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.VILLAIN);
        this.power = new MageInt(5);
        this.toughness = new MageInt(5);

        // Nonland cards in your hand have miracle {0}.
        this.addAbility(new SimpleStaticAbility(new MoleculeManEffect()), new MiracleWatcher());
    }

    private MoleculeMan(final MoleculeMan card) {
        super(card);
    }

    @Override
    public MoleculeMan copy() {
        return new MoleculeMan(this);
    }
}

class MoleculeManEffect extends ContinuousEffectImpl {

    private final Map<UUID, MiracleAbility> grantedAbilities = new HashMap<>();

    MoleculeManEffect() {
        super(Duration.WhileOnBattlefield, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        this.staticText = "Nonland cards in your hand have miracle {0}. "
                + "<i>(You may cast a card for its miracle cost when you draw it if it's the first card you drew this turn.)</i>";
    }

    private MoleculeManEffect(final MoleculeManEffect effect) {
        super(effect);
        for (Map.Entry<UUID, MiracleAbility> entry : effect.grantedAbilities.entrySet()) {
            this.grantedAbilities.put(entry.getKey(), entry.getValue().copy());
        }
    }

    @Override
    public MoleculeManEffect copy() {
        return new MoleculeManEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        for (Card card : controller.getHand().getCards(StaticFilters.FILTER_CARD_NON_LAND, game)) {
            MiracleAbility miracle = grantedAbilities.computeIfAbsent(card.getId(), id -> new MiracleAbility("{0}"));
            game.getState().addOtherAbility(card, miracle, false);
        }
        return true;
    }
}
