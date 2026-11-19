package mage.cards.l;

import mage.MageInt;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.common.RollDiceEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class LineCutter extends CardImpl {

    public LineCutter(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{B}");

        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.ROGUE);
        this.subtype.add(SubType.GUEST);
        this.power = new MageInt(3);
        this.toughness = new MageInt(4);

        // When this creature enters, roll to visit your Attractions.
        this.addAbility(new EntersBattlefieldTriggeredAbility(
                new RollDiceEffect(6).setText("roll to visit your Attractions")
        ));
    }

    private LineCutter(final LineCutter card) {
        super(card);
    }

    @Override
    public LineCutter copy() {
        return new LineCutter(this);
    }
}
