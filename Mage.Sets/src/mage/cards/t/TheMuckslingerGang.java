package mage.cards.t;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.continuous.BoostControlledEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.permanent.AttackingPredicate;
import mage.filter.predicate.permanent.BlockingPredicate;
import mage.game.Game;
import mage.game.command.CommandObject;
import mage.game.command.Commander;
import mage.game.stack.Spell;
import mage.game.stack.StackObject;
import mage.players.Player;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class TheMuckslingerGang extends CardImpl {

    private static final FilterCreaturePermanent filter = new FilterCreaturePermanent("each other creature you control that isn't attacking or blocking");

    static {
        filter.add(Predicates.not(AttackingPredicate.instance));
        filter.add(Predicates.not(BlockingPredicate.instance));
    }

    public TheMuckslingerGang(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{B}{R}{G}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.GOBLIN);
        this.subtype.add(SubType.LIZARD);
        this.subtype.add(SubType.BEAST);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Each other creature you control gets +3/+3 as long as it's not attacking or blocking.
        this.addAbility(new SimpleStaticAbility(
                new BoostControlledEffect(3, 3, Duration.WhileOnBattlefield, filter, true)
                        .setText("each other creature you control gets +3/+3 as long as it's not attacking or blocking")
        ));

        // Creature spells you control and creature cards you own in any zone other than the battlefield or the stack get +3/+3.
        this.addAbility(new SimpleStaticAbility(new TheMuckslingerGangNonBattlefieldEffect()));
    }

    private TheMuckslingerGang(final TheMuckslingerGang card) {
        super(card);
    }

    @Override
    public TheMuckslingerGang copy() {
        return new TheMuckslingerGang(this);
    }
}

class TheMuckslingerGangNonBattlefieldEffect extends ContinuousEffectImpl {

    TheMuckslingerGangNonBattlefieldEffect() {
        super(Duration.WhileOnBattlefield, Layer.PTChangingEffects_7, SubLayer.ModifyPT_7c, Outcome.BoostCreature);
        this.staticText = "creature spells you control and creature cards you own in any zone other than the battlefield or the stack get +3/+3";
    }

    private TheMuckslingerGangNonBattlefieldEffect(final TheMuckslingerGangNonBattlefieldEffect effect) {
        super(effect);
    }

    @Override
    public TheMuckslingerGangNonBattlefieldEffect copy() {
        return new TheMuckslingerGangNonBattlefieldEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        // Creature spells you control on the stack
        for (StackObject stackObject : game.getStack()) {
            if (stackObject instanceof Spell && stackObject.isControlledBy(controller.getId())) {
                Spell spell = (Spell) stackObject;
                if (spell.isCreature(game)) {
                    spell.getPower().setBoostedValue(CardUtil.overflowInc(spell.getPower().getModifiedBaseValue(), 3));
                    spell.getToughness().setBoostedValue(CardUtil.overflowInc(spell.getToughness().getModifiedBaseValue(), 3));
                }
            }
        }
        // Creature cards you own in zones other than the battlefield or stack
        for (Card card : controller.getHand().getCards(game)) {
            boostCardIfCreature(card, game);
        }
        for (Card card : controller.getGraveyard().getCards(game)) {
            boostCardIfCreature(card, game);
        }
        for (Card card : controller.getLibrary().getCards(game)) {
            boostCardIfCreature(card, game);
        }
        for (Card card : game.getExile().getCardsOwned(game, controller.getId())) {
            boostCardIfCreature(card, game);
        }
        for (CommandObject cmd : game.getState().getCommand()) {
            if (cmd instanceof Commander && cmd.isControlledBy(controller.getId())) {
                Card commanderCard = game.getCard(cmd.getId());
                if (commanderCard != null && game.getState().getZone(commanderCard.getId()) == Zone.COMMAND) {
                    boostCardIfCreature(commanderCard, game);
                }
            }
        }
        return true;
    }

    private void boostCardIfCreature(Card card, Game game) {
        if (card.isCreature(game)) {
            card.getPower().setBoostedValue(CardUtil.overflowInc(card.getPower().getModifiedBaseValue(), 3));
            card.getToughness().setBoostedValue(CardUtil.overflowInc(card.getToughness().getModifiedBaseValue(), 3));
        }
    }
}
