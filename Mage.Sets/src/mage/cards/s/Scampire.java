package mage.cards.s;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.ActivateAsSorceryActivatedAbility;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.delayed.AtTheBeginOfNextEndStepDelayedTriggeredAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.ExileTargetEffect;
import mage.abilities.effects.common.continuous.GainAbilityTargetEffect;
import mage.abilities.effects.common.counter.GetTicketCountersControllerEffect;
import mage.abilities.keyword.HasteAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetCardInYourGraveyard;
import mage.target.targetpointer.FixedTarget;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class Scampire extends CardImpl {

    public Scampire(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{B}");

        this.subtype.add(SubType.VAMPIRE);
        this.subtype.add(SubType.EMPLOYEE);
        this.power = new MageInt(1);
        this.toughness = new MageInt(3);

        // When Scampire enters, you get {TK}, then you may put a sticker on a creature card in your graveyard.
        this.addAbility(new EntersBattlefieldTriggeredAbility(
                new GetTicketCountersControllerEffect(1)
                        .setText("you get {TK}, then you may put a sticker on a creature card in your graveyard")
        ));

        // {3}{B}: Return target creature card with a sticker on it from your graveyard to the battlefield. That creature gains haste. Exile it at the beginning of the next end step. Activate only as a sorcery.
        Ability ability = new ActivateAsSorceryActivatedAbility(
                Zone.BATTLEFIELD, new ScampireReturnEffect(), new ManaCostsImpl<>("{3}{B}")
        );
        ability.addTarget(new TargetCardInYourGraveyard(StaticFilters.FILTER_CARD_CREATURE_YOUR_GRAVEYARD));
        this.addAbility(ability);
    }

    private Scampire(final Scampire card) {
        super(card);
    }

    @Override
    public Scampire copy() {
        return new Scampire(this);
    }
}

class ScampireReturnEffect extends OneShotEffect {

    ScampireReturnEffect() {
        super(Outcome.PutCreatureInPlay);
        staticText = "Return target creature card with a sticker on it from your graveyard to the battlefield. "
                + "That creature gains haste. Exile it at the beginning of the next end step";
    }

    private ScampireReturnEffect(final ScampireReturnEffect effect) {
        super(effect);
    }

    @Override
    public ScampireReturnEffect copy() {
        return new ScampireReturnEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Card card = game.getCard(getTargetPointer().getFirst(game, source));
        if (controller == null || card == null) {
            return false;
        }
        if (controller.moveCards(card, Zone.BATTLEFIELD, source, game)) {
            Permanent permanent = game.getPermanent(card.getId());
            if (permanent != null) {
                GainAbilityTargetEffect hasteEffect = new GainAbilityTargetEffect(
                        HasteAbility.getInstance(), Duration.Custom
                );
                hasteEffect.setTargetPointer(new FixedTarget(permanent, game));
                game.addEffect(hasteEffect, source);

                ExileTargetEffect exileEffect = new ExileTargetEffect();
                exileEffect.setTargetPointer(new FixedTarget(permanent, game));
                game.addDelayedTriggeredAbility(
                        new AtTheBeginOfNextEndStepDelayedTriggeredAbility(exileEffect), source
                );
            }
        }
        return true;
    }
}
