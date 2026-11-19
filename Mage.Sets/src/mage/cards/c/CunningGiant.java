package mage.cards.c;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.permanent.ControllerIdPredicate;
import mage.game.Game;
import mage.game.combat.CombatGroup;
import mage.game.events.DamageEvent;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.TargetPermanent;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class CunningGiant extends CardImpl {

    public CunningGiant(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{5}{R}");

        this.subtype.add(SubType.GIANT);
        this.power = new MageInt(4);
        this.toughness = new MageInt(4);

        // If Cunning Giant is unblocked, you may have it assign its combat damage to a creature defending player controls.
        this.addAbility(new SimpleStaticAbility(new CunningGiantEffect()));
    }

    private CunningGiant(final CunningGiant card) {
        super(card);
    }

    @Override
    public CunningGiant copy() {
        return new CunningGiant(this);
    }
}

class CunningGiantEffect extends ReplacementEffectImpl {

    CunningGiantEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Damage);
        staticText = "If {this} is unblocked, you may have it assign its combat damage to a creature defending player controls";
    }

    private CunningGiantEffect(final CunningGiantEffect effect) {
        super(effect);
    }

    @Override
    public CunningGiantEffect copy() {
        return new CunningGiantEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DAMAGE_PLAYER
                || event.getType() == GameEvent.EventType.DAMAGE_PERMANENT;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        DamageEvent damageEvent = (DamageEvent) event;
        if (!damageEvent.isCombatDamage() || !event.getSourceId().equals(source.getSourceId())) {
            return false;
        }
        CombatGroup group = game.getCombat().findGroup(source.getSourceId());
        return group != null && !group.getBlocked() && event.getTargetId().equals(group.getDefenderId());
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        Player controller = game.getPlayer(source.getControllerId());
        CombatGroup group = game.getCombat().findGroup(source.getSourceId());
        if (controller == null || group == null) {
            return false;
        }
        UUID defendingPlayerId = group.getDefendingPlayerId();
        FilterCreaturePermanent filter = new FilterCreaturePermanent("creature defending player controls");
        filter.add(new ControllerIdPredicate(defendingPlayerId));
        TargetPermanent target = new TargetPermanent(filter);
        target.withNotTarget(true);
        if (!target.canChoose(controller.getId(), source, game)) {
            return false;
        }
        if (!controller.chooseUse(Outcome.Damage, "Have Cunning Giant assign its combat damage to a creature defending player controls?", source, game)) {
            return false;
        }
        if (!controller.choose(Outcome.Damage, target, source, game)) {
            return false;
        }
        Permanent chosen = game.getPermanent(target.getFirstTarget());
        if (chosen == null) {
            return false;
        }
        DamageEvent damageEvent = (DamageEvent) event;
        chosen.damage(damageEvent.getAmount(), event.getSourceId(), source, game, true, damageEvent.isPreventable());
        return true;
    }
}
