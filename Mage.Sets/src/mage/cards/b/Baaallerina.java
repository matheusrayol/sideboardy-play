package mage.cards.b;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.GainAbilityTargetEffect;
import mage.abilities.keyword.FlyingAbility;
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
public final class Baaallerina extends CardImpl {

    public Baaallerina(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{U}");

        this.subtype.add(SubType.SHEEP);
        this.subtype.add(SubType.PERFORMER);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // When this creature enters, you may put a name sticker on a nonland permanent you own.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new BaaallerinaStickerEffect()));

        // {2}{U}: Another target creature with a name sticker on it gains flying until end of turn.
        Ability ability = new SimpleActivatedAbility(
                new GainAbilityTargetEffect(FlyingAbility.getInstance(), Duration.EndOfTurn)
                        .setText("Another target creature with a name sticker on it gains flying until end of turn"),
                new ManaCostsImpl<>("{2}{U}")
        );
        ability.addTarget(new TargetPermanent(StaticFilters.FILTER_ANOTHER_CREATURE));
        this.addAbility(ability);
    }

    private Baaallerina(final Baaallerina card) {
        super(card);
    }

    @Override
    public Baaallerina copy() {
        return new Baaallerina(this);
    }
}

class BaaallerinaStickerEffect extends OneShotEffect {

    BaaallerinaStickerEffect() {
        super(Outcome.BoostCreature);
        staticText = "you may put a name sticker on a nonland permanent you own";
    }

    private BaaallerinaStickerEffect(final BaaallerinaStickerEffect effect) {
        super(effect);
    }

    @Override
    public BaaallerinaStickerEffect copy() {
        return new BaaallerinaStickerEffect(this);
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
