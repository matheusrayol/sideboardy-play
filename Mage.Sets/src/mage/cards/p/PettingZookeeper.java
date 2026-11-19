package mage.cards.p;

import mage.MageInt;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.keyword.ReachAbility;
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
public final class PettingZookeeper extends CardImpl {

    public PettingZookeeper(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{G}");

        this.subtype.add(SubType.ELF);
        this.subtype.add(SubType.EMPLOYEE);
        this.power = new MageInt(0);
        this.toughness = new MageInt(4);

        // Reach
        this.addAbility(ReachAbility.getInstance());

        // When this creature enters, open an Attraction.
        this.addAbility(new EntersBattlefieldTriggeredAbility(
                new CreateTokenEffect(new PettingZookeeperAttractionToken())
                        .setText("open an Attraction. (Put the top card of your Attraction deck onto the battlefield.)")
        ));
    }

    private PettingZookeeper(final PettingZookeeper card) {
        super(card);
    }

    @Override
    public PettingZookeeper copy() {
        return new PettingZookeeper(this);
    }
}

class PettingZookeeperAttractionToken extends TokenImpl {

    PettingZookeeperAttractionToken() {
        super("Attraction", "Attraction artifact token");
        cardType.add(CardType.ARTIFACT);
        subtype.add(SubType.ATTRACTION);
    }

    private PettingZookeeperAttractionToken(final PettingZookeeperAttractionToken token) {
        super(token);
    }

    @Override
    public PettingZookeeperAttractionToken copy() {
        return new PettingZookeeperAttractionToken(this);
    }
}
