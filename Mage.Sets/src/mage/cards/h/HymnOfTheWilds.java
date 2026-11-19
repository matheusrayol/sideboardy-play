package mage.cards.h;

import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousRuleModifyingEffectImpl;
import mage.abilities.effects.common.InfoEffect;
import mage.abilities.effects.common.cost.SpellsCostReductionControllerEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.watchers.common.SpellsCastWatcher;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class HymnOfTheWilds extends CardImpl {

    public HymnOfTheWilds(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CONSPIRACY}, "");

        // (Start the game with this conspiracy face up in the command zone.)
        this.addAbility(new SimpleStaticAbility(
                Zone.COMMAND,
                new InfoEffect("(Start the game with this conspiracy face up in the command zone.)")
        ));

        // The first creature spell you cast each turn costs {1} less to cast.
        this.addAbility(new SimpleStaticAbility(Zone.COMMAND, new HymnOfTheWildsCostReductionEffect()));

        // You can't cast instant or sorcery spells.
        this.addAbility(new SimpleStaticAbility(Zone.COMMAND, new HymnOfTheWildsCantCastEffect()));
    }

    private HymnOfTheWilds(final HymnOfTheWilds card) {
        super(card);
    }

    @Override
    public HymnOfTheWilds copy() {
        return new HymnOfTheWilds(this);
    }
}

class HymnOfTheWildsCostReductionEffect extends SpellsCostReductionControllerEffect {

    HymnOfTheWildsCostReductionEffect() {
        super(StaticFilters.FILTER_CARD_CREATURE, 1);
        staticText = "The first creature spell you cast each turn costs {1} less to cast";
    }

    private HymnOfTheWildsCostReductionEffect(final HymnOfTheWildsCostReductionEffect effect) {
        super(effect);
    }

    @Override
    public HymnOfTheWildsCostReductionEffect copy() {
        return new HymnOfTheWildsCostReductionEffect(this);
    }

    @Override
    public boolean applies(Ability abilityToModify, Ability source, Game game) {
        if (!super.applies(abilityToModify, source, game)) {
            return false;
        }
        SpellsCastWatcher watcher = game.getState().getWatcher(SpellsCastWatcher.class);
        return watcher != null
                && watcher.getSpellsCastThisTurn(source.getControllerId()).stream().noneMatch(s -> s.isCreature(game));
    }
}

class HymnOfTheWildsCantCastEffect extends ContinuousRuleModifyingEffectImpl {

    HymnOfTheWildsCantCastEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Detriment);
        staticText = "You can't cast instant or sorcery spells";
    }

    private HymnOfTheWildsCantCastEffect(final HymnOfTheWildsCantCastEffect effect) {
        super(effect);
    }

    @Override
    public HymnOfTheWildsCantCastEffect copy() {
        return new HymnOfTheWildsCantCastEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.CAST_SPELL;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        if (!source.isControlledBy(event.getPlayerId())) {
            return false;
        }
        Card card = game.getCard(event.getSourceId());
        return card != null && card.isInstantOrSorcery(game);
    }
}
