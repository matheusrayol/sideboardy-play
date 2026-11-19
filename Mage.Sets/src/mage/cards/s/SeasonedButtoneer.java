package mage.cards.s;

import mage.MageInt;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.game.permanent.token.TokenImpl;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class SeasonedButtoneer extends CardImpl {

    public SeasonedButtoneer(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{U}");

        this.subtype.add(SubType.VEDALKEN);
        this.subtype.add(SubType.EMPLOYEE);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // When this creature enters, open an Attraction.
        this.addAbility(new EntersBattlefieldTriggeredAbility(
                new CreateTokenEffect(new SeasonedButtoneerAttractionToken())
                        .setText("open an Attraction. (Put the top card of your Attraction deck onto the battlefield.)")
        ));
    }

    private SeasonedButtoneer(final SeasonedButtoneer card) {
        super(card);
    }

    @Override
    public SeasonedButtoneer copy() {
        return new SeasonedButtoneer(this);
    }
}

class SeasonedButtoneerAttractionToken extends TokenImpl {

    SeasonedButtoneerAttractionToken() {
        super("Attraction", "Attraction artifact token");
        cardType.add(CardType.ARTIFACT);
        subtype.add(SubType.ATTRACTION);
    }

    private SeasonedButtoneerAttractionToken(final SeasonedButtoneerAttractionToken token) {
        super(token);
    }

    @Override
    public SeasonedButtoneerAttractionToken copy() {
        return new SeasonedButtoneerAttractionToken(this);
    }
}
