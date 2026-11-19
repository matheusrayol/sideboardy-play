package mage.cards.s;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.Condition;
import mage.abilities.decorator.ConditionalContinuousEffect;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.continuous.GainAbilitySourceEffect;
import mage.abilities.keyword.IndestructibleAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.RollDieType;
import mage.constants.SubType;
import mage.constants.WatcherScope;
import mage.game.Game;
import mage.game.events.DieRolledEvent;
import mage.game.events.GameEvent;
import mage.game.permanent.token.TokenImpl;
import mage.watchers.Watcher;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class SoulSwindler extends CardImpl {

    public SoulSwindler(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{4}{B}");

        this.subtype.add(SubType.DEMON);
        this.subtype.add(SubType.EMPLOYEE);
        this.power = new MageInt(5);
        this.toughness = new MageInt(3);

        // As long as you've visited an Attraction this turn, this creature has indestructible.
        this.addAbility(new SimpleStaticAbility(new ConditionalContinuousEffect(
                new GainAbilitySourceEffect(IndestructibleAbility.getInstance(), Duration.WhileOnBattlefield),
                SoulSwindlerCondition.instance,
                "As long as you've visited an Attraction this turn, {this} has indestructible"
        )), new SoulSwindlerWatcher());

        // When this creature enters, open an Attraction.
        this.addAbility(new EntersBattlefieldTriggeredAbility(
                new CreateTokenEffect(new SoulSwindlerAttractionToken())
                        .setText("open an Attraction. (Put the top card of your Attraction deck onto the battlefield.)")
        ));
    }

    private SoulSwindler(final SoulSwindler card) {
        super(card);
    }

    @Override
    public SoulSwindler copy() {
        return new SoulSwindler(this);
    }
}

class SoulSwindlerWatcher extends Watcher {

    private final Set<UUID> rolledPlayers = new HashSet<>();

    SoulSwindlerWatcher() {
        super(WatcherScope.GAME);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() == GameEvent.EventType.DIE_ROLLED) {
            DieRolledEvent drEvent = (DieRolledEvent) event;
            if (drEvent.getRollDieType() == RollDieType.NUMERICAL) {
                rolledPlayers.add(event.getTargetId());
            }
        }
    }

    @Override
    public void reset() {
        super.reset();
        rolledPlayers.clear();
    }

    boolean rolledThisTurn(UUID playerId) {
        return rolledPlayers.contains(playerId);
    }
}

enum SoulSwindlerCondition implements Condition {
    instance;

    @Override
    public boolean apply(Game game, Ability source) {
        SoulSwindlerWatcher watcher = game.getState().getWatcher(SoulSwindlerWatcher.class);
        return watcher != null && watcher.rolledThisTurn(source.getControllerId());
    }
}

class SoulSwindlerAttractionToken extends TokenImpl {

    SoulSwindlerAttractionToken() {
        super("Attraction", "Attraction artifact token");
        cardType.add(CardType.ARTIFACT);
        subtype.add(SubType.ATTRACTION);
    }

    private SoulSwindlerAttractionToken(final SoulSwindlerAttractionToken token) {
        super(token);
    }

    @Override
    public SoulSwindlerAttractionToken copy() {
        return new SoulSwindlerAttractionToken(this);
    }
}
