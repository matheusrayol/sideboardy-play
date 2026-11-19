package mage.cards.d;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.Mode;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.DestroyTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.filter.FilterPermanent;
import mage.game.permanent.token.TokenImpl;
import mage.target.TargetPermanent;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class DraconianGateBot extends CardImpl {

    public DraconianGateBot(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{5}");

        this.subtype.add(SubType.ROBOT);
        this.power = new MageInt(4);
        this.toughness = new MageInt(2);

        // When this creature enters, choose one —
        // • Open an Attraction. (Put the top card of your Attraction deck onto the battlefield.)
        // • Destroy target Attraction. (It's put into its owner's junkyard.)
        Ability ability = new EntersBattlefieldTriggeredAbility(
                new CreateTokenEffect(new DraconianGateBotAttractionToken())
                        .setText("Open an Attraction. (Put the top card of your Attraction deck onto the battlefield.)")
        );
        Mode mode = new Mode(new DestroyTargetEffect());
        mode.addTarget(new TargetPermanent(new FilterPermanent(SubType.ATTRACTION, "Attraction")));
        ability.addMode(mode);
        this.addAbility(ability);
    }

    private DraconianGateBot(final DraconianGateBot card) {
        super(card);
    }

    @Override
    public DraconianGateBot copy() {
        return new DraconianGateBot(this);
    }
}

class DraconianGateBotAttractionToken extends TokenImpl {

    DraconianGateBotAttractionToken() {
        super("Attraction", "Attraction artifact token");
        cardType.add(CardType.ARTIFACT);
        subtype.add(SubType.ATTRACTION);
    }

    private DraconianGateBotAttractionToken(final DraconianGateBotAttractionToken token) {
        super(token);
    }

    @Override
    public DraconianGateBotAttractionToken copy() {
        return new DraconianGateBotAttractionToken(this);
    }
}
