package mage.cards.m;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.DelayedTriggeredAbility;
import mage.abilities.common.DealsCombatDamageToAPlayerTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CreateTokenCopyTargetEffect;
import mage.abilities.keyword.MenaceAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.Counter;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class MissHighwater extends CardImpl {

    public MissHighwater(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{B}{B}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.DEMON);
        this.subtype.add(SubType.ADVISOR);
        this.power = new MageInt(5);
        this.toughness = new MageInt(3);

        // Menace
        this.addAbility(new MenaceAbility(false));

        // Whenever Miss Highwater deals combat damage to a player who doesn't have a contract counter, they may discard their hand. If they do, they draw seven cards and get a contract counter. For as long as they have a contract counter, when they lose the game, for each artifact and creature they controlled, create a token that's a copy of it.
        this.addAbility(new MissHighwaterTriggeredAbility());
    }

    private MissHighwater(final MissHighwater card) {
        super(card);
    }

    @Override
    public MissHighwater copy() {
        return new MissHighwater(this);
    }
}

class MissHighwaterTriggeredAbility extends DealsCombatDamageToAPlayerTriggeredAbility {

    MissHighwaterTriggeredAbility() {
        super(new MissHighwaterEffect(), false, true);
        setTriggerPhrase("Whenever {this} deals combat damage to a player who doesn't have a contract counter, ");
    }

    private MissHighwaterTriggeredAbility(final MissHighwaterTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public MissHighwaterTriggeredAbility copy() {
        return new MissHighwaterTriggeredAbility(this);
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!super.checkTrigger(event, game)) {
            return false;
        }
        Player damagedPlayer = game.getPlayer(event.getTargetId());
        return damagedPlayer != null && damagedPlayer.getCountersCount("contract") == 0;
    }
}

class MissHighwaterEffect extends OneShotEffect {

    MissHighwaterEffect() {
        super(Outcome.DrawCard);
        this.staticText = "they may discard their hand. If they do, they draw seven cards and get a contract counter. "
                + "For as long as they have a contract counter, when they lose the game, for each artifact and creature they controlled, create a token that's a copy of it";
    }

    private MissHighwaterEffect(final MissHighwaterEffect effect) {
        super(effect);
    }

    @Override
    public MissHighwaterEffect copy() {
        return new MissHighwaterEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player damagedPlayer = game.getPlayer(getTargetPointer().getFirst(game, source));
        if (damagedPlayer == null) {
            return false;
        }
        if (!damagedPlayer.chooseUse(Outcome.DrawCard, "Discard your hand to draw seven cards and get a contract counter?", source, game)) {
            return true;
        }
        damagedPlayer.discard(damagedPlayer.getHand(), false, source, game);
        damagedPlayer.drawCards(7, source, game);
        damagedPlayer.addCounters(new Counter("contract"), source.getControllerId(), source, game);
        game.addDelayedTriggeredAbility(new MissHighwaterLoseDelayedTriggeredAbility(damagedPlayer.getId()), source);
        return true;
    }
}

class MissHighwaterLoseDelayedTriggeredAbility extends DelayedTriggeredAbility {

    private final UUID contractedPlayerId;

    MissHighwaterLoseDelayedTriggeredAbility(UUID contractedPlayerId) {
        super(new MissHighwaterCopyLostEffect(), Duration.EndOfGame, true, false);
        this.contractedPlayerId = contractedPlayerId;
    }

    private MissHighwaterLoseDelayedTriggeredAbility(final MissHighwaterLoseDelayedTriggeredAbility ability) {
        super(ability);
        this.contractedPlayerId = ability.contractedPlayerId;
    }

    @Override
    public MissHighwaterLoseDelayedTriggeredAbility copy() {
        return new MissHighwaterLoseDelayedTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.LOST;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!contractedPlayerId.equals(event.getPlayerId())) {
            return false;
        }
        Player losingPlayer = game.getPlayer(contractedPlayerId);
        if (losingPlayer == null || losingPlayer.getCountersCount("contract") <= 0) {
            return false;
        }
        List<Permanent> snapshots = new ArrayList<>();
        for (Permanent perm : game.getBattlefield().getAllActivePermanents(contractedPlayerId)) {
            if (perm.isArtifact(game) || perm.isCreature(game)) {
                snapshots.add(perm.copy());
            }
        }
        getEffects().setValue("lostPermanents", snapshots);
        return true;
    }

    @Override
    public String getRule() {
        return "For as long as they have a contract counter, when they lose the game, for each artifact and creature they controlled, create a token that's a copy of it.";
    }
}

class MissHighwaterCopyLostEffect extends OneShotEffect {

    MissHighwaterCopyLostEffect() {
        super(Outcome.PutCreatureInPlay);
        this.staticText = "for each artifact and creature they controlled, create a token that's a copy of it";
    }

    private MissHighwaterCopyLostEffect(final MissHighwaterCopyLostEffect effect) {
        super(effect);
    }

    @Override
    public MissHighwaterCopyLostEffect copy() {
        return new MissHighwaterCopyLostEffect(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean apply(Game game, Ability source) {
        Object val = getValue("lostPermanents");
        if (!(val instanceof List)) {
            return false;
        }
        List<Permanent> snapshots = (List<Permanent>) val;
        for (Permanent perm : snapshots) {
            new CreateTokenCopyTargetEffect(source.getControllerId())
                    .setSavedPermanent(perm)
                    .apply(game, source);
        }
        return true;
    }
}
