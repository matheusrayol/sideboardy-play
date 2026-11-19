package mage.cards.a;

import mage.MageInt;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.GainAbilityTargetEffect;
import mage.abilities.keyword.HasteAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.stack.Spell;
import mage.game.stack.StackObject;
import mage.players.Player;
import mage.target.common.TargetCardInHand;
import mage.target.targetpointer.FixedTarget;
import mage.util.CardUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class AlaundoTheSeer extends CardImpl {

    public AlaundoTheSeer(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{G}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.SHAMAN);
        this.power = new MageInt(3);
        this.toughness = new MageInt(5);

        // {T}: Draw a card, then exile a card from your hand and put a number of time counters on it equal to its mana value. It gains "When the last time counter is removed from this card, if it's exiled, you may cast it without paying its mana cost. If you cast a creature spell this way, it gains haste until end of turn." Then remove a time counter from each other card you own in exile.
        this.addAbility(new SimpleActivatedAbility(new AlaundoTheSeerEffect(), new TapSourceCost()));
    }

    private AlaundoTheSeer(final AlaundoTheSeer card) {
        super(card);
    }

    @Override
    public AlaundoTheSeer copy() {
        return new AlaundoTheSeer(this);
    }
}

class AlaundoTheSeerEffect extends OneShotEffect {

    AlaundoTheSeerEffect() {
        super(Outcome.PlayForFree);
        this.staticText = "Draw a card, then exile a card from your hand and put a number of time counters on it equal to its mana value. "
                + "It gains \"When the last time counter is removed from this card, if it's exiled, you may cast it without paying its mana cost. "
                + "If you cast a creature spell this way, it gains haste until end of turn.\" "
                + "Then remove a time counter from each other card you own in exile";
    }

    private AlaundoTheSeerEffect(final AlaundoTheSeerEffect effect) {
        super(effect);
    }

    @Override
    public AlaundoTheSeerEffect copy() {
        return new AlaundoTheSeerEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        controller.drawCards(1, source, game);

        UUID newlyExiledId = null;
        if (!controller.getHand().isEmpty()) {
            TargetCardInHand target = new TargetCardInHand();
            target.withChooseHint("to exile with time counters");
            if (controller.chooseTarget(Outcome.Exile, target, source, game)) {
                Card card = game.getCard(target.getFirstTarget());
                if (card != null && controller.moveCardsToExile(
                        card, source, game, true,
                        CardUtil.getExileZoneId(game, source),
                        CardUtil.getSourceName(game, source)
                )) {
                    newlyExiledId = card.getId();
                    int manaValue = card.getManaValue();
                    if (manaValue > 0) {
                        card.addCounters(CounterType.TIME.createInstance(manaValue), controller.getId(), source, game);
                    }
                    MageObjectReference mor = new MageObjectReference(card, game);
                    game.addEffect(new AlaundoGainAbilityEffect(mor), source);
                }
            }
        }

        List<Card> exiledSnapshot = new ArrayList<>(game.getExile().getAllCards(game));
        for (Card otherCard : exiledSnapshot) {
            if (!otherCard.isOwnedBy(controller.getId()) || otherCard.getId().equals(newlyExiledId)) {
                continue;
            }
            if (otherCard.getCounters(game).getCount(CounterType.TIME) > 0) {
                otherCard.removeCounters(CounterType.TIME.createInstance(), source, game);
            }
        }
        return true;
    }
}

class AlaundoGainAbilityEffect extends ContinuousEffectImpl {

    private final MageObjectReference cardRef;

    AlaundoGainAbilityEffect(MageObjectReference cardRef) {
        super(Duration.Custom, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        this.cardRef = cardRef;
    }

    private AlaundoGainAbilityEffect(final AlaundoGainAbilityEffect effect) {
        super(effect);
        this.cardRef = effect.cardRef;
    }

    @Override
    public AlaundoGainAbilityEffect copy() {
        return new AlaundoGainAbilityEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Card card = cardRef.getCard(game);
        if (card == null || game.getState().getZone(card.getId()) != Zone.EXILED) {
            discard();
            return false;
        }
        Ability ability = new AlaundoLastTimeCounterTriggeredAbility();
        ability.setSourceId(card.getId());
        ability.setControllerId(card.getOwnerId());
        game.getState().addOtherAbility(card, ability);
        return true;
    }
}

class AlaundoLastTimeCounterTriggeredAbility extends TriggeredAbilityImpl {

    AlaundoLastTimeCounterTriggeredAbility() {
        super(Zone.EXILED, new AlaundoCastExiledEffect(), false);
    }

    private AlaundoLastTimeCounterTriggeredAbility(final AlaundoLastTimeCounterTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public AlaundoLastTimeCounterTriggeredAbility copy() {
        return new AlaundoLastTimeCounterTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.COUNTER_REMOVED;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!event.getTargetId().equals(this.getSourceId())
                || !CounterType.TIME.getName().equals(event.getData())) {
            return false;
        }
        Card card = game.getCard(this.getSourceId());
        return card != null
                && game.getState().getZone(card.getId()) == Zone.EXILED
                && card.getCounters(game).getCount(CounterType.TIME) == 0;
    }

    @Override
    public String getRule() {
        return "When the last time counter is removed from this card, if it's exiled, "
                + "you may cast it without paying its mana cost. If you cast a creature spell this way, it gains haste until end of turn.";
    }
}

class AlaundoCastExiledEffect extends OneShotEffect {

    AlaundoCastExiledEffect() {
        super(Outcome.PlayForFree);
        this.staticText = "you may cast it without paying its mana cost. If you cast a creature spell this way, it gains haste until end of turn";
    }

    private AlaundoCastExiledEffect(final AlaundoCastExiledEffect effect) {
        super(effect);
    }

    @Override
    public AlaundoCastExiledEffect copy() {
        return new AlaundoCastExiledEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Card card = game.getCard(source.getSourceId());
        if (controller == null || card == null || game.getState().getZone(card.getId()) != Zone.EXILED) {
            return false;
        }
        card = card.getMainCard();
        if (!controller.chooseUse(Outcome.PlayForFree, "Cast " + card.getLogName() + " without paying its mana cost?", source, game)) {
            return true;
        }
        if (CardUtil.castSpellWithAttributesForFree(controller, source, game, card)) {
            for (StackObject stackObject : game.getStack()) {
                if (stackObject instanceof Spell
                        && ((Spell) stackObject).getMainCard().getId().equals(card.getId())
                        && stackObject.isCreature(game)) {
                    Card castCard = ((Spell) stackObject).getCard();
                    game.addEffect(new GainAbilityTargetEffect(HasteAbility.getInstance(), Duration.EndOfTurn)
                            .setTargetPointer(new FixedTarget(castCard.getId(), castCard.getZoneChangeCounter(game) + 1)), source);
                    break;
                }
            }
        }
        return true;
    }
}
