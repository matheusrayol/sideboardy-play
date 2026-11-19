package mage.cards.m;

import mage.abilities.Ability;
import mage.abilities.common.CantBeBeamedUpAbility;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.AttachEffect;
import mage.abilities.effects.common.DoIfCostPaid;
import mage.abilities.effects.common.DontUntapInControllersUntapStepEnchantedEffect;
import mage.abilities.effects.common.continuous.GainAbilityAttachedEffect;
import mage.abilities.keyword.EnchantAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.AttachmentType;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.filter.FilterPermanent;
import mage.filter.predicate.Predicates;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.TargetPermanent;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class Marooned extends CardImpl {

    private static final FilterPermanent filter = new FilterPermanent("artifact or creature");

    static {
        filter.add(Predicates.or(
                CardType.ARTIFACT.getPredicate(),
                CardType.CREATURE.getPredicate()
        ));
    }

    public Marooned(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{U}");

        this.subtype.add(SubType.AURA);

        // Enchant artifact or creature
        TargetPermanent auraTarget = new TargetPermanent(filter);
        this.getSpellAbility().addTarget(auraTarget);
        this.getSpellAbility().addEffect(new AttachEffect(Outcome.Detriment));
        this.addAbility(new EnchantAbility(auraTarget));

        // When this Aura enters, you may pay {3}. If you do, tap enchanted permanent.
        this.addAbility(new EntersBattlefieldTriggeredAbility(
                new DoIfCostPaid(new MaroonedTapEffect(), new GenericManaCost(3))
        ));

        // Enchanted permanent doesn't untap during its controller's untap step and can't be beamed up.
        this.addAbility(new SimpleStaticAbility(
                new DontUntapInControllersUntapStepEnchantedEffect("permanent")
                        .setText("enchanted permanent doesn't untap during its controller's untap step and can't be beamed up")
        ));
        Ability beamAbility = new SimpleStaticAbility(
                new GainAbilityAttachedEffect(CantBeBeamedUpAbility.getInstance(), AttachmentType.AURA)
        );
        beamAbility.setRuleVisible(false);
        this.addAbility(beamAbility);
    }

    private Marooned(final Marooned card) {
        super(card);
    }

    @Override
    public Marooned copy() {
        return new Marooned(this);
    }
}

class MaroonedTapEffect extends OneShotEffect {

    MaroonedTapEffect() {
        super(Outcome.Tap);
        this.staticText = "tap enchanted permanent";
    }

    private MaroonedTapEffect(final MaroonedTapEffect effect) {
        super(effect);
    }

    @Override
    public MaroonedTapEffect copy() {
        return new MaroonedTapEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent aura = source.getSourcePermanentOrLKI(game);
        if (aura == null || aura.getAttachedTo() == null) {
            return false;
        }
        Permanent enchanted = game.getPermanent(aura.getAttachedTo());
        if (enchanted == null) {
            return false;
        }
        return enchanted.tap(source, game);
    }
}
