package mage.cards.p;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.BoostTargetEffect;
import mage.abilities.effects.common.continuous.GainAbilityTargetEffect;
import mage.abilities.keyword.MenaceAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.counters.Counter;
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
public final class ProficientPyrodancer extends CardImpl {

    public ProficientPyrodancer(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{R}");

        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.PERFORMER);
        this.power = new MageInt(2);
        this.toughness = new MageInt(3);

        // When this creature enters, you may put an art sticker on a nonland permanent you own.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new ProficientPyrodancerStickerEffect()));

        // {2}{R}: Another target creature with an art sticker on it gets +2/+0 and gains menace until end of turn.
        Ability ability = new SimpleActivatedAbility(
                new BoostTargetEffect(2, 0, Duration.EndOfTurn)
                        .setText("Another target creature with an art sticker on it gets +2/+0"),
                new ManaCostsImpl<>("{2}{R}")
        );
        ability.addEffect(new GainAbilityTargetEffect(new MenaceAbility(false), Duration.EndOfTurn)
                .setText("and gains menace until end of turn"));
        ability.addTarget(new TargetPermanent(StaticFilters.FILTER_ANOTHER_CREATURE));
        this.addAbility(ability);
    }

    private ProficientPyrodancer(final ProficientPyrodancer card) {
        super(card);
    }

    @Override
    public ProficientPyrodancer copy() {
        return new ProficientPyrodancer(this);
    }
}

class ProficientPyrodancerStickerEffect extends OneShotEffect {

    ProficientPyrodancerStickerEffect() {
        super(Outcome.BoostCreature);
        staticText = "you may put an art sticker on a nonland permanent you own";
    }

    private ProficientPyrodancerStickerEffect(final ProficientPyrodancerStickerEffect effect) {
        super(effect);
    }

    @Override
    public ProficientPyrodancerStickerEffect copy() {
        return new ProficientPyrodancerStickerEffect(this);
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
                permanent.addCounters(new Counter("art sticker"), source.getControllerId(), source, game);
            }
        }
        return true;
    }
}
