package mage.cards.w;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.delayed.WhenTargetDiesDelayedTriggeredAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.ContinuousRuleModifyingEffectImpl;
import mage.abilities.effects.Effect;
import mage.abilities.effects.RedirectionEffect;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.abilities.effects.common.CreateDelayedTriggeredAbilityEffect;
import mage.abilities.effects.common.ExileTargetEffect;
import mage.abilities.effects.common.ruleModifying.CantRegenerateTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SetTargetPointer;
import mage.constants.SubType;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.target.common.TargetCreaturePermanent;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class Whippoorwill extends CardImpl {

    public Whippoorwill(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{G}");

        this.subtype.add(SubType.BIRD);
        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // {G}{G}, {T}: Target creature can't be regenerated this turn. Damage that would be dealt to that creature this turn can't be prevented or dealt instead to another permanent or player. When the creature dies this turn, exile the creature.
        Ability ability = new SimpleActivatedAbility(
                new CantRegenerateTargetEffect(Duration.EndOfTurn, "Target creature"),
                new ManaCostsImpl<>("{G}{G}")
        );
        ability.addCost(new TapSourceCost());
        ability.addEffect(new WhippoorwillCantPreventEffect());
        ability.addEffect(new WhippoorwillCantRedirectEffect());
        ability.addEffect(new CreateDelayedTriggeredAbilityEffect(
                new WhenTargetDiesDelayedTriggeredAbility(
                        new ExileTargetEffect().setText("exile it"),
                        Duration.EndOfTurn,
                        SetTargetPointer.CARD
                )
        ));
        ability.addTarget(new TargetCreaturePermanent());
        this.addAbility(ability);
    }

    private Whippoorwill(final Whippoorwill card) {
        super(card);
    }

    @Override
    public Whippoorwill copy() {
        return new Whippoorwill(this);
    }
}

class WhippoorwillCantPreventEffect extends ContinuousRuleModifyingEffectImpl {

    WhippoorwillCantPreventEffect() {
        super(Duration.EndOfTurn, Outcome.Benefit, false, false);
        staticText = "Damage that would be dealt to that creature this turn can't be prevented or dealt instead to another permanent or player";
    }

    private WhippoorwillCantPreventEffect(final WhippoorwillCantPreventEffect effect) {
        super(effect);
    }

    @Override
    public WhippoorwillCantPreventEffect copy() {
        return new WhippoorwillCantPreventEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.PREVENT_DAMAGE;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        UUID targetId = getTargetPointer().getFirst(game, source);
        return targetId != null && targetId.equals(event.getTargetId());
    }
}

class WhippoorwillCantRedirectEffect extends ReplacementEffectImpl {

    WhippoorwillCantRedirectEffect() {
        super(Duration.EndOfTurn, Outcome.Benefit);
        staticText = "";
    }

    private WhippoorwillCantRedirectEffect(final WhippoorwillCantRedirectEffect effect) {
        super(effect);
    }

    @Override
    public WhippoorwillCantRedirectEffect copy() {
        return new WhippoorwillCantRedirectEffect(this);
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        for (Effect effect : game.getState().getContinuousEffects().getReplacementEffects()) {
            if (effect instanceof RedirectionEffect && !event.getAppliedEffects().contains(effect.getId())) {
                event.getAppliedEffects().add(effect.getId());
            }
        }
        return false;
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DAMAGE_PERMANENT;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        UUID targetId = getTargetPointer().getFirst(game, source);
        return targetId != null && targetId.equals(event.getTargetId());
    }
}
