package mage.cards.s;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.CrewTriplePowerAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.events.GameEvent;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class SuluHeroicHelmsman extends CardImpl {

    public SuluHeroicHelmsman(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.PILOT);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Sulu stations Spacecraft and crews Vehicles as though his power were tripled.
        this.addAbility(new SimpleStaticAbility(new SuluStationReplacementEffect()));
        Ability crewAbility = new CrewTriplePowerAbility();
        crewAbility.setRuleVisible(false);
        this.addAbility(crewAbility);
    }

    private SuluHeroicHelmsman(final SuluHeroicHelmsman card) {
        super(card);
    }

    @Override
    public SuluHeroicHelmsman copy() {
        return new SuluHeroicHelmsman(this);
    }
}

class SuluStationReplacementEffect extends ReplacementEffectImpl {

    SuluStationReplacementEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Benefit);
        this.staticText = "{this} stations Spacecraft and crews Vehicles as though his power were tripled";
    }

    private SuluStationReplacementEffect(final SuluStationReplacementEffect effect) {
        super(effect);
    }

    @Override
    public SuluStationReplacementEffect copy() {
        return new SuluStationReplacementEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.STATION_PERMANENT;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        return source.getSourceId().equals(event.getTargetId());
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        long tripled = Math.min((long) event.getAmount() * 3L, Integer.MAX_VALUE);
        event.setAmount((int) tripled);
        return false;
    }
}
