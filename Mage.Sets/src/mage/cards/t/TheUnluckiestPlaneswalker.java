package mage.cards.t;

import mage.abilities.Ability;
import mage.abilities.LoyaltyAbility;
import mage.abilities.common.CanBeYourCommanderAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.common.delayed.ManaSpentDelayedTriggeredAbility;
import mage.abilities.dynamicvalue.common.PermanentsOnBattlefieldCount;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CreateDelayedTriggeredAbilityEffect;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.InfoEffect;
import mage.abilities.effects.common.discard.DiscardHandControllerEffect;
import mage.abilities.effects.mana.AddManaOfAnyColorEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterSpell;
import mage.filter.common.FilterControlledPermanent;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.game.permanent.token.Gremlin11Token;
import mage.players.Player;
import mage.target.common.TargetOpponent;

import java.util.ArrayList;
import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class TheUnluckiestPlaneswalker extends CardImpl {

    private static final FilterSpell filterAura = new FilterSpell("an Aura spell");

    static {
        filterAura.add(SubType.AURA.getPredicate());
    }

    public TheUnluckiestPlaneswalker(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.PLANESWALKER}, "{2}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.setStartingLoyalty(4);

        // Rulebreaker — A deck with this commander can have Aura cards of any color identity and any basic land cards.
        this.addAbility(new SimpleStaticAbility(
                Zone.ALL,
                new InfoEffect("Rulebreaker &mdash; A deck with this commander can have Aura cards of any color identity and any basic land cards")
        ));

        // +1: Add one mana of any color. When you spend this mana to cast an Aura spell this turn, create a 1/1 red Gremlin creature token.
        LoyaltyAbility ability1 = new LoyaltyAbility(new AddManaOfAnyColorEffect(), 1);
        ability1.addEffect(new CreateDelayedTriggeredAbilityEffect(
                new ManaSpentDelayedTriggeredAbility(new CreateTokenEffect(new Gremlin11Token()), filterAura)
        ));
        this.addAbility(ability1);

        // −3: Discard your hand, then draw cards equal to twice the number of Auras you control. For each Aura attached to you, attach it to an opponent of your choice.
        LoyaltyAbility ability2 = new LoyaltyAbility(new DiscardHandControllerEffect(), -3);
        ability2.addEffect(new DrawCardSourceControllerEffect(
                new PermanentsOnBattlefieldCount(new FilterControlledPermanent(SubType.AURA, "Auras you control"), 2)
        ).setText(", then draw cards equal to twice the number of Auras you control"));
        ability2.addEffect(new TheUnluckiestPlaneswalkerAttachEffect());
        this.addAbility(ability2);

        // The Unluckiest Planeswalker can be your commander.
        this.addAbility(CanBeYourCommanderAbility.getInstance());
    }

    private TheUnluckiestPlaneswalker(final TheUnluckiestPlaneswalker card) {
        super(card);
    }

    @Override
    public TheUnluckiestPlaneswalker copy() {
        return new TheUnluckiestPlaneswalker(this);
    }
}

class TheUnluckiestPlaneswalkerAttachEffect extends OneShotEffect {

    TheUnluckiestPlaneswalkerAttachEffect() {
        super(Outcome.Benefit);
        this.staticText = "For each Aura attached to you, attach it to an opponent of your choice";
    }

    private TheUnluckiestPlaneswalkerAttachEffect(final TheUnluckiestPlaneswalkerAttachEffect effect) {
        super(effect);
    }

    @Override
    public TheUnluckiestPlaneswalkerAttachEffect copy() {
        return new TheUnluckiestPlaneswalkerAttachEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        for (UUID attachmentId : new ArrayList<>(controller.getAttachments())) {
            Permanent aura = game.getPermanent(attachmentId);
            if (aura == null || !aura.hasSubtype(SubType.AURA, game)) {
                continue;
            }
            TargetOpponent targetOpponent = new TargetOpponent(true);
            targetOpponent.withChooseHint("to attach " + aura.getName() + " to");
            if (controller.choose(Outcome.Detriment, targetOpponent, source, game)) {
                Player opponent = game.getPlayer(targetOpponent.getFirstTarget());
                if (opponent != null && !opponent.hasLeft()) {
                    opponent.addAttachment(aura.getId(), source, game);
                }
            }
        }
        return true;
    }
}
