package mage.cards.e;

import mage.MageInt;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.common.GainLifeEffect;
import mage.abilities.effects.common.LoseLifeOpponentsEffect;
import mage.abilities.keyword.MenaceAbility;
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
public final class ElimGarakSimpleTailor extends CardImpl {

    public ElimGarakSimpleTailor(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{B}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.CARDASSIAN);
        this.subtype.add(SubType.ROGUE);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Menace
        this.addAbility(new MenaceAbility(false));

        // Whenever you face a dilemma, each opponent loses 2 life and you gain 2 life.
        this.addAbility(new ElimGarakTriggeredAbility());
    }

    private ElimGarakSimpleTailor(final ElimGarakSimpleTailor card) {
        super(card);
    }

    @Override
    public ElimGarakSimpleTailor copy() {
        return new ElimGarakSimpleTailor(this);
    }
}

class ElimGarakTriggeredAbility extends TriggeredAbilityImpl {

    ElimGarakTriggeredAbility() {
        super(Zone.BATTLEFIELD, new LoseLifeOpponentsEffect(2), false);
        this.addEffect(new GainLifeEffect(2).concatBy("and"));
        setTriggerPhrase("Whenever you face a dilemma, ");
    }

    private ElimGarakTriggeredAbility(final ElimGarakTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public ElimGarakTriggeredAbility copy() {
        return new ElimGarakTriggeredAbility(this);
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
