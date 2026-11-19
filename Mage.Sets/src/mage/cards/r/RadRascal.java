package mage.cards.r;

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
public final class RadRascal extends CardImpl {

    public RadRascal(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{R}");

        this.subtype.add(SubType.DEVIL);
        this.subtype.add(SubType.EMPLOYEE);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // When this creature enters, open an Attraction.
        this.addAbility(new EntersBattlefieldTriggeredAbility(
                new CreateTokenEffect(new RadRascalAttractionToken())
                        .setText("open an Attraction. (Put the top card of your Attraction deck onto the battlefield.)")
        ));
    }

    private RadRascal(final RadRascal card) {
        super(card);
    }

    @Override
    public RadRascal copy() {
        return new RadRascal(this);
    }
}

class RadRascalAttractionToken extends TokenImpl {

    RadRascalAttractionToken() {
        super("Attraction", "Attraction artifact token");
        cardType.add(CardType.ARTIFACT);
        subtype.add(SubType.ATTRACTION);
    }

    private RadRascalAttractionToken(final RadRascalAttractionToken token) {
        super(token);
    }

    @Override
    public RadRascalAttractionToken copy() {
        return new RadRascalAttractionToken(this);
    }
}
