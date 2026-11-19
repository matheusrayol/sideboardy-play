package mage.cards.a;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.InfoEffect;
import mage.abilities.keyword.DeathtouchAbility;
import mage.abilities.keyword.DoubleStrikeAbility;
import mage.abilities.keyword.FirstStrikeAbility;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.HasteAbility;
import mage.abilities.keyword.HexproofAbility;
import mage.abilities.keyword.IndestructibleAbility;
import mage.abilities.keyword.LifelinkAbility;
import mage.abilities.keyword.MenaceAbility;
import mage.abilities.keyword.ReachAbility;
import mage.abilities.keyword.VigilanceAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Layer;
import mage.constants.Outcome;
import mage.constants.SubLayer;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.Collection;
import java.util.Locale;
import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class AnimusOfPredation extends CardImpl {

    public AnimusOfPredation(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{4}{G}");

        this.subtype.add(SubType.AVATAR);
        this.power = new MageInt(4);
        this.toughness = new MageInt(4);

        // Draft Animus of Predation face up.
        this.addAbility(new SimpleStaticAbility(
                Zone.ALL,
                new InfoEffect("Draft {this} face up - not implemented.")
        ));

        // As you draft a card, you may remove it from the draft face up.
        this.addAbility(new SimpleStaticAbility(
                Zone.ALL,
                new InfoEffect("As you draft a card, you may remove it from the draft face up - not implemented.")
        ));

        // If you removed a creature card with flying from the draft with cards named Animus of Predation, this creature has flying. The same is true for first strike, double strike, deathtouch, haste, hexproof, indestructible, lifelink, menace, reach, and vigilance.
        this.addAbility(new SimpleStaticAbility(new AnimusOfPredationEffect()));
    }

    private AnimusOfPredation(final AnimusOfPredation card) {
        super(card);
    }

    @Override
    public AnimusOfPredation copy() {
        return new AnimusOfPredation(this);
    }
}

class AnimusOfPredationEffect extends ContinuousEffectImpl {

    AnimusOfPredationEffect() {
        super(Duration.WhileOnBattlefield, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        staticText = "If you removed a creature card with flying from the draft with cards named {this}, {this} has flying. "
                + "The same is true for first strike, double strike, deathtouch, haste, hexproof, indestructible, lifelink, menace, reach, and vigilance";
    }

    private AnimusOfPredationEffect(final AnimusOfPredationEffect effect) {
        super(effect);
    }

    @Override
    public AnimusOfPredationEffect copy() {
        return new AnimusOfPredationEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = game.getPermanent(source.getSourceId());
        if (permanent == null) {
            return false;
        }
        Object raw = game.getState().getValue(source.getControllerId() + "_animus_of_predation_abilities");
        if (!(raw instanceof Collection<?>)) {
            return false;
        }
        for (Object item : (Collection<?>) raw) {
            if (!(item instanceof String)) {
                continue;
            }
            Ability granted = abilityForKeyword(((String) item).toLowerCase(Locale.ENGLISH));
            if (granted != null) {
                permanent.addAbility(granted, source.getSourceId(), game);
            }
        }
        return true;
    }

    private static Ability abilityForKeyword(String kw) {
        switch (kw) {
            case "flying":
                return FlyingAbility.getInstance();
            case "first strike":
                return FirstStrikeAbility.getInstance();
            case "double strike":
                return DoubleStrikeAbility.getInstance();
            case "deathtouch":
                return DeathtouchAbility.getInstance();
            case "haste":
                return HasteAbility.getInstance();
            case "hexproof":
                return HexproofAbility.getInstance();
            case "indestructible":
                return IndestructibleAbility.getInstance();
            case "lifelink":
                return LifelinkAbility.getInstance();
            case "menace":
                return new MenaceAbility();
            case "reach":
                return ReachAbility.getInstance();
            case "vigilance":
                return VigilanceAbility.getInstance();
            default:
                return null;
        }
    }
}
