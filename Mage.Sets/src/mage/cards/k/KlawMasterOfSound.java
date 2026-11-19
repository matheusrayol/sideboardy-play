package mage.cards.k;

import mage.MageInt;
import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.DealsCombatDamageToAPlayerTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.ExileFaceDownYouMayPlayAsLongAsExiledTargetEffect;
import mage.abilities.effects.common.continuous.GainAbilitySourceEffect;
import mage.abilities.keyword.DeathtouchAbility;
import mage.abilities.keyword.IndestructibleAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.CardsImpl;
import mage.constants.*;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.stack.Spell;
import mage.players.Player;
import mage.target.targetpointer.FixedTarget;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class KlawMasterOfSound extends CardImpl {

    public KlawMasterOfSound(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{B}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.ELEMENTAL);
        this.subtype.add(SubType.ROGUE);
        this.subtype.add(SubType.VILLAIN);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Deathtouch
        this.addAbility(DeathtouchAbility.getInstance());

        // Whenever you play a card from exile, Klaw gains indestructible until end of turn.
        this.addAbility(new KlawPlayedFromExileTriggeredAbility());

        // Whenever Klaw deals combat damage to a player, look at the top card of that player's library, then exile it face down.
        // You may play it for as long as it remains exiled. Mana of any type can be spent to cast a spell this way.
        this.addAbility(new DealsCombatDamageToAPlayerTriggeredAbility(new KlawCombatDamageEffect(), false, true));
    }

    private KlawMasterOfSound(final KlawMasterOfSound card) {
        super(card);
    }

    @Override
    public KlawMasterOfSound copy() {
        return new KlawMasterOfSound(this);
    }
}

class KlawPlayedFromExileTriggeredAbility extends TriggeredAbilityImpl {

    private boolean landFromExile = false;

    KlawPlayedFromExileTriggeredAbility() {
        super(Zone.BATTLEFIELD, new GainAbilitySourceEffect(IndestructibleAbility.getInstance(), Duration.EndOfTurn), false);
        setTriggerPhrase("Whenever you play a card from exile, ");
    }

    private KlawPlayedFromExileTriggeredAbility(final KlawPlayedFromExileTriggeredAbility ability) {
        super(ability);
        this.landFromExile = ability.landFromExile;
    }

    @Override
    public KlawPlayedFromExileTriggeredAbility copy() {
        return new KlawPlayedFromExileTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.SPELL_CAST
                || event.getType() == GameEvent.EventType.PLAY_LAND
                || event.getType() == GameEvent.EventType.LAND_PLAYED;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (event.getType() == GameEvent.EventType.PLAY_LAND) {
            landFromExile = event.getPlayerId().equals(getControllerId())
                    && game.getState().getZone(event.getTargetId()) == Zone.EXILED;
            return false;
        }
        if (event.getType() == GameEvent.EventType.LAND_PLAYED) {
            boolean result = landFromExile && event.getPlayerId().equals(getControllerId());
            landFromExile = false;
            return result;
        }
        if (!event.getPlayerId().equals(getControllerId())) {
            return false;
        }
        Spell spell = game.getSpellOrLKIStack(event.getTargetId());
        return spell != null && spell.getFromZone() == Zone.EXILED;
    }
}

class KlawCombatDamageEffect extends OneShotEffect {

    KlawCombatDamageEffect() {
        super(Outcome.Benefit);
        this.staticText = "look at the top card of that player's library, then exile it face down. "
                + "You may play it for as long as it remains exiled. Mana of any type can be spent to cast a spell this way";
    }

    private KlawCombatDamageEffect(final KlawCombatDamageEffect effect) {
        super(effect);
    }

    @Override
    public KlawCombatDamageEffect copy() {
        return new KlawCombatDamageEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Player damagedPlayer = game.getPlayer(getTargetPointer().getFirst(game, source));
        if (controller == null || damagedPlayer == null) {
            return false;
        }
        Card topCard = damagedPlayer.getLibrary().getFromTop(game);
        if (topCard == null) {
            return true;
        }
        MageObject sourceObject = source.getSourceObject(game);
        String name = sourceObject == null ? "Klaw, Master of Sound" : sourceObject.getIdName();
        controller.lookAtCards(name, new CardsImpl(topCard), game);
        return new ExileFaceDownYouMayPlayAsLongAsExiledTargetEffect(false, CastManaAdjustment.AS_THOUGH_ANY_MANA_TYPE)
                .setTargetPointer(new FixedTarget(topCard, game))
                .apply(game, source);
    }
}
