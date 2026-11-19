package mage.cards.a;

import mage.abilities.Ability;
import mage.abilities.common.ActivateIfConditionActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.IsStepCondition;
import mage.abilities.costs.common.SacrificeSourceCost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.InfoEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.game.Game;
import mage.players.Player;
import mage.target.common.TargetOpponent;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class AmuletOfQuoz extends CardImpl {

    public AmuletOfQuoz(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{6}");

        // Remove this card from your deck before playing if you're not playing for ante.
        this.addAbility(new SimpleStaticAbility(
                Zone.ALL,
                new InfoEffect("Remove this card from your deck before playing if you're not playing for ante")
        ));

        // {T}, Sacrifice this artifact: Target opponent may ante the top card of their library. If they don't, you flip a coin. If you win the flip, that player loses the game. If you lose the flip, you lose the game. Activate only during your upkeep.
        Ability ability = new ActivateIfConditionActivatedAbility(
                new AmuletOfQuozEffect(),
                new TapSourceCost(),
                IsStepCondition.getMyUpkeep()
        );
        ability.addCost(new SacrificeSourceCost());
        ability.addTarget(new TargetOpponent());
        this.addAbility(ability);
    }

    private AmuletOfQuoz(final AmuletOfQuoz card) {
        super(card);
    }

    @Override
    public AmuletOfQuoz copy() {
        return new AmuletOfQuoz(this);
    }
}

class AmuletOfQuozEffect extends OneShotEffect {

    private static final UUID ANTE_EXILE_ID = UUID.fromString("00000000-0000-0000-0000-00000000a17e");

    AmuletOfQuozEffect() {
        super(Outcome.Detriment);
        this.staticText = "target opponent may ante the top card of their library. If they don't, you flip a coin. "
                + "If you win the flip, that player loses the game. If you lose the flip, you lose the game";
    }

    private AmuletOfQuozEffect(final AmuletOfQuozEffect effect) {
        super(effect);
    }

    @Override
    public AmuletOfQuozEffect copy() {
        return new AmuletOfQuozEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Player opponent = game.getPlayer(getTargetPointer().getFirst(game, source));
        if (controller == null || opponent == null) {
            return false;
        }
        Card topCard = opponent.getLibrary().getFromTop(game);
        if (topCard != null && opponent.chooseUse(
                Outcome.Detriment,
                "Ante the top card of your library? (If you don't, a coin flip decides who loses the game)",
                source,
                game
        )) {
            opponent.moveCardsToExile(topCard, source, game, true, ANTE_EXILE_ID, "Ante");
            return true;
        }
        if (controller.flipCoin(source, game, true)) {
            opponent.lost(game);
        } else {
            controller.lost(game);
        }
        return true;
    }
}
