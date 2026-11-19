package mage.cards.g;

import java.util.UUID;
import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.AttacksTriggeredAbility;
import mage.abilities.effects.ContinuousEffect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.SetBasePowerToughnessTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.TargetPermanent;
import mage.target.targetpointer.FixedTarget;

/**
 *
 * @author muz
 */
public final class GalionElvenkingsButler extends CardImpl {

    public GalionElvenkingsButler(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{G}{G}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.ELF);
        this.subtype.add(SubType.ADVISOR);
        this.power = new MageInt(4);
        this.toughness = new MageInt(4);

        // Whenever Galion attacks, choose up to one other target creature you control. Its base power and toughness become equal to Galion's power and toughness until end of turn.
        Ability ability = new AttacksTriggeredAbility(new GalionElvenkingsButlerEffect(), false);
        ability.addTarget(new TargetPermanent(0, 1, StaticFilters.FILTER_CONTROLLED_ANOTHER_CREATURE, false));
        this.addAbility(ability);
    }

    private GalionElvenkingsButler(final GalionElvenkingsButler card) {
        super(card);
    }

    @Override
    public GalionElvenkingsButler copy() {
        return new GalionElvenkingsButler(this);
    }
}

class GalionElvenkingsButlerEffect extends OneShotEffect {

    GalionElvenkingsButlerEffect() {
        super(Outcome.BoostCreature);
        staticText = "choose up to one other target creature you control. Its base power and toughness become equal to {this}'s power and toughness until end of turn";
    }

    private GalionElvenkingsButlerEffect(final GalionElvenkingsButlerEffect effect) {
        super(effect);
    }

    @Override
    public GalionElvenkingsButlerEffect copy() {
        return new GalionElvenkingsButlerEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent sourcePermanent = source.getSourcePermanentOrLKI(game);
        Permanent targetPermanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (sourcePermanent == null || targetPermanent == null) {
            return false;
        }
        ContinuousEffect effect = new SetBasePowerToughnessTargetEffect(
                sourcePermanent.getPower().getValue(),
                sourcePermanent.getToughness().getValue(),
                Duration.EndOfTurn
        );
        effect.setTargetPointer(new FixedTarget(targetPermanent, game));
        game.addEffect(effect, source);
        return true;
    }
}
