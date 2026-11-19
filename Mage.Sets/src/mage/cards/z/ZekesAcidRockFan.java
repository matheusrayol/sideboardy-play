package mage.cards.z;

import mage.MageInt;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.DrawDiscardControllerEffect;
import mage.abilities.keyword.EmergeAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterCard;
import mage.filter.predicate.Predicates;
import mage.game.Game;
import mage.game.events.EntersTheBattlefieldEvent;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.game.stack.Spell;
import mage.players.Player;

import java.util.*;

/**
 * @author Sideboardy
 */
public final class ZekesAcidRockFan extends CardImpl {

    public ZekesAcidRockFan(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{B}{G}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.SQUID);
        this.subtype.add(SubType.SORCERER);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Each sea monster spell you cast has emerge. The emerge cost is equal to its mana cost.
        this.addAbility(new SimpleStaticAbility(new ZekesGainEmergeEffect()));

        // Whenever a creature you control enters, if it was cast for its emerge cost, draw two cards, then discard a card.
        this.addAbility(new ZekesEmergeTriggeredAbility());
    }

    private ZekesAcidRockFan(final ZekesAcidRockFan card) {
        super(card);
    }

    @Override
    public ZekesAcidRockFan copy() {
        return new ZekesAcidRockFan(this);
    }
}

class ZekesGainEmergeEffect extends ContinuousEffectImpl {

    private static final FilterCard seaMonsterFilter = new FilterCard("sea monster spell");

    static {
        seaMonsterFilter.add(Predicates.or(
                SubType.KRAKEN.getPredicate(),
                SubType.LEVIATHAN.getPredicate(),
                SubType.OCTOPUS.getPredicate(),
                SubType.SERPENT.getPredicate(),
                SubType.WHALE.getPredicate()
        ));
    }

    ZekesGainEmergeEffect() {
        super(Duration.WhileOnBattlefield, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        this.staticText = "Each sea monster spell you cast has emerge. The emerge cost is equal to its mana cost. "
                + "<i>(You may cast those spells by sacrificing a creature and paying the emerge cost reduced by the sacrificed creature's mana value. "
                + "Krakens, Leviathans, Octopuses, Serpents, and Whales are sea monsters.)</i>";
    }

    private ZekesGainEmergeEffect(final ZekesGainEmergeEffect effect) {
        super(effect);
    }

    @Override
    public ZekesGainEmergeEffect copy() {
        return new ZekesGainEmergeEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        Set<Card> cardsToGainEmerge = new HashSet<>();
        cardsToGainEmerge.addAll(controller.getHand().getCards(seaMonsterFilter, game));
        cardsToGainEmerge.addAll(controller.getGraveyard().getCards(seaMonsterFilter, game));
        controller.getLibrary().getCards(game).stream()
                .filter(c -> seaMonsterFilter.match(c, game))
                .forEach(cardsToGainEmerge::add);
        game.getExile().getCardsInRange(game, controller.getId()).stream()
                .filter(c -> seaMonsterFilter.match(c, game))
                .forEach(cardsToGainEmerge::add);
        game.getCommanderCardsFromCommandZone(controller, CommanderCardType.ANY).stream()
                .filter(c -> seaMonsterFilter.match(c, game))
                .forEach(cardsToGainEmerge::add);
        game.getStack().stream()
                .filter(Spell.class::isInstance)
                .filter(s -> s.isControlledBy(controller.getId()))
                .filter(s -> seaMonsterFilter.match((Spell) s, game))
                .map(s -> game.getCard(s.getSourceId()))
                .filter(Objects::nonNull)
                .forEach(cardsToGainEmerge::add);

        for (Card card : cardsToGainEmerge) {
            if (card.getManaCost().getText().isEmpty()) {
                continue;
            }
            Ability ability = new EmergeAbility(card, card.getManaCost().getText());
            ability.setSourceId(card.getId());
            ability.setControllerId(card.getControllerOrOwnerId());
            game.getState().addOtherAbility(card, ability);
        }
        return true;
    }
}

class ZekesEmergeTriggeredAbility extends TriggeredAbilityImpl {

    ZekesEmergeTriggeredAbility() {
        super(Zone.BATTLEFIELD, new DrawDiscardControllerEffect(2, 1), false);
    }

    private ZekesEmergeTriggeredAbility(final ZekesEmergeTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public ZekesEmergeTriggeredAbility copy() {
        return new ZekesEmergeTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ENTERS_THE_BATTLEFIELD;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        Permanent permanent = ((EntersTheBattlefieldEvent) event).getTarget();
        if (permanent == null
                || !permanent.isControlledBy(getControllerId())
                || !permanent.isCreature(game)) {
            return false;
        }
        return game.getPermanentCostsTags()
                .getOrDefault(new MageObjectReference(permanent, game, -1), Collections.emptyMap())
                .containsKey(EmergeAbility.EMERGE_ACTIVATION_CREATURE_REFERENCE);
    }

    @Override
    public String getRule() {
        return "Whenever a creature you control enters, if it was cast for its emerge cost, draw two cards, then discard a card.";
    }
}
