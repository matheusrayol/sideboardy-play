package mage.cards.a;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.InfoEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.CardsImpl;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.filter.FilterCard;
import mage.game.Game;
import mage.players.Player;
import mage.target.TargetCard;
import mage.util.CardUtil;

import java.util.Collection;
import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class ArcaneSavant extends CardImpl {

    public ArcaneSavant(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{U}{U}");

        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.WIZARD);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Before you shuffle your deck to start the game, you may reveal Arcane Savant from your deck and exile an instant or sorcery card you drafted that isn't in your deck.
        this.addAbility(new SimpleStaticAbility(
                Zone.ALL,
                new InfoEffect("Before you shuffle your deck to start the game, you may reveal {this} from your deck "
                        + "and exile an instant or sorcery card you drafted that isn't in your deck - not implemented.")
        ));

        // When Arcane Savant enters, copy a card you exiled with cards named Arcane Savant. You may cast the copy without paying its mana cost.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new ArcaneSavantEffect(), false));
    }

    private ArcaneSavant(final ArcaneSavant card) {
        super(card);
    }

    @Override
    public ArcaneSavant copy() {
        return new ArcaneSavant(this);
    }
}

class ArcaneSavantEffect extends OneShotEffect {

    ArcaneSavantEffect() {
        super(Outcome.PlayForFree);
        staticText = "copy a card you exiled with cards named {this}. You may cast the copy without paying its mana cost";
    }

    private ArcaneSavantEffect(final ArcaneSavantEffect effect) {
        super(effect);
    }

    @Override
    public ArcaneSavantEffect copy() {
        return new ArcaneSavantEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        Object raw = game.getState().getValue(controller.getId() + "_arcane_savant_exile");
        if (!(raw instanceof Collection<?>) || ((Collection<?>) raw).isEmpty()) {
            return true;
        }
        CardsImpl exiled = new CardsImpl();
        for (Object id : (Collection<?>) raw) {
            if (id instanceof UUID) {
                Card c = game.getCard((UUID) id);
                if (c != null) {
                    exiled.add(c);
                }
            }
        }
        if (exiled.isEmpty()) {
            return true;
        }
        Card chosen = exiled.size() == 1 ? exiled.getRandom(game) : null;
        if (chosen == null) {
            TargetCard target = new TargetCard(Zone.EXILED, new FilterCard("card exiled with Arcane Savant"));
            target.withNotTarget(true);
            if (controller.choose(Outcome.PlayForFree, exiled, target, source, game)) {
                chosen = exiled.get(target.getFirstTarget(), game);
            }
        }
        if (chosen != null) {
            Card copyCard = game.copyCard(chosen, source, controller.getId());
            if (controller.chooseUse(Outcome.PlayForFree, "Cast the copy of " + copyCard.getName() + " without paying its mana cost?", source, game)) {
                CardUtil.castSpellWithAttributesForFree(controller, source, game, copyCard);
            }
        }
        return true;
    }
}
