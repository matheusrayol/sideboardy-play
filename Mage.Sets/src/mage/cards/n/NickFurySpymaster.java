package mage.cards.n;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.GainAbilityTargetEffect;
import mage.abilities.keyword.FirstStrikeAbility;
import mage.abilities.keyword.IndestructibleAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.common.FilterCreatureCard;
import mage.filter.predicate.mageobject.ManaValuePredicate;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetCardInHand;
import mage.target.targetpointer.FixedTarget;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class NickFurySpymaster extends CardImpl {

    public NickFurySpymaster(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{4}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.SPY);
        this.subtype.add(SubType.HERO);
        this.power = new MageInt(4);
        this.toughness = new MageInt(4);

        // First strike
        this.addAbility(FirstStrikeAbility.getInstance());

        // Whenever a creature you control attacks alone, draw a card.
        // Then you may put a creature card with mana value 3 or less from your hand onto the battlefield.
        // It enters tapped and attacking and gains indestructible until end of turn.
        this.addAbility(new NickFuryTriggeredAbility());
    }

    private NickFurySpymaster(final NickFurySpymaster card) {
        super(card);
    }

    @Override
    public NickFurySpymaster copy() {
        return new NickFurySpymaster(this);
    }
}

class NickFuryTriggeredAbility extends TriggeredAbilityImpl {

    NickFuryTriggeredAbility() {
        super(Zone.BATTLEFIELD, new NickFuryEffect(), false);
        setTriggerPhrase("Whenever a creature you control attacks alone, ");
    }

    private NickFuryTriggeredAbility(final NickFuryTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public NickFuryTriggeredAbility copy() {
        return new NickFuryTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DECLARED_ATTACKERS;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!isControlledBy(game.getCombat().getAttackingPlayerId())
                || game.getCombat().getAttackers().size() != 1) {
            return false;
        }
        Permanent attacker = game.getPermanent(game.getCombat().getAttackers().iterator().next());
        return attacker != null && attacker.isControlledBy(getControllerId());
    }
}

class NickFuryEffect extends OneShotEffect {

    private static final FilterCreatureCard filter = new FilterCreatureCard("a creature card with mana value 3 or less");

    static {
        filter.add(new ManaValuePredicate(ComparisonType.FEWER_THAN, 4));
    }

    NickFuryEffect() {
        super(Outcome.PutCreatureInPlay);
        this.staticText = "draw a card. Then you may put a creature card with mana value 3 or less from your hand onto the battlefield. "
                + "It enters tapped and attacking and gains indestructible until end of turn";
    }

    private NickFuryEffect(final NickFuryEffect effect) {
        super(effect);
    }

    @Override
    public NickFuryEffect copy() {
        return new NickFuryEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        controller.drawCards(1, source, game);
        TargetCardInHand target = new TargetCardInHand(0, 1, filter);
        if (!controller.choose(Outcome.PutCreatureInPlay, target, source, game)) {
            return true;
        }
        Card card = game.getCard(target.getFirstTarget());
        if (card == null) {
            return true;
        }
        controller.moveCards(card, Zone.BATTLEFIELD, source, game, true, false, false, null);
        Permanent permanent = CardUtil.getPermanentFromCardPutToBattlefield(card, game);
        if (permanent != null) {
            game.getCombat().addAttackingCreature(permanent.getId(), game);
            game.addEffect(new GainAbilityTargetEffect(IndestructibleAbility.getInstance(), Duration.EndOfTurn)
                    .setTargetPointer(new FixedTarget(permanent, game)), source);
        }
        return true;
    }
}
