package mage.cards.d;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.AttacksTriggeredAbility;
import mage.abilities.common.ChooseABackgroundAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.asthought.PlayFromNotOwnHandZoneTargetEffect;
import mage.abilities.keyword.UndauntedAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.stack.Spell;
import mage.players.Player;
import mage.target.TargetCard;
import mage.target.targetpointer.FixedTarget;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class DurnanOfTheYawningPortal extends CardImpl {

    public DurnanOfTheYawningPortal(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{G}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.WARRIOR);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Whenever Durnan of the Yawning Portal attacks, look at the top four cards of your library. You may exile a creature card from among them. Put the rest on the bottom of your library in any order. For as long as that card remains exiled, you may cast it. That spell has undaunted.
        this.addAbility(new AttacksTriggeredAbility(new DurnanOfTheYawningPortalEffect()));

        // Choose a Background
        this.addAbility(ChooseABackgroundAbility.getInstance());
    }

    private DurnanOfTheYawningPortal(final DurnanOfTheYawningPortal card) {
        super(card);
    }

    @Override
    public DurnanOfTheYawningPortal copy() {
        return new DurnanOfTheYawningPortal(this);
    }
}

class DurnanOfTheYawningPortalEffect extends OneShotEffect {

    DurnanOfTheYawningPortalEffect() {
        super(Outcome.PlayForFree);
        this.staticText = "look at the top four cards of your library. You may exile a creature card from among them. "
                + "Put the rest on the bottom of your library in any order. For as long as that card remains exiled, "
                + "you may cast it. That spell has undaunted";
    }

    private DurnanOfTheYawningPortalEffect(final DurnanOfTheYawningPortalEffect effect) {
        super(effect);
    }

    @Override
    public DurnanOfTheYawningPortalEffect copy() {
        return new DurnanOfTheYawningPortalEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        Cards topCards = new CardsImpl(controller.getLibrary().getTopCards(game, 4));
        if (topCards.isEmpty()) {
            return true;
        }
        controller.lookAtCards(source, null, topCards, game);
        TargetCard target = new TargetCard(0, 1, Zone.LIBRARY, StaticFilters.FILTER_CARD_CREATURE);
        target.withChooseHint("to exile");
        if (controller.choose(Outcome.Exile, topCards, target, source, game)) {
            Card chosen = game.getCard(target.getFirstTarget());
            if (chosen != null) {
                topCards.remove(chosen);
                if (controller.moveCardsToExile(
                        chosen, source, game, true,
                        CardUtil.getExileZoneId(game, source),
                        CardUtil.getSourceName(game, source)
                )) {
                    game.addEffect(new PlayFromNotOwnHandZoneTargetEffect(
                            Zone.EXILED, TargetController.YOU, Duration.Custom, false, true
                    ).setTargetPointer(new FixedTarget(chosen, game)), source);
                    game.addEffect(new DurnanGainUndauntedEffect(chosen.getId(), chosen.getZoneChangeCounter(game)), source);
                }
            }
        }
        if (!topCards.isEmpty()) {
            controller.putCardsOnBottomOfLibrary(topCards, game, source, true);
        }
        return true;
    }
}

class DurnanGainUndauntedEffect extends ContinuousEffectImpl {

    private final UUID cardId;
    private final int exiledZcc;

    DurnanGainUndauntedEffect(UUID cardId, int exiledZcc) {
        super(Duration.Custom, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        this.cardId = cardId;
        this.exiledZcc = exiledZcc;
    }

    private DurnanGainUndauntedEffect(final DurnanGainUndauntedEffect effect) {
        super(effect);
        this.cardId = effect.cardId;
        this.exiledZcc = effect.exiledZcc;
    }

    @Override
    public DurnanGainUndauntedEffect copy() {
        return new DurnanGainUndauntedEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Zone zone = game.getState().getZone(cardId);
        int currentZcc = game.getState().getZoneChangeCounter(cardId);
        if (zone == Zone.EXILED && currentZcc == exiledZcc) {
            Card card = game.getCard(cardId);
            if (card != null) {
                UndauntedAbility undaunted = new UndauntedAbility();
                undaunted.setSourceId(card.getId());
                undaunted.setControllerId(source.getControllerId());
                game.getState().addOtherAbility(card, undaunted);
                return true;
            }
        }
        if (zone == Zone.STACK && currentZcc == exiledZcc + 1) {
            Spell spell = game.getStack().getSpell(cardId);
            if (spell != null) {
                UndauntedAbility undaunted = new UndauntedAbility();
                undaunted.setSourceId(spell.getCard().getId());
                undaunted.setControllerId(spell.getControllerId());
                game.getState().addOtherAbility(spell.getCard(), undaunted);
                return true;
            }
        }
        discard();
        return false;
    }
}
