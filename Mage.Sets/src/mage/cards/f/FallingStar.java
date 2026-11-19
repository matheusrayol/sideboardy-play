package mage.cards.f;

import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetCreaturePermanent;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * Adapted from ChaosOrb without physical dexterity mechanics: chooses a creature
 * and succeeds on a d20 roll of 4 or greater.
 *
 * @author sideboardy-play
 */
public final class FallingStar extends CardImpl {

    public FallingStar(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{2}{R}");

        // Flip Falling Star onto the playing area from a height of at least one foot. Falling Star deals 3 damage to each creature it lands on. Tap all creatures dealt damage by Falling Star. If Falling Star doesn't turn completely over at least once during the flip, it has no effect.
        this.getSpellAbility().addEffect(new FallingStarEffect());
    }

    private FallingStar(final FallingStar card) {
        super(card);
    }

    @Override
    public FallingStar copy() {
        return new FallingStar(this);
    }
}

class FallingStarEffect extends OneShotEffect {

    FallingStarEffect() {
        super(Outcome.Damage);
        staticText = "Choose a creature, then a number between 1 and 20 is generated at random. "
                + "If the number is 4 or more, {this} deals 3 damage to that creature and tap it if it was dealt damage this way.";
    }

    private FallingStarEffect(final FallingStarEffect effect) {
        super(effect);
    }

    @Override
    public FallingStarEffect copy() {
        return new FallingStarEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(source.getControllerId());
        if (player == null) {
            return false;
        }
        TargetCreaturePermanent target = new TargetCreaturePermanent();
        target.withNotTarget(true);
        Permanent chosen = null;
        if (target.canChoose(player.getId(), source, game)
                && target.choose(outcome, player.getId(), source.getSourceId(), source, game)) {
            chosen = game.getPermanent(target.getFirstTarget());
        }
        if (chosen != null) {
            game.informPlayers(player.getLogName() + " has chosen " + chosen.getLogName() + CardUtil.getSourceLogName(game, source));
            int result = player.rollDieResult(20, game);
            game.informPlayers("The random number is " + result + CardUtil.getSourceLogName(game, source));
            if (result >= 4) {
                if (chosen.damage(3, source.getSourceId(), source, game) > 0) {
                    chosen.tap(source, game);
                }
            }
        }
        return true;
    }
}
