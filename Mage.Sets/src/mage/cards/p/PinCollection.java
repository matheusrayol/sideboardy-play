package mage.cards.p;

import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.BoostEquippedEffect;
import mage.abilities.keyword.EquipAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.counters.Counter;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class PinCollection extends CardImpl {

    public PinCollection(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{X}{W}");

        this.subtype.add(SubType.EQUIPMENT);

        // When this Equipment enters, you may put an ability sticker with ticket cost X or less on it without paying that sticker's ticket cost.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new PinCollectionEffect(), true));

        // Equipped creature gets +1/+1 and has all abilities of ability stickers on this Equipment.
        this.addAbility(new SimpleStaticAbility(new BoostEquippedEffect(1, 1)
                .setText("Equipped creature gets +1/+1 and has all abilities of ability stickers on this Equipment")));

        // Equip {2}
        this.addAbility(new EquipAbility(2));
    }

    private PinCollection(final PinCollection card) {
        super(card);
    }

    @Override
    public PinCollection copy() {
        return new PinCollection(this);
    }
}

class PinCollectionEffect extends OneShotEffect {

    PinCollectionEffect() {
        super(Outcome.BoostCreature);
        staticText = "put an ability sticker with ticket cost X or less on {this} without paying that sticker's ticket cost";
    }

    private PinCollectionEffect(final PinCollectionEffect effect) {
        super(effect);
    }

    @Override
    public PinCollectionEffect copy() {
        return new PinCollectionEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = source.getSourcePermanentIfItStillExists(game);
        if (permanent != null) {
            permanent.addCounters(new Counter("ability sticker"), source.getControllerId(), source, game);
        }
        return true;
    }
}
