package mage.cards.p;

import mage.ConditionalMana;
import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.SpellAbility;
import mage.abilities.condition.Condition;
import mage.abilities.costs.Cost;
import mage.abilities.costs.mana.ManaCost;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.Effect;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.abilities.mana.ActivatedManaAbilityImpl;
import mage.abilities.mana.conditional.ManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Layer;
import mage.constants.Outcome;
import mage.constants.SubLayer;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.events.ManaEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.Target;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class Piracy extends CardImpl {

    public Piracy(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{U}{U}");

        // Until end of turn, you may tap lands you don't control for mana. Spend this mana only to cast spells.
        this.getSpellAbility().addEffect(new PiracyGrantManaAbilitiesEffect());
        this.getSpellAbility().addEffect(new PiracyRestrictManaEffect());
    }

    private Piracy(final Piracy card) {
        super(card);
    }

    @Override
    public Piracy copy() {
        return new Piracy(this);
    }
}

class PiracyGrantManaAbilitiesEffect extends ContinuousEffectImpl {

    PiracyGrantManaAbilitiesEffect() {
        super(Duration.EndOfTurn, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.Benefit);
        staticText = "Until end of turn, you may tap lands you don't control for mana. Spend this mana only to cast spells";
    }

    private PiracyGrantManaAbilitiesEffect(final PiracyGrantManaAbilitiesEffect effect) {
        super(effect);
    }

    @Override
    public PiracyGrantManaAbilitiesEffect copy() {
        return new PiracyGrantManaAbilitiesEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        UUID controllerId = source.getControllerId();
        for (Permanent land : game.getBattlefield().getAllActivePermanents(StaticFilters.FILTER_LAND, game)) {
            if (land.isControlledBy(controllerId)) {
                continue;
            }
            for (Ability ability : land.getAbilities(game)) {
                if (ability instanceof ActivatedManaAbilityImpl && !(ability instanceof PiracyManaAbilityWrapper)) {
                    PiracyManaAbilityWrapper wrapped = new PiracyManaAbilityWrapper((ActivatedManaAbilityImpl) ability, controllerId);
                    land.addAbility(wrapped, source.getSourceId(), game);
                }
            }
        }
        return true;
    }
}

class PiracyManaAbilityWrapper extends ActivatedManaAbilityImpl {

    private final UUID piracyControllerId;

    PiracyManaAbilityWrapper(ActivatedManaAbilityImpl delegate, UUID piracyControllerId) {
        super(Zone.BATTLEFIELD, null, null);
        this.piracyControllerId = piracyControllerId;
        this.undoPossible = delegate.isUndoPossible();
        this.poolDependant = delegate.isPoolDependant();
        for (Effect effect : delegate.getEffects()) {
            this.addEffect(effect.copy());
        }
        for (Cost cost : delegate.getCosts()) {
            this.addCost(cost.copy());
        }
        for (ManaCost manaCost : delegate.getManaCosts()) {
            this.getManaCosts().add((ManaCost) manaCost.copy());
            this.getManaCostsToPay().add((ManaCost) manaCost.copy());
        }
        for (Target target : delegate.getTargets()) {
            this.addTarget(target.copy());
        }
    }

    private PiracyManaAbilityWrapper(final PiracyManaAbilityWrapper ability) {
        super(ability);
        this.piracyControllerId = ability.piracyControllerId;
    }

    @Override
    public PiracyManaAbilityWrapper copy() {
        return new PiracyManaAbilityWrapper(this);
    }

    @Override
    protected boolean checkTargetController(UUID playerId, Game game) {
        return piracyControllerId.equals(playerId);
    }

    @Override
    public String getRule() {
        return super.getRule();
    }
}

class PiracyRestrictManaEffect extends ReplacementEffectImpl {

    PiracyRestrictManaEffect() {
        super(Duration.EndOfTurn, Outcome.Neutral);
        staticText = "";
    }

    private PiracyRestrictManaEffect(final PiracyRestrictManaEffect effect) {
        super(effect);
    }

    @Override
    public PiracyRestrictManaEffect copy() {
        return new PiracyRestrictManaEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ADD_MANA;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        if (!source.isControlledBy(event.getPlayerId())) {
            return false;
        }
        ManaEvent manaEvent = (ManaEvent) event;
        if (manaEvent.getMana() instanceof PiracyConditionalMana) {
            return false;
        }
        Permanent land = game.getPermanentOrLKIBattlefield(event.getSourceId());
        return land != null && land.isLand(game) && !land.isControlledBy(source.getControllerId());
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        Mana originalMana = ((ManaEvent) event).getMana();
        ConditionalMana restricted = new PiracyConditionalMana(originalMana);
        controller.getManaPool().addMana(restricted, game, source);
        return true;
    }
}

class PiracyConditionalMana extends ConditionalMana {

    PiracyConditionalMana(Mana mana) {
        super(mana);
        staticText = "Spend this mana only to cast spells";
        addCondition(new PiracyManaCondition());
    }
}

class PiracyManaCondition extends ManaCondition {

    @Override
    public boolean apply(Game game, Ability source) {
        return source instanceof SpellAbility && !source.isActivated();
    }

    @Override
    public boolean apply(Game game, Ability source, UUID originalId, Cost costToPay) {
        return apply(game, source);
    }
}
