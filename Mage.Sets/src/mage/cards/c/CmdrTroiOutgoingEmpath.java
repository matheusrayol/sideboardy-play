package mage.cards.c;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.keyword.VigilanceAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.CardsImpl;
import mage.constants.*;
import mage.filter.FilterPlayer;
import mage.filter.StaticFilters;
import mage.filter.predicate.Predicate;
import mage.game.Game;
import mage.game.stack.Spell;
import mage.game.stack.StackObject;
import mage.players.Player;
import mage.target.TargetPlayer;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class CmdrTroiOutgoingEmpath extends CardImpl {

    private static final FilterPlayer filter = new FilterPlayer("opponent whose turn it is");

    static {
        filter.add(TargetController.OPPONENT.getPlayerPredicate());
        filter.add(CmdrTroiActivePlayerPredicate.instance);
    }

    public CmdrTroiOutgoingEmpath(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{U}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.BETAZOID);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.ADVISOR);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Vigilance
        this.addAbility(VigilanceAbility.getInstance());

        // {T}: Target opponent whose turn it is may cast a spell from their hand without paying its mana cost.
        // If they do, you copy that spell. You may choose new targets for the copy. If they don't, you draw a card.
        Ability ability = new SimpleActivatedAbility(new CmdrTroiEffect(), new TapSourceCost());
        ability.addTarget(new TargetPlayer(1, 1, false, filter));
        this.addAbility(ability);
    }

    private CmdrTroiOutgoingEmpath(final CmdrTroiOutgoingEmpath card) {
        super(card);
    }

    @Override
    public CmdrTroiOutgoingEmpath copy() {
        return new CmdrTroiOutgoingEmpath(this);
    }
}

enum CmdrTroiActivePlayerPredicate implements Predicate<Player> {
    instance;

    @Override
    public boolean apply(Player input, Game game) {
        return game.isActivePlayer(input.getId());
    }

    @Override
    public String toString() {
        return "whose turn it is";
    }
}

class CmdrTroiEffect extends OneShotEffect {

    CmdrTroiEffect() {
        super(Outcome.Benefit);
        this.staticText = "target opponent whose turn it is may cast a spell from their hand without paying its mana cost. "
                + "If they do, you copy that spell. You may choose new targets for the copy. If they don't, you draw a card";
    }

    private CmdrTroiEffect(final CmdrTroiEffect effect) {
        super(effect);
    }

    @Override
    public CmdrTroiEffect copy() {
        return new CmdrTroiEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Player opponent = game.getPlayer(getTargetPointer().getFirst(game, source));
        if (controller == null || opponent == null) {
            return false;
        }
        StackObject previousTop = game.getStack().peek();
        boolean cast = CardUtil.castSpellWithAttributesForFree(
                opponent, source, game, new CardsImpl(opponent.getHand()), StaticFilters.FILTER_CARD_NON_LAND
        );
        if (cast) {
            StackObject currentTop = game.getStack().peek();
            if (currentTop != null && currentTop != previousTop && currentTop instanceof Spell) {
                ((Spell) currentTop).createCopyOnStack(game, source, controller.getId(), true);
            }
        } else {
            controller.drawCards(1, source, game);
        }
        return true;
    }
}
