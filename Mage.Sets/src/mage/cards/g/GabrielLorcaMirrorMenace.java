package mage.cards.g;

import mage.MageInt;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.DoIfCostPaid;
import mage.abilities.effects.common.ReturnSourceFromGraveyardToHandEffect;
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
public final class GabrielLorcaMirrorMenace extends CardImpl {

    public GabrielLorcaMirrorMenace(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{4}{B}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.ROGUE);
        this.subtype.add(SubType.OFFICER);
        this.power = new MageInt(5);
        this.toughness = new MageInt(4);

        // Menace
        this.addAbility(new MenaceAbility(false));

        // Whenever you face a dilemma, you may pay {B}. If you do, return this card from your graveyard to your hand.
        this.addAbility(new GabrielLorcaTriggeredAbility());
    }

    private GabrielLorcaMirrorMenace(final GabrielLorcaMirrorMenace card) {
        super(card);
    }

    @Override
    public GabrielLorcaMirrorMenace copy() {
        return new GabrielLorcaMirrorMenace(this);
    }
}

class GabrielLorcaTriggeredAbility extends TriggeredAbilityImpl {

    GabrielLorcaTriggeredAbility() {
        super(
                Zone.GRAVEYARD,
                new DoIfCostPaid(new ReturnSourceFromGraveyardToHandEffect(), new ManaCostsImpl<>("{B}")),
                false
        );
        setTriggerPhrase("Whenever you face a dilemma, ");
    }

    private GabrielLorcaTriggeredAbility(final GabrielLorcaTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public GabrielLorcaTriggeredAbility copy() {
        return new GabrielLorcaTriggeredAbility(this);
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
