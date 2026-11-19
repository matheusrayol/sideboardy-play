package mage.cards.m;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SpellCastControllerTriggeredAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.counters.Counter;
import mage.filter.StaticFilters;
import mage.filter.common.FilterControlledPermanent;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.game.permanent.token.TokenImpl;
import mage.players.Player;
import mage.target.common.TargetCardInYourGraveyard;
import mage.target.common.TargetControlledPermanent;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class MyraTheMagnificent extends CardImpl {

    public MyraTheMagnificent(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{U}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.PERFORMER);
        this.power = new MageInt(2);
        this.toughness = new MageInt(4);

        // Whenever you cast an instant or sorcery spell from your hand, open an Attraction.
        this.addAbility(new SpellCastControllerTriggeredAbility(
                new CreateTokenEffect(new MyraAttractionToken()).setText("open an Attraction"),
                StaticFilters.FILTER_SPELL_AN_INSTANT_OR_SORCERY,
                false
        ));

        // {X}, {T}: Exile target instant or sorcery card with mana value X from your graveyard and choose an Attraction you control that doesn't have a midway counter on it. Put a midway counter on it. For as long as that Attraction is on the battlefield, whenever you visit it, copy the exiled card. You may cast the copy without paying its mana cost.
        Ability ability = new SimpleActivatedAbility(new MyraExileEffect(), new ManaCostsImpl<>("{X}"));
        ability.addCost(new TapSourceCost());
        ability.addTarget(new TargetCardInYourGraveyard(StaticFilters.FILTER_CARD_INSTANT_OR_SORCERY));
        this.addAbility(ability);
    }

    private MyraTheMagnificent(final MyraTheMagnificent card) {
        super(card);
    }

    @Override
    public MyraTheMagnificent copy() {
        return new MyraTheMagnificent(this);
    }
}

class MyraExileEffect extends OneShotEffect {

    MyraExileEffect() {
        super(Outcome.Benefit);
        staticText = "Exile target instant or sorcery card with mana value X from your graveyard "
                + "and choose an Attraction you control that doesn't have a midway counter on it. "
                + "Put a midway counter on it. For as long as that Attraction is on the battlefield, "
                + "whenever you visit it, copy the exiled card. You may cast the copy without paying its mana cost";
    }

    private MyraExileEffect(final MyraExileEffect effect) {
        super(effect);
    }

    @Override
    public MyraExileEffect copy() {
        return new MyraExileEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Card card = game.getCard(getTargetPointer().getFirst(game, source));
        if (controller == null || card == null) {
            return false;
        }
        controller.moveCards(card, Zone.EXILED, source, game);
        FilterControlledPermanent filter = new FilterControlledPermanent(SubType.ATTRACTION, "Attraction you control");
        TargetControlledPermanent target = new TargetControlledPermanent(0, 1, filter, true);
        if (controller.choose(Outcome.Benefit, target, source, game)) {
            Permanent attraction = game.getPermanent(target.getFirstTarget());
            if (attraction != null && attraction.getCounters(game).getCount("midway") == 0) {
                attraction.addCounters(new Counter("midway"), source.getControllerId(), source, game);
            }
        }
        return true;
    }
}

class MyraAttractionToken extends TokenImpl {

    MyraAttractionToken() {
        super("Attraction", "Attraction artifact token");
        cardType.add(CardType.ARTIFACT);
        subtype.add(SubType.ATTRACTION);
    }

    private MyraAttractionToken(final MyraAttractionToken token) {
        super(token);
    }

    @Override
    public MyraAttractionToken copy() {
        return new MyraAttractionToken(this);
    }
}
