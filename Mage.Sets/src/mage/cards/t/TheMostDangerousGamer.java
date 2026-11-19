package mage.cards.t;

import mage.MageInt;
import mage.abilities.common.EntersBattlefieldControlledTriggeredAbility;
import mage.abilities.common.EntersBattlefieldOrAttacksSourceTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.InfoEffect;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.abilities.keyword.DeathtouchAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.filter.FilterPermanent;
import mage.game.permanent.token.TokenImpl;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class TheMostDangerousGamer extends CardImpl {

    public TheMostDangerousGamer(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{B}{G}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.GAMER);
        this.subtype.add(SubType.GUEST);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Deathtouch
        this.addAbility(DeathtouchAbility.getInstance());

        // Whenever The Most Dangerous Gamer enters or attacks, open an Attraction.
        this.addAbility(new EntersBattlefieldOrAttacksSourceTriggeredAbility(
                new CreateTokenEffect(new GamerAttractionToken()).setText("open an Attraction")
        ));

        // Whenever you open an Attraction, put a +1/+1 counter on The Most Dangerous Gamer.
        this.addAbility(new EntersBattlefieldControlledTriggeredAbility(
                Zone.BATTLEFIELD,
                new AddCountersSourceEffect(CounterType.P1P1.createInstance()),
                new FilterPermanent(SubType.ATTRACTION, "an Attraction"),
                false
        ).setTriggerPhrase("Whenever you open an Attraction, "));

        // Whenever you claim the prize of an Attraction, destroy target permanent.
        this.addAbility(new SimpleStaticAbility(new InfoEffect(
                "Whenever you claim the prize of an Attraction, destroy target permanent"
        )));
    }

    private TheMostDangerousGamer(final TheMostDangerousGamer card) {
        super(card);
    }

    @Override
    public TheMostDangerousGamer copy() {
        return new TheMostDangerousGamer(this);
    }
}

class GamerAttractionToken extends TokenImpl {

    GamerAttractionToken() {
        super("Attraction", "Attraction artifact token");
        cardType.add(CardType.ARTIFACT);
        subtype.add(SubType.ATTRACTION);
    }

    private GamerAttractionToken(final GamerAttractionToken token) {
        super(token);
    }

    @Override
    public GamerAttractionToken copy() {
        return new GamerAttractionToken(this);
    }
}
