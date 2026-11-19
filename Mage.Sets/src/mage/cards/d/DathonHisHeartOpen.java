package mage.cards.d;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.ActivateAsSorceryActivatedAbility;
import mage.abilities.common.SpellCastControllerTriggeredAbility;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.DamagePlayersEffect;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.choices.Choice;
import mage.choices.ChoiceImpl;
import mage.constants.*;
import mage.counters.Counter;
import mage.filter.FilterSpell;
import mage.filter.common.FilterControlledPermanent;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.game.stack.Spell;
import mage.players.Player;
import mage.target.common.TargetControlledPermanent;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author sideboardy-play
 */
public final class DathonHisHeartOpen extends CardImpl {

    private static final FilterSpell filter = new FilterSpell("an enchantment spell");
    private static final FilterControlledPermanent filterEnch = new FilterControlledPermanent("enchantment you control");

    static {
        filter.add(CardType.ENCHANTMENT.getPredicate());
        filterEnch.add(CardType.ENCHANTMENT.getPredicate());
    }

    public DathonHisHeartOpen(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{G}{U}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.TAMARIAN);
        this.subtype.add(SubType.OFFICER);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Whenever you cast an enchantment spell, draw a card. If it's a Saga spell, Dathon deals 2 damage to each opponent.
        Ability castTrigger = new SpellCastControllerTriggeredAbility(
                new DrawCardSourceControllerEffect(1),
                filter, false, SetTargetPointer.SPELL
        );
        castTrigger.addEffect(new DathonSagaDamageEffect());
        this.addAbility(castTrigger);

        // {2}: Choose a counter on target enchantment you control. Put another counter of that kind on it. Activate only as a sorcery.
        Ability act = new ActivateAsSorceryActivatedAbility(new DathonCounterEffect(), new GenericManaCost(2));
        act.addTarget(new TargetControlledPermanent(filterEnch));
        this.addAbility(act);
    }

    private DathonHisHeartOpen(final DathonHisHeartOpen card) {
        super(card);
    }

    @Override
    public DathonHisHeartOpen copy() {
        return new DathonHisHeartOpen(this);
    }
}

class DathonSagaDamageEffect extends OneShotEffect {

    DathonSagaDamageEffect() {
        super(Outcome.Damage);
        this.staticText = "If it's a Saga spell, {this} deals 2 damage to each opponent";
    }

    private DathonSagaDamageEffect(final DathonSagaDamageEffect effect) {
        super(effect);
    }

    @Override
    public DathonSagaDamageEffect copy() {
        return new DathonSagaDamageEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Spell spell = game.getSpellOrLKIStack(getTargetPointer().getFirst(game, source));
        if (spell != null && spell.hasSubtype(SubType.SAGA, game)) {
            return new DamagePlayersEffect(2, TargetController.OPPONENT).apply(game, source);
        }
        return false;
    }
}

class DathonCounterEffect extends OneShotEffect {

    DathonCounterEffect() {
        super(Outcome.BoostCreature);
        this.staticText = "Choose a counter on target enchantment you control. Put another counter of that kind on it";
    }

    private DathonCounterEffect(final DathonCounterEffect effect) {
        super(effect);
    }

    @Override
    public DathonCounterEffect copy() {
        return new DathonCounterEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Permanent permanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (controller == null || permanent == null || permanent.getCounters(game).isEmpty()) {
            return false;
        }
        if (permanent.getCounters(game).size() == 1) {
            for (Counter counter : permanent.getCounters(game).values()) {
                permanent.addCounters(new Counter(counter.getName()), source.getControllerId(), source, game);
            }
            return true;
        }
        Choice choice = new ChoiceImpl(true);
        Set<String> choices = new LinkedHashSet<>();
        for (Counter counter : permanent.getCounters(game).values()) {
            choices.add(counter.getName());
        }
        choice.setChoices(choices);
        choice.setMessage("Choose a counter");
        if (controller.choose(Outcome.Benefit, choice, game)) {
            for (Counter counter : permanent.getCounters(game).values()) {
                if (counter.getName().equals(choice.getChoice())) {
                    permanent.addCounters(new Counter(counter.getName()), source.getControllerId(), source, game);
                    return true;
                }
            }
        }
        return false;
    }
}
