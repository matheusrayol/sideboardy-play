package mage.cards.l;

import mage.MageInt;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.DiesSourceTriggeredAbility;
import mage.abilities.common.EntersBattlefieldTappedAbility;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.ReturnSourceFromGraveyardToBattlefieldEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.RollDieType;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.DieRolledEvent;
import mage.game.events.GameEvent;
import mage.game.permanent.token.TokenImpl;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class LifetimePassHolder extends CardImpl {

    public LifetimePassHolder(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{B}");

        this.subtype.add(SubType.ZOMBIE);
        this.subtype.add(SubType.GUEST);
        this.power = new MageInt(2);
        this.toughness = new MageInt(1);

        // This creature enters tapped.
        this.addAbility(new EntersBattlefieldTappedAbility());

        // When this creature dies, open an Attraction.
        this.addAbility(new DiesSourceTriggeredAbility(
                new CreateTokenEffect(new LifetimePassHolderAttractionToken())
                        .setText("open an Attraction. (Put the top card of your Attraction deck onto the battlefield.)")
        ));

        // Whenever you roll to visit your Attractions, if you roll a 6, you may return this card from your graveyard to the battlefield.
        this.addAbility(new LifetimePassHolderTriggeredAbility());
    }

    private LifetimePassHolder(final LifetimePassHolder card) {
        super(card);
    }

    @Override
    public LifetimePassHolder copy() {
        return new LifetimePassHolder(this);
    }
}

class LifetimePassHolderTriggeredAbility extends TriggeredAbilityImpl {

    LifetimePassHolderTriggeredAbility() {
        super(Zone.GRAVEYARD, new ReturnSourceFromGraveyardToBattlefieldEffect(), true);
    }

    private LifetimePassHolderTriggeredAbility(final LifetimePassHolderTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public LifetimePassHolderTriggeredAbility copy() {
        return new LifetimePassHolderTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DIE_ROLLED;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!isControlledBy(event.getTargetId())) {
            return false;
        }
        DieRolledEvent drEvent = (DieRolledEvent) event;
        return drEvent.getRollDieType() == RollDieType.NUMERICAL && drEvent.getResult() == 6;
    }

    @Override
    public String getRule() {
        return "Whenever you roll to visit your Attractions, if you roll a 6, you may return {this} from your graveyard to the battlefield.";
    }
}

class LifetimePassHolderAttractionToken extends TokenImpl {

    LifetimePassHolderAttractionToken() {
        super("Attraction", "Attraction artifact token");
        cardType.add(CardType.ARTIFACT);
        subtype.add(SubType.ATTRACTION);
    }

    private LifetimePassHolderAttractionToken(final LifetimePassHolderAttractionToken token) {
        super(token);
    }

    @Override
    public LifetimePassHolderAttractionToken copy() {
        return new LifetimePassHolderAttractionToken(this);
    }
}
