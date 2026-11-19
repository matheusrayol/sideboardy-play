package mage.cards.v;

import mage.MageInt;
import mage.abilities.common.SpellCastControllerTriggeredAbility;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.keyword.DefenderAbility;
import mage.abilities.keyword.TeamworkAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.token.Robot11HeroToken;
import mage.game.stack.Spell;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class VirtualAssistant extends CardImpl {

    public VirtualAssistant(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{1}{U}");

        this.subtype.add(SubType.ILLUSION);
        this.subtype.add(SubType.ADVISOR);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Defender
        this.addAbility(DefenderAbility.getInstance());

        // Whenever you cast a spell using teamwork, create a 1/1 colorless Robot Hero artifact creature token with flying.
        this.addAbility(new VirtualAssistantTriggeredAbility());
    }

    private VirtualAssistant(final VirtualAssistant card) {
        super(card);
    }

    @Override
    public VirtualAssistant copy() {
        return new VirtualAssistant(this);
    }
}

class VirtualAssistantTriggeredAbility extends SpellCastControllerTriggeredAbility {

    VirtualAssistantTriggeredAbility() {
        super(new CreateTokenEffect(new Robot11HeroToken()), false);
    }

    private VirtualAssistantTriggeredAbility(final VirtualAssistantTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public VirtualAssistantTriggeredAbility copy() {
        return new VirtualAssistantTriggeredAbility(this);
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!super.checkTrigger(event, game)) {
            return false;
        }
        Spell spell = (Spell) getEffects().get(0).getValue("spellCast");
        return spell != null
                && CardUtil.checkSourceCostsTagExists(game, spell.getSpellAbility(), TeamworkAbility.TEAMWORK_ACTIVATION_VALUE_KEY);
    }

    @Override
    public String getRule() {
        return "Whenever you cast a spell using teamwork, create a 1/1 colorless Robot Hero artifact creature token with flying.";
    }
}
