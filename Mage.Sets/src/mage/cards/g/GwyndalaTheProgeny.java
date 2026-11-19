package mage.cards.g;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.BoostEquippedEffect;
import mage.abilities.keyword.EquipAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterControlledPermanent;
import mage.filter.predicate.permanent.TokenPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.game.permanent.token.Token;
import mage.game.permanent.token.TokenImpl;
import mage.target.TargetPermanent;
import mage.util.functions.EmptyCopyApplier;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class GwyndalaTheProgeny extends CardImpl {

    private static final FilterControlledPermanent filter =
            new FilterControlledPermanent(SubType.EQUIPMENT, "Equipment you control");

    public GwyndalaTheProgeny(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.VAU_NAKAT);
        this.subtype.add(SubType.ADVISOR);
        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // When Gwyndala enters, create a colorless Equipment artifact token named Neuroflux Heirloom with "Equipped creature gets +1/+1" and equip {2}. Attach it to Gwyndala.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new GwyndalaCreateTokenEffect()));

        // {2}: Each Equipment token you control becomes a copy of target Equipment you control until end of turn.
        Ability ability = new SimpleActivatedAbility(new GwyndalaCopyEffect(), new GenericManaCost(2));
        ability.addTarget(new TargetPermanent(filter));
        this.addAbility(ability);
    }

    private GwyndalaTheProgeny(final GwyndalaTheProgeny card) {
        super(card);
    }

    @Override
    public GwyndalaTheProgeny copy() {
        return new GwyndalaTheProgeny(this);
    }
}

class GwyndalaCreateTokenEffect extends OneShotEffect {

    GwyndalaCreateTokenEffect() {
        super(Outcome.Benefit);
        this.staticText = "create a colorless Equipment artifact token named Neuroflux Heirloom "
                + "with \"Equipped creature gets +1/+1\" and equip {2}. Attach it to {this}";
    }

    private GwyndalaCreateTokenEffect(final GwyndalaCreateTokenEffect effect) {
        super(effect);
    }

    @Override
    public GwyndalaCreateTokenEffect copy() {
        return new GwyndalaCreateTokenEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Token token = new NeurofluxHeirloomToken();
        if (!token.putOntoBattlefield(1, game, source)) {
            return false;
        }
        Permanent gwyndala = source.getSourcePermanentIfItStillExists(game);
        if (gwyndala != null) {
            for (UUID tokenId : token.getLastAddedTokenIds()) {
                gwyndala.addAttachment(tokenId, source, game);
            }
        }
        return true;
    }
}

class GwyndalaCopyEffect extends OneShotEffect {

    private static final FilterPermanent tokenFilter =
            new FilterControlledPermanent(SubType.EQUIPMENT, "Equipment token you control");

    static {
        tokenFilter.add(TokenPredicate.TRUE);
    }

    GwyndalaCopyEffect() {
        super(Outcome.Copy);
        this.staticText = "each Equipment token you control becomes a copy of target Equipment you control until end of turn";
    }

    private GwyndalaCopyEffect(final GwyndalaCopyEffect effect) {
        super(effect);
    }

    @Override
    public GwyndalaCopyEffect copy() {
        return new GwyndalaCopyEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent target = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (target == null) {
            return false;
        }
        for (Permanent token : game.getBattlefield().getAllActivePermanents(
                tokenFilter, source.getControllerId(), game
        )) {
            if (!token.getId().equals(target.getId())) {
                game.copyPermanent(Duration.EndOfTurn, target, token.getId(), source, new EmptyCopyApplier());
            }
        }
        return true;
    }
}

class NeurofluxHeirloomToken extends TokenImpl {

    NeurofluxHeirloomToken() {
        super("Neuroflux Heirloom", "colorless Equipment artifact token named Neuroflux Heirloom with \"Equipped creature gets +1/+1\" and equip {2}");
        cardType.add(CardType.ARTIFACT);
        subtype.add(SubType.EQUIPMENT);

        this.addAbility(new SimpleStaticAbility(new BoostEquippedEffect(1, 1)));
        this.addAbility(new EquipAbility(2));
    }

    private NeurofluxHeirloomToken(final NeurofluxHeirloomToken token) {
        super(token);
    }

    @Override
    public NeurofluxHeirloomToken copy() {
        return new NeurofluxHeirloomToken(this);
    }
}
