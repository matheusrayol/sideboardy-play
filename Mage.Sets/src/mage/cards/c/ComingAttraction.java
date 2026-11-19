package mage.cards.c;

import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.search.SearchLibraryPutInPlayEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.filter.StaticFilters;
import mage.game.permanent.token.TokenImpl;
import mage.target.common.TargetCardInLibrary;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class ComingAttraction extends CardImpl {

    public ComingAttraction(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{2}{G}");

        // Search your library for a basic land card, put it onto the battlefield tapped, then shuffle. Open an Attraction.
        this.getSpellAbility().addEffect(new SearchLibraryPutInPlayEffect(
                new TargetCardInLibrary(StaticFilters.FILTER_CARD_BASIC_LAND), true
        ));
        this.getSpellAbility().addEffect(
                new CreateTokenEffect(new ComingAttractionToken())
                        .setText("Open an Attraction. (Put the top card of your Attraction deck onto the battlefield.)")
        );
    }

    private ComingAttraction(final ComingAttraction card) {
        super(card);
    }

    @Override
    public ComingAttraction copy() {
        return new ComingAttraction(this);
    }
}

class ComingAttractionToken extends TokenImpl {

    ComingAttractionToken() {
        super("Attraction", "Attraction artifact token");
        cardType.add(CardType.ARTIFACT);
        subtype.add(SubType.ATTRACTION);
    }

    private ComingAttractionToken(final ComingAttractionToken token) {
        super(token);
    }

    @Override
    public ComingAttractionToken copy() {
        return new ComingAttractionToken(this);
    }
}
