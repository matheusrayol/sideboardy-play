package mage.cards.p;

import mage.MageInt;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.DealsCombatDamageToAPlayerTriggeredAbility;
import mage.abilities.common.delayed.AtTheBeginOfYourNextUpkeepDelayedTriggeredAbility;
import mage.abilities.effects.ContinuousEffect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.replacement.ThatSpellGraveyardExileReplacementEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.HasteAbility;
import mage.abilities.keyword.TrampleAbility;
import mage.abilities.keyword.VigilanceAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.players.Player;
import mage.target.common.TargetCardInYourGraveyard;
import mage.target.targetpointer.FixedTarget;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class PowerPack extends CardImpl {

    public PowerPack(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{R}{G}{W}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.HERO);
        this.power = new MageInt(4);
        this.toughness = new MageInt(4);

        // Flying, vigilance, trample, haste
        this.addAbility(FlyingAbility.getInstance());
        this.addAbility(VigilanceAbility.getInstance());
        this.addAbility(TrampleAbility.getInstance());
        this.addAbility(HasteAbility.getInstance());

        // Whenever Power Pack deals combat damage to a player, exile target instant or sorcery card from your graveyard chosen at random.
        // At the beginning of your next upkeep, you may cast that card without paying its mana cost.
        // If that spell would be put into your graveyard, exile it instead.
        Ability ability = new DealsCombatDamageToAPlayerTriggeredAbility(new PowerPackEffect(), false);
        TargetCardInYourGraveyard target = new TargetCardInYourGraveyard(
                StaticFilters.FILTER_CARD_INSTANT_OR_SORCERY_FROM_YOUR_GRAVEYARD
        );
        target.setRandom(true);
        ability.addTarget(target);
        this.addAbility(ability);
    }

    private PowerPack(final PowerPack card) {
        super(card);
    }

    @Override
    public PowerPack copy() {
        return new PowerPack(this);
    }
}

class PowerPackEffect extends OneShotEffect {

    PowerPackEffect() {
        super(Outcome.PlayForFree);
        this.staticText = "exile target instant or sorcery card from your graveyard chosen at random. "
                + "At the beginning of your next upkeep, you may cast that card without paying its mana cost. "
                + ThatSpellGraveyardExileReplacementEffect.RULE_YOUR;
    }

    private PowerPackEffect(final PowerPackEffect effect) {
        super(effect);
    }

    @Override
    public PowerPackEffect copy() {
        return new PowerPackEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Card card = game.getCard(getTargetPointer().getFirst(game, source));
        if (controller == null || card == null) {
            return false;
        }
        controller.moveCards(card, Zone.EXILED, source, game);
        if (game.getState().getZone(card.getId()) == Zone.EXILED) {
            MageObjectReference mor = new MageObjectReference(card, game);
            game.addDelayedTriggeredAbility(
                    new AtTheBeginOfYourNextUpkeepDelayedTriggeredAbility(new PowerPackCastEffect(mor)),
                    source
            );
        }
        return true;
    }
}

class PowerPackCastEffect extends OneShotEffect {

    private final MageObjectReference cardRef;

    PowerPackCastEffect(MageObjectReference cardRef) {
        super(Outcome.PlayForFree);
        this.cardRef = cardRef;
        this.staticText = "you may cast that card without paying its mana cost. "
                + ThatSpellGraveyardExileReplacementEffect.RULE_YOUR;
    }

    private PowerPackCastEffect(final PowerPackCastEffect effect) {
        super(effect);
        this.cardRef = effect.cardRef;
    }

    @Override
    public PowerPackCastEffect copy() {
        return new PowerPackCastEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Card card = cardRef.getCard(game);
        if (controller == null || card == null || game.getState().getZone(card.getId()) != Zone.EXILED) {
            return false;
        }
        if (!controller.chooseUse(Outcome.PlayForFree, "Cast " + card.getLogName() + " without paying its mana cost?", source, game)) {
            return true;
        }
        if (CardUtil.castSpellWithAttributesForFree(controller, source, game, card)) {
            ContinuousEffect effect = new ThatSpellGraveyardExileReplacementEffect(true);
            effect.setTargetPointer(new FixedTarget(card, game));
            game.addEffect(effect, source);
        }
        return true;
    }
}
