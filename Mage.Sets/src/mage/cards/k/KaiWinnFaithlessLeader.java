package mage.cards.k;

import mage.MageInt;
import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.AsEntersBattlefieldAbility;
import mage.abilities.effects.common.ChooseACardNameEffect;
import mage.abilities.effects.common.LoseLifeTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.stack.Spell;
import mage.game.stack.StackObject;
import mage.target.targetpointer.FixedTarget;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author sideboardy-play
 */
public final class KaiWinnFaithlessLeader extends CardImpl {

    public KaiWinnFaithlessLeader(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{B}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.BAJORAN);
        this.subtype.add(SubType.CLERIC);
        this.power = new MageInt(3);
        this.toughness = new MageInt(1);

        // As Kai Winn enters, choose a nonland card name.
        this.addAbility(new AsEntersBattlefieldAbility(
                new ChooseACardNameEffect(ChooseACardNameEffect.TypeOfName.NON_LAND_NAME)
        ));

        // Whenever an opponent casts a spell with the chosen name or activates an ability of a source with the chosen name, that player loses 3 life.
        this.addAbility(new KaiWinnFaithlessLeaderTriggeredAbility());
    }

    private KaiWinnFaithlessLeader(final KaiWinnFaithlessLeader card) {
        super(card);
    }

    @Override
    public KaiWinnFaithlessLeader copy() {
        return new KaiWinnFaithlessLeader(this);
    }
}

class KaiWinnFaithlessLeaderTriggeredAbility extends TriggeredAbilityImpl {

    KaiWinnFaithlessLeaderTriggeredAbility() {
        super(Zone.BATTLEFIELD, new LoseLifeTargetEffect(3).setText("that player loses 3 life"), false);
    }

    private KaiWinnFaithlessLeaderTriggeredAbility(final KaiWinnFaithlessLeaderTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public KaiWinnFaithlessLeaderTriggeredAbility copy() {
        return new KaiWinnFaithlessLeaderTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.SPELL_CAST
                || event.getType() == GameEvent.EventType.ACTIVATED_ABILITY;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        UUID playerId = event.getPlayerId();
        if (!game.getOpponents(getControllerId()).contains(playerId)) {
            return false;
        }
        String chosenName = (String) game.getState().getValue(getSourceId().toString() + ChooseACardNameEffect.INFO_KEY);
        if (chosenName == null || chosenName.isEmpty()) {
            return false;
        }
        if (event.getType() == GameEvent.EventType.SPELL_CAST) {
            Spell spell = game.getStack().getSpell(event.getTargetId());
            if (spell == null) {
                MageObject lki = game.getLastKnownInformation(event.getTargetId(), Zone.STACK);
                if (lki instanceof Spell) {
                    spell = (Spell) lki;
                }
            }
            if (spell == null || !CardUtil.haveSameNames(spell, chosenName, game)) {
                return false;
            }
        } else {
            StackObject stackObject = game.getStack().getStackObject(event.getTargetId());
            Ability activatedAbility = stackObject != null
                    ? stackObject.getStackAbility()
                    : game.getAbility(event.getTargetId(), event.getSourceId()).orElse(null);
            if (activatedAbility == null) {
                return false;
            }
            MageObject sourceObject = game.getObject(activatedAbility.getSourceId());
            if (sourceObject == null) {
                sourceObject = game.getPermanentOrLKIBattlefield(event.getSourceId());
            }
            if (sourceObject == null || !CardUtil.haveSameNames(sourceObject, chosenName, game)) {
                return false;
            }
        }
        getEffects().setTargetPointer(new FixedTarget(playerId));
        return true;
    }

    @Override
    public String getRule() {
        return "Whenever an opponent casts a spell with the chosen name or activates an ability of a source with the chosen name, that player loses 3 life.";
    }
}
