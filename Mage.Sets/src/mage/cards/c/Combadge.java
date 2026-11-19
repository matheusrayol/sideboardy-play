package mage.cards.c;

import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.CastSourceTriggeredAbility;
import mage.abilities.effects.common.CopySourceSpellEffect;
import mage.abilities.effects.common.combat.CantBeBlockedSourceEffect;
import mage.abilities.effects.common.continuous.GainAbilityAttachedEffect;
import mage.abilities.keyword.EquipAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class Combadge extends CardImpl {

    public Combadge(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{3}");

        this.subtype.add(SubType.COMMUNICATOR);
        this.subtype.add(SubType.EQUIPMENT);

        // When you cast this spell, copy it.
        this.addAbility(new CastSourceTriggeredAbility(new CopySourceSpellEffect()));

        // Equipped creature has "Whenever this creature attacks while you control another creature with a Communicator attached to it, this creature can't be blocked this turn."
        this.addAbility(new SimpleStaticAbility(new GainAbilityAttachedEffect(
                new CombadgeTriggeredAbility(),
                AttachmentType.EQUIPMENT
        )));

        // Equip {2}
        this.addAbility(new EquipAbility(2));
    }

    private Combadge(final Combadge card) {
        super(card);
    }

    @Override
    public Combadge copy() {
        return new Combadge(this);
    }
}

class CombadgeTriggeredAbility extends TriggeredAbilityImpl {

    CombadgeTriggeredAbility() {
        super(Zone.BATTLEFIELD, new CantBeBlockedSourceEffect(Duration.EndOfTurn), false);
        setTriggerPhrase("Whenever {this} attacks while you control another creature with a Communicator attached to it, ");
    }

    private CombadgeTriggeredAbility(final CombadgeTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public CombadgeTriggeredAbility copy() {
        return new CombadgeTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ATTACKER_DECLARED;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!event.getSourceId().equals(this.getSourceId())) {
            return false;
        }
        for (Permanent permanent : game.getBattlefield().getAllActivePermanents(
                StaticFilters.FILTER_PERMANENT_CREATURE, this.getControllerId(), game
        )) {
            if (permanent.getId().equals(this.getSourceId())) {
                continue;
            }
            for (UUID attachmentId : permanent.getAttachments()) {
                Permanent attachment = game.getPermanent(attachmentId);
                if (attachment != null && attachment.hasSubtype(SubType.COMMUNICATOR, game)) {
                    return true;
                }
            }
        }
        return false;
    }
}
