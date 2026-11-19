package mage.cards.m;

import mage.ApprovingObject;
import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.DealsCombatDamageToAPlayerTriggeredAbility;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.events.ZoneChangeEvent;
import mage.game.permanent.token.TokenImpl;
import mage.players.Player;
import mage.target.common.TargetCardInYourGraveyard;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class MagarOfTheMagicStrings extends CardImpl {

    public MagarOfTheMagicStrings(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{B}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.MINOTAUR);
        this.subtype.add(SubType.PERFORMER);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // {1}{B}{R}: Note the name of target instant or sorcery card in your graveyard and put it onto the battlefield face down. It's a 3/3 creature with "Whenever this creature deals combat damage to a player, you may create a copy of the card with the noted name. You may cast the copy without paying its mana cost" and "If this creature would leave the battlefield, exile it instead of putting it anywhere else."
        Ability ability = new SimpleActivatedAbility(
                new MagarOfTheMagicStringsEffect(), new ManaCostsImpl<>("{1}{B}{R}")
        );
        ability.addTarget(new TargetCardInYourGraveyard(StaticFilters.FILTER_CARD_INSTANT_OR_SORCERY));
        this.addAbility(ability);
    }

    private MagarOfTheMagicStrings(final MagarOfTheMagicStrings card) {
        super(card);
    }

    @Override
    public MagarOfTheMagicStrings copy() {
        return new MagarOfTheMagicStrings(this);
    }
}

class MagarOfTheMagicStringsEffect extends OneShotEffect {

    MagarOfTheMagicStringsEffect() {
        super(Outcome.PutCreatureInPlay);
        staticText = "Note the name of target instant or sorcery card in your graveyard "
                + "and put it onto the battlefield face down. It's a 3/3 creature with "
                + "\"Whenever this creature deals combat damage to a player, you may create a copy of the card with the noted name. "
                + "You may cast the copy without paying its mana cost\" and "
                + "\"If this creature would leave the battlefield, exile it instead of putting it anywhere else.\"";
    }

    private MagarOfTheMagicStringsEffect(final MagarOfTheMagicStringsEffect effect) {
        super(effect);
    }

    @Override
    public MagarOfTheMagicStringsEffect copy() {
        return new MagarOfTheMagicStringsEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Card targetCard = game.getCard(getTargetPointer().getFirst(game, source));
        if (controller == null || targetCard == null) {
            return false;
        }
        String notedName = targetCard.getName();
        UUID notedCardId = targetCard.getId();
        controller.moveCards(targetCard, Zone.EXILED, source, game);
        return new MagarPuppetToken(notedCardId, notedName).putOntoBattlefield(1, game, source);
    }
}

class MagarPuppetToken extends TokenImpl {

    MagarPuppetToken(UUID notedCardId, String notedName) {
        super("Face-down Creature", "3/3 creature");
        cardType.add(CardType.CREATURE);
        power = new MageInt(3);
        toughness = new MageInt(3);
        this.addAbility(new DealsCombatDamageToAPlayerTriggeredAbility(
                new MagarPuppetCombatEffect(notedCardId, notedName), true
        ));
        this.addAbility(new SimpleStaticAbility(new MagarPuppetLeaveReplacementEffect()));
    }

    private MagarPuppetToken(final MagarPuppetToken token) {
        super(token);
    }

    @Override
    public MagarPuppetToken copy() {
        return new MagarPuppetToken(this);
    }
}

class MagarPuppetCombatEffect extends OneShotEffect {

    private final UUID notedCardId;

    MagarPuppetCombatEffect(UUID notedCardId, String notedName) {
        super(Outcome.PlayForFree);
        this.notedCardId = notedCardId;
        staticText = "create a copy of " + notedName + ". You may cast the copy without paying its mana cost";
    }

    private MagarPuppetCombatEffect(final MagarPuppetCombatEffect effect) {
        super(effect);
        this.notedCardId = effect.notedCardId;
    }

    @Override
    public MagarPuppetCombatEffect copy() {
        return new MagarPuppetCombatEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Card notedCard = game.getCard(notedCardId);
        if (controller == null || notedCard == null || notedCard.getSpellAbility() == null) {
            return false;
        }
        Card copiedCard = game.copyCard(notedCard, source, controller.getId());
        game.getState().setValue("PlayFromNotOwnHandZone" + copiedCard.getId(), Boolean.TRUE);
        controller.cast(
                controller.chooseAbilityForCast(copiedCard, game, true),
                game, true, new ApprovingObject(source, game)
        );
        game.getState().setValue("PlayFromNotOwnHandZone" + copiedCard.getId(), null);
        return true;
    }
}

class MagarPuppetLeaveReplacementEffect extends ReplacementEffectImpl {

    MagarPuppetLeaveReplacementEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Exile);
        staticText = "If this creature would leave the battlefield, exile it instead of putting it anywhere else";
    }

    private MagarPuppetLeaveReplacementEffect(final MagarPuppetLeaveReplacementEffect effect) {
        super(effect);
    }

    @Override
    public MagarPuppetLeaveReplacementEffect copy() {
        return new MagarPuppetLeaveReplacementEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ZONE_CHANGE;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        ZoneChangeEvent zEvent = (ZoneChangeEvent) event;
        return zEvent.getFromZone() == Zone.BATTLEFIELD
                && zEvent.getToZone() != Zone.EXILED
                && event.getTargetId().equals(source.getSourceId());
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        ((ZoneChangeEvent) event).setToZone(Zone.EXILED);
        return false;
    }
}
