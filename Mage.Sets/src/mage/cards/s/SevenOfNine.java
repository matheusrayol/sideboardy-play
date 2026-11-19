package mage.cards.s;

import mage.MageInt;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.m.MelkotianAdjudicator;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.GameEvent;

import java.util.UUID;

/**
 * @author sideboardy-play
 */
public final class SevenOfNine extends CardImpl {

    public SevenOfNine(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{2}{U}{B}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.SCIENTIST);
        this.power = new MageInt(2);
        this.toughness = new MageInt(4);

        // Whenever you face a dilemma, draw a card.
        this.addAbility(new SevenOfNineTriggeredAbility());
    }

    private SevenOfNine(final SevenOfNine card) {
        super(card);
    }

    @Override
    public SevenOfNine copy() {
        return new SevenOfNine(this);
    }
}

class SevenOfNineTriggeredAbility extends TriggeredAbilityImpl {

    SevenOfNineTriggeredAbility() {
        super(Zone.BATTLEFIELD, new DrawCardSourceControllerEffect(1), false);
        setTriggerPhrase("Whenever you face a dilemma, ");
    }

    private SevenOfNineTriggeredAbility(final SevenOfNineTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public SevenOfNineTriggeredAbility copy() {
        return new SevenOfNineTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.SPELL_CAST
                || event.getType() == GameEvent.EventType.ACTIVATED_ABILITY
                || event.getType() == GameEvent.EventType.TRIGGERED_ABILITY;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        return MelkotianAdjudicator.isDilemmaEvent(event, getControllerId(), game);
    }
}
