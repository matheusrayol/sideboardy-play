package mage.cards.l;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.dynamicvalue.common.DifferentlyNamedPermanentCount;
import mage.abilities.effects.OneShotEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.players.Player;
import mage.target.common.TargetOpponent;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class LieutenantNyotaUhura extends CardImpl {

    static final DifferentlyNamedPermanentCount landCount =
            new DifferentlyNamedPermanentCount(StaticFilters.FILTER_CONTROLLED_PERMANENT_LANDS);

    public LieutenantNyotaUhura(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.OFFICER);
        this.power = new MageInt(2);
        this.toughness = new MageInt(3);

        // Strange new worlds — {4}, {T}: Target opponent exiles the top X cards of their library, where X is the number of differently named lands you control.
        // You may cast an instant or sorcery spell from among them without paying its mana cost.
        // Then that player puts the rest on the bottom of their library in a random order.
        Ability ability = new SimpleActivatedAbility(new LieutenantNyotaUhuraEffect(), new GenericManaCost(4));
        ability.addCost(new TapSourceCost());
        ability.addTarget(new TargetOpponent());
        ability.setAbilityWord(AbilityWord.STRANGE_NEW_WORLDS);
        ability.addHint(landCount.getHint());
        this.addAbility(ability);
    }

    private LieutenantNyotaUhura(final LieutenantNyotaUhura card) {
        super(card);
    }

    @Override
    public LieutenantNyotaUhura copy() {
        return new LieutenantNyotaUhura(this);
    }
}

class LieutenantNyotaUhuraEffect extends OneShotEffect {

    LieutenantNyotaUhuraEffect() {
        super(Outcome.PlayForFree);
        this.staticText = "target opponent exiles the top X cards of their library, where X is the number of differently named lands you control. "
                + "You may cast an instant or sorcery spell from among them without paying its mana cost. "
                + "Then that player puts the rest on the bottom of their library in a random order";
    }

    private LieutenantNyotaUhuraEffect(final LieutenantNyotaUhuraEffect effect) {
        super(effect);
    }

    @Override
    public LieutenantNyotaUhuraEffect copy() {
        return new LieutenantNyotaUhuraEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Player opponent = game.getPlayer(getTargetPointer().getFirst(game, source));
        if (controller == null || opponent == null) {
            return false;
        }
        int x = LieutenantNyotaUhura.landCount.calculate(game, source, this);
        if (x <= 0) {
            return true;
        }
        Cards cards = new CardsImpl(opponent.getLibrary().getTopCards(game, x));
        if (cards.isEmpty()) {
            return true;
        }
        opponent.moveCards(cards, Zone.EXILED, source, game);
        cards.retainZone(Zone.EXILED, game);
        CardUtil.castSpellWithAttributesForFree(
                controller, source, game, cards, StaticFilters.FILTER_CARD_INSTANT_OR_SORCERY
        );
        cards.retainZone(Zone.EXILED, game);
        if (!cards.isEmpty()) {
            opponent.putCardsOnBottomOfLibrary(cards, game, source, false);
        }
        return true;
    }
}
