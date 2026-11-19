package mage.cards.a;

import mage.ApprovingObject;
import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.SpellAbility;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.mana.ManaCost;
import mage.abilities.costs.mana.ManaCosts;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.keyword.SurveilEffect;
import mage.abilities.keyword.MiracleAbility;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.CardsImpl;
import mage.constants.*;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.players.Player;
import mage.target.targetpointer.FixedTarget;
import mage.util.CardUtil;
import mage.watchers.common.CardsDrawnThisTurnWatcher;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class AminatouVeilPiercer extends CardImpl {

    public AminatouVeilPiercer(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{W}{U}{B}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.WIZARD);
        this.power = new MageInt(2);
        this.toughness = new MageInt(4);

        // At the beginning of your upkeep, surveil 2.
        this.addAbility(new BeginningOfUpkeepTriggeredAbility(new SurveilEffect(2)));

        // Each enchantment card in your hand has miracle. Its miracle cost is equal to its mana cost reduced by {4}.
        this.addAbility(new SimpleStaticAbility(new AminatouMiracleEffect()));
        this.addAbility(new AminatouMiracleTriggeredAbility());
    }

    private AminatouVeilPiercer(final AminatouVeilPiercer card) {
        super(card);
    }

    @Override
    public AminatouVeilPiercer copy() {
        return new AminatouVeilPiercer(this);
    }
}

class AminatouMiracleEffect extends ContinuousEffectImpl {

    AminatouMiracleEffect() {
        super(Duration.WhileOnBattlefield, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        this.staticText = "Each enchantment card in your hand has miracle. Its miracle cost is equal to its mana cost reduced by {4}";
    }

    private AminatouMiracleEffect(final AminatouMiracleEffect effect) {
        super(effect);
    }

    @Override
    public AminatouMiracleEffect copy() {
        return new AminatouMiracleEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        for (Card card : controller.getHand().getCards(game)) {
            if (!card.isEnchantment(game) || card.isLand(game) || card.getManaCost() == null) {
                continue;
            }
            if (card.getAbilities(game).containsClass(MiracleAbility.class)) {
                continue;
            }
            String reducedText = CardUtil.reduceCost(card.getManaCost(), 4).getText();
            if (reducedText.isEmpty()) {
                reducedText = "{0}";
            }
            MiracleAbility miracleAbility = new MiracleAbility(reducedText);
            miracleAbility.setControllerId(controller.getId());
            miracleAbility.setSourceId(card.getId());
            game.getState().addOtherAbility(card, miracleAbility);
        }
        return true;
    }
}

class AminatouMiracleTriggeredAbility extends TriggeredAbilityImpl {

    AminatouMiracleTriggeredAbility() {
        super(Zone.BATTLEFIELD, null, false);
        setRuleVisible(false);
    }

    private AminatouMiracleTriggeredAbility(final AminatouMiracleTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public AminatouMiracleTriggeredAbility copy() {
        return new AminatouMiracleTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DREW_CARD;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (game.getPhase() == null || !event.getPlayerId().equals(getControllerId())) {
            return false;
        }
        CardsDrawnThisTurnWatcher watcher = game.getState().getWatcher(CardsDrawnThisTurnWatcher.class);
        if (watcher == null || watcher.getCardsDrawnThisTurn(getControllerId()) != 1) {
            return false;
        }
        Card card = game.getCard(event.getTargetId());
        if (card == null
                || !card.isEnchantment(game)
                || card.isLand(game)
                || card.getManaCost() == null
                || card.getAbilities(game).containsClass(MiracleAbility.class)) {
            return false;
        }
        Player controller = game.getPlayer(getControllerId());
        if (controller == null) {
            return false;
        }
        String reducedText = CardUtil.reduceCost(card.getManaCost(), 4).getText();
        if (reducedText.isEmpty()) {
            reducedText = "{0}";
        }
        controller.lookAtCards("Miracle", new CardsImpl(card), game);
        if (!controller.chooseUse(Outcome.Benefit, "Reveal " + card.getLogName() + " to cast it for its miracle cost (" + reducedText + ")?", this, game)) {
            return false;
        }
        controller.revealCards("Miracle", new CardsImpl(card), game);
        game.fireEvent(GameEvent.getEvent(GameEvent.EventType.MIRACLE_CARD_REVEALED, card.getId(), this, controller.getId()));
        this.getEffects().clear();
        this.addEffect(new AminatouCastMiracleEffect(reducedText).setTargetPointer(new FixedTarget(card, game)));
        return true;
    }

    @Override
    public String getRule() {
        return "Miracle trigger for enchantment drawn as first card";
    }
}

class AminatouCastMiracleEffect extends OneShotEffect {

    private final ManaCosts<ManaCost> miracleCosts;

    AminatouCastMiracleEffect(String miracleCosts) {
        super(Outcome.Benefit);
        this.miracleCosts = new ManaCostsImpl<>(miracleCosts);
        this.staticText = "you may cast that card for its miracle cost";
    }

    private AminatouCastMiracleEffect(final AminatouCastMiracleEffect effect) {
        super(effect);
        this.miracleCosts = effect.miracleCosts;
    }

    @Override
    public AminatouCastMiracleEffect copy() {
        return new AminatouCastMiracleEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Card card = game.getCard(getTargetPointer().getFirst(game, source));
        if (controller == null || card == null || game.getState().getZone(card.getId()) != Zone.HAND) {
            return false;
        }
        if (!controller.chooseUse(Outcome.PlayForFree, "Cast " + card.getLogName() + " for its miracle cost?", source, game)) {
            return true;
        }
        SpellAbility abilityToCast = card.getSpellAbility().copy();
        ManaCosts<ManaCost> costRef = abilityToCast.getManaCostsToPay();
        costRef.clear();
        costRef.add(miracleCosts);
        controller.cast(abilityToCast, game, false, new ApprovingObject(source, game));
        return true;
    }
}
