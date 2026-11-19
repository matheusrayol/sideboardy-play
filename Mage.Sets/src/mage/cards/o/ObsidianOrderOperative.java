package mage.cards.o;

import mage.MageInt;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.common.GainLifeEffect;
import mage.abilities.effects.common.LoseLifeOpponentsEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.m.MelkotianAdjudicator;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.GameEvent;

import java.util.UUID;

/**
 * @author sideboardy-play
 */
public final class ObsidianOrderOperative extends CardImpl {

    public ObsidianOrderOperative(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{B}");

        this.subtype.add(SubType.CARDASSIAN);
        this.subtype.add(SubType.OFFICER);
        this.subtype.add(SubType.SPY);
        this.power = new MageInt(2);
        this.toughness = new MageInt(5);

        // Whenever you face a dilemma, each opponent loses 1 life and you gain 1 life.
        this.addAbility(new ObsidianOrderOperativeTriggeredAbility());
    }

    private ObsidianOrderOperative(final ObsidianOrderOperative card) {
        super(card);
    }

    @Override
    public ObsidianOrderOperative copy() {
        return new ObsidianOrderOperative(this);
    }
}

class ObsidianOrderOperativeTriggeredAbility extends TriggeredAbilityImpl {

    ObsidianOrderOperativeTriggeredAbility() {
        super(Zone.BATTLEFIELD, new LoseLifeOpponentsEffect(1), false);
        this.addEffect(new GainLifeEffect(1).concatBy("and"));
        setTriggerPhrase("Whenever you face a dilemma, ");
    }

    private ObsidianOrderOperativeTriggeredAbility(final ObsidianOrderOperativeTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public ObsidianOrderOperativeTriggeredAbility copy() {
        return new ObsidianOrderOperativeTriggeredAbility(this);
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
