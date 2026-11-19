package mage.cards.q;

import java.util.UUID;
import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.AttacksTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.keyword.MenaceAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.choices.ChoiceColor;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.game.Game;
import mage.players.Player;

/**
 *
 * @author muz
 */
public final class QonoSQuartermaster extends CardImpl {

    public QonoSQuartermaster(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{R}");

        this.subtype.add(SubType.KLINGON);
        this.subtype.add(SubType.WARRIOR);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Menace
        this.addAbility(new MenaceAbility());

        // Whenever this creature attacks, add one mana of any color. Until end of turn, you don't lose this mana as steps and phases end.
        this.addAbility(new AttacksTriggeredAbility(new QonoSQuartermasterManaEffect()));
    }

    private QonoSQuartermaster(final QonoSQuartermaster card) {
        super(card);
    }

    @Override
    public QonoSQuartermaster copy() {
        return new QonoSQuartermaster(this);
    }
}

class QonoSQuartermasterManaEffect extends OneShotEffect {

    QonoSQuartermasterManaEffect() {
        super(Outcome.PutManaInPool);
        staticText = "add one mana of any color. Until end of turn, you don't lose this mana as steps and phases end";
    }

    private QonoSQuartermasterManaEffect(final QonoSQuartermasterManaEffect effect) {
        super(effect);
    }

    @Override
    public QonoSQuartermasterManaEffect copy() {
        return new QonoSQuartermasterManaEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        ChoiceColor choice = new ChoiceColor(true);
        if (!controller.choose(outcome, choice, game) || choice.getColor() == null) {
            return false;
        }
        controller.getManaPool().addMana(choice.getMana(1), game, source, true);
        return true;
    }
}
