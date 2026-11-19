package mage.cards.g;

import java.util.UUID;
import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.keyword.MenaceAbility;
import mage.abilities.triggers.BeginningOfEndStepTriggeredAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.TargetController;
import mage.game.Game;
import mage.players.Player;
import mage.watchers.common.PlayerLostLifeWatcher;

/**
 *
 * @author muz
 */
public final class GulDukatColonialPrefect extends CardImpl {

    public GulDukatColonialPrefect(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{4}{B}{B}{B}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.CARDASSIAN);
        this.subtype.add(SubType.ROGUE);
        this.power = new MageInt(6);
        this.toughness = new MageInt(6);

        // Menace
        this.addAbility(new MenaceAbility());

        // At the beginning of each end step, each opponent loses life equal to the life that player lost this turn. You gain life equal to the life lost this way.
        this.addAbility(new BeginningOfEndStepTriggeredAbility(
                TargetController.ANY,
                new GulDukatColonialPrefectEffect(),
                false
        ));
    }

    private GulDukatColonialPrefect(final GulDukatColonialPrefect card) {
        super(card);
    }

    @Override
    public GulDukatColonialPrefect copy() {
        return new GulDukatColonialPrefect(this);
    }
}

class GulDukatColonialPrefectEffect extends OneShotEffect {

    GulDukatColonialPrefectEffect() {
        super(Outcome.LoseLife);
        staticText = "each opponent loses life equal to the life that player lost this turn. You gain life equal to the life lost this way";
    }

    private GulDukatColonialPrefectEffect(final GulDukatColonialPrefectEffect effect) {
        super(effect);
    }

    @Override
    public GulDukatColonialPrefectEffect copy() {
        return new GulDukatColonialPrefectEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        PlayerLostLifeWatcher watcher = game.getState().getWatcher(PlayerLostLifeWatcher.class);
        if (controller == null || watcher == null) {
            return false;
        }
        int totalLost = 0;
        for (UUID opponentId : game.getOpponents(controller.getId())) {
            Player opponent = game.getPlayer(opponentId);
            if (opponent != null) {
                int lifeLost = watcher.getLifeLost(opponentId);
                if (lifeLost > 0) {
                    totalLost += opponent.loseLife(lifeLost, game, source, false);
                }
            }
        }
        if (totalLost > 0) {
            controller.gainLife(totalLost, game, source);
        }
        return true;
    }
}
