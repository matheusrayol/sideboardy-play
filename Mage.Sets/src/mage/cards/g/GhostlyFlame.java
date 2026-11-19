package mage.cards.g;

import mage.MageObject;
import mage.ObjectColor;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.game.stack.Spell;
import mage.players.Player;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class GhostlyFlame extends CardImpl {

    public GhostlyFlame(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{B}{R}");

        // Black and/or red permanents and spells are colorless sources of damage.
        this.addAbility(new SimpleStaticAbility(new GhostlyFlameEffect()));
    }

    private GhostlyFlame(final GhostlyFlame card) {
        super(card);
    }

    @Override
    public GhostlyFlame copy() {
        return new GhostlyFlame(this);
    }
}

class GhostlyFlameEffect extends ReplacementEffectImpl {

    GhostlyFlameEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Neutral);
        this.staticText = "black and/or red permanents and spells are colorless sources of damage";
    }

    private GhostlyFlameEffect(final GhostlyFlameEffect effect) {
        super(effect);
    }

    @Override
    public GhostlyFlameEffect copy() {
        return new GhostlyFlameEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.PREVENT_DAMAGE;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        MageObject damageSource = findPermanentOrSpellSource(event.getSourceId(), game);
        if (damageSource == null) {
            return false;
        }
        ObjectColor srcColor = damageSource.getColor(game);
        if (!srcColor.isBlack() && !srcColor.isRed()) {
            return false;
        }
        MageObject colorlessCopy = damageSource.copy();
        colorlessCopy.getColor(game).setColor(ObjectColor.COLORLESS);

        Permanent permTarget = game.getPermanent(event.getTargetId());
        if (permTarget != null
                && permTarget.hasProtectionFrom(damageSource, game)
                && !permTarget.hasProtectionFrom(colorlessCopy, game)) {
            return true;
        }
        Player playerTarget = game.getPlayer(event.getTargetId());
        return playerTarget != null
                && playerTarget.hasProtectionFrom(damageSource, game)
                && !playerTarget.hasProtectionFrom(colorlessCopy, game);
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        // Color-based protection does not prevent damage from this source
        return true;
    }

    private static MageObject findPermanentOrSpellSource(UUID sourceId, Game game) {
        if (sourceId == null) {
            return null;
        }
        Permanent perm = game.getPermanentOrLKIBattlefield(sourceId);
        if (perm != null) {
            return perm;
        }
        Spell spell = game.getSpellOrLKIStack(sourceId);
        if (spell != null) {
            return spell;
        }
        MageObject obj = game.getObject(sourceId);
        if (obj instanceof Permanent || obj instanceof Spell) {
            return obj;
        }
        return null;
    }
}
