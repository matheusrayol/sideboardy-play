package mage.cards.u;

import mage.abilities.Ability;
import mage.abilities.common.SagaAbility;
import mage.abilities.effects.OneShotEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.*;
import mage.filter.FilterCard;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.TargetCard;
import mage.target.common.TargetCreaturePermanent;
import mage.target.common.TargetOpponent;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class Unification extends CardImpl {

    public Unification(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{3}{W}{U}");

        this.subtype.add(SubType.SAGA);

        // (As this Saga enters and after your draw step, add a lore counter. Sacrifice after III.)
        SagaAbility sagaAbility = new SagaAbility(this);

        // I — The owner of up to one target creature draws two cards, then shuffles that creature into their library.
        sagaAbility.addChapterEffect(
                this, SagaChapter.CHAPTER_I, SagaChapter.CHAPTER_I,
                new UnificationChapter1Effect(),
                new TargetCreaturePermanent(0, 1)
        );

        // II — You may put a Spacecraft card from your hand or graveyard onto the battlefield.
        sagaAbility.addChapterEffect(
                this, SagaChapter.CHAPTER_II,
                new UnificationChapter2Effect()
        );

        // III — You and target opponent each draw two cards.
        sagaAbility.addChapterEffect(
                this, SagaChapter.CHAPTER_III, SagaChapter.CHAPTER_III,
                new UnificationChapter3Effect(),
                new TargetOpponent()
        );

        this.addAbility(sagaAbility);
    }

    private Unification(final Unification card) {
        super(card);
    }

    @Override
    public Unification copy() {
        return new Unification(this);
    }
}

class UnificationChapter1Effect extends OneShotEffect {

    UnificationChapter1Effect() {
        super(Outcome.DrawCard);
        this.staticText = "the owner of up to one target creature draws two cards, then shuffles that creature into their library";
    }

    private UnificationChapter1Effect(final UnificationChapter1Effect effect) {
        super(effect);
    }

    @Override
    public UnificationChapter1Effect copy() {
        return new UnificationChapter1Effect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent creature = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (creature == null) {
            return true;
        }
        Player owner = game.getPlayer(creature.getOwnerId());
        if (owner == null) {
            return false;
        }
        owner.drawCards(2, source, game);
        owner.moveCards(creature, Zone.LIBRARY, source, game);
        owner.shuffleLibrary(source, game);
        return true;
    }
}

class UnificationChapter2Effect extends OneShotEffect {

    private static final FilterCard filter = new FilterCard("a Spacecraft card from your hand or graveyard");

    static {
        filter.add(SubType.SPACECRAFT.getPredicate());
    }

    UnificationChapter2Effect() {
        super(Outcome.PutCardInPlay);
        this.staticText = "you may put a Spacecraft card from your hand or graveyard onto the battlefield";
    }

    private UnificationChapter2Effect(final UnificationChapter2Effect effect) {
        super(effect);
    }

    @Override
    public UnificationChapter2Effect copy() {
        return new UnificationChapter2Effect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        Cards pool = new CardsImpl();
        pool.addAllCards(controller.getHand().getCards(filter, game));
        pool.addAllCards(controller.getGraveyard().getCards(filter, game));
        if (pool.isEmpty()) {
            return true;
        }
        TargetCard target = new TargetCard(0, 1, Zone.ALL, filter);
        target.withNotTarget(true);
        if (controller.choose(Outcome.PutCardInPlay, pool, target, source, game)) {
            Card chosen = game.getCard(target.getFirstTarget());
            if (chosen != null) {
                controller.moveCards(chosen, Zone.BATTLEFIELD, source, game);
            }
        }
        return true;
    }
}

class UnificationChapter3Effect extends OneShotEffect {

    UnificationChapter3Effect() {
        super(Outcome.DrawCard);
        this.staticText = "you and target opponent each draw two cards";
    }

    private UnificationChapter3Effect(final UnificationChapter3Effect effect) {
        super(effect);
    }

    @Override
    public UnificationChapter3Effect copy() {
        return new UnificationChapter3Effect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Player opponent = game.getPlayer(getTargetPointer().getFirst(game, source));
        if (controller != null) {
            controller.drawCards(2, source, game);
        }
        if (opponent != null) {
            opponent.drawCards(2, source, game);
        }
        return true;
    }
}
