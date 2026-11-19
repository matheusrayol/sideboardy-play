package mage.cards.d;

import mage.MageInt;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.DealsCombatDamageToAPlayerTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.SourceIsSpellCondition;
import mage.abilities.costs.AlternativeCostSourceAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.keyword.CrewAbility;
import mage.abilities.keyword.FlyingAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterCard;
import mage.filter.predicate.Predicate;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.game.stack.Spell;
import mage.players.Player;
import mage.util.CardUtil;
import mage.watchers.Watcher;

import java.util.*;

/**
 * @author Sideboardy
 */
public final class DragonsSmile extends CardImpl {

    public DragonsSmile(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{5}{R}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.VEHICLE);
        this.power = new MageInt(4);
        this.toughness = new MageInt(5);

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // Once during each of your turns, you may pay {0} rather than pay the mana cost for a spell you cast from exile.
        Ability staticAbility = new SimpleStaticAbility(new DragonsSmileRuleEffect());
        staticAbility.addWatcher(new DragonsSmileWatcher());
        this.addAbility(staticAbility);

        // Whenever Dragon's Smile deals combat damage to a player, that player exiles that many cards from the top of their library. You may play those cards this turn.
        this.addAbility(new DealsCombatDamageToAPlayerTriggeredAbility(
                new DragonsSmileExileEffect(), false, true
        ));

        // Crew 3
        this.addAbility(new CrewAbility(3));
    }

    private DragonsSmile(final DragonsSmile card) {
        super(card);
    }

    @Override
    public DragonsSmile copy() {
        return new DragonsSmile(this);
    }
}

class DragonsSmileRuleEffect extends ContinuousEffectImpl {

    private static final FilterCard filter = new FilterCard("a spell you cast from exile");

    static {
        filter.add((Predicate<Card>) (input, game) ->
                input != null && game.getState().getZone(input.getMainCard().getId()) == Zone.EXILED);
    }

    private final DragonsSmileAlternativeCostAbility alternativeCastingCostAbility;

    DragonsSmileRuleEffect() {
        super(Duration.WhileOnBattlefield, Outcome.PlayForFree);
        this.alternativeCastingCostAbility = new DragonsSmileAlternativeCostAbility(filter);
        staticText = "once during each of your turns, you may pay {0} rather than pay the mana cost for a spell you cast from exile";
    }

    private DragonsSmileRuleEffect(final DragonsSmileRuleEffect effect) {
        super(effect);
        this.alternativeCastingCostAbility = effect.alternativeCastingCostAbility;
    }

    @Override
    public DragonsSmileRuleEffect copy() {
        return new DragonsSmileRuleEffect(this);
    }

    @Override
    public void init(Ability source, Game game, UUID activePlayerId) {
        super.init(source, game, activePlayerId);
        alternativeCastingCostAbility.setSourceId(source.getSourceId());
        alternativeCastingCostAbility.setControllerId(source.getControllerId());
        DragonsSmileWatcher watcher = game.getState().getWatcher(DragonsSmileWatcher.class);
        if (watcher != null) {
            Permanent perm = game.getPermanent(source.getSourceId());
            if (perm != null) {
                watcher.registerAlternativeCost(alternativeCastingCostAbility.getOriginalId(), new MageObjectReference(perm, game));
            }
        }
    }

    @Override
    public boolean apply(Layer layer, SubLayer sublayer, Ability source, Game game) {
        Player controller = game.getPlayer(source.getControllerId());
        Permanent sourcePerm = game.getPermanent(source.getSourceId());
        if (controller == null || sourcePerm == null || !game.isActivePlayer(source.getControllerId())) {
            return false;
        }
        DragonsSmileWatcher watcher = game.getState().getWatcher(DragonsSmileWatcher.class);
        if (watcher != null && watcher.isUsedThisTurn(new MageObjectReference(sourcePerm, game))) {
            return false;
        }
        alternativeCastingCostAbility.setSourceId(source.getSourceId());
        alternativeCastingCostAbility.setControllerId(source.getControllerId());
        controller.getAlternativeSourceCosts().add(alternativeCastingCostAbility);
        return true;
    }

    @Override
    public boolean apply(Game game, Ability source) {
        return false;
    }

    @Override
    public boolean hasLayer(Layer layer) {
        return layer == Layer.RulesEffects;
    }
}

class DragonsSmileAlternativeCostAbility extends AlternativeCostSourceAbility {

    DragonsSmileAlternativeCostAbility(FilterCard filter) {
        super(new ManaCostsImpl<>("{0}"), SourceIsSpellCondition.instance, null, filter, true);
    }

    private DragonsSmileAlternativeCostAbility(final DragonsSmileAlternativeCostAbility ability) {
        super(ability);
    }

    @Override
    public DragonsSmileAlternativeCostAbility copy() {
        return new DragonsSmileAlternativeCostAbility(this);
    }

    @Override
    public boolean isAvailable(Ability source, Game game) {
        if (!game.isActivePlayer(this.getControllerId())) {
            return false;
        }
        Permanent perm = game.getPermanent(this.getSourceId());
        if (perm == null) {
            return false;
        }
        DragonsSmileWatcher watcher = game.getState().getWatcher(DragonsSmileWatcher.class);
        if (watcher != null && watcher.isUsedThisTurn(new MageObjectReference(perm, game))) {
            return false;
        }
        return super.isAvailable(source, game);
    }
}

class DragonsSmileWatcher extends Watcher {

    private final Map<UUID, MageObjectReference> altIdToSource = new HashMap<>();
    private final Set<MageObjectReference> usedThisTurn = new HashSet<>();

    DragonsSmileWatcher() {
        super(WatcherScope.GAME);
    }

    void registerAlternativeCost(UUID altOriginalId, MageObjectReference sourceMor) {
        altIdToSource.put(altOriginalId, sourceMor);
    }

    boolean isUsedThisTurn(MageObjectReference sourceMor) {
        return usedThisTurn.contains(sourceMor);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() == GameEvent.EventType.SPELL_CAST) {
            Spell spell = game.getSpell(event.getTargetId());
            if (spell != null) {
                for (Map.Entry<UUID, MageObjectReference> entry : altIdToSource.entrySet()) {
                    if (AlternativeCostSourceAbility.getActivatedStatus(game, spell.getSpellAbility(), entry.getKey(), false)) {
                        usedThisTurn.add(entry.getValue());
                    }
                }
            }
        }
    }

    @Override
    public void reset() {
        super.reset();
        usedThisTurn.clear();
    }
}

class DragonsSmileExileEffect extends OneShotEffect {

    DragonsSmileExileEffect() {
        super(Outcome.PlayForFree);
        this.staticText = "that player exiles that many cards from the top of their library. You may play those cards this turn";
    }

    private DragonsSmileExileEffect(final DragonsSmileExileEffect effect) {
        super(effect);
    }

    @Override
    public DragonsSmileExileEffect copy() {
        return new DragonsSmileExileEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Player damagedPlayer = game.getPlayer(getTargetPointer().getFirst(game, source));
        int damage = (Integer) getValue("damage");
        if (controller == null || damagedPlayer == null || damage <= 0) {
            return false;
        }
        Set<Card> exiled = damagedPlayer.getLibrary().getTopCards(game, damage);
        if (exiled.isEmpty()) {
            return true;
        }
        damagedPlayer.moveCardsToExile(exiled, source, game, true, CardUtil.getExileZoneId(game, source), CardUtil.getSourceName(game, source));
        for (Card card : exiled) {
            if (game.getState().getZone(card.getId()) == Zone.EXILED) {
                CardUtil.makeCardPlayable(
                        game, source, card, false, Duration.EndOfTurn, false,
                        controller.getId(), null
                );
            }
        }
        return true;
    }
}
