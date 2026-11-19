package mage.cards.g;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.Mode;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.AsEntersBattlefieldAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.GainLifeEffect;
import mage.abilities.effects.common.LoseLifeOpponentsEffect;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.abilities.hint.Hint;
import mage.abilities.hint.common.ModesAlreadyUsedHint;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.stack.Spell;
import mage.players.Player;

import java.util.UUID;

/**
 * @author sideboardy-play
 */
public final class GollumRiddleMaster extends CardImpl {

    static final String INFO_KEY_SUFFIX = "_gollumOddEven";

    public GollumRiddleMaster(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{B}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HALFLING);
        this.subtype.add(SubType.HORROR);
        this.power = new MageInt(3);
        this.toughness = new MageInt(1);

        // As Gollum enters, choose odd or even. (Zero is even.)
        this.addAbility(new AsEntersBattlefieldAbility(new GollumRiddleMasterChooseEffect()));

        // Whenever an opponent casts a spell with mana value of the chosen quality, choose one that hasn't been chosen —
        // • Put a +1/+1 counter on Gollum.
        // • Each opponent loses 2 life and you gain 2 life.
        // • Draw a card.
        this.addAbility(new GollumRiddleMasterTriggeredAbility());
    }

    private GollumRiddleMaster(final GollumRiddleMaster card) {
        super(card);
    }

    @Override
    public GollumRiddleMaster copy() {
        return new GollumRiddleMaster(this);
    }
}

class GollumRiddleMasterChooseEffect extends OneShotEffect {

    GollumRiddleMasterChooseEffect() {
        super(Outcome.Neutral);
        this.staticText = "choose odd or even. <i>(Zero is even.)</i>";
    }

    private GollumRiddleMasterChooseEffect(final GollumRiddleMasterChooseEffect effect) {
        super(effect);
    }

    @Override
    public GollumRiddleMasterChooseEffect copy() {
        return new GollumRiddleMasterChooseEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        boolean chooseOdd = controller.chooseUse(
                outcome, "Choose odd or even", null,
                "Odd", "Even", source, game
        );
        game.getState().setValue(source.getSourceId().toString() + GollumRiddleMaster.INFO_KEY_SUFFIX, chooseOdd);
        game.informPlayers(controller.getLogName() + " chose " + (chooseOdd ? "odd" : "even"));
        return true;
    }
}

class GollumRiddleMasterTriggeredAbility extends TriggeredAbilityImpl {

    GollumRiddleMasterTriggeredAbility() {
        super(Zone.BATTLEFIELD, new AddCountersSourceEffect(CounterType.P1P1.createInstance()), false);
        this.getModes().getMode().setModeTag("+1/+1 counter");
        this.getModes().setLimitUsageByOnce(false);

        Mode mode2 = new Mode(new LoseLifeOpponentsEffect(2));
        mode2.addEffect(new GainLifeEffect(2).concatBy("and"));
        mode2.setModeTag("drain 2");
        this.addMode(mode2);

        Mode mode3 = new Mode(new DrawCardSourceControllerEffect(1));
        mode3.setModeTag("draw card");
        this.addMode(mode3);

        this.addHint(ModesAlreadyUsedHint.instance);
        this.addHint(GollumRiddleMasterHint.instance);
    }

    private GollumRiddleMasterTriggeredAbility(final GollumRiddleMasterTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public GollumRiddleMasterTriggeredAbility copy() {
        return new GollumRiddleMasterTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.SPELL_CAST;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!game.getOpponents(getControllerId()).contains(event.getPlayerId())) {
            return false;
        }
        Object chosen = game.getState().getValue(getSourceId().toString() + GollumRiddleMaster.INFO_KEY_SUFFIX);
        if (!(chosen instanceof Boolean)) {
            return false;
        }
        boolean chooseOdd = (Boolean) chosen;
        Spell spell = game.getStack().getSpell(event.getTargetId());
        if (spell == null) {
            return false;
        }
        int mv = spell.getManaValue();
        return chooseOdd ? (mv % 2 != 0) : (mv % 2 == 0);
    }

    @Override
    public String getRule() {
        return "Whenever an opponent casts a spell with mana value of the chosen quality, "
                + "choose one that hasn't been chosen &mdash; "
                + "&bull; Put a +1/+1 counter on {this}. "
                + "&bull; Each opponent loses 2 life and you gain 2 life. "
                + "&bull; Draw a card.";
    }
}

enum GollumRiddleMasterHint implements Hint {
    instance;

    @Override
    public String getText(Game game, Ability ability) {
        Object chosen = game.getState().getValue(ability.getSourceId().toString() + GollumRiddleMaster.INFO_KEY_SUFFIX);
        if (chosen instanceof Boolean) {
            return "Chosen quality: " + (((Boolean) chosen) ? "Odd" : "Even");
        }
        return "Chosen quality: none";
    }

    @Override
    public Hint copy() {
        return this;
    }
}
