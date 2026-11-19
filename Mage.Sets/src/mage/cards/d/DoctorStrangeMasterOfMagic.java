package mage.cards.d;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.ActivateIfConditionActivatedAbility;
import mage.abilities.condition.Condition;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.ProwessAbility;
import mage.abilities.keyword.VigilanceAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.watchers.common.SpellsCastWatcher;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class DoctorStrangeMasterOfMagic extends CardImpl {

    public DoctorStrangeMasterOfMagic(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{W}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.SORCERER);
        this.subtype.add(SubType.HERO);
        this.power = new MageInt(2);
        this.toughness = new MageInt(3);

        // Flying, vigilance, prowess
        this.addAbility(FlyingAbility.getInstance());
        this.addAbility(VigilanceAbility.getInstance());
        this.addAbility(new ProwessAbility());

        // {T}: Draw a card. Activate only if you've cast a noncreature spell this turn.
        this.addAbility(new ActivateIfConditionActivatedAbility(
                new DrawCardSourceControllerEffect(1),
                new TapSourceCost(),
                DoctorStrangeCondition.instance
        ));
    }

    private DoctorStrangeMasterOfMagic(final DoctorStrangeMasterOfMagic card) {
        super(card);
    }

    @Override
    public DoctorStrangeMasterOfMagic copy() {
        return new DoctorStrangeMasterOfMagic(this);
    }
}

enum DoctorStrangeCondition implements Condition {
    instance;

    @Override
    public boolean apply(Game game, Ability source) {
        SpellsCastWatcher watcher = game.getState().getWatcher(SpellsCastWatcher.class);
        return watcher != null
                && watcher.getSpellsCastThisTurn(source.getControllerId())
                .stream()
                .anyMatch(spell -> StaticFilters.FILTER_SPELL_NON_CREATURE.match(spell, game));
    }

    @Override
    public String toString() {
        return "you've cast a noncreature spell this turn";
    }
}
