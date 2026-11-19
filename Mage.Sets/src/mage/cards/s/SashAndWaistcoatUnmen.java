package mage.cards.s;

import mage.MageInt;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.ActivateAsSorceryActivatedAbility;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.common.InfoEffect;
import mage.abilities.effects.common.combat.CantBeBlockedTargetEffect;
import mage.abilities.effects.keyword.ManifestEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.target.targetpointer.FixedTarget;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class SashAndWaistcoatUnmen extends CardImpl {

    public SashAndWaistcoatUnmen(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.INSECT);
        this.subtype.add(SubType.ROGUE);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Whenever a face-down creature attacks one of your opponents, it can't be blocked this combat.
        this.addAbility(new SashAndWaistcoatUnmenTriggeredAbility());

        // {3}: Manifest the top card of your library. Any player may activate this ability but only as a sorcery.
        ActivateAsSorceryActivatedAbility ability = new ActivateAsSorceryActivatedAbility(
                Zone.BATTLEFIELD,
                new ManifestEffect(1),
                new GenericManaCost(3)
        );
        ability.setMayActivate(TargetController.ANY);
        ability.addEffect(new InfoEffect("Any player may activate this ability"));
        this.addAbility(ability);
    }

    private SashAndWaistcoatUnmen(final SashAndWaistcoatUnmen card) {
        super(card);
    }

    @Override
    public SashAndWaistcoatUnmen copy() {
        return new SashAndWaistcoatUnmen(this);
    }
}

class SashAndWaistcoatUnmenTriggeredAbility extends TriggeredAbilityImpl {

    SashAndWaistcoatUnmenTriggeredAbility() {
        super(Zone.BATTLEFIELD, new CantBeBlockedTargetEffect(Duration.EndOfCombat).setText("it can't be blocked this combat"), false);
        setTriggerPhrase("Whenever a face-down creature attacks one of your opponents, ");
    }

    private SashAndWaistcoatUnmenTriggeredAbility(final SashAndWaistcoatUnmenTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public SashAndWaistcoatUnmenTriggeredAbility copy() {
        return new SashAndWaistcoatUnmenTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ATTACKER_DECLARED;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        Permanent attacker = game.getPermanent(event.getSourceId());
        if (attacker == null || !attacker.isFaceDown(game)) {
            return false;
        }
        if (!game.getOpponents(getControllerId()).contains(event.getTargetId())) {
            return false;
        }
        getEffects().setTargetPointer(new FixedTarget(attacker, game));
        return true;
    }
}
