package mage.cards.h;

import mage.MageInt;
import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.common.SpellCastControllerTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.mageobject.AnotherPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.TargetPermanent;
import mage.util.functions.CopyApplier;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class HulklingYoungAvenger extends CardImpl {

    static final FilterCreaturePermanent filter = new FilterCreaturePermanent("other creature");

    static {
        filter.add(AnotherPredicate.instance);
    }

    public HulklingYoungAvenger(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{R}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.KREE);
        this.subtype.add(SubType.SKRULL);
        this.subtype.add(SubType.HERO);
        this.power = new MageInt(4);
        this.toughness = new MageInt(4);

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // Whenever you cast a noncreature spell, Hulkling becomes a copy of up to one other target creature until end of turn,
        // except his name is Hulkling, Young Avenger, he's 4/4, and he has flying and this ability.
        Ability ability = new SpellCastControllerTriggeredAbility(
                new HulklingCopyEffect(),
                StaticFilters.FILTER_SPELL_A_NON_CREATURE,
                false
        );
        ability.addTarget(new TargetPermanent(0, 1, filter, false));
        this.addAbility(ability);
    }

    private HulklingYoungAvenger(final HulklingYoungAvenger card) {
        super(card);
    }

    @Override
    public HulklingYoungAvenger copy() {
        return new HulklingYoungAvenger(this);
    }
}

class HulklingCopyEffect extends OneShotEffect {

    HulklingCopyEffect() {
        super(Outcome.Copy);
        this.staticText = "{this} becomes a copy of up to one other target creature until end of turn, "
                + "except his name is Hulkling, Young Avenger, he's 4/4, and he has flying and this ability";
    }

    private HulklingCopyEffect(final HulklingCopyEffect effect) {
        super(effect);
    }

    @Override
    public HulklingCopyEffect copy() {
        return new HulklingCopyEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent sourcePerm = source.getSourcePermanentIfItStillExists(game);
        Permanent targetPerm = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (sourcePerm == null || targetPerm == null) {
            return false;
        }
        game.copyPermanent(Duration.EndOfTurn, targetPerm, sourcePerm.getId(), source, HulklingCopyApplier.instance);
        return true;
    }
}

class HulklingCopyApplier extends CopyApplier {

    static final HulklingCopyApplier instance = new HulklingCopyApplier();

    @Override
    public boolean apply(Game game, MageObject blueprint, Ability source, UUID copyToObjectId) {
        blueprint.setName("Hulkling, Young Avenger");
        blueprint.getPower().setModifiedBaseValue(4);
        blueprint.getToughness().setModifiedBaseValue(4);
        blueprint.getAbilities().add(FlyingAbility.getInstance());
        Ability ability = new SpellCastControllerTriggeredAbility(
                new HulklingCopyEffect(),
                StaticFilters.FILTER_SPELL_A_NON_CREATURE,
                false
        );
        ability.addTarget(new TargetPermanent(0, 1, HulklingYoungAvenger.filter, false));
        blueprint.getAbilities().add(ability);
        return true;
    }
}
