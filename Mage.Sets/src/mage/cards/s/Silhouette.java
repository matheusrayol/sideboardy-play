package mage.cards.s;

import mage.abilities.Ability;
import mage.abilities.effects.PreventionEffectImpl;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.stack.StackObject;
import mage.target.common.TargetCreaturePermanent;
import mage.util.CardUtil;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class Silhouette extends CardImpl {

    public Silhouette(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{1}{U}");

        // Choose target creature. If a spell or ability that targets that creature would cause a source to deal damage to that creature this turn, prevent that damage.
        this.getSpellAbility().addEffect(new SilhouettePreventionEffect());
        this.getSpellAbility().addTarget(new TargetCreaturePermanent());
    }

    private Silhouette(final Silhouette card) {
        super(card);
    }

    @Override
    public Silhouette copy() {
        return new Silhouette(this);
    }
}

class SilhouettePreventionEffect extends PreventionEffectImpl {

    SilhouettePreventionEffect() {
        super(Duration.EndOfTurn, Integer.MAX_VALUE, false, false);
        staticText = "Choose target creature. If a spell or ability that targets that creature would cause a source to deal damage to that creature this turn, prevent that damage.";
    }

    private SilhouettePreventionEffect(final SilhouettePreventionEffect effect) {
        super(effect);
    }

    @Override
    public SilhouettePreventionEffect copy() {
        return new SilhouettePreventionEffect(this);
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        if (!super.applies(event, source, game) || event.getType() != GameEvent.EventType.DAMAGE_PERMANENT) {
            return false;
        }
        UUID targetId = getTargetPointer().getFirst(game, source);
        if (targetId == null || !targetId.equals(event.getTargetId())) {
            return false;
        }
        StackObject top = game.getStack().getFirstOrNull();
        return top != null && CardUtil.getAllSelectedTargets(top.getStackAbility(), game).contains(targetId);
    }
}
