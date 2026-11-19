package mage.cards.m;

import mage.MageInt;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.DealsCombatDamageToAPlayerTriggeredAbility;
import mage.abilities.common.EntersBattlefieldThisOrAnotherTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.GainAbilityTargetEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterCard;
import mage.filter.StaticFilters;
import mage.filter.common.FilterInstantOrSorceryCard;
import mage.filter.predicate.mageobject.ManaValuePredicate;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetCardInYourGraveyard;
import mage.target.targetpointer.FixedTarget;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class MassimoTheMagician extends CardImpl {

    private static final FilterCard filter = new FilterInstantOrSorceryCard("instant or sorcery card with mana value 1 from your graveyard");

    static {
        filter.add(new ManaValuePredicate(ComparisonType.EQUAL_TO, 1));
    }

    public MassimoTheMagician(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{U}{R}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.CAT);
        this.subtype.add(SubType.WIZARD);
        this.power = new MageInt(3);
        this.toughness = new MageInt(2);

        // Whenever Massimo or another creature you control enters, exile up to one target instant or sorcery card with mana value 1 from your graveyard. If a card is exiled this way, that creature gains "Whenever this creature deals combat damage to a player, copy the exiled card. You may cast the copy without paying its mana cost."
        Ability ability = new MassimoTheMagicianTriggeredAbility();
        ability.addTarget(new TargetCardInYourGraveyard(0, 1, filter));
        this.addAbility(ability);
    }

    private MassimoTheMagician(final MassimoTheMagician card) {
        super(card);
    }

    @Override
    public MassimoTheMagician copy() {
        return new MassimoTheMagician(this);
    }
}

class MassimoTheMagicianTriggeredAbility extends EntersBattlefieldThisOrAnotherTriggeredAbility {

    MassimoTheMagicianTriggeredAbility() {
        super(new MassimoTheMagicianEffect(), StaticFilters.FILTER_PERMANENT_CREATURE, false, SetTargetPointer.NONE, true);
    }

    private MassimoTheMagicianTriggeredAbility(final MassimoTheMagicianTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public MassimoTheMagicianTriggeredAbility copy() {
        return new MassimoTheMagicianTriggeredAbility(this);
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!super.checkTrigger(event, game)) {
            return false;
        }
        getEffects().setValue("enteringCreatureId", event.getTargetId());
        return true;
    }
}

class MassimoTheMagicianEffect extends OneShotEffect {

    MassimoTheMagicianEffect() {
        super(Outcome.PlayForFree);
        this.staticText = "exile up to one target instant or sorcery card with mana value 1 from your graveyard. "
                + "If a card is exiled this way, that creature gains \"Whenever this creature deals combat damage to a player, "
                + "copy the exiled card. You may cast the copy without paying its mana cost.\"";
    }

    private MassimoTheMagicianEffect(final MassimoTheMagicianEffect effect) {
        super(effect);
    }

    @Override
    public MassimoTheMagicianEffect copy() {
        return new MassimoTheMagicianEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Card card = game.getCard(getTargetPointer().getFirst(game, source));
        if (controller == null || card == null) {
            return false;
        }
        if (!controller.moveCards(card, Zone.EXILED, source, game)) {
            return false;
        }
        Card exiledCard = game.getCard(card.getId());
        if (exiledCard == null || game.getState().getZone(exiledCard.getId()) != Zone.EXILED) {
            return false;
        }
        UUID enteringId = (UUID) getValue("enteringCreatureId");
        Permanent creature = enteringId != null ? game.getPermanent(enteringId) : null;
        if (creature != null) {
            Ability gainedAbility = new DealsCombatDamageToAPlayerTriggeredAbility(
                    new MassimoCopyCastEffect(new MageObjectReference(exiledCard, game)), false
            );
            game.addEffect(
                    new GainAbilityTargetEffect(gainedAbility, Duration.Custom)
                            .setTargetPointer(new FixedTarget(creature, game)),
                    source
            );
        }
        return true;
    }
}

class MassimoCopyCastEffect extends OneShotEffect {

    private final MageObjectReference exiledCardMor;

    MassimoCopyCastEffect(MageObjectReference exiledCardMor) {
        super(Outcome.PlayForFree);
        this.exiledCardMor = exiledCardMor;
        this.staticText = "copy the exiled card. You may cast the copy without paying its mana cost";
    }

    private MassimoCopyCastEffect(final MassimoCopyCastEffect effect) {
        super(effect);
        this.exiledCardMor = effect.exiledCardMor;
    }

    @Override
    public MassimoCopyCastEffect copy() {
        return new MassimoCopyCastEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Card exiledCard = exiledCardMor.getCard(game);
        if (controller == null || exiledCard == null || game.getState().getZone(exiledCard.getId()) != Zone.EXILED) {
            return false;
        }
        Card copiedCard = game.copyCard(exiledCard, source, controller.getId());
        if (copiedCard == null) {
            return false;
        }
        CardUtil.castSpellWithAttributesForFree(controller, source, game, copiedCard);
        return true;
    }
}
