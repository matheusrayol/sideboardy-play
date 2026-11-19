package mage.cards.s;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.ActivateAsSorceryActivatedAbility;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.counter.AddCountersTargetEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.VigilanceAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.counters.Counter;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.TargetPermanent;
import mage.target.common.TargetControlledPermanent;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class SwordSwallowingSeraph extends CardImpl {

    public SwordSwallowingSeraph(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{4}{W}{W}");

        this.subtype.add(SubType.ANGEL);
        this.subtype.add(SubType.PERFORMER);
        this.power = new MageInt(4);
        this.toughness = new MageInt(4);

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // Vigilance
        this.addAbility(VigilanceAbility.getInstance());

        // When this creature enters, you may put a name sticker on a nonland permanent you own.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new SwordSwallowingSeraphStickerEffect()));

        // {1}{W}, {T}: Put a +1/+1 counter on another target creature with a name sticker on it. Activate only as a sorcery.
        Ability ability = new ActivateAsSorceryActivatedAbility(
                Zone.BATTLEFIELD,
                new AddCountersTargetEffect(CounterType.P1P1.createInstance())
                        .setText("Put a +1/+1 counter on another target creature with a name sticker on it"),
                new ManaCostsImpl<>("{1}{W}")
        );
        ability.addCost(new TapSourceCost());
        ability.addTarget(new TargetPermanent(StaticFilters.FILTER_ANOTHER_CREATURE));
        this.addAbility(ability);
    }

    private SwordSwallowingSeraph(final SwordSwallowingSeraph card) {
        super(card);
    }

    @Override
    public SwordSwallowingSeraph copy() {
        return new SwordSwallowingSeraph(this);
    }
}

class SwordSwallowingSeraphStickerEffect extends OneShotEffect {

    SwordSwallowingSeraphStickerEffect() {
        super(Outcome.BoostCreature);
        staticText = "you may put a name sticker on a nonland permanent you own";
    }

    private SwordSwallowingSeraphStickerEffect(final SwordSwallowingSeraphStickerEffect effect) {
        super(effect);
    }

    @Override
    public SwordSwallowingSeraphStickerEffect copy() {
        return new SwordSwallowingSeraphStickerEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        TargetControlledPermanent target = new TargetControlledPermanent(
                0, 1, StaticFilters.FILTER_CONTROLLED_PERMANENT_NON_LAND, true
        );
        if (controller.choose(Outcome.Benefit, target, source, game)) {
            Permanent permanent = game.getPermanent(target.getFirstTarget());
            if (permanent != null) {
                permanent.addCounters(new Counter("name sticker"), source.getControllerId(), source, game);
            }
        }
        return true;
    }
}
