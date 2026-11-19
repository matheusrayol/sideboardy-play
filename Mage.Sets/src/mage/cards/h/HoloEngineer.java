package mage.cards.h;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.events.CreateTokenEvent;
import mage.game.events.GameEvent;
import mage.game.permanent.token.IllusionArtifactToken;
import mage.game.permanent.token.Token;

import java.util.Map;
import java.util.UUID;

/**
 * @author sideboardy-play
 */
public final class HoloEngineer extends CardImpl {

    public HoloEngineer(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{G}{G}");

        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.ARTIFICER);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // When this creature enters, create a 3/3 colorless Illusion artifact creature token.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new CreateTokenEffect(new IllusionArtifactToken())));

        // If one or more tokens would be created under your control, that many 3/3 colorless Illusion artifact creature tokens are created instead.
        this.addAbility(new SimpleStaticAbility(new HoloEngineerReplacementEffect()));
    }

    private HoloEngineer(final HoloEngineer card) {
        super(card);
    }

    @Override
    public HoloEngineer copy() {
        return new HoloEngineer(this);
    }
}

class HoloEngineerReplacementEffect extends ReplacementEffectImpl {

    HoloEngineerReplacementEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Benefit);
        this.staticText = "If one or more tokens would be created under your control, "
                + "that many 3/3 colorless Illusion artifact creature tokens are created instead";
    }

    private HoloEngineerReplacementEffect(final HoloEngineerReplacementEffect effect) {
        super(effect);
    }

    @Override
    public HoloEngineerReplacementEffect copy() {
        return new HoloEngineerReplacementEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.CREATE_TOKEN;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        return source.isControlledBy(event.getPlayerId()) && event.getAmount() > 0;
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        if (event instanceof CreateTokenEvent) {
            Map<Token, Integer> tokens = ((CreateTokenEvent) event).getTokens();
            int amount = event.getAmount();
            tokens.clear();
            tokens.put(new IllusionArtifactToken(), amount);
        }
        return false;
    }
}
