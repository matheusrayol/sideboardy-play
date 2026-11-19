package mage.cards.c;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.AttacksEachCombatStaticAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.RequirementEffect;
import mage.abilities.effects.common.InfoEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class CogworkTracker extends CardImpl {

    public CogworkTracker(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{4}");

        this.subtype.add(SubType.DOG);
        this.subtype.add(SubType.CONSTRUCT);
        this.power = new MageInt(4);
        this.toughness = new MageInt(4);

        // Reveal Cogwork Tracker as you draft it and note the player who passed it to you.
        this.addAbility(new SimpleStaticAbility(
                Zone.ALL,
                new InfoEffect("Reveal {this} as you draft it and note the player who passed it to you - not implemented.")
        ));

        // Cogwork Tracker attacks each combat if able.
        this.addAbility(new AttacksEachCombatStaticAbility());

        // Cogwork Tracker attacks a player you noted for cards named Cogwork Tracker each combat if able.
        this.addAbility(new SimpleStaticAbility(new CogworkTrackerEffect()));
    }

    private CogworkTracker(final CogworkTracker card) {
        super(card);
    }

    @Override
    public CogworkTracker copy() {
        return new CogworkTracker(this);
    }
}

class CogworkTrackerEffect extends RequirementEffect {

    CogworkTrackerEffect() {
        super(Duration.WhileOnBattlefield);
        staticText = "{this} attacks a player you noted for cards named {this} each combat if able";
    }

    private CogworkTrackerEffect(final CogworkTrackerEffect effect) {
        super(effect);
    }

    @Override
    public CogworkTrackerEffect copy() {
        return new CogworkTrackerEffect(this);
    }

    @Override
    public boolean applies(Permanent permanent, Ability source, Game game) {
        return permanent.getId().equals(source.getSourceId())
                && game.getState().getValue(source.getControllerId() + "_cogwork_tracker_player") instanceof UUID;
    }

    @Override
    public boolean mustAttack(Game game) {
        return true;
    }

    @Override
    public boolean mustBlock(Game game) {
        return false;
    }

    @Override
    public UUID mustAttackDefender(Ability source, Game game) {
        Object val = game.getState().getValue(source.getControllerId() + "_cogwork_tracker_player");
        return val instanceof UUID ? (UUID) val : null;
    }
}
