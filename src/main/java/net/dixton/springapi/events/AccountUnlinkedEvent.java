package net.dixton.springapi.events;

import lombok.Getter;
import lombok.ToString;
import net.dixton.model.account.AccountLink;
import org.springframework.context.ApplicationEvent;

@Getter
@ToString
public class AccountUnlinkedEvent extends ApplicationEvent {

    private final AccountLink accountLink;

    public AccountUnlinkedEvent(Object source, AccountLink accountLink) {
        super(source);
        this.accountLink = accountLink;
    }
}