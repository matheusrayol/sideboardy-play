package mage.cards.m;

import mage.MageInt;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.keyword.ProwessAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.events.DamagedEvent;
import mage.game.events.GameEvent;
import mage.game.permanent.token.RedElementalToken;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class MoltenLavamancer extends CardImpl {

    public MoltenLavamancer(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{R}");

        this.subtype.add(SubType.ELEMENTAL);
        this.subtype.add(SubType.WARLOCK);
        this.subtype.add(SubType.VILLAIN);
        this.power = new MageInt(2);
        this.toughness = new MageInt(3);

        // Prowess
        this.addAbility(new ProwessAbility());

        // Whenever a source you control deals noncombat damage to one or more of your opponents during your turn,
        // you create a 1/1 red Elemental creature token. This ability triggers only once each turn.
        this.addAbility(new MoltenLavamancerTriggeredAbility());
    }

    private MoltenLavamancer(final MoltenLavamancer card) {
        super(card);
    }

    @Override
    public MoltenLavamancer copy() {
        return new MoltenLavamancer(this);
    }
}

class MoltenLavamancerTriggeredAbility extends TriggeredAbilityImpl {

    MoltenLavamancerTriggeredAbility() {
        super(Zone.BATTLEFIELD, new CreateTokenEffect(new RedElementalToken()), false);
        this.setTriggersLimitEachTurn(1);
        setTriggerPhrase("Whenever a source you control deals noncombat damage to one or more of your opponents during your turn, ");
    }

    private MoltenLavamancerTriggeredAbility(final MoltenLavamancerTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public MoltenLavamancerTriggeredAbility copy() {
        return new MoltenLavamancerTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DAMAGED_PLAYER;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!game.isActivePlayer(getControllerId())) {
            return false;
        }
        if (((DamagedEvent) event).isCombatDamage()) {
            return false;
        }
        if (!game.getOpponents(getControllerId()).contains(event.getTargetId())) {
            return false;
        }
        return getControllerId().equals(game.getControllerId(event.getSourceId()));
    }
}
