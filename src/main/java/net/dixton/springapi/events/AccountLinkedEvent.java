package net.dixton.springapi.events;

import lombok.Getter;
import lombok.ToString;
import net.dixton.model.account.AccountLink;
import org.springframework.context.ApplicationEvent;

@Getter
@ToString
public class AccountLinkedEvent extends ApplicationEvent {

    private final AccountLink accountLink;

    public AccountLinkedEvent(Object source, AccountLink accountLink) {
        super(source);
        this.accountLink = accountLink;
    }
}