package mage.cards.d;

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
public final class DeadbeatAttendant extends CardImpl {

    public DeadbeatAttendant(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{B}");

        this.subtype.add(SubType.VAMPIRE);
        this.subtype.add(SubType.EMPLOYEE);
        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // When this creature enters, open an Attraction.
        this.addAbility(new EntersBattlefieldTriggeredAbility(
                new CreateTokenEffect(new DeadbeatAttendantAttractionToken())
                        .setText("open an Attraction. (Put the top card of your Attraction deck onto the battlefield.)")
        ));
    }

    private DeadbeatAttendant(final DeadbeatAttendant card) {
        super(card);
    }

    @Override
    public DeadbeatAttendant copy() {
        return new DeadbeatAttendant(this);
    }
}

class DeadbeatAttendantAttractionToken extends TokenImpl {

    DeadbeatAttendantAttractionToken() {
        super("Attraction", "Attraction artifact token");
        cardType.add(CardType.ARTIFACT);
        subtype.add(SubType.ATTRACTION);
    }

    private DeadbeatAttendantAttractionToken(final DeadbeatAttendantAttractionToken token) {
        super(token);
    }

    @Override
    public DeadbeatAttendantAttractionToken copy() {
        return new DeadbeatAttendantAttractionToken(this);
    }
}
