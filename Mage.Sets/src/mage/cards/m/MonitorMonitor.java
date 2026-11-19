package mage.cards.m;

import mage.MageInt;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.InfoEffect;
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
public final class MonitorMonitor extends CardImpl {

    public MonitorMonitor(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{U}{U}");

        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.EMPLOYEE);
        this.power = new MageInt(2);
        this.toughness = new MageInt(5);

        // When this creature enters, open an Attraction.
        this.addAbility(new EntersBattlefieldTriggeredAbility(
                new CreateTokenEffect(new MonitorMonitorAttractionToken())
                        .setText("open an Attraction. (Put the top card of your Attraction deck onto the battlefield.)")
        ));

        // Once each turn, you may pay {1} to reroll one or more dice you rolled.
        this.addAbility(new SimpleStaticAbility(new InfoEffect(
                "Once each turn, you may pay {1} to reroll one or more dice you rolled"
        )));
    }

    private MonitorMonitor(final MonitorMonitor card) {
        super(card);
    }

    @Override
    public MonitorMonitor copy() {
        return new MonitorMonitor(this);
    }
}

class MonitorMonitorAttractionToken extends TokenImpl {

    MonitorMonitorAttractionToken() {
        super("Attraction", "Attraction artifact token");
        cardType.add(CardType.ARTIFACT);
        subtype.add(SubType.ATTRACTION);
    }

    private MonitorMonitorAttractionToken(final MonitorMonitorAttractionToken token) {
        super(token);
    }

    @Override
    public MonitorMonitorAttractionToken copy() {
        return new MonitorMonitorAttractionToken(this);
    }
}
