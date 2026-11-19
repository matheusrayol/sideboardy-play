package mage.cards.u;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.AttacksTriggeredAbility;
import mage.abilities.common.delayed.AtTheEndOfCombatDelayedTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.ExileTargetEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.StationAbility;
import mage.abilities.keyword.StationLevelAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.game.permanent.token.TokenImpl;
import mage.target.targetpointer.FixedTargets;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Sideboardy
 */
public final class USSStargazerHereAndThere extends CardImpl {

    public USSStargazerHereAndThere(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{3}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.SPACECRAFT);

        // Station
        this.addAbility(new StationAbility());

        // STATION 6+
        // Flying
        // Picard Maneuver — Whenever U.S.S. Stargazer attacks, create a tapped and attacking 4/3 colorless Spacecraft Illusion artifact creature token with flying. Exile that token at end of combat.
        // 4/3
        Ability attackTrigger = new AttacksTriggeredAbility(new USSStargazerAttackEffect(), false)
                .withFlavorWord("Picard Maneuver");
        this.addAbility(new StationLevelAbility(6)
                .withLevelAbility(FlyingAbility.getInstance())
                .withLevelAbility(attackTrigger)
                .withPT(4, 3));
    }

    private USSStargazerHereAndThere(final USSStargazerHereAndThere card) {
        super(card);
    }

    @Override
    public USSStargazerHereAndThere copy() {
        return new USSStargazerHereAndThere(this);
    }
}

class USSStargazerAttackEffect extends OneShotEffect {

    USSStargazerAttackEffect() {
        super(Outcome.PutCreatureInPlay);
        this.staticText = "create a tapped and attacking 4/3 colorless Spacecraft Illusion artifact creature token with flying. Exile that token at end of combat";
    }

    private USSStargazerAttackEffect(final USSStargazerAttackEffect effect) {
        super(effect);
    }

    @Override
    public USSStargazerAttackEffect copy() {
        return new USSStargazerAttackEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        CreateTokenEffect effect = new CreateTokenEffect(new StargazerIllusionToken(), 1, true, true);
        if (!effect.apply(game, source)) {
            return false;
        }
        List<Permanent> created = effect.getLastAddedTokenIds()
                .stream()
                .map(game::getPermanent)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (!created.isEmpty()) {
            game.addDelayedTriggeredAbility(new AtTheEndOfCombatDelayedTriggeredAbility(
                    new ExileTargetEffect().setTargetPointer(new FixedTargets(created, game))
            ), source);
        }
        return true;
    }
}

class StargazerIllusionToken extends TokenImpl {

    StargazerIllusionToken() {
        super("Spacecraft Illusion Token", "4/3 colorless Spacecraft Illusion artifact creature token with flying");
        cardType.add(CardType.ARTIFACT);
        cardType.add(CardType.CREATURE);
        subtype.add(SubType.SPACECRAFT);
        subtype.add(SubType.ILLUSION);
        power = new MageInt(4);
        toughness = new MageInt(3);

        this.addAbility(FlyingAbility.getInstance());
    }

    private StargazerIllusionToken(final StargazerIllusionToken token) {
        super(token);
    }

    @Override
    public StargazerIllusionToken copy() {
        return new StargazerIllusionToken(this);
    }
}
