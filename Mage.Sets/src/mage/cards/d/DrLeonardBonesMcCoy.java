package mage.cards.d;

import mage.MageInt;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.DelayedTriggeredAbility;
import mage.abilities.Mode;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.delayed.AtTheBeginOfNextEndStepDelayedTriggeredAbility;
import mage.abilities.effects.Effect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.ReturnFromGraveyardToHandTargetEffect;
import mage.abilities.keyword.FlashAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.filter.common.FilterPermanentCard;
import mage.filter.predicate.Predicates;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.events.ZoneChangeEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.TargetPermanent;
import mage.target.common.TargetCardInYourGraveyard;
import mage.target.targetpointer.FixedTarget;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author sideboardy-play
 */
public final class DrLeonardBonesMcCoy extends CardImpl {

    private static final FilterPermanentCard filter = new FilterPermanentCard("non-Doctor permanent card from your graveyard");

    static {
        filter.add(Predicates.not(SubType.DOCTOR.getPredicate()));
    }

    public DrLeonardBonesMcCoy(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{G}{G}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.DOCTOR);
        this.power = new MageInt(3);
        this.toughness = new MageInt(2);

        // Flash
        this.addAbility(FlashAbility.getInstance());

        // When Dr. McCoy enters, choose one —
        // • Return target non-Doctor permanent card from your graveyard to your hand.
        // • Choose up to one other target creature. When it dies this turn, return that card to the battlefield under its owner's control with a +1/+1 counter on it at the beginning of the next end step.
        Ability ability = new EntersBattlefieldTriggeredAbility(new ReturnFromGraveyardToHandTargetEffect());
        ability.addTarget(new TargetCardInYourGraveyard(filter));

        Mode mode = new Mode(new DrLeonardBonesMcCoyEffect());
        mode.addTarget(new TargetPermanent(0, 1, StaticFilters.FILTER_ANOTHER_TARGET_CREATURE));
        ability.addMode(mode);

        this.addAbility(ability);
    }

    private DrLeonardBonesMcCoy(final DrLeonardBonesMcCoy card) {
        super(card);
    }

    @Override
    public DrLeonardBonesMcCoy copy() {
        return new DrLeonardBonesMcCoy(this);
    }
}

class DrLeonardBonesMcCoyEffect extends OneShotEffect {

    DrLeonardBonesMcCoyEffect() {
        super(Outcome.Benefit);
        this.staticText = "Choose up to one other target creature. When it dies this turn, "
                + "return that card to the battlefield under its owner's control with a +1/+1 counter on it at the beginning of the next end step";
    }

    private DrLeonardBonesMcCoyEffect(final DrLeonardBonesMcCoyEffect effect) {
        super(effect);
    }

    @Override
    public DrLeonardBonesMcCoyEffect copy() {
        return new DrLeonardBonesMcCoyEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (permanent == null) {
            return false;
        }
        game.addDelayedTriggeredAbility(
                new DrLeonardBonesMcCoyDiesDelayedTriggeredAbility(new MageObjectReference(permanent, game)),
                source
        );
        return true;
    }
}

class DrLeonardBonesMcCoyDiesDelayedTriggeredAbility extends DelayedTriggeredAbility {

    private final MageObjectReference mor;

    DrLeonardBonesMcCoyDiesDelayedTriggeredAbility(MageObjectReference mor) {
        super(new DrLeonardBonesMcCoyScheduleReturnEffect(), Duration.EndOfTurn, true, false);
        this.mor = mor;
        setLeavesTheBattlefieldTrigger(true);
        setTriggerPhrase("When that creature dies this turn, ");
    }

    private DrLeonardBonesMcCoyDiesDelayedTriggeredAbility(final DrLeonardBonesMcCoyDiesDelayedTriggeredAbility ability) {
        super(ability);
        this.mor = ability.mor;
    }

    @Override
    public DrLeonardBonesMcCoyDiesDelayedTriggeredAbility copy() {
        return new DrLeonardBonesMcCoyDiesDelayedTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ZONE_CHANGE;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        ZoneChangeEvent zEvent = (ZoneChangeEvent) event;
        if (zEvent.isDiesEvent() && zEvent.isPermanentMoved() && zEvent.getTarget() != null && mor.refersTo(zEvent.getTarget(), game)) {
            Card card = game.getCard(zEvent.getTargetId());
            if (card != null) {
                getEffects().setTargetPointer(new FixedTarget(card, game));
            }
            return true;
        }
        return false;
    }
}

class DrLeonardBonesMcCoyScheduleReturnEffect extends OneShotEffect {

    DrLeonardBonesMcCoyScheduleReturnEffect() {
        super(Outcome.PutCreatureInPlay);
        this.staticText = "return that card to the battlefield under its owner's control with a +1/+1 counter on it at the beginning of the next end step";
    }

    private DrLeonardBonesMcCoyScheduleReturnEffect(final DrLeonardBonesMcCoyScheduleReturnEffect effect) {
        super(effect);
    }

    @Override
    public DrLeonardBonesMcCoyScheduleReturnEffect copy() {
        return new DrLeonardBonesMcCoyScheduleReturnEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Effect returnEffect = new DrLeonardBonesMcCoyReturnEffect();
        returnEffect.setTargetPointer(getTargetPointer().copy());
        game.addDelayedTriggeredAbility(new AtTheBeginOfNextEndStepDelayedTriggeredAbility(returnEffect), source);
        return true;
    }
}

class DrLeonardBonesMcCoyReturnEffect extends OneShotEffect {

    DrLeonardBonesMcCoyReturnEffect() {
        super(Outcome.PutCreatureInPlay);
        this.staticText = "return that card to the battlefield under its owner's control with a +1/+1 counter on it";
    }

    private DrLeonardBonesMcCoyReturnEffect(final DrLeonardBonesMcCoyReturnEffect effect) {
        super(effect);
    }

    @Override
    public DrLeonardBonesMcCoyReturnEffect copy() {
        return new DrLeonardBonesMcCoyReturnEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Card card = game.getCard(getTargetPointer().getFirst(game, source));
        if (controller == null || card == null) {
            return false;
        }
        if (controller.moveCards(card, Zone.BATTLEFIELD, source, game, false, false, true, null)) {
            Permanent permanent = CardUtil.getPermanentFromCardPutToBattlefield(card, game);
            if (permanent != null) {
                permanent.addCounters(CounterType.P1P1.createInstance(), source.getControllerId(), source, game);
            }
            return true;
        }
        return false;
    }
}
